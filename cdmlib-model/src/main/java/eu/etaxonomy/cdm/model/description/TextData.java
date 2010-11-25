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
import java.util.List;
import java.util.Map;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.IndexedEmbedded;

import eu.etaxonomy.cdm.jaxb.MultilanguageTextAdapter;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.LanguageString;
import eu.etaxonomy.cdm.model.common.MultilanguageTextHelper;
import eu.etaxonomy.cdm.model.common.TermBase;


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
    "multilanguageText",
    "format"
})
@XmlRootElement(name = "TextData")
@Entity
@Audited
@Indexed(index = "eu.etaxonomy.cdm.model.description.DescriptionElementBase")
public class TextData extends DescriptionElementBase {
	private static final long serialVersionUID = -2165015581278282615L;
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(TextData.class);

	//@XmlElement(name = "MultiLanguageText", type = MultilanguageText.class)
	@XmlElement(name = "MultiLanguageText")
    @XmlJavaTypeAdapter(MultilanguageTextAdapter.class)
    @OneToMany (fetch= FetchType.LAZY)
    @Cascade({CascadeType.SAVE_UPDATE,CascadeType.MERGE, CascadeType.DELETE, CascadeType.DELETE_ORPHAN })
    @IndexedEmbedded
    @NotNull
	private Map<Language, LanguageString> multilanguageText = new HashMap<Language,LanguageString>();
	
	@XmlElement(name = "Format")
	@XmlIDREF
	@XmlSchemaType(name = "IDREF")
	@ManyToOne(fetch = FetchType.LAZY)
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
    public Map<Language, LanguageString> getMultilanguageText() {
		return multilanguageText;
	}
    
    /**
     * Sets the multilanguage text. 
	 * The different {@link LanguageString language strings} (texts) contained in the
	 * multilanguage text should all have the same meaning.
     * 
     * @param multilanguageText
     */
    public void setMultilanguageText(Map<Language,LanguageString> multilanguageText) {
    	this.multilanguageText = multilanguageText;
    }
    
    /**
     * Returns the multilanguage text with the content of <i>this</i> text data for
     * a specific language.
     * 
     * @param language the language in which the text string looked for is formulated
     * @return
     */
    public LanguageString getLanguageText(Language language){
    	return multilanguageText.get(language);
    }
 
	/** 
	 * Returns the text string in the given {@link Language language} with the content
	 * of <i>this</i> text data.
	 * 
	 * @param language	the language in which the text string looked for is formulated
	 * @see				#getMultilanguageText(Language)
	 */ 
	public String getText(Language language) {
		LanguageString languageString = multilanguageText.get(language);
		if (languageString == null){
			return null;
		}else{
			return languageString.getText();
		}
	}
    
    /**
	 * Returns the LanguageString in the preferred language. Preferred languages
	 * are specified by the parameter languages, which receives a list of
	 * Language instances in the order of preference. If no representation in
	 * any preferred languages is found the method falls back to return the
	 * Representation in Language.DEFAULT() and if neccesary further falls back
	 * to return the first element found if any.
	 * 
	 * TODO think about this fall-back strategy & 
	 * see also {@link TermBase#getPreferredRepresentation(List)}
	 * 
	 * @param languages
	 * @return
	 */
	public LanguageString getPreferredLanguageString(List<Language> languages) {
		return MultilanguageTextHelper.getPreferredLanguageString(multilanguageText, languages);
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
	public LanguageString putText(String text, Language language) {
		LanguageString result = this.multilanguageText.put(language , LanguageString.NewInstance(text, language));
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
	public LanguageString putText(LanguageString languageString) {
		
		if (languageString == null){
			return null;
		}else{
			Language language = languageString.getLanguage();
			return this.multilanguageText.put(language, languageString);
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
		return this.multilanguageText.remove(language);
	}
	
	/** 
	 * Returns the number of {@link Language languages} in which the content
	 * of <i>this</i> text data has been formulated.
	 * 
	 * @see	#getMultilanguageText()
	 */
	public int countLanguages(){
		return multilanguageText.size();
	}
	

	/** 
	 * Returns the {@link TextFormat format} used for structuring the text representing
	 * the content of <i>this</i> text data.
	 * 
	 * @see	#getMultilanguageText()
	 */
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