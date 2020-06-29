/**
* Copyright (C) 2008 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.common;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

import eu.etaxonomy.cdm.model.term.TermBase;

/**
 * @author a.babadshanjan
 * @since 15.09.2008
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "MultilanguageTextHelper", propOrder = {
    "language",
    "languageString"
})
@XmlRootElement(name = "MultilanguageTextHelper")
public class MultilanguageTextHelper {

	@XmlElement(name = "Language")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
	private Language language;

	@XmlElement(name = "LanguageString")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
	private LanguageString languageString;

// TODO: Need a HashMap instead of just one pair of Language/LanguageString
//	private HashMap<Language, LanguageString> mlText;

	public MultilanguageTextHelper() {
	}

	public MultilanguageTextHelper(Language language, LanguageString languageString) {
	this.language = language;
	this.languageString = languageString;
	}

	@Transient
	public Language getLanguage() {
		return language;
	}

	public void setLanguage(Language language) {
		this.language = language;
	}

	@Transient
	public LanguageString getLanguageString() {
		return languageString;
	}

	public void setLanguageString(LanguageString languageString) {
		this.languageString = languageString;
	}


    /**
     * Returns the LanguageString in the preferred language. Preferred languages
     * are specified by the parameter languages, which receives a list of
     * Language instances in the order of preference. If no representation in
     * any preferred languages is found the method falls back to return the
     * Representation in Language.DEFAULT() and if necessary further falls back
     * to return the first element found if any.
     *
     * TODO think about this fall-back strategy &
     * see also {@link TermBase#getPreferredRepresentation(List)}
     *
     * @param multilanguageText the multi-language text map
     * @param languages the ordered list of preferred languages
     * @return the best matching language string
     */
	public static LanguageString getPreferredLanguageString(Map<Language, LanguageString> multilanguageText,
	        List<Language> languages) {
	    boolean restrictToGivenLanguages = false;
	    return getPreferredLanguageObject(multilanguageText, languages, restrictToGivenLanguages);
	}

	/**
	 * See {@link #getPreferredLanguageString(Map, List)}. If restrictToGivenLanguages is <code>true</code>
	 * a non-<code>null</code> result is returned if a language representation for one
	 * of the given languages exists. No default or arbitrary representation is used.
	 * @param multilanguageText the multi-language text map
	 * @param languages the ordered list of preferred languages
	 * @param restrictToGivenLanguages flag to indicate if a fall-back language string should be used or not
	 * @return the best matching language string
	 */
	public static LanguageString getPreferredLanguageString(Map<Language, LanguageString> multilanguageText,
	            List<Language> languages, boolean restrictToGivenLanguages) {
		return getPreferredLanguageObject(multilanguageText, languages, restrictToGivenLanguages);
	}

	/**
     * See {@link #getPreferredLanguageString(Map, List)}. If restrictToGivenLanguages is <code>true</code>
     * a non-<code>null</code> result is returned if a language representation for one
     * of the given languages exists. No default or arbitrary representation is used.
     * @param lstringMap Map with LAnguate as key and LSTRNG as value
     * @param languages the ordered list of preferred languages
     * @param restrictToGivenLanguages flag to indicate if a fall-back language string should be used or not
     * @return
     * @return the best matching language string
     */
    public static <LSTRING> LSTRING getPreferredLanguageObject(Map<Language, LSTRING> lstringMap,
                List<Language> languages) {
        return getPreferredLanguageObject(lstringMap, languages, false);
    }

	/**
     * See {@link #getPreferredLanguageString(Map, List)}. If restrictToGivenLanguages is <code>true</code>
     * a non-<code>null</code> result is returned if a language representation for one
     * of the given languages exists. No default or arbitrary representation is used.
     * @param lstringMap Map with LAnguate as key and LSTRNG as value
     * @param languages the ordered list of preferred languages
     * @param restrictToGivenLanguages flag to indicate if a fall-back language string should be used or not
	 * @return
     * @return the best matching language string
     */
    public static <LSTRING> LSTRING getPreferredLanguageObject(Map<Language, LSTRING> lstringMap,
                List<Language> languages, boolean restrictToGivenLanguages) {

        LSTRING lstring = null;
        if(languages != null){
            for(Language language : languages) {
                lstring = lstringMap.get(language);
                if(lstring != null){
                    return lstring;
                }
            }
        }
        if (!restrictToGivenLanguages){
            lstring = lstringMap.get(Language.DEFAULT());

            if(lstring == null && lstringMap.size() > 0){
                Iterator<LSTRING> it = lstringMap.values().iterator();
                if(it.hasNext()){
                    lstring = it.next();
                }
            }
        }
        return lstring;
    }

	/**
	 * Returns a {@link Set} of {@link Language Languages} that are contained in the given multi-language map
	 * @param multilanguageText
	 * @return
	 */
	public static Set<Language> getLanguages(Map<Language, LanguageString> multilanguageText){
		return multilanguageText.keySet();
	}
}
