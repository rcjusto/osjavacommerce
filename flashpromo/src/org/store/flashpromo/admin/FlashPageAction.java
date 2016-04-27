package org.store.flashpromo.admin;

import org.store.core.admin.AdminModuleAction;
import org.store.flashpromo.beans.FlashPageConfig;
import org.store.flashpromo.beans.FlashPromoConfig;
import org.store.core.beans.utils.BreadCrumb;
import org.store.core.globals.SomeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.hibernate.Criteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Rogelio Caballero
 * 27/04/12 21:31
 */
@Namespace(value = "/admin")
@ParentPackage(value = "store-admin")
public class FlashPageAction extends AdminModuleAction {

    @Action(value = "flash_page_edit", results = {@Result(type = "velocity", location = "/WEB-INF/views/org/store/flashpromo/page_edit.vm")})
    public String flash_page_edit() throws Exception {

        String[] arr = {"home", "product", "category", "page", "search", "profile", "wishlist", "orders", "shopcart", "checkout"};
        List<String> pages = Arrays.asList(arr);
        addToStack("pages", pages);

        if (!pages.contains(page)) page = pages.get(0);

        List<FlashPageConfig> pageConfig = getDao().createCriteriaForStore(FlashPageConfig.class)
                .add(Restrictions.eq("page", page))
                .setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY)
                .addOrder(Order.asc("id"))
                .list();
        addToStack("config", pageConfig);

        List<FlashPromoConfig> promoConfig = getDao().createCriteriaForStore(FlashPromoConfig.class)
                .addOrder(Order.asc("name")).list();
        addToStack("promotions", promoConfig);
        getBreadCrumbs().add(new BreadCrumb(null, getText("fp.config.title"), null, null));
        return SUCCESS;
    }

    @Action(value = "flash_page_save")
    public String flash_page_save() throws Exception {
        if (id != null && id.length > 0) {
            for (int i = 0; i < id.length; i++) {
                if (StringUtils.isNotEmpty(page)) {
                    Long aid = (id != null && id.length > 0) ? SomeUtils.strToLong(id[i]) : null;
                    FlashPageConfig bean = (FlashPageConfig) getDao().get(FlashPageConfig.class, aid);
                    Long idPromo = (promo != null && promo.length > i) ? SomeUtils.strToLong(promo[i]) : null;
                    FlashPromoConfig promo = (FlashPromoConfig) getDao().get(FlashPromoConfig.class, idPromo);
                    if (promo != null) {
                        if (bean == null) {
                            bean = new FlashPageConfig();
                            bean.setPage(page);
                        }
                        if (active != null && active.length > i) bean.setActive(active[i] != null && "Y".equalsIgnoreCase(active[i]));
                        if (ismodal != null && ismodal.length > i) bean.setModal(ismodal[i] != null && "Y".equalsIgnoreCase(ismodal[i]));
                        if (once != null && once.length > i) bean.setOnlyOnce(once[i] != null && "Y".equalsIgnoreCase(once[i]));
                        bean.setShowAfter((showAfter != null && showAfter.length > i) ? SomeUtils.strToInteger(showAfter[i]) : null);
                        bean.setHideAfter((hideAfter != null && hideAfter.length > i) ? SomeUtils.strToInteger(hideAfter[i]) : null);
                        bean.setPromo(promo);
                        getDao().save(bean);
                    } else if (bean!=null) {
                        getDao().delete(bean);
                    }
                }
            }
        }
        Map<String,String> params = new HashMap<String, String>();
        if (StringUtils.isNotEmpty(page)) params.put("page", page);
        redirectUrl = url("flash_page_edit", "/admin",params);
        return "redirectUrl";
    }
    
    

    private String page;
    private String[] id;
    private String[] active;
    private String[] once;
    private String[] ismodal;
    private String[] showAfter;
    private String[] hideAfter;
    private String[] promo;
   
    public String getPage() {
        return page;
    }

    public void setPage(String page) {
        this.page = page;
    }

    public String[] getId() {
        return id;
    }

    public void setId(String[] id) {
        this.id = id;
    }

    public String[] getActive() {
        return active;
    }

    public void setActive(String[] active) {
        this.active = active;
    }

    public String[] getOnce() {
        return once;
    }

    public void setOnce(String[] once) {
        this.once = once;
    }

    public String[] getIsmodal() {
        return ismodal;
    }

    public void setIsmodal(String[] ismodal) {
        this.ismodal = ismodal;
    }

    public String[] getShowAfter() {
        return showAfter;
    }

    public void setShowAfter(String[] showAfter) {
        this.showAfter = showAfter;
    }

    public String[] getHideAfter() {
        return hideAfter;
    }

    public void setHideAfter(String[] hideAfter) {
        this.hideAfter = hideAfter;
    }

    public String[] getPromo() {
        return promo;
    }

    public void setPromo(String[] promo) {
        this.promo = promo;
    }
}
