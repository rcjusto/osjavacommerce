package org.store.suppliers.techdata;

import org.store.core.globals.SomeUtils;
import org.store.core.utils.suppliers.SupplierProperty;
import org.store.core.utils.suppliers.SupplierService;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.StringPart;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Created by IntelliJ IDEA.
 * User: Rogelio Caballero
 * Date: 14/08/2010
 * Time: 04:09:08 PM
 * To change this template use File | Settings | File Templates.
 */
public class TechDataEuropeServiceImpl implements SupplierService {
    public static Logger log = Logger.getLogger(TechDataEuropeServiceImpl.class);
    private final String URL_TEST = "http://intcom.xml.quality.techdata.de:8080/Onlchk";
    private final String URL_LIVE = "http://intcom.xml.techdata-europe.com:8080/Onlchk";

    private Properties properties;

    public TechDataEuropeServiceImpl() {
    }

    public org.store.core.utils.suppliers.AvailabilityResponse requestAvailability(String mfgPartNumber, String sku) {
        TechDataEuropeServicesResponse res;
        // generar los xml
        Integer quantity = SomeUtils.strToInteger(properties.getProperty("quantity"));
        if (quantity==null) quantity = 1;
        StringBuffer postBody = new StringBuffer();
        postBody.append("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>");
        postBody.append("<OnlineCheck>");
        postBody.append("<Header>");
        postBody.append("<BuyerAccountId>").append(properties.get("accountId")).append("</BuyerAccountId>");
        postBody.append("<AuthCode>").append(properties.get("authorizationCode")).append("</AuthCode>");
        postBody.append("<Type>Full</Type>");
        postBody.append("</Header>");
        postBody.append("<Item line=\"1\">");
        if (StringUtils.isNotEmpty(sku)) {
            postBody.append("<ManufacturerItemIdentifier/>");
            postBody.append("<DistributorItemIdentifier>").append(sku).append("</DistributorItemIdentifier>");
            postBody.append("<Quantity>").append(quantity.toString()).append("</Quantity>");
        } else {
            postBody.append("<ManufacturerItemIdentifier>").append(mfgPartNumber).append("</ManufacturerItemIdentifier>");
            postBody.append("<DistributorItemIdentifier/>");
            postBody.append("<Quantity>").append(quantity.toString()).append("</Quantity>");
        }
        postBody.append("</Item>");
        postBody.append("</OnlineCheck>");
        String url = ("test".equalsIgnoreCase(properties.getProperty("mode"))) ? URL_TEST : URL_LIVE;
        PostMethod post = new PostMethod(url);
        try {
            Part[] parts = {new StringPart("onlinecheck",postBody.toString())};
            MultipartRequestEntity entity = new MultipartRequestEntity(parts, post.getParams());
            post.setRequestEntity(entity);
            HttpClient httpclient = new HttpClient();
            int result = httpclient.executeMethod(post);
            System.out.println("Response status code: " + result);
            res = new TechDataEuropeServicesResponse(post.getResponseBodyAsString());
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
        result.add(new SupplierProperty("accountId"));
        result.add(new SupplierProperty("authorizationCode"));
        result.add(new SupplierProperty("quantity"));
        result.add(new SupplierProperty("mode", "live,test"));
        return result;
    }

    public String getName() {
        return "TECHDATA-EUROPE";
    }

    public void setProperties(Properties p) {
        this.properties = p;
    }
}