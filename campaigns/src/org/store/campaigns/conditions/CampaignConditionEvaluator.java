package org.store.campaigns.conditions;

import org.store.core.beans.User;
import org.store.core.dao.HibernateDAO;

import java.util.Map;

/**
 * Rogelio Caballero
 * 17/12/11 18:31
 */
public abstract class CampaignConditionEvaluator {

    protected HibernateDAO dao;

    public void setDao(HibernateDAO dao) {
        this.dao = dao;
    }

    public abstract String getId();

    public abstract boolean evaluateCondition(User user, Map<String, String> params);

}
