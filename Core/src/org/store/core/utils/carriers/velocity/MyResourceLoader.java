package org.store.core.utils.carriers.velocity;

import org.apache.velocity.runtime.resource.loader.ResourceLoader;
import org.apache.velocity.runtime.resource.Resource;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.commons.collections.ExtendedProperties;

import java.io.InputStream;

/**
 * Created by IntelliJ IDEA.
 * User: Rogelio Caballero Justo
 * Date: 28-nov-2006
 */
public class MyResourceLoader extends ResourceLoader {
    public void init(ExtendedProperties configuration) {
    }

    public InputStream getResourceStream(String source) throws ResourceNotFoundException {
        return getClass().getClassLoader().getResourceAsStream(source);
    }

    public boolean isSourceModified(Resource resource) {
        return false;
    }

    public long getLastModified(Resource resource) {
        return 0; 
    }
}
