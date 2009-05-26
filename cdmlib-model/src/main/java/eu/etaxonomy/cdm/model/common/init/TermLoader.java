/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.common.init;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import au.com.bytecode.opencsv.CSVReader;
import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.IDefinedTerm;
import eu.etaxonomy.cdm.model.common.OrderedTermBase;
import eu.etaxonomy.cdm.model.common.OrderedTermVocabulary;
import eu.etaxonomy.cdm.model.common.TermVocabulary;
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
	
	public <T extends DefinedTermBase> TermVocabulary<T> loadTerms(Class<T> termClass, Map<UUID,DefinedTermBase> terms) {
		
		String filename = termClass.getSimpleName()+".csv";
		
		/**
		 * Check to see if a non-standard filename should be used 
		 * ( The file should still reside in the same directory )
		 */ 
		if(termFileNames.containsKey(termClass)) {
			filename = termFileNames.get(termClass);
		}
		
		String strResourceFileName = "terms" + CdmUtils.getFolderSeperator() + filename;
		logger.debug("strResourceFileName is " + strResourceFileName);
		
		try {
			//Could we use resources?
			//InputStream inputStream = CdmUtils.getReadableResourceStream("terms" + CdmUtils.getFolderSeperator() + filename);
			//if (inputStream == null) {logger.info("inputStream is null");}
			//CSVReader reader = new CSVReader(new InputStreamReader(inputStream, ""));
			CSVReader reader = new CSVReader(CdmUtils.getUtf8ResourceReader("terms" + CdmUtils.getFolderSeperator() + filename));
			
			//vocabulary
			TermVocabulary<T> voc = null;
			String labelAbbrev = null;
			
			if (OrderedTermBase.class.isAssignableFrom(termClass)){
				voc = new OrderedTermVocabulary(termClass.getCanonicalName(), termClass.getSimpleName(), labelAbbrev, termClass.getCanonicalName());
			}else{
				voc = new TermVocabulary<T>(termClass.getCanonicalName(), termClass.getSimpleName(), labelAbbrev, termClass.getCanonicalName());
			}
			
			String [] nextLine = reader.readNext();
			if (nextLine != null){
				voc.readCsvLine(arrayedLine(nextLine));
			}
			
			// Ugly, I know, but I don't think we can use a static method here . . 
			T termInstance = termClass.newInstance(); 
			
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
