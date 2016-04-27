package org.store.config;

import org.apache.struts2.views.velocity.VelocityManager;
import org.apache.velocity.app.Velocity;

import javax.servlet.ServletContext;
import java.util.Properties;

/**
 * Rogelio Caballero
 * 18/02/12 19:00
 */
public class ConfigVelocityManager extends VelocityManager {

    @Override
    public Properties loadConfiguration(ServletContext context) {
        Properties p = super.loadConfiguration(context);
        p.setProperty(Velocity.RESOURCE_LOADER, "jarloader");
        p.setProperty("jarloader.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
        p.setProperty("directive.foreach.counter.name", "velocityCount");
        p.setProperty("directive.foreach.counter.initial.value", "0");
        return p;
    }

}
