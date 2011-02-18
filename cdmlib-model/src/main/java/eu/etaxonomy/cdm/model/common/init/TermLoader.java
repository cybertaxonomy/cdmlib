/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.common.init;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.hibernate.validator.util.NewInstance;
import org.springframework.stereotype.Component;

import au.com.bytecode.opencsv.CSVReader;
import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.OrderedTermBase;
import eu.etaxonomy.cdm.model.common.OrderedTermVocabulary;
import eu.etaxonomy.cdm.model.common.TermVocabulary;
import eu.etaxonomy.cdm.model.common.VocabularyEnum;
import eu.etaxonomy.cdm.model.location.NamedArea;

@Component
public class TermLoader implements ITermLoader {
	private static final Logger logger = Logger.getLogger(TermLoader.class);

	private Map<Class<? extends DefinedTermBase>,String> termFileNames = new HashMap<Class<? extends DefinedTermBase>,String>();
	
	public TermLoader() {
		this.termFileNames.put(NamedArea.class, "TdwgArea.csv");
	}
	
	public void setTermFileNames(Map<Class<? extends DefinedTermBase>,String> termFileNames) {
		this.termFileNames = termFileNames;
	}
	
	public void unloadAllTerms(){
		for(VocabularyEnum vocabularyEnum : VocabularyEnum.values()) {
//			Class<? extends DefinedTermBase<?>> clazz = vocabularyEnum.getClazz();
			this.unloadVocabularyType(vocabularyEnum);
		}
	}

	private <T extends DefinedTermBase> void unloadVocabularyType(VocabularyEnum vocType){
		Class<? extends DefinedTermBase> termClass = vocType.getClazz();
		try {
			T termInstance = ((Class<T>)termClass).newInstance();
			termInstance.resetTerms();
			
		} catch (InstantiationException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		} 

	}

	
	
	public <T extends DefinedTermBase> TermVocabulary<T> loadTerms(VocabularyEnum vocType, Map<UUID,DefinedTermBase> terms) {
		
		String filename = vocType.name()+".csv";
//		termClass.getSimpleName()+".csv";
		
//		/**
//		 * Check to see if a non-standard filename should be used 
//		 * ( The file should still reside in the same directory )
//		 */ 
//		if(termFileNames.containsKey(termClass)) {
//			filename = termFileNames.get(termClass);
//		}
		Class<? extends DefinedTermBase> termClass = vocType.getClazz();
		
		String strResourceFileName = "terms" + CdmUtils.getFolderSeperator() + filename;
		logger.debug("strResourceFileName is " + strResourceFileName);
		
		try {
			CSVReader reader = new CSVReader(CdmUtils.getUtf8ResourceReader("terms" + CdmUtils.getFolderSeperator() + filename));
			
			//vocabulary
			TermVocabulary<T> voc = null;
			String labelAbbrev = null;
			
			if (OrderedTermBase.class.isAssignableFrom(termClass)){
				voc = OrderedTermVocabulary.NewInstance(termClass.getCanonicalName(), termClass.getSimpleName(), labelAbbrev, termClass.getCanonicalName());
			}else{
				voc = TermVocabulary.NewInstance(termClass.getCanonicalName(), vocType.name(), labelAbbrev, termClass.getCanonicalName());
			}
			
			String [] nextLine = reader.readNext();
			if (nextLine != null){
				voc.readCsvLine(arrayedLine(nextLine));
			}
			
			// Ugly, I know, but I don't think we can use a static method here . . 
			T termInstance = ((Class<T>)termClass).newInstance(); 
			
			while ((nextLine = reader.readNext()) != null) {
				// nextLine[] is an array of values from the line
				if (nextLine.length == 0){
					continue;
				}

				T term = (T) termInstance.readCsvLine(termClass,arrayedLine(nextLine), terms);
				terms.put(term.getUuid(), term);
				voc.addTerm(term);
			}
			return voc;
		} catch (Exception e) {
			logger.error(e + " " + e.getCause() + " " + e.getMessage());
			for(StackTraceElement ste : e.getStackTrace()) {
				logger.error(ste);
			}
			throw new RuntimeException(e);
		}
		
	}

	private List<String> arrayedLine(String [] nextLine){
		ArrayList<String> csvTermAttributeList = new ArrayList<String>(10);
		for (String col : nextLine){
			csvTermAttributeList.add(col);
		}
		while (csvTermAttributeList.size()<10){
			csvTermAttributeList.add("");
		}
		return csvTermAttributeList;
	}
}
