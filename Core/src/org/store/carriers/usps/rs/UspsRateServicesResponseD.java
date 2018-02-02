package org.store.carriers.usps.rs;

import org.apache.commons.digester.Digester;
import org.apache.commons.digester.xmlrules.DigesterLoader;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Locale;

/**
 * Created by IntelliJ IDEA.
 * User: Rogelio Caballero Justo
 * Date: 20-nov-2006
 */
public class UspsRateServicesResponseD {

    private String errorCode;
    private String errorDescription;

    private ArrayList<org.store.core.utils.carriers.RateService> rateServices;

    private HashSet<String> pcKeys = new HashSet<String>();
    private LinkedList<UtilPackage> pckList = new LinkedList<UtilPackage>();

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

    public ArrayList<org.store.core.utils.carriers.RateService> getRateServices() {
        updateRates();
        return rateServices;
    }

    public void setRateServices(ArrayList<org.store.core.utils.carriers.RateService> rateServices) {
        this.rateServices = rateServices;
    }

    public void addPack(UtilPackage p) {
        pckList.addLast(p);
        if (pcKeys.isEmpty()) {
            pcKeys.addAll(p.postages.keySet());
        } else {
            HashSet<String> tempSet = new HashSet<String>(pcKeys);
            for (String key : tempSet) {
                if (!p.postages.keySet().contains(key)) {
                    pcKeys.remove(key);
                }
            }
        }
    }

    public UspsRateServicesResponseD(InputStream stream) throws IOException, SAXException {
/*
            FileWriter fw = new FileWriter("c:\\pepe.txt");
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
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Digester digester = DigesterLoader.createDigester(classLoader.getResource("rules_usps_rsd.xml"));
        digester.push(this);
        digester.parse(stream);
//        digester.parse(classLoader.getResourceAsStream("temp_usps.xml"));
    }

    private void updateRates() {
        if (rateServices == null) rateServices = new ArrayList<org.store.core.utils.carriers.RateService>();
        else rateServices.clear();
        DecimalFormatSymbols symb = new DecimalFormatSymbols(new Locale("en","US"));
        symb.setDecimalSeparator('.');
        DecimalFormat df = new DecimalFormat("0.00",symb);
        for (String key : pcKeys) {
            float totalRate = 0;
            for (UtilPackage up : pckList) {
                String rateStr = up.postages.get(key);
                totalRate += strToFloat(rateStr);
            }
            org.store.core.utils.carriers.RateService rs = new org.store.core.utils.carriers.RateService(key, "USD", df.format(totalRate), null);
            rateServices.add(rs);
        }
    }


    public void addError(String code, String desc, String location) {
        setErrorCode(code);
        setErrorDescription(desc);
    }

    public boolean hasError() {
        return errorCode != null && !"".equals(errorCode);
    }

    private float strToFloat(String cad) {
        if (cad == null) return 0;
        float res;
        try {
            res = Float.parseFloat(cad);
        }
        catch (NumberFormatException nfe) {
            res = 0;
        }
        return res;
    }

    private URL getRules() {
        URL rulesURL = ClassLoader.getSystemResource("rules_usps.xml");
        return rulesURL;
    }

}
