
package test.celesa;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for MainSubjectType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="MainSubjectType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="MainSubjectSchemeIdentifier" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="SubjectSchemeVersion" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="SubjectCode" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="SubjectHeadingText" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "MainSubjectType", propOrder = {
    "mainSubjectSchemeIdentifier",
    "subjectSchemeVersion",
    "subjectCode",
    "subjectHeadingText"
})
public class MainSubjectType {

    @XmlElement(name = "MainSubjectSchemeIdentifier", required = true)
    protected String mainSubjectSchemeIdentifier;
    @XmlElement(name = "SubjectSchemeVersion", required = true)
    protected String subjectSchemeVersion;
    @XmlElement(name = "SubjectCode", required = true)
    protected String subjectCode;
    @XmlElement(name = "SubjectHeadingText", required = true)
    protected String subjectHeadingText;

    /**
     * Gets the value of the mainSubjectSchemeIdentifier property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMainSubjectSchemeIdentifier() {
        return mainSubjectSchemeIdentifier;
    }

    /**
     * Sets the value of the mainSubjectSchemeIdentifier property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMainSubjectSchemeIdentifier(String value) {
        this.mainSubjectSchemeIdentifier = value;
    }

    /**
     * Gets the value of the subjectSchemeVersion property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSubjectSchemeVersion() {
        return subjectSchemeVersion;
    }

    /**
     * Sets the value of the subjectSchemeVersion property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSubjectSchemeVersion(String value) {
        this.subjectSchemeVersion = value;
    }

    /**
     * Gets the value of the subjectCode property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSubjectCode() {
        return subjectCode;
    }

    /**
     * Sets the value of the subjectCode property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSubjectCode(String value) {
        this.subjectCode = value;
    }

    /**
     * Gets the value of the subjectHeadingText property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSubjectHeadingText() {
        return subjectHeadingText;
    }

    /**
     * Sets the value of the subjectHeadingText property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSubjectHeadingText(String value) {
        this.subjectHeadingText = value;
    }

}
