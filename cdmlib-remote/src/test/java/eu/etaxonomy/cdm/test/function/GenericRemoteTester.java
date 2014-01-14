/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.test.function;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import org.apache.log4j.Logger;
import org.springframework.core.io.ClassPathResource;

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.api.service.DistributionTree;
import eu.etaxonomy.cdm.common.AccountStore;
import eu.etaxonomy.cdm.database.CdmDataSource;
import eu.etaxonomy.cdm.database.CdmPersistentDataSource;
import eu.etaxonomy.cdm.database.DbSchemaValidation;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.model.location.NamedAreaLevel;
import eu.etaxonomy.cdm.remote.controller.DescriptionPortalController;
import eu.etaxonomy.cdm.remote.editor.UuidList;

/**
 * This class is meant for generic cdmlib-remote testing during development and to try things out.
 * It is not a test class to be run in unit or integration tests.
 * It may be adapted as required,
 * but please do not destroy the overall structure.
 *
 * TODO not yet fully ready as the remote beans are not yet loaded.
 * We need a specific applicationContext
 *
 * @author a.mueller
 *
 */
public class GenericRemoteTester {
    private static final Logger logger = Logger.getLogger(GenericRemoteTester.class);


    private void testNewConfigControler(){
        List<CdmPersistentDataSource> lsDataSources = CdmPersistentDataSource.getAllDataSources();
        DbSchemaValidation schema = DbSchemaValidation.VALIDATE;
        System.out.println(lsDataSources);
        ICdmDataSource dataSource;

        dataSource = lsDataSources.get(1);
//		DatabaseTypeEnum dbType = DatabaseTypeEnum.MySQL;

        String server = "localhost";
        String database = "cdm_test";
//		String database = "test";
        String username = "edit";
        dataSource = CdmDataSource.NewMySqlInstance(server, database, username, AccountStore.readOrStorePassword(server, database, username, null));

//		//H2
//		username = "sa";
//    	dataSource = CdmDataSource.NewH2EmbeddedInstance(database, username, "sa", NomenclaturalCode.ICNAFP);

        String resourceStr = "/eu/etaxonomy/cdm/remote.xml";
        ClassPathResource applicationContextResource = new ClassPathResource(resourceStr);
        CdmApplicationController appCtr = CdmApplicationController.NewInstance(applicationContextResource, dataSource, schema, false);

//		insertSomeData(appCtr);
//		deleteHighLevelNode(appCtr);   //->problem with Duplicate Key in Classification_TaxonNode
        testDistributionTree(appCtr);

        appCtr.close();
    }

    private void testDistributionTree(CdmApplicationController appCtr) {
        DescriptionPortalController ctl = (DescriptionPortalController)appCtr.getBean("descriptionPortalController");
        HttpServletRequest request = null;
        HttpServletResponse response = new HttpServletResponseWrapper(null);
        Set<NamedAreaLevel>  levels = new HashSet<NamedAreaLevel>();
        NamedAreaLevel areaLevel = (NamedAreaLevel)appCtr.getTermService().find(UUID.fromString("38efa5fd-d7f0-451c-9de9-e6cce41e2225"));
        levels.add(areaLevel);
        UuidList descriptionUuidList = new UuidList();
        descriptionUuidList.add(UUID.fromString("c0e05d0c-1a80-4f2e-b051-ed9fd625d740"));

        boolean subAreaPreference = false;
        boolean statusOrderPreference = false;
        DistributionTree tree = ctl.doGetOrderedDistributionsB(
                descriptionUuidList,
                subAreaPreference ,
                statusOrderPreference,
                null,
                levels, request, response);
        tree.toString();
    }


    private void test(){
        System.out.println("Start Test");
        testNewConfigControler();
        System.out.println("\nEnd Test");
    }

    /**
     * @param args
     */
    public static void  main(String[] args) {
        GenericRemoteTester cc = new GenericRemoteTester();
        cc.test();
    }

}
