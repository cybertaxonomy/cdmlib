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
import eu.etaxonomy.cdm.common.UTF8;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.common.IdentifiableSource;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.name.NameTypeDesignation;
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
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationType;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.strategy.cache.TagEnum;
import eu.etaxonomy.cdm.strategy.cache.TaggedText;
import eu.etaxonomy.cdm.strategy.parser.TimePeriodParser;
import eu.etaxonomy.cdm.test.TermTestBase;

/**
 * See also {@link TypeDesignationGroupContainerTest} for further tests.
 *
 * @author a.mueller
 * @since 30.03.2021
 */
public class TypeDesignationGroupContainerFormatterTest extends TermTestBase{

    private static final String DASH_W = UTF8.EN_DASH_SPATIUM.toString();

    private static final boolean WITH_CITATION = true;
    private static final boolean WITH_NAME = true;
    private static final boolean WITH_TYPE_LABEL = true;
    private static final boolean WITH_PRECEDING_MAIN_TYPE = true;
    private static final boolean WITH_ACCESSION_NO_TYPE = true;
    private static final List<Language> languages = null;

    //variables and setup were copied from TypeDesignationGroupContainerTest
    //not all of them are in use yet
    private SpecimenTypeDesignation std_IT;
    private SpecimenTypeDesignation std_HT;
    private SpecimenTypeDesignation std_LT;
    private SpecimenTypeDesignation std_IT_2;
    private SpecimenTypeDesignation std_IT_3;
    private SpecimenTypeDesignation mstd_HT_published;
    private SpecimenTypeDesignation mtd_IT_unpublished;
    private Reference book;

    @Before
    public void setUp() throws Exception {
        Person person1 = Person.NewInstance("DC", "Decandolle", "A.", null);
        Person person2 = Person.NewInstance("Hab.", "Haber", "M.", null);
        Person person3 = Person.NewInstance("Moler", "Moler", "A.P.", null);
        Team team = Team.NewInstance(person1, person2, person3);

        book = ReferenceFactory.newBook();
        book.setAuthorship(team);
        book.setTitle("My interesting book");
        book.setDatePublished(TimePeriodParser.parseStringVerbatim("11 Apr 1962"));

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

    private void createDerivationEvent(FieldUnit fu_1, DerivedUnit specimen_IT_2) {
        DerivationEvent derivationEvent_3 = DerivationEvent.NewInstance(DerivationEventType.ACCESSIONING());
        derivationEvent_3.addOriginal(fu_1);
        derivationEvent_3.addDerivative(specimen_IT_2);
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

        TypeDesignationGroupContainer container = TypeDesignationGroupContainer.NewDefaultInstance(tdList);
        TypeDesignationGroupContainerFormatter formatter = new TypeDesignationGroupContainerFormatter();
        String text = formatter.format(container);
        Assert.assertEquals("Type: \"My second type designation\" [fide Decandolle & al. 1962: 55]; My text type designation", text);

        List<TaggedText> tags = formatter.toTaggedText(container);
        Assert.assertEquals(8, tags.size());
        int i = 0;
        Assert.assertEquals(TagEnum.label, tags.get(i).getType());
        Assert.assertEquals("Type", tags.get(i++).getText());
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
        TypeDesignationGroupContainer container = TypeDesignationGroupContainer.NewDefaultInstance(tdList);
        TypeDesignationGroupContainerFormatter formatter = new TypeDesignationGroupContainerFormatter()
                .withCitation(false);
        String text = formatter.format(container);
        //Type*s* might become Type in future (not defined yet as this is a very rare or even unrealistic combination of specimen and name types, see according comment in formatter
        Assert.assertEquals("Type: not designated; Type: not designated", text);

        List<TaggedText> tags = formatter.toTaggedText(container);
        Assert.assertEquals(7, tags.size());
        int i = 0;
        Assert.assertEquals(TagEnum.label, tags.get(i).getType());
        Assert.assertEquals("Type", tags.get(i++).getText());
        Assert.assertEquals(TagEnum.postSeparator, tags.get(i).getType());
        Assert.assertEquals(": ", tags.get(i++).getText());
        Assert.assertEquals(TagEnum.typeDesignation, tags.get(i).getType());
        Assert.assertEquals("not designated", tags.get(i++).getText());
        Assert.assertEquals(TagEnum.separator, tags.get(i).getType());
        Assert.assertEquals("; ", tags.get(i++).getText());
        Assert.assertEquals(TagEnum.label, tags.get(i).getType());
        //TODO maybe not correct and should be type, not Type
        Assert.assertEquals("Type", tags.get(i++).getText());
        Assert.assertEquals(TagEnum.postSeparator, tags.get(i).getType());
        Assert.assertEquals(": ", tags.get(i++).getText());
        Assert.assertEquals(TagEnum.typeDesignation, tags.get(i).getType());
        Assert.assertEquals("not designated", tags.get(i++).getText());

        //with citation
        formatter = new TypeDesignationGroupContainerFormatter().withCitation(true);
        text = formatter.format(container);

        //Note: having 2x 'not designated' does not make sense in reality
        //... or should be deduplicated as there are 2 sources
        Assert.assertEquals("Type: not designated [fide Decandolle & al. 1962: 66]; Type: not designated [fide Decandolle & al. 1962: 55]", text);
        tags = formatter.toTaggedText(container);
        Assert.assertEquals(13, tags.size());
        i = 0;
        Assert.assertEquals(TagEnum.label, tags.get(i).getType());
        Assert.assertEquals("Type", tags.get(i++).getText());
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
        //TODO maybe not correct and should be type, not Type
        Assert.assertEquals("Type", tags.get(i++).getText());
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

        TypeDesignationGroupContainer container = TypeDesignationGroupContainer.NewDefaultInstance(tds);
        TypeDesignationGroupContainerFormatter formatter = new TypeDesignationGroupContainerFormatter(
                !WITH_CITATION, !WITH_TYPE_LABEL, !WITH_NAME, !WITH_PRECEDING_MAIN_TYPE, WITH_ACCESSION_NO_TYPE, languages);
        String text = formatter.format(container);
        int holotypeIndex = text.indexOf("holotype");
        Assert.assertTrue("Holotype must be first, isotype second", holotypeIndex>0 && (holotypeIndex < text.indexOf("isotype")) );
    }
}