package org.store.pdf;

import org.store.core.beans.Product;
import org.store.core.beans.ProductLang;
import org.store.core.beans.UserLevel;
import org.store.core.dao.HibernateDAO;

/**
 * Rogelio Caballero
 * 15/12/11 22:22
 */
public class PDFProduct {

    private Long id;
    private String code;
    private String name;
    private String imageSmall;
    private String imageNormal;
    private String description;
    private String manufacturer;
    private Double price;

    public PDFProduct(Product product, UserLevel level, String lang, HibernateDAO dao) {
        this.id = product.getIdProduct();
        this.code = product.getPartNumber();
        this.name = product.getProductName(lang);
        ProductLang pl = product.getLanguage(lang);
        if (pl!=null) this.description = pl.getDescription();
        this.manufacturer = (product.getManufacturer()!=null) ? product.getManufacturer().getManufacturerName() : null;
        this.price = product.getFinalPrice(level, 1, null, dao);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public String getImageSmall() {
        return imageSmall;
    }

    public void setImageSmall(String imageSmall) {
        this.imageSmall = imageSmall;
    }

    public String getImageNormal() {
        return imageNormal;
    }

    public void setImageNormal(String imageNormal) {
        this.imageNormal = imageNormal;
    }
}
