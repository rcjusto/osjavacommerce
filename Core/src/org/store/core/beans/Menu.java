package org.store.core.beans;

import org.store.core.beans.utils.StoreBean;
import org.store.core.globals.BaseAction;
import org.apache.commons.lang.StringUtils;
import org.apache.struts2.json.JSONException;
import org.apache.struts2.json.JSONUtil;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.util.HashMap;

/**
 * Elemento de Menu
 */
@Entity
@Table(name = "t_menu")
public class Menu extends BaseBean implements StoreBean {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Codigo de la tienda a la que pertenece el menu
    @Column(length = 10)
    private String inventaryCode;

    // Menu
    @Column(length = 100)
    private String menu;

    // Orden en que aparece el enlace
    private Integer menuOrder;

    @Column(length = 50)
    private String cssClass;

    // Nombre
    @Lob
    private String menuLabel;

    @Transient
    private HashMap<String, String> menuLabels;

    // Enlaces
    @Column(length = 255)
    private String linkAction;
    @ManyToOne
    private Category linkCategory;
    @ManyToOne
    private StaticText linkStaticText;
    @ManyToOne
    private Manufacturer linkManufacturer;
    @ManyToOne
    private ProductLabel linkLabel;
    @Column(length = 1024)
    private String linkUrl;

    /** S: solo aparece si hay usuario conectado
     *  N: solo aparece si no hay usuario conectado
     *  null o '': aparece siempre
     */
    @Column(length = 1)
    private String userConnected;

    // Hijos
    @ManyToOne
    private StaticText menuContent;

    private Long idParent;

    private Boolean showSubcategories;
    private static final String CNT_MENU_WITHOUT_LABEL = "menu.without.label";
    private static final String CNT_DEFAULT_MENU_WITHOUT_LABEL = "[menu without label]";


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMenu() {
        return menu;
    }

    public void setMenu(String menu) {
        this.menu = menu;
    }

    public Integer getMenuOrder() {
        return menuOrder;
    }

    public void setMenuOrder(Integer menuOrder) {
        this.menuOrder = menuOrder;
    }

    public String getUserConnected() {
        return userConnected;
    }

    public void setUserConnected(String userConnected) {
        this.userConnected = userConnected;
    }

    public String getMenuLabel() {
        return menuLabel;
    }

    public void setMenuLabel(String menuLabel) {
        this.menuLabel = menuLabel;
    }

    public HashMap<String, String> getMenuLabels() {
        return menuLabels;
    }

    public void setMenuLabels(HashMap<String, String> menuLabels) {
        this.menuLabels = menuLabels;
    }

    public Category getLinkCategory() {
        return linkCategory;
    }

    public void setLinkCategory(Category linkCategory) {
        this.linkCategory = linkCategory;
        if (linkCategory != null) {
            linkLabel = null;
            linkManufacturer = null;
            linkStaticText = null;
            linkUrl = null;
        }
    }

    public StaticText getLinkStaticText() {
        return linkStaticText;
    }

    public void setLinkStaticText(StaticText linkStaticText) {
        this.linkStaticText = linkStaticText;
        if (linkStaticText != null) {
            linkLabel = null;
            linkManufacturer = null;
            linkCategory = null;
            linkUrl = null;
        }
    }

    public Manufacturer getLinkManufacturer() {
        return linkManufacturer;
    }

    public void setLinkManufacturer(Manufacturer linkManufacturer) {
        this.linkManufacturer = linkManufacturer;
        if (linkManufacturer != null) {
            linkLabel = null;
            linkCategory = null;
            linkStaticText = null;
            linkUrl = null;
        }
    }

    public ProductLabel getLinkLabel() {
        return linkLabel;
    }

    public void setLinkLabel(ProductLabel linkLabel) {
        this.linkLabel = linkLabel;
        if (linkLabel != null) {
            linkCategory = null;
            linkManufacturer = null;
            linkStaticText = null;
            linkUrl = null;
        }
    }

    public String getLinkUrl() {
        return linkUrl;
    }

    public void setLinkUrl(String linkUrl) {
        this.linkUrl = linkUrl;
        if (StringUtils.isNotEmpty(linkUrl)) {
            linkLabel = null;
            linkManufacturer = null;
            linkStaticText = null;
            linkCategory = null;
        }
    }

    public StaticText getMenuContent() {
        return menuContent;
    }

    public void setMenuContent(StaticText menuContent) {
        this.menuContent = menuContent;
    }

    public String getInventaryCode() {
        return inventaryCode;
    }

    public void setInventaryCode(String inventaryCode) {
        this.inventaryCode = inventaryCode;
    }

    public Long getIdParent() {
        return idParent;
    }

    public void setIdParent(Long idParent) {
        this.idParent = idParent;
    }

    public Boolean getShowSubcategories() {
        return showSubcategories;
    }

    public void setShowSubcategories(Boolean showSubcategories) {
        this.showSubcategories = showSubcategories;
    }

    public String getLinkAction() {
        return linkAction;
    }

    public void setLinkAction(String linkAction) {
        this.linkAction = linkAction;
    }

    public String getCssClass() {
        return cssClass;
    }

    public void setCssClass(String cssClass) {
        this.cssClass = cssClass;
    }

    public String getMenuType() {
        String res = "url";
        if (linkCategory != null) res = "category";
        else if (linkManufacturer != null) res = "manufacturer";
        else if (linkLabel != null) res = "label";
        else if (linkStaticText != null) res = "statictext";
        else if (StringUtils.isNotEmpty(linkAction)) res = "action";
        return res;
    }
        
    public String getLabel(String lang) {
        String res = getMenuLabel(lang);
        if (linkCategory != null && StringUtils.isEmpty(res)) res = linkCategory.getCategoryName(lang);
        else if (linkManufacturer != null && StringUtils.isEmpty(res)) res = linkManufacturer.getManufacturerName();
        else if (linkLabel != null && StringUtils.isEmpty(res)) res = getAction().getText(linkLabel.getName(lang));
        else if (linkStaticText != null && StringUtils.isEmpty(res)) res = linkStaticText.getTitle(lang);
        return (StringUtils.isNotEmpty(res)) ? res : getAction().getText(CNT_MENU_WITHOUT_LABEL,CNT_DEFAULT_MENU_WITHOUT_LABEL);
    }

    public String getUrl() {
        BaseAction a = getAction();
        String res = null;
        if (linkCategory != null) res = a.urlCategory(linkCategory);
        else if (linkManufacturer != null) res = a.urlManufacturer(linkManufacturer);
        else if (linkLabel != null) res = a.urlLabel(linkLabel);
        else if (linkStaticText != null) res = a.urlPage(linkStaticText);
        else if (StringUtils.isNotEmpty(linkAction)) res = a.url(linkAction);
        else if (StringUtils.isNotEmpty(linkUrl)) res = linkUrl;
        return (StringUtils.isNotEmpty(res)) ? res : "#";
    }

    public String getMenuLabel(String lang) {
        String res = getMenuLabelLang(lang);
        if (isEmpty(res) && menuLabels != null) {
            for (String v : menuLabels.values()) {
                if (!isEmpty(v)) res = v;
            }
        }
        return res;
    }

    public String getMenuLabelLang(String lang) {
        String res = null;
        if (menuLabels == null || menuLabels.size() < 1) deserialize();
        if (menuLabels != null && menuLabels.containsKey(lang)) res = menuLabels.get(lang);
        return res;
    }

    public void addMenuLabel(String lang, String value) {
        if (menuLabels == null) menuLabels = new HashMap<String, String>();
        menuLabels.put(lang, value);
        serialize();
    }

    public void serialize() {
        try {
            if (menuLabels!=null && menuLabels.size()>0) menuLabel = JSONUtil.serialize(menuLabels);
            else menuLabel = null;
        } catch (JSONException e) {
            log.error(e.getMessage(), e);
        }
    }

    public void deserialize() {
        try {
            if (!isEmpty(menuLabel)) menuLabels = (HashMap) JSONUtil.deserialize(menuLabel);
            else menuLabels = null;
        } catch (JSONException e) {
            log.error(e.getMessage(), e);
        }
    }

}
