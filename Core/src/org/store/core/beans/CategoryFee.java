package org.store.core.beans;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import javax.persistence.*;

/**
 * Category Fee
 */
@Entity
@Table(name = "t_category_fee")
public class CategoryFee extends BaseBean {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Categoria
    @ManyToOne
    private Category category;

    // Fee
    @ManyToOne
    private Fee fee;

    private Double value;
    private Double percent;

    private Boolean applyChildren;


    public CategoryFee() {
        applyChildren = true;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public Fee getFee() {
        return fee;
    }

    public void setFee(Fee fee) {
        this.fee = fee;
    }

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }

    public Double getPercent() {
        return percent;
    }

    public void setPercent(Double percent) {
        this.percent = percent;
    }

    public Boolean getApplyChildren() {
        return applyChildren;
    }

    public void setApplyChildren(Boolean applyChildren) {
        this.applyChildren = applyChildren;
    }

    public String toString() {
        return new ToStringBuilder(this)
                .append("id", getId())
                .toString();
    }

    public boolean equals(Object other) {
        if ((this == other)) return true;
        if (!(other instanceof CategoryFee)) return false;
        CategoryFee castOther = (CategoryFee) other;
        return new EqualsBuilder()
                .append(this.getId(), castOther.getId())
                .isEquals();
    }
    
    public Double getTotal(ShopCartItem item) {
        if (getValue()!=null && getValue()>0) return getValue();
        else if (getPercent()!=null && getPercent()>0) return getPercent() * item.getPrice();
        return  0d;
    }

    public int hashCode() {
        return new HashCodeBuilder()
                .append(getId())
                .toHashCode();
    }



}