package org.store.vampire;

import org.store.vampire.admin.VampireAction;
import org.store.core.dao.HibernateDAO;
import org.store.core.globals.BaseAction;
import org.store.core.globals.StoreThread;
import org.store.core.hibernate.HibernateSessionFactory;
import groovy.lang.Binding;
import groovy.util.GroovyScriptEngine;
import groovy.util.ResourceException;
import groovy.util.ScriptException;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.io.IOException;
import java.util.Calendar;

/**
 * Rogelio Caballero
 * 25/04/12 18:45
 */
public class VampireThread extends StoreThread {

    private String[] urls;
    private String script;
    private String scriptPath;

    public VampireThread(BaseAction action, String script, String[] urls) {
        setDatabaseConfig(action.getDatabaseConfig());
        setStoreCode(action.getStoreCode());
        setBasePath(action.getServletContext().getRealPath("/"));
        this.scriptPath = action.getServletContext().getRealPath(VampireAction.PATH_SCRIPTS);
        this.script = script;
        this.urls = urls;
    }
    
    @Override
    public void run() {

        try {
            Session session = HibernateSessionFactory.getNewSession(databaseConfig);
            Transaction tx = session.beginTransaction();
            HibernateDAO dao = new HibernateDAO(session, storeCode);
            try {
                addOutputMessage("Start time: " + Calendar.getInstance().getTime().toString());

                for(String url : urls) {
                    processUrl(dao, url);
                }

                addOutputMessage("End time: " + Calendar.getInstance().getTime().toString());

            } catch (Exception e) {
                log.error(e.getMessage(), e);
                tx.rollback();
                addOutputMessage("ERROR: " + e.getMessage());
            } finally {
                if (session.isOpen()) session.close();
            }

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        
        
    }

    private Object processUrl(HibernateDAO dao, String url) throws IOException, ScriptException, ResourceException {
        String[] roots = new String[]{scriptPath};
        GroovyScriptEngine gse = new GroovyScriptEngine(roots);
        Binding binding = new Binding();
        binding.setVariable("dao", dao);
        binding.setVariable("urls", url);
        binding.setVariable("doAction", "process");
        gse.run(script, binding);
        return binding.getVariable("output");
    }

}
