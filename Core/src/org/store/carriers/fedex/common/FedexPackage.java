package org.store.carriers.fedex.common;

import org.store.core.beans.StoreProperty;
import org.store.core.utils.carriers.BasePackage;

import java.util.Properties;


/**
 * Created by IntelliJ IDEA.
 * User: Rogelio Caballero Justo
 * Date: 20-nov-2006
 */
public class FedexPackage  {

    BasePackage base;
    private boolean additionalHandling;
    private String dimensionUnit;
    private String weightUnit;


    public FedexPackage(BasePackage base, Properties prop) {
        this.base = base;
        this.dimensionUnit = prop.getProperty("DimentionsUnits");
        this.weightUnit = prop.getProperty("WeightUnits");
    }

    public Long getProductId() {
        return base.getProductId();
    }

    public float getDimensionsLength() {
        return convertDimensionFrom(base.getDimensionsLength(), base.getDimensionUnit());
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
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean isAdditionalHandling() {
        return additionalHandling;
    }

    public void setAdditionalHandling(boolean additionalHandling) {
        this.additionalHandling = additionalHandling;
    }

   public float convertWeightFrom(float size, String fromUnit) {
        if ("KGS".equalsIgnoreCase(weightUnit)) {
            if (StoreProperty.UNIT_WEIGHT_POUND.equalsIgnoreCase(fromUnit)) {
                // POUND TO KG
                return size * 0.45359237f;
            } else if (StoreProperty.UNIT_WEIGHT_KILOGRAM.equalsIgnoreCase(fromUnit)) {
                return size;
            }
        } else if ("LBS".equalsIgnoreCase(weightUnit)) {
            if (StoreProperty.UNIT_WEIGHT_POUND.equalsIgnoreCase(fromUnit)) {
                return size;
            } else if (StoreProperty.UNIT_WEIGHT_KILOGRAM.equalsIgnoreCase(fromUnit)) {
                // KG to POUND
                return size * 2.20462262f;
            }
        }
        return 0f;
    }

    // convertir a cm
    public float convertDimensionFrom(float size, String fromUnit) {
        if ("IN".equalsIgnoreCase(dimensionUnit)) {
            if (StoreProperty.UNIT_DIMENSION_INCH.equalsIgnoreCase(fromUnit)) {
                return size;
            } else if (StoreProperty.UNIT_DIMENSION_METER.equalsIgnoreCase(fromUnit)) {
                // M tp INCH
                return size * 39.3700787f;
            } else if (StoreProperty.UNIT_DIMENSION_CENTIMETER.equalsIgnoreCase(fromUnit)) {
                // CM Tto INCH
                return size * 0.393700787f;
            }
        } else if ("CM".equalsIgnoreCase(dimensionUnit)) {
            if (StoreProperty.UNIT_DIMENSION_INCH.equalsIgnoreCase(fromUnit)) {
                // INCH to CM
                return size * 2.54f;
            } else if (StoreProperty.UNIT_DIMENSION_METER.equalsIgnoreCase(fromUnit)) {
                // M to CM
                return size * 100f;
            } else if (StoreProperty.UNIT_DIMENSION_CENTIMETER.equalsIgnoreCase(fromUnit)) {
                return size;
            }
        }
        return 0f;
    }
}
