package test;

import com.opensymphony.xwork2.ognl.OgnlUtil;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.util.JRLoader;
import ognl.OgnlException;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.TermQuery;
import org.hibernate.*;
import org.hibernate.criterion.Restrictions;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.search.FullTextQuery;
import org.hibernate.search.FullTextSession;
import org.hibernate.search.Search;
import org.hibernate.search.SearchFactory;
import org.hibernate.search.reader.ReaderProvider;
import org.hibernate.search.store.DirectoryProvider;
import org.store.core.beans.LocalizedText;
import org.store.core.beans.Order;
import org.store.core.beans.Product;
import org.store.core.beans.User;
import org.store.core.dao.HibernateDAO;
import org.store.core.globals.SomeUtils;
import org.store.core.globals.config.Store20Database;
import org.store.core.hibernate.HibernateSessionFactory;
import org.store.core.utils.classutil.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: Administrador
 * Date: Mar 21, 2010
 */
public class Main {

    public static Session getSession() {
        Store20Database db = new Store20Database();
        db.setId("mycartronic");
        db.setType("MySQL");
        db.setUrl("jdbc:mysql://localhost/cartronic");
        db.setUser("root");
        db.setPassword("root");
        try {
            return HibernateSessionFactory.getNewSession(db);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static StatelessSession getStatelessSession() {
        Store20Database db = new Store20Database();
        db.setId("mycartronic");
        db.setType("MySQL");
        db.setUrl("jdbc:mysql://localhost/cartronic");
        db.setUser("root");
        db.setPassword("root");
        try {
            return HibernateSessionFactory.getStatelessSession(db);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Session getSession1() {
        Store20Database db = new Store20Database();
        db.setId("default");
        db.setType("MySQL");
        db.setUrl("jdbc:mysql://localhost/store2");
        db.setUser("root");
        db.setPassword("root");
        try {
            return HibernateSessionFactory.getNewSession(db);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void main(final String[] args) throws Exception {
        //findImplementation();
        //reIndex(args);
        //readIndex();
        // makeSearch("name:'Pentium' AND inventaryCode:'cartronic'");
        //reSaveOrders();
         saveLabels("/media/trabajo/proyectos/stores/version2/resources/db/labels.prop");
        //testOgnl();
        //testDBScript();
        //techData();
        //auditProduct();
        // testEncriptation();
        //xmlOrder();
        //test();
    }

    public static void readOnix() {
     }

    private static void test() throws Exception {

        
    }

    public static void xmlOrder() {
        Session session = getSession();
        try {

            Calendar cal = Calendar.getInstance();
            long dIni = cal.getTimeInMillis();
            cal.add(Calendar.DATE, -10);
            Order o = (Order) session.get(Order.class, 30l);
            Map m = BeanUtils.describe(o);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (session.isOpen()) session.close();
        }
    }

    public static void testEncriptation() throws Exception {
        String key = "rcj123probando";
        String password = "ams11466";
        for (int i = 0; i < 20; i++) {
            password = User.generatePassword(1);
            System.out.println("\n Test No." + String.valueOf(i + 1));
            String encripted = SomeUtils.encrypt3Des(password, key);
            System.out.println("Encripted: " + encripted);
            String desencripted = SomeUtils.decrypt3Des(encripted, key);
            System.out.println("Decripted: " + desencripted);
            System.out.println(password.equals(desencripted) ? "Work OK" : "Password is different from decripted");
        }
    }

    public static void auditProduct() throws Exception {
        Session session = getSession();
        try {

            Calendar cal = Calendar.getInstance();
            long dIni = cal.getTimeInMillis();
            cal.add(Calendar.DATE, -10);
            SQLQuery q = session.createSQLQuery("select idProduct from (select t_product.idProduct, (select min(t_product_audit_stock.changeDate) from t_product_audit_stock where t_product.idProduct=t_product_audit_stock.product_idProduct) as firstDate,(select max(t_product_audit_stock.changeDate) from t_product_audit_stock where t_product.idProduct=t_product_audit_stock.product_idProduct and t_product_audit_stock.stock>0) as lastDateWithStock from t_product where stock=0 and inventaryCode=:store and (archived is null or archived=0)) t where (t.lastDateWithStock is null and firstDate<:date) or (t.lastDateWithStock is not null and lastDateWithStock<:date)")
                    .addScalar("idProduct", Hibernate.LONG);
            q.setString("store", "peonline");
            q.setDate("date", cal.getTime());

            List<Long> filterList = q.list();

            Criteria cri = session.createCriteria(Product.class);
            cri.add(Restrictions.le("stock", 0l));
            cri.add(Restrictions.eq("inventaryCode", "peonline"));
            if (filterList != null && !filterList.isEmpty()) cri.add(Restrictions.in("idProduct", filterList));
            List l = cri.list();

            Long demora = Calendar.getInstance().getTimeInMillis() - dIni;
            System.out.println(l.size() + " productos encontrados, demora: " + demora.toString());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (session.isOpen()) session.close();
        }
    }

    public static void jasper() throws Exception {
        Session session = getSession();
        try {
            JasperReport jasperReport = (JasperReport) JRLoader.loadObject("C:\\Users\\Rogelio\\report1.jasper");
            if (jasperReport != null && jasperReport.getParameters() != null) {
                for (JRParameter p : jasperReport.getParameters()) {
                    System.out.println("Param: " + p.getName());
                }
            }
            Map map = new HashMap();
            map.put("storeCode", "store");
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, map, session.connection());
            JasperExportManager.exportReportToPdfFile(jasperPrint, "C:\\Users\\Rogelio\\prueba1.pdf");

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (session.isOpen()) session.close();
        }
    }

    public static void defaultMethod(final String[] args) throws Exception {
        final Session session = getSession();
        try {
            System.out.println("querying all the managed entities...");
            final Map metadataMap = session.getSessionFactory().getAllClassMetadata();
            for (Object key : metadataMap.keySet()) {
                final ClassMetadata classMetadata = (ClassMetadata) metadataMap.get(key);
                final String entityName = classMetadata.getEntityName();
                final Query query = session.createQuery("from " + entityName);
                System.out.println("executing: " + query.getQueryString());
                for (Object o : query.list()) {
                    System.out.println("  " + o);
                }
            }
        } finally {
            session.close();
        }
    }


    public static void reIndex(final String[] args) throws Exception {
        Session session = getSession();
        try {
            FullTextSession fullTextSession = Search.getFullTextSession(session);
            fullTextSession.purgeAll(Product.class);
            fullTextSession.setFlushMode(FlushMode.MANUAL);
            fullTextSession.setCacheMode(CacheMode.IGNORE);
            Transaction transaction = fullTextSession.beginTransaction();
            //Scrollable results will avoid loading too many objects in memory
            ScrollableResults results1 = fullTextSession.createCriteria(Product.class)
                    .add(Restrictions.eq("inventaryCode", "cartronic"))
                    .setFetchSize(50)
                    .scroll(ScrollMode.FORWARD_ONLY);
            int index1 = 0;
            while (results1.next()) {
                index1++;
                fullTextSession.index(results1.get(0)); //index each element
                if (index1 % 50 == 0) {
                    fullTextSession.flushToIndexes(); //apply changes to indexes
                    fullTextSession.clear(); //clear since the queue is processed
                }
            }

            transaction.commit();
            fullTextSession.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (session.isOpen()) session.close();
        }

    }


    private static void makeSearch(String q) {
        Session session = getSession();
        try {
            BooleanQuery query = new BooleanQuery();
            query.add(new FuzzyQuery(new Term("name_es", "Cartucho")), BooleanClause.Occur.MUST);
            query.add(new TermQuery(new Term("store", "cartronic")), BooleanClause.Occur.MUST);

            FullTextSession fullTextSession = Search.getFullTextSession(session);
            /*
            fullTextSession.createFullTextQuery(tq);
            //    MultiFieldQueryParser parser = new MultiFieldQueryParser(Product.FIELDS_FOR_SEARCH, new ReferenciaAnalizer());
            MultiFieldQueryParser parser = new MultiFieldQueryParser(Product.getFieldsToSearch(), new ProductAnalyzer());
            //    QueryParser parser = new QueryParser( "name",new StandardAnalyzer());
            org.apache.lucene.search.Query query = parser.parse(q);
              */
            FullTextQuery hibQuery = fullTextSession.createFullTextQuery(query, Product.class);
            List result = hibQuery.list();
            System.out.println("resultados: " + result.size());
            for (Object obj : result) {
                Product ref = (Product) obj;
                System.out.println(ref.getProductName("en") + " - " + ref.getInventaryCode());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (session.isOpen()) session.close();
        }
    }


    public static void readIndex() {
        Session session = getSession();
        try {
            FullTextSession fullTextSession = Search.getFullTextSession(session);
            SearchFactory searchFactory = fullTextSession.getSearchFactory();
            DirectoryProvider[] provider = searchFactory.getDirectoryProviders(Product.class);

            ReaderProvider readerProvider = searchFactory.getReaderProvider();
            IndexReader reader = readerProvider.openReader(provider[0]);
            try {
                for (int i = 0; i < reader.numDocs(); i++) {
                    Document d = reader.document(i);

                    System.out.println(StringUtils.join(d.getValues("inventaryCode"), ", "));
                }
//do read-only operations on the reader
            } finally {
                readerProvider.closeReader(reader);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (session.isOpen()) session.close();
        }
    }

    public static void findImplementation() {
        ClassFinder finder = new ClassFinder();
        finder.add(new File("D:\\proyectos\\stores\\version2\\web\\WEB-INF\\lib\\store20-suppliers.jar"));

        ClassFilter filter =
                new AndClassFilter
                        // Must not be an interface
                        (new NotClassFilter(new InterfaceOnlyClassFilter()),

                                // Must implement the ClassFilter interface
                                new SubclassClassFilter(org.store.core.utils.suppliers.SupplierService.class));

        Collection<ClassInfo> foundClasses = new ArrayList<ClassInfo>();
        finder.findClasses(foundClasses, filter);
        System.out.println("founded: " + foundClasses.size());
        for (ClassInfo classInfo : foundClasses)
            System.out.println("Found " + classInfo.getClassName());

    }

    public static void reSaveOrders() {
        Session session = getSession();
        Transaction tx = session.beginTransaction();
        try {
            List<Order> orders = session.createCriteria(Order.class).list();
            for (Order o : orders) {
                o.setIdAdminUser(null);
                o.setTotal(1d);
                session.save(o);
            }
            tx.commit();
        } catch (HibernateException e) {
            tx.rollback();
            e.printStackTrace();
        } finally {
            if (session.isOpen()) session.close();
        }
    }

    public static void saveLabels(String filename) {
        Session session = getSession();
        Transaction tx = session.beginTransaction();
        try {
            HibernateDAO dao = new HibernateDAO(session, "cartronic");
            List<LocalizedText> l = dao.getLocalizedTexts();
            if (l != null) {
                Properties p = new Properties();
                for (LocalizedText lt : l) {
                    if (StringUtils.isNotEmpty(lt.getCode()) && StringUtils.isNotEmpty(lt.getValue()))
                        p.setProperty(lt.getCode(), lt.getValue());
                }
                File f = new File(filename);
                if (f.exists()) f.delete();
                try {
                    FileOutputStream fos = new FileOutputStream(f);
                    p.store(fos, "");
                    fos.close();
                    System.out.println("Labels saved at: " + filename);
                    System.out.println(p.size() + " lines");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            tx.commit();
        } catch (HibernateException e) {
            tx.rollback();
            e.printStackTrace();
        } finally {
            if (session.isOpen()) session.close();
        }
        File f = new File(filename);
        if (f.exists()) System.out.println(f.length() + " bytes");
    }

    public static void loadLabels(String filename) {
        File f = new File(filename);
        if (f.exists()) {
            Properties p = new Properties();
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(f);
                p.load(fis);
                fis.close();

                if (!p.isEmpty()) {

                    Session session = getSession();
                    Transaction tx = session.beginTransaction();
                    try {
                        HibernateDAO dao = new HibernateDAO(session, "store");
                        for (String key : p.stringPropertyNames()) {
                            LocalizedText lt = dao.getLocalizedtext(key);
                            if (lt == null) {
                                lt = new LocalizedText();
                                lt.setCode(key);
                                lt.setInventaryCode("store");
                            }
                            lt.setValue(p.getProperty(key));
                            dao.save(lt);
                        }
                        tx.commit();
                    } catch (HibernateException e) {
                        tx.rollback();
                        e.printStackTrace();
                    } finally {
                        if (session.isOpen()) session.close();
                    }


                }

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (fis != null) fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }


    public static void testOgnl() {
        Session session = getSession();
        Transaction tx = session.beginTransaction();
        try {
            HibernateDAO dao = new HibernateDAO(session, "store");
            Product p = dao.getProducts().get(0);

            OgnlUtil u = new OgnlUtil();
            try {
                Object o = u.getValue("category.getCategoryName('es')", new HashMap<String, Object>(), p);
                System.out.println("Value: " + o.toString());
            } catch (OgnlException e) {
                e.printStackTrace();
            }

            tx.commit();
        } catch (HibernateException e) {
            tx.rollback();
            e.printStackTrace();
        } finally {
            if (session.isOpen()) session.close();
        }
    }

    public static void testDBScript() {
        Session session = getSession();
        Transaction tx = session.beginTransaction();
        try {

            SQLQuery q = session.createSQLQuery("select * from t_product where inventaryCode=:store");
            System.out.println(q.getNamedParameters().toString());

            tx.commit();
        } catch (HibernateException e) {
            tx.rollback();
            e.printStackTrace();
        } finally {
            if (session.isOpen()) session.close();
        }
    }


}
