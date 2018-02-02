package org.store.template;

import org.apache.commons.lang.StringUtils;

public class TemplateControl {

    private String name;
    private String description;
    private String parameters;
    private String template;
    private String pages;
    private String fixed;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getParameters() {
        return parameters;
    }

    public void setParameters(String parameters) {
        this.parameters = parameters;
    }

    public String getTemplate() {
        return template;
    }

    public void setTemplate(String template) {
        this.template = template;
    }

    public String[] getParametersArr() {
        return (StringUtils.isNotEmpty(parameters)) ? parameters.split("[|]") : null;
    }
    
    public String getContent(String[] params) {
        return (template!=null && params!=null && params.length>0) ? String.format(template, params) : template;
    }

    public String getPages() {
        return pages;
    }

    public void setPages(String pages) {
        this.pages = pages;
    }

    public String[] getPagesArr() {
        return (StringUtils.isNotEmpty(pages)) ? pages.split("[,]") : null;
    }

    public String getFixed() {
        return fixed;
    }

    public void setFixed(String fixed) {
        this.fixed = fixed;
    }
}
