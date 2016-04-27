package org.store.carriers.fedex.tr;

import org.store.carriers.fedex.CarrierServiceFedexImpl;
import org.store.core.utils.carriers.velocity.VelocityGenerator;
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
import java.util.Properties;

/**
 * Created by IntelliJ IDEA.
 * User: Rogelio Caballero Justo
 * Date: 20-nov-2006
 */
public class FedexTrackRequest {

    private String status;
    private FedexUser user;
    private String trackingNumber;
    private String shipMethod;
    private String carrierCode;
    private final String DETAIL_SCAN = "0";

    public static Logger log = Logger.getLogger("Carriers");


    public FedexTrackRequest(String runStatus, Properties prop) {
        status = runStatus;
        if (prop != null) loadProperties(prop);
    }

    public FedexUser getUser() {
        return user;
    }

    public void setUser(FedexUser user) {
        this.user = user;
    }

    public String getTrackingNumber() {
        return trackingNumber;
    }

    public void setTrackingNumber(String trackingNumber) {
        this.trackingNumber = trackingNumber;
    }

    public String getCarrierCode() {
        return carrierCode;
    }

    public void setCarrierCode(String carrierCode) {
        this.carrierCode = carrierCode;
    }

    public String getShipMethod() {
        return shipMethod;
    }

    public void setShipMethod(String shipMethod) {
        this.shipMethod = shipMethod;
        if ("FEDEXGROUND".equalsIgnoreCase(shipMethod)) carrierCode = "FDXG";
        else if ("GROUNDHOMEDELIVERY".equalsIgnoreCase(shipMethod)) carrierCode = "FDXG";
        else carrierCode = "FDXE";
    }

    public String getDetailScan() {
        return DETAIL_SCAN;
    }

    public FedexTrackResponse execute() throws IOException, SAXException {
        FedexTrackResponse res = null;
        // generar los xml
        String xmlAccess = user.getAccessRequestXml();
        String xmlAddress = getContentXml();
        String postBody = xmlAccess + xmlAddress;

        log.info("FEDEX Void \n" + postBody);

        PostMethod post = new PostMethod(CarrierServiceFedexImpl.URL);
        RequestEntity entity = new StringRequestEntity(postBody, "application/xml", "UTF-8");
        post.setRequestEntity(entity);
        HttpClient httpclient = new HttpClient();
        try {
            int result = httpclient.executeMethod(post);
            System.out.println("Response status code: " + result);
            InputStream stream = post.getResponseBodyAsStream();
            res = new FedexTrackResponse(stream);
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
        context.put("tool", new org.store.core.utils.carriers.velocity.CarrierVelocityTool());
        String templateName = "fedex/fedex_tr.vm";
        return VelocityGenerator.getGeneratedXml(context, templateName);
    }



    public void loadProperties(Properties prop) {
        user = new FedexUser(prop);
        if (status == null || "".equals(status)) status = prop.getProperty("status");

    }

}
