package org.store.core.beans;

import org.store.core.globals.BaseAction;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Entity
@Table(name="t_shopping_cart_item")
public class ShopCartItem extends BaseBean {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long product1;
    private Long variation1;
    @Column(length = 255)
    private String code1;
    @Column(length = 1024)
    private String name1;
    private Long product2;
    private Long variation2;
    @Column(length = 255)
    private String code2;
    @Column(length = 1024)
    private String name2;
    private Integer quantity;
    @Column(length = 255)
    private String selDate;
    @Column(length = 255)
    private String selTime;
    private Double priceOriginal;
    private Double price;
    private Long complement;
    private Double complementPrice;
    private String message;

    // Estas se calculan en el momento
    @Transient
    private Double stock;
    @Transient
    private Product beanProd1;
    @Transient
    private Product beanProd2;
    @Transient
    private Product beanComponent;
    @Transient
    private List<CategoryFee> fees;

    @ManyToOne
    @JoinColumn(name="shopCart_id", insertable = false, updatable = false)
    private ShopCart shopCart;

    public ShopCartItem() {
    }

    public ShopCartItem(ShopCart cart) {
        this.shopCart = cart;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProduct1() {
        return product1;
    }

    public void setProduct1(Long product1) {
        this.product1 = product1;
    }

    public Long getVariation1() {
        return variation1;
    }

    public void setVariation1(Long variation1) {
        this.variation1 = variation1;
    }

    public String getCode1() {
        return code1;
    }

    public void setCode1(String code1) {
        this.code1 = code1;
    }

    public String getName1() {
        return name1;
    }

    public void setName1(String name1) {
        this.name1 = name1;
    }

    public Long getProduct2() {
        return product2;
    }

    public void setProduct2(Long product2) {
        this.product2 = product2;
    }

    public Long getVariation2() {
        return variation2;
    }

    public void setVariation2(Long variation2) {
        this.variation2 = variation2;
    }

    public String getCode2() {
        return code2;
    }

    public void setCode2(String code2) {
        this.code2 = code2;
    }

    public String getName2() {
        return name2;
    }

    public void setName2(String name2) {
        this.name2 = name2;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public String getSelDate() {
        return selDate;
    }

    public void setSelDate(String selDate) {
        this.selDate = selDate;
    }

    public String getSelTime() {
        return selTime;
    }

    public void setSelTime(String selTime) {
        this.selTime = selTime;
    }

    public Double getPrice() {
        return (price!=null) ? BigDecimal.valueOf(price).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue() : null;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Double getPriceOriginal() {
        return (priceOriginal!=null) ? priceOriginal : price;
    }

    public void setPriceOriginal(Double priceOriginal) {
        this.priceOriginal = priceOriginal;
    }

    public Double getStock() {
        return stock!=null ? stock : 0d;
    }

    public void setStock(Double stock) {
        this.stock = stock;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Long getComplement() {
        return complement;
    }

    public void setComplement(Long complement) {
        this.complement = complement;
    }

    public Double getComplementPrice() {
        return complementPrice;
    }

    public void setComplementPrice(Double complementPrice) {
        this.complementPrice = complementPrice;
    }

    public Product getBeanComponent() {
        return beanComponent;
    }

    public void setBeanComponent(Product beanComponent) {
        this.beanComponent = beanComponent;
    }

    public Product getBeanProd1() {
        return beanProd1;
    }

    public void setBeanProd1(Product beanProd1) {
        this.beanProd1 = beanProd1;
    }

    public Product getBeanProd2() {
        return beanProd2;
    }

    public void setBeanProd2(Product beanProd2) {
        this.beanProd2 = beanProd2;
    }

    public List<CategoryFee> getFees() {
        if (fees==null) fees = new ArrayList<CategoryFee>();
        return fees;
    }

    public void setFees(List<CategoryFee> fees) {
        this.fees = fees;
    }

    public ShopCart getShopCart() {
        return shopCart;
    }

    public void setShopCart(ShopCart shopCart) {
        this.shopCart = shopCart;
    }

    public String toString() {
        return new ToStringBuilder(this)
            .append("id", getId())
            .toString();
    }


    public void addQuantity(Integer q) {
        if (q!=null)
            quantity = (quantity==null) ? q : quantity + q;
    }

    public void addFee(CategoryFee f) {
        if (this.fees==null) this.fees = new ArrayList<CategoryFee>();
        this.fees.add(f);
    }

    public Double getSubtotal() {
        return (price!=null && quantity!=null) ? BigDecimal.valueOf(getPrice()*getQuantity()).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue() : 0.0d;
    }

    public Double getSubtotalComplement() {
        return (complementPrice!=null && quantity!=null) ? BigDecimal.valueOf(getComplementPrice()*getQuantity()).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue() : 0.0d;
    }

    public Double getSubtotalOriginal() {
        return (priceOriginal!=null && quantity!=null) ? BigDecimal.valueOf(getPriceOriginal()*getQuantity()).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue() : 0.0d;
    }

    public double getTotalCost() {
        double res = 0.0d;
        if (getBeanProd1()!=null && getBeanProd1().getCostPrice()!=null) res += getBeanProd1().getCostPrice();
        if (getBeanProd2()!=null && getBeanProd2().getCostPrice()!=null) res += getBeanProd2().getCostPrice();
        return res;
    }

    public Double getTotalFees() {
        double res = 0.0d;
        if (fees!=null && !fees.isEmpty()) {
            for(CategoryFee fee : fees) res += fee.getTotal(this) * getQuantity();
        }
        return res * quantity;
    }

    public String getFullCode() {
        StringBuffer buff = new StringBuffer();
        if (StringUtils.isNotEmpty(code1)) {
            buff.append(code1);
            if (StringUtils.isNotEmpty(code2)) buff.append(" + ").append(code2);
        }
        return buff.toString();
    }

    public String getFullName() {
        StringBuffer buff = new StringBuffer();
        if (StringUtils.isNotEmpty(name1)) {
            buff.append(name1);
            if (StringUtils.isNotEmpty(name2)) buff.append(" + ").append(name2);
        }
        return buff.toString();
    }

    public Map<String, Serializable> serialize() {
        Map<String, Serializable> map = new HashMap<String, Serializable>();
        map.put("product1", this.getProduct1().toString());
        if (this.getProduct2() != null) map.put("product2", this.getProduct2().toString());
        if (this.getVariation1() != null) map.put("variation1", this.getVariation1().toString());
        if (this.getVariation2() != null) map.put("variation2", this.getVariation2().toString());
        map.put("quantity", this.getQuantity().toString());
        if (StringUtils.isNotEmpty(this.getSelDate())) map.put("seldate", this.getSelDate());
        if (StringUtils.isNotEmpty(this.getSelTime())) map.put("seltime", this.getSelTime());
        if (this.getPrice()!=null) map.put("price", this.getPrice());
        return map;
    }

    public void initializeItem(BaseAction action) {
        if (action != null) {
            boolean calculatePrice = (this.getPriceOriginal() == null);
            boolean needQuote = false;
            if (getComplement()!=null) {
                Product c = (Product) action.getDao().get(Product.class, getComplement());
                if (c!=null) {
                    setBeanComponent(c);
                    complementPrice = c.getBasePrice();
                }
            }
            if (getProduct1() != null) {
                Product p1 = (Product) action.getDao().get(Product.class, getProduct1());
                if (p1 != null) {
                    needQuote = "Y".equalsIgnoreCase(p1.getNeedQuote());
                    setCode1(p1.getPartNumber());
                    StringBuffer name1 = new StringBuffer();
                    name1.append(p1.getProductName(action.getLocale().getLanguage()));
                    ProductVariation v1 = (ProductVariation) action.getDao().get(ProductVariation.class, getVariation1());
                    if (v1 != null) name1.append(" - ").append(v1.getFullOption());
                    setName1(name1.toString());
                    if (calculatePrice) {
                        setPriceOriginal(p1.getFinalPrice(action.getFrontUserLevel(), getShopCart().getNumProduct(getProduct1()), v1, action.getDao()));
                        if (!needQuote) setPrice(getPriceOriginal());
                    }
                }
                setBeanProd1(p1);
            }
            if (getProduct2() != null) {
                Product p2 = (Product) action.getDao().get(Product.class, getProduct2());
                if (p2 != null) {
                    setCode2(p2.getPartNumber());
                    ProductLang l2 = p2.getLanguage(action.getLocale().getLanguage(), action.getDefaultLanguage());
                    StringBuffer name2 = new StringBuffer();
                    name2.append((l2 != null) ? l2.getProductName() : p2.getPartNumber());
                    ProductVariation v2 = (ProductVariation) action.getDao().get(ProductVariation.class, getVariation2());
                    if (v2 != null) name2.append(" - ").append(v2.getFullOption());
                    setName2(name2.toString());
                }
                setBeanProd2(p2);
            }
            if (calculatePrice && getBeanProd1() != null && getBeanProd2() != null) {
                ProductRelated pr = action.getDao().getProductRelated(getBeanProd1(), getBeanProd2());
                if (pr != null && pr.getCombinedPrice() != null && pr.getCombinedPrice() > 0) {
                    setPriceOriginal(pr.getCombinedPrice());
                    if (!needQuote) setPrice(getPriceOriginal());
                }
            }
        }
    }



}