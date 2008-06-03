/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/


import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.database.DbSchemaValidation;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.taxon.Taxon;

/**
 * @author a.mueller
 * @created 03.06.2008
 * @version 1.0
 */
public class Profiling {
	private static final Logger logger = Logger.getLogger(Profiling.class);
	
	private void testGetRoot(){
		System.out.println("testGetRoot started ...");
		ICdmDataSource cdmDestination = CdmDestinations.cdm_edit_cichorieae();
		DbSchemaValidation val = DbSchemaValidation.VALIDATE;
		try {
			CdmApplicationController app = CdmApplicationController.NewInstance(cdmDestination, val);
			System.out.println("app initialized ...");
			String secUuid = "cd31b368-7c5c-47ac-ba51-647205f1450d";
			ReferenceBase sec = app.getReferenceService().getReferenceByUuid(UUID.fromString(secUuid));
			List<Taxon> roots = app.getTaxonService().getRootTaxa(sec, null, true);
			for (Taxon taxon: roots){
				System.out.println("Taxon: " + taxon.getUuid() + ", " + taxon.getTitleCache());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("testGetRoot ended ...");
	}
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Profiling me = new Profiling();
		me.testGetRoot();
	}
}
