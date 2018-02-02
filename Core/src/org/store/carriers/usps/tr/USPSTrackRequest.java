package org.store.carriers.usps.tr;

import org.store.core.utils.carriers.velocity.CarrierVelocityTool;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.log4j.Logger;
import org.apache.velocity.VelocityContext;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by IntelliJ IDEA.
 * User: Rogelio Caballero Justo
 * Date: 20-nov-2006
 */
public class USPSTrackRequest {
    private String user;
    private final String URL = "http://production.shippingapis.com/ShippingAPI.dll?API=TrackV2&XML=";

    private String shipmentIdentificationNumber;

    private final Logger log = Logger.getLogger("Carriers");


    public USPSTrackRequest(Properties prop) {
        if (prop != null) loadProperties(prop);
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getUrl() {
        return URL;
    }

    public String getShipmentIdentificationNumber() {
        return shipmentIdentificationNumber;
    }

    public void setShipmentIdentificationNumber(String shipmentIdentificationNumber) {
        this.shipmentIdentificationNumber = shipmentIdentificationNumber;
    }

    public USPSTrackResponse execute() throws IOException, SAXException {
        USPSTrackResponse res = null;
        // generar los xml
        String cXml = getContentXml();
        URI uri = new URI(getUrl() + cXml, false);
        log.info("USPS Rate Services - " + cXml);
        String finalURL = uri.getEscapedURI();
        GetMethod getM = new GetMethod(finalURL);
        HttpClient httpclient = new HttpClient();
        try {
            int result = httpclient.executeMethod(getM);
            System.out.println("Response status code: " + result);
            InputStream stream = getM.getResponseBodyAsStream();
            res = new USPSTrackResponse(stream);
        } finally {
            getM.releaseConnection();
        }
        return res;
    }

    public String getContentXml() {
        VelocityContext context = new VelocityContext();
        context.put("bean", this);
        context.put("tool", new CarrierVelocityTool());
        String templateName = "usps/usps_tr.vm";
        return org.store.core.utils.carriers.velocity.VelocityGenerator.getGeneratedXml(context, templateName);
    }

    public void loadProperties(Properties prop) {
        user = prop.getProperty("user");
    }

}
