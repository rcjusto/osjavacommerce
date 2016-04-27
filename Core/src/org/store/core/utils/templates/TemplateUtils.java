package org.store.core.utils.templates;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.lang.StringUtils;

import javax.servlet.ServletContext;
import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TemplateUtils {

    public static List<TemplateConfig> getTemplates(ServletContext context, String store) {

        List<TemplateConfig> result = new ArrayList<TemplateConfig>();

        // buscar primero en los directorios
        File folder = new File(context.getRealPath("/templates"));
        File[] subFolders = folder.listFiles((FileFilter) FileFilterUtils.directoryFileFilter());
        if (subFolders != null && subFolders.length > 0) {
            for (File subFolder : subFolders) {
                File[] arrConfig = subFolder.listFiles(new FilenameFilter() {
                    public boolean accept(File dir, String name) {
                        return "template.config".equalsIgnoreCase(name);
                    }
                });
                if (arrConfig != null && arrConfig.length > 0) {
                    TemplateConfig config = TemplateConfig.loadFromFile(FilenameUtils.getBaseName(subFolder.getName()), arrConfig[0]);
                    if (config != null && config.forStore(store)) result.add(config);
                }
            }
        }
        return result;
    }
    
    public static TemplateConfig duplicateTemplate(ServletContext context, String srcCode, String destCode) {
        if (StringUtils.isEmpty(srcCode)) srcCode = "custom";
        File folderSource = new File(context.getRealPath("/templates/"+srcCode));
        if (folderSource.exists() && folderSource.isDirectory()) {
            if (StringUtils.isEmpty(destCode)) destCode = srcCode;
            destCode = getNewTemplateName(context, destCode);
            File folderDest = new File(context.getRealPath("/templates/"+destCode));
            try {
                FileUtils.copyDirectory(folderSource, folderDest);
                return getTemplateConfig(context, destCode);
            } catch (IOException e) {
                e.printStackTrace(); 
            }
        }
        return null;
    }
    
    public static String getNewTemplateName(ServletContext context, String templateName) {
        String result = templateName;
        int index = 1;
        File folder = new File(context.getRealPath("/templates/"+result));
        while (folder.exists()) {
            result = templateName + "_" + String.valueOf(index++); 
            folder = new File(context.getRealPath("/templates/"+result));
        }
        return result;
    }

    public static TemplateConfig getTemplateConfig(ServletContext context, String code) {
        // buscar primero en los directorios
        File file = new File(context.getRealPath("/templates/" + code + "/template.config"));
        TemplateConfig config = (file.exists()) ? TemplateConfig.loadFromFile(code, file) : null;
        if (config == null) {
            // buscar en los jars
        }
        return config;
    }
    
    public static void saveTemplateConfig(ServletContext context, TemplateConfig config) {
        // buscar primero en los directorios
        File file = new File(context.getRealPath("/templates/" + config.getCode() + "/template.config"));
        config.saveToFile(file);
    }

    public static String[] getSkinsForTemplate(ServletContext context, String code) {
        File skinFolder = new File(context.getRealPath("/templates/" + code + "/skins"));
        if (skinFolder.exists() && skinFolder.isDirectory()) {
            return skinFolder.list(FileFilterUtils.directoryFileFilter());
        }
        return null;
    }

}