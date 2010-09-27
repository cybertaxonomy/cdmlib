/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.common;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.model.common.init.ITermInitializer;
import eu.etaxonomy.cdm.model.common.init.ITermLoader;
import eu.etaxonomy.cdm.model.common.init.TermLoader;

/**
 * @author a.mueller
 *
 */
public class DefaultTermInitializer implements ITermInitializer {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(DefaultTermInitializer.class);
	protected ITermLoader termLoader = new TermLoader();
	
	public final void initialize() {
		termLoader.unloadAllTerms();
		doInitialize();
	}
	
	protected void doInitialize(){
		Map<UUID,DefinedTermBase> terms = new HashMap<UUID,DefinedTermBase>();
		
//		for(Class<? extends DefinedTermBase<?>> clazz : classesToInitialize) {
		for(VocabularyEnum vocabularyEnum : VocabularyEnum.values()) {
//			Class<? extends DefinedTermBase<?>> clazz = vocabularyEnum.getClazz();
			TermVocabulary<?> voc  = termLoader.loadTerms(vocabularyEnum, terms);
			setDefinedTerms(vocabularyEnum.getClazz(),voc);
		}				
	}
	
	protected void setDefinedTerms(Class<? extends DefinedTermBase<?>> clazz, TermVocabulary<?> vocabulary) {
		DefinedTermBase newInstance;
		try {
			newInstance = clazz.newInstance();
			newInstance.setDefaultTerms(vocabulary);
		} catch (InstantiationException e) {
			// TODO Exception type
			throw new RuntimeException("NewInstance could not be initialized in term initializer", e);
		} catch (IllegalAccessException e) {
			// TODO Exception type
			throw new RuntimeException("NewInstance could not be accessed in term initializer", e);
		}
		
	}


}
