package org.store.core.utils.currencies;

import org.store.core.beans.Currency;
import org.store.core.dao.HibernateDAO;
import org.store.core.globals.CurrencyRatesUpdater;
import org.store.core.globals.YahooCurrencyRatesUpdater;
import org.store.core.globals.config.Store20Database;
import org.store.core.hibernate.HibernateSessionFactory;
import org.store.core.utils.quartz.JobStoreThread;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.Calendar;
import java.util.List;

public class CurrencyRatesUpdaterThreat extends JobStoreThread {

    public CurrencyRatesUpdaterThreat(String storeCode, Store20Database databaseConfig) {
        this.storeCode = storeCode;
        this.databaseConfig = databaseConfig;
        setName("currency_rates_task_" + String.valueOf(Calendar.getInstance().getTimeInMillis()));
    }


    public void setStoreCode(String storeCode) {
        this.storeCode = storeCode;
    }

    @Override
    public void run() {
        int res = processCurrencies();
        addOutputMessage(String.valueOf(res) + " currency rates successfully updated.");
        setJobExecutionResult("COMPLETE");
    }

    public int processCurrencies() {
        int res = 0;
        try {
            Session session = HibernateSessionFactory.getNewSession(databaseConfig);
            Transaction tx = session.beginTransaction();
            try {

                HibernateDAO dao = new HibernateDAO(session, storeCode);
                Currency defaultCurrency = dao.getDefaultCurrency();
                List<Currency> list = dao.getCurrencies();
                setExecutionPercent(0d);
                if (list != null && !list.isEmpty()) {
                    int num = 0, total = list.size() - 1;
                    for (Currency c : list) {
                        if (!c.equals(defaultCurrency)) {
                            setExecutionMessage("Updating rates of " + c.getCode());
                            setExecutionPercent(100d * num++ / total);

                            YahooCurrencyRatesUpdater updater = new YahooCurrencyRatesUpdater();
                            CurrencyRatesUpdater.ResponseRateUpdate response = updater.getRate(defaultCurrency.getCode(), c.getCode());
                            if (response != null) {
                                if (response.getRateValue() != null && response.getRateValue() > 0) c.setRatio(response.getRateValue());
                                if (response.getReverseRateValue() != null && response.getReverseRateValue() > 0) c.setReverseRatio(response.getReverseRateValue());
                                if (response.getRateDate() != null) c.setLastUpdate(response.getRateDate());
                                dao.save(c);
                                res++;
                            } else {
                                c.setRatio(1d);
                                c.setReverseRatio(1d);
                                dao.save(c);
                            }
                        }
                    }
                }
                tx.commit();
            } catch (Exception e) {
                tx.rollback();
                log.error(e.getMessage(), e);
            } finally {
                if (session.isOpen()) session.close();
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return res;
    }

}
