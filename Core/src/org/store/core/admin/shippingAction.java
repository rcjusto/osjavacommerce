package org.store.core.admin;

import org.store.core.beans.ShippingMethod;
import org.store.core.beans.utils.BreadCrumb;
import org.store.core.globals.StoreMessages;
import org.store.core.utils.carriers.CarrierMethod;
import org.store.core.utils.carriers.CarrierService;
import org.store.core.utils.carriers.CarrierUtils;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class shippingAction extends AdminModuleAction implements StoreMessages {

    @Override
    public void prepare() throws Exception {
    }

    public String list() throws Exception {
        // Salvar
        if (shippingMethodId != null && shippingMethodId.length > 0) {
            for (int i = 0; i < shippingMethodId.length; i++) {
                ShippingMethod bean = (ShippingMethod) dao.get(ShippingMethod.class, shippingMethodId[i]);
                if (bean != null) {
                    Boolean activeMethod = (shippingMethodActive != null && shippingMethodActive.length > i) ? "Y".equalsIgnoreCase(shippingMethodActive[i]) : null;
                    Boolean defaultMethod = (shippingMethodDefault != null && shippingMethodDefault.length > i) ? "Y".equalsIgnoreCase(shippingMethodDefault[i]) : null;
                    if (activeMethod != null) bean.setActive(activeMethod);
                    if (defaultMethod != null) bean.setDefaultMethod(defaultMethod);
                    bean.setInventaryCode(getStoreCode());
                    dao.save(bean);
                }

            }
        }

        // Listar
        List<ShippingMethod> methods = dao.getShippingMethodList();
        addToStack("shippingMethods", methods);

        // Buscar totos los servicios configurados
        CarrierUtils carrierUtils = new CarrierUtils(getServletContext());
        if (carrierUtils.getCarriers() != null && !carrierUtils.getCarriers().isEmpty()) {
            List<CarrierService> listado = new ArrayList<CarrierService>();
            for (String carrier : carrierUtils.getCarriers().keySet()) {
                CarrierService service = carrierUtils.getCarrierService(carrier, null);
                if (service != null) listado.add(service);
            }
            addToStack("services", listado);
        }
        getBreadCrumbs().add(new BreadCrumb(null, getText("admin.shipping.method.list"), null, null));
        return SUCCESS;
    }

    public String del() throws Exception {
        ShippingMethod method = (ShippingMethod) dao.get(ShippingMethod.class, idMethod);
        if (method != null) {
            String res = dao.isUsedShippingMethod(method);
            if (StringUtils.isNotEmpty(res)) addSessionError(getText(CNT_ERROR_CANNOT_DELETE_SHIPPING_METHOD, CNT_DEFAULT_ERROR_CANNOT_DELETE_SHIPPING_METHOD, new String[]{method.getMethodName(getDefaultLanguage()), res}));
            else {
                dao.delete(method);
                dao.flushSession();
            }
        }
        return SUCCESS;
    }

    public String save() throws Exception {
        if (StringUtils.isNotEmpty(shippingNew)) {
            String[] arr = shippingNew.split("[|][|]");
            if (arr != null && arr.length == 2) {
                String carrier = arr[0];
                String code = arr[1];
                CarrierUtils carrierUtils = new CarrierUtils(getServletContext());
                CarrierService service = carrierUtils.getCarrierService(arr[0], null);
                if (StringUtils.isNotEmpty(code) && service != null) {
                    List<CarrierMethod> methods = service.getShippingMethods();
                    if (methods != null && !methods.isEmpty()) {
                        for (CarrierMethod method : methods) {
                            if (code.equals(method.getCode())) {
                                ShippingMethod shippingMethod = dao.getShippingMethod(carrier, code);
                                if (shippingMethod == null) {
                                    shippingMethod = new ShippingMethod();
                                    shippingMethod.setActive(true);
                                    shippingMethod.setCarrierName(carrier);
                                    shippingMethod.setInventaryCode(getStoreCode());
                                    shippingMethod.setMethodCode(code);
                                    for (int i = 0; i < getLanguages().length; i++) {
                                        if (methodName != null && methodName.length > i && StringUtils.isNotEmpty(methodName[i]))
                                            shippingMethod.setMethodName(getLanguages()[i], methodName[i]);
                                        else shippingMethod.setMethodName(getLanguages()[i], method.getName());
                                    }
                                    dao.save(shippingMethod);
                                } else {
                                    for (int i = 0; i < getLanguages().length; i++) {
                                        if (methodName != null && methodName.length > i && StringUtils.isNotEmpty(methodName[i]))
                                            shippingMethod.setMethodName(getLanguages()[i], methodName[i]);
                                    }
                                    dao.save(shippingMethod);
                                }
                                break;
                            }
                        }
                    }
                }
            }
        }
        return SUCCESS;
    }

    private String shippingNew;
    private Long idMethod;
    private String[] methodName;
    private Long[] shippingMethodId;
    private String[] shippingMethodActive;
    private String[] shippingMethodDefault;

    public String getShippingNew() {
        return shippingNew;
    }

    public void setShippingNew(String shippingNew) {
        this.shippingNew = shippingNew;
    }

    public Long getIdMethod() {
        return idMethod;
    }

    public void setIdMethod(Long idMethod) {
        this.idMethod = idMethod;
    }

    public String[] getMethodName() {
        return methodName;
    }

    public void setMethodName(String[] methodName) {
        this.methodName = methodName;
    }

    public Long[] getShippingMethodId() {
        return shippingMethodId;
    }

    public void setShippingMethodId(Long[] shippingMethodId) {
        this.shippingMethodId = shippingMethodId;
    }

    public String[] getShippingMethodActive() {
        return shippingMethodActive;
    }

    public void setShippingMethodActive(String[] shippingMethodActive) {
        this.shippingMethodActive = shippingMethodActive;
    }

    public String[] getShippingMethodDefault() {
        return shippingMethodDefault;
    }

    public void setShippingMethodDefault(String[] shippingMethodDefault) {
        this.shippingMethodDefault = shippingMethodDefault;
    }
}
