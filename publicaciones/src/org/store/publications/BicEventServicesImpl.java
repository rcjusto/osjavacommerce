package org.store.publications;

import au.com.bytecode.opencsv.CSVReader;
import org.store.core.admin.AdminModuleAction;
import org.store.core.beans.Category;
import org.store.core.beans.CategoryLang;
import org.store.core.beans.Codes;
import org.store.core.beans.utils.DataNavigator;
import org.store.core.front.FrontModuleAction;
import org.store.core.globals.SomeUtils;
import org.store.core.utils.events.DefaultEventServiceImpl;
import org.store.core.utils.events.EventService;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.mozilla.universalchardet.UniversalDetector;

import javax.servlet.ServletContext;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;

/**
 * Rogelio Caballero
 * 23/10/11 0:47
 */
public class BicEventServicesImpl extends DefaultEventServiceImpl {

    public static Logger log = Logger.getLogger(BicEventServicesImpl.class);
    private static final String BIC_LIST_ACTION = "biclistaction";
    private static final String BIC_EDIT_ACTION = "biceditaction";
    private static final String BIC_SAVE_ACTION = "bicsaveaction";
    private static final String BIC_IMPORT_ACTION = "bicimportaction";
    private static final String BIC_LEVELS_ACTION = "biclevelsaction";

    public String getName() {
        return "BIC";
    }

    public String getDescription(FrontModuleAction action) {
        return "BIC - Sistema de clasificacion de materias";
    }

    public Boolean onExecuteAdminEvent(ServletContext ctx, int eventType, AdminModuleAction action, Map<String, Object> map) {
        if (EventService.EVENT_CUSTOM_ACTION == eventType && map.containsKey("actionName")) {
            if (BIC_LIST_ACTION.equalsIgnoreCase((String) map.get("actionName"))) {
                list(action);
                action.setVelocityView("/WEB-INF/views/org/store/publications/views/bic_list.vm");
                map.put("result", "view");
                return true;
            } else if (BIC_EDIT_ACTION.equalsIgnoreCase((String) map.get("actionName"))) {
                edit(action);
                action.setVelocityView("/WEB-INF/views/org/store/publications/views/bic_edit.vm");
                map.put("result", "view");
                return true;
            } else if (BIC_SAVE_ACTION.equalsIgnoreCase((String) map.get("actionName"))) {
                save(action);
                map.put("redirectUrl", action.url(BIC_LIST_ACTION, "admin"));
                return true;
            } else if (BIC_IMPORT_ACTION.equalsIgnoreCase((String) map.get("actionName"))) {
                importAction(action);
                map.put("redirectUrl", action.url(BIC_LIST_ACTION, "admin"));
                return true;
            } else if (BIC_LEVELS_ACTION.equalsIgnoreCase((String) map.get("actionName"))) {
                levelsAction(action);
                map.put("redirectUrl", action.url(BIC_LIST_ACTION, "admin"));
                return true;
            }
        }
        return null;
    }

    private void levelsAction(AdminModuleAction action) {
        Integer level = SomeUtils.strToInteger(action.getRequest().getParameter("level"));
        String levelAction = action.getRequest().getParameter("actionLevel");
        if ("enable".equalsIgnoreCase(levelAction) && level!=null && level>1) {
            List<Codes> list = action.getDao().createCriteriaForStore(Codes.class).add(Restrictions.eq("type", Codes.TYPE_BIC)).list();
            if (list!=null && !list.isEmpty()) {
                for(Codes codes : list) {
                    if (codes.getCode()!=null && codes.getCode().length()==level && !codes.getActive()) {
                        codes.setActive(true);
                        action.getDao().save(codes);
                        action.getDao().flushSession();
                        action.getDao().evict(codes);
                    }
                }
            }
        } else if ("disable".equalsIgnoreCase(levelAction) && level!=null && level>1) {
            List<Codes> list = action.getDao().createCriteriaForStore(Codes.class).add(Restrictions.eq("type", Codes.TYPE_BIC)).list();
            if (list!=null && !list.isEmpty()) {
                for(Codes codes : list) {
                    if (codes.getCode()!=null && codes.getCode().length()==level && codes.getActive()) {
                        codes.setActive(false);
                        action.getDao().save(codes);
                        action.getDao().flushSession();
                        action.getDao().evict(codes);
                    }
                }
            }
        }
    }

    private void importAction(AdminModuleAction action) {

        if (action.getUploads() != null && action.getUploads().length > 0) {
            File f = action.getUploads()[0];
            if (f.exists()) {
                try {
                    // try to detect encoding of csv
                    InputStream fis = new FileInputStream(f);
                    byte[] buf = new byte[4096];
                    UniversalDetector detector = new UniversalDetector(null);
                    int nread;
                    while ((nread = fis.read(buf)) > 0 && !detector.isDone()) detector.handleData(buf, 0, nread);
                    detector.dataEnd();
                    String encoding = detector.getDetectedCharset();
                    if (encoding == null) encoding = "ISO-8859-1";
                    fis.close();
                    // import csv
                    InputStreamReader isr = new InputStreamReader(new FileInputStream(f), encoding);
                    CSVReader reader = new CSVReader(isr);
                    String[] nextLine = reader.readNext(); //header
                    while ((nextLine = reader.readNext()) != null) {
                        if (nextLine.length > 1) {
                            String code = nextLine[0];
                            String value = nextLine[1];

                            if (StringUtils.isNotEmpty(code) && StringUtils.isNotEmpty(value)) {
                                List l = action.getDao().createCriteriaForStore(Codes.class)
                                        .add(Restrictions.eq("type", Codes.TYPE_BIC))
                                        .add(Restrictions.eq("code", code)).list();

                                if (l == null || l.isEmpty()) {
                                    Codes codes = new Codes();
                                    codes.setType(Codes.TYPE_BIC);
                                    codes.setCode(code);
                                    codes.setActive(true);
                                    for (String lang : action.getLanguages()) codes.setText(lang, value);
                                    action.getDao().save(codes);
                                    action.getDao().evict(codes);
                                }
                            }
                        }
                    }
                    reader.close();
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
        }
    }

    private void list(AdminModuleAction action) {
        // remove selected elements
        String[] selecteds = action.getRequest().getParameterValues("selecteds");
        if (selecteds != null && selecteds.length > 0) {
            for (String id : selecteds) {
                Codes bean = (Codes) action.getDao().get(Codes.class, SomeUtils.strToLong(id));
                if (bean != null) action.getDao().delete(bean);
            }
            action.getDao().flushSession();
        }

        // get list
        DataNavigator bics = new DataNavigator(action.getRequest(), "bics");
        Criteria cri = action.getDao().createCriteriaForStore(Codes.class).add(Restrictions.eq("type", Codes.TYPE_BIC));

        String filterCode = action.getRequest().getParameter("filterCode");
        if (StringUtils.isNotEmpty(filterCode)) {
            action.addToStack("filterCode", filterCode);
            cri.add(Restrictions.like("code", filterCode, MatchMode.START));
        }

        String filterActive = action.getRequest().getParameter("filterActive");
        if (StringUtils.isEmpty(filterActive)) filterActive = "all";
        action.addToStack("filterActive", filterActive);
        if ("active".equalsIgnoreCase(filterActive)) cri.add(Restrictions.eq("active", Boolean.TRUE));


        cri.setProjection(Projections.countDistinct("id"));
        Number total = (Number) cri.uniqueResult();
        bics.setTotalRows(total != null ? total.intValue() : 0);
        cri.setProjection(null);
        cri.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        cri.addOrder(Order.asc("code"));
        if (bics.getTotalRows() > 0) {
            cri.setMaxResults(bics.getPageRows());
            cri.setFirstResult(bics.getFirstRow() - 1);
        }
        bics.setListado(cri.list());
        action.addToStack("bics", bics);
    }

    private void edit(AdminModuleAction action) {
        Long id = SomeUtils.strToLong(action.getRequest().getParameter("id"));
        Codes codes = (Codes) action.getDao().get(Codes.class, id);
        if (codes != null) action.addToStack("codes", codes);
    }

    private void save(AdminModuleAction action) {
        Long id = SomeUtils.strToLong(action.getRequest().getParameter("id"));
        String code = action.getRequest().getParameter("code");
        String[] texts = action.getRequest().getParameterValues("text");
        String active = action.getRequest().getParameter("active");
        Codes codes = (Codes) action.getDao().get(Codes.class, id);
        if (codes==null) codes = new Codes();
        codes.setCode(code);
        codes.setActive("true".equalsIgnoreCase(active) || code.length()==1);
        String[] languages = action.getLanguages();
        for(int i=0; i<languages.length; i++) codes.setText(languages[i], (texts!=null && texts.length>i) ? texts[i] : "");
        action.getDao().save(codes);

        // update category
        Category cat = action.getDao().getCategoryByExternalCode(code);
        if (cat!=null) {
            for(String lang : languages) {
                CategoryLang catlang = cat.getLanguage(lang);
                if (catlang!=null && StringUtils.isNotEmpty(codes.getText(lang))) {
                    catlang.setCategoryName(codes.getText(lang));
                    action.getDao().save(catlang);
                }
            }
        }
    }


}
