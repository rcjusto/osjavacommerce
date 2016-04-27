package org.store.core.utils.products;

import org.hibernate.Session;
import org.hibernate.StatelessSession;
import org.hibernate.Transaction;
import org.store.core.beans.ProductLabel;
import org.store.core.dao.HibernateDAO;
import org.store.core.globals.SomeUtils;
import org.store.core.globals.config.Store20Database;
import org.store.core.hibernate.HibernateSessionFactory;
import org.store.core.utils.quartz.JobStoreThread;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Calendar;

public class ProductUpdaterThreat extends JobStoreThread {

    public ProductUpdaterThreat(String storeCode, Store20Database databaseConfig) {
        this.storeCode = storeCode;
        this.databaseConfig = databaseConfig;
        setName("product_task_" + String.valueOf(Calendar.getInstance().getTimeInMillis()));
    }


    public void setStoreCode(String storeCode) {
        this.storeCode = storeCode;
    }

    @Override
    public void run() {
        int res = processProductsEx();
        addOutputMessage(String.valueOf(res) + " labels removed");
        setJobExecutionResult("COMPLETE");
    }

    public int processProductsEx() {
        int res = 0;
        Integer newDays = SomeUtils.strToInteger(getJob().getJobProperty("days_to_remove_new_label"));
        if (newDays != null) {
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DATE, -newDays);

            try {
                // buscar label
                Long idLabel = null;
                Session hsession = HibernateSessionFactory.getNewSession(databaseConfig);
                Transaction tx = hsession.beginTransaction();
                try {
                    HibernateDAO dao = new HibernateDAO(hsession, storeCode);
                    ProductLabel pl = dao.getProductLabelByCode(ProductLabel.LABEL_NEW);
                    if (pl!=null) idLabel = pl.getId();
                    tx.commit();
                } catch (Exception e) {
                    tx.rollback();
                    log.error(e.getMessage(), e);
                } finally {
                    if (hsession.isOpen()) hsession.close();
                }

                if (idLabel!=null) {
                    Session session = HibernateSessionFactory.getNewSession(this.databaseConfig);
                    Transaction tx1 = session.beginTransaction();
                    try {
                        PreparedStatement st = session.connection().prepareStatement("delete from t_product_t_productlabel where labels_id=? and t_product_t_productlabel.t_product_idProduct in (select product_idProduct from t_product_audit_stock where t_product_audit_stock.changeDate<?)");
                        st.setLong(1, idLabel);
                        st.setDate(2, new java.sql.Date(cal.getTime().getTime()));
                        res = st.executeUpdate();
                        tx1.commit();
                    } catch (SQLException e) {
                        tx1.rollback();
                        log.error(e.getMessage(), e);
                    } finally {
                        if (session.isOpen()) {
                            session.close();
                        }
                    }
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
        return res;
    }

    /**
    public int processProducts() {
        int res = 0;
        Integer newDays = SomeUtils.strToInteger(getJob().getJobProperty("days_to_remove_new_label"));
        if (newDays != null) {
            try {
                Session session = HibernateSessionFactory.getNewSession(databaseConfig);
                Transaction tx = session.beginTransaction();
                try {

                    HibernateDAO dao = new HibernateDAO(session, storeCode);
                    setExecutionMessage("Getting products");
                    setExecutionPercent(0d);
                    Date today = Calendar.getInstance().getTime();
                    List<Product> products = dao.getProducts();
                    if (!products.isEmpty()) {
                        int num = 0, total = products.size();
                        for (Product p : products) {
                            setExecutionMessage("Processing product " + num++);
                            setExecutionPercent(100d * num / total);
                            if (p.hasLabel(ProductLabel.LABEL_NEW)) {
                                Date d = dao.getProductCreatedDate(p);
                                if (d != null && SomeUtils.dayDiff(d, today) > newDays) {
                                    p.delLabel(ProductLabel.LABEL_NEW);
                                    dao.save(p);
                                    res++;
                                }
                            }
                        }
                    }
                    tx.commit();
                } catch (Exception e) {
                    tx.rollback();
                    log.error(e.getMessage(), e);
                } finally {
                    if (session.isOpen()) session.close();
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
        return res;
    }
    **/
}
