package org.store.core.globals;

import org.store.core.beans.PromotionalCode;
import org.store.core.beans.ShopCart;
import org.store.core.beans.ShopCartItem;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class PromotionUtils {

    // output
    private List<Map<String, Object>> data;
    private double total = 0.0d;
    private boolean freeShipping = false;
    private double freeProductCost = 0.0d;

    public PromotionUtils(List<PromotionalCode> promotionalCodes, ShopCart cart, BaseAction action, double totalProducts) {
        calculate(promotionalCodes, cart, action, totalProducts);
    }

    public void calculate(List<PromotionalCode> promotionalCodes, ShopCart cart, BaseAction action, double totalProducts) {
        total = 0.0d;
        if (promotionalCodes != null && !promotionalCodes.isEmpty()) {
            freeShipping = false;
            data = new ArrayList<Map<String, Object>>();
            for (PromotionalCode pc : promotionalCodes) {
                if (pc.getFreeShipping()) {
                    Map<String, Object> map = new HashMap<String, Object>();
                    map.put("code", pc.getCode());
                    map.put("name", pc.getName(action.getLocale().getLanguage()));
                    double value = 0.0d;
                    map.put("type", "free-shipping");
                    freeShipping = true;
                    total += value;
                    map.put("value", value);
                    data.add(map);
                }
                if (pc.getFreeProduct() != null) {
                    Map<String, Object> map = new HashMap<String, Object>();
                    map.put("code", pc.getCode());
                    map.put("name", pc.getName(action.getLocale().getLanguage()));
                    double value = 0.0d;
                    map.put("type", "product");
                    map.put("data", pc.getFreeProduct().getIdProduct());
                    if (pc.getFreeProduct().getCostPrice()!=null) freeProductCost += pc.getFreeProduct().getCostPrice();
                    total += value;
                    map.put("value", value);
                    data.add(map);
                }
                if (pc.getDiscount() != null && pc.getDiscount() > 0) {
                    Map<String, Object> map = new HashMap<String, Object>();
                    map.put("code", pc.getCode());
                    map.put("name", pc.getName(action.getLocale().getLanguage()));
                    double value = 0.0d;
                    map.put("type", "discount");
                    value -= couponUsed(pc, cart);
                    total += value;
                    map.put("value", value);
                    data.add(map);
                } else if (pc.getDiscountPercent() != null && pc.getDiscountPercent() > 0) {
                    Map<String, Object> map = new HashMap<String, Object>();
                    map.put("code", pc.getCode());
                    map.put("name", pc.getName(action.getLocale().getLanguage()));
                    double value = 0.0d;
                    map.put("type", "discount-percent");
                    map.put("data", pc.getDiscountPercent());
                    value -= BigDecimal.valueOf(pc.getDiscountPercent() * totalProducts).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
                    total += value;
                    map.put("value", value);
                    data.add(map);
                }
            }
        }
    }

    public double couponUsed(PromotionalCode pc, ShopCart cart) {
        double res = 0.0d;
        if (pc.getDiscount() != null && pc.getDiscount() > 0 && !cart.getItems().isEmpty()) {
            for (ShopCartItem it : cart.getItems()) {
                if (it.getBeanProd1() != null && pc.canApplyTo(it.getBeanProd1())) {
                    if (pc.getDiscount() > res) {
                        res += Math.min(pc.getDiscount() - res, it.getPrice());
                    }
                }
            }
        }
        return res;
    }

    public List<Map<String, Object>> getData() {
        return data;
    }

    public double getTotal() {
        return total;
    }

    public boolean isFreeShipping() {
        return freeShipping;
    }

    public double getFreeProductCost() {
        return freeProductCost;
    }
}
