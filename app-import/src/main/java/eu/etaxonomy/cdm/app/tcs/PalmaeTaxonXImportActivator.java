/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.app.tcs;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.springframework.transaction.TransactionStatus;

import eu.etaxonomy.cdm.app.common.CdmDestinations;
import eu.etaxonomy.cdm.database.DbSchemaValidation;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.common.CdmDefaultImport;
import eu.etaxonomy.cdm.io.common.ICdmImport;
import eu.etaxonomy.cdm.io.common.IImportConfigurator;
import eu.etaxonomy.cdm.io.common.IImportConfigurator.CHECK;
import eu.etaxonomy.cdm.io.taxonx.TaxonXImportConfigurator;

/**
 * @author a.mueller
 * @created 20.06.2008
 * @version 1.0
 */
public class PalmaeTaxonXImportActivator {
	private static final Logger logger = Logger.getLogger(PalmaeTaxonXImportActivator.class);
	
	//database validation status (create, update, validate ...)
	static DbSchemaValidation hbm2dll = DbSchemaValidation.UPDATE;
	//static final String tcsSource = TcsSources.taxonX_local();
	static File source  = TcsSources.taxonX_localDir();
	static ICdmDataSource cdmDestination = CdmDestinations.localH2();
	
	static UUID secUuid = UUID.fromString("5f32b8af-0c97-48ac-8d33-6099ed68c625");
	
	//check - import
	static CHECK check = CHECK.IMPORT_WITHOUT_CHECK;
	
	static boolean doDescriptions = true;
	
	public boolean runImport(){
		boolean success = true;
		//make destination
		ICdmDataSource destination = cdmDestination;
		
		TaxonXImportConfigurator taxonXImportConfigurator = TaxonXImportConfigurator.NewInstance("", destination);
		// invoke import
		CdmDefaultImport<IImportConfigurator> cdmImport = new CdmDefaultImport<IImportConfigurator>();
		
		taxonXImportConfigurator.setSecUuid(secUuid);
		
		taxonXImportConfigurator.setDoFacts(doDescriptions);
		
		taxonXImportConfigurator.setCheck(check);
		taxonXImportConfigurator.setDbSchemaValidation(hbm2dll);

		TransactionStatus tx = cdmImport.getCdmApp().startTransaction();
				
		//new Test().invoke(tcsImportConfigurator);
		if (source.isDirectory()){
			
			for (File file : source.listFiles() ){
				if (file.isFile()){
					URL url;
					try {
						url = file.toURI().toURL();
						taxonXImportConfigurator.setSource(url.toString());
						String originalSourceId = file.getName();
						originalSourceId =originalSourceId.replace(".xml", "");
						taxonXImportConfigurator.setOriginalSourceId(originalSourceId);
						success &= cdmImport.invoke(taxonXImportConfigurator);
					} catch (MalformedURLException e) {
						logger.warn(e);
					}
				}
			}	
		}else{
			success &= cdmImport.invoke(taxonXImportConfigurator);
		}
		cdmImport.getCdmApp().commitTransaction(tx);		
		return success;
	}
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println("Start import from Source("+ source.toString() + ") ...");
		
		PalmaeTaxonXImportActivator importer = new PalmaeTaxonXImportActivator();
		importer.runImport();
		
		 
		System.out.println("End import from Source ("+ source.toString() + ")...");
	}

	
}
