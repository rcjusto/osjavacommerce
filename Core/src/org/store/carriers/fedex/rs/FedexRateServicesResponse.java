package org.store.carriers.fedex.rs;


import org.store.core.utils.carriers.RateService;
import org.apache.commons.digester.Digester;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: Rogelio Caballero Justo
 * Date: 20-nov-2006
 */
public class FedexRateServicesResponse {

    private String errorCode;
    private String errorDescription;

    private ArrayList<org.store.core.utils.carriers.RateService> rateServices;


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

    public ArrayList<RateService> getRateServices() {
        return rateServices;
    }

    public void setRateServices(ArrayList<org.store.core.utils.carriers.RateService> rateServices) {
        this.rateServices = rateServices;
    }

    public FedexRateServicesResponse(InputStream stream) throws IOException, SAXException {
        Digester digester = new Digester();
        digester.push(this);

        digester.addCallMethod("FDXRateAvailableServicesReply/Entry", "addShipment", 4);
        digester.addCallParam("FDXRateAvailableServicesReply/Entry/Service", 0);
        digester.addCallParam("FDXRateAvailableServicesReply/Entry/EstimatedCharges/CurrencyCode", 1);
        digester.addCallParam("FDXRateAvailableServicesReply/Entry/EstimatedCharges/DiscountedCharges/NetCharge", 2);
        digester.addCallParam("FDXRateAvailableServicesReply/Entry/DeliveryDate", 3);

        digester.addCallMethod("FDXRateAvailableServicesReply/Error", "addError", 2);
        digester.addCallParam("FDXRateAvailableServicesReply/Error/Code", 0);
        digester.addCallParam("FDXRateAvailableServicesReply/Error/Message", 1);

        digester.parse(stream);
    }


    public void addShipment(String code, String currencyCode, String monetaryValue, String daysToDelivery) {
        if (rateServices==null) setRateServices(new ArrayList<org.store.core.utils.carriers.RateService>());
        RateService serv = new org.store.core.utils.carriers.RateService(code, currencyCode, monetaryValue, daysToDelivery);
        rateServices.add(serv);
    }

    public void addError(String code, String desc) {
        setErrorCode(code);
        setErrorDescription(desc);
    }

    public boolean hasError() {
        return errorCode!=null && !"".equals(errorCode);
    }

}
