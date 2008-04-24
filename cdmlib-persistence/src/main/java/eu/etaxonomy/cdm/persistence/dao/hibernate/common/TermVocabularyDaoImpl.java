/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.persistence.dao.hibernate.common;

import org.springframework.stereotype.Repository;

import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.TermVocabulary;
import eu.etaxonomy.cdm.persistence.dao.common.ITermVocabularyDao;

/**
 * @author a.mueller
 *
 */
@Repository
public class TermVocabularyDaoImpl extends CdmEntityDaoBase<TermVocabulary<DefinedTermBase>> implements
		ITermVocabularyDao {

	
	/**
	 * @param type
	 */
	public TermVocabularyDaoImpl() {
		super((Class)TermVocabulary.class);
	}

}
