package org.store.core.beans.mail;

import org.store.core.beans.Product;
import org.store.core.globals.BaseAction;

import java.util.ArrayList;
import java.util.List;

/**
 * Rogelio Caballero
 * 20/07/12 21:25
 */
public class MWishlist {

    private List<MProduct> list;

    public MWishlist(List<Product> products, BaseAction action) {
        list = new ArrayList<MProduct>();
        for(Product p : products) {
            list.add(new MProduct(p, action));
        }
    }

    public List<MProduct> getItems() {
        return list;
    }

}
