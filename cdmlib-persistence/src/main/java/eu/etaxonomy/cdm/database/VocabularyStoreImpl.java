/**
 * 
 */
package eu.etaxonomy.cdm.database;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.TermVocabulary;
import eu.etaxonomy.cdm.model.common.init.IVocabularyStore;
import eu.etaxonomy.cdm.model.common.init.TermLoader;
import eu.etaxonomy.cdm.persistence.dao.common.IDefinedTermDao;
import eu.etaxonomy.cdm.persistence.dao.common.ITermVocabularyDao;

/**
 * @author a.mueller
 *
 */
@Component
public class VocabularyStoreImpl implements IVocabularyStore {
	private static Logger logger = Logger.getLogger(VocabularyStoreImpl.class);

	private boolean initialized = false;
	
	private static final UUID uuidEnglish = UUID.fromString("e9f8cdb7-6819-44e8-95d3-e2d0690c3523");

	public static final Language DEFAULT_LANGUAGE(){
		return new Language(uuidEnglish);
	}
	
	static protected Map<UUID, DefinedTermBase> definedTermsMap = null;

	
	@Autowired
	public ITermVocabularyDao vocabularyDao;
	
	@Autowired
	public IDefinedTermDao termDao;
	
	/**
	 * 
	 */
	public VocabularyStoreImpl() {
		super();
	}


	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.common.init.IVocabularySaver#saveOrUpdate(eu.etaxonomy.cdm.model.common.TermVocabulary)
	 */
	public void saveOrUpdate(TermVocabulary<DefinedTermBase> vocabulary) {
		initialize();
		Iterator<DefinedTermBase> termIterator = vocabulary.iterator();
		while (termIterator.hasNext()){
			DefinedTermBase<DefinedTermBase> term = termIterator.next();
			if (definedTermsMap.get(term.getUuid()) != null){
				term.setId(definedTermsMap.get(term.getUuid()).getId()); // to avoid duplicates in the default Language
			}
			definedTermsMap.put(term.getUuid(), term);
		}
		vocabularyDao.saveOrUpdate(vocabulary);
	}

	public DefinedTermBase getTermByUuid(UUID uuid) {
		initialize();
		if (definedTermsMap.get(uuid) != null){
			return definedTermsMap.get(uuid);
		}else{
			return termDao.findByUuid(uuid);
		}
	}
	
	public TermVocabulary<DefinedTermBase> getVocabularyByUuid(UUID uuid){
		initialize();
		return vocabularyDao.findByUuid(uuid);
	}
	
	public boolean initialize(){
		return loadBasicTerms();
	}	
	
	public boolean loadBasicTerms(){
		if (! initialized){
			logger.info("inititialize start ...");
			try {
				Language defaultLanguage = (Language)termDao.findByUuid(DEFAULT_LANGUAGE().getUuid());
				if (defaultLanguage == null){
					termDao.saveOrUpdate(DEFAULT_LANGUAGE());
					definedTermsMap = new HashMap<UUID, DefinedTermBase>();
					definedTermsMap.put(DEFAULT_LANGUAGE().getUuid(), DEFAULT_LANGUAGE());
					initialized = true;
					TermLoader termLoader = new TermLoader(this);
					termLoader.loadAllDefaultTerms();
				}
			} catch (Exception e) {
				logger.error("Error ocurred when initializing and loading terms");
				initialized = false;
				return false;
			}
			initialized = true;
			logger.info("initTermsMap end ...");
		}
		return true;
	}

}
