package org.store.carriers.capost.common;

import org.apache.log4j.Logger;

import java.util.Properties;

/**
 * Created by IntelliJ IDEA.
 * User: Rogelio Caballero Justo
 * Date: 19-nov-2006
 */
public class CaPostUser {

    public static Logger log = Logger.getLogger(CaPostUser.class);

    private String userId;
    private String password;


    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }


    public CaPostUser(Properties prop) {
        userId = prop.getProperty("user.id");
        password = prop.getProperty("password");
    }

}