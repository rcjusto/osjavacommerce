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
 * Bloques de texto a poner en los productos de las categorias
 */
@Entity
@Table(name = "t_category_static_text")
public class CategoryStaticText extends BaseBean implements ProdStaticText {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Categoria
    @ManyToOne
    private Category category;

    // Fee
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

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
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
        if (!(other instanceof CategoryStaticText)) return false;
        CategoryStaticText castOther = (CategoryStaticText) other;
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