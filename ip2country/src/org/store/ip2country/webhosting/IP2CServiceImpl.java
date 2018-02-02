package org.store.ip2country.webhosting;

import org.store.core.globals.IP2CountryService;
import org.store.ip2country.webhosting.ip2c.Country;
import org.store.ip2country.webhosting.ip2c.IP2Country;
import org.apache.log4j.Logger;

import javax.servlet.ServletContext;

/**
 * Rogelio Caballero
 * 23/07/11 18:54
 */
public class IP2CServiceImpl implements IP2CountryService {

    public static Logger log = Logger.getLogger(IP2CServiceImpl.class);
    private  IP2Country ip2c;

    public void init(ServletContext ctx) {

        try {
            String dbName = ctx.getRealPath(IP2CUtils.BIN_NAME);
            ip2c = new IP2Country(dbName, IP2Country.NO_CACHE);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    public String getCountryCode(String ip) {
        try {
            if ("127.0.0.1".equals(ip)) return "us";
            Country c = (ip2c!=null) ? ip2c.getCountry(ip) : null;
            if (c!=null) return c.get2cStr();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

}
