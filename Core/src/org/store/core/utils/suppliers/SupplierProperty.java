package org.store.core.utils.suppliers;

import org.apache.commons.lang.StringUtils;

public class SupplierProperty {

    private String name;
    private String[] options;
    private String defaultValue;

    public SupplierProperty(String name) {
        this.name = name;
    }

    public SupplierProperty(String name, String options) {
        this.name = name;
        this.options = options.split(",");
    }

    public SupplierProperty(String name, String options, String def) {
        this.name = name;
        if (StringUtils.isNotEmpty(options)) this.options = options.split(",");
        this.defaultValue = def;
    }

    public SupplierProperty(String name, String[] options) {
        this.name = name;
        this.options = options;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String[] getOptions() {
        return options;
    }

    public void setOptions(String[] options) {
        this.options = options;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }
}
