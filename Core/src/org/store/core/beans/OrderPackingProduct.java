package org.store.core.beans;

import org.store.core.globals.SomeUtils;
import org.apache.commons.lang.builder.ToStringBuilder;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * Datos del producto embalado para distribucion
 */
@Entity
@Table(name="t_order_packing_product")
public class OrderPackingProduct extends BaseBean {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Tracking del paquete
    @Column(length = 250)
    private String trackingNumber;

    // Paquete donde va incluido
    @ManyToOne
    private OrderPacking packing;

    // Producto
    @ManyToOne
    private OrderDetailProduct orderDetailProduct;


    public OrderPackingProduct() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTrackingNumber() {
        return trackingNumber;
    }

    public void setTrackingNumber(String trackingNumber) {
        this.trackingNumber = trackingNumber;
    }

    public OrderPacking getPacking() {
        return packing;
    }

    public void setPacking(OrderPacking packing) {
        this.packing = packing;
    }

    public OrderDetailProduct getOrderDetailProduct() {
        return orderDetailProduct;
    }

    public void setOrderDetailProduct(OrderDetailProduct orderDetailProduct) {
        this.orderDetailProduct = orderDetailProduct;
    }

    public Order getOrder() {
        return (orderDetailProduct!=null) ? orderDetailProduct.getOrder() : null;
    }

    public Product getProduct() {
        return (orderDetailProduct!=null) ? orderDetailProduct.getProduct() : null;
    }


    public boolean canCreateRmaType(RmaType type) {
        if (packing.getDeliveryDate()==null || type==null) return false;
        long numDays = SomeUtils.dayDiff(packing.getDeliveryDate(),SomeUtils.today());
        return (type.getMaxDays()==null || type.getMaxDays()>=numDays);
    }

    public String toString() {
        return new ToStringBuilder(this)
            .append("id", getId())
            .toString();
    }

}