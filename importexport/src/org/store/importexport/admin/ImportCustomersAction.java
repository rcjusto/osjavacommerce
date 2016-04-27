package org.store.importexport.admin;

import org.store.core.globals.BaseAction;
import org.store.importexport.utils.BeanSerializer;
import org.store.core.beans.utils.TableFile;
import org.store.core.globals.StoreThread;
import org.store.importexport.ImportCustomersThread;
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Rogelio Caballero
 * 12/04/12 15:07
 */
@Namespace(value = "/admin")
@ParentPackage(value = "store-admin")
public class ImportCustomersAction extends ImportBaseAction {

    public static final String TYPE_CUSTOMERS = "customers";

    @Action(value = "import_customers", results = {@Result(type = "velocity", location = "/WEB-INF/views/org/store/importexport/views/import_customers.vm"), @Result(type = "velocity", name = "thread", location = "/WEB-INF/views/org/store/importexport/views/import_customers_thread.vm")})
    public String importCustomers() throws Exception {
        StoreThread sth = getThread();
        if (sth != null) {
            getRequest().setAttribute("thread", sth);
            return "thread";
        } else {
            File logFile = getLogFile();
            if (logFile.exists()) getRequest().setAttribute("lastExecution", FileUtils.readLines(logFile));
            return com.opensymphony.xwork2.Action.SUCCESS;
        }
    }

    @Action(value = "import_customers_fields", results = {@Result(type = "velocity", location = "/WEB-INF/views/org/store/importexport/views/import_customers_fields.vm")})
    public String importCustomersFields() throws Exception {
        if (importFile != null && importFile.length() > 0) {
            File f = File.createTempFile(getStoreCode(), "importCustomers");
            if (f.exists()) f.delete();
            FileUtils.copyFile(importFile, f);
            BeanSerializer ser = BeanSerializer.getInstance(getStoreCode(), TYPE_CUSTOMERS, f.getName());
            TableFile tableFile = new TableFile();
            ser.loadFile(importFile, tableFile, true);
            fileName = f.getAbsolutePath();
            getRequest().setAttribute("fieldNames", tableFile.getHeaders());
        } else {
            addActionError(getText(ERROR_FILE_NOT_SELECTED, ERROR_DEFAULT_FILE_NOT_SELECTED));
        }
        return com.opensymphony.xwork2.Action.SUCCESS;
    }

    @Action(value = "import_customers_do", results = {@Result(type = "velocity", location = "/WEB-INF/views/org/store/importexport/views/import_customers_thread.vm")})
    public String importCustomersDo() throws Exception {
        File f = StringUtils.isNotEmpty(fileName) ? new File(fileName) : null;
        if (f != null && f.exists()) {
            if (fields != null && !fields.isEmpty()) {
                ImportCustomersThread thread = new ImportCustomersThread(this);
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
        return com.opensymphony.xwork2.Action.SUCCESS;
    }

    @Action(value = "import_customers_info", results = {@Result(type = "json", params = {"root", "jsonResp"})})
    public String importCustomersThreadInfo() {
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
        return com.opensymphony.xwork2.Action.SUCCESS;
    }

    public File getLogFile() {
        String fn = getServletContext().getRealPath("/stores/" + getStoreCode() + "/log/import_customers.txt");
        try {
            FileUtils.forceMkdir(new File(FilenameUtils.getPath(fn)));
        } catch (IOException e) {
            BaseAction.log.error(e.getMessage(), e);
        }
        return new File(fn);
    }

    public List<String> getCustomerFields() {
        List<String> res = new ArrayList<String>();
        res.addAll(Arrays.asList("email", "userId", "title", "firstname", "lastname", "sex", "birthday", "registerDate", "visits", "rewardPoints", "phone", "lastIP", "lastBrowser", "language", "level.code"));
        return res;
    }


}
