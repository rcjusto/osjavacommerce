package org.store.suppliers.dandh;

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


public class DandHServicesResponse {

    private AvailabilityResponse response;
    private String warehouseCode;
    private SimpleDateFormat datef = new SimpleDateFormat("yyyy-MM-dd");

    public DandHServicesResponse(String data, String wh) {
        this.warehouseCode = wh;
        this.response = new AvailabilityResponse();
        DecimalFormatSymbols dfs = new DecimalFormatSymbols(new Locale("en"));
        dfs.setDecimalSeparator('.');

        Digester digester = new Digester();
        digester.push(this);
        digester.addCallMethod("XMLRESPONSE/STATUS", "addError", 1);
        digester.addCallParam("XMLRESPONSE/STATUS", 0);

        digester.addCallMethod("XMLRESPONSE/ITEM", "addResponse", 2);
        digester.addCallParam("XMLRESPONSE/ITEM/PARTNUM", 0);
        digester.addCallParam("XMLRESPONSE/ITEM/UNITPRICE", 1);

        digester.addCallMethod("XMLRESPONSE/ITEM/BRANCHQTY", "addWarehouse", 3);
        digester.addCallParam("XMLRESPONSE/ITEM/BRANCHQTY/BRANCH", 0);
        digester.addCallParam("XMLRESPONSE/ITEM/BRANCHQTY/QTY", 1);
        digester.addCallParam("XMLRESPONSE/ITEM/BRANCHQTY/INSTOCKDATE", 2);

        try {
            digester.parse(new StringReader(data));
        } catch (Exception e) {
            this.response.setError(data);
        }
    }

    public void addError(String msg) {
        response.setError(msg);
        if ("success".equalsIgnoreCase(msg)) response.setStatus(AvailabilityResponse.STATUS_ACTIVE);
        else response.setStatus(AvailabilityResponse.STATUS_NOT_FOUND);
    }

    public void addResponse(String sku, String price) {
        response.setSku(sku);
        try {
            Double res = NumberUtils.createDouble(price);
            response.setPrice(res);
        } catch (Exception ignored) {}
    }
    public void addWarehouse(String number, String qty, String eta) {
        Long stock = null;
        try {
            stock = NumberUtils.toLong(qty);
        } catch (Exception ignored) {}
        Date d = null;
        try {
            d = StringUtils.isNotEmpty(eta) ? datef.parse(eta) : null;
        } catch (ParseException ignored) {}
        response.addWarehouse(number, stock, d);

        if (StringUtils.isNotEmpty(warehouseCode) && warehouseCode.equalsIgnoreCase(number)) {
            response.setStock(stock);
            response.setEtaStr(eta);
            response.setEtaDate(d);
        }
    }


    public AvailabilityResponse getResponse() {
        return response;
    }


}