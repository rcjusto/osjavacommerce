package org.store.core.globals;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.Preparable;
import com.opensymphony.xwork2.config.Configuration;
import com.opensymphony.xwork2.inject.Inject;
import com.opensymphony.xwork2.util.ValueStack;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.struts2.ServletActionContext;
import org.apache.struts2.interceptor.ServletRequestAware;
import org.apache.struts2.interceptor.ServletResponseAware;
import org.apache.struts2.interceptor.SessionAware;
import org.apache.struts2.json.JSONException;
import org.apache.struts2.json.JSONUtil;
import org.apache.struts2.util.ServletContextAware;
import org.apache.velocity.Template;
import org.apache.velocity.context.Context;
import org.store.core.beans.*;
import org.store.core.beans.Currency;
import org.store.core.beans.mail.MOrder;
import org.store.core.beans.mail.MProduct;
import org.store.core.beans.mail.MUser;
import org.store.core.beans.utils.DataNavigator;
import org.store.core.beans.utils.PageMeta;
import org.store.core.beans.utils.ProductFilter;
import org.store.core.dao.HibernateDAO;
import org.store.core.dto.CategoryDTO;
import org.store.core.dto.NewsDTO;
import org.store.core.globals.config.Store20Commerce;
import org.store.core.globals.config.Store20Config;
import org.store.core.globals.config.Store20Database;
import org.store.core.mail.MailSenderThreat;
import org.store.core.utils.PluginAdminMenu;
import org.store.core.utils.merchants.MerchantService;
import org.store.core.utils.merchants.MerchantUtils;
import org.store.core.utils.templates.TemplateBannerZone;
import org.store.core.utils.templates.TemplateBlock;
import org.store.core.utils.templates.TemplateConfig;
import org.store.core.utils.templates.TemplateUtils;
import org.store.core.velocity.StoreVelocityManager;

import javax.servlet.ServletContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.swing.tree.DefaultMutableTreeNode;
import java.io.File;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;

import org.apache.struts2.dispatcher.mapper.ActionMapping;
import org.hibernate.Session;
import org.store.core.hibernate.HibernateSessionFactory;

/**
 * Created by IntelliJ IDEA.
 * User: Administrador
 * Date: Mar 22, 2010
 */
public class BaseAction extends ActionSupport implements Preparable, SessionAware, ServletRequestAware, ServletResponseAware, ServletContextAware, StoreMessages {

    public static Logger log = Logger.getLogger(BaseAction.class);

    protected HibernateDAO dao = new HibernateDAO();

    protected User adminUser;
    protected User frontUser;
    protected User affiliateUser;
    protected String affiliateCode;
    protected String permitRoles;
    protected String logAction;
    protected String storeCode;
    protected Currency actualCurrency;
    protected String redirectUrl;
    protected String velocityView;
    protected File[] uploads;
    protected String[] uploadsFilename;

    protected Map session;
    protected HttpServletRequest request;
    protected HttpServletResponse response;
    protected ServletContext servletContext;
    protected Map<String, Object> requestCache = new HashMap<String, Object>();
    protected Map<String, Object> storeSessionObjects;

    private InputStream inputStream;
    private String contentType;
    private String contentLength;
    private String contentDisposition;

    private Store20Commerce commerceConfig;
    private Store20Database databaseConfig;

    private StoreVelocityManager velocityManager;
    private static final String CNT_SUBJECT_STOCK_ALERT = "mail.subject.user.stock.alert";
    private static final String CNT_DEFAULT_SUBJECT_STOCK_ALERT = "Stock alert";
    private static final String CNT_SUBJECT_ADMIN_STOCK_ALERT = "mail.subject.admin.stock.alert";
    private static final String CNT_DEFAULT_SUBJECT_ADMIN_STOCK_ALERT = "There are products in stock alert";
    private static final String CNT_SUBJECT_ORDER_STATUS = "mail.subject.order.status";
    private static final String CNT_DEFAULT_SUBJECT_ORDER_STATUS = "Your order {1} has status {2}";
    private static final String CNT_SUBJECT_WELCOME = "mail.subject.welcome";
    private static final String CNT_DEFAULT_SUBJECT_WELCOME = "Welcome to our store";
    private static final String CNT_SUBJECT_USER_REQUEST_LEVEL = "mail.subject.user.request.level";
    private static final String CNT_DEFAULT_SUBJECT_USER_REQUEST_LEVEL = "Customer level requested";

    private Configuration configuration;

    @Inject
    public void setConfiguration(Configuration config) {
        this.configuration = config;
    }

    public boolean actionExist(String action, String namespace) {
        Map allActionConfigs = configuration.getRuntimeConfiguration().getActionConfigs();
        if (allActionConfigs != null) {
            Map actionMappings = (Map) allActionConfigs.get(namespace);
            if (actionMappings != null) {
                return actionMappings.keySet().contains(action);
            }
        }
        return false;
    }

    @Inject
    public void setStoreVelocityManager(StoreVelocityManager mgr) {
        this.velocityManager = mgr;
    }

    public void setSession(Map map) {
        session = map;
    }

    public void setServletRequest(HttpServletRequest httpServletRequest) {
        request = httpServletRequest;
    }

    public void setServletResponse(HttpServletResponse httpServletResponse) {
        response = httpServletResponse;
    }

    public Map getSession() {
        return session;
    }

    public HttpServletRequest getRequest() {
        return request;
    }

    public String getRedirectUrl() {
        return redirectUrl;
    }

    public void setRedirectUrl(String redirectUrl) {
        this.redirectUrl = redirectUrl;
    }

    public String getVelocityView() {
        return velocityView;
    }

    public void setVelocityView(String velocityView) {
        this.velocityView = velocityView;
    }

    public File[] getUploads() {
        return uploads;
    }

    public void setUploads(File[] uploads) {
        this.uploads = uploads;
    }

    public void addToStack(String key, Object value) {
        getRequest().setAttribute(key, value);
    }

    public void addToStackSession(String key, Object value) {
        Map<String, Object> attMap = (Map<String, Object>) getStoreSessionObjects().get(StoreSessionInterceptor.CNT_ATTRIBUTES);
        if (attMap == null) {
            attMap = new HashMap<String, Object>();
            getStoreSessionObjects().put(StoreSessionInterceptor.CNT_ATTRIBUTES, attMap);
        }
        attMap.put(key, value);
    }

    public HttpServletResponse getResponse() {
        return response;
    }

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

    public String getContentLength() {
        return contentLength;
    }

    public void setContentLength(String contentLength) {
        this.contentLength = contentLength;
    }

    public String getContentDisposition() {
        return contentDisposition;
    }

    public void setContentDisposition(String contentDisposition) {
        this.contentDisposition = contentDisposition;
    }

    public String getPermitRoles() {
        return permitRoles;
    }

    public void setPermitRoles(String permitRoles) {
        this.permitRoles = permitRoles;
    }

    public void addSessionMessage(String msg) {
        List<String> msgList = (List<String>) getStoreSessionObjects().get(StoreSessionInterceptor.CNT_MESSAGES);
        if (msgList == null) {
            msgList = new ArrayList<String>();
            getStoreSessionObjects().put(StoreSessionInterceptor.CNT_MESSAGES, msgList);
        }
        msgList.add(msg);
    }

    public void addSessionError(String msg) {
        List<String> msgList = (List<String>) getStoreSessionObjects().get(StoreSessionInterceptor.CNT_ERRORS);
        if (msgList == null) {
            msgList = new ArrayList<String>();
            getStoreSessionObjects().put(StoreSessionInterceptor.CNT_ERRORS, msgList);
        }
        msgList.add(msg);
    }

    public void addSessionFieldError(String fld, String msg) {
        Map<String, List<String>> msgMap = (Map<String, List<String>>) getStoreSessionObjects().get(StoreSessionInterceptor.CNT_ERRORS_FIELD);
        if (msgMap == null) {
            msgMap = new HashMap<String, List<String>>();
            getStoreSessionObjects().put(StoreSessionInterceptor.CNT_ERRORS_FIELD, msgMap);
        }
        if (msgMap.containsKey(fld)) {
            List<String> l = msgMap.get(fld);
            l.add(msg);
        } else {
            List<String> l = new ArrayList<String>();
            l.add(msg);
            msgMap.put(fld, l);
        }
    }

    public String getLogAction() {
        return logAction;
    }

    public void setLogAction(String logAction) {
        this.logAction = logAction;
    }

    public String[] getActionRoles() {
        return (getPermitRoles() != null && !StringUtils.isEmpty(getPermitRoles())) ? getPermitRoles().split(",") : null;
    }

    public ServletContext getServletContext() {
        return servletContext;
    }

    public void setServletContext(ServletContext sc) {
        servletContext = sc;
    }

    public String getStoreCode() {
        return storeCode;
    }

    public void setStoreCode(String storeCode) {
        this.storeCode = storeCode;
    }

    public HibernateDAO getDao() {
        return dao;
    }

    public void setDao(HibernateDAO dao) {
        this.dao = dao;
    }

    public Map<String, Object> getStoreSessionObjects() {
        return storeSessionObjects;
    }

    public void setStoreSessionObjects(Map<String, Object> storeSessionObjects) {
        this.storeSessionObjects = storeSessionObjects;
    }

    public Store20Commerce getCommerceConfig() {
        return commerceConfig;
    }

    public void setCommerceConfig(Store20Commerce commerceConfig) {
        this.commerceConfig = commerceConfig;
    }

    public Store20Database getDatabaseConfig() {
        return databaseConfig;
    }

    public void setDatabaseConfig(Store20Database databaseConfig) {
        this.databaseConfig = databaseConfig;
    }

    public void prepare() throws Exception {

    }

    public String emptyMethod() {
        return SUCCESS;
    }

    public String getCookie(String name) {
        if (StringUtils.isNotEmpty(name) && getRequest().getCookies() != null) {
            for (Cookie c : getRequest().getCookies()) {
                if (name.equalsIgnoreCase(c.getName())) {
                    return (StringUtils.isNotEmpty(c.getValue())) ? new String(Base64.decodeBase64(c.getValue().getBytes())) : null;
                }
            }
        }
        return null;
    }

    public Cookie getCookie(String name, String value) {
        String encodeValue = new String(Base64.encodeBase64(value.getBytes()));
        return new Cookie(name, encodeValue);
    }

    public ImageResolver getImageResolver() {
        if (!requestCache.containsKey("IMAGE_RESOLVER")) {
            String propZoom = getStoreProperty(StoreProperty.PROP_PRODUCT_IMAGES_ZOOM, StoreProperty.PROP_DEFAULT_PRODUCT_IMAGES_ZOOM);
            String propDetail = getStoreProperty(StoreProperty.PROP_PRODUCT_IMAGES_DETAIL, StoreProperty.PROP_DEFAULT_PRODUCT_IMAGES_DETAIL);
            String propList = getStoreProperty(StoreProperty.PROP_PRODUCT_IMAGES_LIST, StoreProperty.PROP_DEFAULT_PRODUCT_IMAGES_LIST);
            Integer propMaxWidth = SomeUtils.strToInteger(getStoreProperty(StoreProperty.PROP_PRODUCT_IMAGES_MAX_WIDTH, StoreProperty.PROP_DEFAULT_PRODUCT_IMAGES_MAX_WIDTH));
            Integer propMaxHeight = SomeUtils.strToInteger(getStoreProperty(StoreProperty.PROP_PRODUCT_IMAGES_MAX_HEIGHT, StoreProperty.PROP_DEFAULT_PRODUCT_IMAGES_MAX_HEIGHT));
            requestCache.put("IMAGE_RESOLVER", new ImageResolverImpl(getServletContext(), getStoreCode(), propZoom, propDetail, propList, propMaxWidth, propMaxHeight));
        }
        return (ImageResolver) requestCache.get("IMAGE_RESOLVER");
    }

    public IP2CountryService getIP2CountryService() {
        if (!requestCache.containsKey("IP2CountryService")) {
            Object obj = Store20Config.getInstance(getServletContext()).getComponentInstance("IP2CountryService");
            if (obj instanceof IP2CountryService) {
                IP2CountryService ip2CountryService = (IP2CountryService) obj;
                ip2CountryService.init(getServletContext());
                requestCache.put("IP2CountryService", ip2CountryService);
            }
        }
        return (requestCache.containsKey("IP2CountryService")) ? (IP2CountryService) requestCache.get("IP2CountryService") : null;
    }

    public String[] getLanguages() {
        return dao.getLanguages();
    }

    public String getDefaultLanguage() {
        if (!requestCache.containsKey("DEFAULT_LANGUAGE")) requestCache.put("DEFAULT_LANGUAGE", dao.getDefaultLanguage());
        return (String) requestCache.get("DEFAULT_LANGUAGE");
    }

    public String getDefaultCountry() {
        if (!requestCache.containsKey("DEFAULT_COUNTRY")) requestCache.put("DEFAULT_COUNTRY", dao.getDefaultCountry());
        return (String) requestCache.get("DEFAULT_COUNTRY");
    }

    public String getEncryptionKey() {
        StoreProperty bean = dao.getStoreProperty(StoreProperty.PROP_ENCRYPTION_KEY, StoreProperty.TYPE_GENERAL, true);
        if (StringUtils.isEmpty(bean.getValue())) {
            bean.setValue(User.generatePassword(16));
            dao.save(bean);
        }
        return bean.getValue();
    }

    public Currency getDefaultCurrency() {
        if (!requestCache.containsKey("DEFAULT_CURRENCY")) requestCache.put("DEFAULT_CURRENCY", dao.getDefaultCurrency());
        return (Currency) requestCache.get("DEFAULT_CURRENCY");
    }

    public boolean getAllowAnonymousCheckout() {
        return "Y".equalsIgnoreCase(getStoreProperty(StoreProperty.PROP_ALLOW_ANONYMOUS_CHECKOUT, StoreProperty.PROP_DEFAULT_ALLOW_ANONYMOUS_CHECKOUT)) && !(getFrontUser() != null && !getFrontUser().getAnonymousCheckout());
    }

    public String getTemplate() {
        if (getStoreSessionObjects().containsKey(StoreProperty.PROP_TEMPLATE) && getStoreSessionObjects().get(StoreProperty.PROP_TEMPLATE) instanceof String && StringUtils.isNotEmpty((String) getStoreSessionObjects().get(StoreProperty.PROP_TEMPLATE))) return (String) getStoreSessionObjects().get(StoreProperty.PROP_TEMPLATE);
        else return getStoreProperty(StoreProperty.PROP_TEMPLATE, StoreProperty.PROP_DEFAULT_TEMPLATE);
    }

    public String getSkin() {
        if (getStoreSessionObjects().containsKey(StoreProperty.PROP_SKIN) && getStoreSessionObjects().get(StoreProperty.PROP_SKIN) instanceof String && StringUtils.isNotEmpty((String) getStoreSessionObjects().get(StoreProperty.PROP_SKIN))) return (String) getStoreSessionObjects().get(StoreProperty.PROP_SKIN);
        else return getStoreProperty(StoreProperty.PROP_SKIN, StoreProperty.PROP_DEFAULT_SKIN);
    }

    public String getDimensionUnit() {
        return getStoreProperty(StoreProperty.PROP_DIMENSION_UNIT, StoreProperty.PROP_DEFAULT_DIMENSION_UNIT);
    }

    public String getWeightUnit() {
        return getStoreProperty(StoreProperty.PROP_WEIGHT_UNIT, StoreProperty.PROP_DEFAULT_WEIGHT_UNIT);
    }

    public String getProductSortOptions() {
        return getStoreProperty(StoreProperty.PROP_PRODUCT_SORT_OPTIONS, StoreProperty.PROP_DEFAULT_PRODUCT_SORT_OPTIONS);
    }

    public String getProductSortDefaultOption() {
        return getStoreProperty(StoreProperty.PROP_PRODUCT_SORT_DEFAULT_OPTION, StoreProperty.PROP_DEFAULT_PRODUCT_SORT_DEFAULT_OPTION);
    }

    public Boolean getCanShowPrices() {
        return "Y".equalsIgnoreCase(getStoreProperty(StoreProperty.PROP_SHOW_PRICE_WITHOUT_LOGIN, StoreProperty.PROP_DEFAULT_SHOW_PRICE_WITHOUT_LOGIN)) || getFrontUser() != null;
    }

    public Boolean getCanShowStock() {
        return "Y".equalsIgnoreCase(getStoreProperty(StoreProperty.PROP_PRODUCT_SHOW_STOCK_INFORMATION, StoreProperty.PROP_DEFAULT_PRODUCT_SHOW_STOCK_INFORMATION))
                && ("Y".equalsIgnoreCase(getStoreProperty(StoreProperty.PROP_SHOW_STOCK_WITHOUT_LOGIN, StoreProperty.PROP_DEFAULT_SHOW_STOCK_WITHOUT_LOGIN)) || getFrontUser() != null);
    }

    public Category getLastViewCategory() {
        if (getStoreSessionObjects().containsKey(StoreSessionInterceptor.CNT_LAST_CATEGORY)) {
            Object o = getStoreSessionObjects().get(StoreSessionInterceptor.CNT_LAST_CATEGORY);
            if (o instanceof Long) {
                return dao.getCategory((Long) o);
            }
        }
        return null;
    }

    public String getStoreProperty(String propertyKey, String defaultValue) {
        StoreProperty bean = dao.getStoreProperty(propertyKey, StoreProperty.TYPE_GENERAL);
        return (bean != null && StringUtils.isNotEmpty(bean.getValue())) ? bean.getValue() : defaultValue;
    }

    public Object getParentPropertyBean(Object bean, String property) {
        return dao.getParentPropertyBean(bean, property);
    }

    public String getBeanProperty(Object bean, String property) {
        try {
            return (bean != null) ? BeanUtils.getProperty(bean, property) : null;
        } catch (IllegalAccessException e) {
            log.error(e.getMessage(), e);
        } catch (InvocationTargetException e) {
            log.error(e.getMessage(), e);
        } catch (NoSuchMethodException e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    public String getParentProperty(Object bean, String property) {
        bean = dao.getParentPropertyBean(bean, property);
        return getBeanProperty(bean, property);
    }

    public User getAdminUser() {
        return adminUser;
    }

    public void setAdminUser(User user) {
        this.adminUser = user;
        if (user != null) storeSessionObjects.put(StoreSessionInterceptor.CNT_ADMIN_USER, user.getIdUser());
        else storeSessionObjects.remove(StoreSessionInterceptor.CNT_ADMIN_USER);
    }

    public User getFrontUser() {
        return frontUser;
    }


    public User getAffiliateUser() {
        return affiliateUser;
    }

    public void setAffiliateUser(User user) {
        this.affiliateUser = user;
        if (user != null) storeSessionObjects.put(StoreSessionInterceptor.CNT_AFFILIATE_USER, user.getIdUser());
        else storeSessionObjects.remove(StoreSessionInterceptor.CNT_AFFILIATE_USER);
    }

    public String getAffiliateCode() {
        return affiliateCode;
    }

    public void setAffiliateCode(String affiliateCode) {
        this.affiliateCode = affiliateCode;
        if (StringUtils.isNotEmpty(affiliateCode)) storeSessionObjects.put(StoreSessionInterceptor.CNT_AFFILIATE_CODE, affiliateCode);
        else storeSessionObjects.remove(StoreSessionInterceptor.CNT_AFFILIATE_CODE);
    }

    public UserLevel getFrontUserLevel() {
        return (getFrontUser() != null) ? getFrontUser().getLevel() : getAnonymousLevel();
    }


    public Currency getActualCurrency() {
        return actualCurrency;
    }

    public void setActualCurrency(Currency actualCurrency) {
        this.actualCurrency = actualCurrency;
    }

    public List<Product> getRandomProducts(String filterStr, int cant) {
        ProductFilter filter = new ProductFilter(filterStr);
        filter.setSortedField("random");
        return dao.listFrontProducts(null, filter, null, getLocale().getLanguage(), cant);
    }

    public List<Product> getBestSellers(int cant) {
        ProductFilter filter = new ProductFilter();
        filter.setSortedField("extraSales,sales,hits");
        filter.setSortedDirection("desc");
        return dao.listFrontProducts(null, filter, null, getLocale().getLanguage(), cant);
    }


    public boolean hasRole(String roleCode) {
        User admin = getAdminUser();
        return (admin != null && admin.hasRoleCode(roleCode));
    }

    public ProductAuditStock auditStock(Product p, String description) {
        return dao.auditStock(p, description, getFrontUser());
    }

    // Metodos q se llaman de las vistas

    public boolean canAddReview() {
        return "Y".equalsIgnoreCase(getStoreProperty(StoreProperty.PROP_ENABLE_PRODUCT_REVIEWS, StoreProperty.PROP_DEFAULT_ENABLE_PRODUCT_REVIEWS)) && (!"Y".equalsIgnoreCase(getStoreProperty(StoreProperty.PROP_REVIEW_MUST_REGISTERED, StoreProperty.PROP_DEFAULT_REVIEW_MUST_REGISTERED)) || getFrontUser() != null);
    }

    public Manufacturer getManufacturer(Object idO) {
        Long id = null;
        if (idO instanceof Number) id = ((Number) idO).longValue();
        else if (idO instanceof String) id = SomeUtils.strToLong((String) idO);
        return (Manufacturer) dao.get(Manufacturer.class, id);
    }

    public ProductLabel getLabel(String code) {
        return dao.getProductLabelByCode(code);
    }

    public UserLevel getLevel(String code) {
        return dao.getUserLevel(code);
    }

    public UserLevel getDefaultLevel() {
        String defUsrLevelCode = getStoreProperty(StoreProperty.PROP_DEFAULT_USER_LEVEL, null);
        UserLevel l = dao.getUserLevel(defUsrLevelCode);
        if (l == null) l = dao.getUserLevel(UserLevel.DEFAULT_LEVEL);
        if (l == null) {
            l = new UserLevel();
            l.setCode(UserLevel.DEFAULT_LEVEL);
            for (String lang : getLanguages()) l.setName(lang, UserLevel.DEFAULT_LEVEL);
            l.setLevelOrder(1);
            dao.save(l);
        }
        return l;
    }

    public UserLevel getAnonymousLevel() {
        UserLevel l = dao.getUserLevel(UserLevel.ANONYMOUS_LEVEL);
        if (l == null) {
            l = new UserLevel();
            l.setCode(UserLevel.ANONYMOUS_LEVEL);
            for (String lang : getLanguages()) l.setName(lang, UserLevel.ANONYMOUS_LEVEL);
            l.setLevelOrder(0);
            dao.save(l);
        }
        return l;
    }

    public UserLevel getAffiliateLevel() {
        UserLevel l = dao.getUserLevel(UserLevel.AFFILIATE_LEVEL);
        if (l == null) {
            l = new UserLevel();
            l.setCode(UserLevel.AFFILIATE_LEVEL);
            for (String lang : getLanguages()) l.setName(lang, UserLevel.AFFILIATE_LEVEL);
            l.setLevelOrder(10);
            dao.save(l);
        }
        return l;
    }

    public Set<String> getActionsForUser(User u) {
        Set<String> result = new HashSet<String>();
        if (u != null && u.getRoles() != null) {
            for (UserAdminRole uar : u.getRoles()) {
                if (uar.getActions() != null) result.addAll(uar.getActions());
            }
        }
        return result;
    }

    public Map<String, List<PluginAdminMenu>> getAdminMenus() {
        if (!requestCache.containsKey("PLUGIN_MENUS")) requestCache.put("PLUGIN_MENUS", Store20Config.getInstance(getServletContext()).getMenus());
        return (Map<String, List<PluginAdminMenu>>) requestCache.get("PLUGIN_MENUS");
    }

    public List<PluginAdminMenu> getAdminMenus(String cad) {
        Map<String, List<PluginAdminMenu>> map = getAdminMenus();
        if (map != null) {
            if (StringUtils.isNotBlank(cad)) return map.get(cad);
            else {
                List<PluginAdminMenu> result = new ArrayList<PluginAdminMenu>();
                for (List<PluginAdminMenu> list : map.values()) result.addAll(list);
                return result;
            }
        }
        return null;
    }

    public boolean getCanPickInStore() {
        String prop = getStoreProperty(StoreProperty.PROP_ALLOW_PICK_IN_STORE, StoreProperty.PROP_DEFAULT_ALLOW_PICK_IN_STORE);
        if ("Y".equalsIgnoreCase(prop)) {
            List l = getLocationStoreList();
            if (l != null && !l.isEmpty()) return true;
        }
        return false;
    }

    public List<LocationStore> getLocationStoreList() {
        if (!requestCache.containsKey("LOCATIONS_LIST")) requestCache.put("LOCATIONS_LIST", dao.getLocationStores(true));
        return (List<LocationStore>) requestCache.get("LOCATIONS_LIST");
    }

    public LocationStore getLocationStore(Long id) {
        return (LocationStore) dao.get(LocationStore.class, id);
    }

    public List<UserLevel> getUserLevelListAll() {
        if (!requestCache.containsKey("LEVEL_LIST_ALL")) requestCache.put("LEVEL_LIST_ALL", dao.getUserLevels(true));
        return (List<UserLevel>) requestCache.get("LEVEL_LIST_ALL");
    }

    public List<UserLevel> getUserLevelList() {
        if (!requestCache.containsKey("LEVEL_LIST")) requestCache.put("LEVEL_LIST", dao.getUserLevels(false));
        return (List<UserLevel>) requestCache.get("LEVEL_LIST");
    }

    public List<UserLevel> getUserLevelList(Boolean freeCache) {
        if (freeCache && requestCache.containsKey("LEVEL_LIST")) requestCache.remove("LEVEL_LIST");
        return getUserLevelList();
    }

    public List<Provider> getProviderList() {
        if (!requestCache.containsKey("PROVIDER_LIST")) requestCache.put("PROVIDER_LIST", dao.getProviders());
        return (List<Provider>) requestCache.get("PROVIDER_LIST");
    }

    public List<Manufacturer> getPaytermList() {
        if (!requestCache.containsKey("PAYTERMS_LIST")) requestCache.put("PAYTERMS_LIST", dao.getPayterms());
        return (List<Manufacturer>) requestCache.get("PAYTERMS_LIST");
    }

    public List<Manufacturer> getManufacturerList() {
        if (!requestCache.containsKey("MANUFACTURER_LIST")) requestCache.put("MANUFACTURER_LIST", dao.getManufacturers());
        return (List<Manufacturer>) requestCache.get("MANUFACTURER_LIST");
    }

    public List<ProductLabel> getLabelList() {
        if (!requestCache.containsKey("LABEL_LIST")) requestCache.put("LABEL_LIST", dao.getProductLabels());
        return (List<ProductLabel>) requestCache.get("LABEL_LIST");
    }

    public List<AttributeProd> getProdAtributes() {
        if (!requestCache.containsKey("ATTPROD_LIST")) requestCache.put("ATTPROD_LIST", dao.getAttributeProds());
        return (List<AttributeProd>) requestCache.get("ATTPROD_LIST");
    }

    public List<Fee> getFees() {
        if (!requestCache.containsKey("FEE_LIST")) requestCache.put("FEE_LIST", dao.getFees());
        return (List<Fee>) requestCache.get("FEE_LIST");
    }

    public List<Tax> getTaxes() {
        if (!requestCache.containsKey("TAX_LIST")) requestCache.put("TAX_LIST", dao.getTaxes());
        return (List<Tax>) requestCache.get("TAX_LIST");
    }

    public List<OrderStatus> getOrderStatusList() {
        if (!requestCache.containsKey("ORDER_STATUS_LIST")) requestCache.put("ORDER_STATUS_LIST", dao.getOrderStatuses());
        return (List<OrderStatus>) requestCache.get("ORDER_STATUS_LIST");
    }

    public List<State> getStates() {
        if (!requestCache.containsKey("STATE_LIST")) requestCache.put("STATE_LIST", dao.getAllStates());
        return (List<State>) requestCache.get("STATE_LIST");
    }

    public List<ShippingMethod> getShippingMethodList() {
        if (!requestCache.containsKey("SHIPPING_METHOD_LIST")) requestCache.put("SHIPPING_METHOD_LIST", dao.getShippingMethodList());
        return (List<ShippingMethod>) requestCache.get("SHIPPING_METHOD_LIST");
    }


    public List<StaticText> getAllStaticTexts() {
        if (!requestCache.containsKey("STATIC_TEXT_LIST")) requestCache.put("STATIC_TEXT_LIST", dao.getStaticTexts(null));
        return (List<StaticText>) requestCache.get("STATIC_TEXT_LIST");
    }

    public User getUserById(Long id) {
        if (id == null) return null;
        if (!requestCache.containsKey("USER_" + id.toString())) requestCache.put("USER_" + id.toString(), dao.get(User.class, id));
        return (User) requestCache.get("USER_" + id.toString());
    }

    public List<StaticText> getStaticTexts(String type) {
        return dao.getStaticTexts(type);
    }

    public BaseBean findBeanInList(List list, String fieldName, String fieldValue) {
        if (list != null && !list.isEmpty()) {
            for (Object o : list) {
                if (o instanceof BaseBean) {
                    try {
                        String fv = BeanUtils.getProperty(o, fieldName);
                        if (fv != null && fv.equalsIgnoreCase(fieldValue)) return (BaseBean) o;
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                    }
                }
            }
        }
        return null;
    }

    public DataNavigator getNews(Integer pageItems, Integer pageNumber) {
        DataNavigator nav = new DataNavigator("news");
        nav.setPageRows(pageItems);
        nav.setCurrentPage(pageNumber);
        List<StaticText> l = dao.getStaticTexts(nav, StaticText.TYPE_NEWS, null);
        if (l != null && !l.isEmpty()) {
            List<NewsDTO> res = new ArrayList<NewsDTO>();
            for (StaticText s : l) res.add(new NewsDTO(s));
            nav.setListado(res);
            return nav;
        }
        return null;
    }

    public List<CountryFactory.Country> getCountries(boolean configured) {
        if (configured) {
            if (!requestCache.containsKey("CONF_COUNTRIES")) requestCache.put("CONF_COUNTRIES", dao.getConfiguredCountries(getLocale(), true));
            return (List<CountryFactory.Country>) requestCache.get("CONF_COUNTRIES");
        } else {
            if (!requestCache.containsKey("ALL_COUNTRIES")) requestCache.put("ALL_COUNTRIES", CountryFactory.getCountries(getLocale()));
            return (List<CountryFactory.Country>) requestCache.get("ALL_COUNTRIES");
        }
    }

    public List<ComplementGroup> getComplementGroups() {
        if (!requestCache.containsKey("ComplementGroups")) requestCache.put("ComplementGroups", dao.getComplementGroups());
        return (List<ComplementGroup>) requestCache.get("ComplementGroups");
    }

    public List<Currency> getCurrencyList() {
        if (!requestCache.containsKey("CurrencyList")) requestCache.put("CurrencyList", dao.getCurrencies(true));
        return (List<Currency>) requestCache.get("CurrencyList");
    }

    public List<Insurance> getInsurances() {
        if (!requestCache.containsKey("Insurances")) requestCache.put("Insurances", dao.getInsurances());
        return (List<Insurance>) requestCache.get("Insurances");
    }

    public TemplateConfig getTemplateConfig() {
        if (!requestCache.containsKey("TemplateConfig")) {
            TemplateConfig templateConfig = TemplateUtils.getTemplateConfig(getServletContext(), getTemplate());
            if (templateConfig != null) requestCache.put("TemplateConfig", templateConfig);
        }
        return (requestCache.containsKey("TemplateConfig")) ? (TemplateConfig) requestCache.get("TemplateConfig") : null;
    }

    public TemplateBannerZone getTemplateBannerZone(String zone) {
        TemplateConfig templateConfig = getTemplateConfig();
        return (templateConfig != null) ? templateConfig.getBannerZone(zone) : null;
    }

    public List<TemplateBannerZone> getCurrentBannersZones() {
        if (!requestCache.containsKey("CurrentBanners")) {
            List<TemplateBannerZone> result = new ArrayList<TemplateBannerZone>();
            TemplateConfig templateConfig = getTemplateConfig();
            if (templateConfig != null) result.addAll(templateConfig.getBanners());
            result.addAll(Store20Config.getInstance(getServletContext()).getExtraBanners());
            requestCache.put("CurrentBanners", result);
        }
        return (List<TemplateBannerZone>) requestCache.get("CurrentBanners");
    }

    public List<TemplateBlock> getCurrentBlocks() {
        if (!requestCache.containsKey("CurrentBlocks")) {
            List<TemplateBlock> result = new ArrayList<TemplateBlock>();
            TemplateConfig templateConfig = getTemplateConfig();
            if (templateConfig != null) result.addAll(templateConfig.getBlocks());
            result.addAll(Store20Config.getInstance(getServletContext()).getExtraBlocks());
            requestCache.put("CurrentBlocks", result);
        }
        return (List<TemplateBlock>) requestCache.get("CurrentBlocks");
    }

    public TemplateBlock getBlockData(String code) {
        if (StringUtils.isEmpty(code)) return null;
        List<TemplateBlock> currentBlocks = getCurrentBlocks();
        if (currentBlocks != null && !currentBlocks.isEmpty()) {
            for (TemplateBlock b : currentBlocks)
                if (code.equalsIgnoreCase(b.getCode())) return b;
        }
        return null;
    }

    public Product getProduct(Long id) {
        return (Product) dao.get(Product.class, id);
    }

    public Fee getFee(Long id) {
        return (Fee) dao.get(Fee.class, id);
    }

    public Tax getTax(Long id) {
        return (Tax) dao.get(Tax.class, id);
    }

    public List<ProductRelated> getProductsRelated(Product p) {
        // Sin cache. Tener cuidado en las vistas, no llamarlo varias veces
        return dao.getProductsRelated(p);
    }

    public List<ProductRelated> getProductsRelatedCombined(List<ProductRelated> list) {
        List<ProductRelated> res = new ArrayList<ProductRelated>();
        if (list != null && !list.isEmpty()) {
            for (ProductRelated r : list) {
                if (r.getCombinedPrice() != null && r.getCombinedPrice() > 0 && r.getRelated().getMaxToBuy(getFrontUser()) > 0) res.add(r);
            }
        }
        return res;
    }

    public List<ProductRelated> getProductsRelatedCombined(Product p, boolean combined) {
        // Sin cache. Tener cuidado en las vistas, no llamarlo varias veces
        return dao.getProductsRelatedCombined(p, combined);
    }


    public Properties getCarrierProperties(String carrier) {
        if (!requestCache.containsKey("CARRIER_PROPERTIES_" + carrier)) requestCache.put("CARRIER_PROPERTIES_" + carrier, getDao().getCarrierProperties(carrier));
        return (Properties) requestCache.get("CARRIER_PROPERTIES_" + carrier);
    }

    public String getPaymentMethodName(String paymentMethod) {
        MerchantUtils mu = new MerchantUtils(getServletContext());
        MerchantService ms = mu.getService(paymentMethod, this);
        if (ms != null) {
            return getText(ms.getLabel(), paymentMethod);
        }
        return paymentMethod;
    }

    public String getCountryName(String code) {
        return CountryFactory.getCountryName(code, getLocale());
    }

    public DefaultMutableTreeNode getTreeMenu(String menu) {
        DefaultMutableTreeNode menuTree = new DefaultMutableTreeNode();
        dao.fillMenuNodeChilds(menuTree, menu);
        return menuTree;
    }

    public DefaultMutableTreeNode getTreeCategory() {
        if (!requestCache.containsKey("CATEGORY_TREE")) {
            DefaultMutableTreeNode node = new DefaultMutableTreeNode();
            dao.fillCategoryNodeChilds(node);
            requestCache.put("CATEGORY_TREE", node);
        }
        return (DefaultMutableTreeNode) requestCache.get("CATEGORY_TREE");
    }

    public List<Category> getCategoryHierarchy(Long id) {
        Category bean = dao.getCategory(id);
        return dao.getCategoryHierarchy(bean);
    }

    public List<Long> getCategoryHierarchyId(Long id) {
        Category bean = dao.getCategory(id);
        return dao.getCategoryHierarchyId(bean);
    }

    public Category getCategory(Object obj) {
        Long id = null;
        if (obj instanceof Number) id = ((Number) obj).longValue();
        else if (obj instanceof String) id = SomeUtils.strToLong((String) obj);
        return dao.getCategory(id);
    }

    public boolean containsCategory(List<Category> l, Object o) {
        if (o == null || l == null) return false;
        if (o instanceof Category) return l.contains(o);
        if (o instanceof Long) for (Category c : l) if (c.getIdCategory().equals(o)) return true;
        if (o instanceof CategoryDTO) for (Category c : l) if (c.getIdCategory().equals(((CategoryDTO) o).getIdCategory())) return true;
        return false;
    }

    public List<Category> getCategoryHierarchy(Category bean) {
        return dao.getCategoryHierarchy(bean);
    }

    public List<Category> getCategoryChildren(Category bean) {
        return dao.getAllChildCategories(bean, true);
    }

    public List<CategoryDTO> getSubCategories(Object obj, boolean children) {
        Long idCategory = null;
        if (obj == null) idCategory = null;
        else if (obj instanceof String) idCategory = SomeUtils.strToLong((String) obj);
        else if (obj instanceof Number) idCategory = ((Number) obj).longValue();
        else if (obj instanceof Category) idCategory = ((Category) obj).getIdCategory();
        else if (obj instanceof CategoryDTO) idCategory = ((CategoryDTO) obj).getIdCategory();
        return dao.getSubcategories(idCategory, getLocale().getLanguage(), null, children);
    }

    public List sortCategories(List list) {
        if (list != null) Collections.sort(list, new CategoryNameComparator(getLocale().getLanguage()));
        return list;
    }

    class CategoryNameComparator implements Comparator {
        private String lang;

        CategoryNameComparator(String lang) {
            this.lang = lang;
        }

        public int compare(Object o1, Object o2) {
            String cad1 = "";
            Integer pos1 = CategoryDTO.LAST_POSITION;
            if (o1 != null && o1 instanceof Category) cad1 = ((Category) o1).getCategoryName(lang);
            else if (o1 != null && o1 instanceof CategoryDTO) {
                cad1 = ((CategoryDTO) o1).getName();
                pos1 = ((CategoryDTO) o1).getPosition();
            }

            String cad2 = "";
            Integer pos2 = CategoryDTO.LAST_POSITION;
            if (o2 != null && o2 instanceof Category) cad2 = ((Category) o2).getCategoryName(lang);
            else if (o2 != null && o2 instanceof CategoryDTO) {
                cad2 = ((CategoryDTO) o2).getName();
                pos2 = ((CategoryDTO) o2).getPosition();
            }

            int result = pos1.compareTo(pos2);
            return (result != 0) ? result : ((cad1 != null && cad2 != null) ? cad1.compareTo(cad2) : 0);
        }
    }

    public Double toActualCurrency(Double val, Currency curr) {
        return (val != null && curr != null && actualCurrency != null) ? val * curr.getRatio() : null;
    }

    public Double fromCurrency(Double val, Currency curr) {
        return (val != null && curr != null && actualCurrency != null) ? val * curr.getReverseRatio() : null;
    }

    public Double toActualCurrency(Double val) {
        return toActualCurrency(val, getActualCurrency());
    }

    public String formatActualCurrency(Number val) {
        return formatActualCurrency(val, getActualCurrency());
    }

    public String formatActualCurrency(Number val, Currency curr) {
        return curr.formatValue((val != null) ? toActualCurrency(val.doubleValue(), curr) : 0d);
    }

    public boolean productInWishList(Long idProduct) {
        if (idProduct != null && getFrontUser() != null && getFrontUser().getWishList() != null && getFrontUser().getWishList().size() > 0) {
            for (UserWishList w : getFrontUser().getWishList())
                if (idProduct.equals(w.getIdProduct())) return true;
        }
        return false;
    }

    public void auditProductStock(Product bean) {
        Long lastStock = dao.getLastAuditStock(bean);
        auditStock(bean, "Updated in admin module");
        if (lastStock != null && bean.getStock() != null && !lastStock.equals(bean.getStock())) {
            if (lastStock == 0 && bean.getStock() > 0) generateCustomerStockAlert(bean);
        }
        Long stockMin = SomeUtils.strToLong(getParentProperty(bean, "stockMin"));
        if (stockMin != null && bean.getStock() <= stockMin) generateAdminStockAlert(bean);
    }

    public void generateCustomerStockAlert(Product p) {
        List<User> l = dao.getUsersForStockAlert(p);
        Map map = new HashMap();
        map.put("product", new MProduct(p, this));
        Map<String, String> bodies = proccessVelocityTemplateLanguages(Mail.MAIL_TEMPLATE_USER_STOCK_ALERT, map);
        if (l != null && l.size() > 0) {
            String subject = getText(CNT_SUBJECT_STOCK_ALERT, CNT_DEFAULT_SUBJECT_STOCK_ALERT);
            for (User u : l)
                if (StringUtils.isNotEmpty(u.getEmail())) {
                    String lang = (StringUtils.isNotEmpty(u.getLanguage()) && bodies.containsKey((u.getLanguage()))) ? u.getLanguage() : getDefaultLanguage();
                    Mail mail = new Mail();
                    mail.setInventaryCode(getStoreCode());
                    mail.setBody(bodies.get(lang));
                    mail.setSubject(subject);
                    mail.setToAddress(u.getEmail());
                    mail.setReference("CUSTOMER STOCK ALERT " + p.getIdProduct());
                    mail.setPriority(Mail.PRIORITY_MEDIUM);
                    dao.save(mail);
                }
        }
    }

    public void generateAdminStockAlert(Product p) {
        String email = getStoreProperty(StoreProperty.PROP_MAIL_STOCK_ALERT, null);
        if (StringUtils.isNotEmpty(email)) {
            if (p != null) {
                Map map = new HashMap();
                map.put("product", new MProduct(p, this));
                String body = proccessVelocityTemplate(Mail.MAIL_TEMPLATE_ADMIN_STOCK_ALERT, map);
                Mail mail = new Mail();
                mail.setInventaryCode(getStoreCode());
                mail.setBody(body);
                mail.setSubject(getText(CNT_SUBJECT_ADMIN_STOCK_ALERT, CNT_DEFAULT_SUBJECT_ADMIN_STOCK_ALERT));
                mail.setToAddress(email);
                mail.setPriority(Mail.PRIORITY_HIGH);
                mail.setReference("ADMIN STOCK ALERT " + p.getIdProduct());
                mailSaveAndSend(mail);
            }
        }
    }

    public void generateUserRequestLevelEmail(User u) {
        String email = getStoreProperty(StoreProperty.PROP_USER_REQUEST_LEVEL_EMAIL, null);
        if (StringUtils.isNotEmpty(email) && u != null) {
            Map map = new HashMap();
            map.put("user", new MUser(u, this));
            String body = proccessVelocityTemplate(Mail.MAIL_TEMPLATE_USER_REQUEST_LEVEL, map);
            Mail mail = new Mail();
            mail.setInventaryCode(getStoreCode());
            mail.setBody(body);
            mail.setSubject(getText(CNT_SUBJECT_USER_REQUEST_LEVEL, CNT_DEFAULT_SUBJECT_USER_REQUEST_LEVEL));
            mail.setToAddress(email);
            mail.setPriority(Mail.PRIORITY_HIGH);
            mail.setReference("USER REQUEST LEVEL " + u.getIdUser());
            mailSaveAndSend(mail);
        }
    }

    public void sendWelcomeMail(User user, String password) {
        // Send welcome email
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("user", new MUser(user, this));
        map.put("password", password);
        String body = proccessVelocityTemplate(Mail.MAIL_TEMPLATE_USER_WELCOME, map);
        Mail mail = new Mail();
        mail.setInventaryCode(getStoreCode());
        mail.setBody(body);
        mail.setSubject(getText(CNT_SUBJECT_WELCOME, CNT_DEFAULT_SUBJECT_WELCOME));
        mail.setToAddress(user.getEmail());
        mail.setPriority(Mail.PRIORITY_HIGH);
        mail.setReference("WELCOME " + user.getIdUser());
        mailSaveAndSend(mail);
    }

    // orders

    public void recoverOrderStock(Order order) {
        if (order != null) {
            for (OrderDetail detail : order.getOrderDetails()) {
                for (OrderDetailProduct odp : detail.getOrderDetailProducts()) {
                    odp.getProduct().addStock(detail.getQuantity());
                    odp.getProduct().addSales(-detail.getQuantity());
                    dao.save(odp.getProduct());
                    ProductVariation var = (ProductVariation) dao.get(ProductVariation.class, odp.getIdVariation());
                    if (var != null) {
                        var.addStock(detail.getQuantity().longValue());
                        dao.save(var);
                    }
                }
            }
        }
    }

    public void recoverOrderReward(Order order) {
        if (order != null && order.getTotalRewards() != null && order.getTotalRewards() > 0) {
            Number rewardsRate = (Number) getRequest().getAttribute("rewardsRate");
            if (rewardsRate != null && rewardsRate.doubleValue() > 0) {
                int rewardsUsedPoints = (int) Math.ceil(order.getTotalRewards() / rewardsRate.doubleValue());
                Long rewardPoints = order.getUser().getRewardPoints();
                order.getUser().setRewardPoints((rewardPoints != null) ? rewardPoints + rewardsUsedPoints : rewardsUsedPoints);
                dao.save(order.getUser());
                addRewardHistory(order.getUser(), ((Number) rewardsUsedPoints).doubleValue(), order, null, UserRewardHistory.TYPE_PURCHASE_CANCELLATION, "");
            }
        }
    }

    public OrderHistory addOrderHistory(Order order, User user, String comment) {
        OrderHistory bean = null;
        if (order != null && user != null) {
            bean = new OrderHistory();
            bean.setHistoryComment(comment);
            bean.setHistoryDate(SomeUtils.today());
            bean.setOrder(order);
            bean.setHistoryStatus(order.getStatus());
            bean.setUser(user);
            dao.save(bean);
            if (order.getOrderHistory() == null) order.setOrderHistory(new ArrayList<OrderHistory>());
            order.getOrderHistory().add(bean);
        }
        return bean;
    }

    public void sendOrderStatusMail(Order order) {
        if (order != null && order.getUser() != null && StringUtils.isNotEmpty(order.getUser().getEmail())) {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("order", new MOrder(order, this));
            map.put("user", new MUser(order.getUser(), this));
            String body = proccessVelocityTemplate(Mail.MAIL_TEMPLATE_ORDER_STATUS, map);
            Mail mail = new Mail();
            mail.setInventaryCode(getStoreCode());
            mail.setBody(body);
            mail.setSubject(getText(CNT_SUBJECT_ORDER_STATUS, CNT_DEFAULT_SUBJECT_ORDER_STATUS, new String[]{order.getIdOrder().toString(), order.getStatus().getStatusName(getLocale().getLanguage())}));
            mail.setToAddress(order.getUser().getEmail());
            mail.setPriority(10);
            mail.setReference("ORDER STATUS " + order.getIdOrder());
            mailSaveAndSend(mail);
        }
    }

    public List<String> getRmaStatuses() {
        return dao.getRmaStatus(true);
    }

    public <K, V extends Comparable<V>> Map<K, V> sortMapByValues(final Map<K, V> map) {
        if (map == null) return null;
        Comparator<K> valueComparator = new Comparator<K>() {
            public int compare(K k1, K k2) {
                int compare = map.get(k2).compareTo(map.get(k1));
                if (compare == 0) return 1;
                else return compare;
            }
        };
        Map<K, V> sortedByValues = new TreeMap<K, V>(valueComparator);
        sortedByValues.putAll(map);
        return sortedByValues;
    }

    public String getOrderInvoice(Order order) {
        if (order == null) return "";
        if ("Y".equalsIgnoreCase(getStoreProperty(StoreProperty.PROP_INVOICE_NUMBER_AUTOMATIC, "N"))) {
            String format = getStoreProperty(StoreProperty.PROP_INVOICE_NUMBER_PREFIX, "#");
            NumberFormat nf = new DecimalFormat(format);
            return (order.getInvoiceConsecutive() != null) ? nf.format(order.getInvoiceConsecutive()) : "";
        } else {
            return order.getInvoiceNo();
        }
    }

    public void addRewardHistory(User referral, Double amount, Order order, User friend, String type, String s) {
        UserRewardHistory urh = new UserRewardHistory();
        urh.setAmount(amount);
        if (order != null) urh.setIdOrder(order.getIdOrder());
        urh.setFriend(friend);
        urh.setUser(referral);
        urh.setType(type);
        urh.setMessage(s);
        urh.setStatus(UserRewardHistory.STATUS_COMPLETE);
        urh.setCreated(Calendar.getInstance().getTime());
        dao.save(urh);
    }

    /**
     * Metodo que informa que tipo de
     * browser esta utilizando el cliente
     *
     * @param request
     * @return
     */
    public static String getBrowser(HttpServletRequest request) {
        String browserType = request.getHeader("User-Agent");
        String browser = "";
        String version = "";
        browserType = browserType.toLowerCase();

        if (browserType != null) {
            if ((browserType.indexOf("msie") != -1)) {
                browser = "IExplorer";

                String tempStr = browserType.substring(browserType.indexOf("msie"), browserType.length());
                version = tempStr.substring(4, tempStr.indexOf(";"));
            }

            if ((browserType.indexOf("mozilla") != -1) && (browserType.indexOf("spoofer") == -1) && (browserType.indexOf("compatible") == -1)) {
                if (browserType.indexOf("firefox") != -1) {
                    browser = "Firefox";

                    int verPos = browserType.indexOf("/");

                    if (verPos != -1) {
                        version = browserType.substring(verPos + 1, verPos + 5);
                    }
                } else if (browserType.indexOf("netscape") != -1) {
                    browser = "Netscape";

                    int verPos = browserType.indexOf("/");

                    if (verPos != -1) {
                        version = browserType.substring(verPos + 1, verPos + 5);
                    }
                } else {
                    browser = "Mozilla";

                    int verPos = browserType.indexOf("/");

                    if (verPos != -1) {
                        version = browserType.substring(verPos + 1, verPos + 5);
                    }
                }
            }

            if (browserType.indexOf("opera") != -1) {
                browser = "Opera";
            }

            if (browserType.indexOf("safari") != -1) {
                browser = "Safari";
            }

            if (browserType.indexOf("konqueror") != -1) {
                browser = "Konqueror";
            }
        }

        return browser + "-" + version;
    }

    // Static Text

    public StaticText getStaticText(String code, String type) {
        return dao.getStaticText(code, type);
    }

    // banners

    public Banner getBanner(Number id) {
        return (Banner) dao.get(Banner.class, id.longValue());
    }

    public List<Banner> getBanners(String zone) {
        return dao.getBanners(zone);
    }

    public List<Banner> getRandomBanners(String zone, int cant) {
        return dao.getRandomBanners(zone, null, null, cant);
    }

    public List<Banner> getRandomBanners(String zone, int cant, Category category) {
        List<Banner> res = new ArrayList<Banner>();
        if (category != null) {
            while (category != null && res.size() < cant) {
                List<Banner> lc = dao.getRandomBanners(zone, category, null, cant - res.size());
                if (lc != null) res.addAll(lc);
                category = (category.getIdParent() != null) ? dao.getCategory(category.getIdParent()) : null;
            }
        }
        List<Banner> lc = dao.getRandomBanners(zone, null, null, cant - res.size());
        if (lc != null) res.addAll(lc);
        return res;
    }

    public Map getBannerSlider(String bannerZone) {
        String data = dao.getStorePropertyValue(bannerZone, SLIDER_CONFIG, null);
        if (StringUtils.isNotEmpty(data)) {
            try {
                Map map = (Map) JSONUtil.deserialize(data);
                if (map.containsKey("items") && map.get("items") != null && map.get("items") instanceof List && !((List) map.get("items")).isEmpty()) return map;
            } catch (JSONException e) {
                log.error(e.getMessage(), e);
            }
        }
        return null;
    }

    public List<UserComment> getComments(String type, int quantity) {
        DataNavigator nav = new DataNavigator("comments");
        nav.setPageRows(quantity);
        return dao.getComments(nav, type, UserComment.STATUS_ACTIVE, null, null, null);
    }

    // Metas

    public String getProductMeta(Product p, String metaName, String lang) {
        if (p != null) {
            PageMeta meta = p.getProductMeta(metaName, lang);
            String prodMeta = (meta != null) ? meta.getMetaValue() : "";
            if ((meta == null || meta.getAppendParent()) && p.getCategory() != null) {
                String catMeta = getCategoryMeta(p.getCategory(), metaName, lang);
                prodMeta = ((StringUtils.isNotEmpty(catMeta)) ? catMeta : "") + (StringUtils.isNotEmpty(catMeta) && StringUtils.isNotEmpty(prodMeta) ? " - " : "") + prodMeta;
            }
            return prodMeta;
        }
        return "";
    }

    public String getCategoryMeta(Category c, String metaName, String lang) {
        if (c != null) {
            PageMeta meta = c.getCategoryMeta(metaName, lang);
            String cadMeta = (meta != null) ? meta.getMetaValue() : "";
            if ((meta == null || meta.getAppendParent())) {
                Category parCat = dao.getCategory(c.getIdParent());
                if (parCat != null) {
                    String parMeta = getCategoryMeta(parCat, metaName, lang);
                    cadMeta = ((StringUtils.isNotEmpty(parMeta)) ? parMeta : "") + (StringUtils.isNotEmpty(parMeta) && StringUtils.isNotEmpty(cadMeta) ? " - " : "") + cadMeta;
                } else {
                    String storeMeta = getStoreMeta(metaName, lang);
                    cadMeta = ((StringUtils.isNotEmpty(storeMeta)) ? storeMeta : "") + (StringUtils.isNotEmpty(storeMeta) && StringUtils.isNotEmpty(cadMeta) ? " - " : "") + cadMeta;
                }
            }
            return cadMeta;
        }
        return "";
    }

    public String getStoreMeta(String metaName, String lang) {
        StoreProperty bean = dao.getStoreProperty("seo." + metaName, StoreProperty.TYPE_GENERAL);
        return (bean != null) ? bean.getValue(lang) : "";
    }


    public String extractText(String s) {
        return SomeUtils.extractText(s);
    }

    public String extractKeywords(String text) {
        return SomeUtils.extractKeywords(text);
    }

    public String proccessVelocityTemplate(String location) {
        return proccessVelocityTemplate(location, null);
    }

    public String proccessVelocityTemplate(String location, Map<String, Object> map) {
        String res = null;
        if (StringUtils.isNotEmpty(location))
            try {
                velocityManager.init(servletContext);
                if (!location.startsWith("/")) location = ServletActionContext.getActionContext(request).getActionInvocation().getProxy().getNamespace() + "/" + location;
                Template template = velocityManager.getVelocityEngine().getTemplate(location);
                if (template != null) {
                    ValueStack stack = ActionContext.getContext().getValueStack();
                    Context ctx = velocityManager.createContext(stack, request, response);
                    if (map != null) {
                        for (String key : map.keySet()) {
                            Object value = map.get(key);
                            if (value != null) ctx.put(key, map.get(key));
                            else if (ctx.containsKey(key)) ctx.remove(key);
                        }
                    }
                    StringWriter writer = new StringWriter();
                    template.merge(ctx, writer);
                    res = writer.toString();
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }

        return res;
    }

    public String proccessVelocityText(String text, Map<String, Object> map) {
        String res = null;
        try {
            velocityManager.init(servletContext);
            ValueStack stack = ActionContext.getContext().getValueStack();
            Context ctx = velocityManager.createContext(stack, request, response);
            if (map != null) {
                for (String key : map.keySet()) {
                    ctx.put(key, map.get(key));
                }
            }
            StringWriter writer = new StringWriter();
            velocityManager.getVelocityEngine().evaluate(ctx, writer, "testing", text);
            res = writer.toString();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        return res;
    }

    public Map<String, String> proccessVelocityTemplateLanguages(String location, Map<String, Object> map) {
        Map<String, String> res = new HashMap<String, String>();
        if (map == null) map = new HashMap<String, Object>();
        for (String l : getLanguages()) {
            map.put("language", l);
            res.put(l, proccessVelocityTemplate(location, map));
        }
        return res;
    }

    public String urlCategory(CategoryDTO c) {
        if (c == null) return null;
        if ("Y".equalsIgnoreCase(getStoreProperty(StoreProperty.PROP_USE_FRIENDLY_URLS, StoreProperty.PROP_DEFAULT_USE_FRIENDLY_URLS)) && StringUtils.isNotEmpty(c.getUrlCode())) {
            return urlFriendly("category", "", c.getUrlCode(), false);
        } else {
            Map<String, String> map = new HashMap<String, String>();
            if (StringUtils.isNotEmpty(c.getUrlCode())) map.put("code", c.getUrlCode());
            else map.put("idCategory", c.getIdCategory().toString());
            return url("category", "", map);
        }
    }

    public String urlCategory(Category c) {
        if (c == null) return null;
        if ("Y".equalsIgnoreCase(getStoreProperty(StoreProperty.PROP_USE_FRIENDLY_URLS, StoreProperty.PROP_DEFAULT_USE_FRIENDLY_URLS)) && StringUtils.isNotEmpty(c.getUrlCode())) {
            return urlFriendly("category", "", c.getUrlCode(), false);
        } else {
            Map<String, String> map = new HashMap<String, String>();
            if (StringUtils.isNotEmpty(c.getUrlCode())) map.put("code", c.getUrlCode());
            else map.put("idCategory", c.getIdCategory().toString());
            return url("category", "", map);
        }
    }

    public String urlManufacturer(Manufacturer m) {
        if (m == null) return null;
        if ("Y".equalsIgnoreCase(getStoreProperty(StoreProperty.PROP_USE_FRIENDLY_URLS, StoreProperty.PROP_DEFAULT_USE_FRIENDLY_URLS)) && StringUtils.isNotEmpty(m.getUrlCode())) {
            return urlFriendly("manufacturer", "", m.getUrlCode(), false);
        } else {
            Map<String, String> map = new HashMap<String, String>();
            if (StringUtils.isNotEmpty(m.getUrlCode())) map.put("manufacturer", m.getUrlCode());
            else map.put("manufacturer", m.getIdManufacturer().toString());
            return url("category", "", map);
        }
    }

    public String urlLabel(ProductLabel l) {
        if (l == null) return null;
        return urlLabel(l.getCode());
    }

    public String urlLabel(String labelCode) {
        if ("Y".equalsIgnoreCase(getStoreProperty(StoreProperty.PROP_USE_FRIENDLY_URLS, StoreProperty.PROP_DEFAULT_USE_FRIENDLY_URLS)) && StringUtils.isNotEmpty(labelCode)) {
            return urlFriendly("label", "", labelCode, false);
        } else {
            Map<String, String> map = new HashMap<String, String>();
            map.put("label", labelCode);
            return url("category", "", map);
        }
    }

    public String urlProduct(Product p) {
        return urlProduct(p, false);
    }

    public String urlProduct(Product p, boolean includeDomain) {
        if (p == null) return null;
        if ("Y".equalsIgnoreCase(getStoreProperty(StoreProperty.PROP_USE_FRIENDLY_URLS, StoreProperty.PROP_DEFAULT_USE_FRIENDLY_URLS)) && StringUtils.isNotEmpty(p.getUrlCode())) {
            return urlFriendly("product", "", p.getUrlCode(), includeDomain);
        } else {
            Map<String, String> map = new HashMap<String, String>();
            if (StringUtils.isNotEmpty(p.getUrlCode())) map.put("code", p.getUrlCode());
            else map.put("idProduct", p.getIdProduct().toString());
            return url("product", "", map, includeDomain);
        }
    }

    public String urlProductImage(Product p) {
        return urlProductImage(p, false);
    }

    public String urlProductImage(Product p, boolean includeDomain) {
        Map<String, String> map = new HashMap<String, String>();
        map.put("idProduct", p.getIdProduct().toString());
        return url("productImage", "", map, includeDomain);
    }

    public String urlNews(StaticText p) {
        if (p == null) return null;
        if (StringUtils.isNotEmpty(p.getContentUrl())) return p.getContentUrl();
        return urlPage(p, false);
    }

    public String urlPage(StaticText p) {
        if (p == null) return null;
        return urlPage(p, false);
    }

    public String urlPage(StaticText p, boolean includeDomain) {
        if (p == null || p.getId() == null) return null;
        if ("Y".equalsIgnoreCase(getStoreProperty(StoreProperty.PROP_USE_FRIENDLY_URLS, StoreProperty.PROP_DEFAULT_USE_FRIENDLY_URLS)) && StringUtils.isNotEmpty(p.getUrlCode())) {
            return urlFriendly("page", "", p.getUrlCode(), includeDomain);
        } else {
            Map<String, String> map = new HashMap<String, String>();
            if (StringUtils.isNotEmpty(p.getUrlCode())) map.put("code", p.getUrlCode());
            else map.put("idStaticText", p.getId().toString());
            return url("page", "", map, includeDomain);
        }
    }

    public String urlEnc(String url) throws Exception {
        if (StringUtils.isNotEmpty(url)) {
            String encodeUrl = SomeUtils.encrypt3Des(url, getEncryptionKey());
            if (StringUtils.isNotEmpty(encodeUrl)) {
                Map<String, String> map = new HashMap<String, String>();
                map.put("code", encodeUrl);
                return url("enc", "", map, true);
            }
        }
        return "";
    }

    public String url(String action) {
        return url(action, null, null, false);
    }

    public String url(String action, String namespace) {
        return url(action, namespace, null, false);
    }

    public String url(String action, String namespace, Map<String, String> params) {
        return url(action, namespace, params, false);
    }

    public StoreActionMapping getStoreActionMapping() {
        ActionContext context = ActionContext.getContext();
        return (context != null) ? (StoreActionMapping) context.get(ServletActionContext.ACTION_MAPPING) : null;
    }

    public String url(String action, String namespace, Map<String, String> params, boolean includeDomain) {
        ActionContext context = ActionContext.getContext();
        StringBuilder buff = new StringBuilder();
        if (context != null) {
            StoreActionMapping orig = (StoreActionMapping) context.get(ServletActionContext.ACTION_MAPPING);
            if (includeDomain) {
                buff.append(request.getRequestURL().toString().toLowerCase().startsWith("https") ? "https://" : "http://")
                        .append(request.getHeader("Host")).append(request.getContextPath());
            }

            if (namespace == null) namespace = (orig.getNamespace().startsWith("/")) ? orig.getNamespace().substring(1) : orig.getNamespace();
            if (StringUtils.isNotEmpty(orig.getStore())) {
                if (!orig.getStore().startsWith("/")) buff.append("/");
                buff.append(orig.getStore());
            }
            if (StringUtils.isNotEmpty(namespace)) {
                if (!namespace.startsWith("/")) buff.append("/");
                buff.append(namespace);
            }
            if (!action.startsWith("/")) buff.append("/");
            buff.append(action);
            buff.append(".").append(orig.getExtension());

            if (params != null && !params.isEmpty()) {
                StringBuilder parameters = new StringBuilder();
                for (String key : params.keySet()) {
                    try {
                        if (StringUtils.isEmpty(parameters.toString())) parameters.append("?");
                        else parameters.append("&");
                        parameters.append(key).append("=").append(URLEncoder.encode(String.valueOf(params.get(key)), "UTF-8"));
                    } catch (UnsupportedEncodingException e) {
                        log.error(e.getMessage(), e);
                    }
                }
                buff.append(parameters);
            }
        }
        return buff.toString();
    }

    public String urlFriendly(String action, String namespace, String code, boolean includeDomain) {
        ActionContext context = ActionContext.getContext();
        StringBuilder buff = new StringBuilder();
        if (context != null) {
            if (includeDomain) {
                buff.append(request.getRequestURL().toString().toLowerCase().startsWith("https") ? "https://" : "http://")
                        .append(request.getHeader("Host")).append(request.getContextPath());
            }

            StoreActionMapping orig = (StoreActionMapping) context.get(ServletActionContext.ACTION_MAPPING);
            if (namespace == null) namespace = (orig.getNamespace().startsWith("/")) ? orig.getNamespace().substring(1) : orig.getNamespace();
            if (StringUtils.isNotEmpty(orig.getStore())) {
                if (!orig.getStore().startsWith("/")) buff.append("/");
                buff.append(orig.getStore());
            }
            if (StringUtils.isNotEmpty(namespace)) {
                if (!namespace.startsWith("/")) buff.append("/");
                buff.append(namespace);
            }
            if (!action.startsWith("/")) buff.append("/");
            buff.append(action);
            buff.append("/");

            if (StringUtils.isNotEmpty(code)) buff.append(code);
        }
        return buff.toString();
    }

    public String getActionUrl(String actionName, Map<String, String> params) {
        return url(actionName, "", params, true);
    }

    public String getStoreCodeByMapping(String domain, String path) {
        for (Map.Entry<String, Store20Commerce> entry : Store20Config.getInstance(getServletContext()).getStoreMap().entrySet()) {
            if (entry.getValue().hasUrl(domain, path)) return entry.getKey();
        }
        return null;
    }

    public String getText(String key) {
        return super.getText(key, key);
    }

    public String getBaseUrl() {
        ActionContext context = ActionContext.getContext();
        StringBuilder buff = new StringBuilder();
        if (context != null) {
            buff.append(request.getRequestURL().toString().toLowerCase().startsWith("https") ? "https://" : "http://")
                    .append(request.getHeader("Host")).append(request.getContextPath());

            StoreActionMapping orig = (StoreActionMapping) context.get(ServletActionContext.ACTION_MAPPING);
            if (StringUtils.isNotEmpty(orig.getStore())) {
                if (!orig.getStore().startsWith("/")) buff.append("/");
                buff.append(orig.getStore());
            }
            buff.append("/");
        }
        return buff.toString();
    }

    public String getThisUrl() {
        StringBuilder b = new StringBuilder(request.getRequestURL());
        if (StringUtils.isNotEmpty(request.getQueryString())) b.append("?").append(request.getQueryString());
        return b.toString();
    }

    public String skinFile(String file) {
        if (file.contains("[lang]")) file = StringUtils.replace(file, "[lang]", getLocale().getLanguage());
        return "/templates/" + getTemplate() + "/skins/" + getSkin() + (file.startsWith("/") ? file : "/" + file);
    }

    public String storeFile(String file) {
        if (file.contains("[lang]")) file = StringUtils.replace(file, "[lang]", getLocale().getLanguage());
        return "/stores/" + getStoreCode() + (file.startsWith("/") ? file : "/" + file);
    }

    public void addFlash(String type, String value) {
        if (getStoreSessionObjects().containsKey(type) && getStoreSessionObjects().get(type) instanceof List) {
            ((List) getStoreSessionObjects().get(type)).add(value);
        } else {
            List l = new ArrayList();
            l.add(value);
            getStoreSessionObjects().put(type, l);
        }
    }

    public List getFlash(String type) {
        List l = new ArrayList();
        if (getStoreSessionObjects().containsKey(type) && getStoreSessionObjects().get(type) instanceof List) {
            l.addAll((List) getStoreSessionObjects().get(type));
            getStoreSessionObjects().remove(type);
        }
        return l;
    }

    public String getActionName() {
        return ServletActionContext.getActionMapping().getName();
    }

    public String getMailGlobalTop() {
        return proccessVelocityTemplate("/WEB-INF/views/" + getTemplate() + "/mails/global_top.vm");
    }

    public String getMailGlobalBottom() {
        return proccessVelocityTemplate("/WEB-INF/views/" + getTemplate() + "/mails/global_bot.vm");
    }

    public String mailBlock(String block) {
        return proccessVelocityTemplate("/WEB-INF/views/" + getTemplate() + "/mails/" + block + ".vm");
    }

    public void mailSaveAndSend(Mail mail) {
        try {
            Session hSession = HibernateSessionFactory.getSessionAutoCommit(getDatabaseConfig());
            hSession.saveOrUpdate(mail);
            hSession.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        MailSenderThreat.asyncSendMail(mail, this);
    }

}
