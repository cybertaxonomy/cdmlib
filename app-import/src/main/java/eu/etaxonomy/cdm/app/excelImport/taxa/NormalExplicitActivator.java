/**
 * Copyright (C) 2007 EDIT
 * European Distributed Institute of Taxonomy 
 * http://www.e-taxonomy.eu
 * 
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */

package eu.etaxonomy.cdm.app.excelImport.taxa;

import java.net.URI;
import java.net.URISyntaxException;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.app.common.CdmDestinations;
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

	private static final String dbName = "cdm_test_jaxb";
	private static String fileName = 
		new String("C:\\Cichori-test.xls");
	
	private static final ICdmDataSource destinationDb = CdmDestinations.cdm_test_jaxb();
    private static final Logger logger = Logger.getLogger(NormalExplicitActivator.class);
    
    public static void main(String[] args) {

    	NomenclaturalCode code = NomenclaturalCode.ICBN;
    	URI uri;
		try {
			uri = new URI(fileName);
			NormalExplicitImportConfigurator normalExplicitImportConfigurator = 
	    		NormalExplicitImportConfigurator.NewInstance(uri, destinationDb, code);
	
			CdmDefaultImport<NormalExplicitImportConfigurator> normalExplicitImport = 
				new CdmDefaultImport<NormalExplicitImportConfigurator>();
	
			// invoke import
			logger.debug("Invoking Normal Explicit Excel import");
			normalExplicitImport.invoke(normalExplicitImportConfigurator);
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
    	    	
    }
}
