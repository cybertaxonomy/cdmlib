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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

/**
 * @author m.doering
 * Special array that takes care that all LanguageString elements have a unique language
 */
@Entity
public class MultilanguageSet extends CdmBase{
	static Logger logger = Logger.getLogger(MultilanguageSet.class);

	protected Map<Language, LanguageString> languageStrings = new HashMap<Language, LanguageString>();
	
	
	public static MultilanguageSet NewInstance(){
		return new MultilanguageSet();
	}
	
	
	
	@OneToMany
	@Cascade({CascadeType.SAVE_UPDATE})
	public Map<Language, LanguageString> getLanguageStrings(){
		return this.languageStrings;
	}
	public LanguageString put(LanguageString languageString){
		if (languageString == null){
			return null;
		}else {
			return languageStrings.put(languageString.getLanguage(), languageString);
		}
	}
	public LanguageString put(String text, Language language){
		LanguageString languageString = LanguageString.NewInstance(text, language);
		return this.put(languageString);
	}
	public LanguageString remove(Language language){
		return languageStrings.remove(language);
	}
	protected void setLanguageStrings(Map<Language, LanguageString> languageStrings) {
		this.languageStrings = languageStrings;
	}
	

	@Transient
	public String getText(Language language){
		LanguageString languageString = getLanguageString(language);
		return (languageString == null ? null : languageString.getText());
	}
	
	
	@Transient
	public LanguageString getLanguageString(Language language){
		return this.languageStrings.get(language);
	}
	
	public int size(){
		return languageStrings.size();
	}
}
