/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.app.berlinModelImport;

import java.util.UUID;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.app.common.CdmDestinations;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.berlinModel.out.BerlinModelExportConfigurator;
import eu.etaxonomy.cdm.io.common.CdmDefaultExport;
import eu.etaxonomy.cdm.io.common.Source;
import eu.etaxonomy.cdm.io.common.IExportConfigurator.CHECK;
import eu.etaxonomy.cdm.io.common.IExportConfigurator.DO_REFERENCES;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;


/**
 *
 * @author a.mueller
 *
 */
public class SalvadorExport {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(SalvadorExport.class);

	//database validation status (create, update, validate ...)
	static final Source berlinModelDestination = BerlinModelDestinations.El_Salvador_Andreas();
	static final ICdmDataSource cdmSource = CdmDestinations.localH2Salvador();
	static final UUID secUuid = UUID.fromString("d03ef02a-f226-4cb1-bdb4-f6c154f08a34");
	static final int sourceSecId = 7331;
	static final int isHomotypicId = 72;
	static boolean useClassification = true;
	
//	static final UUID featureTreeUuid = UUID.fromString("ae9615b8-bc60-4ed0-ad96-897f9226d568");
//	static final Object[] featureKeyList = new Integer[]{302, 303, 306, 307, 309, 310, 311, 312, 350, 1500, 1800, 1900, 1950, 1980, 2000, 10299}; 
	
	//check - import
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
	static final boolean doTypes = false;  //Types do not exist in El_Salvador DB
	static final boolean doNameFacts = false;  //Name Facts do not exist in El_Salvador DB
	
	//taxa
	static final boolean doTaxa = true;
	static final boolean doRelTaxa = true;
	static final boolean doFacts = true;
	static final boolean doOccurences = false; //occurrences do not exist in Salvador

// ************************ NONE **************************************** //
	
//	//authors
//	static final boolean doAuthors = false;
//	static final boolean doAuthorTeams = false;
//	//references
//	static final DO_REFERENCES doReferences =  DO_REFERENCES.ALL;
//	//names
//	static final boolean doTaxonNames = true;
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
		System.out.println("Start export to Berlin Model ("+ berlinModelDestination.getDatabase() + ") ...");
		
		//make BerlinModel Source
		Source destination = berlinModelDestination;
		
		BerlinModelExportConfigurator bmExportConfigurator = BerlinModelExportConfigurator.NewInstance(destination, source);
		
//		bmExportConfigurator.setSecUuid(secUuid);
//		bmExportConfigurator.setSourceSecId(sourceSecId);
//		bmExportConfigurator.setNomenclaturalCode(nomenclaturalCode);

		bmExportConfigurator.setDoAuthors(doAuthors);
//		bmExportConfigurator.setDoAuthorTeams(doAuthors);
		bmExportConfigurator.setDoReferences(doReferences);
		bmExportConfigurator.setDoTaxonNames(doTaxonNames);
		bmExportConfigurator.setDoRelNames(doRelNames);
		bmExportConfigurator.setDoNameFacts(doNameFacts);
		
		bmExportConfigurator.setDoTaxa(doTaxa);
		bmExportConfigurator.setDoRelTaxa(doRelTaxa);
		bmExportConfigurator.setDoFacts(doFacts);
		bmExportConfigurator.setDoOccurrence(doOccurences);
		bmExportConfigurator.setIsHomotypicId(isHomotypicId);
		bmExportConfigurator.setCheck(check);
		bmExportConfigurator.setUseClassification(useClassification);

		// invoke import
		CdmDefaultExport<BerlinModelExportConfigurator> bmExport = new CdmDefaultExport<BerlinModelExportConfigurator>();
		boolean result = bmExport.invoke(bmExportConfigurator);
		
		System.out.println("End export to BerlinModel ("+ destination.getDatabase() + ")..." + (result? "(successful)":"(with errors)"));
		return result;
	}

	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		SalvadorExport ex = new SalvadorExport();
		ICdmDataSource source = CdmDestinations.chooseDestination(args) != null ? CdmDestinations.chooseDestination(args) : cdmSource;

		ex.doExport(source);
	}
	
	
	

}
