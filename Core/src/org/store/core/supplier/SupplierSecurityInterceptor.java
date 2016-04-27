package org.store.core.supplier;

import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.AbstractInterceptor;
import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.apache.struts2.ServletActionContext;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.store.core.beans.Provider;
import org.store.core.beans.User;
import org.store.core.globals.BaseAction;
import org.store.core.hibernate.HibernateSessionFactory;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

public class SupplierSecurityInterceptor extends AbstractInterceptor {

    public static Logger log = Logger.getLogger(SupplierSecurityInterceptor.class);

    public String intercept(ActionInvocation actionInvocation) throws Exception {
        // Seguridad
        String actName = ServletActionContext.getActionMapping().getName();

        if ("login".equalsIgnoreCase(actName)) return actionInvocation.invoke();

        String ext = ServletActionContext.getActionMapping().getExtension();
        HttpServletRequest request = ServletActionContext.getRequest();
        String thisUrl = request.getRequestURI();
        request.setAttribute("lastUrl", (thisUrl.endsWith("logout." + ext)) ? request.getContextPath() : thisUrl);
        if (actionInvocation.getAction() instanceof SupplierModuleAction) {
            SupplierModuleAction action = (SupplierModuleAction) actionInvocation.getAction();
            Provider provider = action.getProvider();

            if (provider != null) {
                return actionInvocation.invoke();
            }

        }
        return "accessDeny";
    }


}
