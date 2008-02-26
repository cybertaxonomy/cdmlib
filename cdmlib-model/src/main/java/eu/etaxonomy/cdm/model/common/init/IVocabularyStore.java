package eu.etaxonomy.cdm.model.common.init;

import java.util.UUID;

import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.TermVocabulary;

/**
 *  Terms, vocabularies, and representations loaded during termloading by class TermLoader added 
 *  by the user maybe stored and accessed through this interface.
 *  Care must be taken as TermLoader indirectly calls getTermByUuid(uuid) for the default 
 *  language uuid during the loading process. So make sure that initialize() and getTermByUuid(uuid) 
 *  are not implemeted in an endless recursion!
 * 
 * @author a.mueller
 *
 */
public interface IVocabularyStore {
	
	/**
	 * TODO
	 * @param vocabulary
	 */
	public void saveOrUpdate(TermVocabulary<DefinedTermBase> vocabulary);
	
	 /** ATTENTION: Be aware that TermLoader indirectly calls getTermByUuid(uuid)
	 * for the default language. So make sure that before loading the Terms by the
	 * TermLoader getTermByUuid() returns a valid Object without going to endless recursion.
	 * @param uuid the definedTermBases UUID
	 * @return the DefinedTermBase to return
	 */
	public DefinedTermBase getTermByUuid(UUID uuid);
	
	/**
	 * @param uuid
	 * @return
	 */
	public TermVocabulary<DefinedTermBase> getVocabularyByUuid(UUID uuid);
	
	/** Initializes the IVocabularStore by making sure that all basic terms that are
	 * loaded by the TermLoader are available afterwards.
	 * ATTENTION: Be aware that TermLoader indirectly calls getTermByUuid(uuid)
	 * for the default language. So make sure that before loading the Terms by the
	 * TermLoader getTermByUuid() returns a valid Object without going to endless recursion.
	 * @return true if terms where successfully loaded
	 */
	public boolean loadBasicTerms();

}
