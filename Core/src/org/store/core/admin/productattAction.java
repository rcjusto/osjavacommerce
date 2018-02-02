package org.store.core.admin;

import org.store.core.beans.AttributeProd;
import org.store.core.beans.utils.BreadCrumb;
import org.store.core.beans.utils.DataNavigator;
import org.store.core.globals.StoreMessages;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;

import java.io.File;
import java.util.List;

@Namespace(value="/admin")
@ParentPackage(value = "store-admin")
public class productattAction extends AdminModuleAction implements StoreMessages {

    @Override
    public void prepare() throws Exception {
        productAtt = (AttributeProd) dao.get(AttributeProd.class, idProductAtt);
    }

    public String list() throws Exception {
        if (selecteds != null && selecteds.length > 0) {
            for (Long id : selecteds) {
                AttributeProd bean = (AttributeProd) dao.get(AttributeProd.class, id);
                if (bean != null) {
                    dao.deleteAttribute(bean);
                }
            }
            dao.flushSession();
        }

        addToStack("can_import", actionExist("import_products_attributes","/admin"));

        addToStack("productAttGroups", dao.getProductAttGroups());
        if (getModal()) {
            return "modal";
        } else {
            DataNavigator productAtts = new DataNavigator(getRequest(), "productAtts");
            productAtts.setListado(dao.getAttributesProduct(productAtts, productAttGroup));
            addToStack("productAtts", productAtts);
            return SUCCESS;
        }
    }

    public List<AttributeProd> getAttributesByGroup(String group) {
        return dao.getAttributesProduct(null, group);
    }

    public String edit() throws Exception {
        addToStack("productAttGroups", dao.getProductAttGroups());
        getBreadCrumbs().add(new BreadCrumb(null, getText("admin.productatt.list"), url("listproductatt","/admin"), null));
        getBreadCrumbs().add(new BreadCrumb(null, getText(productAtt != null ? "admin.productatt.modify" : "admin.productatt.new"), null, null));
        return SUCCESS;
    }

    public String save() throws Exception {
        if (productAtt != null) {
            if (attributeName != null && ArrayUtils.isSameLength(attributeName, getLanguages())) {
                for (int i = 0; i < getLanguages().length; i++) productAtt.setAttributeName(getLanguages()[i], attributeName[i]);
            }
            productAtt.setInventaryCode(getStoreCode());
            if (StringUtils.isEmpty(productAtt.getAttributeOptions())) productAtt.setAttributeOptions(null);
            dao.save(productAtt);
        }
        return SUCCESS;
    }

    private AttributeProd productAtt;
    private Long idProductAtt;
    private String productAttGroup;
    private String productAttList;
    private File productAttFile;
    private String[] attributeName;

    public AttributeProd getProductAtt() {
        return productAtt;
    }

    public void setProductAtt(AttributeProd productAtt) {
        this.productAtt = productAtt;
    }

    public Long getIdProductAtt() {
        return idProductAtt;
    }

    public void setIdProductAtt(Long idProductAtt) {
        this.idProductAtt = idProductAtt;
    }

    public String getProductAttGroup() {
        return productAttGroup;
    }

    public void setProductAttGroup(String productAttGroup) {
        this.productAttGroup = productAttGroup;
    }

    public String getProductAttList() {
        return productAttList;
    }

    public void setProductAttList(String productAttList) {
        this.productAttList = productAttList;
    }

    public File getProductAttFile() {
        return productAttFile;
    }

    public void setProductAttFile(File productAttFile) {
        this.productAttFile = productAttFile;
    }

    public String[] getAttributeName() {
        return attributeName;
    }

    public void setAttributeName(String[] attributeName) {
        this.attributeName = attributeName;
    }
}
