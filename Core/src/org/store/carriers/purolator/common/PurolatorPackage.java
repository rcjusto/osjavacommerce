package org.store.carriers.purolator.common;

import org.store.core.beans.StoreProperty;
import org.store.core.utils.carriers.BasePackage;
import pws.client.EstimatingService.EstimatingServiceStub;


/**
 * Created by IntelliJ IDEA.
 * User: Rogelio Caballero Justo
 * Date: 20-nov-2006
 */

public class PurolatorPackage {

    BasePackage base;
    private boolean additionalHandling;


    public PurolatorPackage(BasePackage p) {
        base = p;
    }

    public Long getProductId() {
        return base.getProductId();
    }

    public EstimatingServiceStub.Dimension getDimensionsLength() {
        EstimatingServiceStub.Dimension dim = new EstimatingServiceStub.Dimension();
        java.math.BigDecimal v = new java.math.BigDecimal(convertDimensionFrom(base.getDimensionsLength(), base.getDimensionUnit()));
        dim.setValue(v);
        dim.setDimensionUnit(EstimatingServiceStub.DimensionUnit.cm);
        return dim;
    }

    public EstimatingServiceStub.Dimension getDimensionsWidth() {
        EstimatingServiceStub.Dimension dim = new EstimatingServiceStub.Dimension();
        java.math.BigDecimal v = new java.math.BigDecimal(convertDimensionFrom(base.getDimensionsWidth(), base.getDimensionUnit()));
        dim.setValue(v);
        dim.setDimensionUnit(EstimatingServiceStub.DimensionUnit.cm);
        return dim;
    }

    public EstimatingServiceStub.Dimension getDimensionsHeight() {
        EstimatingServiceStub.Dimension dim = new EstimatingServiceStub.Dimension();
        java.math.BigDecimal v = new java.math.BigDecimal(convertDimensionFrom(base.getDimensionsHeight(), base.getDimensionUnit()));
        dim.setValue(v);
        dim.setDimensionUnit(EstimatingServiceStub.DimensionUnit.cm);
        return dim;
    }

    public EstimatingServiceStub.Weight getPackageWeight() {
        EstimatingServiceStub.Weight weight = new EstimatingServiceStub.Weight();
        java.math.BigDecimal w = new java.math.BigDecimal(convertWeightFrom(base.getPackageWeight(), base.getWeightUnit()));
        weight.setValue(w);
        weight.setWeightUnit(EstimatingServiceStub.WeightUnit.kg);
        return weight;
    }

    public String getDescription() {
        return base.getDescription();
    }

    public String getOversizePackage() {
        return null;
    }

    public float getInsuredValueMonetaryValue() {
        return base.getInsuredValueMonetaryValue();
    }

    public boolean canApply() {
        return true;
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
            return size * 0.45359237f;
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
        } else if (StoreProperty.UNIT_DIMENSION_CENTIMETER.equalsIgnoreCase(fromUnit)) {
            return size;
        }
        return 0f;
    }

    public EstimatingServiceStub.Piece getPiece() {
        EstimatingServiceStub.Piece p = new EstimatingServiceStub.Piece();
        p.setWidth(getDimensionsWidth());
        p.setLength(getDimensionsLength());
        p.setHeight(getDimensionsHeight());
        p.setWeight(getPackageWeight());
        return p;
    }

}