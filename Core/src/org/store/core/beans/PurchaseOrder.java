package org.store.core.beans;

import org.store.core.beans.utils.StoreBean;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Purchase orders
 */
@Entity
@Table(name = "t_purchase_order")
public class PurchaseOrder extends BaseBean implements StoreBean {

    public static final String STATUS_NEW = "New";
    public static final String STATUS_SENDED = "Sent";
    public static final String STATUS_RECEIVED = "Received";
    public static final String STATUS_CLOSED = "Closed";
    public static final String STATUS_CANCELED = "Cancelled";
    public static final String STATUS_PAID = "Paid";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 50)
    private String inventaryCode;

    @Column(length = 255)
    private String shipVia;

    @Column(length = 255)
    private String fob;

    private Double freight;

    @Lob
    private String remarks;

    @Column(length = 50)
    private String status;

    private Double discount;

    private Date requiredDate;

    private Date registerDate;

    @Column(length = 255)
    private String paymentType;

    private Double paymentAmount;

    private Date datePaid;

    @Column(length = 255)
    private String checkNumber;

    @Lob
    private String comments;

    @ManyToOne
    private Provider provider;

    @ManyToOne
    private LocationStore locationStore;

    @ManyToOne
    private Payterms payterms;

    @OneToMany(mappedBy = "purchaseOrder")
    @OrderBy(value = "id asc")
    private List<PurchaseOrderLine> lines;

    @OneToMany(mappedBy = "purchaseOrder")
    @OrderBy(value = "id asc")
    private List<PurchaseHistory> history;

    public PurchaseOrder() {
        status = STATUS_NEW;
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

    public String getShipVia() {
        return shipVia;
    }

    public void setShipVia(String shipVia) {
        this.shipVia = shipVia;
    }

    public String getFob() {
        return fob;
    }

    public void setFob(String fob) {
        this.fob = fob;
    }

    public Double getFreight() {
        return freight;
    }

    public void setFreight(Double freight) {
        this.freight = freight;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Double getDiscount() {
        return discount;
    }

    public void setDiscount(Double discount) {
        this.discount = discount;
    }

    public Date getRequiredDate() {
        return requiredDate;
    }

    public void setRequiredDate(Date requiredDate) {
        this.requiredDate = requiredDate;
    }

    public Date getRegisterDate() {
        return registerDate;
    }

    public void setRegisterDate(Date registerDate) {
        this.registerDate = registerDate;
    }

    public String getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(String paymentType) {
        this.paymentType = paymentType;
    }

    public Double getPaymentAmount() {
        return paymentAmount;
    }

    public void setPaymentAmount(Double paymentAmount) {
        this.paymentAmount = paymentAmount;
    }

    public Date getDatePaid() {
        return datePaid;
    }

    public void setDatePaid(Date datePaid) {
        this.datePaid = datePaid;
    }

    public String getCheckNumber() {
        return checkNumber;
    }

    public void setCheckNumber(String checkNumber) {
        this.checkNumber = checkNumber;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public Provider getProvider() {
        return provider;
    }

    public void setProvider(Provider provider) {
        this.provider = provider;
    }

    public LocationStore getLocationStore() {
        return locationStore;
    }

    public void setLocationStore(LocationStore locationStore) {
        this.locationStore = locationStore;
    }

    public Payterms getPayterms() {
        return payterms;
    }

    public void setPayterms(Payterms payterms) {
        this.payterms = payterms;
    }

    public List<PurchaseOrderLine> getLines() {
        return lines;
    }

    public void setLines(List<PurchaseOrderLine> lines) {
        this.lines = lines;
    }

    public List<PurchaseHistory> getHistory() {
        return history;
    }

    public void setHistory(List<PurchaseHistory> history) {
        this.history = history;
    }

    public PurchaseOrderLine getLineByIdProduct(Long idProduct) {
        if (lines!=null && !lines.isEmpty()) {
            for(PurchaseOrderLine pol : lines)
                if (idProduct.equals(pol.getProduct().getIdProduct()))
                    return  pol;
        }
        return null;
    }

    public double getSubtotalProduct() {
        double res = 0;
        if (lines!=null && !lines.isEmpty()) {
            for(PurchaseOrderLine pol : lines) res += pol.getTotal();
        }
        return res;
    }

    public double getSubtotalDiscount() {
        return (discount!=null && discount>0) ? (getSubtotalProduct() * discount / 100) : 0;
    }

    public double getTotal() {
        double res = getSubtotalProduct() - getSubtotalDiscount();
        if (freight!=null) res += freight;
        return res;
    }

    public boolean receivedAll() {
        if (lines!=null && !lines.isEmpty()) {
            for(PurchaseOrderLine pol : lines) if (!pol.receivedAll()) return false;
        }
        return true;
    }

    public String toString() {
        return new ToStringBuilder(this)
                .append("id", getId())
                .toString();
    }

    public boolean equals(Object other) {
        if ((this == other)) return true;
        if (!(other instanceof PurchaseOrder)) return false;
        PurchaseOrder castOther = (PurchaseOrder) other;
        return new EqualsBuilder()
                .append(this.getId(), castOther.getId())
                .isEquals();
    }

    public void addLine(PurchaseOrderLine pol) {
        if (lines==null) lines = new ArrayList<PurchaseOrderLine>();
        lines.add(pol);
    }
}