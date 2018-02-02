package org.store.suppliers.supercom;

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


public class SupercomServicesResponse {

    private org.store.core.utils.suppliers.AvailabilityResponse response;
    private String warehouseCode;
    private SimpleDateFormat datef = new SimpleDateFormat("yyyy-MM-dd");

    public SupercomServicesResponse(String response, String wh) {
        this.warehouseCode = wh;
        this.response = new AvailabilityResponse();
        DecimalFormatSymbols dfs = new DecimalFormatSymbols(new Locale("en"));
        dfs.setDecimalSeparator('.');

        Digester digester = new Digester();
        digester.push(this);
        digester.addCallMethod("Error", "addError", 1);
        digester.addCallParam("Error/Message", 0);

        digester.addCallMethod("PNAResponse/PartList/Sku", "addResponse", 2);
        digester.addCallParam("PNAResponse/PartList/Sku/PartNumber", 0);
        digester.addCallParam("PNAResponse/PartList/Sku/Price", 1);

        digester.addCallMethod("PNAResponse/PartList/Sku/Stock", "addWarehouse", 4);
        digester.addCallParam("PNAResponse/PartList/Sku/Stock", 0, "ID");
        digester.addCallParam("PNAResponse/PartList/Sku/Stock/Available", 1);
        digester.addCallParam("PNAResponse/PartList/Sku/Stock/ETA", 2);
        digester.addCallParam("PNAResponse/PartList/Sku/Stock/OnOrder", 3);

        try {
            digester.parse(new StringReader(response));
        } catch (Exception e) {
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