package org.store.flashpromo.admin;

import org.store.core.admin.AdminModuleAction;
import org.store.flashpromo.beans.FlashPageConfig;
import org.store.flashpromo.beans.FlashPromoConfig;
import org.store.core.beans.utils.BreadCrumb;
import org.store.core.beans.utils.DataNavigator;
import org.store.core.globals.SomeUtils;
import org.store.core.globals.config.Store20Config;
import org.store.flashpromo.beans.FlashPromoConfigLang;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.hibernate.Criteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

import java.util.List;

/**
 * Rogelio Caballero
 * 27/04/12 21:31
 */
@Namespace(value = "/admin")
@ParentPackage(value = "store-admin")
public class FlashPromoAction extends AdminModuleAction {

    @Action(value = "flash_promo_list", results = {@Result(type = "velocity", location = "/WEB-INF/views/org/store/flashpromo/promo_list.vm")})
    public String flash_promo_list() throws Exception {
        if (selecteds != null && selecteds.length > 0) {
            for (Long s : selecteds) {
                FlashPromoConfig promo = (FlashPromoConfig) getDao().get(FlashPromoConfig.class, s);
                if (promo != null) {
                    String err = promoIsUsed(promo);
                    if (StringUtils.isNotEmpty(err)) addActionError(err);
                    else getDao().delete(promo);
                }
            }
        }

        DataNavigator nav = new DataNavigator(getRequest(), "promotions");
        nav.setListado(getPromotions(nav));
        addToStack("promotions", nav);
        getBreadCrumbs().add(new BreadCrumb(null, getText("fp.config.title"), null, null));
        return SUCCESS;
    }

    @Action(value = "flash_promo_edit", results = {@Result(type = "velocity", location = "/WEB-INF/views/org/store/flashpromo/promo_edit.vm")})
    public String flash_promo_edit() throws Exception {
        FlashPromoConfig bean = (FlashPromoConfig) getDao().get(FlashPromoConfig.class, SomeUtils.strToLong(id));
        addToStack("promo", bean);
        addToStack("groups", getGroups());
        getBreadCrumbs().add(new BreadCrumb(null, getText("fp.config.title"), null, null));
        return SUCCESS;
    }

    @Action(value = "flash_promo_save", results = {@Result(type = "redirectAction", location = "flash_promo_list")})
    public String flash_promo_save() throws Exception {

        FlashPromoConfig bean = (FlashPromoConfig) getDao().get(FlashPromoConfig.class, SomeUtils.strToLong(id));
        if (bean == null) {
            bean = new FlashPromoConfig();
        }
        bean.setName(name);
        bean.setVertical(vertical);
        bean.setHorizontal(horizontal);
        bean.setWidth(SomeUtils.strToInteger(width));
        bean.setSubscription("Y".equalsIgnoreCase(subscription));
        bean.setSubscriptionGroup(SomeUtils.strToLong(group));
        getDao().save(bean);

        String[] languages = getLanguages();
        if (content != null && ArrayUtils.isSameLength(content, languages)) {
            for (int i = 0; i < languages.length; i++) {
                FlashPromoConfigLang pLang = bean.getLanguage(languages[i]);
                if (pLang == null) {
                    pLang = new FlashPromoConfigLang();
                    pLang.setConfig(bean);
                    pLang.setLang(languages[i]);
                }
                pLang.setContent(content[i]);
                getDao().save(pLang);
            }
        }

        return SUCCESS;
    }

    private String promoIsUsed(FlashPromoConfig promo) {
        StringBuilder respuesta = new StringBuilder();
        List l = getDao().createCriteriaForStore(FlashPageConfig.class).add(Restrictions.eq("promo", promo)).list();
        if (l != null && !l.isEmpty()) {
            respuesta.append("Can not remove content ").append(promo.getName()).append(" .It's used in page: ");
            respuesta.append(((FlashPageConfig) l.get(0)).getPage());
            respuesta.append(" ...");
            return respuesta.toString();
        }
        return null;
    }

    private List<FlashPromoConfig> getPromotions(DataNavigator nav) {
        Criteria cri = getDao().createCriteriaForStore(FlashPromoConfig.class)
                .addOrder(Order.desc("created"));
        if (nav != null) {
            cri.setProjection(Projections.countDistinct("id"));
            Number total = (Number) cri.uniqueResult();
            nav.setTotalRows(total != null ? total.intValue() : 0);
            cri.setProjection(null);
            cri.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
            if (nav.getTotalRows() > 0) {
                cri.setMaxResults(nav.getPageRows());
                cri.setFirstResult(nav.getFirstRow() - 1);
            }
        }
        return cri.list();
    }

    private List getGroups() {
        Store20Config config = Store20Config.getInstance(getServletContext());
        if (config.getMapEvents().containsKey("campaign_designer")) {
            try {
                Class groupClass = Class.forName("org.store.core.beans.UserGroup");
                return getDao().createCriteriaForStore(groupClass).addOrder(Order.asc("groupName")).list();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private String page;
    private String id;
    private String name;
    private String vertical;
    private String horizontal;
    private String width;
    private String subscription;
    private String group;
    private String[] content;


    public String getPage() {
        return page;
    }

    public void setPage(String page) {
        this.page = page;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVertical() {
        return vertical;
    }

    public void setVertical(String vertical) {
        this.vertical = vertical;
    }

    public String getHorizontal() {
        return horizontal;
    }

    public void setHorizontal(String horizontal) {
        this.horizontal = horizontal;
    }

    public String getWidth() {
        return width;
    }

    public void setWidth(String width) {
        this.width = width;
    }

    public String getSubscription() {
        return subscription;
    }

    public void setSubscription(String subscription) {
        this.subscription = subscription;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String[] getContent() {
        return content;
    }

    public void setContent(String[] content) {
        this.content = content;
    }


}
