package org.store.icecat;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.log4j.Logger;

import java.io.IOException;

/**
 * Rogelio Caballero
 * 24/07/11 3:11
 */
public class RealTimeRequest {

    public static Logger log = Logger.getLogger(RealTimeRequest.class);
    private static final String URL = "http://data.icecat.biz/xml_s3/xml_server3.cgi?prod_id=%s;vendor=%s;lang=%s;output=productxml";

    private String manufacturer;
    private String productId;
    private String language;

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getUrl() {
        return String.format(URL, getProductId(), getManufacturer(), getLanguage());
    }

    public RealTimeResponse execute() {
        RealTimeResponse res = null;
        GetMethod method = new GetMethod(getUrl());
        HttpClient httpclient = new HttpClient();
        Credentials defaultcreds = new UsernamePasswordCredentials("rcjusto", "rcjrcjrcj");
        httpclient.getState().setCredentials(new AuthScope("data.icecat.biz", 80, AuthScope.ANY_REALM), defaultcreds);
        try {
            int result = httpclient.executeMethod(method);
            System.out.println("Response status code: " + result);
            String xmlResponse = method.getResponseBodyAsString();
            res = new RealTimeResponse(xmlResponse);
        } catch (HttpException e) {
            log.error(e.getMessage(), e); 
        } catch (IOException e) {
            log.error(e.getMessage(), e); 
        } finally {
            method.releaseConnection();
        }
        return res;
    }


}
