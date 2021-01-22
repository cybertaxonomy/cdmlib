/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.term;

import java.util.Comparator;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.model.common.Language;

/**
 * @author a.mueller
 * @since 14.05.2008
 */
public class TermLanguageComparator<T extends TermBase> implements Comparator<T> {
	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(TermLanguageComparator.class);

	private final Language defaultLanguage;
	private final Language compareLanguage;

	public TermLanguageComparator(Language defaultLanguage, Language compareLanguage) {
		this.defaultLanguage = defaultLanguage;
		this.compareLanguage = compareLanguage;
	}

	@Override
    public int compare(T termbase1, T termbase2) throws RuntimeException{

	    if (termbase1.equals(termbase2)){
	        return 0;
	    }
		String label1 = makeCompareLabel(termbase1);
		String label2 = makeCompareLabel(termbase2);

		return label1.compareTo(label2);
	}

	private String makeCompareLabel(T termbase){
		String result;
		if (termbase == null){
			return "";
		}
		result = termbase.getLabel(compareLanguage);
		if (result == null){
			result = termbase.getLabel(defaultLanguage);
		}
		if (result == null){
			result = "";
		}
		result += termbase.getUuid().toString();
		return result;
	}

	/**
	 * @return the defaultLanguage
	 */
	public Language getDefaultLanguage() {
		return defaultLanguage;
	}

	/**
	 * @return the compareLanguage
	 */
	public Language getCompareLanguage() {
		return compareLanguage;
	}
}
