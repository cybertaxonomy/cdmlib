/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.app.cyprus;

import java.net.URI;
import java.net.URISyntaxException;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.app.common.CdmDestinations;
import eu.etaxonomy.cdm.database.DbSchemaValidation;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.common.CdmDefaultImport;
import eu.etaxonomy.cdm.io.common.IImportConfigurator.CHECK;
import eu.etaxonomy.cdm.io.specimen.excel.in.SpecimenCdmExcelImportConfigurator;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;

/**
 * @author a.mueller
 * @created 16.12.2010
 * @version 1.0
 */
public class CyprusSpecimenActivator {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(CyprusSpecimenActivator.class);
	
	//database validation status (create, update, validate ...)
	static DbSchemaValidation hbm2dll = DbSchemaValidation.VALIDATE;
	static final URI source = cyprus_specimen_local();

	
//	static final ICdmDataSource cdmDestination = CdmDestinations.localH2();
//	static final ICdmDataSource cdmDestination = CdmDestinations.cdm_test_local_mysql();
	static final ICdmDataSource cdmDestination = CdmDestinations.cdm_cyprus_dev();
//	static final ICdmDataSource cdmDestination = CdmDestinations.cdm_cyprus_production();

	
	//check - import
	static final CHECK check = CHECK.IMPORT_WITHOUT_CHECK;
	
	//taxa
//	static final boolean doTaxa = false;
//	static final boolean doDeduplicate = false;
//	static final boolean doDistribution = true;

	
	private void doImport(ICdmDataSource cdmDestination){
		
		//make Source
		SpecimenCdmExcelImportConfigurator config= SpecimenCdmExcelImportConfigurator.NewInstance(source, cdmDestination);
		config.setCheck(check);
		config.setDbSchemaValidation(hbm2dll);
		
		CdmDefaultImport myImport = new CdmDefaultImport();

		
		//...
		if (true){
			System.out.println("Start import from ("+ source.toString() + ") ...");
			config.setSourceReference(getSourceReference(config.getSourceReferenceTitle()));
			myImport.invoke(config);

			System.out.println("End import from ("+ source.toString() + ")...");
		}
		
	}

	private Reference<?> getSourceReference(String string) {
		Reference<?> result = ReferenceFactory.newGeneric();
		result.setTitleCache(string);
		return result;
	}
	
	//Cyprus
	public static URI cyprus_specimen_local() {
		URI sourceUrl;
		try {
			sourceUrl = new URI("file:/C:/localCopy/Data/zypern/Cyprus-specimens-import02_AM.xls");
			return sourceUrl;
		} catch (URISyntaxException e) {
			e.printStackTrace();
			return null;
		}
	}

	

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		CyprusSpecimenActivator me = new CyprusSpecimenActivator();
		me.doImport(cdmDestination);
	}
	
}
