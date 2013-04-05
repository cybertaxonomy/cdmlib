// $Id$
/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.api.service;

import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.jdbc.datasource.AbstractDriverBasedDataSource;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.database.CdmDataSource;
import eu.etaxonomy.cdm.database.CdmPersistentDataSource;
import eu.etaxonomy.cdm.database.DataSourceNotFoundException;
import eu.etaxonomy.cdm.database.DatabaseTypeEnum;
import eu.etaxonomy.cdm.database.H2Mode;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.model.common.init.TermNotFoundException;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;



/**
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
	public void setApplicationController(CdmApplicationController cdmApplicationController){
		this.application = cdmApplicationController;
	}
	
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.service.IDatabaseService#connectToDatasource(eu.etaxonomy.cdm.database.CdmDataSource)
	 */
	public boolean connectToDatasource(CdmPersistentDataSource dataSource) throws TermNotFoundException{
		this.application.changeDataSource(dataSource);
		logger.debug("DataSource changed to " + dataSource.getName());
		return true;
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.service.IDatabaseService#connectToDatabase(eu.etaxonomy.cdm.database.DatabaseTypeEnum, java.lang.String, java.lang.String, java.lang.String, java.lang.String, int)
	 */
	public boolean connectToDatabase(DatabaseTypeEnum databaseTypeEnum, String server,
			String database, String username, String password, int port, String filePath, H2Mode mode, NomenclaturalCode code) throws TermNotFoundException  {
		ICdmDataSource dataSource = CdmDataSource.NewInstance(databaseTypeEnum, server, database, port, username, password, code);
		CdmPersistentDataSource tmpDataSource =  saveDataSource(TMP_DATASOURCE, dataSource);
		boolean result = connectToDatasource(tmpDataSource);
		CdmPersistentDataSource.delete(tmpDataSource);
		return result;
	}


	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.service.IDatabaseService#connectToDatabase(eu.etaxonomy.cdm.database.DatabaseTypeEnum, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	public boolean connectToDatabase(DatabaseTypeEnum databaseTypeEnum, String server,
			String database, String username, String password)  throws TermNotFoundException {
		return connectToDatabase(databaseTypeEnum, server, database, username, password, databaseTypeEnum.getDefaultPort(), null, null, null) ;
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.service.IDatabaseService#saveDataSource(java.lang.String, eu.etaxonomy.cdm.database.ICdmDataSource)
	 */
	public CdmPersistentDataSource saveDataSource(String strDataSourceName,
			ICdmDataSource dataSource) {
		return CdmPersistentDataSource.save(strDataSourceName, dataSource);
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.service.IDatabaseService#updateDataSource(java.lang.String, eu.etaxonomy.cdm.database.CdmPersistentDataSource)
	 */
	public CdmPersistentDataSource updateDataSource(String strDataSourceName,
			CdmPersistentDataSource dataSource) throws DataSourceNotFoundException {
		return CdmPersistentDataSource.update(strDataSourceName, dataSource);
	}

	
//TODO removed 04.02.2009 as spring 2.5.6 does not support DriverManagerDataSource.getDriverClassName anymore
//Let's see if this is not needed by any other application
//	/* (non-Javadoc)
//	 * @see eu.etaxonomy.cdm.api.service.IDatabaseService#getDatabaseTypeName()
//	 */
//	public DatabaseTypeEnum getDatabaseEnum() {
//		return DatabaseTypeEnum.getDatabaseEnumByDriverClass(getDataSource().getDriverClassName());
//	}
//
//	/* (non-Javadoc)
//	 * @see eu.etaxonomy.cdm.api.service.IDatabaseService#getDriverClassName()
//	 */
//	public String getDriverClassName() {
//		return ( getDataSource()).getDriverClassName();
//	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.service.IDatabaseService#getUrl()
	 */
	public String getUrl() {
		return getDataSource().getUrl();
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.service.IDatabaseService#getUsername()
	 */
	public String getUsername() {
		return getDataSource().getUsername();
	}

    //  returns the AbstractDriverBasedDataSource from hibernate 
	// (generalized in order to also allow using SimpleDriverDataSource)
	private AbstractDriverBasedDataSource getDataSource(){
		AbstractDriverBasedDataSource ds = (AbstractDriverBasedDataSource)SessionFactoryUtils.getDataSource(factory);
		return ds;
	}


	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		this.appContext = applicationContext;
	}
}
