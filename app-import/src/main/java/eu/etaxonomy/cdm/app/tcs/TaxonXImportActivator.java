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
public class TaxonXImportActivator {
	private static Logger logger = Logger.getLogger(TaxonXImportActivator.class);
	
	//database validation status (create, update, validate ...)
	static DbSchemaValidation hbm2dll = DbSchemaValidation.CREATE;
	static final String tcsSource = TcsSources.taxonX_local();
	static final ICdmDataSource cdmDestination = CdmDestinations.localH2();
	
	static final UUID secUuid = UUID.fromString("5f32b8af-0c97-48ac-8d33-6099ed68c625");
	static final int sourceSecId = 7800000;
	static final File directory  = TcsSources.taxonX_localDir();
	
	//check - import
	static final CHECK check = CHECK.CHECK_AND_IMPORT;
	
	static final boolean doDescriptions = true;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println("Start import from Source("+ tcsSource.toString() + ") ...");
		
		//make BerlinModel Source
		String source = tcsSource;
		ICdmDataSource destination = cdmDestination;
		
		TaxonXImportConfigurator taxonXImportConfigurator = TaxonXImportConfigurator.NewInstance(source, destination);
		// invoke import
		ICdmImport<IImportConfigurator> cdmImport = new CdmDefaultImport<IImportConfigurator>();
		
		taxonXImportConfigurator.setSecUuid(secUuid);
		taxonXImportConfigurator.setSourceSecId(sourceSecId);
		
		taxonXImportConfigurator.setDoFacts(doDescriptions);
		
		taxonXImportConfigurator.setCheck(check);
		taxonXImportConfigurator.setDbSchemaValidation(hbm2dll);

		//new Test().invoke(tcsImportConfigurator);
		if (directory.isDirectory()){
			
			for (File file : directory.listFiles() ){
				URL url;
				try {
					url = file.toURI().toURL();
					taxonXImportConfigurator.setSource(url.toString());
					cdmImport.invoke(taxonXImportConfigurator);
				} catch (MalformedURLException e) {
					logger.warn(e);
				}
			}	
		}else{
			cdmImport.invoke(taxonXImportConfigurator);
		}
		 
		System.out.println("End import from Source ("+ source.toString() + ")...");
	}

	
}
