package org.store.core.admin;

import org.store.core.beans.UserLevel;
import org.store.core.beans.utils.BreadCrumb;
import org.store.core.globals.StoreMessages;
import org.store.core.utils.merchants.MerchantService;
import org.store.core.utils.merchants.MerchantUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class customerlevelAction extends AdminModuleAction implements StoreMessages {

    @Override
    public void prepare() throws Exception {
        userLevel = (UserLevel) dao.get(UserLevel.class, idLevel);
    }

    public String list() throws Exception {
        if (selecteds != null && selecteds.length > 0) {
            for (Long id : selecteds) {
                UserLevel bean = (UserLevel) dao.get(UserLevel.class, id);
                if (bean != null) {
                    String res = dao.isUsedUserLevel(bean);
                    if (StringUtils.isNotEmpty(res)) {
                        addActionError(getText(CNT_ERROR_CANNOT_DELETE_USERLEVEL, CNT_DEFAULT_ERROR_CANNOT_DELETE_USERLEVEL, new String[]{bean.getName(getDefaultLanguage()), res}));
                    } else {
                        dao.deleteUserLevel(bean);
                    }
                }
            }
            dao.flushSession();
        }
        getBreadCrumbs().add(new BreadCrumb(null, getText("admin.customer.level.list"), null, null));
        return SUCCESS;
    }

    public String edit() throws Exception {
        List<Map<String, Object>> metodosPago = new ArrayList<Map<String, Object>>();
        MerchantUtils mu = new MerchantUtils(getServletContext());
        Map<String, Class> map = mu.getMerchants();
        if (map != null && !map.isEmpty()) {
            for (String serviceName : map.keySet()) {
                MerchantService service = mu.getService(serviceName, this);
                if (service != null && service.isActive()) {
                    Map<String, Object> m = new HashMap<String, Object>();
                    m.put("name", service.getCode());
                    m.put("label", service.getLabel());
                    metodosPago.add(m);
                }
            }
        }
        addToStack("metodosPago", metodosPago);
        getBreadCrumbs().add(new BreadCrumb(null, getText("admin.customer.level.list"), url("listcustomerlevel","/admin"), null));
        getBreadCrumbs().add(new BreadCrumb(null, getText(userLevel!=null ? "admin.customer.level.modify" : "admin.customer.level.new"), null, null));
      return SUCCESS;
    }

    public String save() throws Exception {
        if (userLevel != null) {
            if (levelName != null && ArrayUtils.isSameLength(getLanguages(), levelName)) {
                for (int i = 0; i < getLanguages().length; i++) {
                    userLevel.setName(getLanguages()[i], levelName[i]);
                }
            }
            userLevel.getPaymentMethods().clear();
            if (paymentMethod != null)
                for (String pm : paymentMethod)
                    userLevel.getPaymentMethods().add(pm);
            userLevel.setInventaryCode(getStoreCode());
            dao.save(userLevel);
        }
        return SUCCESS;
    }

    private UserLevel userLevel;
    private Long idLevel;
    private String[] levelName;
    private String[] paymentMethod;

    public UserLevel getUserLevel() {
        return userLevel;
    }

    public void setUserLevel(UserLevel userLevel) {
        this.userLevel = userLevel;
    }

    public Long getIdLevel() {
        return idLevel;
    }

    public void setIdLevel(Long idLevel) {
        this.idLevel = idLevel;
    }

    public String[] getLevelName() {
        return levelName;
    }

    public void setLevelName(String[] levelName) {
        this.levelName = levelName;
    }

    public String[] getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String[] paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
}
