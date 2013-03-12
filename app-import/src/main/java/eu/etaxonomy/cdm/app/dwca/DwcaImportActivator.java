/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.app.dwca;

import java.net.URI;
import java.util.UUID;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.api.application.ICdmApplicationConfiguration;
import eu.etaxonomy.cdm.app.common.CdmDestinations;
import eu.etaxonomy.cdm.database.DbSchemaValidation;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.common.CdmDefaultImport;
import eu.etaxonomy.cdm.io.common.IImportConfigurator.CHECK;
import eu.etaxonomy.cdm.io.common.events.LoggingIoObserver;
import eu.etaxonomy.cdm.io.dwca.in.DwcaImportConfigurator;
import eu.etaxonomy.cdm.io.dwca.in.DwcaImportConfigurator.DatasetUse;
import eu.etaxonomy.cdm.io.dwca.in.IImportMapping.MappingType;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;

/**
 * @author a.mueller
 * @created 10.11.2011
 */
public class DwcaImportActivator {
	private static final Logger logger = Logger.getLogger(DwcaImportActivator.class);
	
	//database validation status (create, update, validate ...)
	static DbSchemaValidation hbm2dll = DbSchemaValidation.CREATE;
//	static final URI source = dwca_test_in();
//	static final URI source = dwca_test_cich_len();
//	static final URI source = dwca_test_col_sapindaceae();
//	static final URI source = dwca_test_col_cichorium();
//	static final URI source = dwca_test_scratch_test();
//	static final URI source = dwca_test_col_All();
//	static final URI source = dwca_test_col_All_Pesi2();
	static final URI source =  dwca_emonocots_dioscoreaceae();
	
	
	static final ICdmDataSource cdmDestination = CdmDestinations.localH2();
//	static final ICdmDataSource cdmDestination = CdmDestinations.cdm_test_local_mysql_dwca();
//	static final ICdmDataSource cdmDestination = CdmDestinations.cdm_test_local_mysql_test();
//	static final ICdmDataSource cdmDestination = CdmDestinations.localH2();
//	static final ICdmDataSource cdmDestination = CdmDestinations.cdm_test_local_mysql_pesi_test();
//	static final ICdmDataSource cdmDestination = CdmDestinations.cdm_test_local_mysql_pesi();
//	static final ICdmDataSource cdmDestination = CdmDestinations.cdm_test_local_mysql();


	
	//classification
	static final UUID classificationUuid = UUID.fromString("29d4011f-a6dd-4081-beb8-559ba6b84a6b");
	
	//default nom code is ICZN as it allows adding publication year 
	static final NomenclaturalCode defaultNomCode = NomenclaturalCode.ICZN;

	
	//check - import
	static final CHECK check = CHECK.IMPORT_WITHOUT_CHECK;
	static int partitionSize = 1000;
	
	//config
	static DatasetUse datasetUse = DatasetUse.SECUNDUM;
	static boolean scientificNameIdAsOriginalSourceId = true;
	static boolean guessNomRef = true;
	static boolean handleAllRefsAsCitation = true;
	
	//validate
	static boolean validateRankConsistency = false;
	
	
	//taxa
	static final boolean doTaxa = true;
	static final boolean doDistribution = false;
	//deduplicate
	static final boolean doDeduplicate = false;
	
	
	
	static final MappingType mappingType = MappingType.InMemoryMapping;
	
	private void doImport(ICdmDataSource cdmDestination){
		
		//make Source
		DwcaImportConfigurator config= DwcaImportConfigurator.NewInstance(source, cdmDestination);
		config.addObserver(new LoggingIoObserver());
		config.setClassificationUuid(classificationUuid);
		config.setCheck(check);
		config.setDbSchemaValidation(hbm2dll);
		config.setMappingType(mappingType);
		
		config.setScientificNameIdAsOriginalSourceId(scientificNameIdAsOriginalSourceId);
		config.setValidateRankConsistency(validateRankConsistency);
		config.setDefaultPartitionSize(partitionSize);
		config.setNomenclaturalCode(defaultNomCode);
		config.setDatasetUse(datasetUse);
		config.setGuessNomenclaturalReferences(guessNomRef);
		config.setHandleAllRefsAsCitation(handleAllRefsAsCitation);
		
		CdmDefaultImport<DwcaImportConfigurator> myImport = new CdmDefaultImport<DwcaImportConfigurator>();

		
		//...
		if (true){
			System.out.println("Start import from ("+ source.toString() + ") ...");
			config.setSourceReference(getSourceReference(config.getSourceReferenceTitle()));
			myImport.invoke(config);
			System.out.println("End import from ("+ source.toString() + ")...");
		}
		
		
		
		//deduplicate
		if (doDeduplicate){
			ICdmApplicationConfiguration app = myImport.getCdmAppController();
			int count = app.getAgentService().deduplicate(Person.class, null, null);
			logger.warn("Deduplicated " + count + " persons.");
//			count = app.getAgentService().deduplicate(Team.class, null, null);
//			logger.warn("Deduplicated " + count + " teams.");
			count = app.getReferenceService().deduplicate(Reference.class, null, null);
			logger.warn("Deduplicated " + count + " references.");
		}
		
	}

	private Reference<?> getSourceReference(String string) {
		Reference<?> result = ReferenceFactory.newGeneric();
		result.setTitleCache(string);
		return result;
	}

	//Dwca
	public static URI dwca_test_in() {
//		URI sourceUrl = URI.create("http://dev.e-taxonomy.eu/trac/export/14463/trunk/cdmlib/cdmlib-io/src/test/resources/eu/etaxonomy/cdm/io/dwca/in/DwcaZipToStreamConverterTest-input.zip");
		URI sourceUrl = URI.create("file:///C:/Users/pesiimport/Documents/pesi_cdmlib/cdmlib-io/src/test/resources/eu/etaxonomy/cdm/io/dwca/in/DwcaZipToStreamConverterTest-input.zip");
		return sourceUrl;
	}
	
	
	//Dwca
	public static URI dwca_test_cich() {
		URI sourceUrl = URI.create("file:///E:/opt/data/dwca/20110621_1400_cichorieae_dwca.zip");
		return sourceUrl;
	}
	
	//Dwca
	public static URI dwca_test_cich_len() {
		URI sourceUrl = URI.create("file:///C:/localCopy/Data/dwca/export/20110621_1400_cichorieae_dwca.zip");
		return sourceUrl;
	}
	
	//Dwca
	public static URI dwca_test_col_cichorium() {
		URI sourceUrl = URI.create("file:///C:/localCopy/Data/dwca/import/CoL/Cichorium/archive-genus-Cichorium-bl3.zip");
		return sourceUrl;
	}
	
	//Dwca
	public static URI dwca_test_col_sapindaceae() {
		URI sourceUrl = URI.create("file:///C:/localCopy/Data/dwca/import/CoL/Sapindaceae/archive-family-Sapindaceae-bl3.zip");
		return sourceUrl;
	}

	//Dwca
	public static URI dwca_emonocots_local() {
		URI sourceUrl = URI.create("file:///C:/localCopy/Data/dwca/import/Scratchpads/dwca_dioscoreaceae_emonocots.zip");
		return sourceUrl;
	}
	
	//emonocots_dioscoreaceae
	public static URI dwca_emonocots_dioscoreaceae() {
		URI sourceUrl = URI.create("file:////PESIIMPORT3/vibrant/dwca/dwca_emonocots_dioscoreaceae.zip");
		return sourceUrl;
	}
	
	//emonocots_zingiberaceae
	public static URI dwca_emonocots_zingiberaceae() {
		URI sourceUrl = URI.create("file:////PESIIMPORT3/vibrant/dwca/dwca_emonocots_zingiberaceae.zip");
		return sourceUrl;
	}
	//emonocots_cypripedioideae
	public static URI dwca_emonocots_cypripedioideae() {
		URI sourceUrl = URI.create("file:////PESIIMPORT3/vibrant/dwca/dwca_emonocots_cypripedioideae.zip");
		return sourceUrl;
	}
	
	
	//CoL
	public static URI dwca_test_col_All() {
		URI sourceUrl = URI.create("file:///C:/localCopy/Data/dwca/import/CoL/All/archive-complete.zip");
		return sourceUrl;
	}

	//CoL
	public static URI dwca_test_col_All_Pesi2() {
		URI sourceUrl = URI.create("file:///C:/opt/data/CoL/All/archive-complete.zip");
		return sourceUrl;
	}

	

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		DwcaImportActivator me = new DwcaImportActivator();
		me.doImport(cdmDestination);
	}
	
}
