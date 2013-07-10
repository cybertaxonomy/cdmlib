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

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.app.common.CdmDestinations;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.common.CdmDefaultExport;
import eu.etaxonomy.cdm.io.common.DbExportConfiguratorBase.IdType;
import eu.etaxonomy.cdm.io.common.IExportConfigurator.CHECK;
import eu.etaxonomy.cdm.io.common.IExportConfigurator.DO_REFERENCES;
import eu.etaxonomy.cdm.io.common.Source;
import eu.etaxonomy.cdm.io.pesi.out.PesiExportConfigurator;
import eu.etaxonomy.cdm.io.pesi.out.PesiTransformer;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;

/**
 * @author a.mueller
 * @author e.-m.lee
 * @date 16.02.2010
 *
 */
public class PesiExportActivatorERMS {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(PesiExportActivatorERMS.class);

	//database validation status (create, update, validate ...)
	static final Source pesiDestination = PesiDestinations.pesi_test_local_CDM_ERMS2PESI();
//	static final Source pesiDestination = PesiDestinations.pesi_test_local_CDM_FE2PESI();
//	static final Source pesiDestination = PesiDestinations.pesi_test_local_CDM_ERMS2PESI();
	
	static final ICdmDataSource cdmSource = CdmDestinations.cdm_test_local_mysql_erms();
	//Taxon names can't be mapped to their CDM ids as PESI Taxon table mainly holds taxa and there IDs. We ad nameIdStart to the TaxonName id to get a unique id
	static final int nameIdStart = 10000000;
	static final IdType idType = IdType.CDM_ID_WITH_EXCEPTIONS;

	static final int partitionSize = 1000;
	
	//check - export
	static final CHECK check = CHECK.EXPORT_WITHOUT_CHECK;

	//NomeclaturalCode
	static final NomenclaturalCode nomenclaturalCode  = NomenclaturalCode.ICBN;
	
	static final boolean deleteAll = true;
	
	
// ****************** ALL *****************************************
	
	//references
	static final DO_REFERENCES doReferences =  DO_REFERENCES.ALL;
	
	//taxa
	static final boolean doTaxa = true;
	static final boolean doTreeIndex = true;
	static final boolean doRank = true;
	static final boolean doInferredSynonyms = false;
	static final boolean doRelTaxa = true;
	static final boolean doDescriptions = true;
	
	static final boolean doNotes = true;
	static final boolean doNoteSources = true;
	static final boolean doAdditionalTaxonSource = true;
	static final boolean doOccurrence = true;
	static final boolean doOccurrenceSource = true;
	static final boolean doImage = true;
	

// ************************ NONE **************************************** //
	
//	//references
//	static final DO_REFERENCES doReferences =  DO_REFERENCES.NONE;
//	
//	//taxa
//	static final boolean doTaxa = false;
//	static final boolean doRelTaxa = false;
//	static final boolean doNotes = false;
//	static final boolean doNoteSources = false;
//	static final boolean doAdditionalTaxonSource = false;
//	static final boolean doOccurrence = false;
//	static final boolean doOccurrenceSource = false;
//	static final boolean doImage = false;
//	static final boolean doTreeIndex = true;
//	static final boolean doRank = true;
//	static final boolean doInferredSynonyms = true;
	
	
	public boolean 	doExport(ICdmDataSource source){
		System.out.println("Start export to PESI ("+ pesiDestination.getDatabase() + ") ...");
		
		//make PESI Source
		Source destination = pesiDestination;
		PesiTransformer transformer = new PesiTransformer(destination);
		
		PesiExportConfigurator config = PesiExportConfigurator.NewInstance(destination, source, transformer);
		
		config.setDoTaxa(doTaxa);
		config.setDoRelTaxa(doRelTaxa);
		config.setDoOccurrence(doOccurrence);
		config.setDoReferences(doReferences);
		config.setDoImages(doImage);
		config.setDoNotes(doNotes);
		config.setDoNoteSources(doNoteSources);
		config.setDoOccurrenceSource(doOccurrenceSource);
		config.setDoTreeIndex(doTreeIndex);
		config.setDoRank(doRank);
		config.setDoInferredSynonyms(doInferredSynonyms);
		config.setDoDescription(doDescriptions);
		
		config.setCheck(check);
		config.setLimitSave(partitionSize);
		config.setIdType(idType);
		config.setNameIdStart(nameIdStart);
		if (deleteAll){
			destination.update("EXEC sp_deleteAllData");
		}

		// invoke export
		CdmDefaultExport<PesiExportConfigurator> pesiExport = new CdmDefaultExport<PesiExportConfigurator>();
		boolean result = pesiExport.invoke(config);
		
		System.out.println("End export to PESI ("+ destination.getDatabase() + ")..." + (result? "(successful)":"(with errors)"));
		return result;
	}
		
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		PesiExportActivatorERMS ex = new PesiExportActivatorERMS();
		ICdmDataSource source = CdmDestinations.chooseDestination(args) != null ? CdmDestinations.chooseDestination(args) : cdmSource;

		ex.doExport(source);
	}

}
