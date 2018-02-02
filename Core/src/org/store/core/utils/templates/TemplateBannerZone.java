package org.store.core.utils.templates;

public class TemplateBannerZone extends TemplateBlock {

    private int bannersNumber;
    private int bannerWidth;
    private int bannerHeight;

    public TemplateBannerZone(String code) {
        super(code);
    }

    public int getBannersNumber() {
        return bannersNumber;
    }

    public TemplateBannerZone setBannersNumber(int bannersNumber) {
        this.bannersNumber = bannersNumber;
        return this;
    }

    public int getBannerWidth() {
        return bannerWidth;
    }

    public TemplateBannerZone setBannerWidth(int bannerWidth) {
        this.bannerWidth = bannerWidth;
        return this;
    }

    public int getBannerHeight() {
        return bannerHeight;
    }

    public TemplateBannerZone setBannerHeight(int bannerHeight) {
        this.bannerHeight = bannerHeight;
        return this;
    }

    public TemplateBannerZone setName(String lang, String value) {
        super.setName(lang, value);
        return this;
    }

    public TemplateBannerZone setDescription(String lang, String value) {
        super.setDescription(lang, value);
        return this;
    }

}
