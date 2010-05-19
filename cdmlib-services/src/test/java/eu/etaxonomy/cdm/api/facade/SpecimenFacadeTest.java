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
import java.util.List;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import eu.etaxonomy.cdm.model.agent.AgentBase;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.common.DefaultTermInitializer;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.LanguageString;
import eu.etaxonomy.cdm.model.common.TimePeriod;
import eu.etaxonomy.cdm.model.description.Sex;
import eu.etaxonomy.cdm.model.description.Stage;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.location.Point;
import eu.etaxonomy.cdm.model.location.ReferenceSystem;
import eu.etaxonomy.cdm.model.location.TdwgArea;
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

/**
 * @author a.mueller
 * @date 17.05.2010
 *
 */
public class SpecimenFacadeTest {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(SpecimenFacadeTest.class);
	
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
	LanguageString locality = LanguageString.NewInstance("My locality", Language.DEFAULT());

	String accessionNumber = "888462535";
	String catalogNumber = "UU879873590";
	TaxonNameBase taxonName = BotanicalName.NewInstance(Rank.GENUS(), "Abies", null, null, null, null, null, null, null);
	String collectorsNumber = "234589913A34";
	Collection collection = Collection.NewInstance();
	PreservationMethod preservationMethod = PreservationMethod.NewInstance("my prservation", null, null);

	SpecimenFacade specimenFacade;
	
//****************************** SET UP *****************************************/
	
	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		// FIXME maybe this will cause problems in other tests
		new DefaultTermInitializer().initialize();
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
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

		specimenFacade = SpecimenFacade.NewInstance(specimen);

	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

//****************************** SET UP *****************************************/
	
	/**
	 * Test method for {@link eu.etaxonomy.cdm.api.facade.SpecimenFacade#NewInstance()}.
	 */
	@Test
	public void testNewInstance() {
		SpecimenFacade specimenFacade = SpecimenFacade.NewInstance();
		Assert.assertNotNull("The specimen should have been created", specimenFacade.getSpecimen());
		//???
		Assert.assertNotNull("The derivation event should have been created", specimenFacade.getSpecimen().getDerivedFrom());
		Assert.assertNotNull("The field observation should have been created", specimenFacade.getFieldObservation());
		Assert.assertNotNull("The gathering event should have been created", specimenFacade.getGatheringEvent());
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.api.facade.SpecimenFacade#NewInstance(eu.etaxonomy.cdm.model.occurrence.Specimen)}.
	 */
	@Test
	public void testNewInstanceSpecimen() {
		Assert.assertSame("Specimen should be same", specimen, specimenFacade.getSpecimen());
		Assert.assertSame("Derivation event should be same", derivationEvent, specimenFacade.getSpecimen().getDerivedFrom());
		Assert.assertSame("Field observation should be same", fieldObservation, specimenFacade.getFieldObservation());
		Assert.assertSame("Gathering event should be same", gatheringEvent, specimenFacade.getGatheringEvent());
	
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.api.facade.SpecimenFacade#addCollectingArea(eu.etaxonomy.cdm.model.location.NamedArea)}.
	 */
	@Test
	public void testAddGetRemoveCollectingArea()  {
		String tdwgLabel = "GER";
		NamedArea tdwgArea = TdwgArea.getAreaByTdwgAbbreviation(tdwgLabel);
		NamedArea newCollectingArea = NamedArea.NewInstance("A nice area", "nice", "n");
		specimenFacade.addCollectingArea(newCollectingArea);
		Assert.assertEquals("Exactly 1 area must exist", 1, specimenFacade.getCollectingAreas().size());
		Assert.assertSame("Areas should be same", newCollectingArea, specimenFacade.getFieldObservation().getGatheringEvent().getCollectingAreas().iterator().next());
		specimenFacade.addCollectingArea(tdwgArea);
		Assert.assertEquals("Exactly 2 area must exist", 2, specimenFacade.getCollectingAreas().size());
		specimenFacade.removeCollectingArea(newCollectingArea);
		Assert.assertEquals("Exactly 1 area must exist", 1, specimenFacade.getCollectingAreas().size());
		NamedArea remainingArea = specimenFacade.getCollectingAreas().iterator().next();
		Assert.assertEquals("Areas should be same", tdwgArea, remainingArea);
		specimenFacade.removeCollectingArea(tdwgArea);
		Assert.assertEquals("No area should remain", 0, specimenFacade.getCollectingAreas().size());
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.api.facade.SpecimenFacade#getAbsoluteElevation()}. 
	 */
	@Test
	public void testGetSetAbsoluteElevation() {
		Assert.assertEquals("Absolute elevation must be same",absoluteElevation, specimenFacade.getAbsoluteElevation());
		specimenFacade.setAbsoluteElevation(400);
		Assert.assertEquals("Absolute elevation must be 400", Integer.valueOf(400), specimenFacade.getAbsoluteElevation());
	}


	/**
	 * Test method for {@link eu.etaxonomy.cdm.api.facade.SpecimenFacade#getAbsoluteElevationError()}.
	 */
	@Test
	public void testGetSetAbsoluteElevationError() {
		Assert.assertEquals("Absolute elevation error must be same",absoluteElevationError, specimenFacade.getAbsoluteElevationError());
		specimenFacade.setAbsoluteElevationError(4);
		Assert.assertEquals("Absolute elevation error must be 4", Integer.valueOf(4), specimenFacade.getAbsoluteElevationError());
	}

	/**
	 */
	@Test
	public void testGetSetCollector() {
		Assert.assertNotNull("Collector must not be null", specimenFacade.getCollector());	
		Assert.assertEquals("Collector must be same",collector, specimenFacade.getCollector());	
		specimenFacade.setCollector(null);
		Assert.assertNull("Collector must be null", specimenFacade.getCollector());	
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.api.facade.SpecimenFacade#getCollectingMethod()}.
	 */
	@Test
	public void testGetSetCollectingMethod() {
		Assert.assertEquals("Collecting method must be same", collectingMethod, specimenFacade.getCollectingMethod());
		specimenFacade.setCollectingMethod("new method");
		Assert.assertEquals("Collecting method must be 'new method'","new method", specimenFacade.getCollectingMethod());	
	}


	/**
	 * Test method for {@link eu.etaxonomy.cdm.api.facade.SpecimenFacade#getDistanceToGround()}.
	 */
	@Test
	public void testGetSetDistanceToGround() {
		Assert.assertEquals("Distance to ground must be same",distanceToGround, specimenFacade.getDistanceToGround());
		specimenFacade.setDistanceToGround(5);
		Assert.assertEquals("Distance to ground must be 5", Integer.valueOf(5), specimenFacade.getDistanceToGround());
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.api.facade.SpecimenFacade#getDistanceToWaterSurface()}.
	 */
	@Test
	public void testGetDistanceToWaterSurface() {
		Assert.assertEquals("Distance to surface must be same", distanceToSurface, specimenFacade.getDistanceToWaterSurface());
		specimenFacade.setDistanceToWaterSurface(6);
		Assert.assertEquals("Distance to surface must be 6", Integer.valueOf(6), specimenFacade.getDistanceToWaterSurface());
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.api.facade.SpecimenFacade#getExactLocation()}.
	 */
	@Test
	public void testGetExactLocation() {
		Assert.assertNotNull("Exact location must not be null", specimenFacade.getExactLocation());	
		Assert.assertEquals("Exact location must be same", exactLocation, specimenFacade.getExactLocation());	
		specimenFacade.setExactLocation(null);
		Assert.assertNull("Exact location must be null", specimenFacade.getExactLocation());	
	}


	/**
	 * Test method for {@link eu.etaxonomy.cdm.api.facade.SpecimenFacade#getGatheringEventDescription()}.
	 */
	@Test
	public void testGetSetGatheringEventDescription() {
		Assert.assertEquals("Gathering event description must be same", gatheringEventDescription, specimenFacade.getGatheringEventDescription());
		specimenFacade.setGatheringEventDescription("new description");
		Assert.assertEquals("Gathering event description must be 'new description' now","new description", specimenFacade.getGatheringEventDescription());	
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.api.facade.SpecimenFacade#getTimeperiod()}.
	 */
	@Test
	public void testGetTimeperiod() {
		Assert.assertNotNull("Gathering period must not be null", specimenFacade.getGatheringPeriod());	
		Assert.assertEquals("Gathering period must be same", gatheringPeriod, specimenFacade.getGatheringPeriod());	
		specimenFacade.setGatheringPeriod(null);
		Assert.assertNull("Gathering period must be null", specimenFacade.getGatheringPeriod());	
	}

	@Test 
	public void testHasFieldObject() throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException{
		// this test depends on the current implementation of SpecimenFacade. In future
		// field observation may not be initialized from the beginning. Than the following 
		// assert should be set to assertNull
		Assert.assertTrue("field object should not be null (depends on specimen facade initialization !!)", specimenFacade.hasFieldObject());

		Field fieldObservationField = SpecimenFacade.class.getDeclaredField("fieldObservation");
		fieldObservationField.setAccessible(true);
		fieldObservationField.set(specimenFacade, null);
		Assert.assertFalse("The field observation should be null now", specimenFacade.hasFieldObject());
	
		specimenFacade.setDistanceToGround(33);
		Assert.assertTrue("The field observation should have been created again", specimenFacade.hasFieldObject());
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.api.facade.SpecimenFacade#addFieldObjectDefinition(java.lang.String, eu.etaxonomy.cdm.model.common.Language)}.
	 */
	@Test
	public void testAddGetRemoveFieldObjectDefinition() {
		Assert.assertEquals("There should be no definition yet", 0, specimenFacade.getFieldObjectDefinition().size());
		specimenFacade.addFieldObjectDefinition("Tres interesant", Language.FRENCH());
		Assert.assertEquals("There should be exactly one definition", 1, specimenFacade.getFieldObjectDefinition().size());
		Assert.assertEquals("The French definition should be 'Tres interesant'", "Tres interesant", specimenFacade.getFieldObjectDefinition().get(Language.FRENCH()).getText());
		Assert.assertEquals("The French definition should be 'Tres interesant'", "Tres interesant", specimenFacade.getFieldObjectDefinition(Language.FRENCH()));
		specimenFacade.addFieldObjectDefinition("Sehr interessant", Language.GERMAN());
		Assert.assertEquals("There should be exactly 2 definition", 2, specimenFacade.getFieldObjectDefinition().size());
		specimenFacade.removeFieldObjectDefinition(Language.FRENCH());
		Assert.assertEquals("There should remain exactly 1 definition", 1, specimenFacade.getFieldObjectDefinition().size());
		Assert.assertEquals("The remaining German definition should be 'Sehr interessant'", "Sehr interessant", specimenFacade.getFieldObjectDefinition(Language.GERMAN()));
		specimenFacade.removeFieldObjectDefinition(Language.GERMAN());
		Assert.assertEquals("There should remain no definition", 0, specimenFacade.getFieldObjectDefinition().size());
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.api.facade.SpecimenFacade#addFieldObjectMedia(eu.etaxonomy.cdm.model.media.Media)}.
	 */
	@Test
	public void testAddGetHasRemoveFieldObjectMedia() {
		Assert.assertFalse("There should be no image gallery yet", specimenFacade.hasFieldObjectImageGallery());
		Assert.assertFalse("There should be no specimen image gallery either", specimenFacade.hasSpecimenImageGallery());
		
		List<Media> media = specimenFacade.getFieldObjectMedia();
		Assert.assertTrue("There should be an empty image gallery now", specimenFacade.hasFieldObjectImageGallery());
		Assert.assertEquals("There should be no media yet in the gallery", 0, media.size());
		
		Media media1 = Media.NewInstance();
		specimenFacade.addFieldObjectMedia(media1);
		Assert.assertEquals("There should be exactly one specimen media", 1, specimenFacade.getFieldObjectMedia().size());
		Assert.assertEquals("The only media should be media 1", media1, specimenFacade.getFieldObjectMedia().get(0));
		Assert.assertFalse("There should still no specimen image gallery exist", specimenFacade.hasSpecimenImageGallery());
		
		Media media2 = Media.NewInstance();
		specimenFacade.addFieldObjectMedia(media2);
		Assert.assertEquals("There should be exactly 2 specimen media", 2, specimenFacade.getFieldObjectMedia().size());
		Assert.assertEquals("The first media should be media1", media1, specimenFacade.getFieldObjectMedia().get(0));
		Assert.assertEquals("The second media should be media2", media2, specimenFacade.getFieldObjectMedia().get(1));
		
		specimenFacade.removeFieldObjectMedia(media1);
		Assert.assertEquals("There should be exactly one specimen media", 1, specimenFacade.getFieldObjectMedia().size());
		Assert.assertEquals("The only media should be media2", media2, specimenFacade.getFieldObjectMedia().get(0));

		specimenFacade.removeFieldObjectMedia(media1);
		Assert.assertEquals("There should still be exactly one specimen media", 1, specimenFacade.getFieldObjectMedia().size());
		
		specimenFacade.removeFieldObjectMedia(media2);
		Assert.assertEquals("There should remain no media in the gallery", 0, media.size());

	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.api.facade.SpecimenFacade#addFieldObjectMedia(eu.etaxonomy.cdm.model.media.Media)}.
	 */
	@Test
	public void testGetSetEcology() {
		Assert.assertNotNull("An empty ecology data should be created when calling getEcology()", specimenFacade.getEcologyAll());
		Assert.assertEquals("An empty ecology data should be created when calling getEcology()", 0, specimenFacade.getEcologyAll().size());
		specimenFacade.setEcology("Tres jolie ici", Language.FRENCH());
		Assert.assertEquals("Ecology data should exist for 1 language", 1, specimenFacade.getEcologyAll().size());
		Assert.assertEquals("Ecology data should be 'Tres jolie ici' for French", "Tres jolie ici", specimenFacade.getEcology(Language.FRENCH()));
		Assert.assertNull("Ecology data should be null for the default language", specimenFacade.getEcology());
		specimenFacade.setEcology("Nice here");
		Assert.assertEquals("Ecology data should exist for 2 languages", 2, specimenFacade.getEcologyAll().size());
		Assert.assertEquals("Ecology data should be 'Tres jolie ici'", "Tres jolie ici", specimenFacade.getEcology(Language.FRENCH()));
		Assert.assertEquals("Ecology data should be 'Nice here' for the default language", "Nice here", specimenFacade.getEcology());
		Assert.assertEquals("Ecology data should be 'Nice here' for english", "Nice here", specimenFacade.getEcology());
		
		specimenFacade.setEcology("Vert et rouge", Language.FRENCH());
		Assert.assertEquals("Ecology data should exist for 2 languages", 2, specimenFacade.getEcologyAll().size());
		Assert.assertEquals("Ecology data should be 'Vert et rouge'", "Vert et rouge", specimenFacade.getEcology(Language.FRENCH()));
		Assert.assertEquals("Ecology data should be 'Nice here' for the default language", "Nice here", specimenFacade.getEcology());
		
		specimenFacade.setEcology(null, Language.FRENCH());
		Assert.assertEquals("Ecology data should exist for 1 languages", 1, specimenFacade.getEcologyAll().size());
		Assert.assertEquals("Ecology data should be 'Nice here' for the default language", "Nice here", specimenFacade.getEcology());
		Assert.assertNull("Ecology data should be 'null' for French", specimenFacade.getEcology(Language.FRENCH()));
		
		specimenFacade.removeEcology(null);
		Assert.assertEquals("There should be no ecology left", 0, specimenFacade.getEcologyAll().size());
		Assert.assertNull("Ecology data should be 'null' for default language", specimenFacade.getEcology());
		
	}
	
	/**
	 * Test method for {@link eu.etaxonomy.cdm.api.facade.SpecimenFacade#getFieldNumber()}.
	 */
	@Test
	public void testGetSetFieldNumber() {
		Assert.assertEquals("Field number must be same", fieldNumber, specimenFacade.getFieldNumber());
		specimenFacade.setFieldNumber("564AB");
		Assert.assertEquals("New field number must be '564AB'", "564AB", specimenFacade.getFieldNumber());
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.api.facade.SpecimenFacade#getFieldNotes()}.
	 */
	@Test
	public void testGetSetFieldNotes()  {
		Assert.assertEquals("Field notes must be same", fieldNotes, specimenFacade.getFieldNotes());
		specimenFacade.setFieldNotes("A completely new info");
		Assert.assertEquals("New field note must be 'A completely new info'", "A completely new info", specimenFacade.getFieldNotes());
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.api.facade.SpecimenFacade#setGatheringEvent(eu.etaxonomy.cdm.model.occurrence.GatheringEvent)}.
	 */
	@Test
	public void testSetGatheringEvent() {
		GatheringEvent newGatheringEvent = GatheringEvent.NewInstance();
		newGatheringEvent.setDistanceToGround(43);
		Assert.assertFalse("The initial distance to ground should not be 43", specimenFacade.getDistanceToGround() == 43);
		specimenFacade.setGatheringEvent(newGatheringEvent);
		Assert.assertTrue("The final distance to ground should be 43", specimenFacade.getDistanceToGround() == 43);
		Assert.assertSame("The new gathering event should be 'newGatheringEvent'", newGatheringEvent, specimenFacade.getGatheringEvent());
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.api.facade.SpecimenFacade#getGatheringEvent()}.
	 */
	@Test
	public void testGetGatheringEvent() {
		Assert.assertNotNull("Gathering event must not be null", specimenFacade.getGatheringEvent());	
		Assert.assertEquals("Gathering event must be field observations gathering event", specimenFacade.getFieldObservation().getGatheringEvent(), specimenFacade.getGatheringEvent());
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.api.facade.SpecimenFacade#getIndividualCount()}.
	 */
	@Test
	public void testGetSetIndividualCount(){
		Assert.assertEquals("Individual count must be same", individualCount, specimenFacade.getIndividualCount());
		specimenFacade.setIndividualCount(4);
		Assert.assertEquals("New individual count must be '4'", Integer.valueOf(4), specimenFacade.getIndividualCount());

	}


	/**
	 * Test method for {@link eu.etaxonomy.cdm.api.facade.SpecimenFacade#getLifeStage()}.
	 */
	@Test
	public void testGetSetLifeStage(){
		Assert.assertNotNull("Life stage must not be null", specimenFacade.getLifeStage());	
		Assert.assertEquals("Life stage must be same", lifeStage, specimenFacade.getLifeStage());	
		specimenFacade.setLifeStage(null);
		Assert.assertNull("Life stage must be null", specimenFacade.getLifeStage());	
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.api.facade.SpecimenFacade#getSex()}.
	 */
	@Test
	public void testGetSetSex() {
		Assert.assertNotNull("Sex must not be null", specimenFacade.getSex());	
		Assert.assertEquals("Sex must be same", sex, specimenFacade.getSex());	
		specimenFacade.setSex(null);
		Assert.assertNull("Sex must be null", specimenFacade.getSex());	
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.api.facade.SpecimenFacade#getLocality()}.
	 */
	@Test
	public void testGetSetLocality() {
		Assert.assertEquals("Locality must be same", locality, specimenFacade.getLocality());
		specimenFacade.setLocality("A completely new place", Language.FRENCH());
		Assert.assertEquals("New locality must be 'A completely new place'", "A completely new place", specimenFacade.getLocalityText());
		Assert.assertEquals("New locality language must be French", Language.FRENCH(), specimenFacade.getLocalityLanguage());
		specimenFacade.setLocality("Another place");
		Assert.assertEquals("New locality must be 'Another place'", "Another place", specimenFacade.getLocalityText());
		Assert.assertEquals("New locality language must be default", Language.DEFAULT(), specimenFacade.getLocalityLanguage());		
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.api.facade.SpecimenFacade#addSpecimenDefinition(java.lang.String, eu.etaxonomy.cdm.model.common.Language)}.
	 */
	@Test
	public void testAddGetRemoveSpecimenDefinition() {
		Assert.assertEquals("There should be no definition yet", 0, specimenFacade.getSpecimenDefinitions().size());
		specimenFacade.addSpecimenDefinition("Tres interesant", Language.FRENCH());
		Assert.assertEquals("There should be exactly one definition", 1, specimenFacade.getSpecimenDefinitions().size());
		Assert.assertEquals("The French definition should be 'Tres interesant'", "Tres interesant", specimenFacade.getSpecimenDefinitions().get(Language.FRENCH()).getText());
		Assert.assertEquals("The French definition should be 'Tres interesant'", "Tres interesant", specimenFacade.getSpecimenDefinition(Language.FRENCH()));
		specimenFacade.addSpecimenDefinition("Sehr interessant", Language.GERMAN());
		Assert.assertEquals("There should be exactly 2 definition", 2, specimenFacade.getSpecimenDefinitions().size());
		specimenFacade.removeSpecimenDefinition(Language.FRENCH());
		Assert.assertEquals("There should remain exactly 1 definition", 1, specimenFacade.getSpecimenDefinitions().size());
		Assert.assertEquals("The remaining German definition should be 'Sehr interessant'", "Sehr interessant", specimenFacade.getSpecimenDefinition(Language.GERMAN()));
		specimenFacade.removeSpecimenDefinition(Language.GERMAN());
		Assert.assertEquals("There should remain no definition", 0, specimenFacade.getSpecimenDefinitions().size());
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.api.facade.SpecimenFacade#addDetermination(eu.etaxonomy.cdm.model.occurrence.DeterminationEvent)}.
	 */
	@Test
	public void testAddGetRemoveDetermination() {
		Assert.assertEquals("There should be no determination yet", 0, specimenFacade.getDeterminations().size());
		DeterminationEvent determinationEvent1 = DeterminationEvent.NewInstance();
		specimenFacade.addDetermination(determinationEvent1);
		Assert.assertEquals("There should be exactly one determination", 1, specimenFacade.getDeterminations().size());
		Assert.assertEquals("The only determination should be determination 1", determinationEvent1, specimenFacade.getDeterminations().iterator().next());

		
		DeterminationEvent determinationEvent2 = DeterminationEvent.NewInstance();
		specimenFacade.addDetermination(determinationEvent2);
		Assert.assertEquals("There should be exactly 2 determinations", 2, specimenFacade.getDeterminations().size());
		specimenFacade.removeDetermination(determinationEvent1);
		
		Assert.assertEquals("There should remain exactly 1 determination", 1, specimenFacade.getDeterminations().size());
		Assert.assertEquals("The remaining determinations should be determination 2", determinationEvent2, specimenFacade.getDeterminations().iterator().next());
		
		specimenFacade.removeDetermination(determinationEvent1);
		Assert.assertEquals("There should remain exactly 1 determination", 1, specimenFacade.getDeterminations().size());

		specimenFacade.removeDetermination(determinationEvent2);
		Assert.assertEquals("There should remain no definition", 0, specimenFacade.getSpecimenDefinitions().size());

	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.api.facade.SpecimenFacade#addSpecimenMedia(eu.etaxonomy.cdm.model.media.Media)}.
	 */
	@Test
	public void testAddGetHasRemoveSpecimenMedia() {
		Assert.assertFalse("There should be no image gallery yet", specimenFacade.hasSpecimenImageGallery());
		Assert.assertFalse("There should be also no field object image gallery yet", specimenFacade.hasFieldObjectImageGallery());
		
		List<Media> media = specimenFacade.getSpecimenMedia();
		Assert.assertTrue("There should be an empty image gallery now", specimenFacade.hasSpecimenImageGallery());
		Assert.assertEquals("There should be no media yet in the gallery", 0, media.size());
		
		Media media1 = Media.NewInstance();
		specimenFacade.addSpecimenMedia(media1);
		Assert.assertEquals("There should be exactly one specimen media", 1, specimenFacade.getSpecimenMedia().size());
		Assert.assertEquals("The only media should be media 1", media1, specimenFacade.getSpecimenMedia().get(0));
		Assert.assertFalse("There should be still no field object image gallery", specimenFacade.hasFieldObjectImageGallery());
		
		Media media2 = Media.NewInstance();
		specimenFacade.addSpecimenMedia(media2);
		Assert.assertEquals("There should be exactly 2 specimen media", 2, specimenFacade.getSpecimenMedia().size());
		Assert.assertEquals("The first media should be media1", media1, specimenFacade.getSpecimenMedia().get(0));
		Assert.assertEquals("The second media should be media2", media2, specimenFacade.getSpecimenMedia().get(1));
		
		specimenFacade.removeSpecimenMedia(media1);
		Assert.assertEquals("There should be exactly one specimen media", 1, specimenFacade.getSpecimenMedia().size());
		Assert.assertEquals("The only media should be media2", media2, specimenFacade.getSpecimenMedia().get(0));

		specimenFacade.removeSpecimenMedia(media1);
		Assert.assertEquals("There should still be exactly one specimen media", 1, specimenFacade.getSpecimenMedia().size());
		
		specimenFacade.removeSpecimenMedia(media2);
		Assert.assertEquals("There should remain no media in the gallery", 0, media.size());
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.api.facade.SpecimenFacade#getAccessionNumber()}.
	 */
	@Test
	public void testGetSetAccessionNumber() {
		Assert.assertEquals("Accession number must be same", accessionNumber, specimenFacade.getAccessionNumber());
		specimenFacade.setAccessionNumber("A12345693");
		Assert.assertEquals("New accession number must be 'A12345693'", "A12345693", specimenFacade.getAccessionNumber());
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.api.facade.SpecimenFacade#getCatalogNumber()}.
	 */
	@Test
	public void testGetCatalogNumber() {
		Assert.assertEquals("Catalog number must be same", catalogNumber, specimenFacade.getCatalogNumber());
		specimenFacade.setCatalogNumber("B12345693");
		Assert.assertEquals("New catalog number must be 'B12345693'", "B12345693", specimenFacade.getCatalogNumber());
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.api.facade.SpecimenFacade#getPreservation()}.
	 */
	@Test
	public void testGetPreservation() {
		Assert.assertNotNull("Preservation method must not be null", specimenFacade.getPreservationMethod());	
		Assert.assertEquals("Preservation method must be same", preservationMethod, specimenFacade.getPreservationMethod());	
		specimenFacade.setPreservationMethod(null);
		Assert.assertNull("Preservation method must be null", specimenFacade.getPreservationMethod());	
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.api.facade.SpecimenFacade#getStoredUnder()}.
	 */
	@Test
	public void testGetStoredUnder() {
		Assert.assertNotNull("Stored under name must not be null", specimenFacade.getStoredUnder());	
		Assert.assertEquals("Stored under name must be same", taxonName, specimenFacade.getStoredUnder());	
		specimenFacade.setStoredUnder(null);
		Assert.assertNull("Stored under name must be null", specimenFacade.getStoredUnder());	
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.api.facade.SpecimenFacade#getCollectorsNumber()}.
	 */
	@Test
	public void testGetSetCollectorsNumber() {
		Assert.assertEquals("Collectors number must be same", collectorsNumber, specimenFacade.getCollectorsNumber());
		specimenFacade.setCollectorsNumber("C12345693");
		Assert.assertEquals("New collectors number must be 'C12345693'", "C12345693", specimenFacade.getCollectorsNumber());
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.api.facade.SpecimenFacade#getTitleCache()}.
	 */
	@Test
	public void testGetTitleCache() {
		Assert.assertNotNull("The title cache should never return null if not protected", specimenFacade.getTitleCache());
		specimenFacade.setTitleCache(null, false);
		Assert.assertNotNull("The title cache should never return null if not protected", specimenFacade.getTitleCache());
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.api.facade.SpecimenFacade#setTitleCache(java.lang.String)}.
	 */
	@Test
	public void testSetTitleCache() {
		String testTitle = "Absdwk aksjlf";
		specimenFacade.setTitleCache(testTitle, true);
		Assert.assertEquals("Protected title cache should returns the test title", testTitle, specimenFacade.getTitleCache());
		specimenFacade.setTitleCache(testTitle, false);
		Assert.assertFalse("Unprotected title cache should not return the test title", testTitle.equals(specimenFacade.getTitleCache()));
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.api.facade.SpecimenFacade#getSpecimen()}.
	 */
	@Test
	public void testGetSpecimen() {
		Assert.assertEquals("Specimen must be same", specimen, specimenFacade.getSpecimen());	
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.api.facade.SpecimenFacade#getCollection()}.
	 */
	@Test
	public void testGetSetCollection() {
		Assert.assertNotNull("Collection must not be null", specimenFacade.getCollection());	
		Assert.assertEquals("Collection must be same", collection, specimenFacade.getCollection());	
		specimenFacade.setCollection(null);
		Assert.assertNull("Collection must be null", specimenFacade.getCollection());	
	}

}
