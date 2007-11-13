/**
 * 
 */
package eu.etaxonomy.cdm.api.service;

import java.util.Properties;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.orm.hibernate3.LocalSessionFactoryBean;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import eu.etaxonomy.cdm.database.DatabaseType;

/**
 * @author a.mueller
 *
 */
public class DatabaseServiceHibernateImpl extends HibernateDaoSupport implements IDatabaseService {
	private static final Logger logger = Logger.getLogger(DatabaseServiceHibernateImpl.class);
	
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.service.IDatabaseService#connectToDatabase(eu.etaxonomy.cdm.database.DatabaseType, java.lang.String, java.lang.String, java.lang.String, int)
	 */
	public boolean connectToDatabase(DatabaseType databaseType, String url,
						String username, String password, int port) {
		DriverManagerDataSource aDataSource = getDataSource();
		SessionFactory aSessionFactory =  getSessionFactory();
		Session session;
		
		logger.info("Close" + aSessionFactory.getStatistics().getSessionCloseCount());
		logger.info("Connect" + aSessionFactory.getStatistics().getConnectCount());
		
		//close current session
//		session = aSessionFactory.openSession();
//		s.flush();
		
		//change connection
		aDataSource.setDriverClassName(databaseType.getDriverClassName());
		aDataSource.setUsername(username);
		aDataSource.setPassword(password);
		aDataSource.setUrl(url);
		//Properties prop = aDataSource.getConnectionProperties();
		
		//open session
		session = aSessionFactory.openSession();
		session.flush();
		logger.info("DataSource changed to " + aDataSource.getUrl());
		return true;
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
		DriverManagerDataSource ds = (DriverManagerDataSource)SessionFactoryUtils.getDataSource(getSessionFactory());
		return ds;
	}

}
