/**
* Copyright (C) 2021 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.name;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import eu.etaxonomy.cdm.api.service.exception.TypeDesignationSetException;
import eu.etaxonomy.cdm.common.URI;
import eu.etaxonomy.cdm.common.UTF8;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.common.IdentifiableSource;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.name.NameTypeDesignation;
import eu.etaxonomy.cdm.model.name.NameTypeDesignationStatus;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatus;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatusType;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignation;
import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignationStatus;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.name.TaxonNameFactory;
import eu.etaxonomy.cdm.model.name.TextualTypeDesignation;
import eu.etaxonomy.cdm.model.name.TypeDesignationBase;
import eu.etaxonomy.cdm.model.occurrence.Collection;
import eu.etaxonomy.cdm.model.occurrence.DerivationEvent;
import eu.etaxonomy.cdm.model.occurrence.DerivationEventType;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnit;
import eu.etaxonomy.cdm.model.occurrence.FieldUnit;
import eu.etaxonomy.cdm.model.occurrence.MediaSpecimen;
import eu.etaxonomy.cdm.model.occurrence.OccurrenceStatus;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationType;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.model.term.DefinedTerm;
import eu.etaxonomy.cdm.ref.TypedEntityReferenceFactory;
import eu.etaxonomy.cdm.strategy.cache.TagEnum;
import eu.etaxonomy.cdm.strategy.cache.TaggedText;
import eu.etaxonomy.cdm.strategy.cache.TaggedTextFormatter;
import eu.etaxonomy.cdm.strategy.cache.TaggedTextWithLink;
import eu.etaxonomy.cdm.strategy.parser.TimePeriodParser;
import eu.etaxonomy.cdm.test.TermTestBase;

/**
 * See also {@link TypeDesignationSetContainerTest} for further tests.
 *
 * @author a.mueller
 * @since 30.03.2021
 */
public class TypeDesignationSetContainerFormatterTest extends TermTestBase{

    private static final String DASH_W = UTF8.EN_DASH_SPATIUM.toString();

    //variables and setup were copied from TypeDesignationSetContainerTest
    //not all of them are in use yet
    private NameTypeDesignation ntd;
    private NameTypeDesignation ntd_LT;
    private SpecimenTypeDesignation std_IT;
    private SpecimenTypeDesignation std_HT;
    private SpecimenTypeDesignation std_LT;
    private SpecimenTypeDesignation std_IT_2;
    private SpecimenTypeDesignation std_IT_3;
    private SpecimenTypeDesignation mstd_HT_published;
    private SpecimenTypeDesignation mtd_IT_unpublished;
    private Reference book;
    private Team team;

    @Before
    public void setUp() throws Exception {
        Person person1 = Person.NewInstance("DC", "Decandolle", "A.", null);
        Person person2 = Person.NewInstance("Hab.", "Haber", "M.", null);
        Person person3 = Person.NewInstance("Moler", "Moler", "A.P.", null);
        team = Team.NewInstance(person1, person2, person3);

        book = ReferenceFactory.newBook();
        book.setAuthorship(team);
        book.setTitle("My interesting book");
        book.setDatePublished(TimePeriodParser.parseStringVerbatim("11 Apr 1962"));

        ntd = NameTypeDesignation.NewInstance();
        ntd.setId(1);
        TaxonName typeName = TaxonNameFactory.PARSED_BOTANICAL("Prionus coriatius L.");
        ntd.setTypeName(typeName);
//        Reference citation = ReferenceFactory.newGeneric();
//        citation.setTitleCache("Species Plantarum", true);
//        ntd.setCitation(citation);
//      ntd.addPrimaryTaxonomicSource(citation, null);

        ntd_LT = NameTypeDesignation.NewInstance();
        ntd_LT.setTypeStatus(NameTypeDesignationStatus.LECTOTYPE());
        TaxonName typeName2 = TaxonNameFactory.NewBotanicalInstance(Rank.SPECIES());
        typeName2.setTitleCache("Prionus arealus L.", true);
        ntd_LT.setTypeName(typeName2);
        ntd_LT.setCitation(book);

        FieldUnit fu_1 = FieldUnit.NewInstance();
        fu_1.setId(1);
        fu_1.setTitleCache("Testland, near Bughausen, A.Kohlbecker 81989, 2017", true);

        FieldUnit fu_2 = FieldUnit.NewInstance();
        fu_2.setId(2);
        fu_2.setTitleCache("Dreamland, near Kissingen, A.Kohlbecker 66211, 2017", true);

        std_HT = SpecimenTypeDesignation.NewInstance();
        std_HT.setId(1);
        DerivedUnit specimen_HT = DerivedUnit.NewInstance(SpecimenOrObservationType.PreservedSpecimen);
        Collection collection_OHA = Collection.NewInstance("OHA", null);
        specimen_HT.setCollection(collection_OHA);
        specimen_HT.setAccessionNumber("OHA 1234");
        createDerivationEvent(fu_1, specimen_HT);
        specimen_HT.getOriginals().add(fu_1);
        std_HT.setTypeSpecimen(specimen_HT);
        std_HT.setTypeStatus(SpecimenTypeDesignationStatus.HOLOTYPE());

        std_IT = SpecimenTypeDesignation.NewInstance();
        std_IT.setId(2);
        DerivedUnit specimen_IT = DerivedUnit.NewInstance(SpecimenOrObservationType.PreservedSpecimen);
        specimen_IT.setTitleCache("BER", true);
        createDerivationEvent(fu_1, specimen_IT);
        std_IT.setTypeSpecimen(specimen_IT);
        std_IT.setTypeStatus(SpecimenTypeDesignationStatus.ISOTYPE());

        std_LT = SpecimenTypeDesignation.NewInstance();
        DerivedUnit specimen_LT = DerivedUnit.NewInstance(SpecimenOrObservationType.PreservedSpecimen);
        specimen_LT.setTitleCache("LEC", true);
        createDerivationEvent(fu_1, specimen_LT);
        std_LT.setTypeSpecimen(specimen_LT);
        std_LT.setTypeStatus(SpecimenTypeDesignationStatus.LECTOTYPE());
        std_LT.setCitation(book);

        std_IT_2 = SpecimenTypeDesignation.NewInstance();
        std_IT_2.setId(3);
        DerivedUnit specimen_IT_2 = DerivedUnit.NewInstance(SpecimenOrObservationType.PreservedSpecimen);
        specimen_IT_2.setTitleCache("KEW", true);
        createDerivationEvent(fu_1, specimen_IT_2);
        std_IT_2.setTypeSpecimen(specimen_IT_2);
        std_IT_2.setTypeStatus(SpecimenTypeDesignationStatus.ISOTYPE());

        std_IT_3 = SpecimenTypeDesignation.NewInstance();
        std_IT_3.setId(4);
        DerivedUnit specimen_IT_3 = DerivedUnit.NewInstance(SpecimenOrObservationType.PreservedSpecimen);
        specimen_IT_3.setTitleCache("M", true);
        createDerivationEvent(fu_2, specimen_IT_3);
        std_IT_3.setTypeSpecimen(specimen_IT_3);
        std_IT_3.setTypeStatus(SpecimenTypeDesignationStatus.ISOTYPE());

        mstd_HT_published = SpecimenTypeDesignation.NewInstance();
        mstd_HT_published.setId(5);
        MediaSpecimen mediaSpecimen_published = (MediaSpecimen)DerivedUnit.NewInstance(SpecimenOrObservationType.Media);
        Media media = Media.NewInstance();
        Reference ref = ReferenceFactory.newGeneric();
        ref.setTitleCache("A.K. & W.K (2008) Algae of the BGBM", true);
        media.addSource(IdentifiableSource.NewPrimaryMediaSourceInstance(ref, "p.33"));
        mediaSpecimen_published.setMediaSpecimen(media);
        createDerivationEvent(fu_1, mediaSpecimen_published);
        mstd_HT_published.setTypeSpecimen(mediaSpecimen_published);
        mstd_HT_published.setTypeStatus(SpecimenTypeDesignationStatus.HOLOTYPE());

        mtd_IT_unpublished = SpecimenTypeDesignation.NewInstance();
        mtd_IT_unpublished.setId(6);
        MediaSpecimen mediaSpecimen_unpublished = (MediaSpecimen)DerivedUnit.NewInstance(SpecimenOrObservationType.Media);
        eu.etaxonomy.cdm.model.occurrence.Collection collection = eu.etaxonomy.cdm.model.occurrence.Collection.NewInstance();
        collection.setCode("B");
        mediaSpecimen_unpublished.setCollection(collection);
        mediaSpecimen_unpublished.setAccessionNumber("Slide A565656");
        createDerivationEvent(fu_1, mediaSpecimen_unpublished);
        mtd_IT_unpublished.setTypeSpecimen(mediaSpecimen_unpublished);
        mtd_IT_unpublished.setTypeStatus(SpecimenTypeDesignationStatus.ISOTYPE());
    }

    protected void createDerivationEvent(FieldUnit fu_1, DerivedUnit specimen_IT_2) {
        DerivationEvent derivationEvent_3 = DerivationEvent.NewInstance(DerivationEventType.ACCESSIONING());
        derivationEvent_3.addOriginal(fu_1);
        derivationEvent_3.addDerivative(specimen_IT_2);
    }

    @Test
    public void testNameTypeDesignationTaggedText() throws TypeDesignationSetException {

        @SuppressWarnings("rawtypes")
        List<TypeDesignationBase> tds = new ArrayList<>();
        tds.add(ntd);

        TaxonName typifiedName = TaxonNameFactory.NewBotanicalInstance(Rank.GENUS());
        typifiedName.setTitleCache("Prionus L.", true);

        typifiedName.addTypeDesignation(ntd, false);

        TypeDesignationSetContainer manager = TypeDesignationSetContainer.NewDefaultInstance(tds);
        TypeDesignationSetContainerFormatter formatter = new TypeDesignationSetContainerFormatter(true, true, true, false, false);
        String text = formatter.format(manager);
        Assert.assertEquals("Prionus L."+DASH_W+"Nametype: Prionus coriatius L.", text);

        List<TaggedText> taggedText = formatter.toTaggedText(manager);
        Assert.assertEquals("first entry should be the typified name",
                new TaggedText(TagEnum.name, "Prionus L.",TypedEntityReferenceFactory.fromEntity(typifiedName, false)), taggedText.get(0));
        Assert.assertEquals("fourth entry should be the name type nameCache",
                new TaggedText(TagEnum.name, "Prionus"), taggedText.get(4));  //maybe in future the entityReference should be TypedEntityReference.fromEntity(ntd.getTypeName(), false)
        Assert.assertEquals("fourth entry should be the name type nameCache",
                new TaggedText(TagEnum.name, "coriatius"), taggedText.get(5)); //maybe in future the entityReference should be TypedEntityReference.fromEntity(ntd.getTypeName(), false)
        Assert.assertEquals("fifth entry should be the name type authorship cache",
                new TaggedText(TagEnum.authors, "L."), taggedText.get(6));

        //protected titleCache
        ntd.getTypeName().setTitleCache("Prionus coriatius L.", true);
        taggedText = formatter.toTaggedText(manager);
        Assert.assertEquals("fourth entry should be the name type titleCache",
                new TaggedText(TagEnum.name, "Prionus coriatius L."), taggedText.get(4)); //maybe in future the entityReference should be TypedEntityReference.fromEntity(ntd.getTypeName(), false)
        Assert.assertEquals("there should be 5 tags only", 5, taggedText.size());
        ntd.getTypeName().setTitleCache(null, false);

        //with status
        ntd.getTypeName().addStatus(NomenclaturalStatus.NewInstance(NomenclaturalStatusType.ILLEGITIMATE()));
        formatter.withNameIfAvailable(false); //to simplify the evaluation
        text = formatter.format(manager);
        Assert.assertEquals("Nametype: Prionus coriatius L., nom. illeg.", text);

        taggedText = formatter.toTaggedText(manager);
        Assert.assertEquals("sixth entry should be the status separator",
                new TaggedText(TagEnum.separator, ", "), taggedText.get(5));
        Assert.assertEquals("seventh entry should be the abbreviated status",
                "nom. illeg.", taggedText.get(6).getText());
    }


    @Test
    public void testSpecimenLectotype() throws TypeDesignationSetException {

        //create data
        @SuppressWarnings("rawtypes")
        List<TypeDesignationBase> tdList = new ArrayList<>();
        tdList.add(std_LT);
        std_LT.setCitationMicroReference("22");

        TaxonName typifiedName = TaxonNameFactory.NewBotanicalInstance(Rank.SPECIES());
        typifiedName.setTitleCache("Prionus coriatius L.", true);

        typifiedName.addTypeDesignation(std_LT, false);

        //create formatter
        TypeDesignationSetContainer container = TypeDesignationSetContainer.NewDefaultInstance(tdList);
        TypeDesignationSetContainerFormatter formatter = new TypeDesignationSetContainerFormatter()
                .withCitation(true)
                .withStartingTypeLabel(true)
                .withNameIfAvailable(true)
                .withPrecedingMainType(false)
                .withAccessionNoType(false);

        //test text
        List<TaggedText> taggedText = formatter.toTaggedText(container);
        String text = TaggedTextFormatter.createString(taggedText);
        Assert.assertEquals("Prionus coriatius L."+DASH_W+"Type: Testland, near Bughausen, A.Kohlbecker 81989, 2017 (lectotype: LEC designated by Decandolle & al. 1962: 22)", text);


        int i = 9;  //start specimen
        Assert.assertEquals("entry "+(i+1)+" should be the designated by separator",
                " designated by ", taggedText.get(i++).getText());
        Assert.assertEquals("entry "+(i+1)+" should be the starting of specimen type designation",
                "Decandolle, A., Haber, M. & Moler, A.P. 1962: My interesting book",
                taggedText.get(i++).getEntityReference().getLabel());
    }

    @Test
    public void testSpecimenTypeDesignationTaggedTextWithStatus() throws TypeDesignationSetException {

        //create data
        @SuppressWarnings("rawtypes")
        List<TypeDesignationBase> tdList = new ArrayList<>();
        tdList.add(std_HT);

        TaxonName typifiedName = TaxonNameFactory.NewBotanicalInstance(Rank.SPECIES());
        typifiedName.setTitleCache("Prionus coriatius L.", true);

        typifiedName.addTypeDesignation(std_HT, false);
        Reference statusSource = ReferenceFactory.newBook(); //TODO not yet handled in cache strategy as we do not have tagged text here
        statusSource.setTitle("Status test");
        std_HT.getTypeSpecimen().addStatus(OccurrenceStatus.NewInstance(DefinedTerm.getTermByUuid(DefinedTerm.uuidDestroyed), statusSource, "335"));
        URI stableIdentifier = URI.create("http://stable.uri.de/xyz");
        DerivedUnit derivedUnit = std_HT.getTypeSpecimen();
        derivedUnit.setPreferredStableUri(stableIdentifier);

        //create formatter
        TypeDesignationSetContainer container = TypeDesignationSetContainer.NewDefaultInstance(tdList);
        TypeDesignationSetContainerFormatter formatter = new TypeDesignationSetContainerFormatter()
                .withCitation(true)
                .withStartingTypeLabel(true)
                .withNameIfAvailable(true)
                .withPrecedingMainType(false)
                .withAccessionNoType(true);

        //test text
        List<TaggedText> taggedText = formatter.toTaggedText(container);
        String text = TaggedTextFormatter.createString(taggedText);
        Assert.assertEquals("Prionus coriatius L."+DASH_W+"Type: Testland, near Bughausen, A.Kohlbecker 81989, 2017 (holotype: OHA accession no. OHA 1234, destroyed)", text);

        //test tags
//        //TODO the name should be split so it can be put in italics
        Assert.assertEquals("first entry should be the typified name",
                new TaggedText(TagEnum.name, "Prionus coriatius L.",TypedEntityReferenceFactory.fromEntity(typifiedName, false))
                , taggedText.get(0));
//        Assert.assertEquals("fourth entry should be the name type nameCache",
//                new TaggedText(TagEnum.name, "Prionus"), taggedText.get(3));  //maybe in future the entityReference should be TypedEntityReference.fromEntity(ntd.getTypeName(), false)
//        Assert.assertEquals("fourth entry should be the name type nameCache",
//                new TaggedText(TagEnum.name, "coriatius"), taggedText.get(4)); //maybe in future the entityReference should be TypedEntityReference.fromEntity(ntd.getTypeName(), false)
//        Assert.assertEquals("fifth entry should be the name type authorship cache",
//                new TaggedText(TagEnum.authors, "L."), taggedText.get(5));

        int i = 2;  //start specimen
        Assert.assertEquals("entry "+(i+1)+" should be the starting of specimen type designation",
                "Type", taggedText.get(i++).getText());
        Assert.assertEquals("entry "+(i+1)+" should be the separator",
                ": ", taggedText.get(i++).getText());
        Assert.assertEquals("entry "+(i+1)+" should be the field unit", //may get explicit field unit tag type in future
                TagEnum.specimenOrObservation, taggedText.get(i).getType());
        Assert.assertEquals("entry "+(i+1)+" should be the field unit", //may be split into more pieces in future
                "Testland, near Bughausen, A.Kohlbecker 81989, 2017", taggedText.get(i++).getText());
        i++;
        Assert.assertEquals("entry "+(i+1)+" should be the type status", //may be split into more pieces in future
                "holotype", taggedText.get(i++).getText());
        //TODO split collection and field number and specimen status (here 'destroyed') into their own tags
        i = 9; //unit number
        Assert.assertEquals("entry "+(i+1)+" should be the unit number with link",
                "OHA 1234", taggedText.get(i).getText());
        Assert.assertEquals("entry "+(i+1)+" should have a link for the stable identifier",
                TaggedTextWithLink.class, taggedText.get(i).getClass());
        TaggedTextWithLink ttwl = (TaggedTextWithLink)taggedText.get(i);
        Assert.assertEquals("entry "+(i+1)+" link should be the stable identifier",
                stableIdentifier, ttwl.getLink());

        //without unit number
        derivedUnit.setAccessionNumber(null);
        taggedText = formatter.toTaggedText(container);
        Assert.assertEquals("Prionus coriatius L."+DASH_W+"Type: Testland, near Bughausen, A.Kohlbecker 81989, 2017 (holotype: OHA, destroyed)", TaggedTextFormatter.createString(taggedText));
        i = 8; //collection
        Assert.assertEquals("entry "+(i+1)+" should be the collection with link",
                "OHA", taggedText.get(i).getText());
        Assert.assertEquals("entry "+(i+1)+" should have a link for the stable identifier on the collection because unit number is missing",
                TaggedTextWithLink.class, taggedText.get(i).getClass());
        ttwl = (TaggedTextWithLink)taggedText.get(i);
        Assert.assertEquals("entry "+(i+1)+" link should be the stable identifier",
                stableIdentifier, ttwl.getLink());

        //without collection
        derivedUnit.setCollection(null);
        taggedText = formatter.toTaggedText(container);
        Assert.assertEquals("Prionus coriatius L."+DASH_W+"Type: Testland, near Bughausen, A.Kohlbecker 81989, 2017 (holotype: destroyed)", TaggedTextFormatter.createString(taggedText));
        i = 8; //destroyed
        Assert.assertEquals("entry "+(i+1)+" should be the specimen status with link",
                "destroyed", taggedText.get(i).getText());
        Assert.assertEquals("entry "+(i+1)+" should have a link for the stable identifier on the specimen status because unit number and collection are missing",
                TaggedTextWithLink.class, taggedText.get(i).getClass());
        ttwl = (TaggedTextWithLink)taggedText.get(i);
        Assert.assertEquals("entry "+(i+1)+" link should be the stable identifier",
                stableIdentifier, ttwl.getLink());

        //no link
        derivedUnit.setPreferredStableUri(null);
        taggedText = formatter.toTaggedText(container);
        Assert.assertEquals("Prionus coriatius L."+DASH_W+"Type: Testland, near Bughausen, A.Kohlbecker 81989, 2017 (holotype: destroyed)", TaggedTextFormatter.createString(taggedText));
        i = 8; //destroyed
        Assert.assertEquals("entry "+(i+1)+" should be the specimen status with link",
                "destroyed", taggedText.get(i).getText());
        Assert.assertEquals("entry "+(i+1)+" should have no link for the stable identifier",
                TaggedText.class, taggedText.get(i).getClass());

//        //protected titleCache
//        ntd.getTypeName().setTitleCache("Prionus coriatius L.", true);
//        taggedText = formatter.toTaggedText(container);
//        Assert.assertEquals("fourth entry should be the name type titleCache",
//                new TaggedText(TagEnum.name, "Prionus coriatius L."), taggedText.get(3)); //maybe in future the entityReference should be TypedEntityReference.fromEntity(ntd.getTypeName(), false)
//        Assert.assertEquals("there should be 4 tags only", 4, taggedText.size());
    }

    //#10089
    @Test
    public void testOrderedByStatusNotBaseEntity() throws TypeDesignationSetException {

        @SuppressWarnings("rawtypes")
        List<TypeDesignationBase> tds = new ArrayList<>();
        tds.add(std_HT);
        tds.add(std_IT_3);

        TaxonName typifiedName = TaxonNameFactory.NewBotanicalInstance(Rank.SPECIES());
        typifiedName.setTitleCache("Prionus coriatius L.", true);

        typifiedName.addTypeDesignation(std_HT, false);
        typifiedName.addTypeDesignation(std_IT_3, false);

        TypeDesignationSetContainer container = TypeDesignationSetContainer.NewDefaultInstance(tds);
        TypeDesignationSetContainerFormatter formatter = new TypeDesignationSetContainerFormatter(false, false, false, false, true);
        String text = formatter.format(container);
        int holotypeIndex = text.indexOf("holotype");
        Assert.assertTrue("Holotype must be first, isotype second", holotypeIndex>0 && (holotypeIndex < text.indexOf("isotype")) );
    }

    @Test
    public void testTextualTypeDesignation() throws TypeDesignationSetException {
        List<TypeDesignationBase> tdList = new ArrayList<>();
        TextualTypeDesignation ttd = TextualTypeDesignation.NewInstance("My text type designation",
                null, false, book, DASH_W, DASH_W);
        TextualTypeDesignation ttd2 = TextualTypeDesignation.NewInstance("My second type designation",
                null, false, book, DASH_W, DASH_W);
        ttd2.setVerbatim(true);
        ttd2.addPrimaryTaxonomicSource(book, "55");

        tdList.add(ttd);
        tdList.add(ttd2);

        //add to name
        TaxonName typifiedName = TaxonNameFactory.NewBotanicalInstance(Rank.SPECIES());
        typifiedName.setTitleCache("Prionus coriatius L.", true);
        typifiedName.addTypeDesignation(ttd, false);
        typifiedName.addTypeDesignation(ttd2, false);

        TypeDesignationSetContainer container = TypeDesignationSetContainer.NewDefaultInstance(tdList);
        TypeDesignationSetContainerFormatter formatter = new TypeDesignationSetContainerFormatter();
        String text = formatter.format(container);
        Assert.assertEquals("Types: \"My second type designation\" [fide Decandolle & al. 1962: 55]; My text type designation", text);

        List<TaggedText> tags = formatter.toTaggedText(container);
        Assert.assertEquals(8, tags.size());
        int i = 0;
        Assert.assertEquals(TagEnum.label, tags.get(i).getType());
        Assert.assertEquals("Types", tags.get(i++).getText());
        Assert.assertEquals(TagEnum.postSeparator, tags.get(i).getType());
        Assert.assertEquals(": ", tags.get(i++).getText());
        Assert.assertEquals(TagEnum.typeDesignation, tags.get(i).getType());
        Assert.assertEquals("\"My second type designation\"", tags.get(i++).getText());
        Assert.assertEquals(TagEnum.separator, tags.get(i).getType());
        Assert.assertEquals(" [fide ", tags.get(i++).getText());
        Assert.assertEquals(TagEnum.reference, tags.get(i).getType());
        Assert.assertEquals(Reference.class, tags.get(i).getEntityReference().getType());
        Assert.assertEquals("Decandolle & al. 1962: 55", tags.get(i++).getText());
        Assert.assertEquals(TagEnum.separator, tags.get(i).getType());
        Assert.assertEquals("]", tags.get(i++).getText());
        Assert.assertEquals(TagEnum.separator, tags.get(i).getType());
        Assert.assertEquals("; ", tags.get(i++).getText());
        Assert.assertEquals(TagEnum.typeDesignation, tags.get(i).getType());
        Assert.assertEquals("My text type designation", tags.get(i++).getText());
    }

    @Test
    public void testNotDesignated() throws TypeDesignationSetException {

        //create data
        @SuppressWarnings("rawtypes")
        List<TypeDesignationBase> tdList = new ArrayList<>();
        SpecimenTypeDesignation std = SpecimenTypeDesignation.NewInstance();
        std.setNotDesignated(true);
        std.addPrimaryTaxonomicSource(book, "66");

        NameTypeDesignation ntd = NameTypeDesignation.NewInstance();
        ntd.setNotDesignated(true);
        ntd.addPrimaryTaxonomicSource(book, "55");

        tdList.add(std);
        tdList.add(ntd);

        TaxonName typifiedName = TaxonNameFactory.NewBotanicalInstance(Rank.SPECIES());
        typifiedName.setTitleCache("Prionus coriatius L.", true);

        typifiedName.addTypeDesignation(std, false);
        typifiedName.addTypeDesignation(ntd, false);

        //test
        TypeDesignationSetContainer container = TypeDesignationSetContainer.NewDefaultInstance(tdList);
        TypeDesignationSetContainerFormatter formatter = new TypeDesignationSetContainerFormatter()
                .withCitation(false);
        String text = formatter.format(container);
        //Type*s* might become Type in future (not defined yet as this is a very rare or even unrealistic combination of specimen and name types, see according comment in formatter
        Assert.assertEquals("Types: not designated; Nametype: not designated", text);

        List<TaggedText> tags = formatter.toTaggedText(container);
        Assert.assertEquals(7, tags.size());
        int i = 0;
        Assert.assertEquals(TagEnum.label, tags.get(i).getType());
        Assert.assertEquals("Types", tags.get(i++).getText());
        Assert.assertEquals(TagEnum.postSeparator, tags.get(i).getType());
        Assert.assertEquals(": ", tags.get(i++).getText());
        Assert.assertEquals(TagEnum.typeDesignation, tags.get(i).getType());
        Assert.assertEquals("not designated", tags.get(i++).getText());
        Assert.assertEquals(TagEnum.separator, tags.get(i).getType());
        Assert.assertEquals("; ", tags.get(i++).getText());
        Assert.assertEquals(TagEnum.label, tags.get(i).getType());
        //TODO maybe not correct and should be nametype, not Nametype
        Assert.assertEquals("Nametype", tags.get(i++).getText());
        Assert.assertEquals(TagEnum.postSeparator, tags.get(i).getType());
        Assert.assertEquals(": ", tags.get(i++).getText());
        Assert.assertEquals(TagEnum.typeDesignation, tags.get(i).getType());
        Assert.assertEquals("not designated", tags.get(i++).getText());

        //with citation
        formatter = new TypeDesignationSetContainerFormatter().withCitation(true);
        text = formatter.format(container);

        Assert.assertEquals("Types: not designated [fide Decandolle & al. 1962: 66]; Nametype: not designated [fide Decandolle & al. 1962: 55]", text);
        tags = formatter.toTaggedText(container);
        Assert.assertEquals(13, tags.size());
        i = 0;
        Assert.assertEquals(TagEnum.label, tags.get(i).getType());
        Assert.assertEquals("Types", tags.get(i++).getText());
        Assert.assertEquals(TagEnum.postSeparator, tags.get(i).getType());
        Assert.assertEquals(": ", tags.get(i++).getText());
        Assert.assertEquals(TagEnum.typeDesignation, tags.get(i).getType());
        Assert.assertEquals("not designated", tags.get(i++).getText());
        Assert.assertEquals(TagEnum.separator, tags.get(i).getType());
        Assert.assertEquals(" [fide ", tags.get(i++).getText());
        Assert.assertEquals(TagEnum.reference, tags.get(i).getType());
        Assert.assertEquals(Reference.class, tags.get(i).getEntityReference().getType());
        Assert.assertEquals("Decandolle & al. 1962: 66", tags.get(i++).getText());
        Assert.assertEquals(TagEnum.separator, tags.get(i).getType());
        Assert.assertEquals("]", tags.get(i++).getText());
        Assert.assertEquals(TagEnum.separator, tags.get(i).getType());
        Assert.assertEquals("; ", tags.get(i++).getText());
        Assert.assertEquals(TagEnum.label, tags.get(i).getType());
        //TODO maybe not correct and should be nametype, not Nametype
        Assert.assertEquals("Nametype", tags.get(i++).getText());
        Assert.assertEquals(TagEnum.postSeparator, tags.get(i).getType());
        Assert.assertEquals(": ", tags.get(i++).getText());
        Assert.assertEquals(TagEnum.typeDesignation, tags.get(i).getType());
        Assert.assertEquals("not designated", tags.get(i++).getText());
        Assert.assertEquals(TagEnum.separator, tags.get(i).getType());
        Assert.assertEquals(" [fide ", tags.get(i++).getText());
        Assert.assertEquals(TagEnum.reference, tags.get(i).getType());
        Assert.assertEquals(Reference.class, tags.get(i).getEntityReference().getType());
        Assert.assertEquals("Decandolle & al. 1962: 55", tags.get(i++).getText());
        Assert.assertEquals(TagEnum.separator, tags.get(i).getType());
        Assert.assertEquals("]", tags.get(i++).getText());;
    }
}