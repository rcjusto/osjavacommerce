package org.store.core.beans.utils;

/**
 * User: Rogelio Caballero Justo
 * Date: 27/08/2006
 * Time: 05:22:28 PM
 */
public interface MultiLangBean {

   public Object getLanguage(String lang);
   public Object getLanguage(String lang, String defLang);

}
