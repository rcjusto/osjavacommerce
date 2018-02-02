package org.store.core.admin;

import com.opensymphony.xwork2.ActionContext;
import org.store.core.globals.StoreActionMapping;
import org.store.core.utils.events.EventService;
import org.store.core.utils.events.EventUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.struts2.ServletActionContext;

import java.util.HashMap;
import java.util.Map;

public class OtherAction extends AdminModuleAction {

    public String unknownAction() throws Exception {
        ActionContext context = ActionContext.getContext();
        StoreActionMapping mapping = (StoreActionMapping) context.get(ServletActionContext.ACTION_MAPPING);
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("actionName", mapping.getName()); // por compatibilidad
        map.put("actionMapping", mapping);

        if (EventUtils.executeAdminEvent(getServletContext(), EventService.EVENT_CUSTOM_ACTION, this, map)) {
            if (map.containsKey("result")) {
                return (String) map.get("result");
            } else if (map.containsKey("redirectUrl") && map.get("redirectUrl") instanceof String && StringUtils.isNotEmpty((String) map.get("redirectUrl"))) {
                redirectUrl = (String) map.get("redirectUrl");
                return "redirectUrl";
            } else {
                return null;
            }
        } else {
            return SUCCESS;
        }
    }

}
