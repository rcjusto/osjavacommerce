package org.store.merchants.paypal;

import javax.servlet.ServletContext;
import org.apache.commons.lang.StringUtils;
import org.hibernate.Session;
import org.store.core.beans.StaticText;
import org.store.core.beans.StaticTextLang;
import org.store.core.beans.StoreProperty;
import org.store.core.dao.HibernateDAO;
import org.store.core.front.FrontModuleAction;
import org.store.core.globals.config.Store20Config;
import org.store.core.utils.events.DefaultEventServiceImpl;
import org.store.core.utils.templates.TemplateBlock;

public class PayflowHPPEventImpl
  extends DefaultEventServiceImpl
{
  public static final String PAYMENT_SERVICE_NAME = "Payflow Hosted Page";
  private static final String BLOCK_APPROVED_TEXT = "payflow-hpp.pay.result.approved";
  private static final String BLOCK_REJECTED_TEXT = "payflow-hpp.pay.result.rejected";
  
  public String getName()
  {
    return "Payflow Hosted Page";
  }
  
  public String getDescription(FrontModuleAction action)
  {
    return "Process Payflow Hosted Page Response";
  }
  
  public void initialize(ServletContext ctx, Session databaseSession, String store)
  {
    HibernateDAO dao = new HibernateDAO(databaseSession, store);
    StoreProperty bean = dao.getStoreProperty("languages", "GENERAL");
    String[] languages = (bean != null) && (!StringUtils.isEmpty(bean.getValue())) ? bean.getValue().split(",") : null;
    if (languages != null)
    {
      createBlock(dao, "payflow-hpp.pay.result.approved", "<h1>Your order was approved</h1>", store, languages);
      createBlock(dao, "payflow-hpp.pay.result.rejected", "<h1>Your order was rejected</h1>", store, languages);
    }
    Store20Config storeConfig = Store20Config.getInstance(ctx);
    TemplateBlock block1 = new TemplateBlock("payflow-hpp.pay.result.approved");
    if (languages != null) {
      for (String lang : languages) {
        block1.setName(lang, "Payflow HPP<br/>Approved Message");
      }
    }
    storeConfig.addBlock(block1);
    TemplateBlock block2 = new TemplateBlock("payflow-hpp.pay.result.rejected");
    if (languages != null) {
      for (String lang : languages) {
        block2.setName(lang, "Payflow HPP<br/>Rejected Message");
      }
    }
    storeConfig.addBlock(block2);
  }
  
  private void createBlock(HibernateDAO dao, String code, String text, String store, String[] languages)
  {
    StaticText st = dao.getStaticText(code, "block");
    if (st == null)
    {
      st = new StaticText();
      st.setCode(code);
      st.setInventaryCode(store);
      st.setTextType("block");
      dao.save(st);
      for (String lang : languages)
      {
        StaticTextLang stl = new StaticTextLang();
        stl.setStaticLang(lang);
        stl.setStaticText(st);
        stl.setValue(text);
        dao.save(stl);
      }
    }
  }
}
