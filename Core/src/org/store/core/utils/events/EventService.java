package org.store.core.utils.events;

import org.store.core.admin.AdminModuleAction;
import org.store.core.front.FrontModuleAction;
import org.store.core.globals.BaseAction;
import org.hibernate.Session;

import javax.servlet.ServletContext;
import java.util.Map;

public interface EventService {

    public static final int EVENT_REGISTER = 1;
    public static final int EVENT_LOGIN = 2;
    public static final int EVENT_VISIT_STATIC_TEXT = 2;
    public static final int EVENT_VISIT_PRODUCT = 3;
    public static final int EVENT_VISIT_CATEGORY = 4;
    public static final int EVENT_VISIT_SEARCH = 5;
    public static final int EVENT_VISIT_SHOPPCART = 6;
    public static final int EVENT_VISIT_ORDERS = 7;
    public static final int EVENT_VISIT_ORDER = 8;
    public static final int EVENT_VISIT_WISHLIST = 9;
    public static final int EVENT_ADD_TO_CART = 10;
    public static final int EVENT_ADD_TO_WISHLIST = 11;
    public static final int EVENT_APPROVE_ORDER = 12;
    public static final int EVENT_DENY_ORDER = 13;
    public static final int EVENT_SAVE_ORDER = 14;
    public static final int EVENT_ADD_REVIEW = 14;
    public static final int EVENT_REFER_FRIEND = 15;
    public static final int EVENT_VISIT_PROFILE = 16;
    public static final int EVENT_PAYMENT_ADDRESS = 17;
    public static final int EVENT_PAYMENT_CONFIRM = 18;
    public static final int EVENT_PAYMENT_PRE_SAVE = 19;
    public static final int EVENT_PAYMENT_POST_SAVE = 20;
    public static final int EVENT_ONEPAGE_CHECKOUT = 21;
    public static final int EVENT_CUSTOM_ACTION = 99;

    public static final int EVENT_ADMIN_SAVE_USER = 1001;
    public static final int EVENT_ADMIN_CHANGE_ORDER_STATUS = 1002;



    /**
     * Return the name of the plugin. Must be unique in the application
     * @return String
     */
    public String getName();

    /**
     * Description of the plugin
     * @param action Struts action using the plugin
     * @return String
     */
    public String getDescription(BaseAction action);

    /**
     * Triggered for an specific event on front-end
     * @param ctx Servlet context
     * @param eventType Code of event being executed
     * @param action Struts action using the plugin
     * @param map Information about the current execution
     * @return Boolean
     */
    public Boolean onExecuteEvent(ServletContext ctx, int eventType, FrontModuleAction action, Map<String,Object> map);

    /**
     * Triggered for an specific event on back-end
     * @param ctx Servlet context
     * @param eventType Code of event being executed
     * @param action Struts action using the plugin
     * @param map Information about the current execution
     * @return Boolean
     */
    public Boolean onExecuteAdminEvent(ServletContext ctx, int eventType, AdminModuleAction action, Map<String,Object> map);

    /**
     * Triggered before any action execution
     * @param ctx Servlet context
     * @param action Struts action using the plugin
     * @return Boolean
     */
    public Boolean beforeAction(ServletContext ctx, BaseAction action);

    /**
     * Return the view used for default configuration of plugin
     * @return String
     */
    public String getConfigurationTemplate();

    /**
     * Triggered before default configuration edition
     * @param action Struts action using the plugin
     */
    public void loadConfigurationData(BaseAction action);

    /**
     * Triggered when default configuration was updated
     * @param action Struts action using the plugin
     */
    public void saveConfigurationData(BaseAction action);

    /**
     * initialization in each configured store
     * @param ctx Servlet context
     * @param databaseSession information of connection to the store database
     * @param store code of store
     */
    public void initialize(ServletContext ctx, Session databaseSession, String store);

    /**
     * initialization of plugin in application
     * @param ctx Servlet context
     */
    public void initializePlugin(ServletContext ctx);

}
