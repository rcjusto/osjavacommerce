package org.store.carriers.capost.rs;

import org.store.carriers.capost.common.CaPostPackage;
import org.store.carriers.capost.common.CaPostUser;
import org.store.core.utils.carriers.ShipTo;
import org.store.core.utils.carriers.Shipper;
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
public class CaPostRateServicesRequest {

    private String status;
    private CaPostUser user;
    private final String URL = "http://sellonline.canadapost.ca:30000";
    private final String TURN_AROUND_TIME = "24";
    private String language;
    private final boolean INSURANCE_CALCULATION = true;

    private org.store.core.utils.carriers.Shipper shipper;
    private org.store.core.utils.carriers.ShipTo shipTo;
    private ArrayList<CaPostPackage> packages;

    public static Logger log = Logger.getLogger("Carriers");


    public CaPostRateServicesRequest(String runStatus, Properties prop) {
        status = runStatus;
        if (prop != null) loadProperties(prop);
    }

    public CaPostUser getUser() {
        return user;
    }

    public void setUser(CaPostUser user) {
        this.user = user;
    }

    public String getUrl() {
        return URL;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getTurnAroundTime() {
        return TURN_AROUND_TIME;
    }

    public boolean getInsuranceCalculation() {
        return INSURANCE_CALCULATION;
    }

    public Shipper getShipper() {
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

    public ArrayList<CaPostPackage> getPackages() {
        return packages;
    }

    public void setPackages(ArrayList<CaPostPackage> packages) {
        this.packages = packages;
    }

    public CaPostRateServicesResponse execute() throws IOException, SAXException {
        CaPostRateServicesResponse res = null;
        // generar los xml
        String postBody = getContentXml();

        log.info("Canada Post Rate Services \n" +postBody);

        PostMethod post = new PostMethod(getUrl());
        RequestEntity entity = new StringRequestEntity(postBody, "application/xml", "UTF-8");
        post.setRequestEntity(entity);
        HttpClient httpclient = new HttpClient();
        try {
            int result = httpclient.executeMethod(post);
            System.out.println("Response status code: " + result);
            InputStream stream = post.getResponseBodyAsStream();
            res = new CaPostRateServicesResponse(stream);
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
        String templateName = "capost/capost_rs.vm";
        return org.store.core.utils.carriers.velocity.VelocityGenerator.getGeneratedXml(context, templateName);
    }

    public void loadProperties(Properties prop) {
        user = new CaPostUser(prop);
        if (status==null || "".equals(status)) status = prop.getProperty("status");
        shipper = new Shipper(prop);
    }


    public void addPackage(CaPostPackage pack) {
        if (packages == null) setPackages(new ArrayList<CaPostPackage>());
        packages.add(pack);
    }

}