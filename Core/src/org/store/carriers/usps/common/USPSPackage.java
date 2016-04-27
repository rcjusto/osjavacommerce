package org.store.carriers.usps.common;


import org.store.core.beans.StoreProperty;
import org.store.core.utils.carriers.BasePackage;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * Created by IntelliJ IDEA.
 * User: Rogelio Caballero Justo
 * Date: 20-nov-2006
 */
public class USPSPackage {

    private org.store.core.utils.carriers.BasePackage base;
    private boolean additionalHandling;
    private static final String CNT_SIZE_REGULAR = "REGULAR";
    private static final String CNT_SIZE_LARGE = "LARGE";
    private static final String CNT_SIZE_OVERSIZE = "OVERSIZE";


    public USPSPackage(BasePackage base) {
        this.base = base;
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

    public String getOversizePackage() {
        float length = Math.max(getDimensionsWidth(), Math.max(getDimensionsHeight(), getDimensionsLength()));
        float girth;
        if (getDimensionsLength() >= getDimensionsWidth() && getDimensionsLength() >= getDimensionsHeight())
            girth = 2 * (getDimensionsWidth() + getDimensionsHeight());
        else if (getDimensionsHeight() >= getDimensionsWidth())
            girth = 2 * (getDimensionsWidth() + getDimensionsLength());
        else
            girth = 2 * (getDimensionsHeight() + getDimensionsLength());
        float oversize = length + girth;
        if (oversize > 84 && oversize <= 108 && getPackageWeight() <= 30) return "1";
        else if (oversize > 108 && oversize <= 130 && getPackageWeight() <= 70) return "2";
        else if (oversize > 130 && oversize <= 165 && getPackageWeight() <= 90) return "3";
        else return null;
    }

    public float getInsuredValueMonetaryValue() {
        return base.getInsuredValueMonetaryValue();
    }

    public boolean canApply() {
        float[] mes = getMeasures();
        boolean validSize = mes[0] + mes[1] < 130;
        boolean validWeight = getPackageWeight() < 70;
        return validSize && validWeight;
    }

    public boolean isAdditionalHandling() {
        return additionalHandling;
    }

    public void setAdditionalHandling(boolean additionalHandling) {
        this.additionalHandling = additionalHandling;
    }

    private float[] getMeasures() {
        float[] res = new float[2];
        float length = Math.max(getDimensionsWidth(), Math.max(getDimensionsHeight(), getDimensionsLength()));
        float girth;
        if (getDimensionsLength() >= getDimensionsWidth() && getDimensionsLength() >= getDimensionsHeight())
            girth = 2 * (getDimensionsWidth() + getDimensionsHeight());
        else if (getDimensionsHeight() >= getDimensionsWidth())
            girth = 2 * (getDimensionsWidth() + getDimensionsLength());
        else
            girth = 2 * (getDimensionsHeight() + getDimensionsLength());
        res[0] = length;
        res[1] = girth;
        return res;
    }

    public String getSize() {
        float[] mes = getMeasures();
        float tot = mes[0] + mes[1];
        if (tot <= 84) return CNT_SIZE_REGULAR;
        else if (tot > 84 && tot <= 108) return CNT_SIZE_LARGE;
        else if (tot > 108 && tot <= 130) return CNT_SIZE_OVERSIZE;
        else return "";
    }

    public String getMachinable() {
        return (getPackageWeight() < 35) ? "TRUE" : "FALSE";
    }

    public String getPounds() {
        Float w = getPackageWeight();
        return String.valueOf(w.intValue());
    }

    public String getOnces() {
        Locale locEn = new Locale("es");
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(locEn);
        symbols.setDecimalSeparator(',');
        DecimalFormat df = new DecimalFormat("0.0", symbols);
        String val = df.format(getPackageWeight());
        String[] arr = val.split(",");
        int dp = (arr.length > 1) ? Integer.parseInt(arr[1]) : 0;
        if (dp > 15) dp = 15;
        return String.valueOf(dp);
    }


    public float convertWeightFrom(float size, String fromUnit) {
        if (StoreProperty.UNIT_WEIGHT_POUND.equalsIgnoreCase(fromUnit)) {
            return size;
        } else if (StoreProperty.UNIT_WEIGHT_KILOGRAM.equalsIgnoreCase(fromUnit)) {
            // KG to POUND
            return size * 2.20462262f;
        }
        return 0f;
    }

    // convertir a cm

    public float convertDimensionFrom(float size, String fromUnit) {
        if (StoreProperty.UNIT_DIMENSION_INCH.equalsIgnoreCase(fromUnit)) {
            return size;
        } else if (StoreProperty.UNIT_DIMENSION_METER.equalsIgnoreCase(fromUnit)) {
            // M tp INCH
            return size * 39.3700787f;
        } else if (StoreProperty.UNIT_DIMENSION_CENTIMETER.equalsIgnoreCase(fromUnit)) {
            // CM Tto INCH
            return size * 0.393700787f;
        }
        return 0f;
    }

}
