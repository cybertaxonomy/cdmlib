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

import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.occurrence.Collection;
import eu.etaxonomy.cdm.model.occurrence.MediaSpecimen;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationType;
import eu.etaxonomy.cdm.test.TermTestBase;

/**
 * @author a.mueller
 * @since 08.01.2021
 */
public class MediaSpecimenDefaultCacheStrategyTest extends TermTestBase {

    @Test
    public void test() {
        MediaSpecimen specimen = MediaSpecimen.NewInstance(SpecimenOrObservationType.Media);
        specimen.setUuid(UUID.fromString("b5fa679f-12a1-4f47-906a-28b41c90f019"));
        MediaSpecimenDefaultCacheStrategy strategy = new MediaSpecimenDefaultCacheStrategy();

        Assert.assertEquals("MediaSpecimen#0<b5fa679f-12a1-4f47-906a-28b41c90f019>", strategy.getTitleCache(specimen));

        Collection collection = Collection.NewInstance();
        collection.setCode("B");
        specimen.setCollection(collection);
        Assert.assertEquals("B", strategy.getTitleCache(specimen));

        specimen.setAccessionNumber("123");
        Assert.assertEquals("B 123", strategy.getTitleCache(specimen));

        Media media = Media.NewInstance();
        media.setTitleCache("Media title");
        specimen.setMediaSpecimen(media);
        Assert.assertEquals("B 123 (Media title)", strategy.getTitleCache(specimen));

        specimen.setCollection(null);
        specimen.setAccessionNumber(null);
        Assert.assertEquals("Media title", strategy.getTitleCache(specimen));
    }
}