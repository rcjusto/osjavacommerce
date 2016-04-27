package org.store.core.dto;

import org.store.core.beans.Category;

import java.util.List;


public class CategoryDTO {

    private Long idCategory;
    private String urlCode;
    private String name;
    private Long products;
    private Boolean opened;
    private Integer position;
    private Integer level;
    private List<CategoryDTO> children;
    public static final Integer LAST_POSITION = 9999;

    public CategoryDTO() {
    }

    public CategoryDTO(Category bean, String language) {
        if (bean != null) {
            setIdCategory(bean.getIdCategory());
            setUrlCode(bean.getUrlCode());
            setName(bean.getCategoryName(language));
        }
    }

    public Long getIdCategory() {
        return idCategory;
    }

    public void setIdCategory(Long idCategory) {
        this.idCategory = idCategory;
    }

    public String getUrlCode() {
        return urlCode;
    }

    public void setUrlCode(String urlCode) {
        this.urlCode = urlCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getPosition() {
        return (position != null) ? position : LAST_POSITION;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }

    public Long getProducts() {
        return products;
    }

    public void setProducts(Long products) {
        this.products = products;
    }

    public List<CategoryDTO> getChildren() {
        return children;
    }

    public void setChildren(List<CategoryDTO> children) {
        this.children = children;
    }

    public Boolean getOpened() {
        return opened;
    }

    public void setOpened(Boolean opened) {
        this.opened = opened;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public long getNumProducts() {
        long res = 0;
        if (products != null) res += products;
        if (children != null && children.size() > 0)
            for (CategoryDTO dto : children)
                res += dto.getNumProducts();
        return res;
    }
    
    public boolean hasChildrenId(Long id) {
        if (children != null && children.size() > 0) {
            for (CategoryDTO dto : children) {
                if (id.equals(dto.getIdCategory()) || dto.hasChildrenId(id)) return true;
            }
        }
        return false;
    }
    
}
