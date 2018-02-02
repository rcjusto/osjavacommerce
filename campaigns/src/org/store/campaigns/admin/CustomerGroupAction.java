package org.store.campaigns.admin;

import org.store.campaigns.CampaignDAO;
import org.store.campaigns.beans.UserGroup;
import org.store.campaigns.beans.UserGroupMember;
import org.store.core.admin.AdminModuleAction;
import org.store.core.beans.User;
import org.store.core.beans.utils.BreadCrumb;
import org.store.core.beans.utils.DataNavigator;
import org.store.core.globals.StoreMessages;
import org.apache.commons.lang.StringUtils;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;

@Namespace(value = "/admin")
@ParentPackage(value = "store-admin")
public class CustomerGroupAction extends AdminModuleAction implements StoreMessages {

    @Override
    public void prepare() throws Exception {
        userGroup = (UserGroup) dao.get(UserGroup.class, idUserGroup);
    }

    @Action(value = "customergrouplist", results = {@Result(type = "velocity", location = "/WEB-INF/views/admin/customergrouplist.vm")})
    public String list() throws Exception {
        CampaignDAO campaignDAO = new CampaignDAO(dao);
        if (selecteds != null && selecteds.length > 0) {
            for (Long id : selecteds) {
                UserGroup bean = (UserGroup) dao.get(UserGroup.class, id);
                if (bean != null) {
                    String res = campaignDAO.isUsedUserGroup(bean);
                    if (StringUtils.isNotEmpty(res)) {
                        addActionError(getText(CNT_ERROR_CANNOT_DELETE_USERGROUP, CNT_DEFAULT_ERROR_CANNOT_DELETE_USERGROUP, new String[]{bean.getGroupName(), res}));
                    } else {
                        campaignDAO.deleteUserGroup(bean);
                    }
                }
            }
            dao.flushSession();
        }
        addToStack("userGroups", campaignDAO.getUserGroups());
        getBreadCrumbs().add(new BreadCrumb(null, getText("admin.customer.group.list"), null, null));
        return SUCCESS;
    }

    @Action(value = "customergroupedit", results = @Result(type = "velocity", location = "/WEB-INF/views/admin/customergroupedit.vm"))
    public String edit() throws Exception {

        if (userGroup != null) {
            CampaignDAO campaignDAO = new CampaignDAO(dao);

            if (selecteds != null && selecteds.length > 0) {
                for (Long id : selecteds) {
                    UserGroupMember member = campaignDAO.getMember(userGroup, id);
                    if (member != null) dao.delete(member);
                }
            }

            // listado de productos
            DataNavigator nav = new DataNavigator(request, "groupusers");
            nav.setListado(campaignDAO.getUsersForGroup(nav, userGroup));
            addToStack("groupusers", nav);
        }

        getBreadCrumbs().add(new BreadCrumb(null, getText("admin.customer.group.list"), url("customergrouplist", "/admin"), null));
        getBreadCrumbs().add(new BreadCrumb(null, getText(userGroup!=null ? "admin.customer.group.modify" : "admin.customer.group.new"), null, null));
        return SUCCESS;
    }

    @Action(value = "customergroupadd", results = @Result(type = "redirectAction", location = "customergroupedit?idUserGroup=${userGroup.idGroup}"))
    public String addUsers() throws Exception {
        if (userGroup != null && addUser != null && addUser.length > 0) {
            CampaignDAO campaignDAO = new CampaignDAO(dao);
            for (Long id : addUser) {
                UserGroupMember member = campaignDAO.getMember(userGroup, id);
                if (member == null) {
                    User user = (User) dao.get(User.class, id);
                    if (user != null) {
                        member = new UserGroupMember();
                        member.setGroup(userGroup);
                        member.setUser(user);
                        dao.save(member);
                    }
                }
            }
        }
        return SUCCESS;
    }

    @Action(value = "customergroupsave", results = @Result(type = "redirectAction", location = "customergroupedit?idUserGroup=${userGroup.idGroup}"))
    public String save() throws Exception {
        if (userGroup != null) {
            userGroup.setInventaryCode(getStoreCode());
            dao.save(userGroup);
        }
        return SUCCESS;
    }

    private UserGroup userGroup;
    private Long idUserGroup;
    private Long[] addUser;

    public Long[] getAddUser() {
        return addUser;
    }

    public void setAddUser(Long[] addUser) {
        this.addUser = addUser;
    }

    public UserGroup getUserGroup() {
        return userGroup;
    }

    public void setUserGroup(UserGroup userGroup) {
        this.userGroup = userGroup;
    }

    public Long getIdUserGroup() {
        return idUserGroup;
    }

    public void setIdUserGroup(Long idUserGroup) {
        this.idUserGroup = idUserGroup;
    }

}
