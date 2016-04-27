package org.store.core.front;

import org.store.core.beans.AttributeProd;
import org.store.core.beans.Category;
import org.store.core.beans.Product;
import org.store.core.beans.ProductProperty;
import org.store.core.beans.utils.BreadCrumb;
import org.store.core.globals.StoreMessages;
import org.store.core.globals.StoreSessionInterceptor;
import org.apache.commons.lang.StringUtils;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;

import java.util.ArrayList;
import java.util.List;

@Namespace(value = "/")
@ParentPackage(value = "store-front")
public class CompareAction extends FrontModuleAction implements StoreMessages {

    public static final String COMPARE_PRODUCTS = "compare.products";

    @Action(value = "compareIndex", results = {@Result(type = "velocity", location = "/WEB-INF/views/front/compareIndex.vm")})
    public String index() throws Exception {
        List<Product> products = new ArrayList<Product>();
        for (Long id : getComparedProductsId()) {
            Product product = (Product) dao.get(Product.class, id);
            if (product != null && product.canShow()) {
                products.add(product);
            }
        }
        addToStack("products", products);
        return SUCCESS;
    }

    @Action(value = "compare", results = {@Result(type = "velocity", location = "/WEB-INF/views/front/compare.vm")})
    public String compare() throws Exception {
        if (removeId != null) {
            List<Long> comparedProduct = getComparedProductsId();
            if (comparedProduct.contains(removeId)) comparedProduct.remove(removeId);
        }

        List<Product> products = new ArrayList<Product>();
        List<AttributeProd> attributes = new ArrayList<AttributeProd>();
        for (Long id : getComparedProductsId()) {
            Product product = (Product) dao.get(Product.class, id);
            if (product != null && product.canShow()) {
                products.add(product);
                List<ProductProperty> listProp = dao.getProductProperties(product);
                for (ProductProperty pp : listProp) {
                    if (StringUtils.isNotEmpty(pp.getPropertyValue())) product.addProperty("att_" + pp.getAttribute().getId(), pp.getPropertyValue());
                    if (!attributes.contains(pp.getAttribute())) attributes.add(pp.getAttribute());
                }
            }
        }
        if (!products.isEmpty()) {
            addToStack("products", products);
            addToStack("attributes", attributes);
            getBreadCrumbs().add(new BreadCrumb("page", getText("products.comparison"), null, null));
            return SUCCESS;
        } else {
            return "home";
        }
    }

    @Action(value = "compareAdd", results = {@Result(type = "velocity", location = "/WEB-INF/views/front/compareIndex.vm")})
    public String addProduct() throws Exception {
        if (idProduct != null) {
            List<Long> comparedProduct = getComparedProductsId();
            if (comparedProduct.size()<4) {
                Product product = (Product) dao.get(Product.class, idProduct);
                if (product != null && product.canShow() && !comparedProduct.contains(idProduct)) comparedProduct.add(idProduct);
            } else {
                addToStack("maxExceed", Boolean.TRUE);
            }
        }
        return index();
    }

    @Action(value = "compareDel", results = {@Result(type = "velocity", location = "/WEB-INF/views/front/compareIndex.vm")})
    public String delProduct() throws Exception {
        if (idProduct != null) {
            List<Long> comparedProduct = getComparedProductsId();
            if (comparedProduct.contains(idProduct)) comparedProduct.remove(idProduct);
        }
        return index();
    }

    @Action(value = "compareBack")
     public String backToCategory() throws Exception {
        Category category = null;
        if (getStoreSessionObjects().containsKey(StoreSessionInterceptor.CNT_LAST_CATEGORY)) {
            category = dao.getCategory((Long) getStoreSessionObjects().get(StoreSessionInterceptor.CNT_LAST_CATEGORY));
        }
        if (category==null) {
            List<Product> products = getComparedProducts();
            if (products!=null && !products.isEmpty()) category = (products.get(0)).getCategory();
        }
        if (category!=null) {
            redirectUrl = urlCategory(category);
            return "redirectUrl";
        } else {
            return "home";
        }
    }
    
    private List<Long> getComparedProductsId() {
        List<Long> list;
        if (getStoreSessionObjects().containsKey(COMPARE_PRODUCTS)) list = (List<Long>) getStoreSessionObjects().get(COMPARE_PRODUCTS);
        else {
            list = new ArrayList<Long>();
            getStoreSessionObjects().put(COMPARE_PRODUCTS, list);
        }
        return list;
    }

    private Long idProduct;
    private Long removeId;

    public Long getIdProduct() {
        return idProduct;
    }

    public void setIdProduct(Long idProduct) {
        this.idProduct = idProduct;
    }

    public Long getRemoveId() {
        return removeId;
    }

    public void setRemoveId(Long removeId) {
        this.removeId = removeId;
    }
}
