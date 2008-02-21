/**
 * 
 */
package eu.etaxonomy.cdm.database;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.UUID;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.NoDefinedTermClassException;
import eu.etaxonomy.cdm.model.common.TermVocabulary;
import eu.etaxonomy.cdm.model.common.init.IVocabularyStore;
import eu.etaxonomy.cdm.model.common.init.TermLoader;

/**
 * @author AM
 *
 */
@Component
public class CdmTermInitializer {
	private static final Logger logger = Logger.getLogger(CdmTermInitializer.class);
	
	@Autowired()
	TermLoader termLoader;
	
	
	//TODO make it interface
	@Autowired
	VocabularyStoreImpl saver;
	
	
	@PostConstruct
	public void initializeTerms(){
		logger.warn("CdmTermInitializer initializeTerms start ...");
		termLoader.setVocabularyStore(saver);
		if (! termLoader.basicTermsExist(saver)){
			try {
				termLoader.loadAllDefaultTerms();
			} catch (FileNotFoundException e) {
				logger.error(e.getMessage());
			} catch (NoDefinedTermClassException e) {
				logger.error(e.getMessage());
			}
		}
		logger.warn("CdmTermInitializer initializeTerms end ...");
	}

}
