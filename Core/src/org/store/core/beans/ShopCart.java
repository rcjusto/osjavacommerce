package org.store.core.beans;

import org.store.core.beans.utils.StoreBean;
import org.store.core.globals.BaseAction;
import org.store.core.globals.SomeUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.hibernate.annotations.Cascade;
import org.hibernate.event.EventSource;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


@Entity
@Table(name = "t_shopping_cart")
public class ShopCart extends BaseBean implements StoreBean {

    public static final String STATUS_FINISHED = "finished";
    public static final String STATUS_SAVED = "saved";
    public static final String STATUS_PENDING = "pending";
    public static final String STATUS_PREPARING = "preparing";
    public static final String STATUS_APPROVED = "approved";
    public static final String STATUS_CANCELLED = "cancelled";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Date createdDate;
    private Date validUntil;

    @Column(length = 50)
    private String status;

    @Column(length = 10)
    private String inventaryCode;

    @Column(length = 1024)
    private String promoCodes;

    private Long insurance;

    private Boolean useRewards;

    private Double overrideShipping;

    private Double interestPercent;

    @Lob
    private String quoteMessage;

    @ManyToOne
    private UserAddress billingAddress;

    @ManyToOne
    private UserAddress deliveryAddress;

    @ManyToOne
    private LocationStore pickInStore;

    @ManyToOne
    private ShippingMethod shippingMethod;

    @ManyToOne
    private User user;

    private Long adminUser;

    @OneToMany(cascade = {CascadeType.ALL}, fetch = FetchType.EAGER)
    @JoinColumn(name = "shopCart_id")
    @Cascade(value = org.hibernate.annotations.CascadeType.DELETE_ORPHAN)
    @OrderBy(value = "id asc")
    private List<ShopCartItem> items;

    public ShopCart() {
        createdDate = Calendar.getInstance().getTime();
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

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public Date getValidUntil() {
        return validUntil;
    }

    public void setValidUntil(Date validUntil) {
        this.validUntil = validUntil;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getQuoteMessage() {
        return quoteMessage;
    }

    public void setQuoteMessage(String quoteMessage) {
        this.quoteMessage = quoteMessage;
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

    public LocationStore getPickInStore() {
        return pickInStore;
    }

    public void setPickInStore(LocationStore pickInStore) {
        this.pickInStore = pickInStore;
    }

    public ShippingMethod getShippingMethod() {
        return shippingMethod;
    }

    public void setShippingMethod(ShippingMethod shippingMethod) {
        this.shippingMethod = shippingMethod;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public List<ShopCartItem> getItems() {
        if (items == null) items = new ArrayList<ShopCartItem>();
        return items;
    }

    public void setItems(List<ShopCartItem> items) {
        this.items = items;
    }

    public Long getAdminUser() {
        return adminUser;
    }

    public void setAdminUser(Long adminUser) {
        this.adminUser = adminUser;
    }

    public Long getInsurance() {
        return insurance;
    }

    public void setInsurance(Long insurance) {
        this.insurance = insurance;
    }

    public Boolean getUseRewards() {
        return useRewards!=null && useRewards;
    }

    public void setUseRewards(Boolean useRewards) {
        this.useRewards = useRewards;
    }

    public String getPromoCodes() {
        return promoCodes;
    }

    public void setPromoCodes(String promoCodes) {
        this.promoCodes = promoCodes;
    }

    public Double getOverrideShipping() {
        return overrideShipping;
    }

    public void setOverrideShipping(Double overrideShipping) {
        this.overrideShipping = overrideShipping;
    }

    public Double getInterestPercent() {
        return interestPercent;
    }

    public void setInterestPercent(Double interestPercent) {
        this.interestPercent = interestPercent;
    }

    public String[] getPromotionalCodes() {
        return StringUtils.isNotEmpty(promoCodes) ? promoCodes.split(",") : null;
    }

    public void setPromotionalCodes(String[] arr) {
        promoCodes = "";
        if (arr != null && arr.length > 0)
            for (String c : arr) {
                if (StringUtils.isNotEmpty(promoCodes)) promoCodes += ",";
                promoCodes += c;
            }
    }

    public void addPromotionalCode(String code) {
        if (StringUtils.isNotEmpty(code)) {
            if (StringUtils.isNotEmpty(promoCodes)) promoCodes += "," + code;
            else promoCodes = code;
        }
    }

    public void delPromotionalCode(String code) {
        String[] arr = getPromotionalCodes();
        if (arr != null) {
            int index = ArrayUtils.indexOf(arr, code);
            if (index>-1) setPromotionalCodes((String[]) ArrayUtils.remove(arr, index));
        }
    }

    public boolean hasPromotionalCode(String code) {
        String[] arr = getPromotionalCodes();
        return (arr != null) && (ArrayUtils.indexOf(arr, code)>-1);
    }

    public String toString() {
        return new ToStringBuilder(this)
                .append("id", getId())
                .toString();
    }

    public void addItem(ShopCartItem item) {
        item.setShopCart(this);
        getItems().add(item);
    }

    public ShopCartItem getItemById(Long id) {
        if (id != null)
            for (ShopCartItem it : getItems())
                if (id.equals(it.getId())) return it;
        return null;
    }

    public double getTotal() {
        double res = 0;
        for (ShopCartItem it : getItems()) res += it.getSubtotal();
        return res;
    }

    public double getTotalCost() {
        double res = 0;
        for (ShopCartItem it : getItems()) res += it.getTotalCost();
        return res;
    }

    public double getTotalOriginal() {
        double res = 0;
        for (ShopCartItem it : getItems()) res += it.getSubtotalOriginal();
        return res;
    }

    public double getTotalFees() {
        double res = 0;
        for (ShopCartItem it : getItems()) res += it.getTotalFees();
        return res;
    }

    public Integer getComponentCounts() {
        Integer res = 0;
        for (ShopCartItem it : getItems())
            if (it.getBeanComponent()!=null) res += it.getQuantity();
        return res;
    }

    public double getTotalComplements() {
        double res = 0;
        for (ShopCartItem it : getItems()) res += it.getSubtotalComplement();
        return res;
    }

    public boolean needQuote() {
        for (ShopCartItem it : getItems()) if (it.getPrice() == null) return true;
        return false;
    }


    public boolean hasProduct(Product productInCart) {
        if (productInCart != null && !getItems().isEmpty()) {
            for (ShopCartItem item : getItems()) {
                if (item.getBeanProd1() != null && productInCart.equals(item.getBeanProd1())) return true;
                if (item.getBeanProd2() != null && productInCart.equals(item.getBeanProd2())) return true;
            }
        }
        return false;
    }

    public boolean hasCategory(Category categoryInCart) {
        if (categoryInCart != null && !getItems().isEmpty()) {
            for (ShopCartItem item : getItems()) {
                if (item.getBeanProd1() != null && (item.getBeanProd1().belongToCategory(categoryInCart))) return true;
                if (item.getBeanProd2() != null && (item.getBeanProd2().belongToCategory(categoryInCart))) return true;
            }
        }
        return false;
    }

    public boolean hasManufacter(Manufacturer manufacturerInCart) {
        if (manufacturerInCart != null && !getItems().isEmpty()) {
            for (ShopCartItem item : getItems()) {
                if (item.getBeanProd1() != null && item.getBeanProd1().getManufacturer() != null && manufacturerInCart.equals(item.getBeanProd1().getManufacturer()))
                    return true;
                if (item.getBeanProd2() != null && item.getBeanProd2().getManufacturer() != null && manufacturerInCart.equals(item.getBeanProd2().getManufacturer()))
                    return true;
            }
        }
        return false;
    }

    public boolean hasLabel(ProductLabel labelInCart) {
        if (labelInCart != null && !getItems().isEmpty()) {
            for (ShopCartItem item : getItems()) {
                if (item.getBeanProd1() != null && item.getBeanProd1().hasLabel(labelInCart.getCode()))
                    return true;
                if (item.getBeanProd2() != null && item.getBeanProd2().hasLabel(labelInCart.getCode()))
                    return true;
            }
        }
        return false;
    }

    public String getShippingType() {
        if (pickInStore != null) return "pickinstore";
        if (shippingMethod != null) return "shipping";
        else return "billing";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ShopCart shopCart = (ShopCart) o;
        return !(id != null ? !id.equals(shopCart.id) : shopCart.id != null);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    public int getNumProduct(Long id) {
        int res = 0;
        if (id != null && items!=null)
            for (ShopCartItem item : items)
                if (id.equals(item.getProduct1())) res += item.getQuantity();
        return res;
    }

    public void initializeItems(BaseAction action) {
        if (items!=null)
            for(ShopCartItem item : items) {
                item.setShopCart(this);
                item.initializeItem(action);
            }
    }

    public boolean canCheckoutToday() {
        Date today = SomeUtils.dateIni(Calendar.getInstance().getTime());
        return validUntil==null || !validUntil.before(today);
    }

    @Override
    public boolean handlePreDelete(EventSource session) {
        if (items!=null) for(ShopCartItem item : items) session.delete(item);
        return super.handlePreDelete(session);
    }
}