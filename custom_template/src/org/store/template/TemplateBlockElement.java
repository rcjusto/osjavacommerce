package org.store.template;

import org.apache.commons.lang.StringUtils;

/**
 * Rogelio Caballero
 * 2/03/12 15:59
 */
public class TemplateBlockElement {
    
    private String name;
    private String params;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getParams() {
        return params;
    }

    public void setParams(String params) {
        this.params = params;
    }

    public String[] getParametersArr() {
        return (StringUtils.isNotEmpty(params)) ? params.split("[|]") : null;
    }
}
