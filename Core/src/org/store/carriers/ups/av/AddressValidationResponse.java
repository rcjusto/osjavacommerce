package org.store.carriers.ups.av;

import org.apache.commons.digester.Digester;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: Rogelio Caballero Justo
 * Date: 19-nov-2006
 */
public class AddressValidationResponse {

    private String xpciVersion;
    private String responseStatusCode;
    private String responseStatusDescription;

    private ArrayList<AddressResult> addressValidationResults;


    public AddressValidationResponse(InputStream stream) throws IOException, SAXException {
        Digester digester = new Digester();
        digester.push(this);
        digester.addCallMethod("AddressValidationResponse/Response", "addResponse", 3);
        digester.addCallParam("AddressValidationResponse/Response/ResponseStatusCode", 0);
        digester.addCallParam("AddressValidationResponse/Response/ResponseStatusDescription", 1);
        digester.addCallParam("AddressValidationResponse/Response/TransactionReference/XpciVersion", 2);

        digester.addCallMethod("AddressValidationResponse/AddressValidationResult", "addAddress", 6);
        digester.addCallParam("AddressValidationResponse/AddressValidationResult/Rank", 0);
        digester.addCallParam("AddressValidationResponse/AddressValidationResult/Quality", 1);
        digester.addCallParam("AddressValidationResponse/AddressValidationResult/Address/City", 2);
        digester.addCallParam("AddressValidationResponse/AddressValidationResult/Address/StateProvinceCode", 3);
        digester.addCallParam("AddressValidationResponse/AddressValidationResult/PostalCodeLowEnd", 4);
        digester.addCallParam("AddressValidationResponse/AddressValidationResult/PostalCodeHighEnd", 5);

        digester.parse(stream);
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

    public ArrayList<AddressResult> getAddressValidationResults() {
        return addressValidationResults;
    }

    public void setAddressValidationResults(ArrayList<AddressResult> addressValidationResults) {
        this.addressValidationResults = addressValidationResults;
    }

    public boolean hasError() {
        return responseStatusCode != null && !"".equals(responseStatusCode) && !"1".equals(responseStatusCode);
    }

    public boolean isCorrect() {
        if (addressValidationResults != null)
            for (int i = 0; i < addressValidationResults.size(); i++) {
                AddressResult ar = addressValidationResults.get(i);
                if (ar.getQuality() == 1.0) return true;
            }
        return false;
    }

    public void addResponse(String _statusCode, String _statusDescription, String _xpciVersion) {
        setResponseStatusCode(_statusCode);
        setResponseStatusDescription(_statusDescription);
        setXpciVersion(_xpciVersion);
    }

    public void addAddress(String _rank, String _quality, String _city, String _state, String _zipLow, String _zipHigh) {

        int rank;
        try {
            rank = Integer.parseInt(_rank);
        } catch (NumberFormatException e) {
            rank = 0;
        }
        float quality;
        try {
            quality = Float.parseFloat(_quality);
        } catch (NumberFormatException e) {
            quality = 0;
        }

        if (addressValidationResults == null) addressValidationResults = new ArrayList<AddressResult>();

        AddressResult address = findAddress(_city, _state);
        if (address == null) {
            address = new AddressResult();
            address.setRank(rank);
            address.setQuality(quality);
            address.setCity(_city);
            address.setStateProvinceCode(_state);

            addressValidationResults.add(address);
        }
        int zipFrom = Integer.parseInt(_zipLow);
        int zipTo = Integer.parseInt(_zipHigh);

        if (address.getPostalCodes() == null) address.setPostalCodes(new ArrayList<String>());
        for (int i = zipFrom; i <= zipTo; i++) {
            address.getPostalCodes().add(String.valueOf(i));
        }

    }

    private AddressResult findAddress(String city, String state) {
        if (addressValidationResults != null) {
            for (int i = 0; i < addressValidationResults.size(); i++) {
                AddressResult tempAdd = addressValidationResults.get(i);
                if (city.equals(tempAdd.getCity()) && state.equals(tempAdd.getStateProvinceCode())) return tempAdd;
            }
        }
        return null;
    }

}
