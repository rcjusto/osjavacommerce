package org.store.carriers.ups.vo;


import org.apache.commons.digester.Digester;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by IntelliJ IDEA.
 * User: Rogelio Caballero Justo
 * Date: 20-nov-2006
 */
public class UPSVoidResponse {

    private String xpciVersion;
    private String responseStatusCode;
    private String responseStatusDescription;

    private String errorSeverity;
    private String errorCode;
    private String errorDescription;
    private String errorLocation;


    public String getErrorSeverity() {
        return errorSeverity;
    }

    public void setErrorSeverity(String errorSeverity) {
        this.errorSeverity = errorSeverity;
    }

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

    public String getErrorLocation() {
        return errorLocation;
    }

    public void setErrorLocation(String errorLocation) {
        this.errorLocation = errorLocation;
    }

    public String getXpciVersion() {
        return xpciVersion;
    }

    public void setXpciVersion(String xpciVersion) {
        this.xpciVersion = xpciVersion;
    }

    public String getResponseStatusCode() {
        return responseStatusCode;
    }

    public void setResponseStatusCode(String responseStatusCode) {
        this.responseStatusCode = responseStatusCode;
    }

    public String getResponseStatusDescription() {
        return responseStatusDescription;
    }

    public void setResponseStatusDescription(String responseStatusDescription) {
        this.responseStatusDescription = responseStatusDescription;
    }

    public UPSVoidResponse(InputStream stream) throws IOException, SAXException {
        Digester digester = new Digester();
        digester.push(this);
        digester.addCallMethod("VoidShipmentResponse/Response", "addResponse", 3);
        digester.addCallParam("VoidShipmentResponse/Response/ResponseStatusCode", 0);
        digester.addCallParam("VoidShipmentResponse/Response/ResponseStatusDescription", 1);
        digester.addCallParam("VoidShipmentResponse/Response/TransactionReference/XpciVersion", 2);

        digester.addCallMethod("VoidShipmentResponse/Response/Error", "addError", 4);
        digester.addCallParam("VoidShipmentResponse/Response/Error/ErrorSeverity", 0);
        digester.addCallParam("VoidShipmentResponse/Response/Error/ErrorCode", 1);
        digester.addCallParam("VoidShipmentResponse/Response/Error/ErrorDescription", 2);
        digester.addCallParam("VoidShipmentResponse/Response/Error/ErrorLocation/ErrorLocationElementName", 3);

        digester.parse(stream);
    }

    public void addResponse(String _statusCode, String _statusDescription, String _xpciVersion) {
        setResponseStatusCode(_statusCode);
        setResponseStatusDescription(_statusDescription);
        setXpciVersion(_xpciVersion);
    }

    public void addError(String severity, String code, String desc, String location) {
        setErrorSeverity(severity);
        setErrorCode(code);
        setErrorDescription(desc);
        setErrorLocation(location);
    }

    public boolean hasError() {
        return responseStatusCode != null && !"".equals(responseStatusCode) && !"1".equals(responseStatusCode);
    }

}
