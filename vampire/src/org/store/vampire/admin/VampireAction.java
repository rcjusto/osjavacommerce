package org.store.vampire.admin;

import org.store.core.admin.AdminModuleAction;
import org.store.core.beans.*;
import org.store.core.globals.BaseAction;
import org.store.core.globals.SomeUtils;
import org.store.vampire.VampireThread;
import groovy.lang.Binding;
import groovy.util.GroovyScriptEngine;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;

import java.io.*;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * Rogelio Caballero
 * 25/04/12 18:28
 */
@Namespace(value = "/admin")
@ParentPackage(value = "store-admin")
public class VampireAction extends AdminModuleAction {

    public static final String PATH_SCRIPTS = "/WEB-INF/script/vampire";

    @Action(value = "vampire_index", results = {@Result(type = "velocity", location = "/WEB-INF/views/org/store/vampire/views/vampire_index.vm")})
    public String index() throws Exception {
        // select available groovy scripts
        File folder = new File(getServletContext().getRealPath(PATH_SCRIPTS));
        String[] scripts = folder.list(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.endsWith(".groovy");
            }
        });
        if (scripts != null && scripts.length > 0) {
            Map<String, Object> map = new HashMap<String, Object>();
            String[] roots = new String[]{getServletContext().getRealPath(PATH_SCRIPTS)};
            GroovyScriptEngine gse = new GroovyScriptEngine(roots);
            Binding binding = new Binding();
            binding.setVariable("dao", getDao());
            binding.setVariable("doAction", "info");
            for (String sName : scripts) {
                gse.run(sName, binding);
                Object result = binding.getVariable("output");
                if (result != null) map.put(sName, result);
            }
            addToStack("scripts", map);
        }
        return SUCCESS;
    }

    @Action(value = "vampire_process_list", results = {@Result(type = "velocity", location = "/WEB-INF/views/org/store/vampire/views/vampire_list.vm")})
    public String processList() throws Exception {
        String[] roots = new String[]{getServletContext().getRealPath(PATH_SCRIPTS)};
        GroovyScriptEngine gse = new GroovyScriptEngine(roots);
        Binding binding = new Binding();
        binding.setVariable("dao", getDao());
        binding.setVariable("doAction", "list");
        for (String u : url) {
            try {
                binding.setVariable("url", u);
                gse.run(script, binding);
                binding.getVariable("output");
                Object result = binding.getVariable("output");
                if (result != null && result instanceof List) {
                    URL url = new URL(u);
                    for(Object o : (List) result) {
                        Map map = (Map) o;
                        String cad = (String) map.get("url");
                        if (StringUtils.isNotEmpty(cad) && !cad.startsWith("http")) {
                            map.put("url", url.getProtocol() + "://" + url.getHost() + cad );
                        }
                    }
                    addToStack("products", result);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return SUCCESS;
    }

    @Action(value = "vampire_process_url", results = {@Result(type = "json", params = {"root", "jsonResp"})})
    public String processUrl() throws Exception {
        jsonResp = new HashMap<String, Serializable>();
        try {
            if (url != null && url.length > 0) {
                String[] roots = new String[]{getServletContext().getRealPath(PATH_SCRIPTS)};
                GroovyScriptEngine gse = new GroovyScriptEngine(roots);
                Binding binding = new Binding();
                binding.setVariable("dao", dao);
                binding.setVariable("url", url[0]);
                binding.setVariable("doAction", "process");
                gse.run(script, binding);
                Object output = binding.getVariable("output");
                if (output != null && output instanceof Map) {
                    Map<String, Object> map = (Map) output;
                    if (StringUtils.isNotEmpty((String) map.get("code"))) {
                        Category cat = dao.getCategory(category);
                        Product product = dao.getProductByMfgPartNumber((String) map.get("code"));
                        if (product == null) {
                            jsonResp.put("result", "Inserted OK");
                            product = new Product();
                            product.setPartNumber((String) map.get("code"));
                            product.setCategory(cat);
                        } else {
                            jsonResp.put("result", "Updated OK");
                        }
                        product.setMfgPartnumber((String) map.get("code"));
                        if (StringUtils.isNotEmpty((String) map.get("price"))) {
                            Double price = SomeUtils.forceStrToDouble((String) map.get("price"));
                            if (price != null) product.setPrice(price);
                        }
                        if (StringUtils.isNotEmpty((String) map.get("manufacturer"))) {
                            Manufacturer manufacturer = dao.getManufacturerByName((String) map.get("manufacturer"));
                            if (manufacturer == null) {
                                manufacturer = new Manufacturer();
                                manufacturer.setManufacturerName((String) map.get("manufacturer"));
                                dao.save(manufacturer);
                            }
                            product.setManufacturer(manufacturer);
                        }

                        // category
                        if (product.getProductCategories() == null) product.setProductCategories(new HashSet<Category>());
                        if (!product.getProductCategories().contains(cat)) product.getProductCategories().add(cat);

                        dao.save(product);

                        // lang
                        if (StringUtils.isNotEmpty((String) map.get("name"))) {
                            for (String lang : getLanguages()) {
                                ProductLang pl = dao.getProductLang(product, lang);
                                if (pl == null) {
                                    pl = new ProductLang();
                                    pl.setProduct(product);
                                    pl.setProductLang(lang);
                                }
                                pl.setProductName((String) map.get("name"));
                                dao.save(pl);
                            }
                        }
                        if (StringUtils.isNotEmpty((String) map.get("desc"))) {
                            for (String lang : getLanguages()) {
                                ProductLang pl = dao.getProductLang(product, lang);
                                if (pl == null) {
                                    pl = new ProductLang();
                                    pl.setProduct(product);
                                    pl.setProductLang(lang);
                                }
                                pl.setDescription((String) map.get("desc"));
                                dao.save(pl);
                            }
                        }

                        // attributos
                        if (map.containsKey("spec")) {
                            Map<String, Map<String, String>> spec = (Map<String, Map<String, String>>) map.get("spec");
                            for (String group : spec.keySet()) {
                                Map<String, String> values = spec.get(group);
                                for (Map.Entry<String, String> e : values.entrySet()) {
                                    AttributeProd ap = dao.getAttributeByName(getDefaultLanguage(), e.getKey(), group, true);
                                    ProductProperty pp = dao.getProductProperty(product, ap);
                                    if (pp == null) {
                                        pp = new ProductProperty();
                                        pp.setProduct(product);
                                        pp.setAttribute(ap);
                                    }
                                    pp.setPropertyValue(e.getValue());
                                    dao.save(pp);
                                }
                            }
                        }

                        if (StringUtils.isNotEmpty((String) map.get("image"))) {
                            try {
                                String folder = getServletContext().getRealPath("/stores/" + getStoreCode() + "/uploads/");
                                FileUtils.forceMkdir(new File(folder));
                                String ext = FilenameUtils.getExtension((String) map.get("image"));
                                File file = new File(folder, product.getPartNumber() + "." + ext);
                                URL url = new URL((String) map.get("image"));
                                InputStream is = url.openStream();
                                OutputStream os = new BufferedOutputStream(new FileOutputStream(file));
                                byte[] b = new byte[2048];
                                int length;
                                while ((length = is.read(b)) != -1) os.write(b, 0, length);
                                os.close();
                                is.close();

                                if (file.exists()) getImageResolver().processImage(product, file, ext);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            jsonResp.put("result","Fail");
        }

        return SUCCESS;
    }

    @Action(value = "vampire_process_urls", results = {@Result(type = "velocity", location = "/WEB-INF/views/org/store/vampire/views/vampire_process.vm")})
    public String processUrls() throws Exception {
        String thName = getStoreCode() + "_vampire";
        VampireThread th = new VampireThread(this, script, url);
        th.setName(thName);
        th.start();
        addToStack("thName", thName);
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

    private String[] url;
    private String script;
    private Long category;

    public String[] getUrl() {
        return url;
    }

    public void setUrl(String[] url) {
        this.url = url;
    }

    public String getScript() {
        return script;
    }

    public void setScript(String script) {
        this.script = script;
    }

    public Long getCategory() {
        return category;
    }

    public void setCategory(Long category) {
        this.category = category;
    }

}
