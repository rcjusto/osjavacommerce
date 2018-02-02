package org.store.core.beans;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.struts2.json.JSONException;
import org.apache.struts2.json.JSONUtil;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@Entity
@Table(name="t_order_detail_product")
public class OrderDetailProduct extends BaseBean {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Valores personalizados
    private Long idVariation;
    @Column(length = 1024)
    private String caractValue1;
    @Column(length = 1024)
    private String caractValue2;
    @Column(length = 1024)
    private String caractValue3;
    @Column(length = 250)

    // Tracking number del envio
    private String trackingNumber;

    // Estado del envio (para producto digitales, indica si el prducto ya se descargo o no)
    @Column(length = 1024)
    private String trackingStatus;

    // Numero de downloads. Solo en caso q el producto sea downloable
    private Integer downloads;

    @Column(length = 1024)
    private String downloadLink;

    // Producto
    @ManyToOne
    private Product product;

    private Double costPrice;
    private Double tax;

    @Lob
    private String taxData;
    @Transient
    private List<Map<String, Object>> taxesList;

    @Column(length = 512)
    private String feeName;
    private Double feeValue;


    @Column(length = 250)
    private String barCode;

    @ManyToOne
    private OrderDetail orderDetail;

    public OrderDetailProduct() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getIdVariation() {
        return idVariation;
    }

    public void setIdVariation(Long idVariation) {
        this.idVariation = idVariation;
    }

    public String getCaractValue1() {
        return caractValue1;
    }

    public void setCaractValue1(String caractValue1) {
        this.caractValue1 = caractValue1;
    }

    public String getCaractValue2() {
        return caractValue2;
    }

    public void setCaractValue2(String caractValue2) {
        this.caractValue2 = caractValue2;
    }

    public String getCaractValue3() {
        return caractValue3;
    }

    public void setCaractValue3(String caractValue3) {
        this.caractValue3 = caractValue3;
    }

    public String getTrackingNumber() {
        return trackingNumber;
    }

    public void setTrackingNumber(String trackingNumber) {
        this.trackingNumber = trackingNumber;
    }

    public Integer getDownloads() {
        return downloads;
    }

    public void setDownloads(Integer downloads) {
        this.downloads = downloads;
    }

    public String getDownloadLink() {
        return downloadLink;
    }

    public void setDownloadLink(String downloadLink) {
        this.downloadLink = downloadLink;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public OrderDetail getOrderDetail() {
        return orderDetail;
    }

    public void setOrderDetail(OrderDetail orderDetail) {
        this.orderDetail = orderDetail;
    }

    public String getFeeName() {
        return feeName;
    }

    public void setFeeName(String feeName) {
        this.feeName = feeName;
    }

    public Double getFeeValue() {
        return feeValue;
    }

    public void setFeeValue(Double feeValue) {
        this.feeValue = feeValue;
    }

    public Order getOrder() {
        return (orderDetail!=null) ? orderDetail.getOrder() : null;
    }

    public String getBarCode() {
        return barCode;
    }

    public void setBarCode(String barCode) {
        this.barCode = barCode;
    }

    public Double getCostPrice() {
        return costPrice;
    }

    public void setCostPrice(Double costPrice) {
        this.costPrice = costPrice;
    }

    public String getTrackingStatus() {
        return trackingStatus;
    }

    public void setTrackingStatus(String trackingStatus) {
        this.trackingStatus = trackingStatus;
    }

    public Double getTax() {
        return tax;
    }

    public void setTax(Double tax) {
        this.tax = tax;
    }

    public String getTaxData() {
        return taxData;
    }

    public void setTaxData(String taxData) {
        this.taxData = taxData;
    }

    public void addTax(Map<String,Object> m) {
        if (taxesList==null) taxesList = new ArrayList<Map<String,Object>>();
        taxesList.add(m);
        // Serialize data
        try {
            taxData = (taxesList!=null && taxesList.size()>0) ? JSONUtil.serialize(taxesList) : null;
        } catch (JSONException e) {
            log.error(e.getMessage(), e);
        }
    }

    public List<Map<String,Object>> getTaxesList() {
        if (taxesList == null || taxesList.size() < 1) {
            try {
                taxesList =  (!isEmpty(taxData)) ? (List<Map<String,Object>>) JSONUtil.deserialize(taxData) : null;
            } catch (JSONException e) {
                log.error(e.getMessage(), e);
            }
        }
        return taxesList;
    }

    public Double getPercentOfPrice() {
        return (getOrderDetail().getOrderDetailProducts()!=null && getOrderDetail().getOrderDetailProducts().size()>1) ? getProduct().getBasePrice() / getOrderDetail().getSumPrice() : 1;
    }

    public String toString() {
        return new ToStringBuilder(this)
            .append("id", getId())
            .toString();
    }

}