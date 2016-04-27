package org.store.flashpromo;

import org.store.flashpromo.beans.FlashPageConfig;
import org.store.flashpromo.beans.FlashPromoConfig;
import org.store.core.beans.utils.RandomOrder;
import org.store.core.dao.HibernateDAO;
import org.store.core.globals.BaseAction;
import org.store.core.globals.StoreActionMapping;
import org.store.core.globals.config.Store20Config;
import org.store.core.utils.PluginAdminMenu;
import org.store.core.utils.events.DefaultEventServiceImpl;
import org.store.flashpromo.beans.FlashPromoConfigLang;
import org.apache.commons.lang.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import javax.servlet.ServletContext;
import javax.servlet.http.Cookie;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Rogelio Caballero
 * 11/01/12 17:34
 */
public class FlashPromoEventService extends DefaultEventServiceImpl {
    private static final String PLUGIN_NAME = "flash_promo";
    private static final String EDIT_PAGE_ACTION = "flash_page_edit";

    @Override
    public String getName() {
        return PLUGIN_NAME;
    }

    @Override
    public Boolean beforeAction(ServletContext ctx, BaseAction action) {
        String content = getContent(action);
        if (StringUtils.isNotEmpty(content)) addContentToView(action, CONTENT_IN_BOTTOM, content);
        return true;
    }

    @Override
    public void initializePlugin(ServletContext ctx) {
        Store20Config config = Store20Config.getInstance(ctx);
        config.registerBean(FlashPageConfig.class);
        config.registerBean(FlashPromoConfig.class);
        config.registerBean(FlashPromoConfigLang.class);
    }

    @Override
    public void initialize(ServletContext ctx, Session databaseSession, String store) {
        Store20Config config = Store20Config.getInstance(ctx);
        PluginAdminMenu menu0 = new PluginAdminMenu();
        menu0.setMenuParent(PluginAdminMenu.PARENT_CUSTOMERS);
        menu0.setMenuLabel("admin.menu.promotional.pop-up");
        menu0.setMenuText("Promotional Pop-up");
        menu0.setMenuAction(EDIT_PAGE_ACTION);
        config.addPluginAdminMenu(menu0);

        HibernateDAO dao = new HibernateDAO(databaseSession, store);
        String[] langArr = dao.getLanguages();
        initializeLabel(dao, "fp.config.title", "Promotional Pop-up Configuration", langArr);
        initializeLabel(dao, "fb.configure.new.popup", "Add pop-up to page", langArr);
        initializeLabel(dao, "fp.page.name", "Page", langArr);
        initializeLabel(dao, "fp.modal", "Modal", langArr);
        initializeLabel(dao, "fp.width", "Width", langArr);
        initializeLabel(dao, "fp.position", "Position", langArr);
        initializeLabel(dao, "fp.only.once", "Only Once", langArr);
        initializeLabel(dao, "fp.show.after", "Show after", langArr);
        initializeLabel(dao, "fp.hide.after", "Hide after", langArr);
        initializeLabel(dao, "fp.popup.content", "Pop-up Content", langArr);
        initializeLabel(dao, "fp.popup.content.list", "Pop-up Content List", langArr);
        initializeLabel(dao, "fp.subscription", "Add Subscription", langArr);
        initializeLabel(dao, "fp.configure.new.promo", "Configure New Pop-up Content", langArr);
        initializeLabel(dao, "fb.promo.edit.content", "Modify Pop-up Content", langArr);
        initializeLabel(dao, "fp.page.config.title", "Configure Pop-up in Pages", langArr);
        initializeLabel(dao, "fp.promo.config.title", "Configure Pop-up Contents", langArr);
        initializeLabel(dao, "fp.add.user.to.group", "Add user to group..", langArr);
        initializeLabel(dao, "fp.select.page", "Select Page", langArr);
        initializeLabel(dao, "fp.subscription.email", "Your email address", langArr);
        initializeLabel(dao, "fp.subscription.send", "Send", langArr);
        initializeLabel(dao, "fp.sending.subscription", "Sending subscription...", langArr);
        initializeLabel(dao, "fp.registration.ok", "Subscription Successfully", langArr);
    }

    private String getContent(BaseAction action) {
        StoreActionMapping sm = action.getStoreActionMapping();
        if (sm != null) {
            FlashPageConfig page = getActivePage(action, sm.getName());
            if (page != null && page.getActive() && page.getPromo() != null) {
                String content = page.getPromo().getContent(action.getLocale().getLanguage());
                if (StringUtils.isNotEmpty(content)) {
                    Map<String, Object> map = new HashMap<String, Object>();
                    map.put("config", page);
                    map.put("cookieName", getCookieName(page));
                    map.put("content", action.proccessVelocityText(content, map));
                    String template = "/WEB-INF/views/" + action.getTemplate() +"/front/flashpromo.vm";
                    return action.proccessVelocityTemplate(template, map);
                }
            }
        }
        return null;
    }

    private FlashPageConfig getActivePage(BaseAction action, String name) {
        // listado de promo activas para la pagina ordenadas aleatoriamente
        List<FlashPageConfig> l = action.getDao().createCriteriaForStore(FlashPageConfig.class)
                .add(Restrictions.eq("page", name))
                .add(Restrictions.eq("active", Boolean.TRUE))
                .setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY)
                .addOrder(RandomOrder.random())
                .list();
        if (l != null) {
            for (FlashPageConfig c : l) {
                if (!c.getOnlyOnce() || notReaded(action, c)) return c;
            }
        }
        return null;
    }

    private boolean notReaded(BaseAction action, FlashPageConfig bean) {
        if (action.getRequest().getCookies() != null && action.getRequest().getCookies().length > 0) {
            String cookieName = getCookieName(bean);
            for (Cookie c : action.getRequest().getCookies()) {
                if (cookieName.equalsIgnoreCase(c.getName()) && "y".equalsIgnoreCase(c.getValue())) return false;
            }
        }
        return true;
    }

    private String getCookieName(FlashPageConfig bean) {
        return "promo_popup_" + bean.getId();
    }



}
