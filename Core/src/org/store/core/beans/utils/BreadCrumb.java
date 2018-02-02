package org.store.core.beans.utils;

/**
 * Breadcrumb Item
 */
public class BreadCrumb {

    private String name;
    private String link;
    private String type;
    private String close;

    public BreadCrumb(String type, String name, String link, String close) {
        this.type = type;
        this.name = name;
        this.link = link;
        this.close = close;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getClose() {
        return close;
    }

    public void setClose(String close) {
        this.close = close;
    }
}
