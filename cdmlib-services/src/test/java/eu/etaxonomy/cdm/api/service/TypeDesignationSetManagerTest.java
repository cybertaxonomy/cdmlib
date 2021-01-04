/**
* Copyright (C) 2018 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import eu.etaxonomy.cdm.api.service.exception.RegistrationValidationException;
import eu.etaxonomy.cdm.api.service.name.TypeDesignationSetManager;
import eu.etaxonomy.cdm.api.service.name.TypeDesignationWorkingSet;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.common.IdentifiableSource;
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
import eu.etaxonomy.cdm.ref.TypedEntityReference;
import eu.etaxonomy.cdm.strategy.parser.TimePeriodParser;
import eu.etaxonomy.cdm.test.TermTestBase;

/**
 * @author a.kohlbecker, k.luther
 * @since 03.09.2018
 */
public class TypeDesignationSetManagerTest  extends TermTestBase{

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
        public void test1() throws RegistrationValidationException{

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

            TypeDesignationSetManager typeDesignationManager = new TypeDesignationSetManager(tds);
            String result = typeDesignationManager.print(true, true, true);

//            Logger.getLogger(this.getClass()).debug(result);
            assertNotNull(result);
            assertEquals(
                    "Prionus L.\u202F\u2013\u202FTypes: Dreamland, near Kissingen, A.Kohlbecker 66211, 2017 (isotype: M);"
                    + " Testland, near Bughausen, A.Kohlbecker 81989, 2017 (holotype: OHA; isotypes: BER, KEW);"
                    + " Nametype: Prionus coriatius L."
                    , result
                    );

            LinkedHashMap<TypedEntityReference<? extends VersionableEntity>, TypeDesignationWorkingSet> orderedTypeDesignations =
                    typeDesignationManager.getOrderedTypeDesignationWorkingSets();
            Iterator<TypeDesignationWorkingSet> byStatusMapIterator = orderedTypeDesignations.values().iterator();
            Iterator<TypeDesignationStatusBase<?>> keyIt_1 = byStatusMapIterator.next().keySet().iterator();
            Iterator<TypeDesignationStatusBase<?>> keyIt_2 = byStatusMapIterator.next().keySet().iterator();
            assertEquals("isotype", keyIt_1.next().getLabel());
            assertEquals("holotype", keyIt_2.next().getLabel());
            assertEquals("isotype", keyIt_2.next().getLabel());
        }

        @Test
        public void test2() {

            TaxonName typifiedName = TaxonNameFactory.NewBacterialInstance(Rank.GENUS());
            typifiedName.setTitleCache("Prionus L.", true);

            TypeDesignationSetManager typeDesignationManager = new TypeDesignationSetManager(typifiedName);
            String result = typeDesignationManager.print(true, true, true);
            assertEquals(
                    "Prionus L."
                    , result
                    );

            typifiedName.addTypeDesignation(ntd, false);
            typeDesignationManager.addTypeDesigations(ntd);

            assertEquals(
                    "Prionus L.\u202F\u2013\u202FNametype: Prionus coriatius L."
                    , typeDesignationManager.print(true, true, true)
                    );

            typifiedName.addTypeDesignation(std_HT, false);
            typeDesignationManager.addTypeDesigations(std_HT);

            assertEquals(
                    "Prionus L.\u202F\u2013\u202FTypes: Testland, near Bughausen, A.Kohlbecker 81989, 2017 (holotype: OHA); Nametype: Prionus coriatius L."
                    , typeDesignationManager.print(true, true, true)
                    );

            DerivedUnit specimen = std_HT.getTypeSpecimen();
            specimen.setProtectedTitleCache(false);
            Collection collection = Collection.NewInstance();
            collection.setName("OHB");
            specimen.setCollection(collection);

            assertEquals(
                    "Prionus L.\u202F\u2013\u202FTypes: Testland, near Bughausen, A.Kohlbecker 81989, 2017 (holotype: OHA); Nametype: Prionus coriatius L."
                    , typeDesignationManager.print(true, true, true)
                    );

        }

        //see #9262
        @Test
        public void test_desigby_fide(){
            //specimen types
            TaxonName typifiedName = TaxonNameFactory.NewBacterialInstance(Rank.SPECIES());
            typifiedName.setTitleCache("Prionus coriatius L.", true);
            TypeDesignationSetManager typeDesignationManager = new TypeDesignationSetManager(typifiedName);
            typeDesignationManager.addTypeDesigations(std_LT);
            Reference citation = ReferenceFactory.newBook();
            Reference inRef = ReferenceFactory.newBookSection();
            inRef.setInBook(citation);
            citation.setDatePublished(TimePeriodParser.parseStringVerbatim("1989"));
            inRef.setAuthorship(Team.NewTitledInstance("Miller", "Mill."));
            std_LT.addPrimaryTaxonomicSource(inRef, "55");
            assertEquals("Prionus coriatius L.\u202F\u2013\u202FTestland, near Bughausen, A.Kohlbecker 81989, 2017 (lectotype: LEC designated by Decandolle & al. 1962 [fide Miller 1989: 55])",
                    typeDesignationManager.print(true, false, true));
            assertEquals("Prionus coriatius L.\u202F\u2013\u202FTestland, near Bughausen, A.Kohlbecker 81989, 2017 (lectotype: LEC)",
                    typeDesignationManager.print(false, false, true));
            assertEquals("Testland, near Bughausen, A.Kohlbecker 81989, 2017 (lectotype: LEC)",
                    typeDesignationManager.print(false, false, false));

            //name types
            typifiedName = TaxonNameFactory.NewBacterialInstance(Rank.GENUS());
            typifiedName.setTitleCache("Prionus L.", true);
            typeDesignationManager = new TypeDesignationSetManager(typifiedName);
            typeDesignationManager.addTypeDesigations(ntd_LT);
            ntd_LT.addPrimaryTaxonomicSource(inRef, "66");
            assertEquals("Prionus L.\u202F\u2013\u202FLectotype: Prionus arealus L. designated by Decandolle & al. 1962 [fide Miller 1989: 66]",
                    typeDesignationManager.print(true, false, true));
            assertEquals("Prionus L.\u202F\u2013\u202FLectotype: Prionus arealus L.",
                    typeDesignationManager.print(false, false, true));

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

                TypeDesignationSetManager typeDesignationManager = new TypeDesignationSetManager(typifiedName);
                typeDesignationManager.addTypeDesigations(mtd_HT_published);
                typeDesignationManager.addTypeDesigations(mtd_IT_unpublished);

                assertEquals("failed after repreating " + i + " times",
                        "Prionus coriatius L.\u202F\u2013\u202FTypes: Testland, near Bughausen, A.Kohlbecker 81989, 2017 (holotype: [icon] p.33 in A.K. & W.K (2008) Algae of the BGBM; isotype: [icon] B Slide A565656)"
                        , typeDesignationManager.print(true, true, true)
                        );
            }
        }
}