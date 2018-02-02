package org.store.core.beans.utils;

import org.apache.commons.lang.StringUtils;
import org.hibernate.search.bridge.StringBridge;

public class ProductEtaBridge implements StringBridge {

    public String objectToString(Object o) {
        if (o!=null && o instanceof String) {
            return StringUtils.isNotEmpty((String) o) ? "true" : "false";
        }
        return null;
    }

}