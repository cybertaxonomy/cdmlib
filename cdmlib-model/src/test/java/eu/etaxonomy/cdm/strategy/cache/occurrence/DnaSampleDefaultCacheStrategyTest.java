/**
* Copyright (C) 2021 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.strategy.cache.occurrence;

import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;

import eu.etaxonomy.cdm.model.common.Identifier;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.molecular.DnaSample;
import eu.etaxonomy.cdm.model.occurrence.Collection;
import eu.etaxonomy.cdm.model.term.DefinedTerm;
import eu.etaxonomy.cdm.test.TermTestBase;

/**
 * @author a.mueller
 * @since 09.01.2021
 */
public class DnaSampleDefaultCacheStrategyTest extends TermTestBase {

    @Test
    public void test() {
        DnaSample specimen = DnaSample.NewInstance();
        specimen.setUuid(UUID.fromString("b5fa679f-12a1-4f47-906a-28b41c90f019"));
        DnaSampleDefaultCacheStrategy strategy = new DnaSampleDefaultCacheStrategy();

        Assert.assertEquals("DnaSample#0<b5fa679f-12a1-4f47-906a-28b41c90f019>", strategy.getTitleCache(specimen));

        Collection collection = Collection.NewInstance();
        collection.setCode("B");
        specimen.setCollection(collection);
        Assert.assertEquals("B", strategy.getTitleCache(specimen));

        specimen.setAccessionNumber("123");
        Assert.assertEquals("B 123", strategy.getTitleCache(specimen));

        Identifier identifier = Identifier.NewInstance(specimen, "id3", DefinedTerm.IDENTIFIER_NAME_IPNI());
        Assert.assertEquals("B 123", strategy.getTitleCache(specimen));
        specimen.setCollection(null);
        specimen.setAccessionNumber(null);
        Assert.assertEquals("id3", strategy.getTitleCache(specimen));

        specimen.putDefinition(Language.ENGLISH(), "sample definition");
        Assert.assertEquals("id3", strategy.getTitleCache(specimen));
        specimen.removeIdentifier(identifier);
        Assert.assertEquals("sample definition", strategy.getTitleCache(specimen));

        specimen.addImportSource("123", "dna", null, null);
        Assert.assertEquals("sample definition", strategy.getTitleCache(specimen));
        specimen.removeDefinition(Language.ENGLISH());
        Assert.assertEquals("dna:123", strategy.getTitleCache(specimen));

        Assert.assertNull(strategy.getTitleCache(null));
    }
}