/**
* Copyright (C) 2017 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.strategy.cache.name;

import static org.junit.Assert.assertNotNull;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import eu.etaxonomy.cdm.model.common.DefaultTermInitializer;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.name.TaxonNameFactory;

/**
 * @author a.mueller
 * @date 18.09.2017
 *
 */
public class ViralNameDefaultCacheStrategyTest {
    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(ViralNameDefaultCacheStrategyTest.class);

    private TaxonNameDefaultCacheStrategy strategy;
    private TaxonName viralName;


    @BeforeClass
    public static void setUpBeforeClass() {
        DefaultTermInitializer vocabularyStore = new DefaultTermInitializer();
        vocabularyStore.initialize();
    }

    @Before
    public void setUp() throws Exception {
        strategy = TaxonNameDefaultCacheStrategy.NewInstance();
        viralName = TaxonNameFactory.NewViralInstance(Rank.SPECIES()); //TODO do viral names have ranks? See comment on #IViralName
    }


/********* TEST *******************************************/

    /**
     * Test method for {@link eu.etaxonomy.cdm.strategy.cache.name.BotanicNameDefaultCacheStrategy#NewInstance()}.
     */
    @Test
    public final void testNewInstance() {
        assertNotNull(strategy);
    }

    @Test
    public final void testTitle() {
        Assert.assertEquals("", strategy.getTitleCache(viralName));

        viralName.setTitleCache("My virus", true);
        Assert.assertEquals("My virus", strategy.getTitleCache(viralName));

   }


}
