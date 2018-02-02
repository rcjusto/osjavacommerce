package org.store.suppliers.im;

import org.store.core.utils.suppliers.SupplierProperty;
import org.store.core.utils.suppliers.SupplierService;
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

public class IngramMicroServiceImpl implements SupplierService {
    public static Logger log = Logger.getLogger(IngramMicroServiceImpl.class);
    private final String URL = "https://newport.ingrammicro.com/mustang";

    private Properties properties;

    public IngramMicroServiceImpl() {
    }

    public org.store.core.utils.suppliers.AvailabilityResponse requestAvailability(String mfgPartNumber, String sku) {
        IngramMicroServicesResponse res = null;
        // generar los xml
        StringBuffer postBody = new StringBuffer();
        postBody.append("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>");
        postBody.append("<PNARequest>");
        postBody.append("<Version>2.0</Version>");
        postBody.append("<TransactionHeader>");
        postBody.append("<SenderID></SenderID>");
        postBody.append("<ReceiverID></ReceiverID>");
        if (properties.containsKey("countryCode") && StringUtils.isNotEmpty((String) properties.get("countryCode"))) postBody.append("<CountryCode>").append(properties.get("countryCode")).append("</CountryCode>");
        postBody.append("<LoginID>").append(properties.get("loginID")).append("</LoginID>");
        postBody.append("<Password>").append(properties.get("password")).append("</Password>");
        postBody.append("</TransactionHeader>");
        if (StringUtils.isNotEmpty(sku)) postBody.append("<PNAInformation SKU=\"").append(sku).append("\" />");
        else postBody.append("<PNAInformation SKU=\"").append(mfgPartNumber).append("\" />");
    //    postBody.append("<ShowDetail>1</ShowDetail>");
        postBody.append("</PNARequest>");

        PostMethod post = new PostMethod(URL);
        try {
            RequestEntity entity = new StringRequestEntity(postBody.toString(), "application/xml", "UTF-8");
            post.setRequestEntity(entity);
        //    post.setRequestBody(postBody.toString());
            HttpClient httpclient = new HttpClient();
            int result = httpclient.executeMethod(post);
            System.out.println("Response status code: " + result);
            res = new IngramMicroServicesResponse(post.getResponseBodyAsString(), properties.getProperty("warehouse"));
            return res.getResponse();
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        } finally {
            post.releaseConnection();
        }

        return null;
    }

    public List<SupplierProperty> getConfigurationParameters() {
        List<SupplierProperty> result = new ArrayList<SupplierProperty>();
        //    result.add(new SupplierProperty("senderId"));
        //    result.add(new SupplierProperty("receiverId"));
        result.add(new SupplierProperty("loginID"));
        result.add(new SupplierProperty("password"));
        result.add(new SupplierProperty("countryCode"));
        return result;
    }

    public String getName() {
        return "INGRAM MICRO";
    }

    public void setProperties(Properties p) {
        this.properties = p;
    }
}
