package eu.etaxonomy.cdm.io.berlinModel;

import static eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer.*;
import java.sql.ResultSet;
import java.sql.SQLException;


import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.api.service.ITaxonService;
import eu.etaxonomy.cdm.io.source.Source;
import eu.etaxonomy.cdm.model.agent.Agent;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.SynonymRelationshipType;
import eu.etaxonomy.cdm.model.taxon.Taxon;
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
	public boolean doImport(Source source, CdmApplicationController cdmApp){
		if (source == null || cdmApp == null){
			throw new NullPointerException("Source and CdmApplicationController must not be null");
		}
		this.source = source;
		this.cdmApp = cdmApp;

		//Authors
		if (makeAuthors){
			if (! BerlinModelAuthorIO.invoke(source, cdmApp, deleteAll, authorStore)){
				logger.warn("No Authors imported");
				return false;
			}
		}else{
			authorStore = null;
		}
		
		//References
		if (makeReferences){
			if (! BerlinModelReferenceIO.invoke(source, cdmApp, deleteAll, referenceStore, authorStore)){
				return false;
			}
		}else{
			logger.warn("No References imported");
			referenceStore = null;
		}
		
		//TaxonNames
		if (makeTaxonNames){
			if (! BerlinModelTaxonNameIO.invoke(source, cdmApp, deleteAll, taxonNameStore, referenceStore, authorStore)){
				//return false;
			}
		}else{
			logger.warn("No TaxonNames imported");
			taxonNameStore = null;
		}
		
		//make and save Taxa
		if(makeTaxa){
			if (! BerlinModelTaxonIO.invoke(source, cdmApp, deleteAll, taxonStore, taxonNameStore, referenceStore)){
				return false;
			}
		}else{
			logger.warn("No Taxa imported");
			taxonNameStore = null;
		}
		
		//make and save RelPTaxa
		if(makeRelTaxa){
			if (! BerlinModelTaxonIO.invokeRelations(source, cdmApp, deleteAll, taxonStore, referenceStore)){
				return false;
			}
		}else{
			logger.warn("No RelPTaxa imported");
		}

		
		//make and save Facts
		//makeRelTaxa();
		
		//make and save Facts
		if(makeFacts){
			if (! BerlinModelFactsIO.invoke(source, cdmApp, deleteAll, taxonStore, referenceStore)){
				return false;
			}
		}else{
			logger.warn("No Facts imported");
		}
		
		//return
		return true;
	}
	

}
