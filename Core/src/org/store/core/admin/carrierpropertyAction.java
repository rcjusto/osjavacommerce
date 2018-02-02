package org.store.core.admin;

import org.store.core.beans.StoreProperty;
import org.store.core.beans.utils.BreadCrumb;
import org.store.core.globals.StoreMessages;
import org.store.core.utils.carriers.CarrierUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Namespace(value = "/admin")
@ParentPackage(value = "store-admin")
public class carrierpropertyAction extends AdminModuleAction implements StoreMessages {

    @Override
    public void prepare() throws Exception {
    }

    public String list() throws Exception {
        CarrierUtils carrierUtils = new CarrierUtils(getServletContext());
        addToStack("carriers", carrierUtils.getCarriers());
        if (StringUtils.isNotEmpty(carrier)) {
            List<StoreProperty> l = dao.getStoreProperties("CARRIER_" + carrier);
            Map<String, StoreProperty> storeProperties = new HashMap<String, StoreProperty>();
            for (StoreProperty bean : l) storeProperties.put(bean.getCode(), bean);
            addToStack("storeProperties", storeProperties);
            addToStack("carrierProperties", carrierUtils.getCarrierService(carrier, null).getPropertyNames());
        }
        getBreadCrumbs().add(new BreadCrumb(null, getText("admin.configure.live.carriers"), null, null));
        return SUCCESS;
    }

    @Action(value = "savecarrierproperty", results = @Result(type = "redirectAction", location = "listcarrierproperty?carrier=${carrier}"))
    public String save() throws Exception {
        if (storeProperties_id != null && storeProperties_id.length > 0) {
            for (int i = 0; i < storeProperties_id.length; i++) {
                if (!StringUtils.isEmpty(storeProperties_id[i])) {
                    StoreProperty bean = dao.getStoreProperty(storeProperties_id[i], "CARRIER_" + carrier);
                    if (bean == null) {
                        bean = new StoreProperty();
                        bean.setCode(storeProperties_id[i]);
                    }
                    bean.setType("CARRIER_" + carrier);
                    bean.setValue(storeProperties_value[i]);
                    bean.setInventaryCode(getStoreCode());
                    dao.save(bean);
                }
            }
        }
        return SUCCESS;
    }

    private String carrier;
    private String[] storeProperties_id;
    private String[] storeProperties_value;

    public String getCarrier() {
        return carrier;
    }

    public void setCarrier(String carrier) {
        this.carrier = carrier;
    }

    public String[] getStoreProperties_id() {
        return storeProperties_id;
    }

    public void setStoreProperties_id(String[] storeProperties_id) {
        this.storeProperties_id = storeProperties_id;
    }

    public String[] getStoreProperties_value() {
        return storeProperties_value;
    }

    public void setStoreProperties_value(String[] storeProperties_value) {
        this.storeProperties_value = storeProperties_value;
    }
}
