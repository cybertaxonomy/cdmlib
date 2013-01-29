/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.app.exel;

import java.net.URI;
import java.util.UUID;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.app.common.CdmDestinations;
import eu.etaxonomy.cdm.app.tcs.TcsSources;
import eu.etaxonomy.cdm.database.DbSchemaValidation;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.common.CdmDefaultImport;
import eu.etaxonomy.cdm.io.common.IImportConfigurator.CHECK;
import eu.etaxonomy.cdm.io.common.IImportConfigurator.DO_REFERENCES;
import eu.etaxonomy.cdm.io.excel.taxa.NormalExplicitImportConfigurator;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;

/**
 * @author a.mueller
 * @created 20.06.2008
 * @version 1.0
 */
public class NormalExplicitTestActivator {
	private static final Logger logger = Logger.getLogger(NormalExplicitTestActivator.class);
	
	//database validation status (create, update, validate ...)
	static DbSchemaValidation hbm2dll = DbSchemaValidation.CREATE;
//	static final String mySources = TcsSources.taxonX_local();
	static final URI mySource = TcsSources.normalExplicit();
//	static final ICdmDataSource cdmDestination = CdmDestinations.cdm_local_tdwg2010();
	static final ICdmDataSource cdmDestination = CdmDestinations.localH2();
//	static final ICdmDataSource cdmDestination = CdmDestinations.cdm_local_postgres_CdmTest();

	static final UUID treeUuid = UUID.fromString("00505000-0c97-48ac-8d33-6099ed68c625");
	static final String sourceSecId = "TestNormalExplicit";
	
	//check - import
	static final CHECK check = CHECK.CHECK_AND_IMPORT;
	
	static final boolean doMatchTaxa = true;
	
	//references
	static final DO_REFERENCES doReferences =  DO_REFERENCES.ALL;
	//names
	static final boolean doTaxonNames = true;
	
	//taxa
	static final boolean doTaxa = true;
	static final boolean doRelTaxa = true;

	
	public void doImport(ICdmDataSource destination, DbSchemaValidation hbm2dll){
		System.out.println("Start import from Excel("+ mySource.toString() + ") ...");
		
		NormalExplicitImportConfigurator config = NormalExplicitImportConfigurator.NewInstance(mySource,  destination, NomenclaturalCode.ICBN, null);
		
		config.setClassificationUuid(treeUuid);
		config.setSourceSecId(sourceSecId);
		
		config.setDoMatchTaxa(doMatchTaxa);
		
		config.setCheck(check);
		config.setDbSchemaValidation(hbm2dll);

		// invoke import
		CdmDefaultImport<NormalExplicitImportConfigurator> myImport = new CdmDefaultImport<NormalExplicitImportConfigurator>();
		//new Test().invoke(tcsImportConfigurator);
		myImport.invoke(config);
		
		
		logger.info("End");
		System.out.println("End import from Normal Explicit ("+ mySource.toString() + ")...");
		
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		NormalExplicitTestActivator me = new NormalExplicitTestActivator();
		me.doImport(cdmDestination, hbm2dll);
	}
	
}
