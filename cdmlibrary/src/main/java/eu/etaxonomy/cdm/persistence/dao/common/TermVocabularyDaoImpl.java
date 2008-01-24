/**
 * 
 */
package eu.etaxonomy.cdm.persistence.dao.common;

import org.springframework.stereotype.Repository;

import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.TermVocabulary;

/**
 * @author a.mueller
 *
 */
@Repository
public class TermVocabularyDaoImpl extends CdmEntityDaoBase implements
		ITermVocabularyDao {

	/**
	 * @param type
	 */
	public TermVocabularyDaoImpl() {
		super(TermVocabulary.class);
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.common.init.IVocabularySaver#saveOrUpdate(eu.etaxonomy.cdm.model.common.TermVocabulary)
	 */
	public void saveOrUpdate(TermVocabulary<DefinedTermBase> vocabulary) {
		super.saveOrUpdate(vocabulary);
	}

}
