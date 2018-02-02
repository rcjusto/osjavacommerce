package org.store.config;

import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.AbstractInterceptor;
import org.apache.struts2.ServletActionContext;

/**
 * Rogelio Caballero
 * 10/04/12 18:02
 */
public class SecurityInterceptor extends AbstractInterceptor {


    private static final String ATT_CONFIG_ADMIN = "config_admin";

    @Override
    public String intercept(ActionInvocation actionInvocation) throws Exception {
        Object userAtt = ServletActionContext.getRequest().getSession().getAttribute(ATT_CONFIG_ADMIN);
        boolean isUserLoggued = userAtt!=null && userAtt.equals("superadmin");
        boolean isActionLogin = "login".equalsIgnoreCase(actionInvocation.getInvocationContext().getName());
        return isUserLoggued || isActionLogin ? actionInvocation.invoke() : "accessDeny";
    }
    
    public static void loginUser(String user) {
        ServletActionContext.getRequest().getSession().setAttribute(ATT_CONFIG_ADMIN, user);
    }

    public static void logoutUser() {
        if (ServletActionContext.getRequest().getSession().getAttribute(ATT_CONFIG_ADMIN)!=null)
            ServletActionContext.getRequest().getSession().removeAttribute(ATT_CONFIG_ADMIN);
    }

}
