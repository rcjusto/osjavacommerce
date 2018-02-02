package org.store.core.beans;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;


@Entity
@Table(name="t_user_address")
public class UserAddress extends BaseBean {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idAddress;

    @Column(length = 255)
    private String code;
    @Column(length = 255)
    private String firstname;
    @Column(length = 255)
    private String lastname;
    @Column(length = 50)
    private String title;
    @Column(length = 255)
    private String company;
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
    private Boolean billing;
    private Boolean shipping;

    @Column(length = 255)
    private String stateName;
    @ManyToOne
    private State state;
    @ManyToOne
    private User user;

    @Column(length = 255)
    private String externalCode;

    public UserAddress() {
    }
    /*
    public UserAddress(Map map) {
        if (map!=null) {
            idAddress = map.containsKey("idAddress") ? SomeUtils.strToLong((String) map.get("idAddress")) : null;
            code  = map.containsKey("code") ? (String) map.get("code") : null;
            firstname  = map.containsKey("firstname") ? (String) map.get("firstname") : null;
            lastname  = map.containsKey("lastname") ? (String) map.get("lastname") : null;
            title  = map.containsKey("title") ? (String) map.get("title") : null;
            company  = map.containsKey("company") ? (String) map.get("company") : null;
            address  = map.containsKey("address") ? (String) map.get("address") : null;
            address2  = map.containsKey("address2") ? (String) map.get("address2") : null;
            city  = map.containsKey("city") ? (String) map.get("city") : null;
            zipCode = map.containsKey("zipCode") ? (String) map.get("zipCode") : null;
            idCountry = map.containsKey("idCountry") ? (String) map.get("idCountry") : null;
            phone  = map.containsKey("phone") ? (String) map.get("phone") : null;
            fax  = map.containsKey("fax") ? (String) map.get("fax") : null;
            billing  = map.containsKey("billing") && "true".equalsIgnoreCase((String) map.get("billing"));
            shipping  = map.containsKey("shipping") && "true".equalsIgnoreCase((String) map.get("shipping"));

            Long idState = map.containsKey("state") ? SomeUtils.strToLong((String) map.get("state")) : null;
            state = (State) getDao().get(State.class, idState);

            Long idUser = map.containsKey("user") ? SomeUtils.strToLong((String) map.get("user")) : null;
            user = (User) getDao().get(User.class, idUser);
        }
    }
    */
    public Long getIdAddress() {
        return idAddress;
    }

    public void setIdAddress(Long idAddress) {
        this.idAddress = idAddress;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
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

    public Boolean getBilling() {
        return billing!=null && billing;
    }

    public void setBilling(Boolean billing) {
        this.billing = billing;
    }

    public Boolean getShipping() {
        return shipping!=null && shipping;
    }

    public void setShipping(Boolean shipping) {
        this.shipping = shipping;
    }

    public String getExternalCode() {
        return externalCode;
    }

    public void setExternalCode(String externalCode) {
        this.externalCode = externalCode;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getStateCode() {return (state==null || "*".equalsIgnoreCase(state.getStateName())) ? this.stateName : state.getStateCode();}

    public String getStateName() {return (state==null || "*".equalsIgnoreCase(state.getStateName())) ? this.stateName : state.getStateName();}

    public void setStateName(String stateName) {
        this.stateName = stateName;
    }

    public String getFullName() {
        StringBuffer buffer = new StringBuffer();
        if (StringUtils.isNotEmpty(title)) buffer.append(title);
        if (StringUtils.isNotEmpty(firstname)) {
            if (StringUtils.isNotEmpty(buffer.toString())) buffer.append(" ");
            buffer.append(firstname);
        }
        if (StringUtils.isNotBlank(lastname)) {
            if (StringUtils.isNotEmpty(buffer.toString())) buffer.append(" ");
            buffer.append(lastname);
        }
        return buffer.toString();
    }

    public String getFullAddress(boolean all) {
        StringBuffer buffer = new StringBuffer();
        if (StringUtils.isNotEmpty(company)) buffer.append(company);
        if (StringUtils.isNotEmpty(getFullName())) {
            if (StringUtils.isNotEmpty(buffer.toString())) buffer.append(", ");
            buffer.append(getFullName());
        }
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
        if (getStateName()!=null) {
            if (StringUtils.isNotEmpty(buffer.toString())) buffer.append(", ");
            buffer.append(getStateName());
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
            if (StringUtils.isNotEmpty(phone)) {
                if (StringUtils.isNotEmpty(buffer.toString())) buffer.append(", ");
                buffer.append(phone);
            }
        }
        return buffer.toString();
    }

    public String toString() {
        return new ToStringBuilder(this)
            .append("id", getIdAddress())
            .toString();
    }

    public boolean equals(Object other) {
        if ((this == other)) return true;
        if (!(other instanceof UserAddress)) return false;
        UserAddress castOther = (UserAddress) other;
        return new EqualsBuilder()
                .append(this.getIdAddress(), castOther.getIdAddress())
                .isEquals();
    }

    public Map toMap() {
        Map map = null;
        try {
            map = BeanUtils.describe(this);
            map.put("user", (user!=null && user.getIdUser()!=null) ? user.getIdUser().toString() : "");
            map.put("state",(state!=null && state.getIdState()!=null) ? state.getIdState().toString() : "");
        } catch (IllegalAccessException e) {
            log.error(e.getMessage(), e); 
        } catch (InvocationTargetException e) {
            log.error(e.getMessage(), e); 
        } catch (NoSuchMethodException e) {
            log.error(e.getMessage(), e); 
        }
        return map;
    }

}