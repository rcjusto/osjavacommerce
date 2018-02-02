package org.store.campaigns.front;

import org.store.campaigns.CampaignUtils;
import org.store.campaigns.beans.Campaign;
import org.store.campaigns.beans.DesignedMail;
import org.store.campaigns.beans.CampaignUser;
import org.store.core.front.FrontModuleAction;
import org.apache.commons.lang.StringUtils;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;

import java.io.ByteArrayInputStream;
import java.util.Calendar;

/**
 * Rogelio Caballero
 * 25/12/11 16:53
 */
@Namespace(value="/")
@ParentPackage(value = "store-front")
public class CampaignMailAction extends FrontModuleAction {

    @Action(value = "campaignmailcontent", results = @Result(type = "stream", params = {"allowCaching","false","contentType","text/html"}))
    public String content() throws Exception {
        DesignedMail bean = (DesignedMail) dao.get(DesignedMail.class, id);
        String content;
        if (bean!=null) {
            content = CampaignUtils.processCampaignMail(bean.getContent());
            if (StringUtils.isEmpty(content)) content = "Mail is empty";
        } else {
            content = "Mail not found";
        }

        setInputStream(new ByteArrayInputStream(content.getBytes("utf-8")));
        return SUCCESS;
    }

    @Action(value = "campaigncontent", results = @Result(type = "stream", params = {"allowCaching","false","contentType","text/html"}))
    public String contentCampaign() throws Exception {
        Campaign bean = (Campaign) dao.get(Campaign.class, id);
        String content;
        if (bean!=null) {
            content = CampaignUtils.processCampaignMail(bean.getCampaignMail());
            if (StringUtils.isEmpty(content)) content = "Mail is empty";
        } else {
            content = "Mail not found";
        }

        setInputStream(new ByteArrayInputStream(content.getBytes("utf-8")));
        return SUCCESS;
    }

    private final byte[] blankGif = {71,73,70,56,57,97,1,0,1,0,-128,0,0,-1,-1,-1,0,0,0,33,-7,4,1,0,0,0,0,44,0,0,0,0,1,0,1,0,0,2,2,68,1,0,59};
    @Action(value = "campaignopened", results = @Result(type = "stream", params = {"allowCaching","false","contentType","image/gif"}))
    public String contentOpened() throws Exception {
        CampaignUser bean = (CampaignUser) dao.get(CampaignUser.class, id);
        if (bean!=null && bean.getOpened()==null) {
            bean.setOpened(Calendar.getInstance().getTime());
            dao.save(bean);
        }
        setInputStream(new ByteArrayInputStream(blankGif));
        return SUCCESS;
    }


    private Long id;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
