/**
* Copyright (C) 2020 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.dbunit.annotation.DataSets;
import org.unitils.dbunit.datasetloadstrategy.impl.RefreshLoadStrategy;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.api.service.config.NodeDeletionConfigurator.ChildHandling;
import eu.etaxonomy.cdm.api.service.config.TermNodeDeletionConfigurator;
import eu.etaxonomy.cdm.hibernate.HibernateProxyHelper;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.ITreeNode;
import eu.etaxonomy.cdm.model.common.Language;
import  eu.etaxonomy.cdm.model.description.Character;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.term.DefinedTerm;
import eu.etaxonomy.cdm.model.term.Representation;
import eu.etaxonomy.cdm.model.term.TermNode;
import eu.etaxonomy.cdm.model.term.TermTree;
import eu.etaxonomy.cdm.model.term.TermType;
import eu.etaxonomy.cdm.persistence.dto.CharacterDto;
import eu.etaxonomy.cdm.persistence.dto.CharacterNodeDto;
import eu.etaxonomy.cdm.persistence.dto.TermCollectionDto;
import eu.etaxonomy.cdm.persistence.dto.TermDto;
import eu.etaxonomy.cdm.persistence.dto.TermNodeDto;
import eu.etaxonomy.cdm.persistence.dto.TermTreeDto;
import eu.etaxonomy.cdm.test.integration.CdmTransactionalIntegrationTest;
import eu.etaxonomy.cdm.test.unitils.CleanSweepInsertLoadStrategy;

/**
 * @author k.luther
 * @since Oct 30, 2020
 */
public class TermNodeServiceImplTest  extends CdmTransactionalIntegrationTest{

    private static String[] tableNames = new String[]{"TERMCOLLECTION","TERMRELATION"};

    private static final String sep = ITreeNode.separator;
    private static final String pref = ITreeNode.treePrefix;

    private static final UUID featureTreeUuid = UUID.fromString("6c2bc8d9-ee62-4222-be89-4a8e31770878");
    private static final UUID featureTree2Uuid = UUID.fromString("43d67247-936f-42a3-a739-bbcde372e334");
    private static final UUID node2Uuid= UUID.fromString("484a1a77-689c-44be-8e65-347d835f47e8");
    private static final UUID node3Uuid = UUID.fromString("2d41f0c2-b785-4f73-a436-cc2d5e93cc5b");
    private static final UUID node4Uuid = UUID.fromString("fdaec4bd-c78e-44df-ae87-28f18110968c");
    private static final UUID node5Uuid = UUID.fromString("c4d5170a-7967-4dac-ab76-ae2019eefde5");
    private static final UUID node6Uuid = UUID.fromString("b419ba5e-9c8b-449c-ad86-7abfca9a7340");

    private TermNode<Feature> node3;
    private TermNode<Feature> node2;

    @SpringBeanByType
    private ITermNodeService termNodeService;

    @SpringBeanByType
    private ITermTreeService termTreeService;

    @SpringBeanByType
    private ITermService termService;

    @Test
    @DataSets({
        @DataSet(loadStrategy=CleanSweepInsertLoadStrategy.class, value="/eu/etaxonomy/cdm/database/ClearDB_with_Terms_DataSet.xml"),
        @DataSet(value="/eu/etaxonomy/cdm/database/TermsDataSet-with_auditing_info.xml"),
    })
    public void testSaveCharacterNode_supportedData() {
        UUID characterTreeUuid = createAndSaveCharacterTree();

        TermCollectionDto dto = termTreeService.getTermTreeDtoByUuid(characterTreeUuid);
        List<TermNodeDto> children = ((TermTreeDto)dto).getRoot().getChildren();
        CharacterNodeDto nodeDto = (CharacterNodeDto) children.get(0);
        TermDto termDto = nodeDto.getTerm();
        if (termDto instanceof CharacterDto){
            CharacterDto characterDto = (CharacterDto) termDto;
            characterDto.setSupportsCategoricalData(true);
            List<CharacterNodeDto> dtos = new ArrayList<>();
            dtos.add(nodeDto);
            termNodeService.saveCharacterNodeDtoList(dtos);
            commitAndStartNewTransaction();
            @SuppressWarnings("unchecked")
            TermTree<Feature> characterTree = termTreeService.load(characterTreeUuid);
            List<TermNode<Feature>> childNodes = characterTree.getRoot().getChildNodes();
            TermNode<Feature> child = childNodes.get(0);

            Assert.assertTrue(child.getTerm().isSupportsCategoricalData());

        }else{
            Assert.fail();
        }
    }

    @Test
    public void testSaveCharacterNode_representation() {
        UUID characterTreeUuid = createAndSaveCharacterTree();

        TermCollectionDto dto = termTreeService.getTermTreeDtoByUuid(characterTreeUuid);
        List<TermNodeDto> children = ((TermTreeDto)dto).getRoot().getChildren();
        CharacterNodeDto nodeDto = (CharacterNodeDto) children.get(0);
        TermDto termDto = nodeDto.getTerm();
        if (termDto instanceof CharacterDto){
            CharacterDto characterDto = (CharacterDto) termDto;
            Representation rep = characterDto.getPreferredRepresentation(Language.DEFAULT());
            if (rep != null){
                rep.setText("Test");
            }else{
                rep = Representation.NewInstance("Test", "", "", Language.DEFAULT());
                characterDto.addRepresentation(rep);
            }
            List<CharacterNodeDto> dtos = new ArrayList<>();
            dtos.add(nodeDto);
            termNodeService.saveCharacterNodeDtoList(dtos);
            commitAndStartNewTransaction();
            @SuppressWarnings("unchecked")
            TermTree<Feature> characterTree = termTreeService.load(characterTreeUuid);
            List<TermNode<Feature>> childNodes = characterTree.getRoot().getChildNodes();
            TermNode<Feature> child = childNodes.get(0);

            Assert.assertTrue(child.getTerm().getPreferredRepresentation(Language.DEFAULT()).getText().equals("Test"));

        }else{
            Assert.fail();
        }
    }

    private UUID createAndSaveCharacterTree() {
        DefinedTerm structure = DefinedTerm.NewInstance(TermType.Structure);
        TermTree<DefinedTerm> structureTree = TermTree.NewInstance(TermType.Structure);
        TermNode<DefinedTerm> nodeStructure = structureTree.getRoot().addChild(structure);

        DefinedTerm property = DefinedTerm.NewInstance(TermType.Property);
        TermTree<DefinedTerm> propertyTree = TermTree.NewInstance(TermType.Property);
        TermNode<DefinedTerm> nodeProperty = propertyTree.getRoot().addChild(property);
        termService.saveOrUpdate(property);
        termService.saveOrUpdate(structure);
        termTreeService.saveOrUpdate(structureTree);
        termTreeService.saveOrUpdate(propertyTree);

        TermTree<Feature> characterTree = TermTree.NewInstance(TermType.Feature);
        UUID characterTreeUuid = characterTree.getUuid();
        Character character = Character.NewInstance(nodeStructure, nodeProperty);
        character.setSupportsCategoricalData(false);

        characterTree.getRoot().addChild(character);
        termService.saveOrUpdate(character);
        termTreeService.saveOrUpdate(characterTree);
        commitAndStartNewTransaction();

        return characterTreeUuid;
    }


    @Test
    public void testIndexCreatRoot() {
        TermTree<Feature> featureTree = TermTree.NewFeatureInstance();
        termTreeService.save(featureTree);

        Feature feature = (Feature)termService.find(914);
        TermNode<Feature> newNode = featureTree.getRoot().addChild(feature);
        termTreeService.save(featureTree);

        termNodeService.saveOrUpdate(newNode);

        commitAndStartNewTransaction(/*new String[]{"FeatureNode"}*/);
        newNode = termNodeService.load(newNode.getUuid());
        Assert.assertEquals("", sep + pref+featureTree.getId()+sep + featureTree.getRoot().getId()+ sep  + newNode.getId() + sep, newNode.treeIndex());
    }


    @Test
    @DataSet(loadStrategy=RefreshLoadStrategy.class, value="TermNodeServiceImplTest-indexing.xml")
    public final void testIndexCreateNode() {

        Feature feature = (Feature)termService.find(914);

        node2 = termNodeService.load(node2Uuid);
        String oldTreeIndex = node2.treeIndex();

        TermNode<Feature> newNode = node2.addChild(feature);
        termNodeService.saveOrUpdate(node2);

        commitAndStartNewTransaction();
        newNode = termNodeService.load(newNode.getUuid());
        Assert.assertEquals("", oldTreeIndex + newNode.getId() + sep, newNode.treeIndex());
    }


    @Test
    @DataSet(loadStrategy=RefreshLoadStrategy.class, value="TermNodeServiceImplTest-indexing.xml")
    //this may fail in single test if it is the first test as long as #8174 is not fixed
    public void testIndexMoveNode() {
        //in feature tree
        @SuppressWarnings("unused")
        TermTree<?> featureTree = termTreeService.load(featureTreeUuid);
        node2 = termNodeService.load(node2Uuid);
        node3 = termNodeService.load(node3Uuid);
        node3.addChild(node2);
        termNodeService.saveOrUpdate(node2);
        commitAndStartNewTransaction();
        TermNode<?> node6 = termNodeService.load(node6Uuid);
        Assert.assertEquals("Node6 treeindex is not correct", node3.treeIndex() + "2#4#6#", node6.treeIndex());

        //root of new feature tree
        TermTree<Feature> featureTree2 = termTreeService.load(featureTree2Uuid);
        node2 = termNodeService.load(node2Uuid);
        featureTree2.getRoot().addChild(node2);
        termNodeService.saveOrUpdate(node2);
        commitAndStartNewTransaction();
        node2 = termNodeService.load(node2Uuid);
        Assert.assertEquals("Node2 treeindex is not correct", "#t5002#7#2#", node2.treeIndex());
        node6 = termNodeService.load(node6Uuid);
        Assert.assertEquals("Node6 treeindex is not correct", "#t5002#7#2#4#6#", node6.treeIndex());

        //into new classification
        node3 = termNodeService.load(node3Uuid);
        TermNode<Feature> node5 = termNodeService.load(node5Uuid);
        node5.addChild(node3);
        termNodeService.saveOrUpdate(node5);
        commitAndStartNewTransaction(new String[]{"FeatureNode"});
        node3 = termNodeService.load(node3Uuid);
        Assert.assertEquals("Node3 treeindex is not correct", node5.treeIndex() + node3.getId() + sep, node3.treeIndex());
    }

    @Test  //here we may have a test for testing delete of a node and attaching the children
    //to its parents, however this depends on the way delete is implemented and therefore needs
    //to wait until this is finally done
    @DataSet(loadStrategy=RefreshLoadStrategy.class, value="TermNodeServiceImplTest-indexing.xml")
    public final void testIndexDeleteNode() {
        node2 = termNodeService.load(node2Uuid);
        TermNodeDeletionConfigurator config = new TermNodeDeletionConfigurator();
        config.setDeleteElement(false);
        config.setChildHandling(ChildHandling.MOVE_TO_PARENT);
        termNodeService.deleteNode(node2Uuid, config);
        commitAndStartNewTransaction(new String[]{"FeatureNode"});
        TermTree<Feature> tree1 = termTreeService.load(featureTreeUuid);
        assertNotNull(tree1);
        node2 = termNodeService.load(node2Uuid);
        assertNull(node2);
        node3 = termNodeService.load(node3Uuid);
        assertNotNull(node3);
        TermNode<Feature> node4 = termNodeService.load(node4Uuid);
        assertNotNull(node4);
        config.setDeleteElement(false);
        config.setChildHandling(ChildHandling.DELETE);
        termNodeService.deleteNode(node4Uuid, config);
        commitAndStartNewTransaction(new String[]{"FeatureNode"});
        tree1 = termTreeService.load(featureTreeUuid);
        node4 = termNodeService.load(node4Uuid);
        assertNull(node4);
        TermNode<Feature> node6 = termNodeService.load(node6Uuid);
        assertNull(node6);

        HibernateProxyHelper.deproxy(tree1, TermTree.class);
        TermNode<Feature> rootNode = CdmBase.deproxy(tree1.getRoot());
        assertNotNull(tree1);
        termTreeService.delete(tree1.getUuid());
        commitAndStartNewTransaction(/*new String[]{"TaxonNode"}*/);
        tree1 = termTreeService.load(featureTreeUuid);
        assertNull(tree1);
    }

    @Test  //#8127  //#5536 //#10101
    @Ignore  //test still throws exception, see #10101
//    @DataSet(loadStrategy=CleanSweepInsertLoadStrategy.class, value="/eu/etaxonomy/cdm/database/ClearDBDataSet.xml")
    public final void testMergeDetached(){
        int termNodesAtStart = termNodeService.count(TermNode.class);

        //create tree with 2 child nodes
        TermType type = TermType.Feature;
        TermTree<Feature> tree = TermTree.NewInstance(type);
        TermNode<Feature> child1 = tree.getRoot().addChild(Feature.COMMON_NAME());
        TermNode<Feature> child2 = tree.getRoot().addChild(Feature.DISTRIBUTION());
        termTreeService.save(tree);
        commitAndStartNewTransaction();

        //load root node and make it detached
        TermNode<Feature> rootNode = termNodeService.find(tree.getRoot().getUuid());
        rootNode.getChildNodes().get(0);  //initialize children
        commitAndStartNewTransaction(); //detach

        //replace nodes and merge
        TermNode<Feature> childToRemove = rootNode.getChildNodes().get(0);
        rootNode.removeChild(childToRemove);
        TermNode<Feature> child3 =rootNode.addChild(Feature.ADDITIONAL_PUBLICATION());
        TermNode<Feature> mergedRootNode = termNodeService.merge(rootNode, childToRemove);
//        System.out.println("Workaround: " + mergedRootNode.getChildCount());
        commitAndStartNewTransaction(tableNames);

        //test result
        rootNode = termNodeService.find(tree.getRoot().getUuid());
        rootNode.getChildNodes();
        Assert.assertEquals(2, rootNode.getChildNodes().size());
        Assert.assertEquals(child2.getUuid(), rootNode.getChildNodes().get(0).getUuid());
        Assert.assertEquals(child3.getUuid(), rootNode.getChildNodes().get(1).getUuid());
        Assert.assertEquals("Should be root + 2 children", 3 + termNodesAtStart, termNodeService.count(TermNode.class));
        commitAndStartNewTransaction();

//        System.out.println("NEXT");
        //same with key
        //load root node and make it detached
        TermTree<Feature> treeLoaded = termTreeService.find(tree.getUuid());
        rootNode = treeLoaded.getRoot();
        rootNode.getChildNodes().get(0);  //initialize children
        commitAndStartNewTransaction(); //detach

        //replace nodes and merge
        childToRemove = rootNode.getChildNodes().get(0);
        rootNode.removeChild(childToRemove);
        TermNode<Feature> child4 = rootNode.addChild(Feature.DESCRIPTION());

        @SuppressWarnings("unused")
        TermTree<Feature> mergedKey = termTreeService.merge(treeLoaded, childToRemove);

        //NOTE: maybe interesting to know, that if not using orphan removal
        //      resorting the index does not take place if not touching the children list somehow.
        //      The sortindex starts than at some number > 0 and may contain nulls.
        //      If touching the list like below the index starts at 0. This is now
        //      automatically handled in PostMergeEntityListener.
        //      mergedKey.getRoot().getChildren().size();

        commitAndStartNewTransaction(tableNames);

        rootNode = termNodeService.find(tree.getRoot().getUuid());
        rootNode.getChildNodes();
        Assert.assertEquals(2, rootNode.getChildNodes().size());
        Assert.assertEquals(child3.getUuid(), rootNode.getChildNodes().get(0).getUuid());
        Assert.assertEquals(child4.getUuid(), rootNode.getChildNodes().get(1).getUuid());
        Assert.assertEquals("Should be root + 2 children", 3 +termNodesAtStart, termNodeService.count(TermNode.class));
    }

    @Test
//    @DataSet(loadStrategy=CleanSweepInsertLoadStrategy.class, value="/eu/etaxonomy/cdm/database/ClearDBDataSet.xml")
    public void testMergeDetachedWithMove() {
        int termNodesAtStart = termNodeService.count(TermNode.class);

        //create key with 2 child nodes
        TermType type = TermType.Feature;
        TermTree<Feature> tree = TermTree.NewInstance(type);
        TermNode<Feature> child1 = tree.getRoot().addChild(Feature.COMMON_NAME());
        TermNode<Feature> child2 = tree.getRoot().addChild(Feature.DISTRIBUTION());
        termTreeService.save(tree);
        commitAndStartNewTransaction();

        //load root node and make it detached
        TermTree<Feature> keyLoaded = termTreeService.find(tree.getUuid());
        TermNode<Feature> rootNode = keyLoaded.getRoot();
        rootNode.getChildNodes().get(1).getChildNodes().size();  //initialize children
        commitAndStartNewTransaction(); //detach

        //replace nodes and merge
        TermNode<Feature> childMove = rootNode.getChildNodes().get(0);
        TermNode<Feature> newParentNode = rootNode.getChildNodes().get(1);
        newParentNode.addChild(childMove);
        TermNode<Feature> child4 =rootNode.addChild(Feature.ANATOMY());

        @SuppressWarnings("unused")
        //no removed child to delete here
        TermTree<Feature> mergedTree = termTreeService.merge(keyLoaded, new CdmBase[]{});

        commitAndStartNewTransaction(tableNames);

        rootNode = termNodeService.find(tree.getRoot().getUuid());
        rootNode.getChildNodes();
        Assert.assertEquals(2, rootNode.getChildNodes().size());
        TermNode<Feature> firstChild = rootNode.getChildNodes().get(0);
        Assert.assertEquals(child2.getUuid(), firstChild.getUuid());
        Assert.assertEquals(child4.getUuid(), rootNode.getChildNodes().get(1).getUuid());
        Assert.assertEquals(1, firstChild.getChildNodes().size());
        Assert.assertEquals(child1.getUuid(), firstChild.getChildNodes().get(0).getUuid());
        Assert.assertEquals("Should be root + 2 children + 1 grandchild", 4 + termNodesAtStart, termNodeService.count(TermNode.class));
    }

    @Test  //8127  //5536 //10101
    //  @DataSet(loadStrategy=CleanSweepInsertLoadStrategy.class, value="/eu/etaxonomy/cdm/database/ClearDBDataSet.xml")
    public final void testSaveDetached(){
        int termNodesAtStart = termNodeService.count(TermNode.class);

        //create tree with 2 child nodes
        TermType type = TermType.Feature;
        TermTree<Feature> tree = TermTree.NewInstance(type);
        TermNode<Feature> child1 = tree.getRoot().addChild(Feature.COMMON_NAME());
        TermNode<Feature> child2 = tree.getRoot().addChild(Feature.DISTRIBUTION());
        termTreeService.save(tree);
        commitAndStartNewTransaction();

        //load root node and make it detached
        TermNode<Feature> rootNode = termNodeService.find(tree.getRoot().getUuid());
        TermNode<?> r = rootNode.getChildNodes().get(0).getGraph().getRoot();  //initialize children
//        termTreeService.find(tree.getRoot().getUuid());  //initialize tree as it is used later for term type comparison when adding a child
        commitAndStartNewTransaction(); //detach

        //replace nodes and merge
        TermNode<Feature> childToRemove = rootNode.getChildNodes().get(0);
        rootNode.removeChild(childToRemove);
        TermNode<Feature> child3 =rootNode.addChild(Feature.ADDITIONAL_PUBLICATION());
        termNodeService.saveOrUpdate(rootNode);
        termNodeService.delete(childToRemove);
        commitAndStartNewTransaction(tableNames);

        //test result
        rootNode = termNodeService.find(tree.getRoot().getUuid());
        rootNode.getChildNodes();
        Assert.assertEquals(2, rootNode.getChildNodes().size());
        Assert.assertEquals(child2.getUuid(), rootNode.getChildNodes().get(0).getUuid());
        Assert.assertEquals(child3.getUuid(), rootNode.getChildNodes().get(1).getUuid());
        Assert.assertEquals("Should be root + 2 children", 3 + termNodesAtStart, termNodeService.count(TermNode.class));
        commitAndStartNewTransaction();

//      System.out.println("NEXT");
        //same with tree
        //load root node and make it detached
        TermTree<Feature> treeLoaded = termTreeService.find(tree.getUuid());
        rootNode = treeLoaded.getRoot();
        rootNode.getChildNodes().get(0);  //initialize children
        commitAndStartNewTransaction(); //detach

        //replace nodes and merge
        childToRemove = rootNode.getChildNodes().get(0);
        rootNode.removeChild(childToRemove);
        TermNode<Feature> child4 = rootNode.addChild(Feature.DESCRIPTION());
        termTreeService.saveOrUpdate(treeLoaded);
        termNodeService.delete(childToRemove);  //workaround for missing combined method

        commitAndStartNewTransaction(tableNames);

        rootNode = termNodeService.find(tree.getRoot().getUuid());
        rootNode.getChildNodes();
        Assert.assertEquals(2, rootNode.getChildNodes().size());
        Assert.assertEquals(child3.getUuid(), rootNode.getChildNodes().get(0).getUuid());
        Assert.assertEquals(child4.getUuid(), rootNode.getChildNodes().get(1).getUuid());
        Assert.assertEquals("Should be root + 2 children", 3 +termNodesAtStart, termNodeService.count(TermNode.class));
    }

    @Test
//  @DataSet(loadStrategy=CleanSweepInsertLoadStrategy.class, value="/eu/etaxonomy/cdm/database/ClearDBDataSet.xml")
  public void testSaveDetachedWithMove() {

        int termNodesAtStart = termNodeService.count(TermNode.class);

        //create trees with 2 child nodes
        TermType type = TermType.Feature;
        TermTree<Feature> tree = TermTree.NewInstance(type);
        TermNode<Feature> child1 = tree.getRoot().addChild(Feature.COMMON_NAME());
        TermNode<Feature> child2 = tree.getRoot().addChild(Feature.DISTRIBUTION());
        termTreeService.save(tree);
        commitAndStartNewTransaction();

        //load root node and make it detached
        TermTree<Feature> keyLoaded = termTreeService.find(tree.getUuid());
        TermNode<Feature> rootNode = keyLoaded.getRoot();
        rootNode.getChildNodes().get(1).getChildNodes().size();  //initialize children
        commitAndStartNewTransaction(); //detach

        //replace nodes and merge
        TermNode<Feature> childMove = rootNode.getChildNodes().get(0);
        TermNode<Feature> newParentNode = rootNode.getChildNodes().get(1);
        newParentNode.addChild(childMove);
        TermNode<Feature> child4 =rootNode.addChild(Feature.ANATOMY());

        //no removed child to delete here
        termTreeService .saveOrUpdate(keyLoaded);

       commitAndStartNewTransaction(tableNames);

        rootNode = termNodeService.find(tree.getRoot().getUuid());
        rootNode.getChildNodes();
        Assert.assertEquals(2, rootNode.getChildNodes().size());
        TermNode<Feature> firstChild = rootNode.getChildNodes().get(0);
        Assert.assertEquals(child2.getUuid(), firstChild.getUuid());
        Assert.assertEquals(child4.getUuid(), rootNode.getChildNodes().get(1).getUuid());
        Assert.assertEquals(1, firstChild.getChildNodes().size());
        Assert.assertEquals(child1.getUuid(), firstChild.getChildNodes().get(0).getUuid());
        Assert.assertEquals("Should be root + 2 children + 1 grandchild", 4 + termNodesAtStart, termNodeService.count(TermNode.class));
    }

    @Override
    public void createTestDataSet() throws FileNotFoundException {}
}