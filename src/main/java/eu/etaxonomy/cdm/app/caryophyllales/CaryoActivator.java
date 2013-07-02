/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.app.caryophyllales;

import java.util.UUID;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.api.application.ICdmApplicationConfiguration;
import eu.etaxonomy.cdm.app.berlinModelImport.SourceBase;
import eu.etaxonomy.cdm.app.common.CdmDestinations;
import eu.etaxonomy.cdm.database.DbSchemaValidation;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.caryo.CaryoImportConfigurator;
import eu.etaxonomy.cdm.io.common.CdmDefaultImport;
import eu.etaxonomy.cdm.io.common.IImportConfigurator.CHECK;
import eu.etaxonomy.cdm.io.common.Source;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;

/**
 * @author a.mueller
 * @created 16.10.2012
 * @version 1.0
 */
public class CaryoActivator extends SourceBase{
	private static final Logger logger = Logger.getLogger(CaryoActivator.class);
	
	//database validation status (create, update, validate ...)
	static final DbSchemaValidation hbm2dll = DbSchemaValidation.CREATE;
	static final Source source = caryo_len61();

	
	static final ICdmDataSource cdmDestination = CdmDestinations.localH2();
//	static final ICdmDataSource cdmDestination = CdmDestinations.cdm_test_local_mysql();
//	static final ICdmDataSource cdmDestination = CdmDestinations.cdm_local_postgres_CdmTest();
	
	static final String classificationName = "Caryophyllales";
	
	//classification
	static final UUID classificationUuid = UUID.fromString("9edc58b5-de3b-43aa-9f31-1ede7c009c2b");
	
	//check - import
	static final CHECK check = CHECK.IMPORT_WITHOUT_CHECK;
	
	//taxa
	static final boolean doTaxa = true;
	static final boolean doDeduplicate = true;
	
	
	private void doImport(ICdmDataSource cdmDestination){
		
		//make Source
		CaryoImportConfigurator config= CaryoImportConfigurator.NewInstance(source, cdmDestination);
		config.setClassificationUuid(classificationUuid);
		config.setCheck(check);
		config.setDoTaxa(doTaxa);
		config.setDbSchemaValidation(hbm2dll);
		config.setSourceReferenceTitle("NCU - Caryophyllales, v0.4");
		config.setClassificationName(classificationName);
		
		CdmDefaultImport<CaryoImportConfigurator> myImport = new CdmDefaultImport<CaryoImportConfigurator>();

		
		//...
		if (true){
			System.out.println("Start import from ("+ source.toString() + ") ...");
			config.setSourceReference(getSourceReference(config.getSourceReferenceTitle()));
			myImport.invoke(config);
			System.out.println("End import from ("+ source.toString() + ")...");
		}
		
		
		
		//deduplicate
		if (doDeduplicate){
			ICdmApplicationConfiguration app = myImport.getCdmAppController();
			int count = app.getAgentService().deduplicate(Person.class, null, null);
			logger.warn("Deduplicated " + count + " persons.");
//			count = app.getAgentService().deduplicate(Team.class, null, null);
//			logger.warn("Deduplicated " + count + " teams.");
			count = app.getReferenceService().deduplicate(Reference.class, null, null);
			logger.warn("Deduplicated " + count + " references.");
		}
		
	}

	private Reference<?> getSourceReference(String string) {
		Reference<?> result = ReferenceFactory.newGeneric();
		result.setTitleCache(string);
		return result;
	}


	public static Source caryo_len61(){
		//	Cyryophyllales source
		String dbms = Source.SQL_SERVER_2008;
		String strServer = "Lenovo-T61";
		String strDB = "caryo";
		int port = 1433;
		String userName = "WebUser";
		return  makeSource(dbms, strServer, strDB, port, userName, null);
	}



	/**
	 * @param args
	 */
	public static void main(String[] args) {
		CaryoActivator me = new CaryoActivator();
		me.doImport(cdmDestination);
	}
	
}
