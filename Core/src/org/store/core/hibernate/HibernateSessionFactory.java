package org.store.core.hibernate;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.StatelessSession;
import org.hibernate.cfg.Configuration;
import org.hibernate.event.*;
import org.hibernate.event.def.DefaultPostLoadEventListener;
import org.store.core.beans.*;
import org.store.core.beans.utils.ProductInterceptor;
import org.store.core.globals.config.Store20Database;

import java.util.HashMap;
import java.util.Map;


public class HibernateSessionFactory {

    public static final String DEFAULT_HIBERATE_CONFIGFILE = "/hibernate.cfg.xml";

    private static Map<String, Configuration> configuration = null;
    private static Map<String, SessionFactory> sessionFactory;

    private static Map<String, ThreadLocal<Session>> sessions = new HashMap<String, ThreadLocal<Session>>();

    protected static Logger log;

    static {
        initLogger();
    }

    public static void initLogger() {
        if (log != null)
            return;
        log = Logger.getLogger(HibernateSessionFactory.class);
        if (!Logger.getRootLogger().getAllAppenders().hasMoreElements())
            log.addAppender(new ConsoleAppender(new PatternLayout("%d{HH:mm:ss} - %p: %m %n")));
        log.setLevel(Level.WARN);
    }

    public static void setLoggerDebugLevel() {
        if (!log.getLevel().equals(Level.DEBUG))
            log.setLevel(Level.DEBUG);
    }

    public static Session getSession(Store20Database db) throws Exception {
        log.debug("Hibernate Session Required (from current Thread)");
        Session session = (sessions.containsKey(db.getId())) ? (sessions.get(db.getId())).get() : null;
        if (session == null) {
            if (sessionFactory == null || !sessionFactory.containsKey(db.getId())) {
                rebuildSessionFactory(db);
            }
            session = sessionFactory.get(db.getId()).openSession();
            if (!sessions.containsKey(db.getId())) sessions.put(db.getId(), new ThreadLocal<Session>());
            sessions.get(db.getId()).set(session);
            log.debug("No Hibernate Session in current thread. Session New Hibernate Session created and returned");
        } else
            log.debug("Existing Hibernate Session from current thread returned");
        return session;
    }

    public static Session getNewSession(Store20Database db) throws Exception {
        if (db != null && db.getId() != null) {
            try {
                log.debug("New Hibernate Session required");
                if (sessionFactory == null || !sessionFactory.containsKey(db.getId())) {
                    rebuildSessionFactory(db);
                }
                Session session = sessionFactory.get(db.getId()).openSession();
                log.debug("New Hibernate Session created and returned");
                return session;
            } catch (Exception e) {
                log.error("Database: " + db.getId() + "\n" + e.getMessage(), e);
            }
        }
        return null;
    }

    public static StatelessSession getStatelessSession(Store20Database db) throws Exception {
        log.debug("New Hibernate Session required");
        if (sessionFactory == null || !sessionFactory.containsKey(db.getId())) {
            rebuildSessionFactory(db);
        }
        return sessionFactory.get(db.getId()).openStatelessSession();
    }

    public static Session getSessionAutoCommit(Store20Database db) throws Exception {
        Configuration configuration = new Configuration();
        fillConfiguration(configuration, db);
        configuration.setProperty("hibernate.connection.autocommit", "true");
        SessionFactory sf = configuration.buildSessionFactory();
        return sf.openSession();
    }


    public static void closeSession(Store20Database db) {
        Session session = (sessions.containsKey(db.getId())) ? sessions.get(db.getId()).get() : null;
        if ((session != null) && (session.isOpen())) {
            session.close();
            sessions.get(db.getId()).set(null);
        }
        log.debug("Hibernate Session closed and discarted from current thread");
    }

    public static void closeSession(Session session) {
        session.close();
        log.debug("Hibernate Session closed");
    }

    public static void rebuildSessionFactory(Store20Database db) throws Exception {
        try {
            log.debug("Full Hibernate Plugin's Session Factory build started...");
            try {
                if (configuration == null) configuration = new HashMap<String, Configuration>();
                configuration.put(db.getId(), new Configuration());
            } catch (Exception e) {
                e.printStackTrace();
            }

            fillConfiguration(configuration.get(db.getId()), db);
            if (sessionFactory == null) sessionFactory = new HashMap<String, SessionFactory>();
            if (sessionFactory.containsKey(db.getId())) {
                if (sessionFactory.get(db.getId()).isClosed()) {
                    sessionFactory.get(db.getId()).close();
                }
                sessionFactory.remove(db.getId());
            }
            sessionFactory.put(db.getId(), configuration.get(db.getId()).buildSessionFactory());

            Session testSession = sessionFactory.get(db.getId()).openSession();
            testSession.connection().getMetaData().getCatalogs();
            testSession.close();

            log.debug("Full Hibernate Plugin's Session Factory built successful");
        } catch (Exception e) {
            if (sessionFactory != null && sessionFactory.containsKey(db.getId())) sessionFactory.remove(db.getId());
            throw e;
        }
    }

    public static void fillConfiguration(Configuration configuration, Store20Database databaseConfig) {
        configuration.addAnnotatedClass(AttributeProd.class);
        configuration.addAnnotatedClass(Banner.class);
        configuration.addAnnotatedClass(Category.class);
        configuration.addAnnotatedClass(CategoryFee.class);
        configuration.addAnnotatedClass(CategoryLang.class);
        configuration.addAnnotatedClass(CategoryProperty.class);
        configuration.addAnnotatedClass(CategoryStaticText.class);
        configuration.addAnnotatedClass(CategoryTree.class);
        configuration.addAnnotatedClass(CategoryUserLevel.class);
        configuration.addAnnotatedClass(CategoryView.class);
        configuration.addAnnotatedClass(CategoryVolume.class);
        configuration.addAnnotatedClass(Codes.class);
        configuration.addAnnotatedClass(ComplementGroup.class);
        configuration.addAnnotatedClass(CustomShippingMethod.class);
        configuration.addAnnotatedClass(CustomShippingMethodRule.class);
        configuration.addAnnotatedClass(Currency.class);
        configuration.addAnnotatedClass(Help.class);
        configuration.addAnnotatedClass(InquiryAnswer.class);
        configuration.addAnnotatedClass(InquiryAnswerUser.class);
        configuration.addAnnotatedClass(InquiryQuestion.class);
        configuration.addAnnotatedClass(Insurance.class);
        configuration.addAnnotatedClass(Job.class);
        configuration.addAnnotatedClass(Fee.class);
        configuration.addAnnotatedClass(LocalizedText.class);
        configuration.addAnnotatedClass(LocationStore.class);
        configuration.addAnnotatedClass(Mail.class);
        configuration.addAnnotatedClass(Menu.class);
        configuration.addAnnotatedClass(Manufacturer.class);
        configuration.addAnnotatedClass(Order.class);
        configuration.addAnnotatedClass(OrderDetail.class);
        configuration.addAnnotatedClass(OrderDetailProduct.class);
        configuration.addAnnotatedClass(OrderHistory.class);
        configuration.addAnnotatedClass(OrderPacking.class);
        configuration.addAnnotatedClass(OrderPackingProduct.class);
        configuration.addAnnotatedClass(OrderPayment.class);
        configuration.addAnnotatedClass(OrderStatus.class);
        configuration.addAnnotatedClass(Payterms.class);
        configuration.addAnnotatedClass(Product.class);
        configuration.addAnnotatedClass(ProductAuditStock.class);
        configuration.addAnnotatedClass(ProductCompetition.class);
        configuration.addAnnotatedClass(ProductDatetime.class);
        configuration.addAnnotatedClass(ProductLabel.class);
        configuration.addAnnotatedClass(ProductLang.class);
        configuration.addAnnotatedClass(ProductOffer.class);
        configuration.addAnnotatedClass(ProductProperty.class);
        configuration.addAnnotatedClass(ProductProvider.class);
        configuration.addAnnotatedClass(ProductRelated.class);
        configuration.addAnnotatedClass(ProductReview.class);
        configuration.addAnnotatedClass(ProductStaticText.class);
        configuration.addAnnotatedClass(ProductUserLevel.class);
        configuration.addAnnotatedClass(ProductUserTax.class);
        configuration.addAnnotatedClass(ProductVariation.class);
        configuration.addAnnotatedClass(ProductVolume.class);
        configuration.addAnnotatedClass(PromotionalCode.class);
        configuration.addAnnotatedClass(Provider.class);
        configuration.addAnnotatedClass(PurchaseHistory.class);
        configuration.addAnnotatedClass(PurchaseOrder.class);
        configuration.addAnnotatedClass(PurchaseOrderLine.class);
        configuration.addAnnotatedClass(Resource.class);
        configuration.addAnnotatedClass(Rma.class);
        configuration.addAnnotatedClass(RmaLog.class);
        configuration.addAnnotatedClass(RmaType.class);
        configuration.addAnnotatedClass(ShippingMethod.class);
        configuration.addAnnotatedClass(ShippingRate.class);
        configuration.addAnnotatedClass(ShopCart.class);
        configuration.addAnnotatedClass(ShopCartItem.class);
        configuration.addAnnotatedClass(State.class);
        configuration.addAnnotatedClass(StaticText.class);
        configuration.addAnnotatedClass(StaticTextLang.class);
        configuration.addAnnotatedClass(StoreProperty.class);
        configuration.addAnnotatedClass(Tax.class);
        configuration.addAnnotatedClass(TaxPerFamily.class);
        configuration.addAnnotatedClass(User.class);
        configuration.addAnnotatedClass(UserAddress.class);
        configuration.addAnnotatedClass(UserAdminRole.class);
        configuration.addAnnotatedClass(UserComment.class);
        configuration.addAnnotatedClass(UserFriends.class);
        configuration.addAnnotatedClass(UserLevel.class);
        configuration.addAnnotatedClass(UserMessages.class);
        configuration.addAnnotatedClass(UserNote.class);
        configuration.addAnnotatedClass(UserPreference.class);
        configuration.addAnnotatedClass(UserRewardHistory.class);
        configuration.addAnnotatedClass(UserVisit.class);
        configuration.addAnnotatedClass(UserWishList.class);
        configuration.addAnnotatedClass(VMTemplate.class);
        if (databaseConfig != null && databaseConfig.getExtraBeans() != null)
            for (Class c : databaseConfig.getExtraBeans()) configuration.addAnnotatedClass(c);

        configuration.setProperty("hibernate.show_sql", "false");
        configuration.setProperty("hibernate.generate_statistics", "true");
        configuration.setProperty("hibernate.current_session_context_class", "thread");
        configuration.setProperty("hibernate.hbm2ddl.auto", "update");

        configuration.setProperty("hibernate.c3p0.max_size", "100");
        configuration.setProperty("hibernate.c3p0.min_size", "3");
        configuration.setProperty("hibernate.c3p0.timeout", "5000");
        configuration.setProperty("hibernate.c3p0.max_statements", "0");
        configuration.setProperty("hibernate.c3p0.idle_test_period", "150");
        configuration.setProperty("hibernate.c3p0.acquire_increment", "2");

        if (databaseConfig != null) configuration.addProperties(databaseConfig.getConnectionProperties());

        if (databaseConfig != null && databaseConfig.getProperties() != null && !databaseConfig.getProperties().isEmpty()) {
            configuration.addProperties(databaseConfig.getProperties());
        }

        StoreHibernateEventListener storeMainListener = new StoreHibernateEventListener();
        configuration.getEventListeners().setPostLoadEventListeners(
                new PostLoadEventListener[]{storeMainListener, new DefaultPostLoadEventListener()}
        );
        configuration.getEventListeners().setPreInsertEventListeners(new PreInsertEventListener[]{storeMainListener});
        configuration.getEventListeners().setPostInsertEventListeners(new PostInsertEventListener[]{storeMainListener});
        configuration.getEventListeners().setPreUpdateEventListeners(new PreUpdateEventListener[]{storeMainListener});
        configuration.getEventListeners().setPostUpdateEventListeners(new PostUpdateEventListener[]{storeMainListener});
        configuration.getEventListeners().setPreDeleteEventListeners(new PreDeleteEventListener[]{storeMainListener});
        configuration.getEventListeners().setPostDeleteEventListeners(new PostDeleteEventListener[]{storeMainListener});

        configuration.setInterceptor(new ProductInterceptor());
    }

    public static void destroyFactory() {
        log.debug("Full Hibernate Plugin's Session Factory: destroy factory required...");
        if (sessionFactory != null && !sessionFactory.isEmpty()) {
            for (SessionFactory sf : sessionFactory.values()) sf.close();
            sessionFactory.clear();
        }
        log.debug("Full Hibernate Plugin's Session Factory: Destroied sucessful");
    }

    public static SessionFactory getSessionFactory(Store20Database db) {
        return (sessionFactory != null && sessionFactory.containsKey(db.getId())) ? sessionFactory.get(db.getId()) : null;
    }

}