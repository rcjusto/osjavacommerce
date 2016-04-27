package org.store.importexport;

import com.opensymphony.xwork2.validator.validators.EmailValidator;
import org.store.importexport.admin.ImportProductsAction;
import org.store.core.beans.StoreProperty;
import org.store.core.beans.User;
import org.store.core.beans.UserLevel;
import org.store.core.beans.utils.TableFile;
import org.store.core.dao.HibernateDAO;
import org.store.core.globals.BaseAction;
import org.store.core.globals.SomeUtils;
import org.store.core.hibernate.HibernateSessionFactory;
import org.apache.commons.lang.StringUtils;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Rogelio Caballero
 * 12/04/12 15:55
 */
public class ImportCustomersThread extends ImportFileThread {

    public ImportCustomersThread(BaseAction action) {
        super(action);
    }

    @Override
    public void run() {
        TableFile tableFile = getTableFile(ImportProductsAction.TYPE_PRODUCT);
        Map map;
        if (tableFile.getRows() != null && !tableFile.getRows().isEmpty()) {
            long index = 0, total = tableFile.getRows().size(), inserted = 0, modified = 0;
            try {
                Session session = HibernateSessionFactory.getNewSession(databaseConfig);
                Transaction tx = session.beginTransaction();
                HibernateDAO dao = new HibernateDAO(session, storeCode);
                try {
                    addOutputMessage("Start time: " + Calendar.getInstance().getTime().toString());

                    for (TableFile.TableFileRow row : tableFile.getRows()) {
                        setExecutionMessage("Importing row " + (index++) + " of " + total);
                        setExecutionPercent(index * 100d / total);
                        map = getRow(row, getFields());
                        String error = validateCustomerMap(map);
                        if (StringUtils.isNotEmpty(error)) addOutputMessage("Error: " + error);
                        else {
                            if (importCustomer(map, dao)) inserted++;
                            else modified++;
                        }
                    }

                    addOutputMessage("End time: " + Calendar.getInstance().getTime().toString());
                    addOutputMessage("New customers: " + String.valueOf(inserted));
                    addOutputMessage("Updated customers: " + String.valueOf(modified));

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    tx.rollback();
                    addOutputMessage("ERROR: " + e.getMessage());
                } finally {
                    if (session.isOpen()) session.close();
                }

                saveLogFile();
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    private boolean importCustomer(Map<String, String> map, HibernateDAO dao) {
        boolean inserted = false;
        String email = (map.containsKey("email")) ? map.get("email") : null;
        User u = dao.getUserByEmail(email);
        if (u == null) {
            u = new User();
            u.setInventaryCode(getStoreCode());
            u.setRegisterDate(Calendar.getInstance().getTime());
            inserted = true;
        }

        Map<String, String> mapCustomer = new HashMap<String, String>();
        UserLevel level;
        for (String key : map.keySet()) {
            if (key.startsWith("level.")) {
                if ("level.code".equals(key)) {
                    if (StringUtils.isNotEmpty(map.get(key))) {
                        level = dao.getUserLevel(map.get(key));
                        if (level == null) {
                            level = new UserLevel();
                            level.setInventaryCode(getStoreCode());
                            level.setCode(map.get(key));
                            for (String lang : dao.getLanguages()) level.setName(lang, map.get(key));
                            dao.save(level);
                        }
                        u.setLevel(level);
                    }
                }
            } else mapCustomer.put(key, map.get(key));
        }
        u.fromMap(mapCustomer);
        if (StringUtils.isEmpty(u.getUserId())) {
            String uId = (StringUtils.isNotEmpty(u.getEmail())) ? u.getEmail() : User.generatePassword(8);
            while (dao.getUserByUserId(uId) != null) {
                uId = User.generatePassword(8);
            }
            u.setUserId(uId);
        }
        if (StringUtils.isEmpty(u.getPassword())) try {
            u.setPassword(SomeUtils.encrypt3Des(User.generatePassword(8), getEncryptionKey(dao)));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        dao.save(u);

        dao.flushSession();
        dao.evict(u);
        return inserted;
    }

    private Map<String,String> getRow(TableFile.TableFileRow row, List<String> fields) {
        Map result = new HashMap();
        for (int col = 0; col < row.values.size(); col++) {
            if (fields.size() > col && StringUtils.isNotEmpty(fields.get(col))) {
                if (row.values.get(col) != null) result.put(fields.get(col), row.values.get(col));
            }
        }
        return result;
    }


    public String validateCustomerMap(Map<String, String> map) {
        StringBuilder res = new StringBuilder();
        for (String key : map.keySet()) {
            String value = map.get(key);
            if ("email".equalsIgnoreCase(key)) {
                if (StringUtils.isEmpty(value)) res.append("Email is required").append("\n");
                else if (value.length() > 512) res.append("Email too long").append("\n");
                else {
                    Pattern pattern = Pattern.compile(EmailValidator.emailAddressPattern);
                    Matcher matcher = pattern.matcher(value);
                    if (!matcher.matches()) res.append("Invalid email").append("\n");
                }
            } else if ("firstname".equalsIgnoreCase(key) && StringUtils.isNotEmpty(value)) {
                if (value.length() > 250) res.append("First name too long").append("\n");
            } else if ("lastname".equalsIgnoreCase(key) && StringUtils.isNotEmpty(value)) {
                if (value.length() > 250) res.append("First name too long").append("\n");
            } else if ("userId".equalsIgnoreCase(key) && StringUtils.isNotEmpty(value)) {
                if (value.length() > 50) res.append("UserId too long").append("\n");
            } else if ("sex".equalsIgnoreCase(key) && StringUtils.isNotEmpty(value)) {
                if (value.length() > 1) map.put("sex", value.substring(0, 1));
            } else if ("title".equalsIgnoreCase(key) && StringUtils.isNotEmpty(value)) {
                if (value.length() > 50) map.put("title", value.substring(0, 50));
            } else if ("language".equalsIgnoreCase(key) && StringUtils.isNotEmpty(value)) {
                if (value.length() > 2) map.put("language", value.substring(0, 2).toLowerCase());
            }
        }
        return res.toString();
    }

    public String getEncryptionKey(HibernateDAO dao) {
        StoreProperty bean = dao.getStoreProperty(StoreProperty.PROP_ENCRYPTION_KEY, StoreProperty.TYPE_GENERAL, true);
        if (StringUtils.isEmpty(bean.getValue())) {
            bean.setValue(User.generatePassword(16));
            dao.save(bean);
        }
        return bean.getValue();
    }

    
}
