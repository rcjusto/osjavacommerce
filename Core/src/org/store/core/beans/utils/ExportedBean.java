package org.store.core.beans.utils;

import java.util.Map;

/**
 * User: Rogelio Caballero Justo
 * Date: 27/08/2006
 * Time: 05:22:28 PM
 */
public interface ExportedBean {

   public Map<String, Object> toMap(String lang);
   public void fromMap(Map<String, String> m);

}
