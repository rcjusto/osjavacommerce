package org.store.carriers.ups.av;

import org.store.carriers.ups.common.UpsUser;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.Properties;

/**
 * Created by IntelliJ IDEA.
 * User: Rogelio Caballero Justo
 * Date: 19-nov-2006
 */
public class AddressValidationRequest {
    private String status;
    private UpsUser user;

    private static final String URL_LIVE = "https://www.ups.com/ups.app/xml/AV";
    private static final String URL_TEST = "https://wwwcie.ups.com/ups.app/xml/AV";
    private static final String CUSTOMER_CONTEXT = " Address Validation";
    private static final String XPCI_VERSION = "1.0001";
    private static final String REQUEST_ACTION = "AV";
    
    private AddressInput address;

    private final Logger log = Logger.getLogger(AddressValidationRequest.class);

    public UpsUser getUser() {
        return user;
    }

    public void setUser(UpsUser user) {
        this.user = user;
    }

    public String getUrl() {
        return ("test".equalsIgnoreCase(status)) ? URL_TEST : URL_LIVE;
    }

    public String getCustomerContext() {
        return CUSTOMER_CONTEXT;
    }

    public String getXpciVersion() {
        return XPCI_VERSION;
    }

    public String getRequestAction() {
        return REQUEST_ACTION;
    }

    public AddressInput getAddress() {
        return address;
    }

    public void setAddress(AddressInput address) {
        this.address = address;
    }

    public AddressValidationResponse execute() throws IOException, SAXException {
        AddressValidationResponse res = null;
        // generar los xml
        String xmlAccess = user.getAccessRequestXml();
        String xmlAddress = getAddressValidationRequestXml();
        String postBody = xmlAccess + xmlAddress;
        log.info("UPS Address Verification \n" + postBody);
        PostMethod post = new PostMethod(getUrl());
        RequestEntity entity = new StringRequestEntity(postBody, "application/xml", "UTF-8");
        post.setRequestEntity(entity);
        HttpClient httpclient = new HttpClient();
        try {
            int result = httpclient.executeMethod(post);
            System.out.println("Response status code: " + result);
            InputStream stream = post.getResponseBodyAsStream();
            res = new AddressValidationResponse(stream);
        } catch (IOException e) {
            throw e;
        } catch (SAXException e) {
            throw e;
        } finally {
            post.releaseConnection();
        }

        return res;
    }

    public String getAddressValidationRequestXml() {
        String res;
        DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder;
        try {
            docBuilder = docBuilderFactory.newDocumentBuilder();
            org.w3c.dom.Document doc = docBuilder.newDocument();

            Element rootElem = doc.createElement("AddressValidationRequest");
            rootElem.setAttributeNS("xml", "lang", "en-US");
            doc.appendChild(rootElem);

            Node nodeReq = doc.createElement("Request");
            rootElem.appendChild(nodeReq);

            Node nodeRef = doc.createElement("TransactionReference");
            nodeReq.appendChild(nodeRef);

            Node nodeContext = doc.createElement("CustomerContext");
            nodeContext.setTextContent(getCustomerContext());
            nodeRef.appendChild(nodeContext);

            Node nodeXpci = doc.createElement("XpciVersion");
            nodeContext.setTextContent(getXpciVersion());
            nodeRef.appendChild(nodeXpci);

            Node nodeAct = doc.createElement("RequestAction");
            nodeAct.setTextContent(getRequestAction());
            nodeReq.appendChild(nodeAct);

            // Address
            Node nodeAdd = doc.createElement("Address");
            rootElem.appendChild(nodeAdd);

            Node nodeCity = doc.createElement("City");
            nodeCity.setTextContent(address.getCity());
            nodeAdd.appendChild(nodeCity);

            Node nodeState = doc.createElement("StateProvinceCode");
            nodeState.setTextContent(address.getStateProvinceCode());
            nodeAdd.appendChild(nodeState);

            Node nodeZip = doc.createElement("PostalCode");
            nodeZip.setTextContent(address.getPostalCode());
            nodeAdd.appendChild(nodeZip);

            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

            StreamResult result = new StreamResult(new StringWriter());
            DOMSource source = new DOMSource(doc);
            transformer.transform(source, result);

            res = result.getWriter().toString();

        }
        catch (Exception e) {
            log.error(e.getMessage(), e);
            res = null;
        }
        return res;
    }


    public AddressValidationRequest(Properties prop) {
        if (status==null || "".equals(status)) status = prop.getProperty("status");

        UpsUser usr = new UpsUser(prop);
        setUser(usr);
    }

    public static AddressValidationResponse validateAddress(String city, String state, String zipCode, Properties properties) throws Exception {
        if (properties == null) throw new Exception("Properties file not found: ups.properties");
        AddressValidationRequest avr = new AddressValidationRequest(properties);
        AddressInput ai = new AddressInput();
        ai.setCity(city);
        ai.setPostalCode(zipCode);
        ai.setStateProvinceCode(state);
        avr.setAddress(ai);
        return avr.execute();
    }

}
