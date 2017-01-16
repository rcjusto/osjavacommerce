package org.store.core.admin;

import org.store.core.beans.Category;
import org.store.core.beans.Manufacturer;
import org.store.core.beans.Menu;
import org.store.core.beans.ProductLabel;
import org.store.core.beans.StaticText;
import org.store.core.beans.utils.BreadCrumb;
import org.store.core.globals.SomeUtils;
import org.store.core.globals.StoreMessages;
import org.apache.commons.lang.StringUtils;

import javax.swing.tree.DefaultMutableTreeNode;
import java.util.List;

public class menuAction extends AdminModuleAction implements StoreMessages {

    @Override
    public void prepare() throws Exception {
        menu = (Menu) dao.get(Menu.class, idMenu);
    }

    public String list() throws Exception {
        if (selecteds != null && selecteds.length > 0) {
            for (Long id : selecteds) {
                Menu bean = (Menu) dao.get(Menu.class, id);
                if (bean != null) {
                    String res = dao.isUsedMenu(bean, getDefaultLanguage());
                    if (StringUtils.isNotEmpty(res)) {
                        addActionError(getText(CNT_ERROR_CANNOT_DELETE_MENU, CNT_DEFAULT_ERROR_CANNOT_DELETE_MENU, new String[]{bean.getLabel(getDefaultLanguage()), res}));
                    } else {
                        dao.delete(bean);
                    }
                }
            }
        }

        List<String> menuTypes = dao.getMenuTypes();
        if (!menuTypes.contains("TopBar")) menuTypes.add("TopBar");
        if (!menuTypes.contains("BottomBar")) menuTypes.add("BottomBar");
        if (!menuTypes.contains("CenterMenu")) menuTypes.add("CenterMenu");
        addToStack("menuTypes", menuTypes);
        if (StringUtils.isEmpty(menuType) && getSession().containsKey("ADMIN_MENU_NAME")) {
            menuType = (String) getSession().get("ADMIN_MENU_NAME");
        }
        if (StringUtils.isNotEmpty(menuType)) {
            DefaultMutableTreeNode menuTree = new DefaultMutableTreeNode();
            dao.fillMenuNodeChilds(menuTree, menuType);
            addToStack("menuTree", menuTree);
        }
        getBreadCrumbs().add(new BreadCrumb(null, getText("admin.menu.list"), null, null));
        return SUCCESS;
    }

    public String edit() throws Exception {
        List<String> menuTypes = dao.getMenuTypes();
        if (!menuTypes.contains("TopBar")) menuTypes.add("TopBar");
        if (!menuTypes.contains("BottomBar")) menuTypes.add("BottomBar");
        if (!menuTypes.contains("CenterMenu")) menuTypes.add("CenterMenu");
        addToStack("menuTypes", menuTypes);
        if (menu != null && StringUtils.isNotEmpty(menu.getMenu())) menuType = menu.getMenu();
        if (StringUtils.isNotEmpty(menuType)) {
            DefaultMutableTreeNode menuTree = new DefaultMutableTreeNode();
            dao.fillMenuNodeChilds(menuTree, menuType);
            addToStack("menuTree", menuTree);
        }
        getBreadCrumbs().add(new BreadCrumb(null, getText("admin.menu.list"), url("listmenu", "/admin"), null));
        getBreadCrumbs().add(new BreadCrumb(null, getText(menu != null ? "admin.menu.modify" : "admin.menu.new"), null, null));
        return SUCCESS;
    }

    public String del() throws Exception {
        return list();
    }

    public String save() throws Exception {
        if (menu != null) {
            menu.setLinkAction(null);
            menu.setLinkCategory(null);
            menu.setLinkManufacturer(null);
            menu.setLinkLabel(null);
            menu.setLinkStaticText(null);
            menu.setLinkUrl(null);
            if ("c".equalsIgnoreCase(menuLinkType)) {
                Long id = SomeUtils.strToLong(menuLinkValue);
                Category bean = dao.getCategory(id);
                menu.setLinkCategory(bean);
            } else if ("m".equalsIgnoreCase(menuLinkType)) {
                Long id = SomeUtils.strToLong(menuLinkValue);
                Manufacturer bean = (Manufacturer) dao.get(Manufacturer.class, id);
                if (bean != null) menu.setLinkManufacturer(bean);
                else addActionError("error.menu.link.incorrect");
            } else if ("l".equalsIgnoreCase(menuLinkType)) {
                ProductLabel bean = dao.getProductLabelByCode(menuLinkValue);
                if (bean != null) menu.setLinkLabel(bean);
                else addActionError("error.menu.link.incorrect");
            } else if ("s".equalsIgnoreCase(menuLinkType)) {
                Long id = SomeUtils.strToLong(menuLinkValue);
                StaticText bean = (StaticText) dao.get(StaticText.class, id);
                if (bean != null) menu.setLinkStaticText(bean);
                else addActionError("error.menu.link.incorrect");
            } else if ("a".equalsIgnoreCase(menuLinkType)) {
                if (StringUtils.isNotEmpty(menuLinkValue)) menu.setLinkAction(menuLinkValue);
                else addActionError("error.menu.link.incorrect");
            } else if ("u".equalsIgnoreCase(menuLinkType)) {
                if (StringUtils.isNotEmpty(menuLinkValue)) menu.setLinkUrl(menuLinkValue);
                else addActionError("error.menu.link.incorrect");
            } else {
                addActionError("error.menu.link.incorrect");
            }
            if (hasActionErrors()) return INPUT;

            if (menuLabel != null && menuLabel.length > 0) {
                for (int l = 0; l < getLanguages().length; l++) {
                    if (menuLabel.length > l) menu.addMenuLabel(getLanguages()[l], menuLabel[l]);
                }
            }
            menu.setMenuContent((StaticText) dao.get(StaticText.class, menuSpecialContent));
            menu.setInventaryCode(getStoreCode());
            dao.save(menu);
            getSession().put("ADMIN_MENU_NAME", menu.getMenu());
        }
        return SUCCESS;
    }

    private Menu menu;
    private Long idMenu;
    private String menuType;
    private String menuLinkType;
    private String menuLinkValue;
    private String[] menuLabel;
    private Long menuSpecialContent;

    public Menu getMenu() {
        return menu;
    }

    public void setMenu(Menu menu) {
        this.menu = menu;
    }

    public Long getIdMenu() {
        return idMenu;
    }

    public void setIdMenu(Long idMenu) {
        this.idMenu = idMenu;
    }

    public String getMenuType() {
        return menuType;
    }

    public void setMenuType(String menuType) {
        this.menuType = menuType;
    }

    public String getMenuLinkType() {
        return menuLinkType;
    }

    public void setMenuLinkType(String menuLinkType) {
        this.menuLinkType = menuLinkType;
    }

    public String getMenuLinkValue() {
        return menuLinkValue;
    }

    public void setMenuLinkValue(String menuLinkValue) {
        this.menuLinkValue = menuLinkValue;
    }

    public String[] getMenuLabel() {
        return menuLabel;
    }

    public void setMenuLabel(String[] menuLabel) {
        this.menuLabel = menuLabel;
    }

    public Long getMenuSpecialContent() {
        return menuSpecialContent;
    }

    public void setMenuSpecialContent(Long menuSpecialContent) {
        this.menuSpecialContent = menuSpecialContent;
    }
}
