package org.store.core.velocity;

import org.apache.commons.collections.ExtendedProperties;
import org.apache.struts2.ServletActionContext;
import org.apache.struts2.util.ClassLoaderUtils;
import org.apache.struts2.views.velocity.StrutsResourceLoader;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.io.UnicodeInputStream;
import org.apache.velocity.runtime.resource.Resource;
import org.apache.velocity.runtime.resource.loader.ResourceLoader;
import org.apache.velocity.util.StringUtils;
import org.hibernate.Session;
import org.store.core.beans.VMTemplate;
import org.store.core.dao.HibernateDAO;
import org.store.core.globals.BaseAction;
import org.store.core.globals.config.Store20Database;
import org.store.core.hibernate.HibernateSessionFactory;

import java.io.*;

public class StoreVelocityLoader extends ResourceLoader {

    private String path = "";
    private boolean unicode = false;

    public void init(ExtendedProperties configuration) {
        path = configuration.getString("path");
        unicode = configuration.getBoolean("unicode", false);
    }

    @Override
    public InputStream getResourceStream(String templateName) throws ResourceNotFoundException {
        String reducedName = getReducedName(templateName);

        if ((reducedName == null) || (reducedName.length() == 0)) {
            throw new ResourceNotFoundException("No template name provided");
        }

        // find original in db
        InputStream is = getResourceInDB(reducedName);

        // find original in file
        if (is == null) is = getResourceInDisk(templateName);

        if (is == null) is = getResourceInDisk("templates/" +reducedName);

        // find original in jar
        if (is == null) is = getResourceInJar(reducedName);

        // actualizar nombres de templates
        if (is == null) {
            reducedName = getReducedDefaultName(reducedName);
            if (reducedName != null && !"".equals(reducedName)) {
                templateName = "/WEB-INF/views/" + reducedName;

                // find default in db
                is = getResourceInDB(reducedName);

                // find default in file
                if (is == null) is = getResourceInDisk(templateName);

                // find default in jar
                if (is == null) is = getResourceInJar(reducedName);
            }
        }
        if (is==null) {
            log.error("Template not found: " + templateName, new ResourceNotFoundException("Template not found: " + templateName));
        }
        return is;
    }

    @Override
    public boolean isSourceModified(Resource resource) {
        String fullName = resource.getName();
        String reducedName = getReducedName(fullName);
        long lastModified = resource.getLastModified();

        Boolean res = isModifiedInDB(reducedName, lastModified);
        if (res == null) res = isModifiedInDisk(fullName, lastModified);
        if (res == null) res = isModifiedInDisk("templates/" +reducedName, lastModified);
        if (res == null) res = isModifiedInJar(reducedName);
        if (res == null) {
            reducedName = getReducedDefaultName(reducedName);
            if (reducedName != null && !"".equals(reducedName)) {
                fullName = "/WEB-INF/views/" + reducedName;
                res = isModifiedInDB(reducedName, lastModified);
                if (res == null) res = isModifiedInDisk(fullName, lastModified);
                if (res == null) res = isModifiedInJar(reducedName);
            }
        }
        return (res != null) ? res : false;
    }

    @Override
    public long getLastModified(Resource resource) {
        String fullName = resource.getName();
        String reducedName = getReducedName(fullName);

        Long res = lastModifiedInDB(reducedName);
        if (res == null) res = lastModifiedInDisk(fullName);
        if (res == null) res = lastModifiedInDisk("templates/" +reducedName);
        if (res == null) res = lastModifiedInJar(reducedName);
        if (res == null) {
            reducedName = getReducedDefaultName(reducedName);
            if (reducedName != null && !"".equals(reducedName)) {
                fullName = "/WEB-INF/views/" + reducedName;
                res = lastModifiedInDB(reducedName);
                if (res == null) res = lastModifiedInDisk(fullName);
                if (res == null) res = lastModifiedInDB(reducedName);
            }
        }
        return (res != null) ? res : 0l;
    }


    /**
     * *********************************************
     */

    private String getReducedName(String name) {
        return (name.startsWith("/WEB-INF/views/")) ? name.substring(15) : name;
    }

    private String getReducedDefaultName(String reducedName) {
        if ((reducedName.contains("front/") || reducedName.contains("mails/") || reducedName.contains("cntrl/"))) {
            int ind = reducedName.indexOf("/");
            if (ind > 0) {
                StringBuilder defaultName = new StringBuilder();
                defaultName.append("default");
                defaultName.append(reducedName.substring(ind));
                return defaultName.toString();
            }
        }
        return reducedName;
    }

    private VMTemplate getInDB(String name) throws ResourceNotFoundException {
        try {
            HibernateDAO dao = getHibernateDao();
            return dao.getVMTemplateForStore(name, dao.getStoreCode());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private InputStream getResourceInDB(String name) throws ResourceNotFoundException {
        VMTemplate bean = getInDB(name);
        if (bean != null) {
            return new ByteArrayInputStream(bean.getValue().getBytes());
        }
        return null;
    }

    private Boolean isModifiedInDB(String name, long lastModified) {
        VMTemplate bean = getInDB(name);
        if (bean != null && bean.getLastModified() != null) {
            return lastModified != bean.getLastModified().getTime();
        }
        return null;
    }

    private Long lastModifiedInDB(String name) {
        VMTemplate bean = getInDB(name);
        if (bean != null && bean.getLastModified() != null) {
            return bean.getLastModified().getTime();
        }
        return null;
    }

    private HibernateDAO getHibernateDao() throws Exception {
        BaseAction action = (ServletActionContext.getContext().getActionInvocation().getAction() instanceof BaseAction) ? (BaseAction) ServletActionContext.getContext().getActionInvocation().getAction() : null;
        Store20Database databaseConfig = action != null ? action.getDatabaseConfig() : null;
        String store = (action != null) ? action.getStoreCode() : null;
        Session s = HibernateSessionFactory.getSession(databaseConfig);
        return new HibernateDAO(s, store);
    }

    private synchronized InputStream getResourceInDisk(String templateName) throws ResourceNotFoundException {
        String template = StringUtils.normalizePath(templateName);
        if (template == null || template.length() == 0) {
            String msg = "File resource error : argument " + template +
                    " contains .. and may be trying to access content outside of template root.  Rejected.";
            log.error("FileResourceLoader : " + msg);
            throw new ResourceNotFoundException(msg);
        }

        try {
            File file;
            if ("".equals(path)) {
                file = new File(template);
            } else {
                if (template.startsWith("/")) template = template.substring(1);
                file = new File(path, template);
            }

            if (file.canRead()) {
                FileInputStream fis = null;
                try {
                    fis = new FileInputStream(file.getAbsolutePath());
                    if (unicode) {
                        UnicodeInputStream uis = null;
                        try {
                            uis = new UnicodeInputStream(fis, true);
                            return new BufferedInputStream(uis);
                        } catch (IOException e) {
                            if (uis != null) try {
                                uis.close();
                            } catch (IOException ignored) {
                            }
                            throw e;
                        }
                    } else {
                        return new BufferedInputStream(fis);
                    }
                } catch (IOException e) {
                    if (fis != null) try {
                        fis.close();
                    } catch (IOException ignored) {
                    }
                    throw e;
                }
            }
        } catch (IOException ignored) {
        }
        return null;

    }

    private Boolean isModifiedInDisk(String fileName, long lastModified) {
        File file;
        if ("".equals(path)) {
            file = new File(fileName);
        } else {
            if (fileName.startsWith("/")) fileName = fileName.substring(1);
            file = new File(path, fileName);
        }
        if (file.exists() && file.canRead()) {
            return (file.lastModified() != lastModified);
        }
        return null;
    }

    private Long lastModifiedInDisk(String fileName) {
        File file;
        if ("".equals(path)) {
            file = new File(fileName);
        } else {
            if (fileName.startsWith("/")) fileName = fileName.substring(1);
            file = new File(path, fileName);
        }
        if (file.exists() && file.canRead()) {
            return file.lastModified();
        }
        return null;
    }

    private synchronized InputStream getResourceInJar(String name) throws ResourceNotFoundException {
        if (name.startsWith("/")) name = name.substring(1);
        try {
            InputStream is = ClassLoaderUtils.getResourceAsStream(name, StrutsResourceLoader.class);
            if (is != null) return is;
        } catch (Exception ignored) {
        }
        return null;
    }

    private Boolean isModifiedInJar(String name) {
        return (getResourceInJar(name) != null) ? false : null;
    }

    private Long lastModifiedInJar(String name) {
        return (getResourceInJar(name) != null) ? 0l : null;
    }

}