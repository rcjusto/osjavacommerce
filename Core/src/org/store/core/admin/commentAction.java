package org.store.core.admin;

import org.apache.commons.lang.StringUtils;
import org.store.core.beans.UserComment;
import org.store.core.beans.utils.BreadCrumb;
import org.store.core.beans.utils.DataNavigator;
import org.store.core.globals.StoreMessages;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class commentAction extends AdminModuleAction implements StoreMessages {

    @Override
    public void prepare() throws Exception {
        comment = (UserComment) dao.get(UserComment.class, idComment);
    }

    public String list() throws Exception {
        if (selecteds != null && selecteds.length > 0) {
            for (Long id : selecteds) {
                UserComment bean = (UserComment) dao.get(UserComment.class, id);
                if (bean != null) {
                    dao.deleteComment(bean);
                }
            }
            dao.flushSession();
        }
        DataNavigator nav = new DataNavigator(request, "comments");
        nav.setListado(dao.getComments(nav, filterType, filterStatus, filterName, filterText, null));
        addToStack("comments", nav);
        getBreadCrumbs().add(new BreadCrumb(null, getText("admin.comment.list"), null, null));
        return SUCCESS;
    }

    public String edit() throws Exception {
        getBreadCrumbs().add(new BreadCrumb(null, getText("admin.comment.list"), url("listcomment", "/admin"), null));
        getBreadCrumbs().add(new BreadCrumb(null, getText(comment != null ? "admin.comment.modify" : "admin.comment.new"), null, null));
        return SUCCESS;
    }

    public String save() throws Exception {
        if (comment != null) {
            if (comment.getCreated()==null) comment.setCreated(Calendar.getInstance().getTime());
            dao.save(comment);
        }
        return SUCCESS;
    }

    public Map<String, String> getCommentTypeList() {
        commentTypeList = new HashMap<String, String>();
        for(String type : dao.getCommentTypes()) {
            if (StringUtils.isNotEmpty(type)) commentTypeList.put(type, getText("comment.type."+type));
        }
        return commentTypeList;
    }

    public Map<String, String> getCommentStatusList() {
        if (commentStatusList == null || commentStatusList.isEmpty()) {
            commentStatusList = new HashMap<String, String>();
            commentStatusList.put("N", getText("comment.type.N","New"));
            commentStatusList.put("A", getText("comment.type.A","Active"));
            commentStatusList.put("I", getText("comment.type.I","Inactive"));
        }
        return commentStatusList;
    }


    private UserComment comment;
    private Long idComment;
    private Long[] commentId;
    private String filterStatus;
    private String filterName;
    private String filterType;
    private String filterText;
    private Map<String, String> commentTypeList;
    private Map<String, String> commentStatusList;

    public UserComment getComment() {
        return comment;
    }

    public void setComment(UserComment comment) {
        this.comment = comment;
    }

    public Long getIdComment() {
        return idComment;
    }

    public void setIdComment(Long idComment) {
        this.idComment = idComment;
    }

    public Long[] getCommentId() {
        return commentId;
    }

    public void setCommentId(Long[] commentId) {
        this.commentId = commentId;
    }

    public String getFilterStatus() {
        return filterStatus;
    }

    public void setFilterStatus(String filterStatus) {
        this.filterStatus = filterStatus;
    }

    public String getFilterName() {
        return filterName;
    }

    public void setFilterName(String filterName) {
        this.filterName = filterName;
    }

    public String getFilterType() {
        return filterType;
    }

    public void setFilterType(String filterType) {
        this.filterType = filterType;
    }

    public String getFilterText() {
        return filterText;
    }

    public void setFilterText(String filterText) {
        this.filterText = filterText;
    }
}
