/**
 * Copyright (C) 2007 EDIT
 * European Distributed Institute of Taxonomy 
 * http://www.e-taxonomy.eu
 * 
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */

package eu.etaxonomy.cdm.app.excelImport.taxa;

import java.io.File;
import java.net.URI;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.app.common.CdmDestinations;
import eu.etaxonomy.cdm.database.DbSchemaValidation;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.common.CdmDefaultImport;
import eu.etaxonomy.cdm.io.excel.taxa.NormalExplicitImportConfigurator;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;

/**
 * Activator for palms use summary excel import
 * 
 * @author a.mueller
 * @created 18.07.2011
 *
 */
public class UseSummaryActivator {
	private static final Logger logger = Logger.getLogger(UseSummaryActivator.class);
    
	private static String fileName = new String("C:\\tmp\\temp\\NormalExplicit.xls");
	private static DbSchemaValidation dbSchemaValidation = DbSchemaValidation.VALIDATE;

	private static final ICdmDataSource destinationDb = CdmDestinations.cdm_test_useSummary();
//	private static final ICdmDataSource destinationDb = CdmDestinations.cdm_production_palmae();
    
    public static void main(String[] args) {

    	NomenclaturalCode code = NomenclaturalCode.ICBN;
    	URI uri;
//		try {
			File file = new File(fileName);
			uri = file.toURI();
//			uri = new URI(fileName);
			NormalExplicitImportConfigurator config = 
				NormalExplicitImportConfigurator.NewInstance(uri, destinationDb, code, dbSchemaValidation);
	
			config.setSourceReferenceTitle("Use Summary Excel Import");
			
			CdmDefaultImport<NormalExplicitImportConfigurator> normalExplicitImport = 
				new CdmDefaultImport<NormalExplicitImportConfigurator>();
	
			// invoke import
			logger.debug("Invoking Normal Explicit Excel import");
			normalExplicitImport.invoke(config);
//		} catch (URISyntaxException e) {
//			e.printStackTrace();
//		}
    	    	
    }
}
