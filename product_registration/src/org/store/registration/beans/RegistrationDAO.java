package org.store.registration.beans;

import org.hibernate.Criteria;
import org.hibernate.criterion.*;
import org.store.core.beans.User;
import org.store.core.beans.utils.DataNavigator;
import org.store.core.dao.HibernateDAO;
import org.store.core.globals.SomeUtils;

import java.util.Date;
import java.util.List;

/**
 * Created by rogelio on 1/2/14.
 */
public class RegistrationDAO extends HibernateDAO {

    public RegistrationDAO(HibernateDAO dao) {
        super(dao.gethSession(), dao.getStoreCode());
    }

    public List<ProductRegistration> getRegistrations(DataNavigator nav, User user) {
        Criteria cri = createCriteriaForStore(ProductRegistration.class);
        if (user!=null) cri.add(Restrictions.eq("user", user));
        if (nav != null) {
            cri.setProjection(Projections.countDistinct("id"));
            Number total = (Number) cri.uniqueResult();
            nav.setTotalRows(total != null ? total.intValue() : 0);
            cri.setProjection(null);
            cri.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
            if (nav.getTotalRows() > 0) {
                cri.setMaxResults(nav.getPageRows());
                cri.setFirstResult(nav.getFirstRow() - 1);
            }
        }
        cri.addOrder(Order.desc("purchaseDate"));
        cri.addOrder(Order.asc("id"));
        return cri.list();
    }

    public List<ProductRegistration> searchRegistrations(DataNavigator nav, String filterUser, String filterPlace, String filterModel, String filterDateFrom, String filterDateTo) {
        Criteria cri = createCriteriaForStore(ProductRegistration.class);
        if (!isEmpty(filterUser)) {
            cri.createCriteria("user").add(
                    Restrictions.or(
                            Restrictions.like("firstname", filterUser, MatchMode.ANYWHERE),
                            Restrictions.like("lastname", filterUser, MatchMode.ANYWHERE)
                    )
            );
        }

        if (!isEmpty(filterPlace)) cri.add(Restrictions.like("purchasePlace", filterPlace, MatchMode.ANYWHERE));
        if (!isEmpty(filterModel)) cri.add(Restrictions.like("modelNumber", filterModel, MatchMode.ANYWHERE));

        Date dateFrom = SomeUtils.strToDate(filterDateFrom, getDefaultLanguage());
        if (dateFrom!=null) cri.add(Restrictions.ge("purchaseDate", SomeUtils.dateIni(dateFrom)));

        Date dateTo = SomeUtils.strToDate(filterDateTo, getDefaultLanguage());
        if (dateTo!=null) cri.add(Restrictions.le("purchaseDate", SomeUtils.dateEnd(dateTo)));

        if (nav != null) {
            cri.setProjection(Projections.countDistinct("id"));
            Number total = (Number) cri.uniqueResult();
            nav.setTotalRows(total != null ? total.intValue() : 0);
            cri.setProjection(null);
            cri.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
            if (nav.getTotalRows() > 0) {
                cri.setMaxResults(nav.getPageRows());
                cri.setFirstResult(nav.getFirstRow() - 1);
            }
        }
        cri.addOrder(Order.desc("purchaseDate"));
        cri.addOrder(Order.asc("id"));
        return cri.list();
    }

}
