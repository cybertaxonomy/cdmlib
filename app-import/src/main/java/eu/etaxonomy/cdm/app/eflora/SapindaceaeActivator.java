/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.app.eflora;

import java.util.UUID;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.app.common.CdmDestinations;
import eu.etaxonomy.cdm.database.DbSchemaValidation;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.common.CdmDefaultImport;
import eu.etaxonomy.cdm.io.common.IImportConfigurator.CHECK;
import eu.etaxonomy.cdm.io.eflora.sapindaceae.SapindaceaeImportConfigurator;

/**
 * @author a.mueller
 * @created 20.06.2008
 * @version 1.0
 */
public class SapindaceaeActivator {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(SapindaceaeActivator.class);
	
	//database validation status (create, update, validate ...)
	static DbSchemaValidation hbm2dll = DbSchemaValidation.CREATE;
	static final String sapSource1 = EfloraSources.sapindaceae_local();
	static final String sapSource2 = EfloraSources.sapindaceae2_local();
	
//	static final ICdmDataSource cdmDestination = CdmDestinations.cdm_test_andreasM2();
//	static final ICdmDataSource cdmDestination = CdmDestinations.cdm_flora_malesiana_preview();
//	static final ICdmDataSource cdmDestination = CdmDestinations.cdm_flora_malesiana_production();
	static final ICdmDataSource cdmDestination = CdmDestinations.localH2();

	static final UUID treeUuid = UUID.fromString("ca4e4bcb-a1d1-4124-a358-a3d3c41dd450");
	
	//check - import
	static final CHECK check = CHECK.IMPORT_WITHOUT_CHECK;
	
	//taxa
	static final boolean doTaxa = true;

	private boolean includeSapindaceae1 = true;
	private boolean includeSapindaceae2 = true;

	
	private void doImport(ICdmDataSource cdmDestination){
		
		//make BerlinModel Source
		String source = sapSource1;
		SapindaceaeImportConfigurator sapindaceaeConfig= SapindaceaeImportConfigurator.NewInstance(source, cdmDestination);
		sapindaceaeConfig.setTaxonomicTreeUuid(treeUuid);
		sapindaceaeConfig.setDoTaxa(doTaxa);
		sapindaceaeConfig.setCheck(check);
		sapindaceaeConfig.setDbSchemaValidation(hbm2dll);
		
		CdmDefaultImport<SapindaceaeImportConfigurator> myImport = new CdmDefaultImport<SapindaceaeImportConfigurator>();

		
		//Sapindaceae1
		if (includeSapindaceae1){
			System.out.println("Start import from ("+ sapSource1.toString() + ") ...");
			myImport.invoke(sapindaceaeConfig);
			System.out.println("End import from ("+ sapSource1.toString() + ")...");
		}
		
		//Sapindaceae2
		if (includeSapindaceae2){
			System.out.println("Start import from ("+ sapSource2.toString() + ") ...");
			source = sapSource2;
			sapindaceaeConfig.setSource(source);
			myImport.invoke(sapindaceaeConfig);
			System.out.println("End import from ("+ sapSource2.toString() + ")...");
		}
		
//		IReferenceService refService = myImport.getCdmAppController().getReferenceService();
//		ReferenceFactory refFactory = ReferenceFactory.newInstance();
//		IBook book = refFactory.newBook();
//		//book.setDatePublished(TimePeriod.NewInstance(1945));
//		book.setDatePublished(TimePeriod.NewInstance(1945).setEndDay(12).setEndMonth(4));
//		refService.saveOrUpdate((ReferenceBase)book);
//		myImport.getCdmAppController().close();
//		logger.info("End");

	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		SapindaceaeActivator me = new SapindaceaeActivator();
		me.doImport(cdmDestination);
	}
	
}
