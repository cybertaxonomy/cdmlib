/**
 * Copyright (C) 2007 EDIT
 * European Distributed Institute of Taxonomy 
 * http://www.e-taxonomy.eu
 * 
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */

package eu.etaxonomy.cdm.app.excelImport.taxa;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.app.util.TestDatabase;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.common.CdmDefaultImport;
import eu.etaxonomy.cdm.io.excel.taxa.NormalExplicitImportConfigurator;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;

/**
 * @author a.babadshanjan
 * @created 06.01.2009
 *
 */
public class NormalExplicitActivator {

	private static final String dbName = "cdm_test_anahit";
	private static String fileName = 
		new String( System.getProperty("user.home") + "\\Eigene Dateien\\archive\\dbImport\\gbif_test\\NormalExplicit.xls");
	
	private static final ICdmDataSource destinationDb = TestDatabase.CDM_DB(dbName);
    private static final Logger logger = Logger.getLogger(NormalExplicitActivator.class);
    
    public static void main(String[] args) {

    	NormalExplicitImportConfigurator normalExplicitImportConfigurator = 
    		NormalExplicitImportConfigurator.NewInstance(fileName, destinationDb);

    	normalExplicitImportConfigurator.setNomenclaturalCode(NomenclaturalCode.ICBN);
    	
		CdmDefaultImport<NormalExplicitImportConfigurator> normalExplicitImport = 
			new CdmDefaultImport<NormalExplicitImportConfigurator>();

		// invoke import
		logger.debug("Invoking Normal Explicit Excel import");
		normalExplicitImport.invoke(normalExplicitImportConfigurator);
    	
    }
}
