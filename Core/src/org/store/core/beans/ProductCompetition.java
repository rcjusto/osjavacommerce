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
 * Datos de la competencia
 */
@Entity
@Table(name="t_product_competition")
public class ProductCompetition extends BaseBean {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idCompetition;

    // Nombre de la tienda competidora
    @Column(length = 512)
    private String compName;

    // Url del producto en la tienda
    @Column(length = 1024)
    private String compUrl;

    // Precio en la tienda
    private Double compPrice;

    // Producto
    @ManyToOne
    private Product product;

    public ProductCompetition() {
    }


    public Long getIdCompetition() {
        return idCompetition;
    }

    public void setIdCompetition(Long idCompetition) {
        this.idCompetition = idCompetition;
    }

    public String getCompName() {
        return compName;
    }

    public void setCompName(String compName) {
        this.compName = compName;
    }

    public String getCompUrl() {
        return compUrl;
    }

    public void setCompUrl(String compUrl) {
        this.compUrl = compUrl;
    }

    public Double getCompPrice() {
        return compPrice;
    }

    public void setCompPrice(Double compPrice) {
        this.compPrice = compPrice;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public String toString() {
        return new ToStringBuilder(this)
            .append("idCompetition", getIdCompetition())
            .toString();
    }

}
