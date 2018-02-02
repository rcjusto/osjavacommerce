package org.store.core.beans;

import org.apache.commons.lang.builder.ToStringBuilder;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * Valores de attributos para el producto
 */
@Entity
@Table(name="t_category_property")
public class CategoryProperty extends BaseBean {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Valor del attributo
    @Lob
    private String defaultValue;

    // Filtrar por esta propiedad
    private Boolean canfilter;

    // orden en el filtrado
    private Integer orderFilter;

    // Attributo
    @ManyToOne
    private AttributeProd attribute;

    // Producto
    @ManyToOne
    private Category category;

    public CategoryProperty() {
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public AttributeProd getAttribute() {
        return attribute;
    }

    public void setAttribute(AttributeProd attribute) {
        this.attribute = attribute;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public Boolean getCanfilter() {
        return canfilter!=null && canfilter;
    }

    public void setCanfilter(Boolean canfilter) {
        this.canfilter = canfilter;
    }

    public Integer getOrderFilter() {
        return orderFilter;
    }

    public void setOrderFilter(Integer orderFilter) {
        this.orderFilter = orderFilter;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public String toString() {
        return new ToStringBuilder(this)
            .append("id", getId())
            .toString();
    }

}