package org.store.core.utils.merchants;

import org.store.core.beans.Order;
import org.store.core.beans.StoreProperty;
import org.store.core.front.FrontModuleAction;
import org.store.core.globals.BaseAction;
import org.store.core.globals.SomeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.util.Map;
import java.util.Properties;

public abstract class MerchantService {

    public static Logger log = Logger.getLogger(MerchantService.class);
    public static final String TYPE_CREDIT_CARD = "credit.card";
    public static final String TYPE_OFFLINE = "offline";
    public static final String TYPE_HOSTED_PAGE = "hosted.page";
    public static final String TYPE_EXTERNAL = "external";
    public static final String TYPE_CUSTOM = "custom";

    protected static final String PROPERTY_ACTIVE = "active";
    protected static final String PROPERTY_BLOCK_CODE = "block_code";
    protected static final String PROPERTY_SALES_COMISION = "salesComision";

    public abstract boolean validatePayment(Order order, BaseAction action);
    public abstract PaymentResult doPayment(Order order, BaseAction action);
    public abstract PaymentResult doRequestStatus(Order order, BaseAction action);
    public abstract Double getInterestPercent(BaseAction action);

    public abstract Map preparePaymentRedirection(Order order, BaseAction action);
    public abstract String doPaymentRedirection(FrontModuleAction action);

    public abstract String getCode();
    public abstract String getLabel();
    public abstract String getType();
    public abstract String getError();
    public abstract String getForm(BaseAction action);

    public abstract String getPropertiesForm(BaseAction action);

    public void savePropertiesForm(BaseAction action){
        properties.setProperty(PROPERTY_SALES_COMISION, getRequestParam(action, PROPERTY_SALES_COMISION, ""));
        properties.setProperty(PROPERTY_ACTIVE, getRequestParam(action, PROPERTY_ACTIVE, ""));
        saveProperties(action);
    }

    protected Properties properties;

    public String getBlockCode() {
        return properties!=null ? properties.getProperty(PROPERTY_BLOCK_CODE) : null;
    }

    public boolean isActive() {
        return (properties != null) && "Y".equalsIgnoreCase(properties.getProperty(PROPERTY_ACTIVE));
    }

    public Double getSaleComision() {
        return (properties!=null) ? SomeUtils.strToDouble(properties.getProperty(PROPERTY_SALES_COMISION)) : null;
    }

    public void loadProperties(BaseAction action) {
        properties = action.getDao().getMerchantProperties(getCode());
    }

    public void saveProperties(BaseAction action) {
        for(String key : properties.stringPropertyNames()) {
            StoreProperty propBean = action.getDao().getStoreProperty(key, "MERCHANT_" + getCode(), true);
            propBean.setValue(properties.getProperty(key));
            action.getDao().save(propBean);
        }
    }

    public String getProperty(String propName, String defaultValue) {
        return (properties!=null && properties.containsKey(propName)) ? properties.getProperty(propName) : defaultValue;
    }

    public String getRequestParam(BaseAction action, String paramName, String defaultValue) {
        String paramValue = (action!=null && action.getRequest()!=null) ? action.getRequest().getParameter(paramName) : null;
        return (!StringUtils.isEmpty(paramValue)) ? paramValue : defaultValue;
    }

}
