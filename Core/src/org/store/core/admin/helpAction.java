package org.store.core.admin;

import org.store.core.beans.Help;
import org.store.core.beans.utils.BreadCrumb;
import org.store.core.globals.StoreMessages;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

public class helpAction extends AdminModuleAction implements StoreMessages {


    @Override
    public void prepare() throws Exception {
        help = (Help) dao.get(Help.class, idHelp);
    }

    public String list() throws Exception {
        addToStack("helplist", dao.getHelpByPage(page));
        getBreadCrumbs().add(new BreadCrumb(null, getText("admin.help.list"), null, null));
       return SUCCESS;
    }

    public String edit() throws Exception {
        getBreadCrumbs().add(new BreadCrumb(null, getText("admin.help.list"), url("listhelp","/admin"), null));
        getBreadCrumbs().add(new BreadCrumb(null, getText(help!=null ? "admin.help.modify" : "admin.help.new"), null, null));
        return SUCCESS;
    }

    public String save() throws Exception {
        if (help != null) {
            if (helpContent != null && ArrayUtils.isSameLength(helpContent, getLanguages())) {
                for (int i = 0; i < getLanguages().length; i++) {
                    help.setHelpContent(getLanguages()[i], StringUtils.isNotEmpty(helpContent[i]) ? helpContent[i] : "");
                }
            }
            dao.save(help);
        }
        return SUCCESS;
    }

    private Help help;
    private Long idHelp;
    private String[] helpContent;
    private String page;

    public Help getHelp() {
        return help;
    }

    public void setHelp(Help help) {
        this.help = help;
    }

    public Long getIdHelp() {
        return idHelp;
    }

    public void setIdHelp(Long idHelp) {
        this.idHelp = idHelp;
    }

    public String[] getHelpContent() {
        return helpContent;
    }

    public void setHelpContent(String[] helpContent) {
        this.helpContent = helpContent;
    }

    public String getPage() {
        return page;
    }

    public void setPage(String page) {
        this.page = page;
    }
}
