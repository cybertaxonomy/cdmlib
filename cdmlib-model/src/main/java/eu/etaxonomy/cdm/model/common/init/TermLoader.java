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
import eu.etaxonomy.cdm.model.common.IDefTerm;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.NoDefinedTermClassException;
import eu.etaxonomy.cdm.model.common.OrderedTermBase;
import eu.etaxonomy.cdm.model.common.OrderedTermVocabulary;
import eu.etaxonomy.cdm.model.common.TermVocabulary;
import eu.etaxonomy.cdm.model.location.Continent;
import eu.etaxonomy.cdm.model.location.WaterbodyOrCountry;
import eu.etaxonomy.cdm.model.name.HybridRelationshipType;
import eu.etaxonomy.cdm.model.name.NameRelationshipType;
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
	public TermVocabulary<DefinedTermBase> loadTerms(Class<IDefTerm> termClass, String filename, boolean isEnumeration, boolean isOrdered) throws NoDefinedTermClassException, FileNotFoundException {
		TermVocabulary voc;
		DefinedTermBase.setVocabularyStore(vocabularyStore); //otherwise DefinedTermBase is not able to find DefaultLanguage
		if (isOrdered){
			voc = new OrderedTermVocabulary<OrderedTermBase>(termClass.getCanonicalName(), termClass.getSimpleName(), termClass.getCanonicalName());
		}else{
			voc = new TermVocabulary<DefinedTermBase>(termClass.getCanonicalName(), termClass.getSimpleName(), termClass.getCanonicalName());
		}
		try {
			String strResourceFileName = "terms" + CdmUtils.getFolderSeperator() + filename;
			logger.debug("strResourceFileName is " + strResourceFileName);
			InputStream inputStream = CdmUtils.getReadableResourceStream("terms" + CdmUtils.getFolderSeperator() + filename);
			if (inputStream == null) {logger.debug("inputStream is null");}
			CSVReader reader = new CSVReader(new InputStreamReader(inputStream));
			
			String [] nextLine;
			while ((nextLine = reader.readNext()) != null) {
				// nextLine[] is an array of values from the line
				IDefTerm term = termClass.newInstance();
				ArrayList<String> aList = new ArrayList<String>(10);
				for (String col : nextLine){
					aList.add(col);
				}
				while (aList.size()<10){
					aList.add("");
				}
				term.readCsvLine(aList);
				term.setVocabulary(voc);
				// save enumeration and all terms to DB
				if (vocabularyStore != null){
					vocabularyStore.saveOrUpdate(voc);
				}else{
					//e.g. in tests when no database connection exists
					if (logger.isDebugEnabled()) {logger.debug("No vocabularyStore exists. Vocabulary for class '" + termClass +  "' could not be saved to database");}
				}
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		return voc;
	}

	public TermVocabulary<DefinedTermBase> loadDefaultTerms(Class termClass, boolean isOrdered) throws NoDefinedTermClassException, FileNotFoundException {
		if (termClass != null){logger.info("load class " + termClass.getName());}
		return this.loadTerms(termClass, termClass.getSimpleName()+".csv", true, isOrdered );
	}
	
	public void loadAllDefaultTerms() throws FileNotFoundException, NoDefinedTermClassException{
		// first insert default language english, its used everywhere even for Language!
		//Language langEN = new Language("e9f8cdb7-6819-44e8-95d3-e2d0690c3523");
		//dao.save(langEN);
		final boolean ORDERED = true;
		final boolean NOT_ORDERED = false;
		
		logger.info("load terms");
		loadDefaultTerms(Language.class, NOT_ORDERED);
		loadDefaultTerms(WaterbodyOrCountry.class, NOT_ORDERED);
		loadDefaultTerms(Continent.class, NOT_ORDERED);
		loadDefaultTerms(Rank.class, ORDERED);
		loadDefaultTerms(TypeDesignationStatus.class, ORDERED);
		loadDefaultTerms(NomenclaturalStatusType.class, ORDERED);
		loadDefaultTerms(SynonymRelationshipType.class, ORDERED);
		loadDefaultTerms(HybridRelationshipType.class, ORDERED);
		loadDefaultTerms(NameRelationshipType.class, ORDERED);
		loadDefaultTerms(TaxonRelationshipType.class, ORDERED);
		logger.debug("terms loaded");
	}
	
	/**
	 * True, if some of the important basic terms are accessible by saver
	 * @param saver
	 * @return
	 */
	public boolean basicTermsExist(IVocabularyStore saver){
		if (saver == null){
			saver = vocabularyStore;
		}
		if (saver == null){
			return false;
		}	
		Map<String, UUID> allImportantUuid = new HashMap<String,UUID>();
		allImportantUuid.put("English", UUID.fromString("e9f8cdb7-6819-44e8-95d3-e2d0690c3523"));
		allImportantUuid.put("Rank", UUID.fromString("1b11c34c-48a8-4efa-98d5-84f7f66ef43a"));
		for (UUID uuid: allImportantUuid.values()){
			if (! basicTermExists(uuid, saver)){
				return false;
			}
		}
		return true;
	}
	
	private boolean basicTermExists(UUID uuid, IVocabularyStore saver){
		if (saver == null){
			return false;
		}
		DefinedTermBase basicTerm = saver.getTermByUuid(uuid);
		if ( basicTerm == null || ! basicTerm.getUuid().equals(uuid)){
			return false;
		}else{
			return true;
		}
		
	}
	

/******************* not in version 4.5 (cdmlib-model)*/

//
//	public static void main(String[] args) {
//		CdmApplicationController appCtr = new CdmApplicationController();
//		TermLoader tl = (TermLoader) appCtr.applicationContext.getBean("termLoader");
//		try {
//			tl.loadAllDefaultTerms();
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
//	
//	public void loadAllDefaultTerms() throws FileNotFoundException, NoDefinedTermClassException{
//		IVocabularySaver vocabularySaver = dao;
//		loadAllDefaultTerms(vocabularySaver);
//	}
}
