package org.store.core.admin;

import org.store.core.globals.BaseAction;
import groovy.lang.Binding;
import groovy.util.GroovyScriptEngine;
import org.apache.commons.lang.StringUtils;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;

import java.io.File;
import java.io.FilenameFilter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Namespace(value = "/admin")
@ParentPackage(value = "store-admin")
public class ProductProcessingAction extends AdminModuleAction {

    private static final String PATH_SCRIPTS = "/WEB-INF/script/processing";

    @Action(value = "productprocesslist", results = @Result(type = "velocity", location = "/WEB-INF/views/admin/productprocessing.vm"))
    public String list() throws Exception {
        List<Map<String,Object>> listScript = new ArrayList<Map<String, Object>>();
        String[] scripts = getScripts(this);
        if (scripts != null)
            for (String s : scripts) {
                Map<String, Object> map = getName(s, this);
                map.put("code",s);
                listScript.add(map);
            }

        addToStack("scripts", listScript);

        if (StringUtils.isNotEmpty(code)) {
            Map<String, Object> info = getName(code, this);
            if (info!=null) {
                addToStack("info", info);
                String config = loadConfiguration(code, this);
                if (StringUtils.isNotEmpty(config)) addToStack("config", config);
            }
        }

        addToStack("products", dao.listProductsId(idCategory, idSupplier, idManufacturer));

        return SUCCESS;
    }

    @Action(value = "productprocessrun", results = @Result(type = "json", params = {"root","jsonResp"}))
    public String runScript() throws Exception {
        jsonResp = new HashMap<String,Serializable>();
        if (StringUtils.isNotEmpty(code) && idProduct!=null) {
            String res = executeScript();
            if (StringUtils.isNotEmpty(res)) jsonResp.put("msg",res);
        }
        return SUCCESS;
    }

    private String[] getScripts(BaseAction action) {
        // buscar en la carpeta de script de groovy
        File folder = new File(action.getServletContext().getRealPath(PATH_SCRIPTS));
        return folder.list(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.endsWith(".groovy");
            }
        });
    }

    private Map<String, Object> getName(String code, BaseAction action) {
        String[] roots = new String[]{action.getServletContext().getRealPath(PATH_SCRIPTS)};
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

    private String loadConfiguration(String code, BaseAction action) {
        String[] roots = new String[]{action.getServletContext().getRealPath(PATH_SCRIPTS)};
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

    public String executeScript() {
        String res = "";
        String[] roots = new String[]{getServletContext().getRealPath(PATH_SCRIPTS)};
        try {
            GroovyScriptEngine gse = new GroovyScriptEngine(roots);
            Binding binding = new Binding();
            binding.setVariable("action", this);
            binding.setVariable("idProduct", idProduct);
            binding.setVariable("input", "execute");
            gse.run(code, binding);
            res = (String) binding.getVariable("output");
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return e.getMessage();
        }
        return res;
    }

    private String code;
    private Long idProduct;
    private Long idCategory;
    private Long idSupplier;
    private Long idManufacturer;

    public Long getIdProduct() {
        return idProduct;
    }

    public void setIdProduct(Long idProduct) {
        this.idProduct = idProduct;
    }

    public Long getIdCategory() {
        return idCategory;
    }

    public void setIdCategory(Long idCategory) {
        this.idCategory = idCategory;
    }

    public Long getIdSupplier() {
        return idSupplier;
    }

    public void setIdSupplier(Long idSupplier) {
        this.idSupplier = idSupplier;
    }

    public Long getIdManufacturer() {
        return idManufacturer;
    }

    public void setIdManufacturer(Long idManufacturer) {
        this.idManufacturer = idManufacturer;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
