package org.store.importexport.utils;

import org.store.core.beans.utils.ExportedBean;
import org.store.core.beans.utils.TableFile;
import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ExcelBeanSerializerImpl extends BeanSerializer {
    public static Logger log = Logger.getLogger(ExcelBeanSerializerImpl.class);
    private DecimalFormat df = new DecimalFormat("##########################.####");
    protected ExcelBeanSerializerImpl(String store, String type) {
        super(store, type);
    }
   

    @Override
    public void loadFile(File f, TableFile file, boolean onlyHeader) throws IOException {
        if (file==null) file = new TableFile();
        POIFSFileSystem fs;
        FileInputStream fis = new FileInputStream(f);
        try {
            fs = new POIFSFileSystem();
            HSSFWorkbook wb = new HSSFWorkbook(fs);

            HSSFSheet sheet = wb.getSheetAt(0);
            Iterator itRow = sheet.rowIterator();
            // Encabezado
            HSSFRow row = (HSSFRow) itRow.next();
            List<String> header = new ArrayList<String>();
            short firstCol = row.getFirstCellNum();
            short lastCol = row.getLastCellNum();
            file.fields = new ArrayList();
            for (short i = firstCol; i <= lastCol; i++) file.fields.add(getCellValueStr(row.getCell(i)));
            while (itRow.hasNext()) {
                row = (HSSFRow) itRow.next();
                List<String> lista = new ArrayList<String>();
                for (short i = firstCol; i <= lastCol; i++) lista.add(getCellValueStr(row.getCell(i)));
                file.addRow(lista);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            fis.close();
        }
    }

    @Override
    public boolean exportBeanList(List<ExportedBean> list, String[] fieldList, String lang, Map<String, String> friendlyNames, File aFile) throws IOException {
        return false;  // todo
    }

    public String getCellValueStr(HSSFCell c) {
        String res = null;
        if (c != null) {
            switch (c.getCellType()) {
                case HSSFCell.CELL_TYPE_NUMERIC:
                case HSSFCell.CELL_TYPE_FORMULA:
                    res = df.format(c.getNumericCellValue());
                    break;
                default:
                    res = c.getStringCellValue();
            }
        }
        return (res != null) ? res.trim() : null;
    }

 
}
