package org.store.xmlrequest;

import org.apache.commons.digester.Digester;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

public class BeanRequest {

    private String action;
    private String username;
    private String password;

    private String query;
    private List<String> partNumbers = new ArrayList<String>();
    private Long quantity;

    public BeanRequest() {
    }

    public BeanRequest(String xmlData) throws IOException, SAXException {
        Digester digester = new Digester();
        digester.setValidating(false);
        digester.push(this);
        digester.addCallMethod("CatalogRequest/Action", "setAction", 0);
        digester.addCallMethod("CatalogRequest/Username", "setUsername", 0);
        digester.addCallMethod("CatalogRequest/Password", "setPassword", 0);
        digester.addCallMethod("CatalogRequest/PartNumber", "setPartNumber", 0);
        digester.addCallMethod("CatalogRequest/Query", "setQuery", 0);
        digester.addCallMethod("CatalogRequest/Quantity", "setQuantity", 0);

        digester.parse(new StringReader(xmlData));
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public List<String> getPartNumbers() {
        return partNumbers;
    }

    public void setPartNumber(String aPartNumber) {
        if (StringUtils.isNotEmpty(aPartNumber)) {
            if (this.partNumbers == null) this.partNumbers = new ArrayList<String>();
            this.partNumbers.add(aPartNumber);
        }
    }

    public Long getQuantity() {
        return (quantity!=null && quantity>0) ? quantity : 1;
    }

    public void setQuantity(String quantity) {
        this.quantity = NumberUtils.toLong(quantity, 1);
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }
}
