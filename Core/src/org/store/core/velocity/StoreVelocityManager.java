package org.store.core.velocity;

import org.apache.struts2.views.velocity.VelocityManager;
import org.apache.velocity.app.Velocity;

import javax.servlet.ServletContext;
import java.util.Properties;

public class StoreVelocityManager extends VelocityManager {

    @Override
    public Properties loadConfiguration(ServletContext context) {
        Properties p = super.loadConfiguration(context);


        p.setProperty(Velocity.RESOURCE_LOADER, "myloader");
        p.setProperty("myloader.resource.loader.class", "org.store.core.velocity.StoreVelocityLoader");
        p.setProperty("myloader.resource.loader.path", context.getRealPath("/"));
        p.setProperty("myloader.resource.loader.modificationCheckInterval", "2");
        p.setProperty("myloader.resource.loader.cache", "true");

        /*
      p.setProperty(Velocity.RESOURCE_LOADER, "myfileloader,mydbloader,myjarloader");
      p.setProperty("myfileloader.resource.loader.class", "org.store.core.velocity.StoreFileVelocityLoader");
      p.setProperty("myfileloader.resource.loader.path", context.getRealPath("/"));
      p.setProperty("myfileloader.resource.loader.modificationCheckInterval", "2");
      p.setProperty("myfileloader.resource.loader.cache", "true");
      p.setProperty("myjarloader.resource.loader.class", "org.store.core.velocity.StoreJarVelocityLoader");
      p.setProperty("mydbloader.resource.loader.class", "org.store.core.velocity.StoreDbVelocityLoader");
        */
        p.setProperty("directive.foreach.counter.name", "velocityCount");
        p.setProperty("directive.foreach.counter.initial.value", "0");
        StringBuilder buff = new StringBuilder(p.getProperty("userdirective"));
        buff.append(",com.googlecode.htmlcompressor.velocity.HtmlCompressorDirective,com.googlecode.htmlcompressor.velocity.XmlCompressorDirective,com.googlecode.htmlcompressor.velocity.JavaScriptCompressorDirective,com.googlecode.htmlcompressor.velocity.CssCompressorDirective");
        p.setProperty("userdirective", buff.toString());
        p.setProperty("userdirective.compressHtml.removeIntertagSpaces", "true");
        p.setProperty("velocimacro.library", "/WEB-INF/views/global_library.vm");
        return p;
    }

}
