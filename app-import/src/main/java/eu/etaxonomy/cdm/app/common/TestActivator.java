/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.app.common;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.api.service.config.ITaxonServiceConfigurator;
import eu.etaxonomy.cdm.api.service.config.impl.TaxonServiceConfiguratorImpl;
import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.database.DbSchemaValidation;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.common.IImportConfigurator.CHECK;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.location.TdwgArea;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;
import eu.etaxonomy.cdm.persistence.query.MatchMode;

/**
 * @author a.babadshanjan
 * @created 12.05.2009
 */
public class TestActivator {
	private static final Logger logger = Logger.getLogger(TestActivator.class);

	//static final Source faunaEuropaeaSource = FaunaEuropaeaSources.faunEu();
	static final ICdmDataSource cdmDestination = CdmDestinations.cdm_edit_cichorieae_a();
	
	static final int limitSave = 2000;

//	static final CHECK check = CHECK.CHECK_AND_IMPORT;
	static final CHECK check = CHECK.IMPORT_WITHOUT_CHECK;
	static DbSchemaValidation dbSchemaValidation = DbSchemaValidation.VALIDATE;
//	static DbSchemaValidation dbSchemaValidation = DbSchemaValidation.UPDATE;
//	static DbSchemaValidation dbSchemaValidation = DbSchemaValidation.VALIDATE;
	static final NomenclaturalCode nomenclaturalCode  = NomenclaturalCode.ICZN;


	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		ICdmDataSource destination = cdmDestination;
		
		CdmApplicationController app;
		app = CdmApplicationController.NewInstance(destination, dbSchemaValidation);
		
		
		app.changeDataSource(destination);
		ICdmDataSource cdmDestination = CdmDestinations.cdm_edit_cichorieae_a();
		app.changeDataSource(cdmDestination);
		ITaxonServiceConfigurator conf = TaxonServiceConfiguratorImpl.NewInstance();
		conf.setDoSynonyms(true);
		conf.setDoTaxa(true);
		conf.setMatchMode(MatchMode.BEGINNING);
		conf.setSearchString("L*");
		conf.setPageNumber(0);
		conf.setPageSize(50);
		Set<NamedArea> areas = new HashSet<NamedArea>();
		areas.add(TdwgArea.getAreaByTdwgAbbreviation("GER"));
		//conf.setNamedAreas(areas);
		
		Pager<IdentifiableEntity> taxaAndSyn = app.getTaxonService().findTaxaAndNames(conf);
		List<IdentifiableEntity> taxList = taxaAndSyn.getRecords();
		
		for (IdentifiableEntity ent: taxList){
			
			System.err.println(ent.getTitleCache());
		}
		
	
		
		System.out.println("End importing Fauna Europaea data");
	}

}
