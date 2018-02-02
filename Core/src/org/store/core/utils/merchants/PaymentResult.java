package org.store.core.utils.merchants;

import java.util.HashMap;
import java.util.Map;

public class PaymentResult {

    public static final String PAYMENT_TYPE_PAYPAL = "Paypal";
    public static final String CARD_TYPE_MASTERCARD = "MC";
    public static final String CARD_TYPE_VISA = "VISA";
    public static final String CARD_TYPE_AMERICAN_EXPRESS = "AMEX";
    public static final String CARD_TYPE_DINERS_CLUB = "DC";
    public static final String CARD_TYPE_NOVUS_DISCOVER = "ND";
    public static final String CARD_TYPE_SEARS = "S";
    public static final String CARD_TYPE_UNKNOWN = "Unknown";

    public static final String RESULT_ACCEPTED = "ACCEPTED";
    public static final String RESULT_REJECTED = "REJECTED";
    public static final String RESULT_PENDING = "PENDING";

    private String transactionId;
    private String cardType;
    private String transactionResult;
    private String transactionError;
    private String authorizationCode;
    private Map<String, String> transactionInfo;

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getTransactionResult() {
        return transactionResult;
    }

    public void setTransactionResult(String transactionResult) {
        this.transactionResult = transactionResult;
    }

    public String getTransactionError() {
        return transactionError;
    }

    public void setTransactionError(String transactionError) {
        this.transactionError = transactionError;
    }

    public Map<String, String> getTransactionInfo() {
        return transactionInfo;
    }

    public void setTransactionInfo(Map<String, String> transactionInfo) {
        this.transactionInfo = transactionInfo;
    }

    public String getAuthorizationCode() {
        return authorizationCode;
    }

    public void setAuthorizationCode(String authorizationCode) {
        this.authorizationCode = authorizationCode;
    }

    public String getCardType() {
        return cardType;
    }

    public void setCardType(String cardType) {
        this.cardType = cardType;
    }

    public boolean isApproved() {
        return RESULT_ACCEPTED.equalsIgnoreCase(transactionResult);
    }

    public boolean isRejected() {
        return RESULT_REJECTED.equalsIgnoreCase(transactionResult);
    }

    public boolean isPending() {
        return RESULT_PENDING.equalsIgnoreCase(transactionResult);
    }

    public void addTransactionInfo(String key, String value) {
        if (transactionInfo==null) transactionInfo = new HashMap<String, String>();
        transactionInfo.put(key, value);
    }
}
