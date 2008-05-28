/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.remote.dto;

/**
 * Transfer object derived from {@link LanguageString} excluding some {@link CdmBase} fields
 * 
 * Term descriptions are to be delivered by a separate web service request.
 * The UUID is unique for all representations of this term, 
 * that is it does not depend on the {@link LocalisedTermSTO#language}.
 * 
 * @author a.kohlbecker
 * @author m.doering
 * @version 1.0
 * @created 11.12.2007 12:10:45
 *
 */
public class LocalisedTermSTO extends BaseSTO{

	/**
	 * Representation of this term in the specific language as specified by {@link LocalisedTermSTO#language}
	 */
	private String term;
	/**
	 * The language of this term representation
	 */
	private String language;
	
	public String getLanguage() {
		return language;
	}
	public void setLanguage(String language) {
		this.language = language;
	}
	public String getTerm() {
		return term;
	}
	public void setTerm(String term) {
		this.term = term;
	}
	
}
