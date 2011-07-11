/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/ 

package eu.etaxonomy.cdm.database;

import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.dbunit.database.DatabaseConfig;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.dbunit.operation.DatabaseOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.transaction.TransactionStatus;

import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.VocabularyEnum;
import eu.etaxonomy.cdm.test.integration.HsqldbDataTypeFactory;

public class TestingTermInitializer extends PersistentTermInitializer {
    private static final Logger logger = Logger.getLogger(TestingTermInitializer.class);

	private DataSource dataSource;
	
	private Resource termsDataSet;
	
	private Resource termsDtd;
	
	public void setTermsDataSet(Resource termsDataSet) {
		this.termsDataSet = termsDataSet;
	}
	
	public void setTermsDtd(Resource termsDtd) {
		this.termsDtd = termsDtd;
	}

	@Autowired
	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}
	

    @PostConstruct
	@Override
	public void initialize() {
		super.initialize();
	}
	
    @Override
	public void doInitialize(){
		TransactionStatus txStatus = transactionManager.getTransaction(txDefinition);
		IDatabaseConnection connection = null;

		try {
			connection = getConnection();
			IDataSet dataSet = new FlatXmlDataSet(new InputStreamReader(termsDataSet.getInputStream()),new InputStreamReader(termsDtd.getInputStream()));
			
			DatabaseOperation.CLEAN_INSERT.execute(connection, dataSet);
		} catch (Exception e) {
			logger.error(e);
			for(StackTraceElement ste : e.getStackTrace()) {
				logger.error(ste);
			}
		} finally {
			try {
				connection.close();
			} catch (SQLException sqle) {
				logger.error(sqle);
			}
		}
		
		transactionManager.commit(txStatus);
		
		for(VocabularyEnum vocabularyType : VocabularyEnum.values()) {
			Class<? extends DefinedTermBase<?>> clazz = vocabularyType.getClazz();
			UUID vocabularyUuid = vocabularyType.getUuid();
			secondPass(clazz, vocabularyUuid,new HashMap<UUID,DefinedTermBase>());
		}
	}

	protected IDatabaseConnection getConnection() throws SQLException {
		IDatabaseConnection connection = null;
		try {
			connection = new DatabaseConnection(dataSource.getConnection());

			DatabaseConfig config = connection.getConfig();
			config.setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY,new HsqldbDataTypeFactory());
		} catch (Exception e) {
			logger.error(e);
		}
		return connection;
	}
}
