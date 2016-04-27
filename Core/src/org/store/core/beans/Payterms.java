package org.store.core.beans;

import org.store.core.beans.utils.StoreBean;
import org.apache.commons.lang.builder.ToStringBuilder;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Terminos de pago a un proveedor
 */
@Entity
@Table(name="t_payterms")
public class Payterms extends BaseBean implements StoreBean {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idPayterms;

    // Descripcion del termino de pago
    @Column(length = 1024)
    private String description;

    // Tienda en que esta configurado
    @Column(length = 10)
    private String inventaryCode;

    // Numero de dias para el pago
    private Integer numDays;


    public Long getIdPayterms() {
        return idPayterms;
    }

    public void setIdPayterms(Long idPayterms) {
        this.idPayterms = idPayterms;
    }

    public String getInventaryCode() {
        return inventaryCode;
    }

    public void setInventaryCode(String inventaryCode) {
        this.inventaryCode = inventaryCode;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getNumDays() {
        return numDays;
    }

    public void setNumDays(Integer numDays) {
        this.numDays = numDays;
    }

    public String toString() {
        return new ToStringBuilder(this)
            .append("idPayterms", getIdPayterms())
            .toString();
    }

}
