/**
 * 
 */
package eu.etaxonomy.cdm.api.service;

import java.lang.reflect.Proxy;

import org.apache.log4j.Logger;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.orm.hibernate3.LocalSessionFactoryBean;
import org.springframework.orm.hibernate3.annotation.AnnotationSessionFactoryBean;

import eu.etaxonomy.cdm.database.DbType;

/**
 * @author a.mueller
 *
 */
public class DatabaseServiceImpl implements IDatabaseService {
	private static final Logger logger = Logger.getLogger(DatabaseServiceImpl.class);
	
	private Object sessionFactory;
//	
//	public void setDataSource(DriverManagerDataSource dataSource){
//		this.dataSource = dataSource; 
//	}
	
	public void setSessionFactory(Object sessionFactory){
		this.sessionFactory = sessionFactory; 
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.service.IDatabaseService#connectToDatabase(eu.etaxonomy.cdm.database.DbType, java.lang.String, java.lang.String, java.lang.String, int)
	 */
	public boolean connectToDatabase(DbType dbType, String url,
			String username, String password, int port) {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.service.IDatabaseService#getDriverClassName()
	 */
	public String getDriverClassName() {
		logger.info(((LocalSessionFactoryBean) this.sessionFactory).getHibernateProperties().getProperty("dataSource"));
		//.getDriverClassName());
		//HibernateTemplate ht = 
		// TODO Auto-generated method stub
		
		return null;
//		return this.sessionFactory.get  .getDriverClassName();
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.service.IDatabaseService#getUrl()
	 */
	public String getUrl() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.service.IDatabaseService#getUsername()
	 */
	public String getUsername() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.service.IDatabaseService#useLocalHsqldb()
	 */
	public boolean useLocalHsqldb() {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.service.IDatabaseService#useLocalHsqldb(java.lang.String, java.lang.String, boolean, boolean)
	 */
	public boolean useLocalHsqldb(String path, String databaseName,
			boolean silent, boolean startServer) {
		// TODO Auto-generated method stub
		return false;
	}

}
