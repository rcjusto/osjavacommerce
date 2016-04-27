package org.store.core.globals;

import javax.servlet.ServletContext;

public interface IP2CountryService {

    public void init(ServletContext ctx);
    public String getCountryCode(String ip);

}
