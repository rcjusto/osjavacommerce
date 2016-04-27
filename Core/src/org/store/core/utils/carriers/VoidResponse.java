package org.store.core.utils.carriers;

/**
 * User: Rogelio Caballero Justo
 * Date: 04-sep-2007
 * Time: 16:34:15
 */
public class VoidResponse {

    private String errorCode;
    private String errorDesc;

    private boolean acepted = false;


    public VoidResponse(boolean acepted) {
        this.acepted = acepted;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorDesc() {
        return errorDesc;
    }

    public void setErrorDesc(String errorDesc) {
        this.errorDesc = errorDesc;
    }

    public boolean isAcepted() {
        return acepted;
    }

    public void setAcepted(boolean acepted) {
        this.acepted = acepted;
    }
}
