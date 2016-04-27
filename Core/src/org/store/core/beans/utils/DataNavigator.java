package org.store.core.beans.utils;

import org.store.core.globals.SomeUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Administrador
 * Date: Apr 20, 2010
 */
public class DataNavigator {

    private static final int CNT_DEFAULT_ROWS = 10;

    private int pageRows;
    private int totalRows;
    private int currentPage;
    private String name;
    private List listado;

    public DataNavigator(String name) {
        pageRows = CNT_DEFAULT_ROWS;
        currentPage = 1;
        if (name == null || "".equals(name)) name = "datanav";
        this.name = name;
    }

    public DataNavigator(int tr, String name) {
        totalRows = tr;
        pageRows = CNT_DEFAULT_ROWS;
        currentPage = 1;
        if (name == null || "".equals(name)) name = "datanav";
        this.name = name;
    }

    public DataNavigator(HttpServletRequest request, String name) {
        if (name == null || "".equals(name)) name = "datanav";
        this.name = name;
        Integer pr = SomeUtils.strToInteger(request.getParameter(name + ".pagerows"));
        Integer cp = SomeUtils.strToInteger(request.getParameter(name + ".currentpage"));

        // find in session
        if (pr == null) pr = SomeUtils.toInteger(request.getSession().getAttribute(name + ".pagerows"));
        if (cp == null) cp = SomeUtils.toInteger(request.getSession().getAttribute(name + ".currentpage"));

        // find in cookies
        if (pr == null && request.getCookies() != null) {
            for (Cookie c : request.getCookies()) {
                if ((name + ".pagerows").equalsIgnoreCase(c.getName()) && "/".equalsIgnoreCase(c.getPath())) {
                    pr = SomeUtils.strToInteger(c.getValue());
                    break;
                }
            }
        }
        if (pr == null && request.getAttribute(name + ".pagerows") != null && request.getAttribute(name + ".pagerows") instanceof Integer)
            pr = (Integer) request.getAttribute(name + ".pagerows");
        pageRows = (pr != null) ? pr : CNT_DEFAULT_ROWS;

        if (cp == null && request.getAttribute(name + ".currentpage") != null && request.getAttribute(name + ".currentpage") instanceof Integer)
            cp = (Integer) request.getAttribute(name + ".currentpage");
        currentPage = (cp != null) ? cp : 1;

        request.getSession().setAttribute(name + ".pagerows", getPageRows());
    }

    public int getPageRows() {
        return pageRows > 0 ? pageRows : CNT_DEFAULT_ROWS;
    }

    public void setPageRows(int pageRows) {
        this.pageRows = pageRows;
    }

    public int getTotalRows() {
        return totalRows;
    }

    public void setTotalRows(int totalRows) {
        this.totalRows = totalRows;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    public boolean needPagination() {
        return getPageRows() > 0 && getTotalRows() > getPageRows();
    }

    public int getFirstRow() {
        int fr = 1;
        if (totalRows > 0) fr = (currentPage - 1) * getPageRows() + 1;
        else setCurrentPage(1);
        if (fr > totalRows) {
            fr = 1;
            setCurrentPage(1);
        }
        return fr;
    }

    public int getLastRow() {
        return (totalRows > 0) ? Math.min(currentPage * getPageRows(), totalRows) : 0;
    }

    public int getPageCount() {
        return (totalRows > 0) ? ((totalRows - 1) / getPageRows()) + 1 : 0;
    }

    public boolean isNextPage() {
        return currentPage < getPageCount();
    }

    public int getNextPage() {
        return (isNextPage()) ? currentPage + 1 : currentPage;
    }

    public boolean isPrevPage() {
        return currentPage > 1;
    }

    public int getPrevPage() {
        return (isPrevPage()) ? currentPage - 1 : currentPage;
    }

    public List getListado() {
        return listado;
    }

    public void setListado(List listado) {
        this.listado = listado;
    }

    public boolean notEmpty() {
        return listado != null && listado.size() > 0;
    }

    public boolean getShowNavigator() {
        return getPageCount() > 1;
    }

    public String getName() {
        return name;
    }

    public Cookie getPageRowCookie() {
        Cookie c = new Cookie(name + ".pagerows", String.valueOf(getPageRows()));
        c.setMaxAge(298102);
        c.setPath("/");
        return c;
    }

    public List getNavList(List l) {
        if (l == null || l.size() < 1) return null;
        List lTemp = l.subList(getFirstRow() - 1, Math.min(getLastRow(), l.size()));
        if (l.size() > 0 && (lTemp == null || lTemp.size() < 1)) {
            if (currentPage > 0) setCurrentPage(getPageCount());
            else setCurrentPage(1);
            lTemp = l.subList(getFirstRow() - 1, Math.min(getLastRow(), l.size()));
        }
        return lTemp;
    }

    public List<Integer> getPageListLikeGoogle(int cant) {
        int min = Math.max(1, getCurrentPage() - cant);
        int max = Math.min(getPageCount(), getCurrentPage() + cant);
        return Arrays.asList(min, max);
    }

    public void saveToSession(HttpServletRequest request) {
        request.getSession().setAttribute(name + ".currentpage", getCurrentPage());
    }

}
