package org.store.core.admin;

import org.store.core.beans.RmaType;
import org.store.core.beans.utils.BreadCrumb;
import org.store.core.globals.StoreMessages;
import org.apache.commons.lang.StringUtils;

public class rmatypeAction extends AdminModuleAction implements StoreMessages {

    @Override
    public void prepare() throws Exception {
        rmaType = (RmaType) dao.get(RmaType.class, idRmaType);
    }

    public String list() throws Exception {
        if (selecteds != null && selecteds.length > 0) {
            for (Long id : selecteds) {
                RmaType bean = (RmaType) dao.get(RmaType.class, id);
                if (bean != null) {
                    String res = dao.isUsedRmaType(bean);
                    if (StringUtils.isNotEmpty(res)) {
                        addActionError(getText(CNT_ERROR_CANNOT_DELETE_RMATYPE, CNT_DEFAULT_ERROR_CANNOT_DELETE_RMATYPE, new String[]{bean.getName(getDefaultLanguage()), res}));
                    } else {
                        dao.delete(bean);
                    }
                }
            }
            dao.flushSession();
        }
        addToStack("rmatypelist", dao.getRmaTypes());
        getBreadCrumbs().add(new BreadCrumb(null, getText("admin.rma.type.list"), null, null));
        return SUCCESS;
    }

    public String edit() throws Exception {
        getBreadCrumbs().add(new BreadCrumb(null, getText("admin.rma.type.list"), url("listrmatype","/admin"), null));
        getBreadCrumbs().add(new BreadCrumb(null, getText(rmaType!=null ? "admin.rma.type.modify" : "admin.rma.type.new"), null, null));
        return SUCCESS;
    }

    public String save() throws Exception {
        if (rmaType != null) {
            if (rmaTypeName != null && rmaTypeName.length == getLanguages().length) {
                for (int i = 0; i < getLanguages().length; i++) {
                    rmaType.setName(getLanguages()[i], StringUtils.isNotEmpty(rmaTypeName[i]) ? rmaTypeName[i] : "");
                }
            }
            rmaType.setInventaryCode(getStoreCode());
            dao.save(rmaType);
        }
        return SUCCESS;
    }

    private RmaType rmaType;
    private Long idRmaType;
    private String[] rmaTypeName;

    public RmaType getRmaType() {
        return rmaType;
    }

    public void setRmaType(RmaType rmaType) {
        this.rmaType = rmaType;
    }

    public Long getIdRmaType() {
        return idRmaType;
    }

    public void setIdRmaType(Long idRmaType) {
        this.idRmaType = idRmaType;
    }

    public String[] getRmaTypeName() {
        return rmaTypeName;
    }

    public void setRmaTypeName(String[] rmaTypeName) {
        this.rmaTypeName = rmaTypeName;
    }
}
