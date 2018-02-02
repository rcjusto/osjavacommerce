package org.store.carriers.fedex.tr;

import org.store.core.utils.carriers.TrackPackage;
import org.store.carriers.ups.tr.PackageTrackResult;
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
public class FedexTrackResponse implements org.store.core.utils.carriers.TrackResponse {

    private String errorCode;
    private String errorDescription;
    private org.store.core.utils.carriers.Address address;
    private String shipmentWeight;
    private String serviceCode;
    private String serviceDesc;
    private String shipDate;
    private String deliveryDate;
    private String deliveryTime;
    private ArrayList<TrackPackage> packages;

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

    public boolean hasError() {
        return errorCode != null && !"".equals(errorCode);
    }

    public String getAddressLine1() {
        return (address!=null) ? address.getAddressLine1() : "";
    }

    public String getAddressLine2() {
        return (address!=null) ? address.getAddressLine2() : "";
    }

    public String getCity() {
        return (address!=null) ? address.getCity() : "";
    }

    public String getStateProvinceCode() {
        return (address!=null) ? address.getStateProvinceCode() : "";
    }

    public String getPostalCode() {
        return (address!=null) ? address.getPostalCode() : "";
    }

    public String getCountryCode() {
        return (address!=null) ? address.getCountryCode() : "";
    }

    public String getShipmentWeightUnit() {
        return (address!=null) ? address.getAddressLine1() : "";
    }

    public String getShipmentWeight() {
        return shipmentWeight;
    }

    public String getServiceCode() {
        return serviceCode;
    }

    public String getServiceDescription() {
        return serviceDesc;
    }

    public String getPickupDate() {
        return shipDate;
    }

    public String getScheduledDeliveryDate() {
        return deliveryDate;
    }

    public String getScheduledDeliveryTime() {
        return deliveryTime;
    }

    public ArrayList<TrackPackage> getPackages() {
        return packages;
    }

    public FedexTrackResponse(InputStream stream) throws IOException, SAXException {
        Digester digester = new Digester();
        digester.push(this);
        digester.addCallMethod("FDXTrack2Reply/Error", "addError", 2);
        digester.addCallParam("FDXTrack2Reply/Error/Code", 0);
        digester.addCallParam("FDXTrack2Reply/Error/Message", 1);

        digester.addCallMethod("Error", "addError", 2);
        digester.addCallParam("Error/Code", 0);
        digester.addCallParam("Error/Message", 1);

        digester.addCallMethod("FDXTrack2Reply/Package", "addPInfo", 10 );
        digester.addCallParam("FDXTrack2Reply/Package/DestinationAddress/City", 0);
        digester.addCallParam("FDXTrack2Reply/Package/DestinationAddress/StateOrProvinceCode", 1);
        digester.addCallParam("FDXTrack2Reply/Package/DestinationAddress/CountryCode", 2);
        digester.addCallParam("FDXTrack2Reply/Package/ShipmentWeight", 3);
        digester.addCallParam("FDXTrack2Reply/Package/WeightUnits", 4);
        digester.addCallParam("FDXTrack2Reply/Package/CarrierCode", 5);
        digester.addCallParam("FDXTrack2Reply/Package/Service", 6);
        digester.addCallParam("FDXTrack2Reply/Package/ShipDate", 7);
        digester.addCallParam("FDXTrack2Reply/Package/DeliveredDate", 8);
        digester.addCallParam("FDXTrack2Reply/Package/DeliveredTime", 9);

        digester.addCallMethod("FDXTrack2Reply/Package/Event", "addPackage", 8);
        digester.addCallParam("FDXTrack2Reply/Package/Event/Address/City", 0);
        digester.addCallParam("FDXTrack2Reply/Package/Event/Address/StateOrProvinceCode", 1);
        digester.addCallParam("FDXTrack2Reply/Package/Event/Address/PostalCode", 2);
        digester.addCallParam("FDXTrack2Reply/Package/Event/Address/CountryCode", 3);
        digester.addCallParam("FDXTrack2Reply/Package/Event/Type", 4);
        digester.addCallParam("FDXTrack2Reply/Package/Event/Description", 5);
        digester.addCallParam("FDXTrack2Reply/Package/Event/Date", 6);
        digester.addCallParam("FDXTrack2Reply/Package/Event/Time", 7);

        digester.parse(stream);
    }



    public void addError( String code, String desc) {
        setErrorCode(code);
        setErrorDescription(desc);
    }

    public void addPInfo(String City,
                           String StateProvinceCode,
                           String CountryCode,
                           String ShipmentWeight,
                           String WeightUnits,
                           String CarrierCode,
                           String Service,
                           String ShipDate,
                           String DeliveredDate,
                           String DeliveredTime) {
        address = new org.store.core.utils.carriers.Address("","",City, StateProvinceCode, "", CountryCode);
        shipmentWeight = ShipmentWeight + " " + WeightUnits;
        serviceCode = CarrierCode;
        serviceDesc = Service;
        shipDate = ShipDate;
        deliveryDate = DeliveredDate;
        deliveryTime = DeliveredTime;
    }

    public void addPackage(String City,
                           String StateProvinceCode,
                           String PostalCode,
                           String CountryCode,
                           String StatusTypeCode,
                           String StatusTypeDescription,
                           String Date,
                           String Time) {
        if (packages==null) packages = new ArrayList<TrackPackage>();
        PackageTrackResult pack = new PackageTrackResult();
        pack.setAlCity(City);
        pack.setAlState(StateProvinceCode);
        pack.setAlCode(PostalCode);
        pack.setAlCountry(CountryCode);
        pack.setStatusTypeCode(StatusTypeCode);
        pack.setStatusTypeDescription(StatusTypeDescription);
        pack.setActivityDate(Date);
        pack.setActivityTime(Time);
        packages.add(pack);
    }

}
