package org.store.core.admin;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.json.JSONException;
import org.apache.struts2.json.JSONUtil;
import org.store.core.beans.Banner;
import org.store.core.beans.Category;
import org.store.core.beans.StoreProperty;
import org.store.core.beans.utils.BreadCrumb;
import org.store.core.globals.SomeUtils;
import org.store.core.globals.StoreMessages;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Namespace(value = "/admin")
@ParentPackage(value = "store-admin")
public class bannerAction extends AdminModuleAction implements StoreMessages {


    @Override
    public void prepare() throws Exception {
        banner = (Banner) dao.get(Banner.class, idBanner);
    }

    public String list() throws Exception {
        if (selecteds != null && selecteds.length > 0) {
            for (Long id : selecteds) {
                Banner bean = (Banner) dao.get(Banner.class, id);
                if (bean != null) dao.delete(bean);
            }
        }

        if (StringUtils.isEmpty(bannerZone) && getSession().containsKey("lastBannerZone")) {
            bannerZone = (String) getSession().get("lastBannerZone");
        }
        if (StringUtils.isNotEmpty(bannerZone)) {
            addToStack("banners", dao.getBanners(bannerZone));
        }

        getBreadCrumbs().add(new BreadCrumb(null, getText("admin.banner.list"), null, null));
        return SUCCESS;
    }

    public String edit() throws Exception {
        getBreadCrumbs().add(new BreadCrumb(null, getText("admin.banner.list"), url("listbanner", "/admin"), null));
        if (banner == null) {
            banner = new Banner();
            banner.setBannerZone(bannerZone);
            getBreadCrumbs().add(new BreadCrumb(null, getText("admin.banner.new"), null, null));
        } else {
            getBreadCrumbs().add(new BreadCrumb(null, getText("admin.banner.modify"), null, null));
        }
        return SUCCESS;
    }

    public String save() throws Exception {
        if (banner != null) {
            Category category = dao.getCategory(forCategory);
            banner.setForCategory(category);
            banner.setInventaryCode(getStoreCode());
            dao.save(banner);
            if (bannerImage != null && bannerImage.length == getLanguages().length && bannerImageFileName != null && bannerImageFileName.length == getLanguages().length) {
                for (int i = 0; i < getLanguages().length; i++) {
                    String lang = getLanguages()[i];
                    String fn = bannerImageFileName[i];
                    File f = bannerImage[i];
                    if (f != null && f.exists() && StringUtils.isNotEmpty(fn)) {
                        StringBuffer newFileName = new StringBuffer();
                        newFileName.append("/stores/").append(storeCode);
                        newFileName.append(PATH_BANNERS).append("/").append(banner.getId()).append("_").append(lang).append(".").append(FilenameUtils.getExtension(fn).toLowerCase());
                        String absoluteFilename = getServletContext().getRealPath(newFileName.toString());
                        FileUtils.forceMkdir(new File(FilenameUtils.getFullPath(absoluteFilename)));
                        File newFile = new File(absoluteFilename);
                        if (newFile.exists()) newFile.delete();
                        f.renameTo(newFile);
                    }
                }
            }
            getSession().put("lastBannerZone", banner.getBannerZone());
        }
        return SUCCESS;
    }

    @Action(value = "editSlider", results = @Result(type = "velocity", location = "/WEB-INF/views/admin/edit_slider.vm"))
    public String editSlider() {
        getBreadCrumbs().add(new BreadCrumb(null, getText("admin.banner.list"), url("listbanner", "/admin"), null));
        if (StringUtils.isNotEmpty(bannerZone)) {
            getBreadCrumbs().add(new BreadCrumb(null, getText("admin.slider.configurator"), null, null));
            List<Banner> list = dao.getBanners(bannerZone);
            if (list != null && !list.isEmpty()) {
                Map map = new HashMap();
                for (Banner b : list) map.put(b.getId(), b);
                addToStack("banners", map);
            }
            String data = dao.getStorePropertyValue(bannerZone, SLIDER_CONFIG, null);
            if (StringUtils.isNotEmpty(data)) {
                try {
                    Map map = (Map) JSONUtil.deserialize(data);
                    addToStack("data", map);
                } catch (JSONException e) {
                    log.error(e.getMessage(), e);
                }
            }
        }
        return SUCCESS;
    }

    @Action(value = "saveSlider", results = @Result(type = "redirectAction", location = "listbanner"))
    public String saveSlider() {
        if (StringUtils.isNotEmpty(bannerZone)) {
            Map map = new HashMap();
            if (width != null) map.put("width", width);
            if (height != null) map.put("height", height);
            if (delay != null) map.put("delay", delay);
            List items = new ArrayList();
            if (id != null)
                for (int i = 0; i < id.length; i++) {
                    Banner b = (Banner) dao.get(Banner.class, SomeUtils.strToLong(id[i]));
                    if (b != null) {
                        Map m = new HashMap();
                        m.put("id", b.getId());
                        m.put("effect", StringUtils.isEmpty(effect[i]) ? "random" : effect[i]);
                        items.add(m);
                    }
                }
            if (!items.isEmpty()) map.put("items", items);
            try {
                String data = JSONUtil.serialize(map);
                StoreProperty sp = dao.getStoreProperty(bannerZone, SLIDER_CONFIG, true);
                sp.setValue(data);
                dao.save(sp);
            } catch (JSONException e) {
                log.error(e.getMessage(), e);
            }
        }
        return SUCCESS;
    }

    private Banner banner;
    private Long idBanner;
    private String bannerZone;
    private File[] bannerImage;
    private String[] bannerImageFileName;
    private Long forCategory;

    private Integer width;
    private Integer height;
    private Integer delay;
    private String[] effect;
    private String[] id;

    public Banner getBanner() {
        return banner;
    }

    public void setBanner(Banner banner) {
        this.banner = banner;
    }

    public Long getIdBanner() {
        return idBanner;
    }

    public void setIdBanner(Long idBanner) {
        this.idBanner = idBanner;
    }

    public String getBannerZone() {
        return bannerZone;
    }

    public void setBannerZone(String bannerZone) {
        this.bannerZone = bannerZone;
    }

    public File[] getBannerImage() {
        return bannerImage;
    }

    public void setBannerImage(File[] bannerImage) {
        this.bannerImage = bannerImage;
    }

    public String[] getBannerImageFileName() {
        return bannerImageFileName;
    }

    public void setBannerImageFileName(String[] bannerImageFileName) {
        this.bannerImageFileName = bannerImageFileName;
    }

    public Long getForCategory() {
        return forCategory;
    }

    public void setForCategory(Long forCategory) {
        this.forCategory = forCategory;
    }

    public Integer getWidth() {
        return width;
    }

    public void setWidth(Integer width) {
        this.width = width;
    }

    public Integer getHeight() {
        return height;
    }

    public void setHeight(Integer height) {
        this.height = height;
    }

    public Integer getDelay() {
        return delay;
    }

    public void setDelay(Integer delay) {
        this.delay = delay;
    }

    public String[] getEffect() {
        return effect;
    }

    public void setEffect(String[] effect) {
        this.effect = effect;
    }

    public String[] getId() {
        return id;
    }

    public void setId(String[] id) {
        this.id = id;
    }
}
