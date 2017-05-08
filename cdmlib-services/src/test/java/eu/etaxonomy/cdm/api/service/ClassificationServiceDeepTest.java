/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.api.service;

import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.strategy.parser.NonViralNameParserImpl;
import eu.etaxonomy.cdm.test.integration.CdmTransactionalIntegrationTest;

/**
 * @author n.hoffmann
 * @created Sep 22, 2009
 */
public class ClassificationServiceDeepTest extends CdmTransactionalIntegrationTest{

    private static final Logger logger = Logger.getLogger(ClassificationServiceDeepTest.class);

    @SpringBeanByType
    IClassificationService service;

    @SpringBeanByType
    ITaxonNodeService taxonNodeService;
    private static final List<String> NODE_INIT_STRATEGY = Arrays.asList(new String[]{
            "childNodes",
            "childNodes.taxon",
            "childNodes.taxon.name",
            "taxon.sec",
            "taxon.name.*"
            });


    @Test
    @DataSet
    public final void testFixHierarchy(){
        Classification classification = service.find(UUID.fromString("52b41b07-5500-43ae-82e6-ea2fd328c3d5"));

        Set<TaxonNode> taxonNodes = classification.getAllNodes();//= taxonNodeService.listAllNodesForClassification(classification, 0, null);
        for (TaxonNode node: taxonNodes){
            taxonNodeService.load(node.getUuid(), NODE_INIT_STRATEGY);
        }
        UpdateResult result = service.createHierarchyInClassification(classification, null);
        Classification classification2 = CdmBase.deproxy(result.getCdmEntity(), Classification.class);
        //creating the classification was succesful
        Assert.assertNotNull(classification2);
        for(TaxonNode node: classification2.getAllNodes()){
        	node = taxonNodeService.load(node.getUuid(), NODE_INIT_STRATEGY);
        	//check if TaxonNode was moved and has a new parent-child-relation
        	if(node.getTaxon().getUuid().equals(UUID.fromString("e8bc5566-eca6-4814-b18a-814c16c66144"))){
        		//parent has new Child
        		Assert.assertTrue(node.getCountChildren() == 1);
        		//child is Griftus grifatus subsp. fikus
        		Assert.assertTrue(node.getChildNodes().get(0).getTaxon().getUuid().equals(UUID.fromString("15d719a2-d27d-4366-92de-0898b2f3ebc8")));
        	}else if(node.getTaxon().getUuid().equals(UUID.fromString("15d719a2-d27d-4366-92de-0898b2f3ebc8"))){
        		//Assert that taxon has a parent and that parent is Griftus
        		Assert.assertTrue(node.getParent().getTaxon().getUuid().equals(UUID.fromString("e8bc5566-eca6-4814-b18a-814c16c66144")));
        	}
        	//check if existing parent-child-relation was not touched
        	else if(node.getTaxon().getUuid().equals(UUID.fromString("c1cae3aa-960e-482f-b336-f3e657e96c43"))){
        		//node genus genus
        		Assert.assertTrue(node.getCountChildren()==3);
        		//check if existing parent-child-relation was not touched and has a newly created parentNode
        		//assert this node is not anymore in the highest taxa
        		Assert.assertTrue(!node.isTopmostNode());
        		//assert this node has a parent
        		Assert.assertTrue(node.getParent()!=null);
        	}
        }
        UUID uuid = classification2.getUuid();
        logger.debug("New Classification: " + uuid.toString());
        List<TaxonNode> taxonNodes2 = taxonNodeService.listAllNodesForClassification(classification2, 0, null);
        Assert.assertNotEquals(taxonNodes.size(), taxonNodes2.size());
    }



    @Override
//    @Test
    public void createTestDataSet() throws FileNotFoundException {

    	String[] stringTaxonNames= new String[]{"Griftus grifatus subsp. fikus", "Griftus", "Genus genus subsp. tri", "Genus genus subsp. alt" ,
    			"Genus genus", "Garstig alter subsp. ekel", "Garstig", "Genus genus subsp. genus"};

    	Classification classification = Classification.NewInstance("New Classification");
    	classification.setUuid(UUID.fromString("52b41b07-5500-43ae-82e6-ea2fd328c3d5"));

    	//create Taxa from list
    	Map<String, Taxon> map = new HashMap<String, Taxon>();
    	for(String strName : stringTaxonNames){
    		NonViralNameParserImpl parser = NonViralNameParserImpl.NewInstance();
    		TaxonName<?,?> nameBase = (TaxonName<?,?>)parser.parseFullName(strName);
    		Taxon taxon = Taxon.NewInstance(nameBase, null);
    		map.put(strName, taxon);
    	}

    	//create Hierarchy
    	Taxon tp1 = map.get("Garstig");
    	Taxon tp2 = map.get("Griftus");
    	Taxon tp3 = map.get("Genus genus");
    	Taxon tp4 = map.get("Griftus grifatus subsp. fikus");

    	//create parents:
    	TaxonNode p1 = classification.addChildTaxon(tp1, null, null);
    	TaxonNode p2 = classification.addChildTaxon(tp2, null, null);
    	TaxonNode p3 = classification.addChildTaxon(tp3, null, null);
    	TaxonNode p4 = classification.addChildTaxon(tp4, null, null);


    	taxonNodeService.saveOrUpdate(p1);
    	taxonNodeService.saveOrUpdate(p2);
    	taxonNodeService.saveOrUpdate(p3);
    	taxonNodeService.saveOrUpdate(p4);
    	service.saveOrUpdate(classification);


    	//create children
    	Taxon tc1 = map.get("Garstig alter subsp. ekel");
    	Taxon tc2 = map.get("Genus genus subsp. alt");
    	Taxon tc3 = map.get("Genus genus subsp. tri");
    	Taxon tc4 = map.get("Genus genus subsp. genus");

    	//add to parent node
    	taxonNodeService.saveOrUpdate(p1.addChildTaxon(tc1, null, null));
    	taxonNodeService.saveOrUpdate(p3.addChildTaxon(tc2, null, null));
    	taxonNodeService.saveOrUpdate(p3.addChildTaxon(tc3, null, null));
    	taxonNodeService.saveOrUpdate(p3.addChildTaxon(tc4, null, null));

    	//save classification
    	service.saveOrUpdate(classification);
        commitAndStartNewTransaction(null);

        setComplete();
        endTransaction();

        try {
            writeDbUnitDataSetFile(new String[] {
                    "Classification",
                    "LanguageString",
                    "TaxonNode",
                    "TaxonBase",
                    "TaxonName",
                    "HomotypicalGroup"
            });
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
