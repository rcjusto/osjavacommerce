package org.store.core.front;

import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.AbstractInterceptor;
import org.store.core.beans.StoreProperty;
import org.store.core.beans.User;
import org.store.core.globals.BaseAction;
import org.store.core.globals.SomeUtils;
import org.store.core.hibernate.HibernateSessionFactory;
import org.store.core.utils.events.EventUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.struts2.ServletActionContext;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.List;


public class SecurityInterceptor extends AbstractInterceptor {
    public static final String CNT_FRONT_USER_COOKIE = "FRONT_USER";
    public static Logger log = Logger.getLogger(SecurityInterceptor.class);

    public String intercept(ActionInvocation actionInvocation) throws Exception {
        String ext = ServletActionContext.getActionMapping().getExtension();
        HttpServletRequest request = ServletActionContext.getRequest();
        String thisUrl = request.getRequestURI();
        request.setAttribute("lastUrl", (thisUrl.endsWith("logout." + ext)) ? request.getContextPath() : thisUrl);
        if (actionInvocation.getAction() instanceof BaseAction) {
            BaseAction action = (BaseAction) actionInvocation.getAction();
            User frontUser = action.getFrontUser();
            // Verifcar cookies
            if (frontUser == null && request.getCookies() != null) {
                for (Cookie c : request.getCookies()) {
                    if (CNT_FRONT_USER_COOKIE.equalsIgnoreCase(c.getName()) && StringUtils.isNotEmpty(c.getValue())) {
                        String userId = SomeUtils.decrypt3Des(c.getValue(), action.getEncryptionKey());
                        if (StringUtils.isNotEmpty(userId)) {
                            frontUser = findUserByCookie(userId, action);
                            if (frontUser != null && !frontUser.getBlocked()) {
                                frontUser.setLastIP(request.getRemoteAddr());
                                frontUser.setLastBrowser(UserAction.getBrowser(request));
                                frontUser.addVisit();
                                action.getDao().save(frontUser);
                                if (action instanceof FrontModuleAction)
                                    ((FrontModuleAction)action).setFrontUser(frontUser);
                            }
                        }
                    }
                }
            }

            // verificar tienda cerrada
            if ("Y".equalsIgnoreCase(action.getStoreProperty(StoreProperty.PROP_CLOSED_STORE,"N"))) {
                return "storeClosed";
            }

            // Ejecutar metodo beforeAction de los event plugins
            EventUtils.executeBeforeAction(action.getServletContext(), action);
        }
        return actionInvocation.invoke();
    }

    private User findUserByCookie(String userId, BaseAction action) {
        try {
            Session s = HibernateSessionFactory.getSession(action.getDatabaseConfig());
            List<User> l = s.createCriteria(User.class).add(Restrictions.eq("userId", userId)).list();
            if (l != null && !l.isEmpty()) return l.get(0);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }
}
