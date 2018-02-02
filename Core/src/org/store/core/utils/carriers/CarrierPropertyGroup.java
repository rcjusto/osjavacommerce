package org.store.core.utils.carriers;

import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class CarrierPropertyGroup {

    private String groupName;
    private List<CarrierProperty> propertyNames;

    public CarrierPropertyGroup(String groupName) {
        this.groupName = groupName;
        this.propertyNames = new ArrayList<CarrierProperty>();
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public List<CarrierProperty> getPropertyNames() {
        return propertyNames;
    }

    public CarrierPropertyGroup addProperty(String name) {
        return addProperty(name, (String[]) null);
    }
    public CarrierPropertyGroup addProperty(String name, String[] values) {
        CarrierProperty prop = new CarrierProperty();
        prop.setName(name);
        prop.setOptions(values);
        propertyNames.add(prop);
        return this;
    }

    public CarrierPropertyGroup addProperty(String name, String values) {
        String[] arr = (StringUtils.isNotEmpty(values)) ? values.split(",") : null;
        return addProperty(name, arr);
    }

    public class CarrierProperty {
        private String name;
        private String[] options;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String[] getOptions() {
            return options;
        }

        public void setOptions(String[] options) {
            this.options = options;
        }
    }

}
