
package test.celesa;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ProductType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ProductType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="RecordReference" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="NotificationType" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="RecordSourceType" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="RecordSourceIdentifierType" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="RecordSourceIdentifier" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="ProductIdentifier" type="{}ProductIdentifierType"/>
 *         &lt;element name="Barcode" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="ProductForm" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="TradeCategory" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="Title" type="{}TitleType"/>
 *         &lt;element name="Contributor" type="{}ContributorType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="NoContributor" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Language" type="{}LanguageType"/>
 *         &lt;element name="NumberOfPages" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="PagesArabic" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="BASICMainSubject" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="BICMainSubject" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="MainSubject" type="{}MainSubjectType"/>
 *         &lt;element name="AudienceCode" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="OtherText" type="{}OtherTextType" minOccurs="0"/>
 *         &lt;element name="MediaFile" type="{}MediaFileType" minOccurs="0"/>
 *         &lt;element name="Imprint" type="{}ImprintType"/>
 *         &lt;element name="Publisher" type="{}PublisherType"/>
 *         &lt;element name="CityOfPublication" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="CountryOfPublication" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="PublishingStatus" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="PublicationDate" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="YearFirstPublished" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="RelatedProduct" type="{}RelatedProductType" minOccurs="0"/>
 *         &lt;element name="SupplyDetail" type="{}SupplyDetailType"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ProductType", propOrder = {
    "recordReference",
    "notificationType",
    "recordSourceType",
    "recordSourceIdentifierType",
    "recordSourceIdentifier",
    "productIdentifier",
    "barcode",
    "productForm",
    "tradeCategory",
    "title",
    "contributor",
    "noContributor",
    "language",
    "numberOfPages",
    "pagesArabic",
    "basicMainSubject",
    "bicMainSubject",
    "mainSubject",
    "audienceCode",
    "otherText",
    "mediaFile",
    "imprint",
    "publisher",
    "cityOfPublication",
    "countryOfPublication",
    "publishingStatus",
    "publicationDate",
    "yearFirstPublished",
    "relatedProduct",
    "supplyDetail"
})
public class ProductType {

    @XmlElement(name = "RecordReference", required = true)
    protected String recordReference;
    @XmlElement(name = "NotificationType", required = true)
    protected String notificationType;
    @XmlElement(name = "RecordSourceType", required = true)
    protected String recordSourceType;
    @XmlElement(name = "RecordSourceIdentifierType", required = true)
    protected String recordSourceIdentifierType;
    @XmlElement(name = "RecordSourceIdentifier", required = true)
    protected String recordSourceIdentifier;
    @XmlElement(name = "ProductIdentifier", required = true)
    protected ProductIdentifierType productIdentifier;
    @XmlElement(name = "Barcode", required = true)
    protected String barcode;
    @XmlElement(name = "ProductForm", required = true)
    protected String productForm;
    @XmlElement(name = "TradeCategory", required = true)
    protected String tradeCategory;
    @XmlElement(name = "Title", required = true)
    protected TitleType title;
    @XmlElement(name = "Contributor")
    protected List<ContributorType> contributor;
    @XmlElement(name = "NoContributor")
    protected String noContributor;
    @XmlElement(name = "Language", required = true)
    protected LanguageType language;
    @XmlElement(name = "NumberOfPages", required = true)
    protected String numberOfPages;
    @XmlElement(name = "PagesArabic", required = true)
    protected String pagesArabic;
    @XmlElement(name = "BASICMainSubject", required = true)
    protected String basicMainSubject;
    @XmlElement(name = "BICMainSubject", required = true)
    protected String bicMainSubject;
    @XmlElement(name = "MainSubject", required = true)
    protected MainSubjectType mainSubject;
    @XmlElement(name = "AudienceCode", required = true)
    protected String audienceCode;
    @XmlElement(name = "OtherText")
    protected OtherTextType otherText;
    @XmlElement(name = "MediaFile")
    protected MediaFileType mediaFile;
    @XmlElement(name = "Imprint", required = true)
    protected ImprintType imprint;
    @XmlElement(name = "Publisher", required = true)
    protected PublisherType publisher;
    @XmlElement(name = "CityOfPublication", required = true)
    protected String cityOfPublication;
    @XmlElement(name = "CountryOfPublication", required = true)
    protected String countryOfPublication;
    @XmlElement(name = "PublishingStatus", required = true)
    protected String publishingStatus;
    @XmlElement(name = "PublicationDate", required = true)
    protected String publicationDate;
    @XmlElement(name = "YearFirstPublished", required = true)
    protected String yearFirstPublished;
    @XmlElement(name = "RelatedProduct")
    protected RelatedProductType relatedProduct;
    @XmlElement(name = "SupplyDetail", required = true)
    protected SupplyDetailType supplyDetail;

    /**
     * Gets the value of the recordReference property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRecordReference() {
        return recordReference;
    }

    /**
     * Sets the value of the recordReference property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRecordReference(String value) {
        this.recordReference = value;
    }

    /**
     * Gets the value of the notificationType property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getNotificationType() {
        return notificationType;
    }

    /**
     * Sets the value of the notificationType property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNotificationType(String value) {
        this.notificationType = value;
    }

    /**
     * Gets the value of the recordSourceType property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRecordSourceType() {
        return recordSourceType;
    }

    /**
     * Sets the value of the recordSourceType property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRecordSourceType(String value) {
        this.recordSourceType = value;
    }

    /**
     * Gets the value of the recordSourceIdentifierType property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRecordSourceIdentifierType() {
        return recordSourceIdentifierType;
    }

    /**
     * Sets the value of the recordSourceIdentifierType property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRecordSourceIdentifierType(String value) {
        this.recordSourceIdentifierType = value;
    }

    /**
     * Gets the value of the recordSourceIdentifier property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRecordSourceIdentifier() {
        return recordSourceIdentifier;
    }

    /**
     * Sets the value of the recordSourceIdentifier property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRecordSourceIdentifier(String value) {
        this.recordSourceIdentifier = value;
    }

    /**
     * Gets the value of the productIdentifier property.
     * 
     * @return
     *     possible object is
     *     {@link ProductIdentifierType }
     *     
     */
    public ProductIdentifierType getProductIdentifier() {
        return productIdentifier;
    }

    /**
     * Sets the value of the productIdentifier property.
     * 
     * @param value
     *     allowed object is
     *     {@link ProductIdentifierType }
     *     
     */
    public void setProductIdentifier(ProductIdentifierType value) {
        this.productIdentifier = value;
    }

    /**
     * Gets the value of the barcode property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBarcode() {
        return barcode;
    }

    /**
     * Sets the value of the barcode property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBarcode(String value) {
        this.barcode = value;
    }

    /**
     * Gets the value of the productForm property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getProductForm() {
        return productForm;
    }

    /**
     * Sets the value of the productForm property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setProductForm(String value) {
        this.productForm = value;
    }

    /**
     * Gets the value of the tradeCategory property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTradeCategory() {
        return tradeCategory;
    }

    /**
     * Sets the value of the tradeCategory property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTradeCategory(String value) {
        this.tradeCategory = value;
    }

    /**
     * Gets the value of the title property.
     * 
     * @return
     *     possible object is
     *     {@link TitleType }
     *     
     */
    public TitleType getTitle() {
        return title;
    }

    /**
     * Sets the value of the title property.
     * 
     * @param value
     *     allowed object is
     *     {@link TitleType }
     *     
     */
    public void setTitle(TitleType value) {
        this.title = value;
    }

    /**
     * Gets the value of the contributor property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the contributor property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getContributor().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ContributorType }
     * 
     * 
     */
    public List<ContributorType> getContributor() {
        if (contributor == null) {
            contributor = new ArrayList<ContributorType>();
        }
        return this.contributor;
    }

    /**
     * Gets the value of the noContributor property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getNoContributor() {
        return noContributor;
    }

    /**
     * Sets the value of the noContributor property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNoContributor(String value) {
        this.noContributor = value;
    }

    /**
     * Gets the value of the language property.
     * 
     * @return
     *     possible object is
     *     {@link LanguageType }
     *     
     */
    public LanguageType getLanguage() {
        return language;
    }

    /**
     * Sets the value of the language property.
     * 
     * @param value
     *     allowed object is
     *     {@link LanguageType }
     *     
     */
    public void setLanguage(LanguageType value) {
        this.language = value;
    }

    /**
     * Gets the value of the numberOfPages property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getNumberOfPages() {
        return numberOfPages;
    }

    /**
     * Sets the value of the numberOfPages property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNumberOfPages(String value) {
        this.numberOfPages = value;
    }

    /**
     * Gets the value of the pagesArabic property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPagesArabic() {
        return pagesArabic;
    }

    /**
     * Sets the value of the pagesArabic property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPagesArabic(String value) {
        this.pagesArabic = value;
    }

    /**
     * Gets the value of the basicMainSubject property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBASICMainSubject() {
        return basicMainSubject;
    }

    /**
     * Sets the value of the basicMainSubject property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBASICMainSubject(String value) {
        this.basicMainSubject = value;
    }

    /**
     * Gets the value of the bicMainSubject property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBICMainSubject() {
        return bicMainSubject;
    }

    /**
     * Sets the value of the bicMainSubject property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBICMainSubject(String value) {
        this.bicMainSubject = value;
    }

    /**
     * Gets the value of the mainSubject property.
     * 
     * @return
     *     possible object is
     *     {@link MainSubjectType }
     *     
     */
    public MainSubjectType getMainSubject() {
        return mainSubject;
    }

    /**
     * Sets the value of the mainSubject property.
     * 
     * @param value
     *     allowed object is
     *     {@link MainSubjectType }
     *     
     */
    public void setMainSubject(MainSubjectType value) {
        this.mainSubject = value;
    }

    /**
     * Gets the value of the audienceCode property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAudienceCode() {
        return audienceCode;
    }

    /**
     * Sets the value of the audienceCode property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAudienceCode(String value) {
        this.audienceCode = value;
    }

    /**
     * Gets the value of the otherText property.
     * 
     * @return
     *     possible object is
     *     {@link OtherTextType }
     *     
     */
    public OtherTextType getOtherText() {
        return otherText;
    }

    /**
     * Sets the value of the otherText property.
     * 
     * @param value
     *     allowed object is
     *     {@link OtherTextType }
     *     
     */
    public void setOtherText(OtherTextType value) {
        this.otherText = value;
    }

    /**
     * Gets the value of the mediaFile property.
     * 
     * @return
     *     possible object is
     *     {@link MediaFileType }
     *     
     */
    public MediaFileType getMediaFile() {
        return mediaFile;
    }

    /**
     * Sets the value of the mediaFile property.
     * 
     * @param value
     *     allowed object is
     *     {@link MediaFileType }
     *     
     */
    public void setMediaFile(MediaFileType value) {
        this.mediaFile = value;
    }

    /**
     * Gets the value of the imprint property.
     * 
     * @return
     *     possible object is
     *     {@link ImprintType }
     *     
     */
    public ImprintType getImprint() {
        return imprint;
    }

    /**
     * Sets the value of the imprint property.
     * 
     * @param value
     *     allowed object is
     *     {@link ImprintType }
     *     
     */
    public void setImprint(ImprintType value) {
        this.imprint = value;
    }

    /**
     * Gets the value of the publisher property.
     * 
     * @return
     *     possible object is
     *     {@link PublisherType }
     *     
     */
    public PublisherType getPublisher() {
        return publisher;
    }

    /**
     * Sets the value of the publisher property.
     * 
     * @param value
     *     allowed object is
     *     {@link PublisherType }
     *     
     */
    public void setPublisher(PublisherType value) {
        this.publisher = value;
    }

    /**
     * Gets the value of the cityOfPublication property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCityOfPublication() {
        return cityOfPublication;
    }

    /**
     * Sets the value of the cityOfPublication property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCityOfPublication(String value) {
        this.cityOfPublication = value;
    }

    /**
     * Gets the value of the countryOfPublication property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCountryOfPublication() {
        return countryOfPublication;
    }

    /**
     * Sets the value of the countryOfPublication property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCountryOfPublication(String value) {
        this.countryOfPublication = value;
    }

    /**
     * Gets the value of the publishingStatus property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPublishingStatus() {
        return publishingStatus;
    }

    /**
     * Sets the value of the publishingStatus property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPublishingStatus(String value) {
        this.publishingStatus = value;
    }

    /**
     * Gets the value of the publicationDate property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPublicationDate() {
        return publicationDate;
    }

    /**
     * Sets the value of the publicationDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPublicationDate(String value) {
        this.publicationDate = value;
    }

    /**
     * Gets the value of the yearFirstPublished property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getYearFirstPublished() {
        return yearFirstPublished;
    }

    /**
     * Sets the value of the yearFirstPublished property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setYearFirstPublished(String value) {
        this.yearFirstPublished = value;
    }

    /**
     * Gets the value of the relatedProduct property.
     * 
     * @return
     *     possible object is
     *     {@link RelatedProductType }
     *     
     */
    public RelatedProductType getRelatedProduct() {
        return relatedProduct;
    }

    /**
     * Sets the value of the relatedProduct property.
     * 
     * @param value
     *     allowed object is
     *     {@link RelatedProductType }
     *     
     */
    public void setRelatedProduct(RelatedProductType value) {
        this.relatedProduct = value;
    }

    /**
     * Gets the value of the supplyDetail property.
     * 
     * @return
     *     possible object is
     *     {@link SupplyDetailType }
     *     
     */
    public SupplyDetailType getSupplyDetail() {
        return supplyDetail;
    }

    /**
     * Sets the value of the supplyDetail property.
     * 
     * @param value
     *     allowed object is
     *     {@link SupplyDetailType }
     *     
     */
    public void setSupplyDetail(SupplyDetailType value) {
        this.supplyDetail = value;
    }

}
