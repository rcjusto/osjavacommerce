package org.store.campaigns.conditions;

import org.store.core.beans.User;
import org.store.core.globals.SomeUtils;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;

/**
 * Rogelio Caballero
 * 17/12/11 18:34
 */
public class RegisterDateEvaluatorImpl extends CampaignConditionEvaluator {

    public String getId() {
        return "days.from.register";
    }

    public boolean evaluateCondition(User user, Map<String, String> params) {
        Date registerDate = user.getRegisterDate();
        long numDays = SomeUtils.dayDiff(registerDate, Calendar.getInstance().getTime());
        Long value = (params.containsKey("value") && params.get("value")!=null) ? SomeUtils.strToLong(params.get("value")) : null;
        if (value!=null) {
            String operator = (params.containsKey("operator") && params.get("operator")!=null) ? params.get("operator") : null;
            if (">".equalsIgnoreCase(operator)) {
                return numDays > value;
            } else if ("<".equalsIgnoreCase(operator)) {
                return numDays < value;
            } else {
                return numDays == value;
            }
        }
        return false;
    }
}
