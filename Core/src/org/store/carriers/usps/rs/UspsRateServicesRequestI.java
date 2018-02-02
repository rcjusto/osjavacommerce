package org.store.carriers.usps.rs;

import org.store.core.utils.carriers.velocity.VelocityGenerator;
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
import java.util.Locale;
import java.util.Properties;

/**
 * Created by IntelliJ IDEA.
 * User: Rogelio Caballero Justo
 * Date: 20-nov-2006
 */
public class UspsRateServicesRequestI {
    private String user;
    private final String URL = "http://production.shippingapis.com/ShippingAPI.dll?API=IntlRate&XML=";
    private final String MAIL_TYPE = "Package";

    private org.store.core.utils.carriers.Address originAddress;
    private org.store.core.utils.carriers.Address destinationAddress;
    private ArrayList<USPSPackage> packages;

    public static Logger log = Logger.getLogger("Carriers");


    public UspsRateServicesRequestI(Properties prop) {
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

    public String getCountry() {
        Locale locale = new Locale("en");
        Locale tmpLocale = new Locale("en", destinationAddress.getCountryCode());
        return tmpLocale.getDisplayCountry(locale);
    }

    public String getMailType() {
        return MAIL_TYPE;
    }

    public UspsRateServicesResponseI execute() throws IOException, SAXException {
        UspsRateServicesResponseI res = null;
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
            res = new UspsRateServicesResponseI(stream);
        } catch (IOException e) {
            throw e;
        } catch (SAXException e) {
            throw e;
        } finally {
            getM.releaseConnection();
        }
        return res;
    }

    public String getContentXml() {
        VelocityContext context = new VelocityContext();
        context.put("bean", this);
        context.put("tool", new org.store.core.utils.carriers.velocity.CarrierVelocityTool());
        String templateName = "usps/usps_rsi.vm";
        return VelocityGenerator.getGeneratedXml(context, templateName);
    }

    public void loadProperties(Properties prop) {
        user = prop.getProperty("user");

        if (originAddress==null) originAddress = new org.store.core.utils.carriers.Address();
        originAddress.setStateProvinceCode(prop.getProperty("shipper.address.state"));
        originAddress.setPostalCode(prop.getProperty("shipper.address.postalcode"));
        originAddress.setCountryCode(prop.getProperty("shipper.address.country"));
    }

}
