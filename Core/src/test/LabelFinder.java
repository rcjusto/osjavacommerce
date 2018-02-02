package test;

import org.store.core.beans.LocalizedText;
import org.store.core.globals.config.Store20Database;
import org.store.core.hibernate.HibernateSessionFactory;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * Rogelio Caballero
 * 29/11/11 16:07
 */
public class LabelFinder {

    static String[] basePaths = {"/media/trabajo/proyectos/stores/version2/src/com", "/media/trabajo/proyectos/stores/version2/templates"};
    static String[] extensions = {"vm", "java"};

    public static void main(String[] args) {
        listaLabels();
    }

    public static void listaLabels() {
        Store20Database db = new Store20Database();
        db.setId("books");
        db.setType("MySQL");
        db.setUrl("jdbc:mysql://localhost/books");
        db.setUser("root");
        db.setPassword("root");
        try {
            System.out.println("----------");
            org.hibernate.Session s = HibernateSessionFactory.getNewSession(db);
            List<String> labels = new ArrayList<String>();
            List<LocalizedText> list = s.createCriteria(LocalizedText.class).add(Restrictions.eq("inventaryCode", "spanishb")).addOrder(Order.asc("code")).list();
            for (LocalizedText it : list) {
                JSONObject json = new JSONObject(it.getValue());

                String en = json.optString("en", "");
                String es = json.optString("es", "");
                en = StringUtils.isNotEmpty(en) ? en : "";
                es = StringUtils.isNotEmpty(es) ? es : "";
                labels.add(it.getCode() + "|" + en + "|" + es);
            }

            File outFile = new File("/media/trabajo/proyectos/stores/version2/doc/labels.txt");
            try {
                if (!outFile.exists() || outFile.delete())
                    FileUtils.writeLines(outFile, labels);
            } catch (IOException e) {
                e.printStackTrace();
            }

            System.out.println("----------");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void buscaLabels() {
        List l = new ArrayList();
        List<LabelItem> labels = new ArrayList<LabelItem>();
        for (String path : basePaths) {
            File f = new File(path);
            if (f.exists()) {
                l.addAll(FileUtils.listFiles(f, extensions, true));
            }
        }
        int index = 1;
        for (Object o : l) {
            System.out.println(String.valueOf(index++) + "/" + l.size() + " -> " + o.toString());
            processFile((File) o, labels);
        }

        Collections.sort(labels, new Comparator<LabelItem>() {
            public int compare(LabelItem o1, LabelItem o2) {
                boolean has1 = o1.label.indexOf('$') > -1;
                boolean has2 = o2.label.indexOf('$') > -1;
                if (has1) {
                    return (has2) ? o1.label.compareTo(o2.label) : 1;
                } else {
                    return (has2) ? -1 : o1.label.compareTo(o2.label);
                }
            }
        });

        // obtener datos de la bd
        Store20Database db = new Store20Database();
        db.setId("books");
        db.setType("MySQL");
        db.setUrl("jdbc:mysql://localhost/books");
        db.setUser("root");
        db.setPassword("root");
        try {
            System.out.println("----------");
            org.hibernate.Session s = HibernateSessionFactory.getNewSession(db);
            for (LabelItem it : labels) {
                List<LocalizedText> ll = s.createCriteria(LocalizedText.class).add(Restrictions.eq("code", it.label)).list();
                if (ll != null && !ll.isEmpty()) {
                    LocalizedText lt = ll.get(0);

                    JSONObject json = new JSONObject(lt.getValue());

                    String en = json.optString("en", "");
                    String es = json.optString("es", "");
                    it.en = StringUtils.isNotEmpty(en) ? en : "";
                    it.es = StringUtils.isNotEmpty(es) ? es : "";
                }
                StringBuilder buffer = new StringBuilder();
                buffer.append("<tr><td>#label('").append(it.label).append("')</td>");
                buffer.append("<td " + ((StringUtils.isEmpty(it.en)) ? "style=\"background-color:red;\"" : "") + " >").append(it.en).append("</td>");
                buffer.append("<td>").append(it.file).append("</td></tr>");
                System.out.println(buffer.toString());

            }
            System.out.println("----------");
        } catch (Exception e) {
            e.printStackTrace();
        }

        // salvar a txt
        File outFile = new File("/media/trabajo/proyectos/stores/version2/doc/labels.txt");
        try {
            if (!outFile.exists() || outFile.delete())
                FileUtils.writeLines(outFile, labels);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(labels.size() + " labels founded");


    }

    private static void processFile(File f, List<LabelItem> labels) {
        try {
            Iterator it = FileUtils.lineIterator(f);
            while (it.hasNext()) {
                String line = (String) it.next();
                if (StringUtils.isNotEmpty(line)) {
                    processLine(line, labels, f);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void processLine(String line, List<LabelItem> labels, File f) {
        int it = 0;
        while ((it = StringUtils.indexOf(line, "#label(", it + 1)) > -1) {
            String cad = searchNextKey(line, it);
            if (StringUtils.isNotEmpty(cad)) {
                LabelItem item = new LabelItem(cad, f.getName(), "#label");
                if (!labels.contains(item)) labels.add(item);
            }
        }
        while ((it = StringUtils.indexOf(line, "#label(", it + 1)) > -1) {
            String cad = searchNextKey(line, it);
            if (StringUtils.isNotEmpty(cad)) {
                LabelItem item = new LabelItem(cad, f.getName(), "#admlabel");
                if (!labels.contains(item)) labels.add(item);
            }
        }
        while ((it = StringUtils.indexOf(line, "#stext(", it + 1)) > -1) {
            String cad = searchNextKey(line, it);
            if (StringUtils.isNotEmpty(cad) && cad.startsWith("name=")) {
                cad = cad.substring(5);
                if (StringUtils.isNotEmpty(cad)) {
                    LabelItem item = new LabelItem(cad, f.getName(), "#stext");
                    if (!labels.contains(item)) labels.add(item);
                }
            }
        }
        while ((it = StringUtils.indexOf(line, "getText(", it + 1)) > -1) {
            if (StringUtils.indexOf(line, "getText()", it - 1) != it) {
                String cad = searchNextKey(line, it);
                if (StringUtils.isNotEmpty(cad)) {
                    LabelItem item = new LabelItem(cad, f.getName(), "getText");
                    if (!labels.contains(item)) labels.add(item);
                }
            }
        }
    }

    private static String searchNextKey(String line, int it) {
        int i1 = StringUtils.indexOf(line, "\"", it);
        int i2 = StringUtils.indexOf(line, "'", it);
        if (i1 > 0) {
            if (i2 > 0 && i2 < i1) {
                int i3 = StringUtils.indexOf(line, "'", i2 + 1);
                if (i3 > i2) return StringUtils.substring(line, i2 + 1, i3);
            } else {
                int i3 = StringUtils.indexOf(line, "\"", i1 + 1);
                if (i3 > i1) return StringUtils.substring(line, i1 + 1, i3);
            }
        } else if (i2 > 0) {
            int i3 = StringUtils.indexOf(line, "'", i2 + 1);
            if (i3 > i2) return StringUtils.substring(line, i2 + 1, i3);
        }
        return null;
    }

    public static class LabelItem {

        public String label = "";
        public String file = "";
        public String line = "";
        public String en = "";
        public String es = "";

        public LabelItem(String label, String file, String line) {
            this.label = label;
            this.file = file;
            this.line = line;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof LabelItem) {
                LabelItem other = (LabelItem) obj;
                return new EqualsBuilder().append(this.label, other.label).isEquals();
            }
            return false;
        }

        @Override
        public String toString() {
            return label + '|' + en + '|' + es + '|' + file + '|' + line;
        }
    }

}
