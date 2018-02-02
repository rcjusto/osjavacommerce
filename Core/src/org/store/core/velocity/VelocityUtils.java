package org.store.core.velocity;

import org.store.core.beans.StoreProperty;
import org.store.core.beans.utils.MultiLangBean;
import org.store.core.globals.BaseAction;
import org.store.core.globals.SomeUtils;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.struts2.ServletActionContext;
import org.apache.velocity.tools.view.context.ViewContext;
import org.hibernate.stat.QueryStatistics;
import org.hibernate.stat.Statistics;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: Administrador
 * Date: Mar 23, 2010
 */
public class VelocityUtils {

    public static Logger log = Logger.getLogger(VelocityUtils.class);
    protected ServletContext application;
    protected HttpServletRequest request;
    protected HttpServletResponse response;
    protected BaseAction action;


    /**
     * Initializes this instance for the current request.
     *
     * @param obj the ViewContext of the current request
     */
    public void init(Object obj) {
        ViewContext context = (ViewContext) obj;
        this.request = context.getRequest();
        this.response = context.getResponse();
        this.application = context.getServletContext();
        this.action = (ServletActionContext.getContext().getActionInvocation().getAction() instanceof BaseAction) ? (BaseAction) ServletActionContext.getContext().getActionInvocation().getAction() : null;
    }

    public boolean isEmpty(String v) {
        return StringUtils.isEmpty(v);
    }

    public boolean isNotEmpty(String v) {
        return !isEmpty(v);
    }

    public boolean isEmpty(Collection c) {
        return c == null || c.size() < 1;
    }

    public boolean isNotEmpty(Collection c) {
        return !isEmpty(c);
    }

    public List left(List c, int cant) {
        return (c != null && c.size() > 0) ? c.subList(0, Math.min(cant, c.size() - 1)) : null;
    }

    public String abbreviate(String cad, int size) {
        return StringUtils.abbreviate(SomeUtils.extractText(cad), size);
    }

    public String left(String cad, int size) {
        return StringUtils.left(SomeUtils.extractText(cad), size);
    }

    public String right(String cad, int size) {
        return StringUtils.right(SomeUtils.extractText(cad), size);
    }

    public String left(String cad, String sep) {
        return StringUtils.substringBefore(SomeUtils.extractText(cad), sep);
    }

    public String right(String cad, String sep) {
        return StringUtils.substringAfter(SomeUtils.extractText(cad), sep);
    }

    public String[] split(String cad, String sep) {
        return StringUtils.split(cad, sep);
    }

    public List<String> getLines(String cad) {
        if (StringUtils.isEmpty(cad)) return null;
        List<String> res = new ArrayList<String>();
        BufferedReader reader = new BufferedReader(new StringReader(cad));
        String str;
        try {
            while ((str = reader.readLine()) != null) {
                res.add(str);
            }
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
        return res;
    }

    private Map<String,String> cacheImage = new HashMap<String, String>();
    public String findCacheImage(String code, String... paths) {
        if (cacheImage.containsKey(code)) return cacheImage.get(code);
        String[] arr = new String[]{"jpg", "jpeg", "gif", "png", "swf"};
        for(String p : paths) {
            String res = findImage(p, arr);
            if (StringUtils.isNotEmpty(res)) {
                cacheImage.put(code, res);
                return res;
            }
        }
        return "";
    }

    public String findImage(String path) {
        String[] arr = new String[]{"jpg", "jpeg", "gif", "png", "swf"};
        return findImage(path, arr);
    }

    public String findImage(String path, String[] exts) {
        for (String e : exts)
            if (new File(application.getRealPath(path + "." + e)).exists()) return path + "." + e;
        return "";
    }

    public boolean fileExist(String path) {
        return (isNotEmpty(path)) && new File(application.getRealPath(path)).exists();
    }

    public long fileSize(String path) {
        File f = new File(application.getRealPath(path));
        return (f.exists() && f.isFile()) ? f.length() : 0;
    }

    public String fileExt(String path) {
        String ext;
        try {
            ext = FilenameUtils.getExtension(path);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            ext = "";
        }
        return ext;
    }


    public String multiLangField(MultiLangBean obj, String lang, String prop) {
        String res = "";
        Object langObj = obj.getLanguage(lang);
        if (langObj != null)
            try {
                res = (String) PropertyUtils.getProperty(langObj, prop);
            } catch (IllegalAccessException e) {
                log.error(e.getMessage(), e);
            } catch (InvocationTargetException e) {
                log.error(e.getMessage(), e);
            } catch (NoSuchMethodException e) {
                log.error(e.getMessage(), e);
            }
        return res;
    }

    public String formatNumber(String obj, String format) {
        Double d = SomeUtils.strToDouble(obj);
        return (d != null) ? formatNumber(d, format) : null;
    }

    public boolean isNotEmpty(Double d) {
        return d != null && d > 0.0d;
    }

    public boolean isEmpty(Double d) {
        return !isNotEmpty(d);
    }

    public Object getElement(List col, int index) {
        if (col != null) {
            if (index < 0) index = col.size() + index;
            return (index < col.size()) ? col.get(index) : null;
        }
        return null;
    }

    public String formatNumber(Number obj, String format) {
        if (obj == null) return null;
        if ("decimal2".equalsIgnoreCase(format) || "money".equalsIgnoreCase(format)) {
            DecimalFormatSymbols dfs = new DecimalFormatSymbols();
            dfs.setDecimalSeparator('.');
            DecimalFormat df = new DecimalFormat("0.00", dfs);
            return df.format(obj.doubleValue());
        } else if ("decimal4".equalsIgnoreCase(format)) {
            DecimalFormatSymbols dfs = new DecimalFormatSymbols();
            dfs.setDecimalSeparator('.');
            DecimalFormat df = new DecimalFormat("0.0000", dfs);
            return df.format(obj.doubleValue());
        } else {
            DecimalFormatSymbols dfs = new DecimalFormatSymbols();
            dfs.setDecimalSeparator('.');
            DecimalFormat df = new DecimalFormat(format, dfs);
            return df.format(obj.doubleValue());
        }
    }

    public String formatFileSize(long size) {
        return SomeUtils.formatFileSize(size);
    }

    public String formatDate(String d) {
        String lang = (action != null) ? action.getDefaultLanguage() : "en";
        return formatDate(SomeUtils.strToDate(d, lang), lang);
    }

    public String formatDate(String d, String lang) {
        return SomeUtils.formatDate(SomeUtils.strToDate(d, lang), lang);
    }

    public String formatDate(Date d) {
        String lang = (action != null) ? action.getDefaultLanguage() : "en";
        return formatDate(d, lang);
    }

    public String formatDate(Date d, String lang) {
        return SomeUtils.formatDate(d, lang);
    }

    public String formatTime(Date d) {
        return SomeUtils.formatTime(d);
    }

    public String getSiteUrl() {
        StringBuffer buff = new StringBuffer();
        return buff.append(request.getRequestURL().toString().toLowerCase().startsWith("https") ? "https://" : "http://")
                .append(request.getHeader("Host"))
                .append(request.getContextPath())
                .toString();
    }

    public String getSiteHost() {
        StringBuffer buff = new StringBuffer();
        return buff.append(request.getRequestURL().toString().toLowerCase().startsWith("https") ? "https://" : "http://")
                .append(request.getHeader("Host"))
                .toString();
    }

    public List<List<Object>> getRows(Collection col, int numCol) {
        if (col == null || col.size() < 1) return null;
        List<List<Object>> listaRows = new ArrayList<List<Object>>();
        Iterator it = col.iterator();
        Object obj = (it.hasNext()) ? it.next() : null;
        while (obj != null) {
            List<Object> listaCols = new ArrayList<Object>();
            for (int c = 0; c < numCol; c++) {
                listaCols.add((obj != null) ? obj : "");
                obj = (it.hasNext()) ? it.next() : null;
            }
            listaRows.add(listaCols);
        }
        return listaRows;
    }

    public Object getNull() {
        return null;
    }

    public boolean contains(Collection coll, Object o) {
        return (coll != null) && coll.contains(o);
    }

    public boolean containsAny(Collection coll, String cad) {
        String[] arr = (StringUtils.isNotEmpty(cad)) ? cad.split(",") : null;
        if (coll != null && arr != null && arr.length > 0) {
            for (String s : arr) if (coll.contains(s)) return true;
        }
        return false;
    }

    public String encodeUrl(String cad) {
        String result = null;
        try {
            result = URLEncoder.encode(cad, "UTF-8");
            result = StringUtils.replace(result, "+", "%20");
        } catch (UnsupportedEncodingException e) {
            log.error(e.getMessage(), e);
        }
        return result;
    }

    public Map<String, String> toMap(Map<String, String> map, Collection col, String sep) {
        map.putAll(toMap(col, sep));
        return map;
    }

    public Map<String, String> toMap(Map<String, String> map, Collection<String> col) {
        map.putAll(toMap(col));
        return map;
    }

    public Map<String, String> toMap(Collection<String> col) {
        Map<String, String> result = new HashMap<String, String>();
        if (col != null && !col.isEmpty()) {
            Iterator<String> it = col.iterator();
            while (it.hasNext()) {
                String key = it.next();
                String value = (it.hasNext()) ? it.next() : "";
                if (StringUtils.isNotEmpty(key)) result.put(key, value);
            }
        }
        return result;
    }

    public Map<String, Object> toObjectMap(Collection<Object> col) {
        Map<String, Object> result = new HashMap<String, Object>();
        if (col != null && !col.isEmpty()) {
            Iterator<Object> it = col.iterator();
            while (it.hasNext()) {
                String key = (String) it.next();
                Object value = (it.hasNext()) ? it.next() : "";
                if (StringUtils.isNotEmpty(key)) result.put(key, value);
            }
        }
        return result;
    }

    public VelocityUtils put(String key, Object value) {
        request.setAttribute(key, value);
        return this;
    }

    public Map<String, String> toMap(Collection col, String sep) {
        Map<String, String> result = new HashMap<String, String>();
        for (Object o : col) {
            String[] arr = StringUtils.split(o.toString(), sep, 2);
            if (arr != null && arr.length > 0) result.put(arr[0], (arr.length > 1) ? arr[1] : "");
        }
        return result;
    }

    public List<Object> subList(Collection col, int max) {
        List<Object> result = new ArrayList<Object>();
        if (col != null && max > 0) {
            Iterator it = col.iterator();
            int i = 0;
            while (it.hasNext() && max > i++)
                result.add(it.next());
        }
        return result;
    }

    public List<Object> reverse(Collection col) {
        if (col != null && !col.isEmpty()) {
            List<Object> l = new ArrayList<Object>();
            l.addAll(col);
            Collections.reverse(l);
            return l;
        }
        return null;
    }

    public String reCaptcha() {
        String publicKey = action.getStoreProperty(StoreProperty.RECAPTCHA_PUBLIC, null);
        // add recaptcha
        if (StringUtils.isNotEmpty(publicKey)) {
            return "<div class=\"g-recaptcha\" data-callback=\"captchaClicked\" data-sitekey=\""+publicKey+"\"></div>\n";
        } else {
            return "";
        }
    }

    public String htmlToText(String html) {
        return SomeUtils.extractText(html);
    }

    public ContextMap getMap() {
        return new ContextMap();
    }

    public class ContextMap extends HashMap {

        public ContextMap add(String key, Object obj) {
            this.put(key, obj);
            return this;
        }

    }

    private List<String> ids = new ArrayList<String>();

    public String getId(String prefix) {
        if (StringUtils.isEmpty(prefix)) prefix = "id";
        int i = 1;
        while (ids.contains(prefix + String.valueOf(i))) i++;
        ids.add(prefix + String.valueOf(i));
        return prefix + String.valueOf(i);
    }

    private Long startTime;
    private List<DebuggerInfo> debuggerInfo = new ArrayList<DebuggerInfo>();
    public String debug(String cad) {
        log.debug(cad);
        Long diff = 0l;
        if (startTime==null) {
            startTime = Calendar.getInstance().getTimeInMillis();
            debuggerInfo.add(new DebuggerInfo(cad, 0l));
        } else {
            diff = Calendar.getInstance().getTimeInMillis()-startTime;
            debuggerInfo.add(new DebuggerInfo(cad, diff));
        }
        return "<!-- debug ("+diff.toString()+"): " + cad + " -->";
    }
    public String getDebugger() {
        StringBuilder debugger = new StringBuilder("<table id=\"view-debugger\">");
        Long d = 0l;
        if (!debuggerInfo.isEmpty()) {
            for(DebuggerInfo di : debuggerInfo) {
                debugger.append("<tr><td class=\"time\">")
                        .append(di.getTime())
                        .append("</td><td class=\"desc\">")
                        .append(di.getDesc())
                        .append("</td><td class=\"diff\">")
                        .append(di.getTime()-d)
                        .append("</td></tr>");
                d = di.getTime();
            }
        }
        return debugger.append("</table>").toString();
    }

    public String printQueryStats() {
        Statistics stat;
        try {
            stat = action.getDao().gethSession().getSessionFactory().getStatistics();
        } catch (Exception e) {
            stat = null;
        }

        StringBuilder b = new StringBuilder();
        if (stat != null) {
            String[] arr = stat.getQueries();
            if (arr!=null && arr.length>0) {
                b.append("<table id=\"query-stats\">");
                for (String q : arr) {
                    QueryStatistics qs = stat.getQueryStatistics(q);
                    if (qs!=null) b.append("<tr><td class=\"time\">").append(qs.getExecutionMaxTime()).append("</td><td>").append(q).append("</td></tr>");
                }
                b.append("</table>");
            }
        }
        return b.toString();
    }
    
    public class DebuggerInfo {
        private String desc;
        private Long time;

        public DebuggerInfo(String desc, Long time) {
            this.desc = desc;
            this.time = time;
        }

        public String getDesc() {
            return desc;
        }

        public Long getTime() {
            return time;
        }
    }

}
