package org.store.core.beans;

import org.store.core.beans.utils.StoreBean;
import org.apache.commons.lang.builder.ToStringBuilder;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * Taxes
 */
@Entity
@Table(name = "t_tax")
public class Tax extends BaseBean implements StoreBean {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(length = 512)
    private String taxName;
    @ManyToOne
    private State state;

    // Tienda en la q esta configurado el producto
    @Column(length = 10)
    private String inventaryCode;

    @Column(length = 5)
    private String country;

    private Double value;

    private Boolean includeShippping;
    private Boolean includeTaxes;

    @Column(length = 50)
    private String externalCode;

    private Integer taxOrder;

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

    public String getTaxName() {
        return taxName;
    }

    public void setTaxName(String taxName) {
        this.taxName = taxName;
    }

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public Boolean getIncludeShippping() {
        return includeShippping!=null && includeShippping;
    }

    public void setIncludeShippping(Boolean includeShippping) {
        this.includeShippping = includeShippping;
    }

    public Boolean getIncludeTaxes() {
        return includeTaxes!=null && includeTaxes;
    }

    public void setIncludeTaxes(Boolean includeTaxes) {
        this.includeTaxes = includeTaxes;
    }

    public Integer getTaxOrder() {
        return taxOrder;
    }

    public void setTaxOrder(Integer taxOrder) {
        this.taxOrder = taxOrder;
    }

    public String getExternalCode() {
        return externalCode;
    }

    public void setExternalCode(String externalCode) {
        this.externalCode = externalCode;
    }

    public String toString() {
        return new ToStringBuilder(this)
                .append("id", getId())
                .toString();
    }

}