// $Id$
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
import org.junit.Test;

import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.model.taxon.TaxonNodeByNameComparator;

/**
 * @author a.kohlbecker
 * @date 18.07.2011
 *
 */
public class TaxonNodeByNameComparatorTest {

    private static final Logger logger = Logger.getLogger(TaxonNodeByNameComparatorTest.class);



    /**
     * test method for {@link TaxonNodeByNameComparator#compare(eu.etaxonomy.cdm.model.taxon.TaxonNode
     * , eu.etaxonomy.cdm.model.taxon.TaxonNode) }
     */
    @Test
    public void testCompare() {
        Classification classification = Classification.NewInstance("Greuther, 1993");

        Reference sec = ReferenceFactory.newBook();

        BotanicalName botname_1 = BotanicalName.NewInstance(Rank.SPECIES());
        String nameCache_1 = "Epilobium \u00D7aschersonianum Hausskn.";
        botname_1.setNameCache(nameCache_1, true);
        Taxon taxon_1 = Taxon.NewInstance(botname_1, sec);

        BotanicalName botname_2 = BotanicalName.NewInstance(Rank.SPECIES());
        String nameCache_2 = "\u00D7Epilobium \u00D7angustifolium";
        botname_2.setNameCache(nameCache_2, true);
        Taxon taxon_2 = Taxon.NewInstance(botname_2, sec);

        BotanicalName botname_3 = BotanicalName.NewInstance(Rank.SPECIES());
        String nameCache_3 = "Epilobium lamyi";
        botname_3.setNameCache(nameCache_3, true);
        Taxon taxon_3 = Taxon.NewInstance(botname_3, sec);

        BotanicalName botname_4 = BotanicalName.NewInstance(Rank.SPECIES());
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

        Collections.sort(taxonNodes, taxonNodeByNameComparator);
        int i = 0;
        logger.debug("order using default settings");
        logger.debug(((BotanicalName)taxonNodes.get(i).getTaxon().getName()).getNameCache());
        Assert.assertEquals(nameCache_2, ((BotanicalName)taxonNodes.get(i++).getTaxon().getName()).getNameCache());
        logger.debug(((BotanicalName)taxonNodes.get(i).getTaxon().getName()).getNameCache());
        Assert.assertEquals(nameCache_1, ((BotanicalName)taxonNodes.get(i++).getTaxon().getName()).getNameCache());
        logger.debug(((BotanicalName)taxonNodes.get(i).getTaxon().getName()).getNameCache());
        Assert.assertEquals(nameCache_3, ((BotanicalName)taxonNodes.get(i++).getTaxon().getName()).getNameCache());
        logger.debug(((BotanicalName)taxonNodes.get(i).getTaxon().getName()).getNameCache());
        Assert.assertEquals(nameCache_4, ((BotanicalName)taxonNodes.get(i++).getTaxon().getName()).getNameCache());

        // order without ignoring hybrid signs
        taxonNodeByNameComparator.setIgnoreHybridSign(false);

        Collections.sort(taxonNodes, taxonNodeByNameComparator);

        i = 0;
        logger.debug("order without ignoring hybrid signs");
        logger.debug(((BotanicalName)taxonNodes.get(i).getTaxon().getName()).getNameCache());
        Assert.assertEquals(nameCache_3, ((BotanicalName)taxonNodes.get(i++).getTaxon().getName()).getNameCache());
        logger.debug(((BotanicalName)taxonNodes.get(i).getTaxon().getName()).getNameCache());
        Assert.assertEquals(nameCache_4, ((BotanicalName)taxonNodes.get(i++).getTaxon().getName()).getNameCache());
        logger.debug(((BotanicalName)taxonNodes.get(i).getTaxon().getName()).getNameCache());
        Assert.assertEquals(nameCache_1, ((BotanicalName)taxonNodes.get(i++).getTaxon().getName()).getNameCache());
        logger.debug(((BotanicalName)taxonNodes.get(i).getTaxon().getName()).getNameCache());
        Assert.assertEquals(nameCache_2, ((BotanicalName)taxonNodes.get(i++).getTaxon().getName()).getNameCache());

    }

}
