/**
 * 
 */
package eu.etaxonomy.cdm.api.service;

import java.util.Iterator;
import java.util.Properties;

import javax.naming.NamingException;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.cfg.Configuration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.orm.hibernate3.LocalSessionFactoryBean;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.springframework.orm.hibernate3.annotation.AnnotationSessionFactoryBean;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import org.springframework.stereotype.Service;

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.database.CdmDataSource;
import eu.etaxonomy.cdm.database.DatabaseTypeEnum;
import eu.etaxonomy.cdm.database.types.MySQLDatabaseType;

/**
 * @author a.mueller
 *
 */
@Service
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
	public boolean connectToDatasource(CdmDataSource dataSource) {
		this.application.changeDataSource(dataSource);
		logger.debug("DataSource changed to " + dataSource.getName());
		return true;
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.service.IDatabaseService#connectToDatabase(eu.etaxonomy.cdm.database.DatabaseTypeEnum, java.lang.String, java.lang.String, java.lang.String, java.lang.String, int)
	 */
	public boolean connectToDatabase(DatabaseTypeEnum databaseTypeEnum, String server,
			String database, String username, String password, int port) {
		CdmDataSource tmpDataSource =  saveDataSource(TMP_DATASOURCE, databaseTypeEnum, server, database, username, password);
		boolean result = connectToDatasource(tmpDataSource);
		CdmDataSource.delete(tmpDataSource);
		return result;
	}


	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.service.IDatabaseService#connectToDatabase(eu.etaxonomy.cdm.database.DatabaseTypeEnum, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	public boolean connectToDatabase(DatabaseTypeEnum databaseTypeEnum, String server,
			String database, String username, String password) {
		return connectToDatabase(databaseTypeEnum, server, database, username, password, databaseTypeEnum.getDefaultPort()) ;
	}


	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.service.IDatabaseService#saveDataSource(eu.etaxonomy.cdm.database.DatabaseTypeEnum, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	public CdmDataSource saveDataSource(String strDataSourceName, DatabaseTypeEnum databaseTypeEnum,
			String server, String database, String username, String password) {
		return CdmDataSource.save(strDataSourceName, databaseTypeEnum, server, database, username, password);
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

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.service.IDatabaseService#useLocalHsqldb()
	 */
	public boolean useLocalHsqldb() {
		logger.error("Method not yet implemented");
		return false;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.service.IDatabaseService#useLocalHsqldb(java.lang.String, java.lang.String, boolean, boolean)
	 */
	public boolean useLocalHsqldb(String path, String databaseName,
			boolean silent, boolean startServer) {
		// TODO Auto-generated method stub
		logger.error("Method not yet implemented");
		return false;
	}
	

	//returns the DriverManagerDataSource from hibernate
	private DriverManagerDataSource getDataSource(){
		DriverManagerDataSource ds = (DriverManagerDataSource)SessionFactoryUtils.getDataSource(factory);
		return ds;
	}



}
