package org.store.core.utils.reports;

import org.apache.log4j.Logger;

import java.io.File;

public abstract class IReportExport {

    public static Logger log = Logger.getLogger(IReportExport.class);
    public abstract void exportToFile(ReportTable table, File file);
    public abstract byte[] exportToBytes(ReportTable table);
    public abstract String getContentType();
    public abstract String getContentDisposition();

    public static IReportExport getInstance(String format) {
        if ("pdf".equalsIgnoreCase(format)) return new ReportExportPdf();
        return new ReportExportExcel();
    }

}
