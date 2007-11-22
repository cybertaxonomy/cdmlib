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

import eu.etaxonomy.cdm.database.DatabaseEnum;
import eu.etaxonomy.cdm.database.types.MySQLDatabaseType;

/**
 * @author a.mueller
 *
 */
@Service
public class DatabaseServiceHibernateImpl extends ServiceBase implements IDatabaseService, ApplicationContextAware {
	private static final Logger logger = Logger.getLogger(DatabaseServiceHibernateImpl.class);

	@Autowired
	private SessionFactory factory;
	
	protected ApplicationContext appContext;
	
	public void setApplicationContext(ApplicationContext appContext){
		this.appContext = appContext;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.service.IDatabaseService#connectToDatabase(eu.etaxonomy.cdm.database.DatabaseEnum, java.lang.String, java.lang.String, java.lang.String, java.lang.String, int)
	 */
	public boolean connectToDatabase(DatabaseEnum databaseEnum, String server,
			String database, String username, String password, int port) {
		Session session;
		DriverManagerDataSource aDataSource = getDataSource();
		//HibernateTemplate ht = getHibernateTemplate();
		AnnotationSessionFactoryBean sfb = (AnnotationSessionFactoryBean)this.appContext.getBean("&sessionFactory");
		SessionFactory aSessionFactory =  factory; //getSessionFactory();
		
		Object o;
		//flush
		//ht.flush();
		
		//close everything
		//aSessionFactory.close();
		
		//set Dialect
		Properties props = sfb.getHibernateProperties();
		props.setProperty("hibernate.dialect", databaseEnum.getHibernateDialect());
		System.out.println(props.propertyNames());
		System.out.println(props.getProperty("hibernate.dialect"));
		
		//change Datasource
		aDataSource.setDriverClassName(databaseEnum.getDriverClassName());
		aDataSource.setUsername(username);
		aDataSource.setPassword(password);
		aDataSource.setUrl(databaseEnum.getConnectionString(server, database, port));
		
		//update schema
		sfb.updateDatabaseSchema();
		
//		DriverManagerDataSource ds = new DriverManagerDataSource();
//		ds.setDriverClassName(databaseEnum.getDriverClassName());
//		ds.setUsername(username);
//		ds.setPassword(password);
//		ds.setUrl(databaseEnum.getConnectionString(server, database, port));
//		sfb.setDataSource(ds);
		Configuration cfg = sfb.getConfiguration();
		//Object dsProp = cfg.getProperties();
		//System.out.println(dsProp);
		//		cfg.setProperties(sfb.getHibernateProperties());
//		
		cfg.setProperty("hibernate.connection.driver_class", databaseEnum.getDriverClassName());
		cfg.setProperty("hibernate.connection.url", databaseEnum.getConnectionString(server, database, port));
		cfg.setProperty("hibernate.connection.username", username);
		cfg.setProperty("hibernate.connection.password", password);
		cfg.setProperty("hibernate.dialect", databaseEnum.getHibernateDialect());
		
		Properties ps = cfg.getProperties();
		Iterator<Object> it = 	ps.keySet().iterator();
		
		while (it.hasNext()){
			String pn = (String)it.next();
			if (pn.contains("hibernate")){ 
				System.out.println(pn + ": " + ps.getProperty(pn));
			}
		}
		
		
		SessionFactory sf = cfg.buildSessionFactory();
		//ht.setSessionFactory(sf);
		
		
		logger.info("DataSource changed to " + aDataSource.getUrl());
		return true;
	}
	
	protected Session getSession(){
		Session s = factory.getCurrentSession();
		//s.beginTransaction();
		return s;
	}



	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.service.IDatabaseService#connectToDatabase(eu.etaxonomy.cdm.database.DatabaseEnum, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	public boolean connectToDatabase(DatabaseEnum databaseEnum, String server,
			String database, String username, String password) {
		return connectToDatabase(databaseEnum, server, database, username, password, databaseEnum.getDefaultPort()) ;
	}
	

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.service.IDatabaseService#getDatabaseTypeName()
	 */
	public DatabaseEnum getDatabaseEnum() {
		return DatabaseEnum.getDatabaseEnumByDriverClass(getDataSource().getDriverClassName());
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
