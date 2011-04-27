/**
 * 
 */
package eu.etaxonomy.cdm.remote.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.api.service.IVocabularyService;
import eu.etaxonomy.cdm.model.common.DefaultTermInitializer;
import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.Representation;
import eu.etaxonomy.cdm.model.common.TermVocabulary;
import eu.etaxonomy.cdm.model.common.VocabularyEnum;

/**
 * @author j.koch
 *
 */
@Component
public class RemoteTermInitializer extends DefaultTermInitializer {
	private static final Logger logger = Logger.getLogger(RemoteTermInitializer.class);
	
	@Autowired
	private IVocabularyService vocabularyService;
	
	@PostConstruct
	@Override
	public void initialize() {
		super.initialize();
	}
	
	@Override
	protected void doInitialize() {
		logger.info("RemoteTermInitializer initialize start ...");
		Map<UUID,DefinedTermBase> terms = new HashMap<UUID,DefinedTermBase>();
		logger.info("RemoteTermInitializer == false, initializing " + VocabularyEnum.values().length + " term classes");
		
		for(VocabularyEnum vocabularyType : VocabularyEnum.values()) {
			if(!vocabularyType.equals(VocabularyEnum.TdwgArea))
					secondPass(vocabularyType.getClazz(),vocabularyType.getUuid(),terms);
		}
		logger.info("RemoteTermInitializer initialize end ...");
	}
	
	/**
	 * Initializes the static fields of the <code>TermVocabulary</code> classes.
	 * 
	 * @param clazz the <code>Class</code> of the vocabulary
	 * @param vocabularyUuid the <code>UUID</code> of the vocabulary
	 * @param terms a <code>Map</code> containing all already 
	 * 						 loaded terms with their <code>UUID</code> as key
	 */
	protected void secondPass(Class clazz, UUID vocabularyUuid, Map<UUID,DefinedTermBase> terms) {
		logger.info("Initializing vocabulary for class " + clazz.getSimpleName() + " with uuid " + vocabularyUuid );
		
		List<String> propertyPaths = Arrays.asList(new String []{
			"terms.representations.language"
		});
		
		TermVocabulary persistedVocabulary = vocabularyService.load(vocabularyUuid, propertyPaths);
		
		if (persistedVocabulary != null){
			for(Object object : persistedVocabulary.getTerms()) {
				DefinedTermBase definedTermBase = (DefinedTermBase) object;
				terms.put(definedTermBase.getUuid(), definedTermBase);			
			}
		}else{
			logger.error("Persisted Vocabulary does not exist in database: " + vocabularyUuid);
			throw new NullPointerException("Persisted Vocabulary does not exist in database: " + vocabularyUuid);
		}
		logger.info("Setting defined Terms for class " + clazz.getSimpleName());
		super.setDefinedTerms(clazz, persistedVocabulary);
	}

	public void setVocabularyService(IVocabularyService vocabularyService) {
		this.vocabularyService = vocabularyService;
	}

	public IVocabularyService getVocabularyService() {
		return vocabularyService;
	}
}
