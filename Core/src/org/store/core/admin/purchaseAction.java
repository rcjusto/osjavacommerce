package org.store.core.admin;

import org.store.core.beans.LocationStore;
import org.store.core.beans.Mail;
import org.store.core.beans.Payterms;
import org.store.core.beans.Product;
import org.store.core.beans.ProductProvider;
import org.store.core.beans.Provider;
import org.store.core.beans.PurchaseHistory;
import org.store.core.beans.PurchaseOrder;
import org.store.core.beans.PurchaseOrderLine;
import org.store.core.beans.StoreProperty;
import org.store.core.beans.utils.BreadCrumb;
import org.store.core.beans.utils.DataNavigator;
import org.store.core.globals.SomeUtils;
import org.store.core.globals.StoreMessages;
import org.store.core.mail.MailSenderThreat;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Namespace(value = "/admin")
@ParentPackage(value = "store-admin")
public class purchaseAction extends AdminModuleAction implements StoreMessages {

    private static final String CNT_SUBJECT_PURCHASE = "mail.subject.purchase";
    private static final String CNT_DEFAULT_SUBJECT_PURCHASE = "Purchase order from {1}";

    @Override
    public void prepare() throws Exception {
        purchase = (PurchaseOrder) dao.get(PurchaseOrder.class, idPurchase);
    }

    public String list() throws Exception {
        if (selecteds != null && selecteds.length > 0) {
            for (Long id : selecteds) {
                PurchaseOrder bean = (PurchaseOrder) dao.get(PurchaseOrder.class, id);
                if (bean != null) {
                    if (PurchaseOrder.STATUS_NEW.equalsIgnoreCase(bean.getStatus()) || StringUtils.isEmpty(bean.getStatus())) {
                        dao.delete(bean);
                    }
                }
            }
            dao.flushSession();
        }

        processFilters();
        DataNavigator purchases = new DataNavigator(getRequest(), "purchases");
        purchases.setListado(dao.getPurchases(purchases, filters, getDefaultLanguage()));
        addToStack("purchases", purchases);
        return SUCCESS;
    }

    public String edit() throws Exception {
        addToStack("locations", dao.getLocationStores(false));
        getBreadCrumbs().add(new BreadCrumb(null, getText("admin.purchase.order.list"), url("listpurchase","/admin"), null));
        getBreadCrumbs().add(new BreadCrumb(null, getText("admin.purchase.order.data"), null, null));
        return SUCCESS;
    }

    @Action(value = "createpurchase", results = @Result(type = "velocity", location = "/WEB-INF/views/admin/purchaseedit.vm"))
    public String create() {
        Provider provider = (Provider) dao.get(Provider.class, idProvider);
        if (provider != null) {

            purchase = new PurchaseOrder();
            purchase.setProvider(provider);

            if (idProduct != null && idProduct.length > 0) {
                for (int i = 0; i < idProduct.length; i++) {
                    Product product = (Product) dao.get(Product.class, idProduct[i]);
                    ProductProvider pp = (product != null && purchase.getProvider() != null) ? dao.getProductProvider(product, purchase.getProvider()) : null;
                    if (pp != null) {
                        PurchaseOrderLine pol = new PurchaseOrderLine();
                        pol.setPurchaseOrder(purchase);
                        pol.setProduct(product);
                        Integer q = (quantity != null && quantity.length > i) ? SomeUtils.strToInteger(quantity[i]) : 0;
                        pol.setQuantity((q != null && q > 0) ? q : 1);
                        if (pp.getCostPrice() != null && pp.getCostPrice() > 0) pol.setCostPrice((pp.getCostCurrency()!=null && !pp.getCostCurrency().equals(getDefaultCurrency())) ? fromCurrency(pp.getCostPrice(),pp.getCostCurrency()) : pp.getCostPrice());
                        else if (product.getCostPrice() != null) pol.setCostPrice(product.getCostPrice());
                        purchase.addLine(pol);
                    }
                }
            }

            addToStack("locations", dao.getLocationStores(false));

        } else {
            addActionError("El proveedor no existe");
        }
        getBreadCrumbs().add(new BreadCrumb(null, getText("admin.purchase.order.list"), url("listpurchase","/admin"), null));
        getBreadCrumbs().add(new BreadCrumb(null, getText("admin.purchase.order.create"), null, null));
       return SUCCESS;
    }

    private void savePurchase() {
        purchase.setInventaryCode(getStoreCode());
        if (purchase.getRegisterDate() == null) purchase.setRegisterDate(Calendar.getInstance().getTime());

        if (StringUtils.isEmpty(purchase.getStatus())) purchase.setStatus(PurchaseOrder.STATUS_NEW);

        Provider provider = (Provider) dao.get(Provider.class, idProvider);
        if (provider != null) purchase.setProvider(provider);

        LocationStore location = (LocationStore) dao.get(LocationStore.class, idLocation);
        if (location != null) purchase.setLocationStore(location);

        Payterms payterms = (Payterms) dao.get(Payterms.class, idPayterms);
        if (payterms!=null) purchase.setPayterms(payterms);

        purchase.setRequiredDate(SomeUtils.strToDate(requiredDate, getDefaultLanguage()));
        purchase.setDatePaid(SomeUtils.strToDate(datePaid, getDefaultLanguage()));

        dao.save(purchase);

        // items
        if (idProduct != null && idProduct.length > 0) {
            for (int i = 0; i < idProduct.length; i++) {
                PurchaseOrderLine pol = purchase.getLineByIdProduct(idProduct[i]);
                Integer q = (quantity != null && quantity.length > i) ? SomeUtils.strToInteger(quantity[i]) : 0;
                if (pol != null) {
                    if (q != null && q > 0) {
                        pol.setQuantity(q);
                        Double cp = (costPrice != null && costPrice.length > i) ? SomeUtils.strToDouble(costPrice[i]) : null;
                        if (cp != null) pol.setCostPrice(cp);
                        dao.save(pol);
                    } else {
                        purchase.getLines().remove(pol);
                        dao.delete(pol);
                    }
                } else if (PurchaseOrder.STATUS_NEW.equalsIgnoreCase(purchase.getStatus()) && q != null && q > 0) {
                    // si vino un item nuevo, crearlo solo si el purchase esta en NEW
                    Product product = (Product) dao.get(Product.class, idProduct[i]);
                    ProductProvider pp = (product != null && purchase.getProvider() != null) ? dao.getProductProvider(product, purchase.getProvider()) : null;
                    if (pp != null) {
                        pol = new PurchaseOrderLine();
                        pol.setPurchaseOrder(purchase);
                        pol.setProduct(product);
                        pol.setQuantity(q);
                        if (pp.getCostPrice() != null && pp.getCostPrice() > 0) pol.setCostPrice((pp.getCostCurrency()!=null && !pp.getCostCurrency().equals(getDefaultCurrency())) ? fromCurrency(pp.getCostPrice(),pp.getCostCurrency()) : pp.getCostPrice());
                        else if (product.getCostPrice() != null) pol.setCostPrice(product.getCostPrice());
                        dao.save(pol);
                    }
                }
            }
        }
    }

    private void savePurchaseHistory() {
        PurchaseHistory ph = new PurchaseHistory();
        ph.setPurchaseOrder(purchase);
        ph.setHistoryStatus(purchase.getStatus());
        ph.setHistoryDate(Calendar.getInstance().getTime());
        ph.setHistoryUser(getAdminUser().getIdUser());
        ph.setHistoryComment(comments);
        dao.save(ph);
    }

    private void saveEta() {
        // los productos q no tienen stock se les pone el eta
        if (ArrayUtils.contains(new String[]{PurchaseOrder.STATUS_RECEIVED, PurchaseOrder.STATUS_PAID, PurchaseOrder.STATUS_SENDED}, purchase.getStatus())) {
            if (purchase.getRequiredDate() != null && purchase.getRequiredDate().after(Calendar.getInstance().getTime())) {
                for (PurchaseOrderLine pol : purchase.getLines()) {
                    String eta = SomeUtils.dateToStr(purchase.getRequiredDate(), getDefaultLanguage());
                    if (StringUtils.isNotEmpty(eta) && !eta.equalsIgnoreCase(pol.getProduct().getEta())) {
                        pol.getProduct().setEta(eta);
                        dao.save(pol.getProduct());
                    }
                }
            }
        } else if (ArrayUtils.contains(new String[]{PurchaseOrder.STATUS_CANCELED}, purchase.getStatus())) {
            for (PurchaseOrderLine pol : purchase.getLines()) {
                if (StringUtils.isNotEmpty(pol.getProduct().getEta())) {
                    pol.getProduct().setEta(null);
                    dao.save(pol.getProduct());
                }
            }
        }
    }

    public String save() throws Exception {
        if (purchase != null) {
            savePurchase();
            saveEta();
            savePurchaseHistory();

            if ("add_product".equalsIgnoreCase(doAction)) {
                addProduct();
                edit();
                Map<String, String> params = new HashMap<String, String>();
                params.put("idPurchase", purchase.getId().toString());
                setRedirectUrl(url("editpurchase", "admin", params));
                return "redirectUrl";
            } else if ("del_product".equalsIgnoreCase(doAction)) delProduct();
        }
        return SUCCESS;
    }

    @Action(value = "sendpurchase", results = @Result(type = "redirectAction", location = "listpurchase"))
    public String send() throws Exception {
        if (purchase != null) {
            savePurchase();

            if (purchase.getLines() != null && !purchase.getLines().isEmpty()) {
                // cambiar estado
                purchase.setStatus(PurchaseOrder.STATUS_SENDED);
                dao.save(purchase);

                // los productos q no tienen stock se les pone el eta
                saveEta();

                savePurchaseHistory();

                // enviar el email
                if (StringUtils.isNotEmpty(purchase.getProvider().getEmail())) {
                    Mail mail = new Mail();
                    String fromAdd = getStoreProperty(StoreProperty.PROP_EMAIL_PURCHASE_FROM, null);
                    if (StringUtils.isNotEmpty(fromAdd)) mail.setFromAddress(fromAdd);
                    mail.setToAddress(purchase.getProvider().getEmail());
                    mail.setInventaryCode(getStoreCode());
                    mail.setSubject(getText(CNT_SUBJECT_PURCHASE, CNT_DEFAULT_SUBJECT_PURCHASE, new String[]{getStoreProperty(StoreProperty.PROP_SITE_NAME, "")}));
                    mail.setReference("PURCHASE SENT " + purchase.getId());
                    mail.setPriority(Mail.PRIORITY_HIGH);
                    mail.setBody(proccessVelocityTemplate(Mail.MAIL_TEMPLATE_PURCHASE, null));
                    dao.save(mail);
                    MailSenderThreat.asyncSendMail(mail, this);
                }

            } else {
                addActionError("No se puede enviar porque no tiene productos");
            }

        }
        return SUCCESS;
    }


    @Action(value = "receivepurchase", results = @Result(type = "redirectAction", location = "listpurchase"))
    public String receive() throws Exception {
        if (purchase != null) {
            savePurchase();

            boolean changeStatus = false;
            if (idProduct != null && idProduct.length > 0) {
                for (int i = 0; i < idProduct.length; i++) {
                    PurchaseOrderLine pol = purchase.getLineByIdProduct(idProduct[i]);
                    Integer q = null;
                    try {
                        q = (received != null && received.length > i) ? SomeUtils.strToInteger(received[i]) : 0;
                    } catch (Exception e) {
                        q = 0;
                    }
                    if (pol != null) {
                        if (q != null && q > 0) {
                            changeStatus = true;

                            // actualizar la linea
                            pol.addReceived(q);
                            dao.save(pol);

                            // adicionar stock al producto y eliminar el eta
                            pol.getProduct().addStock(q);
                            if (pol.receivedAll()) pol.getProduct().setEta(null);
                            dao.save(pol.getProduct());
                        }
                    }
                }
            }

            if (changeStatus) {
                purchase.setStatus(PurchaseOrder.STATUS_RECEIVED);
                dao.save(purchase);
            }

            savePurchaseHistory();
        }
        return SUCCESS;
    }

    @Action(value = "closepurchase", results = @Result(type = "redirectAction", location = "listpurchase"))
    public String close() throws Exception {
        if (purchase != null && !PurchaseOrder.STATUS_CLOSED.equalsIgnoreCase(purchase.getStatus())) {
            purchase.setStatus(PurchaseOrder.STATUS_CLOSED);
            savePurchase();
            savePurchaseHistory();
        }
        return SUCCESS;
    }

    @Action(value = "paidpurchase", results = @Result(type = "redirectAction", location = "listpurchase"))
    public String paid() throws Exception {
        if (purchase != null && !PurchaseOrder.STATUS_PAID.equalsIgnoreCase(purchase.getStatus())) {
            purchase.setStatus(PurchaseOrder.STATUS_PAID);
            savePurchase();
            savePurchaseHistory();
        }
        return SUCCESS;
    }

    @Action(value = "cancelpurchase", results = @Result(type = "redirectAction", location = "listpurchase"))
    public String cancel() throws Exception {
        if (purchase != null && !PurchaseOrder.STATUS_CANCELED.equalsIgnoreCase(purchase.getStatus())) {
            purchase.setStatus(PurchaseOrder.STATUS_CANCELED);
            savePurchase();
            savePurchaseHistory();
        }
        return SUCCESS;
    }

    @Action(value = "productpurchaseadd", results = @Result(type = "velocity", location = "/WEB-INF/views/admin/purchaseedit_products.vm"))
    public String addProduct() throws Exception {
        if (purchase != null && aProduct != null) {
            boolean old = purchase.getId() != null && purchase.getId() > 0;
            Product product = (Product) dao.get(Product.class, aProduct);
            ProductProvider pp = (product != null && purchase.getProvider() != null) ? dao.getProductProvider(product, purchase.getProvider()) : null;
            if (pp != null) {
                if (!old) {
                    savePurchase();
                    savePurchaseHistory();
                }
                PurchaseOrderLine pol = purchase.getLineByIdProduct(aProduct);
                if (pol == null) {
                    pol = new PurchaseOrderLine();
                    pol.setPurchaseOrder(purchase);
                    if (pp.getCostPrice() != null && pp.getCostPrice() > 0) pol.setCostPrice((pp.getCostCurrency()!=null && !pp.getCostCurrency().equals(getDefaultCurrency())) ? fromCurrency(pp.getCostPrice(),pp.getCostCurrency()) : pp.getCostPrice());
                    else if (product.getCostPrice() != null) pol.setCostPrice(product.getCostPrice());
                    pol.setProduct(product);
                    purchase.addLine(pol);
                }
                Integer q = SomeUtils.strToInteger(aQuantity);
                pol.setQuantity((q != null && q > 0) ? q : 1);
                if (pol.getCostPrice() == null) pol.setCostPrice((pp.getCostCurrency()!=null && !pp.getCostCurrency().equals(getDefaultCurrency())) ? fromCurrency(pp.getCostPrice(),pp.getCostCurrency()) : pp.getCostPrice());
                dao.save(pol);
            }
        }
        return SUCCESS;
    }

    @Action(value = "productpurchasedel", results = @Result(type = "velocity", location = "/WEB-INF/views/admin/purchaseedit_products.vm"))
    public String delProduct() throws Exception {
        if (purchase != null && aProduct != null) {
            PurchaseOrderLine pol = purchase.getLineByIdProduct(aProduct);
            if (pol != null) {
                purchase.getLines().remove(pol);
                dao.delete(pol);
            }
        }
        return SUCCESS;
    }

    @Action(value = "productpurchaseupd", results = @Result(type = "velocity", location = "/WEB-INF/views/admin/purchaseedit_products.vm"))
    public String updProduct() throws Exception {
        if (purchase != null && idProduct != null) {
            if (idProduct.length > 0) {
                for (int i = 0; i < idProduct.length; i++) {
                    PurchaseOrderLine pol = purchase.getLineByIdProduct(idProduct[i]);
                    Integer q = (quantity != null && quantity.length > i) ? SomeUtils.strToInteger(quantity[i]) : 0;
                    if (pol != null) {
                        if (q != null && q > 0) {
                            pol.setQuantity(q);
                            Double cp = (costPrice != null && costPrice.length > i) ? SomeUtils.strToDouble(costPrice[i]) : null;
                            if (cp != null) pol.setCostPrice(cp);
                            dao.save(pol);
                        } else {
                            purchase.getLines().remove(pol);
                            dao.delete(pol);
                        }
                    } else if (PurchaseOrder.STATUS_NEW.equalsIgnoreCase(purchase.getStatus()) && q != null && q > 0) {
                        // si vino un item nuevo, crearlo solo si el purchase esta en NEW
                        Product product = (Product) dao.get(Product.class, idProduct[i]);
                        ProductProvider pp = (product != null && purchase.getProvider() != null) ? dao.getProductProvider(product, purchase.getProvider()) : null;
                        if (pp != null) {
                            pol = new PurchaseOrderLine();
                            pol.setPurchaseOrder(purchase);
                            pol.setProduct(product);
                            pol.setQuantity(q);
                            if (pp.getCostPrice() != null && pp.getCostPrice() > 0) pol.setCostPrice((pp.getCostCurrency()!=null && !pp.getCostCurrency().equals(getDefaultCurrency())) ? fromCurrency(pp.getCostPrice(),pp.getCostCurrency()) : pp.getCostPrice());
                            else if (product.getCostPrice() != null) pol.setCostPrice(product.getCostPrice());
                            dao.save(pol);
                        }
                    }
                }
            }
        }
        return SUCCESS;
    }

    @Action(value = "purchasealerts", results = @Result(type = "velocity", location = "/WEB-INF/views/admin/purchasealerts.vm"))
    public String stockAlerts() throws Exception {

        Map<Provider, List<Product>> alerts = new HashMap<Provider, List<Product>>();
        Map<Long, Long> pendingStock = dao.getPurchaseForReceiveResume();
        List<Product> list = dao.getProductStockAlerts();
        List<Product> withoutSupplier = new ArrayList<Product>();
        for (Product p : list) {
            if (p.getStock()<0) {
                Long requestedPending = (pendingStock!=null && pendingStock.containsKey(p.getIdProduct())) ? pendingStock.get(p.getIdProduct()) : 0;
                p.addProperty("requestedPending", requestedPending);
                p.addProperty("toRequest", Math.max(0,- p.getStock() - requestedPending));

                List<ProductProvider> listPP = dao.getProductProviders(p);
                if (listPP!=null && !listPP.isEmpty()) {
                    for (ProductProvider pp : listPP) {
                        if (alerts.containsKey(pp.getProvider())) {
                            List<Product> l = alerts.get(pp.getProvider());
                            l.add(p);
                        } else {
                            List<Product> l = new ArrayList<Product>();
                            l.add(p);
                            alerts.put(pp.getProvider(), l);
                        }
                    }
                }  else {
                    withoutSupplier.add(p);
                }
            }
        }
        addToStack("alerts", alerts);
        addToStack("withoutSupplier", withoutSupplier);
        getBreadCrumbs().add(new BreadCrumb(null, getText("alerts.for.purchases"), null, null));
        return SUCCESS;
    }

    private PurchaseOrder purchase;
    private Long idProvider;
    private Long idLocation;
    private Long idPayterms;
    private Long idPurchase;
    private String comments;
    private Long[] idProduct;
    private String[] quantity;
    private String[] received;
    private String[] costPrice;
    private Long aProduct;
    private String aQuantity;
    private String doAction;

    private String requiredDate;
    private String datePaid;

    public String getRequiredDate() {
        return requiredDate;
    }

    public void setRequiredDate(String requiredDate) {
        this.requiredDate = requiredDate;
    }

    public String getDatePaid() {
        return datePaid;
    }

    public void setDatePaid(String datePaid) {
        this.datePaid = datePaid;
    }

    public String getDoAction() {
        return doAction;
    }

    public void setDoAction(String doAction) {
        this.doAction = doAction;
    }

    public PurchaseOrder getPurchase() {
        return purchase;
    }

    public void setPurchase(PurchaseOrder purchase) {
        this.purchase = purchase;
    }

    public Long getIdPurchase() {
        return idPurchase;
    }

    public void setIdPurchase(Long idPurchase) {
        this.idPurchase = idPurchase;
    }

    public Long getIdProvider() {
        return idProvider;
    }

    public void setIdProvider(Long idProvider) {
        this.idProvider = idProvider;
    }

    public Long getIdLocation() {
        return idLocation;
    }

    public void setIdLocation(Long idLocation) {
        this.idLocation = idLocation;
    }

    public Long getIdPayterms() {
        return idPayterms;
    }

    public void setIdPayterms(Long idPayterms) {
        this.idPayterms = idPayterms;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public Long[] getIdProduct() {
        return idProduct;
    }

    public void setIdProduct(Long[] idProduct) {
        this.idProduct = idProduct;
    }

    public String[] getQuantity() {
        return quantity;
    }

    public void setQuantity(String[] quantity) {
        this.quantity = quantity;
    }

    public String[] getCostPrice() {
        return costPrice;
    }

    public void setCostPrice(String[] costPrice) {
        this.costPrice = costPrice;
    }

    public String[] getReceived() {
        return received;
    }

    public void setReceived(String[] received) {
        this.received = received;
    }

    public Long getAProduct() {
        return aProduct;
    }

    public void setAProduct(Long aProduct) {
        this.aProduct = aProduct;
    }

    public String getAQuantity() {
        return aQuantity;
    }

    public void setAQuantity(String aQuantity) {
        this.aQuantity = aQuantity;
    }
}
