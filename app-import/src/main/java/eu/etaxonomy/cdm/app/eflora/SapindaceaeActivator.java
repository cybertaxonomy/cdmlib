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

import eu.etaxonomy.cdm.api.service.IReferenceService;
import eu.etaxonomy.cdm.app.common.CdmDestinations;
import eu.etaxonomy.cdm.database.DbSchemaValidation;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.common.CdmDefaultImport;
import eu.etaxonomy.cdm.io.common.IImportConfigurator.CHECK;
import eu.etaxonomy.cdm.io.common.IImportConfigurator.DO_REFERENCES;
import eu.etaxonomy.cdm.io.eflora.sapindaceae.SapindaceaeImportConfigurator;
import eu.etaxonomy.cdm.model.common.TimePeriod;
import eu.etaxonomy.cdm.model.reference.IBook;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;

/**
 * @author a.mueller
 * @created 20.06.2008
 * @version 1.0
 */
public class SapindaceaeActivator {
	private static final Logger logger = Logger.getLogger(SapindaceaeActivator.class);
	
	//database validation status (create, update, validate ...)
	static DbSchemaValidation hbm2dll = DbSchemaValidation.CREATE;
	static final String sapSource = EfloraSources.sapindaceae_local();
	static final ICdmDataSource cdmDestination = CdmDestinations.cdm_test_andreasM2();
//	static final ICdmDataSource cdmDestination = CdmDestinations.localH2();

	static final UUID treeUuid = UUID.fromString("ca4e4bcb-a1d1-4124-a358-a3d3c41dd450");
	
	//check - import
	static final CHECK check = CHECK.CHECK_AND_IMPORT;
	
	//authors
	static final boolean doMetaData = true;
	//references
	static final DO_REFERENCES doReferences =  DO_REFERENCES.ALL;
	//names
	static final boolean doTaxonNames = true;
	static final boolean doRelNames = true;
	
	//taxa
	static final boolean doTaxa = true;
	static final boolean doRelTaxa = true;

	
	private void doImport(){
		System.out.println("Start import from ("+ sapSource.toString() + ") ...");
		
		//make BerlinModel Source
		String source = sapSource;
		ICdmDataSource destination = cdmDestination;
		
		SapindaceaeImportConfigurator sapindaceaeImportConfigurator = SapindaceaeImportConfigurator.NewInstance(source,  destination);
		
		sapindaceaeImportConfigurator.setTaxonomicTreeUuid(treeUuid);
		
//		sapindaceaeImportConfigurator.setDoMetaData(doMetaData);
		sapindaceaeImportConfigurator.setDoReferences(doReferences);
		sapindaceaeImportConfigurator.setDoTaxonNames(doTaxonNames);
		sapindaceaeImportConfigurator.setDoRelNames(doRelNames);
		
		sapindaceaeImportConfigurator.setDoTaxa(doTaxa);
		sapindaceaeImportConfigurator.setDoRelTaxa(doRelTaxa);
		
		sapindaceaeImportConfigurator.setCheck(check);
		sapindaceaeImportConfigurator.setDbSchemaValidation(hbm2dll);

		// invoke import
		CdmDefaultImport<SapindaceaeImportConfigurator> myImport = new CdmDefaultImport<SapindaceaeImportConfigurator>();
		myImport.invoke(sapindaceaeImportConfigurator);
		
		
//		IReferenceService refService = myImport.getCdmAppController().getReferenceService();
//		ReferenceFactory refFactory = ReferenceFactory.newInstance();
//		IBook book = refFactory.newBook();
//		//book.setDatePublished(TimePeriod.NewInstance(1945));
//		book.setDatePublished(TimePeriod.NewInstance(1945).setEndDay(12).setEndMonth(4));
//		refService.saveOrUpdate((ReferenceBase)book);
//		myImport.getCdmAppController().close();
//		logger.info("End");
		System.out.println("End import from ("+ sapSource.toString() + ")...");
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		SapindaceaeActivator me = new SapindaceaeActivator();
		me.doImport();
	}
	
}
