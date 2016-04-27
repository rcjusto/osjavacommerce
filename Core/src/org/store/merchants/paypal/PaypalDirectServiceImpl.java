package org.store.merchants.paypal;

import com.paypal.sdk.core.nvp.NVPDecoder;
import com.paypal.sdk.core.nvp.NVPEncoder;
import com.paypal.sdk.exceptions.PayPalException;
import com.paypal.sdk.profiles.APIProfile;
import com.paypal.sdk.profiles.ProfileFactory;
import com.paypal.sdk.services.CallerServices;
import com.paypal.sdk.services.NVPCallerServices;
import com.paypal.soap.api.AckCodeType;
import com.paypal.soap.api.AddressType;
import com.paypal.soap.api.BasicAmountType;
import com.paypal.soap.api.CountryCodeType;
import com.paypal.soap.api.CreditCardDetailsType;
import com.paypal.soap.api.CreditCardTypeType;
import com.paypal.soap.api.CurrencyCodeType;
import com.paypal.soap.api.DoDirectPaymentRequestDetailsType;
import com.paypal.soap.api.DoDirectPaymentRequestType;
import com.paypal.soap.api.DoDirectPaymentResponseType;
import com.paypal.soap.api.ErrorType;
import com.paypal.soap.api.PayerInfoType;
import com.paypal.soap.api.PaymentActionCodeType;
import com.paypal.soap.api.PaymentDetailsType;
import com.paypal.soap.api.PersonNameType;
import org.store.core.beans.Order;
import org.store.core.beans.utils.CreditCard;
import org.store.core.front.FrontModuleAction;
import org.store.core.globals.BaseAction;
import org.store.core.globals.StoreHtmlField;
import org.store.core.utils.merchants.MerchantService;
import org.store.core.utils.merchants.PaymentResult;
import org.apache.commons.lang.StringUtils;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class PaypalDirectServiceImpl extends MerchantService {

    private static final String FIELD_ENVIRONMENT = "paypal_direct_environment";
    private static final String FIELD_USERNAME = "paypal_direct_username";
    private static final String FIELD_PASSWORD = "paypal_direct_password";
    private static final String FIELD_SIGNATURE = "paypal_direct_signature";


    public boolean validatePayment(Order order, BaseAction action) {
        CreditCard card = CreditCard.fromRequest(action.getRequest(), "paypal");
        if (!card.isValid()) action.addSessionError(action.getText(BaseAction.CNT_INVALID_CREDITCARD_INFO, BaseAction.CNT_DEFAULT_INVALID_CREDITCARD_INFO));
        return card.isValid();
    }

    public PaymentResult doPayment(Order order, BaseAction action) {
        CreditCard card = CreditCard.fromRequest(action.getRequest(), "paypal");

        DecimalFormatSymbols dfs = new DecimalFormatSymbols(new Locale("en"));
        dfs.setDecimalSeparator('.');
        DecimalFormat df = new DecimalFormat("0.00", dfs);

        CallerServices caller = new CallerServices();
        try {
            caller.setAPIProfile(getProfile());

            DoDirectPaymentRequestType request;
            request = new DoDirectPaymentRequestType();
            DoDirectPaymentRequestDetailsType details = new DoDirectPaymentRequestDetailsType();

            CreditCardDetailsType creditCard = new CreditCardDetailsType();
            creditCard.setCreditCardNumber(card.getNumber());
            creditCard.setCreditCardType(CreditCardTypeType.fromString(card.getType()));
            creditCard.setCVV2(card.getCvd());
            creditCard.setExpMonth(Integer.parseInt(card.getMonth()));
            creditCard.setExpYear(Integer.parseInt(card.getYear()));

            PayerInfoType cardOwner = new PayerInfoType();
            cardOwner.setPayerCountry(CountryCodeType.fromString(order.getBillingAddress().getIdCountry()));

            AddressType addr = new AddressType();
            addr.setPostalCode(order.getBillingAddress().getZipCode());
            addr.setStateOrProvince((order.getBillingAddress().getState() != null) ? order.getBillingAddress().getState().getStateCode() : null);
            addr.setStreet1(order.getBillingAddress().getAddress());
            if (StringUtils.isNotEmpty(order.getBillingAddress().getAddress2())) addr.setStreet2(order.getBillingAddress().getAddress2());
            addr.setCountryName(order.getBillingAddress().getIdCountry());
            addr.setCountry(CountryCodeType.fromString(order.getBillingAddress().getIdCountry()));
            addr.setCityName(order.getBillingAddress().getCity());
            cardOwner.setAddress(addr);

            PersonNameType payerName = new PersonNameType();
            payerName.setFirstName(order.getBillingAddress().getFirstname());
            payerName.setLastName(order.getBillingAddress().getLastname());
            cardOwner.setPayerName(payerName);

            creditCard.setCardOwner(cardOwner);
            details.setCreditCard(creditCard);
            if (StringUtils.isNotEmpty(action.getRequest().getRemoteAddr())) details.setIPAddress(action.getRequest().getRemoteAddr());
//            details.setMerchantSessionId("456977");
            details.setPaymentAction(PaymentActionCodeType.Sale);

            PaymentDetailsType payment = new PaymentDetailsType();

            BasicAmountType orderTotal = new BasicAmountType();
            orderTotal.setCurrencyID(CurrencyCodeType.fromString(order.getCurrency().getCode()));
            orderTotal.set_value(df.format(order.getTotal()));
            payment.setOrderTotal(orderTotal);

            details.setPaymentDetails(payment);
            request.setDoDirectPaymentRequestDetails(details);

            DoDirectPaymentResponseType response = (DoDirectPaymentResponseType) caller.call("DoDirectPayment", request);


            PaymentResult result = new PaymentResult();
            if (AckCodeType.Success.equals(response.getAck()) || AckCodeType.SuccessWithWarning.equals(response.getAck())) {
                result.setTransactionResult(PaymentResult.RESULT_ACCEPTED);
            } else {
                result.setTransactionResult(PaymentResult.RESULT_REJECTED);
            }

            if (response.getErrors() != null && response.getErrors().length > 0) {
                StringBuffer buff = new StringBuffer();
                for (ErrorType e : response.getErrors()) {
                    buff.append(e.getLongMessage()).append("\n");
                }
                result.setTransactionError(buff.toString());
            }

            result.setCardType(getCardType(card.getType()));
            result.setTransactionId(response.getTransactionID());
            result.addTransactionInfo("AVS Response", response.getAVSCode());
            result.addTransactionInfo("CVV Response", response.getCVV2Code());
            if (response.getAmount() != null) result.addTransactionInfo("Amount", response.getAmount().get_value());
            if (response.getAmount() != null && response.getAmount().getCurrencyID() != null) result.addTransactionInfo("Currency", response.getAmount().getCurrencyID().toString());
            return result;
        } catch (PayPalException e) {
            log.error(e.getMessage(), e);
        }
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

    private String getCardType(String card) {
        if (StringUtils.isEmpty(card)) return null;
        else if ("MasterCard".equalsIgnoreCase(card)) return PaymentResult.CARD_TYPE_MASTERCARD;
        else if ("Visa".equalsIgnoreCase(card)) return PaymentResult.CARD_TYPE_VISA;
        else if ("Amex".equalsIgnoreCase(card)) return PaymentResult.CARD_TYPE_AMERICAN_EXPRESS;
        else if ("DinersClub".equalsIgnoreCase(card)) return PaymentResult.CARD_TYPE_DINERS_CLUB;
        else if ("Discover".equalsIgnoreCase(card)) return PaymentResult.CARD_TYPE_NOVUS_DISCOVER;
        else return PaymentResult.CARD_TYPE_UNKNOWN;
    }

    public String getCode() {
        return "PayPal Direct Payment";
    }

    public String getLabel() {
        return "paypal.direct";
    }

    public String getType() {
        return MerchantService.TYPE_CREDIT_CARD;
    }

    public String getError() {
        return null;
    }

    public String getForm(BaseAction action) {
        DecimalFormat df = new DecimalFormat("00");
        List<String> months = new ArrayList<String>();
        for (long i = 1; i <= 12; i++) months.add(df.format(i));


        int year = Calendar.getInstance().get(Calendar.YEAR);
        List<String> years = new ArrayList<String>();
        for (long i = 0; i <= 10; i++) years.add(df.format(year + i));

        StringBuffer form = new StringBuffer();

        form.append("<h2>").append(action.getText("credit.card.information", "Credit Card Information")).append("</h2>");

        form.append(new StoreHtmlField(StoreHtmlField.TYPE_SELECT, "paypal.type").addClasses("field string-100")
                .setRequired(true)
                .addOption("Visa", "Visa")
                .addOption("MasterCard", "MasterCard")
                .addOption("Amex", "Amex")
                .addOption("Discover", "Discover")
                .addOption("Switch", "Switch")
                .addOption("Solo", "Solo")
                .setLabel(action.getText("credit.card.type", "Card Type"))
                .toString());

        form.append(new StoreHtmlField(StoreHtmlField.TYPE_INPUT, "paypal.number").addClasses("field string-200").setRequired(true)
                .setLabel(action.getText("credit.card.number", "Card Number"))
                .toString());

        form.append(new StoreHtmlField(StoreHtmlField.TYPE_SELECT, "paypal.month").addClasses("field string-100")
                .setRequired(true)
                .addOptions(months)
                .setLabel(action.getText("credit.card.expiration.month", "Expiration Month"))
                .toString());

        form.append(new StoreHtmlField(StoreHtmlField.TYPE_SELECT, "paypal.year").addClasses("field string-100")
                .setRequired(true)
                .addOptions(years)
                .setLabel(action.getText("credit.card.expiration.year", "Expiration Year"))
                .toString());

        form.append(new StoreHtmlField(StoreHtmlField.TYPE_INPUT, "paypal.cvd").addClasses("field string-100").setRequired(true)
                .setLabel(action.getText("credit.card.cvd", "CVD"))
                .toString());

        form.append(new StoreHtmlField(StoreHtmlField.TYPE_INPUT, "paypal.streetNumber").addClasses("field string-100").setRequired(true)
                .setLabel(action.getText("credit.street.number", "Street Number"))
                .toString());
        form.append(new StoreHtmlField(StoreHtmlField.TYPE_INPUT, "paypal.streetName").addClasses("field string-100").setRequired(true)
                .setLabel(action.getText("credit.street.name", "Street Name"))
                .toString());
        form.append(new StoreHtmlField(StoreHtmlField.TYPE_INPUT, "paypal.postalCode").addClasses("field string-100").setRequired(true)
                .setLabel(action.getText("credit.postal.code", "Postal Code"))
                .toString());

        return form.toString();
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
}