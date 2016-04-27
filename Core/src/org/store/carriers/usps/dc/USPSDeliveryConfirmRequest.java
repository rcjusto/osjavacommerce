package org.store.carriers.usps.dc;

import org.store.core.utils.carriers.ShipTo;
import org.store.core.utils.carriers.velocity.CarrierVelocityTool;
import org.store.core.utils.carriers.Address;
import org.store.core.utils.carriers.Shipper;
import org.store.carriers.usps.common.USPSPackage;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.log4j.Logger;
import org.apache.velocity.VelocityContext;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import static java.lang.Integer.parseInt;

/**
 * Created by IntelliJ IDEA.
 * User: Rogelio Caballero Justo
 * Date: 20-nov-2006
 */
public class USPSDeliveryConfirmRequest {
    private String user;
    private final String URL = "https://secure.shippingapis.com/ShippingAPI.dll?API=DeliveryConfirmationV3&XML=";

    private final String OPTION = "1";
    private final String IMAGE_TYPE = "PDF";
    private String serviceType;

    private Shipper originAddress;
    private ShipTo destinationAddress;
    private USPSPackage packages;

    private final Logger log = Logger.getLogger("Carriers" );


    public USPSDeliveryConfirmRequest(Properties prop) {
        if (prop != null) loadProperties(prop);
    }

    public String getUrl() {
        return URL;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getOption() {
        return OPTION;
    }

    public String getImageType() {
        return IMAGE_TYPE;
    }

    public String getServiceType() {
        if (serviceType.startsWith("Express Mail")) return "Express";
        if (serviceType.startsWith("Priority")) return "Priority";
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    public String getService() {
        return serviceType;
    }

    public Shipper getOriginAddress() {
        return originAddress;
    }

    public void setOriginAddress(Shipper originAddress) {
        this.originAddress = originAddress;
    }

    public ShipTo getDestinationAddress() {
        return destinationAddress;
    }

    public void setDestinationAddress(ShipTo destinationAddress) {
        this.destinationAddress = destinationAddress;
    }

    public USPSPackage getPackages() {
        return packages;
    }

    public void setPackages(USPSPackage packages) {
        this.packages = packages;
    }


    public int getPackageWeithInOunces() {
        if (packages==null) return 0;
        Integer p = strToInteger(packages.getPounds());
        Integer o = strToInteger(packages.getOnces());
        return ((p!=null)?(p*16):0) + ((o!=null)?o:0);
    }

    public USPSDeliveryConfirmResponse execute() throws IOException, SAXException {
        USPSDeliveryConfirmResponse res = null;
        String content = getContentXml();
        log.info("USPS DeliveryConfirm \n" + content);
        URI uri = new URI(getUrl() + content, false);
        String finalURL = uri.getEscapedURI();
        GetMethod getM = new GetMethod(finalURL);
        HttpClient httpclient = new HttpClient();
        try {
            int result = httpclient.executeMethod(getM);
            System.out.println("Response status code: " + result);
            InputStream stream = getM.getResponseBodyAsStream();
            res = new USPSDeliveryConfirmResponse(stream);
        } catch (IOException e) {
            log.error(e);
            throw e;
        } catch (SAXException e) {
            log.error(e);
            throw e;
        } finally {
            getM.releaseConnection();
        }

        return res;
    }

    public String getContentXml() {
        VelocityContext context = new VelocityContext();
        context.put("bean", this);
        context.put("tool", new CarrierVelocityTool());
        String templateName = "usps/usps_dc.vm";
        return org.store.core.utils.carriers.velocity.VelocityGenerator.getGeneratedXml(context, templateName);
    }

    public void loadProperties(Properties prop) {
        user = prop.getProperty("user");

        if (originAddress==null) {
            Address oa = new Address(prop.getProperty("shipper.address.line1"),prop.getProperty("shipper.address.line2"),prop.getProperty("shipper.address.city"),prop.getProperty("shipper.address.state"),prop.getProperty("shipper.address.postalcode"),prop.getProperty("shipper.address.country"));
            originAddress = new org.store.core.utils.carriers.Shipper(prop.getProperty("shipper.name"),prop.getProperty("shipper.attention.name"),"",prop.getProperty("shipper.phone.number"), prop.getProperty("shipper.email"), oa);
        }
    }

    public static Integer strToInteger(String cad) {
        if (cad == null) return null;
        Integer res;
        try {
            res = parseInt(cad);
        }
        catch (NumberFormatException nfe) {
            res = null;
        }
        return res;
    }

    
}
