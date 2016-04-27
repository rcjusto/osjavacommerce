package org.store.carriers.fedex.sr;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.digester.Digester;
import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by IntelliJ IDEA.
 * User: Rogelio Caballero Justo
 * Date: 20-nov-2006
 */
public class FedexShipResponse {

    public static Logger log = Logger.getLogger(FedexShipResponse.class);
    private String errorCode;
    private String errorDescription;

    private String trackingNumber;
    private String trackingFormId;

    private Double baseCharge;
    private Double totalDiscount;
    private Double totalSurcharge;
    private Double netCharge;
    private Double totalRebate;

    private String outboundLabel;


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

    public String getTrackingNumber() {
        return trackingNumber;
    }

    public void setTrackingNumber(String trackingNumber) {
        this.trackingNumber = trackingNumber;
    }

    public String getTrackingFormId() {
        return trackingFormId;
    }

    public void setTrackingFormId(String trackingFormId) {
        this.trackingFormId = trackingFormId;
    }

    public Double getBaseCharge() {
        return baseCharge;
    }

    public void setBaseCharge(Double baseCharge) {
        this.baseCharge = baseCharge;
    }

    public Double getTotalDiscount() {
        return totalDiscount;
    }

    public void setTotalDiscount(Double totalDiscount) {
        this.totalDiscount = totalDiscount;
    }

    public Double getTotalSurcharge() {
        return totalSurcharge;
    }

    public void setTotalSurcharge(Double totalSurcharge) {
        this.totalSurcharge = totalSurcharge;
    }

    public Double getNetCharge() {
        return netCharge;
    }

    public void setNetCharge(Double netCharge) {
        this.netCharge = netCharge;
    }

    public Double getTotalRebate() {
        return totalRebate;
    }

    public void setTotalRebate(Double totalRebate) {
        this.totalRebate = totalRebate;
    }

    public String getOutboundLabel() {
        return outboundLabel;
    }

    public void setOutboundLabel(String outboundLabel) {
        this.outboundLabel = outboundLabel;
    }

    public FedexShipResponse(InputStream stream) throws IOException, SAXException {
   /*         FileWriter fw = new FileWriter("d:\\fedex_ship.txt");
            InputStreamReader isr = new InputStreamReader(stream);
                BufferedReader br = new BufferedReader(isr);
                String line=null;
                while ( (line = br.readLine()) != null)
                {
                        fw.write(line);
                }
            fw.close();
            br.close();
            isr.close();

        */
        Digester digester = new Digester();
        digester.push(this);

        digester.addCallMethod("FDXShipReply/Tracking", "addTracking", 2);
        digester.addCallParam("FDXShipReply/Tracking/TrackingNumber", 0);
        digester.addCallParam("FDXShipReply/Tracking/FormID", 1);

        digester.addCallMethod("FDXShipReply/EstimatedCharges/DiscountedCharges", "addDiscountedCharges", 5);
        digester.addCallParam("FDXShipReply/EstimatedCharges/DiscountedCharges/BaseCharge", 0);
        digester.addCallParam("FDXShipReply/EstimatedCharges/DiscountedCharges/TotalDiscount", 1);
        digester.addCallParam("FDXShipReply/EstimatedCharges/DiscountedCharges/TotalSurcharge", 2);
        digester.addCallParam("FDXShipReply/EstimatedCharges/DiscountedCharges/NetCharge", 3);
        digester.addCallParam("FDXShipReply/EstimatedCharges/DiscountedCharges/TotalRebate", 4);

        digester.addCallMethod("FDXShipReply/Labels", "addLabel", 1);
        digester.addCallParam("FDXShipReply/Labels/OutboundLabel", 0);

        digester.addCallMethod("FDXShipReply/Error", "addError", 2);
        digester.addCallParam("FDXShipReply/Error/Code", 0);
        digester.addCallParam("FDXShipReply/Error/Message", 1);

        digester.addCallMethod("Error", "addError", 2);
        digester.addCallParam("Error/Code", 0);
        digester.addCallParam("Error/Message", 1);

        digester.parse(stream);
    }


    public void addTracking(String trackNumber, String formId) {
        trackingNumber = trackNumber;
        trackingFormId = formId;
    }

    public void addDiscountedCharges(String bc, String td, String ts, String nc, String tr) {
        baseCharge = strToDouble(bc);
        totalDiscount = strToDouble(td);
        totalSurcharge = strToDouble(ts);
        netCharge = strToDouble(nc);
        totalRebate = strToDouble(tr);
    }

    public void addLabel(String img) {
        outboundLabel = img;
    }

    public void addError(String code, String desc) {
        setErrorCode(code);
        setErrorDescription(desc);
    }

    public boolean hasError() {
        return errorCode != null && !"".equals(errorCode);
    }

    public boolean saveGraphicImage(String fileName) {
        try {
            byte[] encodeBytes = outboundLabel.getBytes();
            byte[] decodeBytes = Base64.decodeBase64(encodeBytes);
            FileOutputStream fos = new FileOutputStream(fileName);
            fos.write(decodeBytes, 0, decodeBytes.length);
            fos.close();
            return true;
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
        return false;
    }

    private Double strToDouble(String cad) {
        if (cad == null) return null;
        Double res;
        try {
            res = Double.parseDouble(cad);
        }
        catch (NumberFormatException nfe) {
            res = null;
        }
        return res;
    }

}
