package eu.etaxonomy.cdm.model.common.init;


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
public interface ITermInitializer {
	
	/** Initializes the IVocabularStore by making sure that all basic terms that are
	 * loaded by the TermLoader are available afterwards.
	 * ATTENTION: Be aware that TermLoader indirectly calls getTermByUuid(uuid)
	 * for the default language. So make sure that before loading the Terms by the
	 * TermLoader getTermByUuid() returns a valid Object without going to endless recursion.
	 * @return true if terms where successfully loaded
	 */
	public void initialize();

}
