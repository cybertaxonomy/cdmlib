//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.1-b02-fcs 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2009.11.02 at 12:58:05 PM GMT 
//


package eu.etaxonomy.cdm.remote.dto.oaipmh;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;


/**
 * <p>Java class for deletedRecordType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="deletedRecordType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="no"/>
 *     &lt;enumeration value="persistent"/>
 *     &lt;enumeration value="transient"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlEnum
public enum DeletedRecord {

    @XmlEnumValue("no")
    NO("no"),
    @XmlEnumValue("persistent")
    PERSISTENT("persistent"),
    @XmlEnumValue("transient")
    TRANSIENT("transient");
    private final String value;

    DeletedRecord(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static DeletedRecord fromValue(String v) {
        for (DeletedRecord c: DeletedRecord.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
