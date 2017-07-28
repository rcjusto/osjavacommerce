package org.store.core.admin;

import org.store.core.beans.LocalizedText;
import org.store.core.beans.StaticText;
import org.store.core.beans.StaticTextLang;
import org.store.core.beans.StoreProperty;
import org.store.core.beans.utils.BreadCrumb;
import org.store.core.globals.StoreMessages;
import org.store.core.utils.merchants.MerchantService;
import org.store.core.utils.merchants.MerchantUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

import java.util.*;

public class merchantAction extends AdminModuleAction implements StoreMessages {

    @Override
    public void prepare() throws Exception {
    }

    public String list() throws Exception {
        List<Map> l = new ArrayList<Map>();
        MerchantUtils merchantUtils = new MerchantUtils(getServletContext());
        Map<String, Class> mapServices = merchantUtils.getMerchants();
        if (mapServices != null && !mapServices.isEmpty()) {
            for (String serviceName : mapServices.keySet()) {
                MerchantService ms = merchantUtils.getService(serviceName, this);
                Map map = new HashMap();
                map.put("name", ms.getCode());
                map.put("label", ms.getLabel());
                map.put("type", ms.getType());
                map.put("active", ms.isActive());
                l.add(map);
            }
        }

        Collections.sort(l, new Comparator<Map>() {
            @Override
            public int compare(Map m1, Map m2) {
                String n1 = m1.get("name").toString();
                String n2 = m2.get("name").toString();
                // -1 - less than, 1 - greater than, 0 - equal, all inversed for descending
                return n1.compareTo(n2);
            }
        });

        addToStack("listado", l);
        getBreadCrumbs().add(new BreadCrumb(null, getText("admin.merchant.list"), null, null));
        return SUCCESS;
    }

    public String edit() throws Exception {
        if (StringUtils.isNotEmpty(serviceName)) {
            MerchantUtils merchantUtils = new MerchantUtils(getServletContext());
            MerchantService s = merchantUtils.getService(serviceName, this);
            if (s != null) {
                addToStack("active", s.isActive());
                addToStack("salesComision", s.getSaleComision());
                addToStack("service", s);
                addToStack("label", dao.getLocalizedtext(s.getLabel()));
                addToStack("message", dao.getStaticText(s.getLabel(), StaticText.TYPE_BLOCK));
                String form = s.getPropertiesForm(this);
                if (StringUtils.isNotEmpty(form)) addToStack("form", form);
            }
        }
        getBreadCrumbs().add(new BreadCrumb(null, getText("admin.merchant.list"), url("listmerchant", "/admin"), null));
        getBreadCrumbs().add(new BreadCrumb(null, getText("admin.merchant.modify"), null, null));
        return SUCCESS;
    }

    public String save() throws Exception {
        if (StringUtils.isNotEmpty(serviceName)) {
            MerchantUtils merchantUtils = new MerchantUtils(getServletContext());
            MerchantService s = merchantUtils.getService(serviceName, this);

            if (servicePropName != null && servicePropName.length > 0) {
                for (int i = 0; i < servicePropName.length; i++) {
                    if (!StringUtils.isEmpty(servicePropName[i])) {
                        StoreProperty bean = dao.getStoreProperty(servicePropName[i], "MERCHANT_" + serviceName);
                        if (bean == null) {
                            bean = new StoreProperty();
                            bean.setCode(servicePropName[i]);
                        }
                        bean.setType("MERCHANT_" + serviceName);
                        bean.setValue(servicePropValue != null && servicePropValue.length > i ? servicePropValue[i] : "");
                        bean.setInventaryCode(getStoreCode());
                        dao.save(bean);
                    }
                }
            }
            if (label != null && ArrayUtils.isSameLength(label, getLanguages())) {
                LocalizedText text = dao.getLocalizedtext(s.getLabel());
                if (text == null) {
                    text = new LocalizedText();
                    text.setInventaryCode(getStoreCode());
                    text.setCode(s.getLabel());
                }
                for (int i = 0; i < getLanguages().length; i++)
                    text.addValue(getLanguages()[i], label[i]);
                dao.save(text);
            }
            if (message != null && ArrayUtils.isSameLength(message, getLanguages())) {
                StaticText text = dao.getStaticText(s.getLabel(), StaticText.TYPE_BLOCK);
                if (text == null) {
                    text = new StaticText();
                    text.setInventaryCode(getStoreCode());
                    text.setCode(s.getLabel());
                    text.setTextType(StaticText.TYPE_BLOCK);
                }
                dao.save(text);
                for (int i = 0; i < getLanguages().length; i++) {
                    StaticTextLang stl = text.getLanguage(getLanguages()[i]);
                    if (stl == null) {
                        stl = new StaticTextLang();
                        stl.setStaticLang(getLanguages()[i]);
                        stl.setStaticText(text);
                    }
                    stl.setValue(message[i]);
                    dao.save(stl);
                }
            }

            s.savePropertiesForm(this);

        }


        return SUCCESS;
    }

    private String serviceName;
    private String[] servicePropName;
    private String[] servicePropValue;
    private String staticId;
    private String[] label;
    private String[] message;

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String[] getServicePropName() {
        return servicePropName;
    }

    public void setServicePropName(String[] servicePropName) {
        this.servicePropName = servicePropName;
    }

    public String[] getServicePropValue() {
        return servicePropValue;
    }

    public void setServicePropValue(String[] servicePropValue) {
        this.servicePropValue = servicePropValue;
    }

    public String getStaticId() {
        return staticId;
    }

    public void setStaticId(String staticId) {
        this.staticId = staticId;
    }

    public String[] getLabel() {
        return label;
    }

    public void setLabel(String[] label) {
        this.label = label;
    }

    public String[] getMessage() {
        return message;
    }

    public void setMessage(String[] message) {
        this.message = message;
    }
}
