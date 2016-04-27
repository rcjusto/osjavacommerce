package org.store.carriers.ups.rs;

import org.store.core.utils.carriers.RateService;
import org.apache.commons.digester.Digester;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;


/**
 * Created by IntelliJ IDEA.
 * User: Rogelio Caballero Justo
 * Date: 20-nov-2006
 */
public class UPSRateServicesResponse {

    private String xpciVersion;
    private String responseStatusCode;
    private String responseStatusDescription;

    private String errorSeverity;
    private String errorCode;
    private String errorDescription;
    private String errorLocation;

    private ArrayList<org.store.core.utils.carriers.RateService> rateServices;


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

    public ArrayList<RateService> getRateServices() {
        return rateServices;
    }

    public void setRateServices(ArrayList<RateService> rateServices) {
        this.rateServices = rateServices;
    }

    public UPSRateServicesResponse(InputStream stream) throws IOException, SAXException {
        Digester digester = new Digester();
        digester.push(this);
        digester.addCallMethod("RatingServiceSelectionResponse/Response", "addResponse", 3);
        digester.addCallParam("RatingServiceSelectionResponse/Response/ResponseStatusCode", 0);
        digester.addCallParam("RatingServiceSelectionResponse/Response/ResponseStatusDescription", 1);
        digester.addCallParam("RatingServiceSelectionResponse/Response/TransactionReference/XpciVersion", 2);

        digester.addCallMethod("RatingServiceSelectionResponse/RatedShipment", "addShipment", 4);
        digester.addCallParam("RatingServiceSelectionResponse/RatedShipment/Service/Code", 0);
        digester.addCallParam("RatingServiceSelectionResponse/RatedShipment/TotalCharges/CurrencyCode", 1);
        digester.addCallParam("RatingServiceSelectionResponse/RatedShipment/TotalCharges/MonetaryValue", 2);
        digester.addCallParam("RatingServiceSelectionResponse/RatedShipment/GuaranteedDaysToDelivery", 3);

        digester.addCallMethod("RatingServiceSelectionResponse/Response/Error", "addError", 4);
        digester.addCallParam("RatingServiceSelectionResponse/Response/Error/ErrorSeverity", 0);
        digester.addCallParam("RatingServiceSelectionResponse/Response/Error/ErrorCode", 1);
        digester.addCallParam("RatingServiceSelectionResponse/Response/Error/ErrorDescription", 2);
        digester.addCallParam("RatingServiceSelectionResponse/Response/Error/ErrorLocation/ErrorLocationElementName", 3);

        digester.parse(stream);
    }

    public void addResponse(String _statusCode, String _statusDescription, String _xpciVersion) {
        setResponseStatusCode(_statusCode);
        setResponseStatusDescription(_statusDescription);
        setXpciVersion(_xpciVersion);
    }

    public void addShipment(String code, String currencyCode, String monetaryValue, String daysToDelivery) {
        if (rateServices==null) setRateServices(new ArrayList<RateService>());
        RateService serv = new RateService(code, currencyCode, monetaryValue, daysToDelivery);
        rateServices.add(serv);
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
