/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.app.eflora;

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.api.service.ITermService;
import eu.etaxonomy.cdm.database.DbSchemaValidation;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.api.application.CdmIoApplicationController;
import eu.etaxonomy.cdm.io.common.IImportConfigurator.CHECK;
import eu.etaxonomy.cdm.io.common.events.IIoObserver;
import eu.etaxonomy.cdm.io.common.events.LoggingIoObserver;

/**
 * @author a.mueller
 * @created 15.06.2013
 */
public class FloraMalesianaProspectiveTester extends EfloraActivatorBase {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(FloraMalesianaProspectiveTester.class);
	
	//database validation status (create, update, validate ...)
	static DbSchemaValidation hbm2dll = DbSchemaValidation.VALIDATE;
	
	
//	static final ICdmDataSource cdmDestination = CdmDestinations.cdm_flora_malesiana_prospective_production();
	static final ICdmDataSource cdmDestination = CdmDestinations.localH2();
//	static final ICdmDataSource cdmDestination = CdmDestinations.cdm_test_local_mysql();
//	
	

	//feature tree uuid
	
	//classification
	//check - import
	static CHECK check = CHECK.IMPORT_WITHOUT_CHECK;
	
		private IIoObserver observer = new LoggingIoObserver();
	private Set<IIoObserver> observerList = new HashSet<IIoObserver>();
	
	
	private void doImport(ICdmDataSource cdmDestination){
		observerList.add(observer);

		CdmApplicationController app = CdmIoApplicationController.NewInstance(cdmDestination, hbm2dll);
		ITermService service = app.getTermService();
	}		
	

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		FloraMalesianaProspectiveTester me = new FloraMalesianaProspectiveTester();
		me.doImport(cdmDestination);
	}
	
}
