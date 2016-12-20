/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.api.service;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.jdbc.datasource.AbstractDriverBasedDataSource;
import org.springframework.orm.hibernate5.SessionFactoryUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.config.CdmPersistentSourceUtils;
import eu.etaxonomy.cdm.config.CdmSourceException;
import eu.etaxonomy.cdm.database.CdmDataSource;
import eu.etaxonomy.cdm.database.CdmPersistentDataSource;
import eu.etaxonomy.cdm.database.DataSourceNotFoundException;
import eu.etaxonomy.cdm.database.DatabaseTypeEnum;
import eu.etaxonomy.cdm.database.H2Mode;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.model.common.init.TermNotFoundException;
import eu.etaxonomy.cdm.model.metadata.CdmMetaData.MetaDataPropertyName;



/**
 * Implementation of service which provides functionality to directly access database
 * related information.
 *
 * @author a.mueller
 *
 */
@Service
@Transactional(readOnly = true)
public class DatabaseServiceHibernateImpl  implements IDatabaseService, ApplicationContextAware {
	private static final Logger logger = Logger.getLogger(DatabaseServiceHibernateImpl.class);

	private static final String TMP_DATASOURCE = "tmp";

	@Autowired
	private SessionFactory factory;

	@Autowired
	protected ApplicationContext appContext;

	private CdmApplicationController application;

	@Override
    public void setApplicationController(CdmApplicationController cdmApplicationController){
		this.application = cdmApplicationController;
	}

	@Override
    public boolean connectToDatasource(CdmPersistentDataSource dataSource) throws TermNotFoundException{
		this.application.changeDataSource(dataSource);
		logger.debug("DataSource changed to " + dataSource.getName());
		return true;
	}

	@Override
    public boolean connectToDatabase(DatabaseTypeEnum databaseTypeEnum, String server,
			String database, String username, String password, int port, String filePath, H2Mode mode) throws TermNotFoundException  {
		ICdmDataSource dataSource = CdmDataSource.NewInstance(databaseTypeEnum, server, database, port, username, password);
		CdmPersistentDataSource tmpDataSource =  saveDataSource(TMP_DATASOURCE, dataSource);
		boolean result = connectToDatasource(tmpDataSource);
		CdmPersistentSourceUtils.delete(tmpDataSource);
		return result;
	}

	@Override
    public boolean connectToDatabase(DatabaseTypeEnum databaseTypeEnum, String server,
			String database, String username, String password)  throws TermNotFoundException {
		return connectToDatabase(databaseTypeEnum, server, database, username, password, databaseTypeEnum.getDefaultPort(), null, null) ;
	}

	@Override
    public CdmPersistentDataSource saveDataSource(String strDataSourceName,
			ICdmDataSource dataSource) {
		return CdmPersistentDataSource.save(strDataSourceName, dataSource);
	}

	@Override
    public CdmPersistentDataSource updateDataSource(String strDataSourceName,
			CdmPersistentDataSource dataSource) throws DataSourceNotFoundException {
		return CdmPersistentDataSource.update(strDataSourceName, dataSource);
	}

	@Override
    public String getUrl() {
		return getDataSource().getUrl();
	}

	@Override
    public String getUsername() {
		return getDataSource().getUsername();
	}

	/**
	 * Returns the AbstractDriverBasedDataSource from hibernate,
	 * generalized in order to also allow using SimpleDriverDataSource.
	 *
	 * @return the AbstractDriverBasedDataSource from the hibernate layer
	 */
	private AbstractDriverBasedDataSource getDataSource(){
		AbstractDriverBasedDataSource ds = (AbstractDriverBasedDataSource)SessionFactoryUtils.getDataSource(factory);
		return ds;
	}

	@Override
    public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		this.appContext = applicationContext;
	}

	@Override
	public  String getDbSchemaVersion() throws CdmSourceException  {
		try {
			return (String)getSingleValue(MetaDataPropertyName.DB_SCHEMA_VERSION.getSqlQuery());
		} catch (SQLException e) {
			throw new CdmSourceException(e.getMessage());
		}
	}

	@Override
	public boolean isDbEmpty() throws CdmSourceException {
		// Any CDM DB should have a schema version
		String dbSchemaVersion = getDbSchemaVersion();
		return (dbSchemaVersion == null || dbSchemaVersion.equals(""));
	}

    /**
     * Execute a SQL query which returns a single value
     *
     * @param query , which returns a single value
     * @return
     * @throws SQLException
     */
    private Object getSingleValue(String query) throws SQLException {
        String queryString = query == null? "(null)": query;
        //ResultSet resultSet = executeQuery(query);
        ResultSet resultSet = null;

        Connection connection = SessionFactoryUtils.getDataSource(factory).getConnection();
        if (connection != null){

            Statement statement = connection.createStatement();
            resultSet = statement.executeQuery(query);

            if (resultSet == null || resultSet.next() == false){
                logger.info("No record returned for query " +  queryString);
                return null;
            }
            if (resultSet.getMetaData().getColumnCount() != 1){
                logger.info("More than one column selected in query" +  queryString);
                //first value will be taken
            }
            Object object = resultSet.getObject(1);
            if (resultSet.next()){
                logger.info("Multiple results for query " +  queryString);
                //first row will be taken
            }
            // making sure we close all resources so we don't run out of
            // connections in the connection pool
            resultSet.close();
            statement.close();
            connection.close();

            return object;
        }else{
            throw new RuntimeException("Could not establish connection to database");
        }

    }


	@Override
	public Map<MetaDataPropertyName, String> getCdmMetadataMap() throws CdmSourceException {
		Map<MetaDataPropertyName, String> cdmMetaDataMap = new HashMap<MetaDataPropertyName, String>();

		for(MetaDataPropertyName mdpn : MetaDataPropertyName.values()){
			String value = null;
			try {
				value = (String)getSingleValue(mdpn.getSqlQuery());
			} catch (SQLException e) {
				throw new CdmSourceException(e.getMessage());
			}
			if(value != null) {
				cdmMetaDataMap.put(mdpn, value);
			}
		}
		return cdmMetaDataMap;
	}

}
