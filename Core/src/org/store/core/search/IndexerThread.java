package org.store.core.search;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.store.core.beans.Product;
import org.store.core.beans.ProductLang;
import org.store.core.beans.StoreProperty;
import org.store.core.dao.HibernateDAO;
import org.store.core.globals.StoreThread;
import org.store.core.globals.config.Store20Database;
import org.store.core.hibernate.HibernateSessionFactory;
import org.store.core.utils.quartz.JobStoreThread;

import java.util.Calendar;
import java.util.List;

/**
 * Rogelio Caballero
 * 17/05/12 21:57
 */
public class IndexerThread extends JobStoreThread {

    LuceneIndexer indexer;

    public IndexerThread(String storeCode, Store20Database db) {
        this.storeCode = storeCode;
        this.databaseConfig = db;
        setName("lucene_indexer_" + String.valueOf(Calendar.getInstance().getTimeInMillis()));
    }

    @Override
    public void run() {
        long init = Calendar.getInstance().getTimeInMillis();
        try {
            Session session = HibernateSessionFactory.getNewSession(databaseConfig);
            Transaction tx = session.beginTransaction();
            try {
                HibernateDAO dao = new HibernateDAO(session, storeCode);
                this.basePath = dao.getStorePropertyValue(StoreProperty.PROP_SITE_PATH, StoreProperty.TYPE_GENERAL, "");
                indexer = new LuceneIndexer(basePath, dao.getLanguages(), dao.getDefaultLanguage());
                indexProducts(session);
                tx.commit();
            } catch (Exception e) {
                tx.rollback();
                StoreThread.log.error(e.getMessage(), e);
            } finally {
                if (session.isOpen()) session.close();
            }
            setExecutionMessage("Complete");
            setExecutionPercent(100d);

        } catch (Exception e) {
            StoreThread.log.error(e.getMessage(), e);
        }
        System.out.println("Run in " +String.valueOf(Calendar.getInstance().getTimeInMillis() - init)+" ms");

    }

    private void indexProducts(Session session) {
        List list = session.createSQLQuery("select distinct t_product.idProduct from t_product left join t_product_lang on t_product.idProduct=t_product_lang.product_idProduct where t_product.indexed is null or t_product.indexed = false or t_product_lang.indexed is null or t_product_lang.indexed = false and inventaryCode=:store").setParameter("store", storeCode).list();
        if (!list.isEmpty()) {
            long index = 1, total = list.size();
            for (Object o : list) {
                Long id = ((Number) o).longValue();
                setExecutionMessage("Indexing " + String.valueOf(index++) + " of " + String.valueOf(total));
                setExecutionPercent(index * 100d / total);
                indexProduct(session, id);
            }
        }
    }

    private void indexProduct(Session session, Long id) {
        Product product = (Product) session.get(Product.class, id);
        if (product != null) {
            indexer.indexProduct(product, false);
            product.setIndexed(true);
            session.save(product);
            if (product.getProductLangs() != null) {
                for (ProductLang pl : product.getProductLangs()) {
                    pl.setIndexed(true);
                    session.save(pl);
                }
            }
            session.flush();
            session.clear();
        }
    }

}
