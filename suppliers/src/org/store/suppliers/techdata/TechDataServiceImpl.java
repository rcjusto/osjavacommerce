package org.store.suppliers.techdata;

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

/**
 * Created by IntelliJ IDEA.
 * User: Rogelio Caballero
 * Date: 14/08/2010
 * Time: 04:09:08 PM
 * To change this template use File | Settings | File Templates.
 */
public class TechDataServiceImpl implements SupplierService {
    public static Logger log = Logger.getLogger(TechDataServiceImpl.class);
    private final String URL_TEST = "http://tdxml.cstenet.com/xmlservlet/";
    private final String URL_LIVE = "https://tdxml.techdata.com/xmlservlet";

    private Properties properties;

    public TechDataServiceImpl() {
    }

    public org.store.core.utils.suppliers.AvailabilityResponse requestAvailability(String mfgPartNumber, String sku) {
        TechDataServicesResponse res;
        // generar los xml
        StringBuffer postBody = new StringBuffer();
        postBody.append("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>");
        postBody.append("<XML_PriceAvailability_Submit>");
        postBody.append("<Header>");
        postBody.append("<UserName>").append(properties.get("userName")).append("</UserName>");
        postBody.append("<Password>").append(properties.get("password")).append("</Password>");
        postBody.append("<ResponseVersion>1.4</ResponseVersion>");
        postBody.append("</Header>");
        postBody.append("<Detail>");
        postBody.append("<LineInfo>");
        if (StringUtils.isNotEmpty(sku)) {
            postBody.append("<RefIDQual>VP</RefIDQual>");
            postBody.append("<RefID>").append(sku).append("</RefID>");
        } else {
            postBody.append("<RefIDQual>MG</RefIDQual>");
            postBody.append("<RefID>").append(mfgPartNumber).append("</RefID>");
        }
        postBody.append("</LineInfo>");
        postBody.append("</Detail>");
        postBody.append("</XML_PriceAvailability_Submit>");
        String url = ("test".equalsIgnoreCase(properties.getProperty("mode"))) ? URL_TEST : URL_LIVE;
        PostMethod post = new PostMethod(url);
        try {
            RequestEntity entity = new StringRequestEntity(postBody.toString(), "application/xml", "UTF-8");
            post.setRequestEntity(entity);
            HttpClient httpclient = new HttpClient();
            int result = httpclient.executeMethod(post);
            System.out.println("Response status code: " + result);
            res = new TechDataServicesResponse(post.getResponseBodyAsString(), properties.getProperty("warehouse"));
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
        result.add(new SupplierProperty("userName"));
        result.add(new SupplierProperty("password"));
        result.add(new SupplierProperty("mode", "live,test"));
        return result;
    }

    public String getName() {
        return "TECHDATA";
    }

    public void setProperties(Properties p) {
        this.properties = p;
    }
}