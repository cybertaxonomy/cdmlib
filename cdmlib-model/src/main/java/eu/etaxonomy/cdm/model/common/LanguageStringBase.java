/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.common;

import org.apache.log4j.Logger;
import javax.persistence.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

/**
 * @author a.mueller
 * @version 1.0
 * @created 25.04.2008
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "LanguageStringBase", propOrder = {
    "text",
    "language"
})
@MappedSuperclass
public abstract class LanguageStringBase extends VersionableEntity{
	
	static Logger logger = Logger.getLogger(LanguageStringBase.class);

	@XmlElement(name = "Text")
	protected String text;
	
	@XmlElement(name = "Language")
	@XmlIDREF
	@XmlSchemaType(name = "IDREF")
	protected Language language;

	protected LanguageStringBase() {
		super();
	}

	protected LanguageStringBase(String text, Language language) {
		super();
		this.setLanguage(language);
		this.setText(text);
		
	}
	
	@ManyToOne
	//@Cascade({CascadeType.SAVE_UPDATE})
	public Language getLanguage(){
		return this.language;
	}
	public void setLanguage(Language language){
		this.language = language;
	}

	@Column(length=4096)
	public String getText(){
		return this.text;
	}
	protected void setText(String text) {
		this.text = text;
	}
	
	@Transient
	public String getLanguageLabel(){
		if (language != null){
			return this.language.getRepresentation(Language.DEFAULT()).getLabel();
		}else{
			return null;
		}
	}
	@Transient
	public String getLanguageLabel(Language lang){
		if (language != null){
			return this.language.getRepresentation(lang).getLabel();
		}else{
			return null;
		}
	}
}