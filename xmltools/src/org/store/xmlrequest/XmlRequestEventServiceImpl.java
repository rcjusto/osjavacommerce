package org.store.xmlrequest;

import org.store.core.beans.StoreProperty;
import org.store.core.dao.HibernateDAO;
import org.store.core.globals.config.Store20Config;
import org.store.core.utils.PluginAdminMenu;
import org.store.core.utils.events.DefaultEventServiceImpl;
import org.store.xmlrequest.admin.XMLToolAction;
import org.hibernate.Session;

import javax.servlet.ServletContext;

public class XmlRequestEventServiceImpl extends DefaultEventServiceImpl {

    public static final String PLUGIN_NAME = "xmlrequests";

    public String getName() {
        return PLUGIN_NAME;
    }

    public void initialize(ServletContext ctx, Session databaseSession, String store) {
        // generate menu item
        PluginAdminMenu menu = new PluginAdminMenu();
        menu.setMenuParent(PluginAdminMenu.PARENT_CATALOG);
        menu.setMenuLabel("menu.xml.requests");
        menu.setMenuText("XML Requests Configuration");
        menu.setMenuAction(XMLToolAction.CONFIG_ACTION);
        Store20Config.getInstance(ctx).addPluginAdminMenu(menu);

        // generate default labels
        HibernateDAO dao = new HibernateDAO(databaseSession, store);
        String[] languages = dao.getStorePropertyValue(StoreProperty.PROP_LANGUAGES, StoreProperty.TYPE_GENERAL, "en").split(",");
        initializeLabel(dao, "xmlrequest.label.enabled", "Enabled", languages);
        initializeLabel(dao, "xmlrequest.label.need.credentials", "Credentials Required", languages);
        initializeLabel(dao, "xmlrequest.label.max.items.for.request", "Maximum Items Allowed For Request", languages);
    }

}
