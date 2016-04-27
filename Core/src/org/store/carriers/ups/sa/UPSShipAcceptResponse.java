package org.store.carriers.ups.sa;

import org.apache.commons.digester.Digester;
import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: Rogelio Caballero Justo
 * Date: 20-nov-2006
 */
public class UPSShipAcceptResponse {

    public static Logger log = Logger.getLogger(UPSShipAcceptResponse.class);
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

    private ArrayList<PackageResult> packages;
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

    public String getShipmentIdentificationNumber() {
        return shipmentIdentificationNumber;
    }

    public void setShipmentIdentificationNumber(String shipmentIdentificationNumber) {
        this.shipmentIdentificationNumber = shipmentIdentificationNumber;
    }

    public ArrayList<PackageResult> getPackages() {
        return packages;
    }

    public void setPackages(ArrayList<PackageResult> packages) {
        this.packages = packages;
    }

    public UPSShipAcceptResponse(InputStream stream) throws IOException, SAXException {

        Digester digester = new Digester();
        digester.push(this);
        digester.addCallMethod("ShipmentAcceptResponse/Response", "addResponse", 3);
        digester.addCallParam("ShipmentAcceptResponse/Response/ResponseStatusCode", 0);
        digester.addCallParam("ShipmentAcceptResponse/Response/ResponseStatusDescription", 1);
        digester.addCallParam("ShipmentAcceptResponse/Response/TransactionReference/XpciVersion", 2);

        digester.addCallMethod("ShipmentAcceptResponse/ShipmentResults/ShipmentCharges", "addShipmentCharges", 6);
        digester.addCallParam("ShipmentAcceptResponse/ShipmentResults/ShipmentCharges/TransportationCharges/CurrencyCode", 0);
        digester.addCallParam("ShipmentAcceptResponse/ShipmentResults/ShipmentCharges/TransportationCharges/MonetaryValue", 1);
        digester.addCallParam("ShipmentAcceptResponse/ShipmentResults/ShipmentCharges/ServiceOptionsCharges/CurrencyCode", 2);
        digester.addCallParam("ShipmentAcceptResponse/ShipmentResults/ShipmentCharges/ServiceOptionsCharges/MonetaryValue", 3);
        digester.addCallParam("ShipmentAcceptResponse/ShipmentResults/ShipmentCharges/TotalCharges/CurrencyCode", 4);
        digester.addCallParam("ShipmentAcceptResponse/ShipmentResults/ShipmentCharges/TotalCharges/MonetaryValue", 5);

        digester.addCallMethod("ShipmentAcceptResponse/ShipmentResults/BillingWeight", "addBillingWeight", 2);
        digester.addCallParam("ShipmentAcceptResponse/ShipmentResults/BillingWeight/UnitOfMeasurement/Code", 0);
        digester.addCallParam("ShipmentAcceptResponse/ShipmentResults/BillingWeight/Weight", 1);

        digester.addCallMethod("ShipmentAcceptResponse/ShipmentResults/ShipmentIdentificationNumber", "setShipmentIdentificationNumber", 0);

        digester.addCallMethod("ShipmentAcceptResponse/ShipmentResults/PackageResults", "addPackage", 5);
        digester.addCallParam("ShipmentAcceptResponse/ShipmentResults/PackageResults/TrackingNumber", 0);
        digester.addCallParam("ShipmentAcceptResponse/ShipmentResults/PackageResults/ServiceOptionsCharges/CurrencyCode", 1);
        digester.addCallParam("ShipmentAcceptResponse/ShipmentResults/PackageResults/ServiceOptionsCharges/MonetaryValue", 2);
        digester.addCallParam("ShipmentAcceptResponse/ShipmentResults/PackageResults/LabelImage/LabelImageFormat/Code", 3);
        digester.addCallParam("ShipmentAcceptResponse/ShipmentResults/PackageResults/LabelImage/GraphicImage", 4);

        digester.addCallMethod("ShipmentAcceptResponse/Response/Error", "addError", 4);
        digester.addCallParam("ShipmentAcceptResponse/Response/Error/ErrorSeverity", 0);
        digester.addCallParam("ShipmentAcceptResponse/Response/Error/ErrorCode", 1);
        digester.addCallParam("ShipmentAcceptResponse/Response/Error/ErrorDescription", 2);
        digester.addCallParam("ShipmentAcceptResponse/Response/Error/ErrorLocation/ErrorLocationElementName", 3);
        

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

    public void addPackage(String trackingNumber, String serviceOptionsChargesCode, String serviceOptionsChargesValueStr, String labelImageFormatCode, String graphicImage) {
        if (packages==null) packages = new ArrayList<PackageResult>();
        packages.add(new PackageResult(trackingNumber, serviceOptionsChargesCode, serviceOptionsChargesValueStr, labelImageFormatCode, graphicImage));
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

    public int saveGraphicImages(String path) {
        int res = 0;
        if (packages!=null)
            for (Object aPackage : packages) {
                PackageResult pr = (PackageResult) aPackage;
                if (pr.saveGraphicImage(path + pr.getTrackingNumber() + ".gif")) res++;
            }
        return res;
    }

}
