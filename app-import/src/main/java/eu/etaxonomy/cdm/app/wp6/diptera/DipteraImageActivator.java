/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.app.wp6.diptera;

import java.io.File;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.app.common.CdmDestinations;
import eu.etaxonomy.cdm.app.images.ImageImportConfigurator;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.DipteraImageImport;
import eu.etaxonomy.cdm.io.common.CdmDefaultImport;
import eu.etaxonomy.cdm.io.common.IImportConfigurator;

/**
 * DON'T USE
 * THIS IMPORT IS NOT NEEDED ANYMORE. THE DATA ARE IMPORTED VIA BERLIN MODEL ALREADY.
 * 
 * @author a.babadshanjan
 * @created 27.04.2009
 * @version 1.0
 */
public class DipteraImageActivator  {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger
			.getLogger(DipteraImageActivator.class);
	
	private static final File sourceFile = new File("src/main/resources/images/images_diptera.xls");

	private static final ICdmDataSource cdmDestination = CdmDestinations.cdm_local_dipera();

	
//	static final UUID secUuid = UUID.fromString("6924c75d-e0d0-4a6d-afb7-3dd8c71195ca");
	
	public static void main (String[] cowabunga){
		ImageImportConfigurator imageConfigurator = ImageImportConfigurator.NewInstance(
		        sourceFile, cdmDestination, DipteraImageImport.class);
//		imageConfigurator.setSecUuid(secUuid);
		
		CdmDefaultImport<IImportConfigurator> importer = new CdmDefaultImport<IImportConfigurator>();
		//importer.invoke(imageConfigurator);
	}
}
