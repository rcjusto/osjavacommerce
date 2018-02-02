package org.store.core.beans.utils;

import org.hibernate.search.bridge.StringBridge;

public class ProductBooleanBridge implements StringBridge {

    public String objectToString(Object o) {
        if (o!=null && o instanceof Boolean) {
            return Boolean.TRUE.equals(o) ? "true" : "false";
        }
        return "false";
    }

}