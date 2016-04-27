package org.store.carriers.ups.sc;

import org.apache.commons.digester.Digester;
import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by IntelliJ IDEA.
 * User: Rogelio Caballero Justo
 * Date: 20-nov-2006
 */
public class UPSShipConfirmResponse {

    public static Logger log = Logger.getLogger(UPSShipConfirmResponse.class);
    private String xpciVersion;
    private String responseStatusCode;
    private String responseStatusDescription;

    private String errorSeverity;
    private String errorCode;
    private String errorDescription;
    private String errorLocation;

    private String transportationChargesCurrency;
    private Double transportationChargesValue;
    private String serviceOptionsChargesCurrency;
    private Double serviceOptionsChargesValue;
    private String totalChargesCurrency;
    private Double totalChargesValue;

    private String billingWeightCode;
    private String billingWeightDescription;
    private Double billingWeight;

    private String shipmentDigest;
    private String shipmentIdentificationNumber;


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


    public String getTransportationChargesCurrency() {
        return transportationChargesCurrency;
    }

    public void setTransportationChargesCurrency(String transportationChargesCurrency) {
        this.transportationChargesCurrency = transportationChargesCurrency;
    }

    public Double getTransportationChargesValue() {
        return transportationChargesValue;
    }

    public void setTransportationChargesValue(Double transportationChargesValue) {
        this.transportationChargesValue = transportationChargesValue;
    }

    public String getServiceOptionsChargesCurrency() {
        return serviceOptionsChargesCurrency;
    }

    public void setServiceOptionsChargesCurrency(String serviceOptionsChargesCurrency) {
        this.serviceOptionsChargesCurrency = serviceOptionsChargesCurrency;
    }

    public Double getServiceOptionsChargesValue() {
        return serviceOptionsChargesValue;
    }

    public void setServiceOptionsChargesValue(Double serviceOptionsChargesValue) {
        this.serviceOptionsChargesValue = serviceOptionsChargesValue;
    }

    public String getTotalChargesCurrency() {
        return totalChargesCurrency;
    }

    public void setTotalChargesCurrency(String totalChargesCurrency) {
        this.totalChargesCurrency = totalChargesCurrency;
    }

    public Double getTotalChargesValue() {
        return totalChargesValue;
    }

    public void setTotalChargesValue(Double totalChargesValue) {
        this.totalChargesValue = totalChargesValue;
    }

    public String getBillingWeightCode() {
        return billingWeightCode;
    }

    public void setBillingWeightCode(String billingWeightCode) {
        this.billingWeightCode = billingWeightCode;
    }

    public String getBillingWeightDescription() {
        return billingWeightDescription;
    }

    public void setBillingWeightDescription(String billingWeightDescription) {
        this.billingWeightDescription = billingWeightDescription;
    }

    public Double getBillingWeight() {
        return billingWeight;
    }

    public void setBillingWeight(Double billingWeight) {
        this.billingWeight = billingWeight;
    }

    public String getShipmentDigest() {
        return shipmentDigest;
    }

    public void setShipmentDigest(String shipmentDigest) {
        this.shipmentDigest = shipmentDigest;
    }

    public String getShipmentIdentificationNumber() {
        return shipmentIdentificationNumber;
    }

    public void setShipmentIdentificationNumber(String shipmentIdentificationNumber) {
        this.shipmentIdentificationNumber = shipmentIdentificationNumber;
    }

    public UPSShipConfirmResponse(InputStream stream) throws IOException, SAXException {
        Digester digester = new Digester();
        digester.push(this);
        digester.addCallMethod("ShipmentConfirmResponse/Response", "addResponse", 3);
        digester.addCallParam("ShipmentConfirmResponse/Response/ResponseStatusCode", 0);
        digester.addCallParam("ShipmentConfirmResponse/Response/ResponseStatusDescription", 1);
        digester.addCallParam("ShipmentConfirmResponse/Response/TransactionReference/XpciVersion", 2);

        digester.addCallMethod("ShipmentConfirmResponse/ShipmentCharges", "addShipmentCharges", 6);
        digester.addCallParam("ShipmentConfirmResponse/ShipmentCharges/TransportationCharges/CurrencyCode", 0);
        digester.addCallParam("ShipmentConfirmResponse/ShipmentCharges/TransportationCharges/MonetaryValue", 1);
        digester.addCallParam("ShipmentConfirmResponse/ShipmentCharges/ServiceOptionsCharges/CurrencyCode", 2);
        digester.addCallParam("ShipmentConfirmResponse/ShipmentCharges/ServiceOptionsCharges/MonetaryValue", 3);
        digester.addCallParam("ShipmentConfirmResponse/ShipmentCharges/TotalCharges/CurrencyCode", 4);
        digester.addCallParam("ShipmentConfirmResponse/ShipmentCharges/TotalCharges/MonetaryValue", 5);

        digester.addCallMethod("ShipmentConfirmResponse/BillingWeight", "addBillingWeight", 2);
        digester.addCallParam("ShipmentConfirmResponse/BillingWeight/UnitOfMeasurement/Code", 0);
        digester.addCallParam("ShipmentConfirmResponse/BillingWeight/Weight", 1);

        digester.addCallMethod("ShipmentConfirmResponse/Response/Error", "addError", 4);
        digester.addCallParam("ShipmentConfirmResponse/Response/Error/ErrorSeverity", 0);
        digester.addCallParam("ShipmentConfirmResponse/Response/Error/ErrorCode", 1);
        digester.addCallParam("ShipmentConfirmResponse/Response/Error/ErrorDescription", 2);
        digester.addCallParam("ShipmentConfirmResponse/Response/Error/ErrorLocation/ErrorLocationElementName", 3);

        digester.addCallMethod("ShipmentConfirmResponse/ShipmentDigest", "setShipmentDigest", 0);
        digester.addCallMethod("ShipmentConfirmResponse/ShipmentIdentificationNumber", "setShipmentIdentificationNumber", 0);

        digester.parse(stream);
    }

    public void addResponse(String _statusCode, String _statusDescription, String _xpciVersion) {
        setResponseStatusCode(_statusCode);
        setResponseStatusDescription(_statusDescription);
        setXpciVersion(_xpciVersion);
    }

    public void addShipmentCharges(String transCode, String transValue, String servCode, String servValue, String totalCode, String totalValue  ) {
        setTransportationChargesCurrency(transCode);
        setServiceOptionsChargesCurrency(servCode);
        setTotalChargesCurrency(totalCode);
        try {
            Double d = Double.valueOf(transValue);
            setTransportationChargesValue(d);
        } catch (NumberFormatException e) {
            log.error(e.getMessage(), e);
        }
        try {
            Double d = Double.valueOf(servValue);
            setServiceOptionsChargesValue(d);
        } catch (NumberFormatException e) {
            log.error(e.getMessage(), e);
        }
        try {
            Double d = Double.valueOf(totalValue);
            setTotalChargesValue(d);
        } catch (NumberFormatException e) {
            log.error(e.getMessage(), e);
        }
    }

    public void addBillingWeight(String weightCode, String weight) {
        setBillingWeightCode(weightCode);
        try {
            Double d = Double.valueOf(weight);
            setBillingWeight(d);
        } catch (NumberFormatException e) {
            log.error(e.getMessage(), e);
        }
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
