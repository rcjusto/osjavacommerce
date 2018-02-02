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
 *         &lt;element ref="{http://www.editeur.org/onix/3.0/reference}PublisherRepresentative" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element ref="{http://www.editeur.org/onix/3.0/reference}MarketPublishingStatus"/>
 *         &lt;element ref="{http://www.editeur.org/onix/3.0/reference}MarketPublishingStatusNote" minOccurs="0"/>
 *         &lt;element ref="{http://www.editeur.org/onix/3.0/reference}MarketDate" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element ref="{http://www.editeur.org/onix/3.0/reference}PromotionCampaign" minOccurs="0"/>
 *         &lt;element ref="{http://www.editeur.org/onix/3.0/reference}PromotionContact" minOccurs="0"/>
 *         &lt;element ref="{http://www.editeur.org/onix/3.0/reference}InitialPrintRun" minOccurs="0"/>
 *         &lt;element ref="{http://www.editeur.org/onix/3.0/reference}ReprintDetail" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element ref="{http://www.editeur.org/onix/3.0/reference}CopiesSold" minOccurs="0"/>
 *         &lt;element ref="{http://www.editeur.org/onix/3.0/reference}BookClubAdoption" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attGroup ref="{http://www.editeur.org/onix/3.0/reference}generalAttributes"/>
 *       &lt;attribute name="refname">
 *         &lt;simpleType>
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token">
 *             &lt;enumeration value="MarketPublishingDetail"/>
 *           &lt;/restriction>
 *         &lt;/simpleType>
 *       &lt;/attribute>
 *       &lt;attribute name="shortname">
 *         &lt;simpleType>
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token">
 *             &lt;enumeration value="marketpublishingdetail"/>
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
    "publisherRepresentative",
    "marketPublishingStatus",
    "marketPublishingStatusNote",
    "marketDate",
    "promotionCampaign",
    "promotionContact",
    "initialPrintRun",
    "reprintDetail",
    "copiesSold",
    "bookClubAdoption"
})
@XmlRootElement(name = "MarketPublishingDetail")
public class MarketPublishingDetail {

    @XmlElement(name = "PublisherRepresentative")
    protected List<PublisherRepresentative> publisherRepresentative;
    @XmlElement(name = "MarketPublishingStatus", required = true)
    protected MarketPublishingStatus marketPublishingStatus;
    @XmlElement(name = "MarketPublishingStatusNote")
    protected MarketPublishingStatusNote marketPublishingStatusNote;
    @XmlElement(name = "MarketDate")
    protected List<MarketDate> marketDate;
    @XmlElement(name = "PromotionCampaign")
    protected PromotionCampaign promotionCampaign;
    @XmlElement(name = "PromotionContact")
    protected PromotionContact promotionContact;
    @XmlElement(name = "InitialPrintRun")
    protected InitialPrintRun initialPrintRun;
    @XmlElement(name = "ReprintDetail")
    protected List<ReprintDetail> reprintDetail;
    @XmlElement(name = "CopiesSold")
    protected CopiesSold copiesSold;
    @XmlElement(name = "BookClubAdoption")
    protected BookClubAdoption bookClubAdoption;
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
     * Gets the value of the publisherRepresentative property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the publisherRepresentative property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getPublisherRepresentative().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link PublisherRepresentative }
     * 
     * 
     */
    public List<PublisherRepresentative> getPublisherRepresentative() {
        if (publisherRepresentative == null) {
            publisherRepresentative = new ArrayList<PublisherRepresentative>();
        }
        return this.publisherRepresentative;
    }

    /**
     * Gets the value of the marketPublishingStatus property.
     * 
     * @return
     *     possible object is
     *     {@link MarketPublishingStatus }
     *     
     */
    public MarketPublishingStatus getMarketPublishingStatus() {
        return marketPublishingStatus;
    }

    /**
     * Sets the value of the marketPublishingStatus property.
     * 
     * @param value
     *     allowed object is
     *     {@link MarketPublishingStatus }
     *     
     */
    public void setMarketPublishingStatus(MarketPublishingStatus value) {
        this.marketPublishingStatus = value;
    }

    /**
     * Gets the value of the marketPublishingStatusNote property.
     * 
     * @return
     *     possible object is
     *     {@link MarketPublishingStatusNote }
     *     
     */
    public MarketPublishingStatusNote getMarketPublishingStatusNote() {
        return marketPublishingStatusNote;
    }

    /**
     * Sets the value of the marketPublishingStatusNote property.
     * 
     * @param value
     *     allowed object is
     *     {@link MarketPublishingStatusNote }
     *     
     */
    public void setMarketPublishingStatusNote(MarketPublishingStatusNote value) {
        this.marketPublishingStatusNote = value;
    }

    /**
     * Gets the value of the marketDate property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the marketDate property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getMarketDate().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link MarketDate }
     * 
     * 
     */
    public List<MarketDate> getMarketDate() {
        if (marketDate == null) {
            marketDate = new ArrayList<MarketDate>();
        }
        return this.marketDate;
    }

    /**
     * Gets the value of the promotionCampaign property.
     * 
     * @return
     *     possible object is
     *     {@link PromotionCampaign }
     *     
     */
    public PromotionCampaign getPromotionCampaign() {
        return promotionCampaign;
    }

    /**
     * Sets the value of the promotionCampaign property.
     * 
     * @param value
     *     allowed object is
     *     {@link PromotionCampaign }
     *     
     */
    public void setPromotionCampaign(PromotionCampaign value) {
        this.promotionCampaign = value;
    }

    /**
     * Gets the value of the promotionContact property.
     * 
     * @return
     *     possible object is
     *     {@link PromotionContact }
     *     
     */
    public PromotionContact getPromotionContact() {
        return promotionContact;
    }

    /**
     * Sets the value of the promotionContact property.
     * 
     * @param value
     *     allowed object is
     *     {@link PromotionContact }
     *     
     */
    public void setPromotionContact(PromotionContact value) {
        this.promotionContact = value;
    }

    /**
     * Gets the value of the initialPrintRun property.
     * 
     * @return
     *     possible object is
     *     {@link InitialPrintRun }
     *     
     */
    public InitialPrintRun getInitialPrintRun() {
        return initialPrintRun;
    }

    /**
     * Sets the value of the initialPrintRun property.
     * 
     * @param value
     *     allowed object is
     *     {@link InitialPrintRun }
     *     
     */
    public void setInitialPrintRun(InitialPrintRun value) {
        this.initialPrintRun = value;
    }

    /**
     * Gets the value of the reprintDetail property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the reprintDetail property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getReprintDetail().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ReprintDetail }
     * 
     * 
     */
    public List<ReprintDetail> getReprintDetail() {
        if (reprintDetail == null) {
            reprintDetail = new ArrayList<ReprintDetail>();
        }
        return this.reprintDetail;
    }

    /**
     * Gets the value of the copiesSold property.
     * 
     * @return
     *     possible object is
     *     {@link CopiesSold }
     *     
     */
    public CopiesSold getCopiesSold() {
        return copiesSold;
    }

    /**
     * Sets the value of the copiesSold property.
     * 
     * @param value
     *     allowed object is
     *     {@link CopiesSold }
     *     
     */
    public void setCopiesSold(CopiesSold value) {
        this.copiesSold = value;
    }

    /**
     * Gets the value of the bookClubAdoption property.
     * 
     * @return
     *     possible object is
     *     {@link BookClubAdoption }
     *     
     */
    public BookClubAdoption getBookClubAdoption() {
        return bookClubAdoption;
    }

    /**
     * Sets the value of the bookClubAdoption property.
     * 
     * @param value
     *     allowed object is
     *     {@link BookClubAdoption }
     *     
     */
    public void setBookClubAdoption(BookClubAdoption value) {
        this.bookClubAdoption = value;
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
