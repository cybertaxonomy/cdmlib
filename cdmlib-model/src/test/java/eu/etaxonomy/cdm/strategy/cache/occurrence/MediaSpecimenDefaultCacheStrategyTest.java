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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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

    @BeforeEach
    public void setUp() {
        strategy = MediaSpecimenDefaultCacheStrategy.NewInstance();
        specimen = MediaSpecimen.NewInstance(SpecimenOrObservationType.Media);
        specimen.setUuid(UUID.fromString("b5fa679f-12a1-4f47-906a-28b41c90f019"));
        collection = Collection.NewInstance();
        collection.setCode("B");
    }

    @Test
    public void testWithoutReference() {

        //empty
//        assertEquals("MediaSpecimen#0<b5fa679f-12a1-4f47-906a-28b41c90f019>", strategy.getTitleCache(specimen));
        assertEquals("[icon]", strategy.getTitleCache(specimen));

        //with collection
        specimen.setCollection(collection);
        assertEquals("[icon] B", strategy.getTitleCache(specimen));

        //collection and accession number
        specimen.setAccessionNumber("123");
        assertEquals("[icon] B 123", strategy.getTitleCache(specimen));

        //with collection, accession number and titled media
        Media media = Media.NewInstance();
        media.putTitle(Language.DEFAULT(), "Media title");
        media.setArtist(Person.NewTitledInstance("Artist"));
        specimen.setMediaSpecimen(media);
        assertEquals("[icon] B 123 (Media title)", strategy.getTitleCache(specimen));
        //#9632 test putXXX
        specimen.getMediaSpecimen().putTitle(Language.DEFAULT(), "Media Second Title");
        assertEquals("[icon] B 123 (Media Second Title)", strategy.getTitleCache(specimen));

        //with collection, accession number, titled media and artist
        specimen.getMediaSpecimen().setArtist(Person.NewTitledInstance("Artist 2"));
        assertEquals("[icon] B 123 (Media Second Title)", strategy.getTitleCache(specimen));

        //remove collection and accession number
        //titled media and artist only
        specimen.setCollection(null);
        specimen.setAccessionNumber(null);
        assertEquals("[icon] Media Second Title", strategy.getTitleCache(specimen));

        //remove title
        //artist only
        media.putTitle(Language.DEFAULT(), null);
        assertEquals("[icon] Artist 2", strategy.getTitleCache(specimen));

        //remove artist,
        //empty specimen with only empty media attached
        media.setArtist(null);
//        assertEquals("MediaSpecimen#0<b5fa679f-12a1-4f47-906a-28b41c90f019>", strategy.getTitleCache(specimen));
        assertEquals("[icon]", strategy.getTitleCache(specimen));

        //fully empty but with media representation filename
        MediaRepresentation mediaRep = MediaRepresentation.NewInstance(URI.create("https://www.abc.de/test.jpg"), "jpg", null, 20, ImageFile.class);
        media.addRepresentation(mediaRep);
        assertEquals("[icon] test", strategy.getTitleCache(specimen));

        //null
        assertNull(strategy.getTitleCache(null));
    }

    //#10537, #10596
    @Test
    public void testReferenceMediaSpecimen() {

        strategy = MediaSpecimenDefaultCacheStrategy.NewInstance(true);

        //Reference media specimen are MediaSpecimen which represent e.g. figures in
        //a publication. They often do not have a media attached but therefore should have a source

        //empty
        //old: assertEquals("MediaSpecimen#0<b5fa679f-12a1-4f47-906a-28b41c90f019>", strategy.getTitleCache(specimen));
        assertEquals("[icon]", strategy.getTitleCache(specimen));

        Media media = Media.NewInstance();
        specimen.setMediaSpecimen(media);
        //old: assertEquals("MediaSpecimen#0<b5fa679f-12a1-4f47-906a-28b41c90f019>", strategy.getTitleCache(specimen));
        assertEquals("[icon]", strategy.getTitleCache(specimen));

        Reference book = ReferenceFactory.newBook();
        book.setTitle("My book");
        book.setDatePublished(TimePeriodParser.parseStringVerbatim("1972"));
        IdentifiableSource source = IdentifiableSource.NewPrimaryMediaSourceInstance(book, "25");
        specimen.getMediaSpecimen().addSource(source);
        assertEquals("[icon] in My book 1972: 25", strategy.getTitleCache(specimen));

        media.setTitleCache("Media title");
        assertEquals("[icon] Media title in My book 1972: 25", strategy.getTitleCache(specimen));

        //use reference only if no collection data available
        specimen.setCollection(collection);
        assertEquals("[icon] B (Media title)", strategy.getTitleCache(specimen));
        specimen.setCollection(null);
        specimen.setAccessionNumber("123");
        assertEquals("[icon] 123 (Media title)", strategy.getTitleCache(specimen));
    }

}