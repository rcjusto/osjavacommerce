package org.store.core.beans;

import org.store.core.beans.utils.StoreBean;
import org.store.core.globals.CountryFactory;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.struts2.json.JSONException;
import org.apache.struts2.json.JSONUtil;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

/**
 * Proveedor
 */
@Entity
@Table(name="t_provider")
public class Provider extends BaseBean implements StoreBean {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idProvider;

    // Tienda donde esta configurado del proveedor
    @Column(length = 10)
    private String inventaryCode;

    // Nombre del proveedor
    @Column(length = 255)
    private String providerName;

    // Direccion
    @Column(length = 512)
    private String address;
    @Column(length = 255)
    private String city;
    @Column(length = 20)
    private String zipCode;
    @Column(length = 5)
    private String countryCode;
    @Column(length = 255)
    private String phone;
    @Column(length = 512)
    private String email;
    @Column(length = 255)
    private String accountNo;
    @Column(length = 512)
    private String contact;
    @Column(length = 255)
    private String alternateNo;

    @Column(length = 50)
    private String username;
    @Column(length = 255)
    private String password;
    @Column
    private Boolean ownership;

    @ManyToOne
    private State state;

    // Terminos de pago contratado con el proveedor
    @ManyToOne
    private Payterms payterms;

    private Boolean serviceActive;
    @Column(length = 50)
    private String serviceName;
    @Column(length = 50)
    private String serviceFrequency;
    @Lob
    private String serviceConfig;
    @Transient
    Map<String,String> serviceValues;


    public Provider() {
    }


    public Long getIdProvider() {
        return idProvider;
    }

    public void setIdProvider(Long idProvider) {
        this.idProvider = idProvider;
    }

    public String getInventaryCode() {
        return inventaryCode;
    }

    public void setInventaryCode(String inventaryCode) {
        this.inventaryCode = inventaryCode;
    }

    public String getProviderName() {
        return providerName;
    }

    public void setProviderName(String providerName) {
        this.providerName = providerName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
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

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAccountNo() {
        return accountNo;
    }

    public void setAccountNo(String accountNo) {
        this.accountNo = accountNo;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public String getAlternateNo() {
        return alternateNo;
    }

    public void setAlternateNo(String alternateNo) {
        this.alternateNo = alternateNo;
    }

    public Boolean getServiceActive() {
        return serviceActive!=null && serviceActive;
    }

    public void setServiceActive(Boolean serviceActive) {
        this.serviceActive = serviceActive;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getServiceConfig() {
        return serviceConfig;
    }

    public void setServiceConfig(String serviceConfig) {
        this.serviceConfig = serviceConfig;
    }

    public String getServiceFrequency() {
        return serviceFrequency;
    }

    public void setServiceFrequency(String serviceFrequency) {
        this.serviceFrequency = serviceFrequency;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Boolean getOwnership() {
        return ownership;
    }

    public void setOwnership(Boolean ownership) {
        this.ownership = ownership;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public Payterms getPayterms() {
        return payterms;
    }

    public void setPayterms(Payterms payterms) {
        this.payterms = payterms;
    }

    public String getStateCode() {
        return state!=null ? state.getStateCode() : "";
    }

    public String getFullAddress(String lang) {
        StringBuilder buff = new StringBuilder();
        if (!isEmpty(getAddress())) {
            buff.append(getAddress());
        }
        if (!isEmpty(getCity())) {
            if (!isEmpty(buff.toString())) buff.append(", ");
            buff.append(getCity());
        }
        if (getState()!=null && !isEmpty(getState().getStateName())) {
            if (!isEmpty(buff.toString())) buff.append(", ");
            buff.append(getState().getStateName());
        }
        if (!isEmpty(getZipCode())) {
            if (!isEmpty(buff.toString())) buff.append(" ");
            buff.append(getZipCode());
        }
        if (!isEmpty(getCountryCode())) {
            if (!isEmpty(buff.toString())) buff.append(", ");
            buff.append(CountryFactory.getCountryName(getCountryCode(), new Locale(lang)));
        }
        return buff.toString();
    }

    public String getServiceProperty(String propertyName) {
        if (serviceValues == null || serviceValues.size() < 1)
            try {
                serviceValues =  (!isEmpty(serviceConfig)) ? (Map<String, String>) JSONUtil.deserialize(serviceConfig) : null;
            } catch (JSONException e) {
                log.error(e.getMessage(), e);
            }
        return (serviceValues != null && serviceValues.containsKey(propertyName)) ? serviceValues.get(propertyName) : null;
    }

    public void setServiceProperty(String propertyName, String propertyValue ) {
        if (serviceValues==null) serviceValues = new HashMap<String,String>();
        serviceValues.put(propertyName, propertyValue);
        try {
            serviceConfig =  (serviceValues!=null && serviceValues.size()>0) ? JSONUtil.serialize(serviceValues) :null;
        } catch (JSONException e) {
            log.error(e.getMessage(), e);
        }
    }

    public Properties getServiceProperties() {
        Properties p = new Properties();
        if (serviceValues == null || serviceValues.size() < 1)
            try {
                serviceValues = (!isEmpty(serviceConfig)) ? (Map<String, String>) JSONUtil.deserialize(serviceConfig) : null;
            } catch (JSONException e) {
                log.error(e.getMessage(), e);
            }
        if (serviceValues!=null && !serviceValues.isEmpty()) {
            for (String key : serviceValues.keySet()) {
                p.setProperty(key, serviceValues.get(key));
            }
        }
        return p;
    }

    public boolean equals(Object other) {
        if ((this == other)) return true;
        if (!(other instanceof Provider)) return false;
        Provider castOther = (Provider) other;
        return new EqualsBuilder()
                .append(this.getIdProvider(), castOther.getIdProvider())
                .isEquals();
    }

    public String toString() {
        return new ToStringBuilder(this)
            .append("idProvider", getIdProvider())
            .toString();
    }

}
