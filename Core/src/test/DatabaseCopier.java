package test;

import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.lang.StringUtils;
import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.hibernate.StatelessSession;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;
import org.store.core.beans.*;
import org.store.core.globals.SomeUtils;
import org.store.core.globals.config.Store20Database;
import org.store.core.hibernate.HibernateSessionFactory;

import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

/**
 * Rogelio Caballero
 * 21/06/12 20:22
 */
public class DatabaseCopier {

    private StatelessSession session1;
    private Session session2;
    private String storeCode2;
    private NullAwareBeanUtilsBean copier;
    private Double rate = 1d;

    public DatabaseCopier(StatelessSession session1, Session session2, String storeCode2, Double rate) {
        this.session1 = session1;
        this.session2 = session2;
        this.storeCode2 = storeCode2;
        this.rate = (rate != null) ? rate : 1d;
        this.copier = new NullAwareBeanUtilsBean();
    }

    public Product copyProduct(Long id, Category category2) {
        Product p = (Product) session1.get(Product.class, id);

        List<ProductLabel> labels = session1.createSQLQuery("select {t_productlabel.*} from t_product_t_productlabel left join t_productlabel on t_product_t_productlabel.labels_id=t_productlabel.id " +
                "where t_product_t_productlabel.t_product_idProduct=" + id.toString()).addEntity("t_productlabel", ProductLabel.class).list();

        List<Resource> resources = session1.createSQLQuery("select {t_resources.*} from t_product_t_resources left join t_resources on t_product_t_resources.productResources_id=t_resources.id\n" +
                "where t_product_t_resources.t_product_idProduct=" + id.toString()).addEntity("t_resources", Resource.class).list();

        if (p != null) {
            Transaction tx = session2.beginTransaction();
            try {
                Product p2 = new Product();
                copier.copyProperties(p2, p);
                p2.setIndexed(false);
                p2.setIdProduct(null);
                p2.setCategory(category2);
                p2.setComplementGroup(null);
                p2.setRelatedGroups(null);
                p2.setForUsers(null);
                p2.setManufacturer(copyManufacturer(p.getManufacturer()));
                p2.setProductResources(copyResources(resources));
                p2.setLabels(copyLabels(labels));
                p2.setInventaryCode(storeCode2);
                p2.setStock(0l);
                p2.setEta("4 weeks");

                if (category2 != null) {
                    Set<Category> productCategories = new HashSet<Category>();
                    productCategories.add(category2);
                    p2.setProductCategories(productCategories);
                } else {
                    p2.setProductCategories(null);
                }

                session2.save(p2);

                // idioma
                List<ProductLang> productLangList = session1.createCriteria(ProductLang.class).add(Restrictions.eq("product", p)).list();
                if (productLangList != null && !productLangList.isEmpty()) {
                    for (ProductLang pl : productLangList) {
                        ProductLang pl2 = new ProductLang();
                        copier.copyProperties(pl2, pl);
                        pl2.setIndexed(false);
                        pl2.setId(null);
                        pl2.setProduct(p2);
                        session2.save(pl2);
                    }
                }

                // proveedores    
                Double eurPrice = null;
                List<ProductProvider> productProviderList = session1.createCriteria(ProductProvider.class).add(Restrictions.eq("product", p)).list();
                if (productProviderList != null && !productProviderList.isEmpty()) {
                    for (ProductProvider pp : productProviderList) {
                        eurPrice = pp.getCostPrice();
                        ProductProvider pp2 = new ProductProvider();
                        copier.copyProperties(pp2, pp);
                        pp2.setId(null);
                        pp2.setProduct(p2);
                        pp2.setCostCurrency(copyCurrency(pp.getCostCurrency()));
                        pp2.setProvider(copyProvider(pp.getProvider()));
                        if (pp2.getProvider() != null) session2.save(pp2);
                    }
                }

                if (eurPrice != null) {
                    p2.setCostPrice(eurPrice * rate);
                    session2.save(p2);
                }

                tx.commit();
                //session2.evict(p2);
                session2.clear();
            } catch (Exception e) {
                tx.rollback();
                e.printStackTrace();
            }
        }
        return p;
    }

    private Currency copyCurrency(Currency c1) {
        if (c1 == null) return null;
        List<Currency> list = session2.createCriteria(Currency.class).add(Restrictions.eq("code", c1.getCode())).add(Restrictions.eq("inventaryCode", storeCode2)).list();
        Currency c2 = (list != null && !list.isEmpty()) ? list.get(0) : null;
        if (c2 == null) {
            try {
                c2 = new Currency();
                copier.copyProperties(c2, c1);
                c2.setId(null);
                c2.setInventaryCode(storeCode2);
                session2.save(c2);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return c2;
    }

    private Set<ProductLabel> copyLabels(List<ProductLabel> labels1) {
        if (labels1 != null && !labels1.isEmpty()) {
            Set<ProductLabel> labels2 = new HashSet<ProductLabel>();
            for (ProductLabel l1 : labels1) {
                List<ProductLabel> list = session2.createCriteria(ProductLabel.class).add(Restrictions.eq("code", l1.getCode())).list();
                ProductLabel l2 = (list != null && !list.isEmpty()) ? list.get(0) : null;
                if (l2 == null) {
                    try {
                        l2 = new ProductLabel();
                        copier.copyProperties(l2, l1);
                        l2.setId(null);
                        l2.setInventaryCode(storeCode2);
                        l2.setProducts(null);
                        l2.setApplyPromotions(null);
                        l2.setNotApplyPromotions(null);
                        session2.save(l2);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
                labels2.add(l2);
            }
            return labels2;
        }
        return null;
    }

    private Set<Resource> copyResources(List<Resource> resources1) {
        return null;
    }

    public Provider copyProvider(Provider p1) {
        List<Provider> list = session2.createCriteria(Provider.class)
                .add(Restrictions.eq("inventaryCode", storeCode2))
                .add(Restrictions.eq("providerName", p1.getProviderName()))
                .list();
        Provider p2 = (list != null && !list.isEmpty()) ? list.get(0) : null;
        if (p2 == null) {
            try {
                p2 = new Provider();
                copier.copyProperties(p2, p1);
                p2.setInventaryCode(storeCode2);
                p2.setIdProvider(null);
                session2.save(p2);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return p2;
    }

    public Manufacturer copyManufacturer(Manufacturer m1) {
        if (m1 == null) return null;
        List<Manufacturer> list = session2.createCriteria(Manufacturer.class)
                .add(Restrictions.eq("inventaryCode", storeCode2))
                .add(Restrictions.eq("manufacturerName", m1.getManufacturerName()))
                .list();
        Manufacturer m2 = (list != null && !list.isEmpty()) ? list.get(0) : null;
        if (m2 == null) {
            m2 = new Manufacturer();
            try {
                copier.copyProperties(m2, m1);
            } catch (Exception e) {
                e.printStackTrace();
            }
            m2.setInventaryCode(storeCode2);
            m2.setIdManufacturer(null);
            session2.save(m2);
        }
        return m2;
    }


    public static void main(final String[] args) throws Exception {
        InputStream is = DatabaseCopier.class.getResourceAsStream("dbcopier.properties");
        if (is == null && args != null && args.length > 0 && StringUtils.isNotEmpty(args[0])) {
            is = new FileInputStream(args[0]);
        }
        if (is != null) {
            Properties prop = new Properties();
            prop.load(is);

            Store20Database db = new Store20Database();
            db.setId(prop.getProperty("source.id"));
            db.setType(prop.getProperty("source.type"));
            db.setUrl(prop.getProperty("source.url"));
            db.setUser(prop.getProperty("source.user"));
            db.setPassword(prop.getProperty("source.password"));
            StatelessSession session = null;
            try {
                session = HibernateSessionFactory.getStatelessSession(db);
            } catch (Exception e) {
                System.out.println("Can't connect to source database.");
                e.printStackTrace();
            }

            db = new Store20Database();
            db.setId(prop.getProperty("target.id"));
            db.setType(prop.getProperty("target.type"));
            db.setUrl(prop.getProperty("target.url"));
            db.setUser(prop.getProperty("target.user"));
            db.setPassword(prop.getProperty("target.password"));
            Session session1 = null;
            try {
                session1 = HibernateSessionFactory.getNewSession(db);
                session1.setFlushMode(FlushMode.MANUAL);
            } catch (Exception e) {
                System.out.println("Can't connect to target database.");
                e.printStackTrace();
            }

            try {
                List<Currency> currencies = session1.createCriteria(Currency.class).add(Restrictions.eq("code", "EUR")).list();
                Currency currency = (currencies != null && !currencies.isEmpty()) ? currencies.get(0) : null;
                Double rate = (currency != null && currency.getReverseRatio() != null) ? currency.getReverseRatio() : 1d;

                DatabaseCopier dc = new DatabaseCopier(session, session1, prop.getProperty("target.id"), rate);

                // buscar categoria
                Long idCat = SomeUtils.strToLong(prop.getProperty("target.category"));
                Category targetCategory = (Category) session1.get(Category.class, idCat);

                // buscar productos
                String qProducts = StringUtils.isNotEmpty(prop.getProperty("source.categories"))
                        ? "select t_product.idProduct from t_product where t_product.category_idCategory in (" + prop.getProperty("source.categories") + ")"
                        : "select t_product.idProduct from t_product";

                long index = 0;
                List<Number> list = session.createSQLQuery(qProducts).list();
                for (Number id : list) {
                    if (id != null) {
                        dc.copyProduct(id.longValue(), targetCategory);
                        if (index++ % 50 == 0) {
                            System.out.println("");
                            System.out.print(String.valueOf(index));
                        } else System.out.print(".");
                    }
                }

                System.out.println("---------------------------------------");
                System.out.println("Imported Products: " + String.valueOf(index));
                System.out.println("---------------------------------------");

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (session1.isOpen()) session1.close();
                session.close();
            }
        } else {
            System.out.println("Configuration file not found");
        }
    }


    public class NullAwareBeanUtilsBean extends BeanUtilsBean {

        @Override
        public void copyProperty(Object dest, String name, Object value) throws IllegalAccessException, InvocationTargetException {
            if (value == null) return;
            super.copyProperty(dest, name, value);
        }

    }


}
