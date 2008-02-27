package eu.etaxonomy.cdm.io.berlinModel;

import static eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.api.service.IAgentService;
import eu.etaxonomy.cdm.api.service.INameService;
import eu.etaxonomy.cdm.api.service.IReferenceService;
import eu.etaxonomy.cdm.api.service.IService;
import eu.etaxonomy.cdm.api.service.ITaxonService;
import eu.etaxonomy.cdm.io.source.Source;
import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.reference.Article;
import eu.etaxonomy.cdm.model.reference.Book;
import eu.etaxonomy.cdm.model.reference.BookSection;
import eu.etaxonomy.cdm.model.reference.Database;
import eu.etaxonomy.cdm.model.reference.Generic;
import eu.etaxonomy.cdm.model.reference.Journal;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.reference.StrictReferenceBase;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.SynonymRelationshipType;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.strategy.exceptions.UnknownRankException;

@Service
public class BerlinModelImport {
	private static final Logger logger = Logger.getLogger(BerlinModelImport.class);
	
	private boolean deleteAll = false;
	
	//BerlinModelDB
	private Source source;
	
	//CdmApplication
	private CdmApplicationController cdmApp;
	
	//Constants
	//final boolean OBLIGATORY = true; 
	//final boolean FACULTATIVE = false; 
	final int modCount = 1000;

	
	//Hashmaps for Joins
	private Map<Integer, UUID> referenceMap = new HashMap<Integer, UUID>();
	private Map<Integer, UUID> taxonNameMap = new HashMap<Integer, UUID>();
	private Map<Integer, UUID> taxonMap = new HashMap<Integer, UUID>();


	/**
	 * Executes the whole 
	 */
	public boolean doImport(Source source, CdmApplicationController cdmApp){
		if (source == null || cdmApp == null){
			throw new NullPointerException("Source and CdmApplicationController must not be null");
		}
		this.source = source;
		this.cdmApp = cdmApp;

		//make and save Authors
		makeAuthors();
		
		//make and save References
		if (true){
			if (! BerlinModelReferenceIO.invoke(source, cdmApp, deleteAll, referenceMap)){
				return false;
			}
		}else{
			referenceMap = null;
		}
		
		//make and save Names
		if (! BerlinModelTaxonNameIO.invoke(source, cdmApp, deleteAll, taxonNameMap, referenceMap)){
			return false;
		}
		
		if(true){
			return true;
		}
		
		//make and save Taxa
		if (! BerlinModelTaxonIO.invoke(source, cdmApp, deleteAll, taxonMap, taxonNameMap, referenceMap)){
			return false;
		}
		
		//make and save Facts
		makeRelTaxa();
		
		//make and save Facts
		makeFacts();
		
		if (false){
			//make and save publications
/*				makePublications(root);
				saveToXml(root.getChild("Publications", nsTcs), outputPath, outputFileName + "Publications", format);
				
				saveToXml(root.getChild("TaxonNames", nsTcs), outputPath, outputFileName + "_TaxonNames", format);
				
				//make and save Concepts
				makeConcepts(root);
				saveToXml(root.getChild("TaxonConcepts", nsTcs), outputPath, outputFileName + "_TaxonConcepts", format);
*/		}
		return true;
	}
	
	
	
	/**
	 * @return
	 */
	private boolean makeAuthors(){
		String dbAttrName;
		String cdmAttrName;
		
		logger.info("start makeAuthors ...");
		logger.warn("Authors not yet implemented !!");

		IAgentService agentService = cdmApp.getAgentService();
		boolean delete = deleteAll;
		
//		if (delete){
//			List<Agent> listAllAgents =  agentService.getAllAgents(0, 1000);
//			while(listAllAgents.size() > 0 ){
//				for (Agent name : listAllAgents ){
//					//FIXME
//					//nameService.remove(name);
//				}
//				listAllAgents =  agentService.getAllAgents(0, 1000);
//			}			
//		}
//		try {
			//get data from database
			String strQuery = 
					" SELECT *  " +
                    " FROM AuthorTeam " ;
			ResultSet rs = source.getResultSet(strQuery) ;
			
			
			
			
			
			logger.info("end makeAuthors ...");
			return true;
//		} catch (SQLException e) {
//			logger.error("SQLException:" +  e);
//			return false;
//		}
	}
	

	/**
	 * @return
	 */
	private boolean makeRelTaxa(){
		String dbAttrName;
		String cdmAttrName;
		
		logger.info("start makeTaxonRelationships ...");
		logger.warn("RelTaxa not yet implemented !!");

		ITaxonService taxonService = cdmApp.getTaxonService();
		boolean delete = deleteAll;
		
//		if (delete){
//			List<Agent> listAllAgents =  agentService.getAllAgents(0, 1000);
//			while(listAllAgents.size() > 0 ){
//				for (Agent name : listAllAgents ){
//					//FIXME
//					//nameService.remove(name);
//				}
//				listAllAgents =  agentService.getAllAgents(0, 1000);
//			}			
//		}
		try {
			//get data from database
			String strQuery = 
					" SELECT *  " +
                    " FROM RelPTaxon Join Taxon1 Join Taxon2" ;
			ResultSet rs = source.getResultSet(strQuery) ;
			int i = 0;
			//for each reference
			while (rs.next()){
				
				if ((i++ % modCount) == 0){ logger.info("RelPTaxa handled: " + (i-1));}
				
				
				int taxon1Id = rs.getInt("taxon1Id");
				int taxon2Id = rs.getInt("taxon2Id");
				int factId = rs.getInt("factId");
				int relTypeFk = rs.getInt("relTypeFk");
				
				TaxonBase taxon1 = getTaxonById(taxon1Id, taxonService);
				TaxonBase taxon2 = getTaxonById(taxon2Id, taxonService);
				
				//TODO
				ReferenceBase citation = null;
				String microcitation = null;

				
				if (relTypeFk == IS_INCLUDED_IN){
					((Taxon)taxon2).addTaxonomicChild((Taxon)taxon1, citation, microcitation);
				}else if (relTypeFk == IS_SYNONYM_OF){
					((Taxon)taxon2).addSynonym((Synonym)taxon1, SynonymRelationshipType.SYNONYM_OF());
				}else if (relTypeFk == IS_HOMOTYPIC_SYNONYM_OF){
					((Taxon)taxon2).addSynonym((Synonym)taxon1, SynonymRelationshipType.HOMOTYPIC_SYNONYM_OF());
				}else if (relTypeFk == IS_HETEROTYPIC_SYNONYM_OF){
					((Taxon)taxon2).addSynonym((Synonym)taxon1, SynonymRelationshipType.HETEROTYPIC_SYNONYM_OF());
				}else if (relTypeFk == IS_MISAPPLIED_NAME_OF){
					((Taxon)taxon2).addMisappliedName((Taxon)taxon1, citation, microcitation);
				}else {
					//TODO
					logger.warn("TaxonRelationShipType " + relTypeFk + " not yet implemented");
				}
				
				
			//....
			
			
			
			}
			logger.info("end makeFacts ...");
			return true;
		} catch (SQLException e) {
			logger.error("SQLException:" +  e);
			return false;
		}
	}
	
	
	private TaxonBase getTaxonById(int id, IService<TaxonBase> service){
		TaxonBase result;
		UUID uuid = taxonMap.get(id);
		if (uuid == null){
			result = null;
		}else{
			result  = ((ITaxonService)service).getTaxonByUuid(uuid); //.getCdmObjectByUuid(uuid);//  taxonService.getTaxonByUuid(taxonUuid);
		}
		return result;
	
	}

	/**
	 * @return
	 */
	private boolean makeFacts(){
		String dbAttrName;
		String cdmAttrName;
		
		logger.info("start makeFacts ...");
		logger.warn("Facts not yet implemented !!");

		//IAgentService agentService = cdmApp.getAgentService();
		boolean delete = deleteAll;
		
//		if (delete){
//			List<Agent> listAllAgents =  agentService.getAllAgents(0, 1000);
//			while(listAllAgents.size() > 0 ){
//				for (Agent name : listAllAgents ){
//					//FIXME
//					//nameService.remove(name);
//				}
//				listAllAgents =  agentService.getAllAgents(0, 1000);
//			}			
//		}
		try {
			//get data from database
			String strQuery = 
					" SELECT *  " +
                    " FROM Facts " ;
			ResultSet rs = source.getResultSet(strQuery) ;
			int i = 0;
			//for each reference
			while (rs.next()){
				
				if ((i++ % modCount) == 0){ logger.info("Facts handled: " + (i-1));}
				
				//create TaxonName element
				int factId = rs.getInt("factId");

			//....
			
			
			
			}
			logger.info("end makeFacts ...");
			return true;
		} catch (SQLException e) {
			logger.error("SQLException:" +  e);
			return false;
		}
	}
	

	
	


}
