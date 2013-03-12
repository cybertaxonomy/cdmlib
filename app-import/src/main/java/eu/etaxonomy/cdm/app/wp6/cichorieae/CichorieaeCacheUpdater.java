/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.app.wp6.cichorieae;

import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.app.common.CdmDestinations;
import eu.etaxonomy.cdm.database.DbSchemaValidation;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.common.CacheUpdaterConfigurator;
import eu.etaxonomy.cdm.io.common.CdmDefaultImport;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;


/**
 * TODO add the following to a wiki page:
 * HINT: If you are about to import into a mysql data base running under windows and if you wish to dump and restore the resulting data bas under another operation systen 
 * you must set the mysql system variable lower_case_table_names = 0 in order to create data base with table compatible names.
 * 
 * 
 * @author a.mueller
 *
 */
public class CichorieaeCacheUpdater {
	private static final Logger logger = Logger.getLogger(CichorieaeCacheUpdater.class);

	//database validation status (create, update, validate ...)
	static DbSchemaValidation hbm2dll = DbSchemaValidation.VALIDATE;
	static final ICdmDataSource cdmDestination = CdmDestinations.cdm_production_cichorieae();

	static final List<String> classListStrings =  Arrays.asList(new String[]{
			//IdentifiableEntity.class.getName(),
//			IdentifiableEntity.class.getName(),
			TaxonNameBase.class.getName(),
			TaxonBase.class.getName()
	});
	//new ArrayList<Class<? extends IdentifiableEntity>>();

// **************** ALL *********************	

//	//DescriptionBase
//	static final boolean doTaxonDescription = true;
//	static final boolean doSpecimenDescription = true;
//	static final boolean doNameDescription = true;
//	
//	//AgentBase
//	static final boolean doPerson = true;
//	static final boolean doTeam = true;
//	static final boolean doInstitution = true;
//	
//	//MediaEntities
//	static final boolean doCollection = true;
//	static final boolean doReferenceBase = true;
//	
//	//SpecimenOrObservationBase
//	static final boolean doFieldObservation = true;
//	static final boolean doDeriveUnit = true;
//	static final boolean doLivingBeing = true;
//	static final boolean doObservation = true;
//	static final boolean doSpecimen = true;
//	
//	//Media
//	static final boolean doMedia = true;
//	static final boolean doMediaKey = true;
//	static final boolean doFigure = true;
//	static final boolean doPhylogenticTree = true;
//	
//	
//	//TaxonBase
//	static final boolean doTaxon = true;
//	static final boolean doSynonym = true;
//	
//	static final boolean doSequence = true;
//	
//	//Names
//	static final boolean doViralName = true;
//	static final boolean doNonViralName = true;
//	static final boolean doBotanicalName = true;
//	static final boolean doZoologicalName = true;
//	static final boolean doCultivarPlantName = true;
//	
//	static final boolean doClassification = true;
//	
//	//TermBase
//	static final boolean doFeatureTree = true;
//	static final boolean doPolytomousKey = true;
//	
//	static final boolean doTermVocabulary = true;
//	static final boolean doDefinedTermBase = true;
//	
	
	
	private boolean doInvoke(ICdmDataSource destination){
		boolean success = true;

		CacheUpdaterConfigurator config;
		try {
			config = CacheUpdaterConfigurator.NewInstance(destination, classListStrings);
			
			// invoke import
			CdmDefaultImport<CacheUpdaterConfigurator> myImport = new CdmDefaultImport<CacheUpdaterConfigurator>();
			success &= myImport.invoke(config);
			String successString = success ? "successful" : " with errors ";
			System.out.println("End updating caches for "+ destination.getDatabase() + "..." +  successString);
			return success;
		} catch (ClassNotFoundException e) {
			logger.error(e);
			return false;
		}		
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		ICdmDataSource destination = CdmDestinations.chooseDestination(args) != null ? CdmDestinations.chooseDestination(args) : cdmDestination;
		
		System.out.println("Start updating caches for "+ destination.getDatabase() + "...");
		CichorieaeCacheUpdater me = new CichorieaeCacheUpdater();
		me.doInvoke(destination);
		
	}

}
