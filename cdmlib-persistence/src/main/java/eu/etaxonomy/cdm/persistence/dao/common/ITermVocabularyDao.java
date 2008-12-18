/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.persistence.dao.common;


import java.util.List;

import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.TermVocabulary;


/**
 * @author a.mueller
 *
 */
public interface ITermVocabularyDao extends ICdmEntityDao<TermVocabulary<DefinedTermBase>> {
	
	/**
	 * Return a count of terms that belong to the termVocabulary supplied
	 * 
	 * @param termVocabulary The term vocabulary which 'owns' the terms of interest
	 * @return a count of terms
	 */
	public int countTerms(TermVocabulary termVocabulary);
	
	/**
	 * Return a List of terms that belong to the termVocabulary supplied
	 * 
	 * @param termVocabulary The term vocabulary which 'owns' the terms of interest
	 * @param pageSize The maximum number of terms returned (can be null for all terms)
	 * @param pageNumber The offset (in pageSize chunks) from the start of the result set (0 - based)
	 * @return a List of terms
	 */
	public <T extends DefinedTermBase> List<T> getTerms(TermVocabulary<T> termVocabulary, Integer pageSize, Integer pageNumber);
}
