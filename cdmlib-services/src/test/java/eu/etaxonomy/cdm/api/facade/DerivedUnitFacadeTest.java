// $Id$
/**
 * Copyright (C) 2009 EDIT
 * European Distributed Institute of Taxonomy 
 * http://www.e-taxonomy.eu
 * 
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.api.facade;

import java.lang.reflect.Field;
import java.net.URI;
import java.text.ParseException;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.dbunit.annotation.ExpectedDataSet;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.api.facade.DerivedUnitFacade.DerivedUnitType;
import eu.etaxonomy.cdm.api.service.IOccurrenceService;
import eu.etaxonomy.cdm.api.service.ITermService;
import eu.etaxonomy.cdm.model.agent.AgentBase;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.common.IdentifiableSource;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.LanguageString;
import eu.etaxonomy.cdm.model.common.TimePeriod;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.Sex;
import eu.etaxonomy.cdm.model.description.SpecimenDescription;
import eu.etaxonomy.cdm.model.description.Stage;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.location.Point;
import eu.etaxonomy.cdm.model.location.ReferenceSystem;
import eu.etaxonomy.cdm.model.location.TdwgArea;
import eu.etaxonomy.cdm.model.location.WaterbodyOrCountry;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.occurrence.Collection;
import eu.etaxonomy.cdm.model.occurrence.DerivationEvent;
import eu.etaxonomy.cdm.model.occurrence.DeterminationEvent;
import eu.etaxonomy.cdm.model.occurrence.FieldObservation;
import eu.etaxonomy.cdm.model.occurrence.GatheringEvent;
import eu.etaxonomy.cdm.model.occurrence.PreservationMethod;
import eu.etaxonomy.cdm.model.occurrence.Specimen;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.test.integration.CdmTransactionalIntegrationTest;

/**
 * @author a.mueller
 * @date 17.05.2010
 * 
 */
public class DerivedUnitFacadeTest extends CdmTransactionalIntegrationTest {
	private static final Logger logger = Logger
			.getLogger(DerivedUnitFacadeTest.class);

	@SpringBeanByType
	private IOccurrenceService service;

	@SpringBeanByType
	private ITermService termService;

	Specimen specimen;
	DerivationEvent derivationEvent;
	FieldObservation fieldObservation;
	GatheringEvent gatheringEvent;
	Integer absoluteElevation = 10;
	Integer absoluteElevationError = 2;
	AgentBase collector = Team.NewInstance();
	String collectingMethod = "Collection Method";
	Integer distanceToGround = 22;
	Integer distanceToSurface = 50;
	ReferenceSystem referenceSystem = ReferenceSystem.WGS84();
	Point exactLocation = Point.NewInstance(12.3, 10.567, referenceSystem, 22);
	String gatheringEventDescription = "A nice gathering description";
	TimePeriod gatheringPeriod = TimePeriod.NewInstance(1888, 1889);

	String fieldNumber = "15p23B";
	String fieldNotes = "such a beautiful specimen";

	Integer individualCount = 1;
	Stage lifeStage = Stage.NewInstance("A wonderful stage", "stage", "st");
	Sex sex = Sex.NewInstance("FemaleMale", "FM", "FM");
	LanguageString locality = LanguageString.NewInstance("My locality",
			Language.DEFAULT());

	String accessionNumber = "888462535";
	String catalogNumber = "UU879873590";
	TaxonNameBase taxonName = BotanicalName.NewInstance(Rank.GENUS(), "Abies",
			null, null, null, null, null, null, null);
	String collectorsNumber = "234589913A34";
	Collection collection = Collection.NewInstance();
	PreservationMethod preservationMethod = PreservationMethod.NewInstance(
			"my prservation", null, null);

	DerivedUnitFacade specimenFacade;

	Specimen collectionSpecimen;
	GatheringEvent existingGatheringEvent;
	DerivationEvent firstDerivationEvent;
	FieldObservation firstFieldObject;
	Media media1 = Media.NewInstance();

	DerivedUnitFacade emptyFacade;

	NamedArea country = WaterbodyOrCountry.GERMANY();

	// ****************************** SET UP
	// *****************************************/

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		// new DefaultTermInitializer().initialize();
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		specimen = Specimen.NewInstance();

		derivationEvent = DerivationEvent.NewInstance();
		specimen.setDerivedFrom(derivationEvent);
		fieldObservation = FieldObservation.NewInstance();
		fieldObservation.addDerivationEvent(derivationEvent);
		gatheringEvent = GatheringEvent.NewInstance();
		fieldObservation.setGatheringEvent(gatheringEvent);
		gatheringEvent.setAbsoluteElevation(absoluteElevation);
		gatheringEvent.setAbsoluteElevationError(absoluteElevationError);
		gatheringEvent.setActor(collector);
		gatheringEvent.setCollectingMethod(collectingMethod);
		gatheringEvent.setDistanceToGround(distanceToGround);
		gatheringEvent.setDistanceToWaterSurface(distanceToSurface);
		gatheringEvent.setExactLocation(exactLocation);
		gatheringEvent.setDescription(gatheringEventDescription);
		gatheringEvent.setCountry(country);

		gatheringEvent.setTimeperiod(gatheringPeriod);
		gatheringEvent.setLocality(locality);

		fieldObservation.setFieldNumber(fieldNumber);
		fieldObservation.setFieldNotes(fieldNotes);
		fieldObservation.setIndividualCount(individualCount);
		fieldObservation.setSex(sex);
		fieldObservation.setLifeStage(lifeStage);

		specimen.setAccessionNumber(accessionNumber);
		specimen.setCatalogNumber(catalogNumber);
		specimen.setStoredUnder(taxonName);
		specimen.setCollectorsNumber(collectorsNumber);
		specimen.setCollection(collection);
		specimen.setPreservation(preservationMethod);

		specimenFacade = DerivedUnitFacade.NewInstance(specimen);

		// existing specimen with 2 derivation events in line
		collectionSpecimen = Specimen.NewInstance();
		Specimen middleSpecimen = Specimen.NewInstance();
		firstFieldObject = FieldObservation.NewInstance();

		DerivationEvent lastDerivationEvent = DerivationEvent.NewInstance();
		DerivationEvent middleDerivationEvent = DerivationEvent.NewInstance();
		firstDerivationEvent = DerivationEvent.NewInstance();

		collectionSpecimen.setDerivedFrom(lastDerivationEvent);

		lastDerivationEvent.addOriginal(middleSpecimen);
		middleSpecimen.setDerivedFrom(firstDerivationEvent);
		firstDerivationEvent.addOriginal(firstFieldObject);
		existingGatheringEvent = GatheringEvent.NewInstance();
		firstFieldObject.setGatheringEvent(existingGatheringEvent);

		// empty facade
		emptyFacade = DerivedUnitFacade.NewInstance(DerivedUnitType.Specimen);

	}

	// ****************************** TESTS
	// *****************************************/

	@Test
	@DataSet("DerivedUnitFacadeTest.testSetFieldObjectImageGallery.xml")
	@ExpectedDataSet
	public void testSetFieldObjectImageGallery() {
		UUID imageFeatureUuid = Feature.IMAGE().getUuid();
		Feature imageFeature = (Feature) termService.find(imageFeatureUuid);

		DerivedUnitFacade facade = DerivedUnitFacade
				.NewInstance(DerivedUnitType.Specimen);
		facade.setFieldNumber("12345");
		Media media = Media.NewInstance(URI.create("www.abc.de"), 200, null,
				"jpeg");

		try {
			SpecimenDescription imageGallery = SpecimenDescription
					.NewInstance();
			imageGallery.addDescribedSpecimenOrObservation(facade
					.innerFieldObservation());
			imageGallery.setImageGallery(true);
			TextData textData = TextData.NewInstance();
			textData.setFeature(imageFeature);
			imageGallery.addElement(textData);
			textData.addMedia(media);
			facade.setFieldObjectImageGallery(imageGallery);

		} catch (DerivedUnitFacadeNotSupportedException e1) {
			e1.printStackTrace();
			Assert.fail(e1.getLocalizedMessage());
		}
		this.service.save(facade.innerDerivedUnit());

		// setComplete(); endTransaction();
		// try {if (true){printDataSet(System.out, new
		// String[]{"HIBERNATE_SEQUENCES","SPECIMENOROBSERVATIONBASE","SPECIMENOROBSERVATIONBASE_DERIVATIONEVENT"
		// ,"DERIVATIONEVENT",
		// "DESCRIPTIONBASE","DESCRIPTIONELEMENTBASE","DESCRIPTIONELEMENTBASE_MEDIA","DESCRIPTIONBASE_SPECIMENOROBSERVATIONBASE",
		// "MEDIA", "MEDIAREPRESENTATION","MEDIAREPRESENTATIONPART"});}
		// } catch(Exception e) { logger.warn(e);}

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
		DerivedUnitFacade facade = DerivedUnitFacade
				.NewInstance(DerivedUnitType.Specimen);
		Media media = Media.NewInstance(URI.create("www.derivedUnitImage.de"),
				200, null, "png");

		try {
			SpecimenDescription imageGallery = SpecimenDescription
					.NewInstance();
			imageGallery.addDescribedSpecimenOrObservation(facade
					.innerDerivedUnit());
			imageGallery.setImageGallery(true);
			TextData textData = TextData.NewInstance();
			imageGallery.addElement(textData);
			textData.addMedia(media);
			facade.setDerivedUnitImageGallery(imageGallery);

		} catch (DerivedUnitFacadeNotSupportedException e1) {
			e1.printStackTrace();
			Assert.fail(e1.getLocalizedMessage());
		}
		this.service.save(facade.innerDerivedUnit());

		// setComplete(); endTransaction();
		// try {if (true){printDataSet(System.out, new
		// String[]{"HIBERNATE_SEQUENCES","SPECIMENOROBSERVATIONBASE","SPECIMENOROBSERVATIONBASE_DERIVATIONEVENT"
		// ,"DERIVATIONEVENT",
		// "DESCRIPTIONBASE","DESCRIPTIONELEMENTBASE","DESCRIPTIONELEMENTBASE_MEDIA","DESCRIPTIONBASE_SPECIMENOROBSERVATIONBASE",
		// "MEDIA", "MEDIAREPRESENTATION","MEDIAREPRESENTATIONPART"});}
		// } catch(Exception e) { logger.warn(e);}

	}

	@Test
	@DataSet
	public void testGetFieldObjectImageGalleryBooleanPersisted() {
		UUID specimenUUID = UUID
				.fromString("25383fc8-789b-4eff-92d3-a770d0622351");
		Specimen specimen = (Specimen) service.find(specimenUUID);
		Assert.assertNotNull("Specimen should exist (persisted)", specimen);
		try {
			DerivedUnitFacade facade = DerivedUnitFacade.NewInstance(specimen);
			SpecimenDescription imageGallery = facade
					.getFieldObjectImageGallery(true);
			Assert.assertNotNull("Image gallery should exist", imageGallery);
			Assert.assertEquals("UUID should be equal to the persisted uuid",
					UUID.fromString("8cb772e9-1577-45c6-91ab-dbec1413c060"),
					imageGallery.getUuid());
			Assert.assertEquals("The image gallery should be flagged as such",
					true, imageGallery.isImageGallery());
			Assert.assertEquals(
					"There should be one TextData in image gallery", 1,
					imageGallery.getElements().size());
			List<Media> media = imageGallery.getElements().iterator().next()
					.getMedia();
			Assert.assertEquals("There should be 1 media", 1, media.size());
		} catch (DerivedUnitFacadeNotSupportedException e) {
			e.printStackTrace();
			Assert.fail();
		}
	}

	@Test
	@DataSet
	public void testGetDerivedUnitImageGalleryBooleanPersisted() {
		UUID specimenUUID = UUID
				.fromString("25383fc8-789b-4eff-92d3-a770d0622351");
		Specimen specimen = (Specimen) service.find(specimenUUID);
		Assert.assertNotNull("Specimen should exist (persisted)", specimen);
		try {
			DerivedUnitFacade facade = DerivedUnitFacade.NewInstance(specimen);
			SpecimenDescription imageGallery = facade
					.getDerivedUnitImageGallery(true);
			Assert.assertNotNull("Image gallery should exist", imageGallery);
			Assert.assertEquals("UUID should be equal to the persisted uuid",
					UUID.fromString("cb03acc4-8363-4020-aeef-ea8a8bcc0fe9"),
					imageGallery.getUuid());
			Assert.assertEquals("The image gallery should be flagged as such",
					true, imageGallery.isImageGallery());
			Assert.assertEquals(
					"There should be one TextData in image gallery", 1,
					imageGallery.getElements().size());
			List<Media> media = imageGallery.getElements().iterator().next()
					.getMedia();
			Assert.assertEquals("There should be 1 media", 1, media.size());
		} catch (DerivedUnitFacadeNotSupportedException e) {
			e.printStackTrace();
			Assert.fail();
		}
	}

	@Test
	public void testGetDerivedUnitImageGalleryBoolean() {
		Specimen specimen = Specimen.NewInstance();
		try {
			DerivedUnitFacade facade = DerivedUnitFacade.NewInstance(specimen);
			SpecimenDescription imageGallery = facade
					.getDerivedUnitImageGallery(true);
			Assert.assertNotNull("Image Gallery should have been created",
					imageGallery);
			Assert.assertEquals("The image gallery should be flagged as such",
					true, imageGallery.isImageGallery());
		} catch (DerivedUnitFacadeNotSupportedException e) {
			e.printStackTrace();
			Assert.fail();
		}

	}

	/**
	 * Test method for
	 * {@link eu.etaxonomy.cdm.api.facade.DerivedUnitFacade#NewInstance()}.
	 */
	@Test
	public void testNewInstance() {
		Assert.assertNotNull("The specimen should have been created",
				specimenFacade.innerDerivedUnit());
		// ???
		// Assert.assertNotNull("The derivation event should have been created",
		// specimenFacade.getSpecimen().getDerivedFrom());
		// Assert.assertNotNull("The field observation should have been created",
		// specimenFacade.getFieldObservation());
		// Assert.assertNotNull("The gathering event should have been created",
		// specimenFacade.getGatheringEvent());
	}

	/**
	 * Test method for
	 * {@link eu.etaxonomy.cdm.api.facade.DerivedUnitFacade#NewInstance(eu.etaxonomy.cdm.model.occurrence.Specimen)}
	 * .
	 */
	@Test
	public void testNewInstanceSpecimen() {
		Assert.assertSame("Specimen should be same", specimen,
				specimenFacade.innerDerivedUnit());
		Assert.assertSame("Derivation event should be same", derivationEvent,
				specimenFacade.innerDerivedUnit().getDerivedFrom());
		Assert.assertSame("Field observation should be same", fieldObservation,
				specimenFacade.innerFieldObservation());
		Assert.assertSame("Gathering event should be same", gatheringEvent,
				specimenFacade.innerGatheringEvent());

	}

	@Test
	public void testGatheringEventIsConnectedToDerivedUnit() {
		Specimen specimen = Specimen.NewInstance();
		DerivedUnitFacade specimenFacade;
		try {
			specimenFacade = DerivedUnitFacade.NewInstance(specimen);
			specimenFacade.setDistanceToGround(2);
			FieldObservation specimenFieldObservation = (FieldObservation) specimen
					.getDerivedFrom().getOriginals().iterator().next();
			Assert.assertSame(
					"Facade gathering event and specimen gathering event should be the same",
					specimenFacade.innerGatheringEvent(),
					specimenFieldObservation.getGatheringEvent());
		} catch (DerivedUnitFacadeNotSupportedException e) {
			Assert.fail("An error should not occur in NewInstance()");
		}
	}

	@Test
	public void testNoGatheringEventAndFieldObservation() {
		Specimen specimen = Specimen.NewInstance();
		DerivedUnitFacade specimenFacade;
		try {
			specimenFacade = DerivedUnitFacade.NewInstance(specimen);
			Assert.assertNull("No field observation should exists",
					specimenFacade.innerFieldObservation());
		} catch (DerivedUnitFacadeNotSupportedException e) {
			Assert.fail("An error should not occur in NewInstance()");
		}
	}

	@Test
	public void testInititializeTextDataWithSupportTest() {
		// TODO
		Specimen specimen = Specimen.NewInstance();
		DerivedUnitFacade specimenFacade;
		try {
			specimenFacade = DerivedUnitFacade.NewInstance(specimen);
			specimenFacade.setEcology("Ecology");
			String plantDescription = specimenFacade.getPlantDescription();
			Assert.assertNull(
					"No plantDescription should exist yet and no NPE should be thrown until now",
					plantDescription);
		} catch (DerivedUnitFacadeNotSupportedException e) {
			Assert.fail("An error should not occur in NewInstance()");
		}
	}

	@Test
	public void testGetSetCountry() {
		logger.warn("Not yet implemented");
	}

	/**
	 * Test method for
	 * {@link eu.etaxonomy.cdm.api.facade.DerivedUnitFacade#addCollectingArea(eu.etaxonomy.cdm.model.location.NamedArea)}
	 * .
	 */
	@Test
	public void testAddGetRemoveCollectingArea() {
		String tdwgLabel = "GER";
		NamedArea tdwgArea = TdwgArea.getAreaByTdwgAbbreviation(tdwgLabel);
		NamedArea newCollectingArea = NamedArea.NewInstance("A nice area",
				"nice", "n");
		specimenFacade.addCollectingArea(newCollectingArea);
		Assert.assertEquals("Exactly 1 area must exist", 1, specimenFacade
				.getCollectingAreas().size());
		Assert.assertSame("Areas should be same", newCollectingArea,
				specimenFacade.innerFieldObservation().getGatheringEvent()
						.getCollectingAreas().iterator().next());
		specimenFacade.addCollectingArea(tdwgArea);
		Assert.assertEquals("Exactly 2 areas must exist", 2, specimenFacade
				.getCollectingAreas().size());
		specimenFacade.removeCollectingArea(newCollectingArea);
		Assert.assertEquals("Exactly 1 area must exist", 1, specimenFacade
				.getCollectingAreas().size());
		NamedArea remainingArea = specimenFacade.getCollectingAreas()
				.iterator().next();
		Assert.assertEquals("Areas should be same", tdwgArea, remainingArea);
		specimenFacade.removeCollectingArea(tdwgArea);
		Assert.assertEquals("No area should remain", 0, specimenFacade
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
		Assert.assertEquals("No area must exist", 0, specimenFacade
				.getCollectingAreas().size());
		specimenFacade.addCollectingArea(firstArea);
		Assert.assertEquals("Exactly 1 area must exist", 1, specimenFacade
				.getCollectingAreas().size());

		String tdwgLabel = "GER";
		NamedArea tdwgArea = TdwgArea.getAreaByTdwgAbbreviation(tdwgLabel);
		NamedArea secondArea = NamedArea
				.NewInstance("A nice area", "nice", "n");

		java.util.Collection<NamedArea> areaCollection = new HashSet<NamedArea>();
		areaCollection.add(secondArea);
		areaCollection.add(tdwgArea);
		specimenFacade.addCollectingAreas(areaCollection);
		Assert.assertEquals("Exactly 3 areas must exist", 3, specimenFacade
				.getCollectingAreas().size());

	}

	/**
	 * Test method for
	 * {@link eu.etaxonomy.cdm.api.facade.DerivedUnitFacade#getAbsoluteElevation()}
	 * .
	 */
	@Test
	public void testGetSetAbsoluteElevation() {
		Assert.assertEquals("Absolute elevation must be same",
				absoluteElevation, specimenFacade.getAbsoluteElevation());
		specimenFacade.setAbsoluteElevation(400);
		Assert.assertEquals("Absolute elevation must be 400",
				Integer.valueOf(400), specimenFacade.getAbsoluteElevation());
	}

	/**
	 * Test method for
	 * {@link eu.etaxonomy.cdm.api.facade.DerivedUnitFacade#getAbsoluteElevationError()}
	 * .
	 */
	@Test
	public void testGetSetAbsoluteElevationError() {
		Assert.assertEquals("Absolute elevation error must be same",
				absoluteElevationError,
				specimenFacade.getAbsoluteElevationError());
		specimenFacade.setAbsoluteElevationError(4);
		Assert.assertEquals("Absolute elevation error must be 4",
				Integer.valueOf(4), specimenFacade.getAbsoluteElevationError());
	}

	@Test()
	public void testGetSetAbsoluteElevationRange() {
		Integer expected = absoluteElevation - 2;
		Assert.assertEquals("", expected,
				specimenFacade.getAbsoluteElevationMinimum());
		expected = absoluteElevation + 2;
		Assert.assertEquals("", expected,
				specimenFacade.getAbsoluteElevationMaximum());
		specimenFacade.setAbsoluteElevationRange(30, 36);
		Assert.assertEquals("", Integer.valueOf(36),
				specimenFacade.getAbsoluteElevationMaximum());
		Assert.assertEquals("", Integer.valueOf(30),
				specimenFacade.getAbsoluteElevationMinimum());
		try {
			specimenFacade.setAbsoluteElevationRange(30, 35);
			Assert.fail("Odd distance needs to throw IllegalArgumentException");
		} catch (IllegalArgumentException e) {
			Assert.assertTrue("Exception needs to be thrown", true);
		}
		specimenFacade.setAbsoluteElevationRange(41, null);
		Assert.assertEquals("", Integer.valueOf(41),
				specimenFacade.getAbsoluteElevationMaximum());
		Assert.assertEquals("", Integer.valueOf(41),
				specimenFacade.getAbsoluteElevationMinimum());
		Assert.assertEquals("", Integer.valueOf(41),
				specimenFacade.getAbsoluteElevation());
		Assert.assertNotNull("", specimenFacade.getAbsoluteElevationError());
		Assert.assertEquals("", Integer.valueOf(0),
				specimenFacade.getAbsoluteElevationError());
		specimenFacade.setAbsoluteElevationRange(null, null);
		Assert.assertNull("", specimenFacade.getAbsoluteElevation());
		Assert.assertNull("", specimenFacade.getAbsoluteElevationError());

	}

	/**
	 */
	@Test
	public void testGetSetCollector() {
		Assert.assertNotNull("Collector must not be null",
				specimenFacade.getCollector());
		Assert.assertEquals("Collector must be same", collector,
				specimenFacade.getCollector());
		specimenFacade.setCollector(null);
		Assert.assertNull("Collector must be null",
				specimenFacade.getCollector());
	}

	/**
	 * Test method for
	 * {@link eu.etaxonomy.cdm.api.facade.DerivedUnitFacade#getCollectingMethod()}
	 * .
	 */
	@Test
	public void testGetSetCollectingMethod() {
		Assert.assertEquals("Collecting method must be same", collectingMethod,
				specimenFacade.getCollectingMethod());
		specimenFacade.setCollectingMethod("new method");
		Assert.assertEquals("Collecting method must be 'new method'",
				"new method", specimenFacade.getCollectingMethod());
	}

	/**
	 * Test method for
	 * {@link eu.etaxonomy.cdm.api.facade.DerivedUnitFacade#getDistanceToGround()}
	 * .
	 */
	@Test
	public void testGetSetDistanceToGround() {
		Assert.assertEquals("Distance to ground must be same",
				distanceToGround, specimenFacade.getDistanceToGround());
		specimenFacade.setDistanceToGround(5);
		Assert.assertEquals("Distance to ground must be 5", Integer.valueOf(5),
				specimenFacade.getDistanceToGround());
	}

	/**
	 * Test method for
	 * {@link eu.etaxonomy.cdm.api.facade.DerivedUnitFacade#getDistanceToWaterSurface()}
	 * .
	 */
	@Test
	public void testGetDistanceToWaterSurface() {
		Assert.assertEquals("Distance to surface must be same",
				distanceToSurface, specimenFacade.getDistanceToWaterSurface());
		specimenFacade.setDistanceToWaterSurface(6);
		Assert.assertEquals("Distance to surface must be 6",
				Integer.valueOf(6), specimenFacade.getDistanceToWaterSurface());
		// empty facade tests
		Assert.assertNull("Empty facace must not have any gathering values",
				emptyFacade.getDistanceToWaterSurface());
		emptyFacade.setDistanceToWaterSurface(13);
		Assert.assertNotNull(
				"Field observation must exist if distance to water exists",
				emptyFacade.getFieldObservation(false));
		Assert.assertNotNull(
				"Gathering event must exist if distance to water exists",
				emptyFacade.getGatheringEvent(false));
		FieldObservation specimenFieldObservation = (FieldObservation) emptyFacade
				.innerDerivedUnit().getDerivedFrom().getOriginals().iterator()
				.next();
		Assert.assertSame(
				"Gathering event of facade and of specimen must be the same",
				specimenFieldObservation.getGatheringEvent(),
				emptyFacade.getGatheringEvent(false));
	}

	/**
	 * Test method for
	 * {@link eu.etaxonomy.cdm.api.facade.DerivedUnitFacade#getExactLocation()}.
	 */
	@Test
	public void testGetSetExactLocation() {
		Assert.assertNotNull("Exact location must not be null",
				specimenFacade.getExactLocation());
		Assert.assertEquals("Exact location must be same", exactLocation,
				specimenFacade.getExactLocation());
		specimenFacade.setExactLocation(null);
		Assert.assertNull("Exact location must be null",
				specimenFacade.getExactLocation());
	}

	@Test
	public void testSetExactLocationByParsing() {
		Point point1;
		try {
			specimenFacade.setExactLocationByParsing("112\u00B034'20\"W",
					"34\u00B030,34'N", null, null);
			point1 = specimenFacade.getExactLocation();
			Assert.assertNotNull("", point1.getLatitude());
			System.out.println(point1.getLatitude().toString());
			Assert.assertTrue("",
					point1.getLatitude().toString().startsWith("34.505"));
			System.out.println(point1.getLongitude().toString());
			Assert.assertTrue("",
					point1.getLongitude().toString().startsWith("-112.5722"));

		} catch (ParseException e) {
			Assert.fail("No parsing error should occur");
		}
	}

	/**
	 * Test method for
	 * {@link eu.etaxonomy.cdm.api.facade.DerivedUnitFacade#getGatheringEventDescription()}
	 * .
	 */
	@Test
	public void testGetSetGatheringEventDescription() {
		Assert.assertEquals("Gathering event description must be same",
				gatheringEventDescription,
				specimenFacade.getGatheringEventDescription());
		specimenFacade.setGatheringEventDescription("new description");
		Assert.assertEquals(
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
		Assert.assertNotNull("Gathering period must not be null",
				specimenFacade.getGatheringPeriod());
		Assert.assertEquals("Gathering period must be same", gatheringPeriod,
				specimenFacade.getGatheringPeriod());
		specimenFacade.setGatheringPeriod(null);
		Assert.assertNull("Gathering period must be null",
				specimenFacade.getGatheringPeriod());
	}

	@Test
	public void testHasFieldObject() throws SecurityException,
			NoSuchFieldException, IllegalArgumentException,
			IllegalAccessException {
		// this test depends on the current implementation of SpecimenFacade. In
		// future
		// field observation may not be initialized from the beginning. Than the
		// following
		// assert should be set to assertNull
		Assert.assertTrue(
				"field object should not be null (depends on specimen facade initialization !!)",
				specimenFacade.hasFieldObject());

		Field fieldObservationField = DerivedUnitFacade.class
				.getDeclaredField("fieldObservation");
		fieldObservationField.setAccessible(true);
		fieldObservationField.set(specimenFacade, null);
		Assert.assertFalse("The field observation should be null now",
				specimenFacade.hasFieldObject());

		specimenFacade.setDistanceToGround(33);
		Assert.assertTrue(
				"The field observation should have been created again",
				specimenFacade.hasFieldObject());
	}

	/**
	 * Test method for
	 * {@link eu.etaxonomy.cdm.api.facade.DerivedUnitFacade#addFieldObjectDefinition(java.lang.String, eu.etaxonomy.cdm.model.common.Language)}
	 * .
	 */
	@Test
	public void testAddGetRemoveFieldObjectDefinition() {
		Assert.assertEquals("There should be no definition yet", 0,
				specimenFacade.getFieldObjectDefinition().size());
		specimenFacade.addFieldObjectDefinition("Tres interesant",
				Language.FRENCH());
		Assert.assertEquals("There should be exactly one definition", 1,
				specimenFacade.getFieldObjectDefinition().size());
		Assert.assertEquals(
				"The French definition should be 'Tres interesant'",
				"Tres interesant", specimenFacade.getFieldObjectDefinition()
						.get(Language.FRENCH()).getText());
		Assert.assertEquals(
				"The French definition should be 'Tres interesant'",
				"Tres interesant",
				specimenFacade.getFieldObjectDefinition(Language.FRENCH()));
		specimenFacade.addFieldObjectDefinition("Sehr interessant",
				Language.GERMAN());
		Assert.assertEquals("There should be exactly 2 definition", 2,
				specimenFacade.getFieldObjectDefinition().size());
		specimenFacade.removeFieldObjectDefinition(Language.FRENCH());
		Assert.assertEquals("There should remain exactly 1 definition", 1,
				specimenFacade.getFieldObjectDefinition().size());
		Assert.assertEquals(
				"The remaining German definition should be 'Sehr interessant'",
				"Sehr interessant",
				specimenFacade.getFieldObjectDefinition(Language.GERMAN()));
		specimenFacade.removeFieldObjectDefinition(Language.GERMAN());
		Assert.assertEquals("There should remain no definition", 0,
				specimenFacade.getFieldObjectDefinition().size());
	}

	/**
	 * Test method for
	 * {@link eu.etaxonomy.cdm.api.facade.DerivedUnitFacade#addFieldObjectMedia(eu.etaxonomy.cdm.model.media.Media)}
	 * .
	 */
	@Test
	public void testAddGetHasRemoveFieldObjectMedia() {
		Assert.assertFalse("There should be no image gallery yet",
				specimenFacade.hasFieldObjectImageGallery());
		Assert.assertFalse("There should be no specimen image gallery either",
				specimenFacade.hasDerivedUnitImageGallery());

		List<Media> media = specimenFacade.getFieldObjectMedia();
		Assert.assertFalse("There should still not be an image gallery now",
				specimenFacade.hasFieldObjectImageGallery());
		Assert.assertEquals("There should be no media yet in the gallery", 0,
				media.size());

		Media media1 = Media.NewInstance();
		specimenFacade.addFieldObjectMedia(media1);
		Assert.assertEquals("There should be exactly one specimen media", 1,
				specimenFacade.getFieldObjectMedia().size());
		Assert.assertEquals("The only media should be media 1", media1,
				specimenFacade.getFieldObjectMedia().get(0));
		Assert.assertFalse(
				"There should still no specimen image gallery exist",
				specimenFacade.hasDerivedUnitImageGallery());

		Media media2 = Media.NewInstance();
		specimenFacade.addFieldObjectMedia(media2);
		Assert.assertEquals("There should be exactly 2 specimen media", 2,
				specimenFacade.getFieldObjectMedia().size());
		Assert.assertEquals("The first media should be media1", media1,
				specimenFacade.getFieldObjectMedia().get(0));
		Assert.assertEquals("The second media should be media2", media2,
				specimenFacade.getFieldObjectMedia().get(1));

		specimenFacade.removeFieldObjectMedia(media1);
		Assert.assertEquals("There should be exactly one specimen media", 1,
				specimenFacade.getFieldObjectMedia().size());
		Assert.assertEquals("The only media should be media2", media2,
				specimenFacade.getFieldObjectMedia().get(0));

		specimenFacade.removeFieldObjectMedia(media1);
		Assert.assertEquals("There should still be exactly one specimen media",
				1, specimenFacade.getFieldObjectMedia().size());

		specimenFacade.removeFieldObjectMedia(media2);
		Assert.assertEquals("There should remain no media in the gallery", 0,
				media.size());

	}

	/**
	 * Test method for
	 * {@link eu.etaxonomy.cdm.api.facade.DerivedUnitFacade#addFieldObjectMedia(eu.etaxonomy.cdm.model.media.Media)}
	 * .
	 */
	@Test
	public void testGetSetEcology() {
		Assert.assertNotNull(
				"An empty ecology data should be created when calling getEcology()",
				specimenFacade.getEcologyAll());
		Assert.assertEquals(
				"An empty ecology data should be created when calling getEcology()",
				0, specimenFacade.getEcologyAll().size());
		specimenFacade.setEcology("Tres jolie ici", Language.FRENCH());
		Assert.assertEquals("Ecology data should exist for 1 language", 1,
				specimenFacade.getEcologyAll().size());
		Assert.assertEquals(
				"Ecology data should be 'Tres jolie ici' for French",
				"Tres jolie ici", specimenFacade.getEcology(Language.FRENCH()));
		Assert.assertNull(
				"Ecology data should be null for the default language",
				specimenFacade.getEcology());
		specimenFacade.setEcology("Nice here");
		Assert.assertEquals("Ecology data should exist for 2 languages", 2,
				specimenFacade.getEcologyAll().size());
		Assert.assertEquals("Ecology data should be 'Tres jolie ici'",
				"Tres jolie ici", specimenFacade.getEcology(Language.FRENCH()));
		Assert.assertEquals(
				"Ecology data should be 'Nice here' for the default language",
				"Nice here", specimenFacade.getEcology());
		Assert.assertEquals("Ecology data should be 'Nice here' for english",
				"Nice here", specimenFacade.getEcology());

		specimenFacade.setEcology("Vert et rouge", Language.FRENCH());
		Assert.assertEquals("Ecology data should exist for 2 languages", 2,
				specimenFacade.getEcologyAll().size());
		Assert.assertEquals("Ecology data should be 'Vert et rouge'",
				"Vert et rouge", specimenFacade.getEcology(Language.FRENCH()));
		Assert.assertEquals(
				"Ecology data should be 'Nice here' for the default language",
				"Nice here", specimenFacade.getEcology());

		specimenFacade.setEcology(null, Language.FRENCH());
		Assert.assertEquals("Ecology data should exist for 1 languages", 1,
				specimenFacade.getEcologyAll().size());
		Assert.assertEquals(
				"Ecology data should be 'Nice here' for the default language",
				"Nice here", specimenFacade.getEcology());
		Assert.assertNull("Ecology data should be 'null' for French",
				specimenFacade.getEcology(Language.FRENCH()));

		specimenFacade.removeEcology(null);
		Assert.assertEquals("There should be no ecology left", 0,
				specimenFacade.getEcologyAll().size());
		Assert.assertNull("Ecology data should be 'null' for default language",
				specimenFacade.getEcology());

	}

	/**
	 * Test method for
	 * {@link eu.etaxonomy.cdm.api.facade.DerivedUnitFacade#addFieldObjectMedia(eu.etaxonomy.cdm.model.media.Media)}
	 * .
	 */
	@Test
	public void testGetSetPlantDescription() {
		Assert.assertNotNull(
				"An empty plant description data should be created when calling getPlantDescriptionAll()",
				specimenFacade.getPlantDescriptionAll());
		Assert.assertEquals(
				"An empty plant description data should be created when calling getPlantDescription()",
				0, specimenFacade.getPlantDescriptionAll().size());
		specimenFacade.setPlantDescription("bleu", Language.FRENCH());
		Assert.assertEquals(
				"Plant description data should exist for 1 language", 1,
				specimenFacade.getPlantDescriptionAll().size());
		Assert.assertEquals(
				"Plant description data should be 'bleu' for French", "bleu",
				specimenFacade.getPlantDescription(Language.FRENCH()));
		Assert.assertNull(
				"Plant description data should be null for the default language",
				specimenFacade.getPlantDescription());
		specimenFacade.setPlantDescription("Nice here");
		Assert.assertEquals(
				"Plant description data should exist for 2 languages", 2,
				specimenFacade.getPlantDescriptionAll().size());
		Assert.assertEquals("Plant description data should be 'bleu'", "bleu",
				specimenFacade.getPlantDescription(Language.FRENCH()));
		Assert.assertEquals(
				"Plant description data should be 'Nice here' for the default language",
				"Nice here", specimenFacade.getPlantDescription());
		Assert.assertEquals(
				"Plant description data should be 'Nice here' for english",
				"Nice here", specimenFacade.getPlantDescription());

		specimenFacade.setPlantDescription("Vert et rouge", Language.FRENCH());
		Assert.assertEquals(
				"Plant description data should exist for 2 languages", 2,
				specimenFacade.getPlantDescriptionAll().size());
		Assert.assertEquals("Plant description data should be 'Vert et rouge'",
				"Vert et rouge",
				specimenFacade.getPlantDescription(Language.FRENCH()));
		Assert.assertEquals(
				"Plant description data should be 'Nice here' for the default language",
				"Nice here", specimenFacade.getPlantDescription());

		specimenFacade.setPlantDescription(null, Language.FRENCH());
		Assert.assertEquals(
				"Plant description data should exist for 1 languages", 1,
				specimenFacade.getPlantDescriptionAll().size());
		Assert.assertEquals(
				"Plant description data should be 'Nice here' for the default language",
				"Nice here", specimenFacade.getPlantDescription());
		Assert.assertNull("Plant description data should be 'null' for French",
				specimenFacade.getPlantDescription(Language.FRENCH()));

		// test interference with ecology
		specimenFacade.setEcology("Tres jolie ici", Language.FRENCH());
		Assert.assertEquals("Ecology data should exist for 1 language", 1,
				specimenFacade.getEcologyAll().size());
		Assert.assertEquals(
				"Ecology data should be 'Tres jolie ici' for French",
				"Tres jolie ici", specimenFacade.getEcology(Language.FRENCH()));
		Assert.assertNull(
				"Ecology data should be null for the default language",
				specimenFacade.getEcology());

		// repeat above test
		Assert.assertEquals(
				"Plant description data should exist for 1 languages", 1,
				specimenFacade.getPlantDescriptionAll().size());
		Assert.assertEquals(
				"Plant description data should be 'Nice here' for the default language",
				"Nice here", specimenFacade.getPlantDescription());
		Assert.assertNull("Plant description data should be 'null' for French",
				specimenFacade.getPlantDescription(Language.FRENCH()));

		specimenFacade.removePlantDescription(null);
		Assert.assertEquals("There should be no plant description left", 0,
				specimenFacade.getPlantDescriptionAll().size());
		Assert.assertNull(
				"Plant description data should be 'null' for default language",
				specimenFacade.getPlantDescription());

	}

	/**
	 * Test method for
	 * {@link eu.etaxonomy.cdm.api.facade.DerivedUnitFacade#getFieldNumber()}.
	 */
	@Test
	public void testGetSetFieldNumber() {
		Assert.assertEquals("Field number must be same", fieldNumber,
				specimenFacade.getFieldNumber());
		specimenFacade.setFieldNumber("564AB");
		Assert.assertEquals("New field number must be '564AB'", "564AB",
				specimenFacade.getFieldNumber());
		// empty facade tests
		Assert.assertNull("Empty facace must not have any field value",
				emptyFacade.getFieldNumber());
		emptyFacade.setFieldNumber("1256A");
		Assert.assertNotNull(
				"Field observation must exist if field number exists",
				emptyFacade.getFieldObservation(false));
		FieldObservation specimenFieldObservation = (FieldObservation) emptyFacade
				.innerDerivedUnit().getDerivedFrom().getOriginals().iterator()
				.next();
		Assert.assertSame(
				"Field observation of facade and of specimen must be the same",
				specimenFieldObservation,
				emptyFacade.getFieldObservation(false));
		Assert.assertEquals("1256A", emptyFacade.getFieldNumber());
	}

	/**
	 * Test method for
	 * {@link eu.etaxonomy.cdm.api.facade.DerivedUnitFacade#getFieldNotes()}.
	 */
	@Test
	public void testGetSetFieldNotes() {
		Assert.assertEquals("Field notes must be same", fieldNotes,
				specimenFacade.getFieldNotes());
		specimenFacade.setFieldNotes("A completely new info");
		Assert.assertEquals("New field note must be 'A completely new info'",
				"A completely new info", specimenFacade.getFieldNotes());
	}

	/**
	 * Test method for
	 * {@link eu.etaxonomy.cdm.api.facade.DerivedUnitFacade#setGatheringEvent(eu.etaxonomy.cdm.model.occurrence.GatheringEvent)}
	 * .
	 */
	@Test
	public void testSetGatheringEvent() {
		GatheringEvent newGatheringEvent = GatheringEvent.NewInstance();
		newGatheringEvent.setDistanceToGround(43);
		Assert.assertFalse("The initial distance to ground should not be 43",
				specimenFacade.getDistanceToGround() == 43);
		specimenFacade.setGatheringEvent(newGatheringEvent);
		Assert.assertTrue("The final distance to ground should be 43",
				specimenFacade.getDistanceToGround() == 43);
		Assert.assertSame(
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
		Assert.assertNotNull("Gathering event must not be null",
				specimenFacade.innerGatheringEvent());
		Assert.assertEquals(
				"Gathering event must be field observations gathering event",
				specimenFacade.innerFieldObservation().getGatheringEvent(),
				specimenFacade.innerGatheringEvent());
	}

	/**
	 * Test method for
	 * {@link eu.etaxonomy.cdm.api.facade.DerivedUnitFacade#getIndividualCount()}
	 * .
	 */
	@Test
	public void testGetSetIndividualCount() {
		Assert.assertEquals("Individual count must be same", individualCount,
				specimenFacade.getIndividualCount());
		specimenFacade.setIndividualCount(4);
		Assert.assertEquals("New individual count must be '4'",
				Integer.valueOf(4), specimenFacade.getIndividualCount());

	}

	/**
	 * Test method for
	 * {@link eu.etaxonomy.cdm.api.facade.DerivedUnitFacade#getLifeStage()}.
	 */
	@Test
	public void testGetSetLifeStage() {
		Assert.assertNotNull("Life stage must not be null",
				specimenFacade.getLifeStage());
		Assert.assertEquals("Life stage must be same", lifeStage,
				specimenFacade.getLifeStage());
		specimenFacade.setLifeStage(null);
		Assert.assertNull("Life stage must be null",
				specimenFacade.getLifeStage());
	}

	/**
	 * Test method for
	 * {@link eu.etaxonomy.cdm.api.facade.DerivedUnitFacade#getSex()}.
	 */
	@Test
	public void testGetSetSex() {
		Assert.assertNotNull("Sex must not be null", specimenFacade.getSex());
		Assert.assertEquals("Sex must be same", sex, specimenFacade.getSex());
		specimenFacade.setSex(null);
		Assert.assertNull("Sex must be null", specimenFacade.getSex());
	}

	/**
	 * Test method for
	 * {@link eu.etaxonomy.cdm.api.facade.DerivedUnitFacade#getLocality()}.
	 */
	@Test
	public void testGetSetLocality() {
		Assert.assertEquals("Locality must be same", locality,
				specimenFacade.getLocality());
		specimenFacade.setLocality("A completely new place", Language.FRENCH());
		Assert.assertEquals("New locality must be 'A completely new place'",
				"A completely new place", specimenFacade.getLocalityText());
		Assert.assertEquals("New locality language must be French",
				Language.FRENCH(), specimenFacade.getLocalityLanguage());
		specimenFacade.setLocality("Another place");
		Assert.assertEquals("New locality must be 'Another place'",
				"Another place", specimenFacade.getLocalityText());
		Assert.assertEquals("New locality language must be default",
				Language.DEFAULT(), specimenFacade.getLocalityLanguage());
	}

	/**
	 * Test method for
	 * {@link eu.etaxonomy.cdm.api.facade.DerivedUnitFacade#addDerivedUnitDefinition(java.lang.String, eu.etaxonomy.cdm.model.common.Language)}
	 * .
	 */
	@Test
	public void testAddGetRemoveSpecimenDefinition() {
		Assert.assertEquals("There should be no definition yet", 0,
				specimenFacade.getDerivedUnitDefinitions().size());
		specimenFacade.addDerivedUnitDefinition("Tres interesant",
				Language.FRENCH());
		Assert.assertEquals("There should be exactly one definition", 1,
				specimenFacade.getDerivedUnitDefinitions().size());
		Assert.assertEquals(
				"The French definition should be 'Tres interesant'",
				"Tres interesant", specimenFacade.getDerivedUnitDefinitions()
						.get(Language.FRENCH()).getText());
		Assert.assertEquals(
				"The French definition should be 'Tres interesant'",
				"Tres interesant",
				specimenFacade.getDerivedUnitDefinition(Language.FRENCH()));
		specimenFacade.addDerivedUnitDefinition("Sehr interessant",
				Language.GERMAN());
		Assert.assertEquals("There should be exactly 2 definition", 2,
				specimenFacade.getDerivedUnitDefinitions().size());
		specimenFacade.removeDerivedUnitDefinition(Language.FRENCH());
		Assert.assertEquals("There should remain exactly 1 definition", 1,
				specimenFacade.getDerivedUnitDefinitions().size());
		Assert.assertEquals(
				"The remaining German definition should be 'Sehr interessant'",
				"Sehr interessant",
				specimenFacade.getDerivedUnitDefinition(Language.GERMAN()));
		specimenFacade.removeDerivedUnitDefinition(Language.GERMAN());
		Assert.assertEquals("There should remain no definition", 0,
				specimenFacade.getDerivedUnitDefinitions().size());
	}

	/**
	 * Test method for
	 * {@link eu.etaxonomy.cdm.api.facade.DerivedUnitFacade#addDetermination(eu.etaxonomy.cdm.model.occurrence.DeterminationEvent)}
	 * .
	 */
	@Test
	public void testAddGetRemoveDetermination() {
		Assert.assertEquals("There should be no determination yet", 0,
				specimenFacade.getDeterminations().size());
		DeterminationEvent determinationEvent1 = DeterminationEvent
				.NewInstance();
		specimenFacade.addDetermination(determinationEvent1);
		Assert.assertEquals("There should be exactly one determination", 1,
				specimenFacade.getDeterminations().size());
		Assert.assertEquals("The only determination should be determination 1",
				determinationEvent1, specimenFacade.getDeterminations()
						.iterator().next());

		DeterminationEvent determinationEvent2 = DeterminationEvent
				.NewInstance();
		specimenFacade.addDetermination(determinationEvent2);
		Assert.assertEquals("There should be exactly 2 determinations", 2,
				specimenFacade.getDeterminations().size());
		specimenFacade.removeDetermination(determinationEvent1);

		Assert.assertEquals("There should remain exactly 1 determination", 1,
				specimenFacade.getDeterminations().size());
		Assert.assertEquals(
				"The remaining determinations should be determination 2",
				determinationEvent2, specimenFacade.getDeterminations()
						.iterator().next());

		specimenFacade.removeDetermination(determinationEvent1);
		Assert.assertEquals("There should remain exactly 1 determination", 1,
				specimenFacade.getDeterminations().size());

		specimenFacade.removeDetermination(determinationEvent2);
		Assert.assertEquals("There should remain no definition", 0,
				specimenFacade.getDerivedUnitDefinitions().size());

	}

	/**
	 * Test method for
	 * {@link eu.etaxonomy.cdm.api.facade.DerivedUnitFacade#addDerivedUnitMedia(eu.etaxonomy.cdm.model.media.Media)}
	 * .
	 */
	@Test
	public void testAddGetHasRemoveSpecimenMedia() {
		Assert.assertFalse("There should be no image gallery yet",
				specimenFacade.hasDerivedUnitImageGallery());
		Assert.assertFalse(
				"There should be also no field object image gallery yet",
				specimenFacade.hasFieldObjectImageGallery());

		List<Media> media = specimenFacade.getDerivedUnitMedia();
		Assert.assertFalse(
				"There should still not be an empty image gallery now",
				specimenFacade.hasDerivedUnitImageGallery());
		Assert.assertEquals("There should be no media yet in the gallery", 0,
				media.size());

		Media media1 = Media.NewInstance();
		specimenFacade.addDerivedUnitMedia(media1);
		Assert.assertEquals("There should be exactly one specimen media", 1,
				specimenFacade.getDerivedUnitMedia().size());
		Assert.assertEquals("The only media should be media 1", media1,
				specimenFacade.getDerivedUnitMedia().get(0));
		Assert.assertFalse(
				"There should be still no field object image gallery",
				specimenFacade.hasFieldObjectImageGallery());

		Media media2 = Media.NewInstance();
		specimenFacade.addDerivedUnitMedia(media2);
		Assert.assertEquals("There should be exactly 2 specimen media", 2,
				specimenFacade.getDerivedUnitMedia().size());
		Assert.assertEquals("The first media should be media1", media1,
				specimenFacade.getDerivedUnitMedia().get(0));
		Assert.assertEquals("The second media should be media2", media2,
				specimenFacade.getDerivedUnitMedia().get(1));

		specimenFacade.removeDerivedUnitMedia(media1);
		Assert.assertEquals("There should be exactly one specimen media", 1,
				specimenFacade.getDerivedUnitMedia().size());
		Assert.assertEquals("The only media should be media2", media2,
				specimenFacade.getDerivedUnitMedia().get(0));

		specimenFacade.removeDerivedUnitMedia(media1);
		Assert.assertEquals("There should still be exactly one specimen media",
				1, specimenFacade.getDerivedUnitMedia().size());

		specimenFacade.removeDerivedUnitMedia(media2);
		Assert.assertEquals("There should remain no media in the gallery", 0,
				media.size());
	}

	/**
	 * Test method for
	 * {@link eu.etaxonomy.cdm.api.facade.DerivedUnitFacade#getAccessionNumber()}
	 * .
	 */
	@Test
	public void testGetSetAccessionNumber() {
		Assert.assertEquals("Accession number must be same", accessionNumber,
				specimenFacade.getAccessionNumber());
		specimenFacade.setAccessionNumber("A12345693");
		Assert.assertEquals("New accession number must be 'A12345693'",
				"A12345693", specimenFacade.getAccessionNumber());
	}

	/**
	 * Test method for
	 * {@link eu.etaxonomy.cdm.api.facade.DerivedUnitFacade#getCatalogNumber()}.
	 */
	@Test
	public void testGetCatalogNumber() {
		Assert.assertEquals("Catalog number must be same", catalogNumber,
				specimenFacade.getCatalogNumber());
		specimenFacade.setCatalogNumber("B12345693");
		Assert.assertEquals("New catalog number must be 'B12345693'",
				"B12345693", specimenFacade.getCatalogNumber());
	}

	/**
	 * Test method for
	 * {@link eu.etaxonomy.cdm.api.facade.DerivedUnitFacade#getPreservation()}.
	 */
	@Test
	public void testGetPreservation() {
		try {
			Assert.assertNotNull("Preservation method must not be null",
					specimenFacade.getPreservationMethod());
			Assert.assertEquals("Preservation method must be same",
					preservationMethod, specimenFacade.getPreservationMethod());
			specimenFacade.setPreservationMethod(null);
			Assert.assertNull("Preservation method must be null",
					specimenFacade.getPreservationMethod());
		} catch (MethodNotSupportedByDerivedUnitTypeException e) {
			Assert.fail("Method not supported should not be thrown for a specimen");
		}
		specimenFacade = DerivedUnitFacade
				.NewInstance(DerivedUnitType.Observation);
		try {
			specimenFacade.setPreservationMethod(preservationMethod);
			Assert.fail("Method not supported should be thrown for an observation on set preservation method");

		} catch (MethodNotSupportedByDerivedUnitTypeException e) {
			// ok
		}
		specimenFacade = DerivedUnitFacade
				.NewInstance(DerivedUnitType.LivingBeing);
		try {
			specimenFacade.getPreservationMethod();
			Assert.fail("Method not supported should be thrown for a living being on get preservation method");
		} catch (MethodNotSupportedByDerivedUnitTypeException e) {
			// ok
		}

	}

	/**
	 * Test method for
	 * {@link eu.etaxonomy.cdm.api.facade.DerivedUnitFacade#getStoredUnder()}.
	 */
	@Test
	public void testGetStoredUnder() {
		Assert.assertNotNull("Stored under name must not be null",
				specimenFacade.getStoredUnder());
		Assert.assertEquals("Stored under name must be same", taxonName,
				specimenFacade.getStoredUnder());
		specimenFacade.setStoredUnder(null);
		Assert.assertNull("Stored under name must be null",
				specimenFacade.getStoredUnder());
	}

	/**
	 * Test method for
	 * {@link eu.etaxonomy.cdm.api.facade.DerivedUnitFacade#getCollectorsNumber()}
	 * .
	 */
	@Test
	public void testGetSetCollectorsNumber() {
		Assert.assertEquals("Collectors number must be same", collectorsNumber,
				specimenFacade.getCollectorsNumber());
		specimenFacade.setCollectorsNumber("C12345693");
		Assert.assertEquals("New collectors number must be 'C12345693'",
				"C12345693", specimenFacade.getCollectorsNumber());
	}

	/**
	 * Test method for
	 * {@link eu.etaxonomy.cdm.api.facade.DerivedUnitFacade#getTitleCache()}.
	 */
	@Test
	public void testGetTitleCache() {
		Assert.assertNotNull(
				"The title cache should never return null if not protected",
				specimenFacade.getTitleCache());
		specimenFacade.setTitleCache(null, false);
		Assert.assertNotNull(
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
		Assert.assertEquals(
				"Protected title cache should returns the test title",
				testTitle, specimenFacade.getTitleCache());
		specimenFacade.setTitleCache(testTitle, false);
		Assert.assertFalse(
				"Unprotected title cache should not return the test title",
				testTitle.equals(specimenFacade.getTitleCache()));
	}

	/**
	 * Test method for
	 * {@link eu.etaxonomy.cdm.api.facade.DerivedUnitFacade#innerDerivedUnit()}.
	 */
	@Test
	public void testGetSpecimen() {
		Assert.assertEquals("Specimen must be same", specimen,
				specimenFacade.innerDerivedUnit());
	}

	/**
	 * Test method for
	 * {@link eu.etaxonomy.cdm.api.facade.DerivedUnitFacade#getCollection()}.
	 */
	@Test
	public void testGetSetCollection() {
		Assert.assertNotNull("Collection must not be null",
				specimenFacade.getCollection());
		Assert.assertEquals("Collection must be same", collection,
				specimenFacade.getCollection());
		specimenFacade.setCollection(null);
		Assert.assertNull("Collection must be null",
				specimenFacade.getCollection());
	}

	@Test
	public void testAddGetRemoveSource() {
		Assert.assertEquals("No sources should exist yet", 0, specimenFacade
				.getSources().size());
		Reference reference = ReferenceFactory.newBook();
		IdentifiableSource source1 = specimenFacade.addSource(reference, "54",
				"myName");
		Assert.assertEquals("One source should exist now", 1, specimenFacade
				.getSources().size());
		IdentifiableSource source2 = IdentifiableSource.NewInstance("1",
				"myTable");
		specimenFacade.addSource(source2);
		Assert.assertEquals("One source should exist now", 2, specimenFacade
				.getSources().size());
		specimenFacade.removeSource(source1);
		Assert.assertEquals("One source should exist now", 1, specimenFacade
				.getSources().size());
		Reference reference2 = ReferenceFactory.newJournal();
		IdentifiableSource sourceNotUsed = specimenFacade.addSource(reference2,
				null, null);
		specimenFacade.removeSource(sourceNotUsed);
		Assert.assertEquals("One source should still exist", 1, specimenFacade
				.getSources().size());
		Assert.assertEquals("1", specimenFacade.getSources().iterator().next()
				.getIdInSource());
		specimenFacade.removeSource(source2);
		Assert.assertEquals("No sources should exist anymore", 0,
				specimenFacade.getSources().size());
	}

	@Test
	public void testAddGetRemoveDuplicate() {
		Assert.assertEquals("No duplicates should be available yet", 0,
				specimenFacade.getDuplicates().size());
		Specimen newSpecimen1 = Specimen.NewInstance();
		specimenFacade.addDuplicate(newSpecimen1);
		Assert.assertEquals("There should be 1 duplicate now", 1,
				specimenFacade.getDuplicates().size());
		Specimen newSpecimen2 = Specimen.NewInstance();
		DerivationEvent newDerivationEvent = DerivationEvent.NewInstance();
		newSpecimen2.setDerivedFrom(newDerivationEvent);
		Assert.assertSame(
				"The derivation event should be 'newDerivationEvent'",
				newDerivationEvent, newSpecimen2.getDerivedFrom());
		specimenFacade.addDuplicate(newSpecimen2);
		Assert.assertEquals("There should be 2 duplicates now", 2,
				specimenFacade.getDuplicates().size());
		Assert.assertNotSame(
				"The derivation event should not be 'newDerivationEvent' anymore",
				newDerivationEvent, newSpecimen2.getDerivedFrom());
		Assert.assertSame(
				"The derivation event should not be the facades derivation event",
				derivationEvent, newSpecimen2.getDerivedFrom());
		specimenFacade.removeDuplicate(newSpecimen1);
		Assert.assertEquals("There should be 1 duplicate now", 1,
				specimenFacade.getDuplicates().size());
		Assert.assertSame("The only duplicate should be 'newSpecimen2' now",
				newSpecimen2, specimenFacade.getDuplicates().iterator().next());
		specimenFacade.addDuplicate(specimenFacade.innerDerivedUnit());
		Assert.assertEquals(
				"There should be still 1 duplicate because the facade specimen is not a duplicate",
				1, specimenFacade.getDuplicates().size());

		Collection newCollection = Collection.NewInstance();
		String catalogNumber = "1234890";
		String accessionNumber = "345345";
		String collectorsNumber = "lkjewe";
		TaxonNameBase storedUnder = BotanicalName.NewInstance(Rank.SPECIES());
		PreservationMethod method = PreservationMethod.NewInstance();
		Specimen duplicateSpecimen = specimenFacade.addDuplicate(newCollection,
				catalogNumber, accessionNumber, collectorsNumber, storedUnder,
				method);
		Assert.assertEquals("There should be 2 duplicates now", 2,
				specimenFacade.getDuplicates().size());
		specimenFacade.removeDuplicate(newSpecimen2);
		Assert.assertEquals("There should be 1 duplicates now", 1,
				specimenFacade.getDuplicates().size());
		Collection sameCollection = specimenFacade.getDuplicates().iterator()
				.next().getCollection();
		Assert.assertSame("Collections should be same", newCollection,
				sameCollection);
	}

	// ************************** Existing Specimen with multiple derivation
	// events in line **************/

	@Test
	public void testExistingSpecimen() {
		specimenFacade = null;
		try {
			specimenFacade = DerivedUnitFacade.NewInstance(collectionSpecimen);
		} catch (DerivedUnitFacadeNotSupportedException e) {
			Assert.fail("Multiple derivation events in line should not throw a not supported exception");
		}
		Assert.assertSame(
				"Gathering event should derive from the derivation line",
				existingGatheringEvent, specimenFacade.innerGatheringEvent());
		Assert.assertEquals(
				"Mediasize should be 0. Only Imagegallery media are supported",
				0, specimenFacade.getFieldObjectMedia().size());
	}

	@Test
	public void testMultipleFieldObservationsNotSupported() {
		specimenFacade = null;
		FieldObservation secondFieldObject = FieldObservation.NewInstance();
		firstDerivationEvent.addOriginal(secondFieldObject);
		try {
			specimenFacade = DerivedUnitFacade.NewInstance(collectionSpecimen);
			Assert.fail("Multiple field observations for one specimen should no be supported by the facade");
		} catch (DerivedUnitFacadeNotSupportedException e) {
			// ok
		}
		Assert.assertNull("Specimen facade should not be initialized",
				specimenFacade);
	}

	@Test
	public void testOnlyImageGallerySupported() {
		specimenFacade = null;
		firstFieldObject.addMedia(media1);
		try {
			specimenFacade = DerivedUnitFacade.NewInstance(collectionSpecimen);
			Assert.fail("Only image galleries are supported by the facade but not direct media");
		} catch (DerivedUnitFacadeNotSupportedException e) {
			// ok
		}
		Assert.assertNull("Specimen facade should not be initialized",
				specimenFacade);
	}

	@Test
	public void testEventPropagation() {
		specimenFacade.setDistanceToGround(24);

	}

	// @Ignore // set to ignore because I did not want to check knowingly
	// failing tests. Remove @Ignore when this is fixed
	@Test
	public void testSetBarcode() {
		String barcode = "barcode";
		specimenFacade.setBarcode(barcode);

		Assert.assertEquals(barcode, specimenFacade.getBarcode());
	}

	@Test
	public void testIsEvenDistance() {
		Integer minimum = 20;
		Integer maximum = 1234;

		// this should not throw exceptions
		specimenFacade.setAbsoluteElevationRange(minimum, maximum);
	}
}
