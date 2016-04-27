package org.store.core.utils.suppliers;

import java.util.List;
import java.util.Properties;


public interface SupplierService {                                           

    public AvailabilityResponse requestAvailability(String mfgPartNumber, String sku);
    public List<SupplierProperty> getConfigurationParameters();
    public String getName();
    public void setProperties(Properties p);

}
