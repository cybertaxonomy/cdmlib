/**
 * 
 */
package eu.etaxonomy.cdm.database;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.TermVocabulary;
import eu.etaxonomy.cdm.model.common.init.IVocabularySaver;
import eu.etaxonomy.cdm.persistence.dao.common.ITermVocabularyDao;

/**
 * @author a.mueller
 *
 */
@Component
public class VocabularySaverImpl implements IVocabularySaver {
	private static Logger logger = Logger.getLogger(VocabularySaverImpl.class);

	@Autowired
	ITermVocabularyDao termVocabularyDao;
	
	/**
	 * 
	 */
	public VocabularySaverImpl() {
		super();
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.common.init.IVocabularySaver#saveOrUpdate(eu.etaxonomy.cdm.model.common.TermVocabulary)
	 */
	public void saveOrUpdate(TermVocabulary<DefinedTermBase> vocabulary) {
		termVocabularyDao.saveOrUpdate(vocabulary);
	}

}
