package org.store.locationsmap.front;

import org.hibernate.Session;
import org.store.core.beans.StoreProperty;
import org.store.core.dao.HibernateDAO;
import org.store.core.globals.config.Store20Config;
import org.store.core.utils.PluginAdminMenu;
import org.store.core.utils.events.DefaultEventServiceImpl;
import org.store.core.utils.templates.TemplateBlock;
import org.store.locationsmap.admin.ConfigAction;

import javax.servlet.ServletContext;

/**
 * @author rcaballero
 */

public class LocationsMapEventServiceImpl extends DefaultEventServiceImpl {

    public static final String PLUGIN_NAME = "locationsmap";

    @Override
    public String getName() {
        return PLUGIN_NAME;
    }

    public void initialize(ServletContext ctx, Session databaseSession, String store) {
        Store20Config storeConfig = Store20Config.getInstance(ctx);

        // generate menu item
        PluginAdminMenu menu = new PluginAdminMenu();
        menu.setMenuParent(PluginAdminMenu.PARENT_CMS);
        menu.setMenuLabel("menu.locations.map");
        menu.setMenuText("Locations Map Configuration");
        menu.setMenuAction(ConfigAction.CONFIG_LOAD_ACTION);
        storeConfig.addPluginAdminMenu(menu);

        // generate default labels
        HibernateDAO dao = new HibernateDAO(databaseSession, store);
        String[] languages = dao.getStorePropertyValue(StoreProperty.PROP_LANGUAGES, StoreProperty.TYPE_GENERAL, "en").split(",");
        initializeLabel(dao, "locationsmap.label.apikey", "API Key", languages);
        initializeLabel(dao, "locationsmap.label.center", "Center", languages);
        initializeLabel(dao, "locationsmap.label.zoom", "Zoom", languages);
        initializeLabel(dao, "locationsmap.label.data", "Data", languages);

        // Generate 2 blocks for the front page
        TemplateBlock block1 = new TemplateBlock("locations.map.top");
        for(String lang : languages) block1.setName(lang, "Locations Map Top");
        storeConfig.addBlock(block1);
        TemplateBlock block2 = new TemplateBlock("locations.map.bottom");
        for(String lang : languages) block2.setName(lang, "Locations Map Bottom");
        storeConfig.addBlock(block2);

    }

}
