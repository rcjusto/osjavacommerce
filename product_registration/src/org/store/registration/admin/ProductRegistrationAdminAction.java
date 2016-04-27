package org.store.registration.admin;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.store.core.admin.AdminModuleAction;
import org.store.core.beans.utils.DataNavigator;
import org.store.core.globals.StoreMessages;
import org.store.registration.beans.ProductRegistration;
import org.store.registration.beans.RegistrationDAO;

@Namespace(value = "/admin")
@ParentPackage(value = "store-admin")
public class ProductRegistrationAdminAction extends AdminModuleAction implements StoreMessages {

    @Action(value = "product_registrations", results = {@Result(type = "velocity", location = "/WEB-INF/views/admin/registrations.vm")})
    public String registrations() throws Exception {

        DataNavigator nav = new DataNavigator(getRequest(), "registrations");
        RegistrationDAO rDAO = new RegistrationDAO(dao);
        nav.setListado(rDAO.searchRegistrations(nav, filterUser, filterPlace, filterModel, filterDateFrom, filterDateTo));
        addToStack("registrations", nav);

        return SUCCESS;
    }

    @Action(value = "product_registration", results = {@Result(type = "velocity", location = "/WEB-INF/views/admin/registration.vm")})
    public String registration() throws Exception {

        ProductRegistration bean = (ProductRegistration) dao.get(ProductRegistration.class, idRegistration);
        if (bean==null) bean = new ProductRegistration();
        addToStack("registration", bean);
        return SUCCESS;
    }

    private Long idRegistration;
    private String filterUser;
    private String filterPlace;
    private String filterDateFrom;
    private String filterDateTo;
    private String filterModel;

    public Long getIdRegistration() {
        return idRegistration;
    }

    public void setIdRegistration(Long idRegistration) {
        this.idRegistration = idRegistration;
    }

    public String getFilterUser() {
        return filterUser;
    }

    public void setFilterUser(String filterUser) {
        this.filterUser = filterUser;
    }

    public String getFilterPlace() {
        return filterPlace;
    }

    public void setFilterPlace(String filterPlace) {
        this.filterPlace = filterPlace;
    }

    public String getFilterDateFrom() {
        return filterDateFrom;
    }

    public void setFilterDateFrom(String filterDateFrom) {
        this.filterDateFrom = filterDateFrom;
    }

    public String getFilterDateTo() {
        return filterDateTo;
    }

    public void setFilterDateTo(String filterDateTo) {
        this.filterDateTo = filterDateTo;
    }

    public String getFilterModel() {
        return filterModel;
    }

    public void setFilterModel(String filterModel) {
        this.filterModel = filterModel;
    }
}
