package org.store.core.utils.velocity;

import org.apache.commons.collections.ExtendedProperties;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.resource.Resource;
import org.apache.velocity.runtime.resource.loader.ResourceLoader;

import java.io.InputStream;

public class StoreResourceLoader extends ResourceLoader {

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
