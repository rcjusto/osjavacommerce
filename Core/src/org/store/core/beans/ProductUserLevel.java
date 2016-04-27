package org.store.core.beans;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * Descuento a aplicar por nivel de usuario
 */
@Entity
@Table(name="t_product_userlevel")
public class ProductUserLevel extends BaseBean {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Porciento de descuento
    private Double percentDiscount;

    // Nivel de usuario
    @ManyToOne
    private UserLevel level;

    // Producto
    @ManyToOne
    private Product product;

    public ProductUserLevel() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Double getPercentDiscount() {
        return percentDiscount;
    }

    public void setPercentDiscount(Double percentDiscount) {
        this.percentDiscount = percentDiscount;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public UserLevel getLevel() {
        return level;
    }

    public void setLevel(UserLevel level) {
        this.level = level;
    }

    public String toString() {
        return new ToStringBuilder(this)
            .append("Product", getProduct())
            .append("Level", getLevel())
            .toString();
    }

    public boolean equals(Object other) {
        if ( (this == other ) ) return true;
        if ( !(other instanceof ProductUserLevel) ) return false;
        ProductUserLevel castOther = (ProductUserLevel) other;
        return new EqualsBuilder()
            .append(this.getProduct(), castOther.getProduct())
            .append(this.getLevel(), castOther.getLevel())
            .isEquals();
    }

    public int hashCode() {
        return new HashCodeBuilder()
            .append(getProduct())
            .append(getLevel())
            .toHashCode();
    }

}