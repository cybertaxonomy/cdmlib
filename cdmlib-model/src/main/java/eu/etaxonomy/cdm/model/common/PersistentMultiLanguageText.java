/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/ 

package eu.etaxonomy.cdm.model.common;

import java.util.List;

import org.hibernate.collection.PersistentMap;
import org.hibernate.engine.SessionImplementor;

public class PersistentMultiLanguageText extends PersistentMap implements IMultiLanguageText {

	public PersistentMultiLanguageText(SessionImplementor sessionImplementor, MultilanguageText collection) {
		super(sessionImplementor, collection);
	}

	public PersistentMultiLanguageText() {
		super();
	}

	@Deprecated
	public LanguageString add(LanguageString languageString) {
		if (languageString == null){
			return null;
		}else{
			return (LanguageString)super.put(languageString.getLanguage(), languageString);
		}
	}

	public LanguageString put(LanguageString languageString) {
		if (languageString == null){
			return null;
		}else{
			return (LanguageString)super.put(languageString.getLanguage(), languageString);
		}
	}
	
	public LanguageString getPreferredLanguageString(List<Language> languages) {
		LanguageString languageString = null;
		for (Language language : languages) {
			languageString = (LanguageString)super.get(language);
			if(languageString != null){
				return languageString;
			}
		}
		return (LanguageString)super.get(Language.DEFAULT());
	}

	public String getText(Language language) {
		LanguageString languageString = (LanguageString)super.get(language);
		if (languageString != null){
			return languageString.getText();
		}else {
			return null;
		}
	}
}
