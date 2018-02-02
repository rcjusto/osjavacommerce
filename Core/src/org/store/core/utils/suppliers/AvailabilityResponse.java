package org.store.core.utils.suppliers;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AvailabilityResponse {

    public static final String STATUS_ACTIVE = "active";
    public static final String STATUS_DISCONTINUED = "discontinued";
    public static final String STATUS_NOT_FOUND = "not.found";

    private String sku;
    private String currency;
    private Long stock;
    private Double price;
    private String etaStr;
    private Date etaDate;
    private Long requested;
    private String status;
    private String error;
    private List<Map<String, Object>> allStock;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public Long getStock() {
        return (stock != null) ? stock : 0;
    }

    public void setStock(Long stock) {
        this.stock = stock;
    }

    public void addStock(Long s) {
        if (s != null) this.stock = getStock() + s;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public String getEtaStr() {
        return etaStr;
    }

    public void setEtaStr(String etaStr) {
        this.etaStr = etaStr;
    }

    public Date getEtaDate() {
        return etaDate;
    }

    public void setEtaDate(Date etaDate) {
        this.etaDate = etaDate;
    }

    public Long getRequested() {
        return requested;
    }

    public void setRequested(Long requested) {
        this.requested = requested;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public List<Map<String, Object>> getAllStock() {
        return allStock;
    }

    public void setAllStock(List<Map<String, Object>> allStock) {
        this.allStock = allStock;
    }

    public void addWarehouse(String name, Long stock, Date eta) {
        if (allStock == null) allStock = new ArrayList<Map<String, Object>>();
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("name", name);
        map.put("stock", stock);
        map.put("eta", eta);
        allStock.add(map);
    }

    public long getTotalStock() {
        long res = 0;
        if (allStock != null && !allStock.isEmpty()) {
            for (Map<String, Object> map : allStock) {
                Long st = map.containsKey("stock") ? (Long) map.get("stock") : null;
                if (st != null) res += st;
            }
        }
        return res;
    }

    public Date getNextEta() {
        Date res = null;
        if (allStock != null && !allStock.isEmpty()) {
            for (Map<String, Object> map : allStock) {
                Date eta = map.containsKey("eta") ? (Date) map.get("eta") : null;
                if (eta != null && (res == null || res.after(eta))) res = eta;
            }
        }
        return res;
    }


}
