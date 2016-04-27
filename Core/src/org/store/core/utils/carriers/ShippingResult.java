package org.store.core.utils.carriers;

import java.util.HashMap;
import java.util.ArrayList;

/**
 * User: Rogelio Caballero Justo
 * Date: 02-07-2007
 * Time: 03:08:55 AM
 */
public class ShippingResult {

    private String shippingNumber;
    private double shipmentCharge;
    private String shipmentChargeCurr;
    HashMap<Long, String> trackingNumbers = new HashMap<Long, String>();
    ArrayList<HashMap<String, String>> errors = new ArrayList<HashMap<String, String>>();

    public String getShippingNumber() {
        return shippingNumber;
    }

    public void setShippingNumber(String shippingNumber) {
        this.shippingNumber = shippingNumber;
    }

    public HashMap<Long, String> getTrackingNumbers() {
        return trackingNumbers;
    }

    public void setTrackingNumbers(HashMap<Long, String> trackingNumbers) {
        this.trackingNumbers = trackingNumbers;
    }

    public double getShipmentCharge() {
        return shipmentCharge;
    }

    public void setShipmentCharge(double shipmentCharge) {
        this.shipmentCharge = shipmentCharge;
    }

    public String getShipmentChargeCurr() {
        return shipmentChargeCurr;
    }

    public void setShipmentChargeCurr(String shipmentChargeCurr) {
        this.shipmentChargeCurr = shipmentChargeCurr;
    }

    public ArrayList<HashMap<String, String>> getErrors() {
        return errors;
    }

    public void setErrors(ArrayList<HashMap<String, String>> errors) {
        this.errors = errors;
    }

    public void addError(String carrier, String code, String descripcion) {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("CARRIER", carrier);
        map.put("CODE", code);
        map.put("ERROR", descripcion);
        errors.add(map);
    }

    public boolean hasErrors() {
        return errors!=null && errors.size()>0;
    }
}

