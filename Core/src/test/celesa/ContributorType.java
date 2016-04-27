
package test.celesa;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ContributorType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ContributorType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="SequenceNumber" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="ContributorRole" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="PersonName" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="PersonNameInverted" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ContributorType", propOrder = {
    "sequenceNumber",
    "contributorRole",
    "personName",
    "personNameInverted"
})
public class ContributorType {

    @XmlElement(name = "SequenceNumber", required = true)
    protected String sequenceNumber;
    @XmlElement(name = "ContributorRole", required = true)
    protected String contributorRole;
    @XmlElement(name = "PersonName", required = true)
    protected String personName;
    @XmlElement(name = "PersonNameInverted", required = true)
    protected String personNameInverted;

    /**
     * Gets the value of the sequenceNumber property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSequenceNumber() {
        return sequenceNumber;
    }

    /**
     * Sets the value of the sequenceNumber property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSequenceNumber(String value) {
        this.sequenceNumber = value;
    }

    /**
     * Gets the value of the contributorRole property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getContributorRole() {
        return contributorRole;
    }

    /**
     * Sets the value of the contributorRole property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setContributorRole(String value) {
        this.contributorRole = value;
    }

    /**
     * Gets the value of the personName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPersonName() {
        return personName;
    }

    /**
     * Sets the value of the personName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPersonName(String value) {
        this.personName = value;
    }

    /**
     * Gets the value of the personNameInverted property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPersonNameInverted() {
        return personNameInverted;
    }

    /**
     * Sets the value of the personNameInverted property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPersonNameInverted(String value) {
        this.personNameInverted = value;
    }

}
