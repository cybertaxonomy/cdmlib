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

import eu.etaxonomy.cdm.app.berlinModelImport.BerlinModelSources;
import eu.etaxonomy.cdm.app.common.CdmDestinations;
import eu.etaxonomy.cdm.database.DatabaseTypeEnum;
import eu.etaxonomy.cdm.database.DbSchemaValidation;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.common.Source;


/**
 * TODO add the following to a wiki page:
 * HINT: If you are about to import into a mysql data base running under windows and if you wish to dump and restore the resulting data bas under another operation systen 
 * you must set the mysql system variable lower_case_table_names = 0 in order to create data base with table compatible names.
 * 
 * 
 * @author a.mueller
 *
 */
public class VibrantActivator {
	private static final Logger logger = Logger.getLogger(VibrantActivator.class);

	//database validation status (create, update, validate ...)
//	static DbSchemaValidation hbm2dll = DbSchemaValidation.CREATE;
	static DbSchemaValidation hbm2dll = DbSchemaValidation.VALIDATE;
	
	static final Source iopiSource = BerlinModelSources.iopi();
	static final Source mclSource = BerlinModelSources.mcl();
	static final Source emSource = BerlinModelSources.PESI3_euroMed();
	static final URI dioscoreaceaeSource = DwcaScratchpadImportActivator.dwca_emonocots_dioscoreaceae();
	static final URI cypripedioideaeSource = DwcaScratchpadImportActivator.dwca_emonocots_cypripedioideae();
	static final URI zingiberaceaeSource = DwcaScratchpadImportActivator.dwca_emonocots_zingiberaceae();
	
//	static final ICdmDataSource cdmDestination = CdmDestinations.cdm_test_local_mysql();
//	static final ICdmDataSource cdmDestination = cdm_test_local_vibrant();
//	static final ICdmDataSource cdmDestination = cdm_vibrant_dev();
	static final ICdmDataSource cdmDestination = cdm_vibrant_emonoctos_dev();
	

	static final boolean doMcl = false;
	static final boolean doEuroMed = false;
	static final boolean doIopi = false;
	static final boolean doDioscoreaceae = true;
	static final boolean doZingiberaceae = false;
	static final boolean doCypripedioideae = false;
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		ICdmDataSource cdmRepository = CdmDestinations.chooseDestination(args) != null ? CdmDestinations.chooseDestination(args) : cdmDestination;
		
		if (doMcl){
			MclActivator mclActivator = new MclActivator();
			mclActivator.importMcl(mclSource, cdmRepository, hbm2dll);
			hbm2dll = DbSchemaValidation.NONE;
		}
		
		if (doEuroMed){
			EuroMedActivator emActivator = new EuroMedActivator();
			emActivator.importEm2CDM(emSource, cdmRepository, hbm2dll);
			hbm2dll = DbSchemaValidation.NONE;
		}
		
		if (doIopi){
			IopiActivator iopiActivator = new IopiActivator();
			iopiActivator.importIopi(iopiSource, cdmRepository, hbm2dll);
			hbm2dll = DbSchemaValidation.NONE;
		}
		
		if (doDioscoreaceae){
			DwcaScratchpadImportActivator scratchpadActivator = new DwcaScratchpadImportActivator();
			UUID uuid = UUID.fromString("3bf59b32-9269-4225-944f-570256d40a9b");
			scratchpadActivator.doImport(dioscoreaceaeSource, cdmRepository,uuid , "Dioscoreaceae (Scratchpads)", hbm2dll);
			hbm2dll = DbSchemaValidation.NONE;
		}
		
		if (doZingiberaceae){
			DwcaScratchpadImportActivator scratchpadActivator = new DwcaScratchpadImportActivator();
			UUID uuid = UUID.fromString("8fb0f951-ccd8-41c4-8d0b-99ba1fbd2dc2");
			scratchpadActivator.doImport(zingiberaceaeSource, cdmRepository, uuid, " (Scratchpads)", hbm2dll);
			hbm2dll = DbSchemaValidation.NONE;
		}
		
		if (doCypripedioideae){
			DwcaScratchpadImportActivator scratchpadActivator = new DwcaScratchpadImportActivator();
			UUID uuid = UUID.fromString("a2b0ecf5-1a9d-4d94-a9ef-f57717a49bfd");
			scratchpadActivator.doImport(cypripedioideaeSource, cdmRepository, uuid, " (Scratchpads)", hbm2dll);
			hbm2dll = DbSchemaValidation.NONE;
		}
		
		if (doEuroMed){
			logger.warn("DON'T FORGET to reset E+M filter");
			System.out.println("DON'T FORGET to reset E+M filter");
		}

	}
	
	public static ICdmDataSource cdm_test_local_vibrant(){
		DatabaseTypeEnum dbType = DatabaseTypeEnum.MySQL;
		String cdmServer = "127.0.0.1";
		String cdmDB = "vibrant"; 
		String cdmUserName = "root";
		return CdmDestinations.makeDestination(dbType, cdmServer, cdmDB, -1, cdmUserName, null);
	}
	
	public static ICdmDataSource cdm_vibrant_dev(){
		DatabaseTypeEnum dbType = DatabaseTypeEnum.MySQL;
		String cdmServer = "160.45.63.201";
		String cdmDB = "cdm_vibrant_index"; 
		String cdmUserName = "edit";
		return CdmDestinations.makeDestination(dbType, cdmServer, cdmDB, -1, cdmUserName, null);
	}
	
	public static ICdmDataSource cdm_vibrant_emonoctos_dev(){
		DatabaseTypeEnum dbType = DatabaseTypeEnum.MySQL;
		String cdmServer = "160.45.63.201";
		String cdmDB = "cdm_vibrant_index_emonocots"; 
		String cdmUserName = "edit";
		return CdmDestinations.makeDestination(dbType, cdmServer, cdmDB, -1, cdmUserName, null);
	}

}
