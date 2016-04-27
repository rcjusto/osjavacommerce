package org.store.core.beans;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.struts2.json.JSONException;
import org.apache.struts2.json.JSONUtil;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import javax.persistence.*;
import java.util.*;


@Entity
@Table(name="t_order")
public class Order extends BaseBean {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idOrder;

    // cuando la orden se le transfiere a un proveedor, este es el id de la orden en el proveedor
    @Column(length = 250)
    private String supplierId;

    // codigo de la transaccion de pago, devuelto por la pasarela
    @Column(length = 50)
    private String codeMerchant;

    // informacion adicional devuelta por la pasarela (json encoded)
    @Lob
    private String merchantData;

    // nombre del transportista asignado
    @Column(length = 50)
    private String codeCarrier;

    // fecha de creacion de a orden
    private Date createdDate;

    // ip del comprador
    @Column(length = 50)
    private String remoteIp;

    @ManyToOne
    private OrderStatus status;

    private Double totalProducts;
    private Double totalFees;
    private Double totalComponents;
    private Double totalTax;
    private Double totalShipping;
    private Double totalCost;
    private Double totalInsurance;
    private Double totalDiscountPromotion;
    private Double totalToTax;
    private Double totalRewards;
    private Double totalInterest;

    private Double interestPercent;

    private Double handlingCost;

    @Column
    private Double total;

    // vendedor o revendedor que crea la orden
    private Long idAdminUser;

    // nombre del metodo de pago seleccionado
    @Column(length = 50)
    private String paymentMethod;

    // codigo de referencia a la orden, definido por el cliente
    @Column(length = 250)
    private String customReference;

    // mensaje del cliente
    @Lob
    private String customMessage;

    @ManyToOne
    private Currency currency;

    @Column(length = 250)
    private String affiliateCode;

    private Date exportedDate;

    @Lob
    private String promotionsData;
    @Transient
    private Map<String, Map<String,Object>> promotionsMap;

    @Lob
    private String taxesData;
    @Lob
    private String taxesListData;
    @Transient
    private Map<String,Double> taxesMap;
    @Transient
    private List<Map<String,Object>> taxesList;

    // datos para el invoice
    private Long invoiceConsecutive;
    private String invoiceNo;
    private Date invoiceDate;
    private String purchaseOrder;
    private String paymentCard;  

    @ManyToOne
    @NotFound(action = NotFoundAction.IGNORE)
    private UserAddress billingAddress;
    @ManyToOne
    @NotFound(action = NotFoundAction.IGNORE)
    private UserAddress deliveryAddress;
    @ManyToOne
    @NotFound(action = NotFoundAction.IGNORE)
    private LocationStore pickInStore;
    @ManyToOne
    @NotFound(action = NotFoundAction.IGNORE)
    private ShippingMethod shippingMethod;
    @ManyToOne
    private User user;
    @ManyToOne
    private User affiliate;
    @OneToMany(mappedBy = "order")
    @OrderBy(value = "historyDate asc")
    private List<OrderHistory> orderHistory;
    @OneToMany(mappedBy = "order")
    @OrderBy(value = "idDetail asc")
    private List<OrderDetail> orderDetails;
    @OneToMany(mappedBy = "order", fetch = FetchType.LAZY)
    private Set<OrderPacking> packages;
    @OneToMany(mappedBy = "order", fetch = FetchType.LAZY)
    private Set<OrderPayment> payments;

    public Order() {
        createdDate = Calendar.getInstance().getTime();
    }

    public Long getIdOrder() {
        return idOrder;
    }

    public void setIdOrder(Long idOrder) {
        this.idOrder = idOrder;
    }

    public String getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(String supplierId) {
        this.supplierId = supplierId;
    }

    public String getCodeMerchant() {
        return codeMerchant;
    }

    public void setCodeMerchant(String codeMerchant) {
        this.codeMerchant = codeMerchant;
    }

    public String getCodeCarrier() {
        return codeCarrier;
    }

    public void setCodeCarrier(String codeCarrier) {
        this.codeCarrier = codeCarrier;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public String getRemoteIp() {
        return remoteIp;
    }

    public void setRemoteIp(String remoteIp) {
        this.remoteIp = remoteIp;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public Double getTotalProducts() {
        return totalProducts;
    }

    public void setTotalProducts(Double totalProducts) {
        this.totalProducts = totalProducts;
    }

    public Double getTotalToTax() {
        if (getTaxesList()!=null && !getTaxesList().isEmpty()) {
            Map<String,Object> map = getTaxesList().get(0);
            return (Double) (map.containsKey("totax") ? map.get("totax") : 0);
        } else {
            if (totalToTax!=null) return totalToTax;
            else {
                double toTax = 0;
                if (totalProducts!=null) toTax += totalProducts;
                if (totalFees != null) toTax += totalFees;
                if (totalInterest!=null) toTax += totalInterest;
                if (totalDiscountPromotion!=null) toTax += totalDiscountPromotion;
                if (totalShipping!=null) toTax += totalShipping;
                if (totalInsurance!=null) toTax += totalInsurance;
                return toTax;
            }
        }
    }

    public void setTotalToTax(Double totalToTax) {
        this.totalToTax = totalToTax;
    }

    public Double getTotalTax() {
        return totalTax;
    }

    public void setTotalTax(Double totalTax) {
        this.totalTax = totalTax;
    }

    public Double getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(Double totalCost) {
        this.totalCost = totalCost;
    }

    public Double getTotalDiscountPromotion() {
        return totalDiscountPromotion;
    }

    public void setTotalDiscountPromotion(Double totalDiscountPromotion) {
        this.totalDiscountPromotion = totalDiscountPromotion;
    }

    public Double getTotalRewards() {
        return totalRewards;
    }

    public void setTotalRewards(Double totalRewards) {
        this.totalRewards = totalRewards;
    }

    public Double getTotalComponents() {
        return totalComponents;
    }

    public void setTotalComponents(Double totalComponents) {
        this.totalComponents = totalComponents;
    }

    public Double getTotalFees() {
        return totalFees;
    }

    public void setTotalFees(Double totalFees) {
        this.totalFees = totalFees;
    }

    public Long getIdAdminUser() {
        return idAdminUser;
    }

    public void setIdAdminUser(Long idAdminUser) {
        this.idAdminUser = idAdminUser;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getMerchantData() {
        return merchantData;
    }

    public void setMerchantData(String merchantData) {
        this.merchantData = merchantData;
    }

    public Date getExportedDate() {
        return exportedDate;
    }

    public void setExportedDate(Date exportedDate) {
        this.exportedDate = exportedDate;
    }

    public Double getTotalInsurance() {
        return totalInsurance;
    }

    public void setTotalInsurance(Double totalInsurance) {
        this.totalInsurance = totalInsurance;
    }

    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    public String getAffiliateCode() {
        return affiliateCode;
    }

    public void setAffiliateCode(String affiliateCode) {
        this.affiliateCode = affiliateCode;
    }

    public String getInvoiceNo() {
        return invoiceNo;
    }

    public void setInvoiceNo(String invoiceNo) {
        this.invoiceNo = invoiceNo;
    }

    public Long getInvoiceConsecutive() {
        return invoiceConsecutive;
    }

    public void setInvoiceConsecutive(Long invoiceConsecutive) {
        this.invoiceConsecutive = invoiceConsecutive;
    }

    public boolean hasInvoiceNo() {
        return StringUtils.isNotEmpty(invoiceNo) || invoiceConsecutive!=null;
    }

    public Date getInvoiceDate() {
        return invoiceDate;
    }

    public void setInvoiceDate(Date invoiceDate) {
        this.invoiceDate = invoiceDate;
    }

    public String getPurchaseOrder() {
        return purchaseOrder;
    }

    public void setPurchaseOrder(String purchaseOrder) {
        this.purchaseOrder = purchaseOrder;
    }

    public String getPaymentCard() {
        return paymentCard;
    }

    public void setPaymentCard(String paymentCard) {
        this.paymentCard = paymentCard;
    }

    public String getCustomReference() {
        return customReference;
    }

    public void setCustomReference(String customReference) {
        this.customReference = customReference;
    }

    public String getCustomMessage() {
        return customMessage;
    }

    public void setCustomMessage(String customMessage) {
        this.customMessage = customMessage;
    }

    public UserAddress getBillingAddress() {
        return billingAddress;
    }

    public void setBillingAddress(UserAddress billingAddress) {
        this.billingAddress = billingAddress;
    }

    public UserAddress getDeliveryAddress() {
        return deliveryAddress;
    }

    public void setDeliveryAddress(UserAddress deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }

    public ShippingMethod getShippingMethod() {
        return shippingMethod;
    }

    public void setShippingMethod(ShippingMethod shippingMethod) {
        this.shippingMethod = shippingMethod;
    }

    public LocationStore getPickInStore() {
        return pickInStore;
    }

    public void setPickInStore(LocationStore pickInStore) {
        this.pickInStore = pickInStore;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public User getAffiliate() {
        return affiliate;
    }

    public void setAffiliate(User affiliate) {
        this.affiliate = affiliate;
    }

    public List<OrderHistory> getOrderHistory() {
        return orderHistory;
    }

    public void setOrderHistory(List<OrderHistory> orderHistory) {
        this.orderHistory = orderHistory;
    }

    public List<OrderDetail> getOrderDetails() {
        return orderDetails;
    }

    public void setOrderDetails(List<OrderDetail> orderDetails) {
        this.orderDetails = orderDetails;
    }

    public Set<OrderPacking> getPackages() {
        return packages;
    }

    public void setPackages(Set<OrderPacking> packages) {
        this.packages = packages;
    }

    public Set<OrderPayment> getPayments() {
        return payments;
    }

    public void setPayments(Set<OrderPayment> payments) {
        this.payments = payments;
    }

    public Double getTotalShipping() {
        return totalShipping;
    }

    public void setTotalShipping(Double totalShipping) {
        this.totalShipping = totalShipping;
    }

    public Double getTotalInterest() {
        return totalInterest;
    }

    public void setTotalInterest(Double totalInterest) {
        this.totalInterest = totalInterest;
    }

    public Double getInterestPercent() {
        return interestPercent;
    }

    public void setInterestPercent(Double interestPercent) {
        this.interestPercent = interestPercent;
    }

    public Double getHandlingCost() {
        return handlingCost;
    }

    public void setHandlingCost(Double handlingCost) {
        this.handlingCost = handlingCost;
    }

    public Double getTotal() {
        total = 0.0d;
        if (totalProducts != null) total += totalProducts;
        if (totalFees != null) total += totalFees;
        if (totalShipping != null) total += totalShipping;
        if (totalTax != null) total += totalTax;
        if (totalInsurance!=null) total += totalInsurance;
        if (totalDiscountPromotion != null) total += totalDiscountPromotion;
        if (totalRewards != null) total -= totalRewards;
        if (totalInterest != null) total += totalInterest;
        return total;
    }

    public void setTotal(Double t) {
        total = 0.0d;
        if (totalProducts != null) total += totalProducts;
        if (totalFees != null) total += totalFees;
        if (totalShipping != null) total += totalShipping;
        if (totalTax != null) total += totalTax;
        if (totalInsurance!=null) total += totalInsurance;
        if (totalDiscountPromotion != null) total += totalDiscountPromotion;
        if (totalRewards != null) total -= totalRewards;
        if (totalInterest != null) total += totalInterest;
    }

    public String getPromotionsData() {
        return promotionsData;
    }

    public void setPromotionsData(String promotionsData) {
        this.promotionsData = promotionsData;
    }

    public String getTaxesData() {
        return taxesData;
    }

    public void setTaxesData(String taxesData) {
        this.taxesData = taxesData;
    }

    public String getTaxesListData() {
        return taxesListData;
    }

    public void setTaxesListData(String taxesListData) {
        this.taxesListData = taxesListData;
    }

    public List<Map<String,Object>> getTaxes() {
        if (getTaxesList()!=null && !getTaxesList().isEmpty()) return getTaxesList();
        else {
            if (taxesMap == null || taxesMap.size() < 1) {
                try {
                    taxesMap =  (!isEmpty(taxesData)) ? (HashMap) JSONUtil.deserialize(taxesData) : null;
                } catch (JSONException e) {
                    log.error(e.getMessage(), e); 
                }
            }
            if (taxesMap!=null && !taxesMap.isEmpty()) {
                List<Map<String,Object>> l = new ArrayList<Map<String,Object>>();
                for(Map.Entry<String,Double> e : taxesMap.entrySet()) {
                    Map<String,Object> m = new HashMap<String,Object>();
                    m.put("name", e.getKey());
                    m.put("totax", getTotalToTax());
                    m.put("value", e.getValue());
                    l.add(m);
                }
                return l;
            }
        }
        return null;
    }

    public void addTax(Map<String,Object> m) {
        if (taxesList==null) taxesList = new ArrayList<Map<String,Object>>();
        taxesList.add(m);
        // Serialize data
        try {
            taxesListData = (taxesList!=null && taxesList.size()>0) ? JSONUtil.serialize(taxesList) : null;
        } catch (JSONException e) {
            log.error(e.getMessage(), e); 
        }
    }

    public List<Map<String,Object>> getTaxesList() {
        if (taxesList == null || taxesList.size() < 1) {
            try {
                taxesList =  (!isEmpty(taxesListData)) ? (List<Map<String,Object>>) JSONUtil.deserialize(taxesListData) : null;
            } catch (JSONException e) {
                log.error(e.getMessage(), e); 
            }
        }
        return taxesList;
    }


    public void addPromotion(Map<String,Object> promo) {
        if (promotionsMap==null) promotionsMap = new HashMap<String,Map<String,Object>>();
        promotionsMap.put((String) promo.get("code"), promo);
        // Serialize data
        try {
            promotionsData = (promotionsMap!=null && promotionsMap.size()>0) ? JSONUtil.serialize(promotionsMap) : null;
        } catch (JSONException e) {
            log.error(e.getMessage(), e); 
        }

    }

    public Map<String,Map<String,Object>> getPromotions() {
        if (promotionsMap == null || promotionsMap.size() < 1) {
            try {
                promotionsMap =  (!isEmpty(promotionsData)) ? (HashMap) JSONUtil.deserialize(promotionsData) : null;
            } catch (JSONException e) {
                log.error(e.getMessage(), e); 
            }
        }
        return promotionsMap;
    }

    public List<Map<String,Object>> getPromotions(String type) {
        if (promotionsMap == null || promotionsMap.size() < 1) {
            try {
                promotionsMap =  (!isEmpty(promotionsData)) ? (HashMap) JSONUtil.deserialize(promotionsData) : null;
            } catch (JSONException e) {
                log.error(e.getMessage(), e); 
            }
        }
        List<Map<String,Object>> res = new ArrayList<Map<String,Object>>();
        if (promotionsMap!=null) {
            for(Map<String,Object> p :promotionsMap.values()) {
                if (p.containsKey("type") && type.equalsIgnoreCase((String) p.get("type")))
                    res.add(p);
            }
        }
        return res;
    }

    public boolean hasPromotionCode(String code) {
        Map<String,Map<String,Object>> map = getPromotions();
        return (map!=null && map.keySet().contains(code));
    }

    public Set<OrderPacking> getSentPackages() {
        if (packages!=null && !packages.isEmpty()) {
            Set<OrderPacking> result = new HashSet<OrderPacking>();
            for(OrderPacking p : packages) {
                if (StringUtils.isNotEmpty(p.getTrackingNumber()) && p.getDeliveryDate()!=null) result.add(p);
            }
            return result.isEmpty() ? null : result;
        }
        return null;
    }

    public Date getSentDate() {
        Date res = null;
        if (packages!=null && !packages.isEmpty()) {
            for(OrderPacking p : packages) {
                if (p.getDeliveryDate()!=null) {
                    if (res==null || res.after(p.getDeliveryDate())) res = p.getDeliveryDate();
                }
            }
        }
        return res;
    }

    public double getCostProduct() {
        double result = 0d;
        if (orderDetails!=null && !orderDetails.isEmpty()) {
            for(OrderDetail od : orderDetails) if (od.getOrderDetailProducts()!=null) {
                for(OrderDetailProduct odp : od.getOrderDetailProducts()) {
                    if (odp.getCostPrice()!=null) result += odp.getCostPrice();
                }
            }
        }
        return result;
    }

    public double getTotalPartialPaid() {
        double result = 0;
        if (payments!=null && !payments.isEmpty()) {
            for(OrderPayment p : payments) {
                if (OrderPayment.STATUS_COMPLETE.equalsIgnoreCase(p.getStatus())) result += p.getAmount();
            }
        } else if (OrderStatus.STATUS_APPROVED.equals(getStatus().getStatusType())) {
            result = getTotal();
        }
        return result;
    }

    public double getTotalPartialPending() {
        return getTotal() - getTotalPartialPaid();
    }

    public double getCostShipping() {
        double result = 0d;
        if (packages!=null && !packages.isEmpty()) {
            for(OrderPacking op : packages)
                if (op.getDeliveryCost()!=null) result += op.getDeliveryCost();
        }
        return result;
    }
    public String toString() {
        return new ToStringBuilder(this)
            .append("id", getIdOrder())
            .toString();
    }


    

}