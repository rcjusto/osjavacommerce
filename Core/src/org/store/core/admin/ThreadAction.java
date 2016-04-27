package org.store.core.admin;

import org.store.core.globals.StoreMessages;
import org.store.core.globals.StoreThread;
import org.store.core.utils.quartz.ThreadUtilities;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;

/**
 * Rogelio Caballero
 * 10/09/11 15:02
 */
@Namespace(value = "/admin")
@ParentPackage(value = "store-admin")
public class ThreadAction extends AdminModuleAction implements StoreMessages {

    protected static final String STATUS_OK = "ok";
    protected static final String STATUS_ERROR = "error";

    @Action(value = "threadexecutestate", results = @Result(type = "json", params = {"root","jsonResp"}))
    public String getThreadState() throws Exception {
        jsonResp = new HashMap<String, Serializable>();
        if (StringUtils.isNotEmpty(threadName)) {
            jsonResp.put("result", STATUS_OK);
            jsonResp.put("threadName", threadName);
            Thread th = ThreadUtilities.getThread(threadName);
            if (th != null && th instanceof StoreThread) {
                StoreThread sth = (StoreThread) th;
                jsonResp.put("msg", sth.getExecutionMessage());
                jsonResp.put("percent", sth.getExecutionPercent());
            } else {
                jsonResp.put("msg", "FINISHED");
                jsonResp.put("percent", 100);

                File file = new File(getServletContext().getRealPath("/stores/" + storeCode + "/log/" + threadName + ".log"));
                if (file.exists() && file.canRead()) {
                    String log = FileUtils.readFileToString(file);
                    if (StringUtils.isNotEmpty(log)) jsonResp.put("textmsg", log);
                }
            }
        } else {
            jsonResp.put("result", STATUS_ERROR);
            jsonResp.put("msg", "Task not found");
        }
        return SUCCESS;
    }


    private String threadName;

    public String getThreadName() {
        return threadName;
    }

    public void setThreadName(String threadName) {
        this.threadName = threadName;
    }

}
