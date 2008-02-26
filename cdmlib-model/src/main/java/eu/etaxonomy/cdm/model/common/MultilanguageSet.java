/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.common;

import java.util.ArrayList;
import java.util.AbstractSet;
import java.util.HashSet;

import javax.persistence.Entity;

import org.apache.log4j.Logger;

/**
 * @author markus
 * Special array that takes care that all LanguageString elements have a unique language
 */
//@Entity
public class MultilanguageSet extends HashSet<LanguageString>{
	static Logger logger = Logger.getLogger(MultilanguageSet.class);

	public void add(String text, Language lang){
		LanguageString ls = new LanguageString(text, lang);
		super.add(ls);
	}
	public void remove(Language lang){
		super.remove(get(lang));
	}
	public LanguageString get(Language lang){
		// FIXME: ...
		for (LanguageString ls : this){
			if (ls.getLanguage()==lang){
				return ls;
			}
		}
		return null;
	}
}
