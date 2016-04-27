package org.store.core.beans.utils;

import org.apache.commons.lang.StringUtils;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by IntelliJ IDEA.
 * User: Rogelio
 * Date: 1/08/2010
 * Time: 05:10:34 PM
 * To change this template use File | Settings | File Templates.
 */
public class CreditCard {

    private String type;
    private String number;
    private String month;
    private String year;
    private String cvd;
    private String streetNumber;
    private String streetName;
    private String postalCode;
    private String firstName;
    private String lastName;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getCvd() {
        return cvd;
    }

    public void setCvd(String cvd) {
        this.cvd = cvd;
    }

    public String getStreetNumber() {
        return streetNumber;
    }

    public void setStreetNumber(String streetNumber) {
        this.streetNumber = streetNumber;
    }

    public String getStreetName() {
        return streetName;
    }

    public void setStreetName(String streetName) {
        this.streetName = streetName;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public boolean isValid() {
        return StringUtils.isNotEmpty(number) && StringUtils.isNotEmpty(month) && StringUtils.isNotEmpty(year);
    }

    public static CreditCard fromRequest(HttpServletRequest request, String prefix) {
        CreditCard card = new CreditCard();
        if (StringUtils.isNotEmpty(request.getParameter(prefix+".type"))) card.setType(request.getParameter(prefix+".type"));
        if (StringUtils.isNotEmpty(request.getParameter(prefix+".number"))) card.setNumber(request.getParameter(prefix+".number"));
        if (StringUtils.isNotEmpty(request.getParameter(prefix+".month"))) card.setMonth(request.getParameter(prefix+".month"));
        if (StringUtils.isNotEmpty(request.getParameter(prefix+".year"))) card.setYear(request.getParameter(prefix+".year"));
        if (StringUtils.isNotEmpty(request.getParameter(prefix+".cvd"))) card.setCvd(request.getParameter(prefix+".cvd"));
        if (StringUtils.isNotEmpty(request.getParameter(prefix+".streetNumber"))) card.setStreetNumber(request.getParameter(prefix+".streetNumber"));
        if (StringUtils.isNotEmpty(request.getParameter(prefix+".streetName"))) card.setStreetName(request.getParameter(prefix+".streetName"));
        if (StringUtils.isNotEmpty(request.getParameter(prefix+".postalCode"))) card.setPostalCode(request.getParameter(prefix+".postalCode"));
        if (StringUtils.isNotEmpty(request.getParameter(prefix+".firstName"))) card.setFirstName(request.getParameter(prefix+".firstName"));
        if (StringUtils.isNotEmpty(request.getParameter(prefix+".lastName"))) card.setFirstName(request.getParameter(prefix+".lastName"));
        return card;
    }
}
