/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.common.init;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import au.com.bytecode.opencsv.CSVReader;
import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.OrderedTermBase;
import eu.etaxonomy.cdm.model.common.OrderedTermVocabulary;
import eu.etaxonomy.cdm.model.common.TermType;
import eu.etaxonomy.cdm.model.common.TermVocabulary;
import eu.etaxonomy.cdm.model.common.VocabularyEnum;

@Component
public class TermLoader implements ITermLoader {
	private static final Logger logger = Logger.getLogger(TermLoader.class);

	@Override
	public void unloadAllTerms(){
		for(VocabularyEnum vocabularyEnum : VocabularyEnum.values()) {
//			Class<? extends DefinedTermBase<?>> clazz = vocabularyEnum.getClazz();
			this.unloadVocabularyType(vocabularyEnum);
		}
	}

	private <T extends DefinedTermBase> void unloadVocabularyType(VocabularyEnum vocType){
		Class<? extends DefinedTermBase> termClass = vocType.getClazz();
		getInstance(termClass).resetTerms();
		return;
	}

	@Override
	public UUID loadUuids(VocabularyEnum vocType, Map<UUID, Set<UUID>> uuidMap) {

		try {
			CSVReader reader = getCsvReader(vocType);
			String[] nextLine = reader.readNext();
			UUID uuidVocabulary = UUID.fromString(nextLine[0]);
			Set<UUID> termSet = new HashSet<UUID>();
			uuidMap.put(uuidVocabulary, termSet);

			while ( (nextLine = reader.readNext()) != null) {
				UUID uuidTerm = UUID.fromString(nextLine[0]);
				termSet.add(uuidTerm);
			}
			return uuidVocabulary;


		} catch (Exception e) {
			logger.error(e + " " + e.getCause() + " " + e.getMessage());
			for(StackTraceElement ste : e.getStackTrace()) {
				logger.error(ste);
			}
			throw new RuntimeException(e);
		}

	}

	@Override
	public <T extends DefinedTermBase> TermVocabulary<T> loadTerms(VocabularyEnum vocType, Map<UUID,DefinedTermBase> terms) {


		try {
			CSVReader reader = getCsvReader(vocType);

			String [] nextLine = reader.readNext();


			Class<? extends DefinedTermBase> termClass = vocType.getClazz();

			//vocabulary
			TermVocabulary<T> voc;
			TermType termType = TermType.Unknown;
			if (OrderedTermBase.class.isAssignableFrom(termClass)){
				voc = OrderedTermVocabulary.NewInstance(termType);
			}else{
				voc = TermVocabulary.NewInstance(termType);
			}

			if (nextLine != null){
				voc.readCsvLine(arrayedLine(nextLine));
			}
			termType = voc.getTermType();
			boolean abbrevAsId = (arrayedLine(nextLine).get(5).equals("1"));

			// Ugly, I know, but I don't think we can use a static method here . .

			T classDefiningTermInstance = getInstance(termClass);// ((Class<T>)termClass).newInstance();

			while ((nextLine = reader.readNext()) != null) {
				// nextLine[] is an array of values from the line
				if (nextLine.length == 0){
					continue;
				}

				handleSingleTerm(nextLine, terms, termClass, voc,
						abbrevAsId, classDefiningTermInstance);

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

	/**
	 * Handles a single csv line, creates the term and adds it to the vocabulary and to the terms map.
	 * @param csvLine csv line
	 * @param terms UUID-Term map this term should be added to
	 * @param termClass the class of the term to create
	 * @param voc the vocabulary the term should be added to
	 * @param abbrevAsId boolean value, if true the abbreviation should be taken as idInVocabulary
	 * @param classDefiningTermInstance instance for calling readCsvLine
	 * @return
	 */
	private <T extends DefinedTermBase> T handleSingleTerm(String[] csvLine, Map<UUID,DefinedTermBase> terms,
			Class<? extends DefinedTermBase> termClass,
			TermVocabulary<T> voc, boolean abbrevAsId,
			T classDefiningTermInstance) {
		T term = (T) classDefiningTermInstance.readCsvLine(termClass,arrayedLine(csvLine), terms, abbrevAsId);
		term.setTermType(voc.getTermType());
		voc.addTerm(term);
		terms.put(term.getUuid(), term);
		return term;
	}


	@Override
	public <T extends DefinedTermBase> Set<T> loadSingleTerms(VocabularyEnum vocType,
			TermVocabulary<T> voc, Set<UUID> missingTerms) {
		try {
			Class<? extends DefinedTermBase> termClass = vocType.getClazz();

			CSVReader reader = getCsvReader(vocType);
			String [] nextLine =  reader.readNext();

			if (! UUID.fromString(nextLine[0]).equals(voc.getUuid())){
				throw new IllegalStateException("Vocabularies in csv file and vocabulary must be equal");
			}



			boolean abbrevAsId = (arrayedLine(nextLine).get(5).equals("1"));
			T classDefiningTermInstance = getInstance(termClass);// ((Class<T>)termClass).newInstance();
			Map<UUID,DefinedTermBase> allVocTerms = new HashMap<UUID, DefinedTermBase>();
			for (T term:voc.getTerms()){
				allVocTerms.put(term.getUuid(), term);
			}

			while ((nextLine = reader.readNext()) != null) {
				if (nextLine.length == 0){
					continue;
				}
				UUID uuid = UUID.fromString(nextLine[0]);
				if (missingTerms.contains(uuid)){
					handleSingleTerm(nextLine, allVocTerms, termClass, voc, abbrevAsId, classDefiningTermInstance);
				}
			}

			return null;
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	/**
	 * Returns the {@link CSVReader} for the given {@link VocabularyEnum}.
	 * @param vocType
	 * @return
	 * @throws IOException
	 */
	private CSVReader getCsvReader(VocabularyEnum vocType) throws IOException {
		String filename = vocType.name()+".csv";
		String strResourceFileName = "terms" + CdmUtils.getFolderSeperator() + filename;
		if (logger.isDebugEnabled()){logger.debug("strResourceFileName is " + strResourceFileName);}
		CSVReader reader = new CSVReader(CdmUtils.getUtf8ResourceReader(strResourceFileName));
		return reader;
	}

	/**
	 * Returns a new instance for the given class by using the default constructor.
	 * The constructor must be declared but can be unaccessible (e.g. private)
	 * @param termClass
	 * @return
	 */
	private  <T extends DefinedTermBase> T getInstance(Class<? extends DefinedTermBase> termClass) {
		try {
			Constructor<T> c = ((Class<T>)termClass).getDeclaredConstructor();
			c.setAccessible(true);
			T termInstance = c.newInstance();
			return termInstance;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private List<String> arrayedLine(String [] nextLine){
		ArrayList<String> csvTermAttributeList = new ArrayList<String>(15);
		for (String col : nextLine){
			csvTermAttributeList.add(col);
		}
		while (csvTermAttributeList.size()<15){
			csvTermAttributeList.add("");
		}
		return csvTermAttributeList;
	}

}
