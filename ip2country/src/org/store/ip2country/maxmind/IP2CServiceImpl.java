package org.store.ip2country.maxmind;

import org.store.core.globals.IP2CountryService;
import org.store.ip2country.maxmind.geoip.Country;
import org.store.ip2country.maxmind.geoip.LookupService;
import org.apache.log4j.Logger;

import javax.servlet.ServletContext;

/**
 * Rogelio Caballero
 * 23/07/11 18:54
 */
public class IP2CServiceImpl implements IP2CountryService {

    public static Logger log = Logger.getLogger(IP2CServiceImpl.class);
    private LookupService cl;
    private static final String CNT_DB_FILENAME = "/WEB-INF/GeoIP.dat";

    public void init(ServletContext ctx) {
        try {
            String dbFile = ctx.getRealPath(CNT_DB_FILENAME);
            cl = new LookupService(dbFile, LookupService.GEOIP_MEMORY_CACHE);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    public String getCountryCode(String ip) {
        try {
            if ("127.0.0.1".equals(ip)) return "us";
            Country c = (cl!=null) ? cl.getCountry(ip) : null;
            if (c!=null) return c.getCode();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

}
