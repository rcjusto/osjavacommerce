package org.store.core.globals.config;

import com.opensymphony.xwork2.util.logging.Logger;
import com.opensymphony.xwork2.util.logging.LoggerFactory;
import org.apache.commons.digester.Digester;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.store.core.beans.StoreProperty;
import org.store.core.beans.User;
import org.store.core.beans.UserAdminRole;
import org.store.core.beans.UserLevel;
import org.store.core.dao.HibernateDAO;
import org.store.core.hibernate.HibernateSessionFactory;
import org.store.core.utils.PluginAdminMenu;
import org.store.core.utils.carriers.CarrierService;
import org.store.core.utils.classutil.*;
import org.store.core.utils.events.EventService;
import org.store.core.utils.merchants.MerchantService;
import org.store.core.utils.quartz.BaseJob;
import org.store.core.utils.quartz.QuartzUtils;
import org.store.core.utils.reports.IStoreReport;
import org.store.core.utils.suppliers.SupplierService;
import org.store.core.utils.templates.TemplateBannerZone;
import org.store.core.utils.templates.TemplateBlock;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.servlet.ServletContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FilenameFilter;
import java.util.*;

public class Store20Config {

    private static final Logger LOG = LoggerFactory.getLogger(Store20Config.class);
    public static final String ATTRIBUTE_APPLICATION_CONFIG = "org.store.core.globals.config.Store20Config";
    private static final String PLUGIN_LIBRARIES_PATH = "/WEB-INF/lib";
    private static final String PLUGIN_LIBRARIES_PREFIX = "store20";
    public static final String CONFIG_FILE = "/WEB-INF/classes/configuration.xml";
    public static final String CONFIG_FILE_BAK = "/WEB-INF/classes/configuration.bak";
    public static final String INDEXES_PATH = "/WEB-INF/indexes";

    private List<Class> extraBeans = new ArrayList<Class>();
    private List<Class> reports = new ArrayList<Class>();
    private Map<String, TemplateBlock> extraBlocks = new HashMap<String, TemplateBlock>();
    private Map<String, TemplateBannerZone> extraBanners = new HashMap<String, TemplateBannerZone>();
    private Map<String, Class> mapSuplier = new HashMap<String, Class>();
    private Map<String, Class> mapCarrier = new HashMap<String, Class>();
    private Map<String, Class> mapMerchants = new HashMap<String, Class>();
    private Map<String, Class> mapJobs = new HashMap<String, Class>();
    private Map<String, Class> mapEvents = new HashMap<String, Class>();
    private Map<String, List<PluginAdminMenu>> menus = new HashMap<String, List<PluginAdminMenu>>();
    private Map<String, Store20Commerce> storeMap;
    private Map<String, Store20Database> storeDb;
    private Map<String, Store20Component> storeComponents;


    public static Store20Config getInstance(ServletContext ctx) {
        Object o = ctx.getAttribute(ATTRIBUTE_APPLICATION_CONFIG);
        if (o == null || !(o instanceof Store20Config)) {
            Store20Config cfg = new Store20Config();
            try {
                cfg.initialize(ctx);
            } catch (Exception e) {
                LOG.error(e.getMessage(), e);
            }
            return cfg;
        }
        return (Store20Config) o;
    }

    public void initialize(ServletContext ctx) {
        // Load Configuration

        // put in application context
        ctx.setAttribute(ATTRIBUTE_APPLICATION_CONFIG, this);

        initializeApplication(ctx);

    }

    public void addPluginAdminMenu(PluginAdminMenu menu) {
        List<PluginAdminMenu> listado = menus.containsKey(menu.getMenuParent()) ? menus.get(menu.getMenuParent()) : null;
        if (listado == null) {
            listado = new ArrayList<PluginAdminMenu>();
            menus.put(menu.getMenuParent(), listado);
        }
        if (!listado.contains(menu)) listado.add(menu);
    }

    public void registerBean(Class bean) {
        if (extraBeans == null) extraBeans = new ArrayList<Class>();
        if (!extraBeans.contains(bean)) extraBeans.add(bean);
    }

    public void initializeApplication(ServletContext ctx) {
        try {

            LOG.info("Initializing System ...");

            // busca plugins
            findPlugins(ctx);

            // lee configuracion de comercios
            loadConfigurationFile(ctx);

            // Inicializacion de cada store
            if (storeMap != null && !storeMap.isEmpty())
                for (String storeCode : storeMap.keySet())
                    initializeStore(ctx, storeCode);

            LOG.info("System Initialized");

        } catch (Exception e) {
            LOG.error("UNKNOWN INITIALIZATION ERROR: " + e.getMessage());
            LOG.error(e.getMessage(), e);
        }
    }

    public void initializeStore(ServletContext ctx, String storeCode) throws Exception {
        Store20Commerce commerce = storeMap.get(storeCode);
        Store20Database dbConfig = storeDb.get(commerce.getDatabase());
        Session session = HibernateSessionFactory.getNewSession(dbConfig);
        if (session != null) {
            Transaction tx = session.beginTransaction();
            try {
                HibernateDAO dao = new HibernateDAO(session, storeCode);

                // Verificar que exista el role de superadmin
                UserAdminRole role = dao.getRole(User.ADMROL_SUPERADMIN);
                if (role == null) {
                    role = new UserAdminRole();
                    role.setRoleCode(User.ADMROL_SUPERADMIN);
                    role.setInventaryCode(storeCode);
                    dao.save(role);
                }
                User admin = dao.getUserByUserId("admin");
                if (admin == null) {
                    admin = new User();
                    admin.setUserId("admin");
                    admin.setPassword("admin");
                    admin.setAdmin(true);
                    admin.setFirstname("System");
                    admin.setLastname("Administrator");
                    admin.setRoles(new HashSet<UserAdminRole>());
                    admin.getRoles().add(role);
                    dao.save(admin);
                } else {
                    if (!admin.getAdmin()) {
                        admin.setAdmin(true);
                        if (admin.getRoles() == null) admin.setRoles(new HashSet<UserAdminRole>());
                        if (!admin.getRoles().contains(role)) admin.getRoles().add(role);
                        dao.save(admin);
                    }
                    if (!admin.hasRole(role.getId())) {
                        if (admin.getRoles() == null) admin.setRoles(new HashSet<UserAdminRole>());
                        admin.getRoles().add(role);
                    }
                }

                // localization
                String[] languages = null;
                StoreProperty beanLang = dao.getStoreProperty(StoreProperty.PROP_LANGUAGES, StoreProperty.TYPE_GENERAL, true);
                if (!StringUtils.isEmpty(beanLang.getValue())) {
                    languages = beanLang.getValue().split(",");
                }
                if (languages == null || languages.length < 1) {
                    beanLang.setValue("en");
                    dao.save(beanLang);
                    languages = new String[]{"en"};
                }

                // User Levels
                UserLevel l = dao.getUserLevel(UserLevel.ANONYMOUS_LEVEL);
                if (l == null) {
                    l = new UserLevel();
                    l.setCode(UserLevel.ANONYMOUS_LEVEL);
                    for (String lang : languages) l.setName(lang, UserLevel.ANONYMOUS_LEVEL);
                    l.setLevelOrder(0);
                    dao.save(l);
                }
                UserLevel l1 = dao.getUserLevel(UserLevel.DEFAULT_LEVEL);
                if (l1 == null) {
                    l1 = new UserLevel();
                    l1.setCode(UserLevel.DEFAULT_LEVEL);
                    for (String lang : languages) l1.setName(lang, UserLevel.DEFAULT_LEVEL);
                    l1.setLevelOrder(1);
                    dao.save(l1);
                }

                // Poner el PATH de la aplicacion
                StoreProperty propPath = dao.getStoreProperty(StoreProperty.PROP_SITE_PATH, StoreProperty.TYPE_GENERAL, true);
                propPath.setInventaryCode(storeCode);
                String storePath = ctx.getRealPath("/stores/" + storeCode);
                LOG.info("PATH: " + storePath);
                propPath.setValue(storePath);
                dao.save(propPath);
                // Poner la URL de la aplicacion
                StoreProperty propUrl = dao.getStoreProperty(StoreProperty.PROP_SITE_URL, StoreProperty.TYPE_GENERAL, true);
                propUrl.setInventaryCode(storeCode);
                propUrl.setValue(commerce.getFullUrl(0));
                dao.save(propUrl);
                StoreProperty propHost = dao.getStoreProperty(StoreProperty.PROP_SITE_HOST, StoreProperty.TYPE_GENERAL, true);
                propHost.setInventaryCode(storeCode);
                propHost.setValue(commerce.getFullHost(0));
                dao.save(propHost);
                dao.flushSession();

                // create some path if do not exists
                String[] paths = {
                        "images",
                        "images"+File.separator+"products",
                        "images"+File.separator+"products"+File.separator+"list",
                        "images"+File.separator+"products"+File.separator+"zoom",
                        "images"+File.separator+"products"+File.separator+"cat1",
                        "images"+File.separator+"products"+File.separator+"cat2"
                };
                for(String p : paths) {
                    File f = new File(storePath + File.separator + p);
                    if (!f.exists()) {
                        LOG.info("Creating folder: " + p);
                        FileUtils.forceMkdir(f);
                    }
                }

                // Configurar tareas en background
                try {
                    QuartzUtils qu = new QuartzUtils(ctx, dao);
                    qu.configureTasks(storeCode, dbConfig, this);
                    LOG.info(storeCode + ": Quartz Task Configuration Successfully");
                } catch (Exception e) {
                    LOG.error(storeCode + ": QUARTZ TASKS ERROR: " + e.getMessage());
                }

                // Initialize plugins
                for (String o : mapEvents.keySet()) {
                    Class objClass = mapEvents.get(o);
                    try {
                        Object objService = objClass.newInstance();
                        if (objService instanceof EventService) ((EventService) objService).initialize(ctx, session, storeCode);
                        LOG.info(storeCode + ": Plugin " + o + " Initialized");
                    } catch (Exception e) {
                        LOG.error(storeCode + ": ERROR INITIALIZING PLUGIN " + o + ": " + e.getMessage());
                        LOG.error(e.getMessage(), e);
                    }
                }

                tx.commit();
            } catch (Exception e) {
                tx.rollback();
                LOG.error("DATABASE ERROR: " + e.getMessage());
                LOG.error(e.getMessage(), e);
            } finally {
                if (session.isOpen()) session.close();
            }
        } else {
            LOG.error("DATABASE ERROR: Can't connect");

        }
    }

    private void findPlugins(ServletContext ctx) throws Exception {
        String libraryPath = ctx.getRealPath(PLUGIN_LIBRARIES_PATH);
        File f = new File(libraryPath);
        if (f.exists() && f.isDirectory()) {
            File[] files = f.listFiles(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return name.startsWith(PLUGIN_LIBRARIES_PREFIX);
                }
            });

            ClassFinder finder = new ClassFinder();
            finder.add(files);


            // Supliers
            ClassFilter filterSupplier = new AndClassFilter(new NotClassFilter(new InterfaceOnlyClassFilter()), new SubclassClassFilter(SupplierService.class));
            Collection<ClassInfo> supplierClasses = new ArrayList<ClassInfo>();
            finder.findClasses(supplierClasses, filterSupplier);
            mapSuplier.clear();
            for (ClassInfo ci : supplierClasses) {
                try {
                    Class cl = Class.forName(ci.getClassName());
                    if (cl != null) {
                        Object o = cl.newInstance();
                        if (o instanceof SupplierService) {
                            String supplierName = ((SupplierService) o).getName();
                            if (StringUtils.isNotEmpty(supplierName)) {
                                mapSuplier.put(supplierName, cl);
                                LOG.info("Supplier found: " + supplierName);
                            }
                        }
                    }
                } catch (Exception e) {
                    LOG.error(e.getMessage(), e);
                }
            }

            // Carriers
            ClassFilter filterCarrier = new AndClassFilter(new NotClassFilter(new InterfaceOnlyClassFilter()), new SubclassClassFilter(CarrierService.class));
            Collection<ClassInfo> carrierClasses = new ArrayList<ClassInfo>();
            finder.findClasses(carrierClasses, filterCarrier);
            mapCarrier.clear();
            for (ClassInfo ci : carrierClasses) {
                try {
                    Class cl = Class.forName(ci.getClassName());
                    if (cl != null) {
                        Object o = cl.newInstance();
                        if (o instanceof CarrierService) {
                            String carrierName = ((CarrierService) o).getName();
                            if (StringUtils.isNotEmpty(carrierName)) {
                                mapCarrier.put(carrierName, cl);
                                LOG.info("Carrier found: " + carrierName);
                            }
                        }
                    }
                } catch (Exception e) {
                    LOG.error(e.getMessage(), e);
                }
            }

            // Merchants
            ClassFilter filterMerchants = new AndClassFilter(new NotClassFilter(new InterfaceOnlyClassFilter()), new SubclassClassFilter(MerchantService.class));
            Collection<ClassInfo> merchantClasses = new ArrayList<ClassInfo>();
            finder.findClasses(merchantClasses, filterMerchants);
            mapMerchants.clear();
            for (ClassInfo ci : merchantClasses) {
                try {
                    Class cl = Class.forName(ci.getClassName());
                    if (cl != null) {
                        Object o = cl.newInstance();
                        if (o instanceof MerchantService) {
                            String merchantName = ((MerchantService) o).getCode();
                            if (StringUtils.isNotEmpty(merchantName)) {
                                mapMerchants.put(merchantName, cl);
                                LOG.info("Payment gateway found: " + merchantName);
                            }
                        }
                    }
                } catch (Exception e) {
                    LOG.error(e.getMessage(), e);
                }
            }

            // JOBS
            ClassFilter filterJobs = new AndClassFilter(new NotClassFilter(new InterfaceOnlyClassFilter()), new SubclassClassFilter(BaseJob.class));
            Collection<ClassInfo> jobClasses = new ArrayList<ClassInfo>();
            finder.findClasses(jobClasses, filterJobs);
            mapJobs.clear();
            for (ClassInfo ci : jobClasses) {
                try {
                    Class cl = Class.forName(ci.getClassName());
                    if (cl != null) {
                        Object o = cl.newInstance();
                        if (o instanceof BaseJob) {
                            String jobName = ((BaseJob) o).getName();
                            if (StringUtils.isNotEmpty(jobName)) {
                                mapJobs.put(jobName, cl);
                                LOG.info("Job found: " + jobName);
                            }
                        }
                    }
                } catch (Exception e) {
                    LOG.error(e.getMessage(), e);
                }
            }

            // EVENT PLUGINS
            ClassFilter filterEvents = new AndClassFilter(new NotClassFilter(new InterfaceOnlyClassFilter()), new SubclassClassFilter(EventService.class));
            Collection<ClassInfo> eventClasses = new ArrayList<ClassInfo>();
            finder.findClasses(eventClasses, filterEvents);
            mapEvents.clear();
            for (ClassInfo ci : eventClasses)
                if (!ci.getClassName().endsWith(".DefaultEventServiceImpl")) {
                    try {
                        Class cl = Class.forName(ci.getClassName());
                        if (cl != null) {
                            Object o = cl.newInstance();
                            if (o instanceof EventService && StringUtils.isNotEmpty(((EventService) o).getName())) {
                                EventService es = (EventService) o;
                                es.initializePlugin(ctx);
                                mapEvents.put(es.getName(), cl);
                                LOG.info("Event plugin found: " + es.getName());
                            }
                        }
                    } catch (Exception e) {
                        LOG.error(e.getMessage(), e);
                    }
                }

            // REPORT PLUGINS
            ClassFilter filterReports = new AndClassFilter(new NotClassFilter(new InterfaceOnlyClassFilter()), new SubclassClassFilter(IStoreReport.class));
            Collection<ClassInfo> reportClasses = new ArrayList<ClassInfo>();
            finder.findClasses(reportClasses, filterReports);
            reports.clear();
            for (ClassInfo ci : reportClasses) {
                try {
                    Class cl = Class.forName(ci.getClassName());
                    if (cl != null) {
                        reports.add(cl);
                        LOG.info("Report plugin found: " + cl.getName());
                    }
                } catch (Exception e) {
                    LOG.error(e.getMessage(), e);
                }
            }

        } else {
            LOG.error("Store Initialization Error: Library path not found");
        }
    }

    public void loadConfigurationFile(ServletContext ctx) {
        File file = new File(ctx.getRealPath(CONFIG_FILE));
        if (file.exists())
            try {
                Digester digester = new Digester();
                digester.push(this);

                digester.addObjectCreate("commerces/database", Store20Database.class);
                digester.addSetProperties("commerces/database");
                digester.addSetNext("commerces/database", "addDatabase");
                digester.addCallMethod("commerces/database/type", "setType", 0);
                digester.addCallMethod("commerces/database/url", "setUrl", 0);
                digester.addCallMethod("commerces/database/user", "setUser", 0);
                digester.addCallMethod("commerces/database/password", "setPassword", 0);
                digester.addCallMethod("commerces/database/property", "addProperty", 2);
                digester.addCallParam("commerces/database/property", 0, "name");
                digester.addCallParam("commerces/database/property", 1);

                digester.addObjectCreate("commerces/commerce", Store20Commerce.class);
                digester.addSetProperties("commerces/commerce");
                digester.addSetNext("commerces/commerce", "addCommerce");
                digester.addCallMethod("commerces/commerce/database", "setDatabase", 0);
                digester.addCallMethod("commerces/commerce/url", "addUrl", 2);
                digester.addCallParam("commerces/commerce/url", 0, "domain");
                digester.addCallParam("commerces/commerce/url", 1, "path");

                digester.addObjectCreate("commerces/component", Store20Component.class);
                digester.addSetProperties("commerces/component");
                digester.addSetNext("commerces/component", "addComponent");

                digester.parse(file);

            } catch (Exception e) {
                LOG.error(e.getMessage(), e);
            }
    }

    public void saveConfigurationFile(ServletContext ctx) throws Exception {
        File file = new File(ctx.getRealPath(CONFIG_FILE));
        File fileBak = new File(ctx.getRealPath(CONFIG_FILE_BAK));
        if (fileBak.exists()) fileBak.delete();
        file.renameTo(fileBak);

        DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = dbfac.newDocumentBuilder();
        Document doc = docBuilder.newDocument();

        Element root = doc.createElement("commerces");
        doc.appendChild(root);


        // databases
        if (storeDb != null && !storeDb.isEmpty())
            for (Store20Database db : storeDb.values()) {
                Element nodeDatabase = doc.createElement("database");
                nodeDatabase.setAttribute("id", db.getId());
                root.appendChild(nodeDatabase);

                Element nodeType = doc.createElement("type");
                nodeType.appendChild(doc.createTextNode(db.getType()));
                nodeDatabase.appendChild(nodeType);

                Element nodeUrl = doc.createElement("url");
                nodeUrl.appendChild(doc.createTextNode(db.getUrl()));
                nodeDatabase.appendChild(nodeUrl);

                Element nodeUser = doc.createElement("user");
                nodeUser.appendChild(doc.createTextNode(db.getUser()));
                nodeDatabase.appendChild(nodeUser);

                Element nodePass = doc.createElement("password");
                nodePass.appendChild(doc.createTextNode(db.getPassword()));
                nodeDatabase.appendChild(nodePass);

                if (db.getProperties() != null && !db.getProperties().isEmpty())
                    for (Map.Entry e : db.getProperties().entrySet()) {
                        Element nodeProperty = doc.createElement("property");
                        nodeProperty.setAttribute("name", (String) e.getKey());
                        nodeProperty.appendChild(doc.createTextNode((String) e.getValue()));
                        nodeDatabase.appendChild(nodeProperty);
                    }
            }

        // comercios
        if (storeMap != null && !storeMap.isEmpty())
            for (Store20Commerce commerce : storeMap.values()) {
                Element nodeCommerce = doc.createElement("commerce");
                nodeCommerce.setAttribute("id", commerce.getId());
                root.appendChild(nodeCommerce);

                Element nodeDatabase = doc.createElement("database");
                nodeDatabase.appendChild(doc.createTextNode(commerce.getDatabase()));
                nodeCommerce.appendChild(nodeDatabase);

                for (Store20Commerce.StoreUrl url : commerce.getUrls()) {
                    Element nodeUrl = doc.createElement("url");
                    nodeUrl.setAttribute("domain", url.getDomain());
                    nodeUrl.setAttribute("path", url.getPath());
                    nodeCommerce.appendChild(nodeUrl);
                }
            }

        // componentes
        if (storeComponents != null && !storeComponents.isEmpty()) {
            for (Store20Component component : storeComponents.values()) {
                Element nodeComponent = doc.createElement("component");
                nodeComponent.setAttribute("name", component.getName());
                nodeComponent.setAttribute("value", component.getValue());
                root.appendChild(nodeComponent);
            }
        }

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer trans = transformerFactory.newTransformer();
        trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        trans.setOutputProperty(OutputKeys.INDENT, "yes");

        StreamResult result = new StreamResult(file);
        DOMSource source = new DOMSource(doc);
        trans.transform(source, result);
    }

    public void addDatabase(Store20Database db) {
        db.setStore20Config(this);
        if (storeDb == null) storeDb = new HashMap<String, Store20Database>();
        storeDb.put(db.getId(), db);
    }

    public void addCommerce(Store20Commerce commerce) {
        if (storeMap == null) storeMap = new HashMap<String, Store20Commerce>();
        storeMap.put(commerce.getId(), commerce);
    }

    public void addComponent(Store20Component component) {
        if (storeComponents == null) storeComponents = new HashMap<String, Store20Component>();
        storeComponents.put(component.getName(), component);
    }

    public void addBlock(TemplateBlock block) {
        if (!this.extraBlocks.containsKey(block.getCode())) this.extraBlocks.put(block.getCode(), block);
    }

    public void addBanner(TemplateBannerZone banner) {
        if (!this.extraBanners.containsKey(banner.getCode())) this.extraBanners.put(banner.getCode(), banner);
    }

    public Map<String, Class> getMapSuplier() {
        return mapSuplier;
    }

    public Map<String, Class> getMapCarrier() {
        return mapCarrier;
    }

    public Map<String, Class> getMapMerchants() {
        return mapMerchants;
    }

    public Map<String, Class> getMapJobs() {
        return mapJobs;
    }

    public Collection<TemplateBlock> getExtraBlocks() {
        return extraBlocks.values();
    }

    public Collection<TemplateBannerZone> getExtraBanners() {
        return extraBanners.values();
    }

    public Map<String, Class> getMapEvents() {
        return mapEvents;
    }

    public Map<String, List<PluginAdminMenu>> getMenus() {
        return menus;
    }

    public Map<String, Store20Commerce> getStoreMap() {
        if (storeMap == null) storeMap = new HashMap<String, Store20Commerce>();
        return storeMap;
    }

    public void setStoreMap(Map<String, Store20Commerce> storeMap) {
        this.storeMap = storeMap;
    }

    public Map<String, Store20Database> getStoreDb() {
        if (storeDb == null) storeDb = new HashMap<String, Store20Database>();
        return storeDb;
    }

    public void setStoreDb(Map<String, Store20Database> storeDb) {
        this.storeDb = storeDb;
    }

    public Map<String, Store20Component> getStoreComponents() {
        return storeComponents;
    }

    public String getComponentValue(String name) {
        return (storeComponents != null && !storeComponents.isEmpty() && storeComponents.containsKey(name)) ? storeComponents.get(name).getValue() : null;
    }

    public Object getComponentInstance(String name) {
        String className = getComponentValue(name);
        if (StringUtils.isNotEmpty(className)) {
            try {
                Class cl = Class.forName(className);
                if (cl != null) {
                    return cl.newInstance();
                }
            } catch (Exception e) {
                LOG.error(e.getMessage(), e);
            }
        }
        return null;
    }

    public List<Class> getReports() {
        return reports;
    }

    public void setReports(List<Class> reports) {
        this.reports = reports;
    }

    public List<Class> getExtraBeans() {
        return extraBeans;
    }

    public void setExtraBeans(List<Class> extraBeans) {
        this.extraBeans = extraBeans;
    }
}
