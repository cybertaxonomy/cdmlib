/**
 * 
 */
package eu.etaxonomy.cdm.database;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.ILoadableTerm;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.NoDefinedTermClassException;
import eu.etaxonomy.cdm.model.common.TermVocabulary;
import eu.etaxonomy.cdm.model.common.init.IVocabularyStore;
import eu.etaxonomy.cdm.model.common.init.TermLoader;
import eu.etaxonomy.cdm.persistence.dao.common.IDefinedTermDao;
import eu.etaxonomy.cdm.persistence.dao.common.ITermVocabularyDao;

/**
 * @author a.mueller
 *
 */
/**
 * @author AM
 *
 */
@Component
public class VocabularyStoreImpl implements IVocabularyStore {
	private static Logger logger = Logger.getLogger(VocabularyStoreImpl.class);

	private boolean initialized = false;
	private static final UUID uuidEnglish = UUID.fromString("e9f8cdb7-6819-44e8-95d3-e2d0690c3523");

	private static Language DEFAULT_LANGUAGE = null;
	private static void makeDefaultLanguage() {
		logger.info("make Default language ...");
		DEFAULT_LANGUAGE = Language.NewInstance(uuidEnglish);
	}
	
	static protected Map<UUID, ILoadableTerm> definedTermsMap = null;

	
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
		logger.info("vocabulary save or update start ...");
		initialize();
		Iterator<DefinedTermBase> termIterator = vocabulary.iterator();
		while (termIterator.hasNext()){
			logger.info("iterate ...");
			DefinedTermBase<DefinedTermBase> term = termIterator.next();
			if (definedTermsMap.get(term.getUuid()) != null){
				term.setId(definedTermsMap.get(term.getUuid()).getId()); // to avoid duplicates in the default Language
			}
			definedTermsMap.put(term.getUuid(), term);
		}
		logger.info("vocabulary save or update before dao save ...");
		vocabularyDao.saveOrUpdate(vocabulary);
		logger.info("vocabulary save or update end.");
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.common.init.IVocabularyStore#saveOrUpdate(eu.etaxonomy.cdm.model.common.IDefTerm)
	 */
	public void saveOrUpdate(ILoadableTerm term) {
		initialize();
		if (definedTermsMap.get(term.getUuid()) != null){
			ILoadableTerm oldTerm = definedTermsMap.get(term.getUuid());
			term.setId(oldTerm.getId()); // to avoid duplicates in the default Language
			
		}
		definedTermsMap.put(term.getUuid(), term);
//		vocabularyDao.saveOrUpdate(term);
	}

	public DefinedTermBase<DefinedTermBase> getTermByUuid(UUID uuid) {
		if (initialize()){
			if (definedTermsMap.get(uuid) != null){
				return (DefinedTermBase<DefinedTermBase>)definedTermsMap.get(uuid);
			}else{
				DefinedTermBase term = termDao.findByUuid(uuid);
				if (term != null){
					definedTermsMap.put(term.getUuid(), term);
				}
				return term;
			}
		}else{
			logger.error("Vocabulary Store could not be initialized");
			throw new RuntimeException("Vocabulary Store could not be initialized");
		}
	}
	
	public TermVocabulary<DefinedTermBase> getVocabularyByUuid(UUID uuid){
		if (! initialize()){
			return null;
		}else{
			return vocabularyDao.findByUuid(uuid);
		}
	}

//	private TermLoader termLoader;  //doesn't work yet in service layer for some unclear resason
//	@PostConstruct
//	private void init(){
//		try {
//			logger.info("init ...");
//			DefinedTermBase.setVocabularyStore(this);
//			termLoader = new TermLoader(this);
//			if (! termLoader.basicTermsExist(this)){
//				try {
//					termLoader.loadAllDefaultTerms();
//				} catch (FileNotFoundException e) {
//					logger.error(e.getMessage());
//				} catch (NoDefinedTermClassException e) {
//					logger.error(e.getMessage());
//				}
//			}
//		} catch (RuntimeException e) {
//			logger.error("RuntimeException when initializing Terms");
//			e.printStackTrace();
//			throw e;
//		}
//	}
	

	public boolean initialize(){
		if (! initialized){
			logger.info("inititialize VocabularyStoreImpl ...");
			try {
				logger.info("000 ...");
				DefinedTermBase.setVocabularyStore(this);
				makeDefaultLanguage();
				logger.info("111 ...");
				Language defaultLanguage = (Language)termDao.findByUuid(DEFAULT_LANGUAGE.getUuid());
				logger.info("222 ...");
				if (defaultLanguage == null){
					termDao.saveOrUpdate(DEFAULT_LANGUAGE);
					definedTermsMap = new HashMap<UUID, ILoadableTerm>();
					definedTermsMap.put(DEFAULT_LANGUAGE.getUuid(), DEFAULT_LANGUAGE);
					logger.info("ccc ...");
					initialized = true;
					TermLoader termLoader = new TermLoader(this);
					//termLoader.setVocabularyStore(this);
					termLoader.makeDefaultTermsLoaded(this);
				}else if (definedTermsMap == null){
					definedTermsMap = new HashMap<UUID, ILoadableTerm>();
						definedTermsMap.put(defaultLanguage.getUuid(), defaultLanguage);
				}
			} catch (Exception e) {
				logger.error("loadBasicTerms: Error ocurred when initializing and loading terms");
				initialized = false;
				return false;
			}
			initialized = true;
			logger.info("inititialize VocabularyStoreImpl end ...");
		}
		return true;
	}

}
