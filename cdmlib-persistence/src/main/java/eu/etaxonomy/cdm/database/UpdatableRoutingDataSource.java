package eu.etaxonomy.cdm.database;

import java.util.Map;

import javax.sql.DataSource;

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
	
	private static final String DEFAULT_DATASOURCE_KEY = "empty_default";

	@Override
	protected Object determineCurrentLookupKey() {
		return NamedContextHolder.getContextKey();
	}
	
	/**
	 * preserves the default target datasource on updating the targetdatasource map.
	 * All other datasources are replaced by those stored in the Map.
	 * @param targetDataSources
	 */
	public void updateTargetDataSources(Map targetDataSources) {
		DataSource tds = determineTargetDataSource();
		targetDataSources.put(DEFAULT_DATASOURCE_KEY, tds);
		setTargetDataSources(targetDataSources);
		setDefaultTargetDataSource(targetDataSources);
		afterPropertiesSet();
	}

}
