package org.store.core.beans.utils;

import org.hibernate.search.bridge.StringBridge;

public class ProductStockBridge implements StringBridge {

    public String objectToString(Object o) {
        if (o!=null && o instanceof Long) {
            return ((Long)o) > 0 ? "true" : "false";
        }
        return null;
    }

}