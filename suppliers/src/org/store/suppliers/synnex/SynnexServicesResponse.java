package org.store.suppliers.synnex;

import org.apache.commons.digester.Digester;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.StringReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class SynnexServicesResponse {

    private org.store.core.utils.suppliers.AvailabilityResponse response;
    private String warehouseCode;
    private SimpleDateFormat datef = new SimpleDateFormat("yyyyMMdd");

    public SynnexServicesResponse(String response, String wh) throws IOException, SAXException {
        this.warehouseCode = wh;
        this.response = new org.store.core.utils.suppliers.AvailabilityResponse();

        Digester digester = new Digester();
        digester.push(this);
        digester.addCallMethod("priceResponse/PriceAvailabilityList", "addResponse", 4);
        digester.addCallParam("priceResponse/PriceAvailabilityList/status", 0);
        digester.addCallParam("priceResponse/PriceAvailabilityList/synnexSKU", 1);
        digester.addCallParam("priceResponse/PriceAvailabilityList/price", 2);
        digester.addCallParam("priceResponse/PriceAvailabilityList/totalQuantity", 3);

        digester.addCallMethod("priceResponse/PriceAvailabilityList/AvailabilityByWarehouse", "addWarehouse", 4);
        digester.addCallParam("priceResponse/PriceAvailabilityList/AvailabilityByWarehouse/warehouseInfo/number", 0);
        digester.addCallParam("priceResponse/PriceAvailabilityList/AvailabilityByWarehouse/qty", 1);
        digester.addCallParam("priceResponse/PriceAvailabilityList/AvailabilityByWarehouse/estimatedArrivalDate", 2);
        digester.addCallParam("priceResponse/PriceAvailabilityList/AvailabilityByWarehouse/onOrderQuantity", 3);

        try {
            digester.parse(new StringReader(response));
        } catch (Exception e) {
            this.response.setError(response);
        }
    }

    public void addResponse(String status, String sku, String price, String total) {
        if ("Active".equalsIgnoreCase(status)) response.setStatus(org.store.core.utils.suppliers.AvailabilityResponse.STATUS_ACTIVE);
        else if ("Discontinued".equalsIgnoreCase(status)) response.setStatus(org.store.core.utils.suppliers.AvailabilityResponse.STATUS_DISCONTINUED);
        else response.setStatus(org.store.core.utils.suppliers.AvailabilityResponse.STATUS_NOT_FOUND);
        response.setSku(sku);
        try {
            Double res = NumberUtils.createDouble(price);
            response.setPrice(res);
        } catch (Exception ignored) {
        }
        try {
            Long stock = NumberUtils.toLong(total);
            response.setStock(stock);
        } catch (Exception ignored) {
        }
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
            } catch (Exception ignored) {
            }
        }
    }


    public org.store.core.utils.suppliers.AvailabilityResponse getResponse() {
        return response;
    }


}
