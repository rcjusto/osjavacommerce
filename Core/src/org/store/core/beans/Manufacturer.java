package org.store.core.beans;

import org.store.core.beans.utils.ExportedBean;
import org.store.core.beans.utils.StoreBean;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.hibernate.search.annotations.Field;

import javax.persistence.*;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

/**
 * Fabricante
 */
@Entity
@Table(name = "t_manufacturer")
public class Manufacturer extends BaseBean implements ExportedBean, StoreBean {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idManufacturer;

    // Tienda en q esta configurado el fabricante
    @Column(length = 10)
    private String inventaryCode;

    // Identificador del fabricante en las urls amigables
    @Column(length = 512)
    private String urlCode;

    // Nombre del fabricante
    @Column(length = 512)
    @Field(name = "name")
    private String manufacturerName;

    @Lob
    private String manufacturerDescription;

    // Url del sitio del fabricante
    @Column(length = 1024)
    private String manufacturerUrl;


    public Manufacturer() {
    }


    public Long getIdManufacturer() {
        return this.idManufacturer;
    }

    public void setIdManufacturer(Long idManufacturer) {
        this.idManufacturer = idManufacturer;
    }

    public String getInventaryCode() {
        return inventaryCode;
    }

    public void setInventaryCode(String inventaryCode) {
        this.inventaryCode = inventaryCode;
    }

    public String getUrlCode() {
        return urlCode;
    }

    public void setUrlCode(String urlCode) {
        this.urlCode = urlCode;
    }

    public String getManufacturerName() {
        return this.manufacturerName;
    }

    public void setManufacturerName(String manufacturerName) {
        this.manufacturerName = manufacturerName;
    }

    public String getManufacturerUrl() {
        return this.manufacturerUrl;
    }

    public void setManufacturerUrl(String manufacturerUrl) {
        this.manufacturerUrl = manufacturerUrl;
    }

    public String getManufacturerDescription() {
        return manufacturerDescription;
    }

    public void setManufacturerDescription(String manufacturerDescription) {
        this.manufacturerDescription = manufacturerDescription;
    }

    @Override
    public boolean equals(Object other) {
        if ((this == other)) return true;
        if (!(other instanceof Manufacturer)) return false;
        Manufacturer castOther = (Manufacturer) other;
        return new EqualsBuilder()
                .append(this.getIdManufacturer(), castOther.getIdManufacturer())
                .isEquals();
    }

    public String toString() {
        return new ToStringBuilder(this)
                .append("idManufacturer", getIdManufacturer())
                .toString();
    }

    public Map<String, Object> toMap(String lang) {
        Map res = new HashMap();
        try {
            Map m = describe();
            res.putAll(m);

        } catch (IllegalAccessException e) {
            log.error(e.getMessage(), e);
        } catch (InvocationTargetException e) {
            log.error(e.getMessage(), e);
        } catch (NoSuchMethodException e) {
            log.error(e.getMessage(), e);
        }

        return res;
    }

    public void fromMap(Map<String, String> m) {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
