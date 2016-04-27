
package test.celesa;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ImprintType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ImprintType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="NameCodeType" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="NameCodeTypeName" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="NameCodeValue" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="ImprintName" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ImprintType", propOrder = {
    "nameCodeType",
    "nameCodeTypeName",
    "nameCodeValue",
    "imprintName"
})
public class ImprintType {

    @XmlElement(name = "NameCodeType", required = true)
    protected String nameCodeType;
    @XmlElement(name = "NameCodeTypeName", required = true)
    protected String nameCodeTypeName;
    @XmlElement(name = "NameCodeValue", required = true)
    protected String nameCodeValue;
    @XmlElement(name = "ImprintName", required = true)
    protected String imprintName;

    /**
     * Gets the value of the nameCodeType property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getNameCodeType() {
        return nameCodeType;
    }

    /**
     * Sets the value of the nameCodeType property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNameCodeType(String value) {
        this.nameCodeType = value;
    }

    /**
     * Gets the value of the nameCodeTypeName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getNameCodeTypeName() {
        return nameCodeTypeName;
    }

    /**
     * Sets the value of the nameCodeTypeName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNameCodeTypeName(String value) {
        this.nameCodeTypeName = value;
    }

    /**
     * Gets the value of the nameCodeValue property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getNameCodeValue() {
        return nameCodeValue;
    }

    /**
     * Sets the value of the nameCodeValue property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNameCodeValue(String value) {
        this.nameCodeValue = value;
    }

    /**
     * Gets the value of the imprintName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getImprintName() {
        return imprintName;
    }

    /**
     * Sets the value of the imprintName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setImprintName(String value) {
        this.imprintName = value;
    }

}
