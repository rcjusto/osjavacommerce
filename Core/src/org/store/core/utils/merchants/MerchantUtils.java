package org.store.core.utils.merchants;

import org.store.core.globals.BaseAction;
import org.store.core.globals.config.Store20Config;
import org.apache.log4j.Logger;

import javax.servlet.ServletContext;
import java.util.Map;

public class MerchantUtils {

    public static Logger log = Logger.getLogger(MerchantUtils.class);
    private Map<String, Class> merchants;

    public MerchantUtils(ServletContext context) {
        synchronized (context) {
            merchants = Store20Config.getInstance(context).getMapMerchants();
        }
    }

    public Map<String, Class> getMerchants() {
        return merchants;
    }

    public MerchantService getService(String serviceName, BaseAction action) {
        if (merchants != null && merchants.containsKey(serviceName)) {
            try {
                Object o = merchants.get(serviceName).newInstance();
                if (o instanceof MerchantService) {
                    ((MerchantService) o).loadProperties(action);
                    return (MerchantService) o;
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
