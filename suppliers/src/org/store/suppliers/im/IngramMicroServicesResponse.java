package org.store.suppliers.im;

import org.store.core.utils.suppliers.AvailabilityResponse;
import org.apache.commons.digester.Digester;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;

import java.io.StringReader;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class IngramMicroServicesResponse {

    private AvailabilityResponse response;
    private String warehouseCode;
    private SimpleDateFormat datef = new SimpleDateFormat("yyyy-MM-dd");


    public IngramMicroServicesResponse(String response, String wh) {
        this.warehouseCode = wh;
        this.response = new org.store.core.utils.suppliers.AvailabilityResponse();
        DecimalFormatSymbols dfs = new DecimalFormatSymbols(new Locale("en"));
        dfs.setDecimalSeparator('.');

        Digester digester = new Digester();
        digester.push(this);
        digester.addCallMethod("PNAResponse/TransactionHeader/ErrorStatus", "addError", 1);
        digester.addCallParam("PNAResponse/TransactionHeader/ErrorStatus", 0);

        digester.addCallMethod("PNAResponse/PriceAndAvailability", "addResponse", 2);
        digester.addCallParam("PNAResponse/PriceAndAvailability", 0, "SKU");
        digester.addCallParam("PNAResponse/PriceAndAvailability/Price", 1);

        digester.addCallMethod("PNAResponse/PriceAndAvailability/Branch", "addWarehouse", 4);
        digester.addCallParam("PNAResponse/PriceAndAvailability/Branch", 0, "ID");
        digester.addCallParam("PNAResponse/PriceAndAvailability/Branch/Availability", 1);
        digester.addCallParam("PNAResponse/PriceAndAvailability/Branch/ETADate", 2);
        digester.addCallParam("PNAResponse/PriceAndAvailability/Branch/OnOrder", 3);

        try {
            digester.parse(new StringReader(response));
        } catch (Exception e) {
            if (StringUtils.isNotEmpty(response)) {
                int i1 = response.toLowerCase().indexOf("<body");
                int i2 = response.toLowerCase().indexOf(">", i1);
                int i3 = response.toLowerCase().indexOf("</body>", i2);
                if (i1 > 0 && i2 > i1 && i3 > i2) {
                    response = response.substring(i2 + 1, i3);
                }
            }
            this.response.setError(response);
        }

    }

    public void addError(String msg) {
        response.setError(msg);
    }

    public void addResponse(String sku, String price) {
        response.setStatus(org.store.core.utils.suppliers.AvailabilityResponse.STATUS_ACTIVE);
        response.setSku(sku);
        try {
            Double res = NumberUtils.createDouble(price);
            response.setPrice(res);
        } catch (Exception ignored) {}
    }

    public void addWarehouse(String number, String qty, String eta, String reqQty) {
        Long st = null;
        try {
            st = NumberUtils.toLong(qty);
        } catch (Exception ignored) {}
        Date d = null;
        try {
            d = StringUtils.isNotEmpty(eta) ? datef.parse(eta) : null;
        } catch (ParseException ignored) {}
        response.addWarehouse(number, st, d);

        if (StringUtils.isNotEmpty(warehouseCode) && warehouseCode.equalsIgnoreCase(number)) {
            response.setStock(st);
            response.setEtaStr(eta);
            response.setEtaDate(d);
            try {
                Long reqQ = NumberUtils.toLong(reqQty);
                response.setRequested(reqQ);
            } catch (Exception ignored) {}
        }
    }

    public org.store.core.utils.suppliers.AvailabilityResponse getResponse() {
        return response;
    }

}