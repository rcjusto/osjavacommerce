package org.store.carriers.ups.tr;

import org.store.core.utils.carriers.Address;
import org.store.core.utils.carriers.Shipper;
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
public class UPSTrackResponse implements org.store.core.utils.carriers.TrackResponse {

    private String xpciVersion;
    private String responseStatusCode;
    private String responseStatusDescription;

    private String errorSeverity;
    private String errorCode;
    private String errorDescription;
    private String errorLocation;

    private Shipper shipper;
    private org.store.core.utils.carriers.ShipTo shipTo;

    private String shipmentWeightUnit;
    private String shipmentWeight;
    
    private String serviceCode;
    private String serviceDescription;
    
    private String pickupDate;
    private String scheduledDeliveryDate;

    private ArrayList packages;

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

    public Shipper getShipper() {
        return shipper;
    }

    public void setShipper(Shipper shipper) {
        this.shipper = shipper;
    }

    public org.store.core.utils.carriers.ShipTo getShipTo() {
        return shipTo;
    }

    public void setShipTo(org.store.core.utils.carriers.ShipTo shipTo) {
        this.shipTo = shipTo;
    }

    public String getAddressLine1() {
        return (shipTo!=null) ? shipTo.getAddress().getAddressLine1() : null;
    }

    public String getAddressLine2() {
        return (shipTo!=null) ? shipTo.getAddress().getAddressLine2() : null;
    }

    public String getCity() {
        return (shipTo!=null) ? shipTo.getAddress().getCity() : null;
    }

    public String getStateProvinceCode() {
        return (shipTo!=null) ? shipTo.getAddress().getStateProvinceCode() : null;
    }

    public String getPostalCode() {
        return (shipTo!=null) ? shipTo.getAddress().getPostalCode() : null;
    }

    public String getCountryCode() {
        return (shipTo!=null) ? shipTo.getAddress().getCountryCode() : null;
    }

    public String getShipmentWeightUnit() {
        return shipmentWeightUnit;
    }

    public void setShipmentWeightUnit(String shipmentWeightUnit) {
        this.shipmentWeightUnit = shipmentWeightUnit;
    }

    public String getShipmentWeight() {
        return shipmentWeight;
    }

    public void setShipmentWeight(String shipmentWeight) {
        this.shipmentWeight = shipmentWeight;
    }

    public String getServiceCode() {
        return serviceCode;
    }

    public void setServiceCode(String serviceCode) {
        this.serviceCode = serviceCode;
    }

    public String getServiceDescription() {
        return serviceDescription;
    }

    public void setServiceDescription(String serviceDescription) {
        this.serviceDescription = serviceDescription;
    }

    public String getPickupDate() {
        return pickupDate;
    }

    public void setPickupDate(String pickupDate) {
        this.pickupDate = pickupDate;
    }

    public String getScheduledDeliveryDate() {
        return scheduledDeliveryDate;
    }

    public String getScheduledDeliveryTime() {
        return null;
    }

    public void setScheduledDeliveryDate(String scheduledDeliveryDate) {
        this.scheduledDeliveryDate = scheduledDeliveryDate;
    }

    public ArrayList getPackages() {
        return packages;
    }

    public void setPackages(ArrayList packages) {
        this.packages = packages;
    }

    public UPSTrackResponse(InputStream stream) throws IOException, SAXException {
    /*    FileWriter fw = new FileWriter("c:\\pepe.txt");
        InputStreamReader isr = new InputStreamReader(stream);
            BufferedReader br = new BufferedReader(isr);
            String line=null;
            while ( (line = br.readLine()) != null)
            {
                    fw.write(line);
            }
        fw.close();
        br.close();
        isr.close();
    */
        

        Digester digester = new Digester();
        digester.push(this);
        digester.addCallMethod("TrackResponse/Response", "addResponse", 3);
        digester.addCallParam("TrackResponse/Response/ResponseStatusCode", 0);
        digester.addCallParam("TrackResponse/Response/ResponseStatusDescription", 1);
        digester.addCallParam("TrackResponse/Response/TransactionReference/XpciVersion", 2);

        digester.addCallMethod("TrackResponse/Response/Error", "addError", 4);
        digester.addCallParam("TrackResponse/Response/Error/ErrorSeverity", 0);
        digester.addCallParam("TrackResponse/Response/Error/ErrorCode", 1);
        digester.addCallParam("TrackResponse/Response/Error/ErrorDescription", 2);
        digester.addCallParam("TrackResponse/Response/Error/ErrorLocation/ErrorLocationElementName", 3);

        digester.addCallMethod("TrackResponse/Shipment/Shipper", "addShipper", 7);
        digester.addCallParam("TrackResponse/Shipment/Shipper/ShipperNumber", 0);
        digester.addCallParam("TrackResponse/Shipment/Shipper/Address/AddressLine1", 1);
        digester.addCallParam("TrackResponse/Shipment/Shipper/Address/AddressLine2", 2);
        digester.addCallParam("TrackResponse/Shipment/Shipper/Address/City", 3);
        digester.addCallParam("TrackResponse/Shipment/Shipper/Address/StateProvinceCode", 4);
        digester.addCallParam("TrackResponse/Shipment/Shipper/Address/PostalCode", 5);
        digester.addCallParam("TrackResponse/Shipment/Shipper/Address/CountryCode", 6);

        digester.addCallMethod("TrackResponse/Shipment/ShipTo", "addShipTo", 6);
        digester.addCallParam("TrackResponse/Shipment/ShipTo/Address/AddressLine1", 0);
        digester.addCallParam("TrackResponse/Shipment/ShipTo/Address/AddressLine2", 1);
        digester.addCallParam("TrackResponse/Shipment/ShipTo/Address/City", 2);
        digester.addCallParam("TrackResponse/Shipment/ShipTo/Address/StateProvinceCode", 3);
        digester.addCallParam("TrackResponse/Shipment/ShipTo/Address/PostalCode", 4);
        digester.addCallParam("TrackResponse/Shipment/ShipTo/Address/CountryCode", 5);

        digester.addCallMethod("TrackResponse/Shipment/ShipmentWeight/UnitOfMeasurement/Code", "setShipmentWeightUnit", 0);
        digester.addCallMethod("TrackResponse/Shipment/ShipmentWeight/Weight", "setShipmentWeight", 0);

        digester.addCallMethod("TrackResponse/Shipment/Service/Code", "setServiceCode", 0);
        digester.addCallMethod("TrackResponse/Shipment/Service/Description", "setServiceDescription", 0);

        digester.addCallMethod("TrackResponse/Shipment/PickupDate", "setPickupDate", 0);
        digester.addCallMethod("TrackResponse/Shipment/ScheduledDeliveryDate", "setScheduledDeliveryDate", 0);

        digester.addCallMethod("TrackResponse/Shipment/Package", "addPackage", 18);
        digester.addCallParam("TrackResponse/Shipment/Package/TrackingNumber", 0);
        digester.addCallParam("TrackResponse/Shipment/Package/RescheduledDeliveryDate", 1);
        digester.addCallParam("TrackResponse/Shipment/Package/RescheduledDeliveryTime", 2);
        digester.addCallParam("TrackResponse/Shipment/Package/Activity/ActivityLocation/Address/AddressLine1", 3);
        digester.addCallParam("TrackResponse/Shipment/Package/Activity/ActivityLocation/Address/AddressLine2", 4);
        digester.addCallParam("TrackResponse/Shipment/Package/Activity/ActivityLocation/Address/City", 5);
        digester.addCallParam("TrackResponse/Shipment/Package/Activity/ActivityLocation/Address/StateProvinceCode", 6);
        digester.addCallParam("TrackResponse/Shipment/Package/Activity/ActivityLocation/Address/CountryCode", 7);
        digester.addCallParam("TrackResponse/Shipment/Package/Activity/ActivityLocation/Code", 8);
        digester.addCallParam("TrackResponse/Shipment/Package/Activity/ActivityLocation/Description", 9);
        digester.addCallParam("TrackResponse/Shipment/Package/Activity/ActivityLocation/SignedForByName", 10);
        digester.addCallParam("TrackResponse/Shipment/Package/Activity/Status/StatusType/Code", 11);
        digester.addCallParam("TrackResponse/Shipment/Package/Activity/Status/StatusType/Description", 12);
        digester.addCallParam("TrackResponse/Shipment/Package/Activity/Status/StatusCode/Code", 13);
        digester.addCallParam("TrackResponse/Shipment/Package/Activity/Date", 14);
        digester.addCallParam("TrackResponse/Shipment/Package/Activity/Time", 15);
        digester.addCallParam("TrackResponse/Shipment/Package/PackageWeight/UnitOfMeasurement/Code", 16);
        digester.addCallParam("TrackResponse/Shipment/Package/PackageWeight/Weight", 17);


        digester.parse(stream);
    }

    public void addPackage(String TrackingNumber,
                           String RescheduledDeliveryDate,
                           String RescheduledDeliveryTime,
                           String AddressLine1,
                           String AddressLine2,
                           String City,
                           String StateProvinceCode,
                           String CountryCode,
                           String ActivityLocationCode,
                           String ActivityLocationDescription,
                           String SignedForByName,
                           String StatusTypeCode,
                           String StatusTypeDescription,
                           String StatusCode,
                           String Date,
                           String Time,
                           String UnitOfMeasurement,
                           String Weight) {
        if (packages==null) packages = new ArrayList();
        PackageTrackResult pack = new PackageTrackResult();
        pack.setTrackingNumber(TrackingNumber);
        pack.setRescheduledDeliveryDate(RescheduledDeliveryDate);
        pack.setRescheduledDeliveryTime(RescheduledDeliveryTime);
        pack.setAlAddressLine1(AddressLine1);
        pack.setAlAddressLine2(AddressLine2);
        pack.setAlCity(City);
        pack.setAlState(StateProvinceCode);
        pack.setAlCountry(CountryCode);
        pack.setAlCode(ActivityLocationCode);
        pack.setAlDescription(ActivityLocationDescription);
        pack.setAlSignedForByName(SignedForByName);
        pack.setStatusTypeCode(StatusTypeCode);
        pack.setStatusTypeDescription(StatusTypeDescription);
        pack.setStatusCode(StatusCode);
        pack.setActivityDate(Date);
        pack.setActivityTime(Time);
        pack.setPackageWeightUnit(UnitOfMeasurement);
        pack.setPackageWeight(Weight);
        packages.add(pack);
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

    public void addShipper(String ShipperNumber, String AddressLine1, String AddressLine2, String City,
                          String StateProvinceCode, String PostalCode, String CountryCode) {
        Address add = new Address(AddressLine1, AddressLine2, City, StateProvinceCode, PostalCode, CountryCode);
        setShipper(new Shipper("","",ShipperNumber, "","", add));
    }

    public void addShipTo(String AddressLine1, String AddressLine2, String City,
                          String StateProvinceCode, String PostalCode, String CountryCode) {
        Address add = new Address(AddressLine1, AddressLine2, City, StateProvinceCode, PostalCode, CountryCode);
        setShipTo( new org.store.core.utils.carriers.ShipTo("","", add, null) );
    }

    public boolean hasError() {
        return responseStatusCode != null && !"".equals(responseStatusCode) && !"1".equals(responseStatusCode);
    }

}
