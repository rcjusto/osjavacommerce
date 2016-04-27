//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.10 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2011.10.22 at 02:13:30 AM CDT 
//


package org.store.publications.onix;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for List54.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="List54">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="AB"/>
 *     &lt;enumeration value="AD"/>
 *     &lt;enumeration value="CS"/>
 *     &lt;enumeration value="EX"/>
 *     &lt;enumeration value="IP"/>
 *     &lt;enumeration value="MD"/>
 *     &lt;enumeration value="NP"/>
 *     &lt;enumeration value="NY"/>
 *     &lt;enumeration value="OF"/>
 *     &lt;enumeration value="OI"/>
 *     &lt;enumeration value="OP"/>
 *     &lt;enumeration value="OR"/>
 *     &lt;enumeration value="PP"/>
 *     &lt;enumeration value="RF"/>
 *     &lt;enumeration value="RM"/>
 *     &lt;enumeration value="RP"/>
 *     &lt;enumeration value="RU"/>
 *     &lt;enumeration value="TO"/>
 *     &lt;enumeration value="TP"/>
 *     &lt;enumeration value="TU"/>
 *     &lt;enumeration value="UR"/>
 *     &lt;enumeration value="WR"/>
 *     &lt;enumeration value="WS"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "List54")
@XmlEnum
public enum List54 {


    /**
     * Publication abandoned after having been announced.
     * 
     */
    AB,

    /**
     * Apply direct to publisher, item not available to trade.
     * 
     */
    AD,

    /**
     * Check with customer service.
     * 
     */
    CS,

    /**
     * Wholesaler or vendor only.
     * 
     */
    EX,

    /**
     * In-print and in stock.
     * 
     */
    IP,

    /**
     * May be accompanied by an estimated average time to supply.
     * 
     */
    MD,

    /**
     * MUST be accompanied by an expected availability date.
     * 
     */
    NP,

    /**
     * Wholesaler or vendor only: MUST be accompanied by expected availability date.
     * 
     */
    NY,

    /**
     * This format is out of print, but another format is available: should be accompanied by an identifier for the alternative product.
     * 
     */
    OF,

    /**
     * No current plan to reprint.
     * 
     */
    OI,

    /**
     * Discontinued, deleted from catalogue.
     * 
     */
    OP,

    /**
     * This edition is out of print, but a new edition has been or will soon be published: should be accompanied by an identifier for the new edition.
     * 
     */
    OR,

    /**
     * Publication has been announced, and subsequently postponed with no new date.
     * 
     */
    PP,

    /**
     * Supply of this item has been transferred to another publisher or distributor: should be accompanied by an identifier for the new supplier.
     * 
     */
    RF,
    RM,

    /**
     * MUST be accompanied by an expected availability date.
     * 
     */
    RP,

    /**
     * Use instead of RP as a last resort, only if it is really impossible to give an expected availability date.
     * 
     */
    RU,

    /**
     * This item is not stocked but has to be specially ordered from a supplier (eg import item not stocked locally): may be accompanied by an estimated average time to supply.
     * 
     */
    TO,

    /**
     * Wholesaler or vendor only.
     * 
     */
    TP,

    /**
     * MUST be accompanied by an expected availability date.
     * 
     */
    TU,

    /**
     * The item is out of stock but will be reissued under the same ISBN: MUST be accompanied by an expected availability date and by the reissue date in the <Reissue> composite. See notes on the <Reissue> composite for details on treatment of availability status during reissue.
     * 
     */
    UR,

    /**
     * MUST be accompanied by the remainder date.
     * 
     */
    WR,

    /**
     * Typically, withdrawn indefinitely for legal reasons.
     * 
     */
    WS;

    public String value() {
        return name();
    }

    public static List54 fromValue(String v) {
        return valueOf(v);
    }

}
