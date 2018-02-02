package org.store.core.utils.suppliers;

import org.store.core.beans.Product;
import org.store.core.beans.ProductAuditStock;
import org.store.core.beans.ProductProvider;
import org.store.core.beans.StoreProperty;
import org.store.core.dao.HibernateDAO;
import org.store.core.globals.config.Store20Database;
import org.store.core.hibernate.HibernateSessionFactory;
import org.store.core.utils.quartz.JobStoreThread;
import org.apache.commons.lang.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;


public class SupplierUtils extends JobStoreThread {

    private Map<String, Class> mapServices;
    private boolean useCombinedStock;
    private Long onlyProduct;

    public SupplierUtils(Map<String, Class> mapServices, Session session, String storeCode, Store20Database databaseConfig) {
        this.mapServices = mapServices;
        this.storeCode = storeCode;
        this.databaseConfig = databaseConfig;
        HibernateDAO dao = new HibernateDAO(session, storeCode);
        useCombinedStock = "Y".equalsIgnoreCase(dao.getStorePropertyValue(StoreProperty.PROP_USE_COMBINED_STOCK, StoreProperty.TYPE_GENERAL, StoreProperty.PROP_DEFAULT_USE_COMBINED_STOCK));
        setName("supplier_task_" + String.valueOf(Calendar.getInstance().getTimeInMillis()));
    }

    public void setOnlyProduct(Long onlyProduct) {
        this.onlyProduct = onlyProduct;
    }

    public SupplierService getService(String name, Properties p) {
        if (mapServices != null && mapServices.containsKey(name)) {
            try {
                Object obj = mapServices.get(name).newInstance();
                if (obj instanceof SupplierService) {
                    ((SupplierService) obj).setProperties(p);
                    return (SupplierService) obj;
                }
            } catch (InstantiationException e) {
                log.error(e.getMessage(), e); 
            } catch (IllegalAccessException e) {
                log.error(e.getMessage(), e); 
            }
        }
        return null;
    }

    @Override
    public void run() {
        List<Number> productsToProccess = new ArrayList<Number>();
        if (onlyProduct != null) {
            productsToProccess.add(onlyProduct);
        } else {
            try {
                Session session = HibernateSessionFactory.getNewSession(databaseConfig);
                Transaction tx = session.beginTransaction();
                try {
                    Criteria cri = session.createCriteria(Product.class);
                    cri.add(Restrictions.eq("inventaryCode", storeCode));
                    cri.setProjection(Projections.property("idProduct"));
                    List<Number> list = cri.list();
                    if (list!=null) productsToProccess.addAll(list);
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

        if (!productsToProccess.isEmpty()) {
            int index = 0;
            int totalProd = productsToProccess.size();
            for(Number idProduct: productsToProccess) {
                setExecutionPercent(100d * index++ / totalProd);
                setExecutionMessage("Processing product: " + String.valueOf(index) + "/" + String.valueOf(totalProd));
                if (idProduct!=null) processProduct(idProduct.longValue());
            }
            setExecutionMessage("");
            setExecutionPercent(100d);
        }


    }

    public void processProduct(Long idProduct) {
        try {
            Session session = HibernateSessionFactory.getNewSession(databaseConfig);
            Transaction tx = session.beginTransaction();
            try {
                Product product = (Product) session.get(Product.class, idProduct);
                if (product!=null) {
                    setExecutionMessage(getExecutionMessage() + " (Code: "+product.getPartNumber()+")");
                    processProduct(product, session);
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

    public void processProduct(Product product, Session session) {
        List<ProductProvider> list = session.createCriteria(ProductProvider.class)
                .add(Restrictions.eq("product", product))
                .add(Restrictions.eq("active", Boolean.TRUE))
                .createCriteria("provider", Criteria.LEFT_JOIN).add(Restrictions.or(Restrictions.isNotNull("serviceName"), Restrictions.ne("serviceName", "")))
                .list();

        if (list != null && !list.isEmpty()) {
            Long fullStock = 0l;
            Long stock = null;
            Date eta = null;
            Double betterPriceOfAll = null;
            Double betterPriceWithStock = null;
            Double betterPriceWithETA = null;
            Long oldStock = product.getStock();
            int index = 0, totalProv = list.size();
            for (ProductProvider bean : list) {
                if (bean.getActive() && bean.getProvider() != null && StringUtils.isNotEmpty(bean.getProvider().getServiceName())) {

                    if (onlyProduct != null) {
                        setExecutionMessage("Processing Supplier: " + bean.getProvider().getServiceName());
                        setExecutionPercent(100d * index++ / totalProv);
                    }

                    proccessProductProvider(bean, session);
                    if (bean.getCostPrice() != null && bean.getCostPrice()>0) {
                        if (betterPriceOfAll == null) betterPriceOfAll = (bean.getCostCurrency()!=null) ? bean.getCostCurrency().getReverseRatio() * bean.getCostPrice() : bean.getCostPrice();
                        else if (betterPriceOfAll > bean.getCostPrice()) betterPriceOfAll = (bean.getCostCurrency()!=null) ? bean.getCostCurrency().getReverseRatio() * bean.getCostPrice() : bean.getCostPrice();
                    }
                    if (bean.getStock() != null && bean.getStock() > 0 && bean.getCostPrice() != null && bean.getCostPrice() > 0) {
                        if (betterPriceWithStock == null) {
                            betterPriceWithStock = (bean.getCostCurrency()!=null) ? bean.getCostCurrency().getReverseRatio() * bean.getCostPrice() : bean.getCostPrice();
                            stock = bean.getStock();
                        } else if (bean.getCostPrice() < betterPriceWithStock) {
                            betterPriceWithStock = (bean.getCostCurrency()!=null) ? bean.getCostCurrency().getReverseRatio() * bean.getCostPrice() : bean.getCostPrice();
                            stock = bean.getStock();
                        }
                        fullStock += bean.getStock();
                    }
                    if (bean.getEta() != null && bean.getCostPrice() > 0) {
                        if (betterPriceWithETA == null) {
                            betterPriceWithETA = (bean.getCostCurrency()!=null) ? bean.getCostCurrency().getReverseRatio() * bean.getCostPrice() : bean.getCostPrice();
                            eta = bean.getEta();
                        } else if (bean.getEta().before(eta)) {
                            betterPriceWithETA = (bean.getCostCurrency()!=null) ? bean.getCostCurrency().getReverseRatio() * bean.getCostPrice() : bean.getCostPrice();
                            eta = bean.getEta();
                        }
                    }
                }
            }
            if (betterPriceWithStock != null && stock != null && stock > 0) {
                product.setCostPrice(betterPriceWithStock);
                if (!product.getFixedStock()) product.setStock((useCombinedStock) ? fullStock : stock);
                product.setEta(null);
            } else if (betterPriceWithETA != null && eta != null) {
                product.setCostPrice(betterPriceWithETA);
                if (!product.getFixedStock()) product.setStock(0l);
                product.setEta(eta.toString());
            } else {
                if (!product.getFixedStock()) product.setStock(0l);
                if (betterPriceOfAll != null) product.setCostPrice(betterPriceOfAll);
                product.setEta(null);
            }
            session.saveOrUpdate(product);

            if (!oldStock.equals(product.getStock())) {
                ProductAuditStock bean = new ProductAuditStock();
                bean.setProduct(product);
                bean.setStock(product.getStock());
                bean.setChangeDate(Calendar.getInstance().getTime());
                bean.setDescription("Automatically updated by supplier task");
                session.save(bean);
            }

            session.flush();
        }
        if (onlyProduct != null) {
            setExecutionMessage("");
            setExecutionPercent(100d);
        }
    }

    public void proccessProductProvider(ProductProvider bean, Session session) {
        if (bean.getProvider() != null && StringUtils.isNotEmpty(bean.getProvider().getServiceName())) {
            org.store.core.utils.suppliers.SupplierService service = getService(bean.getProvider().getServiceName(), bean.getProvider().getServiceProperties());
            if (service != null) {
                AvailabilityResponse response = null;
                try {
                    response = service.requestAvailability(bean.getPartNumberForSuppliers(), bean.getSku());
                    if (response != null) {
                        bean.setWarehouses(response.getAllStock());
                        if (org.store.core.utils.suppliers.AvailabilityResponse.STATUS_ACTIVE.equalsIgnoreCase(response.getStatus())) {
                            bean.setStock(response.getTotalStock());
                            bean.setCostPrice(response.getPrice());
                            bean.setEta(response.getNextEta());
                            if (StringUtils.isNotEmpty(response.getCurrency())) {
                                HibernateDAO dao = new HibernateDAO(session, bean.getProduct().getInventaryCode());
                                bean.setCostCurrency(dao.getCurrency(response.getCurrency()));
                            }
                            if (StringUtils.isNotEmpty(response.getSku())) bean.setSku(response.getSku());
                            bean.setLastError(null);
                        } else {
                            bean.setStock(0l);
                            bean.setEta(null);
                            bean.setLastError(response.getError());
                        }
                    } else {
                        bean.setWarehouses(null);
                        bean.setLastError("Error connecting supplier service.");
                    }
                    bean.setLastUpdate(Calendar.getInstance().getTime());
                    session.save(bean);
                } catch (Exception e) {
                    log.error(e.getMessage(), e); 
                }
            }
        }
    }

}
