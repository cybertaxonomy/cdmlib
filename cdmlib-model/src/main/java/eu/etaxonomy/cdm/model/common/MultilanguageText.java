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

import org.apache.log4j.Logger;

/**
 * @author m.doering
 * Special array that takes care that all LanguageString elements have a unique language
 */
public class MultilanguageText
        extends HashMap<Language, LanguageString>
        implements Cloneable, IMultiLanguageText {

	private static final long serialVersionUID = 7876604337076705862L;
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

	protected MultilanguageText(){
		super();
	}

//******************* CONSTRUCTOR ****************************************/
	protected MultilanguageText (LanguageString languageString){
		super();
		this.put(languageString);
	}

	public MultilanguageText(int initialCapacity, float loadFactor) {
		super(initialCapacity, loadFactor);
	}

//************************ GETTER/SETTER ***********************************/
    @Override
	public String getText(Language language){
		LanguageString languageString = super.get(language);
		if (languageString != null){
			return languageString.getText();
		}else {
			return null;
		}
	}


	@Override
    public LanguageString put(LanguageString languageString){
		if (languageString == null){
			return null;
		}else{
			return this.put(languageString.getLanguage(), languageString);
		}
	}

	@Override
    public LanguageString getPreferredLanguageString(List<Language> languages){
		return MultilanguageTextHelper.getPreferredLanguageString(this, languages);
	}

//*********** CLONE **********************************/

	/**
	 * Clones <i>this</i> multi-language text. This is a shortcut that enables to
	 * create a new instance that differs only slightly from <i>this</i> multi-language text
	 * by modifying only some of the attributes.<BR>
	 *
	 * @see java.lang.Object#clone()
	 */
	@Override
	public MultilanguageText clone() {
		MultilanguageText result = (MultilanguageText)super.clone();

		for (LanguageString languageString : this.values()){
			LanguageString newLanguageString;
			try {
				newLanguageString = (LanguageString)languageString.clone();
				result.put(newLanguageString);
			} catch (CloneNotSupportedException e) {
				logger.error(e);
			}
		}
		//no changes to: -
		return result;
	}

    @Override
    public LanguageString put(Language language, String text) {
        LanguageString languageString = new LanguageString(text, language);
        put(languageString);
        return languageString;
    }

}
