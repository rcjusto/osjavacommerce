package org.store.core.beans.utils;

import org.store.core.beans.Product;

import java.util.Comparator;

public class ProductPriceComparator implements Comparator {

    private String direction;

    public ProductPriceComparator(String direction) {
        this.direction = direction;
    }

    public int compare(Object o1, Object o2) {
        Product p1 = (o1 instanceof Product) ? (Product) o1 : null;
        Product p2 = (o2 instanceof Product) ? (Product) o2 : null;
        Double price1 = (p1!=null) ? p1.getBasePrice() : 0d;
        Double price2 = (p2!=null) ? p2.getBasePrice() : 0d;
        return "desc".equalsIgnoreCase(direction) ? price2.compareTo(price1) : price1.compareTo(price2);
    }
}
