package org.store.carriers.fedex.sb;


import org.xml.sax.SAXException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;

/**
 * Created by IntelliJ IDEA.
 * User: Rogelio Caballero Justo
 * Date: 20-nov-2006
 */
public class FedexSubscriptionResponse {

    private String errorCode;
    private String errorDescription;

    private String xml;


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

    public String getXml() {
        return xml;
    }

    public void setXml(String xml) {
        this.xml = xml;
    }

    public FedexSubscriptionResponse(InputStream stream) throws IOException, SAXException {
            StringWriter fw = new StringWriter();
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

        xml = fw.toString();
    }


    public void addError(String code, String desc, String location) {
        setErrorCode(code);
        setErrorDescription(desc);
    }

    public boolean hasError() {
        return errorCode!=null && !"".equals(errorCode);
    }

}
