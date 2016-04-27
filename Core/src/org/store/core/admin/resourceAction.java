package org.store.core.admin;

import org.store.core.beans.Resource;
import org.store.core.beans.utils.BreadCrumb;
import org.store.core.beans.utils.DataNavigator;
import org.store.core.globals.SomeUtils;
import org.store.core.globals.StoreMessages;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Namespace(value = "/admin")
@ParentPackage(value = "store-admin")
public class resourceAction extends AdminModuleAction implements StoreMessages {


    @Override
    public void prepare() throws Exception {
        resource = (Resource) dao.get(Resource.class, idResource);
    }

    public String list() throws Exception {
        if (delResources != null && delResources.length > 0) {
            for (Long idRes : delResources)
                if (idRes != null) {
                    String resp = delete(idRes);
                    if (resp != null) addActionError(resp);
                }
        }
        if (getModal()) {
            Map<String, List<Resource>> map = new HashMap<String, List<Resource>>();
            List<Resource> l = dao.getResources(null, filterResourceType, filterResourceName);
            for (Resource r : l) {
                String resourceType = (StringUtils.isNotEmpty(r.getResourceType())) ? r.getResourceType() : "others";
                if (map.containsKey(resourceType)) {
                    List<Resource> ltype = map.get(resourceType);
                    ltype.add(r);
                } else {
                    List<Resource> ltype = new ArrayList<Resource>();
                    ltype.add(r);
                    map.put(resourceType, ltype);
                }
            }
            addToStack("resourceMap", map);
            return "modal";
        } else {
            DataNavigator nav = new DataNavigator(getRequest(), "resources");
            nav.setListado(dao.getResources(nav, filterResourceType, filterResourceName));
            addToStack("resources", nav);
            getResponse().addCookie(nav.getPageRowCookie());
            getBreadCrumbs().add(new BreadCrumb(null, getText("admin.resource.list"), null, null));
          return SUCCESS;
        }
    }

    public String edit() throws Exception {
        getBreadCrumbs().add(new BreadCrumb(null, getText("admin.resource.list"), url("listresource","/admin"), null));
        getBreadCrumbs().add(new BreadCrumb(null, getText(resource!=null ? "admin.resource.modify" : "admin.resource.new"), null, null));
        return SUCCESS;
    }

    @Action(value = "resourcedownload", results = @Result(type = "stream", params = {"allowCaching","false"}))
    public String download() throws Exception {
        if (resource != null && StringUtils.isNotEmpty(resource.getResourceFileName())) {
            StringBuffer newFileName = new StringBuffer();
            if (!StringUtils.isEmpty(storeCode)) newFileName.append("/stores/").append(storeCode);
            newFileName.append(PATH_RESOURCES).append("/").append(resource.getResourceFileName());
            File f = new File(getServletContext().getRealPath(newFileName.toString()));
            if (f.exists()) {
                FileInputStream fis = new FileInputStream(f);
                BufferedInputStream bis = new BufferedInputStream(fis);
                setInputStream(bis);
                setContentDisposition("attachment;filename=\"" + resource.getFileName() + "\"");
            }

        }
        return SUCCESS;
    }

    public String save() throws Exception {
        if (resource != null) {
            resource.setInventaryCode(getStoreCode());
            for (int l = 0; l < getLanguages().length; l++) {
                String lang = getLanguages()[l];
                resource.setResourceName(lang, (resourceName != null && resourceName.length > 0) ? resourceName[l] : "");
                resource.setResourceDescription(lang, (resourceDescription != null && resourceDescription.length > 0) ? resourceDescription[l] : "");
            }
            resource.setResourceDate(SomeUtils.strToDate(resourceDate, getDefaultLanguage()));
            dao.save(resource);
            // save file
            if (resourceFile != null && resourceFile.exists() && StringUtils.isNotEmpty(resourceFileFileName)) {
                resource.setFileName(resourceFileFileName);
                dao.save(resource);
                StringBuffer newFileName = new StringBuffer();
                if (!StringUtils.isEmpty(storeCode)) newFileName.append("/stores/").append(storeCode);
                newFileName.append(PATH_RESOURCES).append("/").append(resource.getId()).append(".").append(FilenameUtils.getExtension(resourceFileFileName));
                String absoluteFilename = getServletContext().getRealPath(newFileName.toString());
                FileUtils.forceMkdir(new File(FilenameUtils.getFullPath(absoluteFilename)));
                File newFile = new File(absoluteFilename);
                if (newFile.exists()) newFile.delete();
                resourceFile.renameTo(newFile);
            }
        }
        return SUCCESS;
    }

    public String delete(Long id) throws Exception {
        Resource res = (Resource) dao.get(Resource.class, id);
        if (res != null) {
            boolean deleted = true;
            StringBuilder newFileName = new StringBuilder();
            if (StringUtils.isNotEmpty(res.getResourceFileName())) {
                if (!StringUtils.isEmpty(storeCode)) newFileName.append("/stores/").append(storeCode);
                newFileName.append(PATH_RESOURCES).append("/").append(res.getResourceFileName());
                File f = new File(getServletContext().getRealPath(newFileName.toString()));
                if (f.exists()) {
                    deleted = f.delete();
                }
            }

            if (deleted) dao.deleteResources(res);
            else return "Can't delete resource file: " + newFileName.toString();
        } else if (id != null) {
            return getText(CNT_ERROR_RESOURCE_NOT_FOUND, CNT_DEFAULT_ERROR_RESOURCE_NOT_FOUND, new String[]{id.toString()});
        }
        return null;
    }

    private Resource resource;
    private Long idResource;
    private Long[] delResources;
    private String[] resourceName;
    private String[] resourceDescription;
    private File resourceFile;
    private String resourceFileFileName;
    private String filterResourceType;
    private String filterResourceName;
    private String resourceDate;

    public String getResourceDate() {
        return resourceDate;
    }

    public void setResourceDate(String resourceDate) {
        this.resourceDate = resourceDate;
    }

    public Resource getResource() {
        return resource;
    }

    public void setResource(Resource resource) {
        this.resource = resource;
    }

    public Long getIdResource() {
        return idResource;
    }

    public void setIdResource(Long idResource) {
        this.idResource = idResource;
    }

    public Long[] getDelResources() {
        return delResources;
    }

    public void setDelResources(Long[] delResources) {
        this.delResources = delResources;
    }

    public String[] getResourceName() {
        return resourceName;
    }

    public void setResourceName(String[] resourceName) {
        this.resourceName = resourceName;
    }

    public String[] getResourceDescription() {
        return resourceDescription;
    }

    public void setResourceDescription(String[] resourceDescription) {
        this.resourceDescription = resourceDescription;
    }

    public File getResourceFile() {
        return resourceFile;
    }

    public void setResourceFile(File resourceFile) {
        this.resourceFile = resourceFile;
    }

    public String getResourceFileFileName() {
        return resourceFileFileName;
    }

    public void setResourceFileFileName(String resourceFileFileName) {
        this.resourceFileFileName = resourceFileFileName;
    }

    public String getFilterResourceType() {
        return filterResourceType;
    }

    public void setFilterResourceType(String filterResourceType) {
        this.filterResourceType = filterResourceType;
    }

    public String getFilterResourceName() {
        return filterResourceName;
    }

    public void setFilterResourceName(String filterResourceName) {
        this.filterResourceName = filterResourceName;
    }
}
