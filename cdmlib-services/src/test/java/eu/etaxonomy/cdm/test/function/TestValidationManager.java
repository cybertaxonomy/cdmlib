/**
* Copyright (C) 2015 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.test.function;

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.api.validation.ValidationManager;
import eu.etaxonomy.cdm.common.AccountStore;
import eu.etaxonomy.cdm.database.CdmDataSource;
import eu.etaxonomy.cdm.database.DbSchemaValidation;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;

/**
 * @author a.mueller
 * @since 23.01.2015
 *
 */
public class TestValidationManager {


    public void testMe(){
        DbSchemaValidation schema = DbSchemaValidation.CREATE;


        String path = "C:\\Users\\a.mueller\\.cdmLibrary\\writableResources\\h2\\testValidation2";
        String username = "sa";
        CdmDataSource dataSource = CdmDataSource.NewH2EmbeddedInstance("validationTest", username, "", path);
//        dataSource = getMySQLDatasource(schema);


        //CdmPersistentDataSource.save(dataSource.getName(), dataSource);
        CdmApplicationController appCtr;
        appCtr = CdmApplicationController.NewInstance(dataSource,schema);
//      appCtr.getCommonService().createFullSampleData();

        ValidationManager valMan = (ValidationManager)appCtr.getBean("validationManager");
        valMan.registerValidationListeners();

        Reference ref = ReferenceFactory.newDatabase();
        ref.setIsbn("1234");
        appCtr.getReferenceService().save(ref);


        //      insertSomeData(appCtr);
//      deleteHighLevelNode(appCtr);   //->problem with Duplicate Key in Classification_TaxonNode

        appCtr.close();


    }

    private CdmDataSource getMySQLDatasource (DbSchemaValidation schema){
        String server = "localhost";
        String database = (schema == DbSchemaValidation.VALIDATE  ? "AM_test" : "AM_test");
        String username = "edit";
        CdmDataSource dataSource = CdmDataSource.NewMySqlInstance(server, database, username, AccountStore.readOrStorePassword(server, database, username, null));
        return dataSource;
    }

    /**
     * @param args
     */
    public static void  main(String[] args) {
        TestValidationManager cc = new TestValidationManager();
        cc.testMe();
    }
}
