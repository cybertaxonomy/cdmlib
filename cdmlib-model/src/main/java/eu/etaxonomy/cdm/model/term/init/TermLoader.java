/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.term.init;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import au.com.bytecode.opencsv.CSVReader;
import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.term.DefinedTermBase;
import eu.etaxonomy.cdm.model.term.OrderedTermBase;
import eu.etaxonomy.cdm.model.term.OrderedTermVocabulary;
import eu.etaxonomy.cdm.model.term.TermType;
import eu.etaxonomy.cdm.model.term.TermVocabulary;
import eu.etaxonomy.cdm.model.term.VocabularyEnum;

@Component
public class TermLoader implements ITermLoader {

    private static final Logger logger = LogManager.getLogger();

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
	public UUID loadUuids(VocabularyEnum vocType, Map<UUID, List<UUID>> uuidMap) {

		try {
			CSVReader reader = getCsvReader(vocType);
			String[] nextLine = reader.readNext();
			UUID uuidVocabulary = UUID.fromString(nextLine[0]);
			List<UUID> termList = new ArrayList<>();
			uuidMap.put(uuidVocabulary, termList);

			while ( (nextLine = reader.readNext()) != null) {
				UUID uuidTerm = UUID.fromString(nextLine[0]);
				termList.add(uuidTerm);
			}
			reader.close();
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
	public <T extends DefinedTermBase<T>, S extends OrderedTermBase<S>> TermVocabulary<T> loadTerms(
	        VocabularyEnum vocType, Map<UUID,DefinedTermBase> terms) {

		try {
			CSVReader reader = getCsvReader(vocType);
			String [] nextLine = reader.readNext();

			Class<T> termClass = (Class<T>)vocType.getClazz();

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
			S lastInstance = null;
			while ((nextLine = reader.readNext()) != null) {
				// nextLine[] is an array of values from the line
				if (nextLine.length == 0){
					continue;
				}

				lastInstance = handleSingleTerm(nextLine, terms, termClass, voc,
						abbrevAsId, lastInstance, classDefiningTermInstance);
			}
	        reader.close();
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
	private <T extends DefinedTermBase<T>, S extends OrderedTermBase<S> > S handleSingleTerm(
	            String[] csvLine, Map<UUID,DefinedTermBase> terms, Class<T> termClass,
			TermVocabulary<T> voc, boolean abbrevAsId, S lastTerm,
			T classDefiningTermInstance) {
		T term = classDefiningTermInstance.readCsvLine(termClass, arrayedLine(csvLine), voc.getTermType(), terms, abbrevAsId);
		terms.put(term.getUuid(), term);
		if (voc.isInstanceOf(OrderedTermVocabulary.class) && term.isInstanceOf(OrderedTermBase.class)){
		    @SuppressWarnings("unchecked")
            OrderedTermVocabulary<S> orderedVoc = CdmBase.deproxy(voc, OrderedTermVocabulary.class);
		    @SuppressWarnings("unchecked")
            S orderedTerm = (S)CdmBase.deproxy(term, OrderedTermBase.class);
		    if (lastTerm != null){
		        orderedVoc.addTermBelow(orderedTerm, lastTerm);
		    }else{
		        orderedVoc.addTerm(orderedTerm);
		    }
		    return orderedTerm;
		}else{
		    voc.addTerm(term);
		    return null;
		}
	}


	@Override
	public <T extends DefinedTermBase<T>,S extends OrderedTermBase<S>> Set<T> loadSingleTerms(VocabularyEnum vocType,
			TermVocabulary<T> voc, Set<UUID> missingTerms) {
		try {
		    Class<T> termClass = (Class<T>)vocType.getClazz();

			CSVReader reader = getCsvReader(vocType);
			String [] nextLine =  reader.readNext();

			if (! UUID.fromString(nextLine[0]).equals(voc.getUuid())){
				throw new IllegalStateException("Vocabularies in csv file and vocabulary must be equal");
			}

			boolean abbrevAsId = (arrayedLine(nextLine).get(5).equals("1"));
			T classDefiningTermInstance = getInstance(termClass);// ((Class<T>)termClass).newInstance();
			Map<UUID,DefinedTermBase> allVocTerms = new HashMap<>();
			for (T term: voc.getTerms()){
				allVocTerms.put(term.getUuid(), term);
			}

			UUID lastTermUuid = null;
			S lastTerm = null;
			while ((nextLine = reader.readNext()) != null) {
				if (nextLine.length == 0){
					continue;
				}
				UUID uuid = UUID.fromString(nextLine[0]);
				if (missingTerms.contains(uuid)){
				    DefinedTermBase<?> nonOrderedLastTerm = allVocTerms.get(lastTermUuid);
				    if (nonOrderedLastTerm.isInstanceOf(OrderedTermBase.class)){  //to avoid ClassCastException
				        lastTerm = (S)allVocTerms.get(lastTermUuid);
				    }
					lastTerm = handleSingleTerm(nextLine, allVocTerms, termClass, voc, abbrevAsId, lastTerm, classDefiningTermInstance);
				}
				lastTermUuid = uuid;
			}

			return null;
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	/**
	 * Returns the {@link CSVReader} for the given {@link VocabularyEnum}.
	 */
	private CSVReader getCsvReader(VocabularyEnum vocType) throws IOException {
		String filename = vocType.name()+".csv";
		String strResourceFileName = "terms/" + filename;
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
		ArrayList<String> csvTermAttributeList = new ArrayList<>(15);
		for (String col : nextLine){
			csvTermAttributeList.add(col);
		}
		while (csvTermAttributeList.size()<15){
			csvTermAttributeList.add("");
		}
		return csvTermAttributeList;
	}
}