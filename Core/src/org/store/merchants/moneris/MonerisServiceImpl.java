package org.store.merchants.moneris;

import JavaAPI.AvsInfo;
import JavaAPI.CvdInfo;
import JavaAPI.HttpsPostRequest;
import JavaAPI.Purchase;
import JavaAPI.Receipt;
import org.store.core.beans.Order;
import org.store.core.beans.utils.CreditCard;
import org.store.core.front.FrontModuleAction;
import org.store.core.globals.BaseAction;
import org.store.core.globals.SomeUtils;
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
import java.util.Properties;

public class MonerisServiceImpl extends MerchantService {

    private static final String MONERIS_HOST_LIVE = "https://www3.moneris.com/HPPDP/index.php";
    private static final String MONERIS_HOST_TEST = "https://esqa.moneris.com/HPPDP/index.php";

    private static final String FIELD_STATUS = "moneris_status";
    private static final String FIELD_API_TOKEN = "moneris_api_token";
    private static final String FIELD_STORE_ID = "moneris_store_id";
    private static final String FIELD_USE_AVS = "moneris_use_avs";
    private static final String FIELD_USE_CVD = "moneris_use_cvd";

    public boolean validatePayment(Order order, BaseAction action) {
        CreditCard card = CreditCard.fromRequest(action.getRequest(), "moneris");
        if (!card.isValid()) action.addSessionError(action.getText(BaseAction.CNT_INVALID_CREDITCARD_INFO, BaseAction.CNT_DEFAULT_INVALID_CREDITCARD_INFO));
        return card.isValid();
    }

    public PaymentResult doPayment(Order order, BaseAction action) {
        CreditCard card = CreditCard.fromRequest(action.getRequest(), "moneris");

        DecimalFormatSymbols dfs = new DecimalFormatSymbols(new Locale("en"));
        dfs.setDecimalSeparator('.');
        DecimalFormat df = new DecimalFormat("0.00", dfs);

// Hacer el pago
        String status = properties.getProperty("moneris.status");
        String merchant_host = ("live".equalsIgnoreCase(status)) ? MONERIS_HOST_LIVE : MONERIS_HOST_TEST;
        String merchant_store_id = properties.getProperty("moneris.store.id");
        String merchant_api_token = properties.getProperty("moneris.api.token");
        String merchant_crypt = properties.getProperty("moneris.crypt");
        String merchant_expiration = card.getYear() + card.getMonth();

        Purchase p = new Purchase(order.getIdOrder().toString(), df.format(order.getTotal()), card.getNumber(), merchant_expiration, merchant_crypt);

        if ("Y".equalsIgnoreCase(properties.getProperty("moneris.use.avs"))) {
            AvsInfo avs = new AvsInfo(card.getStreetNumber(), card.getStreetName(), card.getPostalCode());
            p.setAvsInfo(avs);
        }
        if ("Y".equalsIgnoreCase(properties.getProperty("moneris.use.cvd"))) {
            CvdInfo cvd = new CvdInfo("1", card.getCvd());
            p.setCvdInfo(cvd);
        }

        HttpsPostRequest mpgReq = new HttpsPostRequest(merchant_host, merchant_store_id, merchant_api_token, p);

        try {
            Receipt receipt = mpgReq.getReceipt();
            PaymentResult result = new PaymentResult();
            result.setTransactionResult(PaymentResult.RESULT_REJECTED);
            if (receipt != null) {
                Integer res = SomeUtils.strToInteger(receipt.getResponseCode());
                if (res != null && res < 50) result.setTransactionResult(PaymentResult.RESULT_ACCEPTED);
                result.setTransactionId(receipt.getReceiptId());
                result.setTransactionError(receipt.getResponseCode());
                result.addTransactionInfo("AuthCode", receipt.getAuthCode());
                result.addTransactionInfo("AvsResultCode", receipt.getAvsResultCode());
                result.addTransactionInfo("CardType", receipt.getCardType());
                result.setCardType(getCardName(receipt.getCardType()));
                result.addTransactionInfo("Complete", receipt.getComplete());
                result.addTransactionInfo("CorporateCard", receipt.getCorporateCard());
                result.addTransactionInfo("CvdResultCode", receipt.getCvdResultCode());
                result.addTransactionInfo("ISO", receipt.getISO());
                result.addTransactionInfo("Message", receipt.getMessage());
                result.addTransactionInfo("MessageId", receipt.getMessageId());
                result.addTransactionInfo("ReferenceNum", receipt.getReferenceNum());
                result.addTransactionInfo("Ticket", receipt.getTicket());
                result.addTransactionInfo("TimedOut", receipt.getTimedOut());
                result.addTransactionInfo("TransAmount", receipt.getTransAmount());
                result.addTransactionInfo("TransDate", receipt.getTransDate());
                result.addTransactionInfo("TransTime", receipt.getTransTime());
                result.addTransactionInfo("TransType", receipt.getTransType());
                result.addTransactionInfo("TxnNumber", receipt.getTxnNumber());
                return result;
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

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
        return null;
    }

    public String doPaymentRedirection(FrontModuleAction action) {
        return null;
    }

    public String getCode() {
        return "Moneris";
    }

    public String getLabel() {
        return "moneris";
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

        form.append(new StoreHtmlField(StoreHtmlField.TYPE_SELECT, "moneris.type").addClasses("field string-100")
                .setRequired(true)
                .addOption("visa", "Visa")
                .addOption("mastercard", "MasterCard")
                .addOption("amex", "Amex")
                .addOption("discovery", "Discovery")
                .setLabel(action.getText("credit.card.type", "Card Type"))
                .toString());

        form.append(new StoreHtmlField(StoreHtmlField.TYPE_INPUT, "moneris.number").addClasses("field string-200").setRequired(true)
                .setLabel(action.getText("credit.card.number", "Card Number"))
                .toString());

        form.append(new StoreHtmlField(StoreHtmlField.TYPE_SELECT, "moneris.month").addClasses("field string-100")
                .setRequired(true)
                .addOptions(months)
                .setLabel(action.getText("credit.card.expiration.month", "Expiration Month"))
                .toString());

        form.append(new StoreHtmlField(StoreHtmlField.TYPE_SELECT, "moneris.year").addClasses("field string-100")
                .setRequired(true)
                .addOptions(years)
                .setLabel(action.getText("credit.card.expiration.year", "Expiration Year"))
                .toString());

        if ("Y".equalsIgnoreCase(properties.getProperty("moneris.use.cvd"))) {
            form.append(new StoreHtmlField(StoreHtmlField.TYPE_INPUT, "moneris.cvd").addClasses("field string-100").setRequired(true)
                    .setLabel(action.getText("credit.card.cvd", "CVD"))
                    .toString());
        }

        if ("Y".equalsIgnoreCase(properties.getProperty("moneris.use.avs"))) {
            form.append(new StoreHtmlField(StoreHtmlField.TYPE_INPUT, "moneris.streetNumber").addClasses("field string-100").setRequired(true)
                    .setLabel(action.getText("credit.street.number", "Street Number"))
                    .toString());
            form.append(new StoreHtmlField(StoreHtmlField.TYPE_INPUT, "moneris.streetName").addClasses("field string-100").setRequired(true)
                    .setLabel(action.getText("credit.street.name", "Street Name"))
                    .toString());
            form.append(new StoreHtmlField(StoreHtmlField.TYPE_INPUT, "moneris.postalCode").addClasses("field string-100").setRequired(true)
                    .setLabel(action.getText("credit.postal.code", "Postal Code"))
                    .toString());
        }

        return form.toString();
    }

    public void setProperties(Properties p) {
        properties = p;
    }

    public String getPropertiesForm(BaseAction action) {
        StringBuffer form = new StringBuffer();

        form.append(new StoreHtmlField(StoreHtmlField.TYPE_SELECT, FIELD_STATUS).addClasses("field string-100")
                .addOption("test", action.getText("test", "Test"))
                .addOption("live", action.getText("live", "Live"))
                .setLabel(action.getText("status", "Status"))
                .setValue(getProperty("moneris.status", "test"))
                .getTableRow());

        form.append(new StoreHtmlField(StoreHtmlField.TYPE_INPUT, FIELD_STORE_ID).addClasses("field string-100")
                .setLabel(action.getText("moneris.store.id", "Store ID")).setValue(getProperty("moneris.store.id", ""))
                .getTableRow());

        form.append(new StoreHtmlField(StoreHtmlField.TYPE_INPUT, FIELD_API_TOKEN).addClasses("field string-100")
                .setLabel(action.getText("moneris.api.token", "API Token")).setValue(getProperty("moneris.api.token", ""))
                .getTableRow());

        form.append(new StoreHtmlField(StoreHtmlField.TYPE_CHECK, FIELD_USE_AVS).addOption("Y", "Yes")
                .setLabel(action.getText("moneris.use.avs", "Use AVS")).setValue(getProperty("moneris.use.avs", ""))
                .getTableRow());

        form.append(new StoreHtmlField(StoreHtmlField.TYPE_CHECK, FIELD_USE_AVS).addOption("Y", "Yes")
                .setLabel(action.getText("moneris.use.cvd", "Use CVD")).setValue(getProperty("moneris.use.cvd", ""))
                .getTableRow());


        return form.toString();
    }

    public void savePropertiesForm(BaseAction action) {
        properties.setProperty("moneris.status", getRequestParam(action, FIELD_STATUS, "test"));
        properties.setProperty("moneris.store.id", getRequestParam(action, FIELD_STORE_ID, ""));
        properties.setProperty("moneris.api.token", getRequestParam(action, FIELD_API_TOKEN, ""));
        properties.setProperty("moneris.use.avs", getRequestParam(action, FIELD_USE_AVS, ""));
        properties.setProperty("moneris.use.cvd", getRequestParam(action, FIELD_USE_CVD, ""));
        super.savePropertiesForm(action);
    }


    public static String getCardName(String card) {
        if (StringUtils.isEmpty(card)) return null;
        else if ("M".equalsIgnoreCase(card)) return PaymentResult.CARD_TYPE_MASTERCARD;
        else if ("V".equalsIgnoreCase(card)) return PaymentResult.CARD_TYPE_VISA;
        else if ("AX".equalsIgnoreCase(card)) return PaymentResult.CARD_TYPE_AMERICAN_EXPRESS;
        else if ("DC".equalsIgnoreCase(card)) return PaymentResult.CARD_TYPE_DINERS_CLUB;
        else if ("NO".equalsIgnoreCase(card)) return PaymentResult.CARD_TYPE_NOVUS_DISCOVER;
        else if ("SE".equalsIgnoreCase(card)) return PaymentResult.CARD_TYPE_SEARS;
        else return PaymentResult.CARD_TYPE_UNKNOWN;
    }


}
