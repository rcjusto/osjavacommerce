
package test.celesa;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for HeaderType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="HeaderType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="FromCompany" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="SentDate" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "HeaderType", propOrder = {
    "fromCompany",
    "sentDate"
})
public class HeaderType {

    @XmlElement(name = "FromCompany", required = true)
    protected String fromCompany;
    @XmlElement(name = "SentDate", required = true)
    protected String sentDate;

    /**
     * Gets the value of the fromCompany property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFromCompany() {
        return fromCompany;
    }

    /**
     * Sets the value of the fromCompany property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFromCompany(String value) {
        this.fromCompany = value;
    }

    /**
     * Gets the value of the sentDate property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSentDate() {
        return sentDate;
    }

    /**
     * Sets the value of the sentDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSentDate(String value) {
        this.sentDate = value;
    }

}
