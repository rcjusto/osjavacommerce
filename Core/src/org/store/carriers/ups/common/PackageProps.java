package org.store.carriers.ups.common;

import java.util.Properties;

/**
 * Created by IntelliJ IDEA.
 * User: Rogelio Caballero Justo
 * Date: 20-nov-2006
 */
public class PackageProps {

    private String packagingTypeCode = "02";
    private String dimensionsUnitOfMeasurementCode;
    private String dimensionalWeightUnitOfMeasurementCode;
    private String packageWeightUnitOfMeasurementCode;
    private String deliveryConfirmationDCISType;


    public PackageProps(String dimensionsUnitOfMeasurementCode, String dimensionalWeightUnitOfMeasurementCode, String packageWeightUnitOfMeasurementCode, String deliveryConfirmationDCISType, String insuredValueCurrencyCode) {
        this.dimensionsUnitOfMeasurementCode = dimensionsUnitOfMeasurementCode;
        this.dimensionalWeightUnitOfMeasurementCode = dimensionalWeightUnitOfMeasurementCode;
        this.packageWeightUnitOfMeasurementCode = packageWeightUnitOfMeasurementCode;
        this.deliveryConfirmationDCISType = deliveryConfirmationDCISType;
    }

    public PackageProps(Properties prop) {
        this.dimensionsUnitOfMeasurementCode = prop.getProperty("package.props.unit.dimensions");
        this.dimensionalWeightUnitOfMeasurementCode = prop.getProperty("package.props.unit.weight");
        this.packageWeightUnitOfMeasurementCode = prop.getProperty("package.props.unit.weight");
        this.deliveryConfirmationDCISType = prop.getProperty("package.props.confirmation.type");
    }

    public String getPackagingTypeCode() {
        return packagingTypeCode;
    }

    public String getDimensionsUnitOfMeasurementCode() {
        return dimensionsUnitOfMeasurementCode;
    }

    public void setDimensionsUnitOfMeasurementCode(String dimensionsUnitOfMeasurementCode) {
        this.dimensionsUnitOfMeasurementCode = dimensionsUnitOfMeasurementCode;
    }

    public String getDimensionalWeightUnitOfMeasurementCode() {
        return dimensionalWeightUnitOfMeasurementCode;
    }

    public void setDimensionalWeightUnitOfMeasurementCode(String dimensionalWeightUnitOfMeasurementCode) {
        this.dimensionalWeightUnitOfMeasurementCode = dimensionalWeightUnitOfMeasurementCode;
    }

    public String getPackageWeightUnitOfMeasurementCode() {
        return packageWeightUnitOfMeasurementCode;
    }

    public void setPackageWeightUnitOfMeasurementCode(String packageWeightUnitOfMeasurementCode) {
        this.packageWeightUnitOfMeasurementCode = packageWeightUnitOfMeasurementCode;
    }

    public String getDeliveryConfirmationDCISType() {
        return deliveryConfirmationDCISType;
    }

    public void setDeliveryConfirmationDCISType(String deliveryConfirmationDCISType) {
        this.deliveryConfirmationDCISType = deliveryConfirmationDCISType;
    }

}
