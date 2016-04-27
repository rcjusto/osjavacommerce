package org.store.core.utils.velocity;

import org.store.core.velocity.VelocityUtils;
import org.apache.log4j.Logger;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.tools.generic.AlternatorTool;
import org.apache.velocity.tools.generic.DateTool;
import org.apache.velocity.tools.generic.EscapeTool;
import org.apache.velocity.tools.generic.ListTool;
import org.apache.velocity.tools.generic.MathTool;
import org.apache.velocity.tools.generic.NumberTool;

import java.io.File;
import java.io.FileWriter;
import java.io.StringWriter;
import java.util.Properties;

public class StoreVelocityGenerator {
    public static Logger log = Logger.getLogger(StoreVelocityGenerator.class);

    public static String getGeneratedString(VelocityContext context, String templateName) {
        try {
            Properties props = (context!=null && context.containsKey("application-path")) ? getVelocityPropertiesEx((String) context.get("application-path")) : getVelocityProperties();
            Velocity.init(props);
            Template template;
            template = Velocity.getTemplate(templateName);
            StringWriter writer = new StringWriter();
            if (template != null) template.merge(context, writer);
            return writer.toString();
        }
        catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    public static void generateFile(VelocityContext context, String templateName, File file) {
        try {
            Properties props = (context!=null && context.containsKey("application-path")) ? getVelocityPropertiesEx((String) context.get("application-path")) : getVelocityProperties();
            Velocity.init(props);
            Template template;
            template = Velocity.getTemplate(templateName);
            FileWriter writer = new FileWriter(file);
            if (template != null) template.merge(context, writer);
            writer.close();
        }
        catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    private static Properties getVelocityProperties() {
        Properties prop = new Properties();
        prop.setProperty("runtime.log.logsystem.class", "org.apache.velocity.runtime.log.NullLogSystem");
        prop.setProperty("resource.loader", "store");
        prop.setProperty("store.resource.loader.class", "org.store.core.utils.velocity.StoreResourceLoader");

        return prop;
    }

    private static Properties getVelocityPropertiesEx(String path) {
        Properties prop = new Properties();
        prop.setProperty("runtime.log.logsystem.class", "org.apache.velocity.runtime.log.NullLogSystem");
        prop.setProperty(Velocity.RESOURCE_LOADER, "myfileloader,mydbloader,myjarloader");
        prop.setProperty("myfileloader.resource.loader.class", "org.store.core.velocity.StoreFileVelocityLoader");
        prop.setProperty("myfileloader.resource.loader.path", path);
        prop.setProperty("myfileloader.resource.loader.modificationCheckInterval", "2");
        prop.setProperty("myfileloader.resource.loader.cache", "true");
        prop.setProperty("myjarloader.resource.loader.class", "org.store.core.velocity.StoreJarVelocityLoader");
        prop.setProperty("mydbloader.resource.loader.class", "org.store.core.velocity.StoreDbVelocityLoader");
        prop.setProperty("directive.foreach.counter.name", "velocityCount");
        prop.setProperty("directive.foreach.counter.initial.value", "0");
        prop.setProperty("velocimacro.library", "/WEB-INF/views/global_library.vm");
        return prop;
    }

    public static VelocityContext createContext() {
        VelocityContext context = new VelocityContext();
        context.put("math", new MathTool());
        context.put("number", new NumberTool());
        context.put("date", new DateTool());
        context.put("esc", new EscapeTool());
        context.put("list", new ListTool());
        context.put("alternator", new AlternatorTool());
        context.put("util", new VelocityUtils());
        return context;
    }

}
