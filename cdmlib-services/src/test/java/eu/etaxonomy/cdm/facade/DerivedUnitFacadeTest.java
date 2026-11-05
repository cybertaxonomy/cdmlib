/**
 * Copyright (C) 2009 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.facade;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.FileNotFoundException;
import java.lang.reflect.Field;
import java.text.ParseException;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import eu.etaxonomy.cdm.api.service.IOccurrenceService;
import eu.etaxonomy.cdm.api.service.ITermService;
import eu.etaxonomy.cdm.api.service.IUserService;
import eu.etaxonomy.cdm.common.URI;
import eu.etaxonomy.cdm.common.UTF8;
import eu.etaxonomy.cdm.model.agent.AgentBase;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.common.IdentifiableSource;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.LanguageString;
import eu.etaxonomy.cdm.model.common.TimePeriod;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.SpecimenDescription;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.location.Country;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.location.Point;
import eu.etaxonomy.cdm.model.location.ReferenceSystem;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.occurrence.Collection;
import eu.etaxonomy.cdm.model.occurrence.DerivationEvent;
import eu.etaxonomy.cdm.model.occurrence.DerivationEventType;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnit;
import eu.etaxonomy.cdm.model.occurrence.DeterminationEvent;
import eu.etaxonomy.cdm.model.occurrence.FieldUnit;
import eu.etaxonomy.cdm.model.occurrence.GatheringEvent;
import eu.etaxonomy.cdm.model.occurrence.PreservationMethod;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationType;
import eu.etaxonomy.cdm.model.permission.User;
import eu.etaxonomy.cdm.model.reference.OriginalSourceType;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.model.term.DefinedTerm;
import eu.etaxonomy.cdm.test.integration.spring.CdmDataSet;
import eu.etaxonomy.cdm.test.integration.spring.CdmExpectedDataSet;
import eu.etaxonomy.cdm.test.integration.spring.CdmServicesTransactionalSpringIntegrationTest;

/**
 * Test class for {@link DerivedUnitFacade}
 * @author a.mueller
 * @since 17.05.2010
 */
public class DerivedUnitFacadeTest extends CdmServicesTransactionalSpringIntegrationTest {

    @Autowired
    private IOccurrenceService service;

    @Autowired
    private ITermService termService;

    @Autowired
    private IUserService userService;

    DerivedUnit specimen;
    DerivationEvent derivationEvent;
    FieldUnit fieldUnit;
    GatheringEvent gatheringEvent;
    Integer absoluteElevation = 10;
    Integer absoluteElevationMaximum = 14;
    AgentBase<?> collector = Team.NewInstance();
    String collectingMethod = "Collection Method";
    Double distanceToGround = 22.0;
    Double distanceToSurface = 0.3;
    Double distanceToSurfaceMax = 0.7;

    ReferenceSystem referenceSystem = ReferenceSystem.WGS84();
    Point exactLocation = Point.NewInstance(12.3, 10.567, referenceSystem, 22);
    String gatheringEventDescription = "A nice gathering description";
    TimePeriod gatheringPeriod = TimePeriod.NewInstance(1888, 1889);

    String fieldNumber = "15p23B";
    String fieldNotes = "such a beautiful specimen";

    String individualCount = "1";
    DefinedTerm lifeStage = DefinedTerm.NewStageInstance("A wonderful stage", "stage", "st");
    DefinedTerm sex = DefinedTerm.NewSexInstance("FemaleMale", "FM", "FM");
    LanguageString locality = LanguageString.NewInstance("My locality",
            Language.DEFAULT());
    DefinedTerm kindOfUnit = DefinedTerm.NewKindOfUnitInstance("Test kind of unit for field unit", "Test kind of unit", "Tkou");

    String accessionNumber = "888462535";
    String catalogNumber = "UU879873590";
    String collectorsNumber = "234589913A34";
    Collection collection = Collection.NewInstance();
    PreservationMethod preservationMethod = PreservationMethod.NewInstance(null, "my prservation");
    String originalLabelInfo = "original label info";
    URI stableUri = URI.create("http://www.abc.de");

    DerivedUnitFacade specimenFacade;

    DerivedUnit collectionSpecimen;
    GatheringEvent existingGatheringEvent;
    DerivationEvent firstDerivationEvent;
    FieldUnit firstFieldObject;
    Media media1 = Media.NewInstance();

    DerivedUnitFacade emptyFacade;

    // ****************************** SET UP **********************************/

    /**
     * @throws java.lang.Exception
     */
    @BeforeEach
    public void setUp() throws Exception {

        specimen = DerivedUnit.NewPreservedSpecimenInstance();

        derivationEvent = DerivationEvent.NewInstance(DerivationEventType.ACCESSIONING());
        specimen.setDerivedFrom(derivationEvent);
        fieldUnit = FieldUnit.NewInstance();
        fieldUnit.addDerivationEvent(derivationEvent);
        gatheringEvent = GatheringEvent.NewInstance();
        fieldUnit.setGatheringEvent(gatheringEvent);
        gatheringEvent.setAbsoluteElevation(absoluteElevation);
        gatheringEvent.setAbsoluteElevationMax(absoluteElevationMaximum);
        gatheringEvent.setActor(collector);
        gatheringEvent.setCollectingMethod(collectingMethod);
        gatheringEvent.setDistanceToGround(distanceToGround);
        gatheringEvent.setDistanceToWaterSurface(distanceToSurface);
        gatheringEvent.setDistanceToWaterSurfaceMax(distanceToSurfaceMax);
        gatheringEvent.setExactLocation(exactLocation);
        gatheringEvent.setDescription(gatheringEventDescription);
        gatheringEvent.setCountry(Country.GERMANY());

        gatheringEvent.setTimeperiod(gatheringPeriod);
        gatheringEvent.setLocality(locality);

        fieldUnit.setFieldNumber(fieldNumber);
        fieldUnit.setFieldNotes(fieldNotes);
        fieldUnit.setIndividualCount(individualCount);
        fieldUnit.setSex(sex);
        fieldUnit.setLifeStage(lifeStage);
        fieldUnit.setKindOfUnit(kindOfUnit);

        specimen.setAccessionNumber(accessionNumber);
        specimen.setCatalogNumber(catalogNumber);
        specimen.setCollection(collection);
        specimen.setPreservation(preservationMethod);
        specimen.setOriginalLabelInfo(originalLabelInfo);
        specimen.setPreferredStableUri(stableUri);

        specimenFacade = DerivedUnitFacade.NewInstance(specimen);

        // existing specimen with 2 derivation events in line
        collectionSpecimen = DerivedUnit.NewPreservedSpecimenInstance();
        DerivedUnit middleSpecimen = DerivedUnit.NewPreservedSpecimenInstance();
        firstFieldObject = FieldUnit.NewInstance();

		//TODO maybe we should define concrete event types here
        DerivationEventType eventType = DerivationEventType.ACCESSIONING();
        DerivationEvent lastDerivationEvent = DerivationEvent.NewInstance(eventType);
        firstDerivationEvent = DerivationEvent.NewInstance(eventType);

        collectionSpecimen.setDerivedFrom(lastDerivationEvent);

        lastDerivationEvent.addOriginal(middleSpecimen);
        middleSpecimen.setDerivedFrom(firstDerivationEvent);
        firstDerivationEvent.addOriginal(firstFieldObject);
        existingGatheringEvent = GatheringEvent.NewInstance();
        firstFieldObject.setGatheringEvent(existingGatheringEvent);

        // empty facade
        emptyFacade = DerivedUnitFacade.NewInstance(SpecimenOrObservationType.PreservedSpecimen);

    }

    // ****************************** TESTS*****************************/

    @Disabled //doesn't run in suite
    @Test
    @CdmDataSet("DerivedUnitFacadeTest.testSetFieldObjectImageGallery.xml")
    @CdmExpectedDataSet
    public void testSetFieldObjectImageGallery() {
        UUID imageFeatureUuid = Feature.IMAGE().getUuid();
        Feature imageFeature = (Feature) termService.find(imageFeatureUuid);

        DerivedUnitFacade facade = DerivedUnitFacade.NewInstance(SpecimenOrObservationType.PreservedSpecimen);
        facade.innerDerivedUnit().setUuid(UUID.fromString("77af784f-931b-4857-be9a-48ccf31ed3f1"));
        facade.setFieldNumber("12345");
        Media media = Media.NewInstance(URI.create("www.abc.de"), 200, null,"jpeg");

        try {
            SpecimenDescription imageGallery = SpecimenDescription.NewInstance();
            imageGallery.setDescribedSpecimenOrObservation(facade.innerFieldUnit());
            imageGallery.setImageGallery(true);
            TextData textData = TextData.NewInstance();
            textData.setFeature(imageFeature);
            imageGallery.addElement(textData);
            textData.addMedia(media);
            facade.setFieldObjectImageGallery(imageGallery);

        } catch (DerivedUnitFacadeNotSupportedException e1) {
            e1.printStackTrace();
            fail(e1.getLocalizedMessage());
        }
        this.service.save(facade.innerDerivedUnit());

         setComplete(); endTransaction();
//         try {if (true){printDataSet(System.out, new
//         String[]{"HIBERNATE_SEQUENCES","SPECIMENOROBSERVATIONBASE","SPECIMENOROBSERVATIONBASE_DERIVATIONEVENT"
//         ,"DERIVATIONEVENT",
//         "DESCRIPTIONBASE","DESCRIPTIONELEMENTBASE","DESCRIPTIONELEMENTBASE_MEDIA",
//         "MEDIA", "MEDIAREPRESENTATION","MEDIAREPRESENTATIONPART"});}
//         } catch(Exception e) { logger.warn(e);}

    }

    @Test
    @Disabled
    // TODO generally works but has id problems when running together with above
    // test ()setFieldObjectImageGallery. Therefore set to ignore.
    @CdmDataSet("DerivedUnitFacadeTest.testSetDerivedUnitImageGallery.xml")
    @CdmExpectedDataSet
    public void testSetDerivedUnitImageGallery() {
        // UUID specimenUUID =
        // UUID.fromString("25383fc8-789b-4eff-92d3-a770d0622351");
        // Specimen specimen = (Specimen)service.find(specimenUUID);
        DerivedUnitFacade facade = DerivedUnitFacade.NewInstance(SpecimenOrObservationType.PreservedSpecimen);
        Media media = Media.NewInstance(URI.create("www.derivedUnitImage.de"),200, null, "png");

        try {
            SpecimenDescription imageGallery = SpecimenDescription
                    .NewInstance();
            imageGallery.setDescribedSpecimenOrObservation(facade.innerDerivedUnit());
            imageGallery.setImageGallery(true);
            TextData textData = TextData.NewInstance();
            imageGallery.addElement(textData);
            textData.addMedia(media);
            facade.setDerivedUnitImageGallery(imageGallery);

        } catch (DerivedUnitFacadeNotSupportedException e1) {
            e1.printStackTrace();
            fail(e1.getLocalizedMessage());
        }
        this.service.save(facade.innerDerivedUnit());

        // setComplete(); endTransaction();
        // try {if (true){printDataSet(System.out, new
        // String[]{"HIBERNATE_SEQUENCES","SPECIMENOROBSERVATIONBASE","SPECIMENOROBSERVATIONBASE_DERIVATIONEVENT"
        // ,"DERIVATIONEVENT",
        // "DESCRIPTIONBASE","DESCRIPTIONELEMENTBASE","DESCRIPTIONELEMENTBASE_MEDIA",
        // "MEDIA", "MEDIAREPRESENTATION","MEDIAREPRESENTATIONPART"});}
        // } catch(Exception e) { logger.warn(e);}

    }

    @Test
    @CdmDataSet
    @Disabled // TODO generally works has id problems with following tests when running in suite
    public void testGetFieldObjectImageGalleryBooleanPersisted() {
        UUID specimenUUID = UUID.fromString("25383fc8-789b-4eff-92d3-a770d0622351");
        DerivedUnit specimen = (DerivedUnit) service.load(specimenUUID);
        assertNotNull(specimen, "Specimen should exist (persisted)");
        try {
            DerivedUnitFacade facade = DerivedUnitFacade.NewInstance(specimen);
            SpecimenDescription imageGallery = facade.getFieldObjectImageGallery(true);
            assertNotNull(imageGallery, "Image gallery should exist");
            assertEquals(UUID.fromString("8cb772e9-1577-45c6-91ab-dbec1413c060"), imageGallery.getUuid(), "UUID should be equal to the persisted uuid");
            assertEquals(true, imageGallery.isImageGallery(), "The image gallery should be flagged as such");
            assertEquals(1, imageGallery.getElements().size(), "There should be one TextData in image gallery");
            List<Media> media = imageGallery.getElements().iterator().next().getMedia();
            assertEquals(1, media.size(), "There should be 1 media");
        } catch (DerivedUnitFacadeNotSupportedException e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    @CdmDataSet
//	@Disabled // TODO generally works has id problems with following tests when running in suite
	public void testGetDerivedUnitImageGalleryBooleanPersisted() {
		UUID specimenUUID = UUID.fromString("25383fc8-789b-4eff-92d3-a770d0622351");
		DerivedUnit specimen = (DerivedUnit) service.load(specimenUUID);
		assertNotNull(specimen, "Specimen should exist (persisted)");
		try {
			DerivedUnitFacade facade = DerivedUnitFacade.NewInstance(specimen);
			SpecimenDescription imageGallery = facade.getDerivedUnitImageGallery(true);
			assertNotNull(imageGallery, "Image gallery should exist");
			assertEquals(UUID.fromString("cb03acc4-8363-4020-aeef-ea8a8bcc0fe9"), imageGallery.getUuid(), "UUID should be equal to the persisted uuid");
			assertEquals(true, imageGallery.isImageGallery(), "The image gallery should be flagged as such");
			assertEquals(1, imageGallery.getElements().size(), "There should be one TextData in image gallery");
			List<Media> media = imageGallery.getElements().iterator().next().getMedia();
			assertEquals(1, media.size(), "There should be 1 media");
		} catch (DerivedUnitFacadeNotSupportedException e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void testGetDerivedUnitImageGalleryBoolean() {
		DerivedUnit specimen = DerivedUnit.NewPreservedSpecimenInstance();
		try {
			DerivedUnitFacade facade = DerivedUnitFacade.NewInstance(specimen);
			SpecimenDescription imageGallery = facade.getDerivedUnitImageGallery(true);
			assertNotNull(imageGallery, "Image Gallery should have been created");
			assertEquals(true, imageGallery.isImageGallery(), "The image gallery should be flagged as such");
		} catch (DerivedUnitFacadeNotSupportedException e) {
			e.printStackTrace();
			fail();
		}

    }

    /**
     * Test method for
     * {@link eu.etaxonomy.cdm.facade.DerivedUnitFacade#NewInstance()}.
     */
    @Test
    public void testNewInstanceGatheringEventCreated() {
        assertNotNull(specimenFacade.innerDerivedUnit(), "The specimen should have been created");
        // ???
         assertNotNull(specimenFacade.innerDerivedUnit().getDerivedFrom(), "The derivation event should have been created");
         assertNotNull(specimenFacade.innerFieldUnit(), "The field unit should have been created");
         assertNotNull(specimenFacade.innerGatheringEvent(), "The gathering event should have been created");
    }

    /**
     * Test method for
     * {@link eu.etaxonomy.cdm.facade.DerivedUnitFacade#NewInstance(eu.etaxonomy.cdm.model.occurrence.Specimen)}
     * .
     */
    @Test
    public void testNewInstanceSpecimen() {
        assertSame(specimen, specimenFacade.innerDerivedUnit(), "Specimen should be same");
        assertSame(derivationEvent, specimenFacade.innerDerivedUnit().getDerivedFrom(), "Derivation event should be same");
        assertSame(fieldUnit, specimenFacade.innerFieldUnit(), "Field unit should be same");
        assertSame(gatheringEvent, specimenFacade.innerGatheringEvent(), "Gathering event should be same");
    }

    @Test
    public void testNewInstanceSpecimenWithoutFieldUnit() {
        DerivedUnit parent = DerivedUnit.NewPreservedSpecimenInstance();
        DerivedUnit child = DerivedUnit.NewPreservedSpecimenInstance();
        DerivationEvent.NewSimpleInstance(parent, child, DerivationEventType.ACCESSIONING());

        DerivedUnitFacade facade;
		try {
			facade = DerivedUnitFacade.NewInstance(child);
		       	assertTrue(facade.innerDerivedUnit().getDerivedFrom().getOriginals().size() == 1);
		       	assertNull(facade.getFieldUnit(false));
		       	DerivationEvent.NewSimpleInstance(FieldUnit.NewInstance(), parent, DerivationEventType.ACCESSIONING());

		} catch (DerivedUnitFacadeNotSupportedException e) {
			e.printStackTrace();
			fail();
		}

    }

    @Test
    public void testGatheringEventIsConnectedToDerivedUnit() {
    	DerivedUnit specimen = DerivedUnit.NewPreservedSpecimenInstance();
        DerivedUnitFacade specimenFacade;
        try {
            specimenFacade = DerivedUnitFacade.NewInstance(specimen);
            specimenFacade.setDistanceToGround(2.0);
            FieldUnit specimenFieldUnit = (FieldUnit) specimen.getDerivedFrom().getOriginals().iterator().next();
            assertSame(specimenFacade.innerGatheringEvent(), specimenFieldUnit.getGatheringEvent(), "Facade gathering event and specimen gathering event should be the same");
        } catch (DerivedUnitFacadeNotSupportedException e) {
            fail("An error should not occur in NewInstance()");
        }
    }

    @Test
    public void testNoGatheringEventAndFieldUnit() {
    	DerivedUnit specimen = DerivedUnit.NewPreservedSpecimenInstance();
        DerivedUnitFacade specimenFacade;
        try {
            specimenFacade = DerivedUnitFacade.NewInstance(specimen);
            assertNull(specimenFacade.innerFieldUnit(), "No field unit should exists");
        } catch (DerivedUnitFacadeNotSupportedException e) {
            fail("An error should not occur in NewInstance()");
        }
    }

    @Test
    public void testInititializeTextDataWithSupportTest() {
        // TODO
    	DerivedUnit specimen = DerivedUnit.NewPreservedSpecimenInstance();
        DerivedUnitFacade specimenFacade;
        try {
            specimenFacade = DerivedUnitFacade.NewInstance(specimen);
            specimenFacade.setEcology("Ecology");
            String plantDescription = specimenFacade.getPlantDescription();
            assertNull(plantDescription, "No plantDescription should exist yet and no NPE should be thrown until now");
        } catch (DerivedUnitFacadeNotSupportedException e) {
            fail("An error should not occur in NewInstance()");
        }
    }

    @Test
    public void testGetSetType() {
        assertEquals(SpecimenOrObservationType.PreservedSpecimen, specimenFacade.getType(), "Type must be same");
        SpecimenOrObservationType newType = SpecimenOrObservationType.Fossil;
        specimenFacade.setType(newType);
        assertEquals(newType, specimenFacade.getType(), "New type must be Fossil");
        assertEquals(newType, specimenFacade.innerDerivedUnit().getRecordBasis(), "DerivedUnit recordBasis must be set to Fossil");

    }

    @Test
    public void testGetSetCountry() {
        assertEquals(Country.GERMANY(), specimenFacade.getCountry(), "Country must be same");
        specimenFacade.setCountry(Country.FRANCE());
        assertEquals(Country.FRANCE(), specimenFacade.getCountry(), "New country must be France");
    }

    /**
     * Test method for
     * {@link eu.etaxonomy.cdm.facade.DerivedUnitFacade#addCollectingArea(eu.etaxonomy.cdm.model.location.NamedArea)}
     * .
     */
    @Test
    public void testAddGetRemoveCollectingArea() {
        String tdwgLabel = "GER";
        NamedArea tdwgArea = termService.getAreaByTdwgAbbreviation(tdwgLabel);
        NamedArea newCollectingArea = NamedArea.NewInstance("A nice area",
                "nice", "n");
        specimenFacade.addCollectingArea(newCollectingArea);
        assertEquals(1, specimenFacade
                .getCollectingAreas().size(), "Exactly 1 area must exist");
        assertSame(newCollectingArea, specimenFacade.innerFieldUnit().getGatheringEvent()
                        .getCollectingAreas().iterator().next(), "Areas should be same");
        specimenFacade.addCollectingArea(tdwgArea);
        assertEquals(2, specimenFacade
                .getCollectingAreas().size(), "Exactly 2 areas must exist");
        specimenFacade.removeCollectingArea(newCollectingArea);
        assertEquals(1, specimenFacade
                .getCollectingAreas().size(), "Exactly 1 area must exist");
        NamedArea remainingArea = specimenFacade.getCollectingAreas()
                .iterator().next();
        assertEquals(tdwgArea, remainingArea, "Areas should be same");
        specimenFacade.removeCollectingArea(tdwgArea);
        assertEquals(0, specimenFacade
                .getCollectingAreas().size(), "No area should remain");
    }

    /**
     * Test method for
     * {@link eu.etaxonomy.cdm.facade.DerivedUnitFacade#addCollectingArea(eu.etaxonomy.cdm.model.location.NamedArea)}
     * .
     */
    @Test
    public void testAddCollectingAreas() {
        NamedArea firstArea = NamedArea.NewInstance("A nice area", "nice", "n");
        assertEquals(0, specimenFacade.getCollectingAreas().size(), "No area must exist");
        specimenFacade.addCollectingArea(firstArea);
        assertEquals(1, specimenFacade.getCollectingAreas().size(), "Exactly 1 area must exist");

        String tdwgLabel = "GER";
        NamedArea tdwgArea = termService.getAreaByTdwgAbbreviation(tdwgLabel);
        NamedArea secondArea = NamedArea.NewInstance("A nice area", "nice", "n");

        java.util.Collection<NamedArea> areaCollection = new HashSet<NamedArea>();
        areaCollection.add(secondArea);
        areaCollection.add(tdwgArea);
        specimenFacade.addCollectingAreas(areaCollection);
        assertEquals(3, specimenFacade
                .getCollectingAreas().size(), "Exactly 3 areas must exist");

    }

    /**
     * Test method for getting and setting absolute elevation.
     * {@link eu.etaxonomy.cdm.facade.DerivedUnitFacade#getAbsoluteElevation()}
     * .
     */
    @Test
    public void testGetSetAbsoluteElevation() {
        assertEquals(absoluteElevation, specimenFacade.getAbsoluteElevation(), "Absolute elevation must be same");
        specimenFacade.setAbsoluteElevation(400);
        assertEquals(Integer.valueOf(400), specimenFacade.getAbsoluteElevation(), "Absolute elevation must be 400");
    }

    /**
     * Test method for
     * {@link eu.etaxonomy.cdm.facade.DerivedUnitFacade#getAbsoluteElevationError()}
     * .
     */
    @Test
    public void testGetSetAbsoluteElevationMaximum() {
        assertEquals(absoluteElevation, specimenFacade.getAbsoluteElevation(), "Absolute elevation must be same");
        assertEquals(absoluteElevationMaximum, specimenFacade.getAbsoluteElevationMaximum(), "Absolute elevation maximum must be same");
        specimenFacade.setAbsoluteElevationMax(4);
        assertEquals(Integer.valueOf(4), specimenFacade.getAbsoluteElevationMaximum(), "Absolute elevation maximum must be 4");
        assertEquals(absoluteElevation, specimenFacade.getAbsoluteElevation(), "Absolute elevation must be same");

    }

    @Test
    public void testGetSetAbsoluteElevationRange() {
        assertEquals(absoluteElevation, specimenFacade.getAbsoluteElevation(), "");
        assertEquals(absoluteElevationMaximum, specimenFacade.getAbsoluteElevationMaximum(), "");

        specimenFacade.setAbsoluteElevationRange(30, 36);
        assertEquals(Integer.valueOf(36), specimenFacade.getAbsoluteElevationMaximum(), "");
        assertEquals(Integer.valueOf(30), specimenFacade.getAbsoluteElevation(), "");
        assertEquals("30" + UTF8.EN_DASH_SPATIUM + "36 m", specimenFacade.absoluteElevationToString(), "");
        assertEquals(null, specimenFacade.getAbsoluteElevationText(), "");

        specimenFacade.setAbsoluteElevationRange(30, 35);
        assertEquals(String.format("30%s35 m", UTF8.EN_DASH_SPATIUM), specimenFacade.absoluteElevationToString(), "Odd range should not throw an exception anymore");

        specimenFacade.setAbsoluteElevationRange(41, null);
        assertEquals(null, specimenFacade.getAbsoluteElevationMaximum(), "");
        assertEquals(Integer.valueOf(41), specimenFacade.getAbsoluteElevation(), "");
        assertEquals(Integer.valueOf(41), specimenFacade.getAbsoluteElevation(), "");
        assertNull(specimenFacade.getAbsoluteElevationText(), "");
        assertEquals("41 m", specimenFacade.absoluteElevationToString(), "");

        specimenFacade.setAbsoluteElevationRange(null, null);
        assertNull(specimenFacade.getAbsoluteElevation(), "");
        assertNull(specimenFacade.getAbsoluteElevationMaximum(), "");
        assertNull(specimenFacade.absoluteElevationToString(), "");

    }

    @Test
    public void testGetSetAbsoluteElevationText() {
        assertEquals(absoluteElevation, specimenFacade.getAbsoluteElevation(), "");
        assertEquals(absoluteElevationMaximum, specimenFacade.getAbsoluteElevationMaximum(), "");
        assertEquals(null, specimenFacade.getAbsoluteElevationText(), "");

        String elevText = "approx. 30 - 35";
        specimenFacade.setAbsoluteElevationText(elevText);
        assertEquals(absoluteElevation, specimenFacade.getAbsoluteElevation(), "");
        assertEquals(absoluteElevationMaximum, specimenFacade.getAbsoluteElevationMaximum(), "");
        assertEquals(elevText, specimenFacade.absoluteElevationToString(), "");

        specimenFacade.setAbsoluteElevationRange(30, 35);
        assertEquals(elevText, specimenFacade.absoluteElevationToString(), "ToString should not change by setting range if text is set");
        assertEquals(Integer.valueOf(30), specimenFacade.getAbsoluteElevation(), "");
        assertEquals(Integer.valueOf(35), specimenFacade.getAbsoluteElevationMaximum(), "");

        specimenFacade.setAbsoluteElevationRange(41, null);
        assertEquals(elevText, specimenFacade.absoluteElevationToString(), "ToString should not change by setting range if text is set");
        assertEquals(Integer.valueOf(41), specimenFacade.getAbsoluteElevation(), "");
        assertEquals(null, specimenFacade.getAbsoluteElevationMaximum(), "");

        specimenFacade.setAbsoluteElevationText(null);
        assertNull(specimenFacade.getAbsoluteElevationText(), "");
        assertEquals("41 m", specimenFacade.absoluteElevationToString(), "ToString should change by setting text to null");
        assertEquals(Integer.valueOf(41), specimenFacade.getAbsoluteElevation(), "");
        assertEquals(null, specimenFacade.getAbsoluteElevationMaximum(), "");
    }

    /**
     */
    @Test
    public void testGetSetCollector() {
        assertNotNull(specimenFacade.getCollector(), "Collector must not be null");
        assertEquals(collector, specimenFacade.getCollector(), "Collector must be same");
        specimenFacade.setCollector(null);
        assertNull(specimenFacade.getCollector(), "Collector must be null");
    }

    /**
     * Test method for
     * {@link eu.etaxonomy.cdm.facade.DerivedUnitFacade#getCollectingMethod()}
     * .
     */
    @Test
    public void testGetSetCollectingMethod() {
        assertEquals(collectingMethod, specimenFacade.getCollectingMethod(), "Collecting method must be same");
        specimenFacade.setCollectingMethod("new method");
        assertEquals("new method", specimenFacade.getCollectingMethod(), "Collecting method must be 'new method'");
    }

    /**
     * Test method for
     * {@link eu.etaxonomy.cdm.facade.DerivedUnitFacade#getDistanceToGround()}
     * .
     */
    @Test
    public void testGetSetDistanceToGround() {
        assertEquals(distanceToGround, specimenFacade.getDistanceToGround(), "Distance to ground must be same");
        specimenFacade.setDistanceToGround(5.0);
        assertEquals(Double.valueOf(5), specimenFacade.getDistanceToGround(), "Distance to ground must be 5");
    }

    /**
     * Test method for
     * {@link eu.etaxonomy.cdm.facade.DerivedUnitFacade#getDistanceToWaterSurface()}
     * .
     */
    @Test
    public void testGetDistanceToWaterSurface() {
        assertEquals(distanceToSurface, specimenFacade.getDistanceToWaterSurface(), "Distance to surface must be same");
        specimenFacade.setDistanceToWaterSurface(6.0);
        assertEquals(Double.valueOf(6), specimenFacade.getDistanceToWaterSurface(), "Distance to surface must be 6");
        // empty facade tests
        assertNull(emptyFacade.getDistanceToWaterSurface(), "Empty facace must not have any gathering values");
        emptyFacade.setDistanceToWaterSurface(13.0);
        assertNotNull(emptyFacade.getFieldUnit(false), "Field unit must exist if distance to water exists");
        assertNotNull(emptyFacade.getGatheringEvent(false), "Gathering event must exist if distance to water exists");
        FieldUnit specimenFieldUnit = (FieldUnit) emptyFacade
                .innerDerivedUnit().getDerivedFrom().getOriginals().iterator().next();
        assertSame(specimenFieldUnit.getGatheringEvent(), emptyFacade.getGatheringEvent(false), "Gathering event of facade and of specimen must be the same");
    }

    @Test
    public void testGetSetDistanceToWaterText() {
        assertEquals(distanceToSurface, specimenFacade.getDistanceToWaterSurface(), "");
        assertEquals(distanceToSurfaceMax, specimenFacade.getDistanceToWaterSurfaceMax(), "");
        assertEquals(null, specimenFacade.getDistanceToWaterSurfaceText(), "");

        String distText = "approx. 0.3 - 0.6";
        specimenFacade.setDistanceToWaterSurfaceText(distText);
        assertEquals(distanceToSurface, specimenFacade.getDistanceToWaterSurface(), "");
        assertEquals(distanceToSurfaceMax, specimenFacade.getDistanceToWaterSurfaceMax(), "");
        assertEquals(distText, specimenFacade.distanceToWaterSurfaceToString(), "");

        specimenFacade.setDistanceToWaterSurfaceRange(0.6, 1.4);
        assertEquals(distText, specimenFacade.distanceToWaterSurfaceToString(), "ToString should not change by setting range if text is set");
        assertEquals(Double.valueOf(0.6), specimenFacade.getDistanceToWaterSurface(), "");
        assertEquals(Double.valueOf(1.4), specimenFacade.getDistanceToWaterSurfaceMax(), "");

        specimenFacade.setDistanceToWaterSurfaceRange(41.2, null);
        assertEquals(distText, specimenFacade.distanceToWaterSurfaceToString(), "ToString should not change by setting range if text is set");
        assertEquals(Double.valueOf(41.2), specimenFacade.getDistanceToWaterSurface(), "");
        assertEquals(null, specimenFacade.getDistanceToWaterSurfaceMax(), "");

        specimenFacade.setDistanceToWaterSurfaceText(null);
        assertNull(specimenFacade.getDistanceToWaterSurfaceText(), "");
        assertEquals("41.2 m", specimenFacade.distanceToWaterSurfaceToString(), "ToString should change by setting text to null");
        assertEquals(Double.valueOf(41.2), specimenFacade.getDistanceToWaterSurface(), "");
        assertEquals(null, specimenFacade.getDistanceToWaterSurfaceMax(), "");
    }

    /**
     * Test method for
     * {@link eu.etaxonomy.cdm.facade.DerivedUnitFacade#getExactLocation()}.
     */
    @Test
    public void testGetSetExactLocation() {
        assertNotNull(specimenFacade.getExactLocation(), "Exact location must not be null");
        assertEquals(exactLocation, specimenFacade.getExactLocation(), "Exact location must be same");
        specimenFacade.setExactLocation(null);
        assertNull(specimenFacade.getExactLocation(), "Exact location must be null");
    }

    @Test
    public void testSetExactLocationByParsing() {
        Point point1;
        try {
            specimenFacade.setExactLocationByParsing("112\u00B034'20\"W",
                    "34\u00B030,34'N", null, null);
            point1 = specimenFacade.getExactLocation();
            assertNotNull(point1.getLatitude(), "");
            System.out.println(point1.getLatitude().toString());
            assertTrue(point1.getLatitude().toString().startsWith("34.505"), "");
            System.out.println(point1.getLongitude().toString());
            assertTrue(point1.getLongitude().toString().startsWith("-112.5722"), "");

        } catch (ParseException e) {
            fail("No parsing error should occur");
        }
    }

    /**
     * Test method for
     * {@link eu.etaxonomy.cdm.facade.DerivedUnitFacade#getGatheringEventDescription()}
     * .
     */
    @Test
    public void testGetSetGatheringEventDescription() {
        assertEquals(gatheringEventDescription, specimenFacade.getGatheringEventDescription(), "Gathering event description must be same");
        specimenFacade.setGatheringEventDescription("new description");
        assertEquals("new description", specimenFacade.getGatheringEventDescription(), "Gathering event description must be 'new description' now");
    }

    /**
     * Test method for
     * {@link eu.etaxonomy.cdm.facade.DerivedUnitFacade#getTimeperiod()}.
     */
    @Test
    public void testGetTimeperiod() {
        assertNotNull(specimenFacade.getGatheringPeriod(), "Gathering period must not be null");
        assertFalse(specimenFacade.getGatheringPeriod().isEmpty(), "Gathering period must not be empty");
        assertEquals(gatheringPeriod, specimenFacade.getGatheringPeriod(), "Gathering period must be same");
        specimenFacade.setGatheringPeriod(null);
        assertTrue(specimenFacade.getGatheringPeriod().isEmpty(), "Gathering period must be null");
    }

    @Test
    public void testHasFieldObject() throws SecurityException,
            NoSuchFieldException, IllegalArgumentException,
            IllegalAccessException {
        // this test depends on the current implementation of SpecimenFacade. In
        // future
        // field unit may not be initialized from the beginning. Than the
        // following
        // assert should be set to assertNull
        assertTrue(specimenFacade.hasFieldObject(), "field object should not be null (depends on specimen facade initialization !!)");

        Field fieldUnitField = DerivedUnitFacade.class.getDeclaredField("fieldUnit");
        fieldUnitField.setAccessible(true);
        fieldUnitField.set(specimenFacade, null);
        assertFalse(specimenFacade.hasFieldObject(), "The field unit should be null now");

        specimenFacade.setDistanceToGround(33.0);
        assertTrue(specimenFacade.hasFieldObject(), "The field unit should have been created again");
    }

    /**
     * Test method for
     * {@link eu.etaxonomy.cdm.facade.DerivedUnitFacade#addFieldObjectDefinition(java.lang.String, eu.etaxonomy.cdm.model.common.Language)}
     * .
     */
    @Test
    public void testAddGetRemoveFieldObjectDefinition() {
        assertEquals(0, specimenFacade.getFieldObjectDefinition().size(), "There should be no definition yet");
        specimenFacade.addFieldObjectDefinition("Tres interesant",
                Language.FRENCH());
        assertEquals(1, specimenFacade.getFieldObjectDefinition().size(), "There should be exactly one definition");
        assertEquals("Tres interesant", specimenFacade.getFieldObjectDefinition()
                        .get(Language.FRENCH()).getText(), "The French definition should be 'Tres interesant'");
        assertEquals("Tres interesant", specimenFacade.getFieldObjectDefinition(Language.FRENCH()), "The French definition should be 'Tres interesant'");
        specimenFacade.addFieldObjectDefinition("Sehr interessant",
                Language.GERMAN());
        assertEquals(2, specimenFacade.getFieldObjectDefinition().size(), "There should be exactly 2 definition");
        specimenFacade.removeFieldObjectDefinition(Language.FRENCH());
        assertEquals(1, specimenFacade.getFieldObjectDefinition().size(), "There should remain exactly 1 definition");
        assertEquals("Sehr interessant", specimenFacade.getFieldObjectDefinition(Language.GERMAN()), "The remaining German definition should be 'Sehr interessant'");
        specimenFacade.removeFieldObjectDefinition(Language.GERMAN());
        assertEquals(0, specimenFacade.getFieldObjectDefinition().size(), "There should remain no definition");
    }

    /**
     * Test method for
     * {@link eu.etaxonomy.cdm.facade.DerivedUnitFacade#addFieldObjectMedia(eu.etaxonomy.cdm.model.media.Media)}
     * .
     */
    @Test
    public void testAddGetHasRemoveFieldObjectMedia() {
        assertFalse(specimenFacade.hasFieldObjectImageGallery(), "There should be no image gallery yet");
        assertFalse(specimenFacade.hasDerivedUnitImageGallery(), "There should be no specimen image gallery either");

        List<Media> media = specimenFacade.getFieldObjectMedia();
        assertFalse(specimenFacade.hasFieldObjectImageGallery(), "There should still not be an image gallery now");
        assertEquals(0, media.size(), "There should be no media yet in the gallery");

        Media media1 = Media.NewInstance();
        specimenFacade.addFieldObjectMedia(media1);
        assertEquals(1, specimenFacade.getFieldObjectMedia().size(), "There should be exactly one specimen media");
        assertEquals(media1, specimenFacade.getFieldObjectMedia().get(0), "The only media should be media 1");
        assertFalse(specimenFacade.hasDerivedUnitImageGallery(), "There should still no specimen image gallery exist");

        Media media2 = Media.NewInstance();
        specimenFacade.addFieldObjectMedia(media2);
        assertEquals(2, specimenFacade.getFieldObjectMedia().size(), "There should be exactly 2 specimen media");
        assertEquals(media1, specimenFacade.getFieldObjectMedia().get(0), "The first media should be media1");
        assertEquals(media2, specimenFacade.getFieldObjectMedia().get(1), "The second media should be media2");

        specimenFacade.removeFieldObjectMedia(media1);
        assertEquals(1, specimenFacade.getFieldObjectMedia().size(), "There should be exactly one specimen media");
        assertEquals(media2, specimenFacade.getFieldObjectMedia().get(0), "The only media should be media2");

        specimenFacade.removeFieldObjectMedia(media1);
        assertEquals(1, specimenFacade.getFieldObjectMedia().size(), "There should still be exactly one specimen media");

        specimenFacade.removeFieldObjectMedia(media2);
        assertEquals(0, media.size(), "There should remain no media in the gallery");

    }

    /**
     * Test method for
     * {@link eu.etaxonomy.cdm.facade.DerivedUnitFacade#getEcology()}
     * .
     */
    @Test
    public void testGetSetEcology() {
        assertNotNull(specimenFacade.getEcologyAll(), "An empty ecology data should be created when calling getEcology()");
        assertEquals(0, specimenFacade.getEcologyAll().size(), "An empty ecology data should be created when calling getEcology()");
        specimenFacade.setEcology("Tres jolie ici", Language.FRENCH());
        assertEquals(1, specimenFacade.getEcologyAll().size(), "Ecology data should exist for 1 language");
        assertEquals("Tres jolie ici", specimenFacade.getEcology(Language.FRENCH()), "Ecology data should be 'Tres jolie ici' for French");
        assertNull(specimenFacade.getEcology(), "Ecology data should be null for the default language");
        specimenFacade.setEcology("Nice here");
        assertEquals(2, specimenFacade.getEcologyAll().size(), "Ecology data should exist for 2 languages");
        assertEquals("Tres jolie ici", specimenFacade.getEcology(Language.FRENCH()), "Ecology data should be 'Tres jolie ici'");
        assertEquals("Nice here", specimenFacade.getEcology(), "Ecology data should be 'Nice here' for the default language");
        assertEquals("Nice here", specimenFacade.getEcology(), "Ecology data should be 'Nice here' for english");

        specimenFacade.setEcology("Vert et rouge", Language.FRENCH());
        assertEquals(2, specimenFacade.getEcologyAll().size(), "Ecology data should exist for 2 languages");
        assertEquals("Vert et rouge", specimenFacade.getEcology(Language.FRENCH()), "Ecology data should be 'Vert et rouge'");
        assertEquals("Nice here", specimenFacade.getEcology(), "Ecology data should be 'Nice here' for the default language");

        specimenFacade.setEcology(null, Language.FRENCH());
        assertEquals(1, specimenFacade.getEcologyAll().size(), "Ecology data should exist for 1 languages");
        assertEquals("Nice here", specimenFacade.getEcology(), "Ecology data should be 'Nice here' for the default language");
        assertNull(specimenFacade.getEcology(Language.FRENCH()), "Ecology data should be 'null' for French");

        specimenFacade.removeEcology(null);
        assertEquals(0, specimenFacade.getEcologyAll().size(), "There should be no ecology left");
        assertNull(specimenFacade.getEcology(), "Ecology data should be 'null' for default language");

    }

    /**
     * Test method for
     * {@link eu.etaxonomy.cdm.facade.DerivedUnitFacade#getPlantDescription()}
     * .
     */
    @Test
    public void testGetSetPlantDescription() {
        assertNotNull(specimenFacade.getPlantDescriptionAll(), "An empty plant description data should be created when calling getPlantDescriptionAll()");
        assertEquals(0, specimenFacade.getPlantDescriptionAll().size(), "An empty plant description data should be created when calling getPlantDescription()");
        specimenFacade.setPlantDescription("bleu", Language.FRENCH());
        assertEquals(1, specimenFacade.getPlantDescriptionAll().size(), "Plant description data should exist for 1 language");
        assertEquals("bleu", specimenFacade.getPlantDescription(Language.FRENCH()), "Plant description data should be 'bleu' for French");
        assertNull(specimenFacade.getPlantDescription(), "Plant description data should be null for the default language");
        specimenFacade.setPlantDescription("Nice here");
        assertEquals(2, specimenFacade.getPlantDescriptionAll().size(), "Plant description data should exist for 2 languages");
        assertEquals("bleu", specimenFacade.getPlantDescription(Language.FRENCH()), "Plant description data should be 'bleu'");
        assertEquals("Nice here", specimenFacade.getPlantDescription(), "Plant description data should be 'Nice here' for the default language");
        assertEquals("Nice here", specimenFacade.getPlantDescription(), "Plant description data should be 'Nice here' for english");

        specimenFacade.setPlantDescription("Vert et rouge", Language.FRENCH());
        assertEquals(2, specimenFacade.getPlantDescriptionAll().size(), "Plant description data should exist for 2 languages");
        assertEquals("Vert et rouge", specimenFacade.getPlantDescription(Language.FRENCH()), "Plant description data should be 'Vert et rouge'");
        assertEquals("Nice here", specimenFacade.getPlantDescription(), "Plant description data should be 'Nice here' for the default language");

        specimenFacade.setPlantDescription(null, Language.FRENCH());
        assertEquals(1, specimenFacade.getPlantDescriptionAll().size(), "Plant description data should exist for 1 languages");
        assertEquals("Nice here", specimenFacade.getPlantDescription(), "Plant description data should be 'Nice here' for the default language");
        assertNull(specimenFacade.getPlantDescription(Language.FRENCH()), "Plant description data should be 'null' for French");

        // test interference with ecology
        specimenFacade.setEcology("Tres jolie ici", Language.FRENCH());
        assertEquals(1, specimenFacade.getEcologyAll().size(), "Ecology data should exist for 1 language");
        assertEquals("Tres jolie ici", specimenFacade.getEcology(Language.FRENCH()), "Ecology data should be 'Tres jolie ici' for French");
        assertNull(specimenFacade.getEcology(), "Ecology data should be null for the default language");

        // repeat above test
        assertEquals(1, specimenFacade.getPlantDescriptionAll().size(), "Plant description data should exist for 1 languages");
        assertEquals("Nice here", specimenFacade.getPlantDescription(), "Plant description data should be 'Nice here' for the default language");
        assertNull(specimenFacade.getPlantDescription(Language.FRENCH()), "Plant description data should be 'null' for French");

        specimenFacade.removePlantDescription(null);
        assertEquals(0, specimenFacade.getPlantDescriptionAll().size(), "There should be no plant description left");
        assertNull(specimenFacade.getPlantDescription(), "Plant description data should be 'null' for default language");

    }

    /**
     * Test method for
     * {@link eu.etaxonomy.cdm.facade.DerivedUnitFacade#getLifeform()}
     * .
     */
    @Test
    public void testGetSetLifeform() {
        Feature lifeform = Feature.LIFEFORM();
        assertNotNull(lifeform, "Lifeform must exist in testdata");
        assertNotNull(specimenFacade.getLifeformAll(), "An empty life-form data should be created when calling getLifeformAll()");
        assertEquals(0, specimenFacade.getLifeformAll().size(), "An empty life-form data should be created when calling getLifeform()");
        specimenFacade.setLifeform("grande", Language.FRENCH());
        assertEquals(1, specimenFacade.getLifeformAll().size(), "Life-form data should exist for 1 language");
        assertEquals("grande", specimenFacade.getLifeform(Language.FRENCH()), "Life-form data should be 'grande' for French");
        assertNull(specimenFacade.getLifeform(), "Lifeform data should be null for the default language");
        specimenFacade.setLifeform("Very big");
        assertEquals(2, specimenFacade.getLifeformAll().size(), "Life-form data should exist for 2 languages");
        assertEquals("grande", specimenFacade.getLifeform(Language.FRENCH()), "French life-form data should be 'grande'");
        assertEquals("Very big", specimenFacade.getLifeform(), "Default language life-form data should be 'Very big' for the default language");
        assertEquals("Very big", specimenFacade.getLifeform(), "Life-form data should be 'Very big' for English");

        specimenFacade.setLifeform("Petite", Language.FRENCH());
        assertEquals(2, specimenFacade.getLifeformAll().size(), "Plant description data should exist for 2 languages");
        assertEquals("Petite", specimenFacade.getLifeform(Language.FRENCH()), "Life-form data should be 'Petite'");
        assertEquals("Very big", specimenFacade.getLifeform(), "Plant description data should be 'Very big' for the default language");

        specimenFacade.setLifeform(null, Language.FRENCH());
        assertEquals(1, specimenFacade.getLifeformAll().size(), "Life-form data should exist for 1 languages");
        assertEquals("Very big", specimenFacade.getLifeform(), "Life-form data should be 'Very big' for the default language");
        assertNull(specimenFacade.getLifeform(Language.FRENCH()), "Life-form data should be 'null' for French");

        // test interference with ecology
        specimenFacade.setEcology("Tres jolie ici", Language.FRENCH());
        assertEquals(1, specimenFacade.getEcologyAll().size(), "Ecology data should exist for 1 language");
        assertEquals("Tres jolie ici", specimenFacade.getEcology(Language.FRENCH()), "Ecology data should be 'Tres jolie ici' for French");
        assertNull(specimenFacade.getEcology(), "Ecology data should be null for the default language");

        // repeat above test
        assertEquals(1, specimenFacade.getLifeformAll().size(), "Life-form data should exist for 1 languages");
        assertEquals("Very big", specimenFacade.getLifeform(), "Lifeform data should be 'Very big' for the default language");
        assertNull(specimenFacade.getLifeform(Language.FRENCH()), "Life-form data should be 'null' for French");

        specimenFacade.removeLifeform(null);
        assertEquals(0, specimenFacade.getLifeformAll().size(), "There should be no life-form left");
        assertNull(specimenFacade.getLifeform(), "Life-form data should be 'null' for default language");
    }

    /**
     * Test method for
     * {@link eu.etaxonomy.cdm.facade.DerivedUnitFacade#getFieldNumber()}.
     */
    @Test
    public void testGetSetFieldNumber() {
        assertEquals(fieldNumber, specimenFacade.getFieldNumber(), "Field number must be same");
        specimenFacade.setFieldNumber("564AB");
        assertEquals("564AB", specimenFacade.getFieldNumber(), "New field number must be '564AB'");
        // empty facade tests
        assertNull(emptyFacade.getFieldNumber(), "Empty facace must not have any field value");
        emptyFacade.setFieldNumber("1256A");
        assertNotNull(emptyFacade.getFieldUnit(false), "Field unit must exist if field number exists");
        FieldUnit specimenFieldUnit = (FieldUnit) emptyFacade
                .innerDerivedUnit().getDerivedFrom().getOriginals().iterator()
                .next();
        assertSame(specimenFieldUnit, emptyFacade.getFieldUnit(false), "Field unit of facade and of specimen must be the same");
        assertEquals("1256A", emptyFacade.getFieldNumber());
    }

    /**
     * Test method for
     * {@link eu.etaxonomy.cdm.facade.DerivedUnitFacade#getFieldNotes()}.
     */
    @Test
    public void testGetSetFieldNotes() {
        assertEquals(fieldNotes, specimenFacade.getFieldNotes(), "Field notes must be same");
        specimenFacade.setFieldNotes("A completely new info");
        assertEquals("A completely new info", specimenFacade.getFieldNotes(), "New field note must be 'A completely new info'");
    }

    /**
     * Test method for
     * {@link eu.etaxonomy.cdm.facade.DerivedUnitFacade#setGatheringEvent(eu.etaxonomy.cdm.model.occurrence.GatheringEvent)}
     * .
     */
    @Test
    @Disabled // #######DerivationEvent ---------------------------------------- 1
    public void testSetGatheringEvent() {
        GatheringEvent newGatheringEvent = GatheringEvent.NewInstance();
        newGatheringEvent.setDistanceToGround(43.0);
        assertFalse(specimenFacade.getDistanceToGround() == 43.0, "The initial distance to ground should not be 43");
        specimenFacade.setGatheringEvent(newGatheringEvent);
        assertTrue(specimenFacade.getDistanceToGround() == 43.0, "The final distance to ground should be 43");
        assertSame(newGatheringEvent, specimenFacade.innerGatheringEvent(), "The new gathering event should be 'newGatheringEvent'");
    }

    /**
     * Test method for
     * {@link eu.etaxonomy.cdm.facade.DerivedUnitFacade#innerGatheringEvent()}
     * .
     */
    @Test
    public void testGetGatheringEvent() {
        assertNotNull(specimenFacade.innerGatheringEvent(), "Gathering event must not be null");
        assertEquals(specimenFacade.innerFieldUnit().getGatheringEvent(), specimenFacade.innerGatheringEvent(), "Gathering event must be field unit's gathering event");
    }

    /**
     * Test method for
     * {@link eu.etaxonomy.cdm.facade.DerivedUnitFacade#getIndividualCount()}
     * .
     */
    @Test
    public void testGetSetIndividualCount() {
        assertEquals(individualCount, specimenFacade.getIndividualCount(), "Individual count must be same");
        specimenFacade.setIndividualCount("4");
        assertEquals("4", specimenFacade.getIndividualCount(), "New individual count must be '4'");
    }

    /**
     * Test method for
     * {@link eu.etaxonomy.cdm.facade.DerivedUnitFacade#getLifeStage()}.
     */
    @Test
    public void testGetSetLifeStage() {
        assertNotNull(specimenFacade.getFieldUnitLifeStage(), "Life stage must not be null");
        assertEquals(lifeStage, specimenFacade.getFieldUnitLifeStage(), "Life stage must be same");
        specimenFacade.setFieldUnitLifeStage(null);
        assertNull(specimenFacade.getFieldUnitLifeStage(), "Life stage must be null");
        //don't create if null
        DerivedUnitFacade facade = emptyFacade();
        emptyFacade().setFieldUnitLifeStage(null);
        assertNull(facade.innerFieldUnit());
    }

    /**
     * Test method for
     * {@link eu.etaxonomy.cdm.facade.DerivedUnitFacade#getSex()}.
     */
    @Test
    public void testGetSetSex() {
        assertNotNull(specimenFacade.getFieldUnitSex(), "Sex must not be null");
        assertEquals(sex, specimenFacade.getFieldUnitSex(), "Sex must be same");
        specimenFacade.setFieldUnitSex(null);
        assertNull(specimenFacade.getFieldUnitSex(), "Sex must be null");
        //don't create if null
        DerivedUnitFacade facade = emptyFacade();
        emptyFacade().setFieldUnitSex(null);
        assertNull(facade.innerFieldUnit());
    }

    /**
     * Test method for
     * {@link eu.etaxonomy.cdm.facade.DerivedUnitFacade#getSex()}.
     */
    @Test
    public void testGetSetKindOfUnit() {
        assertNotNull(specimenFacade.getFieldUnitKindOfUnit(), "Kind-of-unit must not be null");
        assertEquals(kindOfUnit, specimenFacade.getFieldUnitKindOfUnit(), "Kind-of-unit must be same");
        specimenFacade.setFieldUnitKindOfUnit(null);
        assertNull(specimenFacade.getFieldUnitKindOfUnit(), "Kind-of-unit must be null");
        DerivedUnitFacade facade = emptyFacade();
        facade.setFieldUnitKindOfUnit(null);
        assertNull(facade.innerFieldUnit());
    }

    /**
     * Test method for
     * {@link eu.etaxonomy.cdm.facade.DerivedUnitFacade#getLocality()}.
     */
    @Test
    public void testGetSetLocality() {
        assertEquals(locality, specimenFacade.getLocality(), "Locality must be same");
        specimenFacade.setLocality("A completely new place", Language.FRENCH());
        assertEquals("A completely new place", specimenFacade.getLocalityText(), "New locality must be 'A completely new place'");
        assertEquals(Language.FRENCH(), specimenFacade.getLocalityLanguage(), "New locality language must be French");
        specimenFacade.setLocality("Another place");
        assertEquals("Another place", specimenFacade.getLocalityText(), "New locality must be 'Another place'");
        assertEquals(Language.DEFAULT(), specimenFacade.getLocalityLanguage(), "New locality language must be default");
    }

    /**
     * Test method for
     * {@link eu.etaxonomy.cdm.facade.DerivedUnitFacade#addDerivedUnitDefinition(java.lang.String, eu.etaxonomy.cdm.model.common.Language)}
     * .
     */
    @Test
    public void testAddGetRemoveSpecimenDefinition() {
        assertEquals(0, specimenFacade.getDerivedUnitDefinitions().size(), "There should be no definition yet");
        specimenFacade.addDerivedUnitDefinition("Tres interesant",
                Language.FRENCH());
        assertEquals(1, specimenFacade.getDerivedUnitDefinitions().size(), "There should be exactly one definition");
        assertEquals("Tres interesant", specimenFacade.getDerivedUnitDefinitions()
                        .get(Language.FRENCH()).getText(), "The French definition should be 'Tres interesant'");
        assertEquals("Tres interesant", specimenFacade.getDerivedUnitDefinition(Language.FRENCH()), "The French definition should be 'Tres interesant'");
        specimenFacade.addDerivedUnitDefinition("Sehr interessant",
                Language.GERMAN());
        assertEquals(2, specimenFacade.getDerivedUnitDefinitions().size(), "There should be exactly 2 definition");
        specimenFacade.removeDerivedUnitDefinition(Language.FRENCH());
		assertEquals(1, specimenFacade.getDerivedUnitDefinitions().size(), "There should remain exactly 1 definition");
        assertEquals("Sehr interessant", specimenFacade.getDerivedUnitDefinition(Language.GERMAN()), "The remaining German definition should be 'Sehr interessant'");
		specimenFacade.removeDerivedUnitDefinition(Language.GERMAN());
		assertEquals(0, specimenFacade.getDerivedUnitDefinitions().size(), "There should remain no definition");
    }

    /**
     * Test method for
     * {@link eu.etaxonomy.cdm.facade.DerivedUnitFacade#addDetermination(eu.etaxonomy.cdm.model.occurrence.DeterminationEvent)}
     * .
     */
    @Test
    public void testAddGetRemoveDetermination() {
        assertEquals(0, specimenFacade.getDeterminations().size(), "There should be no determination yet");
        DeterminationEvent determinationEvent1 = DeterminationEvent
                .NewInstance();
        specimenFacade.addDetermination(determinationEvent1);
        assertEquals(1, specimenFacade.getDeterminations().size(), "There should be exactly one determination");
        assertEquals(determinationEvent1, specimenFacade.getDeterminations()
                        .iterator().next(), "The only determination should be determination 1");

        DeterminationEvent determinationEvent2 = DeterminationEvent
                .NewInstance();
        specimenFacade.addDetermination(determinationEvent2);
        assertEquals(2, specimenFacade.getDeterminations().size(), "There should be exactly 2 determinations");
        specimenFacade.removeDetermination(determinationEvent1);

        assertEquals(1, specimenFacade.getDeterminations().size(), "There should remain exactly 1 determination");
        assertEquals(determinationEvent2, specimenFacade.getDeterminations()
                        .iterator().next(), "The remaining determinations should be determination 2");

        specimenFacade.removeDetermination(determinationEvent1);
        assertEquals(1, specimenFacade.getDeterminations().size(), "There should remain exactly 1 determination");

        specimenFacade.removeDetermination(determinationEvent2);
        assertEquals(0, specimenFacade.getDerivedUnitDefinitions().size(), "There should remain no definition");

    }

    /**
     * Test method for
     * {@link eu.etaxonomy.cdm.facade.DerivedUnitFacade#addDetermination(eu.etaxonomy.cdm.model.occurrence.DeterminationEvent)}
     * .
     */
    @Test
    public void testPreferredOtherDeterminations() {
        assertEquals(0, specimenFacade.getDeterminations().size(), "There should be no determination yet");
        DeterminationEvent determinationEvent1 = DeterminationEvent.NewInstance();
        specimenFacade.setPreferredDetermination(determinationEvent1);

        assertEquals(1, specimenFacade.getDeterminations().size(), "There should be exactly one determination");
        assertEquals(determinationEvent1, specimenFacade.getDeterminations()
                        .iterator().next(), "The only determination should be determination 1");
        assertEquals(determinationEvent1, specimenFacade.getPreferredDetermination(), "determination 1 should be the preferred determination");
        assertEquals(0, specimenFacade.getOtherDeterminations().size(), "There should be no 'non preferred' determination");

        DeterminationEvent determinationEvent2 = DeterminationEvent.NewInstance();
        specimenFacade.addDetermination(determinationEvent2);
        assertEquals(2, specimenFacade.getDeterminations().size(), "There should be exactly 2 determinations");
        assertEquals(determinationEvent1, specimenFacade.getPreferredDetermination(), "determination 1 should be the preferred determination");
        assertEquals(1, specimenFacade.getOtherDeterminations().size(), "There should be one 'non preferred' determination");
        assertEquals(determinationEvent2, specimenFacade.getOtherDeterminations().iterator().next(), "The only 'non preferred' determination should be determination 2");

        DeterminationEvent determinationEvent3 = DeterminationEvent.NewInstance();
        specimenFacade.setPreferredDetermination(determinationEvent3);
        assertEquals(3, specimenFacade.getDeterminations().size(), "There should be exactly 3 determinations");
        assertEquals(determinationEvent3, specimenFacade.getPreferredDetermination(), "determination 3 should be the preferred determination");
        assertEquals(2, specimenFacade.getOtherDeterminations().size(), "There should be 2 'non preferred' determination");
        assertTrue(specimenFacade.getOtherDeterminations().contains(determinationEvent1), "determination 1 should be contained in the set of 'non preferred' determinations");
        assertTrue(specimenFacade.getOtherDeterminations().contains(determinationEvent2), "determination 2 should be contained in the set of 'non preferred' determinations");

    }

    /**
     * Test method for
     * {@link eu.etaxonomy.cdm.facade.DerivedUnitFacade#addDerivedUnitMedia(eu.etaxonomy.cdm.model.media.Media)}
     * .
     */
    @Test
    public void testAddGetHasRemoveSpecimenMedia() {
        assertFalse(specimenFacade.hasDerivedUnitImageGallery(), "There should be no image gallery yet");
        assertFalse(specimenFacade.hasFieldObjectImageGallery(), "There should be also no field object image gallery yet");

        List<Media> media = specimenFacade.getDerivedUnitMedia();
        assertFalse(specimenFacade.hasDerivedUnitImageGallery(), "There should still not be an empty image gallery now");
        assertEquals(0, media.size(), "There should be no media yet in the gallery");

        Media media1 = Media.NewInstance();
        specimenFacade.addDerivedUnitMedia(media1);
		assertEquals(1, specimenFacade.getDerivedUnitMedia().size(), "There should be exactly one specimen media");
        assertEquals(media1, specimenFacade.getDerivedUnitMedia().get(0), "The only media should be media 1");
        assertFalse(specimenFacade.hasFieldObjectImageGallery(), "There should be still no field object image gallery");

        Media media2 = Media.NewInstance();
        specimenFacade.addDerivedUnitMedia(media2);
		assertEquals(2, specimenFacade.getDerivedUnitMedia().size(), "There should be exactly 2 specimen media");
        assertEquals(media1, specimenFacade.getDerivedUnitMedia().get(0), "The first media should be media1");
        assertEquals(media2, specimenFacade.getDerivedUnitMedia().get(1), "The second media should be media2");

        specimenFacade.removeDerivedUnitMedia(media1);
        assertEquals(1, specimenFacade.getDerivedUnitMedia().size(), "There should be exactly one specimen media");
        assertEquals(media2, specimenFacade.getDerivedUnitMedia().get(0), "The only media should be media2");

        specimenFacade.removeDerivedUnitMedia(media1);
        assertEquals(1, specimenFacade.getDerivedUnitMedia().size(), "There should still be exactly one specimen media");

        specimenFacade.removeDerivedUnitMedia(media2);
        assertEquals(0, media.size(), "There should remain no media in the gallery");
    }

    /**
     * Test method for
     * {@link eu.etaxonomy.cdm.facade.DerivedUnitFacade#getAccessionNumber()}
     * .
     */
    @Test
    public void testGetSetAccessionNumber() {
        assertEquals(accessionNumber, specimenFacade.getAccessionNumber(), "Accession number must be same");
        specimenFacade.setAccessionNumber("A12345693");
		assertEquals("A12345693", specimenFacade.getAccessionNumber(), "New accession number must be 'A12345693'");
    }

    /**
     * Test method for
     * {@link eu.etaxonomy.cdm.facade.DerivedUnitFacade#getCatalogNumber()}.
     */
    @Test
    public void testGetCatalogNumber() {
        assertEquals(catalogNumber, specimenFacade.getCatalogNumber(), "Catalog number must be same");
        specimenFacade.setCatalogNumber("B12345693");
		assertEquals("B12345693", specimenFacade.getCatalogNumber(), "New catalog number must be 'B12345693'");
    }

    /**
     * Test method for
     * {@link eu.etaxonomy.cdm.facade.DerivedUnitFacade#getPreservation()}.
     */
    @Test
    public void testGetPreservation() {
        try {
            assertNotNull(specimenFacade.getPreservationMethod(), "Preservation method must not be null");
            assertEquals(preservationMethod, specimenFacade.getPreservationMethod(), "Preservation method must be same");
            specimenFacade.setPreservationMethod(null);
            assertNull(specimenFacade.getPreservationMethod(), "Preservation method must be null");
        } catch (MethodNotSupportedByDerivedUnitTypeException e) {
            fail("Method not supported should not be thrown for a specimen");
        }
        specimenFacade = DerivedUnitFacade.NewInstance(SpecimenOrObservationType.Observation);
        try {
            specimenFacade.setPreservationMethod(preservationMethod);
            fail("Method not supported should be thrown for an observation on set preservation method");

        } catch (MethodNotSupportedByDerivedUnitTypeException e) {
            // ok
        }
        specimenFacade = DerivedUnitFacade.NewInstance(SpecimenOrObservationType.LivingSpecimen);
        try {
            specimenFacade.getPreservationMethod();
            fail("Method not supported should be thrown for a living being on get preservation method");
        } catch (MethodNotSupportedByDerivedUnitTypeException e) {
            // ok
        }

    }

    @Test
    public void testGetSetOriginalLabelInfo() {
        assertEquals(originalLabelInfo, specimenFacade.getOriginalLabelInfo(), "Original label info must be same");
        specimenFacade.setOriginalLabelInfo("OrigLabel Info xxx");
        assertEquals("OrigLabel Info xxx", specimenFacade.getOriginalLabelInfo(), "New accession number must be 'OrigLabel Info xxx'");
    }

    @Test
    public void testGetSetStableUri() {
        assertEquals(stableUri, specimenFacade.getPreferredStableUri(), "Preferred stable URI must be same");
        URI newURI = URI.create("http://www.fgh.ij");
        specimenFacade.setPreferredStableUri(newURI);
        assertEquals(newURI, specimenFacade.getPreferredStableUri(), "New preferred stable must be '" + newURI.toString() + "'");
    }

//	/**
//	 * Test method for
//	 * {@link eu.etaxonomy.cdm.api.facade.DerivedUnitFacade#getCollectorsNumber()}
//	 * .
//	 */
//	@Test
//	public void testGetSetCollectorsNumber() {
//		assertEquals(collectorsNumber, //				specimenFacade.getCollectorsNumber(), "Collectors number must be same");
//		specimenFacade.setCollectorsNumber("C12345693");
//		assertEquals(//				"C12345693", specimenFacade.getCollectorsNumber(), "New collectors number must be 'C12345693'");
//	}

    /**
     * Test method for
     * {@link eu.etaxonomy.cdm.facade.DerivedUnitFacade#getTitleCache()}.
     */
    @Test
    public void testGetTitleCache() {
        assertNotNull(specimenFacade.getTitleCache(), "The title cache should never return null if not protected");
        specimenFacade.setTitleCache(null, false);
        assertNotNull(specimenFacade.getTitleCache(), "The title cache should never return null if not protected");
    }

    /**
     * Test method for
     * {@link eu.etaxonomy.cdm.facade.DerivedUnitFacade#setTitleCache(java.lang.String)}
     * .
     */
    @Test
    public void testSetTitleCache() {
        String testTitle = "Absdwk aksjlf";
        specimenFacade.setTitleCache(testTitle, true);
        assertEquals(testTitle, specimenFacade.getTitleCache(), "Protected title cache should returns the test title");
        specimenFacade.setTitleCache(testTitle, false);
        assertFalse(testTitle.equals(specimenFacade.getTitleCache()), "Unprotected title cache should not return the test title");
    }

    /**
     * Test method for
     * {@link eu.etaxonomy.cdm.facade.DerivedUnitFacade#innerDerivedUnit()}.
     */
    @Test
    public void testGetSpecimen() {
        assertEquals(specimen, specimenFacade.innerDerivedUnit(), "Specimen must be same");
    }

    /**
     * Test method for
     * {@link eu.etaxonomy.cdm.facade.DerivedUnitFacade#getCollection()}.
     */
    @Test
    public void testGetSetCollection() {
        assertNotNull(specimenFacade.getCollection(), "Collection must not be null");
        assertEquals(collection, specimenFacade.getCollection(), "Collection must be same");
        specimenFacade.setCollection(null);
        assertNull(specimenFacade.getCollection(), "Collection must be null");
    }

    @Test
    public void testAddGetRemoveSource() {
        assertEquals(0, specimenFacade.getSources().size(), "No sources should exist yet");

        Reference reference = ReferenceFactory.newBook();
        IdentifiableSource source1 = specimenFacade.addSource(OriginalSourceType.PrimaryTaxonomicSource, reference, "54", "myName");
        assertEquals(1, specimenFacade.getSources().size(), "One source should exist now");
        IdentifiableSource source2 = IdentifiableSource.NewDataImportInstance("1", "myTable");
        specimenFacade.addSource(source2);
        assertEquals(2, specimenFacade.getSources().size(), "One source should exist now");
        specimenFacade.removeSource(source1);
        assertEquals(1, specimenFacade.getSources().size(), "One source should exist now");
        Reference reference2 = ReferenceFactory.newJournal();
        IdentifiableSource sourceNotUsed = specimenFacade.addSource(OriginalSourceType.PrimaryTaxonomicSource, reference2,null, null);
        specimenFacade.removeSource(sourceNotUsed);
        assertEquals(1, specimenFacade.getSources().size(), "One source should still exist");
        assertEquals("1", specimenFacade.getSources().iterator().next().getIdInSource());
        specimenFacade.removeSource(source2);
        assertEquals(0, specimenFacade.getSources().size(), "No sources should exist anymore");
    }

    @Test
    public void testAddGetRemoveDuplicate() {
        assertEquals(0, specimenFacade.getDuplicates().size(), "No duplicates should be available yet");
        DerivedUnit newSpecimen1 = DerivedUnit.NewPreservedSpecimenInstance();
        specimenFacade.addDuplicate(newSpecimen1);
        assertEquals(1, specimenFacade.getDuplicates().size(), "There should be 1 duplicate now");
        DerivedUnit newSpecimen2 = DerivedUnit.NewPreservedSpecimenInstance();
        DerivationEvent newDerivationEvent = DerivationEvent.NewInstance(DerivationEventType.ACCESSIONING());
        newSpecimen2.setDerivedFrom(newDerivationEvent);
        assertSame(newDerivationEvent, newSpecimen2.getDerivedFrom(), "The derivation event should be 'newDerivationEvent'");
       	specimenFacade.addDuplicate(newSpecimen2);
        assertEquals(2, specimenFacade.getDuplicates().size(), "There should be 2 duplicates now");
        assertNotSame(newDerivationEvent, newSpecimen2.getDerivedFrom(), "The derivation event should not be 'newDerivationEvent' anymore");
        assertSame(derivationEvent, newSpecimen2.getDerivedFrom(), "The derivation event should not be the facades derivation event");
        specimenFacade.removeDuplicate(newSpecimen1);
        assertEquals(1, specimenFacade.getDuplicates().size(), "There should be 1 duplicate now");
        assertSame(newSpecimen2, specimenFacade.getDuplicates().iterator().next(), "The only duplicate should be 'newSpecimen2' now");
       	specimenFacade.addDuplicate(specimenFacade.innerDerivedUnit());
        assertEquals(1, specimenFacade.getDuplicates().size(), "There should be still 1 duplicate because the facade specimen is not a duplicate");

        Collection newCollection = Collection.NewInstance();
        String catalogNumber = "1234890";
        String accessionNumber = "345345";
        PreservationMethod method = PreservationMethod.NewInstance();
        DerivedUnit duplicateSpecimen = specimenFacade.addDuplicate(newCollection,
				catalogNumber, accessionNumber, method);
        assertEquals(2, specimenFacade.getDuplicates().size(), "There should be 2 duplicates now");
        specimenFacade.removeDuplicate(newSpecimen2);
		assertEquals(1, specimenFacade.getDuplicates().size(), "There should be 1 duplicates now");
        Collection sameCollection = specimenFacade.getDuplicates().iterator().next().getCollection();
        assertSame(newCollection, sameCollection, "Collections should be same");
    }

    // ************************** Existing Specimen with multiple derivation
    // events in line **************/

    @Test
    public void testExistingSpecimen() {
        specimenFacade = null;
        try {
            specimenFacade = DerivedUnitFacade.NewInstance(collectionSpecimen);
        } catch (DerivedUnitFacadeNotSupportedException e) {
            fail("Multiple derivation events in line should not throw a not supported exception");
        }
        assertSame(existingGatheringEvent, specimenFacade.innerGatheringEvent(), "Gathering event should derive from the derivation line");
        assertEquals(0, specimenFacade.getFieldObjectMedia().size(), "Mediasize should be 0. Only Imagegallery media are supported");
    }

    @Test
    public void testMultipleFieldUnitNotSupported() {
        specimenFacade = null;
        FieldUnit secondFieldObject = FieldUnit.NewInstance();
        firstDerivationEvent.addOriginal(secondFieldObject);
        try {
            specimenFacade = DerivedUnitFacade.NewInstance(collectionSpecimen);
            fail("Multiple field units for one specimen should no be supported by the facade");
        } catch (DerivedUnitFacadeNotSupportedException e) {
            // ok
        }
        assertNull(specimenFacade, "Specimen facade should not be initialized");
    }

// 	not required anymore #3597
//    @Test // #######DerivationEvent
//    public void testOnlyImageGallerySupported() {
//        specimenFacade = null;
//        firstFieldObject.addMedia(media1);
//        try {
//            specimenFacade = DerivedUnitFacade.NewInstance(collectionSpecimen);
//            fail("Only image galleries are supported by the facade but not direct media");
//        } catch (DerivedUnitFacadeNotSupportedException e) {
//            // ok
//        }
//        assertNull(//                specimenFacade, "Specimen facade should not be initialized");
//    }

    @Test // #######DerivationEvent
    public void testEventPropagation() {
        specimenFacade.setDistanceToGround(24.0);

    }

    // @Disabled // set to ignore because I did not want to check knowingly
    // failing tests. Remove @Disabled when this is fixed
    @Test
    public void testSetBarcode() {
        String barcode = "barcode";
        specimenFacade.setBarcode(barcode);

        assertEquals(barcode, specimenFacade.getBarcode());
    }

    @Test
    public void testIsEvenDistance() {
        Integer minimum = 20;
        Integer maximum = 1234;

        // this should not throw exceptions
        specimenFacade.setAbsoluteElevationRange(minimum, maximum);
    }

    /**
     *
     * See https://dev.e-taxonomy.eu/redmine/issues/2426
     * This test doesn't handle the above issue yet as it doesn't fire events as
     * expected (at least it does not reproduce the behaviour in the Taxonomic Editor).
     * In the meanwhile the property change framework for the facade has been changed
     * so the original problem may have disappeared.
     *
     */
    @Test
    @CdmDataSet(cleanSweep = true, value = {
            "/eu/etaxonomy/cdm/database/ClearDB_with_Terms_DataSet.xml",
            "/eu/etaxonomy/cdm/database/TermsDataSet-with_auditing_info.xml"
    })
   // @Disabled
    public void testNoRecursiveChangeEvents(){
        String username = "username";
        String password = "password";
        User user = User.NewInstance(username, password);
        userService.save(user);
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(user, password);
        SecurityContextHolder.getContext().setAuthentication(token);

        DerivedUnitFacade facade = DerivedUnitFacade.NewInstance(SpecimenOrObservationType.PreservedSpecimen);
        facade.setLocality("testLocality");
        facade.getTitleCache();
	//	facade.innerGatheringEvent().firePropertyChange("createdBy", null, user);
        this.service.save(facade.innerDerivedUnit());
        commitAndStartNewTransaction(null);
    }

    @Test
    public void testBaseUnit() throws DerivedUnitFacadeNotSupportedException{
        DerivedUnitFacade facade = DerivedUnitFacade.NewInstance(specimen);
        assertEquals(specimen, facade.baseUnit(), "baseUnit is incorrect");

        facade = DerivedUnitFacade.NewInstance(SpecimenOrObservationType.FieldUnit, fieldUnit);
        assertEquals(fieldUnit, facade.baseUnit(), "baseUnit is incorrect");

        facade = DerivedUnitFacade.NewInstance(specimen);
        facade.getFieldUnit(true);
        assertEquals(specimen, facade.baseUnit(), "baseUnit is incorrect");
    }

    /**
     * Returns an empty DU facade
     * @return
     */
    private DerivedUnitFacade emptyFacade() {
        DerivedUnitFacade facade = DerivedUnitFacade.NewInstance(SpecimenOrObservationType.DerivedUnit);
        return facade;
    }

    @Override
    public void createTestDataSet() throws FileNotFoundException {
        // TODO Auto-generated method stub

    }

}
