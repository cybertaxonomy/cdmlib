/**
 * Copyright (C) 2007 EDIT
 * European Distributed Institute of Taxonomy 
 * http://www.e-taxonomy.eu
 * 
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */

package eu.etaxonomy.cdm.app.sdd;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.SQLException;

import junit.framework.Assert;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.app.common.CdmDestinations;
import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.database.CdmDataSource;
import eu.etaxonomy.cdm.database.CdmPersistentDataSource;
import eu.etaxonomy.cdm.database.DataSourceNotFoundException;
import eu.etaxonomy.cdm.database.DbSchemaValidation;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.common.CdmDefaultImport;
import eu.etaxonomy.cdm.io.common.IImportConfigurator.CHECK;
import eu.etaxonomy.cdm.io.sdd.in.SDDImportConfigurator;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;

/**
 * @author h.fradin
 * @author l.morris
 * @created 24.10.2008
 * @version 1.0
 */
public class SDDImportActivator {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(SDDImportActivator.class);

	//database validation status (create, update, validate ...)
	static DbSchemaValidation hbm2dll = DbSchemaValidation.CREATE;
	//static final String sddSource = SDDSources.viola_local_andreas();
	//	static final ICdmDataSource cdmDestination = CdmDestinations.cdm_test_andreasM2();

	//	static final ICdmDataSource cdmDestination = CdmDestinations.cdm_portal_test_localhost();

	static final String sourceSecId = "viola_pub_ed_999999";

	//check - import
	static final CHECK check = CHECK.IMPORT_WITHOUT_CHECK;


    private static ICdmDataSource customDataSource() {

        CdmPersistentDataSource loadedDataSource = null;
       //ICdmDataSource dataSource = CdmDataSource.NewMySqlInstance("192.168.2.10", "cdm_test_niels2", 3306, "edit", password, code);
       String dataSourceName = CdmUtils.readInputLine("Database name: ");
       String username = CdmUtils.readInputLine("Username: ");
       String password = CdmUtils.readInputLine("Password: ");
       
       dataSourceName = (dataSourceName.equals("")) ? "cdm_test4" : dataSourceName;
       username = (username.equals("")) ? "ljm" : username;
       
       ICdmDataSource dataSource = CdmDataSource.NewMySqlInstance("127.0.0.1", dataSourceName, 3306, username, password, NomenclaturalCode.ICBN);
       //ICdmDataSource dataSource = CdmDataSource.NewMySqlInstance("127.0.0.1", "cdm_edit_cichorieae", 3306, "ljm", password, NomenclaturalCode.ICBN);
       //ICdmDataSource dataSource = CdmDataSource.NewMySqlInstance("160.45.63.201", "cdm_edit_cichorieae", 3306, "edit", password, NomenclaturalCode.ICBN);
       boolean connectionAvailable;
       try {
           connectionAvailable = dataSource.testConnection();
           logger.debug("LORNA connection avaiable " + connectionAvailable);
           Assert.assertTrue("Testdatabase is not available", connectionAvailable);

       } catch (ClassNotFoundException e1) {
           // TODO Auto-generated catch block
           e1.printStackTrace();
       } catch (SQLException e1) {
           // TODO Auto-generated catch block
           e1.printStackTrace();
       }

       CdmPersistentDataSource.save(dataSourceName, dataSource);
       try {
           loadedDataSource = CdmPersistentDataSource.NewInstance(dataSourceName);
//			CdmApplicationController.NewInstance(loadedDataSource, DbSchemaValidation.CREATE);
           NomenclaturalCode loadedCode = loadedDataSource.getNomenclaturalCode();

           Assert.assertEquals(NomenclaturalCode.ICBN, loadedCode);
       } catch (DataSourceNotFoundException e) {
           // TODO Auto-generated catch block
           e.printStackTrace();
       }
       //return loadedDataSource;
       return dataSource;
   }
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		//String sddSource = SDDSources.SDDImport_local(args[0]+args[1]);
		//System.out.println("Start import from SDD("+ sddSource.toString() + ") ...");

		//make Source
		URI source;
		try {
			//source = new URI(sddSource);
	        //URL url = SDDImportActivator.class.getResource("/eu/etaxonomy/cdm/io/sdd/SDDImportTest-input3.xml");
	        //URL url = SDDImportActivator.class.getResource("/eu/etaxonomy/cdm/app/sdd/SDDImportTest-input3.xml"); //eu.etaxonomy.cdm.app.sdd;
	        //URL url = SDDImportActivator.class.getResource("/sdd/SDD-Test-Simple.xml");
	        URL url = SDDImportActivator.class.getResource("/sdd/ant.sdd.xml");
	        //sdd/SDD-Test-Simple.xml
	        System.out.println("url"+ url);
	        source = url.toURI();
			//source = new URI("/eu/etaxonomy/cdm/app/sdd/SDDImportTest-input3.xml");
			System.out.println("Start import from SDD("+ source.toString() + ") ...");
		
		//	ICdmDataSource destination = CdmDestinations.localH2("cdm","sa","C:/Documents and Settings/lis/Mes documents/CDMtest/");
			//lorna//ICdmDataSource destination = CdmDestinations.localH2(args[3],"sa",args[2]);
			ICdmDataSource destination = customDataSource();
	
			SDDImportConfigurator sddImportConfigurator = SDDImportConfigurator.NewInstance(source,  destination);
	
			///sddImportConfigurator.setSourceSecId(sourceSecId);
	
			///sddImportConfigurator.setCheck(check);
			///sddImportConfigurator.setDbSchemaValidation(hbm2dll);
	
			// invoke import
			CdmDefaultImport<SDDImportConfigurator> sddImport = new CdmDefaultImport<SDDImportConfigurator>();
			sddImport.invoke(sddImportConfigurator);
	
			System.out.println("End import from SDD ("+ source.toString() + ")...");
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}

	}


}
