//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.10 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2011.10.22 at 02:13:30 AM CDT 
//


package org.store.publications.onix;

import java.util.ArrayList;
import java.util.List;
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
 *         &lt;element ref="{http://www.editeur.org/onix/3.0/reference}PublishingRole"/>
 *         &lt;element ref="{http://www.editeur.org/onix/3.0/reference}PublisherIdentifier" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element ref="{http://www.editeur.org/onix/3.0/reference}PublisherName" minOccurs="0"/>
 *         &lt;element ref="{http://www.editeur.org/onix/3.0/reference}Website" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attGroup ref="{http://www.editeur.org/onix/3.0/reference}generalAttributes"/>
 *       &lt;attribute name="refname">
 *         &lt;simpleType>
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token">
 *             &lt;enumeration value="Publisher"/>
 *           &lt;/restriction>
 *         &lt;/simpleType>
 *       &lt;/attribute>
 *       &lt;attribute name="shortname">
 *         &lt;simpleType>
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token">
 *             &lt;enumeration value="publisher"/>
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
    "publishingRole",
    "publisherIdentifier",
    "publisherName",
    "website"
})
@XmlRootElement(name = "Publisher")
public class Publisher {

    @XmlElement(name = "PublishingRole", required = true)
    protected PublishingRole publishingRole;
    @XmlElement(name = "PublisherIdentifier")
    protected List<PublisherIdentifier> publisherIdentifier;
    @XmlElement(name = "PublisherName")
    protected PublisherName publisherName;
    @XmlElement(name = "Website")
    protected List<Website> website;
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
     * Gets the value of the publishingRole property.
     * 
     * @return
     *     possible object is
     *     {@link PublishingRole }
     *     
     */
    public PublishingRole getPublishingRole() {
        return publishingRole;
    }

    /**
     * Sets the value of the publishingRole property.
     * 
     * @param value
     *     allowed object is
     *     {@link PublishingRole }
     *     
     */
    public void setPublishingRole(PublishingRole value) {
        this.publishingRole = value;
    }

    /**
     * Gets the value of the publisherIdentifier property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the publisherIdentifier property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getPublisherIdentifier().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link PublisherIdentifier }
     * 
     * 
     */
    public List<PublisherIdentifier> getPublisherIdentifier() {
        if (publisherIdentifier == null) {
            publisherIdentifier = new ArrayList<PublisherIdentifier>();
        }
        return this.publisherIdentifier;
    }

    /**
     * Gets the value of the publisherName property.
     * 
     * @return
     *     possible object is
     *     {@link PublisherName }
     *     
     */
    public PublisherName getPublisherName() {
        return publisherName;
    }

    /**
     * Sets the value of the publisherName property.
     * 
     * @param value
     *     allowed object is
     *     {@link PublisherName }
     *     
     */
    public void setPublisherName(PublisherName value) {
        this.publisherName = value;
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
     * {@link Website }
     * 
     * 
     */
    public List<Website> getWebsite() {
        if (website == null) {
            website = new ArrayList<Website>();
        }
        return this.website;
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
