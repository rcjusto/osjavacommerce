//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.10 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2011.10.22 at 02:13:30 AM CDT 
//


package org.store.publications.onix;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.editeur.org/onix/3.0/reference}WebsiteRole" minOccurs="0"/>
 *         &lt;element ref="{http://www.editeur.org/onix/3.0/reference}WebsiteDescription" minOccurs="0"/>
 *         &lt;element ref="{http://www.editeur.org/onix/3.0/reference}WebsiteLink"/>
 *       &lt;/sequence>
 *       &lt;attGroup ref="{http://www.editeur.org/onix/3.0/reference}generalAttributes"/>
 *       &lt;attribute name="refname">
 *         &lt;simpleType>
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token">
 *             &lt;enumeration value="Website"/>
 *           &lt;/restriction>
 *         &lt;/simpleType>
 *       &lt;/attribute>
 *       &lt;attribute name="shortname">
 *         &lt;simpleType>
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token">
 *             &lt;enumeration value="website"/>
 *           &lt;/restriction>
 *         &lt;/simpleType>
 *       &lt;/attribute>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "websiteRole",
    "websiteDescription",
    "websiteLink"
})
@XmlRootElement(name = "Website")
public class Website {

    @XmlElement(name = "WebsiteRole")
    protected WebsiteRole websiteRole;
    @XmlElement(name = "WebsiteDescription")
    protected WebsiteDescription websiteDescription;
    @XmlElement(name = "WebsiteLink", required = true)
    protected WebsiteLink websiteLink;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String refname;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String shortname;
    @XmlAttribute
    protected String datestamp;
    @XmlAttribute
    protected String sourcetype;
    @XmlAttribute
    @XmlSchemaType(name = "anySimpleType")
    protected String sourcename;

    /**
     * Gets the value of the websiteRole property.
     * 
     * @return
     *     possible object is
     *     {@link WebsiteRole }
     *     
     */
    public WebsiteRole getWebsiteRole() {
        return websiteRole;
    }

    /**
     * Sets the value of the websiteRole property.
     * 
     * @param value
     *     allowed object is
     *     {@link WebsiteRole }
     *     
     */
    public void setWebsiteRole(WebsiteRole value) {
        this.websiteRole = value;
    }

    /**
     * Gets the value of the websiteDescription property.
     * 
     * @return
     *     possible object is
     *     {@link WebsiteDescription }
     *     
     */
    public WebsiteDescription getWebsiteDescription() {
        return websiteDescription;
    }

    /**
     * Sets the value of the websiteDescription property.
     * 
     * @param value
     *     allowed object is
     *     {@link WebsiteDescription }
     *     
     */
    public void setWebsiteDescription(WebsiteDescription value) {
        this.websiteDescription = value;
    }

    /**
     * Gets the value of the websiteLink property.
     * 
     * @return
     *     possible object is
     *     {@link WebsiteLink }
     *     
     */
    public WebsiteLink getWebsiteLink() {
        return websiteLink;
    }

    /**
     * Sets the value of the websiteLink property.
     * 
     * @param value
     *     allowed object is
     *     {@link WebsiteLink }
     *     
     */
    public void setWebsiteLink(WebsiteLink value) {
        this.websiteLink = value;
    }

    /**
     * Gets the value of the refname property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRefname() {
        return refname;
    }

    /**
     * Sets the value of the refname property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRefname(String value) {
        this.refname = value;
    }

    /**
     * Gets the value of the shortname property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getShortname() {
        return shortname;
    }

    /**
     * Sets the value of the shortname property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setShortname(String value) {
        this.shortname = value;
    }

    /**
     * Gets the value of the datestamp property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDatestamp() {
        return datestamp;
    }

    /**
     * Sets the value of the datestamp property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDatestamp(String value) {
        this.datestamp = value;
    }

    /**
     * Gets the value of the sourcetype property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSourcetype() {
        return sourcetype;
    }

    /**
     * Sets the value of the sourcetype property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSourcetype(String value) {
        this.sourcetype = value;
    }

    /**
     * Gets the value of the sourcename property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSourcename() {
        return sourcename;
    }

    /**
     * Sets the value of the sourcename property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSourcename(String value) {
        this.sourcename = value;
    }

}
