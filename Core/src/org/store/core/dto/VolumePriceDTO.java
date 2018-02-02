package org.store.core.dto;

import org.store.core.beans.CategoryVolume;
import org.store.core.beans.ProductVolume;


public class VolumePriceDTO {

    private Long volume;
    private Double percentDiscount;
    private String description;
    private Double unitPrice;

    public VolumePriceDTO(ProductVolume v, Double up) {
        setVolume(v.getVolume());
        setPercentDiscount(v.getPercentDiscount());
        setDescription(v.getDescription());
        setUnitPrice(up);
    }

    public VolumePriceDTO(CategoryVolume v, Double up) {
        setVolume(v.getVolume());
        setPercentDiscount(v.getPercentDiscount());
        setDescription(v.getDescription());
        setUnitPrice(up);
    }

    public Long getVolume() {
        return volume;
    }

    public void setVolume(Long volume) {
        this.volume = volume;
    }

    public Double getPercentDiscount() {
        return percentDiscount;
    }

    public void setPercentDiscount(Double percentDiscount) {
        this.percentDiscount = percentDiscount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(Double unitPrice) {
        this.unitPrice = unitPrice;
    }
}
