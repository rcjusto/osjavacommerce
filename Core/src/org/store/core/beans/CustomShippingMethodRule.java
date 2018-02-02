package org.store.core.beans;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * Fees
 */
@Entity
@Table(name = "t_custom_shipping_rule")
public class CustomShippingMethodRule extends BaseBean {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 5)
    private String idCountry;

    @ManyToOne
    private State state;

    private Double minWeight;
    private Double maxWeight;
    private Double minTotal;
    private Double maxTotal;

    private Double rulePrice;
    private Double rulePercent;
    private Boolean perWeightUnit;
    private Integer days;

    private Integer ruleOrder;

    @ManyToOne
    private CustomShippingMethod method;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getRuleOrder() {
        return ruleOrder;
    }

    public void setRuleOrder(Integer ruleOrder) {
        this.ruleOrder = ruleOrder;
    }

    public String getIdCountry() {
        return idCountry;
    }

    public void setIdCountry(String idCountry) {
        this.idCountry = idCountry;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public Double getMinWeight() {
        return minWeight;
    }

    public void setMinWeight(Double minWeight) {
        this.minWeight = minWeight;
    }

    public Double getMaxWeight() {
        return maxWeight;
    }

    public void setMaxWeight(Double maxWeight) {
        this.maxWeight = maxWeight;
    }

    public Double getMinTotal() {
        return minTotal;
    }

    public void setMinTotal(Double minTotal) {
        this.minTotal = minTotal;
    }

    public Double getMaxTotal() {
        return maxTotal;
    }

    public void setMaxTotal(Double maxTotal) {
        this.maxTotal = maxTotal;
    }

    public Double getRulePrice() {
        return rulePrice;
    }

    public void setRulePrice(Double rulePrice) {
        this.rulePrice = rulePrice;
    }

    public Double getRulePercent() {
        return rulePercent;
    }

    public void setRulePercent(Double rulePercent) {
        this.rulePercent = rulePercent;
    }

    public Boolean getPerWeightUnit() {
        return perWeightUnit!=null && perWeightUnit;
    }

    public void setPerWeightUnit(Boolean perWeightUnit) {
        this.perWeightUnit = perWeightUnit;
    }

    public Integer getDays() {
        return days;
    }

    public void setDays(Integer days) {
        this.days = days;
    }

    public CustomShippingMethod getMethod() {
        return method;
    }

    public void setMethod(CustomShippingMethod method) {
        this.method = method;
    }

    public String toString() {
        return new ToStringBuilder(this)
                .append("id", getId())
                .toString();
    }

    public boolean equals(Object other) {
        if ((this == other)) return true;
        if (!(other instanceof CustomShippingMethodRule)) return false;
        CustomShippingMethodRule castOther = (CustomShippingMethodRule) other;
        return new EqualsBuilder()
                .append(this.getId(), castOther.getId())
                .isEquals();
    }

    public boolean canApply(String c, State s, double totalCost, double totalWeight) {
        if (StringUtils.isNotEmpty(idCountry) && !idCountry.equalsIgnoreCase(c)) return false;
        if (state!=null && !state.equals(s)) return false;
        if (minTotal!=null && minTotal>totalCost) return false;
        if (minWeight!=null && minWeight>totalWeight) return false;
        if (maxTotal!=null && maxTotal<totalCost) return false;
        if (maxWeight!=null && maxWeight<totalWeight) return false;
        return true;
    }
}