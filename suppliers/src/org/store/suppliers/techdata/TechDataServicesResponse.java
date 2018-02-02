package org.store.suppliers.techdata;

import com.opensymphony.xwork2.util.logging.Logger;
import com.opensymphony.xwork2.util.logging.LoggerFactory;
import org.store.core.utils.suppliers.AvailabilityResponse;
import org.apache.commons.digester.Digester;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;

import java.io.StringReader;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class TechDataServicesResponse {

    private static final Logger LOG = LoggerFactory.getLogger(TechDataServicesResponse.class);
    private AvailabilityResponse response;
    private String warehouseCode;
    private SimpleDateFormat datef = new SimpleDateFormat("MM/dd/yy");

    public TechDataServicesResponse(String data, String wh) {
    //    LOG.debug(data);
        this.warehouseCode = wh;
        this.response = new AvailabilityResponse();
        DecimalFormatSymbols dfs = new DecimalFormatSymbols(new Locale("en"));
        dfs.setDecimalSeparator('.');

        Digester digester = new Digester();
        digester.setEntityResolver(new TechDataEntityResolver());
        digester.push(this);
        digester.addCallMethod("XML_PriceAvailability_Response/Detail/LineInfo/ErrorInfo", "addError", 1);
        digester.addCallParam("XML_PriceAvailability_Response/Detail/LineInfo/ErrorInfo/ErrorDesc", 0);

        digester.addCallMethod("XML_PriceAvailability_Response/Detail/LineInfo", "addResponse", 6);
        digester.addCallParam("XML_PriceAvailability_Response/Detail/LineInfo/RefIDQual1", 0);
        digester.addCallParam("XML_PriceAvailability_Response/Detail/LineInfo/RefID1", 1);
        digester.addCallParam("XML_PriceAvailability_Response/Detail/LineInfo/RefIDQual2", 2);
        digester.addCallParam("XML_PriceAvailability_Response/Detail/LineInfo/RefID2", 3);
        digester.addCallParam("XML_PriceAvailability_Response/Detail/LineInfo/UnitPrice1", 4);
        digester.addCallParam("XML_PriceAvailability_Response/Detail/LineInfo/ItemStatus", 5);

        digester.addCallMethod("XML_PriceAvailability_Response/Detail/LineInfo/WhseInfo", "addWarehouse", 3);
        digester.addCallParam("XML_PriceAvailability_Response/Detail/LineInfo/WhseInfo/WhseCode", 0);
        digester.addCallParam("XML_PriceAvailability_Response/Detail/LineInfo/WhseInfo/Qty", 1);
        digester.addCallParam("XML_PriceAvailability_Response/Detail/LineInfo/WhseInfo/OnOrderETADate", 2);

        try {
            digester.parse(new StringReader(data));
        } catch (Exception e) {
            LOG.error("TE_COGI: " +e.getMessage(), e);
            LOG.error("EL_XML: " + data);
            this.response.setError(data);
        }
    }

    public void addError(String msg) {
        response.setError(msg);
    }

    public void addResponse(String codeType1, String code1, String codeType2, String code2, String price1, String status) {
        if (StringUtils.isNotEmpty(price1) && StringUtils.isNotEmpty(status)) {
            DecimalFormatSymbols dfs = new DecimalFormatSymbols(new Locale("en"));
            dfs.setDecimalSeparator('.');
            dfs.setGroupingSeparator(',');
            DecimalFormat df = new DecimalFormat("$0,000.00",dfs);
            if ("VP".equalsIgnoreCase(codeType1)) response.setSku(code1);
            else if ("VP".equalsIgnoreCase(codeType2)) response.setSku(code2);
            if ("ACTIVE".equalsIgnoreCase(status)) response.setStatus(AvailabilityResponse.STATUS_ACTIVE);
            else if ("NEW".equalsIgnoreCase(status)) response.setStatus(AvailabilityResponse.STATUS_ACTIVE);
            else if ("PHASED OUT".equalsIgnoreCase(status)) response.setStatus(AvailabilityResponse.STATUS_DISCONTINUED);
            else response.setStatus(AvailabilityResponse.STATUS_NOT_FOUND);
            LOG.debug("parsing cost: " + price1);
            try {
                Number res = df.parse(price1);
                if (res != null) {
                    response.setPrice(res.doubleValue());
                    LOG.debug("result: " + res.toString());
                }
            } catch (Exception ignored) {}
        }
    }

    public void addWarehouse(String number, String qty, String eta) {
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
        }
    }

    public AvailabilityResponse getResponse() {
        return response;
    }

}