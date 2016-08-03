package org.store.core.front;

import com.opensymphony.xwork2.util.logging.Logger;
import com.opensymphony.xwork2.util.logging.LoggerFactory;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.lucene.analysis.WordlistLoader;
import org.apache.struts2.util.TokenHelper;
import org.hibernate.Session;
import org.store.core.beans.*;
import org.store.core.beans.mail.MProduct;
import org.store.core.beans.mail.MReview;
import org.store.core.beans.utils.*;
import org.store.core.dto.CategoryDTO;
import org.store.core.globals.ImageResolver;
import org.store.core.globals.ImageResolverImpl;
import org.store.core.globals.SomeUtils;
import org.store.core.globals.StoreSessionInterceptor;
import org.store.core.hibernate.HibernateSessionFactory;
import org.store.core.mail.MailSenderThreat;
import org.store.core.search.LuceneSearcher;
import org.store.core.utils.events.EventService;
import org.store.core.utils.events.EventUtils;
import org.store.core.utils.merchants.MerchantService;
import org.store.core.utils.merchants.MerchantUtils;
import org.store.core.utils.merchants.PaymentResult;
import org.store.sslplugin.annotation.Secured;
import org.store.sslplugin.annotation.Unsecured;

import javax.servlet.http.Cookie;
import java.io.*;
import java.util.*;

public class GeneralAction extends FrontModuleAction {

    private static final Logger LOG = LoggerFactory.getLogger(GeneralAction.class);


    @Override
    public void prepare() throws Exception {
        super.prepare();
        staticText = (StaticText) dao.get(StaticText.class, idStaticText);
        billingAddress = (UserAddress) dao.get(UserAddress.class, idBilling);
        shippingAddress = (UserAddress) dao.get(UserAddress.class, idShipping);
    }

    @Unsecured
    public String home() throws Exception {
        return SUCCESS;
    }

    @Unsecured
    public String error() throws Exception {
        return ERROR;
    }

    public String banner() throws Exception {
        banner = (Banner) dao.get(Banner.class, idBanner);
        if (banner != null) {
            banner.addHit();
            dao.save(banner);
        }
        return SUCCESS;
    }

    @Unsecured
    public String staticPage() throws Exception {
        StaticText bean = dao.getStaticText(code);
        if (bean == null) bean = (StaticText) dao.get(StaticText.class, idStaticText);
        if (bean != null) {
            addToStack("staticText", bean);
            getBreadCrumbs().add(new BreadCrumb("page", bean.getTitle(getLocale().getLanguage()), null, null));
            EventUtils.executeEvent(getServletContext(), EventService.EVENT_VISIT_STATIC_TEXT, this);
            return SUCCESS;
        } else {
            addSessionError(getText(CNT_ERROR_PAGE_NOT_FOUND));
            return "home";
        }
    }

    @Unsecured
    public String news() throws Exception {

        StaticText n = (idNews != null) ? (StaticText) dao.get(StaticText.class, idNews) : null;

        if (n != null) {
            addToStack("news", n);
            getBreadCrumbs().add(new BreadCrumb("news", getText("news"), url("news"), null));
            getBreadCrumbs().add(new BreadCrumb("news", n.getTitle(getLocale().getLanguage()), null, null));
            return "news";
        } else {
            DataNavigator news = new DataNavigator(getRequest(), "news");
            news.setListado(dao.getNews(news));
            addToStack("news", news);

            getBreadCrumbs().add(new BreadCrumb("news", getText("news"), null, null));
            EventUtils.executeEvent(getServletContext(), EventService.EVENT_VISIT_STATIC_TEXT, this);
            return SUCCESS;
        }
    }

    @Unsecured
    public String category() throws Exception {
        // Busqueda de productos
        Integer defaultNumberOfItems = null;
        Map extras = new HashMap();
        if (productFilter == null) productFilter = new ProductFilter();
        category = dao.getCategory(code);
        if (category == null) category = (Category) dao.get(Category.class, idCategory);
        if (category != null) {
            if (!category.isActive() || "_base".equalsIgnoreCase(category.getUrlCode())) return "home";

            getStoreSessionObjects().put(StoreSessionInterceptor.CNT_LAST_CATEGORY, category.getIdCategory());
            productFilter.setFilterCategories(category.getIdCategory().toString());
            defaultNumberOfItems = category.getNumItems();
        }
        if (defaultNumberOfItems == null) {
            defaultNumberOfItems = SomeUtils.strToInteger(getStoreProperty(StoreProperty.PROP_CATEGORY_PAGE_ITEMS, StoreProperty.PROP_DEFAULT_CATEGORY_PAGE_ITEMS));
        }

        List<Integer> pageItemsOptions = new ArrayList<Integer>();
        String[] arrOptions = getStoreProperty(StoreProperty.PROP_CATEGORY_PAGE_ITEMS_OPTIONS, StoreProperty.PROP_DEFAULT_CATEGORY_PAGE_ITEMS_OPTIONS).split(",");
        for (String s : arrOptions) {
            int n = NumberUtils.toInt(s.trim(), 0);
            if (n > 0) pageItemsOptions.add(n);
        }
        addToStack("pageItemsOptions", pageItemsOptions);

        Manufacturer beanMan = dao.getManufacturer(manufacturer);
        if (beanMan != null) productFilter.setFilterManufacturer(beanMan.getIdManufacturer().toString());

        ProductLabel beanLab = dao.getProductLabelByCode(label);
        if (beanLab != null) productFilter.setFilterLabel(beanLab.getCode());

        // If user connected filter by user level
        if (getFrontUserLevel() != null)
            productFilter.setFilterUserLevel(getFrontUserLevel().getId().toString());

        // Define default sorted method
        String[] sortOptions = getProductSortOptions().split(",");
        addToStack("sortOptions", sortOptions);
        if (StringUtils.isEmpty(productFilter.getSorted()) || !ArrayUtils.contains(sortOptions, productFilter.getSorted())) {
            productFilter.setSorted(getProductSortDefaultOption());
        }

        LOG.debug("query 1");
        getRequest().setAttribute("products.pagerows", defaultNumberOfItems);

        // buscar ultima pagina de la categoria vista
        if (request.getCookies() != null && category != null) {
            String cookieName = "lastpage_" + category.getIdCategory().toString();
            for (Cookie c : getRequest().getCookies()) {
                if (cookieName.equalsIgnoreCase(c.getName())) {
                    Integer lastPage = SomeUtils.strToInteger(c.getValue());
                    if (lastPage != null && lastPage > 0) getRequest().setAttribute("products.currentpage", lastPage);
                }
            }
        }

        DataNavigator products = new DataNavigator(getRequest(), "products");
        products.setListado(dao.listFrontProducts(products, productFilter, extras, getLocale().getLanguage(), null));
        addToStack("products", products);
        getResponse().addCookie(products.getPageRowCookie());
        addToStack("extras", extras);
        LOG.debug("query 2");

        Map numProducts = (Map) extras.get("CATEGORY");

        if (category != null) {
            String canCompare = dao.getParentPropertyValue(category, "compare");
            if ("Y".equalsIgnoreCase(canCompare)) addToStack("canCompare", Boolean.TRUE);

            // put cookie with last page
            String cookieName = "lastpage_" + category.getIdCategory().toString();
            Cookie cookie = new Cookie(cookieName, String.valueOf(products.getCurrentPage()));
            cookie.setPath("/");
            getResponse().addCookie(cookie);
            addToStack("lastPage_cookieName", cookieName);

            // Productos Especiales
            String oldFilterLabel = productFilter.getFilterLabel();
            String[] arrLabels = {ProductLabel.LABEL_SPECIAL, ProductLabel.LABEL_HOT};
            for (String label : arrLabels) {
                productFilter.setFilterLabel(label);
                addToStack("products_" + label, dao.listFrontProducts(null, productFilter, null, getLocale().getLanguage(), 30));
            }
            productFilter.setFilterLabel(oldFilterLabel);

            // categorias hijas
            List<CategoryDTO> subcategories = dao.getSubcategories(category.getIdCategory(), getLocale().getLanguage(), numProducts, true);
            CollectionUtils.filter(subcategories, new Predicate() {
                public boolean evaluate(Object o) {
                    return (o instanceof CategoryDTO) && ((CategoryDTO) o).getNumProducts() > 0;
                }
            });
            addToStack("categoriesChildren", subcategories);
            addToStack("numProducts", numProducts);

            // breadcrumb
            if (category.getIdParent() != null)
                for (Category c : getCategoryHierarchy(category.getIdParent()))
                    if (!"_base".equalsIgnoreCase(c.getUrlCode()))
                        getBreadCrumbs().add(new BreadCrumb("category", c.getCategoryName(getLocale().getLanguage()), urlCategory(c), null));
            getBreadCrumbs().add(new BreadCrumb("category", category.getCategoryName(getLocale().getLanguage()), null, null));

            // attributes
            List<CategoryProperty> catProps = dao.getParentCategoryProperties(category, true, true);
            if (catProps != null && !catProps.isEmpty()) {
                addToStack("attributes", catProps);
            }

        }
        List<BreadCrumb> filtersBreadcrumb = new ArrayList<BreadCrumb>();
        if (StringUtils.isNotEmpty(productFilter.getFilterManufacturer())) {
            Manufacturer m = getManufacturer(SomeUtils.strToLong(productFilter.getFilterManufacturer()));
            if (m != null) {
                if (getBreadCrumbs().isEmpty()) getBreadCrumbs().add(new BreadCrumb("manufacturer", m.getManufacturerName(), null, null));
                else filtersBreadcrumb.add(new BreadCrumb("manufacturer", m.getManufacturerName(), null, m.getIdManufacturer().toString()));
            }
        }
        if (productFilter.hasPriceFilter()) {
            Double min = SomeUtils.strToDouble(productFilter.getFilterMinPrice());
            Double max = SomeUtils.strToDouble(productFilter.getFilterMaxPrice());
            String cad = null;
            if (min != null) {
                if (max != null) {
                    cad = formatActualCurrency(min, getDefaultCurrency()) + " - " + formatActualCurrency(max, getDefaultCurrency());
                } else {
                    cad = getText("over", "over") + " " + formatActualCurrency(min, getDefaultCurrency());
                }
            } else if (max != null) {
                cad = getText("under", "under") + " " + formatActualCurrency(max, getDefaultCurrency());
            }
            if (StringUtils.isNotEmpty(cad)) {
                if (getBreadCrumbs().isEmpty()) getBreadCrumbs().add(new BreadCrumb("price", cad, null, null));
                else filtersBreadcrumb.add(new BreadCrumb("price", cad, null, cad));
            }
        }
        if (StringUtils.isNotEmpty(productFilter.getFilterLabel())) {
            ProductLabel l = getLabel(productFilter.getFilterLabel());
            if (l != null) {
                if (getBreadCrumbs().isEmpty()) getBreadCrumbs().add(new BreadCrumb("label", l.getName(getLocale().getLanguage()), null, null));
                else filtersBreadcrumb.add(new BreadCrumb("label", l.getName(getLocale().getLanguage()), null, l.getId().toString()));
            }
        }
        Map<Long, String> attMap = productFilter.getFilterAttributesMap();
        if (!attMap.isEmpty()) {
            for (Long idAtt : attMap.keySet()) {
                AttributeProd ap = (AttributeProd) dao.get(AttributeProd.class, idAtt);
                if (ap != null && StringUtils.isNotEmpty(ap.getAttributeName(getLocale().getLanguage())) && StringUtils.isNotEmpty(attMap.get(idAtt))) {
                    filtersBreadcrumb.add(new BreadCrumb("attribute", ap.getAttributeName(getLocale().getLanguage()) + " = " + attMap.get(idAtt), null, idAtt.toString()));
                }
            }
        }
        addToStack("filtersBreadcrumb", filtersBreadcrumb);
        // if (productFilter!=null) getStoreSessionObjects().put(CNT_LAST_PRODUCT_FILTER, productFilter);
        EventUtils.executeEvent(getServletContext(), EventService.EVENT_VISIT_CATEGORY, this);

        String mode = getCookie("category.navigator.view");
        if (StringUtils.isEmpty(mode)) mode = getStoreProperty(StoreProperty.PROP_LISTING_STYLE, StoreProperty.PROP_DEFAULT_LISTING_STYLE);
        addToStack("navView", mode);

        LOG.debug("query 3");
        return ("ajax".equalsIgnoreCase(output)) ? "ajax" : SUCCESS;
    }

    @Unsecured
    public String search() throws Exception {
        if (StringUtils.isEmpty(query) && StringUtils.isNotEmpty(request.getParameter("q")))
            query = request.getParameter("q");
        if (StringUtils.isNotEmpty(query)) {
            // buscar primero si es un partNumber
            product = dao.getProductByPartNumber(query);
            if (product != null) return "product";
            product = dao.getProductByMfgPartNumber(query);
            if (product != null) return "product";

            // sino
            getRequest().setAttribute("products.pagerows", SomeUtils.strToInteger(getStoreProperty(StoreProperty.PROP_CATEGORY_PAGE_ITEMS, StoreProperty.PROP_DEFAULT_CATEGORY_PAGE_ITEMS)));
            DataNavigator nav = new DataNavigator(getRequest(), "products");
            List<Product> l = new ArrayList<Product>();
            if (query.startsWith("#")) {
                Long id = SomeUtils.strToLong(query.substring(1));
                Product p = (Product) dao.get(Product.class, id);
                if (p != null) l.add(p);
                nav.setTotalRows(1);
            }
            Long userLevel = (getFrontUserLevel() != null) ? getFrontUserLevel().getId() : null;
            if (l.isEmpty()) {
                if ("Y".equalsIgnoreCase(getStoreProperty(StoreProperty.PROP_USE_LUCENE_INDEXER, StoreProperty.PROP_DEFAULT_USE_LUCENE_INDEXER))) {
                    Integer maxResults = SomeUtils.strToInteger(getStoreProperty(StoreProperty.PROP_SEARCH_MAX_RESULTS, StoreProperty.PROP_DEFAULT_SEARCH_MAX_RESULTS));
                    if (maxResults == null || maxResults < 1) maxResults = 200;
                    LuceneSearcher searcher = new LuceneSearcher(getServletContext().getRealPath("/stores/" + getStoreCode()), getDefaultLanguage(), getStopWordsFor(getDefaultLanguage()));
                    l = searcher.search(query, nav, dao, userLevel, maxResults);
                } else {
                    ProductFilter filter = new ProductFilter();
                    filter.setFilterQuery(query);
                    if (userLevel != null) filter.setFilterUserLevel(userLevel.toString());
                    filter.setSortedDirection("desc");
                    filter.setSortedField("hits");
                    l = dao.listFrontProducts(nav, filter, null, getLocale().getLanguage(), null);
                }
            }

            nav.setListado(l);
            addToStack("products", nav);
            getBreadCrumbs().add(new BreadCrumb("search", getText("search.results.for", "Search Results For") + ": <span class=\"query\">" + query + "</span>", null, null));

            String mode = getCookie("category.navigator.view");
            if (StringUtils.isEmpty(mode)) mode = getStoreProperty(StoreProperty.PROP_LISTING_STYLE, StoreProperty.PROP_DEFAULT_LISTING_STYLE);
            addToStack("navView", mode);

            List<Integer> pageItemsOptions = new ArrayList<Integer>();
            String[] arrOptions = getStoreProperty(StoreProperty.PROP_CATEGORY_PAGE_ITEMS_OPTIONS, StoreProperty.PROP_DEFAULT_CATEGORY_PAGE_ITEMS_OPTIONS).split(",");
            for (String s : arrOptions) {
                int n = NumberUtils.toInt(s.trim(), 0);
                if (n > 0) pageItemsOptions.add(n);
            }
            addToStack("pageItemsOptions", pageItemsOptions);
        }
        EventUtils.executeEvent(getServletContext(), EventService.EVENT_VISIT_SEARCH, this);
        return SUCCESS;
    }

    private Set getStopWordsFor(String language) {
        if (session.containsKey("stop_words_" + language) && session.get("stop_words_" + language) instanceof Set) {
            return (Set) session.get("stop_words_" + language);
        } else {
            try {
                File f = new File(getServletContext().getRealPath("/WEB-INF/classes/stop_" + language + ".txt"));
                if (f.exists()) {
                    FileReader reader = new FileReader(f);
                    Set set = WordlistLoader.getSnowballWordSet(reader);
                    session.put("stop_words_" + language, set);
                    return set;
                }
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        }
        return null;
    }

    public String quickSearch() throws Exception {
        quickSearchResult = new ArrayList<Map<String, String>>();
        if (StringUtils.isNotEmpty(term)) {
            List<Product> l = dao.productQuickSearch(term);
            if (l != null && !l.isEmpty()) {
                ImageResolver ir = getImageResolver();
                for (Product p : l) {
                    StringBuilder labels = new StringBuilder(p.getProductType());
                    Set<ProductLabel> labelList = getProductLabels(p);
                    if (labelList != null && !labelList.isEmpty()) {
                        for (ProductLabel lab : labelList) labels.append(" label-").append(lab.getCode());
                    }
                    String img = ir.getImageForProduct(p, "list/");
                    if (StringUtils.isEmpty(img)) img = "/stores/" + getStoreCode() + "/skins/" + getSkin() + "/images/" + getLocale().getLanguage() + "/not-available.gif";
                    else img = "/stores/" + getStoreCode() + "/images/products/list/" + img;
                    Map<String, String> m = new HashMap<String, String>();
                    m.put("value", p.getPartNumber());
                    m.put("label", "<div class=\"clearfix " + labels.toString() + " \"><img src=\"" + img + "\"><span class=\"qs-name\">" + p.getProductName(getLocale().getLanguage()) + "</span><span class=\"qs-code\">" + p.getPartNumber() + "</span></div>");
                    quickSearchResult.add(m);
                }
            }
        }
        return SUCCESS;
    }

    @Unsecured
    public String quickProduct() throws Exception {
        product = dao.getProduct(code);
        if (product == null) product = (Product) dao.get(Product.class, idProduct);
        if (product == null || !product.canShow()) {
            addActionError(getText(CNT_ERROR_PRODUCT_NOT_FOUND, CNT_DEFAULT_ERROR_PRODUCT_NOT_FOUND));
            return error();
        } else {
            product.addHit();
            dao.save(product);

            //Verificar si existen datos para el idioma del locale
            ProductLang pl = product.getLanguage(getLocale().getLanguage());
            if (pl == null) {
                ProductLang pd = product.getLanguage(getLocale().getLanguage(), getDefaultLanguage());
                if (pd != null) {
                    pl = new ProductLang();
                    pl.setProductName(pd.getProductName());
                    pl.setCaract1(pd.getCaract1());
                    pl.setCaract2(pd.getCaract2());
                    pl.setCaract3(pd.getCaract3());
                    pl.setDescription(pd.getDescription());
                    pl.setFeatures(pd.getFeatures());
                    pl.setInformation(pd.getInformation());
                    pl.setMetas(pd.getMetas());
                    dao.save(pl);
                }
            }

            // Product Variations
            addToStack("productVariations", dao.getProductVariations(product));

            // Product StaticTexts
            Map<String, List<ProdStaticText>> mapStaticText = new HashMap<String, List<ProdStaticText>>();
            List<ProductStaticText> lpst = dao.getProductStaticText(product);
            if (lpst != null) {
                for (ProductStaticText pst : lpst) {
                    String place = (StringUtils.isNotEmpty(pst.getContentPlace())) ? pst.getContentPlace() : "[no-place]";
                    List<ProdStaticText> l = null;
                    if (mapStaticText.containsKey(place)) l = mapStaticText.get(place);
                    if (l == null) {
                        l = new ArrayList<ProdStaticText>();
                        mapStaticText.put(place, l);
                    }
                    l.add(pst);
                }
            }
            if (product.getCategory() != null) {
                List<CategoryStaticText> lcst = dao.getParentCategoryStaticTexts(product.getCategory());
                if (lpst != null) {
                    for (CategoryStaticText cst : lcst) {
                        String place = (StringUtils.isNotEmpty(cst.getContentPlace())) ? cst.getContentPlace() : "[no-place]";
                        List<ProdStaticText> l = null;
                        if (mapStaticText.containsKey(place)) l = mapStaticText.get(place);
                        if (l == null) {
                            l = new ArrayList<ProdStaticText>();
                            mapStaticText.put(place, l);
                        }
                        l.add(cst);
                    }
                }
            }
            for (List<ProdStaticText> l : mapStaticText.values()) {
                Collections.sort(l, new Comparator<ProdStaticText>() {
                    public int compare(ProdStaticText o1, ProdStaticText o2) {
                        return (o1.getContentOrder() != null) ? o1.getContentOrder().compareTo(o2.getContentOrder()) : -1;
                    }
                });
            }
            addToStack("productStaticTexts", mapStaticText);

            // Product Attributes
            List<ProductProperty> listProp = dao.getProductProperties(product);
            Collection listPropWithValue = CollectionUtils.select(listProp, new Predicate() {
                public boolean evaluate(Object o) {
                    return o != null && o instanceof ProductProperty && (StringUtils.isNotEmpty(((ProductProperty) o).getPropertyValue()));
                }
            });
            if (listPropWithValue != null && !listPropWithValue.isEmpty()) addToStack("productAttributes", listPropWithValue);

        }
        return SUCCESS;
    }

    @Unsecured
    public String product() throws Exception {
        product = dao.getProduct(code);
        if (product == null) product = (Product) dao.get(Product.class, idProduct);
        if (product == null || !product.canShow()) {
            addActionError(getText(CNT_ERROR_PRODUCT_NOT_FOUND, CNT_DEFAULT_ERROR_PRODUCT_NOT_FOUND));
            return error();
        } else {
            product.addHit();
            dao.save(product);

            //Verificar si existen datos para el idioma del locale
            ProductLang pl = product.getLanguage(getLocale().getLanguage());
            if (pl == null) {
                ProductLang pd = product.getLanguage(getLocale().getLanguage(), getDefaultLanguage());
                if (pd != null) {
                    pl = new ProductLang();
                    pl.setProductName(pd.getProductName());
                    pl.setCaract1(pd.getCaract1());
                    pl.setCaract2(pd.getCaract2());
                    pl.setCaract3(pd.getCaract3());
                    pl.setDescription(pd.getDescription());
                    pl.setFeatures(pd.getFeatures());
                    pl.setInformation(pd.getInformation());
                    pl.setMetas(pd.getMetas());
                    dao.save(pl);
                }
            }
            // Verificar si existen los METAS
            if (pl != null && "Y".equalsIgnoreCase(getStoreProperty(StoreProperty.PROP_AUTOGENERATE_METAS, StoreProperty.PROP_DEFAULT_AUTOGENERATE_METAS))) {
                String kw = extractText(pl.getDescription());
                if (StringUtils.isEmpty(pl.getMetaValue(PageMeta.META_TITLE))) pl.addMeta(PageMeta.META_TITLE, pl.getProductName(), false);
                if (StringUtils.isEmpty(pl.getMetaValue(PageMeta.META_DESCRIPTION))) pl.addMeta(PageMeta.META_DESCRIPTION, kw, false);
                if (StringUtils.isEmpty(pl.getMetaValue(PageMeta.META_KEYWORDS))) pl.addMeta(PageMeta.META_KEYWORDS, extractKeywords(pl.getProductName() + " " + kw), false);
//                if (StringUtils.isEmpty(pl.getMetaValue(PageMeta.META_ABSTRACT))) pl.addMeta(PageMeta.META_ABSTRACT,descText,false);
            }
            // Product Variations
            addToStack("productVariations", dao.getProductVariations(product));

            // Product Reviews
            DataNavigator reviews = new DataNavigator(getRequest(), "reviews");
            reviews.setPageRows(5);
            reviews.setListado(dao.getReviewsForProduct(reviews, product));
            addToStack("reviews", reviews);

            // Product StaticTexts
            Map<String, List<ProdStaticText>> mapStaticText = new HashMap<String, List<ProdStaticText>>();
            List<ProductStaticText> lpst = dao.getProductStaticText(product);
            if (lpst != null) {
                for (ProductStaticText pst : lpst) {
                    String place = (StringUtils.isNotEmpty(pst.getContentPlace())) ? pst.getContentPlace() : "[no-place]";
                    List<ProdStaticText> l = null;
                    if (mapStaticText.containsKey(place)) l = mapStaticText.get(place);
                    if (l == null) {
                        l = new ArrayList<ProdStaticText>();
                        mapStaticText.put(place, l);
                    }
                    l.add(pst);
                }
            }
            if (product.getCategory() != null) {
                List<CategoryStaticText> lcst = dao.getParentCategoryStaticTexts(product.getCategory());
                if (lpst != null) {
                    for (CategoryStaticText cst : lcst) {
                        String place = (StringUtils.isNotEmpty(cst.getContentPlace())) ? cst.getContentPlace() : "[no-place]";
                        List<ProdStaticText> l = null;
                        if (mapStaticText.containsKey(place)) l = mapStaticText.get(place);
                        if (l == null) {
                            l = new ArrayList<ProdStaticText>();
                            mapStaticText.put(place, l);
                        }
                        l.add(cst);
                    }
                }
            }
            for (List<ProdStaticText> l : mapStaticText.values()) {
                Collections.sort(l, new Comparator<ProdStaticText>() {
                    public int compare(ProdStaticText o1, ProdStaticText o2) {
                        return (o1.getContentOrder() != null) ? o1.getContentOrder().compareTo(o2.getContentOrder()) : -1;
                    }
                });
            }
            addToStack("productStaticTexts", mapStaticText);

            // Product Attributes
            List<ProductProperty> listProp = dao.getProductProperties(product);
            Collection listPropWithValue = CollectionUtils.select(listProp, new Predicate() {
                public boolean evaluate(Object o) {
                    return o != null && o instanceof ProductProperty && (StringUtils.isNotEmpty(((ProductProperty) o).getPropertyValue()));
                }
            });
            if (listPropWithValue != null && !listPropWithValue.isEmpty()) addToStack("productAttributes", listPropWithValue);

            // Buscar categoria
            if (getStoreSessionObjects().containsKey(StoreSessionInterceptor.CNT_LAST_CATEGORY) && getStoreSessionObjects().get(StoreSessionInterceptor.CNT_LAST_CATEGORY) != null && getStoreSessionObjects().get(StoreSessionInterceptor.CNT_LAST_CATEGORY) instanceof Long)
                category = dao.getCategory((Long) getStoreSessionObjects().get(StoreSessionInterceptor.CNT_LAST_CATEGORY));

            if (!dao.productShowInCategory(product, category) && CollectionUtils.isNotEmpty(product.getProductCategories())) {
                if (product.getProductCategories().size() == 1) {
                    category = product.getProductCategories().iterator().next();
                } else {
                    if (category == null || !product.getProductCategories().contains(category)) {
                        category = product.getCategory();
                    }
                }
            }

            if (category != null) {

                // categorias hijas
                List<CategoryDTO> subcategories = dao.getSubcategories(category.getIdCategory(), getLocale().getLanguage(), null, true);
                addToStack("categoriesChildren", subcategories);

                // categorias hermanas
                if (category.getIdParent() != null) {
                    List<CategoryDTO> siblings = dao.getSubcategories(category.getIdParent(), getLocale().getLanguage(), null, false);
                    addToStack("categoriesSibling", siblings);
                }

                // breadcrumb
                for (Category c1 : getCategoryHierarchy(category))
                    if (!"_base".equalsIgnoreCase(c1.getUrlCode()))
                        getBreadCrumbs().add(new BreadCrumb("category", c1.getCategoryName(getLocale().getLanguage()), urlCategory(c1), null));

            }
            getBreadCrumbs().add(new BreadCrumb("product", product.getProductName(getLocale().getLanguage()), null, null));

            // backlinks
            if ("Y".equalsIgnoreCase(getStoreProperty(StoreProperty.PROP_SHOW_BACKLINKS, StoreProperty.PROP_DEFAULT_SHOW_BACKLINKS)) && product.getCategory() != null) {
                ProductFilter pf = new ProductFilter();
                pf.setFilterCategories(product.getCategory().getIdCategory().toString());
                addToStack("backlinks", dao.listFrontProducts(null, pf, null, getLocale().getLanguage(), null));
            }

        }
        EventUtils.executeEvent(getServletContext(), EventService.EVENT_VISIT_PRODUCT, this);
        return ("print".equalsIgnoreCase(output)) ? "print" : SUCCESS;
    }

    public String productImage() throws Exception {
        product = (Product) dao.get(Product.class, idProduct);
        if (product != null) {
            String imagePath = getImageResolver().getImageForProduct(product, ImageResolverImpl.PATH_LIST);
            if (StringUtils.isNotEmpty(imagePath)) {
                FileInputStream fis = new FileInputStream(getServletContext().getRealPath("/stores/" + getStoreCode() + "/images/products/list/" + imagePath));
                inputStream = new BufferedInputStream(fis);
                if (imagePath.toLowerCase().endsWith(".jpg")) contentType = "image/jpeg";
                if (imagePath.toLowerCase().endsWith(".gif")) contentType = "image/gif";
                if (imagePath.toLowerCase().endsWith(".png")) contentType = "image/png";
                return SUCCESS;
            }
        }
        String noImage = "/stores/" + getStoreCode() + "/skins/" + getSkin() + "/images/" + getLocale().getLanguage() + "/not-available.gif";
        File f = new File(getServletContext().getRealPath(noImage));
        if (f.exists()) {
            FileInputStream fis = new FileInputStream(f);
            inputStream = new BufferedInputStream(fis);
            contentType = "image/gif";
        }
        return SUCCESS;
    }

    public String productMail() throws Exception {
        if ("Y".equalsIgnoreCase(getStoreProperty("product.email.friend.show", "Y"))) {

            // check recaptcha
            String privateKey = getStoreProperty(StoreProperty.RECAPTCHA_PRIVATE, null);
            if (StringUtils.isNotEmpty(privateKey)) {
                String reCaptchaResponse = request.getParameter("g-recaptcha-response");
                if (!SomeUtils.reCaptcha2(privateKey, request.getRemoteAddr(), reCaptchaResponse)) {
                    addToStack("mailSent", 'N');
                    return SUCCESS;
                }
            }

            if (getFrontUser() != null && StringUtils.isNotEmpty(mailTo) && !dao.userHasFriend(getFrontUser(), mailTo)) {
                UserFriends userFriends = new UserFriends();
                userFriends.setFriendEmail(mailTo);
                userFriends.setReferred(false);
                userFriends.setUser(getFrontUser());
                dao.save(userFriends);
            }

            product = (Product) dao.get(Product.class, idProduct);
            if (product == null) addActionError(getText(CNT_ERROR_PRODUCT_NOT_FOUND, CNT_DEFAULT_ERROR_PRODUCT_NOT_FOUND));
            if (StringUtils.isEmpty(mailTo)) addActionError(getText(CNT_ERROR_MAILTO_REQUIRED, CNT_DEFAULT_ERROR_MAILTO_REQUIRED));
            if (getFrontUser() == null && StringUtils.isEmpty(mailFrom)) addActionError(getText(CNT_ERROR_MAILFROM_REQUIRED, CNT_DEFAULT_ERROR_MAILFROM_REQUIRED));
            if (getFrontUser() == null && StringUtils.isEmpty(mailFromName)) addActionError(getText(CNT_ERROR_FROMNAME_REQUIRED, CNT_DEFAULT_ERROR_FROMNAME_REQUIRED));

            if (hasErrors()) return SUCCESS;

            Map<String, Object> map1 = new HashMap<String, Object>();
            map1.put("product", new MProduct(product, this));
            map1.put("mailFrom", getFrontUser() != null ? getFrontUser().getEmail() : mailFrom);
            map1.put("mailFromName", getFrontUser() != null ? getFrontUser().getFullName() : mailFromName);
            map1.put("mailTo", mailTo);
            map1.put("mailComment", mailComment);
            String body = proccessVelocityTemplate(Mail.MAIL_TEMPLATE_PRODUCT, map1);
            Mail mail = new Mail();
            mail.setInventaryCode(getStoreCode());
            mail.setBody(body);
            mail.setSubject(getText(CNT_SUBJECT_PRODUCT_FRIEND, CNT_DEFAULT_SUBJECT_PRODUCT_FRIEND, new String[]{product.getProductName(getLocale().getLanguage())}));
            //mail.setFromAddress(mailFrom);
            mail.setToAddress(mailTo);
            mail.setPriority(10);
            mail.setReference("PRODUCT TO FRIEND " + product.getPartNumber());
            try {
                Session hSession = HibernateSessionFactory.getSessionAutoCommit(getDatabaseConfig());
                hSession.saveOrUpdate(mail);
                hSession.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            MailSenderThreat.asyncSendMail(mail, this);
            addToStack("mailSent", 'Y');

            Map<String, Object> map = new HashMap<String, Object>();
            map.put("email", mailTo);
            EventUtils.executeEvent(getServletContext(), EventService.EVENT_REFER_FRIEND, this, map);

        }
        return SUCCESS;
    }

    public String productStockAlert() throws Exception {
        setCode("error");
        if (getFrontUser() != null) {
            product = dao.getProduct(code);
            if (product == null) product = (Product) dao.get(Product.class, idProduct);
            if (product != null) {
                getFrontUser().addPreference(UserPreference.STOCK_ALERT, product.getIdProduct().toString());
                setCode("ok");
            }
        }
        return SUCCESS;
    }

    public String productShippingCost() throws Exception {
        Map<String, Object> result = new HashMap<String, Object>();
        product = dao.getProduct(code);
        if (product != null && StringUtils.isNotEmpty(zipCode)) {
            // Request Data for ZipCode
            UserAddress data = zipCodeData(zipCode);
            if (data != null) {
                // todo: shipping cost from zipcode
                ShopCartItem it = new ShopCartItem(getUserSession().getShoppingCart());
                it.setPrice(product.getFinalPrice(getFrontUserLevel(), 1));
                it.setPriceOriginal(it.getPrice());
                it.setBeanProd1(product);
                it.setCode1(product.getPartNumber());
                it.setName1(product.getProductName(getLocale().getLanguage()));
                it.setProduct1(product.getIdProduct());
                it.setQuantity(1);
                List<ShopCartItem> l = new ArrayList<ShopCartItem>();
                l.add(it);
                double cost = getUserSession().calculateDeliveryCostForLocation(l, data.getState(), data.getCity(), data.getZipCode());
                result.put("value", cost);
            } else {
                result.put("error", getText(CNT_ERROR_ZIP_NOT_FOUND, CNT_DEFAULT_ERROR_ZIP_NOT_FOUND));
            }
        }
        addToStack("result", result);
        return SUCCESS;
    }

    public String getReviews() throws Exception {
        product = dao.getProduct(code);
        if (product == null) product = (Product) dao.get(Product.class, idProduct);
        if (product != null) {
            DataNavigator reviews = new DataNavigator(getRequest(), "reviews");
            reviews.setPageRows(5);
            reviews.setListado(dao.getReviewsForProduct(reviews, product));
            addToStack("reviews", reviews);
        }
        return SUCCESS;
    }

    public String addReview() throws Exception {
        if (canAddReview()) {

            String privateKey = getStoreProperty(StoreProperty.RECAPTCHA_PRIVATE, null);
            if (StringUtils.isNotEmpty(privateKey)) {
                String reCaptchaResponse = request.getParameter("g-recaptcha-response");
                if (!SomeUtils.reCaptcha2(privateKey, request.getRemoteAddr(), reCaptchaResponse)) {
                    return SUCCESS;
                }
            }

            boolean validToken;
            synchronized (session) {
                validToken = TokenHelper.validToken();
            }

            product = dao.getProduct(code);
            if (validToken && product != null && review != null) {
                review.setProduct(product);
                if (getFrontUser() != null) {
                    review.setIdUser(getFrontUser().getIdUser());
                    review.setUserName(getFrontUser().getFullName());
                }
                if (review_score_name != null && review_score_name.length > 0 && review_score_value != null && review_score_value.length == review_score_name.length) {
                    for (int i = 0; i < review_score_name.length; i++) {
                        if (StringUtils.isNotEmpty(review_score_name[i]) && review_score_value[i] != null) {
                            Integer rsv = Math.max(Math.min(10, review_score_value[i]), 0);
                            review.setDetailedScore(review_score_name[i], rsv);
                        }
                    }
                }
                if ("y".equalsIgnoreCase(getStoreProperty(StoreProperty.PROP_PRODUCT_REVIEW_APPROVE_AUTO, StoreProperty.PROP_DEFAULT_PRODUCT_REVIEW_APPROVE_AUTO))) review.setVisible(true);
                dao.save(review);
                addSessionMessage(getText(CNT_MSG_PRODUCT_REVIEW_SAVED, CNT_DEFAULT_MSG_PRODUCT_REVIEW_SAVED));
                dao.updateAverageScore(product);
                dao.save(product);

                //send email
                String mailTo = getStoreProperty(StoreProperty.PROP_PRODUCT_REVIEW_MAILTO, null);
                if (StringUtils.isNotEmpty(mailTo)) {
                    Map map1 = new HashMap();
                    map1.put("product", new MProduct(product, this));
                    map1.put("review", new MReview(review, this));
                    String body = proccessVelocityTemplate(Mail.MAIL_TEMPLATE_PRODUCT_REVIEW, map1);
                    Mail mail = new Mail();
                    mail.setInventaryCode(getStoreCode());
                    mail.setBody(body);
                    mail.setSubject(getText(CNT_SUBJECT_PRODUCT_REVIEW, CNT_DEFAULT_SUBJECT_PRODUCT_REVIEW, new String[]{product.getProductName(getLocale().getLanguage())}));
                    mail.setToAddress(mailTo);
                    mail.setPriority(4);
                    mail.setReference("PRODUCT REVIEW " + product.getPartNumber());
                    try {
                        Session hSession = HibernateSessionFactory.getSessionAutoCommit(getDatabaseConfig());
                        hSession.saveOrUpdate(mail);
                        hSession.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    MailSenderThreat.asyncSendMail(mail, this);
                }

                EventUtils.executeEvent(getServletContext(), EventService.EVENT_ADD_REVIEW, this);
            }
        }
        return SUCCESS;
    }

    public String addToWishList() throws Exception {
        Product product = (Product) dao.get(Product.class, idProduct);
        if (getFrontUser() != null && product != null && !productInWishList(product.getIdProduct())) {
            UserWishList w = new UserWishList();
            w.setIdProduct(product.getIdProduct());
            w.setUser(getFrontUser());
            dao.save(w);
            if (getFrontUser().getWishList() == null) getFrontUser().setWishList(new HashSet<UserWishList>());
            getFrontUser().getWishList().add(w);
            setCode("ok");
        } else {
            setCode("error");
        }
        EventUtils.executeEvent(getServletContext(), EventService.EVENT_ADD_TO_WISHLIST, this);
        return SUCCESS;
    }

    public String delFromWishList() throws Exception {
        if (idProduct != null && getFrontUser() != null && getFrontUser().getWishList() != null && productInWishList(idProduct)) {
            UserWishList toDelete = null;
            for (UserWishList w : getFrontUser().getWishList()) {
                if (idProduct.equals(w.getIdProduct())) toDelete = w;
            }
            if (toDelete != null) getFrontUser().getWishList().remove(toDelete);
        }
        return SUCCESS;
    }

    public String resourceDownload() throws Exception {
        Resource resource = (Resource) dao.get(Resource.class, idResource);
        if (resource != null && StringUtils.isNotEmpty(resource.getResourceFileName())) {
            StringBuilder newFileName = new StringBuilder();
            if (!StringUtils.isEmpty(storeCode)) newFileName.append("/stores/").append(storeCode);
            newFileName.append(org.store.core.admin.GeneralAction.PATH_RESOURCES).append("/").append(resource.getResourceFileName());
            File f = new File(getServletContext().getRealPath(newFileName.toString()));
            if (f.exists()) {
                FileInputStream fis = new FileInputStream(f);
                BufferedInputStream bis = new BufferedInputStream(fis);
                setInputStream(bis);
                setContentDisposition("attachment;filename=\"" + resource.getFileName() + "\"");
            }
        }
        return SUCCESS;
    }


    public String blockEdit() throws Exception {
        staticText = dao.getStaticText(code, StaticText.TYPE_BLOCK);
        return SUCCESS;
    }

    public String blockSave() throws Exception {
        output = "";
        if (staticText != null) {
            staticText.setTextType(StaticText.TYPE_BLOCK);
            staticText.setInventaryCode(getStoreCode());
            dao.save(staticText);

            if (ArrayUtils.isSameLength(getLanguages(), staticTextValue)) {
                for (int i = 0; i < getLanguages().length; i++) {
                    StaticTextLang stLang = staticText.getLanguage(getLanguages()[i]);
                    if (stLang == null) {
                        stLang = new StaticTextLang();
                        stLang.setStaticLang(getLanguages()[i]);
                        stLang.setStaticText(staticText);
                    }
                    stLang.setValue(staticTextValue[i]);
                    stLang.setTitle((staticTextTitle != null && staticTextTitle.length > i) ? staticTextTitle[i] : null);
                    dao.save(stLang);
                    if (getLanguages()[i].equalsIgnoreCase(getLocale().getLanguage())) {
                        output = staticTextValue[i];
                    }
                }
            }
        }
        return SUCCESS;
    }

    @Secured
    public String payStepAddress() throws Exception {
        // Si no hay elementos en el carro, ir al shopping cart
        if (getUserSession().getShoppingCart().getItems().isEmpty()) {
            return "shopcart";
        }

        if (getFrontUser() == null) {
            setRedirectUrl(url("paystepAddress"));
            return "register";
        }

        // Verificar si hay un pago externo
        boolean pagoExterno = (getStoreSessionObjects().containsKey(StoreSessionInterceptor.CNT_EXTERNAL_PAYMENT) && StringUtils.isNotEmpty((String) getStoreSessionObjects().get(StoreSessionInterceptor.CNT_EXTERNAL_PAYMENT)));
        addToStack("canChangeBillingAddress", !pagoExterno || getUserSession().getShippingAddress() == null);
        //addToStack("canChangeShippingAddress", !pagoExterno || getUserSession().getBillingAddress() == null);
        addToStack("canChangeShippingAddress", Boolean.TRUE);

        // Sino hay direcciones definidas, usar unas por defecto
        if (getUserSession().getBillingAddress() == null)
            getUserSession().setBillingAddress(getFrontUser().getBillingAddress());
        if (getUserSession().getShippingAddress() == null)
            getUserSession().setShippingAddress(getFrontUser().getShippingAddress());

        // Inicializar
        if ("pickinstore".equalsIgnoreCase(getUserSession().getShippingType()) && !getCanPickInStore()) {
            getUserSession().setShippingType("billing");
        }
        if ("pickinstore".equalsIgnoreCase(getUserSession().getShippingType()) && getUserSession().getShippingStore() == null) {
            if (CollectionUtils.isNotEmpty(getLocationStoreList())) {
                getUserSession().setShippingStore(getLocationStoreList().get(0));
            } else {
                getUserSession().setShippingType("billing");
            }
        }
        if (!getCanDeliver() && ("billing".equalsIgnoreCase(getUserSession().getShippingType()) || "shipping".equalsIgnoreCase(getUserSession().getShippingType()))) {
            if (getCanPickInStore()) {
                getUserSession().setShippingType("pickinstore");
                getUserSession().setShippingStore(getLocationStoreList().get(0));
            } else getUserSession().setShippingType("");
        }

        // reiniciar valores seleccionados
        getUserSession().setShippingMethod(null);
        getUserSession().setShippingValue(null);
        getUserSession().setShippingDate(null);
        // Salvar shopping cart
        updateShoppingCartInSession();
        EventUtils.executeEvent(getServletContext(), EventService.EVENT_PAYMENT_ADDRESS, this);
        return SUCCESS;
    }

    @Secured
    public String payStepAddressBilling() throws Exception {
        boolean pagoExterno = (getStoreSessionObjects().containsKey(StoreSessionInterceptor.CNT_EXTERNAL_PAYMENT) && StringUtils.isNotEmpty((String) getStoreSessionObjects().get(StoreSessionInterceptor.CNT_EXTERNAL_PAYMENT)));
        addToStack("canChangeBillingAddress", !pagoExterno || getUserSession().getShippingAddress() == null);
        if ("change".equalsIgnoreCase(output)) {
            if (billingAddress != null) {
                getUserSession().setBillingAddress(billingAddress);
                updateShoppingCartInSession();
            }
        } else if ("edit".equalsIgnoreCase(output)) {
            if (billingAddress != null && billingAddressState != null) {
                State s = (State) dao.get(State.class, billingAddressState);
                if (s != null) {
                    billingAddress.setState(s);
                    billingAddress.setUser(getFrontUser());
                    billingAddress.setBilling(true);
                    billingAddress.setShipping(shippingAddressUseBilling != null && shippingAddressUseBilling);
                    dao.save(billingAddress);
                    getUserSession().setBillingAddress(billingAddress);
                    updateShoppingCartInSession();
                }
            }
        }
        return SUCCESS;
    }

    @Secured
    public String payStepAddressShipping() throws Exception {
        /** Permitir siempre el cambio de shipping
         boolean pagoExterno = (getStoreSessionObjects().containsKey(StoreSessionInterceptor.CNT_EXTERNAL_PAYMENT) && StringUtils.isNotEmpty((String) getStoreSessionObjects().get(StoreSessionInterceptor.CNT_EXTERNAL_PAYMENT)));
         addToStack("canChangeShippingAddress", !pagoExterno || getUserSession().getBillingAddress() == null);
         */
        addToStack("canChangeShippingAddress", Boolean.TRUE);
        if ("type".equalsIgnoreCase(output)) {
            String oldType = getUserSession().getShippingType();
            getUserSession().setShippingType(shippingType);
            if ("pickinstore".equalsIgnoreCase(shippingType) && !getCanPickInStore()) {
                getUserSession().setShippingType("billing");
            }
            if ("pickinstore".equalsIgnoreCase(shippingType) && getUserSession().getShippingStore() == null) {
                if (CollectionUtils.isNotEmpty(getLocationStoreList())) {
                    getUserSession().setShippingStore(getLocationStoreList().get(0));
                } else {
                    getUserSession().setShippingType("billing");
                }
            }
            if ("shipping".equalsIgnoreCase(shippingType) && getUserSession().getShippingAddress() == null) {
                getUserSession().setShippingType("shipping");
                getUserSession().setShippingAddress(getFrontUser().getShippingAddress());
            }
        } else if ("change".equalsIgnoreCase(output)) {
            if (shippingAddress != null) {
                getUserSession().setShippingType("shipping");
                getUserSession().setShippingAddress(shippingAddress);
                updateShoppingCartInSession();
            }
        } else if ("new".equalsIgnoreCase(output)) {
            getUserSession().setShippingType("shipping");
            getUserSession().setShippingAddress(null);
        } else if ("edit".equalsIgnoreCase(output)) {
            if (shippingAddress != null && shippingAddressState != null) {
                State s = (State) dao.get(State.class, shippingAddressState);
                getUserSession().setShippingType("shipping");
                if (s != null) {
                    shippingAddress.setState(s);
                    shippingAddress.setUser(getFrontUser());
                    shippingAddress.setShipping(true);
                    dao.save(shippingAddress);
                    getUserSession().setShippingAddress(shippingAddress);
                }
            }
        } else if ("store".equalsIgnoreCase(output)) {
            LocationStore store = (LocationStore) dao.get(LocationStore.class, idStore);
            if (store != null) getUserSession().setShippingStore(store);
        } else if ("select".equalsIgnoreCase(output)) {
            if (selectedShippingMethod != null) {
                if (getUserSession().getShippingRates() != null && !getUserSession().getShippingRates().isEmpty()) {
                    for (Map<String, Object> map : getUserSession().getShippingRates()) {
                        if (selectedShippingMethod.equals(map.get("id"))) {
                            ShippingMethod method = (ShippingMethod) dao.get(ShippingMethod.class, selectedShippingMethod);
                            if (method != null) {
                                getUserSession().setShippingMethod(method);
                                getUserSession().setShippingValue((Double) map.get("value"));
                                getUserSession().setShippingDate((String) map.get("days"));
                            }
                        }
                    }
                }
            }
        }
        updateShoppingCartInSession();
        return SUCCESS;
    }

    @Secured
    public String payStepPayment() throws Exception {
        // Si no hay elementos en el carro, ir al shopping cart
        if (getUserSession().getShoppingCart().getItems().isEmpty()) {
            return "shopcart";
        }

        if (getFrontUser() == null) {
            setRedirectUrl(url("paystepAddress"));
            return "register";
        }

        String err = getUserSession().getReadyToPay();
        if (StringUtils.isNotEmpty(err)) {
            addSessionError(err);
            return "address";
        }

        EventUtils.executeEvent(getServletContext(), EventService.EVENT_PAYMENT_CONFIRM, this);

        // si marco insurance agregarlo
        Insurance insurance = (Insurance) dao.get(Insurance.class, idInsurance);
        getUserSession().setShippingInsurance((insurance != null && insurance.getInsuranceValue() != null) ? insurance.getInsuranceValue() : null);
        updateShoppingCartInSession();

        Map priceMap = getUserSession().getPriceMap();
        addToStack("priceMap", priceMap);

        // Obtener la lista de metodos de pago, sino se pago por un metodo externo
        if (!(getStoreSessionObjects().containsKey(StoreSessionInterceptor.CNT_EXTERNAL_PAYMENT) && StringUtils.isNotEmpty((String) getStoreSessionObjects().get(StoreSessionInterceptor.CNT_EXTERNAL_PAYMENT)))) {
            List<Map<String, Object>> metodosPago = getPaymentMethods(false, getFrontUserLevel());
            addToStack("metodos", metodosPago);
        } else {
            MerchantUtils mu = new MerchantUtils(getServletContext());
            String serviceName = (String) getStoreSessionObjects().get(StoreSessionInterceptor.CNT_EXTERNAL_PAYMENT);
            MerchantService service = mu.getService(serviceName, this);
            addToStack("metodoExterno", service);
        }

        return SUCCESS;
    }

    @Secured
    public String payStepSaveOrder() throws Exception {
        // validar
        // Si no hay elementos en el carro, ir al shopping cart
        if (getUserSession().getShoppingCart().getItems().isEmpty()) {
            return "shopcart";
        }

        if (getFrontUser() == null) {
            setRedirectUrl(url("paystepAddress"));
            return "register";
        }

        String err = getUserSession().getReadyToPay();
        if (StringUtils.isNotEmpty(err)) {
            addSessionError(err);
            return "address";
        }

        boolean validToken;
        synchronized (session) {
            validToken = TokenHelper.validToken();
        }
        if (validToken) {
            // Save Order
            EventUtils.executeEvent(getServletContext(), EventService.EVENT_PAYMENT_PRE_SAVE, this);
            Map priceMap = getUserSession().getPriceMap();
            Order order = new Order();
            order.setUser(getFrontUser());
            order.setAffiliate(getAffiliateUser());
            order.setAffiliateCode(getAffiliateCode());
            order.setBillingAddress(getUserSession().getBillingAddress());
            order.setTotalInsurance(getUserSession().getShippingInsurance());
            order.setCodeMerchant(null);
            order.setCurrency(getActualCurrency());
            order.setDeliveryAddress(getUserSession().getShippingAddress());
            if ("pickinstore".equalsIgnoreCase(getUserSession().getShippingType())) {
                order.setPickInStore(getUserSession().getShippingStore());
            } else {
                order.setShippingMethod(getUserSession().getShippingMethod());
                if (getUserSession().getShippingMethod() != null) order.setCodeCarrier(getUserSession().getShippingMethod().getCarrierName());
            }
            order.setPaymentMethod(null);
            order.setRemoteIp(getRequest().getRemoteAddr());
            order.setStatus(dao.getOrderStatus(OrderStatus.STATUS_DEFAULT, true));
            order.setCustomMessage(getCustomMessage());
            order.setCustomReference(getCustomReference());

            if (priceMap.containsKey("subtotal")) order.setTotalProducts((Double) priceMap.get("subtotal"));
            if (priceMap.containsKey("shipping")) order.setTotalShipping((Double) priceMap.get("shipping"));

            // Taxes
            if (priceMap.containsKey("taxInfo")) {
                List<Map<String, Object>> taxList = (List<Map<String, Object>>) priceMap.get("taxInfo");
                double totalTax = 0.0d;
                for (Map<String, Object> map : taxList) {
                    order.addTax(map);
                    Double taxValue = (Double) map.get("value");
                    if (taxValue != null) totalTax += taxValue;
                }
                order.setTotalTax(totalTax);
                if (priceMap.containsKey("toTax")) order.setTotalToTax((Double) priceMap.get("toTax"));
            }

            // Promotions
            if (priceMap.containsKey("promotions")) {
                List<Map<String, Object>> promos = (List<Map<String, Object>>) priceMap.get("promotions");
                double totalPromoDiscount = 0.0d;
                for (Map<String, Object> map : promos) {
                    order.addPromotion(map);
                    if ("discount".equals(map.get("type")) || "discount-percent".equals(map.get("type"))) {
                        Double disc = (Double) map.get("value");
                        totalPromoDiscount += disc;
                    }
                }
                order.setTotalDiscountPromotion(totalPromoDiscount);
            }

            // Rewards points
            if (priceMap.containsKey("usedRewards")) {
                order.setTotalRewards((Double) priceMap.get("usedRewards"));
            }

            order.setTotalComponents(getUserSession().getShoppingCart().getTotalComplements());

            dao.save(order);

            // Productos
            order.setOrderDetails(new ArrayList<OrderDetail>());
            for (ShopCartItem item : getUserSession().getShoppingCart().getItems()) {
                OrderDetail orderDetail = new OrderDetail();
                orderDetail.setOrder(order);
                orderDetail.setPrice(item.getPrice());
                orderDetail.setQuantity(item.getQuantity());
                orderDetail.setSeldate(item.getSelDate());
                orderDetail.setSeltime(item.getSelTime());
                orderDetail.setOrderDetailProducts(new ArrayList<OrderDetailProduct>());
                if (item.getBeanComponent() != null) {
                    orderDetail.setComplementName(item.getBeanComponent().getProductName(getLocale().getLanguage()));
                    orderDetail.setComplementValue(item.getComplementPrice());
                }
                dao.save(orderDetail);

                for (Product p : new Product[]{item.getBeanProd1(), item.getBeanProd2()}) {
                    if (p != null) {
                        OrderDetailProduct orderProd = new OrderDetailProduct();
                        ProductVariation var1 = (ProductVariation) dao.get(ProductVariation.class, item.getVariation1());
                        if (var1 != null) {
                            orderProd.setIdVariation(item.getVariation1());
                            orderProd.setCaractValue1(var1.getCaract1());
                            orderProd.setCaractValue2(var1.getCaract2());
                            orderProd.setCaractValue3(var1.getCaract3());
                            // Rebajar stock
                            var1.addStock(-item.getQuantity().longValue());
                            dao.save(var1);
                        }
                        if (Product.TYPE_DIGITAL.equalsIgnoreCase(p.getProductType()))
                            orderProd.setDownloads(dao.getMaxDownloads(p));
                        if (item.getFees() != null && item.getFees().size() > 0) {
                            CategoryFee fee = item.getFees().get(0);
                            orderProd.setFeeName(fee.getFee().getFeeName());
                            orderProd.setFeeValue(fee.getValue());
                        }
                        orderProd.setProduct(p);
                        orderProd.setCostPrice(p.getCostPrice());
                        orderProd.setOrderDetail(orderDetail);

                        //tax
                        double tax = 0.0;
                        ProductUserTax puTax = dao.getProductUserTaxes(p.getAltCategory(), getFrontUser().getAltCategory());
                        if (puTax != null && puTax.getTaxes() != null && !puTax.getTaxes().isEmpty()) {
                            for (TaxPerFamily tpf : puTax.getTaxes()) {
                                tax += tpf.getValue() * orderDetail.getPrice() * orderProd.getPercentOfPrice();
                                Map<String, Object> m = new HashMap<String, Object>();
                                m.put("value", tpf.getValue());
                                m.put("name", tpf.getTaxName());
                                m.put("code", tpf.getExternalCode());
                                orderProd.addTax(m);
                            }
                        }
                        orderProd.setTax(tax);

                        dao.save(orderProd);
                        orderDetail.getOrderDetailProducts().add(orderProd);
                        // Rebajar stock
                        p.addStock(-item.getQuantity());
                        p.addSales(item.getQuantity());
                        dao.save(p);
                    }
                }
                order.getOrderDetails().add(orderDetail);
            }

            // Free product
            for (Map<String, Object> map : order.getPromotions("product")) {
                Long idProd = (Long) map.get("data");
                Product p = (Product) dao.get(Product.class, idProd);
                if (p != null) {
                    OrderDetail orderDetail = new OrderDetail();
                    orderDetail.setOrder(order);
                    orderDetail.setPrice(0.0d);
                    orderDetail.setQuantity(1);
                    orderDetail.setPromotionCode((String) map.get("code"));
                    orderDetail.setPromotionName((String) map.get("name"));
                    dao.save(orderDetail);
                    OrderDetailProduct orderProd = new OrderDetailProduct();
                    if (Product.TYPE_DIGITAL.equalsIgnoreCase(p.getProductType()))
                        orderProd.setDownloads(dao.getMaxDownloads(p));
                    orderProd.setProduct(p);
                    orderProd.setCostPrice(p.getCostPrice());
                    orderProd.setOrderDetail(orderDetail);
                    orderProd.setTax(0.0);
                    dao.save(orderProd);
                    p.addStock(-1);
                    dao.save(p);
                }
            }

            OrderHistory history = new OrderHistory();
            history.setHistoryComment("");
            history.setHistoryDate(SomeUtils.today());
            history.setHistoryStatus(order.getStatus());
            history.setOrder(order);
            history.setUser(getFrontUser());
            dao.save(history);

            // descontar del usuario
            if (priceMap.containsKey("restRewardsPoints")) {
                getFrontUser().setRewardPoints((Long) priceMap.get("restRewardsPoints"));
                addRewardHistory(getFrontUser(), -((Number) priceMap.get("usedRewardsPoints")).doubleValue(), order, null, UserRewardHistory.TYPE_PURCHASE, "");
                dao.save(getFrontUser());
            }

            dao.flushSession();
            addToStack("order", order);

            //Salvar carro de compras
            getShoppingCart().setStatus(ShopCart.STATUS_FINISHED);
            dao.save(getShoppingCart());
            getUserSession().resetAll();

            EventUtils.executeEvent(getServletContext(), EventService.EVENT_PAYMENT_POST_SAVE, this);

            // Pay Order if we select a payment service
            if (StringUtils.isEmpty(paymentService) && getStoreSessionObjects().containsKey(StoreSessionInterceptor.CNT_EXTERNAL_PAYMENT) && StringUtils.isNotEmpty((String) getStoreSessionObjects().get(StoreSessionInterceptor.CNT_EXTERNAL_PAYMENT))) {
                paymentService = (String) getStoreSessionObjects().get(StoreSessionInterceptor.CNT_EXTERNAL_PAYMENT);
            }
            if (StringUtils.isNotEmpty(paymentService)) {
                idOrder = order.getIdOrder();
                return payStepPayOrder();
            }
        }

        return SUCCESS;
    }

    /**
     * Esta es la accion donde se ejecuta el pago de la orden
     *
     * @return
     * @throws Exception
     */
    @Secured
    public String payStepPayOrder() throws Exception {
        // Pay Order
        OrderStatus defaultStatus = dao.getOrderStatus(OrderStatus.STATUS_DEFAULT, true);
        Order order = (Order) dao.get(Order.class, idOrder);
        if (order != null && defaultStatus.equals(order.getStatus()) && StringUtils.isNotEmpty(paymentService)) {
            MerchantUtils mu = new MerchantUtils(getServletContext());
            MerchantService ms = mu.getService(paymentService, this);

            if (MerchantService.TYPE_HOSTED_PAGE.equalsIgnoreCase(ms.getType())) {
                Map result = ms.preparePaymentRedirection(order, this);
                if (result != null) {
                    addToStack("dataMap", result);
                    return "hostedPage";
                }
            } else {
                if (!ms.validatePayment(order, this)) {
                    return SUCCESS;
                }

                PaymentResult result = ms.doPayment(order, this);
                Map<String, Object> map = new HashMap<String, Object>();
                map.put("order", order);
                if (result != null && result.isApproved()) {
                    order.setStatus(dao.getOrderStatus(OrderStatus.STATUS_APPROVED, true));
                    order.setPaymentMethod(paymentService);
                    order.setCodeMerchant(result.getTransactionId());
                    order.setPaymentCard(result.getCardType());
                    dao.save(order);
                    addOrderHistory(order, getFrontUser(), "");
                    if (order.getStatus().getSendEmail()) sendOrderStatusMail(order);
                    setBlockCode(StaticText.BLOCK_ORDER_APPROVED);
                    EventUtils.executeEvent(getServletContext(), EventService.EVENT_APPROVE_ORDER, this, map);
                } else if (result != null && result.isRejected()) {
                    order.setStatus(dao.getOrderStatus(OrderStatus.STATUS_REJECTED, true));
                    order.setPaymentMethod(paymentService);
                    order.setCodeMerchant(result.getTransactionId());
                    order.setPaymentCard(result.getCardType());
                    dao.save(order);
                    addOrderHistory(order, getFrontUser(), "");
                    if (order.getStatus().getSendEmail()) sendOrderStatusMail(order);
                    // Recuperar stock reservado
                    recoverOrderStock(order);
                    setBlockCode(StaticText.BLOCK_ORDER_REJECTED);
                    EventUtils.executeEvent(getServletContext(), EventService.EVENT_DENY_ORDER, this, map);
                } else if (result != null && result.isPending()) {
                    order.setStatus(dao.getOrderStatus(OrderStatus.STATUS_PAYMENT_VALIDATION, true));
                    order.setPaymentMethod(paymentService);
                    order.setCodeMerchant(result.getTransactionId());
                    order.setPaymentCard(result.getCardType());
                    dao.save(order);
                    addOrderHistory(order, getFrontUser(), "Pending payment validation");
                    if (order.getStatus().getSendEmail()) sendOrderStatusMail(order);
                    setBlockCode(StaticText.BLOCK_ORDER_PENDING);
                } else {
                    order.setStatus(dao.getOrderStatus(OrderStatus.STATUS_DEFAULT, true));
                    order.setPaymentMethod(paymentService);
                    if (result != null) {
                        order.setCodeMerchant(result.getTransactionId());
                        order.setPaymentCard(result.getCardType());
                    }
                    dao.save(order);
                    addOrderHistory(order, getFrontUser(), "");
                    if (order.getStatus().getSendEmail()) sendOrderStatusMail(order);
                    if (StringUtils.isNotEmpty(ms.getBlockCode())) setBlockCode(ms.getBlockCode());
                    EventUtils.executeEvent(getServletContext(), EventService.EVENT_SAVE_ORDER, this, map);
                }
                getStoreSessionObjects().remove(StoreSessionInterceptor.CNT_EXTERNAL_PAYMENT);
                dao.flushSession();
            }
        } else {
            addActionError(getText(CNT_PAYMENT_INVALID_ORDER_STATUS, CNT_DEFAULT_PAYMENT_INVALID_ORDER_STATUS));
        }
        return SUCCESS;
    }

    public String payStepExternal() throws Exception {
        // Si no hay elementos en el carro, ir al shopping cart
        if (getUserSession().getShoppingCart().getItems().isEmpty()) {
            return "shopcart";
        }

        if (StringUtils.isNotEmpty(paymentService)) {
            MerchantUtils mu = new MerchantUtils(getServletContext());
            MerchantService service = mu.getService(paymentService, this);
            redirectUrl = service.doPaymentRedirection(this);
            if (StringUtils.isNotEmpty(redirectUrl)) return "redirectUrl";
            else addSessionError(service.getError());
        }

        return "shopcart";
    }

    @Secured
    public String payStepResult() throws Exception {
        if (idOrder == null && getRequest().getAttribute("idOrder") != null && getRequest().getAttribute("idOrder") instanceof Long) idOrder = (Long) getRequest().getAttribute("idOrder");
        if (StringUtils.isEmpty(blockCode) && getRequest().getAttribute("blockCode") != null && getRequest().getAttribute("blockCode") instanceof String) blockCode = (String) getRequest().getAttribute("blockCode");
        Order order = (Order) dao.get(Order.class, idOrder);
        if (order != null) {
            addToStack("order", order);
            if ("Y".equalsIgnoreCase(getStoreProperty(StoreProperty.PROP_GOOGLE_ECOMMERCE_TRACKING, "N"))) {
                String[] arr = getStoreProperty(StoreProperty.PROP_GOOGLE_ECOMMERCE_TRACKING_STATUS, "").split(",");
                if (ArrayUtils.contains(arr, order.getStatus().getStatusType())) addToStack("doGoogleTracking", Boolean.TRUE);
            }
        }
        return SUCCESS;
    }

    @Secured
    public String payOrder() throws Exception {
        Order order = (Order) dao.get(Order.class, idOrder);
        if (order != null) {
            User user = order.getUser();
            user.setLastIP(getRequest().getRemoteAddr());
            user.setLastBrowser(getBrowser(getRequest()));
            user.addVisit();
            dao.save(user);
            if (!user.equals(getFrontUser())) setFrontUser(user);

            if (OrderStatus.STATUS_DEFAULT.equalsIgnoreCase(order.getStatus().getStatusCode())) {
                addToStack("order", order);
                List<Map<String, Object>> metodosPago = getPaymentMethods(false, getFrontUserLevel());
                addToStack("metodos", metodosPago);
                return SUCCESS;
            } else {
                addActionError(getText(CNT_ERROR_ORDER_ALREADY_PAID, CNT_DEFAULT_ERROR_ORDER_ALREADY_PAID));
                return "homeView";
            }

        } else {
            addActionError(getText(CNT_ERROR_ORDER_NOT_FOUND, CNT_DEFAULT_ERROR_ORDER_NOT_FOUND));
            return "homeView";
        }
    }

    public String enc() throws Exception {
        if (StringUtils.isNotEmpty(code)) {
            String url = SomeUtils.decrypt3Des(code, getEncryptionKey());
            if (StringUtils.isNotEmpty(url)) {
                redirectUrl = url;
                return "redirectUrl";
            }
        }
        return "home";
    }

    public UserAddress zipCodeData(String z) {
        /*
        ToponymSearchCriteria searchCriteria = new ToponymSearchCriteria();
        searchCriteria.setCountryCode(defaultCountry);
        searchCriteria.setFeatureClass(FeatureClass.A);
        searchCriteria.setFeatureCode("ADM1");
        searchCriteria.setStyle(Style.FULL);
        searchCriteria.setLanguage(getDefaultLanguage());
        ToponymSearchResult searchResult = WebService.search(searchCriteria);
        for (Toponym toponym : searchResult.getToponyms()) {
            if (StringUtils.isNotEmpty(toponym.getAdminCode1())) {
                State st = dao.getStateByCode(defaultCountry, toponym.getAdminCode1());
                if (st == null) {
                    st = new State();
                    st.setCountryCode(defaultCountry);
                    st.setInventaryCode(store);
                    st.setStateCode(toponym.getAdminCode1());
                    st.setStateName(toponym.getAdminName1());
                    dao.save(st);
                }

            }
        }
        */
        return null;
    }


    private Map<String, String> jsonResult;
    private String output;
    private Long idOrder;
    private Long idResource;
    private Long idProduct;
    private Long idCategory;
    private Long idBanner;
    private String code;
    private String manufacturer;
    private String label;
    private String query;
    private Banner banner;
    private Product product;
    private Category category;
    private ProductFilter productFilter;
    private ProductReview review;
    private String[] review_score_name;
    private Integer[] review_score_value;


    private UserAddress billingAddress;
    private Long billingAddressState;
    private UserAddress shippingAddress;
    private Long shippingAddressState;
    private Boolean shippingAddressUseBilling;
    private Long selectedShippingMethod;
    private String[] promotionalCode;
    private String delPromotionalCode;
    private Long billingAddressSelected;
    private Long shippingAddressSelected;

    private String mailFromName;
    private String mailFrom;
    private String mailTo;
    private String mailComment;

    private String paymentService;

    private Long idStaticText;
    private StaticText staticText;
    private String[] staticTextTitle;
    private String[] staticTextValue;

    private String shippingType;
    private Long shippingStore;
    private String customReference;
    private String customMessage;

    private Long idBilling;
    private Long idShipping;
    private Long idInsurance;
    private Long idStore;
    private Long idNews;

    private String blockCode;
    private String zipCode;

    private String captcha;

    private String term;
    private List<Map<String, String>> quickSearchResult;

    private InputStream inputStream;
    private String contentType;


    public InputStream getInputStream() {
        return inputStream;
    }

    public void setInputStream(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public Map<String, String> getJsonResult() {
        return jsonResult;
    }

    public void setJsonResult(Map<String, String> jsonResult) {
        this.jsonResult = jsonResult;
    }

    public String getTerm() {
        return term;
    }

    public void setTerm(String term) {
        this.term = term;
    }

    public List<Map<String, String>> getQuickSearchResult() {
        return quickSearchResult;
    }

    public void setQuickSearchResult(List<Map<String, String>> quickSearchResult) {
        this.quickSearchResult = quickSearchResult;
    }

    public String getCaptcha() {
        return captcha;
    }

    public void setCaptcha(String captcha) {
        this.captcha = captcha;
    }

    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    public String getBlockCode() {
        return blockCode;
    }

    public void setBlockCode(String blockCode) {
        this.blockCode = blockCode;
    }

    public Long getIdStore() {
        return idStore;
    }

    public void setIdStore(Long idStore) {
        this.idStore = idStore;
    }

    public Long getIdBilling() {
        return idBilling;
    }

    public void setIdBilling(Long idBilling) {
        this.idBilling = idBilling;
    }

    public Long getIdShipping() {
        return idShipping;
    }

    public void setIdShipping(Long idShipping) {
        this.idShipping = idShipping;
    }

    public Long getIdInsurance() {
        return idInsurance;
    }

    public void setIdInsurance(Long idInsurance) {
        this.idInsurance = idInsurance;
    }

    public Long getShippingStore() {
        return shippingStore;
    }

    public void setShippingStore(Long shippingStore) {
        this.shippingStore = shippingStore;
    }

    public String getShippingType() {
        return shippingType;
    }

    public void setShippingType(String shippingType) {
        this.shippingType = shippingType;
    }

    public Long getIdStaticText() {
        return idStaticText;
    }

    public void setIdStaticText(Long idStaticText) {
        this.idStaticText = idStaticText;
    }

    public StaticText getStaticText() {
        return staticText;
    }

    public void setStaticText(StaticText staticText) {
        this.staticText = staticText;
    }

    public String[] getStaticTextTitle() {
        return staticTextTitle;
    }

    public void setStaticTextTitle(String[] staticTextTitle) {
        this.staticTextTitle = staticTextTitle;
    }

    public String[] getStaticTextValue() {
        return staticTextValue;
    }

    public void setStaticTextValue(String[] staticTextValue) {
        this.staticTextValue = staticTextValue;
    }

    public String getOutput() {
        return output;
    }

    public void setOutput(String output) {
        this.output = output;
    }

    public String getMailFromName() {
        return mailFromName;
    }

    public void setMailFromName(String mailFromName) {
        this.mailFromName = mailFromName;
    }

    public String getMailFrom() {
        return mailFrom;
    }

    public void setMailFrom(String mailFrom) {
        this.mailFrom = mailFrom;
    }

    public String getMailTo() {
        return mailTo;
    }

    public void setMailTo(String mailTo) {
        this.mailTo = mailTo;
    }

    public String getMailComment() {
        return mailComment;
    }

    public void setMailComment(String mailComment) {
        this.mailComment = mailComment;
    }

    public Long getIdOrder() {
        return idOrder;
    }

    public void setIdOrder(Long idOrder) {
        this.idOrder = idOrder;
    }

    public String getPaymentService() {
        return paymentService;
    }

    public void setPaymentService(String paymentService) {
        this.paymentService = paymentService;
    }

    public String[] getPromotionalCode() {
        return promotionalCode;
    }

    public void setPromotionalCode(String[] promotionalCode) {
        this.promotionalCode = promotionalCode;
    }

    public String getDelPromotionalCode() {
        return delPromotionalCode;
    }

    public void setDelPromotionalCode(String delPromotionalCode) {
        this.delPromotionalCode = delPromotionalCode;
    }

    public UserAddress getBillingAddress() {
        return billingAddress;
    }

    public void setBillingAddress(UserAddress billingAddress) {
        this.billingAddress = billingAddress;
    }

    public UserAddress getShippingAddress() {
        return shippingAddress;
    }

    public void setShippingAddress(UserAddress shippingAddress) {
        this.shippingAddress = shippingAddress;
    }

    public Long getBillingAddressState() {
        return billingAddressState;
    }

    public void setBillingAddressState(Long billingAddressState) {
        this.billingAddressState = billingAddressState;
    }

    public Long getShippingAddressState() {
        return shippingAddressState;
    }

    public void setShippingAddressState(Long shippingAddressState) {
        this.shippingAddressState = shippingAddressState;
    }

    public Boolean getShippingAddressUseBilling() {
        return shippingAddressUseBilling;
    }

    public void setShippingAddressUseBilling(Boolean shippingAddressUseBilling) {
        this.shippingAddressUseBilling = shippingAddressUseBilling;
    }

    public Long getBillingAddressSelected() {
        return billingAddressSelected;
    }

    public void setBillingAddressSelected(Long billingAddressSelected) {
        this.billingAddressSelected = billingAddressSelected;
    }

    public Long getShippingAddressSelected() {
        return shippingAddressSelected;
    }

    public void setShippingAddressSelected(Long shippingAddressSelected) {
        this.shippingAddressSelected = shippingAddressSelected;
    }

    public Long getSelectedShippingMethod() {
        return selectedShippingMethod;
    }

    public void setSelectedShippingMethod(Long selectedShippingMethod) {
        this.selectedShippingMethod = selectedShippingMethod;
    }

    public Long getIdResource() {
        return idResource;
    }

    public void setIdResource(Long idResource) {
        this.idResource = idResource;
    }

    public ProductReview getReview() {
        return review;
    }

    public void setReview(ProductReview review) {
        this.review = review;
    }

    public String[] getReview_score_name() {
        return review_score_name;
    }

    public void setReview_score_name(String[] review_score_name) {
        this.review_score_name = review_score_name;
    }

    public Integer[] getReview_score_value() {
        return review_score_value;
    }

    public void setReview_score_value(Integer[] review_score_value) {
        this.review_score_value = review_score_value;
    }

    public ProductFilter getProductFilter() {
        return productFilter;
    }

    public void setProductFilter(ProductFilter productFilter) {
        this.productFilter = productFilter;
    }

    public Long getIdProduct() {
        return idProduct;
    }

    public void setIdProduct(Long idProduct) {
        this.idProduct = idProduct;
    }

    public Long getIdCategory() {
        return idCategory;
    }

    public void setIdCategory(Long idCategory) {
        this.idCategory = idCategory;
    }

    public Long getIdBanner() {
        return idBanner;
    }

    public void setIdBanner(Long idBanner) {
        this.idBanner = idBanner;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public Banner getBanner() {
        return banner;
    }

    public void setBanner(Banner banner) {
        this.banner = banner;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getCustomReference() {
        return customReference;
    }

    public void setCustomReference(String customReference) {
        this.customReference = customReference;
    }

    public String getCustomMessage() {
        return customMessage;
    }

    public void setCustomMessage(String customMessage) {
        this.customMessage = customMessage;
    }

    public Long getIdNews() {
        return idNews;
    }

    public void setIdNews(Long idNews) {
        this.idNews = idNews;
    }
}
