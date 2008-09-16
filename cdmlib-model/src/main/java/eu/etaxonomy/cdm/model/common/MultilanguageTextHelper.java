/**
* Copyright (C) 2008 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
*/

package eu.etaxonomy.cdm.model.common;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

/**
 * @author a.babadshanjan
 * @created 15.09.2008
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "MultilanguageTextHelper", propOrder = {
    "language",
    "languageString"
})
@XmlRootElement(name = "MultilanguageTextHelper")
public class MultilanguageTextHelper {

	@XmlElement(name = "Language")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
	private Language language;
	
	@XmlElement(name = "LanguageString")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
	private LanguageString languageString;
	
//	private HashMap<Language, LanguageString> mlText;
	
	public MultilanguageTextHelper() {
	}
	
	public MultilanguageTextHelper(Language language, LanguageString languageString) {
	this.language = language;
	this.languageString = languageString;
	}
	
	@Transient
	public Language getLanguage() {
		return language;
	}
	
	public void setLanguage(Language language) {
		this.language = language;
	}

	@Transient
	public LanguageString getLanguageString() {
		return languageString;
	}
	
	public void setLanguageString(LanguageString languageString) {
		this.languageString = languageString;
	}
}
