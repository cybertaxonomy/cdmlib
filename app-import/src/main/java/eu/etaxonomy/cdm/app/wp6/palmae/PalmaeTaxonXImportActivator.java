/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.app.wp6.palmae;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.log4j.Logger;
import org.springframework.transaction.TransactionStatus;

import eu.etaxonomy.cdm.app.common.CdmDestinations;
import eu.etaxonomy.cdm.database.DbSchemaValidation;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.common.CdmDefaultImport;
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
	//static File source  = TcsSources.taxonX_localDir();
	static File source  = new File("target/classes/taxonX");
	static ICdmDataSource cdmDestination = CdmDestinations.localH2Palmae();
	
	//check - import
	static CHECK check = CHECK.IMPORT_WITHOUT_CHECK;
	
	static boolean doDescriptions = true;
	static boolean doNomenclature = true;
	static boolean doMods = true;
	
	
	public boolean runImport(){
		boolean success = true;
		//make destination
		ICdmDataSource destination = cdmDestination;
		
		TaxonXImportConfigurator taxonXImportConfigurator = TaxonXImportConfigurator.NewInstance(null, destination);
		// invoke import
		CdmDefaultImport<IImportConfigurator> cdmImport = new CdmDefaultImport<IImportConfigurator>();
		
		taxonXImportConfigurator.setDoFacts(doDescriptions);
		taxonXImportConfigurator.setDoTypes(doNomenclature);
		taxonXImportConfigurator.setDoMods(doMods);
		
		taxonXImportConfigurator.setCheck(check);
		taxonXImportConfigurator.setDbSchemaValidation(hbm2dll);

		cdmImport.startController(taxonXImportConfigurator, destination);
				
		//new Test().invoke(tcsImportConfigurator);
		if (source.isDirectory()){
			makeDirectory(cdmImport, taxonXImportConfigurator, source);
		}else{
			try {
				success &= importFile(cdmImport, taxonXImportConfigurator, source);
			} catch (URISyntaxException e) {
				success = false;
				e.printStackTrace();
			}
		}
		return success;
	}
	
	private boolean makeDirectory(CdmDefaultImport<IImportConfigurator> cdmImport, TaxonXImportConfigurator taxonXImportConfigurator, File source){
		boolean success = true;
		int count = 0;
		for (File file : source.listFiles() ){
			if (file.isFile()){
				doCount(count++, 300, "Files");
				try {
					success &= importFile(cdmImport, taxonXImportConfigurator, file);
				} catch (URISyntaxException e) {
					success = false;
					e.printStackTrace();
				}
			}else{
				if (! file.getName().startsWith(".")){
					makeDirectory(cdmImport, taxonXImportConfigurator, file);
				}
			}
		}
		return success;
	}
	
	private boolean importFile(CdmDefaultImport<IImportConfigurator> cdmImport, 
				TaxonXImportConfigurator config, File file) throws URISyntaxException{
		boolean success = true;
		try{
			URL url = file.toURI().toURL();
			config.setSource(url.toURI());
			String originalSourceId = file.getName();
			originalSourceId =originalSourceId.replace(".xml", "");
			logger.debug(originalSourceId);
			config.setOriginalSourceId(originalSourceId);
			TransactionStatus tx = cdmImport.getCdmAppController().startTransaction();
			success &= cdmImport.invoke(config);
			cdmImport.getCdmAppController().commitTransaction(tx);		
			return success;			
		} catch (MalformedURLException e) {
			logger.warn(e);
			return false;
		}
	}
	
	protected void doCount(int count, int modCount, String pluralString){
		if ((count % modCount ) == 0 && count!= 0 ){ logger.info(pluralString + " handled: " + (count));}
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
