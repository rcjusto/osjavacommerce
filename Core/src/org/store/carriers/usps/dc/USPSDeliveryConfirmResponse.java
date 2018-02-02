package org.store.carriers.usps.dc;

import org.apache.log4j.Logger;
import org.xml.sax.SAXException;
import org.apache.commons.digester.Digester;
import org.apache.commons.codec.binary.Base64;

import java.io.InputStream;
import java.io.IOException;
import java.io.FileOutputStream;

/**
 * Created by IntelliJ IDEA.
 * User: Rogelio Caballero Justo
 * Date: 20-nov-2006
 */
public class USPSDeliveryConfirmResponse {

    public static Logger log = Logger.getLogger(USPSDeliveryConfirmResponse.class);
    private String errorCode;
    private String errorDescription;

    private String deliveryConfirmationNumber;
    private String deliveryConfirmationLabel;
    private String postnet;

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorDescription() {
        return errorDescription;
    }

    public void setErrorDescription(String errorDescription) {
        this.errorDescription = errorDescription;
    }

    public String getDeliveryConfirmationNumber() {
        return deliveryConfirmationNumber;
    }

    public void setDeliveryConfirmationNumber(String deliveryConfirmationNumber) {
        this.deliveryConfirmationNumber = deliveryConfirmationNumber;
    }

    public String getDeliveryConfirmationLabel() {
        return deliveryConfirmationLabel;
    }

    public void setDeliveryConfirmationLabel(String deliveryConfirmationLabel) {
        this.deliveryConfirmationLabel = deliveryConfirmationLabel;
    }

    public String getPostnet() {
        return postnet;
    }

    public void setPostnet(String postnet) {
        this.postnet = postnet;
    }

    public USPSDeliveryConfirmResponse(InputStream stream) throws IOException, SAXException {
        Digester digester = new Digester();
        digester.push(this);
        digester.addCallMethod("Error", "addError", 2);
        digester.addCallParam("Error/Number", 0);
        digester.addCallParam("Error/Description", 1);

        digester.addCallMethod("DeliveryConfirmationV3.0Response", "addResponse", 3);
        digester.addCallParam("DeliveryConfirmationV3.0Response/DeliveryConfirmationNumber", 0);
        digester.addCallParam("DeliveryConfirmationV3.0Response/DeliveryConfirmationLabel", 1);
        digester.addCallParam("DeliveryConfirmationV3.0Response/Postnet", 2);

        digester.parse(stream);
    }

    public void addError(String code, String desc) {
        setErrorCode(code);
        setErrorDescription(desc);
    }

    public void addResponse(String number, String label, String pnet) {
        setDeliveryConfirmationNumber(number);
        setDeliveryConfirmationLabel(label);
        setPostnet(pnet);
    }

    public boolean hasError() {
        return errorCode!=null && !"".equals(errorCode);
    }

    public boolean saveGraphicImage(String fileName) {
        try {
            byte[] encodeBytes = deliveryConfirmationLabel.getBytes();
            byte[] decodeBytes = Base64.decodeBase64(encodeBytes);
            FileOutputStream fos = new FileOutputStream(fileName);
            fos.write(decodeBytes, 0, decodeBytes.length);
            fos.close();
            return true;
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
        return false;
    }

}
