package org.store.template;

import com.opensymphony.xwork2.util.logging.Logger;
import com.opensymphony.xwork2.util.logging.LoggerFactory;
import org.apache.commons.digester.Digester;
import org.apache.commons.lang.StringUtils;
import org.apache.struts2.util.ClassLoaderUtils;
import org.json.JSONException;
import org.json.JSONObject;

import javax.servlet.ServletContext;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Rogelio Caballero
 * 10/01/12 15:14
 */
public class TemplateControlList {

    private static final Logger LOG = LoggerFactory.getLogger(TemplateControlList.class);
    private List<TemplateControl> controls;
    private static final String CONFIG_FILE_IN_FOLDER = "/WEB-INF/classes/controls.xml";
    private static final String CONFIG_FILE_IN_JAR = "org/store/template/controls.xml";

    public TemplateControlList(ServletContext ctx) {
        loadConfigurationFile(ctx);
    }

    public List<TemplateControl> getControls() {
        return controls;
    }

    public void setControls(List<TemplateControl> controls) {
        this.controls = controls;
    }

    public void addControl(TemplateControl tc) {
        if (controls==null) controls = new ArrayList<TemplateControl>();
        controls.add(tc);
    }

    public TemplateControl getControlByName(String name) {
        if (StringUtils.isNotEmpty(name) && controls!=null && !controls.isEmpty()) {
            for(TemplateControl c : controls)
                if (name.equalsIgnoreCase(c.getName())) return c;
        }
        return null;
    }

    public void loadConfigurationFile(ServletContext ctx) {
        InputStream stream = null;
        // buscar en la carpeta classes
        File file = new File(ctx.getRealPath(CONFIG_FILE_IN_FOLDER));
        try { if (file.exists()) stream = new FileInputStream(file); } catch (FileNotFoundException ignored) {}

        // buscar en el jar
        if (stream==null) stream = ClassLoaderUtils.getResourceAsStream(CONFIG_FILE_IN_JAR, TemplateControlList.class);

        // procesar
        if (stream!=null)
            try {
                Digester digester = new Digester();
                digester.push(this);

                digester.addObjectCreate("controls/control", TemplateControl.class);
                digester.addSetProperties("controls/control");
                digester.addSetNext("controls/control", "addControl");
                digester.addCallMethod("controls/control", "setTemplate", 0);

                digester.parse(stream);
            } catch (Exception e) {
                LOG.error(e.getMessage(), e);
            }
    }

    public String toJSON() {
        JSONObject json = new JSONObject();
        if (controls!=null && !controls.isEmpty()) {
            try {
                for(TemplateControl c : controls) {
                    JSONObject props = new JSONObject();
                    props.put("description", c.getDescription());
                    props.put("parameters", c.getParametersArr());
                    json.put(c.getName(),props);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return json.toString();
    }
    
    public TemplateControl getZone(String zone) {
        if (StringUtils.isNotEmpty(zone) && controls!=null && !controls.isEmpty()) {
            for(TemplateControl c : controls)
                if (zone.equalsIgnoreCase(c.getName())) return c;
        }
        return null;
    }

    public String[] getParametersForZone(String zone) {
        if (StringUtils.isNotEmpty(zone) && controls!=null && !controls.isEmpty()) {
            for(TemplateControl c : controls) {
                if (zone.equalsIgnoreCase(c.getName())) return c.getParametersArr();
            }
        }
        return null;
    }

}
