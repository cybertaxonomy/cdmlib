/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.app.tcs;

import java.util.UUID;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.app.common.CdmDestinations;
import eu.etaxonomy.cdm.database.DbSchemaValidation;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.common.ICdmImport;
import eu.etaxonomy.cdm.io.common.IImportConfigurator;
import eu.etaxonomy.cdm.io.common.IImportConfigurator.CHECK;
import eu.etaxonomy.cdm.io.common.IImportConfigurator.DO_REFERENCES;
import eu.etaxonomy.cdm.io.common.CdmDefaultImport;
import eu.etaxonomy.cdm.io.taxonx.TaxonXImportConfigurator;

/**
 * @author a.mueller
 * @created 20.06.2008
 * @version 1.0
 */
public class TaxonXImportActivator {
	private static Logger logger = Logger.getLogger(TaxonXImportActivator.class);
	
	//database validation status (create, update, validate ...)
	static DbSchemaValidation hbm2dll = DbSchemaValidation.CREATE;
	static final String tcsSource = TcsSources.taxonX_local();
	static final ICdmDataSource cdmDestination = CdmDestinations.localH2();
	
	static final UUID secUuid = UUID.fromString("5f32b8af-0c97-48ac-8d33-6099ed68c625");
	static final int sourceSecId = 7800000;
	
	//check - import
	static final CHECK check = CHECK.CHECK_AND_IMPORT;
	
	//authors
	static final boolean doAuthors = false;
	//references
	static final DO_REFERENCES doReferences =  DO_REFERENCES.NONE;
	//names
	static final boolean doTaxonNames = false;
	static final boolean doRelNames = false;
	static final boolean doTypes = false;
	static final boolean doNameFacts = false;
	
	//taxa
	static final boolean doTaxa = false;
	static final boolean doRelTaxa = false;
	static final boolean doDescriptions = false;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println("Start import from Source("+ tcsSource.toString() + ") ...");
		
		//make BerlinModel Source
		String source = tcsSource;
		ICdmDataSource destination = cdmDestination;
		
		TaxonXImportConfigurator taxonXImportConfigurator = TaxonXImportConfigurator.NewInstance(source, destination);
		// invoke import
		ICdmImport<IImportConfigurator> cdmImport = new CdmDefaultImport<IImportConfigurator>();
		
		taxonXImportConfigurator.setSecUuid(secUuid);
		taxonXImportConfigurator.setSourceSecId(sourceSecId);
		
		taxonXImportConfigurator.setDoAuthors(doAuthors);
		taxonXImportConfigurator.setDoReferences(doReferences);
		taxonXImportConfigurator.setDoTaxonNames(doTaxonNames);
		taxonXImportConfigurator.setDoRelNames(doRelNames);
		//tcsImportConfigurator.setDoNameStatus(doNameStatus);
		taxonXImportConfigurator.setDoTypes(doTypes);
		taxonXImportConfigurator.setDoNameFacts(doNameFacts);
		
		taxonXImportConfigurator.setDoTaxa(doTaxa);
		taxonXImportConfigurator.setDoRelTaxa(doRelTaxa);
		taxonXImportConfigurator.setDoFacts(doDescriptions);
		
		taxonXImportConfigurator.setCheck(check);
		taxonXImportConfigurator.setDbSchemaValidation(hbm2dll);

		//new Test().invoke(tcsImportConfigurator);
		cdmImport.invoke(taxonXImportConfigurator);
		System.out.println("End import from Source ("+ source.toString() + ")...");
	}

	
}
