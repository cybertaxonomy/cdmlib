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
import org.junit.Before;
import org.junit.Test;

import eu.etaxonomy.cdm.common.URI;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.common.IdentifiableSource;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.media.ImageFile;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.media.MediaRepresentation;
import eu.etaxonomy.cdm.model.occurrence.Collection;
import eu.etaxonomy.cdm.model.occurrence.MediaSpecimen;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationType;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.strategy.parser.TimePeriodParser;
import eu.etaxonomy.cdm.test.TermTestBase;

/**
 * @author a.mueller
 * @since 08.01.2021
 */
public class MediaSpecimenDefaultCacheStrategyTest extends TermTestBase {

    private MediaSpecimenDefaultCacheStrategy strategy;
    private MediaSpecimen specimen;
    private Collection collection;

    @Before
    public void setUp() {
        strategy = MediaSpecimenDefaultCacheStrategy.NewInstance();
        specimen = MediaSpecimen.NewInstance(SpecimenOrObservationType.Media);
        specimen.setUuid(UUID.fromString("b5fa679f-12a1-4f47-906a-28b41c90f019"));
        collection = Collection.NewInstance();
        collection.setCode("B");
    }

    @Test
    public void test() {

        //empty
//        Assert.assertEquals("MediaSpecimen#0<b5fa679f-12a1-4f47-906a-28b41c90f019>", strategy.getTitleCache(specimen));
        Assert.assertEquals("[icon]", strategy.getTitleCache(specimen));

        //with collection
        specimen.setCollection(collection);
        Assert.assertEquals("[icon] B", strategy.getTitleCache(specimen));

        //collection and accession number
        specimen.setAccessionNumber("123");
        Assert.assertEquals("[icon] B 123", strategy.getTitleCache(specimen));

        //with collection, accession number and titled media
        Media media = Media.NewInstance();
        media.putTitle(Language.DEFAULT(), "Media title");
        media.setArtist(Person.NewTitledInstance("Artist"));
        specimen.setMediaSpecimen(media);
        Assert.assertEquals("[icon] B 123 (Media title)", strategy.getTitleCache(specimen));
        //#9632 test putXXX
        specimen.getMediaSpecimen().putTitle(Language.DEFAULT(), "Media Second Title");
        Assert.assertEquals("[icon] B 123 (Media Second Title)", strategy.getTitleCache(specimen));

        //with collection, accession number, titled media and artist
        specimen.getMediaSpecimen().setArtist(Person.NewTitledInstance("Artist 2"));
        Assert.assertEquals("[icon] B 123 (Media Second Title)", strategy.getTitleCache(specimen));

        //remove collection and accession number
        //titled media and artist only
        specimen.setCollection(null);
        specimen.setAccessionNumber(null);
        Assert.assertEquals("[icon] Media Second Title", strategy.getTitleCache(specimen));

        //remove title
        //artist only
        media.putTitle(Language.DEFAULT(), null);
        Assert.assertEquals("[icon] Artist 2", strategy.getTitleCache(specimen));

        //remove artist,
        //empty specimen with only empty media attached
        media.setArtist(null);
//        Assert.assertEquals("MediaSpecimen#0<b5fa679f-12a1-4f47-906a-28b41c90f019>", strategy.getTitleCache(specimen));
        Assert.assertEquals("[icon]", strategy.getTitleCache(specimen));

        //fully empty but with media representation filename
        MediaRepresentation mediaRep = MediaRepresentation.NewInstance("jpg", null, URI.create("https://www.abc.de/test.jpg"), 20, ImageFile.class);
        media.addRepresentation(mediaRep);
        Assert.assertEquals("[icon] test", strategy.getTitleCache(specimen));

        //null
        Assert.assertNull(strategy.getTitleCache(null));
    }

    @Test
    public void testReferenceMediaSpecimen() {

        strategy = MediaSpecimenDefaultCacheStrategy.NewInstance(true);

        //Reference media specimen are MediaSpecimen which represent e.g. figures in
        //a publication. They often do not have a media attached but therefore should have a source

        //empty
//        Assert.assertEquals("MediaSpecimen#0<b5fa679f-12a1-4f47-906a-28b41c90f019>", strategy.getTitleCache(specimen));
        Assert.assertEquals("[icon]", strategy.getTitleCache(specimen));

        Media media = Media.NewInstance();
//        media.putTitle(Language.DEFAULT(), "Media title");
//        media.setArtist(Person.NewTitledInstance("Artist"));
        specimen.setMediaSpecimen(media);
       // Assert.assertEquals("MediaSpecimen#0<b5fa679f-12a1-4f47-906a-28b41c90f019>", strategy.getTitleCache(specimen));
        Assert.assertEquals("[icon]", strategy.getTitleCache(specimen));

        Reference book = ReferenceFactory.newBook();
        book.setTitle("My book");
        book.setDatePublished(TimePeriodParser.parseStringVerbatim("1972"));
        IdentifiableSource source = IdentifiableSource.NewPrimaryMediaSourceInstance(book, "25");
        specimen.getMediaSpecimen().addSource(source);
        Assert.assertEquals("[icon] in My book 1972: 25", strategy.getTitleCache(specimen));

        media.setTitleCache("Media title");
        Assert.assertEquals("[icon] Media title in My book 1972: 25", strategy.getTitleCache(specimen));

        //use reference only if no collection data available
        specimen.setCollection(collection);
        Assert.assertEquals("[icon] B (Media title)", strategy.getTitleCache(specimen));
        specimen.setCollection(null);
        specimen.setAccessionNumber("123");
        Assert.assertEquals("[icon] 123 (Media title)", strategy.getTitleCache(specimen));
    }

}