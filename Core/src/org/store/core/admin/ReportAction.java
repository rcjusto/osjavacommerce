package org.store.core.admin;


import org.store.core.beans.utils.BreadCrumb;
import org.store.core.globals.config.Store20Config;
import org.store.core.utils.reports.IReportExport;
import org.store.core.utils.reports.IStoreReport;
import org.store.core.utils.reports.ReportExportHtml;
import org.store.core.utils.reports.ReportExportPdf;
import org.store.core.utils.reports.ReportTable;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Namespace(value = "/admin")
@ParentPackage(value = "store-admin")
public class ReportAction extends AdminModuleAction {

    @Action(value = "reports", results = @Result(type = "velocity", location = "/WEB-INF/views/admin/reports.vm"))
    public String reports() throws Exception {
        Store20Config config = Store20Config.getInstance(getServletContext());
        List<IStoreReport> reports = new ArrayList<IStoreReport>();
        if (config != null && config.getReports() != null && !config.getReports().isEmpty()) {
            for (Class cl : config.getReports()) {
                Object o = cl.newInstance();
                if (o instanceof IStoreReport) {
                    reports.add((IStoreReport) o);
                }
            }
        }
        addToStack("reports", reports);
        getBreadCrumbs().add(new BreadCrumb(null, getText("admin.report.list"), null, null));
        return SUCCESS;
    }

    @Action(value = "report", results = @Result(type = "velocity", location = "/WEB-INF/views/admin/report.vm"))
    public String reportLoad() throws Exception {
        if (StringUtils.isNotEmpty(className) && StringUtils.isNotEmpty(code)) {
            Object o = Class.forName(className).newInstance();
            if (o instanceof IStoreReport) {
                IStoreReport report = (IStoreReport) o;
                addToStack("report", report);
                if (ArrayUtils.indexOf(report.getCodes(this), code) > -1) {
                    addToStack("config", report.loadConfiguration(code, this));
                }
            }
        }
        getBreadCrumbs().add(new BreadCrumb(null, getText("admin.report.list"), url("reports","/admin"), null));
        getBreadCrumbs().add(new BreadCrumb(null, getText("admin.report"), null, null));
       return SUCCESS;
    }

    @Action(value = "reportPreview", results = @Result(type = "stream", params = {"inputName","inputStream","contentType","text/html","contentCharSet","utf-8"}))
    public String reportExecute() throws Exception {
        if (StringUtils.isNotEmpty(className) && StringUtils.isNotEmpty(code)) {
            Object o = Class.forName(className).newInstance();
            if (o instanceof IStoreReport) {
                IStoreReport report = (IStoreReport) o;
                if (ArrayUtils.indexOf(report.getCodes(this), code) > -1) {
                    ReportTable rt = report.execute(code, this);
                    if (rt != null) {
                        ReportExportHtml htmlExport = new ReportExportHtml();
                        String cad = htmlExport.exportToString(rt, "", "");
                        if (StringUtils.isEmpty(cad)) cad = getText("empty.report","No hay filas en el reporte");
                        setInputStream(new ByteArrayInputStream(cad.getBytes()));
                    }
                }
            }
        }
        return SUCCESS;
    }

    @Action(value = "reportExport", results = @Result(type = "stream", params = {"inputName","inputStream","contentType","${contentType}","contentDisposition","${contentDisposition}"}))
    public String reportExport() throws Exception {
        if (StringUtils.isNotEmpty(className) && StringUtils.isNotEmpty(code)) {
            Object o = Class.forName(className).newInstance();
            if (o instanceof IStoreReport) {
                IStoreReport report = (IStoreReport) o;
                if (ArrayUtils.indexOf(report.getCodes(this), code) > -1) {
                    ReportTable rt = report.execute(code, this);
                    if (rt != null) {
                        String urlLogo = getUrlAdminLogo();
                        if (StringUtils.isNotEmpty(urlLogo)) rt.addParam(ReportExportPdf.LOGO_PATH, urlLogo);
                        IReportExport export = IReportExport.getInstance(format);
                        byte[] bytes = export.exportToBytes(rt);
                        setInputStream(new ByteArrayInputStream(bytes));
                        setContentDisposition(export.getContentDisposition());
                        setContentType(export.getContentType());
                    }
                }
            }
        }
        return SUCCESS;
    }

    public String getUrlAdminLogo() {
        File f;
        String prefix = getServletContext().getRealPath("/stores/" + getStoreCode()+"/images/admin/logo");
        for(String ext : new String[]{"png","gif","jpg"}) {
            f = new File(prefix + "." + ext);
            if (f.exists()) return prefix + "." + ext;
        }
        return null;
    }

    private String className;
    private String code;
    private String format;

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

}
