
package test.celesa;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ProductIdentifierType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ProductIdentifierType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="ProductIDType" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="IDTypeName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
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
@XmlType(name = "ProductIdentifierType", propOrder = {
    "productIDType",
    "idTypeName",
    "idValue"
})
public class ProductIdentifierType {

    @XmlElement(name = "ProductIDType", required = true)
    protected String productIDType;
    @XmlElement(name = "IDTypeName")
    protected String idTypeName;
    @XmlElement(name = "IDValue", required = true)
    protected String idValue;

    /**
     * Gets the value of the productIDType property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getProductIDType() {
        return productIDType;
    }

    /**
     * Sets the value of the productIDType property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setProductIDType(String value) {
        this.productIDType = value;
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
