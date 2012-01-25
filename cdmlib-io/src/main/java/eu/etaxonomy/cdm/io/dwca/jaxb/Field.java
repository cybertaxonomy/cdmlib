//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.3 in JDK 1.6 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2011.10.17 at 04:06:44 PM CEST 
//


package eu.etaxonomy.cdm.io.dwca.jaxb;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="term" use="required">
 *         &lt;simpleType>
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyURI">
 *             &lt;enumeration value="http://purl.org/dc/terms/description"/>
 *             &lt;enumeration value="http://purl.org/dc/terms/language"/>
 *             &lt;enumeration value="http://purl.org/dc/terms/source"/>
 *             &lt;enumeration value="http://purl.org/dc/terms/type"/>
 *             &lt;enumeration value="http://rs.tdwg.org/dwc/terms/acceptedNameUsageID"/>
 *             &lt;enumeration value="http://rs.tdwg.org/dwc/terms/scientificNameID"/>
 *             &lt;enumeration value="http://rs.tdwg.org/dwc/terms/taxonConceptID"/>
 *             &lt;enumeration value="http://rs.tdwg.org/dwc/terms/vernacularName"/>
 *           &lt;/restriction>
 *         &lt;/simpleType>
 *       &lt;/attribute>
 *       &lt;attribute name="index" use="required">
 *         &lt;simpleType>
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}byte">
 *             &lt;enumeration value="1"/>
 *             &lt;enumeration value="2"/>
 *             &lt;enumeration value="3"/>
 *             &lt;enumeration value="4"/>
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
@XmlType(name = "")
@XmlRootElement(name = "field")
public class Field {

    @XmlAttribute(required = true)
    protected String term;
    @XmlAttribute(required = true)
    protected byte index;

    /**
     * Gets the value of the term property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTerm() {
        return term;
    }

    /**
     * Sets the value of the term property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTerm(String value) {
        this.term = value;
    }

    /**
     * Gets the value of the index property.
     * 
     */
    public byte getIndex() {
        return index;
    }

    /**
     * Sets the value of the index property.
     * 
     */
    public void setIndex(byte value) {
        this.index = value;
    }

}
