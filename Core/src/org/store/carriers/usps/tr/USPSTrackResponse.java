package org.store.carriers.usps.tr;

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
public class USPSTrackResponse {

    private String errorCode;
    private String errorDescription;

    private String guaranteedDeliveryDate;
    private String trackSummary;
    private ArrayList<String> trackDetails = new ArrayList<String>();

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


    public String getGuaranteedDeliveryDate() {
        return guaranteedDeliveryDate;
    }

    public void setGuaranteedDeliveryDate(String guaranteedDeliveryDate) {
        this.guaranteedDeliveryDate = guaranteedDeliveryDate;
    }

    public String getTrackSummary() {
        return trackSummary;
    }

    public void setTrackSummary(String trackSummary) {
        this.trackSummary = trackSummary;
    }

    public ArrayList<String> getTrackDetails() {
        return trackDetails;
    }

    public void setTrackDetails(ArrayList<String> trackDetails) {
        this.trackDetails = trackDetails;
    }

    public USPSTrackResponse(InputStream stream) throws IOException, SAXException {
    /*    FileWriter fw = new FileWriter("c:\\pepe.txt");
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

        digester.addCallMethod("Error", "addError", 2);
        digester.addCallParam("Error/Number", 0);
        digester.addCallParam("Error/Description", 1);


        digester.addBeanPropertySetter("TrackResponse/TrackInfo/TrackSummary","trackSummary");
        digester.addBeanPropertySetter("TrackResponse/TrackInfo/GuaranteedDeliveryDate","guaranteedDeliveryDate");

        digester.addCallMethod("TrackResponse/TrackInfo/TrackDetail", "addDetail", 1);
        digester.addCallParam("TrackResponse/TrackInfo/TrackDetail", 0);

        digester.parse(stream);
    }

    public void addError(String code, String desc) {
        setErrorCode(code);
        setErrorDescription(desc);
    }

    public void addDetail(String desc) {
        trackDetails.add(desc);
    }

    public boolean hasError() {
        return errorCode != null && !"".equals(errorCode);
    }

    
}
