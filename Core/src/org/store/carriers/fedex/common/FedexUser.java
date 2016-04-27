package org.store.carriers.fedex.common;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.util.Properties;
import java.io.StringWriter;

/**
 * Created by IntelliJ IDEA.
 * User: Rogelio Caballero Justo
 * Date: 19-nov-2006
 */
public class FedexUser {

    public static Logger log = Logger.getLogger(FedexUser.class);
    private String accountNumber;
    private String meterNumber;


    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getMeterNumber() {
        return meterNumber;
    }

    public void setMeterNumber(String meterNumber) {
        this.meterNumber = meterNumber;
    }

    public FedexUser(Properties prop) {
        accountNumber = prop.getProperty("AccountNumber");
        meterNumber = prop.getProperty("MeterNumber");
    }

    public String getAccessRequestXml() {
        String res;
        DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder;
        try {
            docBuilder = docBuilderFactory.newDocumentBuilder();
            org.w3c.dom.Document doc = docBuilder.newDocument();

            Element rootElem = doc.createElement("RequestHeader");
            doc.appendChild(rootElem);

            Node node1 = doc.createElement("AccountNumber");
            node1.setTextContent(getAccountNumber());
            rootElem.appendChild(node1);

            Node node2 = doc.createElement("MeterNumber");
            node2.setTextContent(getMeterNumber());
            rootElem.appendChild(node2);


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

}
