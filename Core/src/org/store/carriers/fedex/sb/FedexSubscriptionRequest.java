package org.store.carriers.fedex.sb;

import org.store.carriers.fedex.CarrierServiceFedexImpl;
import org.store.core.utils.carriers.StructuredPhoneNumber;
import org.store.core.utils.carriers.velocity.VelocityGenerator;
import org.store.core.utils.carriers.Address;
import org.store.core.utils.carriers.Shipper;
import org.store.carriers.fedex.common.FedexUser;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.log4j.Logger;
import org.apache.velocity.VelocityContext;

import java.io.InputStream;
import java.util.Properties;

/**
 * Created by IntelliJ IDEA.
 * User: Rogelio Caballero Justo
 * Date: 20-nov-2006
 */
public class FedexSubscriptionRequest {
    private String status;

    private FedexUser user;

    private Shipper shipper;

    public static Logger log = Logger.getLogger("Carriers");


    public FedexSubscriptionRequest(String runStatus, Properties prop) {
        status = runStatus;
        if (prop != null) loadProperties(prop);
    }

    public FedexUser getUser() {
        return user;
    }

    public void setUser(FedexUser user) {
        this.user = user;
    }

    public org.store.core.utils.carriers.Shipper getShipper() {
        return shipper;
    }

    public void setShipper(org.store.core.utils.carriers.Shipper shipper) {
        this.shipper = shipper;
    }

    public FedexSubscriptionResponse execute() throws Exception {
        FedexSubscriptionResponse res = null;
        String postBody = getContentXml();

        log.info("FEDEX Subscription \n" +postBody);
        PostMethod post = new PostMethod(CarrierServiceFedexImpl.URL);
        RequestEntity entity = new StringRequestEntity(postBody, "application/xml", "UTF-8");
        post.setRequestEntity(entity);
        HttpClient httpclient = new HttpClient();
        try {
            int result = httpclient.executeMethod(post);
            System.out.println("Response status code: " + result);
            InputStream stream = post.getResponseBodyAsStream();
            res = new FedexSubscriptionResponse(stream);
        } catch (Exception e) {
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
        String templateName = "fedex/fedex_sb.vm";
        return VelocityGenerator.getGeneratedXml(context, templateName);
    }

    public void loadProperties(Properties prop) {
        user = new FedexUser(prop);
        if (status == null || "".equals(status)) status = prop.getProperty("status");

        if (shipper==null) {
            Address address = new Address(prop.getProperty("shipper.address.line1"),prop.getProperty("shipper.address.line2"),prop.getProperty("shipper.address.city"),prop.getProperty("shipper.address.state"),prop.getProperty("shipper.address.postalcode"),prop.getProperty("shipper.address.country"));
            StructuredPhoneNumber phone = new StructuredPhoneNumber(prop.getProperty("shipper.phone.number"),"-");
            shipper = new Shipper(prop.getProperty("shipper.name"),prop.getProperty("shipper.attention.name"), "",phone.toString("-"),prop.getProperty("shipper.email"),address);
        }
    }

}
