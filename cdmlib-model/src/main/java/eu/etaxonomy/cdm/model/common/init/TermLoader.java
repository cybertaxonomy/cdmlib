package eu.etaxonomy.cdm.model.common.init;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
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
	
	private static Map<UUID, DefinedTermBase> definedTermsMap;
	
	@Autowired
	private IVocabularySaver vocabularySaver;
	
	public static void setDefinedTermsMap(Map<UUID, DefinedTermBase> dtm){
		 definedTermsMap = dtm;
	}

	// load a list of defined terms from a simple text file
	// if isEnumeration is true an Enumeration for the ordered term list will be returned
	@Transactional(readOnly = false)
	public TermVocabulary<DefinedTermBase> loadTerms(Class<IDefTerm> termClass, String filename, boolean isEnumeration) throws NoDefinedTermClassException, FileNotFoundException {
		TermVocabulary<DefinedTermBase> voc = new TermVocabulary<DefinedTermBase>(termClass.getCanonicalName(), termClass.getSimpleName(), termClass.getCanonicalName());
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
				if (vocabularySaver != null){
					vocabularySaver.saveOrUpdate(voc);
				}else{
					//e.g. in tests when no database connection exists
					logger.debug("No dao exists. Vocabulary for class '" + termClass +  "' could not be saved to database");
				}
				if (definedTermsMap != null){
					DefinedTermBase defTermBase = (DefinedTermBase)term;
					definedTermsMap.put(defTermBase.getUuid(), defTermBase);	
				}
				
			}
		} catch (Exception e) {
			logger.error(e.getStackTrace());
		}
		return voc;
	}

	public TermVocabulary loadDefaultTerms(Class termClass) throws NoDefinedTermClassException, FileNotFoundException {
		return this.loadTerms(termClass, termClass.getSimpleName()+".csv", true);
	}
	
	public void loadAllDefaultTerms() throws FileNotFoundException, NoDefinedTermClassException{
		// first insert default language english, its used everywhere even for Language!
		//Language langEN = new Language("e9f8cdb7-6819-44e8-95d3-e2d0690c3523");
		//dao.save(langEN);
		
		logger.debug("load terms");
		loadDefaultTerms(Language.class);
		loadDefaultTerms(WaterbodyOrCountry.class);
		loadDefaultTerms(Continent.class);
		loadDefaultTerms(Rank.class);
		loadDefaultTerms(TypeDesignationStatus.class);
		loadDefaultTerms(NomenclaturalStatusType.class);
		loadDefaultTerms(SynonymRelationshipType.class);
		loadDefaultTerms(HybridRelationshipType.class);
		loadDefaultTerms(NameRelationshipType.class);
		loadDefaultTerms(TaxonRelationshipType.class);
		logger.debug("terms loaded");
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
