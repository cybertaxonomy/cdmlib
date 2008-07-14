package eu.etaxonomy.cdm.io.berlinModel;

import static eu.etaxonomy.cdm.io.common.IImportConfigurator.DO_REFERENCES.NONE;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.api.service.IService;
import eu.etaxonomy.cdm.io.common.MapWrapper;
import eu.etaxonomy.cdm.io.common.Source;
import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;

@Service
public class BerlinModelImport {
	private static final Logger logger = Logger.getLogger(BerlinModelImport.class);
	
	//Constants
	//final boolean OBLIGATORY = true; 
	//final boolean FACULTATIVE = false; 
	final int modCount = 1000;

	
	//Hashmaps for Joins
	//OLD: private Map<Integer, UUID> referenceMap = new HashMap<Integer, UUID>();
	IService service = null;
	private MapWrapper<TeamOrPersonBase> authorStore= new MapWrapper<TeamOrPersonBase>(service);
	private MapWrapper<ReferenceBase> referenceStore= new MapWrapper<ReferenceBase>(service);
	private MapWrapper<ReferenceBase> nomRefStore= new MapWrapper<ReferenceBase>(service);
	private MapWrapper<TaxonNameBase> taxonNameStore = new MapWrapper<TaxonNameBase>(service);
	private MapWrapper<TaxonBase> taxonStore = new MapWrapper<TaxonBase>(service);

	public boolean invoke(BerlinModelImportConfigurator bmiConfig){
		if (bmiConfig.getCheck().equals(BerlinModelImportConfigurator.CHECK.CHECK_ONLY)){
			return doCheck(bmiConfig);
		}else if (bmiConfig.getCheck().equals(BerlinModelImportConfigurator.CHECK.CHECK_AND_IMPORT)){
			doCheck(bmiConfig);
			return doImport(bmiConfig);
		}else if (bmiConfig.getCheck().equals(BerlinModelImportConfigurator.CHECK.IMPORT_WITHOUT_CHECK)){
			return doImport(bmiConfig);
		}else{
			logger.error("Unknown CHECK type");
			return false;
		}
	}
	
	
	protected boolean doCheck(BerlinModelImportConfigurator bmiConfig){
		boolean result = true;
		System.out.println("Start check BerlinModel ("+ bmiConfig.getSourceNameString() + ") ...");
		
		//check
		if (bmiConfig == null){
			logger.warn("BerlinModelImportConfiguration is null");
			return false;
		}else if (! bmiConfig.isValid()){
			logger.warn("BerlinModelImportConfiguration is not valid");
			return false;
		}
		
		
		//check Authors
		if (bmiConfig.isDoAuthors()){
			result &= BerlinModelAuthorIO.check(bmiConfig);
		}

		//check References
		if (bmiConfig.getDoReferences() != NONE){
			result &= BerlinModelReferenceIO.check(bmiConfig);
		}
		
		//check TaxonNames
		if (bmiConfig.isDoTaxonNames()){
			result &=  BerlinModelTaxonNameIO.check(bmiConfig);
		}
		
		//check RelNames
		if(bmiConfig.isDoRelNames()){
			result &= BerlinModelTaxonNameRelationIO.check(bmiConfig);
		}

		//check nameFacts
		if(bmiConfig.isDoNameFacts()){
			result &= BerlinModelNameFactsIO.check(bmiConfig);
		}
		
		//check nameStatus
		if(bmiConfig.isDoNameStatus()){
			result &= BerlinModelNameStatusIO.check(bmiConfig);
		}

		//check nomStatus
		if(bmiConfig.isDoTypes()){
			result &= new BerlinModelTypesIO().check(bmiConfig);
		}
	
		//check Taxa
		if(bmiConfig.isDoTaxa()){
			result &= BerlinModelTaxonIO.check(bmiConfig);
		}
		
		//check RelPTaxa
		if(bmiConfig.isDoRelTaxa()){
			result &= BerlinModelTaxonRelationIO.check(bmiConfig);
		}
		
		//check Facts
		if(bmiConfig.isDoFacts()){
			result &= BerlinModelFactsIO.check(bmiConfig);
		}
		
		//check Occurence
		if(bmiConfig.isDoOccurrence()){
			result &= BerlinModelOccurrenceIO.check(bmiConfig);
		}
		
		//return
		System.out.println("End checking BerlinModel ("+ bmiConfig.getSourceNameString() + ") for import to CDM");
		return result;

	}
	
	
	/**
	 * Executes the whole 
	 */
	protected boolean doImport(BerlinModelImportConfigurator bmiConfig){
		if (bmiConfig == null){
			logger.warn("BerlinModelImportConfiguration is null");
			return false;
		}else if (! bmiConfig.isValid()){
			logger.warn("BerlinModelImportConfiguration is not valid");
			return false;
		}
//		try {
//			cdmApp = CdmApplicationController.NewInstance(bmiConfig.getDestination(), bmiConfig.getDbSchemaValidation());
//		} catch (DataSourceNotFoundException e) {
//			logger.warn("could not connect to destination database");
//			return false;
//		}catch (TermNotFoundException e) {
//			logger.warn("could not find needed term in destination datasource");
//			return false;
//		}
		CdmApplicationController cdmApp = bmiConfig.getCdmAppController();
		Source source = bmiConfig.getSource();
		ReferenceBase sourceReference = bmiConfig.getSourceReference();
		System.out.println("Start import from BerlinModel ("+ bmiConfig.getSourceNameString() + ") to Cdm  (" + cdmApp.getDatabaseService().getUrl() + ") ...");
		

		//Authors
		if (bmiConfig.isDoAuthors()){
			if (! BerlinModelAuthorIO.invoke(bmiConfig, cdmApp, authorStore)){
				logger.warn("No Authors imported");
				return false;
			}
		}else{
			authorStore = null;
		}
		
		//References
		if (bmiConfig.getDoReferences() != NONE){
			if (! BerlinModelReferenceIO.invoke(bmiConfig, cdmApp, nomRefStore, referenceStore, authorStore)){
				return false;
			}
		}else{
			logger.warn("No References imported");
			//referenceStore = null;
		}
		
		//TaxonNames
		if (bmiConfig.isDoTaxonNames()){
			if (! BerlinModelTaxonNameIO.invoke(bmiConfig, cdmApp, taxonNameStore, nomRefStore, referenceStore, authorStore)){
				//return false;
			}
		}else{
			logger.warn("No TaxonNames imported");
			//taxonNameStore = null;
		}

		//make and save RelNames
		if(bmiConfig.isDoRelNames()){
			if (! BerlinModelTaxonNameRelationIO.invoke(bmiConfig, cdmApp, taxonNameStore, referenceStore)){
				return false;
			}
		}else{
			logger.warn("No RelNames imported");
		}

		//check nameStatus
		if(bmiConfig.isDoNameStatus()){
			if (! BerlinModelNameStatusIO.invoke(bmiConfig, cdmApp, taxonNameStore, referenceStore)){
				return false;
			}
		}else{
			logger.warn("No NomStatus imported");
		}
		
		//NameFacts
		if (bmiConfig.isDoNameFacts()){
			if (! BerlinModelNameFactsIO.invoke(bmiConfig, cdmApp, taxonNameStore, referenceStore)){
				//return false;
			}
		}else{
			logger.warn("No NameFacts imported");
			//taxonNameStore = null;
		}

		//check types
		if(bmiConfig.isDoTypes()){
			if (! BerlinModelTypesIO.invoke(bmiConfig, cdmApp, taxonNameStore, referenceStore)){
				return false;
			}
		}else{
			logger.warn("No Types imported");
		}
		
		//make and save Taxa
		if(bmiConfig.isDoTaxa()){
			if (! BerlinModelTaxonIO.invoke(bmiConfig, cdmApp, taxonStore, taxonNameStore, referenceStore, nomRefStore)){
				return false;
			}
		}else{
			logger.warn("No Taxa imported");
			taxonNameStore = null;
		}
		
		//make and save RelPTaxa
		if(bmiConfig.isDoRelTaxa()){
			if (! BerlinModelTaxonRelationIO.invoke(bmiConfig, cdmApp, taxonStore, referenceStore)){
				return false;
			}
		}else{
			logger.warn("No RelPTaxa imported");
		}
		
		//make and save Facts
		if(bmiConfig.isDoFacts()){
			if (! BerlinModelFactsIO.invoke(bmiConfig, cdmApp, taxonStore, referenceStore, nomRefStore)){
				return false;
			}
		}else{
			logger.warn("No Facts imported");
		}
		
		//make and save Occurrences
		if(bmiConfig.isDoOccurrence()){
			if (! BerlinModelOccurrenceIO.invoke(bmiConfig, cdmApp, taxonStore, referenceStore)){
				return false;
			}
		}else{
			logger.warn("No Occurrences imported");
		}
		
		//return
		System.out.println("End import from BerlinModel ("+ bmiConfig.getSourceNameString() + ") to Cdm  (" + cdmApp.getDatabaseService().getUrl() + ") ...");
		return true;
	}
	

}
