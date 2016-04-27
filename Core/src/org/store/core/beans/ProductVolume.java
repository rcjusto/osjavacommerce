package org.store.core.beans;

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
 * Precios por cantidad
 */
@Entity
@Table(name="t_product_volume")
public class ProductVolume extends BaseBean {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // numero minimo de productos a comprar
    private Long volume;

    // Porciento de descuento
    private Double percentDiscount;

    // Texto adicional
    @Column(length = 1024)
    private String description;

    // Producto
    @ManyToOne
    private Product product;

    public ProductVolume() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public String toString() {
        return new ToStringBuilder(this)
            .append("Product", getProduct())
            .append("Size", getVolume())
            .toString();
    }

    public boolean equals(Object other) {
        if ( (this == other ) ) return true;
        if ( !(other instanceof ProductVolume) ) return false;
        ProductVolume castOther = (ProductVolume) other;
        return new EqualsBuilder()
            .append(this.getProduct(), castOther.getProduct())
            .append(this.getVolume(), castOther.getVolume())
            .isEquals();
    }

    public int hashCode() {
        return new HashCodeBuilder()
            .append(getProduct())
            .append(getVolume())
            .toHashCode();
    }

}
