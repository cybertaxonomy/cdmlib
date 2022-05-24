// $Id$
/**
* Copyright (C) 2022 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.compare.taxon;

import java.util.ArrayList;
import java.util.Collections;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;

import eu.etaxonomy.cdm.model.name.IBotanicalName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonNameFactory;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;

/**
 *
 * @author a.mueller
 * @date 23.05.2022 (moved and adapted test from prior incorrect location {@link TaxonNodeByNameComparatorTest}.)
 */
public class TaxonNodeNaturalComparatorTest {

    private static final Logger logger = Logger.getLogger(TaxonNodeByNameComparatorTest.class);

    @SuppressWarnings("deprecation")
    @Test
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

        IBotanicalName botname_5 = TaxonNameFactory.NewBotanicalInstance(Rank.SPECIES());
        String nameCache_5= "Epilobium hirsutum L.";
        botname_5.setNameCache(nameCache_5, true);
        Taxon taxon_5 = Taxon.NewInstance(botname_5, sec);

        TaxonNode node1 = classification.addChildTaxon(taxon_1, sec, null);
        TaxonNode node2 = node1.addChildTaxon(taxon_2, sec, null);
        TaxonNode node3 = classification.addChildTaxon(taxon_3, sec, null);
        TaxonNode node4 = node3.addChildTaxon(taxon_4, sec, null);

        //Artificially create tree index as it is usually only created during persisting (save).
        node1.setTreeIndex("#t1#1#10#");
        node2.setTreeIndex("#t1#1#10#11#");
        node3.setTreeIndex("#t1#1#20#");
        node4.setTreeIndex("#t1#1#20#21#");

        classification.getChildNodes();

        ArrayList<TaxonNode> taxonNodes = new ArrayList<>();
        taxonNodes.addAll(classification.getChildNodes());
        taxonNodes.add(node2);
        taxonNodes.add(node4);

        // order using default settings
        TaxonNodeNaturalComparator taxonNodeComparator = new TaxonNodeNaturalComparator();
        Collections.sort(taxonNodes, taxonNodeComparator);

        int i = 0;
        logger.debug("order using default settings");
        logger.debug(taxonNodes.get(i).getTaxon().getName().getNameCache());
        Assert.assertEquals(nameCache_1, taxonNodes.get(i).getTaxon().getName().getNameCache());

        i++;
        logger.debug(taxonNodes.get(i).getTaxon().getName().getNameCache());
        Assert.assertEquals(nameCache_2, taxonNodes.get(i).getTaxon().getName().getNameCache());

        i++;
        logger.debug(taxonNodes.get(3).getTaxon().getName().getNameCache());
        Assert.assertEquals(nameCache_3, taxonNodes.get(i).getTaxon().getName().getNameCache());

        i++;
        logger.debug(taxonNodes.get(i).getTaxon().getName().getNameCache());
        Assert.assertEquals(nameCache_4, taxonNodes.get(i).getTaxon().getName().getNameCache());

        //add aditional taxon
        TaxonNode node5 = classification.addChildTaxon(taxon_5, 1, null, null);
        node5.setTreeIndex("#t1#1#30#");
        Assert.assertTrue(node5.getSortIndex() == 1);
        taxonNodes.add(node5);
        Collections.sort(taxonNodes, taxonNodeComparator);

        i = 0;

        logger.debug(taxonNodes.get(i).getTaxon().getName().getNameCache());
        Assert.assertEquals(nameCache_1, taxonNodes.get(i).getTaxon().getName().getNameCache());
        i++;
        logger.debug(taxonNodes.get(i).getTaxon().getName().getNameCache());
        Assert.assertEquals(nameCache_2, taxonNodes.get(i).getTaxon().getName().getNameCache());
        i++;
        logger.debug(taxonNodes.get(i).getTaxon().getName().getNameCache());
        Assert.assertEquals(nameCache_5, taxonNodes.get(i).getTaxon().getName().getNameCache());
        i++;
        logger.debug(taxonNodes.get(i).getTaxon().getName().getNameCache());
        Assert.assertEquals(nameCache_3, taxonNodes.get(i).getTaxon().getName().getNameCache());

    }

}
