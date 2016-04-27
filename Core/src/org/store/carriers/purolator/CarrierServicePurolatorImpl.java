package org.store.carriers.purolator;

import org.store.carriers.capost.common.CaPostPackage;
import org.store.carriers.purolator.common.PurolatorPackage;
import org.store.carriers.purolator.rs.PurolatorRateServicesRequest;
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
import java.util.Date;
import java.util.List;
import java.util.Properties;

/**
 * Created by IntelliJ IDEA.
 * User: Rogelio Caballero Justo
 */
public class CarrierServicePurolatorImpl implements org.store.core.utils.carriers.CarrierService {
    private String status;
    public static Logger log = Logger.getLogger(CarrierServicePurolatorImpl.class);
    public Properties properties;

    public CarrierServicePurolatorImpl() {
    }

    public CarrierServicePurolatorImpl(Properties props) {
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

        // Buscar las opciones de shipping
        PurolatorRateServicesRequest rs = new PurolatorRateServicesRequest(status, properties);

        rs.setShipTo(shipTo);
        if (shipFrom != null) rs.setShipper(shipFrom);

        for (BasePackage aPackage : packages)
            rs.addPackage(new PurolatorPackage(aPackage));

        try {
            RateServiceResponse r = rs.execute();
            if (r!=null) resultado = r;
            else resultado.setErrors("Can not connect with Purolator service.");
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            resultado.setErrors("Can not connect with Purolator service.");
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            resultado.setErrors("Error connecting with Purolator service.");
        }
        return resultado;
    }

    public ShippingResult generateShipping(org.store.core.utils.carriers.ShipTo shipTo, List<BasePackage> packages, String shippingMethod, String imgPath) {
        return null;
    }

    public ShippingResult generateShipping(org.store.core.utils.carriers.ShipTo shipTo, Shipper shipFrom, List<BasePackage> packages, String shippingMethod, String imgPath, Date shipDate) {
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
            CaPostPackage caPack = new CaPostPackage(aPackage);
            if (!caPack.canApply()) return false;
        }
        return true;
    }

    public List<CarrierPropertyGroup> getPropertyNames() {
        List<CarrierPropertyGroup> res = new ArrayList<CarrierPropertyGroup>();
        res.add(new CarrierPropertyGroup("shipper")
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
                .addProperty("username")
                .addProperty("password")
                .addProperty("account.key")
        );
        res.add(new CarrierPropertyGroup("general")
                .addProperty("package.type", new String[]{"CustomerPackaging", "ExpressEnvelope", "ExpressPack", "ExpressBox"})
        );
        return res;
    }

    public List<CarrierMethod> getShippingMethods() {
        List<CarrierMethod> res = new ArrayList<CarrierMethod>();
        // Canadian destination
        res.add(new CarrierMethod("PurolatorExpress", "PurolatorExpress"));
        res.add(new CarrierMethod("PurolatorExpress9AM", "PurolatorExpress9AM"));
        res.add(new CarrierMethod("PurolatorExpress10:30AM", "PurolatorExpress10:30AM"));
        res.add(new CarrierMethod("PurolatorExpressEvening", "PurolatorExpressEvening"));
        res.add(new CarrierMethod("PurolatorExpressEnvelope", "PurolatorExpressEnvelope"));
        res.add(new CarrierMethod("PurolatorExpressEnvelope9AM", "PurolatorExpressEnvelope9AM"));
        res.add(new CarrierMethod("PurolatorExpressEnvelope10:30AM", "PurolatorExpressEnvelope10:30AM"));
        res.add(new CarrierMethod("PurolatorExpressEnvelopeEvening", "PurolatorExpressEnvelopeEvening"));
        res.add(new CarrierMethod("PurolatorExpressPack", "PurolatorExpressPack"));
        res.add(new CarrierMethod("PurolatorExpressPack9AM", "PurolatorExpressPack9AM"));
        res.add(new CarrierMethod("PurolatorExpressPack10:30AM", "PurolatorExpressPack10:30AM"));
        res.add(new CarrierMethod("PurolatorExpressPackEvening", "PurolatorExpressPackEvening"));
        res.add(new CarrierMethod("PurolatorExpressBox", "PurolatorExpressBox"));
        res.add(new CarrierMethod("PurolatorExpressBox9AM", "PurolatorExpressBox9AM"));
        res.add(new CarrierMethod("PurolatorExpressBox10:30AM", "PurolatorExpressBox10:30AM"));
        res.add(new CarrierMethod("PurolatorExpressBoxEvening", "PurolatorExpressBoxEvening"));
        res.add(new CarrierMethod("PurolatorGround", "PurolatorGround"));
        res.add(new CarrierMethod("PurolatorGround9AM", "PurolatorGround9AM"));
        res.add(new CarrierMethod("PurolatorGround10:30AM", "PurolatorGround10:30AM"));
        res.add(new CarrierMethod("PurolatorGroundEvening", "PurolatorGroundEvening"));

        // US destination
        res.add(new CarrierMethod("PurolatorExpressU.S.", "PurolatorExpressU.S."));
        res.add(new CarrierMethod("PurolatorExpressU.S.9AM", "PurolatorExpressU.S.9AM"));
        res.add(new CarrierMethod("PurolatorExpressU.S.10:30AM", "PurolatorExpressU.S.10:30AM"));
        res.add(new CarrierMethod("PurolatorExpressU.S.12:00", "PurolatorExpressU.S.12:00"));
        res.add(new CarrierMethod("PurolatorExpressEnvelopeU.S.", "PurolatorExpressEnvelopeU.S."));
        res.add(new CarrierMethod("PurolatorExpressU.S.Envelope9AM", "PurolatorExpressU.S.Envelope9AM"));
        res.add(new CarrierMethod("PurolatorExpressU.S.Envelope10:30AM", "PurolatorExpressU.S.Envelope10:30AM"));
        res.add(new CarrierMethod("PurolatorExpressU.S.Envelope12:00", "PurolatorExpressU.S.Envelope12:00"));
        res.add(new CarrierMethod("PurolatorExpressU.S.Pack12:00", "PurolatorExpressU.S.Pack12:00"));
        res.add(new CarrierMethod("PurolatorExpressBoxU.S.", "PurolatorExpressBoxU.S."));
        res.add(new CarrierMethod("PurolatorExpressU.S.Box9AM", "PurolatorExpressU.S.Box9AM"));
        res.add(new CarrierMethod("PurolatorExpressU.S.Box10:30AM", "PurolatorExpressU.S.Box10:30AM"));
        res.add(new CarrierMethod("PurolatorExpressU.S.Box12:00", "PurolatorExpressU.S.Box12:00"));
        res.add(new CarrierMethod("PurolatorGroundU.S.", "PurolatorGroundU.S."));

        // International destination
        res.add(new CarrierMethod("PurolatorExpressInternational", "PurolatorExpressInternational"));
        res.add(new CarrierMethod("PurolatorExpressInternational9AM", "PurolatorExpressInternational9AM"));
        res.add(new CarrierMethod("PurolatorExpressInternational10:30AM", "PurolatorExpressInternational10:30AM"));
        res.add(new CarrierMethod("PurolatorExpressInternational12:00", "PurolatorExpressInternational12:00"));
        res.add(new CarrierMethod("PurolatorExpressEnvelopeInternational", "PurolatorExpressEnvelopeInternational"));
        res.add(new CarrierMethod("PurolatorExpressInternationalEnvelope9AM", "PurolatorExpressInternationalEnvelope9AM"));
        res.add(new CarrierMethod("PurolatorExpressInternationalEnvelope10:30AM", "PurolatorExpressInternationalEnvelope10:30AM"));
        res.add(new CarrierMethod("PurolatorExpressInternationalEnvelope12:00", "PurolatorExpressInternationalEnvelope12:00"));
        res.add(new CarrierMethod("PurolatorExpressPackInternational", "PurolatorExpressPackInternational"));
        res.add(new CarrierMethod("PurolatorExpressInternationalPack9AM", "PurolatorExpressInternationalPack9AM"));
        res.add(new CarrierMethod("PurolatorExpressInternationalPack10:30AM", "PurolatorExpressInternationalPack10:30AM"));
        res.add(new CarrierMethod("PurolatorExpressInternationalPack12:00", "PurolatorExpressInternationalPack12:00"));
        res.add(new CarrierMethod("PurolatorExpressBoxInternational", "PurolatorExpressBoxInternational"));
        res.add(new CarrierMethod("PurolatorExpressInternationalBox9AM", "PurolatorExpressInternationalBox9AM"));
        res.add(new CarrierMethod("PurolatorExpressInternationalBox10:30AM", "PurolatorExpressInternationalBox10:30AM"));
        res.add(new CarrierMethod("PurolatorExpressInternationalBox12:00", "PurolatorExpressInternationalBox12:00"));
        res.add(new CarrierMethod("PurolatorGroundDistribution", "PurolatorGroundDistribution"));

        return res;
    }

    public void setProperties(Properties p) {
        properties = p;
    }

    public String getName() {
        return "PUROLATOR";
    }


}