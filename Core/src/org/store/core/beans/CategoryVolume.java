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
 * Descuentos a aplicar al precio de los productos por la compra en volumen
 */
@Entity
@Table(name = "t_category_volume")
public class CategoryVolume extends BaseBean {

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

    // Categoria a la que pertenecen los productos
    @ManyToOne
    private Category category;

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

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Double getPercentDiscount() {
        return percentDiscount;
    }

    public void setPercentDiscount(Double percentDiscount) {
        this.percentDiscount = percentDiscount;
    }

    public String toString() {
        return new ToStringBuilder(this)
                .append("category", getCategory())
                .append("volume", getVolume())
                .toString();
    }
    public boolean equals(Object other) {
        if ((this == other)) return true;
        if (!(other instanceof CategoryVolume)) return false;
        CategoryVolume castOther = (CategoryVolume) other;
        return new EqualsBuilder()
                .append(this.getCategory(), castOther.getCategory())
                .append(this.getVolume(), castOther.getVolume())
                .isEquals();
    }

    public int hashCode() {
        return new HashCodeBuilder()
                .append(getCategory())
                .append(getVolume())
                .toHashCode();
    }


}