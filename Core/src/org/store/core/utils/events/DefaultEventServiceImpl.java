package org.store.core.utils.events;

import org.store.core.beans.LocalizedText;
import org.store.core.beans.StoreProperty;
import org.store.core.dao.HibernateDAO;
import org.store.core.front.FrontModuleAction;
import org.store.core.admin.AdminModuleAction;
import org.store.core.globals.BaseAction;
import org.apache.commons.lang.StringUtils;
import org.apache.struts2.json.JSONException;
import org.apache.struts2.json.JSONUtil;
import org.hibernate.Session;

import javax.servlet.ServletContext;
import java.util.Map;

/**
 * Rogelio Caballero
 * 14/12/11 10:46
 */
public abstract class DefaultEventServiceImpl implements EventService {

    public abstract String getName();

    public String getDescription(BaseAction action) {
        return null;
    }

    public Boolean onExecuteEvent(ServletContext ctx, int eventType, FrontModuleAction action, Map<String, Object> map) {
        return null;
    }

    public Boolean onExecuteAdminEvent(ServletContext ctx, int eventType, AdminModuleAction action, Map<String, Object> map) {
        return null;
    }

    public Boolean beforeAction(ServletContext ctx, BaseAction action) {
        return null;
    }

    public String getConfigurationTemplate() {
        return null;
    }

    public void loadConfigurationData(BaseAction action) {
    }

    public void saveConfigurationData(BaseAction action) {
    }

    public void initialize(ServletContext ctx, Session databaseSession, String store) {
    }

    public void initializePlugin(ServletContext ctx) {
    }

    /**
     * Tool to configure labels used in plugin
     *
     * @param dao   Hibernate data access object
     * @param key   Code of the label
     * @param value Text of the label
     * @param langs Array of languages to configure the label
     * @return Object representing the persistence of the label
     */
    protected LocalizedText initializeLabel(HibernateDAO dao, String key, String value, String[] langs) {
        LocalizedText bean = dao.getLocalizedtext(key);
        if (bean == null) {
            bean = new LocalizedText();
            bean.setCode(key);
            for (String l : langs) bean.addValue(l, value);
            dao.save(bean);
        }
        return bean;
    }

    /**
     * Function to load a serialized configuration persisted in a store property
     *
     * @param action   Struts action using the plugin
     * @param propName code used to save the configuration
     * @return Map with the configuration
     */
    protected Map getConfiguration(BaseAction action, String propName) {
        StoreProperty bean = action.getDao().getStoreProperty(propName, StoreProperty.TYPE_GENERAL);
        if (bean != null && StringUtils.isNotEmpty(bean.getValue())) {
            try {
                Object o = JSONUtil.deserialize(bean.getValue());
                if (o != null && o instanceof Map) {
                    return (Map) o;
                }
            } catch (JSONException ignored) {
            }
        }
        return null;
    }

    public static final String CONTENT_IN_TOP = "extraTopContent";
    public static final String CONTENT_IN_BOTTOM = "extraBottomContent";

    protected void addContentToView(BaseAction action, String position, String content) {
        if (StringUtils.isNotEmpty(content)) {
            String oldContent = (String) action.getRequest().getAttribute(position);
            if (oldContent != null) oldContent += content;
            else oldContent = content;
            action.addToStack(position, oldContent);
        }
    }


}
