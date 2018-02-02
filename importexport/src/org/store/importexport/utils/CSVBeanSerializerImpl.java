package org.store.importexport.utils;

import au.com.bytecode.opencsv.CSVReader;
import org.store.core.beans.utils.ExportedBean;
import org.store.core.beans.utils.TableFile;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.mozilla.universalchardet.UniversalDetector;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CSVBeanSerializerImpl extends BeanSerializer {

    public static Logger log = Logger.getLogger(CSVBeanSerializerImpl.class);
    private static final String CSV_COLUMN_SEPARATOR = ",";
    private static final char CSV_COLUMN_DELIMITER = '"';

    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private DecimalFormat df = new DecimalFormat("0.00", new DecimalFormatSymbols(new Locale("en")));


    public CSVBeanSerializerImpl(String store, String type) {
        super(store, type);
    }


    public boolean exportBeanList(List<ExportedBean> list, String[] fieldList, String lang, Map<String, String> friendlyNames, File aFile) throws IOException {
        StringBuilder buff = new StringBuilder();
        if (CollectionUtils.isNotEmpty(list) && fieldList != null && fieldList.length > 0) {
            buff.append(addCSVHeader(fieldList, friendlyNames));
            buff.append(System.getProperty("line.separator"));
            for (ExportedBean bean : list) {
                buff.append(addCSVRow(bean.toMap(lang), fieldList));
                buff.append(System.getProperty("line.separator"));
            }
        }
        return exportFile(buff.toString(), aFile);
    }

    public TableFile load(File f) {
        TableFile res = new TableFile();
        if (f != null) try {
            loadFile(f, res, false);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
        return res;
    }

    public void loadFile(File f, TableFile file, boolean onlyHeader) throws IOException {
        char separator = detectSeparator(f);
        if (separator != 0) {
            FileInputStream fis = new FileInputStream(f);
            byte[] buf = new byte[4096];
            // (1)
            UniversalDetector detector = new UniversalDetector(null);

            // (2)
            int nread;
            while ((nread = fis.read(buf)) > 0 && !detector.isDone()) {
                detector.handleData(buf, 0, nread);
            }
            // (3)
            detector.dataEnd();

            // (4)
            String encoding = detector.getDetectedCharset();

            if (encoding == null) {
                encoding = "ISO-8859-1";
            }

            // (5)
            detector.reset();
            fis.close();
            fis = new FileInputStream(f);

            InputStreamReader isr = new InputStreamReader(fis, encoding);
            CSVReader reader = new CSVReader(isr, separator);
            String[] nextLine = reader.readNext();
            if (nextLine != null) {
                file.fields = Arrays.asList(nextLine);
            }   
            if (!onlyHeader) while ((nextLine = reader.readNext()) != null) {
                file.addRow(nextLine);
            }
            reader.close();
        }
    }

    private char detectSeparator(File f) throws IOException {
        int maxCols = 0;
        char result = 0;
        char[] arrSep = {',', ';', '\t'};
        for (char sep : arrSep) {
            CSVReader reader = new CSVReader(new FileReader(f), sep);
            String[] cols = reader.readNext();
            if (cols != null && cols.length > maxCols) {
                maxCols = cols.length;
                result = sep;
            }
            reader.close();
        }
        return result;
    }

    private String addCSVHeader(String[] fieldList, Map<String, String> friendlyNames) {
        StringBuffer buff = new StringBuffer();
        for (String fieldName : fieldList) {
            addCSVColumn(buff, (friendlyNames != null && friendlyNames.containsKey(fieldName)) ? friendlyNames.get(fieldName) : fieldName);
        }
        return buff.toString();
    }

    private String addCSVRow(Map m, String[] fieldList) {
        StringBuffer buff = new StringBuffer();
        for (String fieldName : fieldList) {
            String value = m.containsKey(fieldName) ? (String) m.get(fieldName) : "";
            addCSVColumn(buff, value);
        }
        return buff.toString();
    }

    private void addCSVColumn(StringBuffer line, String v) {
        if (StringUtils.isNotEmpty(line.toString())) {
            line.append(CSV_COLUMN_SEPARATOR);
        }
        line.append(CSV_COLUMN_DELIMITER);
        if (v != null) {
            v = replace(v, "" + CSV_COLUMN_DELIMITER, "" + CSV_COLUMN_DELIMITER + CSV_COLUMN_DELIMITER);
            line.append(v);
        }
        line.append(CSV_COLUMN_DELIMITER);
    }

    private void addCSVColumn(StringBuffer line, Double v) {
        addCSVColumn(line, (v != null) ? df.format(v) : "");
    }

    private void addCSVColumn(StringBuffer line, Date v) {
        addCSVColumn(line, (v != null) ? sdf.format(v) : "");
    }

    private void addCSVColumn(StringBuffer line, Long v) {
        addCSVColumn(line, (v != null) ? v.toString() : "");
    }

    private void addCSVColumn(StringBuffer line, Integer v) {
        addCSVColumn(line, (v != null) ? v.toString() : "");
    }


    public static String replace(String original, String pattern, String replace) {
        final int len = pattern.length();
        int found = original.indexOf(pattern);

        if (found > -1) {
            StringBuilder sb = new StringBuilder();
            int start = 0;

            while (found != -1) {
                sb.append(original.substring(start, found));
                sb.append(replace);
                start = found + len;
                found = original.indexOf(pattern, start);
            }

            sb.append(original.substring(start));

            return sb.toString();
        } else {
            return original;
        }
    }

    private boolean exportFile(String aContents, File aFile) throws IOException {
        if (!aFile.exists() || aFile.delete()) {
            Writer output = null;
            try {
                output = new BufferedWriter(new FileWriter(aFile));
                output.write(aContents);
            } finally {
                if (output != null) output.close();
            }
            return  true;
        }
        return false;
    }


    private class Letters {
        public static final char LF = '\n';

        public static final char CR = '\r';

        public static final char QUOTE = '"';

        public static final char COMMA = ',';

        public static final char SPACE = ' ';

        public static final char TAB = '\t';

        public static final char POUND = '#';

        public static final char BACKSLASH = '\\';

        public static final char NULL = '\0';
    }

}
