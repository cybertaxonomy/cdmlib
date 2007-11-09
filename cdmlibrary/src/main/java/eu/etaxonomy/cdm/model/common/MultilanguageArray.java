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

import org.apache.log4j.Logger;

/**
 * @author markus
 * Special array that takes care that all LanguageString elements habe a unique language
 */
public class MultilanguageArray extends ArrayList<LanguageString>{
	static Logger logger = Logger.getLogger(MultilanguageArray.class);

	public LanguageString addText(String text, Language lang){
		LanguageString ls = new LanguageString(text, lang);
		super.add(ls);
		return ls;
	}
}
