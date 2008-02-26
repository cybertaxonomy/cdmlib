/**
 * 
 */
package eu.etaxonomy.cdm.model.common.init;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.TermVocabulary;

/**
 * @author AM
 *
 */
public class DefaultVocabularyStore implements IVocabularyStore {
	static Logger logger = Logger.getLogger(DefaultVocabularyStore.class);
	
	private static boolean isInitialized = false;

	public static final Language DEFAULT_LANGUAGE() {
		return new Language(Language.uuidEnglish);
	}
	
	
	static protected Map<UUID, DefinedTermBase> definedTermsMap = null;
	static protected Map<UUID, TermVocabulary<DefinedTermBase>> termVocabularyMap = null;


	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.common.init.IVocabularyStore#getTermByUuid(java.util.UUID)
	 */
	public DefinedTermBase getTermByUuid(UUID uuid) {
		if (!isInitialized  &&  ! loadBasicTerms()){ return null;}
		return definedTermsMap.get(uuid);
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.common.init.IVocabularyStore#getVocabularyByUuid(java.util.UUID)
	 */
	public TermVocabulary<DefinedTermBase> getVocabularyByUuid(UUID uuid) {
		if (!isInitialized  &&  ! loadBasicTerms()){ return null;}
		return termVocabularyMap.get(uuid);
	}
	
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.common.init.IVocabularyStore#saveOrUpdate(eu.etaxonomy.cdm.model.common.TermVocabulary)
	 */
	public void saveOrUpdate(TermVocabulary<DefinedTermBase> vocabulary) {
		loadBasicTerms();
		Iterator<DefinedTermBase> termIterator = vocabulary.iterator();

		while (termIterator.hasNext()){
			DefinedTermBase<DefinedTermBase> term = termIterator.next();
			if (definedTermsMap.get(term.getUuid()) != null){
				term.setId(definedTermsMap.get(term.getUuid()).getId()); // to avoid duplicates in the default Language
			}
			definedTermsMap.put(term.getUuid(), term);
		}
		termVocabularyMap.put(vocabulary.getUuid(), vocabulary);
	}
	
	
	public boolean loadBasicTerms() {
		if (definedTermsMap == null){
			logger.info("initTermsMap start ...");
			definedTermsMap = new HashMap<UUID, DefinedTermBase>();
			try {
				definedTermsMap.put(DEFAULT_LANGUAGE().getUuid(), DEFAULT_LANGUAGE());
				TermLoader termLoader = new TermLoader(this);
				termLoader.loadAllDefaultTerms();
			} catch (Exception e) {
				logger.error("Error ocurred when loading terms");
				return false;
			}				
			logger.debug("initTermsMap end ...");
		}
		if (termVocabularyMap == null){
			logger.info("initVocabularyMap start ...");
			termVocabularyMap = new HashMap<UUID, TermVocabulary<DefinedTermBase>>();
			logger.debug("initVocabularyMap end ...");
		}
		isInitialized =true;
		return true;
	}

//public void initTermList(ITermLister termLister){
//	logger.warn("initTermList");
//	if (definedTermsMap == null){
//		definedTermsMap = new HashMap<UUID, DefinedTermBase>();
//		try {
//			Language defaultLanguage = new Language();//DEFAULT_LANGUAGE();
//			UUID uuid = defaultLanguage.getUuid();
//			definedTermsMap.put(uuid, defaultLanguage);
//			TermLoader termLoader = new TermLoader(this);
//			termLoader.loadAllDefaultTerms();
//		} catch (Exception e) {
//			logger.error("Error ocurred when loading terms");
//		}				
//			
////		}else{
////			List<DefinedTermBase> list = termLister.listTerms();
////			definedTermsMap = new HashMap<UUID, DefinedTermBase>();
////			for (DefinedTermBase dtb: list){
////				definedTermsMap.put(dtb.getUuid(), dtb);
////			}
//		}
//	}
//	logger.debug("initTermList - end");
//}

}
