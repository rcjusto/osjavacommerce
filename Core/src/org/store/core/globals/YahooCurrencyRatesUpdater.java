package org.store.core.globals;


import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;

/**
 * Created by IntelliJ IDEA.
 * User: Administrador
 * Date: Apr 21, 2010
 */
public class YahooCurrencyRatesUpdater implements CurrencyRatesUpdater {

    public static Logger log = Logger.getLogger(YahooCurrencyRatesUpdater.class);

    public ResponseRateUpdate getRate(String fromCurrencyCode, String toCurrencyCode) {
        ResponseRateUpdate result = null;
        try {
            String url = "http://download.finance.yahoo.com/d/quotes.cvs?s=" + fromCurrencyCode.toUpperCase() + toCurrencyCode.toUpperCase() + "=X&f=sl1d1t1ohgv&e=.cvs";
            HttpClient client = new HttpClient();
            HttpMethod method = new GetMethod(url);
            int statusCode = client.executeMethod(method);

            if (statusCode == HttpStatus.SC_OK) {
                String res = method.getResponseBodyAsString();
                if (res != null) {
                    String[] arr = res.split("[,]");
                    if (arr != null && arr.length > 3) {
                        Double ratio = SomeUtils.strToDouble(arr[1]);
                        String dateStr = arr[2].replace('"', ' ').trim();
                        String timeStr = arr[3].replace('"', ' ').trim();

                        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy h:mma");
                        java.util.Date d = sdf.parse(dateStr + " " + timeStr);

                        result = new ResponseRateUpdate(ratio,d);
                    }
                }
            } else {
                result = new ResponseRateUpdate(ERROR_CONNECTING);
                log.error("No se pudo conectar");
            }

            url = "http://download.finance.yahoo.com/d/quotes.cvs?s=" + toCurrencyCode.toUpperCase() + fromCurrencyCode.toUpperCase() + "=X&f=sl1d1t1ohgv&e=.cvs";
            client = new HttpClient();
            method = new GetMethod(url);
            statusCode = client.executeMethod(method);

            if (statusCode == HttpStatus.SC_OK) {
                String res = method.getResponseBodyAsString();
                if (res != null) {
                    String[] arr = res.split("[,]");
                    if (arr != null && arr.length > 3) {
                        Double ratio = SomeUtils.strToDouble(arr[1]);
                        String dateStr = arr[2].replace('"', ' ').trim();
                        String timeStr = arr[3].replace('"', ' ').trim();

                        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy h:mma");
                        java.util.Date d = sdf.parse(dateStr + " " + timeStr);

                        if (result==null) result = new ResponseRateUpdate();
                        if (d!=null) result.setRateDate(d);
                        result.setReverseRateValue(ratio);
                    }
                }
            } else {
                result = new ResponseRateUpdate(ERROR_CONNECTING);
                log.error("No se pudo conectar");
            }
        } catch (IOException e) {
            result = new ResponseRateUpdate(ERROR_CONNECTING);
            log.error(e.getMessage(), e);
        } catch (ParseException e) {
            result = new ResponseRateUpdate(ERROR_RESPONSE);
            log.error(e.getMessage(), e);
        } catch (Exception e) {
            result = new ResponseRateUpdate(ERROR_UNKNOWN);
            log.error(e.getMessage(), e);
        }
        return result;
    }

}
