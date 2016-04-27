package org.store.carriers.fedex.sr;

import org.store.carriers.fedex.CarrierServiceFedexImpl;
import org.store.core.utils.carriers.ShipTo;
import org.store.core.utils.carriers.velocity.VelocityGenerator;
import org.store.core.utils.carriers.Shipper;
import org.store.carriers.fedex.common.FedexPackage;
import org.store.carriers.fedex.common.FedexUser;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.log4j.Logger;
import org.apache.velocity.VelocityContext;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;

/**
 * Created by IntelliJ IDEA.
 * User: Rogelio Caballero Justo
 * Date: 20-nov-2006
 */
public class FedexShipRequest {
   private String status;
    private FedexUser user;

    private String shipDate;
    private String shipTime;
    private String carrierCode;

    private final String RETURN_SHIPMENT_INDICATOR = "NONRETURN";
    private final String DROPOFF_TYPE = "REGULARPICKUP";
    private final String PACKAGING = "YOURPACKAGING";
    private final String PAYOR_TYPE = "SENDER";
    private final String LABEL_TYPE = "2DCOMMON";
    private final String LABEL_IMAGE_TYPE = "PNG";


    private String service;

    private String weightUnits;
    private String dimentionsUnits;
    private String holdAtLocation;
    private String dryIce;
    private String residentialDelivery;
    private String insidePickup;
    private String insideDelivery;
    private String saturdayPickup;
    private String saturdayDelivery;
    private String futureDayShipment;


    private org.store.core.utils.carriers.Shipper originAddress;
    private org.store.core.utils.carriers.ShipTo destinationAddress;
    private FedexPackage pack;

    public static Logger log = Logger.getLogger("Carriers");


    public FedexShipRequest(String runStatus, Properties prop) {
        status = runStatus;
        if (prop != null) loadProperties(prop);
    }

    public FedexUser getUser() {
        return user;
    }

    public void setUser(FedexUser user) {
        this.user = user;
    }

    public String getShipDate() {
        return shipDate;
    }

    public void setShipDate(Date shipDate) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        this.shipDate = sdf.format(shipDate);
    }

    public String getCarrierCode() {
        return carrierCode;
    }

    public void setCarrierCode(String carrierCode) {
        this.carrierCode = carrierCode;
    }

    public String getReturnShipmentIndicator() {
        return RETURN_SHIPMENT_INDICATOR;
    }

    public String getDropoffType() {
        return DROPOFF_TYPE;
    }

    public String getPackaging() {
        return PACKAGING;
    }

    public String getWeightUnits() {
        return weightUnits;
    }

    public void setWeightUnits(String weightUnits) {
        this.weightUnits = weightUnits;
    }

    public String getDimentionsUnits() {
        return dimentionsUnits;
    }

    public void setDimentionsUnits(String dimentionsUnits) {
        this.dimentionsUnits = dimentionsUnits;
    }

    public String getPayorType() {
        return PAYOR_TYPE;
    }

    public String getHoldAtLocation() {
        return holdAtLocation;
    }

    public void setHoldAtLocation(String holdAtLocation) {
        this.holdAtLocation = holdAtLocation;
    }

    public String getDryIce() {
        return dryIce;
    }

    public void setDryIce(String dryIce) {
        this.dryIce = dryIce;
    }

    public String getResidentialDelivery() {
        return residentialDelivery;
    }

    public void setResidentialDelivery(String residentialDelivery) {
        this.residentialDelivery = residentialDelivery;
    }

    public String getInsidePickup() {
        return insidePickup;
    }

    public void setInsidePickup(String insidePickup) {
        this.insidePickup = insidePickup;
    }

    public String getInsideDelivery() {
        return insideDelivery;
    }

    public void setInsideDelivery(String insideDelivery) {
        this.insideDelivery = insideDelivery;
    }

    public String getSaturdayPickup() {
        return saturdayPickup;
    }

    public void setSaturdayPickup(String saturdayPickup) {
        this.saturdayPickup = saturdayPickup;
    }

    public String getSaturdayDelivery() {
        return saturdayDelivery;
    }

    public void setSaturdayDelivery(String saturdayDelivery) {
        this.saturdayDelivery = saturdayDelivery;
    }

    public String getFutureDayShipment() {
        return futureDayShipment;
    }

    public void setFutureDayShipment(String futureDayShipment) {
        this.futureDayShipment = futureDayShipment;
    }

    public String getShipTime() {
        return shipTime;
    }

    public void setShipTime(String shipTime) {
        this.shipTime = shipTime;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;

        // Actualizar el carrier code
        if ("FEDEXGROUND".equalsIgnoreCase(service)) carrierCode = "FDXG";
        else if ("GROUNDHOMEDELIVERY".equalsIgnoreCase(service)) carrierCode = "FDXG";
        else carrierCode = "FDXE";
        if ("GROUNDHOMEDELIVERY".equalsIgnoreCase(service)) residentialDelivery = "true";
    }

    public Shipper getOriginAddress() {
        return originAddress;
    }

    public void setOriginAddress(org.store.core.utils.carriers.Shipper originAddress) {
        this.originAddress = originAddress;
    }

    public ShipTo getDestinationAddress() {
        return destinationAddress;
    }

    public void setDestinationAddress(ShipTo destinationAddress) {
        this.destinationAddress = destinationAddress;
    }

    public FedexPackage getPack() {
        return pack;
    }

    public void setPack(FedexPackage pack) {
        this.pack = pack;
    }

    public String getLabelType() {
        return LABEL_TYPE;
    }

    public String getLabelImageType() {
        return LABEL_IMAGE_TYPE;
    }

    public FedexShipResponse execute() throws IOException, SAXException {

        if (shipDate==null || "".equals(shipDate)) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            shipDate = sdf.format(Calendar.getInstance().getTime());
        }
        if (shipTime==null || "".equals(shipTime)) {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
            shipTime = sdf.format(Calendar.getInstance().getTime());
        }

        FedexShipResponse res = null;
        String postBody = getContentXml();
        log.info("FEDEX Ship Request \n" + postBody);

        PostMethod post = new PostMethod(CarrierServiceFedexImpl.URL);
        RequestEntity entity = new StringRequestEntity(postBody, "application/xml", "UTF-8");
        post.setRequestEntity(entity);
        HttpClient httpclient = new HttpClient();
        try {
            int result = httpclient.executeMethod(post);
            System.out.println("Response status code: " + result);
            InputStream stream = post.getResponseBodyAsStream();
            res = new FedexShipResponse(stream);
        } finally {
            post.releaseConnection();
        }

        return res;
    }

    public String getContentXml() {
        VelocityContext context = new VelocityContext();
        context.put("bean", this);
        context.put("tool", new org.store.core.utils.carriers.velocity.CarrierVelocityTool());
        String templateName = "fedex/fedex_sr.vm";
        return VelocityGenerator.getGeneratedXml(context, templateName);
    }

    public void loadProperties(Properties prop) {
        user = new FedexUser(prop);
        if (status == null || "".equals(status)) status = prop.getProperty("status");

        weightUnits = prop.getProperty("WeightUnits");
        dimentionsUnits = prop.getProperty("DimentionsUnits");
        holdAtLocation = prop.getProperty("HoldAtLocation");
        dryIce = prop.getProperty("DryIce");
        residentialDelivery = prop.getProperty("ResidentialDelivery");
        insidePickup = prop.getProperty("InsidePickup");
        insideDelivery = prop.getProperty("InsideDelivery");
        saturdayPickup = prop.getProperty("SaturdayPickup");
        saturdayDelivery = prop.getProperty("SaturdayDelivery");
        futureDayShipment = prop.getProperty("FutureDayShipment");

        if (originAddress==null)  {
            org.store.core.utils.carriers.Address add = new org.store.core.utils.carriers.Address(prop.getProperty("shipper.address.line1"),prop.getProperty("shipper.address.line2"),prop.getProperty("shipper.address.city"),prop.getProperty("shipper.address.state"),prop.getProperty("shipper.address.postalcode"),prop.getProperty("shipper.address.country"));
            originAddress = new Shipper(prop.getProperty("shipper.name"),prop.getProperty("shipper.attention.name"),prop.getProperty("shipper.shipper.number"),prop.getProperty("shipper.phone.number"),prop.getProperty("shipper.email"),add);
        }
    }

}
