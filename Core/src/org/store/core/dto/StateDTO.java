package org.store.core.dto;

import org.store.core.beans.State;
import org.apache.commons.lang.StringEscapeUtils;

public class StateDTO {

    private Long idState;
    private String countryCode;
    private String stateName;

    public StateDTO(State bean) {
        this.idState = bean.getIdState();
        this.countryCode = bean.getCountryCode();
        this.stateName = bean.getStateName();
    }

    public Long getIdState() {
        return idState;
    }

    public void setIdState(Long idState) {
        this.idState = idState;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getStateName() {
        return stateName;
    }

    public String getHtmlStateName() {
        return StringEscapeUtils.escapeHtml(stateName);
    }

    public void setStateName(String stateName) {
        this.stateName = stateName;
    }


}
