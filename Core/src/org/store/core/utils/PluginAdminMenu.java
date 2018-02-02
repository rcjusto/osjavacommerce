package org.store.core.utils;

import org.store.core.globals.BaseAction;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;

import java.util.HashMap;
import java.util.Map;


public class PluginAdminMenu {

    public static final String PARENT_CATALOG = "catalog";
    public static final String PARENT_CUSTOMERS = "customers";
    public static final String PARENT_SALES = "sales";
    public static final String PARENT_CMS = "cms";
    public static final String PARENT_CONFIGURATION = "configuration";

    public static final String EDIT_PROPERTIES_ACTION = "editpluginproperty";
    public static final String EDIT_PROPERTIES_PARAM_NAME = "plugin";

    private String menuParent;
    private String menuLabel;
    private String menuText;
    private String menuAction;
    private String url;
    private Map<String,String> parameters;


    public String getMenuUrl(BaseAction action) {
        if (StringUtils.isNotEmpty(url)) {
            return url;
        } else if (StringUtils.isNotEmpty(menuAction)) {
            return action.url(menuAction, "admin", parameters, false);
        }
        return null;
    }

    public String getMenuName(BaseAction action) {
        if (StringUtils.isNotEmpty(menuLabel)) {
            return StringUtils.isNotEmpty(menuText) ? action.getText(menuLabel, menuText) : action.getText(menuLabel);
        }
        return null;
    }

    public String getUrl() {
        return url;
    }

    public void addActionParameter(String key, String value) {
        getParameters().put(key, value);
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getMenuParent() {
        return menuParent;
    }

    public void setMenuParent(String menuParent) {
        this.menuParent = menuParent;
    }

    public String getMenuLabel() {
        return menuLabel;
    }

    public void setMenuLabel(String menuLabel) {
        this.menuLabel = menuLabel;
    }

    public String getMenuText() {
        return menuText;
    }

    public void setMenuText(String menuText) {
        this.menuText = menuText;
    }

    public String getMenuAction() {
        return menuAction;
    }

    public void setMenuAction(String menuAction) {
        this.menuAction = menuAction;
    }

    public Map<String, String> getParameters() {
        if (parameters==null) parameters = new HashMap<String, String>();
        return parameters;
    }

    public void setParameters(Map<String, String> parameters) {
        this.parameters = parameters;
    }

    @Override
    public boolean equals(Object other) {
        if ((this == other)) return true;
        if (!(other instanceof PluginAdminMenu)) return false;
        PluginAdminMenu castOther = (PluginAdminMenu) other;
        return new EqualsBuilder()
                .append(this.getMenuLabel(), castOther.getMenuLabel())
                .isEquals();
    }
    
}
