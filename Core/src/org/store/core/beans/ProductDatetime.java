package org.store.core.beans;

import org.apache.commons.lang.builder.ToStringBuilder;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.util.Date;

/**
 * Datos del producto para una fecha especifica
 */
@Entity
@Table(name="t_product_datetime")
public class ProductDatetime extends BaseBean {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idProductDatetime;

    // Fecha especifica
    private Date posibleDate;

    // Hora especifica
    private Date posibleTime;

    // Dias de la semana
    private Integer posibleDateWeek;

    // Cantidad disponible
    private Long stock;

    // Incremento del precio de venta
    private Double priceInc;

    // Incremento del precio de costo
    private Double costPriceInc;

    // Producto
    @ManyToOne
    private Product product;

    public ProductDatetime() {
    }

    public Long getIdProductDatetime() {
        return idProductDatetime;
    }

    public void setIdProductDatetime(Long idProductDatetime) {
        this.idProductDatetime = idProductDatetime;
    }

    public Date getPosibleDate() {
        return posibleDate;
    }

    public void setPosibleDate(Date posibleDate) {
        this.posibleDate = posibleDate;
    }

    public Date getPosibleTime() {
        return posibleTime;
    }

    public void setPosibleTime(Date posibleTime) {
        this.posibleTime = posibleTime;
    }

    public Integer getPosibleDateWeek() {
        return posibleDateWeek;
    }

    public void setPosibleDateWeek(Integer posibleDateWeek) {
        this.posibleDateWeek = posibleDateWeek;
    }

    public Long getStock() {
        return stock;
    }

    public void setStock(Long stock) {
        this.stock = stock;
    }

    public Double getPriceInc() {
        return priceInc;
    }

    public void setPriceInc(Double priceInc) {
        this.priceInc = priceInc;
    }

    public Double getCostPriceInc() {
        return costPriceInc;
    }

    public void setCostPriceInc(Double costPriceInc) {
        this.costPriceInc = costPriceInc;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public String toString() {
        return new ToStringBuilder(this)
            .append("idProductDatetime", getIdProductDatetime())
            .toString();
    }

}
