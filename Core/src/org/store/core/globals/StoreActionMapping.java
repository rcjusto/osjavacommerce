package org.store.core.globals;

import org.apache.struts2.dispatcher.mapper.ActionMapping;

public class StoreActionMapping extends ActionMapping {

    private String domain;
    private String store;

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getStore() {
        return store;
    }

    public void setStore(String store) {
        this.store = store;
    }

}
