/**
* Copyright (C) 2008 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
*/

package eu.etaxonomy.cdm.io.test.function;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.common.AccountStore;
import eu.etaxonomy.cdm.database.CdmDataSource;
import eu.etaxonomy.cdm.database.DataSourceNotFoundException;
import eu.etaxonomy.cdm.database.DbSchemaValidation;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.common.Source;
import eu.etaxonomy.cdm.model.agent.Agent;
import eu.etaxonomy.cdm.model.common.Annotation;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.LanguageString;
import eu.etaxonomy.cdm.model.common.Marker;
import eu.etaxonomy.cdm.model.common.OriginalSource;
import eu.etaxonomy.cdm.model.common.Representation;
import eu.etaxonomy.cdm.model.common.TermVocabulary;
import eu.etaxonomy.cdm.model.common.init.TermNotFoundException;
import eu.etaxonomy.cdm.model.description.DescriptionBase;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.name.HomotypicalGroup;
import eu.etaxonomy.cdm.model.name.NameRelationship;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatus;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.name.TypeDesignationBase;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.taxon.SynonymRelationship;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationship;
import eu.etaxonomy.cdm.persistence.dao.common.ICdmEntityDao;
import eu.etaxonomy.cdm.persistence.dao.common.IDefinedTermDao;
import eu.etaxonomy.cdm.persistence.dao.media.IMediaDao;

/**
 * @author a.babadshanjan
 * @created 27.10.2008
 */
public class TestTransaction {
	
	private static final String sourceDb = "cdm_test_jaxb";
	
	private static final ICdmDataSource sourceOne = TestTransaction.CDM_DB(sourceDb);
	
	private static final String server = "192.168.2.10";
	private static final String username = "edit";

	public static ICdmDataSource CDM_DB(String dbname) {
		String password = AccountStore.readOrStorePassword(dbname, server, username, null);
		ICdmDataSource datasource = CdmDataSource.NewMySqlInstance(server, dbname, username, password);
		return datasource;
	}
	
    private static final Logger logger = Logger.getLogger(TestTransaction.class);
	
	private void test(){
		
		CdmApplicationController appCtr = null;
		logger.info("");

		try {
			appCtr = CdmApplicationController.NewInstance(sourceOne, DbSchemaValidation.VALIDATE, true);

		} catch (Exception e) {
			logger.error("Error creating application controller");
			e.printStackTrace();
			System.exit(1);
		}
		
		try {
			//get data from database
	    	TransactionStatus txStatOne = appCtr.startTransaction(true);
	    	TransactionStatus txStatTwo = appCtr.startTransaction(true);

	    	appCtr.commitTransaction(txStatTwo);
	    	appCtr.commitTransaction(txStatOne);
	    	//java.lang.IllegalStateException: Cannot deactivate transaction synchronization - not active
	    	appCtr.close();
			logger.info(""); 
				
		} catch (Exception e) {
    		logger.error("Error");
    		e.printStackTrace();
		}
	}
	
	/**
	 * @param args
	 */
	public static void  main(String[] args) {
		TestTransaction diff = new TestTransaction();
    	diff.test();
	}
}
