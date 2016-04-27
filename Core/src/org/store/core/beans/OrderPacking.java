package org.store.core.beans;

import org.apache.commons.lang.StringUtils;
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
import javax.persistence.Table;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;


@Entity
@Table(name="t_order_packing")
public class OrderPacking extends BaseBean {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // tracking number del paquete
    @Column(length = 250)
    private String trackingNumber;
    @Lob
    private String customTracking;

    // Fecha de envio del paquete
    private Date deliveryDate;

    // Costo de envio del paquete
    private Double deliveryCost;

    // Moneda
    @ManyToOne
    private Currency deliveryCurrency;

    // Dimensiones del paquete
    private Double dimentionWidth;
    private Double dimentionHeight;
    private Double dimentionLength;

    // Peso del paquete
    private Double weight;

    // Si los datos del shipping del paquete se entraron de forma manual
    private Boolean manualShipping;

    // productos incluidos en la caja
    @OneToMany(mappedBy = "packing", cascade = CascadeType.REMOVE)
    private Set<OrderPackingProduct> packingProductList;

    // Orden a la q pertenece el envio
    @ManyToOne
    private Order order;

    // Metodo de envio utilizado
    @ManyToOne
    private ShippingMethod shippingMethod;


    public OrderPacking() {
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

    public String getCustomTracking() {
        return customTracking;
    }

    public void setCustomTracking(String customTracking) {
        this.customTracking = customTracking;
    }

    public Date getDeliveryDate() {
        return deliveryDate;
    }

    public void setDeliveryDate(Date deliveryDate) {
        this.deliveryDate = deliveryDate;
    }

    public Double getDeliveryCost() {
        return deliveryCost;
    }

    public void setDeliveryCost(Double deliveryCost) {
        this.deliveryCost = deliveryCost;
    }

    public Currency getDeliveryCurrency() {
        return deliveryCurrency;
    }

    public void setDeliveryCurrency(Currency deliveryCurrency) {
        this.deliveryCurrency = deliveryCurrency;
    }

    public Double getDimentionWidth() {
        return dimentionWidth;
    }

    public void setDimentionWidth(Double dimentionWidth) {
        this.dimentionWidth = dimentionWidth;
    }

    public Double getDimentionHeight() {
        return dimentionHeight;
    }

    public void setDimentionHeight(Double dimentionHeight) {
        this.dimentionHeight = dimentionHeight;
    }

    public Double getDimentionLength() {
        return dimentionLength;
    }

    public void setDimentionLength(Double dimentionLength) {
        this.dimentionLength = dimentionLength;
    }

    public Double getWeight() {
        return weight;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }

    public Boolean getManualShipping() {
        return manualShipping;
    }

    public void setManualShipping(Boolean manualShipping) {
        this.manualShipping = manualShipping;
    }

    public Set<OrderPackingProduct> getPackingProductList() {
        return packingProductList;
    }

    public void setPackingProductList(Set<OrderPackingProduct> packingProductList) {
        this.packingProductList = packingProductList;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public ShippingMethod getShippingMethod() {
        return shippingMethod;
    }

    public void setShippingMethod(ShippingMethod shippingMethod) {
        this.shippingMethod = shippingMethod;
    }

    public Map<Product, Integer> getProductMap() {
        Map<Product, Integer> map = new HashMap<Product, Integer>();
        if (packingProductList!=null && packingProductList.size()>0) {
            for(OrderPackingProduct opp : packingProductList) {
                if (map.containsKey(opp.getOrderDetailProduct().getProduct())) {
                    Integer cant = map.get(opp.getOrderDetailProduct().getProduct());
                    map.put(opp.getOrderDetailProduct().getProduct(), cant+1);
                } else {
                    map.put(opp.getOrderDetailProduct().getProduct(),1);
                }
            }
        }
        return map;
    }

    public Double getMonetaryValue() {
        double res = 0.0d;
        Map<Product, Integer> map = getProductMap();
        if (map!=null && !map.isEmpty()) {
            for(Product p : map.keySet()) {
                res += p.getBasePrice() * map.get(p);
            }
        }
        return res;
    }

    public Boolean getPending() {
        return (deliveryDate==null || deliveryCost==null || StringUtils.isEmpty(trackingNumber));
    }

    public String toString() {
        return new ToStringBuilder(this)
            .append("id", getId())
            .toString();
    }

}