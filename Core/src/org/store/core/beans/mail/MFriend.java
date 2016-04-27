package org.store.core.beans.mail;

import org.apache.commons.lang.StringUtils;
import org.store.core.beans.UserFriends;
import org.store.core.globals.BaseAction;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Rogelio Caballero
 * 20/07/12 19:40
 */
public class MFriend {

    private UserFriends friend;
    private BaseAction action;

    public MFriend(UserFriends friend, BaseAction action) {
        this.friend = friend;
        this.action = action;
    }

    public String getLinkUrl() {
        Map<String, String> params = new HashMap<String, String>();
        params.put("email", friend.getFriendEmail());
        params.put("secretCode", friend.getSecretCode());
        try {
            return action.urlEnc(action.url("friendRegister", "", params, false));
        } catch (Exception ignored) {
        }
        return null;
    }

    public String getPromotionalImage() {
        String linkUrl = getLinkUrl();
        String path = action.storeFile("images/custom/referral_promo");
        String[] exts = new String[]{"jpg", "jpeg", "gif", "png", "swf"};
        String image = "";
        for (String e : exts)
            if (new File(action.getServletContext().getRealPath(path + "." + e)).exists())
                image = path + "." + e;
        if (StringUtils.isNotEmpty(image)) {
            StringBuilder buff = new StringBuilder();
            buff.append("<a href=\"").append(linkUrl).append("\">").append("<img src=\"")
                    .append(action.getRequest().getRequestURL().toString().toLowerCase().startsWith("https") ? "https://" : "http://")
                    .append(action.getRequest().getHeader("Host")).append(image).append("\" style=\"border:0;\">").append("</a>");
            return buff.toString();
        }
        return "";
    }

}
