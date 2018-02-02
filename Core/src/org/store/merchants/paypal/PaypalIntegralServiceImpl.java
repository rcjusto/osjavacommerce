package org.store.merchants.paypal;

import com.opensymphony.xwork2.util.logging.Logger;
import com.opensymphony.xwork2.util.logging.LoggerFactory;
import com.paypal.sdk.core.nvp.NVPDecoder;
import com.paypal.sdk.core.nvp.NVPEncoder;
import com.paypal.sdk.exceptions.PayPalException;
import com.paypal.sdk.profiles.APIProfile;
import com.paypal.sdk.profiles.ProfileFactory;
import com.paypal.sdk.services.CallerServices;
import com.paypal.sdk.services.NVPCallerServices;
import com.paypal.soap.api.DetailLevelCodeType;
import com.paypal.soap.api.GetTransactionDetailsRequestType;
import com.paypal.soap.api.GetTransactionDetailsResponseType;
import com.paypal.soap.api.PaymentTransactionType;
import org.store.core.admin.AdminModuleAction;
import org.store.core.beans.Order;
import org.store.core.beans.OrderStatus;
import org.store.core.beans.StaticText;
import org.store.core.front.FrontModuleAction;
import org.store.core.globals.BaseAction;
import org.store.core.globals.StoreHtmlField;
import org.store.core.utils.events.EventService;
import org.store.core.utils.events.EventUtils;
import org.store.core.utils.merchants.MerchantService;
import org.store.core.utils.merchants.PaymentResult;
import org.apache.commons.lang.StringUtils;

import javax.servlet.ServletContext;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class PaypalIntegralServiceImpl extends MerchantService {

    private static final String FIELD_ENVIRONMENT = "paypal_hpp_enviroment";
    private static final String FIELD_HPP_ID = "paypal_hpp_id";
    private static final String FIELD_USERNAME = "paypal_direct_username";
    private static final String FIELD_PASSWORD = "paypal_direct_password";
    private static final String FIELD_SIGNATURE = "paypal_direct_signature";

    private Double thredhold;
    private String error;
    private static final String PAYPAL_HPP_LIVE = "https://securepayments.paypal.com/cgi-bin/acquiringweb";
    private static final String PAYPAL_HPP_SANDBOX = "https://securepayments.sandbox.paypal.com/acquiringweb";
    private static final Logger LOG = LoggerFactory.getLogger(PaypalIntegralServiceImpl.class);


    public boolean validatePayment(Order order, BaseAction action) {
        return true;
    }

    public PaymentResult doPayment(Order order, BaseAction action) {
        return null;
    }

    public PaymentResult doRequestStatus(Order order, BaseAction action) {
        if (order != null && PaypalIntegralEventImpl.PAYPAL_HOSTED_PAGE.equalsIgnoreCase(order.getPaymentMethod()) && StringUtils.isNotEmpty(order.getCodeMerchant())) {
            Map paymentResult = getTransactionDetailsNVP(order.getCodeMerchant());
            if (paymentResult != null) {
                String ack = (String) paymentResult.get("ACK");
                if (ack != null && ack.startsWith("Success")) {
                    PaymentResult result = new PaymentResult();
                    result.setTransactionResult(PaymentResult.RESULT_PENDING);
                    result.setTransactionInfo(paymentResult);
                    String paymentStatus = (String) paymentResult.get("PAYMENTSTATUS");
                    if ("Pending".equalsIgnoreCase(paymentStatus)) {
                        result.setTransactionResult(PaymentResult.RESULT_PENDING);
                    } else if ("Completed".equalsIgnoreCase(paymentStatus) || "Processed".equalsIgnoreCase(paymentStatus)) {
                        // Aceptar
                        result.setTransactionResult(PaymentResult.RESULT_ACCEPTED);
                    } else {
                        // Rechazar
                        result.setTransactionResult(PaymentResult.RESULT_REJECTED);
                    }
                    return result;
                }
            }
        }
        return null;
    }

    @Override
    public Double getInterestPercent(BaseAction action) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Map preparePaymentRedirection(Order order, BaseAction action) {
        Map<String, String> map = new HashMap<String, String>();
        map.put("__url", ("live".equalsIgnoreCase(properties.getProperty("paypal.enviroment"))) ? PAYPAL_HPP_LIVE : PAYPAL_HPP_SANDBOX);
        map.put("__method", "post");
        map.put("business", properties.getProperty("paypal.hpp.id"));
        map.put("cmd", "_hosted-payment");
        map.put("METHOD", "Pay");

        // urls
        map.put("cancel_return", action.url("shopcart", "", null, true));
        map.put("return", action.url(PaypalIntegralEventImpl.ACTION_RETURN, "", null, true));
        map.put("notify_url", action.url(PaypalIntegralEventImpl.ACTION_NOTIFY, "", null, true));

        map.put("currency_code", action.getActualCurrency().getCode());
        DecimalFormatSymbols dfs = new DecimalFormatSymbols(new Locale("en"));
        dfs.setDecimalSeparator('.');
        DecimalFormat df = new DecimalFormat("0.00", dfs);
        map.put("paymentaction", "sale");
        map.put("subtotal", df.format(order.getTotal()));
        map.put("invoice", order.getIdOrder().toString());
        if (order.getUser() != null && StringUtils.isNotEmpty(order.getUser().getEmail())) map.put("buyer_email", order.getUser().getEmail());

        return map;
    }

    public String doPaymentRedirection(FrontModuleAction action) {
        return null;
    }

    public String getCode() {
        return PaypalIntegralEventImpl.PAYPAL_HOSTED_PAGE;
    }

    public String getLabel() {
        return "paypal.hpp";
    }

    public String getType() {
        return MerchantService.TYPE_HOSTED_PAGE;
    }

    public String getError() {
        return error;
    }

    public String getForm(BaseAction action) {
        return null;
    }

    @Override
    public String getPropertiesForm(BaseAction action) {
        StringBuffer form = new StringBuffer();

        form.append(new StoreHtmlField(StoreHtmlField.TYPE_SELECT, FIELD_ENVIRONMENT).addClasses("field string-100")
                .addOption("sandbox", action.getText("sandbox", "SandBox"))
                .addOption("live", action.getText("live", "Live"))
                .setLabel(action.getText("environment", "Environment"))
                .setValue(getProperty("paypal.enviroment", "sandbox"))
                .getTableRow());

        form.append(new StoreHtmlField(StoreHtmlField.TYPE_INPUT, FIELD_USERNAME).addClasses("field string-100")
                .setLabel(action.getText("paypal.api.username", "Username")).setValue(getProperty("paypal.api.username", ""))
                .getTableRow());

        form.append(new StoreHtmlField(StoreHtmlField.TYPE_INPUT, FIELD_PASSWORD).addClasses("field string-100")
                .setLabel(action.getText("paypal.api.password", "Password")).setValue(getProperty("paypal.api.password", ""))
                .getTableRow());

        form.append(new StoreHtmlField(StoreHtmlField.TYPE_INPUT, FIELD_SIGNATURE).addClasses("field string-200")
                .setLabel(action.getText("paypal.signature", "Signature")).setValue(getProperty("paypal.signature", ""))
                .getTableRow());

        form.append(new StoreHtmlField(StoreHtmlField.TYPE_INPUT, FIELD_HPP_ID).addClasses("field string-100")
                .setLabel(action.getText("paypal.hpp.id", "ID")).setValue(getProperty("paypal.hpp.id", ""))
                .getTableRow());

        return form.toString();
    }

    @Override
    public void savePropertiesForm(BaseAction action) {
        properties.setProperty("paypal.hpp.id", getRequestParam(action, FIELD_HPP_ID, ""));
        properties.setProperty("paypal.enviroment", getRequestParam(action, FIELD_ENVIRONMENT, ""));
        properties.setProperty("paypal.api.username", getRequestParam(action, FIELD_USERNAME, ""));
        properties.setProperty("paypal.api.password", getRequestParam(action, FIELD_PASSWORD, ""));
        properties.setProperty("paypal.signature", getRequestParam(action, FIELD_SIGNATURE, ""));
        super.savePropertiesForm(action);
    }

    public Map getTransactionDetailsNVP(String transactionId) {

        NVPCallerServices caller = new NVPCallerServices();

        try {
            caller.setAPIProfile(getProfile());

            //NVPEncoder object is created and all the name value pairs are loaded into it.
            NVPEncoder encoder = new NVPEncoder();
            encoder.add("METHOD", "GetTransactionDetails");
            encoder.add("TRANSACTIONID", transactionId);

            //encode method will encode the name and value and form NVP string for the request
            String strNVPString = encoder.encode();

            //call method will send the request to the server and return the response NVPString
            String ppresponse = caller.call(strNVPString);

            //NVPDecoder object is created
            NVPDecoder resultValues = new NVPDecoder();

            //decode method of NVPDecoder will parse the request and decode the name and value pair
            resultValues.decode(ppresponse);

            return resultValues.getMap();

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        return null;
    }

    public void approveOrder(ServletContext ctx, Order order, BaseAction action) {
        order.setStatus(action.getDao().getOrderStatus(OrderStatus.STATUS_APPROVED, true));
        action.getDao().save(order);
        action.addOrderHistory(order, action.getFrontUser(), "");
        if (order.getStatus().getSendEmail()) action.sendOrderStatusMail(order);
        action.addToStackSession("blockCode", StaticText.BLOCK_ORDER_APPROVED);

        Map<String, Object> mapForEventAction = new HashMap<String, Object>();
        mapForEventAction.put("order", order);
        if (action instanceof FrontModuleAction)
            EventUtils.executeEvent(ctx, EventService.EVENT_APPROVE_ORDER,(FrontModuleAction) action, mapForEventAction);
        else if (action instanceof AdminModuleAction)
            EventUtils.executeAdminEvent(ctx, EventService.EVENT_APPROVE_ORDER,(AdminModuleAction) action, mapForEventAction);
    }

    public void rejectOrder(ServletContext ctx, Order order, BaseAction action) {
        order.setStatus(action.getDao().getOrderStatus(OrderStatus.STATUS_REJECTED, true));
        action.getDao().save(order);
        action.addOrderHistory(order, action.getFrontUser(), "");
        if (order.getStatus().getSendEmail()) action.sendOrderStatusMail(order);
        // Recuperar stock reservado
        action.recoverOrderStock(order);
        action.addToStackSession("blockCode", StaticText.BLOCK_ORDER_REJECTED);

        Map<String, Object> mapForEventAction = new HashMap<String, Object>();
        mapForEventAction.put("order", order);
        if (action instanceof FrontModuleAction)
            EventUtils.executeEvent(ctx, EventService.EVENT_DENY_ORDER,(FrontModuleAction) action, mapForEventAction);
        else if (action instanceof AdminModuleAction)
            EventUtils.executeAdminEvent(ctx, EventService.EVENT_DENY_ORDER, (AdminModuleAction) action, mapForEventAction);

    }

    public PaymentTransactionType getTransactionDetails(String transactionId) {
        PaymentTransactionType result = null;
        CallerServices caller = new CallerServices();
        try {
            caller.setAPIProfile(getProfile());

            GetTransactionDetailsRequestType request = new GetTransactionDetailsRequestType();
            request.setTransactionID(transactionId);
            DetailLevelCodeType[] detail = new DetailLevelCodeType[1];
            detail[0] = DetailLevelCodeType.ReturnAll;
            request.setDetailLevel(detail);

            GetTransactionDetailsResponseType res = (GetTransactionDetailsResponseType) caller.call("GetTransactionDetails", request);
            if (res != null) result = res.getPaymentTransactionDetails();

        } catch (PayPalException e) {
            LOG.error(e.getMessage());
        }

        return result;
    }

    private APIProfile getProfile() throws PayPalException {
        APIProfile profile = null;
        if (properties != null) {
            profile = ProfileFactory.createSignatureAPIProfile();
            profile.setAPIUsername(properties.getProperty("paypal.api.username"));
            profile.setAPIPassword(properties.getProperty("paypal.api.password"));
            profile.setSignature(properties.getProperty("paypal.signature"));
            profile.setEnvironment(properties.getProperty("paypal.enviroment"));
            profile.setTimeout(1000 * 60 * 5);
        }

        return profile;
    }


}