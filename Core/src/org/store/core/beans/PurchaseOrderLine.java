package org.store.core.beans;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

/**
 * Purchase lines
 */
@Entity
@Table(name = "t_purchase_order_line")
public class PurchaseOrderLine extends BaseBean {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double costPrice;
    private Integer quantity;

    @Column(length = 5)
    private String currency;

    private Integer received;

    @Column(length = 255)
    private String status;

    @ManyToOne
    private Product product;

    @ManyToOne
    private PurchaseOrder purchaseOrder;

    public PurchaseOrderLine() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }


    public Double getCostPrice() {
        return costPrice!=null ? costPrice : 0d;
    }

    public void setCostPrice(Double costPrice) {
        this.costPrice = costPrice;
    }

    public Integer getQuantity() {
        return quantity!=null ? quantity : 0;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public Integer getReceived() {
        return received!=null ? received : 0;
    }

    public void setReceived(Integer received) {
        this.received = received;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public PurchaseOrder getPurchaseOrder() {
        return purchaseOrder;
    }

    public void setPurchaseOrder(PurchaseOrder purchaseOrder) {
        this.purchaseOrder = purchaseOrder;
    }


    @Transient
    public double getTotal() {
        return getQuantity() * getCostPrice();
    }

    @Transient
    public double getTotalReceived() {
        return getQuantity() * getReceived();
    }

    public String toString() {
        return new ToStringBuilder(this)
                .append("id", getId())
                .toString();
    }

    public boolean equals(Object other) {
        if ((this == other)) return true;
        if (!(other instanceof PurchaseOrderLine)) return false;
        PurchaseOrderLine castOther = (PurchaseOrderLine) other;
        return new EqualsBuilder()
                .append(this.getId(), castOther.getId())
                .isEquals();
    }

    public void addReceived(Integer q) {
        if (received==null) received = q;
        else received += q;
    }

    public boolean receivedAll() {
        return new EqualsBuilder()
                .append(this.quantity, this.received)
                .isEquals();
    }

}