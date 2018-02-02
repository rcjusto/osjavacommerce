package org.store.merchants.moneris;

import org.store.core.beans.Order;
import org.store.core.beans.UserAddress;
import org.store.core.beans.utils.CreditCard;
import org.store.core.front.FrontModuleAction;
import org.store.core.globals.BaseAction;
import org.store.core.globals.StoreHtmlField;
import org.store.core.utils.merchants.MerchantService;
import org.store.core.utils.merchants.PaymentResult;
import org.apache.commons.lang.StringUtils;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class MonerisHPPServiceImpl extends MerchantService {

    private static final String MONERIS_HOST_LIVE = "https://www3.moneris.com/HPPDP/index.php";
    private static final String MONERIS_HOST_TEST = "https://esqa.moneris.com/HPPDP/index.php";
  //  private static final String MONERIS_HOST_TEST = "http://www.store2.cu:8080/test/monerishpp.php";
    private static final String FIELD_STATUS = "moneris_status";
    private static final String FIELD_STORE_ID = "moneris_store_id";
    private static final String FIELD_HPP_KEY = "moneris_hpp_key";

    public boolean validatePayment(Order order, BaseAction action) {
        return true;
    }

    public PaymentResult doPayment(Order order, BaseAction action) {
        return null;
    }

    public PaymentResult doRequestStatus(Order order, BaseAction action) {
        return null;
    }

    @Override
    public Double getInterestPercent(BaseAction action) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Map preparePaymentRedirection(Order order, BaseAction action) {
        Map<String, String> map = new HashMap<String, String>();
        map.put("__url", ("live".equalsIgnoreCase(properties.getProperty("moneris.status"))) ? MONERIS_HOST_LIVE : MONERIS_HOST_TEST );
        map.put("__method","post");
        map.put("ps_store_id",properties.getProperty("moneris.ps.store.id"));
        map.put("hpp_key",properties.getProperty("moneris.hpp.key"));

        DecimalFormatSymbols dfs = new DecimalFormatSymbols(new Locale("en"));
        dfs.setDecimalSeparator('.');
        DecimalFormat df = new DecimalFormat("0.00",dfs);
        map.put("charge_total",df.format(order.getTotal()));
        map.put("order_id",order.getIdOrder().toString());
        if ("en".equalsIgnoreCase(action.getLocale().getLanguage())) map.put("lang","en-ca");
        else if ("fr".equalsIgnoreCase(action.getLocale().getLanguage())) map.put("lang","fr-ca");
        if (order.getUser()!=null && StringUtils.isNotEmpty(order.getUser().getEmail())) map.put("email", order.getUser().getEmail());

        addAddress(map, "bill", order.getBillingAddress());
        addAddress(map, "ship", order.getDeliveryAddress());

        return map;
    }

    public String doPaymentRedirection(FrontModuleAction action) {
        return null;
    }

    public boolean useAVS(CreditCard card) {
        return false;
    }

    public boolean useCVD(CreditCard card) {
        return false;
    }

    public String[] getCardTypes() {
        return new String[0];  //To change body of implemented methods use File | Settings | File Templates.
    }

    private void addAddress(Map<String,String> map, String prefix, UserAddress address) {
        if (address!=null) {
            if (StringUtils.isNotEmpty(address.getFirstname())) map.put(prefix+"_first_name", address.getFirstname());
            if (StringUtils.isNotEmpty(address.getLastname())) map.put(prefix+"_last_name", address.getLastname());
            if (StringUtils.isNotEmpty(address.getCompany())) map.put(prefix+"_company_name", address.getCompany());
            if (StringUtils.isNotEmpty(address.getAddress())) map.put(prefix+"_address_one", address.getAddress());
            if (StringUtils.isNotEmpty(address.getCity())) map.put(prefix+"_city", address.getCity());
            if (address.getState()!=null) map.put(prefix+"_state_or_province", address.getState().getStateName());
            if (StringUtils.isNotEmpty(address.getZipCode())) map.put(prefix+"_postal_code", address.getZipCode());
            if (StringUtils.isNotEmpty(address.getPhone())) map.put(prefix+"_phone", address.getPhone());
            if (StringUtils.isNotEmpty(address.getIdCountry())) map.put(prefix+"_country", address.getIdCountry());
        }
    }

    public String getCode() {
        return MonerisHPPEventImpl.PAYMENT_SERVICE_NAME;
    }

    public String getLabel() {
        return "moneris.hpp";
    }

    public String getType() {
        return MerchantService.TYPE_HOSTED_PAGE;
    }

    public String getError() {
        return null;
    }

    public String getForm(BaseAction action) {
        return null;
    }

    @Override
    public String getPropertiesForm(BaseAction action) {
        StringBuffer form = new StringBuffer();

        form.append(new StoreHtmlField(StoreHtmlField.TYPE_SELECT, FIELD_STATUS).addClasses("field string-100")
                .addOption("test", action.getText("test", "Test"))
                .addOption("live", action.getText("live", "Live"))
                .setLabel(action.getText("status", "Status"))
                .setValue(getProperty("moneris.status", "test"))
                .getTableRow());

        form.append(new StoreHtmlField(StoreHtmlField.TYPE_INPUT, FIELD_STORE_ID).addClasses("field string-100")
                .setLabel(action.getText("moneris.ps.store.id", "Store ID")).setValue(getProperty("moneris.ps.store.id", ""))
                .getTableRow());

        form.append(new StoreHtmlField(StoreHtmlField.TYPE_INPUT, FIELD_HPP_KEY).addClasses("field string-100")
                .setLabel(action.getText("moneris.hpp.key", "HPP Key")).setValue(getProperty("moneris.hpp.key", ""))
                .getTableRow());

        return form.toString();
    }

    @Override
    public void savePropertiesForm(BaseAction action) {
        properties.setProperty("moneris.status", getRequestParam(action, FIELD_STATUS, "test"));
        properties.setProperty("moneris.ps.store.id", getRequestParam(action, FIELD_STORE_ID, ""));
        properties.setProperty("moneris.hpp.key", getRequestParam(action, FIELD_HPP_KEY, ""));
        super.savePropertiesForm(action);
    }

}