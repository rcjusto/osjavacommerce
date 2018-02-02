package org.store.suppliers.dandh;

import org.store.core.utils.suppliers.SupplierProperty;
import org.store.core.utils.suppliers.SupplierService;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Created by IntelliJ IDEA.
 * User: Administrador
 * Date: 14/08/2010
 * Time: 04:09:08 PM
 * To change this template use File | Settings | File Templates.
 */
public class DandHServiceImpl implements SupplierService {
    public static Logger log = Logger.getLogger(DandHServiceImpl.class);
    private final String URL = "https://www.dandh.ca/dhXML/xmlDispatch";

    private Properties properties;

    public DandHServiceImpl() {
    }

    public org.store.core.utils.suppliers.AvailabilityResponse requestAvailability(String mfgPartNumber, String sku) {
        DandHServicesResponse res = null;
        // generar los xml
        StringBuffer postBody = new StringBuffer();
        postBody.append("<XMLFORMPOST>");
        postBody.append("<REQUEST>price-availability</REQUEST>");
        postBody.append("<LOGIN>");
        postBody.append("<USERID>").append(properties.get("userId")).append("</USERID>");
        postBody.append("<PASSWORD>").append(properties.get("password")).append("</PASSWORD>");
        postBody.append("</LOGIN>");
        if (StringUtils.isNotEmpty(sku)) postBody.append("<PARTNUM>").append(sku).append("</PARTNUM>");
        else postBody.append("<PARTNUM>").append(mfgPartNumber).append("</PARTNUM>");
        postBody.append("</XMLFORMPOST>");

        PostMethod post = new PostMethod(URL);
        try {
            post.addParameter("xmlDoc", postBody.toString());
            HttpClient httpclient = new HttpClient();
            int result = httpclient.executeMethod(post);
            System.out.println("Response status code: " + result);
            res = new DandHServicesResponse(post.getResponseBodyAsString(), properties.getProperty("warehouse"));
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
        result.add(new SupplierProperty("userId"));
        result.add(new SupplierProperty("password"));
        return result;
    }

    public String getName() {
        return "DandH";
    }

    public void setProperties(Properties p) {
        this.properties = p;
    }
}