package org.store.carriers.fedex.vo;

import org.xml.sax.SAXException;
import org.apache.commons.digester.Digester;

import java.io.InputStream;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: Rogelio Caballero Justo
 * Date: 20-nov-2006
 */
public class FedexVoidResponse {

    private String errorCode;
    private String errorDescription;


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


    public FedexVoidResponse(InputStream stream) throws IOException, SAXException {
        Digester digester = new Digester();
        digester.push(this);
        digester.addCallMethod("FDXShipDeleteReply/Error", "addError", 2);
        digester.addCallParam("FDXShipDeleteReply/Error/Code", 0);
        digester.addCallParam("FDXShipDeleteReply/Error/Message", 1);

        digester.parse(stream);
    }



    public void addError( String code, String desc) {
        setErrorCode(code);
        setErrorDescription(desc);
    }

    public boolean hasError() {
        return errorCode != null && !"".equals(errorCode);
    }

}
