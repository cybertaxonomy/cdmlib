/**
* Copyright (C) 2011 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.taxon;

import java.util.ArrayList;
import java.util.Collections;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import eu.etaxonomy.cdm.model.name.IBotanicalName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonNameFactory;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;

/**
 * @author a.kohlbecker
 * @since 18.07.2011
 *
 */
public class TaxonNodeByNameComparatorTest {

    private static final Logger logger = Logger.getLogger(TaxonNodeByNameComparatorTest.class);


    /**
     * test method for {@link TaxonNodeByNameComparator#compare(eu.etaxonomy.cdm.model.taxon.TaxonNode
     * , eu.etaxonomy.cdm.model.taxon.TaxonNode) }
     */
    @Test
    @Ignore
    public void testCompare() {
        Classification classification = Classification.NewInstance("Greuther, 1993");

        Reference sec = ReferenceFactory.newBook();

        IBotanicalName botname_1 = TaxonNameFactory.NewBotanicalInstance(Rank.SPECIES());
        String nameCache_1 = "Epilobium \u00D7aschersonianum Hausskn.";
        botname_1.setNameCache(nameCache_1, true);
        Taxon taxon_1 = Taxon.NewInstance(botname_1, sec);

        IBotanicalName botname_2 = TaxonNameFactory.NewBotanicalInstance(Rank.SPECIES());
        String nameCache_2 = "\u00D7Epilobium \u00D7angustifolium";
        botname_2.setNameCache(nameCache_2, true);
        Taxon taxon_2 = Taxon.NewInstance(botname_2, sec);

        IBotanicalName botname_3 = TaxonNameFactory.NewBotanicalInstance(Rank.SPECIES());
        String nameCache_3 = "Epilobium lamyi";
        botname_3.setNameCache(nameCache_3, true);
        Taxon taxon_3 = Taxon.NewInstance(botname_3, sec);

        IBotanicalName botname_4 = TaxonNameFactory.NewBotanicalInstance(Rank.SPECIES());
        String nameCache_4 = "Epilobium tournefortii";
        botname_4.setNameCache(nameCache_4, true);
        Taxon taxon_4 = Taxon.NewInstance(botname_4, sec);

        classification.addChildTaxon(taxon_1, sec, null);
        classification.addChildTaxon(taxon_2, sec, null);
        classification.addChildTaxon(taxon_3, sec, null);
        classification.addChildTaxon(taxon_4, sec, null);

        classification.getChildNodes();
        ArrayList<TaxonNode> taxonNodes = new ArrayList<TaxonNode>();
        taxonNodes.addAll(classification.getChildNodes());

        // order using default settings
        TaxonNodeByNameComparator taxonNodeByNameComparator = new TaxonNodeByNameComparator();

       // Collections.sort(taxonNodes, taxonNodeByNameComparator);
        int i = 0;
        logger.debug("order using default settings");
        logger.debug(taxonNodes.get(i).getTaxon().getName().getNameCache());
        Assert.assertEquals(nameCache_2, taxonNodes.get(i++).getTaxon().getName().getNameCache());
        logger.debug(taxonNodes.get(i).getTaxon().getName().getNameCache());
        Assert.assertEquals(nameCache_1, taxonNodes.get(i++).getTaxon().getName().getNameCache());
        logger.debug(taxonNodes.get(i).getTaxon().getName().getNameCache());
        Assert.assertEquals(nameCache_3, taxonNodes.get(i++).getTaxon().getName().getNameCache());
        logger.debug(taxonNodes.get(i).getTaxon().getName().getNameCache());
        Assert.assertEquals(nameCache_4, taxonNodes.get(i++).getTaxon().getName().getNameCache());

        // order without ignoring hybrid signs
        taxonNodeByNameComparator.setIgnoreHybridSign(false);

        Collections.sort(taxonNodes, taxonNodeByNameComparator);

        i = 0;
        logger.debug("order without ignoring hybrid signs");
        logger.debug(taxonNodes.get(i).getTaxon().getName().getNameCache());
        Assert.assertEquals(nameCache_3, taxonNodes.get(i++).getTaxon().getName().getNameCache());
        logger.debug(taxonNodes.get(i).getTaxon().getName().getNameCache());
        Assert.assertEquals(nameCache_4, taxonNodes.get(i++).getTaxon().getName().getNameCache());
        logger.debug(taxonNodes.get(i).getTaxon().getName().getNameCache());
        Assert.assertEquals(nameCache_1, taxonNodes.get(i++).getTaxon().getName().getNameCache());
        logger.debug(taxonNodes.get(i).getTaxon().getName().getNameCache());
        Assert.assertEquals(nameCache_2, taxonNodes.get(i++).getTaxon().getName().getNameCache());

    }

    @Test
    public void testNullSave() {
        Classification classification = Classification.NewInstance("Greuther, 1993");

        Reference sec = ReferenceFactory.newBook();

        IBotanicalName botname_1 = TaxonNameFactory.NewBotanicalInstance(Rank.SPECIES());
        String nameCache_1 = "Epilobium \u00D7aschersonianum Hausskn.";
        botname_1.setNameCache(nameCache_1, true);
        Taxon taxon_1 = Taxon.NewInstance(botname_1, sec);

        IBotanicalName botname_2 = TaxonNameFactory.NewBotanicalInstance(Rank.SPECIES());
        String nameCache_2 = "\u00D7Epilobium \u00D7angustifolium";
        botname_2.setNameCache(nameCache_2, true);
        Taxon taxon_2 = Taxon.NewInstance(botname_2, sec);

        TaxonNode node1 = classification.addChildTaxon(taxon_1, sec, null);
        TaxonNode node2 = classification.addChildTaxon(taxon_2, sec, null);

        TaxonNodeByNameComparator taxonNodeByNameComparator = new TaxonNodeByNameComparator();

        Assert.assertEquals(0, taxonNodeByNameComparator.compare(null, null));
        Assert.assertEquals(-1, taxonNodeByNameComparator.compare(node1, null));
        Assert.assertEquals(1, taxonNodeByNameComparator.compare(null, node1));


        node2.getTaxon().setName(null);
        Assert.assertTrue(taxonNodeByNameComparator.compare(node1, node2) > 0);

        node2.setTaxon(null);
        Assert.assertTrue(taxonNodeByNameComparator.compare(node1, node2) > 0);

    }

    /**
     * test method for {@link TaxonNodeByNameComparator#compare(eu.etaxonomy.cdm.model.taxon.TaxonNode
     * , eu.etaxonomy.cdm.model.taxon.TaxonNode) }
     */
    @Test
    @Ignore
    public void testCompareNaturalOrder() {
        Classification classification = Classification.NewInstance("Greuther, 1993");

        Reference sec = ReferenceFactory.newBook();

        IBotanicalName botname_1 = TaxonNameFactory.NewBotanicalInstance(Rank.SPECIES());
        String nameCache_1 = "Epilobium \u00D7aschersonianum Hausskn.";
        botname_1.setNameCache(nameCache_1, true);
        Taxon taxon_1 = Taxon.NewInstance(botname_1, sec);

        IBotanicalName botname_2 = TaxonNameFactory.NewBotanicalInstance(Rank.SPECIES());
        String nameCache_2 = "\u00D7Epilobium \u00D7angustifolium";
        botname_2.setNameCache(nameCache_2, true);
        Taxon taxon_2 = Taxon.NewInstance(botname_2, sec);

        IBotanicalName botname_3 = TaxonNameFactory.NewBotanicalInstance(Rank.SPECIES());
        String nameCache_3 = "Epilobium lamyi";
        botname_3.setNameCache(nameCache_3, true);
        Taxon taxon_3 = Taxon.NewInstance(botname_3, sec);

        IBotanicalName botname_4 = TaxonNameFactory.NewBotanicalInstance(Rank.SPECIES());
        String nameCache_4 = "Epilobium tournefortii";
        botname_4.setNameCache(nameCache_4, true);
        Taxon taxon_4 = Taxon.NewInstance(botname_4, sec);

        IBotanicalName botname_5 = TaxonNameFactory.NewBotanicalInstance(Rank.SPECIES());
        String nameCache_5= "Epilobium hirsutum L.";
        botname_5.setNameCache(nameCache_5, true);
        Taxon taxon_5 = Taxon.NewInstance(botname_5, sec);

        TaxonNode node1 = classification.addChildTaxon(taxon_1, sec, null);
        node1.addChildTaxon(taxon_2, sec, null);
        TaxonNode node2 = classification.addChildTaxon(taxon_3, sec, null);
        node2.addChildTaxon(taxon_4, sec, null);

        classification.getChildNodes();

        ArrayList<TaxonNode> taxonNodes = new ArrayList<TaxonNode>();
        taxonNodes.addAll(classification.getChildNodes());

        // order using default settings
        TaxonNaturalComparator taxonNodeComparator = new TaxonNaturalComparator();
       // Collections.sort(taxonNodes, taxonNodeComparator);

        int i = 0;
        logger.debug("order using default settings");
        logger.debug(taxonNodes.get(i).getTaxon().getName().getNameCache());
        Assert.assertEquals(nameCache_1, taxonNodes.get(i).getTaxon().getName().getNameCache());
        logger.debug(taxonNodes.get(i++).getTaxon().getName().getNameCache());
        Assert.assertEquals(nameCache_2, taxonNodes.get(i).getTaxon().getName().getNameCache());
        logger.debug(taxonNodes.get(i++).getTaxon().getName().getNameCache());
        Assert.assertEquals(nameCache_3, taxonNodes.get(i).getTaxon().getName().getNameCache());
        logger.debug(taxonNodes.get(i++).getTaxon().getName().getNameCache());
        Assert.assertEquals(nameCache_4, taxonNodes.get(i).getTaxon().getName().getNameCache());

        TaxonNode result = classification.addChildTaxon(taxon_5, 2, null, null);
        Assert.assertTrue(result.getSortIndex() == 2);

        i = 0;

        logger.debug(classification.getChildNodes().get(i).getTaxon().getName().getNameCache());
        Assert.assertEquals(nameCache_1, classification.getChildNodes().get(i).getTaxon().getName().getNameCache());
        logger.debug(classification.getChildNodes().get(i++).getTaxon().getName().getNameCache());
        Assert.assertEquals(nameCache_2, classification.getChildNodes().get(i).getTaxon().getName().getNameCache());
        logger.debug(classification.getChildNodes().get(i++).getTaxon().getName().getNameCache());
        Assert.assertEquals(nameCache_5, classification.getChildNodes().get(i).getTaxon().getName().getNameCache());
        logger.debug(classification.getChildNodes().get(i++).getTaxon().getName().getNameCache());
        Assert.assertEquals(nameCache_3, classification.getChildNodes().get(i).getTaxon().getName().getNameCache());

    }


}
