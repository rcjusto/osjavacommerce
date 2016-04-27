package org.store.carriers.ups.sa;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;

import java.io.FileOutputStream;
import java.io.IOException;

/**
 * User: Rogelio Caballero Justo
 * Date: 23-dic-2006
 * Time: 14:59:51
 */
public class PackageResult {

    public static Logger log = Logger.getLogger(PackageResult.class);
    private String trackingNumber;
    private String serviceOptionsChargesCode;
    private Double serviceOptionsChargesValue;
    private String labelImageFormatCode;
    private String graphicImage;


    public String getTrackingNumber() {
        return trackingNumber;
    }

    public void setTrackingNumber(String trackingNumber) {
        this.trackingNumber = trackingNumber;
    }

    public String getServiceOptionsChargesCode() {
        return serviceOptionsChargesCode;
    }

    public void setServiceOptionsChargesCode(String serviceOptionsChargesCode) {
        this.serviceOptionsChargesCode = serviceOptionsChargesCode;
    }

    public Double getServiceOptionsChargesValue() {
        return serviceOptionsChargesValue;
    }

    public void setServiceOptionsChargesValue(Double serviceOptionsChargesValue) {
        this.serviceOptionsChargesValue = serviceOptionsChargesValue;
    }

    public String getLabelImageFormatCode() {
        return labelImageFormatCode;
    }

    public void setLabelImageFormatCode(String labelImageFormatCode) {
        this.labelImageFormatCode = labelImageFormatCode;
    }

    public String getGraphicImage() {
        return graphicImage;
    }

    public void setGraphicImage(String graphicImage) {
        this.graphicImage = graphicImage;
    }


    public PackageResult(String trackingNumber, String serviceOptionsChargesCode, String serviceOptionsChargesValueStr, String labelImageFormatCode, String graphicImage) {
        this.trackingNumber = trackingNumber;
        this.serviceOptionsChargesCode = serviceOptionsChargesCode;
        if (serviceOptionsChargesValueStr != null) {
            try {
                this.serviceOptionsChargesValue = Double.parseDouble(serviceOptionsChargesValueStr);
            } catch (NumberFormatException e) {
                log.error(e.getMessage(), e);
            }
        }
        this.labelImageFormatCode = labelImageFormatCode;
        this.graphicImage = graphicImage;
    }

    public boolean saveGraphicImage(String fileName) {
        try {
            byte[] encodeBytes = graphicImage.getBytes();
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


}
