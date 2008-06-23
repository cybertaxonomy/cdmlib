/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.tcs;

import static eu.etaxonomy.cdm.io.common.IImportConfigurator.DO_REFERENCES.NONE;

import java.io.File;

import org.apache.log4j.Logger;
import org.jdom.Element;

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.api.service.IService;
import eu.etaxonomy.cdm.database.DataSourceNotFoundException;
import eu.etaxonomy.cdm.io.berlinModel.BerlinModelAuthorIO;
import eu.etaxonomy.cdm.io.berlinModel.BerlinModelFactsIO;
import eu.etaxonomy.cdm.io.berlinModel.BerlinModelImportConfigurator;
import eu.etaxonomy.cdm.io.berlinModel.BerlinModelNameFactsIO;
import eu.etaxonomy.cdm.io.berlinModel.BerlinModelOccurrenceIO;
import eu.etaxonomy.cdm.io.berlinModel.BerlinModelReferenceIO;
import eu.etaxonomy.cdm.io.berlinModel.BerlinModelTaxonIO;
import eu.etaxonomy.cdm.io.berlinModel.BerlinModelTaxonNameIO;
import eu.etaxonomy.cdm.io.berlinModel.BerlinModelTypesIO;
import eu.etaxonomy.cdm.io.common.ICdmImport;
import eu.etaxonomy.cdm.io.common.IImportConfigurator;
import eu.etaxonomy.cdm.io.common.MapWrapper;
import eu.etaxonomy.cdm.io.common.Source;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.common.init.TermNotFoundException;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;

/**
 * @author a.mueller
 * @created 20.06.2008
 * @version 1.0
 */
public class TcsImport implements ICdmImport {
	private static Logger logger = Logger.getLogger(TcsImport.class);
	
	
	//Constants
	//final boolean OBLIGATORY = true; 
	//final boolean FACULTATIVE = false; 
	final int modCount = 1000;

	
	//Hashmaps for Joins
	//OLD: private Map<Integer, UUID> referenceMap = new HashMap<Integer, UUID>();
	IService service = null;
	private MapWrapper<Team> authorStore= new MapWrapper<Team>(service);
	private MapWrapper<ReferenceBase> referenceStore= new MapWrapper<ReferenceBase>(service);
	private MapWrapper<TaxonNameBase> taxonNameStore = new MapWrapper<TaxonNameBase>(service);
	private MapWrapper<TaxonBase> taxonStore = new MapWrapper<TaxonBase>(service);

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.tcs.ICdmImport#invoke(eu.etaxonomy.cdm.io.tcs.TcsImportConfigurator)
	 */
	public boolean invoke(TcsImportConfigurator tcsiConfig){
		if (tcsiConfig.getCheck().equals(IImportConfigurator.CHECK.CHECK_ONLY)){
			return doCheck(tcsiConfig);
		}else if (tcsiConfig.getCheck().equals(IImportConfigurator.CHECK.CHECK_AND_IMPORT)){
			doCheck(tcsiConfig);
			return doImport(tcsiConfig);
		}else if (tcsiConfig.getCheck().equals(IImportConfigurator.CHECK.IMPORT_WITHOUT_CHECK)){
			return doImport(tcsiConfig);
		}else{
			logger.error("Unknown CHECK type");
			return false;
		}
	}
	
	
	protected boolean doCheck(TcsImportConfigurator tcsConfig){
		boolean result = true;
		System.out.println("Start check BerlinModel ("+ tcsConfig.getSource().toString() + ") ...");
		
		//check
		if (tcsConfig == null){
			logger.warn("BerlinModelImportConfiguration is null");
			return false;
		}else if (! tcsConfig.isValid()){
			logger.warn("BerlinModelImportConfiguration is not valid");
			return false;
		}
		
		
//		//check Authors
//		if (iConfig.isDoAuthors()){
//			result &= TcsAuthorIO.check(iConfig);
//		}

		//check References
		if (tcsConfig.getDoReferences() != NONE){
			result &= TcsReferenceIO.check(tcsConfig);
		}
		
		//check TaxonNames
		if (tcsConfig.isDoTaxonNames()){
			result &=  TcsTaxonNameIO.check(tcsConfig);
		}
		
		//check RelNames
		if(tcsConfig.isDoRelNames()){
			result &= TcsTaxonNameIO.checkRelations(tcsConfig);
		}

//		//check types
//		if(iConfig.isDoTypes()){
//			result &= BerlinModelTypesIO.check(iConfig);
//		}
	
		//check Taxa
		if(tcsConfig.isDoTaxa()){
			result &= TcsTaxonIO.check(tcsConfig);
		}
		
		//check RelPTaxa
		if(tcsConfig.isDoRelTaxa()){
			result &= TcsTaxonIO.checkRelations(tcsConfig);
		}
		
//		//check Facts
//		if(iConfig.isDoFacts()){
//			result &= TcsFactsIO.check(tcsConfig);
//		}
		
//		//check Occurence
//		if(iConfig.isDoOccurrence()){
//			result &= BerlinModelOccurrenceIO.check(tcsConfig);
//		}
		
		//return
		System.out.println("End checking BerlinModel ("+ tcsConfig.getSource().toString() + ") for import to CDM");
		return result;

	}
	
	
	/**
	 * Executes the whole 
	 */
	protected boolean doImport(TcsImportConfigurator tcsConfig){
		CdmApplicationController cdmApp;
		if (tcsConfig == null){
			logger.warn("BerlinModelImportConfiguration is null");
			return false;
		}else if (! tcsConfig.isValid()){
			logger.warn("BerlinModelImportConfiguration is not valid");
			return false;
		}
		try {
			cdmApp = CdmApplicationController.NewInstance(tcsConfig.getDestination(), tcsConfig.getDbSchemaValidation());
		} catch (DataSourceNotFoundException e) {
			logger.warn("could not connect to destination database");
			return false;
		}catch (TermNotFoundException e) {
			logger.warn("could not find needed term in destination datasource");
			return false;
		}
		Element source = tcsConfig.getSourceRoot();
		ReferenceBase sourceReference = tcsConfig.getSourceReference();
		System.out.println("Start import from BerlinModel ("+ tcsConfig.getSource().toString() + ") to Cdm  (" + cdmApp.getDatabaseService().getUrl() + ") ...");
		

//		//Authors
//		if (tcsConfig.isDoAuthors()){
//			if (! BerlinModelAuthorIO.invoke(tcsConfig, cdmApp, authorStore)){
//				logger.warn("No Authors imported");
//				return false;
//			}
//		}else{
//			authorStore = null;
//		}
		
		//References
		if (tcsConfig.getDoReferences() != NONE){
			if (! TcsReferenceIO.invoke(tcsConfig, cdmApp, referenceStore, authorStore)){
				return false;
			}
		}else{
			logger.warn("No References imported");
			//referenceStore = null;
		}
		
		//TaxonNames
		if (tcsConfig.isDoTaxonNames()){
			if (! TcsTaxonNameIO.invoke(tcsConfig, cdmApp, taxonNameStore, referenceStore, authorStore)){
				//return false;
			}
		}else{
			logger.warn("No TaxonNames imported");
			//taxonNameStore = null;
		}

		//make and save RelNames
		if(tcsConfig.isDoRelNames()){
			if (! TcsTaxonNameIO.invokeRelations(tcsConfig, cdmApp, taxonNameStore, referenceStore)){
				return false;
			}
		}else{
			logger.warn("No RelNames imported");
		}

		//make and save Taxa
		if(tcsConfig.isDoTaxa()){
			if (! TcsTaxonIO.invoke(tcsConfig, cdmApp, taxonStore, taxonNameStore, referenceStore)){
				return false;
			}
		}else{
			logger.warn("No Taxa imported");
			taxonNameStore = null;
		}
		
		//make and save RelPTaxa
		if(tcsConfig.isDoRelTaxa()){
			if (! TcsTaxonIO.invokeRelations(tcsConfig, cdmApp, taxonStore, referenceStore)){
				return false;
			}
		}else{
			logger.warn("No RelPTaxa imported");
		}
//		
//		//make and save Facts
//		if(tcsConfig.isDoFacts()){
//			if (! BerlinModelFactsIO.invoke(tcsConfig, cdmApp, taxonStore, referenceStore)){
//				return false;
//			}
//		}else{
//			logger.warn("No Facts imported");
//		}
		
//		//make and save Occurrences
//		if(tcsConfig.isDoOccurrence()){
//			if (! BerlinModelOccurrenceIO.invoke(tcsConfig, cdmApp, taxonStore, referenceStore)){
//				return false;
//			}
//		}else{
//			logger.warn("No Occurrences imported");
//		}
		
		//return
		System.out.println("End import from BerlinModel ("+ source.getName() + ") to Cdm  (" + cdmApp.getDatabaseService().getUrl() + ") ...");
		return true;
	}
	

}
