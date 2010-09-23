/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.app.tcs;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.api.service.IReferenceService;
import eu.etaxonomy.cdm.app.common.CdmDestinations;
import eu.etaxonomy.cdm.database.DbSchemaValidation;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.common.CdmDefaultImport;
import eu.etaxonomy.cdm.io.common.IImportConfigurator.CHECK;
import eu.etaxonomy.cdm.io.common.IImportConfigurator.DO_REFERENCES;
import eu.etaxonomy.cdm.io.tcsrdf.TcsRdfImportConfigurator;
import eu.etaxonomy.cdm.model.common.TimePeriod;
import eu.etaxonomy.cdm.model.reference.IBook;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;

/**
 * @author a.mueller
 * @created 20.06.2008
 * @version 1.0
 */
public class TcsRdfTestActivator {
	private static final Logger logger = Logger.getLogger(TcsRdfTestActivator.class);
	
	//database validation status (create, update, validate ...)
	static DbSchemaValidation hbm2dll = DbSchemaValidation.CREATE;
	static final String tcsSource = TcsSources.tcsRdf_globis();
	static final ICdmDataSource cdmDestination = CdmDestinations.localH2();

	static final UUID treeUuid = UUID.fromString("00000000-0c97-48ac-8d33-6099ed68345");
	static final String sourceSecId = "XXX";
	
	//check - import
	static final CHECK check = CHECK.CHECK_AND_IMPORT;
	
	//authors
	static final boolean doMetaData = true;
	//references
	static final DO_REFERENCES doReferences =  DO_REFERENCES.ALL;
	//names
	static final boolean doTaxonNames = true;
	static final boolean doRelNames = true;
	
	//taxa
	static final boolean doTaxa = true;
	static final boolean doRelTaxa = true;

	
	private void doImport(){
		System.out.println("Start import from Tcs("+ tcsSource.toString() + ") ...");
		
		//make BerlinModel Source
		URI source;
		try {
			source = new URI(tcsSource);
			ICdmDataSource destination = cdmDestination;
			
			TcsRdfImportConfigurator tcsImportConfigurator = TcsRdfImportConfigurator.NewInstance(source,  destination);
			
			tcsImportConfigurator.setTaxonomicTreeUuid(treeUuid);
			tcsImportConfigurator.setSourceSecId(sourceSecId);
			
			tcsImportConfigurator.setDoReferences(doReferences);
			tcsImportConfigurator.setDoTaxonNames(doTaxonNames);
			tcsImportConfigurator.setDoRelNames(doRelNames);
			
			tcsImportConfigurator.setDoTaxa(doTaxa);
			tcsImportConfigurator.setDoRelTaxa(doRelTaxa);
			
			tcsImportConfigurator.setCheck(check);
			tcsImportConfigurator.setDbSchemaValidation(hbm2dll);
	
			// invoke import
			CdmDefaultImport<TcsRdfImportConfigurator> tcsImport = new CdmDefaultImport<TcsRdfImportConfigurator>();
			tcsImport.invoke(tcsImportConfigurator);
			
			
			IReferenceService refService = tcsImport.getCdmAppController().getReferenceService();
			ReferenceFactory refFactory = ReferenceFactory.newInstance();
			IBook book = refFactory.newBook();
			//book.setDatePublished(TimePeriod.NewInstance(1945));
			book.setDatePublished(TimePeriod.NewInstance(1945).setEndDay(12).setEndMonth(4));
			refService.saveOrUpdate((ReferenceBase)book);
			tcsImport.getCdmAppController().close();
			logger.info("End");
			System.out.println("End import from TCS ("+ source.toString() + ")...");
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		TcsRdfTestActivator me = new TcsRdfTestActivator();
		me.doImport();
	}
	
}
