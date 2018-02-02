package org.store.core.beans;

import org.store.core.beans.utils.StoreBean;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * Fixed or flat shipping rate
 */
@Entity
@Table(name = "t_shipping_rate")
public class ShippingRate extends BaseBean implements StoreBean {

    /** Flat: se agrupan los productos con este shipping y solo se cobra uno
     * mientras no se exceda la cantidad de flat shipping permitida en el carro**/
    public static final String TYPE_FLAT = "flat";
    /** Fixed: se suma el shipping de cada producto independiente **/
    public static final String TYPE_FIXED = "fixed";
    /** Fixed: se usa para eliminar un FLAT o FIXED shipping heredado de la categoria **/
    public static final String TYPE_LIVE = "live";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Tienda donde esta configurado la provincia
    @Column(length = 10)
    private String inventaryCode;

    // Tipo de rate (flat or fixed)
    @Column(length = 20)
    private String shippingType;

    // Estado o Provincia donde se define el shipping
    @ManyToOne
    private State state;

    // Categoria a la que se aplica el shipping
    @ManyToOne
    private Category category;

    // Producto al que se aplica el shipping
    @ManyToOne
    private Product product;

    // Valor del shippping
    private Double value;


    public ShippingRate() {
        this.shippingType = TYPE_FIXED;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getInventaryCode() {
        return inventaryCode;
    }

    public void setInventaryCode(String inventaryCode) {
        this.inventaryCode = inventaryCode;
    }

    public String getShippingType() {
        return shippingType;
    }

    public void setShippingType(String shippingType) {
        if (TYPE_FIXED.equalsIgnoreCase(shippingType) || TYPE_FLAT.equalsIgnoreCase(shippingType) || TYPE_LIVE.equalsIgnoreCase(shippingType)) this.shippingType = shippingType;
        else this.shippingType = TYPE_FIXED;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }

    public boolean equals(Object other) {
        if ((this == other)) return true;
        if (!(other instanceof ShippingRate)) return false;
        ShippingRate castOther = (ShippingRate) other;
        return new EqualsBuilder()
                .append(this.getId(), castOther.getId())
                .isEquals();
    }

    public String toString() {
        return new ToStringBuilder(this)
                .append("id", getId())
                .toString();
    }

}