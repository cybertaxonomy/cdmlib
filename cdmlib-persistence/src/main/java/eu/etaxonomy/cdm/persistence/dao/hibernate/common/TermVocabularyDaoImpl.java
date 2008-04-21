/**
 * 
 */
package eu.etaxonomy.cdm.persistence.dao.hibernate.common;

import java.util.UUID;

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
