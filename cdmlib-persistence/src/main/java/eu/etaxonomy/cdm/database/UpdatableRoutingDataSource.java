package eu.etaxonomy.cdm.database;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.jdbc.datasource.AbstractDataSource;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;


/**
 * A simple RoutingDataSource.
 * Bean definitions must set the key of the default datasource to "empty_default" 
 * This String is defined in the contant <code>DEFAULT_DATASOURCE_KEY</code> and will
 * be used when the RoutingDataSource is beeing updated with a new <code>Map</code> 
 * of data sources.
 * <p>
 * <b>Example of bean definition:</b>
 * <pre>
   &lt;bean id="dataSource"  lazy-init="true" class="eu.etaxonomy.cdm.remote.service.BasepathRoutingDataSource"&gt;
    	&lt;property name="targetDataSources"&gt;
	      &lt;map key-type="java.lang.String"&gt;
	         &lt;entry key="empty_default" value-ref="empty_defaultDataSource"/&gt;
	      &lt;/map&gt;
   		&lt;/property&gt;
   		&lt;property name="defaultTargetDataSource" ref="empty_defaultDataSource"/&gt;
   &lt;/bean&gt;
   </pre>
 * 
 * @author a.kohlbecker
 */
public class UpdatableRoutingDataSource extends AbstractRoutingDataSource {
	
	
	private static final String DATASOURCE_BEANDEF_FILE = "datasources.xml";
	private static final String DATASOURCE_BEANDEF_PATH = System.getProperty("user.home")+File.separator+".cdmLibrary"+File.separator;
	
	private static String userdefinedBeanDefinitionFile = null;
	
	private String defaultDatasourceName = "empty_default";

	@Override
	protected Object determineCurrentLookupKey() {
		return NamedContextHolder.getContextKey();
	}
	
	@Override
	public void afterPropertiesSet() {
		updateDataSources();
		// super.afterPropertiesSet() is called by updateRoutingDataSource()
	}
	
	public void setDefaultDatasourceName(String name){
		this.defaultDatasourceName = name;
	}
	
	
	public void setBeanDefinitionFile(String filename){
		userdefinedBeanDefinitionFile = filename;
	}
	
	public Map<String,SimpleDriverDataSource> updateDataSources() {
		logger.info("loading & testing datasources .. ");
		Map<String,SimpleDriverDataSource> datasources = loadDataSources();
		setTargetDataSources(datasources);
		DataSource defaultDatasource = datasources.get(defaultDatasourceName);
		if(defaultDatasource == null) {
			logger.error("Defaultdatasource '" +defaultDatasourceName + "' not found.");
		}
		setDefaultTargetDataSource(defaultDatasource);
		super.afterPropertiesSet();
		return datasources;
	}

	protected Map<String, SimpleDriverDataSource> loadDataSources() {

		Map<String, SimpleDriverDataSource> dataSources = new HashMap<String, SimpleDriverDataSource>();

		String path = DATASOURCE_BEANDEF_PATH + (userdefinedBeanDefinitionFile == null ? DATASOURCE_BEANDEF_FILE : userdefinedBeanDefinitionFile);
		logger.info("    loading bean definition file: " + path);
		FileSystemResource file = new FileSystemResource(path);
		XmlBeanFactory beanFactory  = new XmlBeanFactory(file);
		
		for(String beanName : beanFactory.getBeanDefinitionNames()){
			SimpleDriverDataSource datasource = (SimpleDriverDataSource)beanFactory.getBean(beanName, SimpleDriverDataSource.class);
			Connection connection = null;
			String sqlerror = null;
			try {
				connection = datasource.getConnection();
				connection.close();
			} catch (SQLException e) {
				sqlerror = e.getMessage() + "["+ e.getSQLState() + "]";
				if(connection !=  null){
					try {connection.close();} catch (SQLException e1) { /* IGNORE */ }
				}
			}
			logger.info("    /" + beanName + " => "+ datasource.getUrl() + "[ "+(sqlerror == null ? "OK" : "ERROR: " + sqlerror) + " ]");
			dataSources.put(beanName, datasource);
		}
		
		return dataSources;
	}

}
