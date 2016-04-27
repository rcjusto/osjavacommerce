package org.store.core.beans;

import org.store.core.beans.utils.StoreBean;
import org.store.core.globals.SomeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.hibernate.Criteria;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

/**
 * Oferta de un producto
 */
@Entity
@Table(name = "t_product_offer")
public class ProductOffer extends BaseBean implements StoreBean {

    public static final String RULE_CATEGORY = "Category";
    public static final String RULE_LABEL = "ProductLabel";
    public static final String RULE_MANUFACTURER = "Manufacturer";
    public static final String RULE_PRODUCT = "Product";

    public static final String OPER_EQUAL = "eq";
    public static final String OPER_CONTAINS = "contains";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idOffer;

    @Column(length = 10)
    private String inventaryCode;

    // Fecha de inicio de la oferta
    private Date dateFrom;

    // Fecha de fin de la oferta
    private Date dateTo;

    // Descuento
    private Double discount;
    private Double discountPercent;

    // Regla que tienen q cumplir los productos para q se cumpla la oferta
    private String ruleObject;
    private String ruleOperation;
    private String ruleValue;


    public ProductOffer() {
    }

    public Long getIdOffer() {
        return idOffer;
    }

    public void setIdOffer(Long idOffer) {
        this.idOffer = idOffer;
    }

    public Date getDateFrom() {
        return dateFrom;
    }

    public void setDateFrom(Date dateFrom) {
        this.dateFrom = dateFrom;
    }

    public Date getDateTo() {
        return dateTo;
    }

    public void setDateTo(Date dateTo) {
        this.dateTo = dateTo;
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

    public Double getPercent() {
        return (discountPercent!=null) ? 1 - discountPercent : null;
    }

    public void setPercent(Double p) {
        this.discountPercent = (p!=null) ? 1 - p : null;
    }

    public String getRuleObject() {
        return ruleObject;
    }

    public void setRuleObject(String ruleObject) {
        this.ruleObject = ruleObject;
    }

    public String getRuleOperation() {
        return ruleOperation;
    }

    public void setRuleOperation(String ruleOperation) {
        this.ruleOperation = ruleOperation;
    }

    public String getRuleValue() {
        return ruleValue;
    }

    public void setRuleValue(String ruleValue) {
        this.ruleValue = ruleValue;
    }

    public String getInventaryCode() {
        return inventaryCode;
    }

    public void setInventaryCode(String inventaryCode) {
        this.inventaryCode = inventaryCode;
    }

    public Criterion getRestriction() {
        if (StringUtils.isNotEmpty(ruleObject) && StringUtils.isNotEmpty(ruleOperation)) {
            if (RULE_PRODUCT.equalsIgnoreCase(ruleObject)) {
                if (OPER_EQUAL.equalsIgnoreCase(ruleOperation)) {
                    Long idPro = SomeUtils.strToLong(ruleValue);
                    if (idPro != null) return Restrictions.eq("idProduct", idPro);
                } else if (OPER_CONTAINS.equals(ruleOperation) && StringUtils.isNotEmpty(ruleValue)) {
                    Long[] arr = SomeUtils.strToLong(ruleValue.split(","));
                    if (arr != null && arr.length > 0) return Restrictions.in("idProduct", arr);
                }
            } else if (RULE_CATEGORY.equalsIgnoreCase(ruleObject)) {
                if (OPER_EQUAL.equalsIgnoreCase(ruleOperation) && getAction()!=null) {
                    Category cat = getAction().getDao().getCategory(SomeUtils.strToLong(ruleValue));
                    if (cat!=null) {
                        Long[] ids = getAction().getDao().getIdCategoryList(cat , false);
                        if (ids!=null && ids.length>0) return Restrictions.in("idCategory",ids);
                    }
                    return Restrictions.eq("idCategory",0l);
                } else if (OPER_CONTAINS.equals(ruleOperation) && StringUtils.isNotEmpty(ruleValue)) {
                    Long[] arr = SomeUtils.strToLong(ruleValue.split(","));
                    if (arr != null && arr.length > 0) return Restrictions.in("idCategory", arr);
                }
            } else if (RULE_LABEL.equalsIgnoreCase(ruleObject)) {

            } else if (RULE_MANUFACTURER.equalsIgnoreCase(ruleObject)) {

            }
        }
        return null;
    }

    public void setRestrictionForCriteria(Criteria criPro) {
        if (StringUtils.isNotEmpty(ruleObject) && StringUtils.isNotEmpty(ruleOperation)) {
            Criterion c = getRestriction();
            if (RULE_PRODUCT.equalsIgnoreCase(ruleObject)) {
                if (c!=null) criPro.add(c);
            } else if (RULE_CATEGORY.equalsIgnoreCase(ruleObject)) {
                if (c!=null) {
                    Criteria criCat = criPro.createCriteria("category");
                    criCat.add(c);
                }
            } else if (RULE_LABEL.equalsIgnoreCase(ruleObject)) {

            } else if (RULE_MANUFACTURER.equalsIgnoreCase(ruleObject)) {

            }
        }
    }

    public boolean isForCategory(Category c) {
        return (c != null && c.getIdCategory() != null) && (RULE_CATEGORY.equalsIgnoreCase(ruleObject) && OPER_EQUAL.equalsIgnoreCase(ruleOperation) && c.getIdCategory().equals(SomeUtils.strToLong(ruleValue)));
    }

    public boolean isForProduct(Product p) {
        return (p != null && p.getIdProduct() != null) && (RULE_PRODUCT.equalsIgnoreCase(ruleObject) && OPER_EQUAL.equalsIgnoreCase(ruleOperation) && p.getIdProduct().equals(SomeUtils.strToLong(ruleValue)));
    }

    public boolean isForManufacturer(Manufacturer m) {
        return (m != null && m.getIdManufacturer() != null) && (RULE_MANUFACTURER.equalsIgnoreCase(ruleObject) && OPER_EQUAL.equalsIgnoreCase(ruleOperation) && m.getIdManufacturer().equals(SomeUtils.strToLong(ruleValue)));
    }

    public boolean isForLabel(ProductLabel l) {
        return (l != null &&  StringUtils.isNotEmpty(l.getCode())) && (RULE_LABEL.equalsIgnoreCase(ruleObject) && OPER_EQUAL.equalsIgnoreCase(ruleOperation) && l.getCode().equalsIgnoreCase(ruleValue));
    }

    public boolean isActive() {
        return !(dateFrom != null && dateFrom.after(SomeUtils.dateEnd(null))) && !(dateTo != null && dateTo.before(SomeUtils.dateIni(null)));
    }

    public String toString() {
        return new ToStringBuilder(this)
                .append("idOffer", getIdOffer())
                .toString();
    }

}
