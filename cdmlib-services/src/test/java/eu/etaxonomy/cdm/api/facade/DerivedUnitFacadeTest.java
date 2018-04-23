/**
 * Copyright (C) 2009 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.api.facade;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.FileNotFoundException;
import java.lang.reflect.Field;
import java.net.URI;
import java.text.ParseException;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.dbunit.annotation.DataSets;
import org.unitils.dbunit.annotation.ExpectedDataSet;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.api.service.IOccurrenceService;
import eu.etaxonomy.cdm.api.service.ITermService;
import eu.etaxonomy.cdm.api.service.IUserService;
import eu.etaxonomy.cdm.common.UTF8;
import eu.etaxonomy.cdm.model.agent.AgentBase;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.common.DefinedTerm;
import eu.etaxonomy.cdm.model.common.IdentifiableSource;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.LanguageString;
import eu.etaxonomy.cdm.model.common.OriginalSourceType;
import eu.etaxonomy.cdm.model.common.TimePeriod;
import eu.etaxonomy.cdm.model.common.User;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.SpecimenDescription;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.location.Country;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.location.Point;
import eu.etaxonomy.cdm.model.location.ReferenceSystem;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.name.TaxonNameFactory;
import eu.etaxonomy.cdm.model.occurrence.Collection;
import eu.etaxonomy.cdm.model.occurrence.DerivationEvent;
import eu.etaxonomy.cdm.model.occurrence.DerivationEventType;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnit;
import eu.etaxonomy.cdm.model.occurrence.DeterminationEvent;
import eu.etaxonomy.cdm.model.occurrence.FieldUnit;
import eu.etaxonomy.cdm.model.occurrence.GatheringEvent;
import eu.etaxonomy.cdm.model.occurrence.PreservationMethod;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationType;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.test.integration.CdmTransactionalIntegrationTest;
import eu.etaxonomy.cdm.test.unitils.CleanSweepInsertLoadStrategy;

/**
 * Test class for {@link DerivedUnitFacade}
 * @author a.mueller
 * @since 17.05.2010
 */

public class DerivedUnitFacadeTest extends CdmTransactionalIntegrationTest {
    @SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(DerivedUnitFacadeTest.class);


    @SpringBeanByType
    private IOccurrenceService service;

    @SpringBeanByType
    private ITermService termService;

    @SpringBeanByType
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

    Integer individualCount = 1;
    DefinedTerm lifeStage = DefinedTerm.NewStageInstance("A wonderful stage", "stage", "st");
    DefinedTerm sex = DefinedTerm.NewSexInstance("FemaleMale", "FM", "FM");
    LanguageString locality = LanguageString.NewInstance("My locality",
            Language.DEFAULT());
    DefinedTerm kindOfUnit = DefinedTerm.NewKindOfUnitInstance("Test kind of unit for field unit", "Test kind of unit", "Tkou");


    String accessionNumber = "888462535";
    String catalogNumber = "UU879873590";
    TaxonName taxonName = TaxonNameFactory.NewBotanicalInstance(Rank.GENUS(), "Abies",
            null, null, null, null, null, null, null);
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
    @Before
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
        specimen.setStoredUnder(taxonName);
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
        DerivationEvent middleDerivationEvent = DerivationEvent.NewInstance(eventType);
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

    @Ignore //doesn't run in suite
    @Test
    @DataSet("DerivedUnitFacadeTest.testSetFieldObjectImageGallery.xml")
    @ExpectedDataSet
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
    @Ignore
    // TODO generally works but has id problems when running together with above
    // test ()setFieldObjectImageGallery. Therefore set to ignore.
    @DataSet("DerivedUnitFacadeTest.testSetDerivedUnitImageGallery.xml")
    @ExpectedDataSet
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
    @DataSet
    @Ignore // TODO generally works has id problems with following tests when running in suite
    public void testGetFieldObjectImageGalleryBooleanPersisted() {
        UUID specimenUUID = UUID.fromString("25383fc8-789b-4eff-92d3-a770d0622351");
        DerivedUnit specimen = (DerivedUnit) service.load(specimenUUID);
        assertNotNull("Specimen should exist (persisted)", specimen);
        try {
            DerivedUnitFacade facade = DerivedUnitFacade.NewInstance(specimen);
            SpecimenDescription imageGallery = facade.getFieldObjectImageGallery(true);
            assertNotNull("Image gallery should exist", imageGallery);
            assertEquals("UUID should be equal to the persisted uuid",
                    UUID.fromString("8cb772e9-1577-45c6-91ab-dbec1413c060"),
                    imageGallery.getUuid());
            assertEquals("The image gallery should be flagged as such",true, imageGallery.isImageGallery());
            assertEquals("There should be one TextData in image gallery", 1,
                    imageGallery.getElements().size());
            List<Media> media = imageGallery.getElements().iterator().next().getMedia();
            assertEquals("There should be 1 media", 1, media.size());
        } catch (DerivedUnitFacadeNotSupportedException e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    @DataSet
//	@Ignore // TODO generally works has id problems with following tests when running in suite
	public void testGetDerivedUnitImageGalleryBooleanPersisted() {
		UUID specimenUUID = UUID.fromString("25383fc8-789b-4eff-92d3-a770d0622351");
		DerivedUnit specimen = (DerivedUnit) service.load(specimenUUID);
		assertNotNull("Specimen should exist (persisted)", specimen);
		try {
			DerivedUnitFacade facade = DerivedUnitFacade.NewInstance(specimen);
			SpecimenDescription imageGallery = facade.getDerivedUnitImageGallery(true);
			assertNotNull("Image gallery should exist", imageGallery);
			assertEquals("UUID should be equal to the persisted uuid",
					UUID.fromString("cb03acc4-8363-4020-aeef-ea8a8bcc0fe9"),
					imageGallery.getUuid());
			assertEquals("The image gallery should be flagged as such",
					true, imageGallery.isImageGallery());
			assertEquals(
					"There should be one TextData in image gallery", 1,
					imageGallery.getElements().size());
			List<Media> media = imageGallery.getElements().iterator().next().getMedia();
			assertEquals("There should be 1 media", 1, media.size());
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
			assertNotNull("Image Gallery should have been created",imageGallery);
			assertEquals("The image gallery should be flagged as such",true, imageGallery.isImageGallery());
		} catch (DerivedUnitFacadeNotSupportedException e) {
			e.printStackTrace();
			fail();
		}

    }

    /**
     * Test method for
     * {@link eu.etaxonomy.cdm.api.facade.DerivedUnitFacade#NewInstance()}.
     */
    @Test
    public void testNewInstanceGatheringEventCreated() {
        assertNotNull("The specimen should have been created",
                specimenFacade.innerDerivedUnit());
        // ???
         assertNotNull("The derivation event should have been created",
        		 specimenFacade.innerDerivedUnit().getDerivedFrom());
         assertNotNull("The field unit should have been created",
        		 specimenFacade.innerFieldUnit());
         assertNotNull("The gathering event should have been created",
        		 specimenFacade.innerGatheringEvent());
    }




    /**
     * Test method for
     * {@link eu.etaxonomy.cdm.api.facade.DerivedUnitFacade#NewInstance(eu.etaxonomy.cdm.model.occurrence.Specimen)}
     * .
     */
    @Test
    public void testNewInstanceSpecimen() {
        assertSame("Specimen should be same", specimen,
                specimenFacade.innerDerivedUnit());
        assertSame("Derivation event should be same", derivationEvent,
                specimenFacade.innerDerivedUnit().getDerivedFrom());
        assertSame("Field unit should be same", fieldUnit,
                specimenFacade.innerFieldUnit());
        assertSame("Gathering event should be same", gatheringEvent,
                specimenFacade.innerGatheringEvent());
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
            assertSame(
                    "Facade gathering event and specimen gathering event should be the same",
                    specimenFacade.innerGatheringEvent(),
                    specimenFieldUnit.getGatheringEvent());
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
            assertNull("No field unit should exists",
                    specimenFacade.innerFieldUnit());
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
            assertNull(
                    "No plantDescription should exist yet and no NPE should be thrown until now",
                    plantDescription);
        } catch (DerivedUnitFacadeNotSupportedException e) {
            fail("An error should not occur in NewInstance()");
        }
    }

    @Test
    public void testGetSetType() {
        assertEquals("Type must be same", SpecimenOrObservationType.PreservedSpecimen, specimenFacade.getType());
        SpecimenOrObservationType newType = SpecimenOrObservationType.Fossil;
        specimenFacade.setType(newType);
        assertEquals("New type must be Fossil", newType, specimenFacade.getType());
        assertEquals("DerivedUnit recordBasis must be set to Fossil", newType, specimenFacade.innerDerivedUnit().getRecordBasis());

    }

    @Test
    public void testGetSetCountry() {
        assertEquals("Country must be same", Country.GERMANY(), specimenFacade.getCountry());
        specimenFacade.setCountry(Country.FRANCEFRENCHREPUBLIC());
        assertEquals("New country must be France", Country.FRANCEFRENCHREPUBLIC(), specimenFacade.getCountry());
    }

    /**
     * Test method for
     * {@link eu.etaxonomy.cdm.api.facade.DerivedUnitFacade#addCollectingArea(eu.etaxonomy.cdm.model.location.NamedArea)}
     * .
     */
    @Test
    public void testAddGetRemoveCollectingArea() {
        String tdwgLabel = "GER";
        NamedArea tdwgArea = termService.getAreaByTdwgAbbreviation(tdwgLabel);
        NamedArea newCollectingArea = NamedArea.NewInstance("A nice area",
                "nice", "n");
        specimenFacade.addCollectingArea(newCollectingArea);
        assertEquals("Exactly 1 area must exist", 1, specimenFacade
                .getCollectingAreas().size());
        assertSame("Areas should be same", newCollectingArea,
                specimenFacade.innerFieldUnit().getGatheringEvent()
                        .getCollectingAreas().iterator().next());
        specimenFacade.addCollectingArea(tdwgArea);
        assertEquals("Exactly 2 areas must exist", 2, specimenFacade
                .getCollectingAreas().size());
        specimenFacade.removeCollectingArea(newCollectingArea);
        assertEquals("Exactly 1 area must exist", 1, specimenFacade
                .getCollectingAreas().size());
        NamedArea remainingArea = specimenFacade.getCollectingAreas()
                .iterator().next();
        assertEquals("Areas should be same", tdwgArea, remainingArea);
        specimenFacade.removeCollectingArea(tdwgArea);
        assertEquals("No area should remain", 0, specimenFacade
                .getCollectingAreas().size());
    }

    /**
     * Test method for
     * {@link eu.etaxonomy.cdm.api.facade.DerivedUnitFacade#addCollectingArea(eu.etaxonomy.cdm.model.location.NamedArea)}
     * .
     */
    @Test
    public void testAddCollectingAreas() {
        NamedArea firstArea = NamedArea.NewInstance("A nice area", "nice", "n");
        assertEquals("No area must exist", 0, specimenFacade.getCollectingAreas().size());
        specimenFacade.addCollectingArea(firstArea);
        assertEquals("Exactly 1 area must exist", 1, specimenFacade.getCollectingAreas().size());

        String tdwgLabel = "GER";
        NamedArea tdwgArea = termService.getAreaByTdwgAbbreviation(tdwgLabel);
        NamedArea secondArea = NamedArea.NewInstance("A nice area", "nice", "n");

        java.util.Collection<NamedArea> areaCollection = new HashSet<NamedArea>();
        areaCollection.add(secondArea);
        areaCollection.add(tdwgArea);
        specimenFacade.addCollectingAreas(areaCollection);
        assertEquals("Exactly 3 areas must exist", 3, specimenFacade
                .getCollectingAreas().size());

    }

    /**
     * Test method for getting and setting absolute elevation.
     * {@link eu.etaxonomy.cdm.api.facade.DerivedUnitFacade#getAbsoluteElevation()}
     * .
     */
    @Test
    public void testGetSetAbsoluteElevation() {
        assertEquals("Absolute elevation must be same",absoluteElevation, specimenFacade.getAbsoluteElevation());
        specimenFacade.setAbsoluteElevation(400);
        assertEquals("Absolute elevation must be 400",Integer.valueOf(400), specimenFacade.getAbsoluteElevation());
    }

    /**
     * Test method for
     * {@link eu.etaxonomy.cdm.api.facade.DerivedUnitFacade#getAbsoluteElevationError()}
     * .
     */
    @Test
    public void testGetSetAbsoluteElevationMaximum() {
        assertEquals("Absolute elevation must be same",absoluteElevation, specimenFacade.getAbsoluteElevation());
        assertEquals("Absolute elevation maximum must be same",absoluteElevationMaximum, specimenFacade.getAbsoluteElevationMaximum());
        specimenFacade.setAbsoluteElevationMax(4);
        assertEquals("Absolute elevation maximum must be 4", Integer.valueOf(4), specimenFacade.getAbsoluteElevationMaximum());
        assertEquals("Absolute elevation must be same",absoluteElevation, specimenFacade.getAbsoluteElevation());

    }

    @Test
    public void testGetSetAbsoluteElevationRange() {
        assertEquals("", absoluteElevation, specimenFacade.getAbsoluteElevation());
        assertEquals("", absoluteElevationMaximum,specimenFacade.getAbsoluteElevationMaximum());

        specimenFacade.setAbsoluteElevationRange(30, 36);
        assertEquals("", Integer.valueOf(36),specimenFacade.getAbsoluteElevationMaximum());
        assertEquals("", Integer.valueOf(30),specimenFacade.getAbsoluteElevation());
        assertEquals("", "30" + UTF8.EN_DASH_SPATIUM + "36 m",specimenFacade.absoluteElevationToString());
        assertEquals("", null,specimenFacade.getAbsoluteElevationText());

        specimenFacade.setAbsoluteElevationRange(30, 35);
        assertEquals("Odd range should not throw an exception anymore",
        		String.format("30%s35 m", UTF8.EN_DASH_SPATIUM),specimenFacade.absoluteElevationToString());

        specimenFacade.setAbsoluteElevationRange(41, null);
        assertEquals("", null,specimenFacade.getAbsoluteElevationMaximum());
        assertEquals("", Integer.valueOf(41),specimenFacade.getAbsoluteElevation());
        assertEquals("", Integer.valueOf(41),specimenFacade.getAbsoluteElevation());
        assertNull("", specimenFacade.getAbsoluteElevationText());
        assertEquals("", "41 m",specimenFacade.absoluteElevationToString());

        specimenFacade.setAbsoluteElevationRange(null, null);
        assertNull("", specimenFacade.getAbsoluteElevation());
        assertNull("", specimenFacade.getAbsoluteElevationMaximum());
        assertNull("", specimenFacade.absoluteElevationToString());

    }

    @Test
    public void testGetSetAbsoluteElevationText() {
        assertEquals("", absoluteElevation, specimenFacade.getAbsoluteElevation());
        assertEquals("", absoluteElevationMaximum,specimenFacade.getAbsoluteElevationMaximum());
        assertEquals("", null,specimenFacade.getAbsoluteElevationText());

        String elevText = "approx. 30 - 35";
        specimenFacade.setAbsoluteElevationText(elevText);
        assertEquals("", absoluteElevation, specimenFacade.getAbsoluteElevation());
        assertEquals("", absoluteElevationMaximum,specimenFacade.getAbsoluteElevationMaximum());
        assertEquals("", elevText,specimenFacade.absoluteElevationToString());

        specimenFacade.setAbsoluteElevationRange(30, 35);
        assertEquals("ToString should not change by setting range if text is set", elevText,specimenFacade.absoluteElevationToString());
        assertEquals("", Integer.valueOf(30), specimenFacade.getAbsoluteElevation());
        assertEquals("", Integer.valueOf(35),specimenFacade.getAbsoluteElevationMaximum());


        specimenFacade.setAbsoluteElevationRange(41, null);
        assertEquals("ToString should not change by setting range if text is set", elevText,specimenFacade.absoluteElevationToString());
        assertEquals("", Integer.valueOf(41), specimenFacade.getAbsoluteElevation());
        assertEquals("", null,specimenFacade.getAbsoluteElevationMaximum());


        specimenFacade.setAbsoluteElevationText(null);
        assertNull("", specimenFacade.getAbsoluteElevationText());
        assertEquals("ToString should change by setting text to null", "41 m",specimenFacade.absoluteElevationToString());
        assertEquals("", Integer.valueOf(41), specimenFacade.getAbsoluteElevation());
        assertEquals("", null,specimenFacade.getAbsoluteElevationMaximum());
    }


    /**
     */
    @Test
    public void testGetSetCollector() {
        assertNotNull("Collector must not be null",
                specimenFacade.getCollector());
        assertEquals("Collector must be same", collector,
                specimenFacade.getCollector());
        specimenFacade.setCollector(null);
        assertNull("Collector must be null",
                specimenFacade.getCollector());
    }

    /**
     * Test method for
     * {@link eu.etaxonomy.cdm.api.facade.DerivedUnitFacade#getCollectingMethod()}
     * .
     */
    @Test
    public void testGetSetCollectingMethod() {
        assertEquals("Collecting method must be same", collectingMethod,
                specimenFacade.getCollectingMethod());
        specimenFacade.setCollectingMethod("new method");
        assertEquals("Collecting method must be 'new method'",
                "new method", specimenFacade.getCollectingMethod());
    }

    /**
     * Test method for
     * {@link eu.etaxonomy.cdm.api.facade.DerivedUnitFacade#getDistanceToGround()}
     * .
     */
    @Test
    public void testGetSetDistanceToGround() {
        assertEquals("Distance to ground must be same",distanceToGround, specimenFacade.getDistanceToGround());
        specimenFacade.setDistanceToGround(5.0);
        assertEquals("Distance to ground must be 5", Double.valueOf(5), specimenFacade.getDistanceToGround());
    }

    /**
     * Test method for
     * {@link eu.etaxonomy.cdm.api.facade.DerivedUnitFacade#getDistanceToWaterSurface()}
     * .
     */
    @Test
    public void testGetDistanceToWaterSurface() {
        assertEquals("Distance to surface must be same", distanceToSurface, specimenFacade.getDistanceToWaterSurface());
        specimenFacade.setDistanceToWaterSurface(6.0);
        assertEquals("Distance to surface must be 6", Double.valueOf(6), specimenFacade.getDistanceToWaterSurface());
        // empty facade tests
        assertNull("Empty facace must not have any gathering values", emptyFacade.getDistanceToWaterSurface());
        emptyFacade.setDistanceToWaterSurface(13.0);
        assertNotNull("Field unit must exist if distance to water exists", emptyFacade.getFieldUnit(false));
        assertNotNull("Gathering event must exist if distance to water exists", emptyFacade.getGatheringEvent(false));
        FieldUnit specimenFieldUnit = (FieldUnit) emptyFacade
                .innerDerivedUnit().getDerivedFrom().getOriginals().iterator().next();
        assertSame("Gathering event of facade and of specimen must be the same",
                specimenFieldUnit.getGatheringEvent(), emptyFacade.getGatheringEvent(false));
    }

    @Test
    public void testGetSetDistanceToWaterText() {
        assertEquals("", distanceToSurface, specimenFacade.getDistanceToWaterSurface());
        assertEquals("", distanceToSurfaceMax ,specimenFacade.getDistanceToWaterSurfaceMax());
        assertEquals("", null,specimenFacade.getDistanceToWaterSurfaceText());

        String distText = "approx. 0.3 - 0.6";
        specimenFacade.setDistanceToWaterSurfaceText(distText);
        assertEquals("", distanceToSurface, specimenFacade.getDistanceToWaterSurface());
        assertEquals("", distanceToSurfaceMax,specimenFacade.getDistanceToWaterSurfaceMax());
        assertEquals("", distText,specimenFacade.distanceToWaterSurfaceToString());

        specimenFacade.setDistanceToWaterSurfaceRange(0.6, 1.4);
        assertEquals("ToString should not change by setting range if text is set", distText,specimenFacade.distanceToWaterSurfaceToString());
        assertEquals("", Double.valueOf(0.6), specimenFacade.getDistanceToWaterSurface());
        assertEquals("", Double.valueOf(1.4),specimenFacade.getDistanceToWaterSurfaceMax());

        specimenFacade.setDistanceToWaterSurfaceRange(41.2, null);
        assertEquals("ToString should not change by setting range if text is set", distText,specimenFacade.distanceToWaterSurfaceToString());
        assertEquals("", Double.valueOf(41.2), specimenFacade.getDistanceToWaterSurface());
        assertEquals("", null,specimenFacade.getDistanceToWaterSurfaceMax());

        specimenFacade.setDistanceToWaterSurfaceText(null);
        assertNull("", specimenFacade.getDistanceToWaterSurfaceText());
        assertEquals("ToString should change by setting text to null", "41.2 m",specimenFacade.distanceToWaterSurfaceToString());
        assertEquals("", Double.valueOf(41.2), specimenFacade.getDistanceToWaterSurface());
        assertEquals("", null,specimenFacade.getDistanceToWaterSurfaceMax());
    }


    /**
     * Test method for
     * {@link eu.etaxonomy.cdm.api.facade.DerivedUnitFacade#getExactLocation()}.
     */
    @Test
    public void testGetSetExactLocation() {
        assertNotNull("Exact location must not be null",
                specimenFacade.getExactLocation());
        assertEquals("Exact location must be same", exactLocation,
                specimenFacade.getExactLocation());
        specimenFacade.setExactLocation(null);
        assertNull("Exact location must be null",
                specimenFacade.getExactLocation());
    }

    @Test
    public void testSetExactLocationByParsing() {
        Point point1;
        try {
            specimenFacade.setExactLocationByParsing("112\u00B034'20\"W",
                    "34\u00B030,34'N", null, null);
            point1 = specimenFacade.getExactLocation();
            assertNotNull("", point1.getLatitude());
            System.out.println(point1.getLatitude().toString());
            assertTrue("",
                    point1.getLatitude().toString().startsWith("34.505"));
            System.out.println(point1.getLongitude().toString());
            assertTrue("",
                    point1.getLongitude().toString().startsWith("-112.5722"));

        } catch (ParseException e) {
            fail("No parsing error should occur");
        }
    }

    /**
     * Test method for
     * {@link eu.etaxonomy.cdm.api.facade.DerivedUnitFacade#getGatheringEventDescription()}
     * .
     */
    @Test
    public void testGetSetGatheringEventDescription() {
        assertEquals("Gathering event description must be same",
                gatheringEventDescription,
                specimenFacade.getGatheringEventDescription());
        specimenFacade.setGatheringEventDescription("new description");
        assertEquals(
                "Gathering event description must be 'new description' now",
                "new description",
                specimenFacade.getGatheringEventDescription());
    }

    /**
     * Test method for
     * {@link eu.etaxonomy.cdm.api.facade.DerivedUnitFacade#getTimeperiod()}.
     */
    @Test
    public void testGetTimeperiod() {
        assertNotNull("Gathering period must not be null", specimenFacade.getGatheringPeriod());
        assertFalse("Gathering period must not be empty", specimenFacade.getGatheringPeriod().isEmpty());
        assertEquals("Gathering period must be same", gatheringPeriod, specimenFacade.getGatheringPeriod());
        specimenFacade.setGatheringPeriod(null);
        assertTrue("Gathering period must be null", specimenFacade.getGatheringPeriod().isEmpty());
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
        assertTrue("field object should not be null (depends on specimen facade initialization !!)",
                specimenFacade.hasFieldObject());

        Field fieldUnitField = DerivedUnitFacade.class.getDeclaredField("fieldUnit");
        fieldUnitField.setAccessible(true);
        fieldUnitField.set(specimenFacade, null);
        assertFalse("The field unit should be null now",
                specimenFacade.hasFieldObject());

        specimenFacade.setDistanceToGround(33.0);
        assertTrue(
                "The field unit should have been created again",
                specimenFacade.hasFieldObject());
    }

    /**
     * Test method for
     * {@link eu.etaxonomy.cdm.api.facade.DerivedUnitFacade#addFieldObjectDefinition(java.lang.String, eu.etaxonomy.cdm.model.common.Language)}
     * .
     */
    @Test
    public void testAddGetRemoveFieldObjectDefinition() {
        assertEquals("There should be no definition yet", 0,
                specimenFacade.getFieldObjectDefinition().size());
        specimenFacade.addFieldObjectDefinition("Tres interesant",
                Language.FRENCH());
        assertEquals("There should be exactly one definition", 1,
                specimenFacade.getFieldObjectDefinition().size());
        assertEquals(
                "The French definition should be 'Tres interesant'",
                "Tres interesant", specimenFacade.getFieldObjectDefinition()
                        .get(Language.FRENCH()).getText());
        assertEquals(
                "The French definition should be 'Tres interesant'",
                "Tres interesant",
                specimenFacade.getFieldObjectDefinition(Language.FRENCH()));
        specimenFacade.addFieldObjectDefinition("Sehr interessant",
                Language.GERMAN());
        assertEquals("There should be exactly 2 definition", 2,
                specimenFacade.getFieldObjectDefinition().size());
        specimenFacade.removeFieldObjectDefinition(Language.FRENCH());
        assertEquals("There should remain exactly 1 definition", 1,
                specimenFacade.getFieldObjectDefinition().size());
        assertEquals(
                "The remaining German definition should be 'Sehr interessant'",
                "Sehr interessant",
                specimenFacade.getFieldObjectDefinition(Language.GERMAN()));
        specimenFacade.removeFieldObjectDefinition(Language.GERMAN());
        assertEquals("There should remain no definition", 0,
                specimenFacade.getFieldObjectDefinition().size());
    }

    /**
     * Test method for
     * {@link eu.etaxonomy.cdm.api.facade.DerivedUnitFacade#addFieldObjectMedia(eu.etaxonomy.cdm.model.media.Media)}
     * .
     */
    @Test
    public void testAddGetHasRemoveFieldObjectMedia() {
        assertFalse("There should be no image gallery yet",
                specimenFacade.hasFieldObjectImageGallery());
        assertFalse("There should be no specimen image gallery either",
                specimenFacade.hasDerivedUnitImageGallery());

        List<Media> media = specimenFacade.getFieldObjectMedia();
        assertFalse("There should still not be an image gallery now",
                specimenFacade.hasFieldObjectImageGallery());
        assertEquals("There should be no media yet in the gallery", 0,
                media.size());

        Media media1 = Media.NewInstance();
        specimenFacade.addFieldObjectMedia(media1);
        assertEquals("There should be exactly one specimen media", 1,
                specimenFacade.getFieldObjectMedia().size());
        assertEquals("The only media should be media 1", media1,
                specimenFacade.getFieldObjectMedia().get(0));
        assertFalse(
                "There should still no specimen image gallery exist",
                specimenFacade.hasDerivedUnitImageGallery());

        Media media2 = Media.NewInstance();
        specimenFacade.addFieldObjectMedia(media2);
        assertEquals("There should be exactly 2 specimen media", 2,
                specimenFacade.getFieldObjectMedia().size());
        assertEquals("The first media should be media1", media1,
                specimenFacade.getFieldObjectMedia().get(0));
        assertEquals("The second media should be media2", media2,
                specimenFacade.getFieldObjectMedia().get(1));

        specimenFacade.removeFieldObjectMedia(media1);
        assertEquals("There should be exactly one specimen media", 1,
                specimenFacade.getFieldObjectMedia().size());
        assertEquals("The only media should be media2", media2,
                specimenFacade.getFieldObjectMedia().get(0));

        specimenFacade.removeFieldObjectMedia(media1);
        assertEquals("There should still be exactly one specimen media",
                1, specimenFacade.getFieldObjectMedia().size());

        specimenFacade.removeFieldObjectMedia(media2);
        assertEquals("There should remain no media in the gallery", 0,
                media.size());

    }

    /**
     * Test method for
     * {@link eu.etaxonomy.cdm.api.facade.DerivedUnitFacade#getEcology()}
     * .
     */
    @Test
    public void testGetSetEcology() {
        assertNotNull(
                "An empty ecology data should be created when calling getEcology()",
                specimenFacade.getEcologyAll());
        assertEquals(
                "An empty ecology data should be created when calling getEcology()",
                0, specimenFacade.getEcologyAll().size());
        specimenFacade.setEcology("Tres jolie ici", Language.FRENCH());
        assertEquals("Ecology data should exist for 1 language", 1,
                specimenFacade.getEcologyAll().size());
        assertEquals(
                "Ecology data should be 'Tres jolie ici' for French",
                "Tres jolie ici", specimenFacade.getEcology(Language.FRENCH()));
        assertNull(
                "Ecology data should be null for the default language",
                specimenFacade.getEcology());
        specimenFacade.setEcology("Nice here");
        assertEquals("Ecology data should exist for 2 languages", 2,
                specimenFacade.getEcologyAll().size());
        assertEquals("Ecology data should be 'Tres jolie ici'",
                "Tres jolie ici", specimenFacade.getEcology(Language.FRENCH()));
        assertEquals(
                "Ecology data should be 'Nice here' for the default language",
                "Nice here", specimenFacade.getEcology());
        assertEquals("Ecology data should be 'Nice here' for english",
                "Nice here", specimenFacade.getEcology());

        specimenFacade.setEcology("Vert et rouge", Language.FRENCH());
        assertEquals("Ecology data should exist for 2 languages", 2,
                specimenFacade.getEcologyAll().size());
        assertEquals("Ecology data should be 'Vert et rouge'",
                "Vert et rouge", specimenFacade.getEcology(Language.FRENCH()));
        assertEquals(
                "Ecology data should be 'Nice here' for the default language",
                "Nice here", specimenFacade.getEcology());

        specimenFacade.setEcology(null, Language.FRENCH());
        assertEquals("Ecology data should exist for 1 languages", 1,
                specimenFacade.getEcologyAll().size());
        assertEquals(
                "Ecology data should be 'Nice here' for the default language",
                "Nice here", specimenFacade.getEcology());
        assertNull("Ecology data should be 'null' for French",
                specimenFacade.getEcology(Language.FRENCH()));

        specimenFacade.removeEcology(null);
        assertEquals("There should be no ecology left", 0,
                specimenFacade.getEcologyAll().size());
        assertNull("Ecology data should be 'null' for default language",
                specimenFacade.getEcology());

    }

    /**
     * Test method for
     * {@link eu.etaxonomy.cdm.api.facade.DerivedUnitFacade#getPlantDescription()}
     * .
     */
    @Test
    public void testGetSetPlantDescription() {
        assertNotNull(
                "An empty plant description data should be created when calling getPlantDescriptionAll()",
                specimenFacade.getPlantDescriptionAll());
        assertEquals(
                "An empty plant description data should be created when calling getPlantDescription()",
                0, specimenFacade.getPlantDescriptionAll().size());
        specimenFacade.setPlantDescription("bleu", Language.FRENCH());
        assertEquals(
                "Plant description data should exist for 1 language", 1,
                specimenFacade.getPlantDescriptionAll().size());
        assertEquals(
                "Plant description data should be 'bleu' for French", "bleu",
                specimenFacade.getPlantDescription(Language.FRENCH()));
        assertNull(
                "Plant description data should be null for the default language",
                specimenFacade.getPlantDescription());
        specimenFacade.setPlantDescription("Nice here");
        assertEquals(
                "Plant description data should exist for 2 languages", 2,
                specimenFacade.getPlantDescriptionAll().size());
        assertEquals("Plant description data should be 'bleu'", "bleu",
                specimenFacade.getPlantDescription(Language.FRENCH()));
        assertEquals(
                "Plant description data should be 'Nice here' for the default language",
                "Nice here", specimenFacade.getPlantDescription());
        assertEquals(
                "Plant description data should be 'Nice here' for english",
                "Nice here", specimenFacade.getPlantDescription());

        specimenFacade.setPlantDescription("Vert et rouge", Language.FRENCH());
        assertEquals(
                "Plant description data should exist for 2 languages", 2,
                specimenFacade.getPlantDescriptionAll().size());
        assertEquals("Plant description data should be 'Vert et rouge'",
                "Vert et rouge",
                specimenFacade.getPlantDescription(Language.FRENCH()));
        assertEquals(
                "Plant description data should be 'Nice here' for the default language",
                "Nice here", specimenFacade.getPlantDescription());

        specimenFacade.setPlantDescription(null, Language.FRENCH());
        assertEquals(
                "Plant description data should exist for 1 languages", 1,
                specimenFacade.getPlantDescriptionAll().size());
        assertEquals(
                "Plant description data should be 'Nice here' for the default language",
                "Nice here", specimenFacade.getPlantDescription());
        assertNull("Plant description data should be 'null' for French",
                specimenFacade.getPlantDescription(Language.FRENCH()));

        // test interference with ecology
        specimenFacade.setEcology("Tres jolie ici", Language.FRENCH());
        assertEquals("Ecology data should exist for 1 language", 1,
                specimenFacade.getEcologyAll().size());
        assertEquals(
                "Ecology data should be 'Tres jolie ici' for French",
                "Tres jolie ici", specimenFacade.getEcology(Language.FRENCH()));
        assertNull(
                "Ecology data should be null for the default language",
                specimenFacade.getEcology());

        // repeat above test
        assertEquals(
                "Plant description data should exist for 1 languages", 1,
                specimenFacade.getPlantDescriptionAll().size());
        assertEquals(
                "Plant description data should be 'Nice here' for the default language",
                "Nice here", specimenFacade.getPlantDescription());
        assertNull("Plant description data should be 'null' for French",
                specimenFacade.getPlantDescription(Language.FRENCH()));

        specimenFacade.removePlantDescription(null);
        assertEquals("There should be no plant description left", 0,
                specimenFacade.getPlantDescriptionAll().size());
        assertNull(
                "Plant description data should be 'null' for default language",
                specimenFacade.getPlantDescription());

    }

    /**
     * Test method for
     * {@link eu.etaxonomy.cdm.api.facade.DerivedUnitFacade#getLifeform()}
     * .
     */
    @Test
    public void testGetSetLifeform() {
        Feature lifeform = Feature.LIFEFORM();
        assertNotNull("Lifeform must exist in testdata",lifeform);
        assertNotNull(
                "An empty life-form data should be created when calling getLifeformAll()",
                specimenFacade.getLifeformAll());
        assertEquals(
                "An empty life-form data should be created when calling getLifeform()",
                0, specimenFacade.getLifeformAll().size());
        specimenFacade.setLifeform("grande", Language.FRENCH());
        assertEquals(
                "Life-form data should exist for 1 language", 1,
                specimenFacade.getLifeformAll().size());
        assertEquals(
                "Life-form data should be 'grande' for French", "grande",
                specimenFacade.getLifeform(Language.FRENCH()));
        assertNull(
                "Lifeform data should be null for the default language",
                specimenFacade.getLifeform());
        specimenFacade.setLifeform("Very big");
        assertEquals(
                "Life-form data should exist for 2 languages", 2,
                specimenFacade.getLifeformAll().size());
        assertEquals("French life-form data should be 'grande'", "grande",
                specimenFacade.getLifeform(Language.FRENCH()));
        assertEquals(
                "Default language life-form data should be 'Very big' for the default language",
                "Very big", specimenFacade.getLifeform());
        assertEquals(
                "Life-form data should be 'Very big' for English",
                "Very big", specimenFacade.getLifeform());

        specimenFacade.setLifeform("Petite", Language.FRENCH());
        assertEquals(
                "Plant description data should exist for 2 languages", 2,
                specimenFacade.getLifeformAll().size());
        assertEquals("Life-form data should be 'Petite'",
                "Petite",
                specimenFacade.getLifeform(Language.FRENCH()));
        assertEquals(
                "Plant description data should be 'Very big' for the default language",
                "Very big", specimenFacade.getLifeform());

        specimenFacade.setLifeform(null, Language.FRENCH());
        assertEquals(
                "Life-form data should exist for 1 languages", 1,
                specimenFacade.getLifeformAll().size());
        assertEquals(
                "Life-form data should be 'Very big' for the default language",
                "Very big", specimenFacade.getLifeform());
        assertNull("Life-form data should be 'null' for French",
                specimenFacade.getLifeform(Language.FRENCH()));

        // test interference with ecology
        specimenFacade.setEcology("Tres jolie ici", Language.FRENCH());
        assertEquals("Ecology data should exist for 1 language", 1,
                specimenFacade.getEcologyAll().size());
        assertEquals(
                "Ecology data should be 'Tres jolie ici' for French",
                "Tres jolie ici", specimenFacade.getEcology(Language.FRENCH()));
        assertNull(
                "Ecology data should be null for the default language",
                specimenFacade.getEcology());

        // repeat above test
        assertEquals(
                "Life-form data should exist for 1 languages", 1,
                specimenFacade.getLifeformAll().size());
        assertEquals(
                "Lifeform data should be 'Very big' for the default language",
                "Very big", specimenFacade.getLifeform());
        assertNull("Life-form data should be 'null' for French",
                specimenFacade.getLifeform(Language.FRENCH()));

        specimenFacade.removeLifeform(null);
        assertEquals("There should be no life-form left", 0,
                specimenFacade.getLifeformAll().size());
        assertNull(
                "Life-form data should be 'null' for default language",
                specimenFacade.getLifeform());
    }

    /**
     * Test method for
     * {@link eu.etaxonomy.cdm.api.facade.DerivedUnitFacade#getFieldNumber()}.
     */
    @Test
    public void testGetSetFieldNumber() {
        assertEquals("Field number must be same", fieldNumber,
                specimenFacade.getFieldNumber());
        specimenFacade.setFieldNumber("564AB");
        assertEquals("New field number must be '564AB'", "564AB",
                specimenFacade.getFieldNumber());
        // empty facade tests
        assertNull("Empty facace must not have any field value",
                emptyFacade.getFieldNumber());
        emptyFacade.setFieldNumber("1256A");
        assertNotNull(
                "Field unit must exist if field number exists",
                emptyFacade.getFieldUnit(false));
        FieldUnit specimenFieldUnit = (FieldUnit) emptyFacade
                .innerDerivedUnit().getDerivedFrom().getOriginals().iterator()
                .next();
        assertSame(
                "Field unit of facade and of specimen must be the same",
                specimenFieldUnit,
                emptyFacade.getFieldUnit(false));
        assertEquals("1256A", emptyFacade.getFieldNumber());
    }

    /**
     * Test method for
     * {@link eu.etaxonomy.cdm.api.facade.DerivedUnitFacade#getFieldNotes()}.
     */
    @Test
    public void testGetSetFieldNotes() {
        assertEquals("Field notes must be same", fieldNotes,
                specimenFacade.getFieldNotes());
        specimenFacade.setFieldNotes("A completely new info");
        assertEquals("New field note must be 'A completely new info'",
                "A completely new info", specimenFacade.getFieldNotes());
    }

    /**
     * Test method for
     * {@link eu.etaxonomy.cdm.api.facade.DerivedUnitFacade#setGatheringEvent(eu.etaxonomy.cdm.model.occurrence.GatheringEvent)}
     * .
     */
    @Test
    @Ignore // #######DerivationEvent ---------------------------------------- 1
    public void testSetGatheringEvent() {
        GatheringEvent newGatheringEvent = GatheringEvent.NewInstance();
        newGatheringEvent.setDistanceToGround(43.0);
        assertFalse("The initial distance to ground should not be 43",
                specimenFacade.getDistanceToGround() == 43.0);
        specimenFacade.setGatheringEvent(newGatheringEvent);
        assertTrue("The final distance to ground should be 43",
                specimenFacade.getDistanceToGround() == 43.0);
        assertSame(
                "The new gathering event should be 'newGatheringEvent'",
                newGatheringEvent, specimenFacade.innerGatheringEvent());
    }

    /**
     * Test method for
     * {@link eu.etaxonomy.cdm.api.facade.DerivedUnitFacade#innerGatheringEvent()}
     * .
     */
    @Test
    public void testGetGatheringEvent() {
        assertNotNull("Gathering event must not be null",
                specimenFacade.innerGatheringEvent());
        assertEquals(
                "Gathering event must be field unit's gathering event",
                specimenFacade.innerFieldUnit().getGatheringEvent(),
                specimenFacade.innerGatheringEvent());
    }

    /**
     * Test method for
     * {@link eu.etaxonomy.cdm.api.facade.DerivedUnitFacade#getIndividualCount()}
     * .
     */
    @Test
    public void testGetSetIndividualCount() {
        assertEquals("Individual count must be same", individualCount,
                specimenFacade.getIndividualCount());
        specimenFacade.setIndividualCount(4);
        assertEquals("New individual count must be '4'",
                Integer.valueOf(4), specimenFacade.getIndividualCount());

    }

    /**
     * Test method for
     * {@link eu.etaxonomy.cdm.api.facade.DerivedUnitFacade#getLifeStage()}.
     */
    @Test
    public void testGetSetLifeStage() {
        assertNotNull("Life stage must not be null", specimenFacade.getLifeStage());
        assertEquals("Life stage must be same", lifeStage, specimenFacade.getLifeStage());
        specimenFacade.setLifeStage(null);
        assertNull("Life stage must be null", specimenFacade.getLifeStage());
        //don't create if null
        DerivedUnitFacade facade = emptyFacade();
        emptyFacade().setLifeStage(null);
        assertNull(facade.innerFieldUnit());
    }

    /**
     * Test method for
     * {@link eu.etaxonomy.cdm.api.facade.DerivedUnitFacade#getSex()}.
     */
    @Test
    public void testGetSetSex() {
        assertNotNull("Sex must not be null", specimenFacade.getSex());
        assertEquals("Sex must be same", sex, specimenFacade.getSex());
        specimenFacade.setSex(null);
        assertNull("Sex must be null", specimenFacade.getSex());
        //don't create if null
        DerivedUnitFacade facade = emptyFacade();
        emptyFacade().setSex(null);
        assertNull(facade.innerFieldUnit());
    }


    /**
     * Test method for
     * {@link eu.etaxonomy.cdm.api.facade.DerivedUnitFacade#getSex()}.
     */
    @Test
    public void testGetSetKindOfUnit() {
        assertNotNull("Kind-of-unit must not be null", specimenFacade.getKindOfUnit());
        assertEquals("Kind-of-unit must be same", kindOfUnit, specimenFacade.getKindOfUnit());
        specimenFacade.setKindOfUnit(null);
        assertNull("Kind-of-unit must be null", specimenFacade.getKindOfUnit());
        DerivedUnitFacade facade = emptyFacade();
        facade.setKindOfUnit(null);
        assertNull(facade.innerFieldUnit());
    }

    /**
     * Test method for
     * {@link eu.etaxonomy.cdm.api.facade.DerivedUnitFacade#getLocality()}.
     */
    @Test
    public void testGetSetLocality() {
        assertEquals("Locality must be same", locality,
                specimenFacade.getLocality());
        specimenFacade.setLocality("A completely new place", Language.FRENCH());
        assertEquals("New locality must be 'A completely new place'",
                "A completely new place", specimenFacade.getLocalityText());
        assertEquals("New locality language must be French",
                Language.FRENCH(), specimenFacade.getLocalityLanguage());
        specimenFacade.setLocality("Another place");
        assertEquals("New locality must be 'Another place'",
                "Another place", specimenFacade.getLocalityText());
        assertEquals("New locality language must be default",
                Language.DEFAULT(), specimenFacade.getLocalityLanguage());
    }

    /**
     * Test method for
     * {@link eu.etaxonomy.cdm.api.facade.DerivedUnitFacade#addDerivedUnitDefinition(java.lang.String, eu.etaxonomy.cdm.model.common.Language)}
     * .
     */
    @Test
    public void testAddGetRemoveSpecimenDefinition() {
        assertEquals("There should be no definition yet", 0,
                specimenFacade.getDerivedUnitDefinitions().size());
        specimenFacade.addDerivedUnitDefinition("Tres interesant",
                Language.FRENCH());
        assertEquals("There should be exactly one definition", 1,
                specimenFacade.getDerivedUnitDefinitions().size());
        assertEquals(
                "The French definition should be 'Tres interesant'",
                "Tres interesant", specimenFacade.getDerivedUnitDefinitions()
                        .get(Language.FRENCH()).getText());
        assertEquals(
                "The French definition should be 'Tres interesant'",
                "Tres interesant",
                specimenFacade.getDerivedUnitDefinition(Language.FRENCH()));
        specimenFacade.addDerivedUnitDefinition("Sehr interessant",
                Language.GERMAN());
        assertEquals("There should be exactly 2 definition", 2,
                specimenFacade.getDerivedUnitDefinitions().size());
        specimenFacade.removeDerivedUnitDefinition(Language.FRENCH());
		assertEquals("There should remain exactly 1 definition", 1,
                specimenFacade.getDerivedUnitDefinitions().size());
        assertEquals(
                "The remaining German definition should be 'Sehr interessant'",
                "Sehr interessant",
		        specimenFacade.getDerivedUnitDefinition(Language.GERMAN()));
		specimenFacade.removeDerivedUnitDefinition(Language.GERMAN());
		assertEquals("There should remain no definition", 0,
                specimenFacade.getDerivedUnitDefinitions().size());
    }

    /**
     * Test method for
     * {@link eu.etaxonomy.cdm.api.facade.DerivedUnitFacade#addDetermination(eu.etaxonomy.cdm.model.occurrence.DeterminationEvent)}
     * .
     */
    @Test
    public void testAddGetRemoveDetermination() {
        assertEquals("There should be no determination yet", 0,
                specimenFacade.getDeterminations().size());
        DeterminationEvent determinationEvent1 = DeterminationEvent
                .NewInstance();
        specimenFacade.addDetermination(determinationEvent1);
        assertEquals("There should be exactly one determination", 1,
                specimenFacade.getDeterminations().size());
        assertEquals("The only determination should be determination 1",
                determinationEvent1, specimenFacade.getDeterminations()
                        .iterator().next());

        DeterminationEvent determinationEvent2 = DeterminationEvent
                .NewInstance();
        specimenFacade.addDetermination(determinationEvent2);
        assertEquals("There should be exactly 2 determinations", 2,
                specimenFacade.getDeterminations().size());
        specimenFacade.removeDetermination(determinationEvent1);

        assertEquals("There should remain exactly 1 determination", 1,
                specimenFacade.getDeterminations().size());
        assertEquals(
                "The remaining determinations should be determination 2",
                determinationEvent2, specimenFacade.getDeterminations()
                        .iterator().next());

        specimenFacade.removeDetermination(determinationEvent1);
        assertEquals("There should remain exactly 1 determination", 1,
                specimenFacade.getDeterminations().size());

        specimenFacade.removeDetermination(determinationEvent2);
        assertEquals("There should remain no definition", 0,
                specimenFacade.getDerivedUnitDefinitions().size());

    }


    /**
     * Test method for
     * {@link eu.etaxonomy.cdm.api.facade.DerivedUnitFacade#addDetermination(eu.etaxonomy.cdm.model.occurrence.DeterminationEvent)}
     * .
     */
    @Test
    public void testPreferredOtherDeterminations() {
        assertEquals("There should be no determination yet", 0,
                specimenFacade.getDeterminations().size());
        DeterminationEvent determinationEvent1 = DeterminationEvent.NewInstance();
        specimenFacade.setPreferredDetermination(determinationEvent1);

        assertEquals("There should be exactly one determination", 1,
                specimenFacade.getDeterminations().size());
        assertEquals("The only determination should be determination 1",
                determinationEvent1, specimenFacade.getDeterminations()
                        .iterator().next());
        assertEquals("determination 1 should be the preferred determination",
                determinationEvent1, specimenFacade.getPreferredDetermination());
        assertEquals("There should be no 'non preferred' determination", 0,
                specimenFacade.getOtherDeterminations().size());



        DeterminationEvent determinationEvent2 = DeterminationEvent.NewInstance();
        specimenFacade.addDetermination(determinationEvent2);
        assertEquals("There should be exactly 2 determinations", 2,
                specimenFacade.getDeterminations().size());
        assertEquals("determination 1 should be the preferred determination",
                determinationEvent1, specimenFacade.getPreferredDetermination());
        assertEquals("There should be one 'non preferred' determination", 1,
                specimenFacade.getOtherDeterminations().size());
        assertEquals("The only 'non preferred' determination should be determination 2",
                determinationEvent2, specimenFacade.getOtherDeterminations().iterator().next());


        DeterminationEvent determinationEvent3 = DeterminationEvent.NewInstance();
        specimenFacade.setPreferredDetermination(determinationEvent3);
        assertEquals("There should be exactly 3 determinations", 3,
                specimenFacade.getDeterminations().size());
        assertEquals("determination 3 should be the preferred determination",
                determinationEvent3, specimenFacade.getPreferredDetermination());
        assertEquals("There should be 2 'non preferred' determination", 2,
                specimenFacade.getOtherDeterminations().size());
        assertTrue("determination 1 should be contained in the set of 'non preferred' determinations",
                specimenFacade.getOtherDeterminations().contains(determinationEvent1));
        assertTrue("determination 2 should be contained in the set of 'non preferred' determinations",
                specimenFacade.getOtherDeterminations().contains(determinationEvent2));

    }

    /**
     * Test method for
     * {@link eu.etaxonomy.cdm.api.facade.DerivedUnitFacade#addDerivedUnitMedia(eu.etaxonomy.cdm.model.media.Media)}
     * .
     */
    @Test
    public void testAddGetHasRemoveSpecimenMedia() {
        assertFalse("There should be no image gallery yet",
                specimenFacade.hasDerivedUnitImageGallery());
        assertFalse(
                "There should be also no field object image gallery yet",
                specimenFacade.hasFieldObjectImageGallery());

        List<Media> media = specimenFacade.getDerivedUnitMedia();
        assertFalse(
                "There should still not be an empty image gallery now",
                specimenFacade.hasDerivedUnitImageGallery());
        assertEquals("There should be no media yet in the gallery", 0,
                media.size());

        Media media1 = Media.NewInstance();
        specimenFacade.addDerivedUnitMedia(media1);
		assertEquals("There should be exactly one specimen media", 1,
                specimenFacade.getDerivedUnitMedia().size());
        assertEquals("The only media should be media 1", media1,
                specimenFacade.getDerivedUnitMedia().get(0));
        assertFalse(
                "There should be still no field object image gallery",
                specimenFacade.hasFieldObjectImageGallery());

        Media media2 = Media.NewInstance();
        specimenFacade.addDerivedUnitMedia(media2);
		assertEquals("There should be exactly 2 specimen media", 2,
                specimenFacade.getDerivedUnitMedia().size());
        assertEquals("The first media should be media1", media1,
                specimenFacade.getDerivedUnitMedia().get(0));
        assertEquals("The second media should be media2", media2,
                specimenFacade.getDerivedUnitMedia().get(1));

        specimenFacade.removeDerivedUnitMedia(media1);
        assertEquals("There should be exactly one specimen media", 1,
                specimenFacade.getDerivedUnitMedia().size());
        assertEquals("The only media should be media2", media2,
                specimenFacade.getDerivedUnitMedia().get(0));

        specimenFacade.removeDerivedUnitMedia(media1);
        assertEquals("There should still be exactly one specimen media",
                1, specimenFacade.getDerivedUnitMedia().size());

        specimenFacade.removeDerivedUnitMedia(media2);
        assertEquals("There should remain no media in the gallery", 0,
                media.size());
    }

    /**
     * Test method for
     * {@link eu.etaxonomy.cdm.api.facade.DerivedUnitFacade#getAccessionNumber()}
     * .
     */
    @Test
    public void testGetSetAccessionNumber() {
        assertEquals("Accession number must be same", accessionNumber,
                specimenFacade.getAccessionNumber());
        specimenFacade.setAccessionNumber("A12345693");
		assertEquals("New accession number must be 'A12345693'",
                "A12345693", specimenFacade.getAccessionNumber());
    }


    /**
     * Test method for
     * {@link eu.etaxonomy.cdm.api.facade.DerivedUnitFacade#getCatalogNumber()}.
     */
    @Test
    public void testGetCatalogNumber() {
        assertEquals("Catalog number must be same", catalogNumber,
                specimenFacade.getCatalogNumber());
        specimenFacade.setCatalogNumber("B12345693");
		assertEquals("New catalog number must be 'B12345693'",
                "B12345693", specimenFacade.getCatalogNumber());
    }

    /**
     * Test method for
     * {@link eu.etaxonomy.cdm.api.facade.DerivedUnitFacade#getPreservation()}.
     */
    @Test
    public void testGetPreservation() {
        try {
            assertNotNull("Preservation method must not be null",
                    specimenFacade.getPreservationMethod());
            assertEquals("Preservation method must be same",
                    preservationMethod, specimenFacade.getPreservationMethod());
            specimenFacade.setPreservationMethod(null);
            assertNull("Preservation method must be null",
                    specimenFacade.getPreservationMethod());
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
        assertEquals("Original label info must be same", originalLabelInfo,
                specimenFacade.getOriginalLabelInfo());
        specimenFacade.setOriginalLabelInfo("OrigLabel Info xxx");
        assertEquals("New accession number must be 'OrigLabel Info xxx'",
                "OrigLabel Info xxx", specimenFacade.getOriginalLabelInfo());
    }

    @Test
    public void testGetSetStableUri() {
        assertEquals("Preferred stable URI must be same", stableUri,
                specimenFacade.getPreferredStableUri());
        URI newURI = URI.create("http://www.fgh.ij");
        specimenFacade.setPreferredStableUri(newURI);
        assertEquals("New preferred stable must be '" + newURI.toString() + "'",
                newURI, specimenFacade.getPreferredStableUri());
    }



    /**
     * Test method for
     * {@link eu.etaxonomy.cdm.api.facade.DerivedUnitFacade#getStoredUnder()}.
     */
    @Test
    public void testGetStoredUnder() {
        assertNotNull("Stored under name must not be null",
                specimenFacade.getStoredUnder());
        assertEquals("Stored under name must be same", taxonName,
                specimenFacade.getStoredUnder());
        specimenFacade.setStoredUnder(null);
        assertNull("Stored under name must be null",
                specimenFacade.getStoredUnder());
    }

//	/**
//	 * Test method for
//	 * {@link eu.etaxonomy.cdm.api.facade.DerivedUnitFacade#getCollectorsNumber()}
//	 * .
//	 */
//	@Test
//	public void testGetSetCollectorsNumber() {
//		assertEquals("Collectors number must be same", collectorsNumber,
//				specimenFacade.getCollectorsNumber());
//		specimenFacade.setCollectorsNumber("C12345693");
//		assertEquals("New collectors number must be 'C12345693'",
//				"C12345693", specimenFacade.getCollectorsNumber());
//	}

    /**
     * Test method for
     * {@link eu.etaxonomy.cdm.api.facade.DerivedUnitFacade#getTitleCache()}.
     */
    @Test
    public void testGetTitleCache() {
        assertNotNull(
                "The title cache should never return null if not protected",
                specimenFacade.getTitleCache());
        specimenFacade.setTitleCache(null, false);
        assertNotNull(
                "The title cache should never return null if not protected",
                specimenFacade.getTitleCache());
    }

    /**
     * Test method for
     * {@link eu.etaxonomy.cdm.api.facade.DerivedUnitFacade#setTitleCache(java.lang.String)}
     * .
     */
    @Test
    public void testSetTitleCache() {
        String testTitle = "Absdwk aksjlf";
        specimenFacade.setTitleCache(testTitle, true);
        assertEquals(
                "Protected title cache should returns the test title",
                testTitle, specimenFacade.getTitleCache());
        specimenFacade.setTitleCache(testTitle, false);
        assertFalse(
                "Unprotected title cache should not return the test title",
                testTitle.equals(specimenFacade.getTitleCache()));
    }

    /**
     * Test method for
     * {@link eu.etaxonomy.cdm.api.facade.DerivedUnitFacade#innerDerivedUnit()}.
     */
    @Test
    public void testGetSpecimen() {
        assertEquals("Specimen must be same", specimen,
                specimenFacade.innerDerivedUnit());
    }

    /**
     * Test method for
     * {@link eu.etaxonomy.cdm.api.facade.DerivedUnitFacade#getCollection()}.
     */
    @Test
    public void testGetSetCollection() {
        assertNotNull("Collection must not be null",
                specimenFacade.getCollection());
        assertEquals("Collection must be same", collection,
                specimenFacade.getCollection());
        specimenFacade.setCollection(null);
        assertNull("Collection must be null",
                specimenFacade.getCollection());
    }

    @Test
    public void testAddGetRemoveSource() {
        assertEquals("No sources should exist yet", 0, specimenFacade.getSources().size());

        Reference reference = ReferenceFactory.newBook();
        IdentifiableSource source1 = specimenFacade.addSource(OriginalSourceType.PrimaryTaxonomicSource, reference, "54", "myName");
        assertEquals("One source should exist now", 1, specimenFacade.getSources().size());
        IdentifiableSource source2 = IdentifiableSource.NewDataImportInstance("1", "myTable");
        specimenFacade.addSource(source2);
        assertEquals("One source should exist now", 2, specimenFacade.getSources().size());
        specimenFacade.removeSource(source1);
        assertEquals("One source should exist now", 1, specimenFacade.getSources().size());
        Reference reference2 = ReferenceFactory.newJournal();
        IdentifiableSource sourceNotUsed = specimenFacade.addSource(OriginalSourceType.PrimaryTaxonomicSource, reference2,null, null);
        specimenFacade.removeSource(sourceNotUsed);
        assertEquals("One source should still exist", 1, specimenFacade.getSources().size());
        assertEquals("1", specimenFacade.getSources().iterator().next().getIdInSource());
        specimenFacade.removeSource(source2);
        assertEquals("No sources should exist anymore", 0,specimenFacade.getSources().size());
    }

    @Test
    public void testAddGetRemoveDuplicate() {
        assertEquals("No duplicates should be available yet", 0,specimenFacade.getDuplicates().size());
        DerivedUnit newSpecimen1 = DerivedUnit.NewPreservedSpecimenInstance();
        specimenFacade.addDuplicate(newSpecimen1);
        assertEquals("There should be 1 duplicate now", 1,specimenFacade.getDuplicates().size());
        DerivedUnit newSpecimen2 = DerivedUnit.NewPreservedSpecimenInstance();
        DerivationEvent newDerivationEvent = DerivationEvent.NewInstance(DerivationEventType.ACCESSIONING());
        newSpecimen2.setDerivedFrom(newDerivationEvent);
        assertSame(
                "The derivation event should be 'newDerivationEvent'",
                newDerivationEvent, newSpecimen2.getDerivedFrom());
       	specimenFacade.addDuplicate(newSpecimen2);
        assertEquals("There should be 2 duplicates now", 2, specimenFacade.getDuplicates().size());
        assertNotSame(
                "The derivation event should not be 'newDerivationEvent' anymore",
                newDerivationEvent, newSpecimen2.getDerivedFrom());
        assertSame(
                "The derivation event should not be the facades derivation event",
                derivationEvent, newSpecimen2.getDerivedFrom());
        specimenFacade.removeDuplicate(newSpecimen1);
        assertEquals("There should be 1 duplicate now", 1,
                specimenFacade.getDuplicates().size());
        assertSame("The only duplicate should be 'newSpecimen2' now",
                newSpecimen2, specimenFacade.getDuplicates().iterator().next());
       	specimenFacade.addDuplicate(specimenFacade.innerDerivedUnit());
        assertEquals(
                "There should be still 1 duplicate because the facade specimen is not a duplicate",
                1, specimenFacade.getDuplicates().size());

        Collection newCollection = Collection.NewInstance();
        String catalogNumber = "1234890";
        String accessionNumber = "345345";
        TaxonName storedUnder = TaxonNameFactory.NewBotanicalInstance(Rank.SPECIES());
        PreservationMethod method = PreservationMethod.NewInstance();
        DerivedUnit duplicateSpecimen = specimenFacade.addDuplicate(newCollection,
				catalogNumber, accessionNumber, storedUnder,
			    method);
        assertEquals("There should be 2 duplicates now", 2,
                specimenFacade.getDuplicates().size());
        specimenFacade.removeDuplicate(newSpecimen2);
		assertEquals("There should be 1 duplicates now", 1,
                specimenFacade.getDuplicates().size());
        Collection sameCollection = specimenFacade.getDuplicates().iterator().next().getCollection();
        assertSame("Collections should be same", newCollection, sameCollection);
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
        assertSame(
                "Gathering event should derive from the derivation line",
                existingGatheringEvent, specimenFacade.innerGatheringEvent());
        assertEquals(
                "Mediasize should be 0. Only Imagegallery media are supported",
                0, specimenFacade.getFieldObjectMedia().size());
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
        assertNull("Specimen facade should not be initialized",
                specimenFacade);
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
//        assertNull("Specimen facade should not be initialized",
//                specimenFacade);
//    }

    @Test // #######DerivationEvent
    public void testEventPropagation() {
        specimenFacade.setDistanceToGround(24.0);

    }

    // @Ignore // set to ignore because I did not want to check knowingly
    // failing tests. Remove @Ignore when this is fixed
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
     * See https://dev.e-taxonomy.eu/trac/ticket/2426
     * This test doesn't handle the above issue yet as it doesn't fire events as
     * expected (at least it does not reproduce the behaviour in the Taxonomic Editor).
     * In the meanwhile the property change framework for the facade has been changed
     * so the original problem may have disappeared.
     *
     */
    @Test
    @DataSets({
        @DataSet(loadStrategy=CleanSweepInsertLoadStrategy.class, value="/eu/etaxonomy/cdm/database/ClearDB_with_Terms_DataSet.xml"),
        @DataSet(value="/eu/etaxonomy/cdm/database/TermsDataSet-with_auditing_info.xml")
    })
   // @Ignore
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
        assertEquals("baseUnit is incorrect", specimen, facade.baseUnit());

        facade = DerivedUnitFacade.NewInstance(SpecimenOrObservationType.FieldUnit, fieldUnit);
        assertEquals("baseUnit is incorrect", fieldUnit, facade.baseUnit());

        facade = DerivedUnitFacade.NewInstance(specimen);
        facade.getFieldUnit(true);
        assertEquals("baseUnit is incorrect", specimen, facade.baseUnit());
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
