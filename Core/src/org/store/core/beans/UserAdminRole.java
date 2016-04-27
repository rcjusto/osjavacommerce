package org.store.core.beans;

import org.store.core.beans.utils.StoreBean;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.hibernate.annotations.CollectionOfElements;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;


@Entity
@Table(name = "t_user_admin_role")
public class UserAdminRole extends BaseBean implements StoreBean {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 50)
    private String roleCode;

    // Tienda en la q esta configurado el producto
    @Column(length = 10)
    private String inventaryCode;

    @CollectionOfElements
    private Set<String> actions;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRoleCode() {
        return roleCode;
    }

    public void setRoleCode(String roleCode) {
        this.roleCode = roleCode;
    }

    public String getInventaryCode() {
        return inventaryCode;
    }

    public void setInventaryCode(String inventaryCode) {
        this.inventaryCode = inventaryCode;
    }

    public Set<String> getActions() {
        return actions;
    }

    public void setActions(Set<String> actions) {
        this.actions = actions;
    }

    public boolean hasAction(String a) {
        return actions != null && actions.contains(a);
    }

    public String toString() {
        return new ToStringBuilder(this)
                .append("id", getId())
                .toString();
    }


    public static Collection getAllActions() {
        String[] actions = new String[]
                {"home",
                        "productlist",
                        "productedit",
                        "productsave",
                        "categorylist",
                        "categoryedit",
                        "categorysave",
                        "complementgrouplist",
                        "complementgroupedit",
                        "complementgroupsave",
                        "complementlist",
                        "complementedit",
                        "complementsave",
                        "listprovider",
                        "editprovider",
                        "saveprovider",
                        "listmanufacturer",
                        "editmanufacturer",
                        "savemanufacturer",
                        "listcurrency",
                        "editcurrency",
                        "savecurrency",
                        "listprolabel",
                        "editprolabel",
                        "saveprolabel",
                        "listproductatt",
                        "editproductatt",
                        "saveproductatt",
                        "productproccessimages",
                        "listpromotions",
                        "editpromotions",
                        "savepromotions",
                        "customerlist",
                        "customeredit",
                        "customersave",
                        "listcustomergroup",
                        "editcustomergroup",
                        "savecustomergroup",
                        "listcustomerlevel",
                        "editcustomerlevel",
                        "savecustomerlevel",
                        "orderlist",
                        "orderedit",
                        "ordersave",
                        "listorderstatus",
                        "editorderstatus",
                        "saveorderstatus",
                        "rmalist",
                        "rmaedit",
                        "rmasave",
                        "listrmatype",
                        "editrmatype",
                        "savermatype",
                        "storeproperties",
                        "storepropertiessave",
                        "staticlabels",
                        "staticlabeledit",
                        "staticlabelsave",
                        "liststaticpage",
                        "editstaticpage",
                        "savestaticpage",
                        "liststaticblock",
                        "editstaticblock",
                        "savestaticblock",
                        "listmenu",
                        "editmenu",
                        "savemenu",
                        "listresource",
                        "editresource",
                        "saveresource",
                        "listshipping",
                        "editshipping",
                        "saveshipping",
                        "listfee",
                        "editfee",
                        "savefee",
                        "listtax",
                        "edittax",
                        "savetax",
                        "listcarrierproperty",
                        "savecarrierproperty"};
        return Arrays.asList(actions);
    }

}