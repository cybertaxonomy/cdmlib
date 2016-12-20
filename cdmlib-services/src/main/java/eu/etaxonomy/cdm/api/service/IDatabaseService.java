/**
 * Copyright (C) 2007 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */

package eu.etaxonomy.cdm.api.service;

import java.util.Map;

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.config.CdmSourceException;
import eu.etaxonomy.cdm.database.CdmPersistentDataSource;
import eu.etaxonomy.cdm.database.DataSourceNotFoundException;
import eu.etaxonomy.cdm.database.DatabaseTypeEnum;
import eu.etaxonomy.cdm.database.H2Mode;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.model.common.init.TermNotFoundException;
import eu.etaxonomy.cdm.model.metadata.CdmMetaData.MetaDataPropertyName;

/**
 * Service interface which provides functionality to directly access database
 * related information.
 *
 * @author a.mueller
 *
 */
public interface IDatabaseService {

	/**
	 * Returns the database URL
	 * @return
	 */
	public String getUrl();

	/**
	 * Returns the username.
	 * @return
	 */
	public String getUsername();

	/**
	 * Connect to the database with the given parameters
	 * @param databaseTypeEnum
	 * @param url
	 * @param username
	 * @param password
	 * @param port
	 * @return returns true if successful
	 */
	public boolean connectToDatabase(DatabaseTypeEnum databaseTypeEnum, String server,
			String database, String username, String password, int port, String filePath, H2Mode mode)  throws TermNotFoundException ;

	/**
	 * Connect to the database with the given parameters. Uses default port.
	 * @param databaseTypeEnum
	 * @param url
	 * @param username
	 * @param password
	 * @return returns true if successful
	 */
	public boolean connectToDatabase(DatabaseTypeEnum databaseTypeEnum, String server, String database, String username, String password)  throws TermNotFoundException;


	/**
	 * Connect to the database with the given parameters. Uses default port.
	 * @param dataSource
	 * @return returns true if successful
	 */
	public boolean connectToDatasource(CdmPersistentDataSource dataSource) throws TermNotFoundException;

	/**
	 * Saves a new ICdmDatasource into the datasource config file.
	 *
	 * @param strDataSourceName
	 * @param dataSource
	 * @param code
	 * @return
	 */
	public CdmPersistentDataSource saveDataSource(String strDataSourceName, ICdmDataSource dataSource);

	/**
	 * Update an already saved datasource in datasource config file
	 * @param strDataSourceName
	 * 			the beanId under which the dataSource should be saved
	 * @param dataSource
	 * 			the dataSource to be saved
	 * @return
	 * 			the CdmDataSource, null if not successful
	 * @throws DataSourceNotFoundException
	 */
	public CdmPersistentDataSource updateDataSource(String strDataSourceName, CdmPersistentDataSource dataSource) throws DataSourceNotFoundException;

	/**
	 * @param cdmApplicationController
	 */
	public void setApplicationController(CdmApplicationController cdmApplicationController);

	/**
	 * Returns the CDM model schema version number
	 *
	 * @return the CDM model schema version number
	 * @throws CdmSourceException , incase of an underlying SQL error
	 */
	public  String getDbSchemaVersion() throws CdmSourceException;

	/**
	 * Returns a boolean flag to indicate whether the database is empty
	 *
	 * @return boolean flag to indicate whether the database is  empty
	 * @throws CdmSourceException , incase of an underlying SQL error
	 */
	public boolean isDbEmpty() throws CdmSourceException;


	public Map<MetaDataPropertyName, String> getCdmMetadataMap() throws CdmSourceException;

}
