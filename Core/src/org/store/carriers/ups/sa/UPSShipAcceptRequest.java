package org.store.carriers.ups.sa;

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
import java.util.Properties;

/**
 * Created by IntelliJ IDEA.
 * User: Rogelio Caballero Justo
 * Date: 20-nov-2006
 */
public class UPSShipAcceptRequest {
    private String status;
    private UpsUser user;
    
    private static final String URL_LIVE = "https://www.ups.com/ups.app/xml/ShipAccept";
    private static final String URL_TEST = "https://wwwcie.ups.com/ups.app/xml/ShipAccept";
    private static final String CUSTOMER_CONTEXT = "Customer Ship Accept";
    private static final String XPCI_VERSION = "1.0001";
    private static final String REQUEST_ACTION = "ShipAccept";
    private static final String REQUEST_OPTION = "01";

    private String shipmentDigest;

    private final Logger log = Logger.getLogger("Carriers");


    public UPSShipAcceptRequest( String runStatus, Properties prop) {
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
        return ("test".equalsIgnoreCase(status)) ? URL_TEST : URL_LIVE;
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

    public String getShipmentDigest() {
        return shipmentDigest;
    }

    public void setShipmentDigest(String shipmentDigest) {
        this.shipmentDigest = shipmentDigest;
    }

    public UPSShipAcceptResponse execute() throws IOException, SAXException {
        UPSShipAcceptResponse res = null;
        // generar los xml
        String xmlAccess = user.getAccessRequestXml();
        String xmlContent = getContentXml();
        String postBody = xmlAccess + xmlContent;

        log.info("UPS ShipAccept \n" + postBody);

        PostMethod post = new PostMethod(getUrl());
        RequestEntity entity = new StringRequestEntity(postBody, "application/xml", "UTF-8");
        post.setRequestEntity(entity);
        HttpClient httpclient = new HttpClient();
        try {
            int result = httpclient.executeMethod(post);
            System.out.println("Response status code: " + result);
            InputStream stream = post.getResponseBodyAsStream();
            res = new UPSShipAcceptResponse(stream);
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
        String templateName = "ups/ups_sa.vm";
        return org.store.core.utils.carriers.velocity.VelocityGenerator.getGeneratedXml(context, templateName);
    }

    public void loadProperties(Properties prop) {
        user = new UpsUser(prop);
        if (status==null || "".equals(status)) status = prop.getProperty("status");
    }

}
