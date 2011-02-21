/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.app.synthesysImport;

import java.net.URI;
import java.util.UUID;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.app.common.CdmDestinations;
import eu.etaxonomy.cdm.app.common.CdmImportSources;
import eu.etaxonomy.cdm.app.tcs.TcsSources;
import eu.etaxonomy.cdm.database.DbSchemaValidation;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.common.CdmDefaultImport;
import eu.etaxonomy.cdm.io.common.IImportConfigurator.CHECK;
import eu.etaxonomy.cdm.io.common.IImportConfigurator.DO_REFERENCES;
import eu.etaxonomy.cdm.io.excel.taxa.NormalExplicitImportConfigurator;
import eu.etaxonomy.cdm.io.specimen.excel.in.SpecimenExcelImportConfigurator;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;

/**
 * @author a.mueller
 * @created 20.06.2008
 * @version 1.0
 */
public class SynthesysSpecimenTestActivator {
	private static final Logger logger = Logger.getLogger(SynthesysSpecimenTestActivator.class);
	
	//database validation status (create, update, validate ...)
	static DbSchemaValidation hbm2dll = DbSchemaValidation.CREATE;
//	static final String mySources = TcsSources.taxonX_local();
	static final URI mySource = CdmImportSources.SYNTHESYS_SPECIMEN();
//	static final ICdmDataSource cdmDestination = CdmDestinations.cdm_local_tdwg2010();
	static final ICdmDataSource cdmDestination = CdmDestinations.localH2();
//	static final ICdmDataSource cdmDestination = CdmDestinations.cdm_local_postgres_CdmTest();

	static final UUID treeUuid = UUID.fromString("190d9456-12e8-47a4-a235-bab36adb247c");
	static final String sourceSecId = "TestSynthesysSpecimen";
	
	//check - import
	static final CHECK check = CHECK.CHECK_AND_IMPORT;
	
	static final boolean doMatchTaxa = true;
	
	//references
	static final DO_REFERENCES doReferences =  DO_REFERENCES.ALL;
	//names
	static final boolean doTaxonNames = true;
	static final boolean doRelNames = true;
	
	//taxa
	static final boolean doTaxa = true;
	static final boolean doRelTaxa = true;

	
	public void doImport(ICdmDataSource destination, DbSchemaValidation hbm2dll){
		System.out.println("Start import from Tcs("+ mySource.toString() + ") ...");
		
		SpecimenExcelImportConfigurator config = SpecimenExcelImportConfigurator.NewInstance(mySource,  destination);
		
		config.setClassificationUuid(treeUuid);
		config.setSourceSecId(sourceSecId);
		
//		config.setDoReferences(doReferences);
//		config.setDoTaxonNames(doTaxonNames);
//		
//		config.setDoMatchTaxa(doMatchTaxa);
//		config.setDoTaxa(doTaxa);
//		config.setDoRelTaxa(doRelTaxa);
		
		config.setCheck(check);
		config.setDbSchemaValidation(hbm2dll);

		// invoke import
		CdmDefaultImport<SpecimenExcelImportConfigurator> myImport = new CdmDefaultImport<SpecimenExcelImportConfigurator>();
		//new Test().invoke(tcsImportConfigurator);
		myImport.invoke(config);
		
		
		logger.info("End");
		System.out.println("End import from Normal Explicit ("+ mySource.toString() + ")...");
		
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		SynthesysSpecimenTestActivator me = new SynthesysSpecimenTestActivator();
		me.doImport(cdmDestination, hbm2dll);
	}
	
}
