package org.store.importexport.admin;

import org.store.core.beans.AttributeProd;
import org.store.core.beans.Product;
import org.store.core.beans.ProductProperty;
import org.store.importexport.utils.BeanSerializer;
import org.store.core.beans.utils.TableFile;
import org.store.core.globals.StoreThread;
import org.store.importexport.ImportProductsThread;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Rogelio Caballero
 * 12/04/12 15:07
 */
@Namespace(value = "/admin")
@ParentPackage(value = "store-admin")
public class ImportProductsAction extends ImportBaseAction {

    public static final String TYPE_PRODUCT = "products";

    @Action(value = "import_products", results = {@Result(type = "velocity", location = "/WEB-INF/views/org/store/importexport/views/import_products.vm"), @Result(type = "velocity", name = "thread", location = "/WEB-INF/views/org/store/importexport/views/import_products_thread.vm")})
    public String importProducts() throws Exception {
        StoreThread sth = getThread();
        if (sth != null) {
            getRequest().setAttribute("thread", sth);
            return "thread";
        } else {
            File logFile = getLogFile();
            if (logFile.exists()) getRequest().setAttribute("lastExecution", FileUtils.readLines(logFile));
            return SUCCESS;
        }
    }

    @Action(value = "import_products_fields", results = {@Result(type = "velocity", location = "/WEB-INF/views/org/store/importexport/views/import_products_fields.vm")})
    public String importProductsFields() throws Exception {
        if (importFile != null && importFile.length() > 0) {
            File f = File.createTempFile(getStoreCode(), "importProducts");
            if (f.exists()) f.delete();
            FileUtils.copyFile(importFile, f);
            BeanSerializer ser = BeanSerializer.getInstance(getStoreCode(), TYPE_PRODUCT, f.getName());
            TableFile tableFile = new TableFile();
            ser.loadFile(importFile, tableFile, true);
            fileName = f.getAbsolutePath();
            getRequest().setAttribute("fieldNames", tableFile.getHeaders());
        } else {
            addActionError(getText(ERROR_FILE_NOT_SELECTED, ERROR_DEFAULT_FILE_NOT_SELECTED));
        }
        return SUCCESS;
    }

    @Action(value = "import_products_do", results = {@Result(type = "velocity", location = "/WEB-INF/views/org/store/importexport/views/import_products_thread.vm")})
    public String importProductsDo() throws Exception {
        File f = StringUtils.isNotEmpty(fileName) ? new File(fileName) : null;
        if (f != null && f.exists()) {
            if (fields != null && !fields.isEmpty()) {
                ImportProductsThread thread = new ImportProductsThread(this);
                thread.setName(THREAD_IMPORT_NAME + getStoreCode());
                thread.setFile(f);
                thread.setFields(fields);
                thread.setLogFile(getLogFile());
                thread.start();
                getRequest().setAttribute("thread", thread);
            } else {
                // no hay campos a importar
            }
        } else {
            // no se encontro el archivo a importar  
        }
        return SUCCESS;
    }

    @Action(value = "import_products_info", results = {@Result(type = "json", params = {"root", "jsonResp"})})
    public String importProductThreadInfo() {
        StoreThread sth = getThread();
        jsonResp = new HashMap<String, Serializable>();
        if (sth != null) {
            jsonResp.put("status", "running");
            jsonResp.put("percent", sth.getExecutionPercent());
            jsonResp.put("message", sth.getExecutionMessage());
        } else {
            jsonResp.put("status", "finished");
            File logFile = getLogFile();
            if (logFile.exists()) {
                try {
                    StringBuilder buff = new StringBuilder();
                    List<String> output = FileUtils.readLines(logFile);
                    if (output != null && !output.isEmpty()) for (String msg : output) buff.append("<div>" + msg + "</div>");
                    jsonResp.put("output", buff.toString());
                } catch (IOException ignored) {
                }
            }
        }
        return SUCCESS;
    }

    public File getLogFile() {
        String fn = getServletContext().getRealPath("/stores/" + getStoreCode() + "/log/import_products.txt");
        try {
            FileUtils.forceMkdir(new File(FilenameUtils.getFullPath(fn)));
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
        return new File(fn);
    }


    @Action(value = "import_products_attributes", results = {
            @Result(type = "redirectAction", location = "listproductatt")
    })
    public String productattimport() throws Exception {
        if (importFile != null && importFile.length()>0) {
            TableFile tableFile = new TableFile();
            BeanSerializer ser = BeanSerializer.getInstance(getStoreCode(), null, importFile.getName());
            ser.loadFile(importFile, tableFile, false);

            // create attributes
            List<AttributeProd> attributes = new ArrayList<AttributeProd>();
            int numCols = tableFile.fields.size();
            if (tableFile.fields!=null && tableFile.fields.size()>1) {
                attributes.add(null);
                for(int col=1; col<numCols; col++) {
                    AttributeProd attribute = null;
                    String attFullName = tableFile.fields.get(col);
                    if (StringUtils.isNotEmpty(attFullName)) {
                        String[] arr = attFullName.split(">");
                        String attName = (arr.length>1) ? arr[1].trim() : arr[0].trim();
                        String attGroup = (arr.length>1) ? arr[0].trim() : "";
                        attribute = dao.getAttributeByName(getDefaultLanguage(), attName, attGroup, false);
                        if (attribute==null) {
                            attribute = new AttributeProd();
                            attribute.setAttributeGroup(attGroup);
                            for(String lang : getLanguages()) attribute.setAttributeName(lang, attName);
                            attribute.setInventaryCode(getStoreCode());
                            dao.save(attribute);
                        }
                    }
                    attributes.add(attribute);
                }
            }

            if (attributes.size()>1 && tableFile.getRows()!=null && tableFile.getRows().size()>0) {
                for(TableFile.TableFileRow row : tableFile.getRows()) {
                    String partNumber = row.getCol(0);
                    Product product = dao.getProductByPartNumber(partNumber);
                    if (product!=null) {
                        for(int col=1; col<numCols; col++) {
                            if (attributes.get(col)!=null) {
                                ProductProperty prop = dao.getProductProperty(product, attributes.get(col));
                                if (prop==null) {
                                    prop = new ProductProperty();
                                    prop.setAttribute(attributes.get(col));
                                    prop.setProduct(product);
                                }
                                prop.setPropertyValue(StringUtils.trim(row.getCol(col)));
                                dao.save(prop);
                            }
                        }
                    }
                }
            }

        }
        return SUCCESS;
    }


}
