package org.store.core.admin;

import org.store.core.beans.Manufacturer;
import org.store.core.beans.utils.BreadCrumb;
import org.store.core.beans.utils.DataNavigator;
import org.store.core.globals.StoreMessages;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;

import java.io.File;
import java.util.List;

@Namespace(value = "/admin")
@ParentPackage(value = "store-admin")
public class manufacturerAction extends AdminModuleAction implements StoreMessages {

    @Override
    public void prepare() throws Exception {
        manufacturer = (Manufacturer) dao.get(Manufacturer.class, idManufacturer);
    }

    public String list() throws Exception {
        if (selecteds != null && selecteds.length > 0) {
            for (Long id : selecteds) {
                Manufacturer bean = (Manufacturer) dao.get(Manufacturer.class, id);
                if (bean != null) {
                    String res = dao.isUsedManufacturer(bean);
                    if (StringUtils.isNotEmpty(res)) {
                        addActionError(getText(CNT_ERROR_CANNOT_DELETE_MANUFACTURER, CNT_DEFAULT_ERROR_CANNOT_DELETE_MANUFACTURER, new String[]{bean.getManufacturerName(), res}));
                    } else {
                        dao.deleteManufacturer(bean);
                    }
                }
            }
            dao.flushSession();
        }

        DataNavigator manufacturers = new DataNavigator(getRequest(), "manufacturers");
        manufacturers.setListado(dao.getManufacturers(manufacturers, filterName));
        addToStack("manufacturers", manufacturers);
        getBreadCrumbs().add(new BreadCrumb(null, getText("admin.manufacturer.list"), null, null));
        return SUCCESS;
    }

    public String edit() throws Exception {
        getBreadCrumbs().add(new BreadCrumb(null, getText("admin.manufacturer.list"), url("listmanufacturer", "/admin"), null));
        getBreadCrumbs().add(new BreadCrumb(null, getText(manufacturer != null ? "admin.manufacturer.modify" : "admin.manufacturer.new"), null, null));
        return SUCCESS;
    }

    public String save() throws Exception {
        if (manufacturer != null) {
            manufacturer.setInventaryCode(getStoreCode());
            dao.updateManufacturerUrlCode(manufacturer, manufacturer.getUrlCode());
            dao.save(manufacturer);
            if (manufacturerImage != null && manufacturerImage.exists() && StringUtils.isNotEmpty(manufacturerImageFileName)) {
                StringBuffer newFileName = new StringBuffer();
                if (!StringUtils.isEmpty(storeCode)) newFileName.append("/stores/").append(storeCode);
                newFileName.append(PATH_MANUFACTURERS).append("/").append(manufacturer.getUrlCode()).append(".").append(FilenameUtils.getExtension(manufacturerImageFileName));
                String absoluteFilename = getServletContext().getRealPath(newFileName.toString());
                FileUtils.forceMkdir(new File(FilenameUtils.getFullPath(absoluteFilename)));
                File newFile = new File(absoluteFilename);
                if (newFile.exists()) newFile.delete();
                manufacturerImage.renameTo(newFile);
            }
        }
        return SUCCESS;
    }

    @Action(value = "manufacturerupdateurl")
    public String updateManufacturerCodeNames() throws Exception {
        int oks = 0;
        int errors = 0;
        List<Manufacturer> listado = dao.getManufacturers();

        for (Manufacturer bean : listado) {
            try {
                if (dao.updateManufacturerUrlCode(bean, null)) oks++;
                else errors++;
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
        response.getWriter().println("Manufacter OK: " + String.valueOf(oks));
        response.getWriter().println("Manufacter Error: " + String.valueOf(errors));
        return null;
    }

    @Action(value = "manufacturersearch") 
    public String quickSearch() throws Exception {
        
        return SUCCESS;
    }

    private Manufacturer manufacturer;
    private Long idManufacturer;
    private File manufacturerImage;
    private String manufacturerImageFileName;
    private String filterName;

    public Manufacturer getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(Manufacturer manufacturer) {
        this.manufacturer = manufacturer;
    }

    public Long getIdManufacturer() {
        return idManufacturer;
    }

    public void setIdManufacturer(Long idManufacturer) {
        this.idManufacturer = idManufacturer;
    }

    public File getManufacturerImage() {
        return manufacturerImage;
    }

    public void setManufacturerImage(File manufacturerImage) {
        this.manufacturerImage = manufacturerImage;
    }

    public String getManufacturerImageFileName() {
        return manufacturerImageFileName;
    }

    public void setManufacturerImageFileName(String manufacturerImageFileName) {
        this.manufacturerImageFileName = manufacturerImageFileName;
    }

    public String getFilterName() {
        return filterName;
    }

    public void setFilterName(String filterName) {
        this.filterName = filterName;
    }
}
