package org.store.core.admin;

import org.store.core.globals.StoreMessages;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Actions;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.InputStream;

@Namespace(value = "/admin")
@ParentPackage(value = "store-admin")
public class ImgExplorerAction extends AdminModuleAction implements StoreMessages {

    // Image Explorer
     @Actions({
             @Action(value = "imgexplorer", results = @Result(type = "velocity", location = "/WEB-INF/views/admin/imgexplorer.vm")),
             @Action(value = "imglist", results = @Result(type = "velocity", location = "/WEB-INF/views/admin/imgexplorer_list.vm"))
     })
    public String imgList() throws Exception {
        File folder = new File(getServletContext().getRealPath("/stores/" + getStoreCode() + PATH_CUSTOM_IMAGES));
        if (!folder.exists()) FileUtils.forceMkdir(folder);
        if (folder.exists()) {
            String[] extensions;
            if ("img".equalsIgnoreCase(filterType)) {
                extensions = new String[]{".jpg", ".gif", ".png"};
            } else if ("swf".equalsIgnoreCase(filterType)) {
                extensions = new String[]{".swf"};
            } else {
                extensions = new String[]{".jpg", ".gif", ".png", ".swf"};
            }
            addToStack("files", folder.listFiles(new ImageFilter(extensions)));
        }
        return SUCCESS;
    }

    @Action(value = "imgupload", results = @Result(type = "redirectAction", location = "imglist"))
    public String imgUpload() throws Exception {
        String filename = getRequest().getParameter("name");
        if (StringUtils.isNotEmpty(filename)) {
            File newFile = new File(getServletContext().getRealPath("/stores/" + getStoreCode() + PATH_CUSTOM_IMAGES + "/" + filename));
            if (newFile.exists()) newFile.delete();
            InputStream iStream = getRequest().getInputStream();
            BufferedOutputStream fOut = null;
            try {
                fOut = new BufferedOutputStream(new FileOutputStream(newFile));
                byte[] buffer = new byte[32 * 1024];
                int bytesRead = 0;
                while ((bytesRead = iStream.read(buffer)) != -1) {
                    fOut.write(buffer, 0, bytesRead);
                }
            }
            catch (Exception e) {
                log.error(e.getMessage(), e);
            }
            finally {
                if (fOut != null) fOut.close();
                if (iStream != null) iStream.close();
            }
        }
        return SUCCESS;
    }

    @Action(value = "imagesupload", results = {
            @Result(type = "velocity", location = "/WEB-INF/views/admin/imgexplorer_upload.vm"),
            @Result(type = "velocity", name="input", location = "/WEB-INF/views/admin/imgexplorer_upload.vm")
    })
    public String imgUploads() throws Exception {
        if (newImages!=null && newImages.length>0) {
            int index = 0;
            for(int i=0; i<newImages.length; i++) {
                File f = newImages[i];
                String filename = (newImagesFileName!=null && newImagesFileName.length>i) ? newImagesFileName[i] : null;
                if (f.exists() && StringUtils.isNotEmpty(filename)) {
                    File newFile = new File(getServletContext().getRealPath("/stores/" + getStoreCode() + PATH_CUSTOM_IMAGES + "/" + filename));
                    if (newFile.exists()) newFile.delete();
                    f.renameTo(newFile);
                    index++;
                }
            }
            if (index>0) addToStack("upload_products", index);
        }
        return SUCCESS;
    }

    @Override
    @Action(value = "imguploadform", results = @Result(type = "velocity", location = "/WEB-INF/views/admin/imgexplorer_upload.vm"))
    public String emptyMethod() {
        return super.emptyMethod();
    }


    @Action(value = "imgdelete", results = @Result(type = "redirectAction", location = "imglist"))
    public String imgDelete() throws Exception {
        if (StringUtils.isNotEmpty(resourceFileFileName)) {
            File file = new File(getServletContext().getRealPath("/stores/" +  getStoreCode() + PATH_CUSTOM_IMAGES + "/" + resourceFileFileName));
            if (file.exists() && file.isFile()) FileUtils.forceDelete(file);
        }
        return SUCCESS;
    }

    private class ImageFilter implements FilenameFilter {

        private String[] extensions;

        private ImageFilter(String[] extensions) {
            this.extensions = extensions;
        }

        public boolean accept(File dir, String name) {
            if (extensions != null) {
                for (String ext : extensions) if (name.endsWith(ext) || name.endsWith(ext.toUpperCase())) return true;
            }
            return false;
        }
    }

    private String resourceFileFileName;
    private String filterType;
    private File[] newImages;
    private String[] newImagesFileName;
    private File image;
    private String imageFileName;

    public String getImageFileName() {
        return imageFileName;
    }

    public void setImageFileName(String imageFileName) {
        this.imageFileName = imageFileName;
    }

    public File getImage() {
        return image;
    }

    public void setImage(File image) {
        this.image = image;
    }

    public String[] getNewImagesFileName() {
        return newImagesFileName;
    }

    public void setNewImagesFileName(String[] newImagesFileName) {
        this.newImagesFileName = newImagesFileName;
    }

    public File[] getNewImages() {
        return newImages;
    }

    public void setNewImages(File[] newImages) {
        this.newImages = newImages;
    }

    public String getResourceFileFileName() {
        return resourceFileFileName;
    }

    public void setResourceFileFileName(String resourceFileFileName) {
        this.resourceFileFileName = resourceFileFileName;
    }

    public String getFilterType() {
        return filterType;
    }

    public void setFilterType(String filterType) {
        this.filterType = filterType;
    }
}
