/**
* Copyright (C) 2021 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.strategy.cache;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @author a.mueller
 * @since 23.02.2021
 */
public class TaggedCacheHelperTest {

    private List<TaggedText> tags;

    @BeforeEach
    public void setUp() throws Exception {
        tags = new ArrayList<>();
        tags.add(TaggedText.NewInstance(TagEnum.label, "My"));
        tags.add(TaggedText.NewInstance(TagEnum.label, "taxon"));
    }

    @Test
    public void test() {
        assertEquals("My taxon", TaggedTextFormatter.createString(tags));

        tags.add(1, TaggedText.NewInstance(TagEnum.separator, ":"));
        assertEquals("My:taxon", TaggedTextFormatter.createString(tags));
    }

    @Test
    public void testHtmlRules() {
        HTMLTagRules rules = new HTMLTagRules();
        rules.addRule(TagEnum.label, "b");
        assertEquals("<b>My taxon</b>", TaggedTextFormatter.createString(tags, rules));

        tags.add(1, TaggedText.NewInstance(TagEnum.separator, ":"));
        assertEquals("<b>My</b>:<b>taxon</b>", TaggedTextFormatter.createString(tags, rules));
    }

}
