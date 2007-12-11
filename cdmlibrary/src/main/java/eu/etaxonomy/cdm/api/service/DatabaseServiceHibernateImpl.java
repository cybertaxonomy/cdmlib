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
import org.hibernate.impl.SessionFactoryImpl;
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
import org.springframework.transaction.annotation.Transactional;

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.database.CdmDataSource;
import eu.etaxonomy.cdm.database.DatabaseTypeEnum;
import eu.etaxonomy.cdm.database.LocalHsqldb;
import eu.etaxonomy.cdm.database.types.MySQLDatabaseType;
import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.location.Continent;
import eu.etaxonomy.cdm.model.location.WaterbodyOrCountry;
import eu.etaxonomy.cdm.model.name.HybridRelationshipType;
import eu.etaxonomy.cdm.model.name.NameRelationshipType;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatusType;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TypeDesignationStatus;
import eu.etaxonomy.cdm.model.taxon.ConceptRelationshipType;
import eu.etaxonomy.cdm.model.taxon.SynonymRelationshipType;

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
	 * @see eu.etaxonomy.cdm.api.service.IDatabaseService#useLocalHsqldb(java.lang.String, java.lang.String, boolean, boolean)
	 */
	public CdmDataSource saveLocalHsqldb(String strDataSourceName, String databasePath, String databaseName, String username, String password, boolean silent, boolean startServer) {
		return CdmDataSource.saveLocalHsqlDb(strDataSourceName, databasePath, databaseName, username, password);
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.service.IDatabaseService#useLocalHsqldb()
	 */
	public boolean useLocalDefaultHsqldb() {
			CdmDataSource dataSource = CdmDataSource.NewLocalHsqlInstance();
			return connectToDatasource(dataSource);
	}


	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.service.IDatabaseService#useLocalHsqldb(java.lang.String, java.lang.String, boolean, boolean)
	 */
	public boolean useLocalHsqldb(String databasePath, String databaseName, String username, String password, boolean silent, boolean startServer) {
		CdmDataSource dataSource = saveLocalHsqldb("tmpHsqlDb", databasePath, databaseName, username, password, silent, startServer);
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
