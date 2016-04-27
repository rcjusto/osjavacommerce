package org.store.importexport.utils;

import org.store.core.beans.utils.ExportedBean;
import org.store.core.beans.utils.TableFile;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

import javax.servlet.ServletContext;
import java.io.File;
import java.io.IOException;
import java.util.*;

public abstract class BeanSerializer {
    public static final String PATH_EXPORT = "/export";

    public abstract void loadFile(File f, TableFile file, boolean onlyHeader) throws IOException;
    public abstract boolean exportBeanList(List<ExportedBean> list, String[] fieldList, String lang, Map<String, String> friendlyNames, File aFile) throws IOException;

    protected String store;
    protected String type;

    
    protected BeanSerializer(String store, String type) {
        this.store = store;
        this.type = type;
    }

    public static List<Map<String, String>> importToMap(TableFile tableFile, String[] exportField,  Map<String, String> friendlyNames) {
        List res = new ArrayList();
        Map<String, String> invFriendlyName = MapUtils.invertMap(friendlyNames);
        for (TableFile.TableFileRow row : tableFile.getRows()) {
            res.add(getRowMap(row, tableFile.getHeaders(), exportField, invFriendlyName));
        }
        return res;
    }

    public static Map<String, String> getRowMap(TableFile.TableFileRow row, List<String> fields, String[] filterFields, Map<String, String> invFriendlyName) {
        Map<String, String> map = new HashMap<String, String>();
        for (int i = 0; i < fields.size(); i++) {
            if (StringUtils.isNotEmpty(filterFields[i])) {
                String fn = (invFriendlyName.containsKey(filterFields[i])) ? invFriendlyName.get(filterFields[i]) : filterFields[i];
                map.put(fn, row.getCol(i));
            }
        }
        return map;
    }

    public static File[] getExportedFiles(ServletContext servletContext, String store, String type) {
        String relPath = "/stores/" + store + PATH_EXPORT + "/" + type;
        File folder = new File(servletContext.getRealPath(relPath));
        try { FileUtils.forceMkdir(folder); } catch (IOException ignored) {}
        File[] list = (folder.exists()) ? folder.listFiles() : null;
        if (list != null && list.length > 0)
            Arrays.sort(list, new Comparator<File>() {
                public int compare(File o1, File o2) {
                    return ((Long) o2.lastModified()).compareTo(o1.lastModified());
                }
            });
        return list;
    }

    public static BeanSerializer getInstance(String store, String type, String filename) {
        if (filename.toLowerCase().endsWith(".xls")) return new ExcelBeanSerializerImpl(store, type);
        else return new CSVBeanSerializerImpl(store, type);
    }


}
