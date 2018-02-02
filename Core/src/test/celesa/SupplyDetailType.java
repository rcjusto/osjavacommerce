
package test.celesa;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for SupplyDetailType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="SupplyDetailType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="SupplierIdentifier" type="{}SupplierIdentifierType"/>
 *         &lt;element name="SupplierName" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="TelephoneNumber" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="FaxNumber" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="EmailAddress" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="Website" type="{}WebsiteType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="SupplierRole" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="AvailabilityCode" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="ProductAvailability" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="Price" type="{}PriceType"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SupplyDetailType", propOrder = {
    "supplierIdentifier",
    "supplierName",
    "telephoneNumber",
    "faxNumber",
    "emailAddress",
    "website",
    "supplierRole",
    "availabilityCode",
    "productAvailability",
    "price"
})
public class SupplyDetailType {

    @XmlElement(name = "SupplierIdentifier", required = true)
    protected SupplierIdentifierType supplierIdentifier;
    @XmlElement(name = "SupplierName", required = true)
    protected String supplierName;
    @XmlElement(name = "TelephoneNumber", required = true)
    protected String telephoneNumber;
    @XmlElement(name = "FaxNumber", required = true)
    protected String faxNumber;
    @XmlElement(name = "EmailAddress", required = true)
    protected String emailAddress;
    @XmlElement(name = "Website")
    protected List<WebsiteType> website;
    @XmlElement(name = "SupplierRole", required = true)
    protected String supplierRole;
    @XmlElement(name = "AvailabilityCode", required = true)
    protected String availabilityCode;
    @XmlElement(name = "ProductAvailability", required = true)
    protected String productAvailability;
    @XmlElement(name = "Price", required = true)
    protected PriceType price;

    /**
     * Gets the value of the supplierIdentifier property.
     * 
     * @return
     *     possible object is
     *     {@link SupplierIdentifierType }
     *     
     */
    public SupplierIdentifierType getSupplierIdentifier() {
        return supplierIdentifier;
    }

    /**
     * Sets the value of the supplierIdentifier property.
     * 
     * @param value
     *     allowed object is
     *     {@link SupplierIdentifierType }
     *     
     */
    public void setSupplierIdentifier(SupplierIdentifierType value) {
        this.supplierIdentifier = value;
    }

    /**
     * Gets the value of the supplierName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSupplierName() {
        return supplierName;
    }

    /**
     * Sets the value of the supplierName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSupplierName(String value) {
        this.supplierName = value;
    }

    /**
     * Gets the value of the telephoneNumber property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTelephoneNumber() {
        return telephoneNumber;
    }

    /**
     * Sets the value of the telephoneNumber property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTelephoneNumber(String value) {
        this.telephoneNumber = value;
    }

    /**
     * Gets the value of the faxNumber property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFaxNumber() {
        return faxNumber;
    }

    /**
     * Sets the value of the faxNumber property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFaxNumber(String value) {
        this.faxNumber = value;
    }

    /**
     * Gets the value of the emailAddress property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getEmailAddress() {
        return emailAddress;
    }

    /**
     * Sets the value of the emailAddress property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEmailAddress(String value) {
        this.emailAddress = value;
    }

    /**
     * Gets the value of the website property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the website property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getWebsite().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link WebsiteType }
     * 
     * 
     */
    public List<WebsiteType> getWebsite() {
        if (website == null) {
            website = new ArrayList<WebsiteType>();
        }
        return this.website;
    }

    /**
     * Gets the value of the supplierRole property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSupplierRole() {
        return supplierRole;
    }

    /**
     * Sets the value of the supplierRole property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSupplierRole(String value) {
        this.supplierRole = value;
    }

    /**
     * Gets the value of the availabilityCode property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAvailabilityCode() {
        return availabilityCode;
    }

    /**
     * Sets the value of the availabilityCode property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAvailabilityCode(String value) {
        this.availabilityCode = value;
    }

    /**
     * Gets the value of the productAvailability property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getProductAvailability() {
        return productAvailability;
    }

    /**
     * Sets the value of the productAvailability property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setProductAvailability(String value) {
        this.productAvailability = value;
    }

    /**
     * Gets the value of the price property.
     * 
     * @return
     *     possible object is
     *     {@link PriceType }
     *     
     */
    public PriceType getPrice() {
        return price;
    }

    /**
     * Sets the value of the price property.
     * 
     * @param value
     *     allowed object is
     *     {@link PriceType }
     *     
     */
    public void setPrice(PriceType value) {
        this.price = value;
    }

}
