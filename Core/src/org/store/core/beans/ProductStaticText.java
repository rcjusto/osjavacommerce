package org.store.core.beans;

import org.store.core.beans.utils.ProdStaticText;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * Bloques de texto a poner en un producto especifico
 */
@Entity
@Table(name = "t_product_static_text")
public class ProductStaticText extends BaseBean implements ProdStaticText {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Producto
    @ManyToOne
    private Product product;

    // Texto a poner
    @ManyToOne
    private StaticText staticText;

    private Integer contentOrder;

    @Column(length = 250)
    private String contentPlace;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public StaticText getStaticText() {
        return staticText;
    }

    public void setStaticText(StaticText staticText) {
        this.staticText = staticText;
    }

    public Integer getContentOrder() {
        return contentOrder;
    }

    public void setContentOrder(Integer contentOrder) {
        this.contentOrder = contentOrder;
    }

    public String getContentPlace() {
        return contentPlace;
    }

    public void setContentPlace(String contentPlace) {
        this.contentPlace = contentPlace;
    }

    public String toString() {
        return new ToStringBuilder(this)
                .append("id", getId())
                .toString();
    }

    public boolean equals(Object other) {
        if ((this == other)) return true;
        if (!(other instanceof ProductStaticText)) return false;
        ProductStaticText castOther = (ProductStaticText) other;
        return new EqualsBuilder()
                .append(this.getId(), castOther.getId())
                .isEquals();
    }

    public int hashCode() {
        return new HashCodeBuilder()
                .append(getId())
                .toHashCode();
    }



}