package eu.etaxonomy.cdm.database;

import java.io.File;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.postgresql.jdbc2.optional.SimpleDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.jdbc.datasource.AbstractDataSource;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.springframework.stereotype.Component;

@Component
public class DataSourceLoader {
	
	static final Logger logger = Logger.getLogger(DataSourceLoader.class);

	private static final String DATASOURCE_BEANDEF_FILE = "datasources.xml";
	private static final String DATASOURCE_BEANDEF_PATH = System.getProperty("user.home")+File.separator+".cdmLibrary"+File.separator;
	
	@Autowired
	private SessionFactory factory;
	
	
	public Map<String, SimpleDriverDataSource> loadDataSources() {

		Map<String, SimpleDriverDataSource> dataSources = new HashMap<String, SimpleDriverDataSource>();

		FileSystemResource file = new FileSystemResource(DATASOURCE_BEANDEF_PATH + DATASOURCE_BEANDEF_FILE);
		XmlBeanFactory beanFactory  = new XmlBeanFactory(file);
		
		for(String beanName : beanFactory.getBeanDefinitionNames()){
			SimpleDriverDataSource datasource = (SimpleDriverDataSource)beanFactory.getBean(beanName, SimpleDriverDataSource.class);
			dataSources.put(beanName, datasource);
		}
		
		return dataSources;
	}
	
	public Map<String,SimpleDriverDataSource> updateRoutingDataSource() {
		AbstractDataSource ads = getDataSource();
		Map<String,SimpleDriverDataSource> datasources = null;
		if(ads instanceof AbstractRoutingDataSource){
			AbstractRoutingDataSource ars = (AbstractRoutingDataSource)ads;
			datasources = loadDataSources();
			ars.setTargetDataSources(datasources);
		}
		return datasources;
	}
	
	public AbstractDataSource getDataSource() {
		AbstractDataSource as = (AbstractDataSource)SessionFactoryUtils.getDataSource(factory);
		return as;
	}
	
//	private SimpleDriverDataSource createSimpleDriverDataSource(String driverClassName, String url, String username, String password){
//		
//		Driver driver = obtainDriver(driverClassName);
//		if(driver == null){
//			return null;
//		}
//		return new SimpleDriverDataSource(driver, url, username, password);
//	}
//	
//	private static Driver obtainDriver(String driverClassName){
//		
//		try {
//			Driver d;
//			Class<?> driverClass = Class.forName(driverClassName);
//			d = (Driver)driverClass.newInstance();
//			return d;
//		} catch (InstantiationException e) {
//			logger.error("unable to instantiate driver: "+ driverClassName);
//		} catch (IllegalAccessException e) {
//			logger.error("illegal to access this class: "+ driverClassName);
//		} catch (ClassNotFoundException e) {
//			logger.error(e.getMessage());
//		}
//		return null;
//		
//	}
//	
//private static Driver obtainDriver(String driverClassName){
//		
//		Driver d;
//		Enumeration<Driver> drivers = DriverManager.getDrivers();
//		while(drivers.hasMoreElements()){
//			d = drivers.nextElement();
//			if(d.getClass().getName().equals(driverClassName)){
//				return d;
//			}
//		}
//		// driver not jet registered
//		try {
//			Class<?> driverClass = Class.forName(driverClassName);
//			d = (Driver)driverClass.newInstance();
//			DriverManager.registerDriver(d);
//			return d;
//		} catch (InstantiationException e) {
//			logger.error("unable to instantiate driver: "+ driverClassName);
//		} catch (IllegalAccessException e) {
//			logger.error("illegal to access this class: "+ driverClassName);
//		} catch (SQLException e) {
//			logger.error(e.getMessage());
//		} catch (ClassNotFoundException e) {
//			logger.error(e.getMessage());
//		}
//		return null;
//		
//	}
	
}
