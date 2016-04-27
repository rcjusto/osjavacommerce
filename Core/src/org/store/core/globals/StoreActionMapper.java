package org.store.core.globals;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.config.Configuration;
import com.opensymphony.xwork2.config.ConfigurationManager;
import com.opensymphony.xwork2.config.entities.PackageConfig;
import org.apache.struts2.ServletActionContext;
import org.apache.struts2.dispatcher.mapper.ActionMapping;
import org.apache.struts2.dispatcher.mapper.DefaultActionMapper;

import javax.servlet.http.HttpServletRequest;

public class StoreActionMapper extends DefaultActionMapper {

    @Override
    public ActionMapping getMapping(HttpServletRequest request, ConfigurationManager configManager) {
        StoreActionMapping mapping = new StoreActionMapping();
        String uri = getUri(request);
        mapping.setDomain(request.getServerName());
        int indexOfSemicolon = uri.indexOf(";");
        uri = (indexOfSemicolon > -1) ? uri.substring(0, indexOfSemicolon) : uri;

        uri = dropExtension(uri, mapping);
        if (uri == null) {
            return null;
        }

        parseNameAndNamespace(uri, mapping, configManager);

        handleSpecialParameters(request, mapping);

        if (mapping.getName() == null) {
            return null;
        }

        parseActionName(mapping);
        return mapping;
    }

    protected void parseNameAndNamespace(String uri, StoreActionMapping mapping, ConfigurationManager configManager) {
        String namespace = "", uriPrefix = "", name;
        int lastSlash = uri.lastIndexOf("/");
        if (lastSlash == -1) {
            name = uri;
        } else if (lastSlash == 0) {
            // root
            namespace = "/";
            name = uri.substring(1);
        } else {
            namespace = "/";
            Configuration config = configManager.getConfiguration();
            name = uri.substring(lastSlash + 1);
            uriPrefix = uri.substring(0, lastSlash);
            for (Object cfg : config.getPackageConfigs().values()) {
                String ns = ((PackageConfig) cfg).getNamespace();
                if (ns != null && !"".equals(ns) && uriPrefix.endsWith(ns)) {
                    namespace = ns;
                    uriPrefix = uriPrefix.substring(0, uriPrefix.length() - namespace.length());
                    break;
                }
            }
        }

        mapping.setNamespace(namespace);
        mapping.setStore(uriPrefix.startsWith("/") ? uriPrefix.substring(1) : uriPrefix);
        mapping.setName(name);
    }

    @Override
    public String getUriFromActionMapping(ActionMapping mapping) {
        StringBuffer buff = new StringBuffer("");
        String actionName = (mapping.getName().startsWith("/")) ? mapping.getName().substring(1) : mapping.getName();
        ActionContext context = ActionContext.getContext();
        if (context != null) {
            StoreActionMapping orig = (StoreActionMapping) context.get(ServletActionContext.ACTION_MAPPING);
            if (orig != null) {
                String namespace = (orig.getNamespace().startsWith("/")) ? orig.getNamespace().substring(1) : orig.getNamespace();
                buff.append(orig.getStore());
                if (namespace != null && !"".equals(namespace)) {
                    if (!"".equals(buff.toString())) buff.append("/");
                    buff.append(namespace);
                }
            }
        }

        if (!"".equals(buff.toString())) buff.append("/");
        buff.append(actionName);

        mapping.setNamespace("");
        mapping.setName(buff.toString());
        return super.getUriFromActionMapping(mapping);
    }

}
