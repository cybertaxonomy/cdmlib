/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.description;


import java.util.HashMap;
import java.util.Map;

import eu.etaxonomy.cdm.jaxb.MultilanguageTextAdapter;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.LanguageString;
import eu.etaxonomy.cdm.model.common.MultilanguageText;
import eu.etaxonomy.cdm.model.taxon.Taxon;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import javax.persistence.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * This class represents information pieces expressed in one or several natural
 * languages (for the {@link Feature feature} "medical use" for instance).
 * A {@link TextFormat format} used for structuring the text may also be stated.
 * <P>
 * This class corresponds partially to NaturalLanguageDescriptionType according
 * to the SDD schema.
 *
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:59
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TextData", propOrder = {
    "multiLanguageText",
    "format"
})
@XmlRootElement(name = "TextData")
@Entity
public class TextData extends DescriptionElementBase {
	
	static Logger logger = Logger.getLogger(TextData.class);

	//@XmlElement(name = "MultiLanguageText", type = MultilanguageText.class)
	@XmlElement(name = "MultiLanguageText")
    @XmlJavaTypeAdapter(MultilanguageTextAdapter.class)
	//private MultilanguageText multiLanguageText;
	private Map<Language, LanguageString> multiLanguageText;
	
	@XmlElement(name = "Format")
	@XmlIDREF
	@XmlSchemaType(name = "IDREF")
	private TextFormat format;
	
	// ************* CONSTRUCTORS *************/	
	/** 
	 * Class constructor: creates a new empty text data instance.
	 * 
	 * @see #TextData(Feature)
	 */
	public TextData(){
		this(null);
	}
	
	/** 
	 * Class constructor: creates a new text data instance with the {@link Feature feature}
	 * to be described.
	 * 
	 * @param	feature	the feature the text data refer to
	 * @see 			#TextData()
	 */
	public TextData(Feature feature){
		super(feature);
		initTextSet();
	}
	
	//********* METHODS **************************************/
	/** 
	 * Creates a new empty text data instance.
	 * 
	 * @see #NewInstance(Feature)
	 * @see #NewInstance(String, Language, TextFormat)
	 */
	public static TextData NewInstance(){
		return new TextData();
	}
	
	/** 
	 * Creates a new text data instance with the {@link Feature feature}
	 * to be described.
	 * 
	 * @param	feature	the feature the text data refer to
	 * @see 			#NewInstance()
	 * @see 			#NewInstance(String, Language, TextFormat)
	 */
	public static TextData NewInstance(Feature feature){
		return new TextData(feature);
	}
	
	/** 
	 * Creates a new text data instance with a given text in a given particular
	 * {@link Language language} and with the given text format for structuring it.
	 * 
	 * @param	text		the text string with the content of the description 
	 * @param	language	the language in which the text string is formulated
	 * @param	format		the text format used to structure the text string
	 * @see 				#NewInstance()
	 * @see 				#NewInstance(Feature)
	 */
	public static TextData NewInstance(String text, Language language, TextFormat format){
		TextData result =  new TextData();
		result.putText(text, language);
		result.setFormat(format);
		return result;
	}

	/** 
	 * Returns the multilanguage text with the content of <i>this</i> text data. 
	 * The different {@link LanguageString language strings} (texts) contained in the
	 * multilanguage text should all have the same meaning.
	 * 
	 * @see	#getText(Language)
	 */
	@OneToMany (fetch= FetchType.LAZY)
	@MapKey(name="language")
    @Cascade({CascadeType.SAVE_UPDATE})
	public Map<Language, LanguageString> getMultilanguageText() {
    //public MultilanguageText getMultilanguageText() {
		initTextSet();
		return multiLanguageText;
	}
	/**
	 * @see	#getMultilanguageText() 
	 */
	protected void setMultilanguageText(Map<Language, LanguageString> texts) {
	//protected void setMultilanguageText(MultilanguageText texts) {
		this.multiLanguageText = texts;
	}
	/** 
	 * Returns the text string in the given {@link Language language} with the content
	 * of <i>this</i> text data.
	 * 
	 * @param language	the language in which the text string looked for is formulated
	 * @see				#getMultilanguageText()
	 */
	@Transient 
	public String getText(Language language) {
		initTextSet();
		LanguageString languageString = multiLanguageText.get(language);
		if (languageString == null){
			return null;
		}else{
			return languageString.getText();
		}
	}
	
	/**
	 * Creates a {@link LanguageString language string} based on the given text string
	 * and the given {@link Language language}, returns it and adds it to the multilanguage 
	 * text representing the content of <i>this</i> text data.
	 * 
	 * @param text		the string representing the content of the text data
	 * 					in a particular language
	 * @param language	the language in which the text string is formulated
	 * @return			the language string
	 * @see    	   		#getMultilanguageText()
	 * @see    	   		#putText(LanguageString)
	 */
	@Transient
	public LanguageString putText(String text, Language language) {
		initTextSet();
		LanguageString result = this.multiLanguageText.put(language , LanguageString.NewInstance(text, language));
		return (result == null ? null : result);
	}
	/**
	 * Adds a translated {@link LanguageString text in a particular language}
	 * to the multilanguage text representing the content of <i>this</i> text data.
	 * The given language string will be returned. 
	 * 
	 * @param languageString	the language string representing the content of
	 * 							the text data in a particular language
	 * @return					the language string
	 * @see    	   				#getMultilanguageText()
	 * @see    	   				#putText(String, Language)
	 */
	@Transient
	public LanguageString putText(LanguageString languageString) {
		initTextSet();
		
		if (languageString == null){
			return null;
		}else{
			Language language = languageString.getLanguage();
			return this.multiLanguageText.put(language, languageString);
		}
	}
	/** 
	 * Removes from the multilanguage representing the content of
	 * <i>this</i> text data the one {@link LanguageString language string}
	 * with the given {@link Language language}. Returns the removed
	 * language string.
	 *
	 * @param  language	the language in which the language string to be removed
	 * 					has been formulated
	 * @return			the language string associated with the given language
	 * @see     		#getMultilanguageText()
	 */
	public LanguageString removeText(Language language) {
		initTextSet();
		return this.multiLanguageText.remove(language);
	}
	
	private void initTextSet(){
		if (multiLanguageText == null){
			multiLanguageText = MultilanguageText.NewInstance();
		}
	}
	
	/** 
	 * Returns the number of {@link Language languages} in which the content
	 * of <i>this</i> text data has been formulated.
	 * 
	 * @see	#getMultilanguageText()
	 */
	public int countLanguages(){
		initTextSet();
		return multiLanguageText.size();
	}
	

	/** 
	 * Returns the {@link TextFormat format} used for structuring the text representing
	 * the content of <i>this</i> text data.
	 * 
	 * @see	#getMultilanguageText()
	 */
	@ManyToOne
	public TextFormat getFormat() {
		return format;
	}
	/** 
	 * @see	#getFormat()
	 */
	public void setFormat(TextFormat format) {
		this.format = format;
	}

}