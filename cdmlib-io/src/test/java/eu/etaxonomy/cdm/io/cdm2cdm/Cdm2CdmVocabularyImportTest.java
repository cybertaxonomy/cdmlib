/**
* Copyright (C) 2021 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.cdm2cdm;

import static org.junit.Assert.assertNotNull;

import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.transaction.TransactionStatus;
import org.unitils.spring.annotation.SpringBeanByName;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.api.service.ITermTreeService;
import eu.etaxonomy.cdm.api.service.IVocabularyService;
import eu.etaxonomy.cdm.database.CdmDataSource;
import eu.etaxonomy.cdm.database.DbSchemaValidation;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.common.CdmApplicationAwareDefaultImport;
import eu.etaxonomy.cdm.io.common.ImportResult;
import eu.etaxonomy.cdm.io.common.events.LoggingIoObserver;
import eu.etaxonomy.cdm.model.term.DefinedTerm;
import eu.etaxonomy.cdm.model.term.TermNode;
import eu.etaxonomy.cdm.model.term.TermTree;
import eu.etaxonomy.cdm.model.term.TermType;
import eu.etaxonomy.cdm.model.term.TermVocabulary;
import eu.etaxonomy.cdm.test.integration.CdmTransactionalIntegrationTest;

/**
 * @author a.mueller
 * @since 18.09.2021
 */
public class Cdm2CdmVocabularyImportTest extends CdmTransactionalIntegrationTest {

    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(Cdm2CdmVocabularyImportTest.class);

    private static CdmApplicationController otherRepository;

    @SpringBeanByName
    private CdmApplicationAwareDefaultImport<?> defaultImport;
    @SpringBeanByType
    private IVocabularyService vocService;
    @SpringBeanByType
    private ITermTreeService treeService;

    private Cdm2CdmImportConfigurator configurator;

    private static UUID uuidStructVoc = UUID.fromString("4373c232-fbf3-4f1d-b766-ad603f7aa866");
    private static UUID uuidStructFirst = UUID.fromString("74c917aa-670a-4dc5-9bd1-c26b98072349");

    private static UUID uuidStructGraph = UUID.fromString("1fdf67c7-e267-44ca-8d35-be66e3746847");

    @BeforeClass
    public static void setUpClass() throws Exception {
    //      this.startH2Server();
          boolean omitTermLoading = true;
          ICdmDataSource dataSource = CdmDataSource.NewH2EmbeddedInstance("testVoc", "sa", "", "C:\\Users\\a.mueller\\tmp\\testVoc");
//          int a = dataSource.executeUpdate("CREATE TABLE HIBERNATE_SEQUENCES ("
//                  + " sequence_name VARCHAR(255), next_val BIGINT )");
//          try {
//              ResultSet rs = dataSource.executeQuery("SELECT * FROM INFORMATION_SCHEMA.TABLES");
//              while(rs.next()){
//                  for (int i = 1; i<=12; i++){
//                      System.out.print(";"+rs.getObject(i));
//                  }
//                  System.out.println();
//              }
//              dataSource.executeQuery("select tbl.next_val from hibernate_sequences tbl where tbl.sequence_name='a'");
            otherRepository = CdmApplicationController.NewInstance(dataSource,
                      DbSchemaValidation.CREATE, omitTermLoading);
//        } catch (Exception e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
          System.out.println("started");
          TermVocabulary<DefinedTerm> voc = createTestVocabulary(otherRepository);
          createTestGraph(otherRepository, voc);
    }

    private static TermVocabulary<DefinedTerm> createTestVocabulary(CdmApplicationController app) {
        TermType termType = TermType.Structure;
        TermVocabulary<DefinedTerm> voc = TermVocabulary.NewInstance(termType, DefinedTerm.class);
        DefinedTerm term = getStructure("1.", uuidStructFirst);
        voc.addTerm(term);
        voc.setUuid(uuidStructVoc);

        app.getVocabularyService().saveOrUpdate(voc);
        return voc;
    }

    private static void createTestGraph(CdmApplicationController app, TermVocabulary<DefinedTerm> voc) {
        TermType termType = TermType.Structure;
        TermTree<DefinedTerm> graph = TermTree.NewInstance(termType, DefinedTerm.class);
        DefinedTerm firstStruct = voc.getTerms().stream().filter(t->t.getUuid().equals(uuidStructFirst)).findFirst().get();
        TermNode<DefinedTerm> firstRoot = graph.getRoot().addChild(firstStruct);
        graph.setUuid(uuidStructGraph);

        app.getTermTreeService().saveOrUpdate(graph);
    }

    //    @AfterClass
    public static void tearDownClass() throws Exception {
        otherRepository.close();
        otherRepository = null;
    }

    @Before
    public void setUp() throws Exception {
        configurator = Cdm2CdmImportConfigurator.NewInstace(otherRepository, null);
        configurator.setDoTaxa(false);
        configurator.setDoDescriptions(false);
        configurator.setVocabularyFilter(new HashSet<>(Arrays.asList(uuidStructVoc)));

        configurator.addObserver(new LoggingIoObserver());
        assertNotNull("Configurator could not be created", configurator);
    }

    @Test
    public void testInit() {
        assertNotNull("import instance should not be null", defaultImport);
        assertNotNull("configurator instance should not be null", configurator);
    }

    @Test
    public void testInvokeVocabulary() {
        @SuppressWarnings("unchecked")
        TermVocabulary<DefinedTerm> voc = vocService.find(uuidStructVoc);
        Assert.assertNull("Vocabulary must not exist before invoke", voc);
        ImportResult result = defaultImport.invoke(this.configurator);
        Assert.assertTrue(result.isSuccess());
        commitAndStartNewTransaction();
        voc = vocService.find(uuidStructVoc);
        Assert.assertNotNull("Vocabulary must exist after invoke", voc);
        @SuppressWarnings("unchecked")
        TermVocabulary<DefinedTerm> otherVoc = otherRepository.getVocabularyService().find(uuidStructVoc);
        Assert.assertNotSame(otherVoc, voc);
        Assert.assertEquals(1, voc.getTerms().size());

        //add term in other
        UUID uuidSecond = UUID.fromString("56546e58-e4ea-47f9-ae49-de772a416003");
        DefinedTerm secondTerm = getStructure("2.", uuidSecond);
        TransactionStatus tx = otherRepository.startTransaction();
        otherVoc = otherRepository.getVocabularyService().find(uuidStructVoc);
        otherVoc.addTerm(secondTerm);
        otherRepository.getTermService().saveOrUpdate(secondTerm);
        otherRepository.commitTransaction(tx);

        //test if added term gets imported
        commitAndStartNewTransaction();
        voc = vocService.find(uuidStructVoc);
        Assert.assertEquals(1, voc.getTerms().size());
        commitAndStartNewTransaction();
        result = defaultImport.invoke(this.configurator);
        commitAndStartNewTransaction();
        voc = vocService.find(uuidStructVoc);
        Assert.assertEquals(2, voc.getTerms().size());
        Assert.assertTrue("As contains works on equal() the term should be contained", voc.getTerms().contains(secondTerm));
        voc.getTerms().stream().filter(a->a.getUuid().equals(uuidSecond)).forEach(t->{
            Assert.assertEquals(secondTerm, t);
            Assert.assertNotSame(secondTerm, t);
        });

        //test invoke for graph
        configurator.setGraphFilter(new HashSet<>(Arrays.asList(uuidStructGraph)));
        TermTree<DefinedTerm> graph = treeService.find(uuidStructGraph);
        Assert.assertNull("Graph must not exist before invoke", graph);
        result = defaultImport.invoke(this.configurator);
        Assert.assertTrue(result.isSuccess());
        commitAndStartNewTransaction();
        graph = treeService.find(uuidStructGraph);
        Assert.assertNotNull("Graph must exist after invoke", graph);
        TransactionStatus txOther = otherRepository.startTransaction();
        @SuppressWarnings("unchecked")
        TermTree<DefinedTerm> otherGraph = otherRepository.getTermTreeService().find(uuidStructGraph);
        Assert.assertNotSame(otherGraph, graph);
        Assert.assertEquals(otherGraph.getRoot(), graph.getRoot());
        Assert.assertNotSame(otherGraph.getRoot(), graph.getRoot());
        Assert.assertEquals(1, graph.getRootChildren().size());
        TermNode<DefinedTerm> otherSingleChild = otherGraph.getRootChildren().iterator().next();
        TermNode<DefinedTerm> thisSingleChild = graph.getRootChildren().iterator().next();
        Assert.assertEquals(otherSingleChild, thisSingleChild);
        Assert.assertNotSame(otherSingleChild, thisSingleChild);


        otherRepository.commitTransaction(txOther);
    }

    private static DefinedTerm getStructure(String distinct, UUID uuid) {
        DefinedTerm term = DefinedTerm.NewInstance(TermType.Structure,
                distinct + " structure description", distinct + " Struc", distinct + "St.");
        term.getRepresentations().iterator().next().setLanguage(null);  //as long as we have static languages
        uuid = (uuid == null? UUID.randomUUID() : uuid);
        term.setUuid(uuid);
        return term;
    }

    @Override
    public void createTestDataSet() throws FileNotFoundException {}
}
