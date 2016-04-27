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
 * Posicion que ocupa la categoria en el menu
 */
@Entity
@Table(name = "t_category_view")
public class CategoryView extends BaseBean {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(length = 20)
    // Menu en el q aparece
    private String place;

    // Orden en el que aparece
    private Integer catOrder;

    // Categoria
    @ManyToOne
    private Category category;

    public CategoryView() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPlace() {
        return place;
    }

    public void setPlace(String place) {
        this.place = place;
    }

    public Integer getCatOrder() {
        return catOrder;
    }

    public void setCatOrder(Integer catOrder) {
        this.catOrder = catOrder;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public String toString() {
        return new ToStringBuilder(this)
                .append("Category", getCategory())
                .append("Place", getPlace())
                .toString();
    }

    public boolean equals(Object other) {
        if ((this == other)) return true;
        if (!(other instanceof CategoryView)) return false;
        CategoryView castOther = (CategoryView) other;
        return new EqualsBuilder()
                .append(this.getCategory(), castOther.getCategory())
                .append(this.getPlace(), castOther.getPlace())
                .isEquals();
    }

    public int hashCode() {
        return new HashCodeBuilder()
                .append(getCategory())
                .append(getPlace())
                .toHashCode();
    }



}
