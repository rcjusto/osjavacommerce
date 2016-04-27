package org.store.core.globals;

import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class StoreHtmlField {

    public static final String TYPE_SELECT = "select";
    public static final String TYPE_INPUT = "input";
    public static final String TYPE_RADIO = "radio";
    public static final String TYPE_CHECK = "checked";

    private String type;
    private String id;
    private String label;
    private String name;
    private String value;
    private String classes;
    private Boolean required;
    private List<StoreHtmlFieldOption> options;

    public StoreHtmlField(String type) {
        this.type = type;
    }

    public StoreHtmlField(String type, String name) {
        this.type = type;
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public StoreHtmlField setType(String type) {
        this.type = type;
        return this;
    }

    public String getId() {
        return id;
    }

    public StoreHtmlField setId(String id) {
        this.id = id;
        return this;
    }

    public String getLabel() {
        return label;
    }

    public StoreHtmlField setLabel(String label) {
        this.label = label;
        return this;
    }

    public String getName() {
        return name;
    }

    public StoreHtmlField setName(String name) {
        this.name = name;
        return this;
    }

    public String getValue() {
        return value;
    }

    public StoreHtmlField setValue(String value) {
        this.value = value;
        return this;
    }

    public String getClasses() {
        if (getRequired()) {
            if (StringUtils.isEmpty(classes)) classes = "required";
            else if (classes.indexOf("required")<0) classes += " required";
        }
        return classes;
    }

    public StoreHtmlField setClasses(String classes) {
        this.classes = classes;
        return this;
    }

    public StoreHtmlField addClasses(String classes) {
        if (StringUtils.isNotEmpty(this.classes)) this.classes += " " +classes;
        else this.classes = classes;
        return this;
    }

    public Boolean getRequired() {
        return required!=null && required;
    }

    public StoreHtmlField setRequired(Boolean required) {
        this.required = required;
        return this;
    }

    public List<StoreHtmlFieldOption> getOptions() {
        return options;
    }

    public StoreHtmlField setOptions(List<StoreHtmlFieldOption> options) {
        this.options = options;
        return this;
    }

    public StoreHtmlField addOption(String value, String text) {
        if (options==null) options = new ArrayList<StoreHtmlFieldOption>();
        options.add(new StoreHtmlFieldOption(value, text));
        return this;
    }

    public StoreHtmlField addOptions(List<String> list) {
        if (options==null) options = new ArrayList<StoreHtmlFieldOption>();
        for (String s : list) options.add(new StoreHtmlFieldOption(s, s));
        return this;
    }

    @Override
    public String toString() {
        StringBuffer buff = new StringBuffer();
        buff.append("<div>");
        addLabel(buff);
        addField(buff);
        buff.append("</div>");
        return buff.toString();
    }

    public String getTableRow() {
        StringBuffer buff = new StringBuffer();
        buff.append("<tr><th>");
        addLabel(buff);
        buff.append("</th><td>");
        addField(buff);
        buff.append("</td></tr>");
        return buff.toString();
    }

    private void addField(StringBuffer buff) {
        if (TYPE_SELECT.equalsIgnoreCase(getType())) {
            buff.append("<select");
            addAttrId(buff);
            addAttrName(buff);
            addAttrClass(buff);
            buff.append(">");
            for(StoreHtmlFieldOption o : getOptions()) {
                buff.append("<option value=\"").append(o.getValue()).append("\"");
                if (o.getValue().equalsIgnoreCase(getValue())) buff.append(" selected=\"selected\"");
                buff.append(">");
                buff.append(o.getText()).append("</option>");
            }
            buff.append("</select><span class=\"field_error\"></span>");
        } else if (TYPE_INPUT.equalsIgnoreCase(getType())) {
            buff.append("<input type=\"text\"");
            addAttrId(buff);
            addAttrName(buff);
            addAttrValue(buff);
            addAttrClass(buff);
            buff.append("/><span class=\"field_error\"></span>");
        } else if (TYPE_CHECK.equalsIgnoreCase(getType())) {
            for(StoreHtmlFieldOption o : getOptions()) {
                buff.append("<label><input type=\"checkbox\"");
                addAttrName(buff);
                if (StringUtils.isNotEmpty(o.getValue())) {
                    buff.append(" value=\"").append(o.getValue()).append("\"");
                }
                if (o.getValue().equalsIgnoreCase(getValue())) {
                    buff.append(" checked=\"checked\"");
                }
                buff.append(">");
                buff.append(o.getText()).append("</label>");
            }
        } else if (TYPE_RADIO.equalsIgnoreCase(getType())) {
            for(StoreHtmlFieldOption o : getOptions()) {
                buff.append("<label><input type=\"radio\"");
                addAttrName(buff);
                if (StringUtils.isNotEmpty(o.getValue())) {
                    buff.append(" value=\"").append(o.getValue()).append("\"");
                }
                if (o.getValue().equalsIgnoreCase(getValue())) {
                    buff.append(" checked=\"checked\"");
                }
                buff.append(">");
                buff.append(o.getText()).append("</label>");
            }
        }
    }

    private void addAttrName(StringBuffer buff) {
        if (StringUtils.isNotEmpty(getName())) {
            buff.append(" name=\"").append(getName()).append("\"");
        }
    }

    private void addAttrId(StringBuffer buff) {
        if (StringUtils.isNotEmpty(getId())) {
            buff.append(" id=\"").append(getId()).append("\"");
        }
    }

    private void addAttrClass(StringBuffer buff) {
        if (StringUtils.isNotEmpty(getClasses())) {
            buff.append(" class=\"").append(getClasses()).append("\"");
        }
    }

    private void addAttrValue(StringBuffer buff) {
        buff.append(" value=\"");
        if (StringUtils.isNotEmpty(getValue())) buff.append(getValue());
        buff.append("\"");
    }

    private void addLabel(StringBuffer buff) {
        buff.append("<span");
        if (getRequired()) buff.append(" class=\"label required\""); else buff.append(" class=\"label\"");
        buff.append(">");
        buff.append(getLabel()).append("</span>");
    }

    public class StoreHtmlFieldOption {
        private String value;
        private String text;

        public StoreHtmlFieldOption(String value, String text) {
            this.value = value;
            this.text = text;
        }

        public String getValue() {
            return value!=null ? value : "";
        }

        public void setValue(String value) {
            this.value = value;
        }

        public String getText() {
            return text!=null ? text : "";
        }

        public void setText(String text) {
            this.text = text;
        }
    }

}
