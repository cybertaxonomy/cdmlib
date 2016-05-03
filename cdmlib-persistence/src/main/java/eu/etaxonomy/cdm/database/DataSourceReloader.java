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

import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;
import org.springframework.orm.hibernate5.SessionFactoryUtils;
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

	public Map<String,DataSourceInfo> reload() {
		return getDataSource().updateDataSources();
	}

	public Map<String,DataSourceInfo> test() {
		Map<String,SimpleDriverDataSource> dataSources = getDataSource().loadDataSources();
		Map<String, DataSourceInfo> dataSourceInfos = getDataSource().testDataSources(dataSources);
		return dataSourceInfos;
	}

}
