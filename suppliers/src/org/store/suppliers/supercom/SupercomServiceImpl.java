package org.store.suppliers.supercom;

import org.store.core.utils.suppliers.SupplierProperty;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;


public class SupercomServiceImpl implements org.store.core.utils.suppliers.SupplierService {

    public static Logger log = Logger.getLogger(SupercomServiceImpl.class);
    private final String URL_CANADA = "http://xml3.supercom.ca/XMLWebService3.asmx/XMLCall";

    private Properties properties;

    public SupercomServiceImpl() {
    }

    public org.store.core.utils.suppliers.AvailabilityResponse requestAvailability(String mfgPartNumber, String sku) {
        SupercomServicesResponse res = null;
        // generar los xml
        StringBuffer postBody = new StringBuffer();
        postBody.append("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>");
        postBody.append("<PNARequest>");
        postBody.append("<Version>3.0</Version>");
        postBody.append("<Header>");
        postBody.append("<UserName>").append(properties.get("userName")).append("</UserName>");
        postBody.append("<Password>").append(properties.get("password")).append("</Password>");
        postBody.append("</Header>");
        postBody.append("<Detail>");
        if (StringUtils.isNotEmpty(sku)) postBody.append("<PartNumber>").append(sku).append("</PartNumber>");
        else postBody.append("<PartNumber>").append(mfgPartNumber).append("</PartNumber>");
        postBody.append("</Detail>");
        postBody.append("</PNARequest>");

        PostMethod post = new PostMethod(URL_CANADA);
        try {
            RequestEntity entity = new StringRequestEntity("str="+postBody.toString(), "application/x-www-form-urlencoded", "UTF-8");
            post.setRequestEntity(entity);
            HttpClient httpclient = new HttpClient();
            int result = httpclient.executeMethod(post);
            System.out.println("Response status code: " + result);
            res = new SupercomServicesResponse(post.getResponseBodyAsString(), properties.getProperty("warehouse"));
            return res.getResponse();
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        } finally {
            post.releaseConnection();
        }

        return null;    }

    public List<SupplierProperty> getConfigurationParameters() {
        List<SupplierProperty> result = new ArrayList<SupplierProperty>();
        result.add(new SupplierProperty("userName"));
        result.add(new SupplierProperty("password"));
        return result;
    }

    public String getName() {
        return "SUPERCOM";
    }

    public void setProperties(Properties p) {
        this.properties = p;
    }
}
