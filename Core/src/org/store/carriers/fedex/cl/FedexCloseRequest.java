package org.store.carriers.fedex.cl;

import org.store.carriers.fedex.CarrierServiceFedexImpl;
import org.store.core.utils.carriers.velocity.CarrierVelocityTool;
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
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;

/**
 * Created by IntelliJ IDEA.
 * User: Rogelio Caballero Justo
 * Date: 20-nov-2006
 */
public class FedexCloseRequest {
   private String status;
    private FedexUser user;

    private Date date;
    private String shipDate;
    private String shipTime;
    private String carrierCode;

    public static Logger log = Logger.getLogger("Carriers");


    public FedexCloseRequest(  String runStatus, Properties prop) {
        status = runStatus;
        if (prop != null) loadProperties(prop);
    }

    public FedexUser getUser() {
        return user;
    }

    public void setUser(FedexUser user) {
        this.user = user;
    }

    public String getCarrierCode() {
        return carrierCode;
    }

    public void setCarrierCode(String carrierCode) {
        this.carrierCode = carrierCode;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getShipDate() {
        return shipDate;
    }

    public String getShipTime() {
        return shipTime;
    }

    public String getDateFN() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyMMddHHmm");
        if (date==null) date = Calendar.getInstance().getTime();
        return sdf.format(date);
    }

    public FedexCloseResponse execute() throws IOException, SAXException {

        if (date==null) date = Calendar.getInstance().getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        shipDate = sdf.format(date);
        SimpleDateFormat sdt = new SimpleDateFormat("HH:mm:ss");
        shipTime = sdt.format(date);

        FedexCloseResponse res = null;
        String postBody = getContentXml();
        log.info("FEDEX Close Request \n" + postBody);

        PostMethod post = new PostMethod(CarrierServiceFedexImpl.URL);
        RequestEntity entity = new StringRequestEntity(postBody, "application/xml", "UTF-8");
        post.setRequestEntity(entity);
        HttpClient httpclient = new HttpClient();
        try {
            int result = httpclient.executeMethod(post);
            System.out.println("Response status code: " + result);
            InputStream stream = post.getResponseBodyAsStream();
            res = new FedexCloseResponse(stream);
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
        context.put("tool", new CarrierVelocityTool());
        String templateName = "fedex/fedex_close.vm";
        return VelocityGenerator.getGeneratedXml(context, templateName);
    }

    public void loadProperties(Properties prop) {
        user = new FedexUser(prop);
        if (status == null || "".equals(status)) status = prop.getProperty("status");
        carrierCode = prop.getProperty("CarrierCode");
    }

}
