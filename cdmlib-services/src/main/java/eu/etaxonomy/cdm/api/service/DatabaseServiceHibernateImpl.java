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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.database.CdmPersistentDataSource;
import eu.etaxonomy.cdm.database.DatabaseTypeEnum;
import eu.etaxonomy.cdm.model.common.init.TermNotFoundException;



/**
 * @author a.mueller
 *
 */
@Service
@Transactional
public class DatabaseServiceHibernateImpl extends ServiceBase implements IDatabaseService, ApplicationContextAware {
	private static final Logger logger = Logger.getLogger(DatabaseServiceHibernateImpl.class);
	
	private static final String TMP_DATASOURCE = "tmp"; 
	
	@Autowired
	private SessionFactory factory;
	
	@Autowired
	protected ApplicationContext appContext;
//	public void setApplicationContext(ApplicationContext appContext){
//		this.appContext = appContext;
//	}
	
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
			String database, String username, String password, int port) throws TermNotFoundException  {
		CdmPersistentDataSource tmpDataSource =  saveDataSource(TMP_DATASOURCE, databaseTypeEnum, server, database, username, password);
		boolean result = connectToDatasource(tmpDataSource);
		CdmPersistentDataSource.delete(tmpDataSource);
		return result;
	}


	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.service.IDatabaseService#connectToDatabase(eu.etaxonomy.cdm.database.DatabaseTypeEnum, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	public boolean connectToDatabase(DatabaseTypeEnum databaseTypeEnum, String server,
			String database, String username, String password)  throws TermNotFoundException {
		return connectToDatabase(databaseTypeEnum, server, database, username, password, databaseTypeEnum.getDefaultPort()) ;
	}


	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.service.IDatabaseService#saveDataSource(eu.etaxonomy.cdm.database.DatabaseTypeEnum, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	public CdmPersistentDataSource saveDataSource(String strDataSourceName, DatabaseTypeEnum databaseTypeEnum,
			String server, String database, String username, String password) throws TermNotFoundException  {
		return CdmPersistentDataSource.save(strDataSourceName, databaseTypeEnum, server, database, username, password);
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.service.IDatabaseService#useLocalHsqldb(java.lang.String, java.lang.String, boolean, boolean)
	 */
	public CdmPersistentDataSource saveLocalHsqldb(String strDataSourceName, String databasePath, String databaseName, String username, String password, boolean silent, boolean startServer) {
		return CdmPersistentDataSource.saveLocalHsqlDb(strDataSourceName, databasePath, databaseName, username, password);
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.service.IDatabaseService#useLocalHsqldb()
	 */
	public boolean useLocalDefaultHsqldb()  throws TermNotFoundException{
		CdmPersistentDataSource dataSource = CdmPersistentDataSource.NewLocalHsqlInstance();
			return connectToDatasource(dataSource);
	}


	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.service.IDatabaseService#useLocalHsqldb(java.lang.String, java.lang.String, boolean, boolean)
	 */
	public boolean useLocalHsqldb(String databasePath, String databaseName, String username, String password, boolean silent, boolean startServer) 
				throws TermNotFoundException{
		CdmPersistentDataSource dataSource = saveLocalHsqldb("tmpHsqlDb", databasePath, databaseName, username, password, silent, startServer);
		return connectToDatasource(dataSource);
	}
	

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.service.IDatabaseService#getDatabaseTypeName()
	 */
	public DatabaseTypeEnum getDatabaseEnum() {
		return DatabaseTypeEnum.getDatabaseEnumByDriverClass(getDataSource().getDriverClassName());
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.service.IDatabaseService#getDriverClassName()
	 */
	public String getDriverClassName() {
		return getDataSource().getDriverClassName();
	}

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


	//returns the DriverManagerDataSource from hibernate
	private DriverManagerDataSource getDataSource(){
		DriverManagerDataSource ds = (DriverManagerDataSource)SessionFactoryUtils.getDataSource(factory);
		return ds;
	}



//	public void createDatabase(){
//		this.connectToDatabase(databaseTypeEnum, server, database, username, password)
//		
//		
//		loader.loadDefaultTerms(WaterbodyOrCountry.class);
//		loader.loadDefaultTerms(Language.class);
//		loader.loadDefaultTerms(Continent.class);
//		loader.loadDefaultTerms(Rank.class);
//		loader.loadDefaultTerms(TypeDesignationStatus.class);
//		loader.loadDefaultTerms(NomenclaturalStatusType.class);
//		loader.loadDefaultTerms(SynonymRelationshipType.class);
//		loader.loadDefaultTerms(HybridRelationshipType.class);
//		loader.loadDefaultTerms(NameRelationshipType.class);
//		loader.loadDefaultTerms(ConceptRelationshipType.class);
//	
//	}


}
