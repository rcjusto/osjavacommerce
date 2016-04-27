package org.store.core.beans.utils;

import org.store.core.beans.Category;
import org.store.core.beans.CategoryTree;
import org.store.core.dao.HibernateDAO;
import org.store.core.dto.CategoryDTO;
import org.apache.commons.lang.ArrayUtils;
import org.hibernate.transform.ResultTransformer;

import java.util.ArrayList;
import java.util.List;

public class CategoryTransformer implements ResultTransformer {

    private HibernateDAO dao;
    private String language;

    public CategoryTransformer(String lang, HibernateDAO d) {
        this.language = lang;
        this.dao = d;
    }

    public Object transformTuple(Object[] objects, String[] strings) {
        int pos = ArrayUtils.indexOf(strings,"this");
        Object o = (pos>-1 && objects.length>pos) ? objects[pos] : objects[0];

        if (o instanceof CategoryTree) {
            CategoryTree tree = (CategoryTree) o;
            if (tree.getChild()!=null) {
                CategoryDTO dto = new CategoryDTO(tree.getChild(),language);
                dto.setPosition(tree.getPosition());
                return dto;
            }
        } else if (o instanceof Category) {
            return new CategoryDTO((Category) o, language);
        } else if (o instanceof Long) {
            Category cat = dao.getCategory((Long) o);
            return new CategoryDTO(cat, language);
        }
        return null;
    }

    public List transformList(List list) {
        if (list!=null) {
            List res = new ArrayList();
            for(Object o : list) if (o!=null) res.add(o);
            return res;
        }
        return null;
    }
}