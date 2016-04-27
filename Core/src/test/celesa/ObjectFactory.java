
package test.celesa;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the test.celesa package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _ONIXMessage_QNAME = new QName("", "ONIXMessage");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: test.celesa
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link RelatedProductType }
     * 
     */
    public RelatedProductType createRelatedProductType() {
        return new RelatedProductType();
    }

    /**
     * Create an instance of {@link ProductIdentifierType }
     * 
     */
    public ProductIdentifierType createProductIdentifierType() {
        return new ProductIdentifierType();
    }

    /**
     * Create an instance of {@link OtherTextType }
     * 
     */
    public OtherTextType createOtherTextType() {
        return new OtherTextType();
    }

    /**
     * Create an instance of {@link ONIXMessageType }
     * 
     */
    public ONIXMessageType createONIXMessageType() {
        return new ONIXMessageType();
    }

    /**
     * Create an instance of {@link ContributorType }
     * 
     */
    public ContributorType createContributorType() {
        return new ContributorType();
    }

    /**
     * Create an instance of {@link ProductType }
     * 
     */
    public ProductType createProductType() {
        return new ProductType();
    }

    /**
     * Create an instance of {@link SupplierIdentifierType }
     * 
     */
    public SupplierIdentifierType createSupplierIdentifierType() {
        return new SupplierIdentifierType();
    }

    /**
     * Create an instance of {@link HeaderType }
     * 
     */
    public HeaderType createHeaderType() {
        return new HeaderType();
    }

    /**
     * Create an instance of {@link DiscountCodedType }
     * 
     */
    public DiscountCodedType createDiscountCodedType() {
        return new DiscountCodedType();
    }

    /**
     * Create an instance of {@link ImprintType }
     * 
     */
    public ImprintType createImprintType() {
        return new ImprintType();
    }

    /**
     * Create an instance of {@link PublisherType }
     * 
     */
    public PublisherType createPublisherType() {
        return new PublisherType();
    }

    /**
     * Create an instance of {@link SupplyDetailType }
     * 
     */
    public SupplyDetailType createSupplyDetailType() {
        return new SupplyDetailType();
    }

    /**
     * Create an instance of {@link LanguageType }
     * 
     */
    public LanguageType createLanguageType() {
        return new LanguageType();
    }

    /**
     * Create an instance of {@link MainSubjectType }
     * 
     */
    public MainSubjectType createMainSubjectType() {
        return new MainSubjectType();
    }

    /**
     * Create an instance of {@link PriceType }
     * 
     */
    public PriceType createPriceType() {
        return new PriceType();
    }

    /**
     * Create an instance of {@link MediaFileType }
     * 
     */
    public MediaFileType createMediaFileType() {
        return new MediaFileType();
    }

    /**
     * Create an instance of {@link TitleType }
     * 
     */
    public TitleType createTitleType() {
        return new TitleType();
    }

    /**
     * Create an instance of {@link WebsiteType }
     * 
     */
    public WebsiteType createWebsiteType() {
        return new WebsiteType();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ONIXMessageType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "ONIXMessage")
    public JAXBElement<ONIXMessageType> createONIXMessage(ONIXMessageType value) {
        return new JAXBElement<ONIXMessageType>(_ONIXMessage_QNAME, ONIXMessageType.class, null, value);
    }

}
