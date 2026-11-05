/**
* Copyright (C) 2017 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.strategy.cache.name;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.name.TaxonNameFactory;
import eu.etaxonomy.cdm.test.TermTestBase;

/**
 * @author a.mueller
 * @since 18.09.2017
 */
public class ViralNameDefaultCacheStrategyTest extends TermTestBase {

    @SuppressWarnings("unused")
    private static final Logger logger = LogManager.getLogger();

    private TaxonNameDefaultCacheStrategy strategy;
    private TaxonName viralName;

    @BeforeEach
    public void setUp() throws Exception {
        strategy = TaxonNameDefaultCacheStrategy.NewInstance();
        viralName = TaxonNameFactory.NewViralInstance(Rank.SPECIES()); //TODO do viral names have ranks? See comment on #IViralName
    }

//********* TEST *******************************************/

    @Test
    public final void testNewInstance() {
        assertNotNull(strategy);
    }

    @Test
    public final void testTitle() {
        assertEquals("", strategy.getTitleCache(viralName));

        viralName.setTitleCache("My virus", true);
        assertEquals("My virus", strategy.getTitleCache(viralName));
   }
}