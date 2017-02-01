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

import eu.etaxonomy.cdm.model.common.OrderedTermBase;
import eu.etaxonomy.cdm.model.common.OrderedTermVocabulary;
import eu.etaxonomy.cdm.persistence.dao.common.IOrderedTermVocabularyDao;

/**
 * @author a.mueller
 * TODO this dao is maybe only a work around for an ClassCastException error thrown when
 * OrderedTermVocabulary<Rank> rankVocabulary = (OrderedTermVocabulary)orderedVocabularyDao.findByUuid(rankUuid);
 * is called in NameServiceImpl.getRankVocabulary().
 * If data exist in the database, it returns a TermVocabulary$CGLIB class created by hibernate
 */
@Repository
public class OrderedTermVocabularyDaoImpl extends CdmEntityDaoBase<OrderedTermVocabulary<OrderedTermBase<?>>> implements
		IOrderedTermVocabularyDao {


	/**
	 * @param type
	 */
	public OrderedTermVocabularyDaoImpl() {
		super((Class)OrderedTermVocabulary.class);
	}


}
