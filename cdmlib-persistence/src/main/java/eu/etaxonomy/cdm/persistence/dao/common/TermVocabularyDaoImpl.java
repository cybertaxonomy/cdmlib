/**
 * 
 */
package eu.etaxonomy.cdm.persistence.dao.common;

import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.TermVocabulary;
import eu.etaxonomy.cdm.model.common.init.IVocabularySaver;

/**
 * @author a.mueller
 *
 */
public class TermVocabularyDaoImpl extends CdmEntityDaoBase implements
		ITermVocabularyDao {

	/**
	 * @param type
	 */
	public TermVocabularyDaoImpl(Class type) {
		super(type);
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.common.init.IVocabularySaver#saveOrUpdate(eu.etaxonomy.cdm.model.common.TermVocabulary)
	 */
	public void saveOrUpdate(TermVocabulary<DefinedTermBase> vocabulary) {
		super.saveOrUpdate(vocabulary);
	}

}
