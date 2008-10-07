package eu.etaxonomy.cdm.model.common.init;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import au.com.bytecode.opencsv.CSVReader;
import eu.etaxonomy.cdm.common.CdmUtils;

import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.ILoadableTerm;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.MarkerType;
import eu.etaxonomy.cdm.model.common.NoDefinedTermClassException;
import eu.etaxonomy.cdm.model.common.OrderedTermBase;
import eu.etaxonomy.cdm.model.common.OrderedTermVocabulary;
import eu.etaxonomy.cdm.model.common.TermVocabulary;
import eu.etaxonomy.cdm.model.description.AbsenceTerm;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.PresenceTerm;
import eu.etaxonomy.cdm.model.location.Continent;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.location.NamedAreaLevel;
import eu.etaxonomy.cdm.model.location.NamedAreaType;
import eu.etaxonomy.cdm.model.location.WaterbodyOrCountry;
import eu.etaxonomy.cdm.model.name.HybridRelationshipType;
import eu.etaxonomy.cdm.model.name.NameRelationshipType;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatusType;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TypeDesignationStatus;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationshipType;
import eu.etaxonomy.cdm.model.taxon.SynonymRelationshipType;

@Component
@Transactional(readOnly = false)
public class TermLoader {
	private static final Logger logger = Logger.getLogger(TermLoader.class);
	
	@Autowired
	private IVocabularyStore vocabularyStore;
//	private VocabularyStoreImpl vocabularyStore;
	
	
	//TODO private -but Autowiring for constructor arguments is needed then for classes that use this class autowired (e.g. CdmTermInitializer)
	@Deprecated //because still public 
	public TermLoader(){
		super();
	}
	public void setVocabularyStore(IVocabularyStore vocabularyStore){
		this.vocabularyStore = vocabularyStore;		
	}
	
	
	
	public TermLoader(IVocabularyStore vocabularyStore){
		super();
		this.vocabularyStore = vocabularyStore;
	}


	// load a list of defined terms from a simple text file
	// if isEnumeration is true an Enumeration for the ordered term list will be returned
	@Transactional(readOnly = false)
	public TermVocabulary<DefinedTermBase> insertTerms(Class<ILoadableTerm> termClass, String filename, boolean isEnumeration, boolean isOrdered) throws NoDefinedTermClassException, FileNotFoundException {
		DefinedTermBase.setVocabularyStore(vocabularyStore); //otherwise DefinedTermBase is not able to find DefaultLanguage
		try {
			String strResourceFileName = "terms" + CdmUtils.getFolderSeperator() + filename;
			logger.debug("strResourceFileName is " + strResourceFileName);
			InputStream inputStream = CdmUtils.getReadableResourceStream("terms" + CdmUtils.getFolderSeperator() + filename);
			if (inputStream == null) {logger.info("inputStream is null");}
			CSVReader reader = new CSVReader(new InputStreamReader(inputStream));
			
			//vocabulary
			TermVocabulary voc = null;
			String labelAbbrev = null;
			if (isOrdered){
				voc = new OrderedTermVocabulary<OrderedTermBase>(termClass.getCanonicalName(), termClass.getSimpleName(), labelAbbrev, termClass.getCanonicalName());
			}else{
				voc = new TermVocabulary<DefinedTermBase>(termClass.getCanonicalName(), termClass.getSimpleName(), labelAbbrev, termClass.getCanonicalName());
			}
			String [] nextLine = reader.readNext();
			if (nextLine != null){
				voc.readCsvLine(arrayedLine(nextLine));
			}
			saveVocabulary(voc, termClass);
			//terms
			while ((nextLine = reader.readNext()) != null) {
				// nextLine[] is an array of values from the line
				if (nextLine.length == 0){
					continue;
				}
				ILoadableTerm term = termClass.newInstance();
				term = term.readCsvLine(arrayedLine(nextLine));
				term.setVocabulary(voc);
				vocabularyStore.saveOrUpdate(term);
				// save enumeration and all terms to DB
			}
			return voc;
		} catch (Exception e) {
			logger.error(e.getMessage());
			e.printStackTrace();
			return null;
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
	
	
	private void saveVocabulary(TermVocabulary voc, Class<ILoadableTerm> termClass){
		if (vocabularyStore != null){
			vocabularyStore.saveOrUpdate(voc);
		}else{
			//e.g. in tests when no database connection exists
			if (logger.isDebugEnabled()) {logger.debug("No vocabularyStore exists. Vocabulary for class '" + termClass +  "' could not be saved to database");}
		}

	}

	public TermVocabulary<DefinedTermBase> insertDefaultTerms(Class termClass, boolean isOrdered) throws NoDefinedTermClassException, FileNotFoundException {
		return insertDefaultTerms(termClass, termClass.getSimpleName()+".csv", isOrdered );
	}
	
	public TermVocabulary<DefinedTermBase> insertDefaultTerms(Class termClass, String csvName, boolean isOrdered) throws NoDefinedTermClassException, FileNotFoundException {
		if (termClass != null){logger.info("load file " + csvName);}
		return this.insertTerms(termClass, csvName, true, isOrdered );
	}
	
	public boolean insertDefaultTerms() throws FileNotFoundException, NoDefinedTermClassException{
		final boolean ORDERED = true;
		final boolean NOT_ORDERED = false;
		
		logger.info("load terms");
		insertDefaultTerms(Language.class, NOT_ORDERED);
		insertDefaultTerms(Continent.class, NOT_ORDERED);
		insertDefaultTerms(WaterbodyOrCountry.class, NOT_ORDERED);
		insertDefaultTerms(Rank.class, ORDERED);
		insertDefaultTerms(TypeDesignationStatus.class, ORDERED);
		insertDefaultTerms(NomenclaturalStatusType.class, ORDERED);
		insertDefaultTerms(SynonymRelationshipType.class, ORDERED);
		insertDefaultTerms(HybridRelationshipType.class, ORDERED);
		insertDefaultTerms(NameRelationshipType.class, ORDERED);
		insertDefaultTerms(TaxonRelationshipType.class, ORDERED);
		insertDefaultTerms(MarkerType.class, NOT_ORDERED);
		insertDefaultTerms(NamedAreaType.class, NOT_ORDERED);
		insertDefaultTerms(NamedAreaLevel.class, NOT_ORDERED);
		insertDefaultTerms(NomenclaturalCode.class, NOT_ORDERED);
		insertDefaultTerms(Feature.class, NOT_ORDERED);
		insertDefaultTerms(NamedArea.class, "TdwgArea.csv", ORDERED);
		insertDefaultTerms(PresenceTerm.class, "PresenceTerm.csv", ORDERED);
		insertDefaultTerms(AbsenceTerm.class, "AbsenceTerm.csv", ORDERED);
		logger.debug("terms loaded");
		return true;
	}
	
	
	public boolean makeDefaultTermsInserted() throws FileNotFoundException, NoDefinedTermClassException{
		return makeDefaultTermsInserted(vocabularyStore);
	}
	
	public boolean makeDefaultTermsInserted(IVocabularyStore vocabularyStore) throws FileNotFoundException, NoDefinedTermClassException{
		if (vocabularyStore == null){
			vocabularyStore = this.vocabularyStore;
		}
		if (! checkBasicTermsExist(vocabularyStore)){
			return insertDefaultTerms();
		}
		return true;
	}
	
	/**
	 * True, if some of the important basic terms are accessible by saver
	 * @param saver
	 * @return
	 */
	private boolean checkBasicTermsExist(IVocabularyStore vocabularyStore){
		if (vocabularyStore == null){
			vocabularyStore = this.vocabularyStore;
		}
		if (vocabularyStore == null){
			return false;
		}	
		Map<String, UUID> allImportantUuid = new HashMap<String,UUID>();
		allImportantUuid.put("English", UUID.fromString("e9f8cdb7-6819-44e8-95d3-e2d0690c3523"));
		allImportantUuid.put("Rank", UUID.fromString("1b11c34c-48a8-4efa-98d5-84f7f66ef43a"));
		for (UUID uuid: allImportantUuid.values()){
			if (! checkTermExists(uuid, vocabularyStore)){
				return false;
			}
		}
		return true;
	}
	
	private boolean checkTermExists(UUID uuid, IVocabularyStore vocabularyStore){
		if (vocabularyStore == null){
			return false;
		}
		ILoadableTerm basicTerm = vocabularyStore.getTermByUuid(uuid);
		if ( basicTerm == null || ! basicTerm.getUuid().equals(uuid)){
			return false;
		}else{
			return true;
		}
		
	}

}
