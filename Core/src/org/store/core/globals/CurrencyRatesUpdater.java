package org.store.core.globals;

import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: Administrador
 * Date: Apr 21, 2010
 */
public interface CurrencyRatesUpdater {

    public static final String ERROR_CONNECTING = "ERROR_CONNECTING";
    public static final String ERROR_RESPONSE = "ERROR_RESPONSE";
    public static final String ERROR_UNKNOWN = "ERROR_UNKNOWN";

    public ResponseRateUpdate getRate(String fromCurrencyCode, String toCurrencyCode);

    public class ResponseRateUpdate {

        public ResponseRateUpdate() {
        }

        public ResponseRateUpdate(Double rateValue, Date rateDate) {
            this.rateValue = rateValue;
            this.rateDate = rateDate;
        }

        public ResponseRateUpdate(String error) {
            this.error = error;
        }

        private Double reverseRateValue;
        private Double rateValue;
        private Date rateDate;
        private String error;

        public Double getRateValue() {
            return rateValue;
        }

        public void setRateValue(Double rateValue) {
            this.rateValue = rateValue;
        }

        public Date getRateDate() {
            return rateDate;
        }

        public void setRateDate(Date rateDate) {
            this.rateDate = rateDate;
        }

        public Double getReverseRateValue() {
            return reverseRateValue;
        }

        public void setReverseRateValue(Double reverseRateValue) {
            this.reverseRateValue = reverseRateValue;
        }

        public String getError() {
            return error;
        }

        public void setError(String error) {
            this.error = error;
        }
    }
}
