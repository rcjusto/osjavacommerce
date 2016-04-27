package org.store.suppliers.dewcommerce;

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
public class DewServiceImpl implements SupplierService {

    public static Logger log = Logger.getLogger(DewServiceImpl.class);
    private Properties properties;

    public DewServiceImpl() {
    }

    public org.store.core.utils.suppliers.AvailabilityResponse requestAvailability(String mfgPartNumber, String sku) {
        DewServicesResponse res = null;
        // generar los xml
        StringBuffer postBody = new StringBuffer();
        postBody.append("<CatalogRequest>");
        postBody.append("<Action>ProductDetail</Action>");
        postBody.append("<Username>").append(properties.get("userId")).append("</Username>");
        postBody.append("<Password>").append(properties.get("password")).append("</Password>");
        if (StringUtils.isNotEmpty(sku)) postBody.append("<PartNumber>").append(sku).append("</PartNumber>");
        else postBody.append("<PartNumber>").append(mfgPartNumber).append("</PartNumber>");
        postBody.append("</CatalogRequest>");

        PostMethod post = new PostMethod(properties.getProperty("url"));
        try {
            post.addParameter("xml", postBody.toString());
            HttpClient httpclient = new HttpClient();
            int result = httpclient.executeMethod(post);
            System.out.println("Response status code: " + result);
            res = new DewServicesResponse(post.getResponseBodyAsString());
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
        result.add(new SupplierProperty("url"));
        return result;
    }

    public String getName() {
        return "DEWCOMMERCE";
    }

    public void setProperties(Properties p) {
        this.properties = p;
    }
}