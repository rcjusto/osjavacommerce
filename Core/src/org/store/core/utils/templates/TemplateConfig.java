package org.store.core.utils.templates;

import org.store.core.globals.SomeUtils;
import org.apache.commons.digester.Digester;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Rogelio Caballero
 * 16/02/12 23:05
 */
public class TemplateConfig {

    private String code;
    private String name;
    private String store;
    private String edit;

    private List<TemplateBlock> blocks;
    private List<TemplateBannerZone> banners;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStore() {
        return store;
    }

    public void setStore(String store) {
        this.store = store;
    }

    public boolean forStore(String aStore) {
        return (StringUtils.isEmpty(store) || store.equalsIgnoreCase(aStore));
    }

    public String getEdit() {
        return edit;
    }

    public void setEdit(String edit) {
        this.edit = edit;
    }

    public List<TemplateBlock> getBlocks() {
        return blocks;
    }

    public void setBlocks(List<TemplateBlock> blocks) {
        this.blocks = blocks;
    }

    public List<TemplateBannerZone> getBanners() {
        return banners;
    }

    public void setBanners(List<TemplateBannerZone> banners) {
        this.banners = banners;
    }
    
    public void addBlock(String code) {
        if (this.blocks==null) this.blocks = new ArrayList<TemplateBlock>();
        this.blocks.add(new TemplateBlock(code));
    }
    
    public void addBanner(String code, String number, String width, String height) {
        if (this.banners==null) this.banners = new ArrayList<TemplateBannerZone>();
        TemplateBannerZone banner = new TemplateBannerZone(code);
        if (StringUtils.isNotEmpty(number)) banner.setBannersNumber(SomeUtils.strToInteger(number));
        if (StringUtils.isNotEmpty(width)) banner.setBannerWidth(SomeUtils.strToInteger(width));
        if (StringUtils.isNotEmpty(height)) banner.setBannerHeight(SomeUtils.strToInteger(height));
        this.banners.add(banner);
    }

    public TemplateBannerZone getBannerZone(String zone) {
        if (zone!=null && !"".equals(zone) && banners!=null) {
            for(TemplateBannerZone banner : banners)
                if (zone.equalsIgnoreCase(banner.getCode())) return banner;
        }
        return null;
    }
    
    public static TemplateConfig loadFromFile(String code, File file) {
        TemplateConfig result = new TemplateConfig();
        result.setCode(code);
        Digester digester = new Digester();
        digester.push(result);
        digester.addCallMethod("template/name", "setName", 0);
        digester.addCallMethod("template/store", "setStore", 0);
        digester.addCallMethod("template/edit", "setEdit", 0);

        digester.addCallMethod("template/block", "addBlock", 1);
        digester.addCallParam("template/block", 0, "code");

        digester.addCallMethod("template/banner", "addBanner", 4);
        digester.addCallParam("template/banner", 0, "code");
        digester.addCallParam("template/banner", 1, "number");
        digester.addCallParam("template/banner", 2, "width");
        digester.addCallParam("template/banner", 3, "height");

        try {
            digester.parse(file);
            if ( StringUtils.isNotEmpty(result.getCode()))
                return result;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        }

        return null;  
    }
    
    public void saveToFile(File file) {
        StringBuilder buff = new StringBuilder();
        buff.append("<template>\n");
        buff.append("<name>"+ getName() +"</name>\n");
        if (StringUtils.isNotEmpty(getStore())) buff.append("<store>"+getStore()+"</store>\n");
        if (StringUtils.isNotEmpty(getEdit())) buff.append("<edit>"+getEdit()+"</edit>\n");
        for(TemplateBannerZone tbz : getBanners()) {
            buff.append("<banner");
            if (StringUtils.isNotEmpty(tbz.getCode())) buff.append(" code=\"").append(tbz.getCode()).append("\"");
            if (tbz.getBannersNumber()>0) buff.append(" number=\"").append(tbz.getBannersNumber()).append("\"");
            if (tbz.getBannerWidth()>0) buff.append(" width=\"").append(tbz.getBannerWidth()).append("\"");
            if (tbz.getBannerHeight()>0) buff.append(" height=\"").append(tbz.getBannerHeight()).append("\"");
            buff.append("/>\n");
        }
        for(TemplateBlock tb : getBlocks()) if (StringUtils.isNotEmpty(tb.getCode())) {
            buff.append("<block");
            buff.append(" code=\"").append(tb.getCode()).append("\"");
            buff.append("/>\n");
        }
        buff.append("</template>\n");
        try {
            FileUtils.writeStringToFile(file, buff.toString());
        } catch (IOException e) {
            e.printStackTrace(); 
        }
    }
    
}
