package org.store.core.beans.mail;

import org.store.core.beans.OrderDetailProduct;
import org.store.core.globals.BaseAction;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Rogelio Caballero
 * 19/07/12 18:30
 */
public class MAvailableLinks {

    private static final String MAIL_TEMPLATE = "/WEB-INF/views/mails/available_links_items.vm";
    private String value;

    public MAvailableLinks(List<OrderDetailProduct> links, BaseAction action) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("links", links);
        this.value = action.proccessVelocityTemplate(MAIL_TEMPLATE, map);
    }

    @Override
    public String toString() {
        return value;
    }

}
