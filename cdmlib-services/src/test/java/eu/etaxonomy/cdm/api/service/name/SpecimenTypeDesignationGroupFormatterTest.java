/**
* Copyright (C) 2024 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.name;

import static org.junit.Assert.assertEquals;

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
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.SpecimenDescription;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.location.Country;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignation;
import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignationStatus;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.name.TaxonNameFactory;
import eu.etaxonomy.cdm.model.name.TypeDesignationBase;
import eu.etaxonomy.cdm.model.occurrence.Collection;
import eu.etaxonomy.cdm.model.occurrence.DerivationEvent;
import eu.etaxonomy.cdm.model.occurrence.DerivationEventType;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnit;
import eu.etaxonomy.cdm.model.occurrence.FieldUnit;
import eu.etaxonomy.cdm.model.occurrence.GatheringEvent;
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
 * @author muellera
 * @since 27.06.2024
 */
public class SpecimenTypeDesignationGroupFormatterTest extends TermTestBase {

    private static final String DASH_W = UTF8.EN_DASH_SPATIUM.toString();

    private static final boolean WITH_CITATION = true;
    private static final boolean WITH_NAME = true;
    private static final boolean WITH_TYPE_LABEL = true;
    private static final boolean WITH_PRECEDING_MAIN_TYPE = true;
    private static final boolean WITH_ACCESSION_NO_TYPE = true;

    //variables and setup were copied from TypeDesignationGroupContainerTest
    //not all of them are in use yet
    private SpecimenTypeDesignation std_IT;
    private SpecimenTypeDesignation std_HT;
    private SpecimenTypeDesignation std_LT;
    private SpecimenTypeDesignation std_IT_2;
    private SpecimenTypeDesignation std_IT_3;
    private SpecimenTypeDesignation mstd_HT_published;
    private SpecimenTypeDesignation mtd_IT_unpublished;
    private FieldUnit fu_1;
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

        TaxonName typeName1 = TaxonNameFactory.NewBotanicalInstance(Rank.SPECIES());

        typeName1.setTitleCache("Prionus arealus L.", true);

        fu_1 = FieldUnit.NewInstance();
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

    private void createDerivationEvent(FieldUnit fu_1, DerivedUnit specimen_IT_2) {
        DerivationEvent derivationEvent_3 = DerivationEvent.NewInstance(DerivationEventType.ACCESSIONING());
        derivationEvent_3.addOriginal(fu_1);
        derivationEvent_3.addDerivative(specimen_IT_2);
    }


    @Test
    public void testNullTypeDesignation() {

        TypeDesignationGroupContainerFormatter formatter
            = new TypeDesignationGroupContainerFormatter();

        //protected cache specimen
        String sobTitleCache = "[Poland] 'in hortis gramineis in Gnichwitz'";
        DerivedUnit du = DerivedUnit.NewPreservedSpecimenInstance();
        du.setTitleCache(sobTitleCache, true);

        //name
        TaxonName typifiedName = TaxonNameFactory.NewBotanicalInstance(Rank.SPECIES());
        typifiedName.setTitleCache("Prionus coriatius L.", true);

        //td
        SpecimenTypeDesignation std = SpecimenTypeDesignation.NewInstance();
        std.setTypeSpecimen(du);
        typifiedName.addTypeDesignation(std, false);

        //test
        TypeDesignationGroupContainer container = new TypeDesignationGroupContainer(typifiedName.getHomotypicalGroup());
        String text = formatter.format(container);
        Assert.assertEquals("Type: [Poland] 'in hortis gramineis in Gnichwitz'", text);

        //with "not designated"
        std.setTypeSpecimen(null);
        std.setNotDesignated(true);

        //test
        container = new TypeDesignationGroupContainer(typifiedName.getHomotypicalGroup());
        text = formatter.format(container);
        Assert.assertEquals("Type: not designated", text);
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

        //create formatter ... without preceding main type
        TypeDesignationGroupContainer container = TypeDesignationGroupContainer.NewDefaultInstance(tdList);
        TypeDesignationGroupContainerFormatter formatter = new TypeDesignationGroupContainerFormatter()
                .withCitation(WITH_CITATION)
                .withStartingTypeLabel(WITH_TYPE_LABEL)
                .withNameIfAvailable(WITH_NAME)
                .withPrecedingMainType(!WITH_PRECEDING_MAIN_TYPE)
                .withAccessionNoType(!WITH_ACCESSION_NO_TYPE);

        //test text
        List<TaggedText> taggedText = formatter.toTaggedText(container);
        String text = TaggedTextFormatter.createString(taggedText);
        Assert.assertEquals("Prionus coriatius L."+DASH_W+"Type: Testland, near Bughausen, A.Kohlbecker 81989, 2017 (lectotype (designated by Decandolle & al. 1962: 22): LEC)", text);


        int i = 7;  //start specimen
        Assert.assertEquals("entry "+(i+1)+" should be the designated by separator",
                " (designated by ", taggedText.get(i++).getText());
        Assert.assertEquals("entry "+(i+1)+" should be the starting of specimen type designation",
                "Decandolle, A., Haber, M. & Moler, A.P. 1962: My interesting book",
                taggedText.get(i++).getEntityReference().getLabel());

        //... with preceding main type
        formatter.withPrecedingMainType(true);
        taggedText = formatter.toTaggedText(container);
        text = TaggedTextFormatter.createString(taggedText);
        Assert.assertEquals("Prionus coriatius L."+DASH_W+"Lectotype (designated by Decandolle & al. 1962: 22): Testland, near Bughausen, A.Kohlbecker 81989, 2017 (LEC)", text);
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
        TypeDesignationGroupContainer container = TypeDesignationGroupContainer.NewDefaultInstance(tdList);
        TypeDesignationGroupContainerFormatter formatter = new TypeDesignationGroupContainerFormatter()
                .withCitation(WITH_CITATION)
                .withStartingTypeLabel(WITH_TYPE_LABEL)
                .withNameIfAvailable(WITH_NAME)
                .withPrecedingMainType(!WITH_PRECEDING_MAIN_TYPE)
                .withAccessionNoType(WITH_ACCESSION_NO_TYPE);

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

    //see #9262, see also similar test in NameTypeDesignationGroupFormatterTest
    @Test
    public void test_desigby_fide(){

        //specimen types
        TaxonName typifiedName = TaxonNameFactory.NewBacterialInstance(Rank.SPECIES());
        typifiedName.setTitleCache("Prionus coriatius L.", true);

        Reference citation = ReferenceFactory.newBook();
        Reference inRef = ReferenceFactory.newBookSection();
        inRef.setInBook(citation);
        citation.setDatePublished(TimePeriodParser.parseStringVerbatim("1989"));
        inRef.setAuthorship(Team.NewTitledInstance("Miller", "Mill."));
        std_LT.addPrimaryTaxonomicSource(inRef, "55");

        //container
        TypeDesignationGroupContainer typeDesignationContainer = new TypeDesignationGroupContainer(typifiedName);
        typeDesignationContainer.addTypeDesigations(std_LT);

        //test
        assertEquals("Prionus coriatius L."+DASH_W+"Testland, near Bughausen, A.Kohlbecker 81989, 2017 (lectotype (designated by Decandolle & al. 1962): LEC [fide Miller 1989: 55])",
                typeDesignationContainer.print(WITH_CITATION, !WITH_TYPE_LABEL, WITH_NAME, !WITH_PRECEDING_MAIN_TYPE, !WITH_ACCESSION_NO_TYPE));
        assertEquals("Prionus coriatius L."+DASH_W+"Testland, near Bughausen, A.Kohlbecker 81989, 2017 (lectotype: LEC)",
                typeDesignationContainer.print(!WITH_CITATION,!WITH_TYPE_LABEL, WITH_NAME, !WITH_PRECEDING_MAIN_TYPE, !WITH_ACCESSION_NO_TYPE));
        assertEquals("Testland, near Bughausen, A.Kohlbecker 81989, 2017 (lectotype: LEC)",
                typeDesignationContainer.print(!WITH_CITATION, !WITH_TYPE_LABEL, !WITH_NAME, !WITH_PRECEDING_MAIN_TYPE, !WITH_ACCESSION_NO_TYPE));
    }

    @Test
    public void test_plant_description(){
        TaxonName typifiedName = TaxonNameFactory.NewBacterialInstance(Rank.SPECIES());
        typifiedName.setTitleCache("Prionus coriatius L.", true);

        fu_1.setProtectedTitleCache(false);
        GatheringEvent ge = GatheringEvent.NewInstance();
        ge.setCountry(Country.GERMANY());
        ge.putLocality(Language.DEFAULT(), "near Bughausen");
        ge.setTimeperiod(TimePeriodParser.parseString("1972"));
        fu_1.setGatheringEvent(ge);
        fu_1.setFieldNumber("FN123");
        ge.setActor(Person.NewTitledInstance("A. Collector"));

        SpecimenDescription description = SpecimenDescription.NewInstance(fu_1);
        description.addElement(TextData.NewInstance(Feature.DESCRIPTION(), "My plant description", Language.DEFAULT(), null));
        description.addElement(TextData.NewInstance(Feature.ECOLOGY(), "My ecology", Language.DEFAULT(), null));

        //container
//        SpecimenTypeDesignationGroupFormatter formatter = SpecimenTypeDesignationGroupFormatter.INSTANCE();
        TypeDesignationGroupContainer typeDesignationContainer = new TypeDesignationGroupContainer(typifiedName);
        typeDesignationContainer.addTypeDesigations(std_LT);


        //test
        assertEquals("Lectotype: Germany, near Bughausen, My ecology, My plant description, 1972, A. Collector FN123 (LEC)",
                typeDesignationContainer.print(!WITH_CITATION, !WITH_TYPE_LABEL, !WITH_NAME, WITH_PRECEDING_MAIN_TYPE, !WITH_ACCESSION_NO_TYPE));
    }
}