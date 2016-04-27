package org.store.core.utils.templates;

import org.store.core.globals.BaseAction;
import org.store.core.utils.events.DefaultEventServiceImpl;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import javax.servlet.ServletContext;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Rogelio Caballero
 * 27/12/11 13:12
 */
public class CssProcessingEvent extends DefaultEventServiceImpl {

    public static Logger log = Logger.getLogger(CssProcessingEvent.class);


    @Override
    public String getName() {
        return "CSS Post-processing";
    }

    @Override
    public Boolean beforeAction(ServletContext ctx, BaseAction action) {
        // buscar
        File fSrc = new File(ctx.getRealPath("/stores/" + action.getStoreCode() + "/skins/" + action.getSkin() + "/css/site_skin.css"));
        File fMap = new File(ctx.getRealPath("/stores/" + action.getStoreCode() + "/skins/" + action.getSkin() + "/css/site_skin.properties"));
        File fCss = new File(ctx.getRealPath("/stores/" + action.getStoreCode() + "/skins/" + action.getSkin() + "/css/site.css"));
        if (needToGenerateCss(fSrc, fMap, fCss)) try {
            generateCss(fSrc, fMap, fCss);
        } catch (IOException e) {
            e.printStackTrace();
        }
        action.addToStack("css_version", fCss.lastModified());
        return true;
    }

        private boolean needToGenerateCss(File fSrc, File fMap, File fCss) {
        return fSrc.exists() && fMap.exists() && (!fCss.exists() || (fSrc.lastModified() > fCss.lastModified()) || (fMap.lastModified() > fCss.lastModified()));
    }

    private void generateCss(File fSrc, File fMap, File fCss) throws IOException {
        Map map = new HashMap();
        if (fMap.exists()) {
            Properties p = new Properties();
            p.load(new FileInputStream(fMap));
            map.putAll(p);
        }
        generateCss(fSrc, fCss, map);
    }

    private void generateCss(File fSrc, File fCss, Map<String, String> skinProperties) throws IOException {
        String content = FileUtils.readFileToString(fSrc);
        String[] arrSource = new String[skinProperties.size()];
        String[] arrTarget = new String[skinProperties.size()];
        int index = 0;
        for (Map.Entry e : skinProperties.entrySet()) {
            arrSource[index] = "$" + e.getKey();
            arrTarget[index] = (String) e.getValue();
            index++;
        }
        content = StringUtils.replaceEach(content, arrSource, arrTarget);
        FileUtils.writeStringToFile(fCss, content);
    }

}
