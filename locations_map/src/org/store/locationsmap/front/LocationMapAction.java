package org.store.locationsmap.front;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.store.core.front.FrontModuleAction;


/**
 * @author rcaballero
 */
@Namespace(value="/")
@ParentPackage(value = "store-front")
public class LocationMapAction extends FrontModuleAction {

    @Action(value = "locations_map", results = @Result(type = "velocity", location = "/WEB-INF/views/org/store/locationsmap/locations_map_page.vm"))
    public String locationsMap() throws Exception {
        return SUCCESS;
    }

}
