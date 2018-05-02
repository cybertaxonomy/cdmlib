/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.common;

import java.util.Comparator;

import org.apache.log4j.Logger;


/**
 * @author a.mueller
 * @since 14.05.2008
 * @version 1.0
 */
public class TermLanguageComparator<T extends TermBase> implements Comparator<T> {
	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(TermLanguageComparator.class);

	private Language defaultLanguage = Language.DEFAULT();
	private Language compareLanguage = Language.DEFAULT();



	/**
	 *
	 */
	public TermLanguageComparator() {
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
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
	 * @param defaultLanguage the defaultLanguage to set
	 */
	public void setDefaultLanguage(Language defaultLanguage) {
		this.defaultLanguage = defaultLanguage;
	}

	/**
	 * @return the compareLanguage
	 */
	public Language getCompareLanguage() {
		return compareLanguage;
	}

	/**
	 * @param compareLanguage the compareLanguage to set
	 */
	public void setCompareLanguage(Language compareLanguage) {
		this.compareLanguage = compareLanguage;
	}
}
