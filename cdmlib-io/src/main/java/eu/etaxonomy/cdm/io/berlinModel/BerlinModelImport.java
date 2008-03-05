package eu.etaxonomy.cdm.io.berlinModel;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.io.source.Source;
import eu.etaxonomy.cdm.model.agent.Agent;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;

@Service
public class BerlinModelImport {
	private static final Logger logger = Logger.getLogger(BerlinModelImport.class);
	
	private boolean deleteAll = false;
	
	private boolean makeAuthors = false;
	private boolean makeReferences = true;
	private boolean makeTaxonNames = true;
	private boolean makeTaxa = true;
	private boolean makeRelNames = false;
	private boolean makeNameStatus = false;
	private boolean makeRelTaxa = true;
	private boolean makeFacts = true;
	
	
	//BerlinModelDB
	private Source source;
	
	//CdmApplication
	private CdmApplicationController cdmApp;
	
	//Constants
	//final boolean OBLIGATORY = true; 
	//final boolean FACULTATIVE = false; 
	final int modCount = 1000;

	
	//Hashmaps for Joins
	//OLD: private Map<Integer, UUID> referenceMap = new HashMap<Integer, UUID>();
	private MapWrapper<Agent> authorStore= new MapWrapper<Agent>(null);
	private MapWrapper<ReferenceBase> referenceStore= new MapWrapper<ReferenceBase>(null);
	private MapWrapper<TaxonNameBase> taxonNameStore = new MapWrapper<TaxonNameBase>(null);
	private MapWrapper<TaxonBase> taxonStore = new MapWrapper<TaxonBase>(null);


	/**
	 * Executes the whole 
	 */
	public boolean doImport(ReferenceBase berlinModelRef, Source source, CdmApplicationController cdmApp){
		System.out.println("Start import from BerlinModel ("+ source.getDatabase() + ") to Cdm  (" + cdmApp.getDatabaseService().getUrl() + ") ...");
		if (source == null || cdmApp == null){
			throw new NullPointerException("Source and CdmApplicationController must not be null");
		}
		this.source = source;
		this.cdmApp = cdmApp;

		//Authors
		if (makeAuthors){
			if (! BerlinModelAuthorIO.invoke(berlinModelRef ,source, cdmApp, deleteAll, authorStore)){
				logger.warn("No Authors imported");
				return false;
			}
		}else{
			authorStore = null;
		}
		
		//References
		if (makeReferences){
			if (! BerlinModelReferenceIO.invoke(berlinModelRef ,source, cdmApp, deleteAll, referenceStore, authorStore)){
				return false;
			}
		}else{
			logger.warn("No References imported");
			referenceStore = null;
		}
		
		//TaxonNames
		if (makeTaxonNames){
			if (! BerlinModelTaxonNameIO.invoke(berlinModelRef ,source, cdmApp, deleteAll, taxonNameStore, referenceStore, authorStore)){
				//return false;
			}
		}else{
			logger.warn("No TaxonNames imported");
			taxonNameStore = null;
		}

		
		//make and save RelNames
		if(makeRelNames){
			if (! BerlinModelTaxonNameIO.invokeRelations(berlinModelRef ,source, cdmApp, deleteAll, taxonNameStore, referenceStore)){
				return false;
			}
		}else{
			logger.warn("No RelPTaxa imported");
		}

		//TODO NomStatus
		//TODO Types
		
		//make and save Taxa
		if(makeTaxa){
			if (! BerlinModelTaxonIO.invoke(berlinModelRef, source, cdmApp, deleteAll, taxonStore, taxonNameStore, referenceStore)){
				return false;
			}
		}else{
			logger.warn("No Taxa imported");
			taxonNameStore = null;
		}
		
		//make and save RelPTaxa
		if(makeRelTaxa){
			if (! BerlinModelTaxonIO.invokeRelations(berlinModelRef, source, cdmApp, deleteAll, taxonStore, referenceStore)){
				return false;
			}
		}else{
			logger.warn("No RelPTaxa imported");
		}
		
		//make and save Facts
		if(makeFacts){
			if (! BerlinModelFactsIO.invoke(berlinModelRef, source, cdmApp, deleteAll, taxonStore, referenceStore)){
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
