// $Id$
/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.app.pesi;

import java.util.UUID;

import org.apache.log4j.Logger;
import org.springframework.transaction.TransactionStatus;

import eu.etaxonomy.cdm.app.common.CdmDestinations;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.common.CdmDefaultExport;
import eu.etaxonomy.cdm.io.common.Source;
import eu.etaxonomy.cdm.io.common.IExportConfigurator.CHECK;
import eu.etaxonomy.cdm.io.common.IImportConfigurator.DO_REFERENCES;
import eu.etaxonomy.cdm.io.pesi.out.PesiExportConfigurator;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;

/**
 * @author a.mueller
 * @author e.-m.lee
 * @date 16.02.2010
 *
 */
public class PesiExportActivator {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(PesiExportActivator.class);

	//database validation status (create, update, validate ...)
	static final Source pesiDestination = PesiDestinations.pesi_test_local();
	static final ICdmDataSource cdmSource = CdmDestinations.cdm_test_jaxb2();
	static final UUID secUuid = UUID.fromString("d03ef02a-f226-4cb1-bdb4-f6c154f08a34");
	static final int sourceSecId = 7331;
	static final int isHomotypicId = 72;
	static boolean useTaxonomicTree = true;
	
	//check - export
	static final CHECK check = CHECK.EXPORT_WITHOUT_CHECK;

	//NomeclaturalCode
	static final NomenclaturalCode nomenclaturalCode  = NomenclaturalCode.ICBN;

// ****************** ALL *****************************************
	
	//authors
	static final boolean doAuthors = true;
	//references
	static final DO_REFERENCES doReferences =  DO_REFERENCES.ALL;
	//names
	static final boolean doTaxonNames = true;
	static final boolean doRelNames = true;
	static final boolean doNameStatus = true;
	static final boolean doTypes = false;
	static final boolean doNameFacts = false;
	
	//taxa
	static final boolean doTaxa = true;
	static final boolean doRelTaxa = true;
	static final boolean doFacts = true;
	static final boolean doOccurences = false;

// ************************ NONE **************************************** //
	
//	//authors
//	static final boolean doAuthors = false;
//	static final boolean doAuthorTeams = false;
//	//references
//	static final DO_REFERENCES doReferences =  DO_REFERENCES.ALL;
//	//names
//	static final boolean doTaxonNames = true;
//	static final boolean doRelNames = true;
//	static final boolean doNameStatus = false;
//	static final boolean doTypes = false;
//	static final boolean doNameFacts = false;
//	
//	//taxa
//	static final boolean doTaxa = false;
//	static final boolean doRelTaxa = false;
//	static final boolean doFacts = false;
//	static final boolean doOccurences = false;
//	
	
	public boolean 	doExport(ICdmDataSource source){
		System.out.println("Start export to PESI ("+ pesiDestination.getDatabase() + ") ...");
		
		//make PESI Source
		Source destination = pesiDestination;
		
		PesiExportConfigurator pesiExportConfigurator = PesiExportConfigurator.NewInstance(destination, source);
		
		pesiExportConfigurator.setDoAuthors(doAuthors);
		pesiExportConfigurator.setDoReferences(doReferences);
		pesiExportConfigurator.setDoTaxonNames(doTaxonNames);
		pesiExportConfigurator.setDoRelNames(doRelNames);
		pesiExportConfigurator.setDoNameStatus(doNameStatus);
		pesiExportConfigurator.setDoTypes(doTypes);
		pesiExportConfigurator.setDoNameFacts(doNameFacts);
		
		pesiExportConfigurator.setDoTaxa(doTaxa);
		pesiExportConfigurator.setDoRelTaxa(doRelTaxa);
		pesiExportConfigurator.setDoFacts(doFacts);
		pesiExportConfigurator.setDoOccurrence(doOccurences);
//		pesiExportConfigurator.setIsHomotypicId(isHomotypicId);
		pesiExportConfigurator.setCheck(check);
		pesiExportConfigurator.setUseTaxonomicTree(useTaxonomicTree);

		// invoke export
		CdmDefaultExport<PesiExportConfigurator> pesiExport = new CdmDefaultExport<PesiExportConfigurator>();
		boolean result = pesiExport.invoke(pesiExportConfigurator);
		
		System.out.println("End export to PESI ("+ destination.getDatabase() + ")..." + (result? "(successful)":"(with errors)"));
		return result;
	}
		
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		PesiExportActivator ex = new PesiExportActivator();
		ICdmDataSource source = CdmDestinations.chooseDestination(args) != null ? CdmDestinations.chooseDestination(args) : cdmSource;

		ex.doExport(source);
	}

}
