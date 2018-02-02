package org.store.core.utils.reports;

import com.opensymphony.xwork2.ognl.OgnlUtil;
import org.store.core.globals.BaseAction;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ReportFilterField {

    public static Logger log = Logger.getLogger(ReportFilterField.class);
    public static final String TYPE_TEXT = "text";
    public static final String TYPE_SELECT = "select";
    public static final String TYPE_RADIO = "radio";
    public static final String TYPE_CHECKBOX = "checkbox";
    public static final String TYPE_CHECKBOXLIST = "checkboxlist";
    public static final String TYPE_RANGE = "range";

    private String type;
    private String name;
    private String name2;
    private String value;
    private String[] values;
    private String value2;
    private String cssClass;
    private String label;
    private String label2;
    private List<FieldOption> options;
    private BaseAction action;

    public ReportFilterField(String type) {
        this.type = type;
    }

    public ReportFilterField(String type, String name) {
        this.type = type;
        this.name = name;
    }

    public ReportFilterField(String type, String name, String value) {
        this.type = type;
        this.name = name;
        this.value = value;
    }

    public ReportFilterField(String type, String name, String value, String label) {
        this.type = type;
        this.name = name;
        this.value = value;
        this.label = label;
    }

    public String getType() {
        return type;
    }

    public ReportFilterField setType(String type) {
        this.type = type;
        return this;
    }

    public String getName() {
        return name;
    }

    public ReportFilterField setName(String name) {
        this.name = name;
        return this;
    }

    public String getValue() {
        return value;
    }

    public ReportFilterField setValue(String value) {
        this.value = value;
        return this;
    }

    public String getCssClass() {
        return cssClass;
    }

    public ReportFilterField setCssClass(String cssClass) {
        this.cssClass = cssClass;
        return this;
    }

    public String getLabel() {
        return label;
    }

    public ReportFilterField setLabel(String label) {
        this.label = label;
        return this;
    }

    public String getLabel2() {
        return label2;
    }

    public ReportFilterField setLabel2(String label2) {
        this.label2 = label2;
        return this;
    }

    public String getName2() {
        return name2;
    }

    public ReportFilterField setName2(String name2) {
        this.name2 = name2;
        return this;
    }

    public String getValue2() {
        return value2;
    }

    public ReportFilterField setValue2(String value2) {
        this.value2 = value2;
        return this;
    }

    public BaseAction getAction() {
        return action;
    }

    public ReportFilterField setAction(BaseAction action) {
        this.action = action;
        return this;
    }

    public List<FieldOption> getOptions() {
        return options;
    }

    public ReportFilterField setOptions(List<FieldOption> options) {
        this.options = options;
        return this;
    }

    public ReportFilterField addOption(String key, String value) {
        if (options == null) options = new ArrayList<FieldOption>();
        options.add(new FieldOption(key, value));
        return this;
    }

    public ReportFilterField addOptionsFromBeanList(List list, String propertyKey, String propertyValue) {
        if (options == null) options = new ArrayList<FieldOption>();
        try {
            OgnlUtil ognl = new OgnlUtil();
            Map<String,Object> map = new HashMap<String,Object>();
            if (action!=null) map.put("action",action);
            if (list != null)
                for (Object o : list) {
                    Object key =  ognl.getValue(propertyKey, map, o); if (key==null) key = "";
                    Object val = ognl.getValue(propertyValue, map, o); if (val==null) val = "";
                    options.add(new FieldOption(key.toString(), val.toString()));
                }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return this;
    }

    public ReportFilterField addOptionsFromMapList(List<Map<String,Object>> list, String propertyKey, String propertyValue) {
        if (options == null) options = new ArrayList<FieldOption>();
        try {
            if (list != null)
                for (Map<String,Object> o : list) {
                    Object key = o.get(propertyKey); if (key==null) key = "";
                    Object val = o.get(propertyValue); if (val==null) val = "";
                    options.add(new FieldOption(key.toString(), val.toString()));
                }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return this;
    }

    public ReportFilterField addOptionsFromMap(Map<String, String> map) {
        if (options == null) options = new ArrayList<FieldOption>();
        try {
            if (map != null)
                for (Map.Entry<String, String> o : map.entrySet()) {
                    options.add(new FieldOption(o.getKey(), o.getValue()));
                }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return this;
    }

    public String toString() {
        StringBuffer buffer = new StringBuffer();
        if (TYPE_SELECT.equalsIgnoreCase(getType())) {
            buffer.append("<p class=\"clearfix\"><label>");
            buffer.append("<span class=\"label\">").append(label).append("</span>");
            buffer.append("<select name=\"").append(name).append("\" class=\"").append(cssClass).append("\" >");
            if (options != null)
                for (FieldOption e : options) {
                    buffer.append("<option value=\"").append(e.getKey()).append("\" ");
                    if (value != null && value.equalsIgnoreCase(e.getKey())) buffer.append("selected=\"selected\"");
                    buffer.append(">").append(e.getValue()).append("</option>");
                }
            buffer.append("</select>");
            buffer.append("</label></p>");
        } else if (TYPE_RANGE.equalsIgnoreCase(getType())) {
            buffer.append("<p class=\"clearfix\">");
            buffer.append("<label><span class=\"label\">").append(label).append("</span></label>");
            buffer.append("<input type=\"text\" name=\"").append(name).append("\" value=\"").append(value).append("\" class=\"").append(cssClass).append("\" />");
            buffer.append("<span class=\"label2\">").append(label2).append("</span>");
            buffer.append("<input type=\"text\" name=\"").append(name2).append("\" value=\"").append(value2).append("\" class=\"").append(cssClass).append("\" />");
            buffer.append("</p>");
        } else if (TYPE_TEXT.equalsIgnoreCase(getType())) {
            buffer.append("<p class=\"clearfix\"><label>");
            buffer.append("<span class=\"label\">").append(label).append("</span>");
            buffer.append("<input type=\"text\" name=\"").append(name).append("\" value=\"").append(value).append("\" class=\"").append(cssClass).append("\" />");
            buffer.append("</label></p>");
        } else if (TYPE_RADIO.equalsIgnoreCase(getType())) {
            buffer.append("<p class=\"clearfix\">");
            buffer.append("<span class=\"label\">").append(label).append("</span>");
            if (options != null)
                for (FieldOption e : options) {
                    buffer.append("<label>");
                    buffer.append("<input type=\"radio\" name=\"").append(name).append("\" value=\"").append(e.getKey()).append("\" class=\"").append(cssClass).append("\" ");
                    if (value != null && value.equalsIgnoreCase(e.getKey())) buffer.append("checked=\"checked\"");
                    buffer.append("/>");
                    buffer.append("<span>").append(e.getValue()).append("</span>");
                    buffer.append("</label>");
                }
            buffer.append("</p>");
        } else if (TYPE_CHECKBOX.equalsIgnoreCase(getType())) {
            buffer.append("<p class=\"clearfix\">");
            if (StringUtils.isNotEmpty(label)) buffer.append("<span class=\"label\">").append(label).append("</span>");
            buffer.append("<label>");
            buffer.append("<input type=\"checkbox\" name=\"").append(name).append("\" value=\"").append(value2).append("\" class=\"").append(cssClass).append("\" ");
            if (StringUtils.isEmpty(value2)) value2 = "on";
            if (value != null && value.equalsIgnoreCase(value2)) buffer.append("checked=\"checked\"");
            buffer.append("/>");
            buffer.append("<span>").append(label2).append("</span>");
            buffer.append("</label>");
            buffer.append("</p>");
        } else if (TYPE_CHECKBOXLIST.equalsIgnoreCase(getType())) {
            buffer.append("<p class=\"clearfix\">");
            if (StringUtils.isNotEmpty(label)) buffer.append("<span class=\"label\">").append(label).append("</span>");
            for (FieldOption e : options) {
                buffer.append("<label>");
                buffer.append("<input type=\"checkbox\" name=\"").append(name).append("\" value=\"").append(e.getKey()).append("\" class=\"").append(cssClass).append("\" ");
                if (values != null && ArrayUtils.indexOf(values, e.getKey()) > -1) buffer.append("checked=\"checked\"");
                buffer.append("/>");
                buffer.append("<span>").append(e.getValue()).append("</span>");
                buffer.append("</label>");
            }
            buffer.append("</p>");
        }
        return buffer.toString();
    }

    public class FieldOption {
        private String key;
        private String value;

        public FieldOption(String key, String value) {
            this.key = key;
            this.value = value;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }

}
