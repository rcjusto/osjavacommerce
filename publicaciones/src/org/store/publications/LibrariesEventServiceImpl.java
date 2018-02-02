package org.store.publications;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.struts2.json.JSONException;
import org.apache.struts2.json.JSONUtil;
import org.hibernate.Session;
import org.store.core.admin.AdminModuleAction;
import org.store.core.admin.OrderAction;
import org.store.core.beans.*;
import org.store.core.beans.mail.MAvailableLinks;
import org.store.core.beans.mail.MOrder;
import org.store.core.beans.mail.MUser;
import org.store.core.dao.HibernateDAO;
import org.store.core.front.FrontModuleAction;
import org.store.core.globals.BaseAction;
import org.store.core.globals.StoreMessages;
import org.store.core.globals.config.Store20Config;
import org.store.core.mail.MailSenderThreat;
import org.store.core.utils.PluginAdminMenu;
import org.store.core.utils.events.DefaultEventServiceImpl;
import org.store.core.utils.events.EventService;

import javax.servlet.ServletContext;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LibrariesEventServiceImpl extends DefaultEventServiceImpl {

    public static Logger log = Logger.getLogger(LibrariesEventServiceImpl.class);
    public static final String PROP_CONFIGURATION_PROPERTY = "store.plugin.digitalbooks.configuration";
    private static final String PROP_HEADER_TEXT = "store.plugin.publications.header";
    private static final String PROP_HEADER_TEXT_DEFAULT = "Integration with Libranda, DigitalBooks and Publidisa";

    private static final String ONIX_PROCESS_MANUAL_ACTION = "onix_manual_start";
    private static final String ONIX_REMOVE_MANUAL_ACTION = "onix_manual_remove";

    public static final String PLUGIN_NAME = "publications";
    public static final String FOLDER_LIBRANDA = "libranda";
    public static final String FOLDER_DIGITALBOOKS = "digitalbooks";
    public static final String FOLDER_PUBLIDISA = "publidisa";

    public String getName() {
        return PLUGIN_NAME;
    }

    public String getDescription(FrontModuleAction action) {
        return action.getText(PROP_HEADER_TEXT, PROP_HEADER_TEXT_DEFAULT);
    }

    public Boolean onExecuteEvent(ServletContext ctx, int eventType, FrontModuleAction action, Map<String, Object> map) {
        if (EventService.EVENT_APPROVE_ORDER == eventType && map != null && map.containsKey("order")) {
            try {
                onOrderSaved(action, (Order) map.get("order"));
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
            return true;
        }
        return false;
    }

    public Boolean onExecuteAdminEvent(ServletContext ctx, int eventType, AdminModuleAction action, Map<String, Object> map) {
        if (EventService.EVENT_CUSTOM_ACTION == eventType && map.containsKey("actionName")) {
            if (ONIX_PROCESS_MANUAL_ACTION.equalsIgnoreCase((String) map.get("actionName"))) {
                String supplier = action.getRequest().getParameter("supplier");
                if ("LIBRANDA".equalsIgnoreCase(supplier)) {
                    org.store.publications.libranda.ONIXProcessor processor = new org.store.publications.libranda.ONIXProcessor(action);
                    processor.initialize();
                    File path = getONIXFolder(action, FOLDER_LIBRANDA);
                    processor.setOnixFile(path.getAbsolutePath() + File.separator + action.getRequest().getParameter("file"));
                    processor.start();
                } else if ("DIGITALBOOKS".equalsIgnoreCase(supplier)) {
                    org.store.publications.digitalbooks.CatalogProcessor processor = new org.store.publications.digitalbooks.CatalogProcessor(action.getDatabaseConfig(), action.getStoreCode(), action.getServletContext().getRealPath("/"));
                    File path = getONIXFolder(action, FOLDER_LIBRANDA);
                    processor.setONIXFile(new File(path.getAbsolutePath() + File.separator + action.getRequest().getParameter("file")));
                    processor.start();
                } else if ("PUBLIDISA".equalsIgnoreCase(supplier)) {
                    org.store.publications.publidisa.CatalogProcessor processor = new org.store.publications.publidisa.CatalogProcessor(action.getDatabaseConfig(), action.getStoreCode(), action.getServletContext().getRealPath("/"));
                    File path = getONIXFolder(action, FOLDER_LIBRANDA);
                    processor.setONIXFile(new File(path.getAbsolutePath() + File.separator + action.getRequest().getParameter("file")));
                    processor.start();
                }
                return true;
            }
        } else if (EventService.EVENT_ADMIN_CHANGE_ORDER_STATUS == eventType && action instanceof OrderAction) {
            onOrderSaved(action, ((OrderAction) action).getOrder());
            return true;
        }
        return false;
    }

    // set order and get link
    private void onOrderSaved(BaseAction action, Order order) {
        if (order != null && OrderStatus.STATUS_APPROVED.equalsIgnoreCase(order.getStatus().getStatusCode())) {
            Map config = getConfiguration(action);
            List<OrderDetailProduct> newLinks = new ArrayList<OrderDetailProduct>();
            // LIBRANDA
            List<OrderDetailProduct> linksL = null;
            try {
                org.store.publications.libranda.OrderProcessor opL = new org.store.publications.libranda.OrderProcessor(action.getDao(), config);
                linksL = opL.processOrder(order.getIdOrder());
                if (linksL != null && !linksL.isEmpty()) newLinks.addAll(linksL);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
            // DIGITAL BOOKS
            try {
                org.store.publications.digitalbooks.OrderProcessor opD = new org.store.publications.digitalbooks.OrderProcessor(action.getDao(), config);
                List<OrderDetailProduct> linksD = opD.processOrder(order.getIdOrder());
                if (linksD != null && !linksD.isEmpty()) newLinks.addAll(linksL);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
            // PUBLIDISA
            try {
                org.store.publications.publidisa.OrderProcessor opP = new org.store.publications.publidisa.OrderProcessor(action.getDao(), config);
                List<OrderDetailProduct> linksP = opP.processOrder(order.getIdOrder());
                if (linksP != null && !linksP.isEmpty()) newLinks.addAll(linksL);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }

            // enviar correo con enlaces
            if (!newLinks.isEmpty()) {
                Map<String, Object> map = new HashMap<String, Object>();
                map.put("order", new MOrder(order, action));
                map.put("user", new MUser(order.getUser(), action));
                map.put("links",  new MAvailableLinks(newLinks, action));
                String body = action.proccessVelocityTemplate(Mail.MAIL_TEMPLATE_AVAILABLE_LINKS, map);
                Mail mail = new Mail();
                mail.setInventaryCode(action.getStoreCode());
                mail.setBody(body);
                mail.setSubject(action.getText(StoreMessages.CNT_SUBJECT_LINKS_AVAILABLE, StoreMessages.CNT_DEFAULT_SUBJECT_LINKS_AVAILABLE));
                mail.setToAddress(order.getUser().getEmail());
                mail.setPriority(Mail.PRIORITY_HIGH);
                mail.setReference("NEW_LINKS " + order.getIdOrder());
                action.getDao().save(mail);
                MailSenderThreat.asyncSendMail(mail, action);
            }
        }
    }


    public void loadConfigurationData(BaseAction action) {
        Map data = getConfiguration(action);
        if (data != null) action.addToStack("config", data);

        // urls
        action.addToStack("urlStart", action.url(ONIX_PROCESS_MANUAL_ACTION, "admin"));
        action.addToStack("urlDelFile", action.url(ONIX_REMOVE_MANUAL_ACTION, "admin"));

        // read available onix files
        // LIBRANDA
        File pathL = getONIXFolder(action, FOLDER_LIBRANDA);
        File[] onixFilesL = pathL.listFiles((FilenameFilter) new SuffixFileFilter(".onix"));
        if (onixFilesL != null && onixFilesL.length > 0) action.addToStack("libranda_files", onixFilesL);

        // DIGITAL BOOKS
        File pathD = getONIXFolder(action, FOLDER_DIGITALBOOKS);
        File[] onixFilesD = pathD.listFiles((FilenameFilter) new SuffixFileFilter(".zip"));
        if (onixFilesD != null && onixFilesD.length > 0) action.addToStack("digitalbooks_files", onixFilesD);

        // PUBLIDISA
        File pathP = getONIXFolder(action, FOLDER_PUBLIDISA);
        File[] onixFilesP = pathP.listFiles((FilenameFilter) new SuffixFileFilter(".zip"));
        if (onixFilesP != null && onixFilesP.length > 0) action.addToStack("publidisa_files", onixFilesP);
    }

    public void saveConfigurationData(BaseAction action) {
        Map<String, Serializable> map = getConfiguration(action);
        if (map == null) map = new HashMap<String, Serializable>();
        String supplier = action.getRequest().getParameter("supplier");
        if ("CATEGORIES".equalsIgnoreCase(supplier)) {
            map.put("category_digital", action.getRequest().getParameter("category_digital"));
            map.put("category_paper", action.getRequest().getParameter("category_paper"));
        } else if ("LIBRANDA".equalsIgnoreCase(supplier)) {
            //LIBRANDA
            map.put("libranda_enabled", "yes".equalsIgnoreCase(action.getRequest().getParameter("libranda_enabled")));
            map.put("libranda_gencode", action.getRequest().getParameter("libranda_gencode"));
            map.put("libranda_password", action.getRequest().getParameter("libranda_password"));
            map.put("libranda_outletName", action.getRequest().getParameter("libranda_outletName"));
        } else if ("DIGITALBOOKS".equalsIgnoreCase(supplier)) {
            // DIGITAL BOOKS
            map.put("digitalbooks_enabled", "yes".equalsIgnoreCase(action.getRequest().getParameter("digitalbooks_enabled")));
            map.put("digitalbooks_user", action.getRequest().getParameter("digitalbooks_user"));
            map.put("digitalbooks_password", action.getRequest().getParameter("digitalbooks_password"));
            map.put("digitalbooks_elibrary", action.getRequest().getParameter("digitalbooks_elibrary"));
            map.put("digitalbooks_country", action.getRequest().getParameter("digitalbooks_country"));
        } else if ("PUBLIDISA".equalsIgnoreCase(supplier)) {
            // PUBLIDISA
            map.put("publidisa_enabled", "yes".equalsIgnoreCase(action.getRequest().getParameter("publidisa_enabled")));
            map.put("publidisa_login", action.getRequest().getParameter("publidisa_login"));
            map.put("publidisa_userCode", action.getRequest().getParameter("publidisa_userCode"));
            map.put("publidisa_password", action.getRequest().getParameter("publidisa_password"));
            map.put("publidisa_downloadPreview", StringUtils.isNotEmpty(action.getRequest().getParameter("publidisa_downloadPreview")) ? action.getRequest().getParameter("publidisa_downloadPreview") : "");
            map.put("publidisa_processDigital", StringUtils.isNotEmpty(action.getRequest().getParameter("publidisa_processDigital")) ? action.getRequest().getParameter("publidisa_processDigital") : "");
            map.put("publidisa_processPaper", StringUtils.isNotEmpty(action.getRequest().getParameter("publidisa_processPaper")) ? action.getRequest().getParameter("publidisa_processPaper") : "");
        }
        try {
            String cad = JSONUtil.serialize(map);
            StoreProperty bean = action.getDao().getStoreProperty(PROP_CONFIGURATION_PROPERTY, StoreProperty.TYPE_GENERAL, true);
            bean.setValue(cad);
            action.getDao().save(bean);
        } catch (JSONException e) {
            log.error(e.getMessage(), e);
        }
    }

    public void initialize(ServletContext ctx, Session databaseSession, String store) {
        // generate menu item
        PluginAdminMenu menu = new PluginAdminMenu();
        menu.setMenuParent(PluginAdminMenu.PARENT_CATALOG);
        menu.setMenuLabel("menu.publications");
        menu.setMenuText("Publications Configuration");
        menu.setMenuAction(PluginAdminMenu.EDIT_PROPERTIES_ACTION);
        menu.addActionParameter(PluginAdminMenu.EDIT_PROPERTIES_PARAM_NAME, getName());
        Store20Config.getInstance(ctx).addPluginAdminMenu(menu);

        // generate default labels
        HibernateDAO dao = new HibernateDAO(databaseSession, store);
        String[] languages = dao.getStorePropertyValue(StoreProperty.PROP_LANGUAGES, StoreProperty.TYPE_GENERAL, "en").split(",");
        initializeLabel(dao, "libranda.section.onix", "Process ONIX Files", languages);
        initializeLabel(dao, "libranda.label.select.file", "Select a file to process", languages);
        initializeLabel(dao, "libranda.label.delete.file", "Delete file", languages);
        initializeLabel(dao, "libranda.label.process.file", "Process File", languages);

    }

    public String getConfigurationTemplate() {
        return "/WEB-INF/views/org/store/publications/views/config.vm";
    }

    private File getONIXFolder(BaseAction action, String folder) {
        File f = new File(action.getServletContext().getRealPath("/stores/" + action.getStoreCode() + "/publications/" + folder));
        if (!f.exists()) try {
            FileUtils.forceMkdir(f);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
        return f;
    }

    private Map getConfiguration(BaseAction action) {
        StoreProperty bean = action.getDao().getStoreProperty(PROP_CONFIGURATION_PROPERTY, StoreProperty.TYPE_GENERAL);
        if (bean != null && StringUtils.isNotEmpty(bean.getValue())) {
            try {
                Object o = JSONUtil.deserialize(bean.getValue());
                if (o != null && o instanceof Map) {
                    return (Map) o;
                }
            } catch (JSONException e) {
                log.error(e.getMessage(), e);
            }
        }
        return null;
    }

    public static void setInternalOrderId(Order order, String supplier, String supplierId) {
        if (supplierId == null) supplierId = "";
        Map<String, String> map = null;
        try {
            map = (StringUtils.isNotEmpty(order.getSupplierId())) ? (Map<String, String>) JSONUtil.deserialize(order.getSupplierId()) : null;
        } catch (JSONException ignored) {
        }
        if (map == null) map = new HashMap<String, String>();
        map.put(supplier, supplierId);
        try {
            order.setSupplierId(JSONUtil.serialize(map));
        } catch (JSONException ignored) {
        }
    }

    public static String getInternalOrderId(Order order, String supplier) {
        Map<String, String> map = null;
        try {
            map = (StringUtils.isNotEmpty(order.getSupplierId())) ? (Map<String, String>) JSONUtil.deserialize(order.getSupplierId()) : null;
        } catch (JSONException ignored) {
        }
        return (map != null && map.containsKey(supplier)) ? map.get(supplier) : null;
    }

}
