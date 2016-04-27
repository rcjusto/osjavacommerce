package org.store.config;

import com.opensymphony.xwork2.ActionSupport;
import com.opensymphony.xwork2.inject.Inject;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.store.core.beans.Currency;
import org.store.core.beans.*;
import org.store.core.dao.HibernateDAO;
import org.store.core.globals.CountryFactory;
import org.store.core.globals.config.Store20Commerce;
import org.store.core.globals.config.Store20Config;
import org.store.core.globals.config.Store20Database;
import org.store.core.hibernate.HibernateSessionFactory;
import org.store.core.utils.templates.TemplateUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.struts2.ServletActionContext;
import org.apache.struts2.util.ClassLoaderUtils;
import org.apache.struts2.views.velocity.VelocityManager;
import org.geonames.*;
import org.hibernate.HibernateException;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.impl.SessionFactoryImpl;
import org.hibernate.persister.entity.SingleTableEntityPersister;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.*;

@Namespace(value = "/config")
@ParentPackage(value = "store-config")
public class ConfigAction extends ActionSupport {
    public static Logger log = Logger.getLogger(ConfigAction.class);

    private VelocityManager velocityManager;
    private static final String CNT_USER = "superadmin";
    private static final String CNT_PASSWORD = "saso1234";


    @Inject
    public void setVelocityManager(VelocityManager mgr) {
        this.velocityManager = mgr;
    }

    @Action(value = "login", results = {
            @Result(type = "velocity", name = "error", location = "/org/store/config/views/login.vm"),
            @Result(type = "redirectAction", location = "home")
    })
    public String login() throws Exception {
        if (StringUtils.isNotEmpty(user)) {
            if (CNT_USER.equals(user) && CNT_PASSWORD.equals(password)) {
                SecurityInterceptor.loginUser(user);
                return  SUCCESS;
            } else {
                addActionError("Invalid User or Password");
                return "error";
            }
        }  else {
            return "error";
        }
    }

    @Action(value = "logout", results = {
            @Result(type = "redirectAction", location = "home")
    })
    public String logout() throws Exception {
        SecurityInterceptor.logoutUser();
        return SUCCESS;
    }

    @Action(value = "home", results = {
            @Result(type = "velocity", location = "/org/store/config/views/home.vm")
    })
    public String home() throws Exception {
        listDatabase();
        listCommerce();
        return SUCCESS;
    }

    @Action(value = "reloadConfig", results = {
            @Result(type = "redirectAction", location = "home")
    })
    public String reloadConfig() throws Exception {
        Store20Config.getInstance(ServletActionContext.getServletContext()).loadConfigurationFile(ServletActionContext.getServletContext());
        return SUCCESS;
    }

    @Action(value = "reloadBakConfig", results = {
            @Result(type = "redirectAction", location = "home")
    })
    public String reloadBakConfig() throws Exception {
        File file = new File(ServletActionContext.getServletContext().getRealPath(Store20Config.CONFIG_FILE));
        File fileBak = new File(ServletActionContext.getServletContext().getRealPath(Store20Config.CONFIG_FILE_BAK));

        if (fileBak.exists()) {
            if (file.exists()) file.delete();
            fileBak.renameTo(file);
        }

        Store20Config.getInstance(ServletActionContext.getServletContext()).loadConfigurationFile(ServletActionContext.getServletContext());
        return SUCCESS;
    }

    @Action(value = "listDatabase", results = {
            @Result(type = "velocity", location = "/org/store/config/views/block_databases.vm")
    })
    public String listDatabase() throws Exception {
        ServletActionContext.getContext().put("databases", Store20Config.getInstance(ServletActionContext.getServletContext()).getStoreDb());
        return SUCCESS;
    }

    @Action(value = "editDatabase", results = {
            @Result(type = "velocity", location = "/org/store/config/views/block_editdatabase.vm")
    })
    public String editDatabase() throws Exception {
        setNuevo("S");
        if (StringUtils.isNotEmpty(id)) {
            Map<String, Store20Database> map = Store20Config.getInstance(ServletActionContext.getServletContext()).getStoreDb();
            if (map != null && map.containsKey(id)) {
                Store20Database database = map.get(id);
                setId(database.getId());
                setDbType(database.getType());
                setDbUrl(database.getUrl());
                setDbUser(database.getUser());
                setDbPassword(database.getPassword());
                setNuevo("N");
            }
        }
        return SUCCESS;
    }

    @Action(value = "delDatabase", results = {
            @Result(type = "velocity", name = "input", location = "/org/store/config/views/block_databases.vm"),
            @Result(type = "redirectAction", location = "listDatabase")
    })
    public String delDatabase() throws Exception {
        if (StringUtils.isNotEmpty(id)) {
            Map<String, Store20Commerce> map = Store20Config.getInstance(ServletActionContext.getServletContext()).getStoreMap();
            for (Store20Commerce commerce : map.values()) {
                if (id.equalsIgnoreCase(commerce.getDatabase())) {
                    addActionError("Can not delete database '" + id + "'. It is used in some commerces.");
                    listDatabase();
                    return INPUT;
                }
            }
            Store20Config.getInstance(ServletActionContext.getServletContext()).getStoreDb().remove(id);
            Store20Config.getInstance(ServletActionContext.getServletContext()).saveConfigurationFile(ServletActionContext.getServletContext());
        }
        return SUCCESS;
    }

    @Action(value = "saveDatabase", results = {
            @Result(type = "velocity", name = "input", location = "/org/store/config/views/block_editdatabase.vm"),
            @Result(type = "redirectAction", location = "listDatabase")
    })
    public String saveDatabase() throws Exception {

        if (StringUtils.isEmpty(id)) addActionError("Database Id is required");
        if (StringUtils.isEmpty(dbType)) addActionError("Database Type is required");
        if (StringUtils.isEmpty(dbUrl)) addActionError("Database URL is required");
        if (StringUtils.isEmpty(dbUser)) addActionError("Database Username is required");
        if (hasActionErrors()) {
            return INPUT;
        }

        // Try to connect to database
        try {
            Class.forName(getDatabaseDriver(dbType)).newInstance();
        } catch (InstantiationException e) {
            addActionError("Database type '" + dbType + "' not supported. JDBC Driver Error.");
            log.error(e.getMessage(), e); 
        } catch (IllegalAccessException e) {
            addActionError("Database type '" + dbType + "' not supported. JDBC Driver Error.");
            log.error(e.getMessage(), e); 
        } catch (ClassNotFoundException e) {
            addActionError("Database type '" + dbType + "' not supported. JDBC Driver do not exist: '" + getDatabaseDriver(dbType) + "'");
            log.error(e.getMessage(), e); 
        }
        if (hasActionErrors()) {
            return INPUT;
        }

        try {
            DriverManager.getConnection(dbUrl, dbUser, dbPassword);
        } catch (SQLException e) {
            addActionError("Cannot connect with database. <div>" + e.getMessage() + "</div>");
        }
        if (hasActionErrors()) {
            return INPUT;
        }

        // Si llegamos hasta aqui, salvarlo
        Store20Database database;
        Map<String, Store20Database> map = Store20Config.getInstance(ServletActionContext.getServletContext()).getStoreDb();
        if (map.containsKey(id)) {
            database = map.get(id);
        } else {
            database = new Store20Database(id);
            Store20Config.getInstance(ServletActionContext.getServletContext()).getStoreDb().put(id, database);
        }
        database.setType(dbType);
        database.setUrl(dbUrl);
        database.setUser(dbUser);
        database.setPassword(dbPassword);

        try {
            Store20Config.getInstance(ServletActionContext.getServletContext()).saveConfigurationFile(ServletActionContext.getServletContext());
        } catch (Exception e) {
            log.error(e.getMessage(), e); 
        }

        return SUCCESS;
    }

    private String getDatabaseDriver(String dbType) {
        if ("mysql".equalsIgnoreCase(dbType)) return "com.mysql.jdbc.Driver";
        else if ("sqlserver".equalsIgnoreCase(dbType)) return "com.microsoft.sqlserver.jdbc.SQLServerDriver";
        return "";
    }


    @Action(value = "listCommerce", results = {
            @Result(type = "velocity", location = "/org/store/config/views/block_commerces.vm")
    })
    public String listCommerce() throws Exception {
        ServletActionContext.getContext().put("commerces", Store20Config.getInstance(ServletActionContext.getServletContext()).getStoreMap());
        return SUCCESS;
    }

    public Map<String, Object> getStoreStatistics(String store) {
        Map<String, Object> result = null;
        Store20Commerce commerce = Store20Config.getInstance(ServletActionContext.getServletContext()).getStoreMap().get(store);
        Store20Database database = Store20Config.getInstance(ServletActionContext.getServletContext()).getStoreDb().get(commerce.getDatabase());
        Session hSession;
        try {
            hSession = HibernateSessionFactory.getNewSession(database);
            HibernateDAO dao = new HibernateDAO(hSession, store);
            try {
                result = dao.getHomeStats();
            } catch (HibernateException e) {
                log.error(e.getMessage(), e); 
            } finally {
                if (hSession.isOpen()) hSession.close();
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return result;
    }

    @Action(value = "editCommerce", results = {
            @Result(type = "velocity", location = "/org/store/config/views/block_editcommerces.vm")
    })
    public String editCommerce() throws Exception {
        Map<String, Store20Database> dbMap = Store20Config.getInstance(ServletActionContext.getServletContext()).getStoreDb();
        ServletActionContext.getContext().put("databases", dbMap.keySet());
        // Countries
        ServletActionContext.getContext().put("countries", CountryFactory.getCountries(getLocale()));
        ServletActionContext.getContext().put("templates", TemplateUtils.getTemplates(ServletActionContext.getServletContext(), store));

        if (StringUtils.isNotEmpty(store)) {
            Store20Commerce commerce = Store20Config.getInstance(ServletActionContext.getServletContext()).getStoreMap().get(store);
            if (commerce != null) {
                Store20Database database = (dbMap != null) ? dbMap.get(commerce.getDatabase()) : null;
                Session hSession;
                try {
                    hSession = HibernateSessionFactory.getNewSession(database);
                    HibernateDAO dao = new HibernateDAO(hSession, store);
                    try {
                        List<StoreProperty> l = dao.getStoreProperties(StoreProperty.TYPE_GENERAL);
                        Map<String, String> map = new HashMap<String, String>();
                        for (StoreProperty prop : l) map.put(prop.getCode(), prop.getValue());
                        ServletActionContext.getContext().put("properties", map);

                        // idiomas
                        String languages = map.get(StoreProperty.PROP_LANGUAGES);
                        if (StringUtils.isNotEmpty(languages)) ServletActionContext.getContext().put("languages", languages.split(","));

                        // monedas
                        List<String> lCurrCode = new ArrayList<String>();
                        List<Currency> lCurrency = dao.getCurrencies();
                        if (lCurrency != null) for (Currency c : lCurrency) lCurrCode.add(c.getCode());
                        if (lCurrCode != null && !lCurrCode.isEmpty()) ServletActionContext.getContext().put("currencies", lCurrCode);

                        ServletActionContext.getContext().put("commerce", commerce);

                        /*

                        // localizedtexts
                        List<LocalizedText> lText = hSession.createCriteria(LocalizedText.class).add(Restrictions.eq("inventaryCode",store)).list();
                        if (lText!=null && !lText.isEmpty())  ServletActionContext.getContext().put("texts",lText);

                        // menus
                        List<Menu> lMenuTop = hSession.createCriteria(Menu.class).add(Restrictions.eq("inventaryCode",store)).add(Restrictions.eq("menu","TopBar")).list();
                        if (lMenuTop!=null && !lMenuTop.isEmpty())  ServletActionContext.getContext().put("menus",lMenuTop);
                        */

                    } catch (HibernateException e) {
                        log.error(e.getMessage(), e); 
                    } finally {
                        if (hSession.isOpen()) hSession.close();
                    }
                } catch (Exception e) {
                    addActionError("Database configuration error: " + e.getMessage());
                }
            }
        }
        return SUCCESS;
    }

    @Action(value = "delCommerce", results = {
            @Result(type = "redirectAction", location = "listCommerce")
    })
    public String delCommerce() throws Exception {
        if (StringUtils.isNotEmpty(store)) {
            Map<String, Store20Commerce> map = Store20Config.getInstance(ServletActionContext.getServletContext()).getStoreMap();
            if (map != null && map.containsKey(store)) {
                map.remove(store);
                Store20Config.getInstance(ServletActionContext.getServletContext()).saveConfigurationFile(ServletActionContext.getServletContext());
            }
        }
        return SUCCESS;
    }

    @Action(value = "saveCommerce", results = {
            @Result(type = "velocity", name = "input", location = "/org/store/config/views/block_editcommerces.vm"),
            @Result(type = "redirectAction", location = "listCommerce")
    })
    public String saveCommerce() throws Exception {

        if (StringUtils.isNotEmpty(store)) {
            Store20Commerce commerce = Store20Config.getInstance(ServletActionContext.getServletContext()).getStoreMap().get(store);
            // si no existe la tienda configurarla en el xml
            if (commerce == null) {

                try {
                    commerce = new Store20Commerce(store);
                    commerce.setDatabase(database);
                    Store20Config.getInstance(ServletActionContext.getServletContext()).addCommerce(commerce);
                    Store20Config.getInstance(ServletActionContext.getServletContext()).saveConfigurationFile(ServletActionContext.getServletContext());
                    Store20Config.getInstance(ServletActionContext.getServletContext()).initializeStore(ServletActionContext.getServletContext(), store);
                } catch (Exception e) {
                    log.error(e.getMessage(), e); 
                }
            } else if (!commerce.getDatabase().equalsIgnoreCase(database)) {
                commerce.setDatabase(database);
                Store20Config.getInstance(ServletActionContext.getServletContext()).saveConfigurationFile(ServletActionContext.getServletContext());
                Store20Config.getInstance(ServletActionContext.getServletContext()).initializeStore(ServletActionContext.getServletContext(), store);
            }
            Store20Database database = Store20Config.getInstance(ServletActionContext.getServletContext()).getStoreDb().get(commerce.getDatabase());
            Session hSession;
            hSession = HibernateSessionFactory.getNewSession(database);
            Transaction tx = hSession.beginTransaction();
            try {
                HibernateDAO dao = new HibernateDAO(hSession, store);

                StoreProperty p = dao.getStoreProperty(StoreProperty.PROP_SITE_NAME, StoreProperty.TYPE_GENERAL, true);
                p.setValue(siteName);
                dao.save(p);

                p = dao.getStoreProperty(StoreProperty.PROP_SITE_URL, StoreProperty.TYPE_GENERAL, true);
                p.setValue("http://" + getDomain() + "/" + getPath());
                dao.save(p);

                p = dao.getStoreProperty(StoreProperty.PROP_TEMPLATE, StoreProperty.TYPE_GENERAL, true);
                p.setValue(template);
                dao.save(p);

                p = dao.getStoreProperty(StoreProperty.PROP_SITE_PATH, StoreProperty.TYPE_GENERAL, true);
                p.setValue(ServletActionContext.getServletContext().getRealPath("/stores/" + store));
                dao.save(p);

                if (!ArrayUtils.isEmpty(languages)) {
                    p = dao.getStoreProperty(StoreProperty.PROP_LANGUAGES, StoreProperty.TYPE_GENERAL, true);
                    p.setValue(StringUtils.join(languages, ","));
                    dao.save(p);

                    String defL = (StringUtils.isNotEmpty(defaultLanguage) && ArrayUtils.contains(languages, defaultLanguage)) ? defaultLanguage : languages[0];
                    p = dao.getStoreProperty(StoreProperty.PROP_DEFAULT_LANGUAGE, StoreProperty.TYPE_GENERAL, true);
                    p.setValue(defL);
                    dao.save(p);
                }

                if (ArrayUtils.contains(currencies, defaultCurrency)) {
                    p = dao.getStoreProperty(StoreProperty.PROP_DEFAULT_CURRENCY, StoreProperty.TYPE_GENERAL, true);
                    p.setValue(defaultCurrency);
                    dao.save(p);
                } else if (!ArrayUtils.isEmpty(currencies)) {
                    p = dao.getStoreProperty(StoreProperty.PROP_DEFAULT_CURRENCY, StoreProperty.TYPE_GENERAL, true);
                    p.setValue(currencies[0]);
                    dao.save(p);
                }

                p = dao.getStoreProperty(StoreProperty.PROP_DIMENSION_UNIT, StoreProperty.TYPE_GENERAL, true);
                p.setValue(dimensionUnit);
                dao.save(p);

                p = dao.getStoreProperty(StoreProperty.PROP_WEIGHT_UNIT, StoreProperty.TYPE_GENERAL, true);
                p.setValue(weightUnit);
                dao.save(p);

                p = dao.getStoreProperty(StoreProperty.PROP_DEFAULT_COUNTRY, StoreProperty.TYPE_GENERAL, true);
                p.setValue(defaultCountry);
                dao.save(p);

                // Salvar currencies
                List<Currency> lc = dao.getCurrencies();
                if (lc != null && !lc.isEmpty()) {
                    for (Currency curr : lc) {
                        if (!ArrayUtils.contains(currencies, curr.getCode()))
                            dao.delete(curr);
                    }
                }
                if (!ArrayUtils.isEmpty(currencies)) {
                    for (String currCode : currencies) {
                        Currency curr = dao.getCurrency(currCode);
                        if (curr == null) {
                            curr = new Currency();
                            curr.setActive(true);
                            curr.setInventaryCode(store);
                            curr.setCode(currCode);
                            curr.setSymbol(getCurrSymbol(currCode));
                            curr.setRatio(1.0);
                            dao.save(curr);
                        }
                    }
                }

                // Crear order states
                createOrderStatus(dao, OrderStatus.STATUS_REJECTED, 3);
                createOrderStatus(dao, OrderStatus.STATUS_APPROVED, 2);
                createOrderStatus(dao, OrderStatus.STATUS_DEFAULT, 1);

                // Crear product labels
                createLabel(dao, ProductLabel.LABEL_NEW);
                createLabel(dao, ProductLabel.LABEL_FREE_SHIPPING);
                createLabel(dao, ProductLabel.LABEL_HOT);
                createLabel(dao, ProductLabel.LABEL_SPECIAL);

                // Crear user level
                createLevel(dao, UserLevel.DEFAULT_LEVEL, 1);
                createLevel(dao, UserLevel.AFFILIATE_LEVEL, 1);

                // Encriptacion pa los passwords
                StoreProperty encryptionBean = dao.getStoreProperty(StoreProperty.PROP_ENCRYPTION_KEY, StoreProperty.TYPE_GENERAL, true);
                if (StringUtils.isEmpty(encryptionBean.getValue())) {
                    encryptionBean.setValue(User.generatePassword(16));
                    dao.save(encryptionBean);
                }

                // Crear admin user
                UserAdminRole superAdminRole = dao.getRole(User.ADMROL_SUPERADMIN);
                if (superAdminRole == null) {
                    superAdminRole = new UserAdminRole();
                    superAdminRole.setRoleCode(User.ADMROL_SUPERADMIN);
                    superAdminRole.setInventaryCode(store);
                    superAdminRole.setActions(new HashSet(UserAdminRole.getAllActions()));
                    dao.save(superAdminRole);
                }
                if (dao.getAdminUser("admin") == null) {
                    User bean = new User();
                    bean.setAdmin(true);
                    bean.setInventaryCode(store);
                    bean.setFirstname("Store");
                    bean.setLastname("Administrator");
                    bean.setUserId("admin");
                    bean.setPassword("admin");
                    Set<UserAdminRole> roles = new HashSet<UserAdminRole>();
                    roles.add(superAdminRole);
                    bean.setRoles(roles);
                    dao.save(bean);
                }

                // Crear topbar menu
                List<Menu> l = dao.getMenus("TopBar", null);
                if (l == null || l.isEmpty()) {
                    Menu bean = new Menu();
                    bean.setMenu("TopBar");
                    bean.addMenuLabel("en", "All Categories");
                    bean.addMenuLabel("es", "Categorias");
                    bean.addMenuLabel("fr", "All Categories");
                    bean.setShowSubcategories(true);
                    bean.setInventaryCode(store);
                    dao.save(bean);
                }

                // Carpetas de la tienda
                String[] folders = getStoreFolders();
                if (folders != null && folders.length > 0) {
                    for (String folder : folders) {
                        FileUtils.forceMkdir(new File(ServletActionContext.getServletContext().getRealPath("/stores/" + store + folder)));
                    }
                }

                // load labels
                /*
                InputStream is = getClass().getResourceAsStream("/db/labels.properties");
                if (is != null) {
                    Properties labelProp = new Properties();
                    labelProp.load(is);
                    if (!labelProp.isEmpty()) {
                        for (String key : labelProp.stringPropertyNames()) {
                            createLocalizedText(dao, key, labelProp.getProperty(key));
                        }
                    }
                }
                */

                // Generate States
                if (getGenerateStates() && StringUtils.isNotEmpty(defaultCountry)) {
                    try {
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
                    } catch (Exception e) {
                        log.error(e.getMessage(), e); 
                    }
                }

                tx.commit();
            } catch (HibernateException e) {
                tx.rollback();
                log.error(e.getMessage(), e); 
            } catch (Exception e) {
                tx.rollback();
                log.error(e.getMessage(), e); 
            } finally {
                if (hSession.isOpen()) hSession.close();
            }
        }

        return SUCCESS;
    }

    @Action(value = "delCommerceUrl", results = {
            @Result(type = "redirectAction", location = "listCommerce")
    })
    public String delCommerceUrl() throws Exception {
        if (StringUtils.isNotEmpty(id)) {
            Store20Commerce commerce = Store20Config.getInstance(ServletActionContext.getServletContext()).getStoreMap().get(id);
            if (commerce != null) {
                if (index != null && commerce.getUrls() != null && index < commerce.getUrls().size()) {
                    commerce.getUrls().remove(index.intValue());
                    try {
                        Store20Config.getInstance(ServletActionContext.getServletContext()).saveConfigurationFile(ServletActionContext.getServletContext());
                    } catch (Exception e) {
                        log.error(e.getMessage(), e); 
                    }
                }
            }
        }
        return SUCCESS;
    }

    @Action(value = "saveCommerceUrl", results = {
            @Result(type = "redirectAction", location = "listCommerce")
    })
    public String saveCommerceUrl() throws Exception {
        if (StringUtils.isNotEmpty(id)) {
            Store20Commerce commerce = Store20Config.getInstance(ServletActionContext.getServletContext()).getStoreMap().get(id);
            if (commerce != null) {
                if (index != null && commerce.getUrls() != null && index < commerce.getUrls().size()) {
                    Store20Commerce.StoreUrl url = commerce.getUrls().get(index);
                    url.setDomain(domain);
                    url.setPath(path);
                } else {
                    commerce.addUrl(domain, path);
                }
                try {
                    Store20Config.getInstance(ServletActionContext.getServletContext()).saveConfigurationFile(ServletActionContext.getServletContext());
                } catch (Exception e) {
                    log.error(e.getMessage(), e); 
                }
            }
        }
        return SUCCESS;
    }

    @Action(value = "importCommerce", results = {
            @Result(type = "redirectAction", location = "home")
    })
    public String importCommerceId() {
        if (StringUtils.isNotEmpty(id) && StringUtils.isNotEmpty(nuevo)) {
            Store20Commerce commerce = Store20Config.getInstance(ServletActionContext.getServletContext()).getStoreMap().get(id);
            if (commerce != null) {
                Store20Database database = Store20Config.getInstance(ServletActionContext.getServletContext()).getStoreDb().get(commerce.getDatabase());
                if (database != null) {
                    try {
                        Session hSession = HibernateSessionFactory.getNewSession(database);
                        Transaction tx = hSession.beginTransaction();
                        try {
                            Map map = hSession.getSessionFactory().getAllClassMetadata();
                            for (Object o : map.keySet()) {
                                SingleTableEntityPersister p = (SingleTableEntityPersister) map.get(o);
                                if (ArrayUtils.contains(p.getPropertyNames(), "inventaryCode")) {
                                    System.out.print(o.toString() + " - ");
                                    try {
                                        hSession.createQuery("update " + o.toString() + " b set b.inventaryCode=:newCode where b.inventaryCode = :oldCode")
                                                .setString("newCode", id)
                                                .setString("oldCode", nuevo)
                                                .executeUpdate();
                                        System.out.println("OK");
                                    } catch (HibernateException e) {
                                        System.out.println("ERROR");
                                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                                    }
                                }

                            }
                            tx.commit();
                        } catch (HibernateException e) {
                            tx.rollback();
                            LOG.error(e.getMessage());
                            log.error(e.getMessage(), e); 
                        } catch (Exception e) {
                            tx.rollback();
                            LOG.error(e.getMessage());
                            log.error(e.getMessage(), e); 
                        } finally {
                            if (hSession.isOpen()) hSession.close();
                        }
                    } catch (Exception e) {
                        log.error(e.getMessage(), e); 
                    }
                }
            }
        }
        return SUCCESS;
    }

    @Action(value = "executeScript", results = {
            @Result(type = "redirectAction", location = "editCommerce?store=${store}")
    })
    public String executeScript() throws Exception {
        LOG.debug("Executing Script");
        if (StringUtils.isNotEmpty(store) && StringUtils.isNotEmpty(scriptName)) {
            if (scriptName.endsWith("labels.properties")) loadNewLabels();
            else executeSqlScript();
        }
        return SUCCESS;
    }


    private void executeSqlScript() throws Exception {
        Store20Commerce commerce = Store20Config.getInstance(ServletActionContext.getServletContext()).getStoreMap().get(store);
        Store20Database database = Store20Config.getInstance(ServletActionContext.getServletContext()).getStoreDb().get(commerce.getDatabase());
        LOG.debug("Store: " + store + ", Script: " + scriptName);
        InputStream scriptIS = ClassLoaderUtils.getResourceAsStream(scriptName, ConfigAction.class);
        if (scriptIS != null) {
            LOG.debug("Script found");
            Session hSession = HibernateSessionFactory.getNewSession(database);
            Transaction tx = hSession.beginTransaction();
            try {
                String line;
                String sqlGetIdentity = (hSession.getSessionFactory() instanceof SessionFactoryImpl) ? ((SessionFactoryImpl) hSession.getSessionFactory()).getDialect().getIdentitySelectString("", "", 1) : null;
                BufferedReader input = new BufferedReader(new InputStreamReader(scriptIS));
                try {
                    Map<String, Object> map = new HashMap<String, Object>();
                    map.put("store", store);
                    while ((line = input.readLine()) != null) {
                        if (StringUtils.isNotEmpty(line.trim())) {
                            try {
                                line = line.trim();
                                LOG.debug("Executing: " + line);
                                if (line.startsWith("$")) {
                                    int pos = StringUtils.indexOf(line, "=");
                                    String var = StringUtils.substring(line, 1, pos).trim();
                                    line = StringUtils.substring(line, pos + 1).trim();
                                    SQLQuery q = hSession.createSQLQuery(line);
                                    if (!ArrayUtils.isEmpty(q.getNamedParameters())) {
                                        for (String param : q.getNamedParameters()) {
                                            if (map.containsKey(param)) q.setParameter(param, map.get(param));
                                        }
                                    }
                                    q.executeUpdate();
                                    if (StringUtils.isNotEmpty(var) && StringUtils.isNotEmpty(sqlGetIdentity)) {
                                        map.put(var, hSession.createSQLQuery(sqlGetIdentity).uniqueResult());
                                    }
                                } else {
                                    SQLQuery q = hSession.createSQLQuery(line);
                                    if (!ArrayUtils.isEmpty(q.getNamedParameters())) {
                                        for (String param : q.getNamedParameters()) {
                                            if (map.containsKey(param)) q.setParameter(param, map.get(param));
                                        }
                                    }
                                    q.executeUpdate();
                                }
                            } catch (HibernateException e) {
                                LOG.error(e.getMessage());
                                log.error(e.getMessage(), e); 
                            }
                        }
                    }
                } catch (Exception e) {
                    LOG.error(e.getMessage());
                    log.error(e.getMessage(), e); 
                } finally {
                    try {
                        if (input != null) input.close();
                    } catch (IOException ignored) {
                    }
                }
                tx.commit();
            } catch (HibernateException e) {
                tx.rollback();
                LOG.error(e.getMessage());
                log.error(e.getMessage(), e); 
            } catch (Exception e) {
                tx.rollback();
                LOG.error(e.getMessage());
                log.error(e.getMessage(), e); 
            } finally {
                if (hSession.isOpen()) hSession.close();
            }
        } else {
            LOG.error("Script not found");
        }
    }

    private void loadNewLabels() throws Exception {
        Store20Commerce commerce = Store20Config.getInstance(ServletActionContext.getServletContext()).getStoreMap().get(store);
        Store20Database database = Store20Config.getInstance(ServletActionContext.getServletContext()).getStoreDb().get(commerce.getDatabase());
        Session hSession = HibernateSessionFactory.getNewSession(database);
        Transaction tx = hSession.beginTransaction();
        try {
            HibernateDAO dao = new HibernateDAO(hSession, store);
            InputStream is = ClassLoaderUtils.getResourceAsStream("db/labels.prop", ConfigAction.class);

            if (is != null) {
                Properties labelProp = new Properties();
                labelProp.load(is);
                if (!labelProp.isEmpty()) {
                    for (String key : labelProp.stringPropertyNames()) {
                        LocalizedText lt = dao.getLocalizedtext(key);
                        if (lt == null) {
                            lt = new LocalizedText();
                            lt.setCode(key);
                            lt.setInventaryCode(store);
                            lt.setValue(labelProp.getProperty(key));
                            dao.save(lt);
                        }
                    }
                }
            } else {
                LOG.error("Store Initialization Error: Could not load resource '/db/labels.props'");
            }
            tx.commit();
        } catch (HibernateException e) {
            tx.rollback();
            LOG.error(e.getMessage());
            log.error(e.getMessage(), e); 
        } catch (Exception e) {
            tx.rollback();
            LOG.error(e.getMessage());
            log.error(e.getMessage(), e); 
        } finally {
            if (hSession.isOpen()) hSession.close();
        }
    }


    private void createOrderStatus(HibernateDAO dao, String statusCode, int o) {
        OrderStatus bean = dao.getOrderStatus(statusCode, false);
        if (bean == null) {
            bean = new OrderStatus();
            bean.setStatusCode(statusCode);
            bean.setStatusType(statusCode);
            bean.setSendEmail(true);
            bean.setStatusOrder(o);
            bean.setInventaryCode(store);
            String name = StringUtils.capitalize(StringUtils.replace(statusCode, "_", " "));
            if (languages != null && languages.length > 0)
                for (String l : languages)
                    bean.setStatusName(l, name);
            dao.save(bean);
        }
    }

    private void createLevel(HibernateDAO dao, String level, int order) {
        UserLevel bean = dao.getUserLevel(level);
        if (bean == null) {
            bean = new UserLevel();
            bean.setLevelOrder(order);
            bean.setCode(level);
            bean.setDisableVolume(false);
            bean.setDiscountPercent(1.0d);
            bean.setInventaryCode(store);
            String name = StringUtils.capitalize(StringUtils.replace(level, "_", " "));
            if (languages != null && languages.length > 0)
                for (String l : languages)
                    bean.setName(l, name);
            dao.save(bean);
        }
    }

    private void createLabel(HibernateDAO dao, String code) {
        if (dao.getProductLabelByCode(code) == null) {
            ProductLabel bean = new ProductLabel();
            bean.setCode(code);
            bean.setInventaryCode(store);
            String name = StringUtils.capitalize(StringUtils.replace(code, "_", " "));
            if (languages != null && languages.length > 0)
                for (String l : languages)
                    bean.setName(l, name);
            dao.save(bean);
        }
    }

    private String[] getStoreFolders() {
        return new String[]{
                "/images/admin",
                "/images/b",
                "/images/custom",
                "/images/manufacturers",
                "/images/products/list",
                "/images/products/zoom",
                "/images/shipping",
                "/resources"
        };
    }

    private String getCurrSymbol(String currCode) {
        if ("USD".equalsIgnoreCase(currCode)) return "$";
        else if ("CAD".equalsIgnoreCase(currCode)) return "$";
        else if ("EUR".equalsIgnoreCase(currCode)) return "&euro;";
        return null;
    }

    private String user;
    private String password;

    private Integer index;
    private String id;
    private String dbType;
    private String dbUrl;
    private String dbUser;
    private String dbPassword;
    private String dbUseLucene;
    private String nuevo;

    private String store;
    private String siteName;
    private String database;
    private String[] languages;
    private String[] currencies;
    private String defaultLanguage;
    private String defaultCurrency;
    private String defaultCountry;
    private String dimensionUnit;
    private String weightUnit;
    private String template;
    private Boolean generateStates;
    private String domain;
    private String path;
    private String scriptName;

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Integer getIndex() {
        return index;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNuevo() {
        return nuevo;
    }

    public void setNuevo(String nuevo) {
        this.nuevo = nuevo;
    }

    public String getScriptName() {
        return scriptName;
    }

    public void setScriptName(String scriptName) {
        this.scriptName = scriptName;
    }

    public String getDefaultCountry() {
        return defaultCountry;
    }

    public void setDefaultCountry(String defaultCountry) {
        this.defaultCountry = defaultCountry;
    }

    public Boolean getGenerateStates() {
        return generateStates != null && generateStates;
    }

    public void setGenerateStates(Boolean generateStates) {
        this.generateStates = generateStates;
    }

    public String getTemplate() {
        return template;
    }

    public void setTemplate(String template) {
        this.template = template;
    }

    public String getSiteName() {
        return siteName;
    }

    public void setSiteName(String siteName) {
        this.siteName = siteName;
    }

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public String[] getLanguages() {
        return languages;
    }

    public void setLanguages(String[] languages) {
        this.languages = languages;
    }

    public String[] getCurrencies() {
        return currencies;
    }

    public void setCurrencies(String[] currencies) {
        this.currencies = currencies;
    }

    public String getDefaultLanguage() {
        return defaultLanguage;
    }

    public void setDefaultLanguage(String defaultLanguage) {
        this.defaultLanguage = defaultLanguage;
    }

    public String getDefaultCurrency() {
        return defaultCurrency;
    }

    public void setDefaultCurrency(String defaultCurrency) {
        this.defaultCurrency = defaultCurrency;
    }

    public String getDimensionUnit() {
        return dimensionUnit;
    }

    public void setDimensionUnit(String dimensionUnit) {
        this.dimensionUnit = dimensionUnit;
    }

    public String getWeightUnit() {
        return weightUnit;
    }

    public void setWeightUnit(String weightUnit) {
        this.weightUnit = weightUnit;
    }

    public String getStore() {
        return store;
    }

    public void setStore(String store) {
        this.store = store;
    }

    public String getDbType() {
        return dbType;
    }

    public String getDbUrl() {
        return dbUrl;
    }

    public String getDbUser() {
        return dbUser;
    }

    public String getDbPassword() {
        return dbPassword;
    }

    public void setDbType(String dbType) {
        this.dbType = dbType;
    }

    public void setDbUrl(String dbUrl) {
        this.dbUrl = dbUrl;
    }

    public void setDbUser(String dbUser) {
        this.dbUser = dbUser;
    }

    public void setDbPassword(String dbPassword) {
        this.dbPassword = dbPassword;
    }

    public String getDbUseLucene() {
        return dbUseLucene;
    }

    public void setDbUseLucene(String dbUseLucene) {
        this.dbUseLucene = dbUseLucene;
    }

    public String getDomain() {
        return domain != null ? domain : "";
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getPath() {
        return path != null ? path : "";
    }

    public void setPath(String path) {
        this.path = path;
    }

}
