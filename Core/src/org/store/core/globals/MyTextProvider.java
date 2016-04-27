package org.store.core.globals;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.DefaultTextProvider;
import org.store.core.dao.HibernateDAO;
import org.apache.log4j.Logger;

import java.text.MessageFormat;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Created by IntelliJ IDEA.
 * User: Administrador
 * Date: Mar 29, 2010
 */
public class MyTextProvider extends DefaultTextProvider {

    public static Logger log = Logger.getLogger(MyTextProvider.class);
    private static final Object[] EMPTY_ARGS = new Object[0];

    public String getText(String key) {
        if (key != null && !"".equals(key))
            try {
                HibernateDAO dao = new HibernateDAO();
                if (ActionContext.getContext().getActionInvocation()!=null && ActionContext.getContext().getActionInvocation().getAction()!=null && ActionContext.getContext().getActionInvocation().getAction() instanceof BaseAction) {
                    dao.setStoreCode(((BaseAction) ActionContext.getContext().getActionInvocation().getAction()).getStoreCode());
                }
                return dao.getText(key, ActionContext.getContext().getLocale().getLanguage());
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        return null;
    }

    public String getText(String key, String[] args) {
        String defaultText = getText(key);

        Object[] params;
        if (args != null) {
            params = args;
        } else {
            params = EMPTY_ARGS;
        }
        if (defaultText != null) {
            MessageFormat format = new MessageFormat(defaultText);
            format.setLocale(ActionContext.getContext().getLocale());
            format.applyPattern(defaultText);
            return format.format(params);
        }
        return null;
    }

    public String getText(String key, List<?> args) {
        String defaultText = getText(key);
        Object[] params;
        if (args != null) {
            params = args.toArray();
        } else {
            params = EMPTY_ARGS;
        }

        if (defaultText != null) {
            MessageFormat format = new MessageFormat(defaultText);
            format.setLocale(ActionContext.getContext().getLocale());
            format.applyPattern(defaultText);
            return format.format(params);
        }
        return null;
    }



    public ResourceBundle getTexts(String bundleName) {
        return null;
    }


}
