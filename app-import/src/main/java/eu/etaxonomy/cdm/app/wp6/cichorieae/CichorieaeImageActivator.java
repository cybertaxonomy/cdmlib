/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.app.wp6.cichorieae;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.app.common.CdmDestinations;
import eu.etaxonomy.cdm.app.images.ImageImportConfigurator;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.CichorieaeImageImport;
import eu.etaxonomy.cdm.io.common.CdmDefaultImport;
import eu.etaxonomy.cdm.io.common.IImportConfigurator;

/**
 * @author n.hoffmann
 * @created 18.11.2008
 * @version 1.0
 */
public class CichorieaeImageActivator  {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(CichorieaeImageActivator.class);
	
	//	private static final File sourceFile = new File("src/main/resources/images/images_cich.xls");
	private static final ICdmDataSource cdmDestination = CdmDestinations.localH2Cichorieae();
//	private static final ICdmDataSource cdmDestination = CdmDestinations.cdm_import_cichorieae();
	
	static final UUID secUuid = UUID.fromString("6924c75d-e0d0-4a6d-afb7-3dd8c71195ca");
//	static final UUID treeUuid = UUID.fromString("00db28a7-50e1-4abc-86ec-b2a8ce870de9");
	static final UUID treeUuid = UUID.fromString("534e190f-3339-49ba-95d9-fa27d5493e3e");
	
	public static void main (String[] cowabunga){
		
		ICdmDataSource destination = CdmDestinations.chooseDestination(cowabunga) != null ? CdmDestinations.chooseDestination(cowabunga) : cdmDestination;
		
		URI imageFolderCichorieae;
		try {
			imageFolderCichorieae = new URI (CichorieaeActivator.imageFolderString);
			ImageImportConfigurator imageConfigurator = ImageImportConfigurator.NewInstance(
					imageFolderCichorieae, destination, CichorieaeImageImport.class);
			imageConfigurator.setSecUuid(secUuid);
			imageConfigurator.setTaxonomicTreeUuid(treeUuid);
			
			CdmDefaultImport<IImportConfigurator> importer = new CdmDefaultImport<IImportConfigurator>();
			importer.invoke(imageConfigurator);
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}
}
