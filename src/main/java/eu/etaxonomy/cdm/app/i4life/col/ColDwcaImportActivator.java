/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.app.i4life.col;

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
public class ColDwcaImportActivator {
	private static final Logger logger = Logger.getLogger(ColDwcaImportActivator.class);
	
	//database validation status (create, update, validate ...)
	static DbSchemaValidation hbm2dll = DbSchemaValidation.CREATE;
	static final URI source = dwca_col_All();

	static final ICdmDataSource cdmDestination = CdmDestinations.cdm_test_local_mysql_dwca();


	static boolean isNoQuotes = true;
	
	//classification
	static final UUID classificationUuid = UUID.fromString("29d4011f-a6dd-4081-beb8-559ba6b84a6b");
	
	//default nom code is ICZN as it allows adding publication year 
	static final NomenclaturalCode defaultNomCode = NomenclaturalCode.ICZN;

	
	//check - import
	static final CHECK check = CHECK.IMPORT_WITHOUT_CHECK;
	static int partitionSize = 1000;
	
	//config
	static DatasetUse datasetUse = DatasetUse.ORIGINAL_SOURCE;
	static boolean scientificNameIdAsOriginalSourceId = true;
	static boolean guessNomRef = true;
	private boolean handleAllRefsAsCitation = true;
	
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
		config.setNoQuotes(isNoQuotes);
		
		CdmDefaultImport myImport = new CdmDefaultImport();

		
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
	
	
	//CoL
	public static URI dwca_col_All() {
		URI sourceUrl = URI.create("file:////Pesiimport3/col/col_20Nov2012.zip");
		return sourceUrl;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		ColDwcaImportActivator me = new ColDwcaImportActivator();
		me.doImport(cdmDestination);
	}
	
}
