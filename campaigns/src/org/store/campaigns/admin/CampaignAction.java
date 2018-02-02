package org.store.campaigns.admin;

import org.store.campaigns.CampaignDAO;
import org.store.campaigns.CampaignUtils;
import org.store.core.admin.AdminModuleAction;
import org.store.campaigns.beans.Campaign;
import org.store.campaigns.beans.CampaignUser;
import org.store.core.beans.Mail;
import org.store.core.beans.StoreProperty;
import org.store.core.beans.User;
import org.store.campaigns.beans.UserGroup;
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
import org.apache.struts2.json.JSONUtil;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Namespace(value = "/admin")
@ParentPackage(value = "store-admin")
public class CampaignAction extends AdminModuleAction implements StoreMessages {

    @Override
    public void prepare() throws Exception {
        campaign = (Campaign) dao.get(Campaign.class, idCampaign);
    }

    @Action(value = "campaignduplicate", results = {@Result(type = "velocity", location = "/WEB-INF/views/admin/campaignedit.vm")})
    public String duplicate() throws Exception {
        Campaign sourceCampaign = (Campaign) dao.get(Campaign.class, idCampaign);
        if (sourceCampaign != null) {
            campaign = new Campaign();
            campaign.setCampaignMail(sourceCampaign.getCampaignMail());
            campaign.setDateToSend(sourceCampaign.getDateToSend());
            campaign.setUserGroup(sourceCampaign.getUserGroup());
        }
        return edit();
    }

    @Action(value = "campaignlist", results = {@Result(type = "velocity", location = "/WEB-INF/views/admin/campaignlist.vm")})
    public String list() throws Exception {
        if (selecteds != null && selecteds.length > 0) {
            for (Long id : selecteds) {
                Campaign bean = (Campaign) dao.get(Campaign.class, id);
                if (bean != null) dao.delete(bean);
            }
            dao.flushSession();
        }

        DataNavigator campaigns = new DataNavigator(getRequest(), "campaigns");
        CampaignDAO campaignDAO = new CampaignDAO(dao);
        campaigns.setListado(campaignDAO.getCampaigns(campaigns));
        addToStack("campaigns", campaigns);
        getBreadCrumbs().add(new BreadCrumb(null, getText("admin.campaign.list"), null, null));
        return SUCCESS;
    }

    @Action(value = "campaignedit", results = {
            @Result(type = "velocity", location = "/WEB-INF/views/admin/campaignedit.vm"),
            @Result(name = "show", type = "velocity", location = "/WEB-INF/views/admin/campaignshow.vm")})
    public String edit() throws Exception {
        // Verificar si la campaña ya fue enviada
        if (campaign != null && !Campaign.STATUS_CREATING.equalsIgnoreCase(campaign.getStatus())) {
            CampaignDAO campaignDAO = new CampaignDAO(dao);
            addToStack("mails", getEmailStatus(campaign));

            Calendar cal = Calendar.getInstance();
            if (campaign.getDateToSend().before(cal.getTime())) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                Date start = SomeUtils.dateIni(campaign.getDateToSend());
                Date end = SomeUtils.dateEnd(cal.getTime());
                Long interval = (SomeUtils.dayDiff(start, end)+1);

                int maxYAxis = 0;
                List<List<Serializable>> lineOpened = new ArrayList<List<Serializable>>();
                List<List<Serializable>> lineClicked = new ArrayList<List<Serializable>>();
                cal.setTime(start);
                while (cal.getTime().before(end)) {
                    Date rangeStart = cal.getTime();
                    cal.add(Calendar.HOUR, interval.intValue());
                    Date rangeEnd = cal.getTime();

                    List<Serializable> lO = new ArrayList<Serializable>();
                    lO.add(sdf.format(rangeEnd));
                    Object opened = campaignDAO.getCampaignStatsInRange(campaign.getId(), rangeStart, rangeEnd, "opened");
                    int oInt = (opened!=null && opened instanceof Number) ? ((Number)opened).intValue() : 0;
                    lO.add( oInt );
                    lineOpened.add(lO);
                    if (oInt>maxYAxis) maxYAxis = oInt;

                    List<Serializable> lC = new ArrayList<Serializable>();
                    lC.add(sdf.format(rangeEnd));
                    Object clicked = campaignDAO.getCampaignStatsInRange(campaign.getId(), rangeStart, rangeEnd, "clicked");
                    lC.add((clicked!=null && clicked instanceof Number) ? ((Number)clicked).intValue() : 0 );
                    lineClicked.add(lC);
                }
                Double d = Math.ceil(maxYAxis / 4d) * 4;
                addToStack("chartYMax", d.intValue());
                addToStack("chartOpened", JSONUtil.serialize(lineOpened));
                addToStack("chartClicked", JSONUtil.serialize(lineClicked));
                addToStack("chartMin", start);
                addToStack("chartMax", end);
                addToStack("chartInterval", interval * 2);

                addToStack("urls", campaignDAO.getClickedLinks(campaign.getId()));
            }
            return "show";
        }
        getBreadCrumbs().add(new BreadCrumb(null, getText("admin.campaign.list"), url("campaignlist", "/admin"), null));
        getBreadCrumbs().add(new BreadCrumb(null, getText(campaign!=null ? "admin.campaign.modify" : "admin.campaign.new"), null, null));
        return SUCCESS;
    }

    @Action(value = "campaignsave", results = @Result(type = "redirectAction", location = "campaignedit?idCampaign=${campaign.id}"))
    public String save() throws Exception {
        if (campaign != null) {
            campaign.setInventaryCode(getStoreCode());
            dao.save(campaign);
        }
        return SUCCESS;
    }

    @Action(value = "campaigntest")
    public String test() throws Exception {
        if (StringUtils.isNotEmpty(testEmailAddress) && StringUtils.isNotEmpty(testEmailBody)) {

            String baseUrl = getStoreProperty(StoreProperty.PROP_SITE_URL, "");
            if (!baseUrl.endsWith("/")) baseUrl += "/";

            if (StringUtils.isEmpty(testEmailSubject)) testEmailSubject = getStoreProperty(StoreProperty.PROP_SITE_NAME, "");
            Long id = (idCampaign != null) ? idCampaign : 0l;
            testEmailBody = CampaignUtils.processCampaignMail(testEmailBody);
            testEmailBody = CampaignUtils.personalizeCampaignMail(testEmailBody, getAdminUser(), null, baseUrl);
            Mail mail = new Mail();
            mail.setFromAddress(getStoreProperty(StoreProperty.PROP_MAIL_FRONT_NEWSLETTER, null));
            mail.setBody(testEmailBody);
            mail.setToAddress(testEmailAddress);
            mail.setSubject(testEmailSubject);
            mail.setInventaryCode(getStoreCode());
            dao.save(mail);
            if (MailSenderThreat.sendPendingMail(mail, this)) getResponse().getWriter().print("ok");
            else getResponse().getWriter().print("error");
            dao.save(mail);
        }
        return null;
    }

    @Action(value = "campaigngenerate", results = {
            @Result(name = "input", type = "velocity", location = "/WEB-INF/views/admin/campaignedit.vm"),
            @Result(type = "redirectAction", location = "campaignedit?idCampaign=${campaign.id}")})
    public String generateEmails() throws Exception {
        if (campaign != null) {
            campaign.setUserGroup((UserGroup) dao.get(UserGroup.class, idGroup));
            campaign.setDateToSend(SomeUtils.strToDate(sentDate, getLocale().getLanguage()));

            // si esta en otro estado, pasar directo al show
            if (!Campaign.STATUS_CREATING.equalsIgnoreCase(campaign.getStatus())) return SUCCESS;

            // Validaciones
            List<User> usersForCampaign = new ArrayList<User>();
            if (campaign.getDateToSend() == null) addActionError(getText(CNT_ERROR_CAMPAIGN_INVALID_DATE, CNT_ERROR_CAMPAIGN_INVALID_DATE_DEFAULT));
            if (Campaign.SEND_TO_ALL_CUSTOMERS.equalsIgnoreCase(campaign.getSendTo())) {
                usersForCampaign.addAll(dao.getUsers());
            } else if (Campaign.SEND_TO_NEWSLETTER_SUBSCRIBERS.equalsIgnoreCase(campaign.getSendTo())) {
                usersForCampaign.addAll(dao.getUsersForNewsletter());
            } else if (Campaign.SEND_TO_GROUP.equalsIgnoreCase(campaign.getSendTo())) {
                if (campaign.getUserGroup() == null) {
                    addActionError(getText(CNT_ERROR_CAMPAIGN_INVALID_GROUP, CNT_ERROR_CAMPAIGN_INVALID_GROUP_DEFAULT));
                } else {
                    CampaignDAO campaignDAO = new CampaignDAO(dao);
                    List<User> users = campaignDAO.getAllUsersForGroup(campaign.getUserGroup());
                    if (users==null || users.isEmpty()) {
                        addActionError(getText(CNT_ERROR_CAMPAIGN_GROUP_EMPTY, CNT_ERROR_CAMPAIGN_GROUP_EMPTY_DEFAULT));
                    } else {
                        usersForCampaign.addAll(users);
                    }
                }
            }

            if (usersForCampaign.isEmpty())
                addActionError(getText(CNT_ERROR_CAMPAIGN_INVALID_SENDTO, CNT_ERROR_CAMPAIGN_INVALID_SENDTO_DEFAULT));

            if (hasActionErrors()) return INPUT;

            dao.save(campaign);

            // Si el subject es vacio poner el nombre de la tienda
            if (StringUtils.isEmpty(campaign.getSubject()))
                campaign.setSubject(getStoreProperty(StoreProperty.PROP_SITE_NAME, ""));

            String baseUrl = getStoreProperty(StoreProperty.PROP_SITE_URL, "");
            if (!baseUrl.endsWith("/")) baseUrl += "/";

            // Generar correos
            String fromAddress = getStoreProperty(StoreProperty.PROP_MAIL_FRONT_NEWSLETTER, null);
            String content = CampaignUtils.processCampaignMail(campaign.getCampaignMail());
            for (User user : usersForCampaign)
                if (StringUtils.isNotEmpty(user.getEmail())) {
                    CampaignUser cu = new CampaignUser();
                    cu.setIdCampaign(campaign.getId());
                    cu.setIdUser(user.getIdUser());
                    dao.save(cu);

                    Mail mail = new Mail();
                    mail.setBody(CampaignUtils.personalizeCampaignMail(content, user, cu, baseUrl));
                    mail.setFromAddress(fromAddress);
                    mail.setToAddress(user.getEmail());
                    mail.setSubject(campaign.getSubject());
                    mail.setSentDate(campaign.getDateToSend());
                    mail.setInventaryCode(getStoreCode());
                    mail.setPriority(Mail.PRIORITY_LOW);
                    mail.setReference(Campaign.EMAIL_REFERENCE_PREFIX + campaign.getId().toString());
                    dao.save(mail);
                }

            campaign.setStatus(Campaign.STATUS_READY);

        }
        return SUCCESS;
    }

    @Action(value = "campaigncancel", results = {@Result(type = "redirectAction", location = "campaignedit?idCampaign=${campaign.id}")})
    public String cancelEmails() throws Exception {
        if (campaign != null && Campaign.STATUS_READY.equalsIgnoreCase(campaign.getStatus())) {
            Map map = getEmailStatus(campaign);
            if ((Boolean) map.get("cancellable")) {
                CampaignDAO campaignDAO = new CampaignDAO(dao);
                campaignDAO.removeMailsForCampaign(campaign);
                campaign.setStatus(Campaign.STATUS_CREATING);
                dao.save(campaign);
            } else {
                addSessionError(getText(CNT_ERROR_CAMPAIGN_CANNOT_CANCEL, CNT_ERROR_CAMPAIGN_CANNOT_CANCEL_DEFAULT));
            }
        }
        return SUCCESS;
    }

    @Action(value = "campaignmembers", results = @Result(type = "velocity", location = "/WEB-INF/views/admin/campaignmembers.vm"))
    public String members() throws Exception {
        getBreadCrumbs().add(new BreadCrumb(null, getText("admin.campaign.list"), url("campaignlist", "/admin"), null));
        if (campaign!=null) {
            CampaignDAO campaignDAO = new CampaignDAO(dao);
            DataNavigator nav = new DataNavigator(request, "members");
            nav.setPageRows(20);
            nav.setListado(campaignDAO.getMembers(nav, campaign.getId(), filter));
            addToStack("members", nav);
            addToStack("mails", getEmailStatus(campaign));
            Map<String,String> params = new HashMap<String, String>();
            params.put("idCampaign", campaign.getId().toString());
            getBreadCrumbs().add(new BreadCrumb(null, getText("admin.campaign.modify"), url("campaignedit", "/admin",params), null));
            getBreadCrumbs().add(new BreadCrumb(null, getText("view.recipient.activity"), null, null));
        }
        return SUCCESS;
    }

    @Action(value = "campaignmemberclicks", results = @Result(type = "velocity", location = "/WEB-INF/views/admin/campaignmemberclicks.vm"))
    public String memberClicks() throws Exception {
        if (campaign!=null) {
            CampaignDAO campaignDAO = new CampaignDAO(dao);
            addToStack("clicks", campaignDAO.getClicks(campaign.getId(), idUser));
        }
        return SUCCESS;
    }

    public Map getEmailStatus(Campaign campaign) {
        int total = 0;
        int pending = 0;
        int sent = 0;
        int errorT = 0;
        int errorF = 0;
        CampaignDAO campaignDAO = new CampaignDAO(dao);
        List<Object[]> lista = campaignDAO.getCampaignEmailStatus(campaign.getId());
        if (lista != null && !lista.isEmpty()) {
            for (Object[] arr : lista) {
                Integer status = ((Number) arr[0]).intValue();
                Integer cant = ((Number) arr[1]).intValue();
                total += cant;
                switch (status) {
                    case 0:
                        pending += cant;
                        break;
                    case 10:
                        sent += cant;
                        break;
                    case -1:
                        errorF += cant;
                        break;
                    default:
                        errorT += cant;
                        break;
                }
            }
        }
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("total", total);
        map.put("pending", pending);
        map.put("sent", sent);
        map.put("errorT", errorT);
        map.put("errorF", errorF);
        map.put("cancellable", pending == total);

        lista = campaignDAO.getCampaignStatus(campaign.getId());
        if (lista!=null && !lista.isEmpty()) {
            Object[] arr = lista.get(0);
            if (arr!=null && arr.length==2) {
                Integer opened = (arr[0]!=null && arr[0] instanceof Number) ? ((Number)arr[0]).intValue() : 0;
                map.put("opened", opened);
                Integer clicked = (arr[1]!=null && arr[1] instanceof Number) ? ((Number)arr[1]).intValue() : 0;
                map.put("clicked", clicked);
            }
        }
        return map;
    }


    public List<UserGroup> getUserGroups() {
        CampaignDAO campaignDAO = new CampaignDAO(dao);
        if (!requestCache.containsKey("USERGROUP_LIST")) requestCache.put("USERGROUP_LIST", campaignDAO.getUserGroups());
        return (List<UserGroup>) requestCache.get("USERGROUP_LIST");
    }

    private Campaign campaign;
    private Long idCampaign;
    private Long idUser;
    private String sentDate;
    private Long idProduct;
    private Long idGroup;
    private String template;
    private String filter;

    private String testEmailAddress;
    private String testEmailSubject;
    private String testEmailBody;

    public Long getIdUser() {
        return idUser;
    }

    public void setIdUser(Long idUser) {
        this.idUser = idUser;
    }

    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }

    public String getTemplate() {
        return template;
    }

    public void setTemplate(String template) {
        this.template = template;
    }

    public Campaign getCampaign() {
        return campaign;
    }

    public void setCampaign(Campaign campaign) {
        this.campaign = campaign;
    }

    public Long getIdCampaign() {
        return idCampaign;
    }

    public void setIdCampaign(Long idCampaign) {
        this.idCampaign = idCampaign;
    }

    public String getSentDate() {
        return sentDate;
    }

    public void setSentDate(String sentDate) {
        this.sentDate = sentDate;
    }

    public Long getIdProduct() {
        return idProduct;
    }

    public void setIdProduct(Long idProduct) {
        this.idProduct = idProduct;
    }

    public Long getIdGroup() {
        return idGroup;
    }

    public void setIdGroup(Long idGroup) {
        this.idGroup = idGroup;
    }

    public String getTestEmailAddress() {
        return testEmailAddress;
    }

    public void setTestEmailAddress(String testEmailAddress) {
        this.testEmailAddress = testEmailAddress;
    }

    public String getTestEmailBody() {
        return testEmailBody;
    }

    public void setTestEmailBody(String testEmailBody) {
        this.testEmailBody = testEmailBody;
    }

    public String getTestEmailSubject() {
        return testEmailSubject;
    }

    public void setTestEmailSubject(String testEmailSubject) {
        this.testEmailSubject = testEmailSubject;
    }
}
