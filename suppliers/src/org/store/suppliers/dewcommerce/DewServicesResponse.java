package org.store.suppliers.dewcommerce;

import org.store.core.globals.SomeUtils;
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


public class DewServicesResponse {

    private AvailabilityResponse response;
    private SimpleDateFormat datef = new SimpleDateFormat("yyyy-MM-dd");

    public DewServicesResponse(String data) {
        this.response = new AvailabilityResponse();
        DecimalFormatSymbols dfs = new DecimalFormatSymbols(new Locale("en"));
        dfs.setDecimalSeparator('.');

        Digester digester = new Digester();
        digester.push(this);
        digester.addCallMethod("CatalogResponse/Error", "addError", 1);
        digester.addCallParam("CatalogResponse/Error", 0);

        digester.addCallMethod("CatalogResponse/ProductDetail", "addResponse", 4);
        digester.addCallParam("CatalogResponse/ProductDetail/Stock", 0);
        digester.addCallParam("CatalogResponse/ProductDetail/Price", 1);
        digester.addCallParam("CatalogResponse/ProductDetail/Price", 2, "currency");
        digester.addCallParam("CatalogResponse/ProductDetail/Eta", 3);

        try {
            digester.parse(new StringReader(data));
        } catch (Exception e) {
            this.response.setError(data);
        }
    }

    public void addError(String msg) {
        response.setError(msg);
        response.setStatus(AvailabilityResponse.STATUS_NOT_FOUND);
    }

    public void addResponse(String qty, String price, String currency, String eta) {
        response.setPrice(SomeUtils.strToDouble(price));
        Long stock = null;
        try {
            stock = NumberUtils.toLong(qty);
            response.setStatus(AvailabilityResponse.STATUS_ACTIVE);
        } catch (Exception ignored) {}
        Date d = null;
        try {
            d = StringUtils.isNotEmpty(eta) ? datef.parse(eta) : null;
        } catch (ParseException ignored) {}
        response.addWarehouse("", stock, d);

        response.setStock(stock);
        response.setEtaStr(eta);
        response.setEtaDate(d);
    }


    public AvailabilityResponse getResponse() {
        return response;
    }


}