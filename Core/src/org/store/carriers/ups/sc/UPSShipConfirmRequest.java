package org.store.carriers.ups.sc;

import org.store.core.utils.carriers.ShipTo;
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
public class UPSShipConfirmRequest {
    private String viewPath;
    private String status;
    private UpsUser user;
    
    private static final String URL_LIVE = "https://www.ups.com/ups.app/xml/ShipConfirm";
    private static final String URL_TEST = "https://wwwcie.ups.com/ups.app/xml/ShipConfirm";
    private static final String CUSTOMER_CONTEXT = "Customer Ship Confirm";
    private static final String XPCI_VERSION = "1.0001";
    private static final String REQUEST_ACTION = "ShipConfirm";
    private static final String REQUEST_OPTION = "nonvalidate";
    private static final String LABEL_PRINT_METHOD = "GIF";
    private static final String LABEL_IMAGE_FORMAT = "GIF";

    private String serviceCode;

    private Shipper shipper;
    private ShipTo shipTo;
    private ArrayList<UPSPackage> packages;
    private PackageProps packProps;

    private boolean saturdayDeliveryIndicator;

    private final Logger log = Logger.getLogger("Carriers");


    public UPSShipConfirmRequest(String runStatus, Properties prop) {
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
        return ("test".equalsIgnoreCase(status)) ? URL_TEST: URL_LIVE;
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

    public Shipper getShipper() {
        return shipper;
    }

    public void setShipper(Shipper shipper) {
        this.shipper = shipper;
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


    public String getLabelPrintMethod() {
        return LABEL_PRINT_METHOD;
    }

    public String getLabelImageFormat() {
        return LABEL_IMAGE_FORMAT;
    }

    public String getServiceCode() {
        return serviceCode;
    }

    public void setServiceCode(String serviceCode) {
        this.serviceCode = serviceCode;
    }


    public ShipTo getShipTo() {
        return shipTo;
    }

    public void setShipTo(ShipTo shipTo) {
        this.shipTo = shipTo;
    }

    public float getInvoiceLineTotal() {
        float res = 0;
        if (packages != null) {
            for (UPSPackage p : packages) {
                res += p.getInsuredValueMonetaryValue();
            }
        }
        return res;
    }

    public String getCurrencyCode() {
        String res = "USD";
        if (packages != null && packages.size()>0) {
            res = packages.get(0).getCurrencyCode();
        }
        return res;
    }

    public UPSShipConfirmResponse execute() throws IOException, SAXException {
        UPSShipConfirmResponse res = null;
        // generar los xml
        String xmlAccess = user.getAccessRequestXml();
        String xmlAddress = getContentXml();
        String postBody = xmlAccess + xmlAddress;

        log.info("UPS ShipConfirm \n" + postBody);

        PostMethod post = new PostMethod(getUrl());
        RequestEntity entity = new StringRequestEntity(postBody, "application/xml", "UTF-8");
        post.setRequestEntity(entity);
        HttpClient httpclient = new HttpClient();
        try {
            int result = httpclient.executeMethod(post);
            System.out.println("Response status code: " + result);
            InputStream stream = post.getResponseBodyAsStream();
            res = new UPSShipConfirmResponse(stream);
        } catch (IOException e) {
            throw e;
        } catch (SAXException e) {
            throw e;
        } finally {
            post.releaseConnection();
        }

        return res;
    }

    public String getContentXml() {
        VelocityContext context = new VelocityContext();
        context.put("bean", this);
        context.put("tool", new org.store.core.utils.carriers.velocity.CarrierVelocityTool());
        String templateName = "ups/ups_sc.vm";
        return VelocityGenerator.getGeneratedXml(context, templateName);
    }

    public void loadProperties(Properties prop) {
        user = new UpsUser(prop);
        if (status==null || "".equals(status)) status = prop.getProperty("status");
        saturdayDeliveryIndicator = "true".equalsIgnoreCase(prop.getProperty("sa.saturday.delivery"));
        shipper = new Shipper(prop);
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
