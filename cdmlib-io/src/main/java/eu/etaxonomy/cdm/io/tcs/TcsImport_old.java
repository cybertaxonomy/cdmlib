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

import org.apache.log4j.Logger;
import org.jdom.Element;
import org.springframework.stereotype.Service;

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.api.service.IService;
import eu.etaxonomy.cdm.database.DataSourceNotFoundException;
import eu.etaxonomy.cdm.io.common.ICdmImport;
import eu.etaxonomy.cdm.io.common.IIO;
import eu.etaxonomy.cdm.io.common.IImportConfigurator;
import eu.etaxonomy.cdm.io.common.MapWrapper;
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
@Service
public class TcsImport_old<T extends TcsImportConfigurator> implements ICdmImport<T> {
	private static Logger logger = Logger.getLogger(TcsImport_old.class);
	
	
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
	
	
	protected boolean doCheck(TcsImportConfigurator config){
		boolean result = true;
		System.out.println("Start check TcsImport_old ("+ config.getSource().toString() + ") ...");
		
		//check
		if (config == null){
			logger.warn("BerlinModelImportConfiguration is null");
			return false;
		}else if (! config.isValid()){
			logger.warn("BerlinModelImportConfiguration is not valid");
			return false;
		}
		
		IIO<IImportConfigurator> iio;
		
//		//check Authors
//		if (iConfig.isDoAuthors()){
//			result &= TcsAuthorIO.check(iConfig);
//		}

		//check References
		if (config.getDoReferences() != NONE){
			iio = config.getReferenceIO();
			if (iio != null){
				result &= iio.check(config);
			}
		}
		
		//check TaxonNames
		if (config.isDoTaxonNames()){
			iio = config.getTaxonNameIO();
			if (iio != null){
				result &= iio.check(config);
			}
		}
		
		//check RelNames
		if(config.isDoRelNames()){
			iio = config.getTaxonNameRelationIO();
			if (iio != null){
				result &= iio.check(config);
			}
		}

//		//check types
//		if(iConfig.isDoTypes()){
//			result &= BerlinModelTypesIO.check(iConfig);
//		}
	
		//check Taxa
		if(config.isDoTaxa()){
			iio = config.getTaxonIO();
			if (iio != null){
				result &= iio.check(config);
			}
		}
		
		//check RelPTaxa
		if(config.isDoRelTaxa()){
			iio = config.getTaxonRelationIO();
			if (iio != null){
				result &= iio.check(config);
			}
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
		System.out.println("End checking BerlinModel ("+ config.getSource().toString() + ") for import to CDM");
		return result;

	}
	
	
	/**
	 * Executes the whole 
	 */
	protected boolean doImport(TcsImportConfigurator config){
		CdmApplicationController cdmApp;
		if (config == null){
			logger.warn("BerlinModelImportConfiguration is null");
			return false;
		}else if (! config.isValid()){
			logger.warn("BerlinModelImportConfiguration is not valid");
			return false;
		}
		try {
			cdmApp = CdmApplicationController.NewInstance(config.getDestination(), config.getDbSchemaValidation());
		} catch (DataSourceNotFoundException e) {
			logger.warn("could not connect to destination database");
			return false;
		}catch (TermNotFoundException e) {
			logger.warn("could not find needed term in destination datasource");
			return false;
		}
		Element source = config.getSourceRoot();
		ReferenceBase sourceReference = config.getSourceReference();
		System.out.println("Start import from BerlinModel ("+ config.getSource().toString() + ") to Cdm  (" + cdmApp.getDatabaseService().getUrl() + ") ...");
		
		IIO iio;
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
		if (config.getDoReferences() != NONE){
			iio = config.getReferenceIO();
			if (iio != null && ! iio.invoke(config, cdmApp, new MapWrapper[]{referenceStore, authorStore})){
				return false;
			}
		}else{
			logger.warn("No References imported");
			//referenceStore = null;
		}
		
		//TaxonNames
		if (config.isDoTaxonNames()){
			iio = config.getTaxonNameIO();
			if (iio != null && ! iio.invoke(config, cdmApp, new MapWrapper[]{taxonNameStore, referenceStore, authorStore})){
				//return false;
			}

		}else{
			logger.warn("No TaxonNames imported");
			//taxonNameStore = null;
		}

		//make and save RelNames
		if(config.isDoRelNames()){
			iio = config.getTaxonNameRelationIO();
			if (iio != null && ! iio.invoke(config, cdmApp, new MapWrapper[]{taxonNameStore, referenceStore})){
				return false;
			}
		}else{
			logger.warn("No RelNames imported");
		}

		//make and save Taxa
		if(config.isDoTaxa()){
			iio = config.getTaxonIO();
			if (iio != null && ! iio.invoke(config, cdmApp, new MapWrapper[]{ taxonStore, taxonNameStore, referenceStore})){
				return false;
			}
		}else{
			logger.warn("No Taxa imported");
			taxonNameStore = null;
		}
		
		//make and save RelPTaxa
		if(config.isDoRelTaxa()){
			iio = config.getTaxonRelationIO();
			if(iio != null && ! iio.invoke(config, cdmApp, new MapWrapper[]{taxonStore, referenceStore})){
				return false;
			}
		}else{
			logger.warn("No RelPTaxa imported");
		}
//		
//		//make and save Facts
//		if(tcsConfig.isDoFacts()){
//			if (! TaxonXDescriptionIO.invoke(tcsConfig, cdmApp, taxonStore, referenceStore)){
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
