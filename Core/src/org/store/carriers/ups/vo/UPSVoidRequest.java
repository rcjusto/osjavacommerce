package org.store.carriers.ups.vo;

import org.store.core.utils.carriers.velocity.CarrierVelocityTool;
import org.store.carriers.ups.common.UpsUser;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.log4j.Logger;
import org.apache.velocity.VelocityContext;
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
 * Date: 20-nov-2006
 */
public class UPSVoidRequest {

    private String viewPath;
    private String status;
    private UpsUser user;
    private static final String URL_LIVE = "https://www.ups.com/ups.app/xml/Void";
    private static final String URL_TEST = "https://wwwcie.ups.com/ups.app/xml/Void";
    private static final String CUSTOMER_CONTEXT = "Customer Void Request";
    private static final String XPCI_VERSION = "1.0001";
    private static final String REQUEST_ACTION = "1";
    private static final String REQUEST_OPTION = "";

    private String shipmentIdentificationNumber;

    public static Logger log = Logger.getLogger(UPSVoidRequest.class);


    public UPSVoidRequest(String runStatus, Properties prop) {
        status = runStatus;
        if (prop != null) loadProperties(prop);
    }

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

    public String getRequestOption() {
        return REQUEST_OPTION;
    }

    public String getShipmentIdentificationNumber() {
        return shipmentIdentificationNumber;
    }

    public void setShipmentIdentificationNumber(String shipmentIdentificationNumber) {
        this.shipmentIdentificationNumber = shipmentIdentificationNumber;
    }

    public UPSVoidResponse execute() throws IOException, SAXException {
        UPSVoidResponse res = null;
        // generar los xml
        String xmlAccess = user.getAccessRequestXml();
        String xmlAddress = getContentXmlEx();
        String postBody = xmlAccess + xmlAddress;

        log.info("UPS Void \n" + postBody);

        PostMethod post = new PostMethod(getUrl());
        RequestEntity entity = new StringRequestEntity(postBody, "application/xml", "UTF-8");
        post.setRequestEntity(entity);
        HttpClient httpclient = new HttpClient();
        try {
            int result = httpclient.executeMethod(post);
            System.out.println("Response status code: " + result);
            InputStream stream = post.getResponseBodyAsStream();
            res = new UPSVoidResponse(stream);
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
        context.put("tool", new CarrierVelocityTool());
        String templateName = "ups/ups_void.vm";
        return org.store.core.utils.carriers.velocity.VelocityGenerator.getGeneratedXml(context, templateName);
    }

    public String getContentXmlEx() {
        String res;
        DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder;
        try {
            docBuilder = docBuilderFactory.newDocumentBuilder();
            org.w3c.dom.Document doc = docBuilder.newDocument();

            Element rootElem = doc.createElement("VoidShipmentRequest");
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

            Node nodeOpt = doc.createElement("RequestOption");
            nodeOpt.setTextContent(getRequestOption());
            nodeReq.appendChild(nodeOpt);

            Node nodeIdNumber = doc.createElement("ShipmentIdentificationNumber");
            nodeIdNumber.setTextContent(getShipmentIdentificationNumber());
            rootElem.appendChild(nodeIdNumber);

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

    public void loadProperties(Properties prop) {
        user = new UpsUser(prop);
        if (status == null || "".equals(status)) status = prop.getProperty("status");
    }

}
