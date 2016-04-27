package org.store.publications;

import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.store.core.beans.Currency;
import org.store.core.beans.Provider;
import org.store.core.dao.HibernateDAO;
import org.store.core.globals.config.Store20Database;
import org.store.core.hibernate.HibernateSessionFactory;
import org.store.core.utils.quartz.JobStoreThread;

import java.util.List;

/**
 * Rogelio Caballero
 * 11/06/12 17:44
 */
public class PublicationExchangeUpdater extends JobStoreThread {

    private String[] supplierNames = {
            org.store.publications.digitalbooks.CatalogProcessor.SUPPLIER_NAME,
            org.store.publications.libranda.CatalogProcessor.SUPPLIER_NAME,
            org.store.publications.publidisa.CatalogProcessor.SUPPLIER_NAME,
            "Celesa"
    };

    private Store20Database databaseConfig;
    private String storeCode;

    public PublicationExchangeUpdater(Store20Database databaseConfig, String storeCode) {
        this.databaseConfig = databaseConfig;
        this.storeCode = storeCode;
    }

    @Override
    public void run() {
        try {
            Session session = HibernateSessionFactory.getNewSession(this.databaseConfig);
            Transaction tx = session.beginTransaction();
            try {
                HibernateDAO dao = new HibernateDAO(session, storeCode);

                int index = 0;
                for (String name : supplierNames) {
                    Provider provider = dao.getProviderByAltName(name);
                    if (provider != null) {
                        setExecutionMessage("Processing supplier '" + provider.getProviderName());
                        setExecutionPercent(25d * index++ );
                        processSupplierEx(provider, dao);
                    }
                }

                tx.commit();
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                addOutputMessage(e.getMessage());
                setJobExecutionResult("Error");
                tx.rollback();
            } finally {
                if (session.isOpen()) session.close();
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            addOutputMessage(e.getMessage());
            setJobExecutionResult("Error");
        }
    }

    private void processSupplierEx(Provider provider, HibernateDAO dao) {
        Currency defaultCurrency = dao.getDefaultCurrency();
        List lCurr = dao.gethSession().createSQLQuery("select distinct t_product_provider.costCurrency_id from t_product_provider where t_product_provider.provider_idProvider=" + provider.getIdProvider().toString()).list();
        if (lCurr != null && !lCurr.isEmpty()) {
            for (Object o : lCurr) {
                Currency currency = (o instanceof Number) ? (Currency) dao.get(Currency.class, ((Number)o).longValue()) : null;
                if (currency != null && !currency.equals(defaultCurrency) && currency.getReverseRatio() != null) {
                    SQLQuery query = dao.gethSession().createSQLQuery("update t_product, t_product_provider set t_product.costPrice = t_product_provider.costPrice * :rate " +
                            "where t_product.idProduct=t_product_provider.product_idProduct and t_product_provider.provider_idProvider=" + provider.getIdProvider() + " and t_product_provider.costCurrency_id=" + currency.getId() + " and t_product_provider.costPrice is not null");
                    query.setDouble("rate", currency.getReverseRatio());
                    int updated = query.executeUpdate();
                    log.debug("PublicationExchangeUpdater: " + provider.getProviderName() + " - " +currency.getCode() + " => " + String.valueOf(updated) + " updated" );
                }
            }
        }
    }

    /*
    private void processSupplier(Provider provider, HibernateDAO dao) {
        String msg = "Processing supplier '" + provider.getProviderName() + "' - ";
        // buscar productos
        List<ProductProvider> list = dao.gethSession().createCriteria(ProductProvider.class)
                .add(Restrictions.eq("provider", provider))
                .add(Restrictions.ne("costCurrency", dao.getDefaultCurrency()))
                .list();

        if (list != null && !list.isEmpty()) {
            int index = 0, total = list.size();
            for (ProductProvider pp : list) {
                setExecutionMessage(msg + String.valueOf(index++) + "/" + String.valueOf(total));
                setExecutionPercent(index * 100d / total);
                if (pp.getCostCurrency() != null && pp.getCostCurrency().getReverseRatio() != null && pp.getCostPrice() != null) {
                    pp.getProduct().setCostPrice(pp.getCostCurrency().getReverseRatio() * pp.getCostPrice());
                    dao.save(pp.getProduct());
                }
                dao.flushSession();
                dao.evict(pp.getProduct());
                dao.evict(pp);
            }
        }

    }
     */

}
