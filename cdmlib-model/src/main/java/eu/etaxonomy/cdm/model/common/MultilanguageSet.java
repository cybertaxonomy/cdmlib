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
import org.apache.log4j.Logger;


/**
 * @author m.doering
 * Special array that takes care that all LanguageString elements have a unique language
 */
public class MultilanguageSet extends HashMap<Language, String> {
	static Logger logger = Logger.getLogger(MultilanguageSet.class);
	
	/**
	 * Factory method
	 * @return
	 */
	public static MultilanguageSet NewInstance(){
		MultilanguageSet result =  new MultilanguageSet();
		return result;
	}
	
	/**
	 * Factory method
	 * @return
	 */
	public static MultilanguageSet NewInstance(LanguageString languageString){
		MultilanguageSet result =  new MultilanguageSet(languageString);
		return result;
	}
	
	protected MultilanguageSet(){
		super();
	}
	
	/**
	 * Constructor
	 */
	protected MultilanguageSet (LanguageString languageString){
		super();
		this.add(languageString);
	}
	
	public String getText(Language language){
		return super.get(language);
	}
	
	/**
	 * @param languageString
	 * @return String the previous text in the MultilanguageSet that was associated with the language
	 * defined in languageString, or null if there was no such text before. (A null return can also indicate that the text was previously null.)
	 */
	public String add(LanguageString languageString){
		if (languageString == null){
			return null;
		}else{
			String result =this.put(languageString.getLanguage(), languageString.getText());
			return result;
		}
	}
	

}
