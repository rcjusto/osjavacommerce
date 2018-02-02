package org.store.template;

import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;
import org.xhtmlrenderer.css.newmatch.Condition;
import org.xhtmlrenderer.css.newmatch.Selector;
import org.xhtmlrenderer.css.parser.CSSErrorHandler;
import org.xhtmlrenderer.css.parser.CSSParser;
import org.xhtmlrenderer.css.sheet.PropertyDeclaration;
import org.xhtmlrenderer.css.sheet.Ruleset;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Rogelio Caballero
 * 14/01/12 4:21
 */
public class TemplateCssParser {

    public static String parseCssToJSON(File cssFile, String baseUrl) {
        Map map = parseCss(cssFile, baseUrl);
        JSONObject json = new JSONObject(map);
        return json.toString();
    }

    public static String mapToCss(Map<String, Map<String, String>> map) {
        StringBuilder buff = new StringBuilder();
        if (map!=null && !map.isEmpty()) {
            for(Map.Entry<String, Map<String, String>> e : map.entrySet()) {
                buff.append(e.getKey());
                buff.append("{");
                if (e.getValue()!=null && !e.getValue().isEmpty()) {
                    for(Map.Entry<String,String> e1 : e.getValue().entrySet()) {
                        if (StringUtils.isNotEmpty(e1.getValue()) && StringUtils.isNotEmpty(e1.getKey())) buff.append(e1.getKey()).append(":").append(e1.getValue()).append(";");
                    }
                }
                buff.append("}\n");
            }
        }
        return buff.toString();
    }

    public static Map<String, Map<String, String>> parseCss(File cssFile, String baseUrl) {
        CSSErrorHandler errorHandler = new CSSErrorHandler() {
            public void error(String uri, String message) {}
        };

        Map<String, Map<String, String>> result = new HashMap<String, Map<String, String>>();
        try {
            CSSParser p = new CSSParser(errorHandler);
            org.xhtmlrenderer.css.sheet.Stylesheet s = p.parseStylesheet(baseUrl, 0, new FileReader(cssFile));
            if (s.getContents() != null && !s.getContents().isEmpty()) {
                for (Object o : s.getContents()) {
                    if (o instanceof Ruleset) {
                        Ruleset rs = (Ruleset) o;
                        Map<String,String> props = new HashMap<String, String>();
                        if (rs.getPropertyDeclarations() != null && !rs.getPropertyDeclarations().isEmpty()) {
                            for (Object o1 : rs.getPropertyDeclarations()) {
                                if (o1 instanceof PropertyDeclaration) {
                                    PropertyDeclaration pd = (PropertyDeclaration) o1;
                                    if (pd.getPropertyName()!=null && pd.getValue()!=null)
                                        props.put(pd.getPropertyName(), processValue(pd.getPropertyName(), pd.getValue().toString()));
                                }
                            }
                        }
                        if (rs.getFSSelectors() != null && !rs.getFSSelectors().isEmpty()) {
                            for (Object o1 : rs.getFSSelectors()) {
                                if (o1 instanceof Selector) {
                                    Selector selector = (Selector) o1;
                                    result.put(getSelectorName(selector), props);
                                }
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }

    private static String processValue(String propertyName, String value) {
        if ("background-position".equalsIgnoreCase(propertyName)) {
            return StringUtils.replaceEach(value, new String[]{"[","]",","}, new String[]{"","",""});
        }  else {
            return value;
        }
    }

    private static String getSelectorName(Selector selector) {
        StringBuilder buff = new StringBuilder();
        if (selector != null) {
            if (StringUtils.isNotEmpty(selector.getName())) buff.append(selector.getName());
            if (selector.getConditions() != null && !selector.getConditions().isEmpty()) {
                for (Object o : selector.getConditions()) {
                    if (o instanceof Condition) {
                        buff.append(((Condition) o).getValue());
                    }
                }
            }
            if (selector.isPseudoClass(Selector.HOVER_PSEUDOCLASS)) buff.append(":hover");
            if (selector.isPseudoClass(Selector.VISITED_PSEUDOCLASS)) buff.append(":visited");
            if (selector.isPseudoClass(Selector.FOCUS_PSEUDOCLASS)) buff.append(":focus");
            if (selector.getChainedSelector() != null) {
                buff.append(" ").append(getSelectorName(selector.getChainedSelector()));
            }
        }
        return buff.toString();
    }

}
