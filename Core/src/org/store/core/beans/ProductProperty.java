package org.store.core.beans;

import org.apache.commons.lang.builder.ToStringBuilder;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * Valores de attributos para el producto
 */
@Entity
@Table(name="t_product_property")
public class ProductProperty extends BaseBean {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Valor del attributo
    @Column(length = 1024)
    private String propertyValue;

    // Attributo
    @ManyToOne
    private AttributeProd attribute;

    // Producto
    @ManyToOne
    private Product product;

    public ProductProperty() {
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

    public String getPropertyValue() {
        return propertyValue;
    }

    public void setPropertyValue(String propertyValue) {
        this.propertyValue = propertyValue;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public String toString() {
        return new ToStringBuilder(this)
            .append("id", getId())
            .toString();
    }

}