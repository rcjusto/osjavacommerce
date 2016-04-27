package org.store.carriers.usps.rs;

import org.store.core.utils.carriers.velocity.CarrierVelocityTool;
import org.store.carriers.usps.common.USPSPackage;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.log4j.Logger;
import org.apache.velocity.VelocityContext;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Properties;

/**
 * Created by IntelliJ IDEA.
 * User: Rogelio Caballero Justo
 * Date: 20-nov-2006
 */
public class UspsRateServicesRequestD {
    private String user;
    private final String URL = "http://production.shippingapis.com/ShippingAPI.dll?API=RateV2&XML=";
    private String services = "All";

    private org.store.core.utils.carriers.Address originAddress;
    private org.store.core.utils.carriers.Address destinationAddress;
    private ArrayList<USPSPackage> packages;

    public static Logger log = Logger.getLogger("Carriers");


    public UspsRateServicesRequestD(Properties prop) {
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

    public org.store.core.utils.carriers.Address getOriginAddress() {
        return originAddress;
    }

    public void setOriginAddress(org.store.core.utils.carriers.Address originAddress) {
        this.originAddress = originAddress;
    }

    public org.store.core.utils.carriers.Address getDestinationAddress() {
        return destinationAddress;
    }

    public void setDestinationAddress(org.store.core.utils.carriers.Address destinationAddress) {
        this.destinationAddress = destinationAddress;
    }


    public ArrayList<USPSPackage> getPackages() {
        return packages;
    }

    public void setPackages(ArrayList<USPSPackage> packages) {
        this.packages = packages;
    }

    public String getServices() {
        return services;
    }

    public void setServices(String services) {
        this.services = services;
    }

    public UspsRateServicesResponseD execute() throws IOException, SAXException {
        UspsRateServicesResponseD res = null;
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
            res = new UspsRateServicesResponseD(stream);
        } finally {
            getM.releaseConnection();
        }
        return res;
    }

    public String getContentXml() {
        VelocityContext context = new VelocityContext();
        context.put("bean", this);
        context.put("tool", new CarrierVelocityTool());
        String templateName = "usps/usps_rsd.vm";
        return org.store.core.utils.carriers.velocity.VelocityGenerator.getGeneratedXml(context, templateName);
    }

    public void loadProperties(Properties prop) {
        user = prop.getProperty("user");

        if (originAddress==null) originAddress = new org.store.core.utils.carriers.Address();
        originAddress.setStateProvinceCode(prop.getProperty("shipper.address.state"));
        originAddress.setPostalCode(prop.getProperty("shipper.address.postalcode"));
        originAddress.setCountryCode(prop.getProperty("shipper.address.country"));
    }

}
