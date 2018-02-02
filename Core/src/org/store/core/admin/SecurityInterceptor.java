package org.store.core.admin;

import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.AbstractInterceptor;
import org.store.core.beans.User;
import org.store.core.globals.BaseAction;
import org.store.core.hibernate.HibernateSessionFactory;
import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.apache.struts2.ServletActionContext;
import org.hibernate.SQLQuery;
import org.hibernate.Session;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

public class SecurityInterceptor extends AbstractInterceptor {
    public static final String CNT_ADMIN_USER_COOKIE = "trfqrfqerfrqer";
    public static Logger log = Logger.getLogger(SecurityInterceptor.class);

    public String intercept(ActionInvocation actionInvocation) throws Exception {
        // Seguridad
        String actName = ServletActionContext.getActionMapping().getName();

        if ("login".equalsIgnoreCase(actName)) return actionInvocation.invoke();

        String ext = ServletActionContext.getActionMapping().getExtension();
        HttpServletRequest request = ServletActionContext.getRequest();
        String thisUrl = request.getRequestURI();
        request.setAttribute("lastUrl", (thisUrl.endsWith("logout." + ext)) ? request.getContextPath() : thisUrl);
        if (actionInvocation.getAction() instanceof BaseAction) {
            BaseAction action = (BaseAction) actionInvocation.getAction();
            User adminUser = action.getAdminUser();
            List<Number> roles = getActionRoles(actName, action);

            // verifcar roles, tiene que haber usuario logueado
            if (adminUser != null) {

                // si la accion no tiene roles definidos cualquier usuario de administracion puede acceder
                if (CollectionUtils.isEmpty(roles))
                    return actionInvocation.invoke();

                // si la accion tiene roles, verificar que usuario conectado tiene alguno de ellos
                for (Number r : roles)
                    if (adminUser.hasRole(r.longValue()) || adminUser.hasRoleCode(User.ADMROL_SUPERADMIN))
                        return actionInvocation.invoke();
            }

        }
        return "accessDeny";
    }

    private List<Number> getActionRoles(String fullActionName, BaseAction action) {
        int ind = fullActionName.lastIndexOf('/');
        String actionName = (ind > 0) ? fullActionName.substring(ind + 1) : fullActionName;
        try {
            Session s = HibernateSessionFactory.getSession(action.getDatabaseConfig());
            SQLQuery q = s.createSQLQuery("select distinct t_user_admin_role_id from t_user_admin_role_actions where element=:action");
            q.setString("action", actionName);
            return q.list();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

}
