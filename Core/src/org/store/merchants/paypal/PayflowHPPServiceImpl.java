package org.store.merchants.paypal;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.store.core.beans.Currency;
import org.store.core.beans.Order;
import org.store.core.beans.utils.CreditCard;
import org.store.core.front.FrontModuleAction;
import org.store.core.globals.BaseAction;
import org.store.core.globals.StoreHtmlField;
import org.store.core.utils.merchants.MerchantService;
import org.store.core.utils.merchants.PaymentResult;

public class PayflowHPPServiceImpl
  extends MerchantService
{
  private static final String PAYFLOWPRO_HOST_LIVE = "https://payflowpro.paypal.com";
  private static final String PAYFLOWPRO_HOST_TEST = "https://pilot-payflowpro.paypal.com";
  private static final String PAYFLOWLINK_HOST_LIVE = "https://payflowlink.paypal.com";
  private static final String PAYFLOWLINK_HOST_TEST = "https://pilot-payflowlink.paypal.com";
  private static final String FIELD_PARTNER = "PARTNER";
  private static final String FIELD_VENDOR = "VENDOR";
  private static final String FIELD_USER = "USER";
  private static final String FIELD_PASS = "PASS";
  private static final String FIELD_ENVIRONMENT = "environment";
  private static final String FIELD_IFRAME = "iframe";
  private static final String PROP_ENVIRONMENT = "payflow.status";
  private static final String PROP_IFRAME = "payflow.iframe";
  private static final String PROP_PARTNER = "payflow.partner";
  private static final String PROP_VENDOR = "payflow.vendor";
  private static final String PROP_USER = "payflow.user";
  private static final String PROP_PASS = "payflow.pass";
  
  public boolean validatePayment(Order order, BaseAction action)
  {
    return true;
  }
  
  public PaymentResult doPayment(Order order, BaseAction action)
  {
    return null;
  }
  
  public PaymentResult doRequestStatus(Order order, BaseAction action)
  {
    return null;
  }
  
  public Double getInterestPercent(BaseAction action)
  {
    return null;
  }
  
  public Map preparePaymentRedirection(Order order, BaseAction action)
  {
    Map<String, String> map = new HashMap();
    
    String urlPro = "live".equalsIgnoreCase(this.properties.getProperty("payflow.status")) ? "https://payflowpro.paypal.com" : "https://pilot-payflowpro.paypal.com";
    String urlLink = "live".equalsIgnoreCase(this.properties.getProperty("payflow.status")) ? "https://payflowlink.paypal.com" : "https://pilot-payflowlink.paypal.com";
    
    String SECURETOKENID = getSecureTokenID(order.getIdOrder());
    String SECURETOKEN = null;
    
    DecimalFormatSymbols dfs = new DecimalFormatSymbols(new Locale("en"));
    dfs.setDecimalSeparator('.');
    DecimalFormat df = new DecimalFormat("0.00", dfs);
    StringBuilder postBody = new StringBuilder();
    postBody.append("PARTNER=").append(this.properties.getProperty("payflow.partner"));
    postBody.append("&VENDOR=").append(this.properties.getProperty("payflow.vendor"));
    postBody.append("&USER=").append(this.properties.getProperty("payflow.user"));
    postBody.append("&PWD=").append(this.properties.getProperty("payflow.pass"));
    postBody.append("&TRXTYPE=S");
    postBody.append("&CREATESECURETOKEN=Y");
    postBody.append("&CURRENCY=").append(order.getCurrency().getCode());
    postBody.append("&AMT=").append(df.format(order.getTotal()));
    postBody.append("&SECURETOKENID=").append(SECURETOKENID);
    
    PostMethod post = new PostMethod(urlPro);
    RequestEntity entity = new StringRequestEntity(postBody.toString());
    post.setRequestEntity(entity);
    HttpClient httpclient = new HttpClient();
    try
    {
      httpclient.executeMethod(post);
      Map<String, String> getResponse = parseReponse(post.getResponseBodyAsString());
      if ((getResponse.containsKey("RESULT")) && ("0".equalsIgnoreCase((String)getResponse.get("RESULT"))))
      {
        SECURETOKEN = (String)getResponse.get("SECURETOKEN");
        if (StringUtils.isNotEmpty(SECURETOKEN))
        {
          if ("Y".equalsIgnoreCase(this.properties.getProperty("payflow.iframe", "N"))) {
            map.put("__iframe", urlLink + "?SECURETOKEN=" + SECURETOKEN + "&SECURETOKENID=" + SECURETOKENID);
          }
          map.put("__url", urlLink);
          map.put("__method", "post");
          map.put("MODE", "live".equalsIgnoreCase(this.properties.getProperty("payflow.status")) ? "LIVE" : "TEST");
          map.put("SECURETOKEN", SECURETOKEN);
          map.put("SECURETOKENID", SECURETOKENID);
        }
        else
        {
          map.put("__error", "The secure token is empty");
        }
      }
      else
      {
        map.put("__error", getResponse.get("RESPMSG"));
      }
    }
    catch (Exception e)
    {
      map.put("__error", e.getMessage());
      log.error(e.getMessage());
    }
    finally
    {
      post.releaseConnection();
    }
    return map;
  }
  
  public String doPaymentRedirection(FrontModuleAction action)
  {
    return null;
  }
  
  public boolean useAVS(CreditCard card)
  {
    return false;
  }
  
  public boolean useCVD(CreditCard card)
  {
    return false;
  }
  
  public String[] getCardTypes()
  {
    return new String[0];
  }
  
  public String getCode()
  {
    return "Payflow Hosted Page";
  }
  
  public String getLabel()
  {
    return "payflow.hpp";
  }
  
  public String getType()
  {
    return "hosted.page";
  }
  
  public String getError()
  {
    return null;
  }
  
  public String getForm(BaseAction action)
  {
    return null;
  }
  
  public String getPropertiesForm(BaseAction action)
  {
    StringBuffer form = new StringBuffer();
    
    form.append(new StoreHtmlField("select", "environment").addClasses("field string-100").addOption("test", action.getText("test", "Test")).addOption("live", action.getText("live", "Live")).setLabel(action.getText("status", "Status")).setValue(getProperty("payflow.status", "test")).getTableRow());
    
    form.append(new StoreHtmlField("select", "iframe").addClasses("field string-100").addOption("N", action.getText("N", "No")).addOption("Y", action.getText("Y", "Yes")).setLabel(action.getText("use.iframe", "Use IFrame")).setValue(getProperty("payflow.iframe", "N")).getTableRow());
    
    form.append(new StoreHtmlField("input", "PARTNER").addClasses("field string-100").setLabel(action.getText("payflow.partner", "Partner")).setValue(getProperty("payflow.partner", "")).getTableRow());
    
    form.append(new StoreHtmlField("input", "VENDOR").addClasses("field string-100").setLabel(action.getText("payflow.vendor", "Vendor")).setValue(getProperty("payflow.vendor", "")).getTableRow());
    
    form.append(new StoreHtmlField("input", "USER").addClasses("field string-100").setLabel(action.getText("payflow.user", "User")).setValue(getProperty("payflow.user", "")).getTableRow());
    
    form.append(new StoreHtmlField("input", "PASS").addClasses("field string-100").setLabel(action.getText("payflow.pass", "Password")).setValue(getProperty("payflow.pass", "")).getTableRow());
    
    return form.toString();
  }
  
  public void savePropertiesForm(BaseAction action)
  {
    this.properties.setProperty("payflow.status", getRequestParam(action, "environment", "test"));
    this.properties.setProperty("payflow.iframe", getRequestParam(action, "iframe", "N"));
    this.properties.setProperty("payflow.partner", getRequestParam(action, "PARTNER", ""));
    this.properties.setProperty("payflow.vendor", getRequestParam(action, "VENDOR", ""));
    this.properties.setProperty("payflow.user", getRequestParam(action, "USER", ""));
    this.properties.setProperty("payflow.pass", getRequestParam(action, "PASS", ""));
    super.savePropertiesForm(action);
  }
  
  public static String getSecureTokenID(Long orderId)
  {
    return RandomStringUtils.randomAlphanumeric(26) + StringUtils.leftPad(orderId.toString(), 10, "0");
  }
  
  public static Map<String, String> parseReponse(String cad)
  {
    Map<String, String> result = new HashMap();
    String[] arr = cad.split("&");
    for (String e : arr)
    {
      String[] arr1 = e.split("=");
      if ((arr1.length > 0) && (StringUtils.isNotEmpty(arr1[0]))) {
        result.put(arr1[0], (arr1.length > 1) && (StringUtils.isNotEmpty(arr1[1])) ? arr1[1] : "");
      }
    }
    return result;
  }
}
