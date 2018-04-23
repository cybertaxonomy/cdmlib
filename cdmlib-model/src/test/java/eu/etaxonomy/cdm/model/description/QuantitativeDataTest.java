/**
* Copyright (C) 2018 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.description;

import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import eu.etaxonomy.cdm.model.common.DefaultTermInitializer;
import eu.etaxonomy.cdm.model.common.Language;

/**
 * @author a.mueller
 * @since 23.04.2018
 *
 */
public class QuantitativeDataTest {

    private QuantitativeData quantData;

    @BeforeClass
    public static void setUpBeforeClass() {
        if (Language.DEFAULT() == null){
            DefaultTermInitializer vocabularyStore = new DefaultTermInitializer();
            vocabularyStore.initialize();
        }
    }

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        quantData = QuantitativeData.NewInstance();

        StatisticalMeasurementValue statisticalValue = StatisticalMeasurementValue.NewInstance();
        statisticalValue.setType(StatisticalMeasure.AVERAGE() );
        statisticalValue.setValue((float) 23.8);
        quantData.addStatisticalValue(statisticalValue);

    }


    @Test
    public void testClone(){
        QuantitativeData clone = (QuantitativeData) quantData.clone();
        float cloneValue = clone.getStatisticalValues().iterator().next().getValue();
        float origValue = quantData.getStatisticalValues().iterator().next().getValue();
        assertTrue(origValue == cloneValue);
        assertNotSame(clone.getStatisticalValues().iterator().next(), quantData.getStatisticalValues().iterator().next());

    }

}
