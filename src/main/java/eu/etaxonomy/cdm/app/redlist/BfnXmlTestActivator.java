/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.app.redlist;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.UUID;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.app.common.CdmDestinations;
import eu.etaxonomy.cdm.app.common.CdmImportSources;
import eu.etaxonomy.cdm.app.exel.NormalExplicitTestActivator;
import eu.etaxonomy.cdm.database.DbSchemaValidation;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.common.CdmDefaultImport;
import eu.etaxonomy.cdm.io.common.Source;
import eu.etaxonomy.cdm.io.common.IImportConfigurator.CHECK;
import eu.etaxonomy.cdm.io.common.IImportConfigurator.DO_REFERENCES;
import eu.etaxonomy.cdm.io.redlist.bfnXml.BfnXmlImportConfigurator;
import eu.etaxonomy.cdm.io.tcsxml.in.TcsXmlImportConfigurator;

/**
 * @author a.oppermann
 * @created 16.07.2013
 * @version 1.0
 */
public class BfnXmlTestActivator {
	private static final Logger logger = Logger.getLogger(BfnXmlTestActivator.class);
	
	//database validation status (create, update, validate ...)
	static DbSchemaValidation hbm2dll = DbSchemaValidation.CREATE;
//	static final String tcsSource = TcsSources.tcsXml_cichorium();
//	static final ICdmDataSource cdmDestination = CdmDestinations.cdm_test_andreasM();
	static final ICdmDataSource cdmDestination = CdmDestinations.cdm_redlist_localhost();
//	static final ICdmDataSource cdmDestination = CdmDestinations.cdm_local_postgres_CdmTest();


	static final UUID treeUuid = UUID.fromString("00000000-0c97-48ac-8d33-6099ed68c625");
	static final String sourceSecId = "TestBfn";
	
	private static final String strSource = "/eu/etaxonomy/cdm/io/bfnXml/rldb_Myxo.xml";
	
	static final boolean includeNormalExplicit = true; 
	
	//check - import
	static final CHECK check = CHECK.CHECK_AND_IMPORT;
	
	//authors
	static final boolean doMetaData = false;
	//references
	static final DO_REFERENCES doReferences =  DO_REFERENCES.ALL;
	//names
	static final boolean doTaxonNames = true;
	static final boolean doRelNames = false;
	
	//taxa
	static final boolean doTaxa = true;
	static final boolean doRelTaxa = false;

	
	private void doImport(){
		System.out.println("Start import from BfnXML to "+ cdmDestination.getDatabase() + " ...");
		
		//make Source
		URI source;
		try {
			source = this.getClass().getResource(strSource).toURI();
			ICdmDataSource destination = cdmDestination;
			
			BfnXmlImportConfigurator bfnImportConfigurator = BfnXmlImportConfigurator.NewInstance(source,  destination);
			
			bfnImportConfigurator.setClassificationName("RoteListe Myxno");
			bfnImportConfigurator.setClassificationUuid(treeUuid);
			bfnImportConfigurator.setSourceSecId(sourceSecId);
			
			bfnImportConfigurator.setDoMetaData(doMetaData);
			bfnImportConfigurator.setDoReferences(doReferences);
			bfnImportConfigurator.setDoTaxonNames(doTaxonNames);
			bfnImportConfigurator.setDoRelNames(doRelNames);
			
			bfnImportConfigurator.setDoTaxa(doTaxa);
			bfnImportConfigurator.setDoRelTaxa(doRelTaxa);
			
			bfnImportConfigurator.setCheck(check);
			bfnImportConfigurator.setDbSchemaValidation(hbm2dll);
	
			// invoke import
			CdmDefaultImport<BfnXmlImportConfigurator> bfnImport = new CdmDefaultImport<BfnXmlImportConfigurator>();
			//new Test().invoke(tcsImportConfigurator);
			bfnImport.invoke(bfnImportConfigurator);
			
			
//			IReferenceService refService = tcsImport.getCdmAppController().getReferenceService();
//			IBook book = ReferenceFactory.newBook();
//			book.setDatePublished(TimePeriod.NewInstance(1945).setEndDay(12).setEndMonth(4));
//			refService.saveOrUpdate((Reference)book);
//			tcsImport.getCdmAppController().close();
			
//			NormalExplicitTestActivator normExActivator = new NormalExplicitTestActivator();
//			normExActivator.doImport(destination, DbSchemaValidation.VALIDATE);
//			
			logger.info("End");
			System.out.println("End import from TCS ("+ source.toString() + ")...");
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		BfnXmlTestActivator bfnXmlTestActivator = new BfnXmlTestActivator();
		bfnXmlTestActivator.doImport();
	}
	
}
