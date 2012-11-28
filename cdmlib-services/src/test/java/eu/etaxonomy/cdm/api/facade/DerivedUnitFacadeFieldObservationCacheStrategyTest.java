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

import junit.framework.Assert;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.LanguageString;
import eu.etaxonomy.cdm.model.common.TimePeriod;
import eu.etaxonomy.cdm.model.description.Sex;
import eu.etaxonomy.cdm.model.description.Stage;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.location.Point;
import eu.etaxonomy.cdm.model.location.ReferenceSystem;
import eu.etaxonomy.cdm.model.location.WaterbodyOrCountry;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.occurrence.Collection;
import eu.etaxonomy.cdm.model.occurrence.DerivationEvent;
import eu.etaxonomy.cdm.model.occurrence.FieldObservation;
import eu.etaxonomy.cdm.model.occurrence.GatheringEvent;
import eu.etaxonomy.cdm.model.occurrence.PreservationMethod;
import eu.etaxonomy.cdm.model.occurrence.Specimen;
import eu.etaxonomy.cdm.test.integration.CdmIntegrationTest;

/**
 * @author a.mueller
 * @date 03.06.2010
 *
 */

public class DerivedUnitFacadeFieldObservationCacheStrategyTest extends CdmIntegrationTest {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(DerivedUnitFacadeFieldObservationCacheStrategyTest.class);

	Specimen specimen;
	DerivationEvent derivationEvent;
	FieldObservation fieldObservation;
	GatheringEvent gatheringEvent;
	Integer absoluteElevation = 40;
	Integer absoluteElevationError = 2;
	Team collector = Team.NewInstance();
	String collectingMethod = "Collection Method";
	Integer distanceToGround = 22;
	Integer distanceToSurface = 50;
	ReferenceSystem referenceSystem = ReferenceSystem.WGS84();
	Point exactLocation = Point.NewInstance(12.3, 10.567, referenceSystem, 22);
	String gatheringEventDescription = "A nice gathering description";
	TimePeriod gatheringPeriod = TimePeriod.parseString("03.05.2005");
	String ecology = "sand dunes";
	String plantDescription = "flowers blue";

	String fieldNumber = "5678";
	String fieldNotes = "such a beautiful specimen";
	Person primaryCollector;

	Integer individualCount = 1;
	Stage lifeStage = Stage.NewInstance("A wonderful stage", "stage", "st");
	Sex sex = Sex.NewInstance("FemaleMale", "FM", "FM");
	LanguageString locality = LanguageString.NewInstance("Berlin-Dahlem, E side of Englerallee", Language.DEFAULT());
	NamedArea country = WaterbodyOrCountry.GERMANY();

	String exsiccatum = "Greuter, Pl. Dahlem. 456";
	String accessionNumber = "8909756";
	String catalogNumber = "UU879873590";
	TaxonNameBase taxonName = BotanicalName.NewInstance(Rank.GENUS(), "Abies", null, null, null, null, null, null, null);
	String collectorsNumber = "234589913A34";
	Collection collection = Collection.NewInstance();

	PreservationMethod preservationMethod = PreservationMethod.NewInstance("my prservation", null, null);

	DerivedUnitFacade specimenFacade;

	Specimen collectionSpecimen;
	GatheringEvent existingGatheringEvent;
	DerivationEvent firstDerivationEvent;
	FieldObservation firstFieldObject;
	Media media1 = Media.NewInstance();


//****************************** SET UP *****************************************/

	/**
	 * @throws java.lang.Exception
	 */
//	@BeforeClass
//	public static void setUpBeforeClass() throws Exception {
		// FIXME maybe this will cause problems in other tests
		//		new DefaultTermInitializer().initialize();
		// INDEED !!!! it causes problems thus this is replaced by making this test a  CdmIntegrationTest !!!
//	}

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
		gatheringEvent.setCountry(country);

		fieldObservation.setFieldNumber(fieldNumber);
		fieldObservation.setFieldNotes(fieldNotes);
		fieldObservation.setIndividualCount(individualCount);
		fieldObservation.setSex(sex);
		fieldObservation.setLifeStage(lifeStage);
		primaryCollector = Person.NewTitledInstance("Kilian");
		collector.addTeamMember(primaryCollector);
		Person secondCollector = Person.NewInstance();
		secondCollector.setFirstname("Andreas");
		secondCollector.setLastname("Muller");
		collector.addTeamMember(secondCollector);
		Person thirdCollector = Person.NewTitledInstance("Kohlbecker");
		collector.addTeamMember(thirdCollector);
		fieldObservation.setPrimaryCollector(primaryCollector);

		specimen.setAccessionNumber(accessionNumber);
		specimen.setCatalogNumber(catalogNumber);
		specimen.setStoredUnder(taxonName);
		specimen.setCollection(collection);
		specimen.setPreservation(preservationMethod);
		specimen.setExsiccatum(exsiccatum);

		specimenFacade = DerivedUnitFacade.NewInstance(specimen);

		//existing specimen with 2 derivation events in line
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

	}
	/**
	 * Test method for {@link eu.etaxonomy.cdm.api.facade.DerivedUnitFacadeCacheStrategy#getTitleCache(eu.etaxonomy.cdm.model.occurrence.Specimen)}.
	 */
	@Test
	public void testGetTitleCache() {
		String correctCache = "Germany, Berlin-Dahlem, E side of Englerallee, alt. 40 m, 10\u00B034'1.2\"N, 12\u00B018'E (WGS84), sand dunes, 3.5.2005, Kilian 5678, A. Muller & Kohlbecker; flowers blue.";
		specimenFacade.setEcology(ecology);
		specimenFacade.setPlantDescription(plantDescription);
		collection.setCode("B");
		Assert.assertEquals(correctCache, specimenFacade.innerFieldObservation().getTitleCache());
	}

}
