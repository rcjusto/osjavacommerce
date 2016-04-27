package org.store.core.utils.carriers;

import java.util.Properties;

/**
 * Created by IntelliJ IDEA.
 * User: Rogelio Caballero Justo
 * Date: 20-nov-2006
 */
public class Shipper {

    private String name;
    private String attentionName;
    private String shipperNumber;
    private String phoneNumber;
    private String email;
    private org.store.core.utils.carriers.Address address;


    public Shipper(String name, String attentionName, String shipperNumber, String phoneNumber, String email, org.store.core.utils.carriers.Address address) {
        this.name = name;
        this.attentionName = attentionName;
        this.shipperNumber = shipperNumber;
        this.phoneNumber = phoneNumber;
        this.address = address;
        this.email = email;
    }

    public Shipper(Properties prop) {
        this.name = prop.getProperty("shipper.name");
        this.attentionName = prop.getProperty("shipper.attention.name");
        this.shipperNumber = prop.getProperty("shipper.shipper.number");
        this.phoneNumber = prop.getProperty("shipper.phone.number");
        this.email = prop.getProperty("shipper.email");

        this.address = new org.store.core.utils.carriers.Address();
        this.address.setAddressLine1(prop.getProperty("shipper.address.line1"));
        this.address.setAddressLine2(prop.getProperty("shipper.address.line2"));
        this.address.setCity(prop.getProperty("shipper.address.city"));
        this.address.setCountryCode(prop.getProperty("shipper.address.country"));
        this.address.setPostalCode(prop.getProperty("shipper.address.postalcode"));
        this.address.setStateProvinceCode(prop.getProperty("shipper.address.state"));
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAttentionName() {
        return attentionName;
    }

    public void setAttentionName(String attentionName) {
        this.attentionName = attentionName;
    }

    public String getShipperNumber() {
        return shipperNumber;
    }

    public void setShipperNumber(String shipperNumber) {
        this.shipperNumber = shipperNumber;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public org.store.core.utils.carriers.Address getAddress() {
        return address;
    }

    public void setAddress(org.store.core.utils.carriers.Address address) {
        this.address = address;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
