/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.common;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.log4j.Logger;



/**
 * @author m.doering
 * Special array that takes care that all LanguageString elements have a unique language
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "MultilanguageText")
@XmlRootElement(name = "MultilanguageText")
public class MultilanguageText extends HashMap<Language, LanguageString> implements Cloneable{
	private static final long serialVersionUID = 7876604337076705862L;
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(MultilanguageText.class);
		
	/**
	 * Factory method
	 * @return
	 */
	public static MultilanguageText NewInstance(){
		MultilanguageText result =  new MultilanguageText();
		return result;
	}
	
	/**
	 * Factory method
	 * @return
	 */
	public static MultilanguageText NewInstance(LanguageString languageString){
		MultilanguageText result =  new MultilanguageText(languageString);
		return result;
	}
	
	public MultilanguageText(){
		super();
	}
	
	
	/**
	 * Constructor
	 */
	protected MultilanguageText (LanguageString languageString){
		super();
		this.add(languageString);
	}
	
	
	
	/**
	 * @param language
	 * @return
	 */
	public String getText(Language language){
		LanguageString languageString = super.get(language);
		if (languageString != null){
			return languageString.getText();
		}else {
			return null;
		}
	}
	
	/**
	 * @param languageString
	 * @return String the previous text in the MultilanguageSet that was associated with the language
	 * defined in languageString, or null if there was no such text before. (A null return can also indicate that the text was previously null.)
	 */
	public LanguageString add(LanguageString languageString){
		if (languageString == null){
			return null;
		}else{
			return this.put(languageString.getLanguage(), languageString);
		}
	}
	
	
	/**
	 * Iterates on the languages. As soon as there exists a language string for this language in this multilanguage text
	 * it is returned.
	 * @param languages
	 * @return 
	 */
	public LanguageString getPreferredLanguageString(List<Language> languages){
		
		LanguageString languageString = null;
		for (Language language : languages) {
			languageString = super.get(language);
			if(languageString != null){
				return languageString;
			}
		}
		return super.get(Language.DEFAULT());
	}
	
//*********** CLONE **********************************/	
	
	/** 
	 * Clones <i>this</i> multi-language text. This is a shortcut that enables to
	 * create a new instance that differs only slightly from <i>this</i> multi-language text
	 * by modifying only some of the attributes.<BR>
	 * This method overrides the clone method from {@link DerivedUnitBase DerivedUnitBase}.
	 * 
	 * @see DerivedUnitBase#clone()
	 * @see eu.etaxonomy.cdm.model.media.IdentifiableMediaEntity#clone()
	 * @see java.lang.Object#clone()
	 */
	@Override
	public MultilanguageText clone(){
		MultilanguageText result = (MultilanguageText)super.clone();
		Set<Language> languages = super.keySet();
		for (Language language : languages){
			LanguageString languageString = super.get(language);
			this.put(language, languageString);
		}
		//no changes to: -
		return result;
	}

	

}
