package org.store.core.beans;

import org.apache.commons.lang.builder.ToStringBuilder;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * Preferencias del usuario
 */
@Entity
@Table(name="t_user_preference")
public class UserPreference extends BaseBean {
    public static final String STOCK_ALERT = "STOCK_ALERT";
    public static final String FEE_EXEMPTION = "FEE_EXEMPTION";
    public static final String TAX_EXEMPTION = "TAX_EXEMPTION";

    /** identifier field */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idPreference;

    /** codigo */
    @Column(length = 255)
    private String preferenceCode;

    /** valor */
    @Lob
    private String preferenceValue;

    /** persistent field */
    @ManyToOne
    private User user;

    public UserPreference() {
    }

    public Long getIdPreference() {
        return idPreference;
    }

    public void setIdPreference(Long idPreference) {
        this.idPreference = idPreference;
    }

    public String getPreferenceCode() {
        return preferenceCode;
    }

    public void setPreferenceCode(String preferenceCode) {
        this.preferenceCode = preferenceCode;
    }

    public String getPreferenceValue() {
        return preferenceValue;
    }

    public void setPreferenceValue(String preferenceValue) {
        this.preferenceValue = preferenceValue;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String toString() {
        return new ToStringBuilder(this)
            .append("id", getIdPreference())
            .toString();
    }

}