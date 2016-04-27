package org.store.core.beans.utils;

import org.store.core.dao.HibernateDAO;
import org.apache.commons.lang.ArrayUtils;
import org.hibernate.transform.ResultTransformer;

import java.io.Serializable;
import java.util.List;

public class IdToBeanTransformer implements ResultTransformer {

    private HibernateDAO dao;
    private Class clazz;

    public IdToBeanTransformer(HibernateDAO dao, Class clazz) {
        this.dao = dao;
        this.clazz = clazz;
    }

    public Object transformTuple(Object[] objects, String[] strings) {
        int pos =  ArrayUtils.indexOf(strings,"idProduct");
        if (pos>-1 && pos<objects.length) {
            Object o = objects[pos];
            return dao.get(clazz, (o instanceof Number) ? ((Number)o).longValue() : (Serializable) o);
        } else if (objects != null && objects.length > 0) {
            for(Object o : objects) {
                if (o instanceof Number) {
                    return dao.get(clazz, ((Number) o).longValue());
                }
            }
        }
        return null;
    }

    public List transformList(List list) {
        return list;
    }
}
