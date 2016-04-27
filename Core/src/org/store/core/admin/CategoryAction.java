package org.store.core.admin;

import org.store.core.beans.*;
import org.store.core.beans.utils.BreadCrumb;
import org.store.core.dao.HibernateDAO;
import org.store.core.globals.SomeUtils;
import org.store.core.globals.StoreThread;
import org.store.core.globals.config.Store20Database;
import org.store.core.hibernate.HibernateSessionFactory;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.hibernate.Session;

import javax.swing.tree.DefaultMutableTreeNode;
import java.io.File;
import java.io.Serializable;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: Administrador
 * Date: Mar 22, 2010
 */
@Namespace(value = "/admin")
@ParentPackage(value = "store-admin")
public class CategoryAction extends AdminModuleAction {
    private static final String EXPORT_FOLDER_CATEGORY = "categories";
    private static final String CNT_ERROR_CANNOT_DETELE_CATEGORY = "error.cannot.delete.category";
    private static final String CNT_DEFAULT_ERROR_CANNOT_DETELE_CATEGORY = "Can not delete category {0}. {1}";
    private static final int MAX_CATEGORIES = 50;

    @Override
    public void prepare() throws Exception {
        category = dao.getCategory(idCategory);
        if (category != null && category.getIdCategory() != null) {
            parent = dao.getCategory(category.getIdParent());

            categoryParents = dao.getCategoryParents(category);
            categoryChildren = dao.getCategoryChildren(category, getDefaultLanguage());

            categoryProperties = dao.getCategoryProperties(category);
            categoryPropertiesParent = (parent != null) ? dao.getParentCategoryProperties(category, false) : null;


            categoryStaticTexts = dao.getCategoryStaticTexts(category);
            categoryStaticTextsParent = (parent != null) ? dao.getParentCategoryStaticTexts(parent) : null;

            // Esto es para eliminar los attributos q ya estan definidos en algun padre
            if (CollectionUtils.isNotEmpty(categoryProperties) && CollectionUtils.isNotEmpty(categoryPropertiesParent)) {
                Set<AttributeProd> parentAtts = new HashSet<AttributeProd>();
                for (CategoryProperty cp : categoryPropertiesParent) {
                    parentAtts.add(cp.getAttribute());
                }
                for (CategoryProperty cp : categoryProperties) {
                    if (parentAtts.contains(cp.getAttribute())) cp.addProperty("IN_PARENT", Boolean.TRUE);
                }
            }
        } else {
            parent = dao.getCategory(idParent);
        }
        super.prepare();
    }

    @Action(value = "categoryselector", results = @Result(type = "velocity", location = "/WEB-INF/views/admin/categorylist_selector.vm"))
    public String categoryselector() throws Exception {
        return categorylist();
    }

    @Action(value = "categorychildrenfix", results = @Result(type = "redirectAction", location = "categorylist"))
    public String categorychildrenfix() throws Exception {
        Category root = dao.getRootCategory();
        List<Category> children = dao.getChildCategories(null, false);
        for(Category cat : children) {
            if (!cat.isRootCategory()) {
                cat.setIdParent(root.getIdCategory());
                dao.save(cat);
            }
        }
        return SUCCESS;
    }

    @Action(value = "categorychildren", results = @Result(type = "velocity", location = "/WEB-INF/views/admin/categorychildren.vm"))
    public String categorychildren() throws Exception {
        if (idParent != null) {
            Category cat = dao.getCategory(idParent);
            if (cat != null) {
                List children = dao.getChildCategories(cat, false);
                if (children != null && !children.isEmpty()) addToStack("categories", children);
            }
        }
        return SUCCESS;
    }

    @Action(value = "categorylist", results = @Result(type = "velocity", location = "/WEB-INF/views/admin/categorylist.vm"))
    public String categorylist() throws Exception {

        if (categoryUpdate != null && "Y".equalsIgnoreCase(categoryUpdate)) {

            if (categoryId != null && categoryId.length > 0) {
                for (int i = 0; i < categoryId.length; i++) {
                    Category bean = dao.getCategory(categoryId[i]);
                    if (bean != null) {
                        if (categoryActive != null && categoryActive.length > i) bean.setActive("Y".equalsIgnoreCase(categoryActive[i]));
                        if (categoryVisible != null && categoryVisible.length > i) bean.setVisible("Y".equalsIgnoreCase(categoryVisible[i]));
                        if (categoryNeedShipping != null && categoryNeedShipping.length > i) bean.setNeedShipping(categoryNeedShipping[i]);
                        if (categoryMarkup != null && categoryMarkup.length > i && categoryMarkup[i] != null) bean.setMarkupFactor(SomeUtils.strToDouble(categoryMarkup[i]));
                        if (categoryERMarkup != null && categoryERMarkup.length > i && categoryERMarkup[i] != null) bean.setErMarkupFactor(SomeUtils.strToDouble(categoryERMarkup[i]));
                        if (categoryPosition != null && categoryPosition.length > i && categoryPosition[i] != null) bean.setDefaultPosition(SomeUtils.strToInteger(categoryPosition[i]));
                        dao.save(bean);
                    }
                }
                dao.flushSession();
            }

            if (selecteds != null && selecteds.length > 0) {
                for (Long id : selecteds) {
                    Category cat = (Category) dao.get(Category.class, id);
                    if (cat != null) {
                        String res = dao.isUsedCategory(cat, getDefaultLanguage());
                        if (StringUtils.isNotEmpty(res)) {
                            addActionError(getText(CNT_ERROR_CANNOT_DETELE_CATEGORY, CNT_DEFAULT_ERROR_CANNOT_DETELE_CATEGORY, new String[]{cat.getCategoryName(getDefaultLanguage()), res}));
                        } else {
                            dao.deleteCategory(cat);
                            dao.flushSession();
                        }
                    }
                }
                dao.flushSession();
            }

        }

        addToStack("can_export", actionExist("export_categories", "/admin"));

        Category root = dao.getRootCategory();
        List<Category> l = new ArrayList<Category>();
        List<Category> l1 = dao.getChildCategories(root, false);
        if (l1!=null && !l1.isEmpty()) l.addAll(l1);
        List<Category> l2 = dao.getChildCategories(null, false);
        for(Category c : l2) {
            if (!c.isRootCategory() && !l.contains(c)) l.add(c);
        }
        addToStack("categories", l);
        addToStack("cat", root);

        getBreadCrumbs().add(new BreadCrumb(null, getText("admin.category.list"), null, null));
        return SUCCESS;
    }

    @Action(value = "categorydata", results = @Result(type = "velocity", location = "/WEB-INF/views/admin/categorydata.vm"))
    public String categorydata() throws Exception {
        addToStack("categoryFilter", dao.getCategoriesCount() < MAX_CATEGORIES);
        getBreadCrumbs().add(new BreadCrumb(null, getText("admin.category.list"), url("categorylist", "/admin"), null));
        getBreadCrumbs().add(new BreadCrumb(null, getText("admin.category"), null, null));
        return SUCCESS;
    }

    @Action(value = "categorysave", results = {
            @Result(type = "redirectAction", location = "categorylist"),
            @Result(name = "input", type = "velocity", location = "/WEB-INF/views/admin/categorydata.vm"),
            @Result(name = "reedit", type = "redirectAction", location = "categorydata?idCategory=${category.idCategory}")
    })
    public String categorysave() throws Exception {
        boolean isNew = false;
        if (category != null) {
            isNew = category.getIdCategory() == null || category.getIdCategory() < 1;
            category.setInventaryCode(getStoreCode());
            category.setCountriesCanBuyList(buyFromCountries);
            dao.save(category);
            for (int l = 0; l < getLanguages().length; l++) {
                String lang = getLanguages()[l];
                CategoryLang catLang = dao.getCategoryLang(category, lang);
                if (catLang == null) {
                    catLang = new CategoryLang();
                    catLang.setCategory(category);
                    catLang.setCategoryLang(lang);
                }
                catLang.setCategoryName(categoryName[l]);
                catLang.setDescription(description[l]);
                catLang.resetMetas();
                if (metaTitle != null && metaTitle.length > l && StringUtils.isNotEmpty(metaTitle[l])) catLang.addMeta("title", metaTitle[l], metaTitleAppend);
                if (metaDescription != null && metaDescription.length > l && StringUtils.isNotEmpty(metaDescription[l])) catLang.addMeta("description", metaDescription[l], metaDescriptionAppend);
                if (metaKeywords != null && metaKeywords.length > l && StringUtils.isNotEmpty(metaKeywords[l])) catLang.addMeta("keywords", metaKeywords[l], metaKeywordsAppend);
                if (metaAbstract != null && metaAbstract.length > l && StringUtils.isNotEmpty(metaAbstract[l])) catLang.addMeta("abstract", metaAbstract[l], metaAbstractAppend);
                if (metasName != null && metasName.length > 0 && metasValue != null && metasValue.length == metasName.length) {
                    for (int i = 0; i < metasName.length; i++) {
                        if (StringUtils.isNotEmpty(metasName[i]) && StringUtils.isNotEmpty(metasValue[i])) {
                            catLang.addMeta(metasName[i], metasValue[i], (metasAppend != null && metasAppend.length > i) ? metasAppend[i] : false);
                        }
                    }
                }
                dao.save(catLang);
            }

            if (!category.isRootCategory()) {
                dao.updateCategoryUrlCode(category, seoUrl, getDefaultLanguage());
                if (category.getIdParent() != null) {
                    parent = (Category) dao.get(Category.class, category.getIdParent());
                    if (parent != null && !parent.isRootCategory()) {
                        CategoryTree ct = dao.getCategoryTree(parent, category);
                        if (ct == null) {
                            ct = new CategoryTree();
                            ct.setChild(category);
                            ct.setParent(parent);
                            dao.save(ct);
                        }
                    }
                }
            }
        }
        return isNew ? "reedit" : SUCCESS;
    }

    @Action(value = "categorytreeadd", results = @Result(type = "velocity", location = "/WEB-INF/views/admin/categorydata_tree.vm"))
    public String categoryTreeAdd() throws Exception {
        if (idParent != null && category != null && !category.isRootCategory()) {
            Category parentCat = dao.getCategory(idParent);
            if (parentCat != null && !parentCat.equals(category)) {
                CategoryTree bean = dao.getCategoryTree(parentCat, category);
                if (bean == null) {
                    bean = new CategoryTree();
                    bean.setChild(category);
                    bean.setParent(parentCat);
                    dao.save(bean);
                }
            }
        } else if (idChild != null && category != null && !category.isRootCategory()) {
            int position = 1;
            for (Long id : idChild) {
                if (id != null && !id.equals(category.getIdCategory())) {
                    Category childCat = dao.getCategory(id);
                    if (childCat != null) {
                        CategoryTree bean = dao.getCategoryTree(category, childCat);
                        if (bean == null) {
                            bean = new CategoryTree();
                            bean.setChild(childCat);
                            bean.setParent(category);
                        }
                        bean.setPosition(position++);
                        dao.save(bean);
                    }
                }
            }
        }
        categoryParents = dao.getCategoryParents(category);
        categoryChildren = dao.getCategoryChildren(category, getDefaultLanguage());
        return SUCCESS;
    }

    @Action(value = "categorytreedel", results = @Result(type = "velocity", location = "/WEB-INF/views/admin/categorydata_tree.vm"))
    public String categoryTreeDel() throws Exception {
        if (idParent != null && category != null) {
            Category parentCat = dao.getCategory(idParent);
            if (parentCat != null) {
                CategoryTree bean = dao.getCategoryTree(parentCat, category);
                if (bean != null) dao.delete(bean);
            }
        } else if (idChild != null && category != null) {
            for (Long id : idChild) {
                Category childCat = dao.getCategory(id);
                if (childCat != null) {
                    CategoryTree bean = dao.getCategoryTree(category, childCat);
                    if (bean != null) dao.delete(bean);
                }
            }
        }
        categoryParents = dao.getCategoryParents(category);
        categoryChildren = dao.getCategoryChildren(category, getDefaultLanguage());
        return SUCCESS;
    }

    @Action(value = "categorypricesave", results = {
            @Result(type = "velocity", location = "/WEB-INF/views/admin/categorydata_price.vm"),
            @Result(name = "input", type = "velocity", location = "/WEB-INF/views/admin/categorydata_price.vm")
    })
    public String categoryPriceSave() throws Exception {
        if (category != null) {
            // UserLevel discount
            if (levelId != null) {
                for (int i = 0; i < levelId.length; i++) {
                    if (levelId[i] != null) {
                        UserLevel uLevel = (UserLevel) dao.get(UserLevel.class, levelId[i]);
                        if (uLevel != null) {
                            CategoryUserLevel bean = dao.getCategoryUserLevel(category, uLevel);
                            if (levelPercent[i] != null) {
                                if (bean == null) {
                                    bean = new CategoryUserLevel();
                                    bean.setCategory(category);
                                    bean.setLevel(uLevel);
                                }
                                bean.setPercentDiscount(levelPercent[i]);
                                dao.save(bean);
                            } else if (bean != null) {
                                dao.delete(bean);
                            }
                        }
                    }
                }
            }
            // Volume Prices
            if (volumenId != null) {
                // Delete
                List<CategoryVolume> categoryVolume = dao.getCategoryVolume(category);
                for (CategoryVolume bean : categoryVolume) {
                    if (ArrayUtils.indexOf(volumen, bean.getVolume()) < 0) dao.delete(bean);
                }
                // Add new
                for (int i = 0; i < volumen.length; i++) {
                    if (volumen[i] != null && volumenPercent[i] != null) {
                        CategoryVolume bean = dao.getCategoryVolume(category, volumen[i]);
                        if (bean == null) {
                            bean = new CategoryVolume();
                            bean.setCategory(category);
                            bean.setVolume(volumen[i]);
                        }
                        bean.setPercentDiscount(volumenPercent[i]);
                        bean.setDescription(volumenDescription[i]);
                        dao.save(bean);
                    }
                }
                requestCache.remove("categoryVolume");
            }
            // Promotions
            if (promotionId != null) {
                for (int i = 0; i < promotionId.length; i++) {
                    ProductOffer bean = (promotionId[i] != null) ? (ProductOffer) dao.get(ProductOffer.class, promotionId[i]) : null;
                    if (promotionPercent[i] != null || promotionValue[i] != null) {
                        if (bean == null) bean = new ProductOffer();
                        bean.setRuleObject(ProductOffer.RULE_CATEGORY);
                        bean.setRuleOperation(ProductOffer.OPER_EQUAL);
                        bean.setRuleValue(category.getIdCategory().toString());
                        bean.setDateFrom(SomeUtils.strToDate(promotionFrom[i], getDefaultLanguage()));
                        bean.setDateTo(SomeUtils.strToDate(promotionTo[i], getDefaultLanguage()));
                        bean.setDiscount(promotionValue[i]);
                        bean.setPercent(promotionPercent[i]);
                        dao.save(bean);
                    } else {
                        if (bean != null && bean.isForCategory(category)) dao.delete(bean);
                    }
                }
            }
            // Fees (Esto tiene su pagina aparte)

            // Shipping Rates
            if (shippingState != null && shippingState.length > 0) {
                for (int i = 0; i < shippingState.length; i++) {
                    Long sState = shippingState[i];
                    State state = (State) dao.get(State.class, sState);
                    String sType = (shippingType != null && shippingType.length > i) ? shippingType[i] : null;
                    Double sValue = (shippingValue != null && shippingValue.length > i) ? shippingValue[i] : null;
                    if (state != null) {
                        ShippingRate bean = dao.getCategoryShipping(category, state);
                        if (sValue != null) {
                            if (bean == null) bean = new ShippingRate();
                            bean.setCategory(category);
                            bean.setProduct(null);
                            bean.setState(state);
                            bean.setValue(sValue);
                            bean.setShippingType(sType);
                            dao.save(bean);
                        } else if (bean != null) {
                            dao.delete(bean);
                        }
                    }
                }
            }

        }
        return SUCCESS;
    }

    public String categoryLevelsSave() throws Exception {
        if (category != null && levelId != null) {
            for (int i = 0; i < levelId.length; i++) {
                if (levelId[i] != null) {
                    UserLevel uLevel = (UserLevel) dao.get(UserLevel.class, levelId[i]);
                    if (uLevel != null) {
                        CategoryUserLevel bean = dao.getCategoryUserLevel(category, uLevel);
                        if (levelPercent[i] != null) {
                            if (bean == null) {
                                bean = new CategoryUserLevel();
                                bean.setCategory(category);
                                bean.setLevel(uLevel);
                            }
                            bean.setPercentDiscount(levelPercent[i]);
                            dao.save(bean);
                        } else if (bean != null) {
                            dao.delete(bean);
                        }
                    }
                }
            }
        }
        return SUCCESS;
    }

    public String categoryVolumenSave() throws Exception {
        if (category != null && volumenId != null) {
            // Delete
            List<CategoryVolume> categoryVolume = dao.getCategoryVolume(category);
            for (CategoryVolume bean : categoryVolume) {
                if (ArrayUtils.indexOf(volumen, bean.getVolume()) < 0) dao.delete(bean);
            }
            // Add new
            for (int i = 0; i < volumen.length; i++) {
                if (volumen[i] != null && volumenPercent[i] != null) {
                    CategoryVolume bean = dao.getCategoryVolume(category, volumen[i]);
                    if (bean == null) {
                        bean = new CategoryVolume();
                        bean.setCategory(category);
                        bean.setVolume(volumen[i]);
                    }
                    bean.setPercentDiscount(volumenPercent[i]);
                    bean.setDescription(volumenDescription[i]);
                    dao.save(bean);
                }
            }
            requestCache.remove("categoryVolume");
        }
        return SUCCESS;
    }

    public String categoryPromotionSave() throws Exception {
        if (category != null && promotionId != null) {
            for (int i = 0; i < promotionId.length; i++) {
                ProductOffer bean = (promotionId[i] != null) ? (ProductOffer) dao.get(ProductOffer.class, promotionId[i]) : null;
                if (promotionPercent[i] != null || promotionValue[i] != null) {
                    if (bean == null) bean = new ProductOffer();
                    bean.setRuleObject(ProductOffer.RULE_CATEGORY);
                    bean.setRuleOperation(ProductOffer.OPER_EQUAL);
                    bean.setRuleValue(category.getIdCategory().toString());
                    bean.setDateFrom(SomeUtils.strToDate(promotionFrom[i], getDefaultLanguage()));
                    bean.setDateTo(SomeUtils.strToDate(promotionTo[i], getDefaultLanguage()));
                    bean.setDiscount(promotionValue[i]);
                    bean.setDiscountPercent(promotionPercent[i]);
                    dao.save(bean);
                } else {
                    if (bean != null && bean.isForCategory(category)) dao.delete(bean);
                }
            }
        }
        return SUCCESS;
    }

    public String categoryProperties() throws Exception {
        categoryProperties = (category != null) ? dao.getCategoryProperties(category) : null;
        categoryPropertiesParent = (category != null && category.getIdParent() != null) ? dao.getParentCategoryProperties(category, false) : null;
        return SUCCESS;
    }

    @Action(value = "categorypropertiesadd", results = {
            @Result(type = "velocity", location = "/WEB-INF/views/admin/categorydata_properties.vm"),
            @Result(name = "input", type = "velocity", location = "/WEB-INF/views/admin/categorydata_properties.vm")
    })
    public String categoryPropertiesAdd() throws Exception {
        if (category != null) {
            // Eliminar los q no estan
            categoryProperties = dao.getCategoryProperties(category);
            if (categoryProperties != null) {
                for (CategoryProperty bean : categoryProperties) {
                    if (ArrayUtils.indexOf(selectedProductAttribute, bean.getAttribute().getId()) < 0)
                        dao.delete(bean);
                }
            }
            // Adicionar
            if (selectedProductAttribute != null) {
                for (Long idProdAtt : selectedProductAttribute) {
                    AttributeProd beanAtt = (AttributeProd) dao.get(AttributeProd.class, idProdAtt);
                    if (beanAtt != null) {
                        CategoryProperty bean = dao.getCategoryProperty(category, beanAtt);
                        if (bean == null) {
                            bean = new CategoryProperty();
                            bean.setCategory(category);
                            bean.setAttribute(beanAtt);
                            bean.setCanfilter(false);
                            dao.save(bean);
                        }
                    }
                }
            }
        }
        return categoryProperties();
    }

    @Action(value = "categorypropertiessave", results = {
            @Result(type = "velocity", location = "/WEB-INF/views/admin/categorydata_properties.vm"),
            @Result(name = "input", type = "velocity", location = "/WEB-INF/views/admin/categorydata_properties.vm")
    })
    public String categoryPropertiesSave() throws Exception {
        if (category != null && propertiesId != null) {
            for (int i = 0; i < propertiesId.length; i++) {
                CategoryProperty bean = (propertiesId[i] != null) ? (CategoryProperty) dao.get(CategoryProperty.class, propertiesId[i]) : null;
                if (bean != null) {
                    bean.setCategory(category);
                    bean.setCanfilter("Y".equalsIgnoreCase(propertiesFilter[i]));
                    bean.setOrderFilter(propertiesSort[i]);
                    dao.save(bean);
                }
            }
        }
        return categoryProperties();
    }

    public String categoryStaticTexts() throws Exception {
        categoryStaticTexts = dao.getCategoryStaticTexts(category);
        categoryStaticTextsParent = (parent != null) ? dao.getParentCategoryStaticTexts(parent) : null;
        return SUCCESS;
    }

    @Action(value = "categorystatictextsadd", results = {
            @Result(type = "velocity", location = "/WEB-INF/views/admin/categorydata_statictexts.vm"),
            @Result(name = "input", type = "velocity", location = "/WEB-INF/views/admin/categorydata_statictexts.vm")
    })
    public String categoryStaticTextAdd() throws Exception {
        if (category != null) {
            // Eliminar los q no estan
            categoryStaticTexts = dao.getCategoryStaticTexts(category);
            if (categoryStaticTexts != null) {
                for (CategoryStaticText bean : categoryStaticTexts) {
                    if (ArrayUtils.indexOf(selectedStaticText, bean.getStaticText().getId()) < 0)
                        dao.delete(bean);
                }
            }
            // Adicionar
            if (selectedStaticText != null) {
                for (Long idST : selectedStaticText) {
                    StaticText beanST = (StaticText) dao.get(StaticText.class, idST);
                    if (beanST != null) {
                        CategoryStaticText bean = dao.getCategoryStaticText(category, beanST);
                        if (bean == null) {
                            bean = new CategoryStaticText();
                            bean.setCategory(category);
                            bean.setStaticText(beanST);
                            dao.save(bean);
                        }
                    }
                }
            }
        }
        return categoryStaticTexts();
    }

    @Action(value = "categorystatictextssave", results = {
            @Result(type = "velocity", location = "/WEB-INF/views/admin/categorydata_statictexts.vm"),
            @Result(name = "input", type = "velocity", location = "/WEB-INF/views/admin/categorydata_statictexts.vm")
    })
    public String categoryStaticTextSave() throws Exception {
        if (category != null && contentStaticTextId != null) {
            for (int i = 0; i < contentStaticTextId.length; i++) {
                CategoryStaticText bean = (contentStaticTextId[i] != null) ? (CategoryStaticText) dao.get(CategoryStaticText.class, contentStaticTextId[i]) : null;
                if (bean != null) {
                    bean.setCategory(category);
                    bean.setContentOrder(contentOrder[i]);
                    bean.setContentPlace(contentPlace[i]);
                    dao.save(bean);
                }
            }
        }
        return categoryStaticTexts();
    }

    public String categoryFeeSave() throws Exception {
        if (feeId != null && feeId.length > 0) {
            for (int i = 0; i < feeId.length; i++) {
                Fee fee = (Fee) dao.get(Fee.class, feeId[i]);
                if (fee != null) {
                    CategoryFee bean = dao.getCategoryFee(fee, category);
                    if (feeValue[i] != null) {
                        if (bean == null) {
                            bean = new CategoryFee();
                            bean.setCategory(category);
                            bean.setFee(fee);
                        }
                        bean.setValue(feeValue[i]);
                        dao.save(bean);
                    } else if (bean != null) {
                        dao.delete(bean);
                    }
                }
            }
        }
        return SUCCESS;
    }

    @Action(value = "categorycomplementadd", results = @Result(type = "velocity", location = "/WEB-INF/views/admin/categorydata_complements.vm"))
    public String categoryComplementAdd() throws Exception {
        if (category != null && idGroup != null && idGroup.length > 0) {
            if (category.getRelatedGroups() == null) category.setRelatedGroups(new HashSet<ComplementGroup>());
            for (Long id : idGroup) {
                ComplementGroup group = (ComplementGroup) dao.get(ComplementGroup.class, id);
                if (group != null && !category.getRelatedGroups().contains(group)) {
                    category.getRelatedGroups().add(group);
                }
            }
            dao.save(category);
        }
        return SUCCESS;
    }

    @Action(value = "categorycomplementdel", results = @Result(type = "velocity", location = "/WEB-INF/views/admin/categorydata_complements.vm"))
    public String categoryComplementDel() throws Exception {
        if (category != null && category.getRelatedGroups() != null && idGroup != null && idGroup.length > 0) {
            for (Long id : idGroup) {
                ComplementGroup group = (ComplementGroup) dao.get(ComplementGroup.class, id);
                if (group != null && category.getRelatedGroups().contains(group)) {
                    category.getRelatedGroups().remove(group);
                }
            }
            dao.save(category);
        }
        return SUCCESS;
    }

    @Action(value = "categorypricerange", results = @Result(type = "velocity", location = "/WEB-INF/views/admin/categorydata_pricerange.vm"))
    public String categoryPriceRange() throws Exception {
        if (category != null) {
            List<Map<String, Double>> list = new ArrayList<Map<String, Double>>();
            if (minPrice != null && minPrice.length > 0 && ArrayUtils.isSameLength(minPrice, maxPrice)) {
                for (int i = 0; i < minPrice.length; i++) {
                    Double min = SomeUtils.strToDouble(minPrice[i]);
                    Double max = SomeUtils.strToDouble(maxPrice[i]);
                    if (min != null || max != null) {
                        Map map = new HashMap();
                        if (min != null) map.put("min", min);
                        if (max != null) map.put("max", max);
                        list.add(map);
                    }
                }
            }

            if (!list.isEmpty()) {
                // ordernar
                Collections.sort(list, new Comparator<Map<String, Double>>() {
                    public int compare(Map<String, Double> o1, Map<String, Double> o2) {
                        Double m1 = (o1 != null && o1.containsKey("min")) ? o1.get("min") : -1;
                        Double m2 = (o2 != null && o2.containsKey("min")) ? o2.get("min") : -1;
                        return m1.compareTo(m2);
                    }
                });
            }

            category.setPriceRange(list);
            dao.save(category);
        }
        return SUCCESS;
    }

    @Action(value = "categoryupdateproducts", results = @Result(type = "json", params = {"root", "jsonResp"}))
    public String categoryUpdateProducts() throws Exception {
        jsonResp = new HashMap<String, Serializable>();

        if (category != null) {
            UpdateProductThread thread = new UpdateProductThread(getDatabaseConfig(), getStoreCode(), category.getIdCategory());
            thread.setName("job" + getStoreCode() + "updcatprod" + category.getIdCategory().toString());
            jsonResp.put("result", "ok");
            thread.start();
        }

        return SUCCESS;
    }


    @Action(value = "categoryaddlabels", results = {
            @Result(type = "velocity", name = "input", location = "/WEB-INF/views/admin/categorydata_labels.vm"),
            @Result(type = "velocity", location = "/WEB-INF/views/admin/categorydata_labels.vm")
    })
    public String categoryAddLabels() throws Exception {
        if (category != null && labels != null) {
            for (String l : labels) {
                ProductLabel bean = dao.getProductLabelByCode(l);
                if (bean != null) {
                    category.addLabel(bean);
                    dao.save(category);
                }
            }
        }
        return SUCCESS;
    }

    @Action(value = "categorydellabels", results = {
            @Result(type = "velocity", name = "input", location = "/WEB-INF/views/admin/categorydata_labels.vm"),
            @Result(type = "velocity", location = "/WEB-INF/views/admin/categorydata_labels.vm")
    })
    public String categoryDelLabels() throws Exception {
        if (category != null && labels != null) {
            for (String l : labels) {
                category.delLabel(l);
                dao.save(category);
            }
        }
        return SUCCESS;
    }

    @Action(value = "categorydel", results = @Result(type = "redirectAction", location = "categorylist"))
    public String categorydel() throws Exception {
        if (category != null) {
            dao.delete(category);
        }
        return SUCCESS;
    }

    @Action(value = "categoryupdateurl")
    public String updateCodeNames() throws Exception {
        int oks = 0;
        int errors = 0;
        List<Category> listado = dao.getCategories(false);

        for (Category bean : listado) {
            try {
                if (dao.updateCategoryUrlCode(bean, null, getDefaultLanguage())) oks++;
                else errors++;
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
        response.getWriter().println("Categories OK: " + String.valueOf(oks));
        response.getWriter().println("Categories Error: " + String.valueOf(errors));
        return null;
    }

    public List<ProductOffer> getCategoryOffers() {
        if (!requestCache.containsKey("categoryOffers")) requestCache.put("categoryOffers", dao.getCategoryOffers(category));
        return (List<ProductOffer>) requestCache.get("categoryOffers");
    }

    public List<ProductOffer> getCategoryOffersParent() {
        if (CollectionUtils.isEmpty(getCategoryOffers()) && parent != null) {
            if (!requestCache.containsKey("parentCategoryOffers")) requestCache.put("parentCategoryOffers", dao.getParentCategoryOffers(parent));
            return (List<ProductOffer>) requestCache.get("parentCategoryOffers");
        }
        return null;
    }

    public List<CategoryVolume> getCategoryVolume() {
        if (!requestCache.containsKey("categoryVolume")) requestCache.put("categoryVolume", dao.getCategoryVolume(category));
        return (List<CategoryVolume>) requestCache.get("categoryVolume");
    }

    public List<CategoryVolume> getCategoryVolumeParent() {
        if (CollectionUtils.isEmpty(getCategoryVolume()) && parent != null) {
            if (!requestCache.containsKey("parentCategoryVolume")) requestCache.put("parentCategoryVolume", dao.getParentCategoryVolume(parent));
            return (List<CategoryVolume>) requestCache.get("parentCategoryVolume");
        }
        return null;
    }

    public List<CategoryFee> getCategoryFee() {
        if (!requestCache.containsKey("categoryFee")) requestCache.put("categoryFee", dao.getCategoryFee(category));
        return (List<CategoryFee>) requestCache.get("categoryFee");
    }

    public List<CategoryFee> getCategoryFeeParent() {
        if (CollectionUtils.isEmpty(getCategoryFee()) && parent != null) {
            if (!requestCache.containsKey("parentCategoryFee")) requestCache.put("parentCategoryFee", dao.getParentCategoryFee(category));
            return (List<CategoryFee>) requestCache.get("parentCategoryFee");
        }
        return null;
    }

    public Set<ProductLabel> getCategoryLabelsParent() {
        if (parent != null) {
            if (!requestCache.containsKey("parentCategoryLabels")) requestCache.put("parentCategoryLabels", dao.getParentCategoryLabels(parent));
            return (Set<ProductLabel>) requestCache.get("parentCategoryLabels");
        }
        return null;
    }

    public Map<State, ShippingRate> getCategoryShipping() {
        if (!requestCache.containsKey("categoryShipping")) requestCache.put("categoryShipping", dao.getCategoryShipping(category));
        return (Map<State, ShippingRate>) requestCache.get("categoryShipping");
    }

    public Map<State, ShippingRate> getCategoryShippingParent() {
        if (parent != null) {
            if (!requestCache.containsKey("parentCategoryShipping")) requestCache.put("parentCategoryShipping", dao.getParentCategoryShipping(parent));
            return (Map<State, ShippingRate>) requestCache.get("parentCategoryShipping");
        }
        return null;
    }

    public Set<ComplementGroup> getCategoryComplementParent() {
        if (parent != null) {
            if (!requestCache.containsKey("parentCategoryComplement")) requestCache.put("parentCategoryComplement", dao.getParentCategoryComplement(parent));
            return (Set<ComplementGroup>) requestCache.get("parentCategoryComplement");
        }
        return null;
    }


    private DefaultMutableTreeNode categoryTree;
    private List categoryChildren;
    private List categoryParents;
    private List<CategoryUserLevel> categoryUser;
    private List<CategoryUserLevel> categoryUserParent;
    private List<CategoryProperty> categoryProperties;
    private List<CategoryProperty> categoryPropertiesParent;
    private List<CategoryStaticText> categoryStaticTexts;
    private List<CategoryStaticText> categoryStaticTextsParent;

    private Category category;
    private Category parent;
    private Long idCategory;
    private Long idParent;
    private Long[] idChild;
    private String[] categoryName;
    private String[] description;

    private String[] seoTitle;
    private String[] seoKeywords;
    private String[] seoDescription;
    private String seoUrl;

    private Long[] levelId;
    private Double[] levelPercent;

    private Long[] volumenId;
    private Long[] volumen;
    private Double[] volumenPercent;
    private String[] volumenDescription;

    private Long[] promotionId;
    private String[] promotionFrom;
    private String[] promotionTo;
    private Double[] promotionPercent;
    private Double[] promotionValue;

    private Long[] selectedProductAttribute;
    private Long[] propertiesId;
    private String[] propertiesFilter;
    private Integer[] propertiesSort;

    private Long[] selectedStaticText;
    private Long[] contentStaticTextId;
    private String[] contentPlace;
    private Integer[] contentOrder;

    private Long[] feeId;
    private Double[] feeValue;

    private String[] exportField;
    private String exportFile;
    private File[] files;
    private String[] forDelete;

    private String[] metaTitle;
    private Boolean metaTitleAppend;
    private String[] metaDescription;
    private Boolean metaDescriptionAppend;
    private String[] metaKeywords;
    private Boolean metaKeywordsAppend;
    private String[] metaAbstract;
    private Boolean metaAbstractAppend;

    private String[] metasName;
    private String[] metasValue;
    private Boolean[] metasAppend;

    private String[] shippingType;
    private Long[] shippingState;
    private Double[] shippingValue;

    private Long[] categoryId;
    private String[] categoryActive;
    private String[] categoryVisible;
    private String[] categoryNeedShipping;
    private String[] categoryMarkup;
    private String[] categoryERMarkup;
    private String[] categoryPosition;

    private Long[] idGroup;
    private Long[] selecteds;
    private String categoryUpdate;

    private String[] buyFromCountries;

    private String[] minPrice;
    private String[] maxPrice;

    private String[] labels;


    public String[] getCategoryPosition() {return categoryPosition;}

    public void setCategoryPosition(String[] categoryPosition) {this.categoryPosition = categoryPosition;}

    public String[] getMinPrice() {
        return minPrice;
    }

    public void setMinPrice(String[] minPrice) {
        this.minPrice = minPrice;
    }

    public String[] getMaxPrice() {
        return maxPrice;
    }

    public void setMaxPrice(String[] maxPrice) {
        this.maxPrice = maxPrice;
    }

    public String[] getBuyFromCountries() {
        return buyFromCountries;
    }

    public void setBuyFromCountries(String[] buyFromCountries) {
        this.buyFromCountries = buyFromCountries;
    }

    public String getCategoryUpdate() {
        return categoryUpdate;
    }

    public void setCategoryUpdate(String categoryUpdate) {
        this.categoryUpdate = categoryUpdate;
    }

    public Long[] getSelecteds() {
        return selecteds;
    }

    public void setSelecteds(Long[] selecteds) {
        this.selecteds = selecteds;
    }

    public Long[] getIdGroup() {
        return idGroup;
    }

    public void setIdGroup(Long[] idGroup) {
        this.idGroup = idGroup;
    }

    public Long[] getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long[] categoryId) {
        this.categoryId = categoryId;
    }

    public String[] getCategoryVisible() {
        return categoryVisible;
    }

    public void setCategoryVisible(String[] categoryVisible) {
        this.categoryVisible = categoryVisible;
    }

    public String[] getCategoryActive() {
        return categoryActive;
    }

    public void setCategoryActive(String[] categoryActive) {
        this.categoryActive = categoryActive;
    }

    public String[] getCategoryNeedShipping() {
        return categoryNeedShipping;
    }

    public void setCategoryNeedShipping(String[] categoryNeedShipping) {
        this.categoryNeedShipping = categoryNeedShipping;
    }

    public String[] getCategoryMarkup() {
        return categoryMarkup;
    }

    public void setCategoryMarkup(String[] categoryMarkup) {
        this.categoryMarkup = categoryMarkup;
    }

    public String[] getCategoryERMarkup() {
        return categoryERMarkup;
    }

    public void setCategoryERMarkup(String[] categoryERMarkup) {
        this.categoryERMarkup = categoryERMarkup;
    }

    public String[] getShippingType() {
        return shippingType;
    }

    public void setShippingType(String[] shippingType) {
        this.shippingType = shippingType;
    }

    public Long[] getShippingState() {
        return shippingState;
    }

    public void setShippingState(Long[] shippingState) {
        this.shippingState = shippingState;
    }

    public Double[] getShippingValue() {
        return shippingValue;
    }

    public void setShippingValue(Double[] shippingValue) {
        this.shippingValue = shippingValue;
    }

    public String[] getMetaTitle() {
        return metaTitle;
    }

    public void setMetaTitle(String[] metaTitle) {
        this.metaTitle = metaTitle;
    }

    public Boolean getMetaTitleAppend() {
        return metaTitleAppend;
    }

    public void setMetaTitleAppend(Boolean metaTitleAppend) {
        this.metaTitleAppend = metaTitleAppend;
    }

    public String[] getMetaDescription() {
        return metaDescription;
    }

    public void setMetaDescription(String[] metaDescription) {
        this.metaDescription = metaDescription;
    }

    public Boolean getMetaDescriptionAppend() {
        return metaDescriptionAppend;
    }

    public void setMetaDescriptionAppend(Boolean metaDescriptionAppend) {
        this.metaDescriptionAppend = metaDescriptionAppend;
    }

    public String[] getMetaKeywords() {
        return metaKeywords;
    }

    public void setMetaKeywords(String[] metaKeywords) {
        this.metaKeywords = metaKeywords;
    }

    public Boolean getMetaKeywordsAppend() {
        return metaKeywordsAppend;
    }

    public void setMetaKeywordsAppend(Boolean metaKeywordsAppend) {
        this.metaKeywordsAppend = metaKeywordsAppend;
    }

    public String[] getMetaAbstract() {
        return metaAbstract;
    }

    public void setMetaAbstract(String[] metaAbstract) {
        this.metaAbstract = metaAbstract;
    }

    public Boolean getMetaAbstractAppend() {
        return metaAbstractAppend;
    }

    public void setMetaAbstractAppend(Boolean metaAbstractAppend) {
        this.metaAbstractAppend = metaAbstractAppend;
    }

    public String[] getMetasName() {
        return metasName;
    }

    public void setMetasName(String[] metasName) {
        this.metasName = metasName;
    }

    public String[] getMetasValue() {
        return metasValue;
    }

    public void setMetasValue(String[] metasValue) {
        this.metasValue = metasValue;
    }

    public Boolean[] getMetasAppend() {
        return metasAppend;
    }

    public void setMetasAppend(Boolean[] metasAppend) {
        this.metasAppend = metasAppend;
    }

    public Long[] getFeeId() {
        return feeId;
    }

    public void setFeeId(Long[] feeId) {
        this.feeId = feeId;
    }

    public Double[] getFeeValue() {
        return feeValue;
    }

    public void setFeeValue(Double[] feeValue) {
        this.feeValue = feeValue;
    }

    public String[] getForDelete() {
        return forDelete;
    }

    public void setForDelete(String[] forDelete) {
        this.forDelete = forDelete;
    }

    public DefaultMutableTreeNode getCategoryTree() {
        return categoryTree;
    }

    public void setCategoryTree(DefaultMutableTreeNode categoryTree) {
        this.categoryTree = categoryTree;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public Category getParent() {
        return parent;
    }

    public void setParent(Category parent) {
        this.parent = parent;
    }

    public Long getIdCategory() {
        return idCategory;
    }

    public void setIdCategory(Long idCategory) {
        this.idCategory = idCategory;
    }

    public Long getIdParent() {
        return idParent;
    }

    public void setIdParent(Long idParent) {
        this.idParent = idParent;
    }

    public Long[] getIdChild() {
        return idChild;
    }

    public void setIdChild(Long[] idChild) {
        this.idChild = idChild;
    }

    public String[] getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String[] categoryName) {
        this.categoryName = categoryName;
    }

    public String[] getDescription() {
        return description;
    }

    public void setDescription(String[] description) {
        this.description = description;
    }

    public String[] getSeoTitle() {
        return seoTitle;
    }

    public void setSeoTitle(String[] seoTitle) {
        this.seoTitle = seoTitle;
    }

    public String[] getSeoKeywords() {
        return seoKeywords;
    }

    public void setSeoKeywords(String[] seoKeywords) {
        this.seoKeywords = seoKeywords;
    }

    public String[] getSeoDescription() {
        return seoDescription;
    }

    public void setSeoDescription(String[] seoDescription) {
        this.seoDescription = seoDescription;
    }

    public String getSeoUrl() {
        return seoUrl;
    }

    public void setSeoUrl(String seoUrl) {
        this.seoUrl = seoUrl;
    }

    public List<Category> getCategoryChildren() {
        return categoryChildren;
    }

    public void setCategoryChildren(List<Category> categoryChildren) {
        this.categoryChildren = categoryChildren;
    }

    public List<Category> getCategoryParents() {
        return categoryParents;
    }

    public void setCategoryParents(List<Category> categoryParents) {
        this.categoryParents = categoryParents;
    }

    public Long[] getVolumenId() {
        return volumenId;
    }

    public void setVolumenId(Long[] volumenId) {
        this.volumenId = volumenId;
    }

    public Long[] getVolumen() {
        return volumen;
    }

    public void setVolumen(Long[] volumen) {
        this.volumen = volumen;
    }

    public Double[] getVolumenPercent() {
        return volumenPercent;
    }

    public void setVolumenPercent(Double[] volumenPercent) {
        this.volumenPercent = volumenPercent;
    }

    public String[] getVolumenDescription() {
        return volumenDescription;
    }

    public void setVolumenDescription(String[] volumenDescription) {
        this.volumenDescription = volumenDescription;
    }

    public Long[] getPromotionId() {
        return promotionId;
    }

    public void setPromotionId(Long[] promotionId) {
        this.promotionId = promotionId;
    }

    public String[] getPromotionFrom() {
        return promotionFrom;
    }

    public void setPromotionFrom(String[] promotionFrom) {
        this.promotionFrom = promotionFrom;
    }

    public String[] getPromotionTo() {
        return promotionTo;
    }

    public void setPromotionTo(String[] promotionTo) {
        this.promotionTo = promotionTo;
    }

    public Double[] getPromotionPercent() {
        return promotionPercent;
    }

    public void setPromotionPercent(Double[] promotionPercent) {
        this.promotionPercent = promotionPercent;
    }

    public Double[] getPromotionValue() {
        return promotionValue;
    }

    public void setPromotionValue(Double[] promotionValue) {
        this.promotionValue = promotionValue;
    }

    public List<CategoryProperty> getCategoryProperties() {
        return categoryProperties;
    }

    public void setCategoryProperties(List<CategoryProperty> categoryProperties) {
        this.categoryProperties = categoryProperties;
    }

    public List<CategoryProperty> getCategoryPropertiesParent() {
        return categoryPropertiesParent;
    }

    public void setCategoryPropertiesParent(List<CategoryProperty> categoryPropertiesParent) {
        this.categoryPropertiesParent = categoryPropertiesParent;
    }

    public Long[] getSelectedProductAttribute() {
        return selectedProductAttribute;
    }

    public void setSelectedProductAttribute(Long[] selectedProductAttribute) {
        this.selectedProductAttribute = selectedProductAttribute;
    }

    public Long[] getPropertiesId() {
        return propertiesId;
    }

    public void setPropertiesId(Long[] propertiesId) {
        this.propertiesId = propertiesId;
    }

    public String[] getPropertiesFilter() {
        return propertiesFilter;
    }

    public void setPropertiesFilter(String[] propertiesFilter) {
        this.propertiesFilter = propertiesFilter;
    }

    public Integer[] getPropertiesSort() {
        return propertiesSort;
    }

    public void setPropertiesSort(Integer[] propertiesSort) {
        this.propertiesSort = propertiesSort;
    }

    public String[] getExportField() {
        return exportField;
    }

    public void setExportField(String[] exportField) {
        this.exportField = exportField;
    }

    public String getExportFile() {
        return exportFile;
    }

    public void setExportFile(String exportFile) {
        this.exportFile = exportFile;
    }

    public List<CategoryUserLevel> getCategoryUser() {
        return categoryUser;
    }

    public void setCategoryUser(List<CategoryUserLevel> categoryUser) {
        this.categoryUser = categoryUser;
    }

    public List<CategoryUserLevel> getCategoryUserParent() {
        return categoryUserParent;
    }

    public void setCategoryUserParent(List<CategoryUserLevel> categoryUserParent) {
        this.categoryUserParent = categoryUserParent;
    }

    public Long[] getLevelId() {
        return levelId;
    }

    public void setLevelId(Long[] levelId) {
        this.levelId = levelId;
    }

    public Double[] getLevelPercent() {
        return levelPercent;
    }

    public void setLevelPercent(Double[] levelPercent) {
        this.levelPercent = levelPercent;
    }

    public File[] getFiles() {
        return files;
    }

    public void setFiles(File[] files) {
        this.files = files;
    }

    public List<CategoryStaticText> getCategoryStaticTexts() {
        return categoryStaticTexts;
    }

    public void setCategoryStaticTexts(List<CategoryStaticText> categoryStaticTexts) {
        this.categoryStaticTexts = categoryStaticTexts;
    }

    public List<CategoryStaticText> getCategoryStaticTextsParent() {
        return categoryStaticTextsParent;
    }

    public void setCategoryStaticTextsParent(List<CategoryStaticText> categoryStaticTextsParent) {
        this.categoryStaticTextsParent = categoryStaticTextsParent;
    }

    public Long[] getSelectedStaticText() {
        return selectedStaticText;
    }

    public void setSelectedStaticText(Long[] selectedStaticText) {
        this.selectedStaticText = selectedStaticText;
    }

    public Long[] getContentStaticTextId() {
        return contentStaticTextId;
    }

    public void setContentStaticTextId(Long[] contentStaticTextId) {
        this.contentStaticTextId = contentStaticTextId;
    }

    public String[] getContentPlace() {
        return contentPlace;
    }

    public void setContentPlace(String[] contentPlace) {
        this.contentPlace = contentPlace;
    }

    public Integer[] getContentOrder() {
        return contentOrder;
    }

    public void setContentOrder(Integer[] contentOrder) {
        this.contentOrder = contentOrder;
    }

    public String[] getLabels() {
        return labels;
    }

    public void setLabels(String[] labels) {
        this.labels = labels;
    }

    public class UpdateProductThread extends StoreThread {

        private Store20Database database;
        private String storeCode;
        private Long idCategory;

        public UpdateProductThread(Store20Database database, String storeCode, Long idCategory) {
            this.database = database;
            this.storeCode = storeCode;
            this.idCategory = idCategory;
        }

        @Override
        public void run() {

            try {
                Session session = HibernateSessionFactory.getSessionAutoCommit(database);
                try {
                    setExecutionMessage("Searching products...");
                    setExecutionPercent(0d);
                    HibernateDAO dao = new HibernateDAO(session, storeCode);
                    List<Number> list = dao.getIdProductsInCategory(idCategory);
                    if (list != null && !list.isEmpty()) {
                        int total = list.size(), index = 0;
                        for (Number id : list) {
                            setExecutionPercent(100d * index++ / total);
                            setExecutionMessage("Updating product " + String.valueOf(index) + " of " + String.valueOf(total));
                            Product p = (Product) dao.get(Product.class, id.longValue());
                            if (p.updateCalculatedPrice(dao)) {
                                session.persist(p);
                                session.flush();
                            }
                            session.evict(p);
                        }
                    }
                    setExecutionPercent(100d);
                    setExecutionMessage("Complete");
//                    tx.commit();
                } catch (Exception e) {
//                    tx.rollback();
                    log.error(e.getMessage(), e);
                } finally {
                    if (session.isOpen()) session.close();
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }


        }
    }

}