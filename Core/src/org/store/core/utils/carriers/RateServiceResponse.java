package org.store.core.utils.carriers;


import java.util.ArrayList;

/**
 * User: Rogelio Caballero Justo
 * Date: 04-14-2007
 * Time: 10:16:52 PM
 */
public class RateServiceResponse {

    ArrayList<org.store.core.utils.carriers.RateService> rateServices = new ArrayList<org.store.core.utils.carriers.RateService>();
    String errors;


    public ArrayList<org.store.core.utils.carriers.RateService> getRateServices() {
        return rateServices;
    }

    public void setRateServices(ArrayList<org.store.core.utils.carriers.RateService> rateServices) {
        this.rateServices = rateServices;
    }

    public String getErrors() {
        return errors;
    }

    public void setErrors(String errors) {
        this.errors = errors;
    }

    public void addRateService(org.store.core.utils.carriers.RateService rs) {
        rateServices.add(rs);
    }


}
