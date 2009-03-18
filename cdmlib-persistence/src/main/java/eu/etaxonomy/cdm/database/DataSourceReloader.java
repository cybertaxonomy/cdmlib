package eu.etaxonomy.cdm.database;

import java.util.Map;

import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.springframework.stereotype.Component;

@Component
public class DataSourceReloader {
	
	static final Logger logger = Logger.getLogger(DataSourceReloader.class);

	private SessionFactory factory;
	
	@Autowired
	public void setSessionFacory(SessionFactory factory){
		this.factory = factory;
	}
	
	public UpdatableRoutingDataSource getDataSource() {
		UpdatableRoutingDataSource as = (UpdatableRoutingDataSource)SessionFactoryUtils.getDataSource(factory);
		return as;
	}
	
	public Map<String,SimpleDriverDataSource> reload() {
		return getDataSource().updateDataSources();
	}
	
	
}
