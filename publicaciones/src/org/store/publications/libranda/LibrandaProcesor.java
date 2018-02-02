package org.store.publications.libranda;

import org.store.core.dao.HibernateDAO;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Rogelio Caballero
 * 18/09/11 3:07
 */
public class LibrandaProcesor {

    protected HibernateDAO dao;
    private List<String> errors;
    private Map config;

    public LibrandaProcesor(HibernateDAO dao, Map config) {
        this.dao = dao;
        this.config = config;
    }

    protected String getGenCode() {
        return (config!=null && config.containsKey("libranda_gencode")) ? (String) config.get("libranda_gencode") : null;
    }
    protected String getPassword() {
        return (config!=null && config.containsKey("libranda_password")) ? (String) config.get("libranda_password") : null;
    }
    protected String getOutletName() {
        return (config!=null && config.containsKey("libranda_outletName")) ? (String) config.get("libranda_outletName") : null;
    }

    public void addError(String error) {
        if (errors==null) errors = new ArrayList<String>();
        errors.add(error);
    }

    public List<String> getErrors() {
        return errors;
    }

}
