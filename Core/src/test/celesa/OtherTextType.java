
package test.celesa;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for OtherTextType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="OtherTextType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="TextTypeCode" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="TextFormat" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="Text" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "OtherTextType", propOrder = {
    "textTypeCode",
    "textFormat",
    "text"
})
public class OtherTextType {

    @XmlElement(name = "TextTypeCode", required = true)
    protected String textTypeCode;
    @XmlElement(name = "TextFormat", required = true)
    protected String textFormat;
    @XmlElement(name = "Text", required = true)
    protected String text;

    /**
     * Gets the value of the textTypeCode property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTextTypeCode() {
        return textTypeCode;
    }

    /**
     * Sets the value of the textTypeCode property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTextTypeCode(String value) {
        this.textTypeCode = value;
    }

    /**
     * Gets the value of the textFormat property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTextFormat() {
        return textFormat;
    }

    /**
     * Sets the value of the textFormat property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTextFormat(String value) {
        this.textFormat = value;
    }

    /**
     * Gets the value of the text property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getText() {
        return text;
    }

    /**
     * Sets the value of the text property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setText(String value) {
        this.text = value;
    }

}
