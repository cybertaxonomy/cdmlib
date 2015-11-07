/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.database;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.dbunit.database.DatabaseConfig;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.ext.h2.H2DataTypeFactory;
import org.dbunit.operation.DatabaseOperation;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import eu.etaxonomy.cdm.model.common.DefaultTermInitializer;
import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.Representation;
import eu.etaxonomy.cdm.model.common.TermVocabulary;
import eu.etaxonomy.cdm.model.common.VocabularyEnum;

public class TestingTermInitializer extends DefaultTermInitializer {
    private static final Logger logger = Logger.getLogger(TestingTermInitializer.class);

    protected PlatformTransactionManager transactionManager;
    protected DefaultTransactionDefinition txDefinition = new DefaultTransactionDefinition();

    protected TestingTermVocabularyDao vocabularyDao;

    private DataSource dataSource;

    private Resource termsDataSet;

    private Resource termsDtd;


    @Autowired
    public void setTransactionManager(PlatformTransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    @Autowired
    public void setVocabularyDao(TestingTermVocabularyDao vocabularyDao) {
        this.vocabularyDao = vocabularyDao;
    }

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void setTermsDataSet(Resource termsDataSet) {
        this.termsDataSet = termsDataSet;
    }

    public void setTermsDtd(Resource termsDtd) {
        this.termsDtd = termsDtd;
    }


    @PostConstruct
    @Override
    public void initialize() {
        super.initialize();
    }

    @Override
    public void doInitialize(){
        logger.info("TestingTermInitializer initialize start ...");
        if (isOmit()){
            logger.info("TestingTermInitializer.omit == true, returning without initializing terms");
            return;
        } else {
            TransactionStatus txStatus = transactionManager.getTransaction(txDefinition);
            IDatabaseConnection connection = null;

            try {

                connection = getConnection();

//				MultiSchemaXmlDataSetFactory dataSetFactory = new MultiSchemaXmlDataSetFactory();
//		    	MultiSchemaDataSet multiSchemaDataset = dataSetFactory.createDataSet(termsDataSet.getFile());
//
//		    	if(multiSchemaDataset != null){
//			       	for (String name : multiSchemaDataset.getSchemaNames()) {
//			    		IDataSet clearDataSet = multiSchemaDataset.getDataSetForSchema(name);
//			    		DatabaseOperation.CLEAN_INSERT.execute(connection, clearDataSet);
//			    	}
//		    	}

//                logger.info("loading data base schema from " + termsDtd.getFile().getAbsolutePath());
//                logger.info("loading data set from " + termsDataSet.getFile().getAbsolutePath());


                //old: IDataSet dataSet = new FlatXmlDataSet(new InputStreamReader(termsDataSet.getInputStream()),new InputStreamReader(termsDtd.getInputStream()));

                IDataSet dataSet = new FlatXmlDataSetBuilder()
                	.setMetaDataSetFromDtd(termsDtd.getInputStream())
                	.build(termsDataSet.getInputStream());


                //ITable definedTermBase = dataSet.getTable("DEFINEDTERMBASE");
//				for(int rowId = 0; rowId < definedTermBase.getRowCount(); rowId++) {
//					System.err.println(rowId + " : " + definedTermBase.getValue(rowId, "CREATEDBY_ID"));
//				}
                DatabaseOperation.CLEAN_INSERT.execute(connection, dataSet);

            } catch (Exception e) {
                logger.error(e);
                for(StackTraceElement ste : e.getStackTrace()) {
                    logger.error(ste);
                }
            } finally {
//                try {
//                    this.transactionManager.commit(txStatus);
//                    if (connection != null){
//                        connection.close();
//                    }
//                } catch (SQLException sqle) {
//                    logger.error(sqle);
//                }
            }

            transactionManager.commit(txStatus);

            txStatus = transactionManager.getTransaction(txDefinition);

            for(VocabularyEnum vocabularyType : VocabularyEnum.values()) {
            	initializeAndStore(vocabularyType, new HashMap<UUID,DefinedTermBase>(), null);
            }
            transactionManager.commit(txStatus);
            //txStatus = transactionManager.getTransaction(txDefinition);
        }
        logger.info("TestingTermInitializer initialize end ...");
    }

    /**
     * This methods should be an exact copy of the
     * cdmlib-persistence PersistentTermInitializer.initializeAndStore() method.
     * The problems is that both do not know about each other within their environment.
     *
     * FIXME Try to handle it such that we do not need to duplicate redundant code here.
     *
     *
     * Initializes the static fields of the <code>TermVocabulary</code> classes.
     *
     * @param clazz the <code>Class</code> of the vocabulary
     * @param vocabularyUuid the <code>UUID</code> of the vocabulary
     * @param terms a <code>Map</code> containing all already
     *                       loaded terms with their <code>UUID</code> as key
     * @param vocabularyMap
     */
    protected void initializeAndStore(VocabularyEnum vocType, Map<UUID,DefinedTermBase> terms, Map<UUID, TermVocabulary<?>> vocabularyMap) {
        Class<? extends DefinedTermBase<?>> clazz = vocType.getClazz();
        UUID vocabularyUuid = vocType.getUuid();

        if (logger.isDebugEnabled()){ logger.debug("Loading vocabulary for class " + clazz.getSimpleName() + " with uuid " + vocabularyUuid );}

        TermVocabulary<? extends DefinedTermBase> persistedVocabulary;
        if (vocabularyMap == null || vocabularyMap.get(vocabularyUuid) == null ){
            persistedVocabulary = vocabularyDao.findByUuid(vocabularyUuid);
        }else{
            persistedVocabulary = vocabularyMap.get(vocabularyUuid);
        }

        if (logger.isDebugEnabled()){ logger.debug("Initializing terms in vocabulary for class " + clazz.getSimpleName() + " with uuid " + vocabularyUuid );}
        //not really needed anymore as we do term initializing from the beginning now
        if (persistedVocabulary != null){
            for(DefinedTermBase<?> definedTermBase : persistedVocabulary.getTerms()) {

                Hibernate.initialize(definedTermBase.getRepresentations());
                for(Representation r : definedTermBase.getRepresentations()) {
                    Hibernate.initialize(r.getLanguage());
                }
                terms.put(definedTermBase.getUuid(), definedTermBase);
            }
        }else{
            logger.error("Persisted Vocabulary does not exist in database: " + vocabularyUuid);
            throw new IllegalStateException("Persisted Vocabulary does not exist in database: " + vocabularyUuid);
        }


        //fill term store
        if (logger.isDebugEnabled()){ logger.debug("Setting defined Terms for class " + clazz.getSimpleName() + ", " + persistedVocabulary.getTerms().size() + " in vocabulary");}
        super.setDefinedTerms(clazz, persistedVocabulary);
        if (logger.isDebugEnabled()){ logger.debug("Second pass - DONE");}

    }

    protected IDatabaseConnection getConnection() {
        IDatabaseConnection connection = null;
        try {
            connection = new DatabaseConnection(dataSource.getConnection());

            DatabaseConfig config = connection.getConfig();
            //FIXME must use unitils.properties: org.unitils.core.dbsupport.DbSupport.implClassName & database.dialect to find configured DataTypeFactory
            config.setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new H2DataTypeFactory());
        } catch (Exception e) {
            logger.error(e);
        }
        return connection;
    }
}
