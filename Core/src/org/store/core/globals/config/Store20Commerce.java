package org.store.core.globals.config;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * Rogelio Caballero
 * 17/06/11 16:09
 */
public class Store20Commerce {
    public Store20Commerce() {
    }

    public Store20Commerce(String id) {
        this.id = id;
    }

    private String id;
    private String name;
    private String database;
    private List<StoreUrl> urls;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public List<StoreUrl> getUrls() {
        if (urls==null) urls = new ArrayList<StoreUrl>();
        return urls;
    }

    public void setUrls(List<StoreUrl> urls) {
        this.urls = urls;
    }

    public String getFullUrl(int index) {
        return (urls!=null && urls.size()>index ) ? "http://" + urls.get(index).normalizeUrl() : null;
    }

    public String getFullHost(int index) {
        return (urls!=null && urls.size()>index ) ? "http://" + urls.get(index).getDomain() : null;
    }

    public void addUrl(String domain, String path) {
        if (StringUtils.isNotEmpty(domain)) {
            if (urls==null) urls = new ArrayList<StoreUrl>();
            urls.add(new StoreUrl(domain, path));
        }
    }

    public boolean hasUrl(String domain, String path) {
        if (urls!=null && !urls.isEmpty()) {
            for(StoreUrl url : urls)
                if (url.equals(domain, path)) return true;
        }
        return false;
    }


    public class StoreUrl {
        private String domain;
        private String path;

        public StoreUrl(String domain, String path) {
            this.domain = domain;
            this.path = path;
        }

        public String getDomain() {
            return domain;
        }

        public void setDomain(String domain) {
            this.domain = domain;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public String normalizeUrl() {
            if (StringUtils.isNotEmpty(domain)) {
                return domain + "/" + (StringUtils.isNotEmpty(path) ? path + "/" : "");
            }
            return "";
        }

        public String normalizeUrl(String port) {
            if (StringUtils.isNotEmpty(domain)) {
                return domain + (StringUtils.isNotEmpty(port) ? ":" + port : "")  + "/" + (StringUtils.isNotEmpty(path) ? path + "/" : "");
            }
            return "";
        }

        public boolean equals(String domain, String path) {
            return new EqualsBuilder().append(domain, this.domain).append(path, this.path).isEquals();
        }
    }

}
