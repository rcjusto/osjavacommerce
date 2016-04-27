package org.store.core.beans.mail;

import org.store.core.beans.User;
import org.store.core.globals.BaseAction;
import org.store.core.globals.SomeUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Rogelio Caballero
 * 19/07/12 18:14
 */
public class MUser {
    private User user;
    private BaseAction action;

    public User getData() {
        return user;
    }

    public MUser(User user, BaseAction action) {
        this.user = user;
        this.action = action;
    }

    public String getUserId() {
        return user.getUserId();
    }

    public String getEmail() {
        return  user.getEmail();
    }

    public String getFirstname() {
        return  user.getFirstname();
    }

    public String getLastname() {
        return  user.getLastname();
    }

    public String getFullname() {
        return  user.getFullName();
    }

    public String getSex() {
        return  user.getSex();
    }

    public String getTitle() {
        return  user.getTitle();
    }

    public String getCompanyName() {
        return  user.getCompanyName();
    }

    public String getLanguage() {
        return  user.getLanguage();
    }

    public String getPhone() {
        return  user.getPhone();
    }

    public String getWebsite() {
        return  user.getWebsite();
    }

    public String getCancelSubscriptionLink() {
        String url = getCancelSubscriptionUrl();
        StringBuilder b = new StringBuilder();
        b.append("<a href=\"").append(url).append("\" target=\"_blank\">").append(url).append("</a>");
        return b.toString();
    }

    public String getCancelSubscriptionUrl() {
        Map<String, String> map = new HashMap<String, String>();
        map.put("code", user.getAccessCode());
        map.put("act","cancelnewsletter");
        return action.url("useraction","",map,true);
    }

    public String getResetPasswordLink() {
        String url = getResetPasswordUrl();
        StringBuilder b = new StringBuilder();
        b.append("<a href=\"").append(url).append("\" target=\"_blank\">").append(url).append("</a>");
        return b.toString();
    }

    public String getResetPasswordUrl() {
        try {
            Map params = new HashMap();
            params.put("code", SomeUtils.encrypt3Des(user.getUserId(), action.getEncryptionKey()));
            return action.url("resetpassword", "", params, true);
        } catch (Exception ignored) {}
        return "";
    }

}
