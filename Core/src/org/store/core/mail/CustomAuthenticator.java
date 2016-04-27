package org.store.core.mail;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;

/**
 * Created by IntelliJ IDEA.
 * User: lechon
 * Date: 12/09/2005
 * Time: 07:44:10 PM
 * To change this template use File | Settings | File Templates.
 */
public class CustomAuthenticator extends Authenticator {

    private String user;
    private String password;

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public PasswordAuthentication getPasswordAuthentication() {
        return new PasswordAuthentication(getUser(), getPassword());
    }

}
