package org.store.core.utils.carriers;

public class CarrierMethod {

    private String code;
    private String name;

    public CarrierMethod() {
    }

    public CarrierMethod(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
