package org.store.core.beans.utils;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.log4j.Logger;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

public class PageMeta implements Serializable {

    public static Logger log = Logger.getLogger(PageMeta.class);
    public static final String META_TITLE = "title";
    public static final String META_DESCRIPTION = "description";
    public static final String META_KEYWORDS = "keywords";
    public static final String META_ABSTRACT = "abstract";

    private String metaName;
    private boolean appendParent;
    private String metaValue;

    public PageMeta(String metaName, String metaValue, boolean appendParent) {
        this.appendParent = appendParent;
        this.metaValue = metaValue;
        this.metaName = metaName;
    }

    public PageMeta(Map map) {
        if (map!=null) {
            try {
                BeanUtils.populate(this, map);
            } catch (IllegalAccessException e) {
                log.error(e.getMessage(), e);
            } catch (InvocationTargetException e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    public boolean getAppendParent() {
        return appendParent;
    }

    public void setAppendParent(boolean appendParent) {
        this.appendParent = appendParent;
    }

    public String getMetaValue() {
        return (metaValue!=null) ? metaValue : "";
    }

    public void setMetaValue(String metaValue) {
        this.metaValue = metaValue;
    }

    public String getMetaName() {
        return metaName;
    }

    public void setMetaName(String metaName) {
        this.metaName = metaName;
    }
}
