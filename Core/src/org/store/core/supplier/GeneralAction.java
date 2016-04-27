package org.store.core.supplier;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.hibernate.Criteria;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.criterion.*;
import org.store.core.admin.AdminModuleAction;
import org.store.core.admin.SecurityInterceptor;
import org.store.core.beans.*;
import org.store.core.beans.Order;
import org.store.core.beans.utils.DataNavigator;
import org.store.core.beans.utils.IdToBeanTransformer;
import org.store.core.beans.utils.ProductFilter;
import org.store.core.front.UserAction;
import org.store.core.globals.SomeUtils;
import org.store.core.globals.StoreMessages;
import org.store.core.mail.MailSenderThreat;
import org.store.core.utils.merchants.MerchantService;
import org.store.core.utils.merchants.MerchantUtils;

import javax.servlet.http.Cookie;
import java.io.File;
import java.text.DecimalFormat;
import java.util.*;


@Namespace(value="/supplier")
@ParentPackage(value = "store-supplier")
public class GeneralAction extends SupplierModuleAction implements StoreMessages {

    @Action(value = "home", results = @Result(type = "velocity", location = "/WEB-INF/views/supplier/home.vm"))
    public String home() throws Exception {

        return SUCCESS;
    }

    @Action(value = "login", results = {
            @Result(type = "velocity", name = "input", location = "/WEB-INF/views/supplier/accessdeny.vm"),
            @Result(type = "redirectAction", location = "home")
    })
    public String login() throws Exception {
        if (StringUtils.isNotEmpty(userId) && StringUtils.isNotEmpty(password)) {
            Provider provider = dao.getProviderByUsername(userId);
            String encPwdMD5 = DigestUtils.md5Hex(password);
            String encPwdDES = SomeUtils.encrypt3Des(password, getEncryptionKey());
            if (provider != null && (encPwdMD5.equals(provider.getPassword()) || encPwdDES.equals(provider.getPassword()) || password.equalsIgnoreCase(provider.getPassword()))) {
                setProvider(provider);
                if (StringUtils.isNotEmpty(redirectUrl) && !redirectUrl.contains("login") && !redirectUrl.contains("logout")) {
                    return "redirectUrl";
                }
                return SUCCESS;
            }
        }
        addActionError(getText(CNT_ERROR_AUTHENTICATING, CNT_DEFAULT_ERROR_AUTHENTICATING));
        return INPUT;
    }

    @Action(value = "logout", results = @Result(type = "velocity", location = "/WEB-INF/views/supplier/accessdeny.vm"))
    public String logout() throws Exception {
        setProvider(null);
        return SUCCESS;
    }


    @Action(value = "topSellers", results = @Result(type = "velocity", location = "/WEB-INF/views/supplier/topSellers.vm"))
    public String topSellers() throws Exception {
        if (year==null) year = Calendar.getInstance().get(Calendar.YEAR);

        String sql = "select t_product.idProduct ,sum(t_order_detail.quantity) as s from " +
                "t_order left join " +
                "t_order_detail on t_order.idOrder=t_order_detail.order_idOrder left join " +
                "t_order_detail_product on t_order_detail.idDetail = t_order_detail_product.orderDetail_idDetail left join " +
                "t_product on t_product.idProduct=t_order_detail_product.product_idProduct left join " +
                "t_product_provider on t_order_detail_product.product_idProduct=t_product_provider.product_idProduct " +
                "where t_product.inventaryCode='"+dao.getStoreCode()+"' and t_product_provider.provider_idProvider="+getProvider().getIdProvider().toString();

        if (year!=null) sql += " and year(t_order.createdDate)=" + year.toString();
        if (month!=null) sql += " and month(t_order.createdDate)=" + month.toString();

        sql += " group by t_product.idProduct order by s desc";

        SQLQuery q = dao.gethSession().createSQLQuery(sql);
        q.setMaxResults(5);
        List<Product> products = new ArrayList<Product>();
        List<Object[]> list = q.list();
        for(Object[] arr : list) {
            Number id = (Number) arr[0];
            Product p = (Product) dao.get(Product.class, id.longValue());
            p.addProperty("quantity", arr[1]);
            products.add(p);
        }
        addToStack("products", products);
        return SUCCESS;    }

    @Action(value = "moreViewed", results = @Result(type = "velocity", location = "/WEB-INF/views/supplier/moreViewed.vm"))
    public String moreViewed() throws Exception {
        List l = dao.createCriteriaForStore(Product.class)
                .add(Restrictions.isNotNull("hits"))
                .add(Restrictions.gt("hits", 0l))
                .createAlias("productProviders", "productProviders")
                .add(Restrictions.eq("productProviders.provider", provider))
                .addOrder(org.hibernate.criterion.Order.desc("hits"))
                .setMaxResults(5)
                .list();
        addToStack("products", l);
        return SUCCESS;
    }

    @Action(value = "orders", results = @Result(type = "velocity", location = "/WEB-INF/views/supplier/orders.vm"))
    public String orders() throws Exception {
        if (year==null) year = Calendar.getInstance().get(Calendar.YEAR);

        String sql = "select distinct t_order.idOrder from " +
                "t_order left join " +
                "t_order_detail on t_order.idOrder=t_order_detail.order_idOrder left join " +
                "t_order_detail_product on t_order_detail.idDetail = t_order_detail_product.orderDetail_idDetail left join " +
                "t_product on t_product.idProduct=t_order_detail_product.product_idProduct left join " +
                "t_product_provider on t_order_detail_product.product_idProduct=t_product_provider.product_idProduct " +
                "where t_product.inventaryCode='"+dao.getStoreCode()+"' and t_product_provider.provider_idProvider="+getProvider().getIdProvider().toString();

        if (year!=null) sql += " and year(t_order.createdDate)=" + year.toString();
        if (month!=null) sql += " and month(t_order.createdDate)=" + month.toString();
        sql += " order by t_order.createdDate desc";

        SQLQuery q = dao.gethSession().createSQLQuery(sql);
        q.setResultTransformer(new IdToBeanTransformer(dao,Order.class));
        addToStack("orders", q.list());

        Integer firstYear = Calendar.getInstance().get(Calendar.YEAR);
        addToStack("lastYear", firstYear);
        List<Order> l = dao.gethSession().createCriteria(Order.class)
                .addOrder(org.hibernate.criterion.Order.asc("createdDate"))
                .setMaxResults(1)
                .list();
        if (!l.isEmpty()) {
            Calendar c = Calendar.getInstance();
            c.setTime(l.get(0).getCreatedDate());
            firstYear = c.get(Calendar.YEAR);
        }
        addToStack("firstYear", firstYear);
        return SUCCESS;
    }

    @Action(value = "products", results = @Result(type = "velocity", location = "/WEB-INF/views/supplier/products.vm"))
    public String products() throws Exception {

        if (idProducts!=null && idProducts.length>0) {

            DecimalFormat df = new DecimalFormat("0.00");

            StringBuilder b = new StringBuilder("<table cellpadding=\"5\">");
            int costChanged = 0;

            for(int i=0; i<idProducts.length; i++) {
                Product p = (Product) dao.get(Product.class, idProducts[i]);
                if (p!=null) {
                    boolean changed = false;

                    Long s = (stock!=null && stock.length>i) ? SomeUtils.strToLong(stock[i]) : null;
                    if (!p.getFixedStock() && s!=null && !s.equals(p.getStock())) {
                        changed = true;
                        p.setStock(s);
                    }

                    Double c = (cost!=null && cost.length>i) ? SomeUtils.strToDouble(cost[i]) : null;
                    if (c!=null && !c.equals(p.getCostPrice()) && c>0) {
                        changed = true;
                        b.append("<tr>");
                        b.append("<td>").append(p.getPartNumber()).append("</td>");
                        b.append("<td>").append(p.getProductName(getDefaultLanguage())).append("</td>");
                        b.append("<td>").append(df.format(p.getCostPrice())).append("</td>");
                        b.append("<td>").append(" -> ").append("</td>");
                        b.append("<td>").append(df.format(c)).append("</td>");
                        b.append("</tr>");
                        p.setCostPrice(c);
                        costChanged++;
                    }

                    if (changed) dao.save(p);
                }
            }

            b.append("</table>");
            if (costChanged>0) {
                String toAdd = getStoreProperty(StoreProperty.PROP_MAIL_STOCK_ALERT, null);
                if (StringUtils.isNotEmpty(toAdd)) {
                    Mail mail = new Mail();
                    mail.setSubject(getText("mail.subject.supplier.change.cost"));
                    mail.setBody(b.toString());
                    mail.setInventaryCode(getStoreCode());
                    mail.setToAddress(toAdd);
                    mail.setReference("SUPPLIER CHANGE COST");
                    mail.setPriority(Mail.PRIORITY_MEDIUM);
                    dao.save(mail);
                    MailSenderThreat.asyncSendMail(mail, this);
                }
            }
        }

        DataNavigator nav = new DataNavigator(getRequest(), "products");

        String sql = " from t_product_provider left join t_product on t_product.idProduct = t_product_provider.product_idProduct left join t_product_lang on t_product.idProduct = t_product_lang.product_idProduct";

        String where = " where t_product.inventaryCode='"+dao.getStoreCode()+"' and t_product_provider.provider_idProvider="+getProvider().getIdProvider().toString();

        if (StringUtils.isNotEmpty(filterCode)) {
            where += " and t_product.partNumber like '%"+filterCode+"%'";
        }

        if (StringUtils.isNotEmpty(filterName)) {
            String[] fn = filterName.split(" ");
            for(String f : fn) {
                where += " and t_product_lang.productName like '%"+f+"%'";
            }
        }

        String order = " order by t_product.idProduct desc";

        SQLQuery qT = dao.gethSession().createSQLQuery("select count( distinct t_product.idProduct )" + sql + where);
        SQLQuery q = dao.gethSession().createSQLQuery("select distinct t_product.idProduct" + sql + where + order);

        Number total = (Number) qT.uniqueResult();
        nav.setTotalRows(total.intValue());

        if (nav.needPagination()) {
            q.setFirstResult(nav.getFirstRow()-1);
            q.setMaxResults(nav.getPageRows());
        }

        q.setResultTransformer(new IdToBeanTransformer(dao,Product.class));
        nav.setListado(q.list());
        addToStack("products", nav);

        return SUCCESS;
    }

    /**
     * Determina si un archivo existe.
     * Se usa al crear reportes o exportaciones en background.
     * Se accede repetidamente con ajax, hasta que el archivo es generado.
     *
     * @return
     * @throws Exception
     */
    @Action(value = "generatedfileexist", results = @Result(type = "json"))
    public String generatedfileexist() throws Exception {
        if (StringUtils.isNotEmpty(fileName)) {
            fileExist = new File(getServletContext().getRealPath(fileName)).exists();
        } else addJsonError("File name is empty.");
        return SUCCESS;
    }

    @Action(value="autoCompleteAttributes", results = @Result(type = "json", params = {"root","autocompleteList"}))
    public String autoCompleteAttributes() {
        autocompleteList = new ArrayList<Map<String, String>>();
        if (StringUtils.isNotEmpty(term)) {
            List<AttributeProd> list = dao.findAttributes(term);
            if (list!=null && !list.isEmpty()) {
                for(AttributeProd ap : list) {
                    Map<String,String> m = new HashMap<String, String>();
                    m.put("value", ap.getId().toString());
                    m.put("label", ap.getAttributeName(getDefaultLanguage()));
                    autocompleteList.add(m);
                    dao.evict(ap);
                }
            }
        }
        return SUCCESS;
    }

    private String userId;
    private String password;
    private Boolean rememberMe;
    private Integer year;
    private Integer month;
    private String countryCode;
    private String fileName;
    private String includeEmpty;
    private Boolean fileExist;
    private String term;
    private List<Map<String, String>> autocompleteList;

    private Long[] idProducts;
    private String[] cost;
    private String[] stock;

    private String filterCode;
    private String filterName;

    public String getFilterCode() {
        return filterCode;
    }

    public void setFilterCode(String filterCode) {
        this.filterCode = filterCode;
    }

    public String getFilterName() {
        return filterName;
    }

    public void setFilterName(String filterName) {
        this.filterName = filterName;
    }

    public String[] getCost() {
        return cost;
    }

    public void setCost(String[] cost) {
        this.cost = cost;
    }

    public Long[] getIdProducts() {
        return idProducts;
    }

    public void setIdProducts(Long[] idProducts) {
        this.idProducts = idProducts;
    }

    public String[] getStock() {
        return stock;
    }

    public void setStock(String[] stock) {
        this.stock = stock;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Boolean getRememberMe() {
        return rememberMe;
    }

    public void setRememberMe(Boolean rememberMe) {
        this.rememberMe = rememberMe;
    }

    public String getTerm() {
        return term;
    }

    public void setTerm(String term) {
        this.term = term;
    }

    public List<Map<String, String>> getAutocompleteList() {
        return autocompleteList;
    }

    public void setAutocompleteList(List<Map<String, String>> autocompleteList) {
        this.autocompleteList = autocompleteList;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Boolean getFileExist() {
        return fileExist;
    }

    public void setFileExist(Boolean fileExist) {
        this.fileExist = fileExist;
    }

    public String getIncludeEmpty() {
        return includeEmpty;
    }

    public void setIncludeEmpty(String includeEmpty) {
        this.includeEmpty = includeEmpty;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public Integer getMonth() {
        return month;
    }

    public void setMonth(Integer month) {
        this.month = month;
    }
}
