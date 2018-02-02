package org.store.carriers.capost.common;

import org.store.core.beans.StoreProperty;
import org.store.core.utils.carriers.BasePackage;


/**
 * Created by IntelliJ IDEA.
 * User: Rogelio Caballero Justo
 * Date: 20-nov-2006
 */

public class CaPostPackage {

    org.store.core.utils.carriers.BasePackage base;
    private boolean additionalHandling;


    public CaPostPackage(BasePackage p) {
        base = p;
    }

    public Long getProductId() {
        return base.getProductId();
    }

    public float getDimensionsLength() {
        return convertDimensionFrom(base.getDimensionsLength(),base.getDimensionUnit());
    }

    public float getDimensionsWidth() {
        return convertDimensionFrom(base.getDimensionsWidth(),base.getDimensionUnit());
    }

    public float getDimensionsHeight() {
        return convertDimensionFrom(base.getDimensionsHeight(),base.getDimensionUnit());
    }

    public float getPackageWeight() {
        return convertWeightFrom(base.getPackageWeight(),base.getWeightUnit());
    }

    public String getDescription() {
        return base.getDescription();
    }

    public String getOversizePackage() {
        float length =  Math.max( getDimensionsWidth() , Math.max(getDimensionsHeight(), getDimensionsLength()) );
        float girth;
        if (getDimensionsLength()>=getDimensionsWidth() && getDimensionsLength()>=getDimensionsHeight())
            girth = 2 * (getDimensionsWidth() + getDimensionsHeight());
        else if (getDimensionsHeight()>=getDimensionsWidth())
            girth = 2 * (getDimensionsWidth() + getDimensionsLength());
        else
            girth = 2 * (getDimensionsHeight() + getDimensionsLength());
        float oversize = length + girth;
        if (oversize>84 && oversize<=108 && getPackageWeight()<=30 ) return "1";
        else if (oversize>108 && oversize<=130 && getPackageWeight()<=70) return "2";
        else if (oversize>130 && oversize<=165 && getPackageWeight()<=90) return "3";
        else return null;
    }

    public float getInsuredValueMonetaryValue() {
        return base.getInsuredValueMonetaryValue();
    }

    public boolean canApply() {
        return getDimensionsHeight()<=108 &&
                getDimensionsLength()<=108 &&
                getDimensionsWidth()<=108 &&
                getPackageWeight()<90;
    }

    public boolean isAdditionalHandling() {
        return additionalHandling;
    }

    public void setAdditionalHandling(boolean additionalHandling) {
        this.additionalHandling = additionalHandling;
    }

    // convertir a kg
    public float convertWeightFrom(float size, String fromUnit) {
        if (StoreProperty.UNIT_WEIGHT_POUND.equalsIgnoreCase(fromUnit)) {
            return size *  0.45359237f;  
        } else if (StoreProperty.UNIT_WEIGHT_KILOGRAM.equalsIgnoreCase(fromUnit)) {
            return size;
        }
        return 0f;
    }

    // convertir a cm
    public float convertDimensionFrom(float size, String fromUnit) {
        if (StoreProperty.UNIT_DIMENSION_INCH.equalsIgnoreCase(fromUnit)) {
            return size * 2.54f; 
        } else if (StoreProperty.UNIT_DIMENSION_METER.equalsIgnoreCase(fromUnit)) {
            return size * 100f;
        } else if (StoreProperty.UNIT_DIMENSION_CENTIMETER.equalsIgnoreCase(fromUnit))  {
            return size;
        }
        return 0f;
    }

}