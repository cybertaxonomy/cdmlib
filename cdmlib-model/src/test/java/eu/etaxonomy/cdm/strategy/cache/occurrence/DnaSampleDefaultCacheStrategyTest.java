/**
* Copyright (C) 2021 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.strategy.cache.occurrence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import eu.etaxonomy.cdm.model.common.Identifier;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.molecular.DnaSample;
import eu.etaxonomy.cdm.model.occurrence.Collection;
import eu.etaxonomy.cdm.model.term.IdentifierType;
import eu.etaxonomy.cdm.test.TermTestBase;

/**
 * @author a.mueller
 * @since 09.01.2021
 */
public class DnaSampleDefaultCacheStrategyTest extends TermTestBase {

    @Test
    public void test() {

        DnaSample dnaSample = DnaSample.NewInstance();
        dnaSample.setUuid(UUID.fromString("b5fa679f-12a1-4f47-906a-28b41c90f019"));
        DnaSampleDefaultCacheStrategy strategy = new DnaSampleDefaultCacheStrategy();

        assertEquals("DnaSample#0<b5fa679f-12a1-4f47-906a-28b41c90f019>", strategy.getTitleCache(dnaSample));

        Collection collection = Collection.NewInstance();
        collection.setCode("B");
        dnaSample.setCollection(collection);
        assertEquals("B", strategy.getTitleCache(dnaSample));

        dnaSample.setAccessionNumber("123");
        assertEquals("B 123", strategy.getTitleCache(dnaSample));

        Identifier identifier = Identifier.NewInstance(dnaSample, "id3", IdentifierType.IDENTIFIER_NAME_IPNI());
        assertEquals("B 123", strategy.getTitleCache(dnaSample));
        dnaSample.setCollection(null);
        dnaSample.setAccessionNumber(null);
        assertEquals("id3", strategy.getTitleCache(dnaSample));

        dnaSample.putDefinition(Language.ENGLISH(), "sample definition");
        assertEquals("id3", strategy.getTitleCache(dnaSample));
        dnaSample.removeIdentifier(identifier);
        assertEquals("sample definition", strategy.getTitleCache(dnaSample));

        dnaSample.addImportSource("123", "dna", null, null);
        assertEquals("sample definition", strategy.getTitleCache(dnaSample));
        dnaSample.removeDefinition(Language.ENGLISH());
        assertEquals("dna:123", strategy.getTitleCache(dnaSample));

        assertNull(strategy.getTitleCache(null));
    }
}