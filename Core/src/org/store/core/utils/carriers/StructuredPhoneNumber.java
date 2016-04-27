package org.store.core.utils.carriers;

/**
 * User: Rogelio Caballero Justo
 * Date: 23-dic-2006
 * Time: 16:54:59
 */
public class StructuredPhoneNumber {
    private String phoneCountryCode;
    private String phoneDialPlanNumber;
    private String phoneLineNumber;


    public StructuredPhoneNumber(String phoneCountryCode, String phoneDialPlanNumber, String phoneLineNumber) {
        this.phoneCountryCode = phoneCountryCode;
        this.phoneDialPlanNumber = phoneDialPlanNumber;
        this.phoneLineNumber = phoneLineNumber;
    }

    public StructuredPhoneNumber(String phone, String sep) {
        this.phoneLineNumber = "";
        if (phone != null && !"".equals(phone)) {
            String[] arr = phone.split(sep);
            if (arr.length > 0) this.phoneCountryCode = arr[0];
            if (arr.length > 1) this.phoneDialPlanNumber = arr[1];
            if (arr.length > 2) {
                for(int i=2; i<arr.length; i++) this.phoneLineNumber += arr[i];
            }
        }
    }

    public String getPhoneCountryCode() {
        return phoneCountryCode;
    }

    public void setPhoneCountryCode(String phoneCountryCode) {
        this.phoneCountryCode = phoneCountryCode;
    }

    public String getPhoneDialPlanNumber() {
        return phoneDialPlanNumber;
    }

    public void setPhoneDialPlanNumber(String phoneDialPlanNumber) {
        this.phoneDialPlanNumber = phoneDialPlanNumber;
    }

    public String getPhoneLineNumber() {
        return phoneLineNumber;
    }

    public void setPhoneLineNumber(String phoneLineNumber) {
        this.phoneLineNumber = phoneLineNumber;
    }

    public String toString(String sep) {
        String res = "";
        if (this.phoneCountryCode!=null ) res += this.phoneCountryCode;
        if (this.phoneDialPlanNumber!=null ) res += (("".equals(res))?"":sep) + this.phoneDialPlanNumber;
        if (this.phoneLineNumber!=null ) res += (("".equals(res))?"":sep) + this.phoneLineNumber;
        return res;
    }

}
