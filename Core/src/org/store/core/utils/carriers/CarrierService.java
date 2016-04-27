package org.store.core.utils.carriers;

import java.util.Date;
import java.util.List;
import java.util.Properties;

/**
 * Created by IntelliJ IDEA.
 * User: Rogelio Caballero Justo
 * Date: 30-nov-2006
 */
public interface CarrierService {

    public RateServiceResponse getRateServices(ShipTo shipTo, List<org.store.core.utils.carriers.BasePackage> packages);
    public RateServiceResponse getRateServices(ShipTo shipTo, org.store.core.utils.carriers.Shipper shipFrom, List<org.store.core.utils.carriers.BasePackage> packages);

    public ShippingResult generateShipping(ShipTo shipTo, List<org.store.core.utils.carriers.BasePackage> packages, String shippingMethod, String imgPath);
    public ShippingResult generateShipping(ShipTo shipTo, Shipper shipFrom, List<org.store.core.utils.carriers.BasePackage> packages, String shippingMethod, String imgPath, Date shipDate);

//    public TrackResponse getTracking(String trackingNumber);

    public VoidResponse makeVoid(String trackingNumber,String shippingMethod);

    public String getHTMLTracking(String trackingNumber, String shipMethod);

    public boolean available(ShipTo shipTo, List<org.store.core.utils.carriers.BasePackage> packages);

    public List<CarrierPropertyGroup> getPropertyNames();
    public List<CarrierMethod> getShippingMethods();

    public void setProperties(Properties p);

    public String getName();
}
