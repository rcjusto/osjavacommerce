package org.store.core.beans;

import org.store.core.beans.utils.StoreBean;
import org.store.core.front.UserSession;
import org.store.core.globals.SomeUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.struts2.json.JSONException;
import org.apache.struts2.json.JSONUtil;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


@Entity
@Table(name = "t_promotional_code")
public class PromotionalCode extends BaseBean implements StoreBean {

    public static final String TYPE_PROMOTION = "P";
    public static final String TYPE_COUPON = "C";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idPromotion;

    // Codigo de la promocion
    @Column(length = 50)
    private String code;

    @Column(length = 20)
    private String promotionType;

    // Tienda en la q esta configurado el producto
    @Column(length = 10)
    private String inventaryCode;


    // Nombre de la promocion
    @Lob
    private String name;
    @Transient
    private Map<String,String> mapValues;

    // Puede ser usado solo por
    @ManyToOne
    private User user;

    // Puede ser usado solo por
    @ManyToOne
    private UserLevel userLevel;

    // Filters to apply code
    private Boolean active;

    // Filters to apply code
    private Boolean onlyOnce;

    private Date validFrom;
    private Date validTo;

    // Filters to apply code depending on shopping cart
    private Double minTotalShopcart;
    private Double maxTotalShopcart;
    @ManyToOne
    private Product productInCart;
    @ManyToOne
    private Category categoryInCart;
    @ManyToOne
    private Manufacturer manufacturerInCart;
    @ManyToOne
    private ProductLabel labelInCart;

    @ManyToMany
    @JoinTable(name = "t_promotional_code_apply_categories",
            joinColumns = @JoinColumn(name = "t_promotional_code_idPromotion"),
            inverseJoinColumns = @JoinColumn(name = "applyToCategories_idCategory"))
    private Set<Category> applyToCategories;

    @ManyToMany
    @JoinTable(name = "t_promotional_code_not_apply_categories",
            joinColumns = @JoinColumn(name = "t_promotional_code_idPromotion"),
            inverseJoinColumns = @JoinColumn(name = "notApplyToCategories_idCategory"))
    private Set<Category> notApplyToCategories;

    @ManyToMany
    @JoinTable(name = "t_promotional_code_apply_labels",
            joinColumns = @JoinColumn(name = "t_promotional_code_idPromotion"),
            inverseJoinColumns = @JoinColumn(name = "applyToLabels_id"))
    private Set<ProductLabel> applyToLabels;

    @ManyToMany
    @JoinTable(name = "t_promotional_code_not_apply_labels",
            joinColumns = @JoinColumn(name = "t_promotional_code_idPromotion"),
            inverseJoinColumns = @JoinColumn(name = "notApplyToLabels_id"))
    private Set<ProductLabel> notApplyToLabels;

    // Promotions
    private Boolean freeShipping;
    private Double discount;
    private Double discountPercent;
    @ManyToOne
    private Product freeProduct;


    public Long getIdPromotion() {
        return idPromotion;
    }

    public void setIdPromotion(Long idPromotion) {
        this.idPromotion = idPromotion;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getInventaryCode() {
        return inventaryCode;
    }

    public void setInventaryCode(String inventaryCode) {
        this.inventaryCode = inventaryCode;
    }

    public String getName() {
        return name;
    }

    public String getPromotionType() {
        return (promotionType!=null && TYPE_COUPON.equalsIgnoreCase(promotionType)) ? TYPE_COUPON : TYPE_PROMOTION;
    }

    public void setPromotionType(String promotionType) {
        this.promotionType = promotionType;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getValidFrom() {
        return validFrom;
    }

    public void setValidFrom(Date validFrom) {
        this.validFrom = validFrom;
    }

    public Date getValidTo() {
        return validTo;
    }

    public void setValidTo(Date validTo) {
        this.validTo = validTo;
    }

    public Double getMinTotalShopcart() {
        return minTotalShopcart;
    }

    public void setMinTotalShopcart(Double minTotalShopcart) {
        this.minTotalShopcart = minTotalShopcart;
    }

    public Double getMaxTotalShopcart() {
        return maxTotalShopcart;
    }

    public void setMaxTotalShopcart(Double maxTotalShopcart) {
        this.maxTotalShopcart = maxTotalShopcart;
    }

    public Boolean getActive() {
        return active != null && active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Product getProductInCart() {
        return productInCart;
    }

    public void setProductInCart(Product productInCart) {
        this.productInCart = productInCart;
    }

    public Category getCategoryInCart() {
        return categoryInCart;
    }

    public void setCategoryInCart(Category categoryInCart) {
        this.categoryInCart = categoryInCart;
    }

    public Manufacturer getManufacturerInCart() {
        return manufacturerInCart;
    }

    public void setManufacturerInCart(Manufacturer manufacturerInCart) {
        this.manufacturerInCart = manufacturerInCart;
    }

    public ProductLabel getLabelInCart() {
        return labelInCart;
    }

    public void setLabelInCart(ProductLabel labelInCart) {
        this.labelInCart = labelInCart;
    }

    public Boolean getFreeShipping() {
        return freeShipping != null && freeShipping;
    }

    public void setFreeShipping(Boolean freeShipping) {
        this.freeShipping = freeShipping;
    }

    public Double getDiscount() {
        return discount;
    }

    public void setDiscount(Double discount) {
        this.discount = discount;
    }

    public Double getDiscountPercent() {
        return discountPercent;
    }

    public void setDiscountPercent(Double discountPercent) {
        this.discountPercent = discountPercent;
    }

    public Product getFreeProduct() {
        return freeProduct;
    }

    public void setFreeProduct(Product freeProduct) {
        this.freeProduct = freeProduct;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public UserLevel getUserLevel() {
        return userLevel;
    }

    public void setUserLevel(UserLevel userLevel) {
        this.userLevel = userLevel;
    }

    public Boolean getOnlyOnce() {
        return onlyOnce!=null && onlyOnce;
    }

    public void setOnlyOnce(Boolean onlyOnce) {
        this.onlyOnce = onlyOnce;
    }

    public Set<Category> getApplyToCategories() {
        return applyToCategories;
    }

    public void setApplyToCategories(Set<Category> applyToCategories) {
        this.applyToCategories = applyToCategories;
    }

    public Set<Category> getNotApplyToCategories() {
        return notApplyToCategories;
    }

    public void setNotApplyToCategories(Set<Category> notApplyToCategories) {
        this.notApplyToCategories = notApplyToCategories;
    }

    public Set<ProductLabel> getApplyToLabels() {
        return applyToLabels;
    }

    public void setApplyToLabels(Set<ProductLabel> applyToLabels) {
        this.applyToLabels = applyToLabels;
    }

    public Set<ProductLabel> getNotApplyToLabels() {
        return notApplyToLabels;
    }

    public void setNotApplyToLabels(Set<ProductLabel> notApplyToLabels) {
        this.notApplyToLabels = notApplyToLabels;
    }

    public boolean isValid() {
        return getActive() && !(validFrom != null && validFrom.after(SomeUtils.dateEnd(SomeUtils.today()))) && !(validTo != null && validTo.before(SomeUtils.dateIni(SomeUtils.today())));
    }

    public boolean isValid(UserSession shopCart) {
        if (!isValid()) return false;
        if (shopCart != null) {
            double shopCartTotal = shopCart.getTotal();
            if (minTotalShopcart!=null && shopCartTotal<minTotalShopcart) return false;
            if (maxTotalShopcart!=null && shopCartTotal>maxTotalShopcart) return false;
            if (productInCart!=null && !shopCart.hasProduct(productInCart)) return false;
            if (categoryInCart!=null && !shopCart.hasCategory(categoryInCart)) return false;
            if (manufacturerInCart!=null && !shopCart.hasManufacter(manufacturerInCart)) return false;
            if (labelInCart!=null && !shopCart.hasLabel(labelInCart)) return false;
        }
        return true;
    }

    public boolean isValid(ShopCart shopCart) {
        if (!isValid()) return false;
        if (shopCart != null) {
            double shopCartTotal = shopCart.getTotal();
            if (minTotalShopcart!=null && shopCartTotal<minTotalShopcart) return false;
            if (maxTotalShopcart!=null && shopCartTotal>maxTotalShopcart) return false;
            if (productInCart!=null && !shopCart.hasProduct(productInCart)) return false;
            if (categoryInCart!=null && !shopCart.hasCategory(categoryInCart)) return false;
            if (manufacturerInCart!=null && !shopCart.hasManufacter(manufacturerInCart)) return false;
            if (labelInCart!=null && !shopCart.hasLabel(labelInCart)) return false;
        }
        return true;
    }

    public boolean isValid(User aUser) {
        boolean userRequired = false;
        if (user!=null) {
            if (!user.equals(aUser)) return false;
            userRequired = true;
        }
        if (userLevel!=null) {
            if (aUser==null || !userLevel.equals(aUser.getLevel())) return false;
            userRequired = true;
        }
        if (userRequired && getOnlyOnce()) {
            List<Order> orders = getAction().getDao().getOrdersByUser(null, aUser);
            if (orders!=null) {
                for(Order order : orders) {
                    if (!OrderStatus.STATUS_REJECTED.equalsIgnoreCase(order.getStatus().getStatusType()) && order.hasPromotionCode(code))
                        return false;
                }
            }
        }
        return true;
    }

    public String getName(String lang) {
        if (mapValues == null || mapValues.isEmpty()) deserialize();
        return (mapValues != null && mapValues.containsKey(lang)) ? mapValues.get(lang) : null;
    }
    public void setName(String lang, String value) {
        if (mapValues==null) mapValues = new HashMap<String,String>();
        mapValues.put(lang, value);
        serialize();
    }

    public void serialize() {
        try {
            name = (mapValues!=null && mapValues.size()>0) ? JSONUtil.serialize(mapValues) : null;
        } catch (JSONException e) {
            log.error(e.getMessage(), e);
        }
    }

    public void deserialize() {
        try {
            mapValues = (!isEmpty(name)) ? (Map) JSONUtil.deserialize(name) : null;
        } catch (JSONException e) {
            log.error(e.getMessage(), e);
        }
    }

    public String toString() {
        return new ToStringBuilder(this)
                .append("id", getIdPromotion())
                .toString();
    }

    public boolean canApplyTo(Product product) {
        // Validar q no este en las categorias prohibidas
        if (notApplyToCategories!=null && !notApplyToCategories.isEmpty()) {
            for(Category c : notApplyToCategories) if (product.belongToCategory(c)) return false;
        }
        // Validar q no este en las labels prohibidos
        if (notApplyToLabels!=null && !notApplyToLabels.isEmpty()) {
            for(ProductLabel l : notApplyToLabels) if (product.hasLabel(l.getCode())) return false;
        }
        // Validar q este en una de las categorias obligatorias
        if (applyToCategories!=null && !applyToCategories.isEmpty()) {
            boolean res = false;
            for(Category c : applyToCategories) if (product.belongToCategory(c)) res = true;
            if (!res) return false;
        }
        // Validar q tenga uno de los labels obligatorios
        if (applyToLabels!=null && !applyToLabels.isEmpty()) {
            boolean res = false;
            for(ProductLabel l : applyToLabels) if (product.hasLabel(l.getCode())) res = true;
            if (!res) return false;
        }
        return true;
    }
}