package org.store.pdf.admin;

import org.store.core.admin.AdminModuleAction;
import org.store.core.beans.Product;
import org.store.core.beans.utils.BreadCrumb;
import org.store.core.globals.SomeUtils;
import org.store.pdf.PDFGeneratorThread;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.*;
import java.util.*;

/**
 * Rogelio Caballero
 * 27/04/12 20:35
 */
@Namespace(value = "/admin")
@ParentPackage(value = "store-admin")
public class PDFCatalogAction extends AdminModuleAction {

    private static final String PDF_SEL_PRODUCTS = "pdf_selected_products";

    @Action(value = "pdf_main_config", results = {@Result(type = "velocity", location = "/WEB-INF/views/org/store/pdf/views/config.vm")})
    public String pdf_main_config() throws Exception {
        List<Long> productsId = getSelectedProducts();
        if (productsId != null && !productsId.isEmpty()) addToStack("numProducts", productsId.size());

        String pathName = getServletContext().getRealPath("/stores/" + getStoreCode() + "/pdf/");
        File path = new File(pathName);
        if (path.exists() && path.isDirectory()) {
            File[] list = path.listFiles(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return name.endsWith(".pdf");
                }
            });
            if (list != null && list.length > 0) addToStack("pdfs", list);
        }

        Properties layouts = loadLayouts();
        if (layouts != null && !layouts.isEmpty()) addToStack("layouts", layouts.keySet());

        getBreadCrumbs().add(new BreadCrumb(null, getText("plugin.pdf.generator"), null, null));
        return SUCCESS;
    }

    @Action(value = "pdf_product_sel", results = {@Result(type = "velocity", location = "/WEB-INF/views/org/store/pdf/views/product_sel.vm")})
    public String pdf_product_sel() throws Exception {
        List<Long> selectedProducts = getSelectedProducts();

        // adicionar los nuevos
        if (newproducts != null && newproducts.length > 0) {
            for (String idS : newproducts) {
                Long idP = SomeUtils.strToLong(idS);
                if (idP != null && !selectedProducts.contains(idP)) selectedProducts.add(idP);
            }
        }

        // eliminar
        if (getSelecteds() != null && getSelecteds().length > 0) {
            for (Long idP : getSelecteds())
                if (selectedProducts.contains(idP)) selectedProducts.remove(idP);
        }

        // leer datos
        if (!selectedProducts.isEmpty()) {
            List<Map<String, String>> list = new ArrayList<Map<String, String>>();
            for (Long idP : selectedProducts) {
                Product product = getProduct(idP);
                if (product != null) {
                    Map<String, String> m = new HashMap<String, String>();
                    m.put("id", product.getIdProduct().toString());
                    m.put("name", product.getProductName(getDefaultLanguage()));
                    m.put("code", product.getPartNumber());
                    list.add(m);
                }
            }
            addToStack("products", list);
        }

        return SUCCESS;
    }

    @Action(value = "pdf_remove_pdf", results = {@Result(type = "redirectAction", location = "pdf_main_config")})
    public String pdf_remove_pdf() throws Exception {
        if (StringUtils.isNotEmpty(filename)) {
            File path = new File(getServletContext().getRealPath("/stores/" + getStoreCode() + "/pdf/" + filename));
            if (path.exists()) path.delete();
        }
        return SUCCESS;
    }

    @Action(value = "pdf_generate_pdf", results = {
            @Result(type = "velocity", location = "/WEB-INF/views/org/store/pdf/views/result_thread.vm"),
            @Result(type = "velocity", name = "custom", location = "/WEB-INF/views/org/store/pdf/views/result.vm")
    })
    public String pdf_generate_pdf() throws Exception {
        if ("custom".equalsIgnoreCase(mode)) {
            ArrayList<String> resp = new ArrayList<String>();
            if (StringUtils.isNotEmpty(url)) {
                try {
                    if (StringUtils.isEmpty(filename)) filename = String.valueOf(Calendar.getInstance().getTimeInMillis());
                    String pathName = getServletContext().getRealPath("/stores/" + getStoreCode() + "/pdf/");
                    try {
                        FileUtils.forceMkdir(new File(pathName));
                    } catch (IOException ignored) {
                    }
                    if (!pathName.endsWith(File.separator)) pathName += File.separator;

                    String path = pathName + filename + ".pdf";
                    OutputStream os = new FileOutputStream(path);
                    ITextRenderer renderer = new ITextRenderer();
                    renderer.setDocument(url);
                    renderer.layout();
                    renderer.createPDF(os);
                    os.close();
                    resp.add(FilenameUtils.getName(path));
                } catch (Exception e) {
                    addActionError(e.getMessage());
                    e.printStackTrace();
                }
            }
            if (!resp.isEmpty()) {
                addToStack("files", resp);
            } else {
                addActionError("Unknown Error");
            }
            return "custom";
        } else {
            Properties layouts = loadLayouts();
            if (StringUtils.isEmpty(layout) || !layouts.containsKey(layout)) layout = (String) layouts.keySet().iterator().next();
            String pathLayout = layouts.getProperty(layout);
            Long idLevel = SomeUtils.strToLong(userLevel);

            PDFGeneratorThread thread = new PDFGeneratorThread(this, idLevel, mode, pathLayout, filename);
            thread.setName("pdf_gen_" + getRequest().getSession().getId());
            Long idCategory = SomeUtils.strToLong(category);
            thread.setIdCategory(idCategory);
            thread.setProductIds(getSelectedProducts());
            thread.start();

            addToStack("filename", filename);
            return SUCCESS;
        }
    }

    private List<Long> getSelectedProducts() {
        if (getStoreSessionObjects().containsKey(PDF_SEL_PRODUCTS)) {
            Object o = getStoreSessionObjects().get(PDF_SEL_PRODUCTS);
            if (o instanceof List) return (ArrayList<Long>) o;
        }
        List<Long> l = new ArrayList<Long>();
        getStoreSessionObjects().put(PDF_SEL_PRODUCTS, l);
        return l;
    }

    private Properties loadLayouts() {
        Properties properties = new Properties();
        // buscar en carpeta del comercio
        try {
            File defFile = new File(getServletContext().getRealPath("/WEB-INF/views/pdf/" + getStoreCode() + "/layouts.properties"));
            if (defFile.exists()) properties.load(new FileInputStream(defFile));
        } catch (IOException ignored) {
        }

        // buscar en el del comercio
        try {
            InputStream is = getClass().getResourceAsStream("/pdf/" + getStoreCode() + "/layouts.properties");
            if (is != null) properties.load(is);
        } catch (IOException ignored) {
        }

        // buscar la carpeta default
        if (properties.isEmpty()) {
            try {
                File defFile = new File(getServletContext().getRealPath("/WEB-INF/views/org/store/pdf/layouts.properties"));
                if (defFile.exists()) properties.load(new FileInputStream(defFile));
            } catch (IOException ignored) {
            }
        }

        // buscar en el jar
        if (properties.isEmpty()) {
            try {
                InputStream is = getClass().getResourceAsStream("/org/store/pdf/layouts.properties");
                if (is != null) properties.load(is);
            } catch (IOException ignored) {
            }
        }

        return properties;
    }

    private String url;
    private String layout;
    private String userLevel;
    private String category;
    private String filename;
    private String mode;
    private String[] newproducts;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getLayout() {
        return layout;
    }

    public void setLayout(String layout) {
        this.layout = layout;
    }

    public String getUserLevel() {
        return userLevel;
    }

    public void setUserLevel(String userLevel) {
        this.userLevel = userLevel;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String[] getNewproducts() {
        return newproducts;
    }

    public void setNewproducts(String[] newproducts) {
        this.newproducts = newproducts;
    }
}
