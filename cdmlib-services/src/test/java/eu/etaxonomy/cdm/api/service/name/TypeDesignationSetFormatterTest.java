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

import eu.etaxonomy.cdm.api.service.exception.RegistrationValidationException;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.common.IdentifiableSource;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.name.NameTypeDesignation;
import eu.etaxonomy.cdm.model.name.NameTypeDesignationStatus;
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
import eu.etaxonomy.cdm.model.occurrence.MediaSpecimen;
import eu.etaxonomy.cdm.model.occurrence.OccurrenceStatus;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationType;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.model.term.DefinedTerm;
import eu.etaxonomy.cdm.ref.TypedEntityReference;
import eu.etaxonomy.cdm.strategy.cache.TagEnum;
import eu.etaxonomy.cdm.strategy.cache.TaggedText;
import eu.etaxonomy.cdm.strategy.parser.TimePeriodParser;
import eu.etaxonomy.cdm.test.TermTestBase;

/**
 * @author a.mueller
 * @since 30.03.2021
 */
public class TypeDesignationSetFormatterTest extends TermTestBase{

    //variables and setup were copied from TypeDesignationSetManagerTest
    //not all of them are in use yet
    private NameTypeDesignation ntd;
    private NameTypeDesignation ntd_LT;
    private SpecimenTypeDesignation std_IT;
    private SpecimenTypeDesignation std_HT;
    private SpecimenTypeDesignation std_LT;
    private SpecimenTypeDesignation std_IT_2;
    private SpecimenTypeDesignation std_IT_3;
    private SpecimenTypeDesignation mtd_HT_published;
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

        mtd_HT_published = SpecimenTypeDesignation.NewInstance();
        mtd_HT_published.setId(5);
        MediaSpecimen mediaSpecimen_published = (MediaSpecimen)DerivedUnit.NewInstance(SpecimenOrObservationType.Media);
        Media media = Media.NewInstance();
        Reference ref = ReferenceFactory.newGeneric();
        ref.setTitleCache("A.K. & W.K (2008) Algae of the BGBM", true);
        media.addSource(IdentifiableSource.NewPrimaryMediaSourceInstance(ref, "p.33"));
        mediaSpecimen_published.setMediaSpecimen(media);
        createDerivationEvent(fu_1, mediaSpecimen_published);
        mtd_HT_published.setTypeSpecimen(mediaSpecimen_published);
        mtd_HT_published.setTypeStatus(SpecimenTypeDesignationStatus.HOLOTYPE());

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
    public void testNameTypeDesignationTaggedText() throws RegistrationValidationException {

        @SuppressWarnings("rawtypes")
        List<TypeDesignationBase> tds = new ArrayList<>();
        tds.add(ntd);

        TaxonName typifiedName = TaxonNameFactory.NewBotanicalInstance(Rank.GENUS());
        typifiedName.setTitleCache("Prionus L.", true);

        typifiedName.addTypeDesignation(ntd, false);

        TypeDesignationSetManager manager = new TypeDesignationSetManager(tds);
        TypeDesignationSetFormatter formatter = new TypeDesignationSetFormatter(true, true, true);
        String text = formatter.format(manager);
        Assert.assertEquals("Prionus L.\u202F\u2013\u202FNametype: Prionus coriatius L.", text);

        List<TaggedText> taggedText = formatter.toTaggedText(manager);
        Assert.assertEquals("first entry should be the typified name",
                new TaggedText(TagEnum.name, "Prionus L.",TypedEntityReference.fromEntity(typifiedName, false)), taggedText.get(0));
        Assert.assertEquals("fourth entry should be the name type nameCache",
                new TaggedText(TagEnum.name, "Prionus"), taggedText.get(3));  //maybe in future the entityReference should be TypedEntityReference.fromEntity(ntd.getTypeName(), false)
        Assert.assertEquals("fourth entry should be the name type nameCache",
                new TaggedText(TagEnum.name, "coriatius"), taggedText.get(4)); //maybe in future the entityReference should be TypedEntityReference.fromEntity(ntd.getTypeName(), false)
        Assert.assertEquals("fifth entry should be the name type authorship cache",
                new TaggedText(TagEnum.authors, "L."), taggedText.get(5));

        //protected titleCache
        ntd.getTypeName().setTitleCache("Prionus coriatius L.", true);
        taggedText = formatter.toTaggedText(manager);
        Assert.assertEquals("fourth entry should be the name type titleCache",
                new TaggedText(TagEnum.name, "Prionus coriatius L."), taggedText.get(3)); //maybe in future the entityReference should be TypedEntityReference.fromEntity(ntd.getTypeName(), false)
        Assert.assertEquals("there should be 4 tags only", 4, taggedText.size());
    }

    @Test
    public void testSpecimenTypeDesignationTaggedTextWithStatus() throws RegistrationValidationException {

        @SuppressWarnings("rawtypes")
        List<TypeDesignationBase> tds = new ArrayList<>();
        tds.add(std_HT);

        TaxonName typifiedName = TaxonNameFactory.NewBotanicalInstance(Rank.SPECIES());
        typifiedName.setTitleCache("Prionus coriatius L.", true);

        typifiedName.addTypeDesignation(std_HT, false);
        Reference statusSource = ReferenceFactory.newBook(); //TODO not yet handled in cache strategy as we do not have tagged text here
        statusSource.setTitle("Status test");
        std_HT.getTypeSpecimen().addStatus(OccurrenceStatus.NewInstance(DefinedTerm.getTermByUuid(DefinedTerm.uuidDestroyed), statusSource, "335"));

        TypeDesignationSetManager manager = new TypeDesignationSetManager(tds);
        TypeDesignationSetFormatter formatter = new TypeDesignationSetFormatter(true, true, true);

        String text = formatter.format(manager);
        Assert.assertEquals("Prionus coriatius L.\u202F\u2013\u202FType: Testland, near Bughausen, A.Kohlbecker 81989, 2017 (holotype: OHA 1234, destroyed)", text);

        List<TaggedText> taggedText = formatter.toTaggedText(manager);
        Assert.assertEquals("first entry should be the typified name",
                new TaggedText(TagEnum.name, "Prionus coriatius L.",TypedEntityReference.fromEntity(typifiedName, false)), taggedText.get(0));
//        Assert.assertEquals("fourth entry should be the name type nameCache",
//                new TaggedText(TagEnum.name, "Prionus"), taggedText.get(3));  //maybe in future the entityReference should be TypedEntityReference.fromEntity(ntd.getTypeName(), false)
//        Assert.assertEquals("fourth entry should be the name type nameCache",
//                new TaggedText(TagEnum.name, "coriatius"), taggedText.get(4)); //maybe in future the entityReference should be TypedEntityReference.fromEntity(ntd.getTypeName(), false)
//        Assert.assertEquals("fifth entry should be the name type authorship cache",
//                new TaggedText(TagEnum.authors, "L."), taggedText.get(5));
//
//        //protected titleCache
//        ntd.getTypeName().setTitleCache("Prionus coriatius L.", true);
//        taggedText = formatter.toTaggedText(manager);
//        Assert.assertEquals("fourth entry should be the name type titleCache",
//                new TaggedText(TagEnum.name, "Prionus coriatius L."), taggedText.get(3)); //maybe in future the entityReference should be TypedEntityReference.fromEntity(ntd.getTypeName(), false)
//        Assert.assertEquals("there should be 4 tags only", 4, taggedText.size());
    }
}