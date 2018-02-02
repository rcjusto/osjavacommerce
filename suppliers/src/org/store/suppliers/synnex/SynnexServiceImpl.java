package org.store.suppliers.synnex;

import org.store.core.utils.suppliers.SupplierProperty;
import org.store.core.utils.suppliers.SupplierService;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;


public class SynnexServiceImpl implements SupplierService {

    public static Logger log = Logger.getLogger(SynnexServiceImpl.class);
    private Properties properties;
    private String url;
    private static final String URL_US = "https://ec.synnex.com/SynnexXML/PriceAvailability";
    private static final String URL_CANADA = "https://ec.synnex.ca/SynnexXML/PriceAvailability";

    public SynnexServiceImpl() {
    }

    public org.store.core.utils.suppliers.AvailabilityResponse requestAvailability(String mfgPartNumber, String sku) {
        if (StringUtils.isNotEmpty(url)) {
            SynnexServicesResponse res = null;
            // generar los xml
            StringBuffer postBody = new StringBuffer();
            postBody.append("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>");
            postBody.append("<priceRequest>");
            postBody.append("<customerNo>").append(properties.get("customerNo")).append("</customerNo>");
            postBody.append("<userName>").append(properties.get("userName")).append("</userName>");
            postBody.append("<password>").append(properties.get("password")).append("</password>");
            postBody.append("<skuList>");
            if (StringUtils.isNotEmpty(sku)) postBody.append("<synnexSKU>").append(sku).append("</synnexSKU>");
            else postBody.append("<mfgPN>").append(mfgPartNumber).append("</mfgPN>");
            postBody.append("<lineNumber>1</lineNumber>");
            postBody.append("</skuList>");
            postBody.append("</priceRequest>");

            PostMethod post = new PostMethod(url);
            try {
                post.addParameter("xmldata",postBody.toString());
                HttpClient httpclient = new HttpClient();
                int result = httpclient.executeMethod(post);
                System.out.println("Response status code: " + result);
                res = new SynnexServicesResponse(post.getResponseBodyAsString(), properties.getProperty("warehouse"));
                return res.getResponse();
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            } catch (SAXException e) {
                log.error(e.getMessage(), e);
            } finally {
                post.releaseConnection();
            }
        } else {
            org.store.core.utils.suppliers.AvailabilityResponse resp = new org.store.core.utils.suppliers.AvailabilityResponse();
            resp.setError("SYNNEX is not available for this country");
            return resp;
        }
        return null;
    }

    public List<SupplierProperty> getConfigurationParameters() {
        List<SupplierProperty> result = new ArrayList<SupplierProperty>();
        result.add(new SupplierProperty("customerNo"));
        result.add(new SupplierProperty("userName"));
        result.add(new SupplierProperty("password"));
        result.add(new SupplierProperty("country","US,CA"));
        result.add(new SupplierProperty("mode","live,test"));
        return result;
    }

    public String getName() {
        return "SYNNEX";
    }

    public void setProperties(Properties p) {
        this.properties = p;
        if ("US".equalsIgnoreCase(properties.getProperty("country"))) {
            url = URL_US;
        } else if ("CA".equalsIgnoreCase(properties.getProperty("country"))) {
            url = URL_CANADA;
        }
        if ("test".equals(properties.getProperty("mode"))) {
            if ("US".equals(properties.getProperty("country"))) {
                properties.setProperty("customerNo", "110486");
            } else if ("CA".equals(properties.getProperty("country"))) {
                properties.setProperty("customerNo", "1013793");
            }
            properties.setProperty("userName", "b2b@synnex.com");
            properties.setProperty("password", "synnex");
        }
    }
}
