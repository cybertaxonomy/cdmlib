/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.database;

import java.io.FileNotFoundException;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.model.common.NoDefinedTermClassException;
import eu.etaxonomy.cdm.model.common.init.IVocabularyStore;
import eu.etaxonomy.cdm.model.common.init.TermLoader;

/**
 * Spring bean class to initialize the {@link IVocabularyStore IVocabularyStore}.
 * To initialize the store the {@link TermLoader TermLoader} and the {@link IVocabularyStore IVocabularyStore}
 * are injected via spring and the initializeTerms method is called as an init-method (@PostConstruct). 
 * @author a.mueller
 */
@Component
public class CdmTermInitializer {
	private static final Logger logger = Logger.getLogger(CdmTermInitializer.class);
	
    //TODO: This is a workaround to omit term loading for JAXB serializing/deserializing.
	public static boolean omit = false;
	
	@Autowired
	TermLoader termLoader;
	
	@Autowired
	VocabularyStoreImpl vocabularyStore;
	
	
	@PostConstruct
	public void initializeTerms(){
		if (omit == true){
			return;
		}
		try {
			logger.info("CdmTermInitializer initializeTerms start ...");
			termLoader.setVocabularyStore(vocabularyStore);
			//DefinedTermBase.setVocabularyStore(vocabularyStore);
			//vocabularyStore.initialize();
			//if (! termLoader.basicTermsExist(vocabularyStore)){
				try {
					termLoader.makeDefaultTermsInserted(vocabularyStore);
				} catch (FileNotFoundException e) {
					logger.error(e.getMessage());
				} catch (NoDefinedTermClassException e) {
					logger.error(e.getMessage());
				}
			//}
			logger.info("CdmTermInitializer initializeTerms end ...");
		} catch (RuntimeException e) {
			logger.error("RuntimeException when initializing Terms");
			e.printStackTrace();
			throw e;
		}
	}
}
