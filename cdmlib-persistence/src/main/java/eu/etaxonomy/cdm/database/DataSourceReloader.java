/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.database;

import java.util.Map;

import javax.sql.DataSource;

import org.hibernate.SessionFactory;
import org.hibernate.engine.jdbc.connections.spi.ConnectionProvider;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;
import org.springframework.stereotype.Component;

@Component
public class DataSourceReloader {

	private SessionFactory factory;

	@Autowired
	public void setSessionFacory(SessionFactory factory){
		this.factory = factory;
	}

	public UpdatableRoutingDataSource getDataSource() {
		UpdatableRoutingDataSource as = (UpdatableRoutingDataSource) dataSourceFrom(factory);
		return as;
	}

	private static DataSource dataSourceFrom(SessionFactory sessionFactory) {
		return sessionFactory.unwrap(SessionFactoryImplementor.class)
				.getServiceRegistry()
				.requireService(ConnectionProvider.class)
				.unwrap(DataSource.class);
	}

	public Map<String,DataSourceInfo> reload() {
		return getDataSource().updateDataSources();
	}

	public Map<String,DataSourceInfo> test() {
		Map<String,SimpleDriverDataSource> dataSources = getDataSource().loadDataSources();
		Map<String, DataSourceInfo> dataSourceInfos = getDataSource().testDataSources(dataSources);
		return dataSourceInfos;
	}
}