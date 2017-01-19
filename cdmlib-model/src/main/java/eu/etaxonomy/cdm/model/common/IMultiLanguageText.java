/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.common;

import java.util.List;

public interface IMultiLanguageText {

	/**
	 * @param language
	 * @return
	 */
	public String getText(Language language);

	/**
	 * @param languageString
	 * @return String the previous text in the MultilanguageSet that was associated with the language
	 * defined in languageString, or null if there was no such text before. (A null return can also indicate that the text was previously null.)
	 */
	public LanguageString put(LanguageString languageString);

	/**
	 * @param languageString
	 * @return String the previous text in the MultilanguageSet that was associated with the language
	 * defined in languageString, or null if there was no such text before. (A null return can also indicate that the text was previously null.)
	 * @deprecated should follow the put semantic of maps, this method will be removed in v4.0
	 * 					Use the {@link #put(LanguageString) put} method instead
	 */
	@Deprecated
	public LanguageString add(LanguageString languageString);
	/**
	 * Iterates on the languages. As soon as there exists a language string for this language in 
	 * this multilanguage text
	 * it is returned.
	 * @param languages
	 * @return 
	 */
	public LanguageString getPreferredLanguageString(List<Language> languages);

}
