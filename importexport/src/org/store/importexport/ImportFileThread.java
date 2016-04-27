package org.store.importexport;

import org.store.importexport.utils.BeanSerializer;
import org.store.core.beans.utils.TableFile;
import org.store.core.globals.BaseAction;
import org.store.core.globals.StoreThread;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Rogelio Caballero
 * 12/04/12 15:54
 */
public class ImportFileThread extends StoreThread {
    
    private File file;
    private List<String> fields;
    private File logFile;


    public ImportFileThread(BaseAction action) {
        setDatabaseConfig(action.getDatabaseConfig());
        setStoreCode(action.getStoreCode());
        setBasePath(action.getServletContext().getRealPath("/"));
    }

    public TableFile getTableFile(String type) {
        BeanSerializer ser = BeanSerializer.getInstance(storeCode, type, file.getName());
        TableFile tableFile = new TableFile();
        try {
            ser.loadFile(file, tableFile, false);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
        return tableFile;
    }
    
    public void saveLogFile() {
        if (logFile!=null) {
            if (logFile.exists()) logFile.delete();
            if (getOutputMessages()!=null && !getOutputMessages().isEmpty()) {
                try {
                    FileUtils.writeLines(logFile, getOutputMessages());
                } catch (IOException e) {
                    log.error(e.getMessage(), e);
                }
            }
        }
    }
    
    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public List<String> getFields() {
        return fields;
    }

    public void setFields(List<String> fields) {
        this.fields = fields;
    }

    public File getLogFile() {
        return logFile;
    }

    public void setLogFile(File logFile) {
        this.logFile = logFile;
    }
}
