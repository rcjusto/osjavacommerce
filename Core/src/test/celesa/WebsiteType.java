
package test.celesa;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for WebsiteType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="WebsiteType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="WebsiteRole" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="WebsiteDescription" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="WebsiteLink" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "WebsiteType", propOrder = {
    "websiteRole",
    "websiteDescription",
    "websiteLink"
})
public class WebsiteType {

    @XmlElement(name = "WebsiteRole", required = true)
    protected String websiteRole;
    @XmlElement(name = "WebsiteDescription", required = true)
    protected String websiteDescription;
    @XmlElement(name = "WebsiteLink", required = true)
    protected String websiteLink;

    /**
     * Gets the value of the websiteRole property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getWebsiteRole() {
        return websiteRole;
    }

    /**
     * Sets the value of the websiteRole property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setWebsiteRole(String value) {
        this.websiteRole = value;
    }

    /**
     * Gets the value of the websiteDescription property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getWebsiteDescription() {
        return websiteDescription;
    }

    /**
     * Sets the value of the websiteDescription property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setWebsiteDescription(String value) {
        this.websiteDescription = value;
    }

    /**
     * Gets the value of the websiteLink property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getWebsiteLink() {
        return websiteLink;
    }

    /**
     * Sets the value of the websiteLink property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setWebsiteLink(String value) {
        this.websiteLink = value;
    }

}
