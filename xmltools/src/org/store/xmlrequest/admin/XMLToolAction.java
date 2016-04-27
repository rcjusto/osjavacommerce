package org.store.xmlrequest.admin;

import org.store.core.admin.AdminModuleAction;
import org.store.core.beans.StoreProperty;
import org.store.core.beans.utils.BreadCrumb;
import org.store.xmlrequest.front.XMLRequestAction;
import org.apache.commons.lang.StringUtils;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.json.JSONException;
import org.apache.struts2.json.JSONUtil;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Rogelio Caballero
 * 27/04/12 23:23
 */
@Namespace(value = "/admin")
@ParentPackage(value = "store-admin")
public class XMLToolAction extends AdminModuleAction {

    public static final String CONFIG_ACTION = "xmltool_config";
    public static final String PROP_XML_REQUEST_PROPERTY = "store.plugin.xmlrequest.configuration";

    @Action(value = CONFIG_ACTION, results = {@Result(type = "velocity", location = "/WEB-INF/views/org/store/xmlrequest/config.vm")})
    public String xmltool_config() throws Exception {

        StoreProperty bean = getDao().getStoreProperty(PROP_XML_REQUEST_PROPERTY, StoreProperty.TYPE_GENERAL);
        if (bean != null && StringUtils.isNotEmpty(bean.getValue())) {
            try {
                Object o = JSONUtil.deserialize(bean.getValue());
                if (o != null && o instanceof Map) addToStack("configData", o);
            } catch (JSONException ignored) {}
        }

        addToStack("serviceUrl", url(XMLRequestAction.XML_ACTION, "", null, true));
        getBreadCrumbs().add(new BreadCrumb(null, getText("admin.xmlrequest.title"), null, null));
        return SUCCESS;
    }

    @Action(value = "xmltool_config_save", results = {@Result(type = "redirectAction", location = "xmltool_config")})
    public String xmltool_config_save() throws Exception {

        StoreProperty bean = getDao().getStoreProperty(PROP_XML_REQUEST_PROPERTY, StoreProperty.TYPE_GENERAL, true);
        Map<String, Serializable> map = new HashMap<String, Serializable>();
        map.put("enabled", "yes".equalsIgnoreCase(enabled));
        map.put("needCredentials", "yes".equalsIgnoreCase(needCredentials));
        map.put("maxItemsForRequest", maxItemsForRequest!=null ? maxItemsForRequest : 0);
        try {
            String cad = JSONUtil.serialize(map);
            bean.setValue(cad);
            getDao().save(bean);
        } catch (JSONException e) {
            LOG.error(e.getMessage(), e);
        }
        return SUCCESS;
    }
    
    private String enabled;
    private String needCredentials;
    private Integer maxItemsForRequest;

    public String getEnabled() {
        return enabled;
    }

    public void setEnabled(String enabled) {
        this.enabled = enabled;
    }

    public String getNeedCredentials() {
        return needCredentials;
    }

    public void setNeedCredentials(String needCredentials) {
        this.needCredentials = needCredentials;
    }

    public Integer getMaxItemsForRequest() {
        return maxItemsForRequest;
    }

    public void setMaxItemsForRequest(Integer maxItemsForRequest) {
        this.maxItemsForRequest = maxItemsForRequest;
    }
}
