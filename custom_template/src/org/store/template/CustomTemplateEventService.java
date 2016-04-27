package org.store.template;

import org.store.core.dao.HibernateDAO;
import org.store.core.globals.BaseAction;
import org.store.core.utils.events.DefaultEventServiceImpl;
import org.hibernate.Session;

import javax.servlet.ServletContext;

/**
 * Rogelio Caballero
 * 9/01/12 18:06
 */
public class CustomTemplateEventService extends DefaultEventServiceImpl {
    private static final String PLUGIN_NAME = "custom_template";


    @Override
    public String getName() {
        return PLUGIN_NAME;
    }

    @Override
    public Boolean beforeAction(ServletContext ctx, BaseAction action) {
        if (action.getTemplate().startsWith("custom_")) {
            action.addToStack("decorator", new TemplateDecorator(action));
            return true;
        }
        return super.beforeAction(ctx, action);
    }

    @Override
    public void initialize(ServletContext ctx, Session databaseSession, String store) {
        HibernateDAO dao = new HibernateDAO(databaseSession, store);
        String[] languages = dao.getLanguages();
        initializeLabel(dao, "zone.parameter.bannerZone","Banner Zone", languages);
        initializeLabel(dao, "zone.parameter.noOfBanners","# of banners", languages);
        initializeLabel(dao, "zone.parameter.blockName","Block name", languages);
        super.initialize(ctx, databaseSession, store);
    }
}
