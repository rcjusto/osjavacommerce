package org.store.core.beans;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;


@Entity
@Table(name="t_product_variation")
public class ProductVariation extends BaseBean {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(length = 255)
    private String caract1;
    @Column(length = 255)
    private String caract2;
    @Column(length = 255)
    private String caract3;
    private Long stock;
    private Double priceInc;
    private Double costPriceInc;

    // Tamanno del producto
    private Double dimentionLength;
    private Double dimentionWidth;
    private Double dimentionHeight;

    // Peso del producto
    private Double weight;

    @ManyToOne
    private Product product;

    public ProductVariation() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCaract1() {
        return caract1;
    }

    public void setCaract1(String caract1) {
        this.caract1 = caract1;
    }

    public String getCaract2() {
        return caract2;
    }

    public void setCaract2(String caract2) {
        this.caract2 = caract2;
    }

    public String getCaract3() {
        return caract3;
    }

    public void setCaract3(String caract3) {
        this.caract3 = caract3;
    }

    public Long getStock() {
        return stock;
    }

    public void setStock(Long stock) {
        this.stock = stock;
    }

    public Double getPriceInc() {
        return priceInc;
    }

    public void setPriceInc(Double priceInc) {
        this.priceInc = priceInc;
    }

    public Double getCostPriceInc() {
        return costPriceInc;
    }

    public void setCostPriceInc(Double costPriceInc) {
        this.costPriceInc = costPriceInc;
    }

    public Double getDimentionLength() {
        return dimentionLength;
    }

    public void setDimentionLength(Double dimentionLength) {
        this.dimentionLength = dimentionLength;
    }

    public Double getDimentionWidth() {
        return dimentionWidth;
    }

    public void setDimentionWidth(Double dimentionWidth) {
        this.dimentionWidth = dimentionWidth;
    }

    public Double getDimentionHeight() {
        return dimentionHeight;
    }

    public void setDimentionHeight(Double dimentionHeight) {
        this.dimentionHeight = dimentionHeight;
    }

    public Double getWeight() {
        return weight;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public String getFullOption() {
        StringBuffer buff = new StringBuffer();
        if (StringUtils.isNotEmpty(caract1)) buff.append(caract1);
        if (StringUtils.isNotEmpty(caract2)) {
            if (StringUtils.isNotEmpty(buff.toString())) buff.append(", ");
            buff.append(caract2);
        }
        if (StringUtils.isNotEmpty(caract3)) {
            if (StringUtils.isNotEmpty(buff.toString())) buff.append(", ");
            buff.append(caract3);
        }
        return buff.toString();
    }

    public String toString() {
        return new ToStringBuilder(this)
            .append("id", getId())
            .toString();
    }

    public void addStock(Long v) {
        if (v!=null) {
            if (stock!=null) stock += v;
            else stock = v;
        }
    }
}
