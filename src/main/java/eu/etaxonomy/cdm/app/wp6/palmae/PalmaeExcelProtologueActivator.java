/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.app.wp6.palmae;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.app.common.CdmDestinations;
import eu.etaxonomy.cdm.app.images.ImageImportConfigurator;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.PalmaeProtologueImport;
import eu.etaxonomy.cdm.io.common.CdmDefaultImport;
import eu.etaxonomy.cdm.io.common.IImportConfigurator;

/**
 * @author n.hoffmann
 * @created 19.11.2008
 * @version 2.0 (18.02.2009)
 */
public class PalmaeExcelProtologueActivator {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(PalmaeExcelProtologueActivator.class);

	public static final String sourceFileString = "src/main/resources/images/protologue_links_palmae.xls";
	private static final ICdmDataSource cdmDestination = CdmDestinations.localH2Palmae();

	static final UUID secUuid = UUID.fromString("5f32b8af-0c97-48ac-8d33-6099ed68c625");
	
	// set the webserver path to the protologues
	private static final String urlString = "http://wp5.e-taxonomy.eu/media/palmae/protologe/";
	
	public static void main (String[] whatever){
		URI uri;
		try {
			uri = new URI(sourceFileString);
			ImageImportConfigurator imageConfigurator = ImageImportConfigurator.NewInstance(uri, cdmDestination, urlString, PalmaeProtologueImport.class);
			imageConfigurator.setSecUuid(secUuid);
			
			CdmDefaultImport<IImportConfigurator> importer = new CdmDefaultImport<IImportConfigurator>();
			importer.invoke(imageConfigurator);
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		
	}
	
}
