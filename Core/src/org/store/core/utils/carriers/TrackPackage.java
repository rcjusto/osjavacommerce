package org.store.core.utils.carriers;

/**
 * User: Rogelio Caballero Justo
 * Date: 08-ene-2007
 * Time: 1:20:51
 */
public interface TrackPackage {

    public String getTrackingNumber();

    public String getRescheduledDeliveryDate();
    public String getRescheduledDeliveryTime();

    public String getAlAddressLine1();
    public String getAlAddressLine2();
    public String getAlCity();
    public String getAlState();
    public String getAlCountry();
    public String getAlCode();
    public String getAlDescription();
    public String getAlSignedForByName();

    public String getStatusTypeCode();
    public String getStatusTypeDescription();

    public String getActivityDate();
    public String getActivityTime();

    public String getPackageWeightUnit();
    public String getPackageWeight();
    
}
