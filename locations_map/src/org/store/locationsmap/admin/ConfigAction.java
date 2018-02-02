package org.store.locationsmap.admin;

import org.apache.commons.lang.StringUtils;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.json.JSONException;
import org.apache.struts2.json.JSONUtil;
import org.store.core.admin.AdminModuleAction;
import org.store.core.beans.StoreProperty;
import org.store.core.beans.utils.BreadCrumb;
import org.store.core.globals.SomeUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author rcaballero
 */
@Namespace(value = "/admin")
@ParentPackage(value = "store-admin")
public class ConfigAction extends AdminModuleAction {

    public static final String CONFIG_LOAD_ACTION = "locationsmap_config";
    public static final String CONFIG_SAVE_ACTION = "locationsmap_config_save";

    public static final String PROP_APIKEY_PROPERTY = "store.plugin.locations_map.api_key";
    public static final String PROP_CENTER_PROPERTY = "store.plugin.locations_map.center";
    public static final String PROP_ZOOM_PROPERTY = "store.plugin.locations_map.zoom";
    public static final String PROP_DATA_PROPERTY = "store.plugin.locations_map.data";


    @Action(value = CONFIG_LOAD_ACTION, results = {@Result(type = "velocity", location = "/WEB-INF/views/org/store/locationsmap/config.vm")})
    public String locationsmap_config() throws Exception {
        getBreadCrumbs().add(new BreadCrumb(null, getText("admin.locationsmap.title"), null, null));
        return SUCCESS;
    }

    @Action(value = CONFIG_SAVE_ACTION, results = {@Result(type = "redirectAction", location = CONFIG_LOAD_ACTION)})
    public String locationsmap_config_save() throws Exception {

        saveStoreProperty(PROP_APIKEY_PROPERTY, apikey);
        saveStoreProperty(PROP_CENTER_PROPERTY, center);
        saveStoreProperty(PROP_ZOOM_PROPERTY, zoom);

        List<Map<String, Serializable>> list = new ArrayList<Map<String, Serializable>>();
        if (lat != null)
            for (int i = 0; i < lat.length; i++) {
                if (lng != null && lat[i] != null && StringUtils.isNotEmpty(lat[i]) && lng.length > i && lng[i] != null && StringUtils.isNotEmpty(lng[i])) {
                    Double dlat = SomeUtils.strToDouble(lat[i]);
                    Double dlng = SomeUtils.strToDouble(lng[i]);
                    if (dlat != null && dlng != null) {
                        Map<String, Serializable> map = new HashMap<String, Serializable>();
                        map.put("lat", lat[i]);
                        map.put("lng", lng[i]);
                        map.put("label", label != null && label.length > i && StringUtils.isNotEmpty(label[i]) ? label[i] : "");
                        map.put("title", title != null && title.length > i && StringUtils.isNotEmpty(title[i]) ? title[i] : "");
                        map.put("description", desc != null && desc.length > i && StringUtils.isNotEmpty(desc[i]) ? desc[i] : "");
                        list.add(map);
                    }
                }
            }
        String js = JSONUtil.serialize(list);

        saveStoreProperty(PROP_DATA_PROPERTY, js);

        return SUCCESS;
    }

    private void saveStoreProperty(String key, String value) {
        StoreProperty bean = getDao().getStoreProperty(key, StoreProperty.TYPE_GENERAL, true);
        bean.setValue(value);
        getDao().save(bean);
    }

    private String apikey;
    private String center;
    private String zoom;
    private String jsData;
    private String[] lat;
    private String[] lng;
    private String[] label;
    private String[] title;
    private String[] desc;

    public String getApikey() {
        return apikey;
    }

    public void setApikey(String apikey) {
        this.apikey = apikey;
    }

    public String getCenter() {
        return center;
    }

    public void setCenter(String center) {
        this.center = center;
    }

    public String getZoom() {
        return zoom;
    }

    public void setZoom(String zoom) {
        this.zoom = zoom;
    }

    public String getJsData() {
        return jsData;
    }

    public void setJsData(String jsData) {
        this.jsData = jsData;
    }

    public String[] getLat() {
        return lat;
    }

    public void setLat(String[] lat) {
        this.lat = lat;
    }

    public String[] getLng() {
        return lng;
    }

    public void setLng(String[] lng) {
        this.lng = lng;
    }

    public String[] getLabel() {
        return label;
    }

    public void setLabel(String[] label) {
        this.label = label;
    }

    public String[] getTitle() {
        return title;
    }

    public void setTitle(String[] title) {
        this.title = title;
    }

    public String[] getDesc() {
        return desc;
    }

    public void setDesc(String[] desc) {
        this.desc = desc;
    }
}
