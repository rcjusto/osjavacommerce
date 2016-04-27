package org.store.suppliers.techdata;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;


public class TechDataEntityResolver implements EntityResolver {

    public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
        if (systemId!=null && systemId.endsWith("/XML_PriceAvailability_Response.dtd")) {
            return getResponseDTD();
        }
        return null; 
    }

    private InputSource getResponseDTD() {
        InputStream is = this.getClass().getResourceAsStream("/org/store/suppliers/techdata/XML_PriceAvailability_Response.dtd");
        return new InputSource(is);
    }

}
