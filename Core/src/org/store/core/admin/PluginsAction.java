package org.store.core.admin;

import org.store.core.globals.StoreMessages;
import org.store.core.utils.events.EventService;
import org.store.core.utils.events.EventUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;

@Namespace(value = "/admin")
@ParentPackage(value = "store-admin")
public class PluginsAction extends AdminModuleAction implements StoreMessages {

    @Action(value = "editpluginproperty", results = @Result(type = "velocity", location = "/WEB-INF/views/admin/plugin_properties.vm"))
    public String edit() throws Exception {
        if (StringUtils.isNotEmpty(plugin)) {
            EventService service = EventUtils.getPlugin(getServletContext(), plugin);
            if (service != null) {
                addToStack("service", service);
                String template = service.getConfigurationTemplate();
                if (StringUtils.isNotEmpty(template)) addToStack("plugin_template", template);
                service.loadConfigurationData(this);
            }
        }
        return SUCCESS;
    }

    @Action(value = "savepluginproperty", results = {
            @Result(type = "redirectAction", location = "editpluginproperty?plugin=${plugin}"),
            @Result(type = "velocity", name="input", location = "/WEB-INF/views/admin/plugin_properties.vm")
    })
    public String save() throws Exception {
        if (StringUtils.isNotEmpty(plugin)) {
            EventService service = EventUtils.getPlugin(getServletContext(), plugin);
            addToStack("service", service);
            service.saveConfigurationData(this);
        }
        return SUCCESS;
    }

    private String plugin;

    public String getPlugin() {
        return plugin;
    }

    public void setPlugin(String plugin) {
        this.plugin = plugin;
    }
}
