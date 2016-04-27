package org.store.merchants.nmi;

import com.opensymphony.xwork2.util.logging.Logger;
import com.opensymphony.xwork2.util.logging.LoggerFactory;
import org.store.core.beans.Order;
import org.store.core.beans.utils.CreditCard;
import org.store.core.front.FrontModuleAction;
import org.store.core.globals.BaseAction;
import org.store.core.globals.StoreHtmlField;
import org.store.core.utils.merchants.MerchantService;
import org.store.core.utils.merchants.PaymentResult;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.lang.StringUtils;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

public class NMIServiceImpl extends MerchantService {

    private static final Logger LOG = LoggerFactory.getLogger(NMIServiceImpl.class);

    private static final String HOST = "https://secure.networkmerchants.com/api/transact.php";

    private static final String FIELD_USERNAME = "nmi_username";
    private static final String FIELD_PASSWORD = "nmi_password";
    private static final String FIELD_USE_CARDHOLDER = "nmi_use_cardholder";
    private static final String FIELD_USE_CVD = "nmi_use_cvd";

    public boolean validatePayment(Order order, BaseAction action) {
        CreditCard card = CreditCard.fromRequest(action.getRequest(), "nmi");
        if (!card.isValid()) action.addSessionError(action.getText(BaseAction.CNT_INVALID_CREDITCARD_INFO, BaseAction.CNT_DEFAULT_INVALID_CREDITCARD_INFO));
        return card.isValid();
    }

    public PaymentResult doPayment(Order order, BaseAction action) {
        CreditCard card = CreditCard.fromRequest(action.getRequest(), "nmi");

        DecimalFormatSymbols dfs = new DecimalFormatSymbols(new Locale("en"));
        dfs.setDecimalSeparator('.');
        DecimalFormat df = new DecimalFormat("0.00", dfs);

// Hacer el pago
        PostMethod method = new PostMethod(HOST);

        method.addParameter("username", properties.getProperty("nmi.username"));
        method.addParameter("password", properties.getProperty("nmi.password"));
        method.addParameter("type", "sale");
        method.addParameter("ccnumber", card.getNumber());
        method.addParameter("ccexp", card.getMonth() + card.getYear());
        method.addParameter("amount", df.format(order.getTotal()));
        if (StringUtils.isNotEmpty(order.getRemoteIp())) method.addParameter("ipaddress ", order.getRemoteIp());

        if ("Y".equalsIgnoreCase(properties.getProperty("nmi.use.cardholder"))) {
            if (StringUtils.isNotEmpty(card.getFirstName())) method.addParameter("firstname", card.getFirstName());
            if (StringUtils.isNotEmpty(card.getLastName())) method.addParameter("lastname", card.getLastName());
        }
        if ("Y".equalsIgnoreCase(properties.getProperty("nmi.use.cvd"))) {
            method.addParameter("cvv", card.getCvd());
        }

        HttpClient httpclient = new HttpClient();
        try {
            int postResult = httpclient.executeMethod(method);
            LOG.debug("Response status code: " + postResult);
            String responseStr = method.getResponseBodyAsString();
            LOG.debug(responseStr);

            Map<String, String> responseMap = new HashMap<String, String>();
            StringTokenizer st = new StringTokenizer(responseStr, "&");
            while (st.hasMoreTokens()) {
                String varString = st.nextToken();
                StringTokenizer varSt = new StringTokenizer(varString, "=");
                if (varSt.countTokens() > 2 || varSt.countTokens() < 1) {
                    LOG.error("Bad variable from processor center: " + varString);
                    throw new Exception("Bad variable from processor center: " + varString);
                }
                if (varSt.countTokens() == 1) {
                    responseMap.put(varSt.nextToken(), "");
                } else {
                    responseMap.put(varSt.nextToken(), varSt.nextToken());
                }
            }

            PaymentResult result = new PaymentResult();
            result.setTransactionResult(PaymentResult.RESULT_PENDING);
            if (responseMap.containsKey("response") && responseMap.get("response") != null) {
                if ("1".equalsIgnoreCase(responseMap.get("response"))) result.setTransactionResult(PaymentResult.RESULT_ACCEPTED);
                else if ("2".equalsIgnoreCase(responseMap.get("response"))) result.setTransactionResult(PaymentResult.RESULT_REJECTED);

                result.setTransactionError(responseMap.get("responsetext"));
                result.setTransactionId(responseMap.get("transactionid"));
                result.setAuthorizationCode(responseMap.get("authcode"));

                for(String key : responseMap.keySet()) result.addTransactionInfo(key, responseMap.get(key));

                return result;
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
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
        return "NetworkMerchants";
    }

    public String getLabel() {
        return "nmi";
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

        StringBuffer form = new StringBuffer();

        form.append("<h2>").append(action.getText("credit.card.information", "Credit Card Information")).append("</h2>");

        form.append(new StoreHtmlField(StoreHtmlField.TYPE_INPUT, "nmi.number").addClasses("field string-200").setRequired(true)
                .setLabel(action.getText("credit.card.number", "Card Number"))
                .toString());

        form.append(new StoreHtmlField(StoreHtmlField.TYPE_SELECT, "nmi.month").addClasses("field string-100")
                .setRequired(true)
                .addOptions(months)
                .setLabel(action.getText("credit.card.expiration.month", "Expiration Month"))
                .toString());

        StoreHtmlField yearField = new StoreHtmlField(StoreHtmlField.TYPE_SELECT, "nmi.year").addClasses("field string-100")
                .setRequired(true)
                .setLabel(action.getText("credit.card.expiration.year", "Expiration Year"));
        int year = Calendar.getInstance().get(Calendar.YEAR);
        for (long i = 0; i <= 10; i++) yearField.addOption(String.valueOf(year+i-2000),String.valueOf(year+i));
        form.append(yearField.toString());

        if ("Y".equalsIgnoreCase(properties.getProperty("nmi.use.cvd"))) {
            form.append(new StoreHtmlField(StoreHtmlField.TYPE_INPUT, "nmi.cvd").addClasses("field string-100").setRequired(true)
                    .setLabel(action.getText("credit.card.cvd", "CVD"))
                    .toString());
        }

        if ("Y".equalsIgnoreCase(properties.getProperty("nmi.use.cardholder"))) {
            form.append(new StoreHtmlField(StoreHtmlField.TYPE_INPUT, "nmi.firstName").addClasses("field string-200").setRequired(true)
                    .setLabel(action.getText("credit.cardholder.firstname", "Cardholder's FirstName"))
                    .toString());
            form.append(new StoreHtmlField(StoreHtmlField.TYPE_INPUT, "nmi.lastName").addClasses("field string-200").setRequired(true)
                    .setLabel(action.getText("credit.cardholder.lastname", "Cardholder's LastName"))
                    .toString());
        }

        return form.toString();
    }

    public void setProperties(Properties p) {
        properties = p;
    }

    public String getPropertiesForm(BaseAction action) {
        StringBuffer form = new StringBuffer();

        form.append(new StoreHtmlField(StoreHtmlField.TYPE_INPUT, FIELD_USERNAME).addClasses("field string-100")
                .setLabel(action.getText("nmi.username", "Username")).setValue(getProperty("nmi.username", ""))
                .getTableRow());

        form.append(new StoreHtmlField(StoreHtmlField.TYPE_INPUT, FIELD_PASSWORD).addClasses("field string-100")
                .setLabel(action.getText("nmi.password", "Password")).setValue(getProperty("nmi.password", ""))
                .getTableRow());

        form.append(new StoreHtmlField(StoreHtmlField.TYPE_CHECK, FIELD_USE_CARDHOLDER).addOption("Y", "Yes")
                .setLabel(action.getText("nmi.use.avs", "Request cardholder")).setValue(getProperty("nmi.use.cardholder", ""))
                .getTableRow());

        form.append(new StoreHtmlField(StoreHtmlField.TYPE_CHECK, FIELD_USE_CVD).addOption("Y", "Yes")
                .setLabel(action.getText("nmi.use.cvd", "Reques CVD")).setValue(getProperty("nmi.use.cvd", ""))
                .getTableRow());


        return form.toString();
    }

    public void savePropertiesForm(BaseAction action) {
        properties.setProperty("nmi.username", getRequestParam(action, FIELD_USERNAME, ""));
        properties.setProperty("nmi.password", getRequestParam(action, FIELD_PASSWORD, ""));
        properties.setProperty("nmi.use.cardholder", getRequestParam(action, FIELD_USE_CARDHOLDER, ""));
        properties.setProperty("nmi.use.cvd", getRequestParam(action, FIELD_USE_CVD, ""));
        super.savePropertiesForm(action);
    }


}
