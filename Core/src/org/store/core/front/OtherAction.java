package org.store.core.front;

import com.octo.captcha.service.CaptchaServiceException;
import com.opensymphony.xwork2.ActionContext;
import org.store.core.beans.StoreProperty;
import org.store.core.globals.CaptchaServiceSingleton;
import org.store.core.globals.SomeUtils;
import org.store.core.globals.StoreActionMapping;
import org.store.core.utils.events.EventService;
import org.store.core.utils.events.EventUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.struts2.ServletActionContext;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;


public class OtherAction extends FrontModuleAction {

    public String unknownAction() throws Exception {
        ActionContext context = ActionContext.getContext();
        StoreActionMapping mapping = (StoreActionMapping) context.get(ServletActionContext.ACTION_MAPPING);
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("actionName", mapping.getName()); // por compatibilidad
        map.put("actionMapping", mapping);
        
        if (EventUtils.executeEvent(getServletContext(), EventService.EVENT_CUSTOM_ACTION, this, map)) {
            if (map.containsKey("result")) {
                return (String) map.get("result");
            } else if (map.containsKey("redirectUrl") && map.get("redirectUrl") instanceof String && StringUtils.isNotEmpty((String) map.get("redirectUrl"))) {
                redirectUrl = (String) map.get("redirectUrl");
                return "redirectUrl";
            } else {
                return null;
            }
        } else {
            return SUCCESS;
        }
    }

    public String captcha() throws Exception {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            String captchaId = getStoreCode() + getRequest().getSession().getId();
            BufferedImage image = CaptchaServiceSingleton.getInstance(this).getImageChallengeForID(captchaId, getLocale());
            ImageIO.write(image, "jpeg", os);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        setInputStream(new ByteArrayInputStream(os.toByteArray()));
        return SUCCESS;
    }

    public String validCaptcha() throws Exception {
        /*
        boolean isCaptchaCorrect = false;
        String captchaId = getStoreCode() + getRequest().getSession().getId();
        try {
            isCaptchaCorrect = CaptchaServiceSingleton.getInstance(this).validateResponseForID(captchaId, captcha);
        } catch (CaptchaServiceException e) {
            log.error(e.getMessage(), e);
        }
        */

        result = "ok";
        String privateKey = getStoreProperty(StoreProperty.RECAPTCHA_PRIVATE, null);
        if (StringUtils.isNotEmpty(privateKey)) {
            String reCaptchaResponse = request.getParameter("g-recaptcha-response");
            if (!SomeUtils.reCaptcha2(privateKey, request.getRemoteAddr(), reCaptchaResponse)) {
                result = "wrong";
            }
        }
        return SUCCESS;
    }

    private String captcha;
    private String result;

    public String getCaptcha() {
        return captcha;
    }

    public void setCaptcha(String captcha) {
        this.captcha = captcha;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }
}
