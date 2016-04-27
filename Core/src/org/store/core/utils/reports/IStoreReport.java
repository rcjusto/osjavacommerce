package org.store.core.utils.reports;

import org.store.core.globals.BaseAction;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.util.Map;

public abstract class IStoreReport {

    public static Logger log = Logger.getLogger(IStoreReport.class);
    public abstract String[] getCodes(BaseAction action);

    public abstract Map<String, Object> getName(String code, BaseAction action);

    public abstract String loadConfiguration(String code, BaseAction action);

    public abstract ReportTable execute(String code, BaseAction action);

    protected String getParameter(BaseAction action, String paramName) {
        String paramValue = action.getRequest().getParameter(paramName);
        return StringUtils.isNotEmpty(paramValue) ? paramValue : "";
    }

}
