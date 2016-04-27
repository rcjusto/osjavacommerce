package org.store.campaigns;

import org.store.campaigns.beans.Campaign;
import org.store.campaigns.beans.CampaignCondition;
import org.store.campaigns.beans.CampaignUser;
import org.store.campaigns.beans.CampaignUserClick;
import org.store.campaigns.beans.DesignedMail;
import org.store.core.beans.User;
import org.store.campaigns.beans.UserGroup;
import org.store.campaigns.beans.UserGroupMember;
import org.store.core.dao.HibernateDAO;
import org.store.core.front.FrontModuleAction;
import org.store.core.front.UserAction;
import org.store.core.globals.BaseAction;
import org.store.core.globals.LinkUrl;
import org.store.core.globals.SomeUtils;
import org.store.core.globals.config.Store20Config;
import org.store.core.utils.PluginAdminMenu;
import org.store.core.utils.events.DefaultEventServiceImpl;
import org.store.core.utils.events.EventService;
import org.apache.commons.lang.math.NumberUtils;
import org.hibernate.Session;

import javax.servlet.ServletContext;
import java.util.Calendar;
import java.util.Map;

/**
 * Rogelio Caballero
 * 17/12/11 16:45
 */
public class CampaignEventService extends DefaultEventServiceImpl {
    private static final String PLUGIN_NAME = "campaign_designer";

    @Override
    public String getName() {
        return PLUGIN_NAME;
    }

    @Override
    public Boolean beforeAction(ServletContext ctx, BaseAction action) {
        if (action.getRequest().getParameter("campaign") != null) {
            CampaignUser campaign = (CampaignUser) action.getDao().get(CampaignUser.class, NumberUtils.createLong(action.getRequest().getParameter("campaign")));
            if (campaign != null) {
                if (campaign.getClicked() == null || campaign.getOpened() == null) {
                    if (campaign.getOpened() == null) campaign.setOpened(Calendar.getInstance().getTime());
                    if (campaign.getClicked() == null) campaign.setClicked(Calendar.getInstance().getTime());
                    action.getDao().save(campaign);
                }

                LinkUrl url = new LinkUrl(action.getThisUrl());
                url.delParameters("campaign");
                CampaignUserClick cuc = new CampaignUserClick(campaign);
                cuc.setUrl(url.toString());
                action.getDao().save(cuc);
            }
        }
        return null;
    }

    @Override
    public Boolean onExecuteEvent(ServletContext ctx, int eventType, FrontModuleAction action, Map<String, Object> map) {
        if (EventService.EVENT_REGISTER == eventType && action instanceof UserAction) {
            String[] arrGroups = action.getRequest().getParameterValues("addToGroup");
            if (arrGroups != null && arrGroups.length > 0) {
                User user = ((UserAction) action).getUser();
                if (user != null) {
                    for(String gName : arrGroups) {
                        UserGroup group = (UserGroup) action.getDao().get(UserGroup.class, SomeUtils.strToLong(gName));
                        if (group!=null) {
                            UserGroupMember ugm = new UserGroupMember();
                            ugm.setGroup(group);
                            ugm.setUser(user);
                            action.getDao().save(ugm);
                        }
                    }
                }
            }
            return true;
        }
        return null;
    }

    @Override
    public void initialize(ServletContext ctx, Session databaseSession, String store) {
        Store20Config config = Store20Config.getInstance(ctx);
        PluginAdminMenu menu0 = new PluginAdminMenu();
        menu0.setMenuParent(PluginAdminMenu.PARENT_CUSTOMERS);
        menu0.setMenuLabel("admin.menu.mail.designer");
        menu0.setMenuText("Mail Designer");
        menu0.setMenuAction("designedmaillist");
        config.addPluginAdminMenu(menu0);

        PluginAdminMenu menu2 = new PluginAdminMenu();
        menu2.setMenuParent(PluginAdminMenu.PARENT_CUSTOMERS);
        menu2.setMenuLabel("admin.menu.campaigns");
        menu2.setMenuAction("campaignlist");
        config.addPluginAdminMenu(menu2);

        PluginAdminMenu menu1 = new PluginAdminMenu();
        menu1.setMenuParent(PluginAdminMenu.PARENT_CUSTOMERS);
        menu1.setMenuLabel("admin.menu.user.groups");
        menu1.setMenuAction("customergrouplist");
        config.addPluginAdminMenu(menu1);

        HibernateDAO dao = new HibernateDAO(databaseSession, store);
        String[] langArr = dao.getLanguages();
        initializeLabel(dao, "admin.campaign.mail.list", "List of Designed Mail", langArr);
        initializeLabel(dao, "admin.campaign.mail.new", "Design New Mail", langArr);
        initializeLabel(dao, "admin.campaign.mail.modify", "Modify Mail", langArr);
        initializeLabel(dao, "mail.preview.id", "Mail Preview. ID", langArr);
        initializeLabel(dao, "campaign.mail.designer.help", "Pase el puntero del mouse sobre cada bloque de la plantilla de correo para modificar su contenido. Los botones disponibles para cada fila o bloque de la plantilla son los siguentes:", langArr);
        initializeLabel(dao, "please.select.email", "Please, select an email.", langArr);
        initializeLabel(dao, "admin.campaign.mail.select", "Move the mouse over table rows to see a preview. Click to select.", langArr);
    }

    @Override
    public void initializePlugin(ServletContext ctx) {
        Store20Config config = Store20Config.getInstance(ctx);
        config.registerBean(Campaign.class);
        config.registerBean(CampaignCondition.class);
        config.registerBean(CampaignUser.class);
        config.registerBean(CampaignUserClick.class);
        config.registerBean(DesignedMail.class);
        config.registerBean(UserGroup.class);
        config.registerBean(UserGroupMember.class);
    }
}
