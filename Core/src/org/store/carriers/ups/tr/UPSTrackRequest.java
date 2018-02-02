package org.store.carriers.ups.tr;

import org.store.core.utils.carriers.velocity.CarrierVelocityTool;
import org.store.core.utils.carriers.velocity.VelocityGenerator;
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
public class UPSTrackRequest {

    private String viewPath;
    private String status;
    private UpsUser user;
    private static final String URL_LIVE = "https://www.ups.com/ups.app/xml/Track";
    private static final String URL_TEST = "https://wwwcie.ups.com/ups.app/xml/Track";
    private static final String CUSTOMER_CONTEXT = "Customer Track Request";
    private static final String XPCI_VERSION = "1.0001";
    private static final String REQUEST_ACTION = "Track";
    private static final String REQUEST_OPTION = "none";

    private String shipmentIdentificationNumber;

    private final Logger log = Logger.getLogger("Carriers");


    public UPSTrackRequest(String runStatus, Properties prop) {
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

    public String getShipmentIdentificationNumber() {
        return shipmentIdentificationNumber;
    }

    public void setShipmentIdentificationNumber(String shipmentIdentificationNumber) {
        this.shipmentIdentificationNumber = shipmentIdentificationNumber;
    }

    public UPSTrackResponse execute() throws IOException, SAXException {
        UPSTrackResponse res = null;
        // generar los xml
        String xmlAccess = user.getAccessRequestXml();
        String xmlAddress = getContentXml();
        String postBody = xmlAccess + xmlAddress;
        log.info("UPS Tracking \n" + postBody);

        PostMethod post = new PostMethod(getUrl());
        RequestEntity entity = new StringRequestEntity(postBody, "application/xml", "UTF-8");
        post.setRequestEntity(entity);
        HttpClient httpclient = new HttpClient();
        try {
            int result = httpclient.executeMethod(post);
            System.out.println("Response status code: " + result);
            InputStream stream = post.getResponseBodyAsStream();
            res = new UPSTrackResponse(stream);
        } finally {
            post.releaseConnection();
        }

        return res;
    }

    public String getContentXml() {
        VelocityContext context = new VelocityContext();
        context.put("bean", this);
        context.put("tool", new CarrierVelocityTool());
        String templateName = "ups/ups_tr.vm";
        return VelocityGenerator.getGeneratedXml(context, templateName);
    }

    public void loadProperties(Properties prop) {
        user = new UpsUser(prop);
        if (status==null || "".equals(status)) status = prop.getProperty("status");
    }

}
