package org.store.carriers.capost.rs;

import org.store.core.utils.carriers.RateService;
import org.store.core.globals.SomeUtils;
import org.apache.commons.digester.Digester;
import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: Rogelio Caballero Justo
 * Date: 20-nov-2006
 */
public class CaPostRateServicesResponse {

    public static Logger log = Logger.getLogger(CaPostRateServicesResponse.class);
    private String responseStatusCode;
    private String responseStatusDescription;

    private String errorCode;
    private String errorDescription;

    private ArrayList<RateService> rateServices;


    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorDescription() {
        return errorDescription;
    }

    public void setErrorDescription(String errorDescription) {
        this.errorDescription = errorDescription;
    }

    public String getResponseStatusCode() {
        return responseStatusCode;
    }

    public void setResponseStatusCode(String responseStatusCode) {
        this.responseStatusCode = responseStatusCode;
    }

    public String getResponseStatusDescription() {
        return responseStatusDescription;
    }

    public void setResponseStatusDescription(String responseStatusDescription) {
        this.responseStatusDescription = responseStatusDescription;
    }

    public ArrayList<org.store.core.utils.carriers.RateService> getRateServices() {
        return rateServices;
    }

    public void setRateServices(ArrayList<RateService> rateServices) {
        this.rateServices = rateServices;
    }

    public CaPostRateServicesResponse(InputStream stream) throws IOException, SAXException {
        Digester digester = new Digester();
        digester.push(this);
        digester.addCallMethod("eparcel/ratesAndServicesResponse", "addResponse", 2);
        digester.addCallParam("eparcel/ratesAndServicesResponse/statusCode", 0);
        digester.addCallParam("eparcel/ratesAndServicesResponse/statusMessage", 1);

        digester.addCallMethod("eparcel/ratesAndServicesResponse/product", "addShipment", 5);
        digester.addCallParam("eparcel/ratesAndServicesResponse/product", 0,"id");
        digester.addCallParam("eparcel/ratesAndServicesResponse/product/rate", 1);
        digester.addCallParam("eparcel/ratesAndServicesResponse/product/shippingDate", 2);
        digester.addCallParam("eparcel/ratesAndServicesResponse/product/deliveryDate", 3);
        digester.addCallParam("eparcel/ratesAndServicesResponse/product/nextDayAM", 4);

        digester.addCallMethod("eparcel/error", "addError", 2);
        digester.addCallParam("eparcel/error/statusCode", 0);
        digester.addCallParam("eparcel/error/statusMessage", 1);
        digester.parse(stream);
    }

    public void addResponse(String _statusCode, String _statusDescription) {
        setResponseStatusCode(_statusCode);
        setResponseStatusDescription(_statusDescription);
    }

    public void addShipment(String code, String monetaryValue, String shippingDate,String deliveryDate,  String am) {
        if (rateServices==null) setRateServices(new ArrayList<org.store.core.utils.carriers.RateService>());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Long numdays = null;
        if (shippingDate!=null && !"".equals(shippingDate) && deliveryDate!=null && !"".equals(deliveryDate)) {
            try {
                Date sd = sdf.parse(shippingDate);
                Date dd = sdf.parse(deliveryDate);
                numdays = SomeUtils.dayDiff(sd,dd);
            } catch (ParseException e) {
                log.error(e.getMessage(), e);
            }
        }
        RateService serv = new RateService(code, "CAD", monetaryValue, (numdays!=null) ? numdays.toString() : "", deliveryDate, "true".equalsIgnoreCase(am));
        rateServices.add(serv);
    }

    public void addError(String code, String desc) {
        setErrorCode(code);
        setErrorDescription(desc);
    }

    public boolean hasError() {
        return !"1".equals(responseStatusCode) && !"2".equals(responseStatusCode);
    }

}