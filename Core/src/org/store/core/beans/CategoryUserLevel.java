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
 * Descuentos a aplicar por nivel de usuario
 */
@Entity
@Table(name = "t_category_userlevel")
public class CategoryUserLevel extends BaseBean {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Porciento de descuento
    private Double percentDiscount;


    // nivel de usuario
    @ManyToOne
    private UserLevel level;

    // Categoria a la que pertenecen los productos
    @ManyToOne
    private Category category;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public UserLevel getLevel() {
        return level;
    }

    public void setLevel(UserLevel level) {
        this.level = level;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
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
                .append("volume", getLevel())
                .toString();
    }
    public boolean equals(Object other) {
        if ((this == other)) return true;
        if (!(other instanceof CategoryUserLevel)) return false;
        CategoryUserLevel castOther = (CategoryUserLevel) other;
        return new EqualsBuilder()
                .append(this.getCategory(), castOther.getCategory())
                .append(this.getLevel(), castOther.getLevel())
                .isEquals();
    }

    public int hashCode() {
        return new HashCodeBuilder()
                .append(getCategory())
                .append(getLevel())
                .toHashCode();
    }


}