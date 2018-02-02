package org.store.carriers.ups.common;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;
import java.util.Properties;

/**
 * Created by IntelliJ IDEA.
 * User: Rogelio Caballero Justo
 * Date: 19-nov-2006
 */
public class UpsUser {

    public static Logger log = Logger.getLogger(UpsUser.class);

    private String accessLicenseNumber;
    private String userId;
    private String password;


    public String getAccessLicenseNumber() {
        return accessLicenseNumber;
    }

    public void setAccessLicenseNumber(String accessLicenseNumber) {
        this.accessLicenseNumber = accessLicenseNumber;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }


    public UpsUser(Properties prop) {
        accessLicenseNumber = prop.getProperty("access.license.number");
        userId = prop.getProperty("user.id");
        password = prop.getProperty("password");
    }

    public String getAccessRequestXml() {
        String res;
        DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder;
        try {
            docBuilder = docBuilderFactory.newDocumentBuilder();
            org.w3c.dom.Document doc = docBuilder.newDocument();

            Element rootElem = doc.createElement("AccessRequest");
            doc.appendChild(rootElem);

            Node node1 = doc.createElement("AccessLicenseNumber");
            node1.setTextContent(getAccessLicenseNumber());
            rootElem.appendChild(node1);

            Node node2 = doc.createElement("UserId");
            node2.setTextContent(getUserId());
            rootElem.appendChild(node2);

            Node node3 = doc.createElement("Password");
            node3.setTextContent(getPassword());
            rootElem.appendChild(node3);

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
