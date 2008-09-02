/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.description;


import java.util.Map;

import eu.etaxonomy.cdm.jaxb.MultilanguageSetAdapter;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.LanguageString;
import eu.etaxonomy.cdm.model.common.MultilanguageSet;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import javax.persistence.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:59
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TextData", propOrder = {
    "multilanguageStringMap",
    "format"
})
@XmlRootElement(name = "TextData")
@Entity
public class TextData extends DescriptionElementBase {
	
	static Logger logger = Logger.getLogger(TextData.class);

	@XmlElement(name = "MultilanguageStringMap")
    @XmlJavaTypeAdapter(MultilanguageSetAdapter.class)
	private Map<Language, LanguageString> multilanguageStringMap;
	
	@XmlElement(name = "Format")
	@XmlIDREF
	@XmlSchemaType(name = "IDREF")
	private TextFormat format;
	
	public static TextData NewInstance(){
		return new TextData();
	}
	
	public static TextData NewInstance(Feature feature){
		return new TextData(feature);
	}
	
	public static TextData NewInstance(String text, Language language, TextFormat format){
		TextData result =  new TextData();
		result.putText(text, language);
		result.setFormat(format);
		return result;
	}
	
	/**
	 * Constructor
	 */
	public TextData(){
		this(null);
	}
	
	public TextData(Feature feature){
		super(feature);
		initTextSet();
	}

	/**
	 * @return
	 */
	@OneToMany (fetch= FetchType.LAZY)
	@MapKey(name="language")
    @Cascade({CascadeType.SAVE_UPDATE})
	public Map<Language, LanguageString> getMultilanguageText() {
		initTextSet();
		return multilanguageStringMap;
	}
	protected void setMultilanguageText(Map<Language, LanguageString> texts) {
		this.multilanguageStringMap = texts;
	}
	@Transient 
	public String getText(Language language) {
		initTextSet();
		LanguageString languageString = multilanguageStringMap.get(language);
		if (languageString == null){
			return null;
		}else{
			return languageString.getText();
		}
	}
	
	@Transient
	public MultilanguageSet getMultilanguageSet() {
		return null;  //TODO
	}

	@Transient
	public LanguageString putText(String text, Language language) {
		initTextSet();
		LanguageString result = this.multilanguageStringMap.put(language , LanguageString.NewInstance(text, language));
		return (result == null ? null : result);
	}
	@Transient
	public LanguageString putText(LanguageString languageString) {
		initTextSet();
		
		if (languageString == null){
			return null;
		}else{
			Language language = languageString.getLanguage();
			return this.multilanguageStringMap.put(language, languageString);
		}
	}
	public LanguageString removeText(Language language) {
		initTextSet();
		return this.multilanguageStringMap.remove(language);
	}
	
	private void initTextSet(){
		if (multilanguageStringMap == null){
			multilanguageStringMap = MultilanguageSet.NewInstance();
		}
	}
	
	public int countLanguages(){
		initTextSet();
		return multilanguageStringMap.size();
	}
	

	@ManyToOne
	public TextFormat getFormat() {
		return format;
	}
	public void setFormat(TextFormat format) {
		this.format = format;
	}

}