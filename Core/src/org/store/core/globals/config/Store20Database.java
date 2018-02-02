package org.store.core.globals.config;

import org.apache.commons.lang.StringUtils;

import java.util.List;
import java.util.Properties;

/**
 * Rogelio Caballero
 * 17/06/11 16:09
 */
public class Store20Database {

    private String id;
    private Properties properties;
    private String type;
    private String url;
    private String user;
    private String password;
    private Store20Config store20Config;
    private static final String TYPE_MYSQL = "MySQL";
    private static final String TYPE_MSSQL = "SQLServer";

    public Store20Database() {
    }

    public Store20Database(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Store20Config getStore20Config() {
        return store20Config;
    }

    public List<Class> getExtraBeans() {
        return (store20Config!=null && store20Config.getExtraBeans()!=null) ? store20Config.getExtraBeans() : null;
    }

    public void setStore20Config(Store20Config store20Config) {
        this.store20Config = store20Config;
    }

    public Properties getProperties() {
        return properties;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    public void addProperty(String name, String value) {
        if (properties==null) properties = new Properties();
        properties.setProperty(name, value);
    }

    public Properties getConnectionProperties() {
        Properties result = new Properties();
        if (TYPE_MYSQL.equalsIgnoreCase(type)) {
            result.setProperty("hibernate.connection.driver_class","com.mysql.jdbc.Driver");
            result.setProperty("hibernate.dialect","org.store.core.globals.MySQL503Dialect");
        } else if (TYPE_MSSQL.equalsIgnoreCase(type)) {
            result.setProperty("hibernate.connection.driver_class","com.microsoft.sqlserver.jdbc.SQLServerDriver");
            result.setProperty("hibernate.dialect","org.hibernate.dialect.SQLServerDialect");
        }
        if (StringUtils.isNotEmpty(url)) result.setProperty("hibernate.connection.url", url);
        if (StringUtils.isNotEmpty(user)) result.setProperty("hibernate.connection.username", user);
        if (StringUtils.isNotEmpty(password)) result.setProperty("hibernate.connection.password", password);
        return  result;
    }

}
