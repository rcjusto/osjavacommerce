package org.store.core.utils.templates;


import org.apache.commons.lang.builder.EqualsBuilder;

import java.util.HashMap;
import java.util.Map;


public class TemplateBlock {
    private String code;
    private Map<String,String> name;
    private Map<String,String> description;

    public TemplateBlock(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName(String lang) {
        if (name!=null && name.containsKey(lang)) return name.get(lang);
        if (name!=null && name.containsKey("default")) return name.get("default");
        return code;
    }

    public Map<String, String> getName() {
        return name;
    }

    public void setName(Map<String, String> name) {
        this.name = name;
    }

    public TemplateBlock setName(String lang , String value) {
        if (this.name==null) this.name = new HashMap<String,String>();
        this.name.put(lang, value);
        return this;
    }


    public String getDescription(String lang) {
        if (description!=null && description.containsKey(lang)) return description.get(lang);
        if (description!=null && description.containsKey("default")) return description.get("default");
        return "";
    }

    public Map<String, String> getDescription() {
        return description;
    }

    public TemplateBlock setDescription(Map<String, String> description) {
        this.description = description;
        return this;
    }

    public TemplateBlock setDescription(String lang , String value) {
        if (this.description==null) this.description = new HashMap<String,String>();
        this.description.put(lang, value);
        return this;
    }

    @Override
    public boolean equals(Object other) {
        if ((this == other)) return true;
        if (!(other instanceof TemplateBlock)) return false;
        TemplateBlock castOther = (TemplateBlock) other;
        return new EqualsBuilder()
                .append(this.getCode(), castOther.getCode())
                .isEquals();
    }
    
}
