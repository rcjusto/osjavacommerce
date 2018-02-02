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
 *       &lt;choice>
 *         &lt;element ref="{http://www.editeur.org/onix/3.0/reference}Bible"/>
 *         &lt;sequence>
 *           &lt;element ref="{http://www.editeur.org/onix/3.0/reference}ReligiousTextIdentifier"/>
 *           &lt;element ref="{http://www.editeur.org/onix/3.0/reference}ReligiousTextFeature" maxOccurs="unbounded"/>
 *         &lt;/sequence>
 *       &lt;/choice>
 *       &lt;attGroup ref="{http://www.editeur.org/onix/3.0/reference}generalAttributes"/>
 *       &lt;attribute name="refname">
 *         &lt;simpleType>
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token">
 *             &lt;enumeration value="ReligiousText"/>
 *           &lt;/restriction>
 *         &lt;/simpleType>
 *       &lt;/attribute>
 *       &lt;attribute name="shortname">
 *         &lt;simpleType>
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token">
 *             &lt;enumeration value="religioustext"/>
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
    "bible",
    "religiousTextIdentifier",
    "religiousTextFeature"
})
@XmlRootElement(name = "ReligiousText")
public class ReligiousText {

    @XmlElement(name = "Bible")
    protected Bible bible;
    @XmlElement(name = "ReligiousTextIdentifier")
    protected ReligiousTextIdentifier religiousTextIdentifier;
    @XmlElement(name = "ReligiousTextFeature")
    protected List<ReligiousTextFeature> religiousTextFeature;
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
     * Gets the value of the bible property.
     * 
     * @return
     *     possible object is
     *     {@link Bible }
     *     
     */
    public Bible getBible() {
        return bible;
    }

    /**
     * Sets the value of the bible property.
     * 
     * @param value
     *     allowed object is
     *     {@link Bible }
     *     
     */
    public void setBible(Bible value) {
        this.bible = value;
    }

    /**
     * Gets the value of the religiousTextIdentifier property.
     * 
     * @return
     *     possible object is
     *     {@link ReligiousTextIdentifier }
     *     
     */
    public ReligiousTextIdentifier getReligiousTextIdentifier() {
        return religiousTextIdentifier;
    }

    /**
     * Sets the value of the religiousTextIdentifier property.
     * 
     * @param value
     *     allowed object is
     *     {@link ReligiousTextIdentifier }
     *     
     */
    public void setReligiousTextIdentifier(ReligiousTextIdentifier value) {
        this.religiousTextIdentifier = value;
    }

    /**
     * Gets the value of the religiousTextFeature property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the religiousTextFeature property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getReligiousTextFeature().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ReligiousTextFeature }
     * 
     * 
     */
    public List<ReligiousTextFeature> getReligiousTextFeature() {
        if (religiousTextFeature == null) {
            religiousTextFeature = new ArrayList<ReligiousTextFeature>();
        }
        return this.religiousTextFeature;
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
