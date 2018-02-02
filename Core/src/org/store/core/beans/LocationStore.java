package org.store.core.beans;

import org.store.core.beans.utils.StoreBean;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * Taxes
 */
@Entity
@Table(name = "t_phisical_store")
public class LocationStore extends BaseBean implements StoreBean {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Tienda en la q esta configurado el status
    @Column(length = 10)
    private String inventaryCode;

    @Column(length = 512)
    private String storeName;

    @Column(length = 512)
    private String email;

    @Column(length = 255)
    private String address;
    @Column(length = 255)
    private String address2;
    @Column(length = 255)
    private String city;
    @Column(length = 12)
    private String zipCode;
    @Column(length = 5)
    private String idCountry;
    @Column(length = 255)
    private String phone;
    @Column(length = 255)
    private String fax;
    @ManyToOne
    private State state;

    // used in pick in store delivery mode
    private Boolean active;

    // used for physical address of store
    private Boolean main;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getInventaryCode() {
        return inventaryCode;
    }

    public void setInventaryCode(String inventaryCode) {
        this.inventaryCode = inventaryCode;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public String getStoreName() {
        return storeName;
    }

    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getAddress2() {
        return address2;
    }

    public void setAddress2(String address2) {
        this.address2 = address2;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    public String getIdCountry() {
        return idCountry;
    }

    public void setIdCountry(String idCountry) {
        this.idCountry = idCountry;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getFax() {
        return fax;
    }

    public void setFax(String fax) {
        this.fax = fax;
    }

    public Boolean getActive() {
        return active!=null && active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Boolean getMain() {
        return main!=null && main;
    }

    public void setMain(Boolean main) {
        this.main = main;
    }

    public String getStateCode() {
        return state!=null ? state.getStateCode() : "";
    }

    public String toString() {
        return new ToStringBuilder(this)
                .append("id", getId())
                .toString();
    }

    public String getFullAddress(boolean all) {
        StringBuffer buffer = new StringBuffer();
        if (StringUtils.isNotEmpty(storeName)) buffer.append(storeName);
        if (StringUtils.isNotEmpty(address)) {
            if (StringUtils.isNotEmpty(buffer.toString())) buffer.append(", ");
            buffer.append(address);
        }
        if (StringUtils.isNotEmpty(address2)) {
            if (StringUtils.isNotEmpty(buffer.toString())) buffer.append(", ");
            buffer.append(address2);
        }
        if (StringUtils.isNotEmpty(city)) {
            if (StringUtils.isNotEmpty(buffer.toString())) buffer.append(", ");
            buffer.append(city);
        }
        if (state!=null) {
            if (StringUtils.isNotEmpty(buffer.toString())) buffer.append(", ");
            buffer.append(state.getStateName());
        }
        if (StringUtils.isNotEmpty(zipCode)) {
            if (StringUtils.isNotEmpty(buffer.toString())) buffer.append(" ");
            buffer.append(zipCode);
        }
        if (all) {
            if (StringUtils.isNotEmpty(idCountry)) {
                if (StringUtils.isNotEmpty(buffer.toString())) buffer.append(", ");
                buffer.append(idCountry);
            }
            if (StringUtils.isNotEmpty(email)) {
                if (StringUtils.isNotEmpty(buffer.toString())) buffer.append(", ");
                buffer.append(email);
            }
            if (StringUtils.isNotEmpty(phone)) {
                if (StringUtils.isNotEmpty(buffer.toString())) buffer.append(", ");
                buffer.append(phone);
            }
        }
        return buffer.toString();
    }

}