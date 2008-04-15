package eu.etaxonomy.cdm.io.berlinModel;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.database.DataSourceNotFoundException;
import eu.etaxonomy.cdm.io.source.Source;
import eu.etaxonomy.cdm.model.agent.Agent;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;

@Service
public class BerlinModelImport {
	private static final Logger logger = Logger.getLogger(BerlinModelImport.class);
	
	//TODO
	private boolean deleteAll = false;
	
	private boolean makeAuthors = true;

	//references
	private boolean makeReferences = true;

	//names
	private boolean makeTaxonNames = true;
	private boolean makeRelNames = true;
		private boolean makeNameStatus = true;

	//taxa
	private boolean makeTaxa = true;
	private boolean makeRelTaxa = true;
		private boolean makeFacts = true;
	
	
	//BerlinModelDB
	private Source sourceX;
	
	//CdmApplication
	private CdmApplicationController cdmApp;
	
	//Constants
	//final boolean OBLIGATORY = true; 
	//final boolean FACULTATIVE = false; 
	final int modCount = 1000;

	
	//Hashmaps for Joins
	//OLD: private Map<Integer, UUID> referenceMap = new HashMap<Integer, UUID>();
	private MapWrapper<Team> authorStore= new MapWrapper<Team>(null);
	private MapWrapper<ReferenceBase> referenceStore= new MapWrapper<ReferenceBase>(null);
	private MapWrapper<TaxonNameBase> taxonNameStore = new MapWrapper<TaxonNameBase>(null);
	private MapWrapper<TaxonBase> taxonStore = new MapWrapper<TaxonBase>(null);


	/**
	 * Executes the whole 
	 */
	public boolean doImport(BerlinModelImportConfigurator bmiConfig){
		CdmApplicationController cdmApp;
		if (bmiConfig == null){
			logger.warn("BerlinModelImportConfiguration is null");
			return false;
		}else if (! bmiConfig.isValid()){
			logger.warn("BerlinModelImportConfiguration is not valid");
			return false;
		}
		try {
			cdmApp = CdmApplicationController.NewInstance(bmiConfig.getDestination());
		} catch (DataSourceNotFoundException e) {
			logger.warn("could not connect to destination database");
			return false;
		}
		Source source = bmiConfig.getSource();
		ReferenceBase sourceReference = bmiConfig.getSourceReference();
		System.out.println("Start import from BerlinModel ("+ bmiConfig.getSource().getDatabase() + ") to Cdm  (" + cdmApp.getDatabaseService().getUrl() + ") ...");
		

		//Authors
		if (makeAuthors){
			if (! BerlinModelAuthorIO.invoke(sourceReference ,source, cdmApp, deleteAll, authorStore)){
				logger.warn("No Authors imported");
				return false;
			}
		}else{
			authorStore = null;
		}
		
		//References
		if (makeReferences){
			if (! BerlinModelReferenceIO.invoke(sourceReference ,source, cdmApp, deleteAll, referenceStore, authorStore)){
				return false;
			}
		}else{
			logger.warn("No References imported");
			referenceStore = null;
		}
		
		//TaxonNames
		if (makeTaxonNames){
			if (! BerlinModelTaxonNameIO.invoke(sourceReference ,source, cdmApp, deleteAll, taxonNameStore, referenceStore, authorStore)){
				//return false;
			}
		}else{
			logger.warn("No TaxonNames imported");
			taxonNameStore = null;
		}

		
		//make and save RelNames
		if(makeRelNames){
			if (! BerlinModelTaxonNameIO.invokeRelations(sourceReference ,source, cdmApp, deleteAll, taxonNameStore, referenceStore)){
				return false;
			}
		}else{
			logger.warn("No RelPTaxa imported");
		}

		//TODO NomStatus
		//TODO Types
		
		//make and save Taxa
		if(makeTaxa){
			if (! BerlinModelTaxonIO.invoke(sourceReference, source, cdmApp, deleteAll, taxonStore, taxonNameStore, referenceStore)){
				return false;
			}
		}else{
			logger.warn("No Taxa imported");
			taxonNameStore = null;
		}
		
		//make and save RelPTaxa
		if(makeRelTaxa){
			if (! BerlinModelTaxonIO.invokeRelations(sourceReference, source, cdmApp, deleteAll, taxonStore, referenceStore)){
				return false;
			}
		}else{
			logger.warn("No RelPTaxa imported");
		}
		
		//make and save Facts
		if(makeFacts){
			if (! BerlinModelFactsIO.invoke(sourceReference, source, cdmApp, deleteAll, taxonStore, referenceStore)){
				return false;
			}
		}else{
			logger.warn("No Facts imported");
		}
		
		//return
		System.out.println("End import from BerlinModel ("+ source.getDatabase() + ") to Cdm  (" + cdmApp.getDatabaseService().getUrl() + ") ...");
		return true;
	}
	

}
