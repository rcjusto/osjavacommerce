package org.store.core.globals;

import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class LinkUrl {

    private String resource;
    private List<LinkUrlParam> parameters;

    public LinkUrl(String url) {
        if (StringUtils.isNotEmpty(url)) {
            String[] urlParts = url.split("[?]");
            if (urlParts.length>0) {
                this.resource = urlParts[0];
                if (urlParts.length>1) {
                    String[] arrParam = urlParts[1].split("[&]");
                    if (arrParam!=null && arrParam.length>0) {
                        parameters = new ArrayList<LinkUrlParam>();
                        for(String param : arrParam) {
                            String[] arr = param.split("[=]");
                            if (arr!=null && arr.length>0) {
                                parameters.add(new LinkUrlParam(arr[0], arr.length > 1 ? arr[1] : ""));
                            }
                        }
                    }
                }
            }
        }
    }

    public void addParameter(String key, String value) {
        if (parameters==null) parameters = new ArrayList<LinkUrlParam>();
        parameters.add(new LinkUrlParam(key, value));
    }

    public void delParameters(String key) {
        if (StringUtils.isNotEmpty(key) && parameters!=null) {
            for(int i=parameters.size()-1; i>=0; i--) {
                LinkUrlParam p = parameters.get(i);
                if (key.equalsIgnoreCase(p.getName())) parameters.remove(p);
            }
        }
    }

    public String toString() {
        StringBuilder buff = new StringBuilder();
        if (StringUtils.isNotEmpty(resource)) buff.append(resource);
        if (parameters!=null && !parameters.isEmpty()) {
            buff.append("?");
            boolean first = true;
            for(LinkUrlParam param : parameters) {
                if (first) first = false; else buff.append("&");
                buff.append(param.toString());
            }
        }
        return buff.toString();
    }

    public class LinkUrlParam {
        private String name;
        private String value;

        public LinkUrlParam(String name, String value) {
            this.name = name;
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            StringBuilder buff = new StringBuilder();
            if (StringUtils.isNotEmpty(name)) {
                buff.append(name).append("=");
                if (StringUtils.isNotEmpty(value)) buff.append(value);
            }
            return buff.toString();
        }
    }

}
