package org.store.core.utils.carriers;

import java.util.ArrayList;

/**
 * User: Rogelio Caballero Justo
 * Date: 08-ene-2007
 * Time: 0:42:01
 */
public interface TrackResponse {

    public String getAddressLine1();
    public String getAddressLine2();
    public String getCity();
    public String getStateProvinceCode();
    public String getPostalCode();
    public String getCountryCode();

    public String getShipmentWeightUnit();
    public String getShipmentWeight();

    public String getServiceCode();
    public String getServiceDescription();

    public String getPickupDate();
    public String getScheduledDeliveryDate();
    public String getScheduledDeliveryTime();

    public ArrayList<TrackPackage> getPackages();
    
}
