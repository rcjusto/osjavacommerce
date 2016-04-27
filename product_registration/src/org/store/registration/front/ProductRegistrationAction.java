package org.store.registration.front;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.store.core.beans.utils.DataNavigator;
import org.store.core.front.FrontModuleAction;
import org.store.core.globals.SomeUtils;
import org.store.registration.beans.ProductRegistration;
import org.store.registration.beans.RegistrationDAO;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Namespace(value="/")
@ParentPackage(value = "store-front")
public class ProductRegistrationAction extends FrontModuleAction {

    @Action(value = "product_registrations_login")
    public String login() throws Exception {
        Map<String,String> m = new HashMap<String,String>();
        m.put("redirectUrl",url("product_registrations"));
        setRedirectUrl(url("profile", "/", m));
        return "redirectUrl";
    }

    @Action(value = "product_registrations", results = {@Result(type = "velocity", location = "/WEB-INF/views/front/registrations.vm"),@Result(name="new", type = "redirectAction", location = "product_registration")})
    public String registrations() throws Exception {
        if (getFrontUser()==null) return login();

        RegistrationDAO rDAO = new RegistrationDAO(dao);
        List l = rDAO.getRegistrations(null, getFrontUser());
        addToStack("registrations", l);
        return !l.isEmpty() ? SUCCESS : "new";
    }

    @Action(value = "product_registration", results = {@Result(type = "velocity", location = "/WEB-INF/views/front/registration.vm")})
    public String registration() throws Exception {
        if (getFrontUser()==null) return login();

        registration = (ProductRegistration) dao.get(ProductRegistration.class, idRegistration);
        if (registration==null) registration = new ProductRegistration();
        return SUCCESS;
    }

    @Action(value = "product_registration_save", results = {@Result(type = "redirectAction", location = "product_registrations")})
    public String registrationSave() throws Exception {
        if (getFrontUser()==null) return login();

        if (registration!=null) {
            registration.setUser(getFrontUser());
            if (extraNumbers!=null && extraNumbers.length>0) {
                for(String code : extraNumbers) registration.addModelNumber(code);
            }
            registration.setPurchaseDate(SomeUtils.strToDate(purchaseDate, getLocale().getLanguage()));
            dao.save(registration);
        }
        return SUCCESS;
    }


    private Long idRegistration;
    private ProductRegistration registration;
    private String purchaseDate;
    private String[] extraNumbers;

    public Long getIdRegistration() {
        return idRegistration;
    }

    public void setIdRegistration(Long idRegistration) {
        this.idRegistration = idRegistration;
    }

    public ProductRegistration getRegistration() {
        return registration;
    }

    public void setRegistration(ProductRegistration registration) {
        this.registration = registration;
    }

    public String getPurchaseDate() {return purchaseDate;}

    public void setPurchaseDate(String purchaseDate) {this.purchaseDate = purchaseDate;}

    public String[] getExtraNumbers() {return extraNumbers;}

    public void setExtraNumbers(String[] extraNumbers) {this.extraNumbers = extraNumbers;}
}
