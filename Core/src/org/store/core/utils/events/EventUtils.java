package org.store.core.utils.events;

import org.store.core.admin.AdminModuleAction;
import org.store.core.front.FrontModuleAction;
import org.store.core.globals.BaseAction;
import org.store.core.globals.config.Store20Config;
import org.apache.log4j.Logger;
import org.hibernate.Session;

import javax.servlet.ServletContext;
import java.util.Map;


public class EventUtils {

    public static Logger log = Logger.getLogger(EventUtils.class);
    public static void initialize(ServletContext ctx, Session databaseSession, String store) {
        Map<String, Class> map;
        synchronized (ctx) {
            map = Store20Config.getInstance(ctx).getMapEvents();
        }
        if (map != null && !map.isEmpty()) {
            for (Class c : map.values()) {
                try {
                    Object obj = c.newInstance();
                    if (obj instanceof EventService) {
                        ((EventService) obj).initialize(ctx, databaseSession, store);
                    }
                } catch (InstantiationException e) {
                    log.error(e.getMessage(), e); 
                } catch (IllegalAccessException e) {
                    log.error(e.getMessage(), e); 
                }
            }
        }
    }

    public static void executeEvent(ServletContext ctx, int event, FrontModuleAction action) {
        executeEvent(ctx, event, action, null);
    }

    public static void executeAdminEvent(ServletContext ctx, int event, AdminModuleAction action) {
        executeAdminEvent(ctx, event, action, null);
    }

    public static boolean executeEvent(ServletContext ctx, int event, FrontModuleAction action, Map<String,Object> extraData) {
        boolean result = false;
        Map<String, Class> map;
        synchronized (ctx) {
            map = Store20Config.getInstance(ctx).getMapEvents();
        }
        if (map != null && !map.isEmpty()) {
            for (Class c : map.values()) {
                try {
                    Object obj = c.newInstance();
                    if (obj instanceof EventService) {
                        Boolean res = ((EventService) obj).onExecuteEvent(ctx, event, action, extraData);
                        if (res!=null && res)  result = true;
                    }
                } catch (InstantiationException e) {
                    log.error(e.getMessage(), e); 
                } catch (Exception e) {
                    log.error(e.getMessage(), e); 
                }
            }
        }
        return result;
    }

    public static boolean executeAdminEvent(ServletContext ctx, int event, AdminModuleAction action, Map<String,Object> extraData) {
        boolean result = false;
        Map<String, Class> map;
        synchronized (ctx) {
            map = Store20Config.getInstance(ctx).getMapEvents();
        }
        if (map != null && !map.isEmpty()) {
            for (Class c : map.values()) {
                try {
                    Object obj = c.newInstance();
                    if (obj instanceof EventService) {
                        Boolean res = (((EventService) obj).onExecuteAdminEvent(ctx, event, action, extraData));
                        if (res!=null && res)  result = true;
                    }
                } catch (InstantiationException e) {
                    log.error(e.getMessage(), e); 
                } catch (IllegalAccessException e) {
                    log.error(e.getMessage(), e); 
                }
            }
        }
        return result;
    }

    public static void executeBeforeAction(ServletContext ctx, BaseAction action) {
        Map<String, Class> map;
        synchronized (ctx) {
            map = Store20Config.getInstance(ctx).getMapEvents();
        }
        if (map != null && !map.isEmpty()) {
            for (Class c : map.values()) {
                try {
                    Object obj = c.newInstance();
                    if (obj instanceof EventService) {
                        ((EventService) obj).beforeAction(ctx, action);
                    }
                } catch (InstantiationException e) {
                    log.error(e.getMessage(), e);
                } catch (IllegalAccessException e) {
                    log.error(e.getMessage(), e);
                }
            }
        }
    }

    public static EventService getPlugin(ServletContext ctx, String pluginName) {
        synchronized (ctx) {
            Map<String, Class> map = Store20Config.getInstance(ctx).getMapEvents();
            if (map != null && map.containsKey(pluginName)) {
                Class c = map.get(pluginName);
                try {
                    Object obj = c.newInstance();
                    if (obj instanceof EventService) {
                        return (EventService) obj;
                    }
                } catch (InstantiationException e) {
                    log.error(e.getMessage(), e); 
                } catch (IllegalAccessException e) {
                    log.error(e.getMessage(), e); 
                }
            }
        }
        return null;
    }


}
