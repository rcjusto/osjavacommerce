package org.store.carriers.ups.tr;

import org.store.core.utils.carriers.TrackPackage;


/**
 * User: Rogelio Caballero Justo
 * Date: 06-ene-2007
 * Time: 23:43:06
 */
public class PackageTrackResult implements TrackPackage {

    private String trackingNumber;

    private String rescheduledDeliveryDate;
    private String rescheduledDeliveryTime;

    private String alAddressLine1;
    private String alAddressLine2;
    private String alCity;
    private String alState;
    private String alCountry;
    private String alCode;
    private String alDescription;
    private String alSignedForByName;

    private String statusTypeCode;
    private String statusTypeDescription;
    private String statusCode;

    private String activityDate;
    private String activityTime;

    private String packageWeightUnit;
    private String packageWeight;


    public String getTrackingNumber() {
        return trackingNumber;
    }

    public void setTrackingNumber(String trackingNumber) {
        this.trackingNumber = trackingNumber;
    }

    public String getAlCity() {
        return alCity;
    }

    public void setAlCity(String alCity) {
        this.alCity = alCity;
    }

    public String getAlState() {
        return alState;
    }

    public void setAlState(String alState) {
        this.alState = alState;
    }

    public String getAlCountry() {
        return alCountry;
    }

    public void setAlCountry(String alCountry) {
        this.alCountry = alCountry;
    }

    public String getAlCode() {
        return alCode;
    }

    public void setAlCode(String alCode) {
        this.alCode = alCode;
    }

    public String getAlDescription() {
        return alDescription;
    }

    public void setAlDescription(String alDescription) {
        this.alDescription = alDescription;
    }


    public String getStatusTypeCode() {
        return statusTypeCode;
    }

    public void setStatusTypeCode(String statusTypeCode) {
        this.statusTypeCode = statusTypeCode;
    }

    public String getStatusTypeDescription() {
        return statusTypeDescription;
    }

    public void setStatusTypeDescription(String statusTypeDescription) {
        this.statusTypeDescription = statusTypeDescription;
    }

    public String getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(String statusCode) {
        this.statusCode = statusCode;
    }

    public String getActivityDate() {
        return activityDate;
    }

    public void setActivityDate(String activityDate) {
        this.activityDate = activityDate;
    }

    public String getActivityTime() {
        return activityTime;
    }

    public void setActivityTime(String activityTime) {
        this.activityTime = activityTime;
    }

    public String getPackageWeightUnit() {
        return packageWeightUnit;
    }

    public void setPackageWeightUnit(String packageWeightUnit) {
        this.packageWeightUnit = packageWeightUnit;
    }

    public String getPackageWeight() {
        return packageWeight;
    }

    public void setPackageWeight(String packageWeight) {
        this.packageWeight = packageWeight;
    }

    public String getRescheduledDeliveryDate() {
        return rescheduledDeliveryDate;
    }

    public void setRescheduledDeliveryDate(String rescheduledDeliveryDate) {
        this.rescheduledDeliveryDate = rescheduledDeliveryDate;
    }

    public String getRescheduledDeliveryTime() {
        return rescheduledDeliveryTime;
    }

    public void setRescheduledDeliveryTime(String rescheduledDeliveryTime) {
        this.rescheduledDeliveryTime = rescheduledDeliveryTime;
    }

    public String getAlAddressLine1() {
        return alAddressLine1;
    }

    public void setAlAddressLine1(String alAddressLine1) {
        this.alAddressLine1 = alAddressLine1;
    }

    public String getAlAddressLine2() {
        return alAddressLine2;
    }

    public void setAlAddressLine2(String alAddressLine2) {
        this.alAddressLine2 = alAddressLine2;
    }

    public String getAlSignedForByName() {
        return alSignedForByName;
    }

    public void setAlSignedForByName(String alSignedForByName) {
        this.alSignedForByName = alSignedForByName;
    }
}
