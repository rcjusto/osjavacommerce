package org.store.fedex;

import com.fedex.rate.stub.ServiceType;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.velocity.VelocityContext;
import org.store.core.utils.carriers.*;
import org.store.core.utils.velocity.StoreVelocityGenerator;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

/**
 * Rogelio Caballero
 * 5/06/12 17:08
 */
public class FedexServiceImpl implements CarrierService {

    public static Logger log = Logger.getLogger(FedexServiceImpl.class);
    private Properties properties;
    private static final String TRACKING_VIEW = "org/store/fedex/resources/tracking.vm";

    public FedexServiceImpl() {
    }

    public FedexServiceImpl(Properties properties) {
        this.properties = properties;
    }

    public RateServiceResponse getRateServices(ShipTo shipTo, List<BasePackage> packages) {
        return getRateServices(shipTo, null, packages);  //To change body of implemented methods use File | Settings | File Templates.
    }

    public RateServiceResponse getRateServices(ShipTo shipTo, Shipper shipFrom, List<BasePackage> packages) {
        org.store.fedex.RateService rs = new org.store.fedex.RateService(properties);
        return rs.getRateServices(shipTo, shipFrom, packages);
    }

    public ShippingResult generateShipping(ShipTo shipTo, List<BasePackage> packages, String shippingMethod, String imgPath) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public ShippingResult generateShipping(ShipTo shipTo, Shipper shipFrom, List<BasePackage> packages, String shippingMethod, String imgPath, Date shipDate) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public VoidResponse makeVoid(String trackingNumber, String shippingMethod) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getHTMLTracking(String trackingNumber, String shipMethod) {
        TrackService ts = new TrackService(properties, trackingNumber);
        if (ts.getResult()!=null && !ts.getResult().isEmpty()) {
            VelocityContext context = StoreVelocityGenerator.createContext();
            context.put("packages", ts.getResult());
            return StoreVelocityGenerator.getGeneratedString(context, TRACKING_VIEW);
        } else if (StringUtils.isNotEmpty(ts.getErrors())) {
            return "<p class=\"error\">" + ts.getErrors() + "</p>";
        }

        return null;
    }

    public boolean available(ShipTo shipTo, List<BasePackage> packages) {
        return true;
    }

    public List<CarrierPropertyGroup> getPropertyNames() {
        List<CarrierPropertyGroup> res = new ArrayList<CarrierPropertyGroup>();
        res.add( new CarrierPropertyGroup("shipper")
                .addProperty("shipper.name")
                .addProperty("shipper.attention.name")
                .addProperty("shipper.address.line1")
                .addProperty("shipper.address.line2")
                .addProperty("shipper.address.city")
                .addProperty("shipper.address.state")
                .addProperty("shipper.address.postalcode")
                .addProperty("shipper.address.country")
                .addProperty("shipper.phone.number")
        );
        res.add(new CarrierPropertyGroup("options")
                .addProperty("Mode", "live,test")
                .addProperty("HoldAtLocation", "true,false")
                .addProperty("InsidePickup","true,false")
                .addProperty("InsideDelivery","true,false")
                .addProperty("SaturdayPickup","true,false")
                .addProperty("SaturdayDelivery","true,false")
        );
        res.add(new CarrierPropertyGroup("live.credentials")
                .addProperty("live.AccountNumber")
                .addProperty("live.MeterNumber")
                .addProperty("live.CustomerKey")
                .addProperty("live.CustomerPassword")
        );
        res.add(new CarrierPropertyGroup("test.credentials")
                .addProperty("test.AccountNumber")
                .addProperty("test.MeterNumber")
                .addProperty("test.CustomerKey")
                .addProperty("test.CustomerPassword")
        );
        return res;
    }

    public List<CarrierMethod> getShippingMethods() {
        List<CarrierMethod> res = new ArrayList<CarrierMethod>();
        res.add(new CarrierMethod(ServiceType._EUROPE_FIRST_INTERNATIONAL_PRIORITY,"FedEx Europe First International Priority"));
        res.add(new CarrierMethod(ServiceType._FEDEX_1_DAY_FREIGHT,"FedEx 1Day Freight"));
        res.add(new CarrierMethod(ServiceType._FEDEX_2_DAY,"FedEx 2Day"));
        res.add(new CarrierMethod(ServiceType._FEDEX_2_DAY_AM,"FedEx 2Day AM"));
        res.add(new CarrierMethod(ServiceType._FEDEX_2_DAY_FREIGHT,"FedEx 2Day Freight"));
        res.add(new CarrierMethod(ServiceType._FEDEX_3_DAY_FREIGHT ,"FedEx 3Day Freight"));
        res.add(new CarrierMethod(ServiceType._FEDEX_EXPRESS_SAVER ,"FedEx Express Saver"));
        res.add(new CarrierMethod(ServiceType._FEDEX_FIRST_FREIGHT ,"FedEx First Freight"));
        res.add(new CarrierMethod(ServiceType._FEDEX_FREIGHT_ECONOMY ,"FedEx First Economy"));
        res.add(new CarrierMethod(ServiceType._FEDEX_FREIGHT_PRIORITY ,"FedEx First Priority"));
        res.add(new CarrierMethod(ServiceType._FEDEX_GROUND ,"FedEx Ground"));
        res.add(new CarrierMethod(ServiceType._FIRST_OVERNIGHT ,"FedEx First Overnight"));
        res.add(new CarrierMethod(ServiceType._GROUND_HOME_DELIVERY ,"FedEx Home Delivery"));
        res.add(new CarrierMethod(ServiceType._INTERNATIONAL_ECONOMY ,"FedEx International Economy"));
        res.add(new CarrierMethod(ServiceType._INTERNATIONAL_ECONOMY_FREIGHT ,"FedEx International Freight"));
        res.add(new CarrierMethod(ServiceType._INTERNATIONAL_FIRST ,"FedEx International First"));
        res.add(new CarrierMethod(ServiceType._INTERNATIONAL_PRIORITY ,"FedEx International Priority"));
        res.add(new CarrierMethod(ServiceType._INTERNATIONAL_PRIORITY_FREIGHT ,"FedEx International Priority Freight"));
        res.add(new CarrierMethod(ServiceType._PRIORITY_OVERNIGHT ,"FedEx Priority Overnight"));
        res.add(new CarrierMethod(ServiceType._SMART_POST ,"FedEx Smart Post"));
        res.add(new CarrierMethod(ServiceType._STANDARD_OVERNIGHT ,"FedEx Standard Overnight"));
        return res;
    }

    public void setProperties(Properties p) {
        this.properties = p;
    }

    public String getName() {
        return "FEDEX";
    }


}
