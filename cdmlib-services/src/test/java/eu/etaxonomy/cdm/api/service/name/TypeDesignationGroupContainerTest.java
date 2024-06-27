/**
* Copyright (C) 2018 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.name;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import eu.etaxonomy.cdm.api.service.exception.TypeDesignationSetException;
import eu.etaxonomy.cdm.common.UTF8;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.common.IdentifiableSource;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.VerbatimTimePeriod;
import eu.etaxonomy.cdm.model.common.VersionableEntity;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.name.NameTypeDesignation;
import eu.etaxonomy.cdm.model.name.NameTypeDesignationStatus;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignation;
import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignationStatus;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.name.TaxonNameFactory;
import eu.etaxonomy.cdm.model.name.TypeDesignationBase;
import eu.etaxonomy.cdm.model.name.TypeDesignationStatusBase;
import eu.etaxonomy.cdm.model.occurrence.Collection;
import eu.etaxonomy.cdm.model.occurrence.DerivationEvent;
import eu.etaxonomy.cdm.model.occurrence.DerivationEventType;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnit;
import eu.etaxonomy.cdm.model.occurrence.FieldUnit;
import eu.etaxonomy.cdm.model.occurrence.MediaSpecimen;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationType;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.strategy.parser.TimePeriodParser;
import eu.etaxonomy.cdm.test.TermTestBase;

/**
 * @author a.kohlbecker, k.luther, a.mueller
 * @since 03.09.2018
 */
public class TypeDesignationGroupContainerTest extends TermTestBase{

    private static final String DASH_W = UTF8.EN_DASH_SPATIUM.toString();

    private static final boolean WITH_CITATION = true;
    private static final boolean WITH_NAME = true;
    private static final boolean WITH_TYPE_LABEL = true;
    private static final boolean WITH_PRECEDING_MAIN_TYPE = true;
    private static final boolean WITH_ACCESSION_NO_TYPE = true;


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
    public void init(){

        Person person1 = Person.NewInstance("DC", "Decandolle", "A.", null);
        Person person2 = Person.NewInstance("Hab.", "Haber", "M.", null);
        Person person3 = Person.NewInstance("Moler", "Moler", "A.P.", null);
        team = Team.NewInstance(person1, person2, person3);

        book = ReferenceFactory.newBook();
        book.setAuthorship(team);
        book.setDatePublished(TimePeriodParser.parseStringVerbatim("11 Apr 1962"));

        ntd = NameTypeDesignation.NewInstance();
        ntd.setId(1);
        TaxonName typeName = TaxonNameFactory.NewBacterialInstance(Rank.SPECIES());
        typeName.setTitleCache("Prionus coriatius L.", true);
        ntd.setTypeName(typeName);
//            Reference citation = ReferenceFactory.newGeneric();
//            citation.setTitleCache("Species Plantarum", true);
//            ntd.setCitation(citation);
//          ntd.addPrimaryTaxonomicSource(citation, null);

        ntd_LT = NameTypeDesignation.NewInstance();
        ntd_LT.setTypeStatus(NameTypeDesignationStatus.LECTOTYPE());
        TaxonName typeName2 = TaxonNameFactory.NewBacterialInstance(Rank.SPECIES());
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
        specimen_HT.setTitleCache("OHA", true);
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
        std_IT_3.setTypeStatus(SpecimenTypeDesignationStatus.SYNTYPE());

        mtd_HT_published = SpecimenTypeDesignation.NewInstance();
        mtd_HT_published.setId(5);
        MediaSpecimen mediaSpecimen_published = (MediaSpecimen)DerivedUnit.NewInstance(SpecimenOrObservationType.Media);
        Media media = Media.NewInstance();
        media.putTitle(Language.DEFAULT(), "A nice picture");
        Reference ref = ReferenceFactory.newBook();
        ref.setTitle("Algae of the BGBM");
        ref.setDatePublished(VerbatimTimePeriod.NewVerbatimInstance(2008));
        ref.setAuthorship(Team.NewInstance(Person.NewInstance(null, "Kohlbecker", "A.", null), Person.NewInstance(null, "Kusber", "W.-H.", null)));
        media.addSource(IdentifiableSource.NewPrimaryMediaSourceInstance(ref, "33"));
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
    public void testOrder() throws TypeDesignationSetException{

        @SuppressWarnings("rawtypes")
        List<TypeDesignationBase> tds = new ArrayList<>();
        tds.add(ntd);
        tds.add(std_IT);
        tds.add(std_HT);
        tds.add(std_IT_2);
        tds.add(std_IT_3);

        TaxonName typifiedName = TaxonNameFactory.NewBacterialInstance(Rank.GENUS());
        typifiedName.setTitleCache("Prionus L.", true);

        typifiedName.addTypeDesignation(ntd, false);
        typifiedName.addTypeDesignation(std_HT, false);
        typifiedName.addTypeDesignation(std_IT, false);
        typifiedName.addTypeDesignation(std_IT_2, false);
        typifiedName.addTypeDesignation(std_IT_3, false);

        //order by base entity
        TypeDesignationGroupComparator.ORDER_BY orderByBaseEntity = TypeDesignationGroupComparator.ORDER_BY.BASE_ENTITY;
        TypeDesignationGroupContainer typeDesignationManager = TypeDesignationGroupContainer.NewInstance(tds, orderByBaseEntity);
        String result = typeDesignationManager.print(WITH_CITATION, WITH_TYPE_LABEL, WITH_NAME, !WITH_PRECEDING_MAIN_TYPE, !WITH_ACCESSION_NO_TYPE);

        assertNotNull(result);
        assertEquals(
                "Prionus L."+DASH_W+"Type: Dreamland, near Kissingen, A.Kohlbecker 66211, 2017 (syntype: M);"
                + " Testland, near Bughausen, A.Kohlbecker 81989, 2017 (holotype: OHA; isotypes: BER, KEW);"
                + " Type: Prionus coriatius L."
                , result
                );

        Map<VersionableEntity,TypeDesignationGroup> orderedTypeDesignations =
                typeDesignationManager.getOrderedTypeDesignationSets();
        Iterator<TypeDesignationGroup> byStatusMapIterator = orderedTypeDesignations.values().iterator();
        Iterator<TypeDesignationStatusBase<?>> keyIt_1 = byStatusMapIterator.next().keySet().iterator();
        Iterator<TypeDesignationStatusBase<?>> keyIt_2 = byStatusMapIterator.next().keySet().iterator();
        assertEquals("syntype", keyIt_1.next().getLabel());
        assertEquals("holotype", keyIt_2.next().getLabel());
        assertEquals("isotype", keyIt_2.next().getLabel());

        //order by type status
        TypeDesignationGroupComparator.ORDER_BY orderByTypeStatus = TypeDesignationGroupComparator.ORDER_BY.TYPE_STATUS;
        typeDesignationManager = TypeDesignationGroupContainer.NewInstance(tds, orderByTypeStatus);
        result = typeDesignationManager.print(WITH_CITATION, WITH_TYPE_LABEL, WITH_NAME, !WITH_PRECEDING_MAIN_TYPE, !WITH_ACCESSION_NO_TYPE);
        assertEquals(
                "Prionus L."+DASH_W+"Type: Testland, near Bughausen, A.Kohlbecker 81989, 2017 (holotype: OHA; isotypes: BER, KEW);"
                + " Dreamland, near Kissingen, A.Kohlbecker 66211, 2017 (syntype: M);"
                + " Type: Prionus coriatius L."
                , result
                );
        //same with preceding type
        result = typeDesignationManager.print(WITH_CITATION, WITH_TYPE_LABEL, WITH_NAME, WITH_PRECEDING_MAIN_TYPE, !WITH_ACCESSION_NO_TYPE);
        assertEquals(
                "Prionus L."+DASH_W+"Holotype: Testland, near Bughausen, A.Kohlbecker 81989, 2017 (OHA; isotypes: BER, KEW);"
                + " Syntype: Dreamland, near Kissingen, A.Kohlbecker 66211, 2017 (M);"
                + " Type: Prionus coriatius L."
                , result
                );

        orderedTypeDesignations = typeDesignationManager.getOrderedTypeDesignationSets();
        byStatusMapIterator = orderedTypeDesignations.values().iterator();
        keyIt_1 = byStatusMapIterator.next().keySet().iterator();
        keyIt_2 = byStatusMapIterator.next().keySet().iterator();
        assertEquals("holotype", keyIt_1.next().getLabel());
        assertEquals("isotype", keyIt_1.next().getLabel());
        assertEquals("syntype", keyIt_2.next().getLabel());
    }

    @Test
    public void test2() {

        TaxonName typifiedName = TaxonNameFactory.NewBacterialInstance(Rank.GENUS());
        typifiedName.setTitleCache("Prionus L.", true);

        TypeDesignationGroupContainer typeDesignationManager = new TypeDesignationGroupContainer(typifiedName);
        String result = typeDesignationManager.print(WITH_CITATION, WITH_TYPE_LABEL, WITH_NAME, !WITH_PRECEDING_MAIN_TYPE, !WITH_ACCESSION_NO_TYPE);
        assertEquals(
                "Prionus L."
                , result
                );

        typifiedName.addTypeDesignation(ntd, false);
        typeDesignationManager.addTypeDesigations(ntd);

        assertEquals(
                "Prionus L."+DASH_W+"Type: Prionus coriatius L."
                , typeDesignationManager.print(WITH_CITATION, WITH_TYPE_LABEL, WITH_NAME, !WITH_PRECEDING_MAIN_TYPE, !WITH_ACCESSION_NO_TYPE)
                );

        typifiedName.addTypeDesignation(std_HT, false);
        typeDesignationManager.addTypeDesigations(std_HT);

        assertEquals(
                "Prionus L."+DASH_W+"Type: Testland, near Bughausen, A.Kohlbecker 81989, 2017 (holotype: OHA); Type: Prionus coriatius L."
                , typeDesignationManager.print(WITH_CITATION, WITH_TYPE_LABEL, WITH_NAME, !WITH_PRECEDING_MAIN_TYPE, !WITH_ACCESSION_NO_TYPE)
                );

        DerivedUnit specimen = std_HT.getTypeSpecimen();
        specimen.setProtectedTitleCache(false);
        Collection collection = Collection.NewInstance();
        collection.setName("My collection");
        specimen.setCollection(collection);

        assertEquals(
                "Prionus L."+DASH_W+"Type: Testland, near Bughausen, A.Kohlbecker 81989, 2017 (holotype: My collection); Type: Prionus coriatius L."
                , typeDesignationManager.print(WITH_CITATION, WITH_TYPE_LABEL, WITH_NAME, !WITH_PRECEDING_MAIN_TYPE, !WITH_ACCESSION_NO_TYPE)
                );
    }

    //see #9262
    @Test
    public void test_desigby_fide(){
        //specimen types
        TaxonName typifiedName = TaxonNameFactory.NewBacterialInstance(Rank.SPECIES());
        typifiedName.setTitleCache("Prionus coriatius L.", true);
        TypeDesignationGroupContainer typeDesignationManager = new TypeDesignationGroupContainer(typifiedName);
        typeDesignationManager.addTypeDesigations(std_LT);
        Reference citation = ReferenceFactory.newBook();
        Reference inRef = ReferenceFactory.newBookSection();
        inRef.setInBook(citation);
        citation.setDatePublished(TimePeriodParser.parseStringVerbatim("1989"));
        inRef.setAuthorship(Team.NewTitledInstance("Miller", "Mill."));
        std_LT.addPrimaryTaxonomicSource(inRef, "55");
        assertEquals("Prionus coriatius L."+DASH_W+"Testland, near Bughausen, A.Kohlbecker 81989, 2017 (lectotype (designated by Decandolle & al. 1962): LEC [fide Miller 1989: 55])",
                typeDesignationManager.print(WITH_CITATION, !WITH_TYPE_LABEL, WITH_NAME, !WITH_PRECEDING_MAIN_TYPE, !WITH_ACCESSION_NO_TYPE));
        assertEquals("Prionus coriatius L."+DASH_W+"Testland, near Bughausen, A.Kohlbecker 81989, 2017 (lectotype: LEC)",
                typeDesignationManager.print(!WITH_CITATION,!WITH_TYPE_LABEL, WITH_NAME, !WITH_PRECEDING_MAIN_TYPE, !WITH_ACCESSION_NO_TYPE));
        assertEquals("Testland, near Bughausen, A.Kohlbecker 81989, 2017 (lectotype: LEC)",
                typeDesignationManager.print(!WITH_CITATION, !WITH_TYPE_LABEL, !WITH_NAME, !WITH_PRECEDING_MAIN_TYPE, !WITH_ACCESSION_NO_TYPE));

        //name types
        typifiedName = TaxonNameFactory.NewBacterialInstance(Rank.GENUS());
        typifiedName.setTitleCache("Prionus L.", true);
        typeDesignationManager = new TypeDesignationGroupContainer(typifiedName);
        typeDesignationManager.addTypeDesigations(ntd_LT);
        ntd_LT.addPrimaryTaxonomicSource(inRef, "66");
        assertEquals("Prionus L."+DASH_W+"Lectotype (designated by Decandolle & al. 1962): Prionus arealus L. [fide Miller 1989: 66]",
                typeDesignationManager.print(WITH_CITATION, !WITH_TYPE_LABEL, WITH_NAME, !WITH_PRECEDING_MAIN_TYPE, !WITH_ACCESSION_NO_TYPE));
        assertEquals("Prionus L."+DASH_W+"Lectotype: Prionus arealus L.",
                typeDesignationManager.print(!WITH_CITATION, !WITH_TYPE_LABEL, WITH_NAME, !WITH_PRECEDING_MAIN_TYPE, !WITH_ACCESSION_NO_TYPE));
    }

    @Test
    public void test_mediaType(){

        for(int i = 0; i < 10; i++ ){
            init();
            // repeat 10 times to assure the order of typedesignations is fix in the representations
            TaxonName typifiedName = TaxonNameFactory.NewBacterialInstance(Rank.SPECIES());
            typifiedName.setTitleCache("Prionus coriatius L.", true);
            typifiedName.addTypeDesignation(mtd_HT_published, false);
            typifiedName.addTypeDesignation(mtd_IT_unpublished, false);

            TypeDesignationGroupContainer typeDesignationManager = new TypeDesignationGroupContainer(typifiedName);
            typeDesignationManager.addTypeDesigations(mtd_HT_published);
            typeDesignationManager.addTypeDesigations(mtd_IT_unpublished);

            assertEquals("failed after repeating " + i + " times",
                    "Prionus coriatius L."+DASH_W+"Type: Testland, near Bughausen, A.Kohlbecker 81989, 2017 (holotype: [icon] A nice picture in Kohlbecker & Kusber 2008: 33; isotype: [icon] B Slide A565656)"
                    , typeDesignationManager.print(WITH_CITATION, WITH_TYPE_LABEL, WITH_NAME, !WITH_PRECEDING_MAIN_TYPE, !WITH_ACCESSION_NO_TYPE)
                    );

            Media media = ((MediaSpecimen)mtd_HT_published.getTypeSpecimen()).getMediaSpecimen();
            Reference ref2 = ReferenceFactory.newBook();
            ref2.setDatePublished(VerbatimTimePeriod.NewVerbatimInstance(2009));
            ref2.setAuthorship(Person.NewInstance(null, "Mueller", "A.", null));
            IdentifiableSource newSource = IdentifiableSource.NewPrimaryMediaSourceInstance(ref2, "tab. 4");
            media.addSource(newSource);
            String with2Sources = typeDesignationManager.print(WITH_CITATION, WITH_TYPE_LABEL, WITH_NAME, !WITH_PRECEDING_MAIN_TYPE, !WITH_ACCESSION_NO_TYPE);
            Assert.assertTrue("failed after repeating " + i + " times",
                    //the order of the sources is currently not yet defined (rare case), therefore 2 possibilities
                    with2Sources.equals("Prionus coriatius L."+DASH_W+"Type: Testland, near Bughausen, A.Kohlbecker 81989, 2017 (holotype: [icon] A nice picture in Mueller 2009: tab. 4, Kohlbecker & Kusber 2008: 33; isotype: [icon] B Slide A565656)")
                    || with2Sources.equals("Prionus coriatius L."+DASH_W+"Type: Testland, near Bughausen, A.Kohlbecker 81989, 2017 (holotype: [icon] A nice picture in Kohlbecker & Kusber 2008: 33, Mueller 2009: tab. 4; isotype: [icon] B Slide A565656)"))
                    ;
            media.removeSource(newSource);

            //without field unit
            DerivedUnit mediaSpecimen = mtd_HT_published.getTypeSpecimen();
            mediaSpecimen.setDerivedFrom(null);
            TypeDesignationGroupContainer typeDesignationManager2 = new TypeDesignationGroupContainer(typifiedName);
            typeDesignationManager2.addTypeDesigations(mtd_HT_published);

            assertEquals("failed after repeating " + i + " times",
                    "Holotype: [icon] A nice picture in Kohlbecker & Kusber 2008: 33"
                    , typeDesignationManager2.print(WITH_CITATION, WITH_TYPE_LABEL, !WITH_NAME, WITH_PRECEDING_MAIN_TYPE, !WITH_ACCESSION_NO_TYPE)
                    );

            //with empty field unit
            FieldUnit emptyFieldUnit = FieldUnit.NewInstance();
            createDerivationEvent(emptyFieldUnit, mediaSpecimen);
            TypeDesignationGroupContainer typeDesignationManager3 = new TypeDesignationGroupContainer(typifiedName);
            typeDesignationManager3.addTypeDesigations(mtd_HT_published);
            assertEquals("failed after repeating " + i + " times",
                    "Holotype: [icon] A nice picture in Kohlbecker & Kusber 2008: 33"
                    , typeDesignationManager3.print(WITH_CITATION, WITH_TYPE_LABEL, !WITH_NAME, WITH_PRECEDING_MAIN_TYPE, !WITH_ACCESSION_NO_TYPE)
                    );
        }
    }

    @Test
    public void test_withoutFieldUnit(){
        TaxonName typifiedName = TaxonNameFactory.NewBotanicalInstance(Rank.SPECIES());
        typifiedName.setTitleCache("Prionus coriatius L.", true);
        DerivedUnit protectedSpecimen = DerivedUnit.NewPreservedSpecimenInstance();
        protectedSpecimen.setTitleCache("Mexico. Oaxaca: Coixtlahuaca, Tepelmeme Villa de Morelos, aproximadamente 1 km S del Río Santa Lucía, 1285 m, 27 March 1994, U. Guzmán Cruz 1065 (MEXU 280206)", true);
        Reference citation = ReferenceFactory.newBook();
        citation.setTitle("The book of types");
        SpecimenTypeDesignation protectedDesignation = typifiedName.addSpecimenTypeDesignation(protectedSpecimen, SpecimenTypeDesignationStatus.NEOTYPE(), citation, "55", null, false, false);

        TypeDesignationGroupContainer typeDesignationManager = new TypeDesignationGroupContainer(typifiedName);
        typeDesignationManager.addTypeDesigations(protectedDesignation);

        assertEquals("Prionus coriatius L."+DASH_W+"Neotype (designated by The book of types: 55): Mexico. Oaxaca: Coixtlahuaca, Tepelmeme Villa de Morelos, aproximadamente 1 km S del Río Santa Lucía, 1285 m, 27 March 1994, U. Guzmán Cruz 1065 (MEXU 280206)"
                , typeDesignationManager.print(WITH_CITATION, !WITH_TYPE_LABEL, WITH_NAME, !WITH_PRECEDING_MAIN_TYPE, !WITH_ACCESSION_NO_TYPE)
                );
        protectedDesignation.setTypeStatus(null);
        typeDesignationManager.addTypeDesigations(protectedDesignation);
        assertEquals("Prionus coriatius L."+DASH_W+"Mexico. Oaxaca: Coixtlahuaca, Tepelmeme Villa de Morelos, aproximadamente 1 km S del Río Santa Lucía, 1285 m, 27 March 1994, U. Guzmán Cruz 1065 (MEXU 280206) designated by The book of types: 55"
                , typeDesignationManager.print(WITH_CITATION, !WITH_TYPE_LABEL, WITH_NAME, !WITH_PRECEDING_MAIN_TYPE, !WITH_ACCESSION_NO_TYPE)
                );

        DerivedUnit withoutFieldUnit = DerivedUnit.NewPreservedSpecimenInstance();
        withoutFieldUnit.setAccessionNumber("280207");
        withoutFieldUnit.setCollection(Collection.NewInstance("B", "Herbarium Berolinense"));
        SpecimenTypeDesignation withoutFieldUnitDesignation = typifiedName.addSpecimenTypeDesignation(withoutFieldUnit, SpecimenTypeDesignationStatus.HOLOTYPE(), null, null, null, false, false);
        typeDesignationManager.addTypeDesigations(withoutFieldUnitDesignation);
        assertEquals("Prionus coriatius L."+DASH_W+"Mexico. Oaxaca: Coixtlahuaca, Tepelmeme Villa de Morelos, aproximadamente 1 km S del Río Santa Lucía, 1285 m, 27 March 1994, U. Guzmán Cruz 1065 (MEXU 280206) designated by The book of types: 55; holotype: B 280207"
                , typeDesignationManager.print(WITH_CITATION, !WITH_TYPE_LABEL, WITH_NAME, !WITH_PRECEDING_MAIN_TYPE, !WITH_ACCESSION_NO_TYPE)
                );
    }

    @Test
    public void test_withoutStatus(){
        //for another important test without status see also test_withoutFieldUnit()

        TaxonName typifiedName = TaxonNameFactory.NewBotanicalInstance(Rank.SPECIES());
        typifiedName.setTitleCache("Prionus coriatius L.", true);
        TypeDesignationGroupContainer typeDesignationManager = new TypeDesignationGroupContainer(typifiedName);
        std_LT.setTypeStatus(null);
        typeDesignationManager.addTypeDesigations(std_LT);
        //Note: this is a rare and dirty data only case, a designated by should always go together with a defined type status
        assertEquals("Prionus coriatius L."+DASH_W+"Type: Testland, near Bughausen, A.Kohlbecker 81989, 2017 (LEC designated by Decandolle & al. 1962)",
                typeDesignationManager.print(WITH_CITATION, WITH_TYPE_LABEL, WITH_NAME, !WITH_PRECEDING_MAIN_TYPE, !WITH_ACCESSION_NO_TYPE));
        assertEquals("Prionus coriatius L."+DASH_W+"Testland, near Bughausen, A.Kohlbecker 81989, 2017 (LEC)",
                typeDesignationManager.print(!WITH_CITATION, !WITH_TYPE_LABEL, WITH_NAME, !WITH_PRECEDING_MAIN_TYPE, !WITH_ACCESSION_NO_TYPE));
        assertEquals("Testland, near Bughausen, A.Kohlbecker 81989, 2017 (LEC)",
                typeDesignationManager.print(!WITH_CITATION, !WITH_TYPE_LABEL, !WITH_NAME, !WITH_PRECEDING_MAIN_TYPE, !WITH_ACCESSION_NO_TYPE));

        //name types
        //TODO how far is this "without status", type has a (lectotype) status
        typifiedName = TaxonNameFactory.NewBacterialInstance(Rank.GENUS());
        typifiedName.setTitleCache("Prionus L.", true);
        typeDesignationManager = new TypeDesignationGroupContainer(typifiedName);
        typeDesignationManager.addTypeDesigations(ntd_LT);
//            ntd_LT.addPrimaryTaxonomicSource(inRef, "66");
        assertEquals("Prionus L."+DASH_W+"Lectotype (designated by Decandolle & al. 1962): Prionus arealus L.",
                typeDesignationManager.print(WITH_CITATION, !WITH_TYPE_LABEL, WITH_NAME, !WITH_PRECEDING_MAIN_TYPE, !WITH_ACCESSION_NO_TYPE));
        assertEquals("Prionus L."+DASH_W+"Lectotype: Prionus arealus L.",
                typeDesignationManager.print(!WITH_CITATION, !WITH_TYPE_LABEL, WITH_NAME, !WITH_PRECEDING_MAIN_TYPE, !WITH_ACCESSION_NO_TYPE));
    }

    @Test
    public void test_withMissingStatus(){
        //name types
        TaxonName typifiedName = TaxonNameFactory.NewBotanicalInstance(Rank.GENUS());
        typifiedName.setTitleCache("Prionus L.", true);
        TypeDesignationGroupContainer typeDesignationContainer = new TypeDesignationGroupContainer(typifiedName);
        typeDesignationContainer.addTypeDesigations(ntd);
        assertEquals("Prionus L."+DASH_W+"Type: Prionus coriatius L.",
                typeDesignationContainer.print(WITH_CITATION, WITH_TYPE_LABEL, WITH_NAME, !WITH_PRECEDING_MAIN_TYPE, !WITH_ACCESSION_NO_TYPE));
        assertEquals("Prionus L."+DASH_W+"Type: Prionus coriatius L.",
                typeDesignationContainer.print(WITH_CITATION, WITH_TYPE_LABEL, WITH_NAME, WITH_PRECEDING_MAIN_TYPE, !WITH_ACCESSION_NO_TYPE));
        assertEquals("Prionus L."+DASH_W+"Prionus coriatius L.",
                typeDesignationContainer.print(!WITH_CITATION, !WITH_TYPE_LABEL, WITH_NAME, !WITH_PRECEDING_MAIN_TYPE, !WITH_ACCESSION_NO_TYPE));

    }

}