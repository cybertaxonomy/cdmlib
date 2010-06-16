/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/ 

package eu.etaxonomy.cdm.database;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.jdbc.datasource.SimpleDriverDataSource;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;


/**
 * A simple RoutingDataSource.
 * Bean definitions must set the key of the default datasource to "default" 
 * This String is defined in the contant <code>DEFAULT_DATASOURCE_KEY</code> and will
 * be used when the RoutingDataSource is beeing updated with a new <code>Map</code> 
 * of data sources.
 * <p>
 * <b>Example of bean definition:</b>
 * <pre>
   &lt;bean id="dataSource"  lazy-init="true" class="eu.etaxonomy.cdm.remote.service.BasepathRoutingDataSource"&gt;
    	&lt;property name="targetDataSources"&gt;
	      &lt;map key-type="java.lang.String"&gt;
	         &lt;entry key="default" value-ref="defaultDataSource"/&gt;
	      &lt;/map&gt;
   		&lt;/property&gt;
   		&lt;property name="defaultTargetDataSource" ref="defaultDataSource"/&gt;
   &lt;/bean&gt;
   </pre>
 * 
 * @author a.kohlbecker
 */
@Deprecated
public class UpdatableRoutingDataSource extends AbstractRoutingDataSource {
	

	private String defaultDatasourceName = "default";

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
	
	

	
	public Map<String, DataSourceInfo> updateDataSources() {
		
		logger.info("loading & testing datasources .. ");
		Map<String,SimpleDriverDataSource> dataSources = loadDataSources();
		Map<String, DataSourceInfo> dataSourceInfos = testDataSources(dataSources);
		
		setTargetDataSources((Map)dataSources);
		DataSource defaultDatasource = dataSources.get(defaultDatasourceName);
		if(defaultDatasource == null) {
			logger.error("Defaultdatasource '" +defaultDatasourceName + "' not found.");
		}
		setDefaultTargetDataSource(defaultDatasource);
		super.afterPropertiesSet();
		
		return dataSourceInfos;
	}
	
	protected Map<String, SimpleDriverDataSource> loadDataSources(){
		return DataSourceBeanLoader.loadDataSources(SimpleDriverDataSource.class);
	}

	/**
	 * @param dataSources
	 * @return
	 */
	protected Map<String, DataSourceInfo> testDataSources(Map<String, SimpleDriverDataSource> dataSources) {
		
		Map<String, DataSourceInfo> dataSourceInfos = new HashMap<String, DataSourceInfo>();

		for(String key : dataSources.keySet()){
			SimpleDriverDataSource datasource = dataSources.get(key);
			DataSourceInfo dsi = new DataSourceInfo(datasource);
			Connection connection = null;
			String sqlerror = null;
			try {
				connection = datasource.getConnection();
				connection.close();
			} catch (SQLException e) {
				sqlerror = e.getMessage() + "["+ e.getSQLState() + "]";
				dsi.getProblems().add(sqlerror);
				if(connection !=  null){
					try {connection.close();} catch (SQLException e1) { /* IGNORE */ }
				}
			}
			logger.info("    /" + key + " => "+ datasource.getUrl() + "[ "+(sqlerror == null ? "OK" : "ERROR: " + sqlerror) + " ]");
			dataSourceInfos.put(key, dsi);
		}
		
		return dataSourceInfos;
	}

}
