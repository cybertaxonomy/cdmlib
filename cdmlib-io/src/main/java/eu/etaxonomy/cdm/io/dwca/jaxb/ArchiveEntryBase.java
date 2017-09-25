/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.dwca.jaxb;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * @author a.mueller
 * @date 17.10.2011
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ArchiveEntryBase", propOrder = {
    "files",
    "field"
})
@XmlRootElement(name = "archiveEntryBase")
public abstract class ArchiveEntryBase {


    @XmlElement(required = true)
    protected Files files;
     @XmlElement(required = true)
    protected List<Field> field;
    @XmlAttribute(required = true)
    protected String rowType;
    @XmlAttribute(required = true)
    protected String linesTerminatedBy;
    @XmlAttribute(required = true)
    protected boolean ignoreHeaderLines;
    @XmlAttribute(required = true)
    protected String fieldsTerminatedBy;
    @XmlAttribute(required = true)
    protected String fieldsEnclosedBy;
    @XmlAttribute(required = true)
    protected String encoding;

    public Files getFiles() {
        return files;
    }
    public void setFiles(Files value) {
        this.files = value;
    }


    /**
     * Gets the value of the field property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the field property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getField().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Field }
     *
     *
     */
    public List<Field> getField() {
        if (field == null) {
            field = new ArrayList<>();
        }
        return this.field;
    }

    public String getRowType() {
        return rowType;
    }
    public void setRowType(String value) {
        this.rowType = value;
    }

    public String getLinesTerminatedBy() {
        return normalizeSpecialChars(linesTerminatedBy);
    }
    public void setLinesTerminatedBy(String value) {
        this.linesTerminatedBy = normalizeSpecialChars(value);
    }

    private String normalizeSpecialChars(String string) {
		if (string != null){
	    	if (string.startsWith("\\")){
				if (string.equals("\\t")){
					string = "\t";
				}
			}
	    	//TODO some more
		}
		return string;
	}

	/**
     * Gets the value of the ignoreHeaderLines property.
     *
     */
    public boolean getIgnoreHeaderLines() {
        return ignoreHeaderLines;
    }

    /**
     * Sets the value of the ignoreHeaderLines property.
     *
     */
    public void setIgnoreHeaderLines(boolean value) {
        this.ignoreHeaderLines = value;
    }

    /**
     * Gets the value of the fieldsTerminatedBy property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getFieldsTerminatedBy() {
        return normalizeSpecialChars(fieldsTerminatedBy);
    }

    /**
     * Sets the value of the fieldsTerminatedBy property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setFieldsTerminatedBy(String value) {
        this.fieldsTerminatedBy = normalizeSpecialChars(value);
    }

    /**
     * Gets the value of the fieldsEnclosedBy property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getFieldsEnclosedBy() {
        return normalizeSpecialChars(fieldsEnclosedBy);
    }

    /**
     * Sets the value of the fieldsEnclosedBy property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setFieldsEnclosedBy(String value) {
        this.fieldsEnclosedBy = normalizeSpecialChars(value);
    }

    /**
     * Gets the value of the encoding property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getEncoding() {
        return encoding;
    }

    /**
     * Sets the value of the encoding property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setEncoding(String value) {
        this.encoding = value;
    }
}
