package org.store.core.beans.utils;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.criterion.CriteriaQuery;
import org.hibernate.criterion.Order;
import org.hibernate.dialect.MySQLDialect;
import org.hibernate.dialect.SQLServerDialect;
import org.hibernate.engine.SessionFactoryImplementor;



public class RandomOrder extends Order {


    protected RandomOrder(String propertyName, boolean ascending) {
        super(propertyName, ascending);
    }

    public static Order random() {
		return new RandomOrder("", true);
	}

    @Override
    public String toSqlString(Criteria criteria, CriteriaQuery criteriaQuery) throws HibernateException {
        SessionFactoryImplementor factory = criteriaQuery.getFactory();
        if (factory.getDialect() instanceof SQLServerDialect) {
            return "newid()";
        } else if (factory.getDialect() instanceof MySQLDialect) {
            return "rand()";
        }
        return "";
    }

}
