package org.store.core.globals;

import org.store.core.beans.ProductUserTax;
import org.store.core.beans.ShopCart;
import org.store.core.beans.ShopCartItem;
import org.store.core.beans.State;
import org.store.core.beans.Tax;
import org.store.core.beans.TaxPerFamily;
import org.store.core.beans.User;
import org.store.core.dao.HibernateDAO;
import org.apache.commons.lang.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TaxesUtils {

    private List<Map<String, Object>> data;
    private Double total;

    // input
    private Double totalWithoutShipping;
    private Double totalShipping;

    public TaxesUtils(ShopCart cart, State deliveryState, User user, BaseAction action, Double totalWithoutShipping, Double totalShipping) {
        this.totalWithoutShipping = totalWithoutShipping;
        this.totalShipping = totalShipping;
        calculate(cart, deliveryState, user, action);
    }

    public void calculate(ShopCart cart, State deliveryState, User user, BaseAction action) {
        calculateTaxesPerProduct(cart, user, action.getDao());
        calculateTaxesPerState(deliveryState, user, action.getDao());
    }

    public void calculateTaxesPerProduct(ShopCart cart, User user, HibernateDAO dao) {
        if (data == null) data = new ArrayList<Map<String, Object>>();
        if (total == null) total = 0.0d;

        Map<TaxPerFamily, Double> taxMap = new HashMap<TaxPerFamily, Double>();
        for (ShopCartItem it : cart.getItems()) {
            if (it.getBeanProd1() != null && StringUtils.isNotEmpty(it.getBeanProd1().getAltCategory())) {
                ProductUserTax puTax = dao.getProductUserTaxes(it.getBeanProd1().getAltCategory(), user.getAltCategory());
                if (puTax != null) {
                    for (TaxPerFamily tpf : puTax.getTaxes()) {
                        Double old = taxMap.containsKey(tpf) ? taxMap.get(tpf) : 0.0d;
                        taxMap.put(tpf, old + it.getPrice() * it.getQuantity());
                    }
                }
            }
            if (it.getBeanProd2() != null && StringUtils.isNotEmpty(it.getBeanProd2().getAltCategory())) {
                ProductUserTax puTax = dao.getProductUserTaxes(it.getBeanProd2().getAltCategory(), user.getAltCategory());
                if (puTax != null) {
                    for (TaxPerFamily tpf : puTax.getTaxes()) {
                        Double old = taxMap.containsKey(tpf) ? taxMap.get(tpf) : 0.0d;
                        taxMap.put(tpf, old + it.getPrice() * it.getQuantity());
                    }
                }
            }
        }
        if (!taxMap.isEmpty()) {
            for (TaxPerFamily tpf : taxMap.keySet()) {
                Double toTax = taxMap.get(tpf);
                if (toTax != null && tpf.getValue() != null) {
                    double taxApplied = BigDecimal.valueOf(toTax * tpf.getValue()).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
                    Map<String, Object> map = new HashMap<String, Object>();
                    map.put("name", tpf.getTaxName());
                    map.put("percent", tpf.getValue());
                    map.put("totax", toTax);
                    map.put("value", taxApplied);
                    data.add(map);
                    total += taxApplied;
                }
            }
        }
    }

    public void calculateTaxesPerState(State deliveryState, User user, HibernateDAO dao) {
        if (data == null) data = new ArrayList<Map<String, Object>>();
        if (total == null) total = 0.0d;

        List<Tax> taxes = (deliveryState != null) ? dao.getTaxes(deliveryState.getCountryCode(), deliveryState) : null;
        if (taxes != null && taxes.size() > 0) {
            double previousTaxes = 0.00;
            Double amountToTax = null;
            for (Tax tax : taxes)
                if (user == null || !user.hasTaxExemption(tax.getId())) {
                    double toTax = 0.0;
                    if (totalWithoutShipping != null) toTax += totalWithoutShipping;
                    if (tax.getIncludeShippping() && totalShipping != null) toTax += totalShipping;
                    if (amountToTax == null) amountToTax = toTax;
                    if (tax.getIncludeTaxes()) toTax += previousTaxes;
                    double taxApplied = BigDecimal.valueOf(tax.getValue() * toTax).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
                    previousTaxes += taxApplied;
                    total += taxApplied;
                    Map<String, Object> map = new HashMap<String, Object>();
                    map.put("name", tax.getTaxName());
                    map.put("percent", tax.getValue());
                    map.put("totax", toTax);
                    map.put("value", taxApplied);
                    data.add(map);
                }
        }
    }

    public List<Map<String, Object>> getData() {
        return data;
    }

    public double getTotal() {
        return total!=null ? total : 0.0;
    }


}
