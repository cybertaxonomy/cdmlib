/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.app.vibrant;

import java.net.URI;
import java.util.UUID;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.app.common.CdmDestinations;
import eu.etaxonomy.cdm.database.DbSchemaValidation;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.common.CdmDefaultImport;
import eu.etaxonomy.cdm.io.common.IImportConfigurator.CHECK;
import eu.etaxonomy.cdm.io.common.events.LoggingIoObserver;
import eu.etaxonomy.cdm.io.dwca.in.DwcaImportConfigurator;
import eu.etaxonomy.cdm.io.dwca.in.DwcaImportConfigurator.DatasetUse;
import eu.etaxonomy.cdm.io.dwca.in.IImportMapping.MappingType;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;

/**
 * @author a.mueller
 * @created 03.04.2012
 * @version 1.0
 */
public class DwcaScratchpadImportActivator {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(DwcaScratchpadImportActivator.class);
	
	//database validation status (create, update, validate ...)
	static DbSchemaValidation hbm2dll = DbSchemaValidation.CREATE;

	//	static final URI source =  dwca_emonocots_dioscoreaceae();
	static final URI source =  dwca_emonocots_zingiberaceae();
//	static final URI source =  dwca_emonocots_cypripedioideae();
	
//	static final ICdmDataSource cdmDestination = CdmDestinations.localH2();
	static final ICdmDataSource cdmDestination = CdmDestinations.cdm_test_local_mysql();
//	static final ICdmDataSource cdmDestination = CdmDestinations.cdm_test_local_mysql_test();
	
	//default nom code is ICZN as it allows adding publication year 
	static final NomenclaturalCode defaultNomCode = NomenclaturalCode.ICBN;

	//title
	static final String title = "Scratchpad test import";
	
	//check - import
	static final CHECK check = CHECK.IMPORT_WITHOUT_CHECK;
	static int partitionSize = 1000;
	
	//config
	static DatasetUse datasetUse = DatasetUse.CLASSIFICATION;
	
	//validate
	static boolean validateRankConsistency = false;
	
	
	//taxa
	static final boolean doTaxa = true;
	static final boolean doDistribution = true;
	
	
	
	static final MappingType mappingType = MappingType.InMemoryMapping;
	
	//classification
	static final UUID classificationUuid = UUID.fromString("d9d199b6-eaf4-47c8-a732-0639bc445c56");
	
	
	//config
	static boolean scientificNameIdAsOriginalSourceId = true;
	static boolean guessNomRef = false;
	private boolean handleAllRefsAsCitation = false;
	private static final boolean useSourceReferenceAsSec = true;
	

	//deduplicate
	static final boolean doDeduplicate = false;

	
	
	protected void doImport(URI source, ICdmDataSource cdmDestination, UUID classificationUuid, String title, DbSchemaValidation hbm2dll){
		
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
		config.setUseSourceReferenceAsSec(useSourceReferenceAsSec);
		config.setSourceReferenceTitle(title);
		
		CdmDefaultImport myImport = new CdmDefaultImport();

		
		//...
		if (true){
			System.out.println("Start import from ("+ source.toString() + ") ...");
			config.setSourceReference(getSourceReference(config.getSourceReferenceTitle()));
			myImport.invoke(config);
			System.out.println("End import from ("+ source.toString() + ")...");
		}
		
		
	}

	private Reference<?> getSourceReference(String string) {
		Reference<?> result = ReferenceFactory.newGeneric();
		result.setTitleCache(string);
		return result;
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
	

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		DwcaScratchpadImportActivator me = new DwcaScratchpadImportActivator();
		me.doImport(source, cdmDestination, classificationUuid, title, hbm2dll);
	}
	
}
