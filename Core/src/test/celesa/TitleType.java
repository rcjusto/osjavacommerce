
package test.celesa;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for TitleType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="TitleType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="TextCaseFlag" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="TitleType" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="TitleText" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="TitlePrefix" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="TitleWithoutPrefix" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TitleType", propOrder = {
    "textCaseFlag",
    "titleType",
    "titleText",
    "titlePrefix",
    "titleWithoutPrefix"
})
public class TitleType {

    @XmlElement(name = "TextCaseFlag", required = true)
    protected String textCaseFlag;
    @XmlElement(name = "TitleType", required = true)
    protected String titleType;
    @XmlElement(name = "TitleText", required = true)
    protected String titleText;
    @XmlElement(name = "TitlePrefix", required = true)
    protected String titlePrefix;
    @XmlElement(name = "TitleWithoutPrefix", required = true)
    protected String titleWithoutPrefix;

    /**
     * Gets the value of the textCaseFlag property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTextCaseFlag() {
        return textCaseFlag;
    }

    /**
     * Sets the value of the textCaseFlag property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTextCaseFlag(String value) {
        this.textCaseFlag = value;
    }

    /**
     * Gets the value of the titleType property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTitleType() {
        return titleType;
    }

    /**
     * Sets the value of the titleType property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTitleType(String value) {
        this.titleType = value;
    }

    /**
     * Gets the value of the titleText property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTitleText() {
        return titleText;
    }

    /**
     * Sets the value of the titleText property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTitleText(String value) {
        this.titleText = value;
    }

    /**
     * Gets the value of the titlePrefix property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTitlePrefix() {
        return titlePrefix;
    }

    /**
     * Sets the value of the titlePrefix property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTitlePrefix(String value) {
        this.titlePrefix = value;
    }

    /**
     * Gets the value of the titleWithoutPrefix property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTitleWithoutPrefix() {
        return titleWithoutPrefix;
    }

    /**
     * Sets the value of the titleWithoutPrefix property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTitleWithoutPrefix(String value) {
        this.titleWithoutPrefix = value;
    }

}
