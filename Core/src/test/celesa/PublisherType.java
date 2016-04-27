
package test.celesa;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for PublisherType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="PublisherType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="PublishingRole" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="NameCodeType" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="NameCodeTypeName" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="NameCodeValue" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="PublisherName" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="Website" type="{}WebsiteType" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PublisherType", propOrder = {
    "publishingRole",
    "nameCodeType",
    "nameCodeTypeName",
    "nameCodeValue",
    "publisherName",
    "website"
})
public class PublisherType {

    @XmlElement(name = "PublishingRole", required = true)
    protected String publishingRole;
    @XmlElement(name = "NameCodeType", required = true)
    protected String nameCodeType;
    @XmlElement(name = "NameCodeTypeName", required = true)
    protected String nameCodeTypeName;
    @XmlElement(name = "NameCodeValue", required = true)
    protected String nameCodeValue;
    @XmlElement(name = "PublisherName", required = true)
    protected String publisherName;
    @XmlElement(name = "Website")
    protected WebsiteType website;

    /**
     * Gets the value of the publishingRole property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPublishingRole() {
        return publishingRole;
    }

    /**
     * Sets the value of the publishingRole property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPublishingRole(String value) {
        this.publishingRole = value;
    }

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
     * Gets the value of the publisherName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPublisherName() {
        return publisherName;
    }

    /**
     * Sets the value of the publisherName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPublisherName(String value) {
        this.publisherName = value;
    }

    /**
     * Gets the value of the website property.
     * 
     * @return
     *     possible object is
     *     {@link WebsiteType }
     *     
     */
    public WebsiteType getWebsite() {
        return website;
    }

    /**
     * Sets the value of the website property.
     * 
     * @param value
     *     allowed object is
     *     {@link WebsiteType }
     *     
     */
    public void setWebsite(WebsiteType value) {
        this.website = value;
    }

}
