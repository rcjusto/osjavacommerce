package org.store.core.globals;

import org.store.core.globals.config.Store20Database;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Rogelio Caballero
 * 10/09/11 14:59
 */
public class StoreThread extends Thread {

    public static Logger log = Logger.getLogger(StoreThread.class);
    protected List<String> outputMessages = new ArrayList<String>();
    protected Store20Database databaseConfig;
    protected String storeCode;
    protected String basePath = "";

    private String executionMessage;
    private Double executionPercent;

    public String getExecutionMessage() {
        return executionMessage != null ? executionMessage : "";
    }

    public void setExecutionMessage(String executionMessage) {
        this.executionMessage = executionMessage;
    }

    public Double getExecutionPercent() {
        return executionPercent;
    }

    public void setExecutionPercent(Double executionPercent) {
        this.executionPercent = executionPercent;
    }

    public Store20Database getDatabaseConfig() {
        return databaseConfig;
    }

    public void setDatabaseConfig(Store20Database databaseConfig) {
        this.databaseConfig = databaseConfig;
    }

    public String getStoreCode() {
        return storeCode;
    }

    public void setStoreCode(String storeCode) {
        this.storeCode = storeCode;
    }

    public String getBasePath() {
        return basePath;
    }

    public void setBasePath(String basePath) {
        this.basePath = basePath;
    }

    public List<String> getOutputMessages() {
        return outputMessages;
    }

    public void addOutputMessage(String msg) {
        if (StringUtils.isNotEmpty(msg)) {
            outputMessages.add(msg);
        }
    }

    public void setExecutionResult(String result) {
        try {
            FileUtils.forceMkdir(new File(basePath + File.separator + storeCode + File.separator + "log"));
            File file = new File(basePath + File.separator + storeCode + File.separator + "log" +File.separator + getName()+ ".log");
            StringBuilder str = new StringBuilder();
            str.append("<p><strong>").append("Last Execution").append(": </strong>").append(Calendar.getInstance().getTime().toString()).append("</p>");
            str.append("<p><strong>").append("Result").append(": </strong>").append(result).append("</p>");
            if (outputMessages != null && !outputMessages.isEmpty()) {
                str.append("<p>");
                for (String s : outputMessages) str.append(s).append("<br/>");
                str.append("</p>");
            }
            FileUtils.writeStringToFile(file, str.toString());
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }

}
