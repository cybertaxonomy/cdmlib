/**
* Copyright (C) 2025 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.strategy.cache.occurrence;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.etaxonomy.cdm.model.occurrence.Collection;

/**
 * Unit tests for {@link CollectionDefaultCacheStrategy}
 *
 * @author muellera
 * @since 05.11.2025
 */
public class CollectionDefaultCacheStrategyTest {

    private CollectionDefaultCacheStrategy formatter;

    @BeforeEach
    public void setUp() throws Exception {
        formatter = new CollectionDefaultCacheStrategy();
    }

    @Test
    public void test() {

        //create data
        Collection collection = Collection.NewInstance("B", "Herbarium berolinense");
        collection.setTownOrLocation("Berlin ");

        //default
        assertEquals("B - Herbarium berolinense (Berlin)", formatter.getTitleCache(collection));

        //no code
        collection.setCode(null);
        assertEquals("Herbarium berolinense (Berlin)", formatter.getTitleCache(collection));

        //no name
        collection.setCode("B");
        collection.setName(null);
        assertEquals("B (Berlin)", formatter.getTitleCache(collection));

        //no code & name
        collection.setCode(null);
        assertEquals("Berlin", formatter.getTitleCache(collection));

        //no town
        collection = Collection.NewInstance("B", "Herbarium berolinense");
        assertEquals("B - Herbarium berolinense", formatter.getTitleCache(collection));

        //equal code and name
        collection.setCode("Herbarium berolinense");
        assertEquals("Herbarium berolinense", formatter.getTitleCache(collection));
        collection.setCode(" Herbarium berolinense ");
        assertEquals("Herbarium berolinense", formatter.getTitleCache(collection));
        collection.setCode("Herbarium berolinense  ");
        assertEquals("Herbarium berolinense", formatter.getTitleCache(collection));

        //empty
        collection = Collection.NewInstance();
        assertEquals("Collection#0<"+collection.getUuid()+">",
                formatter.getTitleCache(collection));
    }
}