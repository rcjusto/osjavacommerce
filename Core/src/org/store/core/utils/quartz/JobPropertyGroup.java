package org.store.core.utils.quartz;

import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class JobPropertyGroup {

    private String groupName;
    private List<JobProperty> propertyNames;

    public JobPropertyGroup(String groupName) {
        this.groupName = groupName;
        this.propertyNames = new ArrayList<JobProperty>();
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public List<JobProperty> getPropertyNames() {
        return propertyNames;
    }

    public JobPropertyGroup addProperty(String name) {
        return addProperty(name, (String[]) null, "");
    }

    public JobPropertyGroup addProperty(String name, String[] values, String dv) {
        JobProperty prop = new JobProperty();
        prop.setName(name);
        prop.setOptions(values);
        prop.setDefaultValue(dv);
        propertyNames.add(prop);
        return this;
    }

    public JobPropertyGroup addProperty(String name, String values, String dv) {
        String[] arr = (StringUtils.isNotEmpty(values)) ? values.split(",") : null;
        return addProperty(name, arr, dv);
    }

    public class JobProperty {
        private String name;
        private String defaultValue;
        private String[] options;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDefaultValue() {
            return (defaultValue!=null) ? defaultValue : "";
        }

        public void setDefaultValue(String defaultValue) {
            this.defaultValue = defaultValue;
        }

        public String[] getOptions() {
            return options;
        }

        public void setOptions(String[] options) {
            this.options = options;
        }
    }

}
