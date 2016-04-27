package org.store.core.utils.events.rewards;

import org.store.core.admin.AdminModuleAction;
import org.store.core.beans.Mail;
import org.store.core.beans.Order;
import org.store.core.beans.OrderStatus;
import org.store.core.beans.StoreProperty;
import org.store.core.beans.User;
import org.store.core.beans.UserRewardHistory;
import org.store.core.beans.mail.MUser;
import org.store.core.dao.HibernateDAO;
import org.store.core.front.FrontModuleAction;
import org.store.core.globals.BaseAction;
import org.store.core.globals.config.Store20Config;
import org.store.core.mail.MailSenderThreat;
import org.store.core.utils.PluginAdminMenu;
import org.store.core.utils.events.DefaultEventServiceImpl;
import org.store.core.utils.events.EventService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.log4j.Logger;
import org.apache.struts2.json.JSONException;
import org.apache.struts2.json.JSONUtil;
import org.hibernate.Session;

import javax.servlet.ServletContext;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RewardsEventServiceImpl extends DefaultEventServiceImpl {
    public static Logger log = Logger.getLogger(RewardsEventServiceImpl.class);
    private static final String PROP_REWARD_PROPERTY = "store.plugin.reward.configuration";
    private static final String PROP_HEADER_TEXT = "store.plugin.reward.header";
    private static final String PROP_HEADER_TEXT_DEFAULT = "The Reward Points Program allows customers to earn points for certain actions they take on the site. Points are awarded based on making purchases and customer actions such as registration.";
    private static final String USE_REWARD_ACTION = "rewardUse";
    private static final String CNT_SUBJECT_FRIEND_REGISTERED = "subject.friend.registered";
    private static final String CNT_DEFAULT_SUBJECT_FRIEND_REGISTERED = "Friend has been registered";
    private static final String CNT_SUBJECT_FRIEND_PURCHASE = "subject.friend.purchase";
    private static final String CNT_DEFAULT_SUBJECT_FRIEND_PURCHASE = "Friend make a purchase";

    public String getName() {
        return "rewards";
    }

    public String getDescription(FrontModuleAction action) {
        return action.getText(PROP_HEADER_TEXT, PROP_HEADER_TEXT_DEFAULT);
    }

    public Boolean onExecuteEvent(ServletContext ctx, int eventType, FrontModuleAction action, Map<String, Object> map) {
        Map config = getConfiguration(action);
        if ((action.getFrontUser() != null) && config != null && config.containsKey("enabled") && config.get("enabled") != null && Boolean.TRUE.equals(config.get("enabled"))) {

            // Acciones para asignar puntos
            if ((EventService.EVENT_REGISTER == eventType || EventService.EVENT_APPROVE_ORDER == eventType || EventService.EVENT_REFER_FRIEND == eventType)) {
                Object obj = (map != null && map.containsKey("order")) ? map.get("order") : null;
                if (EventService.EVENT_REGISTER == eventType) {
                    actionRegister(action, config);
                } else if (EventService.EVENT_APPROVE_ORDER == eventType && obj != null && obj instanceof Order) {
                    actionPurchase(action, (Order) obj, config);
                } else if (EventService.EVENT_REFER_FRIEND == eventType) {
                    Number points = (Number) config.get("pointsReferFriend");
                    String emailFriend = (map != null && map.containsKey("email")) ? (String) map.get("email") : null;
                    if (StringUtils.isNotEmpty(emailFriend) && points != null && points.intValue() > 0) {
                        action.getFrontUser().addRewardPoints(points.longValue());
                        action.getDao().save(action.getFrontUser());
                        addRewardHistory(action, action.getFrontUser(), points.doubleValue(), null, null, UserRewardHistory.TYPE_REFER_FRIEND, "");
                    }
                }
                return true;
            }

            // Accion para activar o desactivar el uso de puntos para pagar la orden
            if (EventService.EVENT_CUSTOM_ACTION == eventType && map.containsKey("actionName") && USE_REWARD_ACTION.equalsIgnoreCase((String) map.get("actionName"))) {
                String useRewards = action.getRequest().getParameter("useRewards");
                if (action.getUserSession() != null && useRewards != null) {
                    action.getUserSession().setUseRewards("Y".equalsIgnoreCase(useRewards));
                    action.getUserSession().getShoppingCart().setUseRewards("Y".equalsIgnoreCase(useRewards));
                    action.updateShoppingCartInSession();
                    map.put("result", "confirm");
                }
                return true;
            }
        }
        return false;
    }

    private void actionRegister(FrontModuleAction action, Map config) {
        Number points = (Number) config.get("pointsRegistration");
        if (points != null && points.intValue() > 0) {
            action.getFrontUser().addRewardPoints(points.longValue());
            action.getDao().save(action.getFrontUser());
            addRewardHistory(action, action.getFrontUser(), points.doubleValue(), null, null, UserRewardHistory.TYPE_REGISTERING, "");
        }

        // find referral
        User referral = action.getDao().getReferral(action.getFrontUser().getEmail());
        if (referral != null) {
            Number pointsR = (Number) config.get("pointsFriendRegister");
            if (pointsR != null && pointsR.intValue() > 0) {
                referral.addRewardPoints(pointsR.longValue());
                action.getDao().save(referral);
                addRewardHistory(action, referral, pointsR.doubleValue(), null, action.getFrontUser(), UserRewardHistory.TYPE_FRIEND_REGISTERING, "");

                // send email to referral
                if (StringUtils.isNotEmpty(referral.getEmail())) {
                    Map<String, Object> map = new HashMap<String, Object>();
                    map.put("user", new MUser(referral, action));
                    map.put("friend", new MUser(action.getFrontUser(), action));
                    map.put("points", pointsR);
                    String body = action.proccessVelocityTemplate(Mail.MAIL_TEMPLATE_FRIEND_REGISTERED, map);
                    Mail mail = new Mail();
                    mail.setInventaryCode(action.getStoreCode());
                    mail.setBody(body);
                    mail.setSubject(action.getText(CNT_SUBJECT_FRIEND_REGISTERED, CNT_DEFAULT_SUBJECT_FRIEND_REGISTERED));
                    mail.setToAddress(referral.getEmail());
                    mail.setPriority(Mail.PRIORITY_MEDIUM);
                    mail.setReference("FRIEND_REGISTERED " + referral.getIdUser());
                    action.getDao().save(mail);
                    MailSenderThreat.asyncSendMail(mail, action);
                }

            }
        }
    }

    private void actionPurchase(BaseAction action, Order order, Map config) {
        Number points = (Number) config.get("pointsApprovedOrder");
        Number amount = (Number) config.get("amountApprovedOrder");
        if (points != null && points.intValue() > 0 && amount != null && amount.intValue() > 0) {
            Integer cant = order.getTotal().intValue() / amount.intValue();
            if (cant != null && cant > 0) {
                order.getUser().addRewardPoints(points.longValue() * cant);
                action.getDao().save(order.getUser());
                addRewardHistory(action, order.getUser(), points.doubleValue() * cant, order, null, UserRewardHistory.TYPE_PURCHASE, "");
            }
        }

        // find referral
        User referral = action.getDao().getReferral(order.getUser().getEmail());
        if (referral != null) {
            Number pointsR = (Number) config.get("pointsFriendApprovedOrder");
            Number amountR = (Number) config.get("amountFriendApprovedOrder");
            if (pointsR != null && pointsR.intValue() > 0 && amountR != null && amountR.intValue() > 0) {
                Integer cant = order.getTotal().intValue() / amountR.intValue();
                if (cant != null && cant > 0) {
                    referral.addRewardPoints(pointsR.longValue() * cant);
                    action.getDao().save(referral);
                    addRewardHistory(action, referral, pointsR.doubleValue() * cant, order, order.getUser(), UserRewardHistory.TYPE_FRIEND_PURCHASE, "");

                    // send email to referral
                    if (StringUtils.isNotEmpty(referral.getEmail())) {
                        Map<String, Object> map = new HashMap<String, Object>();
                        map.put("user", new MUser(referral, action));
                        map.put("friend", new MUser(order.getUser(), action));
                        map.put("points", pointsR.longValue() * cant);
                        String body = action.proccessVelocityTemplate(Mail.MAIL_TEMPLATE_FRIEND_PURCHASE, map);
                        Mail mail = new Mail();
                        mail.setInventaryCode(action.getStoreCode());
                        mail.setBody(body);
                        mail.setSubject(action.getText(CNT_SUBJECT_FRIEND_PURCHASE, CNT_DEFAULT_SUBJECT_FRIEND_PURCHASE));
                        mail.setToAddress(referral.getEmail());
                        mail.setPriority(Mail.PRIORITY_MEDIUM);
                        mail.setReference("FRIEND_PURCHASE " + referral.getIdUser());
                        action.getDao().save(mail);
                        MailSenderThreat.asyncSendMail(mail, action);
                    }
                }
            }
        }
    }

    private void addRewardHistory(BaseAction action, User referral, Double amount, Order order, User friend, String type, String s) {
        action.addRewardHistory(referral, amount, order, friend, type, s);
    }

    public Boolean onExecuteAdminEvent(ServletContext ctx, int eventType, AdminModuleAction action, Map<String, Object> map) {
        Map config = getConfiguration(action);
        if (config != null && config.containsKey("enabled") && config.get("enabled") != null && Boolean.TRUE.equals(config.get("enabled"))) {
            action.addToStack("rewardsEnabled", Boolean.TRUE);
            Number rate = (Number) config.get("exchangeRate");
            if (rate != null) action.addToStack("rewardsRate", rate);

            Object obj = (map != null && map.containsKey("order")) ? map.get("order") : null;
            if (EventService.EVENT_ADMIN_CHANGE_ORDER_STATUS == eventType && obj != null && obj instanceof Order) {
                if (map.containsKey("new_status") && OrderStatus.STATUS_APPROVED.equalsIgnoreCase((String) map.get("new_status"))) {
                    actionPurchase(action, (Order) obj, config);
                }
            }

        }
        return null;
    }

    public Boolean beforeAction(ServletContext ctx, BaseAction action) {
        Map config = getConfiguration(action);
        if (config != null && config.containsKey("enabled") && config.get("enabled") != null && Boolean.TRUE.equals(config.get("enabled"))) {
            action.addToStack("rewardsEnabled", Boolean.TRUE);
            Number rate = (Number) config.get("exchangeRate");
            if (rate != null) action.addToStack("rewardsRate", rate);
            action.addToStack("rewardsConfig", config);
        }
        return true;
    }

    public void loadConfigurationData(BaseAction action) {
        Map data = getConfiguration(action);
        if (data != null) action.addToStack("rewardData", data);
        // lista of all admin users with email
        List<User> adminList = action.getDao().getAdminUsers();
        Collection adminWithEmail = CollectionUtils.select(adminList, new Predicate() {
            public boolean evaluate(Object o) {
                return (o instanceof User) && StringUtils.isNotEmpty(((User) o).getEmail());
            }
        });
        if (adminWithEmail != null && !adminWithEmail.isEmpty()) action.addToStack("adminWithEmail", adminWithEmail);
    }

    public void saveConfigurationData(BaseAction action) {
        StoreProperty bean = action.getDao().getStoreProperty(PROP_REWARD_PROPERTY, StoreProperty.TYPE_GENERAL, true);
        Map<String, Serializable> map = new HashMap<String, Serializable>();
        map.put("enabled", "yes".equalsIgnoreCase(action.getRequest().getParameter("enabled")));
        map.put("exchangeRate", NumberUtils.toDouble(action.getRequest().getParameter("exchangeRate"), 1d));
        map.put("pointsRegistration", NumberUtils.toInt(action.getRequest().getParameter("pointsRegistration"), 0));
        map.put("pointsReferFriend", NumberUtils.toInt(action.getRequest().getParameter("pointsReferFriend"), 0));
        map.put("pointsApprovedOrder", NumberUtils.toInt(action.getRequest().getParameter("pointsApprovedOrder"), 0));
        map.put("amountApprovedOrder", NumberUtils.toInt(action.getRequest().getParameter("amountApprovedOrder"), 0));
        map.put("pointsSubmitReview", NumberUtils.toInt(action.getRequest().getParameter("pointsSubmitReview"), 0));
        map.put("pointsSubmitPoll", NumberUtils.toInt(action.getRequest().getParameter("pointsSubmitPoll"), 0));
        map.put("pointsFriendRegister", NumberUtils.toInt(action.getRequest().getParameter("pointsFriendRegister"), 0));
        map.put("pointsFriendLink", NumberUtils.toInt(action.getRequest().getParameter("pointsFriendLink"), 0));
        map.put("amountFriendApprovedOrder", NumberUtils.toInt(action.getRequest().getParameter("amountFriendApprovedOrder"), 0));
        map.put("pointsFriendApprovedOrder", NumberUtils.toInt(action.getRequest().getParameter("pointsFriendApprovedOrder"), 0));
        map.put("contactUser", action.getRequest().getParameter("contactUser"));
        String[] welcomeTexts = action.getRequest().getParameterValues("welcomeText");
        String[] languages = action.getLanguages();
        if (welcomeTexts != null && ArrayUtils.isSameLength(welcomeTexts, languages)) {
            for (int i = 0; i < languages.length; i++)
                map.put("welcomeText_" + languages[i], (welcomeTexts[i] != null) ? welcomeTexts[i] : "");
        }
        try {
            String cad = JSONUtil.serialize(map);
            bean.setValue(cad);
            action.getDao().save(bean);
        } catch (JSONException e) {
            log.error(e.getMessage(), e);
        }
    }

    public void initialize(ServletContext ctx, Session databaseSession, String store) {
        // generate menu item
        PluginAdminMenu menu = new PluginAdminMenu();
        menu.setMenuParent(PluginAdminMenu.PARENT_CUSTOMERS);
        menu.setMenuLabel("menu.rewards");
        menu.setMenuText("Rewards Configuration");
        menu.setMenuAction(PluginAdminMenu.EDIT_PROPERTIES_ACTION);
        menu.addActionParameter(PluginAdminMenu.EDIT_PROPERTIES_PARAM_NAME, getName());
        Store20Config.getInstance(ctx).addPluginAdminMenu(menu);

        // generate default labels
        HibernateDAO dao = new HibernateDAO(databaseSession, store);
        String[] languages = dao.getStorePropertyValue(StoreProperty.PROP_LANGUAGES, StoreProperty.TYPE_GENERAL, "en").split(",");
        initializeLabel(dao, "reward.label.enabled", "Enabled", languages);
        initializeLabel(dao, "reward.label.exchange.rate", "Exchange Rate", languages);
        initializeLabel(dao, "reward.label.1rewardpoint.equals", "1 reward point =", languages);
        initializeLabel(dao, "reward.label.points.for.registration", "Points for registration", languages);
        initializeLabel(dao, "reward.label.points.for.refer.friend", "Points for refer a friend", languages);
        initializeLabel(dao, "reward.label.points.for.purchase", "Points for purchase", languages);
        initializeLabel(dao, "reward.label.each", "Each", languages);
        initializeLabel(dao, "reward.label.spent.will.earn", "spent will earn", languages);
        initializeLabel(dao, "reward.label.reward.points", "reward points", languages);
        initializeLabel(dao, "reward.label.actions", "Actions", languages);
        initializeLabel(dao, "reward.label.points", "points", languages);
        initializeLabel(dao, "reward.label.profile", "Reward Points", languages);
        initializeLabel(dao, "reward.label.points.for.submit.review", "Points for submit a review", languages);
        initializeLabel(dao, "reward.label.points.for.submit.poll", "Points for submit a poll", languages);
        initializeLabel(dao, "reward.label.points.when.friend.registering", "Points for friend registering", languages);
        initializeLabel(dao, "reward.label.points.when.friend.click.link", "Points for friend click Referral Link", languages);
        initializeLabel(dao, "reward.label.points.for.friend.purchase", "Points for friend purchase", languages);

        initializeLabel(dao, "use.reward", "Use my reward points", languages);
        initializeLabel(dao, "used.rewards", "Customer Rewards", languages);
        initializeLabel(dao, "not.use.reward", "Do not use reward points", languages);
        initializeLabel(dao, "rewards.using.info", "You are using {0} rewards points equivalents to {1}.", languages);
        initializeLabel(dao, "rewards.not.using.info", "You have {0} reward points equivalents to {1}. To use this rewards points to pay this order, please click the following button.", languages);
    }

    public String getConfigurationTemplate() {
        return "/WEB-INF/views/org/store/core/utils/events/rewards/config.vm";
    }

    private Map getConfiguration(BaseAction action) {
        StoreProperty bean = action.getDao().getStoreProperty(PROP_REWARD_PROPERTY, StoreProperty.TYPE_GENERAL);
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

}
