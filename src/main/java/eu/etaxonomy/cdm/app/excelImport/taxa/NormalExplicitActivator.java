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

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.app.common.CdmDestinations;
import eu.etaxonomy.cdm.database.DbSchemaValidation;
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
	private static final Logger logger = Logger.getLogger(NormalExplicitActivator.class);
    
//	private static String fileName = 
//		new String("D:\\_Tagungen\\2010-09 TDWG 2010\\Workshop\\data\\NormalExplicit.xls");
	
	private static URI source  = 
		URI.create("file:/C:/localCopy/meetings_workshops/2011_Göttingen/GermanSL12/GermanSL.xls");

	private static DbSchemaValidation dbSchemaValidation = DbSchemaValidation.CREATE;
	
//	private static final ICdmDataSource destinationDb = CdmDestinations.cdm_test_jaxb();
	private static final ICdmDataSource destinationDb = CdmDestinations.cdm_test_local_mysql();
    
    public static void main(String[] args) {

    	NomenclaturalCode code = NomenclaturalCode.ICBN;
    	URI uri = source;
		NormalExplicitImportConfigurator normalExplicitImportConfigurator = 
    		NormalExplicitImportConfigurator.NewInstance(uri, destinationDb, code,dbSchemaValidation);

		CdmDefaultImport<NormalExplicitImportConfigurator> normalExplicitImport = 
			new CdmDefaultImport<NormalExplicitImportConfigurator>();

		// invoke import
		logger.debug("Invoking Normal Explicit Excel import");
		normalExplicitImport.invoke(normalExplicitImportConfigurator);
    	    	
    }
}
