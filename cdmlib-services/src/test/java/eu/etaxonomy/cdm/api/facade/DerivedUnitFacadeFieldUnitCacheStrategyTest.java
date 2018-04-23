/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.facade;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.common.DefinedTerm;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.LanguageString;
import eu.etaxonomy.cdm.model.common.TimePeriod;
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
import eu.etaxonomy.cdm.model.occurrence.FieldUnit;
import eu.etaxonomy.cdm.model.occurrence.GatheringEvent;
import eu.etaxonomy.cdm.model.occurrence.PreservationMethod;
import eu.etaxonomy.cdm.strategy.parser.TimePeriodParser;
import eu.etaxonomy.cdm.test.TermTestBase;

/**
 * @author a.mueller
 \* @since 03.06.2010
 */

public class DerivedUnitFacadeFieldUnitCacheStrategyTest extends TermTestBase {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(DerivedUnitFacadeFieldUnitCacheStrategyTest.class);

	DerivedUnit specimen;
	DerivationEvent derivationEvent;
	FieldUnit fieldUnit;
	GatheringEvent gatheringEvent;
	Integer absoluteElevation = 40;
	Integer absoluteElevationError = 2;
	Team collector = Team.NewInstance();
	String collectingMethod = "Collection Method";
	Double distanceToGround = 22.0;
	Double distanceToSurface = 50.0;
	ReferenceSystem referenceSystem = ReferenceSystem.WGS84();
	Point exactLocation = Point.NewInstance(12.3, 10.567, referenceSystem, 22);
	String gatheringEventDescription = "A nice gathering description";
	TimePeriod gatheringPeriod = TimePeriodParser.parseString("03.05.2005");
	String ecology = "sand dunes";
	String plantDescription = "flowers blue";

	String fieldNumber = "5678";
	String fieldNotes = "such a beautiful specimen";
	Person primaryCollector;

	Integer individualCount = 1;
	DefinedTerm lifeStage = DefinedTerm.NewStageInstance("A wonderful stage", "stage", "st");
	DefinedTerm sex = DefinedTerm.NewSexInstance("FemaleMale", "FM", "FM");
	LanguageString locality = LanguageString.NewInstance("Berlin-Dahlem, E side of Englerallee", Language.DEFAULT());
	NamedArea country = Country.GERMANY();

	String exsiccatum = "Greuter, Pl. Dahlem. 456";
	String accessionNumber = "8909756";
	String catalogNumber = "UU879873590";
	TaxonName taxonName = TaxonNameFactory.NewBotanicalInstance(Rank.GENUS(), "Abies", null, null, null, null, null, null, null);
	String collectorsNumber = "234589913A34";
	Collection collection = Collection.NewInstance();

	PreservationMethod preservationMethod = PreservationMethod.NewInstance(null, "my prservation");

	DerivedUnitFacade specimenFacade;

	DerivedUnit collectionSpecimen;
	GatheringEvent existingGatheringEvent;
	DerivationEvent firstDerivationEvent;
	FieldUnit firstFieldObject;
	Media media1 = Media.NewInstance();


//****************************** SET UP *****************************************/

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
//		gatheringEvent.setAbsoluteElevationError(absoluteElevationError);
		gatheringEvent.setActor(collector);
		gatheringEvent.setCollectingMethod(collectingMethod);
		gatheringEvent.setDistanceToGround(distanceToGround);
		gatheringEvent.setDistanceToWaterSurface(distanceToSurface);
		gatheringEvent.setExactLocation(exactLocation);
		gatheringEvent.setDescription(gatheringEventDescription);

		gatheringEvent.setTimeperiod(gatheringPeriod);
		gatheringEvent.setLocality(locality);
		gatheringEvent.setCountry(country);

		fieldUnit.setFieldNumber(fieldNumber);
		fieldUnit.setFieldNotes(fieldNotes);
		fieldUnit.setIndividualCount(individualCount);
		fieldUnit.setSex(sex);
		fieldUnit.setLifeStage(lifeStage);
		primaryCollector = Person.NewTitledInstance("Kilian");
		collector.addTeamMember(primaryCollector);
		Person secondCollector = Person.NewInstance();
		secondCollector.setFirstname("Andreas");
		secondCollector.setLastname("Muller");
		collector.addTeamMember(secondCollector);
		Person thirdCollector = Person.NewTitledInstance("Kohlbecker");
		collector.addTeamMember(thirdCollector);
		fieldUnit.setPrimaryCollector(primaryCollector);

		specimen.setAccessionNumber(accessionNumber);
		specimen.setCatalogNumber(catalogNumber);
		specimen.setStoredUnder(taxonName);
		specimen.setCollection(collection);
		specimen.setPreservation(preservationMethod);
		specimen.setExsiccatum(exsiccatum);

		specimenFacade = DerivedUnitFacade.NewInstance(specimen);

		//existing specimen with 2 derivation events in line
		collectionSpecimen = DerivedUnit.NewPreservedSpecimenInstance();
		DerivedUnit middleSpecimen = DerivedUnit.NewPreservedSpecimenInstance();
		firstFieldObject = FieldUnit.NewInstance();

		//TODO maybe we should define concrete event types here
		DerivationEvent lastDerivationEvent = DerivationEvent.NewInstance(null);
		DerivationEvent middleDerivationEvent = DerivationEvent.NewInstance(null);
		firstDerivationEvent = DerivationEvent.NewInstance(null);

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
		Assert.assertEquals(correctCache, specimenFacade.innerFieldUnit().getTitleCache());

		//freetext without unit
		String altitudeText = "approx. 40";
		specimenFacade.setAbsoluteElevationText(altitudeText);
		String expected = correctCache.replace("alt. 40 m", "alt. "+ altitudeText);
		Assert.assertEquals(expected, specimenFacade.innerFieldUnit().getTitleCache());

		//freetext with unit
		String altitudeTextM = "approx. 40 m";
		specimenFacade.setAbsoluteElevationText(altitudeTextM);
		expected = correctCache.replace("alt. 40 m", "alt. "+ altitudeTextM);
		Assert.assertEquals(expected, specimenFacade.innerFieldUnit().getTitleCache());

	}


}
