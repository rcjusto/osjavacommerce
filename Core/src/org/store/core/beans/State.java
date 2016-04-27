package org.store.core.beans;

import org.store.core.beans.utils.StoreBean;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Estado o provincia
 */
@Entity
@Table(name = "t_state")
public class State extends BaseBean implements StoreBean {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idState;

    // Tienda donde esta configurado la provincia
    @Column(length = 10)
    private String inventaryCode;

    // Codigo del pais
    @Column(length = 5)
    private String countryCode;

    // Codigo del estado
    @Column(length = 5)
    private String stateCode;

    // Nombre del estado
    @Column(length = 255)
    private String stateName;


    public State() {
    }


    public Long getIdState() {
        return idState;
    }

    public void setIdState(Long idState) {
        this.idState = idState;
    }

    public String getStateCode() {
        return stateCode;
    }

    public void setStateCode(String stateCode) {
        this.stateCode = stateCode;
    }

    public String getInventaryCode() {
        return inventaryCode;
    }

    public void setInventaryCode(String inventaryCode) {
        this.inventaryCode = inventaryCode;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getStateName() {
        return this.stateName;
    }

    public void setStateName(String stateName) {
        this.stateName = stateName;
    }

    public boolean equals(Object other) {
        if ((this == other)) return true;
        if (!(other instanceof State)) return false;
        State castOther = (State) other;
        return new EqualsBuilder()
                .append(this.getIdState(), castOther.getIdState())
                .isEquals();
    }

    public String toString() {
        return new ToStringBuilder(this)
                .append("idState", getIdState())
                .toString();
    }

}
