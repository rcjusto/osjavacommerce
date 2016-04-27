package org.store.core.utils.carriers.velocity;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.Template;

import java.io.File;
import java.io.StringWriter;
import java.util.Properties;

/**
 * Created by IntelliJ IDEA.
 * User: Rogelio Caballero Justo
 * Date: 28-nov-2006
 */
public class VelocityGenerator {
    public static Logger log = Logger.getLogger(VelocityGenerator.class);

    public static String getGeneratedXml(VelocityContext context, String templateName) {
        try {
            Velocity.setProperty(Velocity.RUNTIME_LOG, FilenameUtils.concat(System.getProperty("java.io.tmpdir"), "velocity.log"));
            Velocity.init(getVelocityProperties());
            Template template;
            template = Velocity.getTemplate("org/store/carriers/resources/"+templateName);
            StringWriter writer = new StringWriter();
            if (template != null) template.merge(context, writer);
            return writer.toString();
        }
        catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    private static Properties getVelocityProperties() {
        Properties prop = new Properties();
        prop.setProperty("resource.loader", "mio");
        prop.setProperty("mio.resource.loader.class", "org.store.core.utils.carriers.velocity.MyResourceLoader");
    //    prop.setProperty("file.resource.loader.path", path );
        return prop;
    }

}
