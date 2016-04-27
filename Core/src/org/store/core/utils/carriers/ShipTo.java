package org.store.core.utils.carriers;

/**
 * Created by IntelliJ IDEA.
 * User: Rogelio Caballero Justo
 * Date: 20-nov-2006
 */
public class ShipTo {

    private String companyName;
    private String attentionName;
    private Address address;
    private StructuredPhoneNumber phoneNumber;

    public ShipTo(String companyName, String attentionName, Address address, StructuredPhoneNumber phone) {
        this.companyName = companyName;
        this.attentionName = attentionName;
        this.address = address;
        this.phoneNumber = phone;
    }


    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getAttentionName() {
        return attentionName;
    }

    public void setAttentionName(String attentionName) {
        this.attentionName = attentionName;
    }

    public StructuredPhoneNumber getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(StructuredPhoneNumber phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public org.store.core.utils.carriers.Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }
}
