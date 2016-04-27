package org.store.suppliers.techdata;

import com.opensymphony.xwork2.util.logging.Logger;
import com.opensymphony.xwork2.util.logging.LoggerFactory;
import org.store.core.globals.SomeUtils;
import org.store.core.utils.suppliers.AvailabilityResponse;
import org.apache.commons.digester.Digester;
import org.apache.commons.lang.StringUtils;

import java.io.StringReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class TechDataEuropeServicesResponse {

    private static final Logger LOG = LoggerFactory.getLogger(TechDataEuropeServicesResponse.class);
    private AvailabilityResponse response;
    private SimpleDateFormat datef = new SimpleDateFormat("dd/MM/yy");

    public TechDataEuropeServicesResponse(String data) {
        // LOG.debug(data);
        this.response = new AvailabilityResponse();
        this.response.setStatus(AvailabilityResponse.STATUS_NOT_FOUND);

        Digester digester = new Digester();
        digester.setEntityResolver(new TechDataEntityResolver());
        digester.push(this);
        digester.addCallMethod("OnlineCheck/ErrorMsg", "addError", 1);
        digester.addCallParam("OnlineCheck/ErrorMsg", 0);

        digester.addCallMethod("OnlineCheck/Item", "addResponse", 4);
        digester.addCallParam("OnlineCheck/Item/AvailabilityTotal", 0);
        digester.addCallParam("OnlineCheck/Item/UnitPriceAmount", 1);
        digester.addCallParam("OnlineCheck/Item/EstimatedDeliverySchedule/EstimatedDelivery/EstimatedDeliveryDate", 2);
        digester.addCallParam("OnlineCheck/Item/EstimatedDeliverySchedule/EstimatedDelivery/EstimatedDeliveryQuantity", 3);


        try {
            digester.parse(new StringReader(data));
        } catch (Exception e) {
            LOG.error("TE_COGI: " + e.getMessage(), e);
            LOG.error("EL_XML: " + data);
            this.response.setError(data);
        }
    }

    public void addError(String msg) {
        response.setError(msg);
    }

    public void addResponse(String stockStr, String priceStr, String etaDate, String etaQty) {
        response.setStock(SomeUtils.strToLong(stockStr));
        response.setPrice(SomeUtils.forceStrToDouble(priceStr));

        if (response.getStock()!=null && response.getStock()>0) {
            response.addWarehouse("local", response.getStock(), null);
        } else if (StringUtils.isNotEmpty(etaDate)) {
            try {
                Date eta = datef.parse(etaDate);
                response.setEtaDate(eta);
                response.setRequested(SomeUtils.strToLong(etaQty));
                response.addWarehouse("local", response.getStock(), eta);
            } catch (ParseException ignored) {}
        }

        if (response.getStock()!=null) response.setStatus(AvailabilityResponse.STATUS_ACTIVE);
    }

    public AvailabilityResponse getResponse() {
        return response;
    }

}