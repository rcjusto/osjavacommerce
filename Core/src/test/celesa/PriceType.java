
package test.celesa;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for PriceType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="PriceType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="PriceTypeCode" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="PriceQualifier" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="PriceTypeDescription" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="PricePer" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="MinimumOrderQuantity" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="DiscountCoded" type="{}DiscountCodedType"/>
 *         &lt;element name="DiscountPercent" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="PriceStatus" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="PriceAmount" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="CurrencyCode" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="CountryCode" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="PriceEffectiveFrom" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="PriceEffectiveUntil" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PriceType", propOrder = {
    "priceTypeCode",
    "priceQualifier",
    "priceTypeDescription",
    "pricePer",
    "minimumOrderQuantity",
    "discountCoded",
    "discountPercent",
    "priceStatus",
    "priceAmount",
    "currencyCode",
    "countryCode",
    "priceEffectiveFrom",
    "priceEffectiveUntil"
})
public class PriceType {

    @XmlElement(name = "PriceTypeCode", required = true)
    protected String priceTypeCode;
    @XmlElement(name = "PriceQualifier", required = true)
    protected String priceQualifier;
    @XmlElement(name = "PriceTypeDescription", required = true)
    protected String priceTypeDescription;
    @XmlElement(name = "PricePer", required = true)
    protected String pricePer;
    @XmlElement(name = "MinimumOrderQuantity", required = true)
    protected String minimumOrderQuantity;
    @XmlElement(name = "DiscountCoded", required = true)
    protected DiscountCodedType discountCoded;
    @XmlElement(name = "DiscountPercent", required = true)
    protected String discountPercent;
    @XmlElement(name = "PriceStatus", required = true)
    protected String priceStatus;
    @XmlElement(name = "PriceAmount", required = true)
    protected String priceAmount;
    @XmlElement(name = "CurrencyCode", required = true)
    protected String currencyCode;
    @XmlElement(name = "CountryCode", required = true)
    protected String countryCode;
    @XmlElement(name = "PriceEffectiveFrom", required = true)
    protected String priceEffectiveFrom;
    @XmlElement(name = "PriceEffectiveUntil", required = true)
    protected String priceEffectiveUntil;

    /**
     * Gets the value of the priceTypeCode property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPriceTypeCode() {
        return priceTypeCode;
    }

    /**
     * Sets the value of the priceTypeCode property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPriceTypeCode(String value) {
        this.priceTypeCode = value;
    }

    /**
     * Gets the value of the priceQualifier property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPriceQualifier() {
        return priceQualifier;
    }

    /**
     * Sets the value of the priceQualifier property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPriceQualifier(String value) {
        this.priceQualifier = value;
    }

    /**
     * Gets the value of the priceTypeDescription property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPriceTypeDescription() {
        return priceTypeDescription;
    }

    /**
     * Sets the value of the priceTypeDescription property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPriceTypeDescription(String value) {
        this.priceTypeDescription = value;
    }

    /**
     * Gets the value of the pricePer property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPricePer() {
        return pricePer;
    }

    /**
     * Sets the value of the pricePer property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPricePer(String value) {
        this.pricePer = value;
    }

    /**
     * Gets the value of the minimumOrderQuantity property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMinimumOrderQuantity() {
        return minimumOrderQuantity;
    }

    /**
     * Sets the value of the minimumOrderQuantity property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMinimumOrderQuantity(String value) {
        this.minimumOrderQuantity = value;
    }

    /**
     * Gets the value of the discountCoded property.
     * 
     * @return
     *     possible object is
     *     {@link DiscountCodedType }
     *     
     */
    public DiscountCodedType getDiscountCoded() {
        return discountCoded;
    }

    /**
     * Sets the value of the discountCoded property.
     * 
     * @param value
     *     allowed object is
     *     {@link DiscountCodedType }
     *     
     */
    public void setDiscountCoded(DiscountCodedType value) {
        this.discountCoded = value;
    }

    /**
     * Gets the value of the discountPercent property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDiscountPercent() {
        return discountPercent;
    }

    /**
     * Sets the value of the discountPercent property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDiscountPercent(String value) {
        this.discountPercent = value;
    }

    /**
     * Gets the value of the priceStatus property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPriceStatus() {
        return priceStatus;
    }

    /**
     * Sets the value of the priceStatus property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPriceStatus(String value) {
        this.priceStatus = value;
    }

    /**
     * Gets the value of the priceAmount property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPriceAmount() {
        return priceAmount;
    }

    /**
     * Sets the value of the priceAmount property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPriceAmount(String value) {
        this.priceAmount = value;
    }

    /**
     * Gets the value of the currencyCode property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCurrencyCode() {
        return currencyCode;
    }

    /**
     * Sets the value of the currencyCode property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCurrencyCode(String value) {
        this.currencyCode = value;
    }

    /**
     * Gets the value of the countryCode property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCountryCode() {
        return countryCode;
    }

    /**
     * Sets the value of the countryCode property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCountryCode(String value) {
        this.countryCode = value;
    }

    /**
     * Gets the value of the priceEffectiveFrom property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPriceEffectiveFrom() {
        return priceEffectiveFrom;
    }

    /**
     * Sets the value of the priceEffectiveFrom property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPriceEffectiveFrom(String value) {
        this.priceEffectiveFrom = value;
    }

    /**
     * Gets the value of the priceEffectiveUntil property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPriceEffectiveUntil() {
        return priceEffectiveUntil;
    }

    /**
     * Sets the value of the priceEffectiveUntil property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPriceEffectiveUntil(String value) {
        this.priceEffectiveUntil = value;
    }

}
