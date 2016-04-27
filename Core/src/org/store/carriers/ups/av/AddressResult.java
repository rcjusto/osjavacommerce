package org.store.carriers.ups.av;

import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: Rogelio Caballero Justo
 * Date: 19-nov-2006
 */
public class AddressResult {

    private int rank;
    private float quality;
    private String city;
    private String stateProvinceCode;
    private ArrayList<String> postalCodes;


    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getStateProvinceCode() {
        return stateProvinceCode;
    }

    public void setStateProvinceCode(String stateProvinceCode) {
        this.stateProvinceCode = stateProvinceCode;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public float getQuality() {
        return quality;
    }

    public void setQuality(float quality) {
        this.quality = quality;
    }


    public ArrayList<String> getPostalCodes() {
        return postalCodes;
    }

    public void setPostalCodes(ArrayList<String> postalCodes) {
        this.postalCodes = postalCodes;
    }
}
