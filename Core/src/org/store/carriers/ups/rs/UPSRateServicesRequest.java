package org.store.carriers.ups.rs;

import org.store.core.utils.carriers.ShipTo;
import org.store.core.utils.carriers.velocity.CarrierVelocityTool;
import org.store.core.utils.carriers.velocity.VelocityGenerator;
import org.store.core.utils.carriers.Shipper;
import org.store.carriers.ups.common.PackageProps;
import org.store.carriers.ups.common.UPSPackage;
import org.store.carriers.ups.common.UpsUser;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;
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
public class UPSRateServicesRequest {

    private String viewPath;
    private String status;
    private UpsUser user;

    private Shipper shipper;
    private ShipTo shipTo;
    private ArrayList<UPSPackage> packages;
    private PackageProps packProps;

    private boolean saturdayDeliveryIndicator;

    public static Logger log = Logger.getLogger("Carriers");
    private static final String URL_LIVE = "https://www.ups.com/ups.app/xml/Rate";
    private static final String URL_TEST = "https://wwwcie.ups.com/ups.app/xml/Rate";
    private static final String CUSTOMER_CONTEXT = "Customer Rate Services Selection";
    private static final String XPCI_VERSION = "1.0001";
    private static final String REQUEST_ACTION = "Rate";
    private static final String REQUEST_OPTION = "shop";
    private static final String PICKUP_TYPE = "01";
    private static final String CUSTOMER_CLASSIFICATION = "01";

    public UPSRateServicesRequest(String runStatus, Properties prop) {
        status = runStatus;
        if (prop != null) loadProperties(prop);
    }

    public UpsUser getUser() {
        return user;
    }

    public void setUser(UpsUser user) {
        this.user = user;
    }

    public String getUrl() {
        if ("test".equalsIgnoreCase(status)) return URL_TEST;
        else return URL_LIVE;
    }


    public String getCustomerContext() {
        return CUSTOMER_CONTEXT;
    }


    public String getXpciVersion() {
        return XPCI_VERSION;
    }

    public String getRequestAction() {
        return REQUEST_ACTION;
    }

    public String getRequestOption() {
        return REQUEST_OPTION;
    }

    public String getPickupType() {
        return PICKUP_TYPE;
    }

    public String getCustomerClassification() {
        return CUSTOMER_CLASSIFICATION;
    }

    public org.store.core.utils.carriers.Shipper getShipper() {
        return shipper;
    }

    public void setShipper(org.store.core.utils.carriers.Shipper shipper) {
        this.shipper = shipper;
    }

    public ShipTo getShipTo() {
        return shipTo;
    }

    public void setShipTo(ShipTo shipTo) {
        this.shipTo = shipTo;
    }

    public ArrayList<UPSPackage> getPackages() {
        return packages;
    }

    public void setPackages(ArrayList<UPSPackage> packages) {
        this.packages = packages;
    }

    public boolean isSaturdayDeliveryIndicator() {
        return saturdayDeliveryIndicator;
    }

    public void setSaturdayDeliveryIndicator(boolean saturdayDeliveryIndicator) {
        this.saturdayDeliveryIndicator = saturdayDeliveryIndicator;
    }

    public PackageProps getPackProps() {
        return packProps;
    }

    public void setPackProps(PackageProps packProps) {
        this.packProps = packProps;
    }

    public UPSRateServicesResponse execute() throws IOException, SAXException {
        UPSRateServicesResponse res = null;
        // generar los xml
        String xmlAccess = user.getAccessRequestXml();
        String xmlAddress = getContentXml();
        String postBody = xmlAccess + xmlAddress;

        log.info("UPS Rate Services \n" +postBody);

        PostMethod post = new PostMethod(getUrl());
        RequestEntity entity = new StringRequestEntity(postBody, "application/xml", "UTF-8");
        post.setRequestEntity(entity);
        HttpClient httpclient = new HttpClient();
        try {
            int result = httpclient.executeMethod(post);
            System.out.println("Response status code: " + result);
            InputStream stream = post.getResponseBodyAsStream();
            res = new UPSRateServicesResponse(stream);
        } catch (IOException e) {
            log.error(e.getMessage());
            throw e;
        } catch (SAXException e) {
            log.error(e.getMessage());
            throw e;
        } finally {
            post.releaseConnection();
        }

        return res;
    }

    public String getContentXml() {
        VelocityContext context = new VelocityContext();
        context.put("bean", this);
        context.put("tool", new CarrierVelocityTool());
        String templateName = "ups/ups_rss.vm";
        return VelocityGenerator.getGeneratedXml(context, templateName);
    }

    public void loadProperties(Properties prop) {
        user = new UpsUser(prop);
        if (status==null || "".equals(status)) status = prop.getProperty("status");
        saturdayDeliveryIndicator = "true".equalsIgnoreCase(prop.getProperty("rs.saturday.delivery"));
        shipper = new org.store.core.utils.carriers.Shipper(prop);
        packProps = new PackageProps(prop);
    }


    public void addPackage(UPSPackage pack) {
        if (packages == null) setPackages(new ArrayList<UPSPackage>());
        packages.add(pack);
    }

    public float getTotalWeight() {
        float res = 0;
        if (packages != null) {
            for (UPSPackage p : packages) {
                res += p.getPackageWeight();
            }
        }
        return res;
    }

}
