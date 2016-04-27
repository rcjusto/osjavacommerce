package org.store.core.beans;

import org.store.core.dao.HibernateDAO;
import org.store.core.globals.BaseAction;
import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.beanutils.DynaProperty;
import org.apache.commons.lang.ArrayUtils;
import org.apache.log4j.Logger;
import org.apache.struts2.ServletActionContext;
import org.hibernate.event.EventSource;

import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * User: Rogelio Caballero Justo
 * Date: 26/08/2006
 * Time: 01:58:00 PM
 */
public class BaseBean implements Serializable {

    public static Logger log = Logger.getLogger(BaseBean.class);
    private BaseAction action;
    private HibernateDAO dao;
    private HashMap<String, Object> otherProps;

    public BaseAction getAction() {
        if (action==null && ServletActionContext.getContext()!=null) {
            action = (ServletActionContext.getContext().getActionInvocation().getAction() instanceof BaseAction) ? (BaseAction) ServletActionContext.getContext().getActionInvocation().getAction() : null;
        }
        return action;
    }

/*    public HibernateDAO getDao() {
        if (dao==null) {
            dao = new HibernateDAO();
        }
        return dao;
    }
  */  
    public void handlePostLoad(EventSource session) {

    }

    public boolean handlePreUpdate(EventSource session, boolean isNew) {
        return false;
    }

    public void handlePostUpdate(EventSource session, boolean isNew) {

    }

    public void handlePostDelete(EventSource session) {

    }

    public boolean handlePreDelete(EventSource session) {
        return false;
    }

    public void addProperty(String key, Object value) {
        if (otherProps == null) otherProps = new HashMap<String, Object>();
        otherProps.put(key, value);
    }

    public void delProperty(String key) {
        if (otherProps != null && otherProps.containsKey(key)) {
            otherProps.remove(key);
        }
    }

    public Object getProperty(String key) {
        if (otherProps == null || !otherProps.containsKey(key)) return null;
        return otherProps.get(key);
    }

    public boolean isEmpty(String cad) {
        return cad == null || "".equals(cad.trim());
    }

    private final Class[] excludeFromDescribe = {Class.class, Map.class, Set.class, List.class};
    public Map describe() throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        Map description = new HashMap();
        if (this instanceof DynaBean) {
            DynaProperty descriptors[] = ((DynaBean) this).getDynaClass().getDynaProperties();
            for (DynaProperty descriptor : descriptors) {
                String name = descriptor.getName();
                if (!ArrayUtils.contains(excludeFromDescribe, descriptor.getType())) description.put(name, BeanUtilsBean.getInstance().getProperty(this, name));
            }
        } else {
            PropertyDescriptor descriptors[] = BeanUtilsBean.getInstance().getPropertyUtils().getPropertyDescriptors(this);
            for (PropertyDescriptor descriptor : descriptors) {
                String name = descriptor.getName();
                if (descriptor.getReadMethod() != null && !ArrayUtils.contains(excludeFromDescribe, descriptor.getPropertyType()))
                    description.put(name, BeanUtilsBean.getInstance().getProperty(this, name));
            }
        }

        return description;
    }

}
