package org.store.carriers.usps.rs;

import java.util.HashMap;
import java.util.Map;

/**
 * User: Rogelio Caballero Justo
 * Date: 04-11-2007
 * Time: 10:45:12 PM
 */
public class UtilPackage {

    String ID;
    HashMap<String, String> postages = new HashMap<String, String>();
    HashMap<String, Map<String, String>> services = new HashMap<String, Map<String, String>>();

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public void addPostage(String s1, String s2) {
        postages.put(s1, s2);
    }

    public void addService(String s1, String s2, String s3) {
        Map<String, String> map = new HashMap<String, String>();
        map.put("Postage", s1);
        map.put("SvcCommitments", s3);
        services.put(s2, map);
    }

}
