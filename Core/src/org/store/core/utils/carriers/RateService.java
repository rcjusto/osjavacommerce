package org.store.core.utils.carriers;

import org.store.core.beans.ShippingMethod;
import org.apache.log4j.Logger;

/**
 * Created by IntelliJ IDEA.
 * User: Rogelio Caballero Justo
 * Date: 20-nov-2006
 */
public class RateService {
    public static Logger log = Logger.getLogger(RateService.class);
    private String code;
    private String currencyCode;
    private float value;
    private String daysToDelivery;
    private String deliveryDate;
    private Boolean am;
    private String carrier;
    private ShippingMethod method;


    public RateService(String code, String currencyCode, String value, String daysToDelivery) {
        this.code = code;
        this.currencyCode = currencyCode;

        float _value = 0;
        try {
            _value = Float.parseFloat(value);
        } catch (NumberFormatException e) {
            log.error(e.getMessage(), e);
        }
        this.value = _value;

        this.daysToDelivery = daysToDelivery;
    }

    public RateService(String code, String currencyCode, String value, String daysToDelivery, String deliveryDate, Boolean am) {
        this.code = code;
        this.currencyCode = currencyCode;

        float _value = 0;
        try {
            _value = Float.parseFloat(value);
        } catch (NumberFormatException e) {
            log.error(e.getMessage(), e);
        }
        this.value = _value;
        this.daysToDelivery = daysToDelivery;
        this.deliveryDate = deliveryDate;
        this.am = am;
    }

    public String getCarrier() {
        return carrier;
    }

    public void setCarrier(String carrier) {
        this.carrier = carrier;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public float getValue() {
        return value;
    }

    public void setValue(float value) {
        this.value = value;
    }

    public String getDaysToDelivery() {
        return daysToDelivery;
    }

    public void setDaysToDelivery(String daysToDelivery) {
        this.daysToDelivery = daysToDelivery;
    }

    public String getDeliveryDate() {
        return deliveryDate;
    }

    public void setDeliveryDate(String deliveryDate) {
        this.deliveryDate = deliveryDate;
    }

    public Boolean getAm() {
        return am;
    }

    public void setAm(Boolean am) {
        this.am = am;
    }

    public ShippingMethod getMethod() {
        return method;
    }

    public void setMethod(ShippingMethod method) {
        this.method = method;
    }
}
