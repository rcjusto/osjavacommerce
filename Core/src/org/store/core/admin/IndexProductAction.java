package org.store.core.admin;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;

/**
 * Rogelio Caballero
 * 18/05/12 14:12
 */
@Namespace(value = "/admin")
@ParentPackage(value = "store-admin")
public class IndexProductAction extends AdminModuleAction {

    @Action(value = "productindex", results = @Result(type = "velocity", name = "input", location = "/WEB-INF/views/admin/accessdeny.vm"))
    public String productIndex() throws Exception {

        return SUCCESS;
    }

    
}
