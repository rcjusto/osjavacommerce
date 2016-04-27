package org.store.core.beans;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * RMAs
 */
@Entity
@Table(name = "t_rma")
public class Rma extends BaseBean {

    public static final String STATUS_REQUESTED = "requested";
    public static final String STATUS_ACCEPTED = "accepted";
    public static final String STATUS_REJECTED = "rejected";
    public static final String STATUS_CLOSED = "closed";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Tipo del RMA
    @ManyToOne
    private RmaType rmaType;

    // EStado del RMA
    @Column(length = 50)
    private String rmaStatus;

    @Column(length = 250)
    private String rmaSerialNumber;

    private Date createdDate;

    // Metodo por el que el usuario envio de regreso la mercancia
    @Column(length = 50)
    private String returnShippingMethod;

    // Tracking number del envio de la devolucion
    @Column(length = 50)
    private String returnTrackingNumber;

    // Estado de la verificacion de la mercancia devuelta por el cliente (completa, incompleta, ...)
    @Column(length = 50)
    private String verificationStatus;

    // Comentarios sobre la mercancia retornada (que falta)
    @Lob
    private String verificationDetails;

    // Tipo de reempazo (refund money o return product)
    @Column(length = 50)
    private String replacementType;

    // Metodo de envio usado para el reemplazo de la mercancia
    @ManyToOne
    private ShippingMethod replacementShippingMethod;

    // Tracking del envio de reemplazo
    @Column(length = 50)
    private String replacementTrackingNumber;

    @Column(length = 250)
    private String rmaNumber;

    // Producto de la orden q se devuelve
    @ManyToOne
    private OrderPackingProduct orderProduct;

    @OneToMany(mappedBy = "rma", cascade = CascadeType.REMOVE)
    @OrderBy(value = "actionDate")
    private List<RmaLog> logs;

    public Rma() {
        createdDate = Calendar.getInstance().getTime();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public RmaType getRmaType() {
        return rmaType;
    }

    public void setRmaType(RmaType rmaType) {
        this.rmaType = rmaType;
    }

    public String getRmaStatus() {
        return rmaStatus;
    }

    public void setRmaStatus(String rmaStatus) {
        this.rmaStatus = rmaStatus;
    }

    public String getVerificationStatus() {
        return verificationStatus;
    }

    public void setVerificationStatus(String verificationStatus) {
        this.verificationStatus = verificationStatus;
    }

    public String getVerificationDetails() {
        return verificationDetails;
    }

    public void setVerificationDetails(String verificationDetails) {
        this.verificationDetails = verificationDetails;
    }

    public String getReturnShippingMethod() {
        return returnShippingMethod;
    }

    public void setReturnShippingMethod(String returnShippingMethod) {
        this.returnShippingMethod = returnShippingMethod;
    }

    public String getReturnTrackingNumber() {
        return returnTrackingNumber;
    }

    public void setReturnTrackingNumber(String returnTrackingNumber) {
        this.returnTrackingNumber = returnTrackingNumber;
    }

    public String getReplacementType() {
        return replacementType;
    }

    public void setReplacementType(String replacementType) {
        this.replacementType = replacementType;
    }

    public ShippingMethod getReplacementShippingMethod() {
        return replacementShippingMethod;
    }

    public void setReplacementShippingMethod(ShippingMethod replacementShippingMethod) {
        this.replacementShippingMethod = replacementShippingMethod;
    }

    public String getReplacementTrackingNumber() {
        return replacementTrackingNumber;
    }

    public void setReplacementTrackingNumber(String replacementTrackingNumber) {
        this.replacementTrackingNumber = replacementTrackingNumber;
    }

    public OrderPackingProduct getOrderProduct() {
        return orderProduct;
    }

    public void setOrderProduct(OrderPackingProduct orderProduct) {
        this.orderProduct = orderProduct;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public List<RmaLog> getLogs() {
        return logs;
    }

    public void setLogs(List<RmaLog> logs) {
        this.logs = logs;
    }

    public String getRmaSerialNumber() {
        return rmaSerialNumber;
    }

    public void setRmaSerialNumber(String rmaSerialNumber) {
        this.rmaSerialNumber = rmaSerialNumber;
    }

    public String getRmaNumber() {
        return rmaNumber;
    }

    public void setRmaNumber(String rmaNumber) {
        this.rmaNumber = rmaNumber;
    }

    public boolean isOpen() {
        return STATUS_ACCEPTED.equalsIgnoreCase(rmaStatus) || STATUS_REQUESTED.equalsIgnoreCase(rmaStatus);
    }

    public Order getOrder() {
        return (orderProduct!=null) ? orderProduct.getOrder() : null;
    }

    public Product getProduct() {
        return (orderProduct!=null) ? orderProduct.getProduct() : null;
    }

    public String toString() {
        return new ToStringBuilder(this)
                .append("id", getId())
                .toString();
    }

    public boolean equals(Object other) {
        if ((this == other)) return true;
        if (!(other instanceof Rma)) return false;
        Rma castOther = (Rma) other;
        return new EqualsBuilder()
                .append(this.getId(), castOther.getId())
                .isEquals();
    }


}