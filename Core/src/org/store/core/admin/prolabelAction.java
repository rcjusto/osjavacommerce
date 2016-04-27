package org.store.core.admin;

import org.store.core.beans.Manufacturer;
import org.store.core.beans.ProductLabel;
import org.store.core.beans.Provider;
import org.store.core.beans.utils.BreadCrumb;
import org.store.core.globals.StoreMessages;
import org.apache.commons.lang.StringUtils;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;

@Namespace(value = "/admin")
@ParentPackage(value = "store-admin")
public class prolabelAction extends AdminModuleAction implements StoreMessages {

    @Override
    public void prepare() throws Exception {
        prolabel = (ProductLabel) dao.get(ProductLabel.class, idLabel);
        provider = (Provider) dao.get(Provider.class, idProvider);
        manufacturer = (Manufacturer) dao.get(Manufacturer.class, idManufacturer);
    }

    public String list() throws Exception {
        // Eliminar marcados
        if (selecteds != null && selecteds.length > 0) {
            for (Long id : selecteds) {
                ProductLabel lab = dao.getProductLabel(id);
                if (lab != null) {
                    String res = dao.isUsedLabel(lab);
                    if (StringUtils.isNotEmpty(res)) {
                        addActionError(getText(CNT_CANNOT_DELETE_LABEL, CNT_DEFAULT_CANNOT_DELETE_LABEL, new String[]{lab.getCode(), res}));
                    } else {
                        dao.deleteLabel(lab);
                    }
                }
            }
            dao.flushSession();
        }
        // Listado
        addToStack("prolabels", dao.getProductLabels());
        getBreadCrumbs().add(new BreadCrumb(null, getText("admin.product.label.list"), null, null));
        return SUCCESS;
    }

    public String edit() throws Exception {
        getBreadCrumbs().add(new BreadCrumb(null, getText("admin.product.label.list"), url("listprolabel", "/admin"), null));
        getBreadCrumbs().add(new BreadCrumb(null, getText(prolabel != null ? "admin.product.label.modify" : "admin.product.label.new"), null, null));
        return SUCCESS;
    }

    public String save() throws Exception {
        if (prolabel != null) {
            for (int l = 0; l < getLanguages().length; l++) {
                String lang = getLanguages()[l];
                prolabel.setName(lang, (labelName != null && labelName.length > 0) ? labelName[l] : "");
                prolabel.setContentDetail(lang, (contentDetail != null && contentDetail.length > 0) ? contentDetail[l] : "");
                prolabel.setContentList(lang, (contentList != null && contentList.length > 0) ? contentList[l] : "");
            }
            prolabel.setInventaryCode(getStoreCode());
            dao.save(prolabel);
        }
        return SUCCESS;
    }

    @Action(value = "applyprolabel", results = @Result(type = "redirectAction", location = "listprolabel"))
    public String apply() throws Exception {
        if (prolabel != null) {
            if (manufacturer != null) dao.applyLabelToManufacturer(prolabel, manufacturer);
            if (provider != null) dao.applyLabelToSupplier(prolabel, provider);
        }
        return SUCCESS;
    }

    private Manufacturer manufacturer;
    private Long idManufacturer;
    private ProductLabel prolabel;
    private Long idLabel;
    private String[] labelName;
    private String[] contentDetail;
    private String[] contentList;
    private Provider provider;
    private Long idProvider;

    public Manufacturer getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(Manufacturer manufacturer) {
        this.manufacturer = manufacturer;
    }

    public Long getIdManufacturer() {
        return idManufacturer;
    }

    public void setIdManufacturer(Long idManufacturer) {
        this.idManufacturer = idManufacturer;
    }

    public ProductLabel getProlabel() {
        return prolabel;
    }

    public void setProlabel(ProductLabel prolabel) {
        this.prolabel = prolabel;
    }

    public Long getIdLabel() {
        return idLabel;
    }

    public void setIdLabel(Long idLabel) {
        this.idLabel = idLabel;
    }

    public String[] getLabelName() {
        return labelName;
    }

    public void setLabelName(String[] labelName) {
        this.labelName = labelName;
    }

    public String[] getContentDetail() {
        return contentDetail;
    }

    public void setContentDetail(String[] contentDetail) {
        this.contentDetail = contentDetail;
    }

    public String[] getContentList() {
        return contentList;
    }

    public void setContentList(String[] contentList) {
        this.contentList = contentList;
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
}
