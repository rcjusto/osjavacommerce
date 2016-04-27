package org.store.campaigns.admin;

import org.store.campaigns.CampaignDAO;
import org.store.campaigns.CampaignUtils;
import org.store.campaigns.HTMLSanitiser;
import org.store.campaigns.beans.DesignedMail;
import org.store.core.admin.AdminModuleAction;
import org.store.core.beans.Mail;
import org.store.core.beans.Product;
import org.store.core.beans.StoreProperty;
import org.store.core.beans.utils.BreadCrumb;
import org.store.core.beans.utils.DataNavigator;
import org.store.core.globals.ImageResolverImpl;
import org.store.core.globals.StoreMessages;
import org.store.core.mail.MailSenderThreat;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.xhtmlrenderer.simple.Graphics2DRenderer;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Namespace(value= "/admin")
@ParentPackage(value = "store-admin")
public class MailDesignerAction extends AdminModuleAction implements StoreMessages {

    private static final String PATH_MAIL_THUMBNAIL = File.separator + "images" + File.separator + "mails";

    @Override
    public void prepare() throws Exception {
        designedMail = (DesignedMail) dao.get(DesignedMail.class, idDesignedMail);
    }

    @Action(value = "designedmailduplicate", results = {@Result(type="velocity", location="/WEB-INF/views/admin/designedmailedit.vm")})
    public String duplicate() throws Exception {
        DesignedMail sourceDesigned = (DesignedMail) dao.get(DesignedMail.class, idDesignedMail);
        if (sourceDesigned != null) {
            designedMail = new DesignedMail();
            designedMail.setContent(sourceDesigned.getContent());
            designedMail.setInventaryCode(sourceDesigned.getInventaryCode());
            designedMail.setTemplateName(sourceDesigned.getTemplateName());
        }
        return edit();
    }

    @Action(value = "designedmaillist", results = {
            @Result(type = "velocity", location = "/WEB-INF/views/admin/designedmaillist.vm"),
            @Result(type = "velocity", name = "modal", location = "/WEB-INF/views/admin/designedmaillist_modal.vm")
    })
    public String list() throws Exception {
        if (selecteds != null && selecteds.length > 0) {
            for (Long id : selecteds) {
                DesignedMail bean = (DesignedMail) dao.get(DesignedMail.class, id);
                if (bean != null) dao.delete(bean);
            }
            dao.flushSession();
        }

        DataNavigator mails = new DataNavigator(getRequest(), "mails");
        mails.setPageRows(20);
        CampaignDAO campaignDAO = new CampaignDAO(dao);
        mails.setListado(campaignDAO.getDesignedMails(mails));
        addToStack("mails", mails);
        getBreadCrumbs().add(new BreadCrumb(null, getText("admin.campaign.mail.list"), null, null));
        return (getModal()) ? "modal" : SUCCESS;
    }

    @Action(value = "designedmailedit", results = @Result(type = "velocity", location = "/WEB-INF/views/admin/designedmailedit.vm"))
    public String edit() throws Exception {
        // load templates
        File folder = new File(getServletContext().getRealPath("/stores/" + getStoreCode() + "/campaigns/"));
        if (folder.exists()) {
            File[] arrTemplates = folder.listFiles(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return (name.endsWith(".template"));
                }
            });
            if (arrTemplates != null && arrTemplates.length > 0) {
                Map<String, String> map = new HashMap<String, String>();
                for (File f : arrTemplates) {
                    String fileContent = FileUtils.readFileToString(f);
                    if (StringUtils.isNotEmpty(fileContent)) {
                        map.put(FilenameUtils.removeExtension(f.getName()), proccessVelocityText(fileContent, new HashMap<String, Object>()));
                    }
                }
                addToStack("mailtemplates", map);

                // si hay un solo template ya ponerlo por defecto
                if (map.values()!=null && map.values().size()==1 && designedMail ==null) {
                    designedMail = new DesignedMail();
                    designedMail.setContent(map.values().iterator().next());
                }
            }

            File[] arrTemplateRows = folder.listFiles(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return (name.endsWith(".row"));
                }
            });
            if (arrTemplateRows != null && arrTemplateRows.length > 0) {
                Map<String, String> map = new HashMap<String, String>();
                for (File f : arrTemplateRows) {
                    String fileContent = FileUtils.readFileToString(f);
                    if (StringUtils.isNotEmpty(fileContent)) {
                        map.put(FilenameUtils.removeExtension(f.getName()), proccessVelocityText(fileContent, new HashMap<String, Object>()));
                    }
                }
                addToStack("mailtemplaterows", map);
            }

            File[] arrProductLayouts = folder.listFiles(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return (name.endsWith(".prod"));
                }
            });
            if (arrProductLayouts!=null && arrProductLayouts.length>0) {
                List<String> pList = new ArrayList<String>();
                for(File f : arrProductLayouts) pList.add(FilenameUtils.getBaseName(f.getName()));
                addToStack("productLayouts", pList);
            }

        }
        getBreadCrumbs().add(new BreadCrumb(null, getText("admin.campaign.mail.list"), url("designedmaillist", "/admin"), null));
        getBreadCrumbs().add(new BreadCrumb(null, getText(designedMail!=null ? "admin.designed.mail.modify" : "admin.design.new.mail"), null, null));
        return SUCCESS;
    }

    @Action(value = "designedmailsave", results = @Result(type = "redirectAction", location = "designedmailedit?idDesignedMail=${designedMail.id}"))
    public String save() throws Exception {
        if (designedMail != null) {
            dao.save(designedMail);
            // save mail thumbnail
            try {
                StringBuilder newFileName = new StringBuilder();
                if (!StringUtils.isEmpty(storeCode)) newFileName.append("/stores/").append(storeCode);
                newFileName.append(PATH_MAIL_THUMBNAIL).append("/").append(designedMail.getId());
                String absoluteFilename = getServletContext().getRealPath(newFileName.toString());
                FileUtils.forceMkdir(new File(FilenameUtils.getFullPath(absoluteFilename)));

                File fileHtml = new File(FilenameUtils.getFullPath(absoluteFilename) + designedMail.getId().toString() + ".html");
                FileUtils.writeStringToFile(fileHtml, HTMLSanitiser.encodeInvalidMarkup(CampaignUtils.processCampaignMail(designedMail.getContent())), "utf-8");

                if (fileHtml.exists()) {
                    BufferedImage buff = Graphics2DRenderer.renderToImageAutoSize(fileHtml.toURI().toURL().toExternalForm(),740, BufferedImage.TYPE_INT_RGB);

                    File fileJPG = new File(absoluteFilename+".jpg");
                    if (!fileJPG.exists() || fileJPG.delete()) ImageIO.write(ImageResolverImpl.resize(buff,720,1000,null), "jpg", fileJPG);

                    File fileJPGth = new File(absoluteFilename+"_th.jpg");
                    if (!fileJPGth.exists() || fileJPGth.delete()) ImageIO.write(ImageResolverImpl.resize(buff,200,300,null), "jpg", fileJPGth);

                    fileHtml.delete();
                }

            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
        return SUCCESS;
    }



    @Action(value = "designedmailproduct", results = @Result(type = "stream", params = {"allowCaching","false","contentType","text/html"}))
    public String product() throws Exception {
        Product product = (Product) dao.get(Product.class, idProduct);
        if (product != null) {
            File templateFile = null;
            if (StringUtils.isNotEmpty(template)) templateFile = new File(getServletContext().getRealPath("/stores/" + getStoreCode() + "/campaigns/"+template+".prod"));
            if (templateFile==null || !templateFile.exists()) templateFile = new File(getServletContext().getRealPath("/stores/" + getStoreCode() + "/campaigns/default.prod"));
            if (templateFile.exists()) {
                Map<String, String> params = new HashMap<String, String>();
                params.put("code", product.getUrlCode());
                Map<String, Object> map = new HashMap<String, Object>();
                map.put("urlDetail", url("product", "", params, true));
                map.put("image", getImageResolver().getImageForProduct(product, "list/"));
                map.put("product", product);
                String text = FileUtils.readFileToString(templateFile);
                String content = proccessVelocityText(text, map);
                setInputStream(new ByteArrayInputStream(content.getBytes("utf-8")));
            }
        }
        return SUCCESS;
    }

    @Action(value = "designedmailtest")
    public String test() throws Exception {
        if (StringUtils.isNotEmpty(testEmailAddress) && StringUtils.isNotEmpty(testEmailBody)) {
            if (StringUtils.isEmpty(testEmailSubject)) testEmailSubject = getStoreProperty(StoreProperty.PROP_SITE_NAME, "");
            testEmailBody = CampaignUtils.processCampaignMail(testEmailBody);
            Mail mail = new Mail();
            mail.setFromAddress(getStoreProperty(StoreProperty.PROP_MAIL_FRONT_NEWSLETTER, null));
            mail.setBody(testEmailBody);
            mail.setToAddress(testEmailAddress);
            mail.setSubject(testEmailSubject);
            mail.setInventaryCode(getStoreCode());
            dao.save(mail);
            if (MailSenderThreat.sendPendingMail(mail, this)) getResponse().getWriter().print("ok");
            else getResponse().getWriter().print("error");
            dao.save(mail);
        }
        return null;
    }

    private DesignedMail designedMail;
    private Long idDesignedMail;
    private Long idProduct;
    private String template;

    private String testEmailAddress;
    private String testEmailSubject;
    private String testEmailBody;


    public String getTemplate() {
        return template;
    }

    public void setTemplate(String template) {
        this.template = template;
    }

    public DesignedMail getDesignedMail() {
        return designedMail;
    }

    public void setDesignedMail(DesignedMail designedMail) {
        this.designedMail = designedMail;
    }

    public Long getIdDesignedMail() {
        return idDesignedMail;
    }

    public void setIdDesignedMail(Long idDesignedMail) {
        this.idDesignedMail = idDesignedMail;
    }

    public Long getIdProduct() {
        return idProduct;
    }

    public void setIdProduct(Long idProduct) {
        this.idProduct = idProduct;
    }

    public String getTestEmailAddress() {
        return testEmailAddress;
    }

    public void setTestEmailAddress(String testEmailAddress) {
        this.testEmailAddress = testEmailAddress;
    }

    public String getTestEmailBody() {
        return testEmailBody;
    }

    public void setTestEmailBody(String testEmailBody) {
        this.testEmailBody = testEmailBody;
    }

    public String getTestEmailSubject() {
        return testEmailSubject;
    }

    public void setTestEmailSubject(String testEmailSubject) {
        this.testEmailSubject = testEmailSubject;
    }
}
