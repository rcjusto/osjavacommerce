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
import com.paypal.soap.api.AckCodeType;
import com.paypal.soap.api.BasicAmountType;
import com.paypal.soap.api.CurrencyCodeType;
import com.paypal.soap.api.DoExpressCheckoutPaymentRequestDetailsType;
import com.paypal.soap.api.DoExpressCheckoutPaymentRequestType;
import com.paypal.soap.api.DoExpressCheckoutPaymentResponseDetailsType;
import com.paypal.soap.api.DoExpressCheckoutPaymentResponseType;
import com.paypal.soap.api.ErrorType;
import com.paypal.soap.api.GetExpressCheckoutDetailsRequestType;
import com.paypal.soap.api.GetExpressCheckoutDetailsResponseType;
import com.paypal.soap.api.PayerInfoType;
import com.paypal.soap.api.PaymentActionCodeType;
import com.paypal.soap.api.PaymentDetailsType;
import com.paypal.soap.api.SetExpressCheckoutRequestDetailsType;
import com.paypal.soap.api.SetExpressCheckoutRequestType;
import com.paypal.soap.api.SetExpressCheckoutResponseType;
import org.store.core.beans.Order;
import org.store.core.beans.utils.CreditCard;
import org.store.core.front.FrontModuleAction;
import org.store.core.front.UserSession;
import org.store.core.globals.BaseAction;
import org.store.core.globals.StoreHtmlField;
import org.store.core.globals.StoreSessionInterceptor;
import org.store.core.utils.merchants.MerchantService;
import org.store.core.utils.merchants.PaymentResult;
import org.apache.commons.lang.StringUtils;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class PaypalExpressServiceImpl extends MerchantService {

    private static final String FIELD_ENVIRONMENT = "paypal_direct_environment";
    private static final String FIELD_USERNAME = "paypal_direct_username";
    private static final String FIELD_PASSWORD = "paypal_direct_password";
    private static final String FIELD_SIGNATURE = "paypal_direct_signature";

    private Double thredhold;
    private String error;
    private static final String PROP_LIVE_PAYPAL_URLEXPRESS = "https://www.paypal.com/cgi-bin/webscr?cmd=_express-checkout";
    private static final String PROP_SANDBOX_PAYPAL_URLEXPRESS = "https://www.sandbox.paypal.com/cgi-bin/webscr?cmd=_express-checkout";
    private static final Logger LOG = LoggerFactory.getLogger(PaypalExpressServiceImpl.class);
    private static final String PARAM_PAYPAL_TOKEN = "org.store.merchants.paypal.TOKEN";
    private static final String PARAM_PAYPAL_PAYER_ID = "org.store.merchants.paypal.PAYER_ID";


    public boolean validatePayment(Order order, BaseAction action) {
        return true;
    }

    public PaymentResult doPayment(Order order, BaseAction action) {
        PaymentResult paymentResult = null;
        CallerServices caller = new CallerServices();

        Map map = (action != null) ? action.getStoreSessionObjects() : null;
        String token = (map != null && map.containsKey(PARAM_PAYPAL_TOKEN)) ? (String) map.get(PARAM_PAYPAL_TOKEN) : null;
        String payerId = (map != null && map.containsKey(PARAM_PAYPAL_PAYER_ID)) ? (String) map.get(PARAM_PAYPAL_PAYER_ID) : null;

        DecimalFormatSymbols symbols = new DecimalFormatSymbols(new Locale("en"));
        symbols.setDecimalSeparator('.');
        DecimalFormat df = new DecimalFormat("0.00", symbols);

        try {
            caller.setAPIProfile(getProfile());

            DoExpressCheckoutPaymentRequestType request = new DoExpressCheckoutPaymentRequestType();
            DoExpressCheckoutPaymentRequestDetailsType reqDetail = new DoExpressCheckoutPaymentRequestDetailsType();

            reqDetail.setPayerID(payerId);
            reqDetail.setPaymentAction(PaymentActionCodeType.Sale);
            reqDetail.setToken(token);
            PaymentDetailsType pdt = new PaymentDetailsType();
            BasicAmountType bat = new BasicAmountType();
            bat.setCurrencyID(CurrencyCodeType.fromString(order.getCurrency().getCode()));
            bat.set_value(df.format(order.getTotal()));
            pdt.setOrderTotal(bat);
            reqDetail.setPaymentDetails(pdt);

            request.setDoExpressCheckoutPaymentRequestDetails(reqDetail);

            DoExpressCheckoutPaymentResponseType response = (DoExpressCheckoutPaymentResponseType) caller.call("DoExpressCheckoutPayment", request);
            paymentResult = new PaymentResult();
            DoExpressCheckoutPaymentResponseDetailsType resDetail = response.getDoExpressCheckoutPaymentResponseDetails();
            if (AckCodeType.Success.equals(response.getAck()) || AckCodeType.SuccessWithWarning.equals(response.getAck())) {
                String paymentStatus = (resDetail.getPaymentInfo().getPaymentStatus() != null && resDetail.getPaymentInfo().getPaymentStatus().getValue() != null) ? resDetail.getPaymentInfo().getPaymentStatus().getValue().toString() : null;
                if ("Pending".equalsIgnoreCase(paymentStatus)) {
                    paymentResult.setTransactionResult(PaymentResult.RESULT_PENDING);
                } else if ("Completed".equalsIgnoreCase(paymentStatus) || "Processed".equalsIgnoreCase(paymentStatus)) {
                    paymentResult.setTransactionResult(PaymentResult.RESULT_ACCEPTED);
                } else {
                    paymentResult.setTransactionResult(PaymentResult.RESULT_REJECTED);
                }
            } else {
                paymentResult.setTransactionResult(PaymentResult.RESULT_REJECTED);
            }
            paymentResult.setTransactionId(resDetail.getPaymentInfo().getTransactionID());
            if (response.getErrors() != null && response.getErrors().length > 0) {
                StringBuffer buff = new StringBuffer();
                for (ErrorType err : response.getErrors()) buff.append(err.getErrorCode().toString()).append(": ").append(err.getLongMessage()).append("\r");
                paymentResult.setTransactionError(buff.toString());
            }

            Map<String, String> info = new HashMap<String, String>();
            paymentResult.setTransactionInfo(info);
            paymentResult.setCardType(PaymentResult.PAYMENT_TYPE_PAYPAL);
        } catch (PayPalException e) {
            log.error(e.getMessage(), e);
        }
        return paymentResult;
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
                        result.setTransactionResult(PaymentResult.RESULT_ACCEPTED);
                    } else {
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
        return null;
    }

    public String doPaymentRedirection(FrontModuleAction action) {
        String result = null;

        UserSession us = action.getUserSession();
        if (!us.getShoppingCart().getItems().isEmpty()) {
            DecimalFormatSymbols symbols = new DecimalFormatSymbols(new Locale("en"));
            symbols.setDecimalSeparator('.');
            DecimalFormat df = new DecimalFormat("0.00", symbols);

            Map map = us.getPriceMap();
            Double total = null;
            if (map != null && map.containsKey("total")) total = (Double) map.get("total");
            if (total == null) total = us.getTotal();

            CallerServices caller = new CallerServices();
            try {
                caller.setAPIProfile(getProfile());

                SetExpressCheckoutRequestType request = new SetExpressCheckoutRequestType();
                SetExpressCheckoutRequestDetailsType reqDetail = new SetExpressCheckoutRequestDetailsType();
                BasicAmountType basicAmount = new BasicAmountType();

                basicAmount.set_value(df.format(total));
                basicAmount.setCurrencyID(CurrencyCodeType.fromString(action.getActualCurrency().getCode()));
                reqDetail.setOrderTotal(basicAmount);

                if (!us.needShipping()) {
                    reqDetail.setNoShipping("1");
                }

                BasicAmountType amount1 = new BasicAmountType();
                amount1.set_value(df.format(total + 100));
                amount1.setCurrencyID(CurrencyCodeType.fromString(action.getActualCurrency().getCode()));
                reqDetail.setMaxAmount(amount1);
                reqDetail.setPaymentAction(PaymentActionCodeType.Sale);

                reqDetail.setCancelURL(action.getActionUrl(PaypalExpressEventImpl.ACTION_CANCEL, null));
                reqDetail.setReturnURL(action.getActionUrl(PaypalExpressEventImpl.ACTION_RESPONSE, null));
                request.setSetExpressCheckoutRequestDetails(reqDetail);
                SetExpressCheckoutResponseType res = (SetExpressCheckoutResponseType) caller.call("SetExpressCheckout", request);
                String token = res.getToken();
                if (StringUtils.isNotEmpty(token)) result = getUrl(token);
                else {
                    LOG.error("Token empty in SetExpressCheckout");
                    error = "Wrong response from Paypal";
                }
            } catch (PayPalException e) {
                error = e.getMessage();
                LOG.error(e.getMessage(), e);
            } catch (Exception e) {
                error = e.getMessage();
                LOG.error(e.getMessage(), e);
            }

        }
        return result;
    }

    public PayerInfoType getExpressCheckoutDetail(String token, FrontModuleAction action) {
        PayerInfoType payer = null;
        CallerServices caller = new CallerServices();
        try {
            caller.setAPIProfile(getProfile());

            GetExpressCheckoutDetailsRequestType request = new GetExpressCheckoutDetailsRequestType();
            request.setToken(token);

            GetExpressCheckoutDetailsResponseType res = (GetExpressCheckoutDetailsResponseType) caller.call("GetExpressCheckoutDetails", request);
            payer = res.getGetExpressCheckoutDetailsResponseDetails().getPayerInfo();

            action.getStoreSessionObjects().put(PARAM_PAYPAL_TOKEN, token);
            action.getStoreSessionObjects().put(PARAM_PAYPAL_PAYER_ID, payer.getPayerID());
            action.getStoreSessionObjects().put(StoreSessionInterceptor.CNT_EXTERNAL_PAYMENT, getCode());

        } catch (PayPalException e) {
            LOG.error(e.getMessage());
        }

        return payer;
    }

    public boolean useAVS(CreditCard card) {
        return true;
    }

    public boolean useCVD(CreditCard card) {
        return true;
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

    private String getUrl(String token) {
        return (("live".equalsIgnoreCase(properties.getProperty("paypal.enviroment"))) ? PROP_LIVE_PAYPAL_URLEXPRESS : PROP_SANDBOX_PAYPAL_URLEXPRESS) + "&token=" + token;
    }


    public String getCode() {
        return PaypalExpressEventImpl.PAYPAL_EXPRESS_CHECKOUT;
    }

    public String getLabel() {
        return "paypal.express";
    }

    public String getType() {
        return MerchantService.TYPE_EXTERNAL;
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

        return form.toString();
    }

    @Override
    public void savePropertiesForm(BaseAction action) {
        properties.setProperty("paypal.enviroment", getRequestParam(action, FIELD_ENVIRONMENT, ""));
        properties.setProperty("paypal.api.username", getRequestParam(action, FIELD_USERNAME, ""));
        properties.setProperty("paypal.api.password", getRequestParam(action, FIELD_PASSWORD, ""));
        properties.setProperty("paypal.signature", getRequestParam(action, FIELD_SIGNATURE, ""));
        super.savePropertiesForm(action);
    }

}