package org.store.core.utils.carriers;

import org.store.core.globals.config.Store20Config;
import org.apache.log4j.Logger;

import javax.servlet.ServletContext;
import java.util.Map;
import java.util.Properties;


/**
 * User: Rogelio Caballero Justo
 * Date: 02-12-2007
 * Time: 06:47:54 PM
 */
public class CarrierUtils {

    public static Logger log = Logger.getLogger(CarrierUtils.class);
    public static final String CARRIER_STATUS_TESTING = "test";
    public static final String CARRIER_STATUS_LIVE = "live";

    private Map<String, Class> carriers;

    public CarrierUtils(ServletContext context) {
        synchronized (context) {
            carriers = Store20Config.getInstance(context).getMapCarrier();
        }
    }

    public Map<String, Class> getCarriers() {
        return carriers;
    }

    public CarrierService getCarrierService(String carrier, Properties p) {
        if (carriers!=null && carriers.containsKey(carrier)) {
            try {
                Object o = carriers.get(carrier).newInstance();
                if (o instanceof CarrierService) {
                    ((CarrierService)o).setProperties(p);
                    return (CarrierService) o;
                }
            } catch (InstantiationException e) {
                log.error(e.getMessage(), e);
            } catch (IllegalAccessException e) {
                log.error(e.getMessage(), e);
            }
        }
        return null;
    }

}
