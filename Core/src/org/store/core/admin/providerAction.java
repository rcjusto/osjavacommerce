package org.store.core.admin;

import org.store.core.beans.Payterms;
import org.store.core.beans.Provider;
import org.store.core.beans.State;
import org.store.core.beans.utils.BreadCrumb;
import org.store.core.beans.utils.DataNavigator;
import org.store.core.globals.CountryFactory;
import org.store.core.globals.config.Store20Config;
import org.store.core.globals.StoreMessages;
import org.store.core.utils.suppliers.SupplierService;
import org.store.core.utils.suppliers.SupplierUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;

import java.util.List;
import java.util.Map;
import java.util.Properties;

@Namespace(value = "/admin")
@ParentPackage(value = "store-admin")
public class providerAction extends AdminModuleAction implements StoreMessages {

    @Override
    public void prepare() throws Exception {
        provider = (Provider) dao.get(Provider.class, idProvider);
        payterm = (Payterms) dao.get(Payterms.class, idPayterm);
    }

    public String list() throws Exception {
        if (selecteds != null && selecteds.length > 0) {
            for (Long id : selecteds) {
                Provider bean = (Provider) dao.get(Provider.class, id);
                if (bean != null) {
                    String res = dao.isUsedProvider(bean);
                    if (StringUtils.isNotEmpty(res)) {
                        addActionError(getText(CNT_ERROR_CANNOT_DELETE_PROVIDER, CNT_DEFAULT_ERROR_CANNOT_DELETE_PROVIDER, new String[]{bean.getProviderName(), res}));
                    } else {
                        dao.deleteProvider(bean);
                    }
                }
            }
            dao.flushSession();
        }

        DataNavigator providers = new DataNavigator(getRequest(), "providers");
        providers.setListado(dao.getProviders(providers));
        addToStack("providers", providers);
        getBreadCrumbs().add(new BreadCrumb(null, getText("admin.provider.list"), null, null));
        return SUCCESS;
    }

    public String edit() throws Exception {
        countries = CountryFactory.getCountries(getLocale());
        addToStack("payterms", dao.getPayterms());
        getBreadCrumbs().add(new BreadCrumb(null, getText("admin.provider.list"), url("listprovider","/admin"), null));
        getBreadCrumbs().add(new BreadCrumb(null, getText(provider != null ? "admin.provider.modify" : "admin.provider.new"), null, null));
        return SUCCESS;
    }

    @Action(value = "providergetproperties", results = @Result(type = "velocity", location = "/WEB-INF/views/admin/providerproperties.vm"))
    public String getProperties() throws Exception {
        if (StringUtils.isNotEmpty(serviceName)) {
            Map<String, Class> map;
            synchronized (getServletContext()) {
                map = Store20Config.getInstance(getServletContext()).getMapSuplier();
            }
            SupplierUtils su = new SupplierUtils(map, dao.gethSession(), getStoreCode(), getDatabaseConfig());
            SupplierService service = su.getService(serviceName, new Properties());
            if (service != null) addToStack("propertyNames", service.getConfigurationParameters());
        }
        return SUCCESS;
    }

    public String save() throws Exception {
        if (provider != null) {
            State state = (idState != null) ? (State) dao.get(State.class, idState) : null;
            if (state == null && !StringUtils.isEmpty(newState)) {
                state = new State();
                state.setCountryCode(provider.getCountryCode());
                state.setStateCode(StringUtils.left(newState, 5));
                state.setStateName(newState);
                dao.save(state);
            }
            provider.setPayterms(payterm);
            provider.setState(state);
            if (servicePropName != null && servicePropName.length > 0) {
                for (int i = 0; i < servicePropName.length; i++) {
                    if (StringUtils.isNotEmpty(servicePropName[i]))
                        provider.setServiceProperty(servicePropName[i], (servicePropValue != null && servicePropValue.length > i) ? servicePropValue[i] : null);
                }
            }
            provider.setInventaryCode(getStoreCode());
            dao.save(provider);
        }
        return SUCCESS;
    }

    public Object[] getProviderServices() {
        Map<String, Class> map = Store20Config.getInstance(getServletContext()).getMapSuplier();
        return (map != null && !map.isEmpty()) ? map.keySet().toArray() : null;
    }

    List countries;
    private Provider provider;
    private Long idProvider;
    private Long idState;
    private String newState;
    private String serviceName;
    private String[] servicePropName;
    private String[] servicePropValue;
    private Payterms payterm;
    private Long idPayterm;

    public List getCountries() {
        return countries;
    }

    public void setCountries(List countries) {
        this.countries = countries;
    }

    public Provider getProvider() {
        return provider;
    }

    public void setProvider(Provider provider) {
        this.provider = provider;
    }

    public Long getIdProvider() {
        return idProvider;
    }

    public void setIdProvider(Long idProvider) {
        this.idProvider = idProvider;
    }

    public Long getIdState() {
        return idState;
    }

    public void setIdState(Long idState) {
        this.idState = idState;
    }

    public String getNewState() {
        return newState;
    }

    public void setNewState(String newState) {
        this.newState = newState;
    }

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

    public Payterms getPayterm() {
        return payterm;
    }

    public void setPayterm(Payterms payterm) {
        this.payterm = payterm;
    }

    public Long getIdPayterm() {
        return idPayterm;
    }

    public void setIdPayterm(Long idPayterm) {
        this.idPayterm = idPayterm;
    }
}
