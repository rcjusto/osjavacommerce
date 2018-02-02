package org.store.carriers.capost;

import ca.canadapost.cpcdp.rating.GetRatesClient;
import org.store.carriers.capost.common.CaPostPackage;
import org.store.carriers.capost.rs.CaPostRateServicesRequest;
import org.store.carriers.capost.rs.CaPostRateServicesResponse;
import org.store.core.utils.carriers.BasePackage;
import org.store.core.utils.carriers.CarrierMethod;
import org.store.core.utils.carriers.CarrierPropertyGroup;
import org.store.core.utils.carriers.RateServiceResponse;
import org.store.core.utils.carriers.Shipper;
import org.store.core.utils.carriers.ShippingResult;
import org.store.core.utils.carriers.VoidResponse;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Properties;

/**
 * Created by IntelliJ IDEA.
 * User: Rogelio Caballero Justo
 * Date: 30-nov-2006
 */
public class CarrierServiceCaPostImpl implements org.store.core.utils.carriers.CarrierService {
    private String status;
    public static Logger log = Logger.getLogger(CarrierServiceCaPostImpl.class);
    public Properties properties;

    public CarrierServiceCaPostImpl() {
    }

    public CarrierServiceCaPostImpl(Properties props) {
        this.properties = props;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public RateServiceResponse getRateServices(org.store.core.utils.carriers.ShipTo shipTo, List<BasePackage> packages) {
        return getRateServices(shipTo, null, packages);
    }
    public RateServiceResponse getRateServices(org.store.core.utils.carriers.ShipTo shipTo, Shipper shipFrom, List<BasePackage> packages) {
        RateServiceResponse resultado = new RateServiceResponse();
        try {
            shipFrom = new Shipper(properties);
            status = properties.getProperty("status");
            GetRatesClient client = new GetRatesClient(status, properties);
            resultado = client.getRateServices(shipTo, shipFrom, packages);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            resultado.setErrors("Error connecting with Canada Post service.");
        }
        return resultado;
    }

    public org.store.core.utils.carriers.ShippingResult generateShipping(org.store.core.utils.carriers.ShipTo shipTo, List<BasePackage> packages, String shippingMethod, String imgPath) {
        return generateShipping(shipTo, null, packages, shippingMethod, imgPath, Calendar.getInstance().getTime());
    }
    public ShippingResult generateShipping(org.store.core.utils.carriers.ShipTo shipTo, Shipper shipFrom,  List<BasePackage> packages, String shippingMethod, String imgPath, Date shipDate) {
        return null;
    }

    public VoidResponse makeVoid(String trackingNumber, String shippingMethod) {
        return null;
    }

    public org.store.core.utils.carriers.TrackResponse getTracking(String trackingNumber) {
        return null;
    }

    public String getHTMLTracking(String trackingNumber, String shipMethod) {
        return "";
    }

    public boolean available(org.store.core.utils.carriers.ShipTo shipTo, List<BasePackage> packages) {
        for (BasePackage aPackage : packages) {
            CaPostPackage caPack =  new CaPostPackage(aPackage);
            if (!caPack.canApply()) return false;
        }
        return true;
    }

    public List<CarrierPropertyGroup> getPropertyNames() {
        List<CarrierPropertyGroup> res = new ArrayList<CarrierPropertyGroup>();
        res.add( new CarrierPropertyGroup("shipper")
                .addProperty("shipper.attention.name")
                .addProperty("shipper.address.line1")
                .addProperty("shipper.address.line2")
                .addProperty("shipper.address.city")
                .addProperty("shipper.address.state")
                .addProperty("shipper.address.postalcode")
                .addProperty("shipper.address.country")
                .addProperty("shipper.phone.number")
        );
        res.add(new CarrierPropertyGroup("credentials")
            .addProperty("customer.number")
            .addProperty("user.id")
            .addProperty("password")
            .addProperty("status","LIVE|Live,TEST|Test")
        );
        return res;
    }

    public List<CarrierMethod> getShippingMethods() {
        List<CarrierMethod> res = new ArrayList<CarrierMethod>();
        // Canadian destination
        res.add(new CarrierMethod("DOM.RP","Regular Parcel"));
        res.add(new CarrierMethod("DOM.EP","Expedited Parcel"));
        res.add(new CarrierMethod("DOM.XP","Xpresspost"));
        res.add(new CarrierMethod("DOM.XP.CERT","Xpresspost Certified"));
        res.add(new CarrierMethod("DOM.PC","Priority"));
        res.add(new CarrierMethod("DOM.LIB","Library Books"));
        // US destination
        res.add(new CarrierMethod("USA.EP","Expedited Parcel USA"));
        res.add(new CarrierMethod("USA.SP.AIR","Small Packet USA Air"));
        res.add(new CarrierMethod("USA.TP","Tracked Packet – USA"));
        res.add(new CarrierMethod("USA.PW.PARCEL","Priority Worldwide Parcel USA"));
        res.add(new CarrierMethod("USA.TP.LVM","Tracked Packet – USA (LVM)"));
        res.add(new CarrierMethod("USA.PW.ENV","Priority Worldwide Envelope USA"));
        res.add(new CarrierMethod("USA.PW.PAK","Priority Worldwide Pak USA"));
        res.add(new CarrierMethod("USA.XP","Xpresspost USA"));
        // International destination
        res.add(new CarrierMethod("INT.XP","Xpresspost International"));
        res.add(new CarrierMethod("INT.IP.AIR","International Parcel Air"));
        res.add(new CarrierMethod("INT.IP.SURF","International Parcel Surface"));
        res.add(new CarrierMethod("INT.PW.ENV","Priority Worldwide Envelope Int"));
        res.add(new CarrierMethod("INT.PW.PAK","Priority Worldwide pak Int"));
        res.add(new CarrierMethod("INT.PW.PARCEL","Priority Worldwide parcel Int"));
        res.add(new CarrierMethod("INT.SP.AIR","Small Packet International Air"));
        res.add(new CarrierMethod("INT.SP.SURF","Small Packet International Surface"));
        res.add(new CarrierMethod("INT.TP","Tracked Packet – International"));

        return res;
    }

    public void setProperties(Properties p) {
        properties = p;
    }

    public String getName() {
        return "CANADA POST";
    }


}