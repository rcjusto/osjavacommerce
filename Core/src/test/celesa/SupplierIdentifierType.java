
package test.celesa;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for SupplierIdentifierType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="SupplierIdentifierType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="SupplierIDType" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="IDTypeName" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="IDValue" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SupplierIdentifierType", propOrder = {
    "supplierIDType",
    "idTypeName",
    "idValue"
})
public class SupplierIdentifierType {

    @XmlElement(name = "SupplierIDType", required = true)
    protected String supplierIDType;
    @XmlElement(name = "IDTypeName", required = true)
    protected String idTypeName;
    @XmlElement(name = "IDValue", required = true)
    protected String idValue;

    /**
     * Gets the value of the supplierIDType property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSupplierIDType() {
        return supplierIDType;
    }

    /**
     * Sets the value of the supplierIDType property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSupplierIDType(String value) {
        this.supplierIDType = value;
    }

    /**
     * Gets the value of the idTypeName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getIDTypeName() {
        return idTypeName;
    }

    /**
     * Sets the value of the idTypeName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setIDTypeName(String value) {
        this.idTypeName = value;
    }

    /**
     * Gets the value of the idValue property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getIDValue() {
        return idValue;
    }

    /**
     * Sets the value of the idValue property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setIDValue(String value) {
        this.idValue = value;
    }

}
