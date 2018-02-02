
package test.celesa;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for MediaFileType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="MediaFileType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="MediaFileTypeCode" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="MediaFileFormatCode" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="MediaFileLinkTypeCode" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="MediaFileLink" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "MediaFileType", propOrder = {
    "mediaFileTypeCode",
    "mediaFileFormatCode",
    "mediaFileLinkTypeCode",
    "mediaFileLink"
})
public class MediaFileType {

    @XmlElement(name = "MediaFileTypeCode", required = true)
    protected String mediaFileTypeCode;
    @XmlElement(name = "MediaFileFormatCode", required = true)
    protected String mediaFileFormatCode;
    @XmlElement(name = "MediaFileLinkTypeCode", required = true)
    protected String mediaFileLinkTypeCode;
    @XmlElement(name = "MediaFileLink", required = true)
    protected String mediaFileLink;

    /**
     * Gets the value of the mediaFileTypeCode property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMediaFileTypeCode() {
        return mediaFileTypeCode;
    }

    /**
     * Sets the value of the mediaFileTypeCode property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMediaFileTypeCode(String value) {
        this.mediaFileTypeCode = value;
    }

    /**
     * Gets the value of the mediaFileFormatCode property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMediaFileFormatCode() {
        return mediaFileFormatCode;
    }

    /**
     * Sets the value of the mediaFileFormatCode property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMediaFileFormatCode(String value) {
        this.mediaFileFormatCode = value;
    }

    /**
     * Gets the value of the mediaFileLinkTypeCode property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMediaFileLinkTypeCode() {
        return mediaFileLinkTypeCode;
    }

    /**
     * Sets the value of the mediaFileLinkTypeCode property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMediaFileLinkTypeCode(String value) {
        this.mediaFileLinkTypeCode = value;
    }

    /**
     * Gets the value of the mediaFileLink property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMediaFileLink() {
        return mediaFileLink;
    }

    /**
     * Sets the value of the mediaFileLink property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMediaFileLink(String value) {
        this.mediaFileLink = value;
    }

}
