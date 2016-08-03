package org.store.core.dao;

import com.opensymphony.xwork2.util.logging.Logger;
import com.opensymphony.xwork2.util.logging.LoggerFactory;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.TermQuery;
import org.apache.struts2.ServletActionContext;
import org.apache.struts2.json.JSONException;
import org.apache.struts2.json.JSONUtil;
import org.hibernate.*;
import org.hibernate.criterion.*;
import org.hibernate.criterion.Order;
import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.MySQLDialect;
import org.hibernate.dialect.SQLServerDialect;
import org.hibernate.impl.SessionFactoryImpl;
import org.hibernate.search.FullTextQuery;
import org.hibernate.search.FullTextSession;
import org.hibernate.search.Search;
import org.hibernate.type.StandardBasicTypes;
import org.hibernate.type.Type;
import org.store.core.beans.*;
import org.store.core.beans.Currency;
import org.store.core.beans.utils.*;
import org.store.core.dto.CategoryDTO;
import org.store.core.dto.StateDTO;
import org.store.core.globals.BaseAction;
import org.store.core.globals.CountryFactory;
import org.store.core.globals.SomeUtils;
import org.store.core.hibernate.HibernateSessionFactory;
import org.store.core.hibernate.SessionTarget;
import org.store.core.hibernate.TransactionTarget;
import org.store.core.search.LuceneIndexer;

import javax.servlet.http.HttpServletRequest;
import javax.swing.tree.DefaultMutableTreeNode;
import java.io.IOException;
import java.io.Serializable;
import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: Administrador
 * Date: Mar 23, 2010
 */
public class HibernateDAO {
    private static final Logger LOG = LoggerFactory.getLogger(HibernateDAO.class);

    public static final String CHILDREN_CATEGORIES = "CHILDREN_CATEGORIES";

    @SessionTarget
    protected Session hSession;

    @TransactionTarget
    protected Transaction hTransaction;

    private Map<String, String> storeProperties;
    private String storeCode;

    public HibernateDAO() {
        storeProperties = new HashMap<String, String>();
    }

    public HibernateDAO(Session session, String store) {
        this.hSession = session;
        this.storeCode = store;
        storeProperties = new HashMap<String, String>();
    }

    public String getStoreCode() {
        return storeCode;
    }

    public void setStoreCode(String storeCode) {
        this.storeCode = storeCode;
    }

    public boolean hasStore() {
        return StringUtils.isNotEmpty(storeCode);
    }

    public void addStore(Criteria cri) {
        if (hasStore()) cri.add(Restrictions.eq("inventaryCode", storeCode));
    }

    public Criteria createCriteriaForStore(Class c) {
        Criteria cri = gethSession().createCriteria(c);
        addStore(cri);
        return cri;
    }

    public Session gethSession() {
        if (hSession == null)
            try {
                Object action = (ServletActionContext.getContext() != null && ServletActionContext.getContext().getActionInvocation() != null) ? ServletActionContext.getContext().getActionInvocation().getAction() : null;
                if (action != null && action instanceof BaseAction)
                    hSession = HibernateSessionFactory.getSession(((BaseAction) action).getDatabaseConfig());
            } catch (Exception e) {
                LOG.error(e.getMessage(), e);
            }
        return hSession;
    }

    public boolean isEmpty(String cad) {
        return cad == null || "".equals(cad);
    }

    public void save(BaseBean bean) {
        if (bean instanceof StoreBean && hasStore()) {
            ((StoreBean) bean).setInventaryCode(storeCode);
        }
        gethSession().saveOrUpdate(bean);
    }

    public void delete(BaseBean bean) {
        gethSession().delete(bean);
    }

    public void evict(BaseBean bean) {
        gethSession().evict(bean);
    }

    public void flushSession() {
        gethSession().flush();
    }

    public void clearSession() {
        gethSession().clear();
    }

    public BaseBean get(Class c, Serializable id) {
        try {
            BaseBean bean = (id != null) ? (BaseBean) gethSession().get(c, id) : null;
            if (hasStore() && bean != null && bean instanceof StoreBean) {
                if (!storeCode.equalsIgnoreCase(((StoreBean) bean).getInventaryCode())) return null;
            }
            return bean;
        } catch (HibernateException e) {
            LOG.error(e.getMessage(), e);
        }
        return null;
    }

    public Dialect getCurrentDialect() {
        return ((SessionFactoryImpl) gethSession().getSessionFactory()).getDialect();
    }

    public String randomFunction() {
        Dialect dialect = getCurrentDialect();
        if (dialect instanceof SQLServerDialect) {
            return "newid()";
        } else if (dialect instanceof MySQLDialect) {
            return "rand()";
        }
        return "";
    }

    public String getStorePropertyValue(String key, String type, String defaultValue) {
        if (storeProperties.containsKey(type + "-" + key)) return storeProperties.get(type + "-" + key);
        StoreProperty bean = getStoreProperty(key, type);
        if (bean != null && bean.getValue() != null) {
            storeProperties.put(type + "-" + key, bean.getValue());
            return bean.getValue();
        } else if (defaultValue != null) {
            storeProperties.put(type + "-" + key, defaultValue);
            return defaultValue;
        }
        return null;
    }

    public StoreProperty getStoreProperty(String key, String type) {
        Criteria cri = createCriteriaForStore(StoreProperty.class)
                .add(Restrictions.eq("code", key))
                .add(Restrictions.eq("type", type));
        List<StoreProperty> l = cri.list();
        return (l != null && l.size() > 0) ? l.get(0) : null;
    }

    public StoreProperty getStoreProperty(String key, String type, boolean createIfNotExists) {
        StoreProperty bean = getStoreProperty(key, type);
        if (bean == null && createIfNotExists) {
            bean = new StoreProperty();
            bean.setCode(key);
            bean.setType(type);
            if (hasStore()) bean.setInventaryCode(storeCode);
        }
        return bean;
    }

    public List<StoreProperty> getStoreProperties(String propertyType) {
        Criteria cri = createCriteriaForStore(StoreProperty.class)
                .add(Restrictions.eq("type", propertyType))
                .addOrder(Order.asc("code"));
        return cri.list();
    }

    public Category getCategory(Long id) {
        return (Category) get(Category.class, id);
    }

    public List<Category> getCategoryHierarchy(Category bean) {
        List<Category> l = new ArrayList<Category>();
        if (bean != null) {
            l.add(bean);
            while (bean != null && bean.getIdParent() != null) {
                bean = getCategory(bean.getIdParent());
                if (bean != null) l.add(bean);
            }
        }
        if (l.size() > 1) Collections.reverse(l);
        return l;
    }

    public List<Long> getCategoryHierarchyId(Category bean) {
        List<Long> l = new ArrayList<Long>();
        if (bean != null) {
            l.add(bean.getIdCategory());
            while (bean != null && bean.getIdParent() != null) {
                bean = getCategory(bean.getIdParent());
                if (bean != null) l.add(bean.getIdCategory());
            }
        }
        if (l.size() > 1) Collections.reverse(l);
        return l;
    }

    // Este metodo busca los hijos del padre principal usando getAllChildCategories()

    public void getAllCategoriesOrdered(ArrayList<Category> catList, Category parent, boolean active) {
        int parentIndex = -1;
        for (int c = 0; c < catList.size(); c++) {
            Category bean = catList.get(c);
            if (parent.getIdCategory().equals(bean.getIdCategory())) {
                parentIndex = c;
            }
        }
        List<Category> lista = getAllChildCategories(parent, active);
        for (Category beanCat : lista)
            if (!catList.contains(beanCat)) {
                catList.add(parentIndex + 1, beanCat);
                getAllCategoriesOrdered(catList, beanCat, active);
            }
    }

    public Long[] getIdCategoryList(Category parent, boolean active) {
        ArrayList<Category> list = new ArrayList<Category>();
        getAllCategoriesOrdered(list, parent, active);
        Long[] res = new Long[list.size() + 1];
        for (int i = 0; i < list.size(); i++) res[i] = (list.get(i)).getIdCategory();
        res[list.size()] = parent.getIdCategory();
        return res;
    }

    public List<Category> getChildCategories(Category parent, boolean onlyVisible) {
        Criteria cri = createCriteriaForStore(Category.class);
        if (onlyVisible) cri.add(Restrictions.eq("visible", Boolean.TRUE));
        if (parent != null) cri.add(Restrictions.eq("idParent", parent.getIdCategory()));
        else cri.add(Restrictions.isNull("idParent"));
        cri.addOrder(Order.asc("defaultPosition"));
        List<Category> l = cri.list();
        if (l != null && parent != null) parent.addProperty(CHILDREN_CATEGORIES, l);
        return l;
    }

    public List<Category> getAllChildCategories(Category parent, boolean onlyVisible) {
        List<Category> res = new ArrayList<Category>();
        if (parent != null) {
            Criteria cri = gethSession().createCriteria(CategoryTree.class);
            cri.add(Restrictions.eq("parent", parent));
            List<CategoryTree> l = cri.list();
            if (l != null) {
                for (CategoryTree bean : l) {
                    if (!onlyVisible || bean.getChild().getVisible())
                        if (!res.contains(bean.getChild())) res.add(bean.getChild());
                }
            }
        } else {
            Query q = gethSession().createQuery("select distinct ct.child.idCategory from CategoryTree ct");
            List<Long> childId = q.list();

            Criteria cri = createCriteriaForStore(Category.class);
            if (childId != null && !childId.isEmpty()) cri.add(Restrictions.not(Restrictions.in("idCategory", childId)));
            if (onlyVisible) cri.add(Restrictions.eq("visible", Boolean.TRUE));
            res.addAll(cri.list());
        }
        return res;
    }

    public DefaultMutableTreeNode getCategoryTree(Boolean visible) {
        DefaultMutableTreeNode res = new DefaultMutableTreeNode();
        fillCategoryNodeChilds(res, visible);
        return res;
    }

    public String getCategoryOptionsForSelect(Boolean visible, String lang) {
        StringBuilder buff = new StringBuilder();
        fillCategoryOptionsForSelect(getCategoryTree(visible), buff, lang);
        return buff.toString();
    }

    public void fillCategoryOptionsForSelect(DefaultMutableTreeNode node, StringBuilder buff, String lang) {
        if (node != null && buff != null) {
            Enumeration en = node.children();
            if (en != null) {
                while (en.hasMoreElements()) {
                    DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) en.nextElement();
                    Category childCat = (Category) childNode.getUserObject();
                    buff.append("<option value=\"").append(childCat.getIdCategory()).append("\" level=\"").append(childNode.getLevel() - 1).append("\">");
                    buff.append(childCat.getCategoryName(lang)).append("</option>\n");
                    fillCategoryOptionsForSelect(childNode, buff, lang);
                }
            }
        }
    }

    public void fillCategoryNodeChilds(DefaultMutableTreeNode node) {
        fillCategoryNodeChilds(node, false);
    }


    public void fillCategoryNodeChilds(DefaultMutableTreeNode node, Boolean visible) {
        Category beanCat = null;
        if (node.getUserObject() != null) {
            beanCat = (Category) node.getUserObject();
        }

        List<Category> listaChild = getChildCategories(beanCat, visible);
        for (Object aListaChild : listaChild) {
            Category beanChild = (Category) aListaChild;
            DefaultMutableTreeNode nodeChild = new DefaultMutableTreeNode(beanChild);
            node.add(nodeChild);
            fillCategoryNodeChilds(nodeChild, visible);
        }
    }

    public Manufacturer getOtherManufacturerByUrlCode(String code, Manufacturer bean) {
        Criteria cri = createCriteriaForStore(Manufacturer.class);
        cri.add(Restrictions.eq("urlCode", code));
        if (bean != null && bean.getIdManufacturer() != null) cri.add(Restrictions.ne("idManufacturer", bean.getIdManufacturer()));
        List l = cri.list();
        return (Manufacturer) ((l != null && l.size() > 0) ? l.get(0) : null);
    }

    public boolean updateManufacturerUrlCode(Manufacturer bean, String name) {
        if (isEmpty(name)) name = bean.getManufacturerName();
        if (isEmpty(name)) name = "no-name";
        String urlCode = SomeUtils.replaceForUrl(name);
        if (urlCode != null) while (getOtherManufacturerByUrlCode(urlCode, bean) != null) urlCode += '-';
        bean.setUrlCode(urlCode);
        save(bean);
        return !isEmpty(urlCode);
    }

    public List<State> getStatesByCountry(String country) {
        if (isEmpty(country)) return null;
        Criteria cri = createCriteriaForStore(State.class);
        cri.add(Restrictions.eq("countryCode", country));
        cri.addOrder(Order.asc("stateName"));
        return cri.list();
    }

    public List<StateDTO> getStatesDTOByCountry(String country) {
        if (isEmpty(country)) return null;
        Criteria cri = createCriteriaForStore(State.class);
        cri.add(Restrictions.eq("countryCode", country));
        cri.addOrder(Order.asc("stateName"));
        List<State> l = cri.list();
        List<StateDTO> res = new ArrayList<StateDTO>();
        for (State s : l) res.add(new StateDTO(s));
        return res;
    }

    public List<State> getAllStates() {
        Criteria cri = createCriteriaForStore(State.class);
        cri.addOrder(Order.asc("countryCode"));
        cri.addOrder(Order.asc("stateName"));
        return cri.list();
    }

    public void deleteState(State bean) {
        gethSession().createQuery("delete User b where b.level = :bean").setEntity("bean", bean).executeUpdate();
        delete(bean);
    }

    public CategoryLang getCategoryLang(Category category, String lang) {
        List l = gethSession().createCriteria(CategoryLang.class).add(Restrictions.eq("category", category)).add(Restrictions.eq("categoryLang", lang)).list();
        return (l != null && l.size() > 0) ? (CategoryLang) l.get(0) : null;
    }

    public Category getOtherCategoryByUrlCode(String code, Category bean) {
        Criteria cri = createCriteriaForStore(Category.class);
        cri.add(Restrictions.eq("urlCode", code));
        if (bean != null && bean.getIdCategory() != null) cri.add(Restrictions.ne("idCategory", bean.getIdCategory()));
        List l = cri.list();
        return (Category) ((l != null && l.size() > 0) ? l.get(0) : null);
    }

    public boolean updateCategoryUrlCode(Category bean, String name, String defaulLanguage) {
        CategoryLang catlang = getCategoryLang(bean, defaulLanguage);
        if (isEmpty(name) && catlang != null) name = catlang.getCategoryName();
        if (isEmpty(name)) name = "no-name";
        String urlCode = SomeUtils.replaceForUrl(name);
        if (urlCode != null) while (getOtherCategoryByUrlCode(urlCode, bean) != null) urlCode += '-';
        bean.setUrlCode(urlCode);
        save(bean);
        return !isEmpty(urlCode);
    }

    public Category getRootCategory() {
        Category cat = getCategory(Category.ROOT_CATEGORY_CODE);
        if (cat == null) {
            cat = new Category();
            cat.setActive(true);
            cat.setVisible(false);
            cat.setUrlCode(Category.ROOT_CATEGORY_CODE);
            save(cat);
            for (String lang : getLanguages()) {
                CategoryLang cl = new CategoryLang();
                cl.setCategoryName("Default Category Options");
                cl.setCategoryLang(lang);
                cl.setCategory(cat);
                save(cl);
            }
        }
        return cat;
    }

    public List getCategoryParents(Category category) {
        Query q = gethSession().createQuery("select c.parent from CategoryTree c where c.child=:bean");
        q.setEntity("bean", category);
        return q.list();
    }

    public List getCategoryChildren(Category category, String lang) {
        Query q = gethSession().createQuery("select c from CategoryTree c where c.parent=:bean order by c.position");
        q.setEntity("bean", category);
        q.setResultTransformer(new CategoryTransformer(lang, this));
        return q.list();
    }

    public List<CategoryVolume> getCategoryVolume(Category category) {
        return gethSession().createCriteria(CategoryVolume.class)
                .add(Restrictions.eq("category", category))
                .addOrder(Order.asc("volume"))
                .list();
    }

    public List<CategoryVolume> getParentCategoryVolume(Category category) {
        List<CategoryVolume> l = getCategoryVolume(category);
        while (CollectionUtils.isEmpty(l) && category != null && category.getIdParent() != null) {
            category = getCategory(category.getIdParent());
            l = (category != null) ? getCategoryVolume(category) : null;
        }
        return l;
    }

    public List<CategoryFee> getCategoryFee(Category category, String country) {
        return gethSession().createCriteria(CategoryFee.class)
                .add(Restrictions.eq("category", category))
                .createCriteria("fee").add(Restrictions.eq("country", country))
                .addOrder(Order.asc("id"))
                .list();
    }

    public List<CategoryFee> getCategoryFee(Category category, State state) {
        return gethSession().createCriteria(CategoryFee.class)
                .add(Restrictions.eq("category", category))
                .createCriteria("fee").add(Restrictions.eq("state", state))
                .addOrder(Order.asc("id"))
                .list();
    }

    public List<CategoryFee> getCategoryFee(Category category) {
        return gethSession().createCriteria(CategoryFee.class)
                .add(Restrictions.eq("category", category))
                .addOrder(Order.asc("id"))
                .list();
    }

    public List<CategoryFee> getParentCategoryFee(Category category, String country) {
        List<CategoryFee> l = getCategoryFee(category, country);
        while (CollectionUtils.isEmpty(l) && category != null && category.getIdParent() != null) {
            category = getCategory(category.getIdParent());
            l = (category != null) ? getCategoryFee(category, country) : null;
        }
        return l;
    }

    public List<CategoryFee> getParentCategoryFee(Category category, State state) {
        List<CategoryFee> l = getCategoryFee(category, state);
        while (CollectionUtils.isEmpty(l) && category != null && category.getIdParent() != null) {
            category = getCategory(category.getIdParent());
            l = (category != null) ? getCategoryFee(category, state) : null;
        }
        return l;
    }

    public List<CategoryFee> getParentCategoryFee(Category category) {
        List<CategoryFee> l = getCategoryFee(category);
        while (CollectionUtils.isEmpty(l) && category != null && category.getIdParent() != null) {
            category = getCategory(category.getIdParent());
            l = (category != null) ? getCategoryFee(category) : null;
        }
        return l;
    }

    public Set<ProductLabel> getParentCategoryLabels(Category category) {
        Set<ProductLabel> result = new HashSet<ProductLabel>();
        while (category != null) {
            Set<ProductLabel> l = category.getLabels();
            if (l != null) result.addAll(l);
            category = getCategory(category.getIdParent());
        }
        return result;
    }

    public Set<ProductLabel> getProductLabels(Product product) {
        Set<ProductLabel> result = new HashSet<ProductLabel>();
        if (product.getLabels() != null) result.addAll(product.getLabels());
        Category category = product.getCategory();
        while (category != null) {
            if (category.getLabels() != null) result.addAll(category.getLabels());
            category = (category.getIdParent() != null) ? getCategory(category.getIdParent()) : null;
        }
        return result;
    }

    public Map<State, ShippingRate> getProductShipping(Product product) {
        List<ShippingRate> l = gethSession().createCriteria(ShippingRate.class)
                .add(Restrictions.eq("product", product))
                .addOrder(Order.asc("id"))
                .list();
        Map<State, ShippingRate> res = new HashMap<State, ShippingRate>();
        for (ShippingRate r : l) {
            if (!res.containsKey(r.getState())) res.put(r.getState(), r);
        }
        return res;
    }

    public Map<State, ShippingRate> getCategoryShipping(Category category) {
        List<ShippingRate> l = gethSession().createCriteria(ShippingRate.class)
                .add(Restrictions.eq("category", category))
                .addOrder(Order.asc("id"))
                .list();
        Map<State, ShippingRate> res = new HashMap<State, ShippingRate>();
        for (ShippingRate r : l) {
            if (!res.containsKey(r.getState())) res.put(r.getState(), r);
        }
        return res;
    }

    public Map<State, ShippingRate> getParentCategoryShipping(Category category) {
        Map<State, ShippingRate> res = getCategoryShipping(category);
        while (category != null && category.getIdParent() != null) {
            category = getCategory(category.getIdParent());
            Map<State, ShippingRate> m = (category != null) ? getCategoryShipping(category) : null;
            if (m != null) for (State s : m.keySet()) {
                if (!res.containsKey(s)) res.put(s, m.get(s));
            }
        }
        return res;
    }

    public Set<ComplementGroup> getParentCategoryComplement(Category category) {
        Set<ComplementGroup> res = category.getRelatedGroups();
        if (res == null) res = new HashSet<ComplementGroup>();
        while (category != null && category.getIdParent() != null) {
            category = getCategory(category.getIdParent());
            Set<ComplementGroup> m = (category != null) ? category.getRelatedGroups() : null;
            if (m != null)
                for (ComplementGroup s : m) {
                    if (!res.contains(s)) res.add(s);
                }
        }
        return res;
    }

    public List<CategoryStaticText> getCategoryStaticTexts(Category category) {
        return gethSession().createCriteria(CategoryStaticText.class)
                .add(Restrictions.eq("category", category))
                .addOrder(Order.asc("contentOrder"))
                .list();
    }

    public List<CategoryStaticText> getParentCategoryStaticTexts(Category category) {
        List<CategoryStaticText> res = new ArrayList<CategoryStaticText>();
        List<CategoryStaticText> l = getCategoryStaticTexts(category);
        if (l != null) res.addAll(l);
        while (CollectionUtils.isEmpty(l) && category != null && category.getIdParent() != null) {
            category = getCategory(category.getIdParent());
            l = (category != null) ? getCategoryStaticTexts(category) : null;
            if (l != null) res.addAll(l);
        }
        return res;
    }

    public CategoryVolume getCategoryVolume(Category category, Long volume) {
        List l = gethSession().createCriteria(CategoryVolume.class)
                .add(Restrictions.eq("category", category))
                .add(Restrictions.eq("volume", volume))
                .list();
        return (l != null && l.size() > 0) ? (CategoryVolume) l.get(0) : null;
    }

    public CategoryUserLevel getCategoryUserLevel(Category category, UserLevel level) {
        if (category == null || level == null) return null;
        List l = gethSession().createCriteria(CategoryUserLevel.class)
                .add(Restrictions.eq("category", category))
                .add(Restrictions.eq("level", level))
                .list();
        return (CollectionUtils.isNotEmpty(l)) ? (CategoryUserLevel) l.get(0) : null;
    }

    public ProductUserLevel getProductUserLevel(Product product, UserLevel level) {
        if (product == null || level == null) return null;
        List l = gethSession().createCriteria(ProductUserLevel.class)
                .add(Restrictions.eq("product", product))
                .add(Restrictions.eq("level", level))
                .list();
        return (CollectionUtils.isNotEmpty(l)) ? (ProductUserLevel) l.get(0) : null;
    }

    public Double getCategoryUserLevelPercent(Category category, UserLevel level) {
        if (category == null || level == null) return null;
        List l = gethSession().createCriteria(CategoryUserLevel.class)
                .add(Restrictions.eq("category", category))
                .add(Restrictions.eq("level", level))
                .list();
        CategoryUserLevel bean = (CollectionUtils.isNotEmpty(l)) ? (CategoryUserLevel) l.get(0) : null;
        return (bean != null) ? bean.getPercentDiscount() : null;
    }

    public Double getParentCategoryUserLevelPercent(Category category, UserLevel level) {
        if (category == null || level == null) return null;
        Double l = getCategoryUserLevelPercent(category, level);
        while (l == null && category != null && category.getIdParent() != null) {
            category = getCategory(category.getIdParent());
            l = (category != null) ? getCategoryUserLevelPercent(category, level) : null;
        }
        return (l != null) ? l : level.getDiscountPercent();
    }

    public Double getProductUserLevelPercent(Product product, UserLevel level) {
        if (product == null || level == null) return null;
        List l = gethSession().createCriteria(ProductUserLevel.class)
                .add(Restrictions.eq("product", product))
                .add(Restrictions.eq("level", level))
                .list();
        ProductUserLevel bean = (CollectionUtils.isNotEmpty(l)) ? (ProductUserLevel) l.get(0) : null;
        return (bean != null) ? bean.getPercentDiscount() : null;
    }

    public Double getParentProductUserLevelPercent(Product product, UserLevel level) {
        if (product == null || level == null) return null;
        Double l = getProductUserLevelPercent(product, level);
        Category category = product.getCategory();
        if (l == null && category != null) {
            l = getCategoryUserLevelPercent(category, level);
        }
        while (l == null && category != null && category.getIdParent() != null) {
            category = getCategory(category.getIdParent());
            l = (category != null) ? getCategoryUserLevelPercent(category, level) : null;
        }
        return (l != null) ? l : level.getDiscountPercent();
    }

    public List<ProductOffer> getCategoryOffers(Category category) {
        return (category != null) ? getOffersForCategory(category) : null;
    }

    public List<ProductOffer> getParentCategoryOffers(Category category) {
        List<ProductOffer> l = getCategoryOffers(category);
        while (CollectionUtils.isEmpty(l) && category != null && category.getIdParent() != null) {
            category = getCategory(category.getIdParent());
            l = (category != null) ? getCategoryOffers(category) : null;
        }
        return l;
    }

    public List<ProductOffer> getProductOffers(Product product) {
        return createCriteriaForStore(ProductOffer.class)
                .add(Restrictions.eq("ruleObject", ProductOffer.RULE_PRODUCT))
                .add(Restrictions.eq("ruleValue", product.getIdProduct().toString()))
                .list();
    }

    public void updateAverageScore(Product p) {
        Object o = gethSession().createCriteria(ProductReview.class)
                .add(Restrictions.eq("product", p))
                .add(Restrictions.eq("visible", Boolean.TRUE))
                .setProjection(Projections.projectionList().add(Projections.avg("averageScore")).add(Projections.countDistinct("product")))
                .uniqueResult();
        if (o instanceof Object[] && ((Object[]) o).length == 2) {
            Object[] arr = (Object[]) o;
            p.setRatingBy((Double) arr[0]);
            p.setReviews(((Number) arr[1]).longValue());
        }
    }

    public Object getParentPropertyBean(Object bean, String property) {
        if (bean != null && StringUtils.isNotEmpty(property))
            try {
                String res = BeanUtils.getProperty(bean, property);
                if (res != null && !StringUtils.isEmpty(res)) return bean;
                while (StringUtils.isEmpty(res) && bean != null) {
                    bean = getParentCategory(bean);
                    if (bean != null) {
                        res = BeanUtils.getProperty(bean, property);
                        if (res != null && !StringUtils.isEmpty(res)) return bean;
                    }
                }
            } catch (IllegalAccessException e) {
                LOG.error(e.getMessage(), e);
            } catch (InvocationTargetException e) {
                LOG.error(e.getMessage(), e);
            } catch (NoSuchMethodException e) {
                LOG.error(e.getMessage(), e);
            }
        return null;
    }

    public String getParentPropertyValue(Object bean, String property) {
        if (bean != null && StringUtils.isNotEmpty(property))
            try {
                String res = BeanUtils.getProperty(bean, property);
                if (res != null && !StringUtils.isEmpty(res)) return res;
                while (StringUtils.isEmpty(res) && bean != null) {
                    bean = getParentCategory(bean);
                    if (bean != null) {
                        res = BeanUtils.getProperty(bean, property);
                        if (res != null && !StringUtils.isEmpty(res)) return res;
                    }
                }
            } catch (IllegalAccessException e) {
                LOG.error(e.getMessage(), e);
            } catch (InvocationTargetException e) {
                LOG.error(e.getMessage(), e);
            } catch (NoSuchMethodException e) {
                LOG.error(e.getMessage(), e);
            }
        return null;
    }

    private Category getParentCategory(Object bean) {
        if (bean != null && bean instanceof Category) {
            return getCategory(((Category) bean).getIdParent());
        } else if (bean != null && bean instanceof Product) {
            return ((Product) bean).getCategory();
        }
        return null;
    }

    public CategoryTree getCategoryTree(Category parentCat, Category childCat) {
        List l = (parentCat != null && childCat != null) ? hSession.createCriteria(CategoryTree.class).add(Restrictions.eq("parent", parentCat)).add(Restrictions.eq("child", childCat)).list() : null;
        return (l != null && l.size() > 0) ? (CategoryTree) l.get(0) : null;
    }

    public List<AttributeProd> getAttributesProduct(org.store.core.beans.utils.DataNavigator nav, String group) {
        Criteria cri = createCriteriaForStore(AttributeProd.class);
        if (StringUtils.isNotEmpty(group)) cri.add(Restrictions.eq("attributeGroup", group));
        if (nav != null) {
            cri.setProjection(Projections.countDistinct("id"));
            Number total = (Number) cri.uniqueResult();
            nav.setTotalRows(total != null ? total.intValue() : 0);
            cri.setProjection(null);
            cri.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
            cri.addOrder(Order.asc("id"));
            if (nav.getTotalRows() > 0) {
                cri.setMaxResults(nav.getPageRows());
                cri.setFirstResult(nav.getFirstRow() - 1);
            }
        } else {
            cri.addOrder(Order.asc("id"));
        }
        return cri.list();
    }

    public List<String> getProductAttGroups() {
        return createCriteriaForStore(AttributeProd.class).setProjection(Projections.groupProperty("attributeGroup").as("group"))
                .addOrder(Order.asc("group"))
                .list();
    }

    public List<CategoryProperty> getCategoryProperties(Category category) {
        return getCategoryProperties(category, false);
    }

    public List<CategoryProperty> getCategoryProperties(Category category, boolean forFilter) {
        if (category != null) {
            Criteria cri = gethSession().createCriteria(CategoryProperty.class);
            cri.add(Restrictions.eq("category", category));
            if (forFilter) cri.add(Restrictions.eq("canfilter", Boolean.TRUE));
            cri.addOrder(Order.asc("orderFilter"));
            cri.createCriteria("attribute").addOrder(Order.asc("attributeGroup")).addOrder(Order.asc("id"));
            return cri.list();
        }
        return null;
    }

    public List<CategoryProperty> getParentCategoryProperties(Category category, boolean includeSelf) {
        return getParentCategoryProperties(category, includeSelf, false);
    }

    public List<CategoryProperty> getParentCategoryProperties(Category category, boolean includeSelf, boolean forFilter) {
        List<CategoryProperty> res = (includeSelf) ? getCategoryProperties(category, forFilter) : null;
        if (res == null) res = new ArrayList<CategoryProperty>();
        while (category != null && category.getIdParent() != null) {
            category = getCategory(category.getIdParent());
            List<CategoryProperty> l = (category != null) ? getCategoryProperties(category, forFilter) : null;
            if (CollectionUtils.isNotEmpty(l)) res.addAll(l);
        }
        return res;
    }


    public CategoryProperty getCategoryProperty(Category category, AttributeProd beanAtt) {
        List l = gethSession().createCriteria(CategoryProperty.class).add(Restrictions.eq("category", category)).add(Restrictions.eq("attribute", beanAtt)).list();
        return (l != null && l.size() > 0) ? (CategoryProperty) l.get(0) : null;
    }

    public boolean productApplyOffer(Product p, ProductOffer offer) {
        Criteria cri = createCriteriaForStore(Product.class).add(Restrictions.or(Restrictions.eq("archived", Boolean.FALSE), Restrictions.isNull("archived")));
        cri.add(Restrictions.eq("idProduct", p.getIdProduct()));
        offer.setRestrictionForCriteria(cri);
        List l = cri.list();
        return (l != null && l.size() > 0);
    }

    public List<ProductOffer> getOffersForCategory(Category c) {
        List<ProductOffer> res = new ArrayList<ProductOffer>();
        List<ProductOffer> lista = getOffers(false);
        for (ProductOffer offer : lista) {
            if (offer.isForCategory(c)) res.add(offer);
        }
        return res;
    }

    public List<ProductOffer> getOffers(boolean onlyActive) {
        Criteria cri = createCriteriaForStore(ProductOffer.class);
        if (onlyActive) {
            cri.add(Restrictions.or(Restrictions.isNull("dateFrom"), Restrictions.le("dateFrom", SomeUtils.dateEnd(null))));
            cri.add(Restrictions.or(Restrictions.isNull("dateTo"), Restrictions.ge("dateTo", SomeUtils.dateIni(null))));
        }
        cri.addOrder(Order.desc("idOffer"));
        return cri.list();
    }

    public List<ProductOffer> getOffersForProduct(Product p, boolean onlyActive) {
        List<ProductOffer> res = new ArrayList<ProductOffer>();
        List<ProductOffer> lista = getOffers(onlyActive);
        for (ProductOffer offer : lista) {
            if (productApplyOffer(p, offer)) res.add(offer);
        }
        return res;
    }

    /**
     * Listado de productos en el modulo de administracion
     *
     * @param nav            Navegador
     * @param filter         Filtros
     * @param filterSupplier Proveedor
     * @return Lista de productos q cumplen las condiciones
     */
    public List listProducts(org.store.core.beans.utils.DataNavigator nav, ProductFilter filter, Long filterSupplier) {
        Criteria cri = createCriteriaForStore(Product.class);
        cri.add(Restrictions.or(Restrictions.isNull("productType"), Restrictions.ne("productType", Product.TYPE_COMPLEMENT)));
        cri.createAlias("productLangs", "lang", CriteriaSpecification.LEFT_JOIN);
        if (filter != null) {
            if ("Y".equalsIgnoreCase(filter.getShowArchived())) cri.add(Restrictions.eq("archived", Boolean.TRUE));
            else if ("N".equalsIgnoreCase(filter.getShowArchived())) cri.add(Restrictions.or(Restrictions.eq("archived", Boolean.FALSE), Restrictions.isNull("archived")));

            if (StringUtils.isNotEmpty(filter.getFilterId())) {
                Long idProd = SomeUtils.strToLong(filter.getFilterId());
                if (idProd != null) cri.add(Restrictions.sqlRestriction("{alias}.idProduct=" + idProd));
            }

            // Filtrar por categorias
            if (StringUtils.isNotEmpty(filter.getFilterCategories())) {
                Long idcat = SomeUtils.strToLong(filter.getFilterCategories());
                Category beanCat = (Category) get(Category.class, idcat);
                if (beanCat != null) {
                    Long[] ids = getIdCategoryList(beanCat, false);
                    if (ids != null && ids.length > 0) cri.createCriteria("productCategories").add(Restrictions.in("idCategory", ids));
                }
            }
            // Filtrar por manufacturer
            if (StringUtils.isNotEmpty(filter.getFilterManufacturer())) {
                Long[] ids = SomeUtils.strToLong(filter.getFilterManufacturer().split(","));
                if (ids != null && ids.length > 0) cri.createCriteria("manufacturer").add(Restrictions.in("idManufacturer", ids));
            }
            // Filtrar por supplier
            if (filterSupplier != null) {
                Provider provider = (Provider) get(Provider.class, filterSupplier);
                if (provider != null) {
                    cri.createAlias("productProviders", "productProviders");
                    cri.add(Restrictions.eq("productProviders.provider", provider));
                }
            }
            // Filtrar por active
            if ("Y".equalsIgnoreCase(filter.getFilterEnabled()))
                cri.add(Restrictions.eq("active", Boolean.TRUE));
            else if ("N".equalsIgnoreCase(filter.getFilterEnabled()))
                cri.add(Restrictions.eq("active", Boolean.FALSE));
            // Filtrar por label
            if (StringUtils.isNotEmpty(filter.getFilterLabel())) {
                cri.createCriteria("labels").add(Restrictions.eq("code", filter.getFilterLabel()));
            }
            // Filtrar por precio
            if (StringUtils.isNotEmpty(filter.getFilterMaxPrice())) {
                Double maxPrice = SomeUtils.strToDouble(filter.getFilterMaxPrice());
                if (maxPrice != null) cri.add(Restrictions.le("price", maxPrice));
            }
            if (StringUtils.isNotEmpty(filter.getFilterMinPrice())) {
                Double minPrice = SomeUtils.strToDouble(filter.getFilterMinPrice());
                if (minPrice != null) cri.add(Restrictions.ge("price", minPrice));
            }
            // Filtrar por stock
            if (StringUtils.isNotEmpty(filter.getFilterMaxStock())) {
                Long maxStock = SomeUtils.strToLong(filter.getFilterMaxStock());
                if (maxStock != null) cri.add(Restrictions.le("stock", maxStock));
            }
            if (StringUtils.isNotEmpty(filter.getFilterMinStock())) {
                Long minStock = SomeUtils.strToLong(filter.getFilterMinStock());
                if (minStock != null) cri.add(Restrictions.ge("stock", minStock));
            }
            if (StringUtils.isNotEmpty(filter.getFilterCode())) {
                cri.add(Restrictions.or(
                        Restrictions.like("partNumber", filter.getFilterCode(), MatchMode.ANYWHERE),
                        Restrictions.like("mfgPartnumber", filter.getFilterCode(), MatchMode.ANYWHERE)
                ));
            }
            if (StringUtils.isNotEmpty(filter.getFilterName())) {
                for (String n : filter.getFilterName().split(" ")) {
                    cri.add(Restrictions.like("lang.productName", n, MatchMode.ANYWHERE));
                }
            }
            // Este filtro puede que disminuya muchisimo el rendimiento de la consulta
            if (StringUtils.isNotEmpty(filter.getFilterDayWithoutStock())) {
                Integer daysWithStock = NumberUtils.createInteger(filter.getFilterDayWithoutStock());
                if (daysWithStock != null && daysWithStock > 0) {
                    Calendar cal = Calendar.getInstance();
                    cal.add(Calendar.DATE, -daysWithStock);
                    cri.add(Restrictions.sqlRestriction("{alias}.idProduct in (select idProduct from (select t_product.idProduct, (select min(t_product_audit_stock.changeDate) from t_product_audit_stock where t_product.idProduct=t_product_audit_stock.product_idProduct) as firstDate,(select max(t_product_audit_stock.changeDate) from t_product_audit_stock where t_product.idProduct=t_product_audit_stock.product_idProduct and t_product_audit_stock.stock>0) as lastDateWithStock from t_product where stock=0 and inventaryCode=? and (archived is null or archived=0)) t where (t.lastDateWithStock is null and firstDate<?) or (t.lastDateWithStock is not null and lastDateWithStock<?))", new Object[]{storeCode, cal.getTime(), cal.getTime()}, new Type[]{StandardBasicTypes.STRING, StandardBasicTypes.DATE, StandardBasicTypes.DATE}));
                }
            }
            // todo: queda pendiente el filtrado por oferta
        }
        if (nav != null) {
            cri.setProjection(Projections.countDistinct("idProduct"));
            Number total = (Number) cri.uniqueResult();
            nav.setTotalRows(total != null ? total.intValue() : 0);
            cri.setProjection(null);

            ProjectionList p = Projections.projectionList();
            p.add(Projections.property("idProduct"), "idProduct");
            if (filter != null && !"random".equalsIgnoreCase(filter.getSortedField()))
                for (String f : filter.getSortedField().split(",")) {
                    if ("productName".equalsIgnoreCase(filter.getSortedField())) p.add(Projections.property("lang." + f.trim()));
                    else p.add(Projections.property(f.trim()));
                }
            cri.setProjection(Projections.distinct(p));
            cri.setResultTransformer(new IdToBeanTransformer(this, Product.class));
            if (nav.getTotalRows() > nav.getPageRows()) {
                cri.setFirstResult(nav.getFirstRow() - 1);
                cri.setMaxResults(nav.getPageRows());
            }
        } else {
            ProjectionList p = Projections.projectionList();
            p.add(Projections.property("idProduct"), "idProduct");
            if (filter != null && !"random".equalsIgnoreCase(filter.getSortedField()))
                for (String f : filter.getSortedField().split(",")) {
                    if ("productName".equalsIgnoreCase(filter.getSortedField())) p.add(Projections.property("lang." + f.trim()));
                    else p.add(Projections.property(f.trim()));
                }
            cri.setProjection(Projections.distinct(p));
            cri.setResultTransformer(new IdToBeanTransformer(this, Product.class));
        }
        // Order by
        if (filter != null) {
            for (String f : filter.getSortedField().split(",")) {
                if ("productName".equalsIgnoreCase(filter.getSortedField())) {
                    cri.addOrder(("desc".equalsIgnoreCase(filter.getSortedDirection())) ? Order.desc("lang." + f.trim()) : Order.asc("lang." + f.trim()));
                } else cri.addOrder(("desc".equalsIgnoreCase(filter.getSortedDirection())) ? Order.desc(f.trim()) : Order.asc(f.trim()));
            }
        }
        return cri.list();
    }

    public List searchUsers(DataNavigator nav, UserFilter filter, Long notInGroup, String notInLevel) {
        Criteria cri = createCriteriaForStore(User.class);
        cri.add(Restrictions.or(Restrictions.isNull("anonymousCheckout"), Restrictions.eq("anonymousCheckout", Boolean.FALSE)));
        if (filter != null) {
            boolean executePreFilter = false;
            StringBuilder buff = new StringBuilder();
            buff.append("select distinct t_user.idUser ");
            buff.append("from t_user ");
            buff.append(" left join t_order on t_user.idUser=t_order.user_idUser ");
            buff.append(" left join t_order_detail on t_order.idOrder=t_order_detail.order_idOrder ");
            buff.append(" left join t_order_detail_product on t_order_detail.idDetail=t_order_detail_product.orderDetail_idDetail ");
            buff.append(" left join t_product on t_product.idProduct=t_order_detail_product.product_idProduct ");
            buff.append(" left join t_product_t_category on t_product.idProduct=t_product_t_category.t_product_idProduct");
            buff.append(" where (anonymousCheckout is NULL or anonymousCheckout=0) ");
            if (StringUtils.isNotEmpty(filter.getFilterMinOrder())) {
                Double minOrder = SomeUtils.strToDouble(filter.getFilterMinOrder());
                if (minOrder != null) {
                    buff.append(" and (t_order.totalProducts>=").append(minOrder.toString()).append(") ");
                    executePreFilter = true;
                }
            }
            if (StringUtils.isNotEmpty(filter.getFilterMaxOrder())) {
                Double maxOrder = SomeUtils.strToDouble(filter.getFilterMaxOrder());
                if (maxOrder != null) {
                    buff.append(" and (t_order.totalProducts <= ").append(maxOrder.toString()).append(") ");
                    executePreFilter = true;
                }
            }
            if (StringUtils.isNotEmpty(filter.getFilterCategories())) {
                Long idcat = SomeUtils.strToLong(filter.getFilterCategories());
                Category beanCat = (Category) get(Category.class, idcat);
                if (beanCat != null) {
                    Long[] ids = getIdCategoryList(beanCat, false);
                    if (ids != null && ids.length > 0) {
                        buff.append(" and (t_product_t_category.productCategories_idCategory IN (").append(StringUtils.join(ids, ",")).append(")) ");
                        executePreFilter = true;
                    }
                }
            }
            if (StringUtils.isNotEmpty(filter.getFilterName())) {
                String[] arrName = filter.getFilterName().split(" ");
                if (arrName != null && arrName.length > 0) {
                    for (String cad : arrName) {
                        if (!isEmpty(cad.trim())) {
                            buff.append(" and ( t_user.firstname LIKE '%").append(cad).append("%' ");
                            buff.append(" or t_user.lastname LIKE '%").append(cad).append("%' ");
                            buff.append(" or t_user.email LIKE '%").append(cad).append("%') ");
                            executePreFilter = true;
                        }
                    }
                }

            }
            if (notInGroup != null) {
                buff.append(" and t_user.idUser not in (select t_user_group_t_user.users_idUser from t_user_group_t_user where t_user_group_t_user.groups_idGroup=").append(notInGroup.toString()).append(") ");
                executePreFilter = true;
            }
            if (StringUtils.isNotEmpty(notInLevel)) {
                buff.append(" and (t_user.level_level<>'").append(notInLevel).append("' )");
                executePreFilter = true;
            }
            if (filter.getFilterLevel() != null) {
                buff.append(" and (t_user.level_id='").append(filter.getFilterLevel()).append("' ) ");
                executePreFilter = true;
            }

            if (executePreFilter) {
                Query q = gethSession().createSQLQuery(buff.toString()).addScalar("idUser", StandardBasicTypes.LONG);
                List lista = q.list();
                if (lista != null && lista.size() > 0) cri.add(Restrictions.in("idUser", q.list()));
                else cri.add(Restrictions.eq("idUser", (long) 0));
            }

        }

        if (nav != null) {
            cri.setProjection(Projections.countDistinct("idUser"));
            Number total = (Number) cri.uniqueResult();
            nav.setTotalRows(total != null ? total.intValue() : 0);
            cri.setProjection(null);
            cri.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
            if (nav.getTotalRows() > nav.getPageRows()) {
                cri.setMaxResults(nav.getPageRows());
                cri.setFirstResult(nav.getFirstRow() - 1);
            }
        }

        // Order by
        if (filter != null) {
            for (String f : filter.getSortedField().split(",")) {
                cri.addOrder(("desc".equalsIgnoreCase(filter.getSortedDirection())) ? Order.desc(f.trim()) : Order.asc(f.trim()));
            }
        }
        return cri.list();
    }

    public List<Order> getUserOrders(User user) {
        Criteria cri = gethSession().createCriteria(org.store.core.beans.Order.class);
        cri.add(Restrictions.eq("user", user));
        cri.addOrder(Order.desc("idOrder"));
        return cri.list();
    }

    public org.store.core.beans.Order getUserLastOrder(User user) {
        Criteria cri = gethSession().createCriteria(org.store.core.beans.Order.class);
        cri.add(Restrictions.eq("user", user));
        cri.addOrder(Order.desc("idOrder"));
        cri.setMaxResults(1);
        return (org.store.core.beans.Order) cri.uniqueResult();
    }

    public User getAdminUser(String userId) {
        List l = createCriteriaForStore(User.class).add(Restrictions.eq("admin", Boolean.TRUE)).add(Restrictions.eq("userId", userId)).list();
        return (CollectionUtils.isNotEmpty(l)) ? (User) l.get(0) : null;
    }

    public List<User> getAdminUsers() {
        return createCriteriaForStore(User.class).add(Restrictions.eq("admin", Boolean.TRUE)).list();
    }

    public List<UserNote> getUserNotes(User user) {
        if (user == null) return null;
        return gethSession().createCriteria(UserNote.class)
                .add(Restrictions.eq("user", user))
                .addOrder(Order.asc("idNote"))
                .list();
    }

    public ProductLang getProductLang(Product product, String lang) {
        List l = gethSession().createCriteria(ProductLang.class).add(Restrictions.eq("product", product)).add(Restrictions.eq("productLang", lang)).list();
        return (l != null && l.size() > 0) ? (ProductLang) l.get(0) : null;
    }

    public boolean updateProductUrlCode(Product bean, String name, String defaulLanguage) {
        ProductLang catlang = getProductLang(bean, defaulLanguage);
        if ((name == null || isEmpty(name.trim())) && catlang != null) name = catlang.getProductName();
        if (isEmpty(name)) {
            bean.setUrlCode(null);
            save(bean);
            return false;
        } else {
            String urlCode = SomeUtils.replaceForUrl(name);
            if (urlCode != null) while (getOtherProductByUrlCode(urlCode, bean) != null) urlCode += '-';
            bean.setUrlCode(urlCode);
            save(bean);
            return true;
        }
    }

    public void indexProduct(Product p, boolean isNew) {
        indexProduct(p.getIdProduct(), isNew);
    }

    public void indexProduct(Long id, boolean isNew) {
        String path = getStorePropertyValue(StoreProperty.PROP_SITE_PATH, StoreProperty.TYPE_GENERAL, "");
        Product product = (Product) get(Product.class, id);
        if (product != null) {
            if (product.getProductLangs() != null) {
                for (ProductLang pl : product.getProductLangs()) {
                    pl.setIndexed(true);
                    save(pl);
                }
            }
            LuceneIndexer.indexProduct(product, isNew, path, getLanguages(), getDefaultLanguage());
            product.setIndexed(true);
            save(product);
        }
    }

    public Product getOtherProductByUrlCode(String code, Product bean) {
        Criteria cri = createCriteriaForStore(Product.class);
        cri.add(Restrictions.eq("urlCode", code));
        if (bean != null && bean.getIdProduct() != null) cri.add(Restrictions.ne("idProduct", bean.getIdProduct()));
        List l = cri.list();
        return (Product) ((l != null && l.size() > 0) ? l.get(0) : null);
    }

    public List<ProductVolume> getProductVolume(Product product) {
        return gethSession().createCriteria(ProductVolume.class)
                .add(Restrictions.eq("product", product))
                .addOrder(Order.asc("volume"))
                .list();
    }

    public ProductVolume getProductVolume(Product product, Long volume) {
        List l = gethSession().createCriteria(ProductVolume.class)
                .add(Restrictions.eq("product", product))
                .add(Restrictions.eq("volume", volume))
                .list();
        return (l != null && l.size() > 0) ? (ProductVolume) l.get(0) : null;
    }

    public ProductProvider getProductProvider(Product product, Provider prov) {
        List l = gethSession().createCriteria(ProductProvider.class)
                .add(Restrictions.eq("product", product))
                .add(Restrictions.eq("provider", prov))
                .list();
        return (l != null && l.size() > 0) ? (ProductProvider) l.get(0) : null;
    }

    public List<ProductProvider> getProductProviders(Product product) {
        return gethSession().createCriteria(ProductProvider.class)
                .add(Restrictions.eq("product", product))
                .addOrder(Order.asc("id"))
                .list();
    }

    public List<ProductReview> getProductReviews(Product product, boolean onlyVisible) {
        if (product == null) return null;
        Criteria cri = gethSession().createCriteria(ProductReview.class).add(Restrictions.eq("product", product));
        if (onlyVisible) cri.add(Restrictions.eq("visible", Boolean.TRUE));
        cri.addOrder(Order.desc("created"));
        return cri.list();
    }

    public List<ProductReview> getReviewsForProduct(DataNavigator nav, Product product) {
        if (product == null) return null;
        Criteria cri = gethSession().createCriteria(ProductReview.class).add(Restrictions.eq("product", product));
        cri.add(Restrictions.eq("visible", Boolean.TRUE));

        if (nav != null) {
            cri.setProjection(Projections.countDistinct("idReview"));
            Number total = (Number) cri.uniqueResult();
            nav.setTotalRows(total != null ? total.intValue() : 0);
            cri.setProjection(null);
            cri.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
            cri.addOrder(Order.desc("created"));
            if (nav.getTotalRows() > 0) {
                cri.setMaxResults(nav.getPageRows());
                cri.setFirstResult(nav.getFirstRow() - 1);
            }
        } else {
            cri.addOrder(Order.desc("created"));
        }
        return cri.list();
    }

    public List<ProductProperty> getProductProperties(Product product) {
        if (product == null) return null;
        return gethSession().createCriteria(ProductProperty.class)
                .add(Restrictions.eq("product", product))
                .createCriteria("attribute").addOrder(Order.asc("attributeGroup")).addOrder(Order.asc("id"))
                .list();
    }

    public ProductProperty getProductProperty(Product product, AttributeProd attribute) {
        if (product == null || attribute == null) return null;
        List l = gethSession().createCriteria(ProductProperty.class)
                .add(Restrictions.eq("product", product))
                .add(Restrictions.eq("attribute", attribute))
                .list();
        return (l != null && l.size() > 0) ? (ProductProperty) l.get(0) : null;
    }

    public void deleteProductProperties(Product product) {
        gethSession().createQuery("delete ProductProperty b where b.product = :product").setEntity("product", product).executeUpdate();
    }

    public List<ProductVariation> getProductVariations(Product product) {
        if (product == null) return null;
        return gethSession().createCriteria(ProductVariation.class)
                .add(Restrictions.eq("product", product))
                .addOrder(Order.asc("id"))
                .list();
    }

    public AttributeProd getAttributeByName(String lang, String name, String groupName, boolean createIfNotExist) {
        List<AttributeProd> l = getAttributesByName(lang, name);
        if (l != null) {
            for (AttributeProd ap : l) {
                if (StringUtils.isEmpty(groupName) || groupName.equalsIgnoreCase(ap.getAttributeGroup())) return ap;
            }
        }
        if (createIfNotExist) {
            AttributeProd ap = new AttributeProd();
            ap.setAttributeGroup(groupName);
            for (String la : getLanguages()) ap.setAttributeName(la, name);
            ap.setInventaryCode(getStoreCode());
            save(ap);
            flushSession();
            return ap;
        }
        return null;
    }

    public AttributeProd getAttributeByName(String lang, String name) {
        List l = getAttributesByName(lang, name);
        return (l != null && l.size() > 0) ? (AttributeProd) l.get(0) : null;
    }

    public List<AttributeProd> getAttributesByName(String lang, String name) {
        StringBuilder buff = new StringBuilder();
        if (StringUtils.isNotEmpty(lang)) buff.append("\"").append(lang).append("\":");
        if (StringUtils.isNotEmpty(name)) {
            try {
                String sName = JSONUtil.serialize(name);
                buff.append(sName);
            } catch (JSONException e) {
                LOG.error(e.getMessage(), e);
            }
        }
        if (StringUtils.isEmpty(buff.toString())) return null;
        return createCriteriaForStore(AttributeProd.class)
                .add(Restrictions.like("attributeName", buff.toString(), MatchMode.ANYWHERE))
                .list();
    }

    public Product getProductByPartNumber(String partNumber) {
        if (StringUtils.isEmpty(partNumber)) return null;
        List l = createCriteriaForStore(Product.class).add(Restrictions.eq("partNumber", partNumber)).list();
        return (l != null && l.size() > 0) ? (Product) l.get(0) : null;
    }

    public Product getProductByMfgPartNumber(String partNumber) {
        if (StringUtils.isEmpty(partNumber)) return null;
        List l = createCriteriaForStore(Product.class).add(Restrictions.eq("mfgPartnumber", partNumber)).list();
        return (l != null && l.size() > 0) ? (Product) l.get(0) : null;
    }

    public User getUserByUserId(String email) {
        if (StringUtils.isEmpty(email)) return null;
        List l = createCriteriaForStore(User.class).add(Restrictions.eq("userId", email)).add(Restrictions.or(Restrictions.isNull("anonymousCheckout"), Restrictions.eq("anonymousCheckout", Boolean.FALSE))).list();
        return (l != null && l.size() > 0) ? (User) l.get(0) : null;
    }

    public List<CountryFactory.Country> getConfiguredCountries(Locale locale, boolean sort) {
        List<CountryFactory.Country> countries = new ArrayList<CountryFactory.Country>();
        List<String> l = createCriteriaForStore(State.class).setProjection(Projections.distinct(Projections.property("countryCode"))).list();
        for (String cCode : l) {
            String cName = CountryFactory.getCountryName(cCode, locale);
            countries.add(new CountryFactory.Country(cCode, cName));
        }
        if (!sort) return countries;
        Collections.sort(countries, new CountryFactory.CountryComparator());
        return countries;
    }

    public UserPreference getUserPreference(User u, String s) {
        List l = gethSession().createCriteria(UserPreference.class).add(Restrictions.eq("user", u)).add(Restrictions.eq("preferenceCode", s)).list();
        return (l != null && l.size() > 0) ? (UserPreference) l.get(0) : null;
    }

    public User getUserByEmail(String email) {
        if (StringUtils.isEmpty(email)) return null;
        List l = createCriteriaForStore(User.class).add(Restrictions.eq("email", email)).add(Restrictions.or(Restrictions.isNull("anonymousCheckout"), Restrictions.eq("anonymousCheckout", Boolean.FALSE))).list();
        return (l != null && l.size() > 0) ? (User) l.get(0) : null;
    }

    public List<UserPreference> getUserPreferences(User user) {
        return gethSession().createCriteria(UserPreference.class)
                .add(Restrictions.eq("user", user))
                .addOrder(Order.asc("preferenceCode"))
                .list();
    }

    public List<CategoryFee> getFeeCategories(Fee fee) {
        return gethSession().createCriteria(CategoryFee.class).add(Restrictions.eq("fee", fee)).addOrder(Order.asc("id")).list();
    }

    public CategoryFee getCategoryFee(Fee fee, Category cat) {
        List l = gethSession().createCriteria(CategoryFee.class).add(Restrictions.eq("fee", fee)).add(Restrictions.eq("category", cat)).list();
        return (l != null && l.size() > 0) ? (CategoryFee) l.get(0) : null;
    }

    public Category getCategoryByName(String name, String lang) {
        Criteria cri = gethSession().createCriteria(CategoryLang.class).add(Restrictions.eq("categoryName", name)).add(Restrictions.eq("categoryLang", lang));
        if (hasStore()) {
            cri.createAlias("category", "category", Criteria.LEFT_JOIN);
            cri.add(Restrictions.eq("category.inventaryCode", storeCode));
        }
        List l = cri.list();
        return (l != null && l.size() > 0) ? ((CategoryLang) l.get(0)).getCategory() : null;
    }

    public List<Category> findCategories(String term) {
        Criteria cri = gethSession().createCriteria(CategoryLang.class).add(Restrictions.like("categoryName", term, MatchMode.ANYWHERE));
        if (hasStore()) {
            cri.createAlias("category", "category", Criteria.LEFT_JOIN);
            cri.add(Restrictions.eq("category.inventaryCode", storeCode));
        }
        cri.setProjection(Projections.distinct(Projections.property("category.idCategory")));
        cri.setResultTransformer(new IdToBeanTransformer(this, Category.class));
        cri.setMaxResults(20);
        return cri.list();
    }

    public Manufacturer getManufacturerByName(String name) {
        List l = createCriteriaForStore(Manufacturer.class).add(Restrictions.eq("manufacturerName", name)).list();
        return (l != null && l.size() > 0) ? (Manufacturer) l.get(0) : null;
    }

    public Product getProduct(String code) {
        List l = createCriteriaForStore(Product.class).add(Restrictions.eq("urlCode", code)).list();
        return (l != null && l.size() > 0) ? (Product) l.get(0) : null;
    }

    public Category getCategory(String code) {
        List l = createCriteriaForStore(Category.class).add(Restrictions.eq("urlCode", code)).list();
        return (l != null && l.size() > 0) ? (Category) l.get(0) : null;
    }

    public Category getCategoryByExternalCode(String code) {
        List l = createCriteriaForStore(Category.class).add(Restrictions.eq("externalCode", code)).list();
        return (l != null && l.size() > 0) ? (Category) l.get(0) : null;
    }

    public Category getCategoryByExternalCodeAncestorOf(String code, Category parent) {
        List<Category> l = createCriteriaForStore(Category.class).add(Restrictions.eq("externalCode", code)).list();
        if (l != null && !l.isEmpty()) {
            for (Category c : l) {
                if (parent == null) return c;
                else if (categoryParentOf(parent, c)) return c;
            }
        }
        return null;
    }

    private boolean categoryParentOf(Category parent, Category c) {
        if (parent == null || c == null) return false;
        while (c != null && c.getIdParent() != null) {
            c = getCategory(c.getIdParent());
            if (parent.equals(c)) return true;
        }
        return false;
    }

    public Manufacturer getManufacturer(String code) {
        List l = createCriteriaForStore(Manufacturer.class).add(Restrictions.eq("urlCode", code)).list();
        return (l != null && l.size() > 0) ? (Manufacturer) l.get(0) : null;
    }
/*
    public List listFrontProducts(org.store.core.beans.utils.DataNavigator nav, ProductFilter filter, Map extras) {
        boolean isRandomSort = (filter != null && "random".equalsIgnoreCase(filter.getSortedField()));
        //    Criteria criLang = null;
        Criteria cri = createCriteriaForStore(Product.class).add(Restrictions.or(Restrictions.eq("archived", Boolean.FALSE), Restrictions.isNull("archived")));
        cri.add(Restrictions.eq("active", Boolean.TRUE));
        cri.add(Restrictions.or(Restrictions.isNull("productType"), Restrictions.ne("productType", Product.TYPE_COMPLEMENT)));
        String showunavailables = getStorePropertyValue(StoreProperty.PROP_PRODUCT_SHOW_UNAVAILABLE, StoreProperty.TYPE_GENERAL, StoreProperty.PROP_DEFAULT_PRODUCT_SHOW_UNAVAILABLE);
        if (StoreProperty.PROP_PRODUCT_HAS_STOCK.equalsIgnoreCase(showunavailables))
            cri.add(Restrictions.gt("stock", 0l));
        else if (StoreProperty.PROP_PRODUCT_HAS_STOCK_OR_ETA.equalsIgnoreCase(showunavailables))
            cri.add(Restrictions.or(Restrictions.gt("stock", 0l), Restrictions.and(Restrictions.isNotNull("eta"), Restrictions.ne("eta", ""))));

        cri.createAlias("labels", "labels", CriteriaSpecification.LEFT_JOIN);
        cri.createAlias("productCategories", "cats", CriteriaSpecification.LEFT_JOIN);
        cri.createAlias("manufacturer", "manufacturer", CriteriaSpecification.LEFT_JOIN);
        cri.createAlias("productLangs", "lang", CriteriaSpecification.LEFT_JOIN);
        if (filter != null) {
            // Inicializar el ordenamiento
            if (StringUtils.isEmpty(filter.getSortedField())) {
                filter.setSorted("hits:desc");
            }

            // Filtrar por id
            if (StringUtils.isNotBlank(filter.getFilterId())) {
                Long idProd = SomeUtils.strToLong(filter.getFilterId());
                if (idProd != null) cri.add(Restrictions.eq("idProduct", idProd));
            }
            // Filtrar por categorias
            if (StringUtils.isNotEmpty(filter.getFilterCategories())) {
                Long idcat = SomeUtils.strToLong(filter.getFilterCategories());
                Category beanCat = (Category) get(Category.class, idcat);
                if (beanCat != null) {
                    Long[] ids = getIdCategoryList(beanCat, false);
                    if (ids != null && ids.length > 0) cri.add(Restrictions.in("cats.idCategory", ids));
                }
            }
            // Filtrar por precio
            if (StringUtils.isNotEmpty(filter.getFilterMaxPrice())) {
                Double maxPrice = SomeUtils.strToDouble(filter.getFilterMaxPrice());
                if (maxPrice != null) cri.add(Restrictions.le("price", maxPrice));
            }
            if (StringUtils.isNotEmpty(filter.getFilterMinPrice())) {
                Double minPrice = SomeUtils.strToDouble(filter.getFilterMinPrice());
                if (minPrice != null) cri.add(Restrictions.ge("price", minPrice));
            }
            // Filtrar por stock
            if (StringUtils.isNotEmpty(filter.getFilterMaxStock())) {
                Long maxStock = SomeUtils.strToLong(filter.getFilterMaxStock());
                if (maxStock != null) cri.add(Restrictions.le("stock", maxStock));
            }
            if (StringUtils.isNotEmpty(filter.getFilterMinStock())) {
                Long minStock = SomeUtils.strToLong(filter.getFilterMinStock());
                if (minStock != null) cri.add(Restrictions.ge("stock", minStock));
            }
            if (StringUtils.isNotEmpty(filter.getFilterCode())) {
                cri.add(Restrictions.like("partNumber", filter.getFilterCode(), MatchMode.ANYWHERE));
            }
            if (StringUtils.isNotEmpty(filter.getFilterName())) {
                for (String n : filter.getFilterName().split(" ")) {
                    cri.add(Restrictions.like("lang.productName", n, MatchMode.ANYWHERE));
                }
            }
            // Filtrar por manufacturer
            if (StringUtils.isNotEmpty(filter.getFilterManufacturer())) {
                Long[] ids = SomeUtils.strToLong(filter.getFilterManufacturer().split(","));
                if (ids != null && ids.length > 0) cri.add(Restrictions.in("manufacturer.idManufacturer", ids));
            }
            // Filtrar por label
            if (StringUtils.isNotEmpty(filter.getFilterLabel())) {
                cri.add(Restrictions.eq("labels.code", filter.getFilterLabel()));
            }
        }

        if (extras != null) {
            // Productos por categoria
            Map<Object, Object> mapCategorias = new HashMap<Object, Object>();
            List lcat = cri.setProjection(Projections.projectionList()
                    .add(Projections.countDistinct("idProduct"))
                    .add(Projections.groupProperty("cats.idCategory")))
                    .list();
            for (Object o : lcat) {
                Object[] arr = (Object[]) o;
                mapCategorias.put(arr[1], arr[0]);
            }
            extras.put("CATEGORY", mapCategorias);

            // productos por manufacturer
            Map<Object, Object> mapManufacturer = new HashMap<Object, Object>();
            List lman = cri.setProjection(Projections.projectionList()
                    .add(Projections.countDistinct("idProduct"))
                    .add(Projections.groupProperty("manufacturer.idManufacturer")))
                    .list();
            for (Object o : lman) {
                Object[] arr = (Object[]) o;
                Manufacturer man = (Manufacturer) get(Manufacturer.class, (Serializable) arr[1]);
                if (man != null && StringUtils.isNotEmpty(man.getManufacturerName())) mapManufacturer.put(man, arr[0]);
            }
            extras.put("MANUFACTURER", mapManufacturer);

            // productos por manufacturer
            Map<Object, Object> mapLabels = new HashMap<Object, Object>();
            List llab = cri.setProjection(Projections.projectionList()
                    .add(Projections.countDistinct("idProduct"))
                    .add(Projections.groupProperty("labels.id")))
                    .list();
            for (Object o : llab) {
                Object[] arr = (Object[]) o;
                ProductLabel label = getProductLabel((Long) arr[1]);
                if (label != null && label.getFilterInListing()) mapLabels.put(label, arr[0]);
            }
            extras.put("LABELS", mapLabels);
        }

        if (isRandomSort) {
            // si queremos un resultado aleatorio
            cri.setProjection(Projections.distinct(Projections.property("idProduct")));
            List<Long> l = cri.list();
            if (l != null && l.size() > 0) {
                Criteria c = gethSession().createCriteria(Product.class)
                        .add(Restrictions.in("idProduct", l))
                        .addOrder(RandomOrder.random());
                if (nav != null) c.setMaxResults(nav.getPageRows());
                return c.list();
            } else return null;
        } else {
            // Si hay navegador contar los productos
            if (nav != null) {
                cri.setProjection(Projections.countDistinct("idProduct"));
                nav.setTotalRows((Integer) cri.uniqueResult());
                cri.setProjection(null);
            }

            // Hacer la proyeccion con el distinct
            ProjectionList p = Projections.projectionList();
            p.add(Projections.property("idProduct"), "id");
            if (filter != null)
                for (String f : filter.getSortedField().split(",")) {
                    if ("productName".equalsIgnoreCase(filter.getSortedField())) p.add(Projections.property("lang." + f.trim()));
                    else if ("manufacturer".equalsIgnoreCase(filter.getSortedField())) p.add(Projections.property("manufacturer.manufacturerName"));
                    else p.add(Projections.property(f.trim()));
                }
            cri.setProjection(Projections.distinct(p));
            cri.setResultTransformer(new IdToBeanTransformer(this, Product.class));

            // Si hay navegador y hace falta paginado, filtrar solo una pagina
            if (nav != null && nav.needPagination()) {
                cri.setFirstResult(nav.getFirstRow() - 1);
                cri.setMaxResults(nav.getPageRows());
            }

            // Ordenar
            if (filter != null) {
                for (String f : filter.getSortedField().split(",")) {
                    if ("productName".equalsIgnoreCase(filter.getSortedField()))
                        cri.addOrder(("desc".equalsIgnoreCase(filter.getSortedDirection())) ? Order.desc("lang." + f.trim()) : Order.asc("lang." + f.trim()));
                    else if ("manufacturer".equalsIgnoreCase(filter.getSortedField()))
                        cri.addOrder(("desc".equalsIgnoreCase(filter.getSortedDirection())) ? Order.desc("manufacturer.manufacturerName") : Order.asc("manufacturer.manufacturerName"));
                    else
                        cri.addOrder(("desc".equalsIgnoreCase(filter.getSortedDirection())) ? Order.desc(f.trim()) : Order.asc(f.trim()));
                }
            }
            return cri.list();
        }
    }
    */

    public List listFrontProducts(org.store.core.beans.utils.DataNavigator nav, ProductFilter filter, Map extras, String lang, Integer limit) {
        boolean isRandomSort = (filter != null && "random".equalsIgnoreCase(filter.getSortedField()));
        if (StringUtils.isEmpty(lang)) lang = "en";

        StringBuilder queryFrom = new StringBuilder(" from t_product p left join t_product_forusers on p.idProduct=t_product_forusers.t_product_idProduct ");

        if (StringUtils.isNotEmpty(filter.getFilterQuery()) || "productName".equalsIgnoreCase(filter.getSortedField()))
            queryFrom.append(" left join t_product_lang pl on p.idProduct=pl.product_idProduct and pl.productLang='").append(lang).append("' ");

        if ("manufacturer".equalsIgnoreCase(filter.getSortedField()))
            queryFrom.append(" left join t_manufacturer m on p.manufacturer_idManufacturer=m.idManufacturer ");

        if (StringUtils.isNotEmpty(filter.getFilterLabel()) || extras != null)
            queryFrom.append(" left join t_product_t_productlabel l on p.idProduct=l.t_product_idProduct ");

        Map<Long, String> filterAtt = filter.getFilterAttributesMap();
        if ((filterAtt != null && !filterAtt.isEmpty()) || extras != null)
            queryFrom.append(" left join  t_product_property on p.idProduct=t_product_property.product_idProduct ");

        if (StringUtils.isNotEmpty(filter.getFilterCategories()) || extras != null)
            queryFrom.append(" left join t_product_t_category c on p.idProduct=c.t_product_idProduct left join t_category_tree t on t.child_idCategory=c.productCategories_idCategory ");

        StringBuilder where = new StringBuilder(" where p.inventaryCode='" + storeCode + "' and p.active=1 and (p.archived is null or p.archived=0) and (p.productType is null or p.productType<>'" + Product.TYPE_COMPLEMENT + "') ");

        String showunavailables = getStorePropertyValue(StoreProperty.PROP_PRODUCT_SHOW_UNAVAILABLE, StoreProperty.TYPE_GENERAL, StoreProperty.PROP_DEFAULT_PRODUCT_SHOW_UNAVAILABLE);
        if (StoreProperty.PROP_PRODUCT_HAS_STOCK.equalsIgnoreCase(showunavailables))
            where.append(" and p.stock>0 ");
        else if (StoreProperty.PROP_PRODUCT_HAS_STOCK_OR_ETA.equalsIgnoreCase(showunavailables))
            where.append(" and (p.stock>0 or p.eta is not null and p.eta<>'') ");

        // Filtrar por nivel de usuario
        Long idLevel = (filter != null && StringUtils.isNotEmpty(filter.getFilterUserLevel())) ? SomeUtils.strToLong(filter.getFilterUserLevel()) : null;
        if (idLevel != null)
            where.append(" and (t_product_forusers.forUsers is null or t_product_forusers.forUsers=").append(idLevel).append(") ");
        else
            where.append(" and t_product_forusers.forUsers is null ");

        boolean hasFilterPrice = false;
        Category beanCat = null;
        if (filter != null) {
            // Filtrar por categorias
            if (StringUtils.isNotEmpty(filter.getFilterCategories())) {
                Long idcat = SomeUtils.strToLong(filter.getFilterCategories());
                beanCat = (Category) get(Category.class, idcat);
                if (beanCat != null) {
                    Long[] ids = getIdCategoryList(beanCat, false);
                    if (ids != null && ids.length > 0) {
                        StringBuilder buff = new StringBuilder();
                        for (Long idCat : ids) {
                            if (StringUtils.isNotEmpty(buff.toString())) buff.append(",");
                            buff.append(idCat);
                        }
                        where.append(" and c.productCategories_idCategory in (").append(buff.toString()).append(") ");
                    }
                }
            }

            // Filtrar por manufacturer
            if (StringUtils.isNotEmpty(filter.getFilterManufacturer())) {
                Long[] ids = SomeUtils.strToLong(filter.getFilterManufacturer().split(","));
                if (ids != null && ids.length == 1)
                    where.append(" and p.manufacturer_idManufacturer=").append(ids[0]).append(" ");
                else if (ids != null && ids.length > 1) {
                    StringBuilder buff = new StringBuilder();
                    for (Long id : ids) {
                        if (StringUtils.isNotEmpty(buff.toString())) buff.append(",");
                        buff.append(id);
                    }
                    where.append(" and p.manufacturer_idManufacturer in (").append(buff.toString()).append(") ");
                }
            }

            // Filtrar por label
            if (StringUtils.isNotEmpty(filter.getFilterLabel())) {
                ProductLabel label = getProductLabelByCode(filter.getFilterLabel());
                if (label != null) where.append(" and l.labels_id=").append(label.getId()).append(" ");
            }

            // Filtrar pro propiedades
            if (filterAtt != null && !filterAtt.isEmpty()) {
                for (Long idAtt : filterAtt.keySet()) {
                    AttributeProd ap = (AttributeProd) get(AttributeProd.class, idAtt);
                    if (ap != null && StringUtils.isNotEmpty(filterAtt.get(idAtt))) {
                        queryFrom.append(" left join t_product_property pp").append(idAtt).append(" on p.idProduct=pp").append(idAtt).append(".product_idProduct and pp").append(idAtt).append(".attribute_id=").append(ap.getId()).append(" ");
                        where.append(" and pp").append(idAtt).append(".propertyValue='").append(StringEscapeUtils.escapeSql(filterAtt.get(idAtt))).append("' ");
                    }
                }
            }

            //Filtrar por precio
            Double minPrice = SomeUtils.strToDouble(filter.getFilterMinPrice());
            Double maxPrice = SomeUtils.strToDouble(filter.getFilterMaxPrice());
            DecimalFormatSymbols dfs = new DecimalFormatSymbols(new Locale("en"));
            dfs.setDecimalSeparator('.');
            DecimalFormat df = new DecimalFormat("0.0000", dfs);
            if (minPrice != null) {
                where.append(" and p.calculatedPrice>=").append(df.format(minPrice)).append(" ");
                hasFilterPrice = true;
            }
            if (maxPrice != null) {
                where.append(" and p.calculatedPrice<").append(df.format(maxPrice)).append(" ");
                hasFilterPrice = true;
            }

            // filtrar por nombre
            if (StringUtils.isNotEmpty(filter.getFilterQuery())) {
                String[] arrQuery = filter.getFilterQuery().trim().split(" ");
                if (arrQuery != null && arrQuery.length > 0) {
                    for (String q : arrQuery)
                        if (StringUtils.isNotEmpty(q.trim())) {
                            where.append(" AND ((pl.productName like '%").append(q).append("%') OR (pl.description like '%").append(q).append("%') OR (p.partNumber like '%").append(q).append("%') OR (p.searchKeywords like '%").append(q).append("%')) ");
                        }
                }
            }

        }

        Number totalRows = null;
        if (extras != null) {
            // Productos por categoria
            Map<Object, Object> mapCategorias = new HashMap<Object, Object>();
            List lCat = gethSession().createSQLQuery("select count(distinct p.idProduct), c.productCategories_idCategory " + queryFrom.toString() + where.toString() + " group by c.productCategories_idCategory").list();
            for (Object o : lCat) {
                Object[] arr = (Object[]) o;
                if (arr.length == 2 && arr[1] != null)
                    mapCategorias.put(((Number) arr[1]).longValue(), arr[0]);
            }
            extras.put("CATEGORY", mapCategorias);

            // productos por manufacturer
            Integer maxManufacturers = SomeUtils.strToInteger(getStorePropertyValue(StoreProperty.PROP_MAX_MANUFACTURER_LIST, StoreProperty.TYPE_GENERAL, "20"));
            if (maxManufacturers == null) maxManufacturers = 20;
            List<Manufacturer> listManufacturer = new ArrayList<Manufacturer>();
            List lMan = gethSession().createSQLQuery("select count(distinct p.idProduct) as c, p.manufacturer_idManufacturer " + queryFrom.toString() + where.toString() + " group by p.manufacturer_idManufacturer order by c desc").list();
            int index = 0;
            for (Object o : lMan) {
                Object[] arr = (Object[]) o;
                if (arr.length == 2 && arr[1] != null && (arr[0] instanceof Number) && (((Number) arr[0]).intValue() > 0)) {
                    Manufacturer man = (Manufacturer) get(Manufacturer.class, ((Number) arr[1]).longValue());
                    if (man != null && StringUtils.isNotEmpty(man.getManufacturerName())) {
                        man.addProperty("NUM_PROD", arr[0]);
                        listManufacturer.add(man);
                        if (index++ > maxManufacturers) break;
                    }
                }
            }
            extras.put("MANUFACTURER", listManufacturer);

            // productos por label
            Map<Object, Object> mapLabels = new HashMap<Object, Object>();
            List lLab = gethSession().createSQLQuery("select count(distinct p.idProduct), l.labels_id " + queryFrom.toString() + where.toString() + " group by l.labels_id").list();
            for (Object o : lLab) {
                Object[] arr = (Object[]) o;
                if (arr.length == 2 && arr[1] != null) {
                    ProductLabel label = getProductLabel(((Number) arr[1]).longValue());
                    if (label != null && label.getFilterInListing()) mapLabels.put(label, arr[0]);
                }
            }
            extras.put("LABELS", mapLabels);

            if (!hasFilterPrice && "Y".equalsIgnoreCase(getParentPropertyValue(beanCat, "filterByPrice"))) {
                List<Map<String, Double>> listaPrecios = beanCat.getPriceRange();
                if (listaPrecios == null || listaPrecios.isEmpty()) {
                    Object[] arr = (Object[]) gethSession().createSQLQuery("select count(distinct p.idProduct), min(p.calculatedPrice), max(p.calculatedPrice) " + queryFrom.toString() + where.toString()).uniqueResult();
                    totalRows = (arr.length > 0 && arr[0] != null && arr[0] instanceof Number) ? (Number) arr[0] : null;
                    Double min = (arr.length > 1 && arr[1] != null && arr[1] instanceof Number) ? ((Number) arr[1]).doubleValue() : null;
                    Double max = (arr.length > 2 && arr[2] != null && arr[2] instanceof Number) ? ((Number) arr[2]).doubleValue() : null;
                    Dialect dialect = getCurrentDialect();
                    if (dialect instanceof SQLServerDialect) {
                        Object oMin = gethSession().createSQLQuery("select min(p.calculatedPrice) " + queryFrom.toString() + where.toString()).uniqueResult();
                        if (oMin != null && oMin instanceof Number) min = ((Number) oMin).doubleValue();
                        Object oMax = gethSession().createSQLQuery("select max(p.calculatedPrice) " + queryFrom.toString() + where.toString()).uniqueResult();
                        if (oMax != null && oMax instanceof Number) max = ((Number) oMax).doubleValue();
                    }
                    if (min != null && max != null) {
                        PriceFilter pf = new PriceFilter(5, min, max);
                        listaPrecios = pf.getAlg1();
                    }
                }
                if (listaPrecios != null && !listaPrecios.isEmpty()) {
                    for (Map<String, Double> m : listaPrecios) {
                        String qStr = "select count(distinct p.idProduct) " + queryFrom.toString() + where.toString();
                        if (m.containsKey("min") && m.get("min") != null) qStr += " and p.calculatedPrice>:minP ";
                        if (m.containsKey("max") && m.get("max") != null) qStr += " and p.calculatedPrice<=:maxP ";
                        SQLQuery qPrice = gethSession().createSQLQuery(qStr);
                        if (m.containsKey("min") && m.get("min") != null) qPrice.setDouble("minP", m.get("min"));
                        if (m.containsKey("max") && m.get("max") != null) qPrice.setDouble("maxP", m.get("max"));
                        Number c = (Number) qPrice.uniqueResult();
                        m.put("count", (c != null) ? c.doubleValue() : 0);
                    }
                    extras.put("PRICES", listaPrecios);
                }
            }

            // productos por atributos
            if (beanCat != null) {
                Map<Object, Object> mapAttributes = new HashMap<Object, Object>();
                List<CategoryProperty> catProps = getParentCategoryProperties(beanCat, true, true);
                if (catProps != null && !catProps.isEmpty()) {
                    for (CategoryProperty cp : catProps) {
                        Map<Object, Object> mapAttValues = new HashMap<Object, Object>();
                        List lAtt = gethSession().createSQLQuery("select count(distinct p.idProduct), t_product_property.propertyValue " + queryFrom.toString() + where.toString() + " and t_product_property.attribute_id=" + cp.getAttribute().getId() + " group by t_product_property.propertyValue").list();
                        for (Object o : lAtt) {
                            Object[] arr = (Object[]) o;
                            if (arr.length == 2 && arr[1] != null) {
                                mapAttValues.put(arr[1], arr[0]);
                            }
                        }
                        mapAttributes.put(cp, mapAttValues);
                    }
                }
                extras.put("ATTRIBUTES", mapAttributes);
            }

        }

        if (isRandomSort) {
            // si queremos un resultado aleatorio
            SQLQuery q = gethSession().createSQLQuery("select idProduct, " + randomFunction() + " from (select distinct p.idProduct " + queryFrom.toString() + where.toString() + ") t order by " + randomFunction());
            if (nav != null) q.setMaxResults(nav.getPageRows());
            else if (limit != null && limit > 0) q.setMaxResults(limit);
            else q.setMaxResults(100);
            q.setResultTransformer(new IdToBeanTransformer(this, Product.class));
            return q.list();

        } else {
            // Si hay navegador contar los productos
            if (nav != null) {
                if (totalRows == null) totalRows = (Number) gethSession().createSQLQuery("select count(distinct p.idProduct) " + queryFrom.toString() + where.toString()).uniqueResult();
                nav.setTotalRows(totalRows.intValue());
            }
            // Ordenar
            boolean filterByPrice = false;
            StringBuilder sort = new StringBuilder();
            StringBuilder select = new StringBuilder("select distinct p.idProduct");
            if (filter != null) {
                for (String f : filter.getSortedField().split(",")) {
                    if ("calculatedPrice".equalsIgnoreCase(filter.getSortedField())) filterByPrice = true;

                    if ("productName".equalsIgnoreCase(filter.getSortedField())) {
                        if (StringUtils.isNotEmpty(sort.toString())) sort.append(", ");
                        sort.append(" pl.productName").append(("desc".equalsIgnoreCase(filter.getSortedDirection())) ? " desc " : " asc ");
                        select.append(", pl.productName ");
                    } else if ("manufacturer".equalsIgnoreCase(filter.getSortedField())) {
                        if (StringUtils.isNotEmpty(sort.toString())) sort.append(", ");
                        sort.append(" m.manufacturerName").append(("desc".equalsIgnoreCase(filter.getSortedDirection())) ? " desc " : " asc ");
                        select.append(", m.manufacturerName ");
                    } else {
                        if (StringUtils.isNotEmpty(sort.toString())) sort.append(", ");
                        sort.append(" ").append(f.trim()).append(("desc".equalsIgnoreCase(filter.getSortedDirection())) ? " desc " : " asc ");
                        select.append(", ").append(f.trim()).append(" ");
                    }
                }
            }
            if (StringUtils.isNotEmpty(sort.toString())) sort.append(", ");
            sort.append(" p.idProduct ");
            sort.insert(0, " order by ");
            SQLQuery query = gethSession().createSQLQuery(select.toString() + queryFrom.toString() + where.toString() + sort.toString());
            query.setResultTransformer(new IdToBeanTransformer(this, Product.class));

            // Si hay navegador y hace falta paginado, filtrar solo una pagina
            if (nav != null && nav.needPagination()) {
                if (filterByPrice) {
                    return getPageOfProductsSortedByPrice(query.list(), filter, nav);
                } else {
                    query.setFirstResult(nav.getFirstRow() - 1);
                    query.setMaxResults(nav.getPageRows());
                    return query.list();
                }
            } else {
                if (limit != null && limit > 0) query.setMaxResults(limit);
                else query.setMaxResults(100);
                return query.list();
            }
        }
    }

    private List getPageOfProductsSortedByPrice(List list, ProductFilter filter, DataNavigator nav) {
        if (list != null && !list.isEmpty()) {
            List result = new ArrayList();
            Collections.sort(list, new ProductPriceComparator(filter.getSortedDirection()));
            for (int i = nav.getFirstRow() - 1; i < nav.getLastRow(); i++)
                if (i < list.size()) result.add(list.get(i));
            return result;
        }
        return null;
    }

    public List<CategoryDTO> getSubcategories(Long idCategory, String lang, Map numProducts, boolean children) {
        return getSubcategories(idCategory, lang, numProducts, children, 0);
    }

    public List<CategoryDTO> getSubcategories(Long idCategory, String lang, Map numProducts, boolean children, int level) {
        List<CategoryDTO> l = null;
        if (idCategory != null) {
            String query = "select t_category.idCategory, t_category.urlCode, t_category_lang.categoryName, t_category_tree.position " +
                    "from t_category_tree left join t_category on t_category.idCategory=t_category_tree.child_idCategory left join t_category_lang on t_category.idCategory=t_category_lang.category_idCategory and t_category_lang.categoryLang='" + lang + "' " +
                    "where t_category.inventaryCode='" + getStoreCode() + "' and t_category_tree.parent_idCategory=" + idCategory.toString() + " and t_category.active=1 and t_category.visible=1 order by t_category_tree.position, t_category.defaultPosition";

            SQLQuery q = gethSession().createSQLQuery(query);
            List<Object[]> lObj = q.list();
            if (lObj != null && !lObj.isEmpty()) {
                l = new ArrayList<CategoryDTO>();
                for (Object[] arr : lObj) {
                    Number idCat = (Number) arr[0];
                    Number pos = (Number) arr[3];
                    CategoryDTO dto = new CategoryDTO();
                    if (idCat != null) dto.setIdCategory(idCat.longValue());
                    dto.setUrlCode((String) arr[1]);
                    dto.setName((String) arr[2]);
                    if (pos != null) dto.setPosition(pos.intValue());
                    l.add(dto);
                }
            }
            /** Optimizado arriba haciendo la consulta directamente
             Criteria cri = gethSession().createCriteria(CategoryTree.class).setCacheable(false);
             cri.createAlias("child", "child", Criteria.INNER_JOIN);
             cri.createAlias("parent", "parent", Criteria.LEFT_JOIN);
             cri.add(Restrictions.eq("parent.idCategory", idCategory));
             cri.add(Restrictions.eq("child.visible", Boolean.TRUE));
             cri.setProjection(Projections.distinct(Projections.property("id")));
             cri.setResultTransformer(new CategoryTreeTransformer(lang, this));
             l = cri.list();
             **/
        } else {
            String query = "select t_category.idCategory, t_category.urlCode, t_category_lang.categoryName, t_category.defaultPosition from t_category left join t_category_lang on t_category.idCategory=t_category_lang.category_idCategory and t_category_lang.categoryLang='" + lang + "' " +
                    "where t_category.inventaryCode='" + getStoreCode() + "' and t_category.active=1 and t_category.visible=1 and t_category.idCategory not in (select t_category_tree.child_idCategory from t_category_tree) order by t_category.defaultPosition";
            SQLQuery q = gethSession().createSQLQuery(query);
            List<Object[]> lObj = q.list();
            if (lObj != null && !lObj.isEmpty()) {
                l = new ArrayList<CategoryDTO>();
                for (Object[] arr : lObj) {
                    Number idCat = (Number) arr[0];
                    CategoryDTO dto = new CategoryDTO();
                    if (idCat != null) dto.setIdCategory(idCat.longValue());
                    dto.setUrlCode((String) arr[1]);
                    dto.setName((String) arr[2]);
                    dto.setPosition(arr[3] != null ? (Integer) arr[3] : 999999);
                    l.add(dto);
                }
            }
            /*

            List lChildren = gethSession().createQuery("select distinct t.child.idCategory from CategoryTree t").list();
            Criteria cri = gethSession().createCriteria(Category.class)
                    .add(Restrictions.eq("visible", Boolean.TRUE))
                    .add(Restrictions.eq("inventaryCode", storeCode));
            if (lChildren != null && !lChildren.isEmpty()) cri.add(Restrictions.not(Restrictions.in("idCategory", lChildren)));
            cri.setProjection(Projections.distinct(Projections.property("idCategory")));
            cri.setResultTransformer(new CategoryTransformer(lang, this));
            l = cri.list();
            */
        }
        if (l != null && l.size() > 0) {
            for (CategoryDTO dto : l) {
                dto.setLevel(level);
                if (numProducts != null && numProducts.containsKey(dto.getIdCategory())) dto.setProducts(((Number) numProducts.get(dto.getIdCategory())).longValue());
                if (children) dto.setChildren(getSubcategories(dto.getIdCategory(), lang, numProducts, children, level + 1));
            }
        }
        return l;
    }

    public List<Banner> getRandomBanners(String zone, Category category, Product product, int cant) {
        Criteria cri = createCriteriaForStore(Banner.class);
        cri.add(Restrictions.eq("active", Boolean.TRUE));
        if (StringUtils.isNotEmpty(zone)) cri.add(Restrictions.eq("bannerZone", zone));
        if (category != null) cri.add(Restrictions.eq("forCategory", category));
        else cri.add(Restrictions.isNull("forCategory"));
        if (product != null) cri.add(Restrictions.eq("forProduct", product));
        else cri.add(Restrictions.isNull("forProduct"));
        cri.addOrder(RandomOrder.random());
        cri.setMaxResults(cant);
        return cri.list();
    }

    public StaticTextLang getStaticTextLang(StaticText staticText, String lang) {
        List l = gethSession().createCriteria(StaticTextLang.class).add(Restrictions.eq("staticText", staticText)).add(Restrictions.eq("staticLang", lang)).list();
        return (l != null && l.size() > 0) ? (StaticTextLang) l.get(0) : null;
    }

    public void fillMenuNodeChilds(DefaultMutableTreeNode node, String menuType) {
        Menu bean = null;
        if (node.getUserObject() != null) {
            bean = (Menu) node.getUserObject();
        }

        List<Menu> listaChild = getMenus(menuType, bean);
        for (Object aListaChild : listaChild) {
            Menu beanChild = (Menu) aListaChild;
            DefaultMutableTreeNode nodeChild = new DefaultMutableTreeNode(beanChild);
            node.add(nodeChild);
            fillMenuNodeChilds(nodeChild, menuType);
        }
    }

    public List<Menu> getMenus(String menuType, Menu parent) {
        Criteria cri = createCriteriaForStore(Menu.class);
        cri.add(Restrictions.eq("menu", menuType));
        if (parent != null) cri.add(Restrictions.eq("idParent", parent.getId()));
        else cri.add(Restrictions.isNull("idParent"));
        cri.addOrder(Order.asc("menuOrder"));
        return cri.list();
    }

    public List<String> getMenuTypes() {
        return createCriteriaForStore(Menu.class)
                .setProjection(Projections.distinct(Projections.property("menu")))
                .addOrder(Order.asc("menu")).list();
    }

    public boolean anonymousVolumePrice() {
        List l = createCriteriaForStore(UserLevel.class).add(Restrictions.eq("disableVolume", Boolean.TRUE)).list();
        return (l == null || l.size() < 1);
    }

    public Long getLastAuditStock(Product p) {
        List l = gethSession().createCriteria(ProductAuditStock.class)
                .add(Restrictions.eq("product", p))
                .addOrder(Order.desc("changeDate"))
                .setMaxResults(1)
                .setProjection(Projections.property("stock"))
                .list();
        return (l != null && l.size() > 0) ? (Long) l.get(0) : null;
    }

    public Date getProductCreatedDate(Product p) {
        List l = gethSession().createCriteria(ProductAuditStock.class)
                .add(Restrictions.eq("product", p))
                .addOrder(Order.asc("changeDate"))
                .setMaxResults(1)
                .setProjection(Projections.property("changeDate"))
                .list();
        return (l != null && l.size() > 0) ? (Date) l.get(0) : null;
    }

    public List<User> getUsersForStockAlert(Product p) {
        return gethSession().createCriteria(UserPreference.class)
                .add(Restrictions.eq("preferenceCode", UserPreference.STOCK_ALERT))
                .add(Restrictions.like("preferenceValue", p.getIdProduct().toString(), MatchMode.EXACT))
                .setProjection(Projections.property("user"))
                .list();
    }

    public boolean canSaveStaticText(StaticText staticText) {
        Criteria cri = createCriteriaForStore(StaticText.class);
        cri.add(Restrictions.eq("code", staticText.getCode()));
        if (staticText.getId() != null) cri.add(Restrictions.ne("id", staticText.getId()));
        List l = cri.list();
        return (CollectionUtils.isEmpty(l));
    }

    public CategoryStaticText getCategoryStaticText(Category category, StaticText beanST) {
        List l = gethSession().createCriteria(CategoryStaticText.class)
                .add(Restrictions.eq("category", category))
                .add(Restrictions.eq("staticText", beanST))
                .list();
        return (l != null && l.size() > 0) ? (CategoryStaticText) l.get(0) : null;
    }

    public List<ProductStaticText> getProductStaticText(Product product) {
        return gethSession().createCriteria(ProductStaticText.class)
                .add(Restrictions.eq("product", product))
                .addOrder(Order.asc("contentOrder"))
                .addOrder(Order.asc("id")).list();
    }

    public ProductStaticText getProductStaticText(Product product, StaticText beanST) {
        List l = gethSession().createCriteria(ProductStaticText.class)
                .add(Restrictions.eq("product", product))
                .add(Restrictions.eq("staticText", beanST))
                .list();
        return (l != null && l.size() > 0) ? (ProductStaticText) l.get(0) : null;
    }

    public List<Resource> getResources(DataNavigator nav, String resourceType, String filterName) {
        Criteria cri = createCriteriaForStore(Resource.class);
        if (StringUtils.isNotEmpty(resourceType)) cri.add(Restrictions.eq("resourceType", resourceType));
        if (StringUtils.isNotEmpty(filterName)) cri.add(Restrictions.like("resourceName", filterName, MatchMode.ANYWHERE));
        if (nav != null) {
            cri.setProjection(Projections.countDistinct("id"));
            Number total = (Number) cri.uniqueResult();
            nav.setTotalRows(total != null ? total.intValue() : 0);
            cri.setProjection(null);
            if (nav.needPagination()) {
                cri.setFirstResult(nav.getFirstRow() - 1);
                cri.setMaxResults(nav.getPageRows());
            }
        }
        cri.addOrder(Order.asc("id"));
        return cri.list();
    }

    public List<Resource> getResourcesByFilename(String filename) {
        if (StringUtils.isNotEmpty(filename)) {
            return createCriteriaForStore(Resource.class).add(Restrictions.eq("fileName", filename)).list();
        }
        return null;
    }

    public boolean resourceExist(String filename) {
        List l = getResourcesByFilename(filename);
        return l != null && !l.isEmpty();
    }

    public List<Banner> getBanners(String bannerZone) {
        Criteria cri = createCriteriaForStore(Banner.class)
                .add(Restrictions.eq("bannerZone", bannerZone))
                .addOrder(Order.desc("id"));
        return cri.list();
    }

    public OrderStatus getOrderStatus(String status, boolean createIfNotExist) {
        List l = createCriteriaForStore(OrderStatus.class).add(Restrictions.eq("statusCode", status)).list();
        OrderStatus orderStatus = (l != null && l.size() > 0) ? (OrderStatus) l.get(0) : null;
        if (orderStatus == null && createIfNotExist) {
            orderStatus = new OrderStatus();
            orderStatus.setStatusCode(status);
            orderStatus.setStatusType(status);
            orderStatus.setSendEmail(true);
            if (hasStore()) orderStatus.setInventaryCode(storeCode);
            save(orderStatus);
        }
        return orderStatus;
    }

    public int getNumBuyedProduct(Product product, User u) {
        int res = 0;
        Criteria cri = gethSession().createCriteria(OrderDetailProduct.class);
        cri.add(Restrictions.eq("product", product));
        cri.createCriteria("orderDetail").createCriteria("order").add(Restrictions.eq("user", u));
        List<OrderDetailProduct> l = cri.list();
        for (OrderDetailProduct odp : l) {
            if (!OrderStatus.STATUS_REJECTED.equalsIgnoreCase(odp.getOrderDetail().getOrder().getStatus().getStatusType())) res += odp.getOrderDetail().getQuantity();
        }
        return res;
    }

    public ShippingRate getCategoryShipping(Category category, State state) {
        List l = gethSession().createCriteria(ShippingRate.class)
                .add(Restrictions.eq("category", category))
                .add(Restrictions.eq("state", state))
                .list();
        return (l != null && l.size() > 0) ? (ShippingRate) l.get(0) : null;
    }

    public ShippingRate getProductShipping(Product product, State state) {
        List l = gethSession().createCriteria(ShippingRate.class)
                .add(Restrictions.eq("product", product))
                .add(Restrictions.eq("state", state))
                .list();
        return (l != null && l.size() > 0) ? (ShippingRate) l.get(0) : null;
    }

    public ShippingRate getShippingRate(Product product, State state) {
        ShippingRate res = getProductShipping(product, state);
        if (res == null && product.getCategory() != null) {
            Map<State, ShippingRate> map = getParentCategoryShipping(product.getCategory());
            if (map != null && map.containsKey(state)) res = map.get(state);
        }
        return res;
    }

    public List<Tax> getTaxes(String countryCode, State state) {
        return createCriteriaForStore(Tax.class)
                .add(Restrictions.disjunction().add(Restrictions.isNull("country")).add(Restrictions.eq("country", "")).add(Restrictions.eq("country", countryCode)))
                .add(Restrictions.or(Restrictions.isNull("state"), Restrictions.eq("state", state)))
                .addOrder(Order.asc("taxOrder"))
                .list();
    }

    public List<String> getCarriers(boolean onlyActives) {
        Criteria cri = createCriteriaForStore(ShippingMethod.class);
        if (onlyActives) cri.add(Restrictions.eq("active", Boolean.TRUE));
        cri.setProjection(Projections.distinct(Projections.property("carrierName")));
        return cri.list();
    }

    public ShippingMethod getDefaultShippingMethod() {
        List l = createCriteriaForStore(ShippingMethod.class)
                .add(Restrictions.eq("defaultMethod", Boolean.TRUE))
                .list();
        return (l != null && l.size() > 0) ? (ShippingMethod) l.get(0) : null;
    }

    public Map<String, ShippingMethod> getMapShippingMethods() {
        List<ShippingMethod> l = createCriteriaForStore(ShippingMethod.class)
                .addOrder(Order.asc("carrierName"))
                .addOrder(Order.asc("methodCode"))
                .list();
        if (l != null && !l.isEmpty()) {
            Map<String, ShippingMethod> res = new HashMap<String, ShippingMethod>();
            for (ShippingMethod m : l) {
                res.put(m.getCarrierName().toUpperCase() + "-" + m.getMethodCode(), m);
            }
            return res;
        }
        return null;
    }

    public Properties getCarrierProperties(String carrier) {
        List<StoreProperty> l = createCriteriaForStore(StoreProperty.class)
                .add(Restrictions.eq("type", "CARRIER_" + carrier))
                .list();
        if (l != null) {
            Properties prop = new Properties();
            for (StoreProperty sp : l) {
                prop.setProperty(sp.getCode(), sp.getValue());
            }
            return prop;
        }
        return null;
    }

    public Properties getMerchantProperties(String merchant) {
        List<StoreProperty> l = createCriteriaForStore(StoreProperty.class)
                .add(Restrictions.eq("type", "MERCHANT_" + merchant))
                .list();
        if (l != null) {
            Properties prop = new Properties();
            for (StoreProperty sp : l) {
                prop.setProperty(sp.getCode(), sp.getValue());
            }
            return prop;
        }
        return null;
    }


    public PromotionalCode getPromotionalCode(String code) {
        List l = createCriteriaForStore(PromotionalCode.class)
                .add(Restrictions.eq("code", code)).list();
        return (l != null && !l.isEmpty()) ? (PromotionalCode) l.get(0) : null;
    }

    public boolean productNeedShipping(Product beanProd1) {
        return "Y".equalsIgnoreCase(getParentPropertyValue(beanProd1, "needShipping"));
    }

    public Integer getMaxDownloads(Product beanProd1) {
        if (beanProd1 != null && beanProd1.getMaxDownloads() != null) return beanProd1.getMaxDownloads();
        else {
            Integer maxNumber = SomeUtils.strToInteger(getStorePropertyValue(StoreProperty.PROP_MAX_PRODUCT_DOWNLOADS, StoreProperty.TYPE_GENERAL, StoreProperty.PROP_DEFAULT_MAX_PRODUCT_DOWNLOADS));
            return (maxNumber != null) ? maxNumber : 3;
        }
    }

    public List<org.store.core.beans.Order> getOrdersByUser(DataNavigator nav, User user) {
        Criteria cri = gethSession().createCriteria(org.store.core.beans.Order.class).add(Restrictions.eq("user", user));
        if (nav != null) {
            cri.setProjection(Projections.countDistinct("idOrder"));
            Number total = (Number) cri.uniqueResult();
            nav.setTotalRows(total != null ? total.intValue() : 0);
            cri.setProjection(null);
            if (nav.needPagination()) {
                cri.setFirstResult(nav.getFirstRow() - 1);
                cri.setMaxResults(nav.getPageRows());
            }
        }
        cri.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        cri.addOrder(Order.desc("createdDate"));
        return cri.list();
    }

    public List<org.store.core.beans.Order> getOrders(DataNavigator nav, Long filterCode, Date filterDateFrom, Date filterDateTo, List<OrderStatus> filterStatus, String filterUser, Long filterAdmin, String orderField, String orderDirection) {
        Criteria cri = gethSession().createCriteria(org.store.core.beans.Order.class);
        if (filterCode != null) cri.add(Restrictions.eq("idOrder", filterCode));
        if (filterDateFrom != null) cri.add(Restrictions.ge("createdDate", filterDateFrom));
        if (filterDateTo != null) cri.add(Restrictions.le("createdDate", filterDateTo));
        if (filterStatus != null && filterStatus.size() > 0) cri.add(Restrictions.in("status", filterStatus));
        if (filterAdmin != null) cri.add(Restrictions.eq("idAdminUser", filterAdmin));
        Criteria criUser = cri.createCriteria("user", Criteria.LEFT_JOIN);
        addStore(criUser);
        if (StringUtils.isNotEmpty(filterUser)) {
            for (String n : filterUser.split(" ")) {
                Disjunction d = Restrictions.disjunction();
                d.add(Restrictions.like("firstname", n, MatchMode.ANYWHERE));
                d.add(Restrictions.like("lastname", n, MatchMode.ANYWHERE));
                d.add(Restrictions.like("email", n, MatchMode.ANYWHERE));
                criUser.add(d);
            }
        }
        if (nav != null) {
            cri.setProjection(Projections.countDistinct("idOrder"));
            Number total = (Number) cri.uniqueResult();
            nav.setTotalRows(total != null ? total.intValue() : 0);
            cri.setProjection(null);
            if (nav.needPagination()) {
                cri.setFirstResult(nav.getFirstRow() - 1);
                cri.setMaxResults(nav.getPageRows());
            }
        }
        cri.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        if (StringUtils.isEmpty(orderField)) {
            orderField = "idOrder";
            orderDirection = "desc";
        }
        if ("desc".equalsIgnoreCase(orderDirection)) cri.addOrder(Order.desc(orderField));
        else cri.addOrder(Order.asc(orderField));
        return cri.list();
    }

    public int getPackedOrderDetailProducts(OrderDetailProduct odp) {
        if (odp != null) {
            Criteria cri = gethSession().createCriteria(OrderPackingProduct.class).add(Restrictions.eq("orderDetailProduct", odp));
            cri.setProjection(Projections.countDistinct("id"));
            Number total = (Number) cri.uniqueResult();
            return (total != null ? total.intValue() : 0);
        }
        return 0;
    }

    public List<OrderPacking> getOrderPackages(org.store.core.beans.Order order) {
        return (order != null) ? gethSession().createCriteria(OrderPacking.class).add(Restrictions.eq("order", order)).list() : null;
    }

    public List<ShippingMethod> getShippingMethodList() {
        return createCriteriaForStore(ShippingMethod.class).addOrder(Order.asc("carrierName")).addOrder(Order.asc("methodCode")).list();
    }

    public List<OrderPacking> getPendingOrderPackages(org.store.core.beans.Order order) {
        List<OrderPacking> l = getOrderPackages(order);
        if (l != null) {
            List<OrderPacking> res = new ArrayList<OrderPacking>();
            for (OrderPacking p : l) if (p.getPending()) res.add(p);
            return res;
        }
        return null;
    }

    public List<Rma> getRmas(OrderPackingProduct opp) {
        return (opp != null) ? gethSession().createCriteria(Rma.class).add(Restrictions.eq("orderProduct", opp)).addOrder(Order.asc("createdDate")).list() : null;
    }

    public List getRmas(DataNavigator nav, String rmaNumber, Date filterDateFrom, Date filterDateTo, String[] filterStatus, RmaType rmaType) {
        Criteria cri = gethSession().createCriteria(Rma.class);
        addStore(cri.createCriteria("orderProduct").createCriteria("packing").createCriteria("order").createCriteria("user"));
        if (StringUtils.isNotEmpty(rmaNumber)) cri.add(Restrictions.like("rmaNumber", rmaNumber, MatchMode.ANYWHERE));
        if (rmaType != null) cri.add(Restrictions.eq("rmaType", rmaType));
        if (filterDateFrom != null) cri.add(Restrictions.ge("createdDate", filterDateFrom));
        if (filterDateTo != null) cri.add(Restrictions.le("createdDate", filterDateTo));
        if (filterStatus != null && filterStatus.length > 0) cri.add(Restrictions.in("rmaStatus", filterStatus));
        if (nav != null) {
            cri.setProjection(Projections.countDistinct("id"));
            Number total = (Number) cri.uniqueResult();
            nav.setTotalRows(total != null ? total.intValue() : 0);
            cri.setProjection(null);
            if (nav.needPagination()) {
                cri.setFirstResult(nav.getFirstRow() - 1);
                cri.setMaxResults(nav.getPageRows());
            }
        }
        cri.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        cri.addOrder(Order.desc("id"));
        return cri.list();
    }

    public ProductRelated getProductRelated(Product product, Product related) {
        List l = (product != null && related != null) ? gethSession().createCriteria(ProductRelated.class)
                .add(Restrictions.eq("product", product))
                .add(Restrictions.eq("related", related))
                .list() : null;
        return (l != null && l.size() > 0) ? (ProductRelated) l.get(0) : null;
    }

    public List<ProductRelated> getProductsRelated(Product product) {
        return (product != null) ? gethSession().createCriteria(ProductRelated.class)
                .add(Restrictions.eq("product", product))
                .addOrder(Order.asc("id"))
                .list() : null;
    }

    public List<ProductRelated> getProductsRelatedCombined(Product p, boolean combined) {
        if (p != null) {
            Criteria cri = gethSession().createCriteria(ProductRelated.class).add(Restrictions.eq("product", p));
            if (combined) {
                cri.add(Restrictions.isNotNull("combinedPrice"));
                cri.addOrder(Order.asc("combinedPrice"));
            } else {
                cri.add(Restrictions.isNull("combinedPrice"));
                cri.addOrder(Order.asc("id"));
            }
            return cri.list();
        }
        return null;
    }

    public List<StaticText> getStaticTexts(String filterType) {
        Criteria cri = createCriteriaForStore(StaticText.class);
        if (StringUtils.isNotEmpty(filterType)) cri.add(Restrictions.eq("textType", filterType));
        cri.addOrder(Order.asc("textType"));
        cri.addOrder(Order.asc("code"));
        return cri.list();
    }

    public List<StaticText> getStaticTexts(DataNavigator nav, String filterType, String filterName) {
        Criteria cri = createCriteriaForStore(StaticText.class);
        if (StringUtils.isNotEmpty(filterType)) cri.add(Restrictions.eq("textType", filterType));
        if (StringUtils.isNotEmpty(filterName))
            cri.createCriteria("staticTextLangs", Criteria.LEFT_JOIN).add(Restrictions.like("title", filterName, MatchMode.ANYWHERE));
        if (nav != null) {
            cri.setProjection(Projections.countDistinct("id"));
            Number total = (Number) cri.uniqueResult();
            nav.setTotalRows(total != null ? total.intValue() : 0);
            cri.setProjection(Projections.distinct(Projections.property("id")));
            cri.setResultTransformer(new IdToBeanTransformer(this, StaticText.class));
            if (nav.needPagination()) {
                cri.setMaxResults(nav.getPageRows());
                cri.setFirstResult(nav.getFirstRow() - 1);
            }
        }
        //     cri.addOrder(Order.desc("contentDate"));
        cri.addOrder(Order.desc("id"));
        return cri.list();
    }

    public Currency getCurrency(String code) {
        List l = (code != null) ? createCriteriaForStore(Currency.class).add(Restrictions.eq("code", code)).list() : null;
        return (l != null && l.size() > 0) ? (Currency) l.get(0) : null;
    }

    public List<Product> simpleSearchLucene(DataNavigator nav, String search, String lang, Long userLevel) throws ParseException, IOException {
        FullTextSession fullTextSession = Search.getFullTextSession(hSession);

        BooleanQuery query = new BooleanQuery();
        BooleanQuery tq = new BooleanQuery();

        ProductAnalyzer sa = new ProductAnalyzer();
        TokenStream ts = sa.tokenStream("name_" + lang, new StringReader(search));
        while (ts.incrementToken()) {
            String s = ts.getAttribute(CharTermAttribute.class).toString();
            if (StringUtils.isNotEmpty(s))
                tq.add(new TermQuery(new Term("name_" + lang, s)), BooleanClause.Occur.SHOULD);
        }
        tq.add(new FuzzyQuery(new Term("name_" + lang, search.toLowerCase())), BooleanClause.Occur.SHOULD);
        tq.add(new TermQuery(new Term("partNumber", search)), BooleanClause.Occur.SHOULD);
        tq.add(new TermQuery(new Term("mfgPartnumber", search)), BooleanClause.Occur.SHOULD);
        tq.add(new TermQuery(new Term("idProduct", search)), BooleanClause.Occur.SHOULD);

        query.add(tq, BooleanClause.Occur.MUST);
        if (hasStore()) query.add(new TermQuery(new Term("store", storeCode)), BooleanClause.Occur.MUST);
        query.add(new TermQuery(new Term("active", "true")), BooleanClause.Occur.MUST);
        query.add(new TermQuery(new Term("archived", "false")), BooleanClause.Occur.MUST);
        String showunavailables = getStorePropertyValue(StoreProperty.PROP_PRODUCT_SHOW_UNAVAILABLE, StoreProperty.TYPE_GENERAL, StoreProperty.PROP_DEFAULT_PRODUCT_SHOW_UNAVAILABLE);
        if (StoreProperty.PROP_PRODUCT_HAS_STOCK.equalsIgnoreCase(showunavailables)) query.add(new TermQuery(new Term("stock", "true")), BooleanClause.Occur.MUST);
        else if (StoreProperty.PROP_PRODUCT_HAS_STOCK_OR_ETA.equalsIgnoreCase(showunavailables)) {
            BooleanQuery query1 = new BooleanQuery();
            query1.add(new TermQuery(new Term("stock", "true")), BooleanClause.Occur.SHOULD);
            query1.add(new TermQuery(new Term("eta", "true")), BooleanClause.Occur.SHOULD);
            query.add(query1, BooleanClause.Occur.MUST);
        }
        query.add(new TermQuery(new Term("productType", "complement")), BooleanClause.Occur.MUST_NOT);
        if (userLevel != null) {
            BooleanQuery query1 = new BooleanQuery();
            query1.add(new TermQuery(new Term("forUsers", "l" + userLevel.toString() + "l")), BooleanClause.Occur.SHOULD);
            query1.add(new TermQuery(new Term("forUsers", "public")), BooleanClause.Occur.SHOULD);
            query.add(query1, BooleanClause.Occur.MUST);
        } else query.add(new TermQuery(new Term("forUsers", "public")), BooleanClause.Occur.MUST);

        FullTextQuery hibQuery = fullTextSession.createFullTextQuery(query, Product.class);
        if (nav != null) {
            nav.setTotalRows(hibQuery.getResultSize());
            if (nav.needPagination()) {
                hibQuery.setFirstResult(nav.getFirstRow() - 1);
                hibQuery.setMaxResults(nav.getPageRows());
            }
        }
        List<Product> result = hibQuery.list();
        for (int i = 0; i < result.size(); i++) {
            Product p = result.get(i);
            p.addProperty("explain", hibQuery.explain(i));
        }
        return result;
    }

    public List<Product> getComplements(ComplementGroup group, boolean onlyActive) {
        Criteria cri = createCriteriaForStore(Product.class).add(Restrictions.eq("productType", Product.TYPE_COMPLEMENT));
        cri.add(Restrictions.eq("complementGroup", group));
        if (onlyActive) cri.add(Restrictions.eq("active", Boolean.TRUE));
        cri.addOrder(Order.asc("idProduct"));
        return cri.list();
    }

    public StaticText getStaticText(String urlCode) {
        List<StaticText> l = createCriteriaForStore(StaticText.class)
                .add(Restrictions.eq("urlCode", urlCode)).list();
        return (l != null && l.size() > 0) ? l.get(0) : null;

    }

    public StaticText getStaticText(String code, String type) {
        List<StaticText> l = createCriteriaForStore(StaticText.class)
                .add(Restrictions.eq("code", code))
                .add(Restrictions.eq("textType", type))
                .list();
        return (l != null && l.size() > 0) ? l.get(0) : null;
    }

    public StaticText getStaticTextByTitle(String title, String type) {
        List<StaticText> l = createCriteriaForStore(StaticText.class)
                .add(Restrictions.eq("textType", type))
                .createAlias("staticTextLangs", "staticTextLangs").add(Restrictions.eq("staticTextLangs.title", title))
                .list();
        return (l != null && l.size() > 0) ? l.get(0) : null;
    }

    public UserLevel getUserLevel(String code) {
        if (StringUtils.isEmpty(code)) return null;
        List l = createCriteriaForStore(UserLevel.class).add(Restrictions.eq("code", code)).list();
        return (l != null && l.size() > 0) ? (UserLevel) l.get(0) : null;
    }

    public UserAdminRole getRole(String code) {
        List l = createCriteriaForStore(UserAdminRole.class).add(Restrictions.eq("roleCode", code)).list();
        return (l != null && l.size() > 0) ? (UserAdminRole) l.get(0) : null;
    }

    public UserVisit getUserVisit(HttpServletRequest request) {
        if (request.getSession() != null) {
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_YEAR, -1);
            Criteria cri = createCriteriaForStore(UserVisit.class)
                    .add(Restrictions.eq("sessionId", request.getSession().getId()))
                    .add(Restrictions.eq("ipAddress", request.getRemoteAddr()))
                    .add(Restrictions.ge("visitDate", cal.getTime()));
            List<UserVisit> l = cri.list();
            return (l != null && l.size() > 0) ? l.get(0) : null;
        }
        return null;
    }

    public Job getJob(String jobName, boolean createIfNotExist) {
        Job job = getJob(jobName);
        if (job == null && createIfNotExist) {
            job = new Job();
            job.setName(jobName);
        }
        return job;
    }

    public Job getJob(String jobName) {
        List<org.store.core.beans.Job> l = createCriteriaForStore(org.store.core.beans.Job.class).add(Restrictions.eq("name", jobName)).list();
        return (l != null && l.size() > 0) ? l.get(0) : null;
    }

    public List<Category> getCategories(boolean onlyActive) {
        Criteria cri = createCriteriaForStore(Category.class);
        if (onlyActive) cri.add(Restrictions.eq("visible", Boolean.TRUE));
        return cri.list();
    }

    public StaticText getOtherStaticTextByUrlCode(String code, StaticText bean) {
        Criteria cri = createCriteriaForStore(StaticText.class);
        cri.add(Restrictions.eq("urlCode", code));
        if (bean != null && bean.getId() != null) cri.add(Restrictions.ne("id", bean.getId()));
        List l = cri.list();
        return (StaticText) ((l != null && l.size() > 0) ? l.get(0) : null);
    }

    public boolean updateStaticTextUrlCode(StaticText bean, String name, String defaultLanguage) {
        StaticTextLang catlang = getStaticTextLang(bean, defaultLanguage);
        if (isEmpty(name) && catlang != null) name = catlang.getTitle();
        if (isEmpty(name)) name = "no-name";
        String urlCode = SomeUtils.replaceForUrl(name);
        if (urlCode != null) while (getOtherStaticTextByUrlCode(urlCode, bean) != null) urlCode += '-';
        bean.setUrlCode(urlCode);
        save(bean);
        return !isEmpty(urlCode);
    }

    public Map<String, Date> getReportDates() {
        Map<String, Date> result = new HashMap<String, Date>();
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, cal.getMinimum(Calendar.HOUR_OF_DAY));
        cal.set(Calendar.MINUTE, cal.getMinimum(Calendar.MINUTE));
        cal.set(Calendar.SECOND, cal.getMinimum(Calendar.SECOND));
        cal.set(Calendar.MILLISECOND, cal.getMinimum(Calendar.MILLISECOND));
        result.put("today", cal.getTime());
        cal.set(Calendar.DAY_OF_MONTH, cal.getMinimum(Calendar.DAY_OF_MONTH));
        result.put("month", cal.getTime());
        cal.set(Calendar.MONTH, cal.getMinimum(Calendar.MONTH));
        result.put("year", cal.getTime());
        cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, cal.getMinimum(Calendar.HOUR_OF_DAY));
        cal.set(Calendar.MINUTE, cal.getMinimum(Calendar.MINUTE));
        cal.set(Calendar.SECOND, cal.getMinimum(Calendar.SECOND));
        cal.set(Calendar.MILLISECOND, cal.getMinimum(Calendar.MILLISECOND));
        cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
        result.put("week", cal.getTime());
        return result;
    }

    public Map<String, Long> getHits(Map<String, Date> dates, boolean logins, String[] excludedIps) {
        Map<String, Long> result = new HashMap<String, Long>();
        String baseQuery = "select count(id) from UserVisit v where v.inventaryCode=:invCode";
        if (excludedIps != null && excludedIps.length > 0) {
            StringBuilder buff = new StringBuilder();
            for (String ip : excludedIps) {
                if (!isEmpty(buff.toString())) buff.append(",");
                buff.append("'").append(ip.trim()).append("'");
            }
            baseQuery += " and ipAddress not in (" + buff.toString() + ")";
        }
        if (logins) baseQuery += " and v.user is not null";
        Query q = gethSession().createQuery(baseQuery);
        q.setString("invCode", storeCode);
        Number number = (Number) q.uniqueResult();
        if (number != null) result.put("all", number.longValue());

        q = gethSession().createQuery(baseQuery + " and v.visitDate>=:date");
        q.setString("invCode", storeCode);
        for (String key : dates.keySet()) {
            q.setDate("date", dates.get(key));
            number = (Number) q.uniqueResult();
            if (number != null) result.put(key, number.longValue());
        }
        return result;
    }

    public Map<String, Long> getRegistrations(Map<String, Date> dates, boolean notAnonymous) {
        Map<String, Long> result = new HashMap<String, Long>();
        String baseQuery = "select count(idUser) from User u where u.inventaryCode=:invCode";
        if (notAnonymous) baseQuery += " and (u.anonymousCheckout is null or u.anonymousCheckout<>1)";

        Query q = gethSession().createQuery(baseQuery);
        q.setString("invCode", storeCode);
        Number number = (Number) q.uniqueResult();
        if (number != null) result.put("all", number.longValue());

        q = gethSession().createQuery(baseQuery + " and u.registerDate>=:date");
        q.setString("invCode", storeCode);
        for (String key : dates.keySet()) {
            q.setDate("date", dates.get(key));
            number = (Number) q.uniqueResult();
            if (number != null) result.put(key, number.longValue());
        }
        return result;
    }

    public Map<String, Object> getOrderStats(Map<String, Date> dates, OrderStatus status) {
        Map<String, Object> result = new HashMap<String, Object>();
        String baseQuery = "select count(o.idOrder) as cant, sum(o.total) as tarif from Order o where o.user.inventaryCode=:invCode";
        if (status != null) baseQuery += " and (o.status.id=:orderStatus)";

        Query q = gethSession().createQuery(baseQuery);
        if (status != null) q.setLong("orderStatus", status.getId());
        q.setString("invCode", storeCode);
        Object[] objArr = (Object[]) q.uniqueResult();
        Number cant = (Number) objArr[0];
        Number tarif = (Number) objArr[1];
        if (cant != null) result.put("all_c", cant.longValue());
        if (tarif != null) result.put("all_t", tarif.doubleValue());

        q = gethSession().createQuery(baseQuery + " and o.createdDate>=:date");
        q.setString("invCode", storeCode);
        if (status != null) q.setLong("orderStatus", status.getId());
        for (String key : dates.keySet()) {
            q.setDate("date", dates.get(key));
            Object[] objArr1 = (Object[]) q.uniqueResult();
            Number cant1 = (Number) objArr1[0];
            Number tarif1 = (Number) objArr1[1];
            if (cant1 != null) result.put(key + "_c", cant1.longValue());
            if (tarif1 != null) result.put(key + "_t", tarif1.doubleValue());
        }
        return result;
    }

    public Map<String, Object> getHomeStats() {
        Map<String, Object> result = new HashMap<String, Object>();
        Criteria cri = createCriteriaForStore(Product.class).add(Restrictions.or(Restrictions.eq("archived", Boolean.FALSE), Restrictions.isNull("archived"))).setProjection(Projections.count("idProduct"));
        Number total = (Number) cri.uniqueResult();
        if (total != null && total.longValue() > 0) {
            result.put("prod_total", total.longValue());

            // activos
            cri.add(Restrictions.eq("active", Boolean.TRUE));
            Number actives = (Number) cri.uniqueResult();
            if (actives != null) {
                result.put("prod_active", actives.longValue());
                result.put("prod_inactive", total.longValue() - actives.longValue());
            } else {
                result.put("prod_active", 0l);
                result.put("prod_inactive", total.longValue());
            }

            // mas vendidos
            List lSales = createCriteriaForStore(Product.class).add(Restrictions.or(Restrictions.eq("archived", Boolean.FALSE), Restrictions.isNull("archived"))).add(Restrictions.gt("sales", 0l)).addOrder(Order.desc("sales")).setMaxResults(10).list();
            if (lSales != null && lSales.size() > 0) result.put("prod_sales", lSales);

            // mas vistos
            List lHits = createCriteriaForStore(Product.class).add(Restrictions.or(Restrictions.eq("archived", Boolean.FALSE), Restrictions.isNull("archived"))).add(Restrictions.gt("hits", 0l)).addOrder(Order.desc("hits")).setMaxResults(10).list();
            if (lHits != null && lHits.size() > 0) result.put("prod_hits", lHits);

        }
        Number manufacturers = (Number) createCriteriaForStore(Manufacturer.class).setProjection(Projections.count("idManufacturer")).uniqueResult();
        if (manufacturers != null) result.put("manufacturer", manufacturers.longValue());

        Number usersTotal = (Number) createCriteriaForStore(User.class).setProjection(Projections.count("idUser")).uniqueResult();
        if (usersTotal != null) result.put("user_total", usersTotal);

        Number users = (Number) createCriteriaForStore(User.class).add(Restrictions.or(Restrictions.isNull("admin"), Restrictions.eq("admin", Boolean.FALSE))).setProjection(Projections.count("idUser")).uniqueResult();
        if (users != null) result.put("users", users);

        cri = createCriteriaForStore(Category.class).setProjection(Projections.count("idCategory"));
        Number totalCat = (Number) cri.uniqueResult();
        if (totalCat != null && totalCat.longValue() > 0) {
            result.put("cat_total", totalCat.longValue());

            cri.add(Restrictions.eq("visible", Boolean.TRUE));
            Number activeCat = (Number) cri.uniqueResult();
            if (activeCat != null) {
                result.put("cat_active", activeCat.longValue());
                result.put("cat_inactive", totalCat.longValue() - activeCat.longValue());
            } else {
                result.put("cat_active", 0l);
                result.put("cat_inactive", totalCat.longValue());
            }
        }

        return result;
    }

    public ShippingMethod getShippingMethod(String carrier, String code) {
        List list = createCriteriaForStore(ShippingMethod.class)
                .add(Restrictions.eq("carrierName", carrier))
                .add(Restrictions.eq("methodCode", code))
                .list();
        return (list != null && list.size() > 0) ? (ShippingMethod) list.get(0) : null;
    }

    public List<org.store.core.beans.Order> getOrdersToExport(String[] statuses) {
        Criteria cri = gethSession().createCriteria(org.store.core.beans.Order.class).add(Restrictions.isNull("exportedDate"));
        cri.createAlias("status", "status", Criteria.LEFT_JOIN);
        cri.add(Restrictions.in("status.statusCode", statuses));
        cri.addOrder(Order.asc("createdDate"));
        return cri.list();
    }

    public List<User> getUsers() {
        return createCriteriaForStore(User.class).add(Restrictions.or(Restrictions.isNull("anonymousCheckout"), Restrictions.eq("anonymousCheckout", Boolean.FALSE))).list();
    }

    public List<LocationStore> getLocationStores(boolean onlyActive) {
        Criteria cri = createCriteriaForStore(LocationStore.class);
        if (onlyActive) cri.add(Restrictions.eq("active", Boolean.TRUE));
        return cri.list();
    }

    public List<CustomShippingMethod> getCustomShippingMethods() {
        return createCriteriaForStore(CustomShippingMethod.class).addOrder(Order.asc("code")).list();
    }

    public State getStateByCode(String country, String code) {
        List l = createCriteriaForStore(State.class)
                .add(Restrictions.eq("countryCode", country))
                .add(Restrictions.eq("stateCode", code))
                .list();
        return (l != null && l.size() > 0) ? (State) l.get(0) : null;
    }

    public State getStateByName(String country, String name) {
        List l = createCriteriaForStore(State.class)
                .add(Restrictions.eq("countryCode", country))
                .add(Restrictions.or(Restrictions.eq("stateCode", name), Restrictions.eq("stateName", name)))
                .list();
        return (l != null && l.size() > 0) ? (State) l.get(0) : null;
    }

    public LocalizedText getLocalizedtext(String code) {
        List l = createCriteriaForStore(LocalizedText.class)
                .add(Restrictions.eq("code", code))
                .addOrder(Order.asc("id"))
                .list();
        return (l != null && l.size() > 0) ? (LocalizedText) l.get(0) : null;
    }

    public String getLocalizedtextValue(String code, String lang) {
        LocalizedText lt = getLocalizedtext(code);
        return (lt != null && StringUtils.isNotEmpty(lt.getValue(lang))) ? lt.getValue(lang) : code;
    }

    public String isUsedState(State state) {
        StringBuilder respuesta = new StringBuilder();
        List l;
        l = gethSession().createCriteria(CustomShippingMethodRule.class).add(Restrictions.eq("state", state)).list();
        if (l != null && !l.isEmpty()) {
            respuesta.append("Used in Shipping methods: ").append(((CustomShippingMethodRule) (l.get(0))).getMethod().getName(getDefaultLanguage()));
            return respuesta.toString();
        }
        l = gethSession().createCriteria(ShippingRate.class).add(Restrictions.eq("state", state)).list();
        if (l != null && !l.isEmpty()) {
            ShippingRate rate = (ShippingRate) l.get(0);
            if (rate.getCategory() != null) respuesta.append("Used in shipping rates for Category: ").append(rate.getCategory().getCategoryName(getDefaultLanguage()));
            else if (rate.getProduct() != null) respuesta.append("Used in shipping rates for Product: ").append(rate.getProduct().getProductName(getDefaultLanguage()));
            return respuesta.toString();
        }
        l = gethSession().createCriteria(Provider.class).add(Restrictions.eq("state", state)).list();
        if (l != null && !l.isEmpty()) {
            respuesta.append("Used in supplier: ");
            for (Object o : l.subList(0, Math.min(5, l.size() - 1))) {
                respuesta.append(((Provider) o).getProviderName()).append(", ");
            }
            return respuesta.toString();
        }
        l = gethSession().createCriteria(UserAddress.class).add(Restrictions.eq("state", state)).list();
        if (l != null && !l.isEmpty()) {
            respuesta.append("Used in address of customers: ");
            for (Object o : l.subList(0, Math.min(5, l.size() - 1))) {
                respuesta.append(((UserAddress) o).getUser().getFullName()).append(", ");
            }
            return respuesta.toString();
        }
        l = gethSession().createCriteria(LocationStore.class).add(Restrictions.eq("state", state)).list();
        if (l != null && !l.isEmpty()) {
            respuesta.append("Used in address of store location: ");
            for (Object o : l.subList(0, Math.min(5, l.size() - 1))) {
                respuesta.append(((LocationStore) o).getFullAddress(false)).append(", ");
            }
            return respuesta.toString();
        }
        return null;
    }

    public String isUsedProduct(Product p) {
        StringBuilder respuesta = new StringBuilder();
        List l;
        l = gethSession().createCriteria(Banner.class).add(Restrictions.eq("forProduct", p)).list();
        if (l != null && !l.isEmpty()) {
            respuesta.append("Used in banners:");
            for (Object o : l) respuesta.append(" ").append(((Banner) o).getId());
            return respuesta.toString();
        }
        l = gethSession().createCriteria(OrderDetailProduct.class).add(Restrictions.eq("product", p)).list();
        if (l != null && !l.isEmpty()) {
            respuesta.append("Used in orders:");
            for (Object o : l)
                respuesta.append(" ").append(((OrderDetailProduct) o).getOrder().getIdOrder());
            return respuesta.toString();
        }
        l = gethSession().createCriteria(PromotionalCode.class).add(Restrictions.eq("productInCart", p)).list();
        if (l != null && !l.isEmpty()) {
            respuesta.append("Used in promotions:");
            for (Object o : l)
                respuesta.append(" ").append(((PromotionalCode) o).getCode());
            return respuesta.toString();
        }
        l = gethSession().createCriteria(PromotionalCode.class).add(Restrictions.eq("freeProduct", p)).list();
        if (l != null && !l.isEmpty()) {
            respuesta.append("Used in promotions:");
            for (Object o : l)
                respuesta.append(" ").append(((PromotionalCode) o).getCode());
            return respuesta.toString();
        }

        return null;
    }

    public void deleteProduct(Product product) {
        gethSession().createQuery("delete Banner b where b.forProduct = :product").setEntity("product", product).executeUpdate();
        gethSession().createQuery("delete ProductAuditStock b where b.product = :product").setEntity("product", product).executeUpdate();
        gethSession().createQuery("delete ProductCompetition b where b.product = :product").setEntity("product", product).executeUpdate();
        gethSession().createQuery("delete ProductDatetime b where b.product = :product").setEntity("product", product).executeUpdate();
        gethSession().createQuery("delete ProductLang b where b.product = :product").setEntity("product", product).executeUpdate();
        gethSession().createQuery("delete ProductProperty b where b.product = :product").setEntity("product", product).executeUpdate();
        gethSession().createQuery("delete ProductProvider b where b.product = :product").setEntity("product", product).executeUpdate();
        gethSession().createQuery("delete ProductRelated b where b.product = :product").setEntity("product", product).executeUpdate();
        gethSession().createQuery("delete ProductRelated b where b.related = :product").setEntity("product", product).executeUpdate();
        gethSession().createQuery("delete ProductReview b where b.product = :product").setEntity("product", product).executeUpdate();
        gethSession().createQuery("delete ProductStaticText b where b.product = :product").setEntity("product", product).executeUpdate();
        gethSession().createQuery("delete ProductUserLevel b where b.product = :product").setEntity("product", product).executeUpdate();
        gethSession().createQuery("delete ProductVariation b where b.product = :product").setEntity("product", product).executeUpdate();
        gethSession().createQuery("delete ProductVolume b where b.product = :product").setEntity("product", product).executeUpdate();
        gethSession().createQuery("delete PromotionalCode b where b.productInCart = :product").setEntity("product", product).executeUpdate();
        gethSession().createQuery("delete PromotionalCode b where b.freeProduct = :product").setEntity("product", product).executeUpdate();
        gethSession().createQuery("delete ShippingRate b where b.product = :product").setEntity("product", product).executeUpdate();
//        gethSession().createQuery("delete Product b where b.idProduct = :idproduct").setLong("idproduct", product.getIdProduct()).executeUpdate();
        gethSession().delete(product);
    }

    public String isUsedCategory(Category c, String lang) {
        StringBuilder respuesta = new StringBuilder();
        List l;
        l = gethSession().createCriteria(Banner.class).add(Restrictions.eq("forCategory", c)).list();
        if (l != null && !l.isEmpty()) {
            respuesta.append("Used in banners:");
            for (Object o : l)
                respuesta.append(" ").append(((Banner) o).getId());
            return respuesta.toString();
        }
        l = gethSession().createCriteria(Product.class).add(Restrictions.eq("category", c)).list();
        if (l != null && !l.isEmpty()) {
            respuesta.append("Used in products:");
            for (Object o : l)
                respuesta.append(" ").append(((Product) o).getPartNumber());
            return respuesta.toString();
        }
        l = gethSession().createCriteria(CategoryTree.class).add(Restrictions.eq("parent", c)).list();
        if (l != null && !l.isEmpty()) {
            respuesta.append("Parent of categories:");
            for (Object o : l)
                respuesta.append(" ").append(((CategoryTree) o).getChild().getCategoryName(lang));
            return respuesta.toString();
        }
        l = gethSession().createCriteria(PromotionalCode.class).add(Restrictions.eq("categoryInCart", c)).list();
        if (l != null && !l.isEmpty()) {
            respuesta.append("Used in promotions:");
            for (Object o : l)
                respuesta.append(" ").append(((PromotionalCode) o).getCode());
            return respuesta.toString();
        }

        return null;
    }

    public void deleteCategory(Category category) {
        gethSession().createQuery("delete Banner b where b.forCategory = :category").setEntity("category", category).executeUpdate();
        gethSession().createQuery("delete CategoryLang b where b.category = :category").setEntity("category", category).executeUpdate();
        gethSession().createQuery("delete ShippingRate b where b.category = :category").setEntity("category", category).executeUpdate();
        gethSession().createQuery("delete CategoryFee b where b.category = :category").setEntity("category", category).executeUpdate();
        gethSession().createQuery("delete CategoryStaticText b where b.category = :category").setEntity("category", category).executeUpdate();
        gethSession().createQuery("delete CategoryProperty b where b.category = :category").setEntity("category", category).executeUpdate();
        gethSession().createQuery("delete CategoryTree b where b.child = :category").setEntity("category", category).executeUpdate();
        gethSession().createQuery("delete CategoryTree b where b.parent = :category").setEntity("category", category).executeUpdate();
        gethSession().createQuery("delete CategoryUserLevel b where b.category = :category").setEntity("category", category).executeUpdate();
        gethSession().createQuery("delete CategoryView b where b.category = :category").setEntity("category", category).executeUpdate();
        gethSession().createQuery("delete CategoryVolume b where b.category = :category").setEntity("category", category).executeUpdate();
        gethSession().createQuery("delete Menu b where b.linkCategory = :category").setEntity("category", category).executeUpdate();
        gethSession().delete(category);
    }

    public String isUsedLabel(ProductLabel label) {
        if (ProductLabel.LABEL_FREE_SHIPPING.equalsIgnoreCase(label.getCode())) return "Internal Label";
        if (ProductLabel.LABEL_HOT.equalsIgnoreCase(label.getCode())) return "Internal Label";
        if (ProductLabel.LABEL_NEW.equalsIgnoreCase(label.getCode())) return "Internal Label";
        if (ProductLabel.LABEL_SPECIAL.equalsIgnoreCase(label.getCode())) return "Internal Label";
        return null;
    }

    public void deleteLabel(ProductLabel label) {
        gethSession().createQuery("delete Menu b where b.linkLabel = :label").setEntity("label", label).executeUpdate();
        gethSession().delete(label);
    }

    public String isUsedCurrency(Currency bean) {
        String defaultCurrency = getStorePropertyValue(StoreProperty.PROP_DEFAULT_CURRENCY, StoreProperty.TYPE_GENERAL, null);
        if (StringUtils.isNotEmpty(defaultCurrency) && defaultCurrency.equalsIgnoreCase(bean.getCode())) {
            return "Default currency of store";
        }
        StringBuilder respuesta = new StringBuilder();
        List l = gethSession().createCriteria(org.store.core.beans.Order.class).add(Restrictions.eq("currency", bean)).list();
        if (l != null && !l.isEmpty()) {
            respuesta.append("Used in orders:");
            for (int i = 0; i < Math.min(10, l.size()); i++)
                respuesta.append(" ").append(((org.store.core.beans.Order) l.get(i)).getIdOrder());
            respuesta.append(" ...");
            return respuesta.toString();
        }
        return null;
    }

    public String isUsedPayterm(Payterms bean) {
        StringBuilder respuesta = new StringBuilder();
        List l;
        l = gethSession().createCriteria(Provider.class).add(Restrictions.eq("payterms", bean)).list();
        if (l != null && !l.isEmpty()) {
            respuesta.append("Used in suppliers:");
            for (int i = 0; i < Math.min(10, l.size()); i++)
                respuesta.append(" ").append(((Provider) l.get(i)).getProviderName());
            respuesta.append(" ...");
            return respuesta.toString();
        }
        l = gethSession().createCriteria(User.class).add(Restrictions.eq("payterms", bean)).list();
        if (l != null && !l.isEmpty()) {
            respuesta.append("Used in users:");
            for (int i = 0; i < Math.min(10, l.size()); i++)
                respuesta.append(" ").append(((User) l.get(i)).getFullName());
            respuesta.append(" ...");
            return respuesta.toString();
        }
        return null;
    }

    public String isUsedManufacturer(Manufacturer bean) {
        StringBuilder respuesta = new StringBuilder();
        List l;
        l = gethSession().createCriteria(Product.class).add(Restrictions.eq("manufacturer", bean)).list();
        if (l != null && !l.isEmpty()) {
            respuesta.append("Used in products:");
            for (int i = 0; i < Math.min(10, l.size()); i++)
                respuesta.append(" ").append(((Product) l.get(i)).getPartNumber());
            respuesta.append(" ...");
            return respuesta.toString();
        }
        return null;
    }

    public void deleteManufacturer(Manufacturer bean) {
        gethSession().createQuery("delete Menu b where b.linkManufacturer = :bean").setEntity("bean", bean).executeUpdate();
        gethSession().createQuery("update Product b set b.manufacturer=null where b.manufacturer = :bean").setEntity("bean", bean).executeUpdate();
        gethSession().delete(bean);
    }

    public String isUsedProvider(Provider bean) {
        StringBuilder respuesta = new StringBuilder();
        List l;
        l = gethSession().createCriteria(PurchaseOrder.class).add(Restrictions.eq("provider", bean)).list();
        if (l != null && !l.isEmpty()) {
            respuesta.append("Used in purchase:");
            for (int i = 0; i < Math.min(10, l.size()); i++)
                respuesta.append(" ").append(((PurchaseOrder) l.get(i)).getId());
            respuesta.append(" ...");
            return respuesta.toString();
        }
        return null;
    }

    public void deleteProvider(Provider bean) {
        gethSession().createQuery("delete ProductProvider b where b.provider = :bean").setEntity("bean", bean).executeUpdate();
        gethSession().delete(bean);
    }

    public void deleteAttribute(AttributeProd bean) {
        gethSession().createQuery("delete CategoryProperty b where b.attribute = :bean").setEntity("bean", bean).executeUpdate();
        gethSession().createQuery("delete ProductProperty b where b.attribute = :bean").setEntity("bean", bean).executeUpdate();
        gethSession().delete(bean);
    }

    public String isUsedUserLevel(UserLevel bean) {
        if (UserLevel.DEFAULT_LEVEL.equalsIgnoreCase(bean.getCode())) return "Internal Customer Level";
        if (UserLevel.AFFILIATE_LEVEL.equalsIgnoreCase(bean.getCode())) return "Internal Customer Level";
        StringBuilder respuesta = new StringBuilder();
        List l;
        l = gethSession().createCriteria(User.class).add(Restrictions.eq("level", bean)).list();
        if (l != null && !l.isEmpty()) {
            respuesta.append("Used in customers:");
            for (int i = 0; i < Math.min(10, l.size()); i++)
                respuesta.append(" ").append(((User) l.get(i)).getFullName());
            respuesta.append(" ...");
            return respuesta.toString();
        }
        return null;
    }

    public void deleteUserLevel(UserLevel bean) {
        gethSession().createQuery("delete User b where b.level = :bean").setEntity("bean", bean).executeUpdate();
        gethSession().createQuery("delete CategoryUserLevel b where b.level = :bean").setEntity("bean", bean).executeUpdate();
        gethSession().createQuery("delete ProductUserLevel b where b.level = :bean").setEntity("bean", bean).executeUpdate();
        gethSession().createSQLQuery("DELETE FROM t_product_forusers  WHERE t_product_forusers.forUsers = :bean").setLong("bean", bean.getId()).executeUpdate();
        gethSession().delete(bean);
    }

    public void deleteFee(Fee bean) {
        gethSession().createQuery("delete CategoryFee b where b.fee = :bean").setEntity("bean", bean).executeUpdate();
        gethSession().delete(bean);
    }

    public void deleteComment(UserComment bean) {
        gethSession().delete(bean);
    }

    public String isUsedStatus(OrderStatus bean) {
        if (OrderStatus.STATUS_APPROVED.equalsIgnoreCase(bean.getStatusCode())) return "Internal Status";
        if (OrderStatus.STATUS_DEFAULT.equalsIgnoreCase(bean.getStatusCode())) return "Internal Status";
        if (OrderStatus.STATUS_REJECTED.equalsIgnoreCase(bean.getStatusCode())) return "Internal Status";
        StringBuilder respuesta = new StringBuilder();
        List l;
        l = gethSession().createCriteria(org.store.core.beans.Order.class).add(Restrictions.eq("status", bean)).list();
        if (l != null && !l.isEmpty()) {
            respuesta.append("Used in orders:");
            for (int i = 0; i < Math.min(10, l.size()); i++)
                respuesta.append(" ").append(((org.store.core.beans.Order) l.get(i)).getIdOrder());
            respuesta.append(" ...");
            return respuesta.toString();
        }
        return null;
    }

    public String isUsedRmaType(RmaType bean) {
        StringBuilder respuesta = new StringBuilder();
        List l;
        l = gethSession().createCriteria(Rma.class).add(Restrictions.eq("rmaType", bean)).list();
        if (l != null && !l.isEmpty()) {
            respuesta.append("Used in RMA orders:");
            for (int i = 0; i < Math.min(10, l.size()); i++)
                respuesta.append(" ").append(((Rma) l.get(i)).getId());
            respuesta.append(" ...");
            return respuesta.toString();
        }
        return null;
    }

    public String isUsedLocationStore(LocationStore locationStore) {
        StringBuilder respuesta = new StringBuilder();
        List l = gethSession().createCriteria(org.store.core.beans.Order.class).add(Restrictions.eq("pickInStore", locationStore)).list();
        if (l != null && !l.isEmpty()) {
            respuesta.append("Used in orders:");
            for (int i = 0; i < Math.min(10, l.size()); i++)
                respuesta.append(" ").append(((org.store.core.beans.Order) l.get(i)).getIdOrder());
            respuesta.append(" ...");
            return respuesta.toString();
        }
        return null;
    }

    public String isUsedComplementGroup(ComplementGroup bean) {
        StringBuilder respuesta = new StringBuilder();
        List l = gethSession().createCriteria(Product.class).add(Restrictions.eq("complementGroup", bean)).list();
        if (l != null && !l.isEmpty()) {
            respuesta.append("Contain products:");
            for (int i = 0; i < Math.min(10, l.size()); i++)
                respuesta.append(" ").append(((Product) l.get(i)).getPartNumber());
            respuesta.append(" ...");
            return respuesta.toString();
        }
        return null;
    }

    public String isUsedMenu(Menu bean, String lang) {
        StringBuilder respuesta = new StringBuilder();
        List l = gethSession().createCriteria(Menu.class).add(Restrictions.eq("idParent", bean.getId())).list();
        if (l != null && !l.isEmpty()) {
            respuesta.append("Contain submenus:");
            for (int i = 0; i < Math.min(10, l.size()); i++)
                respuesta.append(" ").append(((Menu) l.get(i)).getLabel(lang));
            respuesta.append(" ...");
            return respuesta.toString();
        }
        return null;
    }

    public String isUsedCustomShippingMethod(CustomShippingMethod bean) {
        List l = createCriteriaForStore(ShippingMethod.class)
                .add(Restrictions.eq("methodCode", bean.getCode()))
                .add(Restrictions.eq("carrierName", "CUSTOM"))
                .list();
        return (l != null && !l.isEmpty()) ? "Shipping method is in use" : null;
    }

    public String isUsedShippingMethod(ShippingMethod bean) {
        StringBuilder respuesta = new StringBuilder();
        List l;
        l = gethSession().createCriteria(org.store.core.beans.Order.class).add(Restrictions.eq("shippingMethod", bean)).list();
        if (l != null && !l.isEmpty()) {
            respuesta.append("Used in orders:");
            for (int i = 0; i < Math.min(10, l.size()); i++)
                respuesta.append(" ").append(((org.store.core.beans.Order) l.get(i)).getIdOrder());
            respuesta.append(" ...");
            return respuesta.toString();
        }
        l = gethSession().createCriteria(OrderPacking.class).add(Restrictions.eq("shippingMethod", bean)).list();
        if (l != null && !l.isEmpty()) {
            respuesta.append("Used in orders packages:");
            for (int i = 0; i < Math.min(10, l.size()); i++)
                respuesta.append(" ").append(((OrderPacking) l.get(i)).getId());
            respuesta.append(" ...");
            return respuesta.toString();
        }
        return null;
    }

    public String isUsedCustomer(User bean) {
        StringBuilder respuesta = new StringBuilder();
        List l;
        int i;
        l = gethSession().createCriteria(org.store.core.beans.Order.class).add(Restrictions.eq("user", bean)).list();
        if (l != null && !l.isEmpty()) {
            respuesta.append("Owner of orders:");
            for (i = 0; i < Math.min(10, l.size()); i++) respuesta.append(" ").append(((org.store.core.beans.Order) l.get(i)).getIdOrder());
            if (i < l.size() - 1) respuesta.append(" ...");
            return respuesta.toString();
        }
        l = gethSession().createCriteria(org.store.core.beans.ShopCart.class).add(Restrictions.eq("user", bean)).list();
        if (l != null && !l.isEmpty()) {
            respuesta.append("Owner of shopping carts:");
            for (i = 0; i < Math.min(10, l.size()); i++) respuesta.append(" ").append(((org.store.core.beans.ShopCart) l.get(i)).getId());
            if (i < l.size() - 1) respuesta.append(" ...");
            return respuesta.toString();
        }
        l = gethSession().createCriteria(org.store.core.beans.Order.class).add(Restrictions.eq("affiliate", bean)).list();
        if (l != null && !l.isEmpty()) {
            respuesta.append("Affiliate of orders:");
            for (i = 0; i < Math.min(10, l.size()); i++) respuesta.append(" ").append(((org.store.core.beans.Order) l.get(i)).getIdOrder());
            if (i < l.size() - 1) respuesta.append(" ...");
            return respuesta.toString();
        }
        return null;
    }

    public List<Currency> getCurrencies() {
        return getCurrencies(false);
    }

    public List<Currency> getCurrencies(boolean onlyActive) {
        Criteria cri = createCriteriaForStore(Currency.class).addOrder(Order.asc("code"));
        if (onlyActive) cri.add(Restrictions.eq("active", Boolean.TRUE));
        return cri.list();
    }

    public List<ComplementGroup> getComplementGroups() {
        return createCriteriaForStore(ComplementGroup.class).addOrder(Order.asc("idGroup")).list();
    }

    public List<UserLevel> getUserLevels(boolean all) {
        Criteria cri = createCriteriaForStore(UserLevel.class).addOrder(Order.asc("levelOrder"));
        if (!all) cri.add(Restrictions.ne("code", UserLevel.ANONYMOUS_LEVEL));
        return cri.list();
    }

    public List<Provider> getProviders() {
        return createCriteriaForStore(Provider.class).addOrder(Order.asc("providerName")).list();
    }

    public List<Payterms> getPayterms() {
        return createCriteriaForStore(Payterms.class).addOrder(Order.asc("description")).list();
    }

    public List<Manufacturer> getManufacturers() {
        return createCriteriaForStore(Manufacturer.class).addOrder(Order.asc("manufacturerName")).list();
    }

    public List<ProductLabel> getProductLabels() {
        return createCriteriaForStore(ProductLabel.class).addOrder(Order.asc("code")).list();
    }

    public List<AttributeProd> getAttributeProds() {
        return createCriteriaForStore(AttributeProd.class).addOrder(Order.asc("attributeGroup")).addOrder(Order.asc("id")).list();
    }

    public List<Fee> getFees() {
        return createCriteriaForStore(Fee.class).addOrder(Order.asc("feeName")).list();
    }

    public List<Tax> getTaxes() {
        return createCriteriaForStore(Tax.class).addOrder(Order.asc("taxName")).list();
    }

    public List<OrderStatus> getOrderStatuses() {
        return createCriteriaForStore(OrderStatus.class).addOrder(Order.asc("statusCode")).list();
    }

    public List<RmaType> getRmaTypes() {
        return createCriteriaForStore(RmaType.class).addOrder(Order.asc("id")).list();
    }

    public List<LocalizedText> getLocalizedTexts() {
        return createCriteriaForStore(LocalizedText.class).addOrder(Order.asc("code")).list();
    }

    public List<LocalizedText> getLocalizedTexts(DataNavigator nav, String filterText) {
        Criteria cri = createCriteriaForStore(LocalizedText.class).addOrder(Order.asc("code"));
        if (StringUtils.isNotEmpty(filterText)) {
            cri.add(Restrictions.or(
                    Restrictions.like("code", filterText, MatchMode.ANYWHERE),
                    Restrictions.like("value", filterText, MatchMode.ANYWHERE)
            ));
        }
        if (nav != null) {
            cri.setProjection(Projections.countDistinct("id"));
            Number total = (Number) cri.uniqueResult();
            nav.setTotalRows(total != null ? total.intValue() : 0);
            cri.setProjection(null);
            cri.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
            cri.addOrder(Order.asc("code"));
            if (nav.getTotalRows() > 0) {
                cri.setMaxResults(nav.getPageRows());
                cri.setFirstResult(nav.getFirstRow() - 1);
            }
        }
        return cri.list();
    }

    public List<Manufacturer> getManufacturers(DataNavigator nav, String filterName) {
        Criteria cri = createCriteriaForStore(Manufacturer.class);
        if (StringUtils.isNotEmpty(filterName)) {
            String[] arr = filterName.split(" ");
            for (String t : arr) {
                cri.add(Restrictions.like("manufacturerName", t, MatchMode.ANYWHERE));
            }
        }
        if (nav != null) {
            cri.setProjection(Projections.countDistinct("idManufacturer"));
            Number total = (Number) cri.uniqueResult();
            nav.setTotalRows(total != null ? total.intValue() : 0);
            cri.setProjection(null);
            cri.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
            cri.addOrder(Order.asc("manufacturerName"));
            cri.addOrder(Order.asc("idManufacturer"));
            if (nav.getTotalRows() > 0) {
                cri.setMaxResults(nav.getPageRows());
                cri.setFirstResult(nav.getFirstRow() - 1);
            }
        }
        return cri.list();
    }

    public List getHotManufacturers(Integer maxRows) {
        Criteria cri = createCriteriaForStore(Product.class);
        cri.add(Restrictions.eq("active", Boolean.TRUE));
        cri.add(Restrictions.isNotNull("manufacturer"));
        ProjectionList list = Projections.projectionList();
        list.add(Projections.count("idProduct"), "cantidad");
        list.add(Projections.groupProperty("manufacturer"));
        cri.setProjection(list);
        cri.addOrder(Order.desc("cantidad"));
        if (maxRows != null) cri.setMaxResults(maxRows);
        return cri.list();
    }

    public List<Provider> getProviders(DataNavigator nav) {
        Criteria cri = createCriteriaForStore(Provider.class);
        if (nav != null) {
            cri.setProjection(Projections.countDistinct("idProvider"));
            Number total = (Number) cri.uniqueResult();
            nav.setTotalRows(total != null ? total.intValue() : 0);
            cri.setProjection(null);
            cri.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
            cri.addOrder(Order.asc("providerName"));
            cri.addOrder(Order.asc("idProvider"));
            if (nav.getTotalRows() > 0) {
                cri.setMaxResults(nav.getPageRows());
                cri.setFirstResult(nav.getFirstRow() - 1);
            }
        }
        return cri.list();
    }

    public List<PromotionalCode> getPromotionalCodes() {
        return createCriteriaForStore(PromotionalCode.class).addOrder(Order.asc("code")).list();
    }

    public List<Help> getHelpByPage(String page) {
        return gethSession().createCriteria(Help.class).add(Restrictions.eq("page", page)).addOrder(Order.asc("code")).list();
    }

    public List<Product> getProducts() {
        return createCriteriaForStore(Product.class).addOrder(Order.asc("idProduct")).list();
    }

    public List<UserAdminRole> getUserAdminRoles() {
        return createCriteriaForStore(UserAdminRole.class).addOrder(Order.asc("roleCode")).list();
    }

    public ProductLabel getProductLabelByCode(String code) {
        return getProductLabelByCode(code, false);
    }

    public ProductLabel getProductLabelByCode(String code, boolean create) {
        List l = createCriteriaForStore(ProductLabel.class)
                .add(Restrictions.eq("code", code))
                .list();
        ProductLabel pl = (l != null && l.size() > 0) ? (ProductLabel) l.get(0) : null;
        if (pl == null && create) {
            pl = new ProductLabel();
            pl.setCode(code);
            for (String lang : getLanguages()) pl.setName(lang, code);
            save(pl);
        }
        return pl;
    }

    public ProductLabel getProductLabel(Long id) {
        return (id != null) ? (ProductLabel) gethSession().get(ProductLabel.class, id) : null;
    }

    public List<Insurance> getInsurances() {
        return createCriteriaForStore(Insurance.class).addOrder(Order.asc("id")).list();
    }

    public void deleteStaticText(StaticText staticText) {
        gethSession().createQuery("delete CategoryStaticText b where b.staticText = :staticText").setEntity("staticText", staticText).executeUpdate();
        gethSession().createQuery("delete ProductStaticText b where b.staticText = :staticText").setEntity("staticText", staticText).executeUpdate();
        gethSession().createQuery("delete Menu b where b.menuContent = :staticText").setEntity("staticText", staticText).executeUpdate();
        gethSession().createQuery("delete Menu b where b.linkStaticText = :staticText").setEntity("staticText", staticText).executeUpdate();
        gethSession().delete(staticText);
    }

    public void deleteUser(User user) {
        gethSession().createQuery("delete UserNote b where b.user = :user").setEntity("user", user).executeUpdate();
        gethSession().createQuery("delete UserVisit b where b.user = :user").setEntity("user", user).executeUpdate();
        gethSession().createQuery("delete PromotionalCode b where b.user = :user").setEntity("user", user).executeUpdate();
        gethSession().createQuery("update ProductAuditStock b set b.user=null where b.user = :user").setEntity("user", user).executeUpdate();
        gethSession().delete(user);
    }

    public void deleteResources(Resource res) {
        gethSession().createSQLQuery("DELETE FROM t_product_t_resources WHERE productResources_id = :id").setLong("id", res.getId()).executeUpdate();
        gethSession().delete(res);
    }

    public Provider getProviderByName(String supplierName) {
        List l = createCriteriaForStore(Provider.class).add(Restrictions.eq("providerName", supplierName)).list();
        return (l != null && l.size() > 0) ? (Provider) l.get(0) : null;
    }

    public Provider getProviderByAltName(String name) {
        List l = createCriteriaForStore(Provider.class).add(Restrictions.eq("alternateNo", name)).list();
        return (l != null && l.size() > 0) ? (Provider) l.get(0) : null;
    }

    public Provider getProviderByUsername(String name) {
        List l = createCriteriaForStore(Provider.class).add(Restrictions.eq("username", name)).list();
        return (l != null && l.size() > 0) ? (Provider) l.get(0) : null;
    }

    public List<String> getRmaStatus(boolean configureDefault) {
        List<String> statuses = new ArrayList<String>();
        StoreProperty bean = getStoreProperty(StoreProperty.PROP_RMA_STATUS, StoreProperty.TYPE_GENERAL);
        if (bean == null) {
            bean = new StoreProperty();
            bean.setCode(StoreProperty.PROP_RMA_STATUS);
            bean.setType(StoreProperty.TYPE_GENERAL);
            save(bean);
        }
        String[] arrTmp = (StringUtils.isNotEmpty(bean.getValue())) ? bean.getValue().split(",") : null;
        if (arrTmp != null && arrTmp.length > 0) statuses.addAll(Arrays.asList(arrTmp));
        if (configureDefault) {
            boolean modified = false;
            for (String statusCode : new String[]{Rma.STATUS_ACCEPTED, Rma.STATUS_REJECTED, Rma.STATUS_REQUESTED, Rma.STATUS_CLOSED})
                if (!statuses.contains(statusCode)) {
                    statuses.add(statusCode);
                    modified = true;
                }
            if (modified) {
                bean.setValue(SomeUtils.join(statuses, ","));
                save(bean);
            }
        }
        return statuses;
    }

    public boolean productShowInCategory(Product p, Category c) {
        if (p != null && c != null) {
            Long[] ids = getIdCategoryList(c, false);
            if (ids != null && ids.length > 0 && p.getProductCategories() != null && !p.getProductCategories().isEmpty()) {
                for (Category pc : p.getProductCategories()) {
                    if (ArrayUtils.contains(ids, pc.getIdCategory())) return true;
                }

            }
        }
        return false;
    }

    private Criteria addLikeCondition(Criteria cri, String field, String value) {
        if (StringUtils.isNotEmpty(value)) {
            MatchMode mm = MatchMode.ANYWHERE;
            if (value.startsWith("'")) {
                if (value.endsWith("'")) {
                    value = value.substring(1, value.length() - 1);
                    mm = MatchMode.EXACT;
                } else {
                    value = value.substring(1);
                    mm = MatchMode.START;
                }
            } else if (value.endsWith("'")) {
                value = value.substring(0, value.length() - 1);
                mm = MatchMode.END;
            }
            return cri.add(Restrictions.like(field, value, mm));
        }
        return cri;
    }

    public List getMails(DataNavigator nav, String filterStatus, String filterSubject, String filterEmail, String filterReference, Date dateFrom, Date dateTo) {
        Criteria cri = createCriteriaForStore(Mail.class);
        addLikeCondition(cri, "subject", filterSubject);
        addLikeCondition(cri, "toAddress", filterEmail);
        addLikeCondition(cri, "reference", filterReference);
        if (dateFrom != null) cri.add(Restrictions.ge("sentDate", SomeUtils.dateIni(dateFrom)));
        if (dateTo != null) cri.add(Restrictions.le("sentDate", SomeUtils.dateEnd(dateTo)));
        if (StringUtils.isNotEmpty(filterStatus)) {
            if (Mail.STATUS_FATAL.equals(filterStatus)) cri.add(Restrictions.eq("status", -1));
            if (Mail.STATUS_PENDING.equals(filterStatus)) cri.add(Restrictions.eq("status", 0));
            if (Mail.STATUS_SENT.equals(filterStatus)) cri.add(Restrictions.eq("status", 10));
            if (Mail.STATUS_ERROR.equals(filterStatus)) cri.add(Restrictions.ge("status", 1)).add(Restrictions.le("status", 9));
        }
        if (nav != null) {
            cri.setProjection(Projections.countDistinct("idMail"));
            Number total = (Number) cri.uniqueResult();
            nav.setTotalRows(total != null ? total.intValue() : 0);
            cri.setProjection(null);
            cri.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
            if (nav.getTotalRows() > 0) {
                cri.setMaxResults(nav.getPageRows());
                cri.setFirstResult(nav.getFirstRow() - 1);
            }
        }
        cri.addOrder(Order.desc("idMail"));
        return cri.list();
    }

    public List<Insurance> getInsurancesFor(double total) {
        return createCriteriaForStore(Insurance.class)
                .add(Restrictions.or(Restrictions.isNull("minTotal"), Restrictions.lt("minTotal", total)))
                .add(Restrictions.or(Restrictions.isNull("maxTotal"), Restrictions.ge("maxTotal", total)))
                .add(Restrictions.eq("active", Boolean.TRUE))
                .add(Restrictions.isNotNull("insuranceValue"))
                .list();
    }

    public List<ProductReview> getPendingReviews() {
        Criteria cri = gethSession().createCriteria(ProductReview.class);
        cri.add(Restrictions.isNull("visible"));
        return cri.list();
    }

    public List<ProductReview> getReviews(DataNavigator nav, String status, Date dateFrom, Date dateTo, String code) {
        Criteria cri = gethSession().createCriteria(ProductReview.class);
        if ("visible".equalsIgnoreCase(status)) cri.add(Restrictions.isNotNull("visible")).add(Restrictions.eq("visible", Boolean.TRUE));
        else if ("pending".equalsIgnoreCase(status)) cri.add(Restrictions.or(Restrictions.isNull("visible"), Restrictions.eq("visible", Boolean.FALSE)));
        if (dateFrom != null) cri.add(Restrictions.ge("created", SomeUtils.dateIni(dateFrom)));
        if (dateTo != null) cri.add(Restrictions.le("created", SomeUtils.dateEnd(dateTo)));
        if (StringUtils.isNotEmpty(code)) {
            cri.createAlias("product", "product");
            cri.add(Restrictions.like("product.partNumber", code, MatchMode.ANYWHERE));
        }
        if (nav != null) {
            cri.setProjection(Projections.countDistinct("idReview"));
            Number total = (Number) cri.uniqueResult();
            nav.setTotalRows(total != null ? total.intValue() : 0);
            if (nav.getTotalRows() > 0) {
                cri.setMaxResults(nav.getPageRows());
                cri.setFirstResult(nav.getFirstRow() - 1);
            }
        }
        cri.addOrder(Order.desc("idReview"));
        cri.setProjection(Projections.distinct(Projections.property("idReview")));
        cri.setResultTransformer(new IdToBeanTransformer(this, ProductReview.class));
        return cri.list();
    }

    // todo: probar en sql server
    public int applyLabelToSupplier(ProductLabel label, Provider provider) {
        if (label == null || provider == null) return 0;
        Dialect dialect = getCurrentDialect();
        if (dialect instanceof SQLServerDialect) {
            return 0;
        } else if (dialect instanceof MySQLDialect) {
            SQLQuery q = gethSession().createSQLQuery("INSERT INTO t_product_t_productlabel (t_product_idProduct,labels_id) " +
                    "SELECT t_product_provider.product_idProduct, :label FROM t_product_provider LEFT JOIN t_product_t_productlabel " +
                    "ON t_product_provider.product_idProduct=t_product_t_productlabel.t_product_idProduct AND t_product_t_productlabel.labels_id=:label " +
                    "WHERE t_product_provider.provider_idProvider=:supplier AND t_product_t_productlabel.labels_id IS null");

            q.setLong("label", label.getId());
            q.setLong("supplier", provider.getIdProvider());
            return q.executeUpdate();
        } else {
            return 0;
        }
    }

    // todo: probar en sql server
    public int applyLabelToManufacturer(ProductLabel label, Manufacturer manufacturer) {
        if (label == null || manufacturer == null) return 0;
        Dialect dialect = getCurrentDialect();
        if (dialect instanceof SQLServerDialect) {
            return 0;
        } else if (dialect instanceof MySQLDialect) {
            SQLQuery q = gethSession().createSQLQuery("INSERT INTO t_product_t_productlabel (t_product_idProduct,labels_id) " +
                    "SELECT t_product.idProduct, :label FROM t_product LEFT JOIN t_product_t_productlabel " +
                    "ON t_product.idProduct=t_product_t_productlabel.t_product_idProduct AND t_product_t_productlabel.labels_id=:label " +
                    "WHERE t_product.manufacturer_idManufacturer=:manufacturer AND t_product_t_productlabel.labels_id IS null");

            q.setLong("label", label.getId());
            q.setLong("manufacturer", manufacturer.getIdManufacturer());
            return q.executeUpdate();
        } else {
            return 0;
        }
    }

    public VMTemplate getVMTemplateForStore(String code, String store) {
        Criteria cri = gethSession().createCriteria(VMTemplate.class)
                .add(Restrictions.eq("code", code));
        if (StringUtils.isNotEmpty(store)) cri.add(Restrictions.eq("inventaryCode", store));
        else cri.add(Restrictions.isNull("inventaryCode"));
        List l = cri.list();
        return (l != null && !l.isEmpty()) ? (VMTemplate) l.get(0) : null;
    }

    public Product getSampleProduct() {
        List<Product> l = getSampleProducts(1);
        return (l != null && !l.isEmpty()) ? l.get(0) : null;
    }

    public org.store.core.beans.Order getSampleOrder() {
        List l = gethSession().createCriteria(org.store.core.beans.Order.class)
                .createAlias("user", "user", Criteria.LEFT_JOIN)
                .add(Restrictions.eq("user.inventaryCode", storeCode))
                .addOrder(RandomOrder.random())
                .setMaxResults(1).list();
        return (l != null && !l.isEmpty()) ? (org.store.core.beans.Order) l.get(0) : null;
    }

    public List<Product> getSampleProducts(int cant) {
        return createCriteriaForStore(Product.class).setMaxResults(cant).list();
    }

    public List<User> getUsersForNewsletter() {
        Criteria cri = createCriteriaForStore(User.class);
        cri.createCriteria("preferences")
                .add(Restrictions.eq("preferenceCode", "register.subscriptions"))
                .add(Restrictions.eq("preferenceValue", "yes"));
        cri.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        return cri.list();
    }


    public List getUserCartAlerts(User u) {
        return createCriteriaForStore(ShopCart.class)
                .add(Restrictions.eq("user", u))
                .add(Restrictions.in("status", new String[]{ShopCart.STATUS_APPROVED, ShopCart.STATUS_PENDING, ShopCart.STATUS_SAVED}))
                .add(Restrictions.isNotEmpty("items"))
                .add(Restrictions.or(Restrictions.isNull("validUntil"), Restrictions.ge("validUntil", SomeUtils.dateEnd(SomeUtils.today()))))
                .setProjection(Projections.projectionList().add(Projections.groupProperty("status")).add(Projections.countDistinct("id")))
                .list();
    }

    public List<ShopCart> getSavedCarts(User u) {
        return createCriteriaForStore(ShopCart.class)
                .add(Restrictions.eq("user", u))
                .add(Restrictions.eq("status", ShopCart.STATUS_SAVED))
                .add(Restrictions.isNotEmpty("items"))
                .addOrder(Order.desc("id"))
                .setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY)
                .list();
    }

    public List<ShopCart> getQuotedCarts(User u) {
        return createCriteriaForStore(ShopCart.class)
                .add(Restrictions.eq("user", u))
                .add(Restrictions.or(Restrictions.eq("status", ShopCart.STATUS_APPROVED), Restrictions.eq("status", ShopCart.STATUS_PENDING)))
                .add(Restrictions.or(Restrictions.isNull("validUntil"), Restrictions.ge("validUntil", SomeUtils.dateEnd(SomeUtils.today()))))
                .addOrder(Order.desc("id"))
                .setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY)
                .list();
    }

    public List<ShopCart> getApprovedCarts(User u) {
        return createCriteriaForStore(ShopCart.class)
                .add(Restrictions.eq("user", u))
                .add(Restrictions.eq("status", ShopCart.STATUS_APPROVED))
                .add(Restrictions.or(Restrictions.isNull("validUntil"), Restrictions.ge("validUntil", SomeUtils.dateEnd(SomeUtils.today()))))
                .addOrder(Order.desc("id"))
                .setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY)
                .list();
    }

    public ShopCart getLastSaveCart(User u) {
        List l = createCriteriaForStore(ShopCart.class)
                .add(Restrictions.eq("user", u))
                .add(Restrictions.eq("status", ShopCart.STATUS_SAVED))
                .add(Restrictions.isNotEmpty("items"))
                .addOrder(Order.desc("id"))
                .setMaxResults(1)
                .list();
        return (l != null && !l.isEmpty()) ? (ShopCart) l.get(0) : null;
    }


    public List getShopCarts(DataNavigator nav, String status, Date dateFrom, Date dateTo) {
        Criteria cri = createCriteriaForStore(ShopCart.class);
        cri.add(Restrictions.isNotEmpty("items"));
        if (StringUtils.isNotEmpty(status)) cri.add(Restrictions.eq("status", status));
        if (dateFrom != null) cri.add(Restrictions.ge("createdDate", SomeUtils.dateIni(dateFrom)));
        if (dateTo != null) cri.add(Restrictions.le("createdDate", SomeUtils.dateEnd(dateTo)));
        if (nav != null) {
            cri.setProjection(Projections.countDistinct("id"));
            Number total = (Number) cri.uniqueResult();
            nav.setTotalRows(total != null ? total.intValue() : 0);
            if (nav.getTotalRows() > 0) {
                cri.setMaxResults(nav.getPageRows());
                cri.setFirstResult(nav.getFirstRow() - 1);
            }
        }
        cri.addOrder(Order.desc("id"));
        cri.setProjection(Projections.distinct(Projections.property("id")));
        cri.setResultTransformer(new IdToBeanTransformer(this, ShopCart.class));
        return cri.list();
    }

    public UserAddress getAddressByExternalCode(User user, String extCode) {
        List l = gethSession().createCriteria(UserAddress.class)
                .add(Restrictions.eq("user", user))
                .add(Restrictions.eq("externalCode", extCode))
                .list();
        return (l != null && !l.isEmpty()) ? (UserAddress) l.get(0) : null;
    }

    public TaxPerFamily getTaxPerFamilyByExtCode(String code) {
        List list = createCriteriaForStore(TaxPerFamily.class).add(Restrictions.eq("externalCode", code)).list();
        return (list != null && !list.isEmpty()) ? (TaxPerFamily) list.get(0) : null;
    }

    public ProductUserTax getProductUserTaxes(String familyProduct, String categoryUser) {
        Criteria cri = createCriteriaForStore(ProductUserTax.class);
        if (StringUtils.isNotEmpty(familyProduct)) cri.add(Restrictions.eq("familyProduct", familyProduct));
        if (StringUtils.isNotEmpty(categoryUser)) cri.add(Restrictions.eq("categoryUser", categoryUser));
        List lista = cri.list();
        return (lista != null && !lista.isEmpty()) ? (ProductUserTax) lista.get(0) : null;
    }

    public List<ShopCart> getShopCartsPreparing(Long idUser) {
        return createCriteriaForStore(ShopCart.class).add(Restrictions.eq("adminUser", idUser)).add(Restrictions.eq("status", ShopCart.STATUS_PREPARING)).addOrder(Order.desc("id")).setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY).list();
    }

    public List listProductsId(Long idCategory, Long idSupplier, Long idManufacturer) {
        String query = "select distinct t_product.idProduct from t_product left join t_product_provider on t_product.idProduct=t_product_provider.product_idProduct" +
                " where t_product.inventaryCode='" + getStoreCode() + "'";
        if (idManufacturer != null) query += " and t_product.manufacturer_idManufacturer=" + idManufacturer;
        if (idCategory != null) query += " and t_product.category_idCategory=" + idCategory;
        if (idSupplier != null) query += " and t_product_provider.provider_idProvider=" + idSupplier;
        query += " order by t_product.idProduct";
        return gethSession().createSQLQuery(query).addScalar("idProduct").list();
    }

    public List<OrderPayment> getOrderPayments(org.store.core.beans.Order order) {
        return (order != null) ? gethSession().createCriteria(OrderPayment.class).add(Restrictions.eq("order", order)).addOrder(Order.asc("expectedDate")).list() : null;
    }

    public List getPayments(DataNavigator nav, Map filters, String lang) {
        Criteria cri = gethSession().createCriteria(OrderPayment.class);
        Criteria criOrder = cri.createCriteria("order");
        criOrder.createCriteria("user").add(Restrictions.eq("inventaryCode", getStoreCode()));

        if (filters != null) {
            Long idSales = filters.containsKey("sales") ? SomeUtils.strToLong((String) filters.get("sales")) : null;
            if (idSales != null) criOrder.add(Restrictions.eq("idAdminUser", idSales));

            String status = filters.containsKey("status") ? (String) filters.get("status") : null;
            if (StringUtils.isNotEmpty(status)) cri.add(Restrictions.eq("status", status));

            Date dateFrom = filters.containsKey("dateIni") ? SomeUtils.strToDate((String) filters.get("dateIni"), lang) : null;
            if (dateFrom != null) cri.add(Restrictions.ge("expectedDate", SomeUtils.dateIni(dateFrom)));

            Date dateTo = filters.containsKey("dateEnd") ? SomeUtils.strToDate((String) filters.get("dateEnd"), lang) : null;
            if (dateTo != null) cri.add(Restrictions.le("expectedDate", SomeUtils.dateEnd(dateTo)));
        }

        if (nav != null) {
            cri.setProjection(Projections.countDistinct("id"));
            nav.setTotalRows(((Number) cri.uniqueResult()).intValue());
        }

        cri.setProjection(Projections.distinct(Projections.property("id")));
        if (nav != null && nav.needPagination()) {
            cri.setMaxResults(nav.getPageRows());
            cri.setFirstResult(nav.getFirstRow() - 1);
        }
        cri.setResultTransformer(new IdToBeanTransformer(this, OrderPayment.class));

        return cri.list();
    }

    public String getCountriesCanBuyCategory(Category category) {
        if (category != null) {
            while (category != null) {
                if (StringUtils.isNotEmpty(category.getCountriesCanBuy())) return category.getCountriesCanBuy();
                category = getCategory(category.getIdParent());
            }
        }
        return null;
    }

    public List<InquiryQuestion> getInquiresForRegister() {
        return createCriteriaForStore(InquiryQuestion.class)
                .add(Restrictions.isNotNull("useInRegister"))
                .add(Restrictions.gt("useInRegister", 0))
                .add(Restrictions.isNotEmpty("answers"))
                .addOrder(Order.asc("useInRegister"))
                .list();
    }

    public List getQuestions(DataNavigator nav) {
        Criteria cri = createCriteriaForStore(InquiryQuestion.class);

        if (nav != null) {
            cri.setProjection(Projections.countDistinct("id"));
            nav.setTotalRows(cri.uniqueResult() != null ? ((Number) cri.uniqueResult()).intValue() : 0);
        }

        cri.setProjection(Projections.distinct(Projections.property("id")));
        if (nav != null && nav.needPagination()) {
            cri.setMaxResults(nav.getPageRows());
            cri.setFirstResult(nav.getFirstRow() - 1);
        }
        cri.setResultTransformer(new IdToBeanTransformer(this, InquiryQuestion.class));

        return cri.list();
    }

    public InquiryAnswerUser getUserQuestionAnswer(User user, InquiryQuestion question) {
        List l = gethSession().createCriteria(InquiryAnswerUser.class)
                .add(Restrictions.eq("user", user))
                .createAlias("answer", "answer")
                .add(Restrictions.eq("answer.question", question))
                .list();
        return (l != null && !l.isEmpty()) ? (InquiryAnswerUser) l.get(0) : null;
    }

    public List getComments(DataNavigator nav, String type, String status, String name, String text, String sort) {
        Criteria cri = createCriteriaForStore(UserComment.class);

        if (!isEmpty(type))
            cri.add(Restrictions.eq("commentType", type));

        if (!isEmpty(status))
            cri.add(Restrictions.eq("commentStatus", status));

        if (!isEmpty(name))
            cri.add(Restrictions.or(
                    Restrictions.eq("userName", name),
                    Restrictions.eq("userEmail", name)
            ));

        if (!isEmpty(text))
            cri.add(Restrictions.or(
                    Restrictions.eq("title", text),
                    Restrictions.eq("comment", text)
            ));

        if (nav != null) {
            cri.setProjection(Projections.countDistinct("id"));
            nav.setTotalRows(cri.uniqueResult() != null ? ((Number) cri.uniqueResult()).intValue() : 0);
        }

        cri.setProjection(Projections.distinct(Projections.property("id")));
        if (nav != null && nav.needPagination()) {
            cri.setMaxResults(nav.getPageRows());
            cri.setFirstResult(nav.getFirstRow() - 1);
        }

        if (!isEmpty(sort)) {
            String[] arrSort = sort.split(":");
            if (arrSort.length > 1) {
                if ("asc".equalsIgnoreCase(arrSort[0])) cri.addOrder(Order.asc(arrSort[1]));
                else cri.addOrder(Order.desc(arrSort[1]));
            } else {
                cri.addOrder(Order.asc(sort));
            }
        } else {
            cri.addOrder(Order.desc("idComment"));
        }

        cri.setResultTransformer(new IdToBeanTransformer(this, UserComment.class));

        return cri.list();
    }

    public Set<String> getCommentTypes() {
        Set<String> result = new HashSet<String>();
        List l = createCriteriaForStore(UserComment.class).setProjection(Projections.distinct(Projections.property("commentType"))).list();
        for (Object s : l) result.add((String) s);
        return result;
    }

    public String[] getLanguages() {
        StoreProperty bean = getStoreProperty(StoreProperty.PROP_LANGUAGES, StoreProperty.TYPE_GENERAL);
        if (bean != null && !StringUtils.isEmpty(bean.getValue())) {
            return bean.getValue().split(",");
        }
        return null;
    }

    public String getDefaultLanguage() {
        StoreProperty bean = getStoreProperty(StoreProperty.PROP_DEFAULT_LANGUAGE, StoreProperty.TYPE_GENERAL);
        return (bean != null && !StringUtils.isEmpty(bean.getValue())) ? bean.getValue() : "en";
    }

    public Currency getDefaultCurrency() {
        StoreProperty bean = getStoreProperty(StoreProperty.PROP_DEFAULT_CURRENCY, StoreProperty.TYPE_GENERAL);
        String defCurr = (bean != null && StringUtils.isNotEmpty(bean.getValue())) ? bean.getValue() : "USD";
        Currency curr = getCurrency(defCurr);
        if (curr == null) {
            curr = new Currency();
            curr.setCode(defCurr);
            curr.setRatio(1.0);
            curr.setInventaryCode(getStoreCode());
            save(curr);
            flushSession();
        }
        return curr;
    }

    public String getDefaultCountry() {
        StoreProperty bean = getStoreProperty(StoreProperty.PROP_DEFAULT_COUNTRY, StoreProperty.TYPE_GENERAL);
        if (bean != null && !StringUtils.isEmpty(bean.getValue())) {
            return bean.getValue();
        }
        return "";
    }

    public Long getLastInvoice() {
        Criteria cri = gethSession().createCriteria(org.store.core.beans.Order.class)
                .add(Restrictions.isNotNull("invoiceConsecutive"))
                .createAlias("user", "user", Criteria.LEFT_JOIN)
                .add(Restrictions.eq("user.inventaryCode", storeCode))
                .setProjection(Projections.max("invoiceConsecutive"));
        Number value = (Number) cri.uniqueResult();
        return (value != null) ? value.longValue() : 0l;
    }

    public List<Product> productQuickSearch(String term) {
        Criteria cri = createCriteriaForStore(Product.class)
                .add(Restrictions.eq("active", Boolean.TRUE))
                .add(Restrictions.or(Restrictions.isNull("archived"), Restrictions.eq("archived", Boolean.FALSE)))
                .add(Restrictions.or(Restrictions.isNull("productType"), Restrictions.ne("productType", Product.TYPE_COMPLEMENT)))
                .add(Restrictions.isEmpty("forUsers"));

        String showunavailables = getStorePropertyValue(StoreProperty.PROP_PRODUCT_SHOW_UNAVAILABLE, StoreProperty.TYPE_GENERAL, StoreProperty.PROP_DEFAULT_PRODUCT_SHOW_UNAVAILABLE);
        if (StoreProperty.PROP_PRODUCT_HAS_STOCK.equalsIgnoreCase(showunavailables)) cri.add(Restrictions.gt("stock", 0l));
        else if (StoreProperty.PROP_PRODUCT_HAS_STOCK_OR_ETA.equalsIgnoreCase(showunavailables)) {
            cri.add(Restrictions.or(Restrictions.gt("stock", 0l), Restrictions.and(Restrictions.isNotNull("eta"), Restrictions.ne("eta", ""))));
        }
        cri.add(Restrictions.or(Restrictions.like("partNumber", term, MatchMode.START), Restrictions.like("urlCode", term, MatchMode.ANYWHERE)));
        cri.setMaxResults(20);
        return cri.list();
    }

    public List<AttributeProd> findAttributes(String term) {
        return createCriteriaForStore(AttributeProd.class).add(Restrictions.like("attributeName", term, MatchMode.ANYWHERE)).list();
    }

    public List<Manufacturer> findManufacturers(String term) {
        return createCriteriaForStore(Manufacturer.class).add(Restrictions.like("manufacturerName", term, MatchMode.ANYWHERE)).addOrder(Order.asc("manufacturerName")).setMaxResults(50).list();
    }

    public List<PurchaseOrder> getPurchases(DataNavigator nav, Map filters, String lang) {
        Criteria cri = createCriteriaForStore(PurchaseOrder.class);

        if (filters != null) {
            Long id = filters.containsKey("code") ? SomeUtils.strToLong((String) filters.get("code")) : null;
            if (id != null) cri.add(Restrictions.eq("id", id));

            String status = filters.containsKey("status") ? (String) filters.get("status") : null;
            if (StringUtils.isNotEmpty(status)) cri.add(Restrictions.eq("status", status));

            Date dateFrom = filters.containsKey("dateIni") ? SomeUtils.strToDate((String) filters.get("dateIni"), lang) : null;
            if (dateFrom != null) cri.add(Restrictions.ge("registerDate", SomeUtils.dateIni(dateFrom)));

            Date dateTo = filters.containsKey("dateEnd") ? SomeUtils.strToDate((String) filters.get("dateEnd"), lang) : null;
            if (dateTo != null) cri.add(Restrictions.le("registerDate", SomeUtils.dateEnd(dateTo)));

            Provider prov = filters.containsKey("provider") ? (Provider) get(Provider.class, SomeUtils.strToLong((String) filters.get("provider"))) : null;
            if (prov != null) cri.add(Restrictions.eq("provider", prov));

        }


        if (nav != null) {
            cri.setProjection(Projections.countDistinct("id"));
            Number total = (Number) cri.uniqueResult();
            nav.setTotalRows(total != null ? total.intValue() : 0);
            cri.setProjection(null);
            cri.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
            cri.addOrder(Order.desc("registerDate"));
            if (nav.getTotalRows() > 0) {
                cri.setMaxResults(nav.getPageRows());
                cri.setFirstResult(nav.getFirstRow() - 1);
            }
        }
        return cri.list();
    }


    public Product getProductOfProvider(Provider provider, String partNumber) {
        List l = gethSession().createCriteria(ProductProvider.class).add(Restrictions.eq("provider", provider)).add(Restrictions.eq("sku", partNumber)).list();
        return (l != null && !l.isEmpty()) ? ((ProductProvider) l.get(0)).getProduct() : null;
    }

    public boolean productHasProvider(Product product, Provider provider) {
        List l = gethSession().createCriteria(ProductProvider.class)
                .add(Restrictions.eq("product", product))
                .add(Restrictions.eq("provider", provider))
                .list();
        return l != null && !l.isEmpty();
    }

    public List<Product> getProductStockAlerts() {
        SQLQuery q = gethSession().createSQLQuery("SELECT DISTINCT t_product.idProduct FROM t_order_detail_product LEFT JOIN t_order_detail ON t_order_detail_product.orderDetail_idDetail=t_order_detail.idDetail " +
                "LEFT JOIN t_order ON t_order.idOrder=t_order_detail.order_idOrder LEFT JOIN t_product ON t_product.idProduct=t_order_detail_product.product_idProduct " +
                "LEFT JOIN t_order_status ON t_order_status.id=t_order.status_id " +
                "WHERE t_order_status.statusType IN ('pending','approved') AND t_product.stock<0");
        q.setResultTransformer(new IdToBeanTransformer(this, Product.class));
        return q.list();
    }

    public Map getStockAlertsStats() {
        Map result = new HashMap();
        Object o = createCriteriaForStore(Product.class)
                .add(Restrictions.or(Restrictions.isNull("archived"), Restrictions.eq("archived", Boolean.FALSE)))
                .add(Restrictions.eq("active", Boolean.TRUE))
                .add(Restrictions.or(Restrictions.le("stock", 0l), Restrictions.geProperty("stockMin", "stock")))
                .setProjection(Projections.countDistinct("idProduct")).uniqueResult();
        if (o != null && o instanceof Number) {
            result.put("products", ((Number) o).longValue());
        }

        List<Product> alerts = getProductStockAlerts();
        if (alerts != null && !alerts.isEmpty()) {
            result.put("pending", alerts.size());
            gethSession().evict(alerts);
        }
        return result;
    }

    public Integer getCartAlerts() {
        Object obj = createCriteriaForStore(ShopCart.class)
                .add(Restrictions.eq("status", ShopCart.STATUS_PENDING))
                .setProjection(Projections.countDistinct("id"))
                .uniqueResult();
        return (obj != null && obj instanceof Number) ? ((Number) obj).intValue() : 0;
    }

    public Map getPurchaseForReceiveResume() {
        String query = "SELECT t_product.idProduct, sum(t_purchase_order_line.quantity) AS quantity, sum(t_purchase_order_line.received) AS rceived FROM t_purchase_order_line LEFT JOIN t_purchase_order ON t_purchase_order.id=t_purchase_order_line.purchaseOrder_id LEFT JOIN t_product ON t_purchase_order_line.product_idProduct=t_product.idProduct " +
                "WHERE t_purchase_order.status IN ('Sent','Received') AND t_purchase_order_line.received IS null OR t_purchase_order_line.received<t_purchase_order_line.quantity " +
                "GROUP BY t_product.idProduct";
        List<Object[]> l = gethSession().createSQLQuery(query).list();
        if (l != null && !l.isEmpty()) {
            Map<Long, Long> map = new HashMap<Long, Long>();
            for (Object[] arr : l) {
                Number idProduct = (Number) arr[0];
                Number quantity = (Number) arr[1];
                Number received = (Number) arr[2];
                map.put(idProduct.longValue(), ((quantity != null) ? quantity.longValue() : 0) - ((received != null) ? received.longValue() : 0));
            }
            return map;
        }
        return null;
    }


    public Codes getCode(String code, String type) {
        List l = createCriteriaForStore(Codes.class)
                .add(Restrictions.eq("code", code))
                .add(Restrictions.eq("type", type))
                .list();
        return (l != null && !l.isEmpty()) ? (Codes) l.get(0) : null;
    }

    public List<UserFriends> getReferrals(String email) {
        return gethSession().createCriteria(UserFriends.class)
                .add(Restrictions.eq("friendEmail", email.toLowerCase()))
                .addOrder(Order.asc("id"))
                .list();
    }

    public User getReferral(String email) {
        List<UserFriends> list = gethSession().createCriteria(UserFriends.class)
                .add(Restrictions.eq("friendEmail", email.toLowerCase()))
                .add(Restrictions.eq("referred", Boolean.TRUE))
                .addOrder(Order.asc("id"))
                .list();
        return (list != null && !list.isEmpty()) ? list.get(0).getUser() : null;
    }

    public List<UserFriends> getUserFriends(User user) {
        return gethSession().createCriteria(UserFriends.class).add(Restrictions.eq("user", user))
                .addOrder(Order.asc("id"))
                .list();
    }

    public boolean userHasFriend(User user, User friend) {
        if (user == null || friend == null) return false;
        List list = gethSession().createCriteria(UserFriends.class).add(Restrictions.eq("user", user))
                .add(Restrictions.eq("friend", friend))
                .list();
        return (list != null && !list.isEmpty());
    }

    public boolean userHasFriend(User user, String email) {
        if (user == null || email == null) return false;
        List list = gethSession().createCriteria(UserFriends.class).add(Restrictions.eq("user", user))
                .add(Restrictions.eq("friendEmail", email.toLowerCase()))
                .list();
        return (list != null && !list.isEmpty());
    }

    public Map<User, Integer> getFriendsMessagesCount(User user, boolean unread) {
        Criteria cri = gethSession().createCriteria(UserMessages.class)
                .add(Restrictions.eq("toUser", user));
        if (unread) cri.add(Restrictions.or(Restrictions.isNull("readed"), Restrictions.eq("readed", Boolean.FALSE)));
        cri.setProjection(Projections.projectionList().add(Projections.count("id")).add(Projections.groupProperty("fromUser")));
        List<Object[]> l = cri.list();
        if (l != null && !l.isEmpty()) {
            Map<User, Integer> result = new HashMap<User, Integer>();
            for (Object[] arr : l) {
                Number cant = (Number) arr[0];
                User from = (User) arr[1];
                if (cant != null && cant.intValue() > 0 && from != null)
                    result.put(from, cant.intValue());
            }
            return result;
        }
        return null;
    }

    public List<UserRewardHistory> getRewardsHistory(DataNavigator nav, User user) {
        Criteria cri = gethSession().createCriteria(UserRewardHistory.class)
                .add(Restrictions.eq("user", user))
                .addOrder(Order.desc("created"));

        if (nav != null) {
            cri.setProjection(Projections.countDistinct("id"));
            Number total = (Number) cri.uniqueResult();
            nav.setTotalRows(total != null ? total.intValue() : 0);
            cri.setProjection(null);
            cri.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
            if (nav.getTotalRows() > 0) {
                cri.setMaxResults(nav.getPageRows());
                cri.setFirstResult(nav.getFirstRow() - 1);
            }
        }
        return cri.list();

    }

    public List<UserRewardHistory> getEarnsFromFriend(User user, User friend) {
        return gethSession().createCriteria(UserRewardHistory.class)
                .add(Restrictions.eq("user", user))
                .add(Restrictions.eq("friend", friend))
                .addOrder(Order.desc("created"))
                .list();
    }


    public Map<User, Double> getEarnsFromFriends(User user) {
        Criteria cri = gethSession().createCriteria(UserRewardHistory.class)
                .add(Restrictions.eq("user", user))
                .add(Restrictions.eq("status", UserRewardHistory.STATUS_COMPLETE))
                .add(Restrictions.in("type", new String[]{UserRewardHistory.TYPE_FRIEND_PURCHASE, UserRewardHistory.TYPE_FRIEND_REGISTERING, UserRewardHistory.TYPE_FRIEND_LINK}));
        cri.setProjection(Projections.projectionList().add(Projections.sum("amount")).add(Projections.groupProperty("friend")));
        List<Object[]> l = cri.list();
        if (l != null && !l.isEmpty()) {
            Map<User, Double> result = new HashMap<User, Double>();
            for (Object[] arr : l) {
                Number cant = (Number) arr[0];
                User friend = (User) arr[1];
                if (cant != null && cant.intValue() > 0 && friend != null)
                    result.put(friend, cant.doubleValue());
            }
            return result;
        }
        return null;
    }

    public List<UserMessages> getFriendMessages(User fromUser, User toUser, boolean unread) {
        Criteria cri = gethSession().createCriteria(UserMessages.class)
                .add(Restrictions.eq("toUser", toUser))
                .add(Restrictions.eq("fromUser", fromUser));
        if (unread) cri.add(Restrictions.or(Restrictions.isNull("readed"), Restrictions.eq("readed", Boolean.FALSE)));
        cri.addOrder(Order.asc("id"));
        return cri.list();
    }

    public boolean userHasReferred(String email) {
        if (email == null) return false;
        List list = gethSession().createCriteria(UserFriends.class).add(Restrictions.eq("friendEmail", email.toLowerCase())).list();
        return (list != null && !list.isEmpty());
    }

    public List<Product> getFriendSalesReport(User user, Integer maxProducts) {
        String query = "select cant, {t_product.*} from " +
                "(select t_order_detail_product.product_idProduct, sum(t_order_detail.quantity) as cant from t_order left join t_user_friends on t_order.user_idUser=t_user_friends.friend_idUser " +
                "left join t_order_detail on t_order.idOrder=t_order_detail.order_idOrder " +
                "left join t_order_detail_product on t_order_detail.idDetail=t_order_detail_product.orderDetail_idDetail " +
                "left join t_order_status on t_order_status.id=t_order.status_id " +
                "where t_user_friends.user_idUser=" + user.getIdUser().toString() + " and t_order_status.statusType = 'approved' and t_user_friends.referred=1 " +
                "group by t_order_detail_product.product_idProduct) t " +
                "left join t_product on t_product.idProduct=t.product_idProduct " +
                "order by cant desc";
        SQLQuery q = gethSession().createSQLQuery(query).addScalar("cant").addEntity("t_product", Product.class);
        if (maxProducts != null && maxProducts > 0) q.setMaxResults(maxProducts);
        List<Object[]> list = q.list();
        if (list != null && !list.isEmpty()) {
            List<Product> result = new ArrayList<Product>();
            for (Object[] arr : list) {
                Number cant = (Number) arr[0];
                Product p = (Product) arr[1];
                p.addProperty("sales", cant);
                result.add(p);
            }
            return result;
        }
        return null;
    }

    public ProductAuditStock auditStock(Product p, String description, User user) {
        ProductAuditStock bean = new ProductAuditStock();
        bean.setProduct(p);
        bean.setUser(user);
        bean.setStock(p.getStock());
        bean.setChangeDate(Calendar.getInstance().getTime());
        bean.setDescription(description);
        save(bean);
        return bean;
    }

    public List<Number> getIdProductsInCategory(Long idCategory) {
        Category beanCat = (Category) get(Category.class, idCategory);
        if (beanCat != null) {
            Long[] ids = getIdCategoryList(beanCat, false);
            if (ids != null && ids.length > 0) {
                StringBuilder b = new StringBuilder();
                for (Long id : ids) {
                    if (StringUtils.isNotEmpty(b.toString())) b.append(",");
                    b.append(id);
                }
                SQLQuery q = gethSession().createSQLQuery("select distinct t_product_t_category.t_product_idProduct from t_product_t_category where t_product_t_category.productCategories_idCategory in (" + b.toString() + ")");
                return q.list();
            }
        }
        return null;
    }

    public List<StaticText> getNews(DataNavigator nav) {
        Criteria cri = gethSession().createCriteria(StaticText.class).add(Restrictions.eq("textType", StaticText.TYPE_NEWS));
        if (nav != null) {
            cri.setProjection(Projections.countDistinct("id"));
            Number total = (Number) cri.uniqueResult();
            nav.setTotalRows(total != null ? total.intValue() : 0);
            cri.setProjection(null);
            if (nav.needPagination()) {
                cri.setFirstResult(nav.getFirstRow() - 1);
                cri.setMaxResults(nav.getPageRows());
            }
        }
        cri.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        cri.addOrder(Order.desc("contentDate"));
        return cri.list();
    }


    public int getManufacturersCount() {
        Object o = createCriteriaForStore(Manufacturer.class).setProjection(Projections.countDistinct("idManufacturer")).uniqueResult();
        return (o != null && o instanceof Number) ? ((Number) o).intValue() : 0;
    }

    public int getCategoriesCount() {
        Object o = createCriteriaForStore(Category.class).setProjection(Projections.countDistinct("idCategory")).uniqueResult();
        return (o != null && o instanceof Number) ? ((Number) o).intValue() : 0;
    }

    public String getText(String key, String lang) {
        LocalizedText bean = getLocalizedtext(key);
        return (bean != null) ? bean.getValue(lang) : null;
    }

    public String getStorePath() {
        return getStorePropertyValue(StoreProperty.PROP_SITE_PATH, StoreProperty.TYPE_GENERAL, "");
    }
}