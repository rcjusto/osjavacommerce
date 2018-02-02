package org.store.core.admin;

import org.store.core.beans.Mail;
import org.store.core.beans.utils.BreadCrumb;
import org.store.core.beans.utils.DataNavigator;
import org.store.core.globals.SomeUtils;
import org.store.core.globals.StoreMessages;
import org.store.core.mail.MailSenderThreat;
import org.apache.commons.lang.StringUtils;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;

import java.util.Date;
import java.util.Map;

@Namespace(value = "/admin")
@ParentPackage(value = "store-admin")
public class mailAction extends AdminModuleAction implements StoreMessages {

    @Override
    public void prepare() throws Exception {
        mail = (Mail) dao.get(Mail.class, idMail);
    }

    public String list() throws Exception {
        if (selecteds != null && selecteds.length > 0) {
            for (Long id : selecteds) {
                Mail m = (Mail) dao.get(Mail.class, id);
                if (m != null) dao.delete(m);
            }
            dao.flushSession();
        }

        processFilters();
        DataNavigator nav = new DataNavigator(getRequest(), "mails");
        Date dateFrom = SomeUtils.strToDate(getFilterVal("filterDateFrom"), getDefaultLanguage());
        Date dateTo = SomeUtils.strToDate(getFilterVal("filterDateTo"), getDefaultLanguage());

        nav.setListado(dao.getMails(nav, getFilterVal("filterStatus"), getFilterVal("filterSubject"), getFilterVal("filterEmail"), getFilterVal("filterReference"), dateFrom, dateTo));
        addToStack("mails", nav);
        return SUCCESS;
    }

    public String edit() throws Exception {
        getBreadCrumbs().add(new BreadCrumb(null, getText("admin.mail.list"), url("listmail","/admin"), null));
        getBreadCrumbs().add(new BreadCrumb(null, getText("admin.mail.details"), null, null));
      return SUCCESS;
    }

    public String save() throws Exception {
        if (mail != null) {
            dao.save(mail);
        }
        return SUCCESS;
    }

    @Action(value = "mailresend", results = @Result(type = "redirectAction", location = "editmail?idMail=${mail.idMail}"))
    public String send() throws Exception {
        if (mail != null) {
            dao.save(mail);
            MailSenderThreat.sendPendingMail(mail, this);
        }
        return SUCCESS;
    }

    protected void processFilters() {
        if (filters != null && !filters.isEmpty()) {
            for (Object key : filters.keySet()) {
                if (key instanceof String) filters.put(key, getFilterVal((String) key));
            }
        }
    }

    protected String getFilterVal(String cad) {
        if (filters != null && StringUtils.isNotEmpty(cad) && filters.containsKey(cad)) {
            Object o = filters.get(cad);
            if (o != null) {
                if (o instanceof String) return (String) o;
                else if (o instanceof String[] && ((String[]) o).length > 0) return ((String[]) o)[0];
                else return o.toString();
            }
        }
        return null;
    }

    private Mail mail;
    private Long idMail;

    private Map filters;
    private String sentDate;

    public Mail getMail() {
        return mail;
    }

    public void setMail(Mail mail) {
        this.mail = mail;
    }

    public Long getIdMail() {
        return idMail;
    }

    public void setIdMail(Long idMail) {
        this.idMail = idMail;
    }

    public Map getFilters() {
        return filters;
    }

    public void setFilters(Map filters) {
        this.filters = filters;
    }

    public String getSentDate() {
        return sentDate;
    }

    public void setSentDate(String sentDate) {
        this.sentDate = sentDate;
    }
}
