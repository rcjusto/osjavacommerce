package org.store.core.utils.reports;

import org.store.core.globals.BaseAction;
import groovy.lang.Binding;
import groovy.util.GroovyScriptEngine;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Map;


public class GroovyStoreReportImpl extends IStoreReport {

    private static final String PATH_REPORTS = "/WEB-INF/script/reports";

    @Override
    public String[] getCodes(BaseAction action) {
        // buscar en la carpeta de script de groovy
        File folder = new File(action.getServletContext().getRealPath(PATH_REPORTS));
        return folder.list(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.endsWith(".groovy");
            }
        });
    }

    @Override
    public Map<String, Object> getName(String code, BaseAction action) {
        String[] roots = new String[]{action.getServletContext().getRealPath(PATH_REPORTS)};
        try {
            GroovyScriptEngine gse = new GroovyScriptEngine(roots);
            Binding binding = new Binding();
            binding.setVariable("action", action);
            binding.setVariable("input", "info");
            gse.run(code, binding);
            return (Map<String, Object>) binding.getVariable("output");
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    @Override
    public String loadConfiguration(String code, BaseAction action) {
        String[] roots = new String[]{action.getServletContext().getRealPath(PATH_REPORTS)};
        try {
            GroovyScriptEngine gse = new GroovyScriptEngine(roots);
            Binding binding = new Binding();
            binding.setVariable("action", action);
            binding.setVariable("input", "config");
            gse.run(code, binding);
            return (String) binding.getVariable("output");
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    @Override
    public ReportTable execute(String code, BaseAction action) {
        String[] roots = new String[]{action.getServletContext().getRealPath(PATH_REPORTS)};
        try {
            GroovyScriptEngine gse = new GroovyScriptEngine(roots);
            Binding binding = new Binding();
            binding.setVariable("action", action);
            binding.setVariable("input", "execute");
            gse.run(code, binding);
            return (ReportTable) binding.getVariable("output");
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }
}
