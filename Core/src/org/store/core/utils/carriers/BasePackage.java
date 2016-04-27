package org.store.core.utils.carriers;

/**
 * Created by IntelliJ IDEA.
 * User: Rogelio Caballero Justo
 * Date: 20-nov-2006
 */
public class BasePackage {

    private Long productId;
    private String description;
    private float dimensionsLength;
    private float dimensionsWidth;
    private float dimensionsHeight;
    private float packageWeight;
    private float insuredValueMonetaryValue;
    private String currencyCode;
    private String dimensionUnit;
    private String weightUnit;


    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public float getDimensionsLength() {
        return dimensionsLength;
    }

    public void setDimensionsLength(float dimensionsLength) {
        this.dimensionsLength = dimensionsLength;
    }

    public float getDimensionsWidth() {
        return dimensionsWidth;
    }

    public void setDimensionsWidth(float dimensionsWidth) {
        this.dimensionsWidth = dimensionsWidth;
    }

    public float getDimensionsHeight() {
        return dimensionsHeight;
    }

    public void setDimensionsHeight(float dimensionsHeight) {
        this.dimensionsHeight = dimensionsHeight;
    }

    public float getPackageWeight() {
        return packageWeight;
    }

    public void setPackageWeight(float packageWeight) {
        this.packageWeight = packageWeight;
    }

    public float getInsuredValueMonetaryValue() {
        return insuredValueMonetaryValue;
    }

    public void setInsuredValueMonetaryValue(float insuredValueMonetaryValue) {
        this.insuredValueMonetaryValue = insuredValueMonetaryValue;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDimensionUnit() {
        return dimensionUnit;
    }

    public void setDimensionUnit(String dimensionUnit) {
        this.dimensionUnit = dimensionUnit;
    }

    public String getWeightUnit() {
        return weightUnit;
    }

    public void setWeightUnit(String weightUnit) {
        this.weightUnit = weightUnit;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }
}
