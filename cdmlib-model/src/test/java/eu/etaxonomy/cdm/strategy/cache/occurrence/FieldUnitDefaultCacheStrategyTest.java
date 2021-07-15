/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.strategy.cache.occurrence;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.agent.Team;
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
import eu.etaxonomy.cdm.model.occurrence.DerivationEvent;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnit;
import eu.etaxonomy.cdm.model.occurrence.FieldUnit;
import eu.etaxonomy.cdm.model.occurrence.GatheringEvent;
import eu.etaxonomy.cdm.model.term.DefinedTerm;
import eu.etaxonomy.cdm.strategy.parser.TimePeriodParser;
import eu.etaxonomy.cdm.test.TermTestBase;

/**
 * Note: this class is mostly a copy from the orignal class DerivedUnitFacadeFieldUnitCacheStrategyTest
 *       in cdmlib-service. (#9678)
 *
 * @author a.mueller
 * @since 21.06.2021
 */
public class FieldUnitDefaultCacheStrategyTest extends TermTestBase {

    @SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(FieldUnitDefaultCacheStrategyTest.class);

	private FieldUnit fieldUnit;
	private GatheringEvent gatheringEvent;
	private Integer absoluteElevation = 40;
	private Integer absoluteElevationError = 2;
	private Team collector = Team.NewInstance();
	private String collectingMethod = "Collection Method";
	private Double distanceToGround = 22.0;
	private Double distanceToSurface = 50.0;
	private ReferenceSystem referenceSystem = ReferenceSystem.WGS84();
	private Point exactLocation = Point.NewInstance(12.3, 10.567, referenceSystem, 22);
	private String gatheringEventDescription = "A nice gathering description";
	private TimePeriod gatheringPeriod = TimePeriodParser.parseString("03.05.2005");
	private String ecology = "sand dunes";
	private String plantDescription = "flowers blue";

	private String fieldNumber = "5678";
	private String fieldNotes = "such a beautiful specimen";
	private Person primaryCollector;

	private String individualCount = "1";
	private DefinedTerm lifeStage = DefinedTerm.NewStageInstance("A wonderful stage", "stage", "st");
	private DefinedTerm sex = DefinedTerm.NewSexInstance("FemaleMale", "FM", "FM");
	private LanguageString locality = LanguageString.NewInstance("Berlin-Dahlem, E side of Englerallee", Language.DEFAULT());
	private NamedArea country = Country.GERMANY();

	private DerivedUnit collectionSpecimen;
	private GatheringEvent existingGatheringEvent;
	private DerivationEvent firstDerivationEvent;
	private FieldUnit firstFieldObject;

//****************************** SET UP *****************************************/

	@Before
	public void setUp() throws Exception {
		fieldUnit = FieldUnit.NewInstance();

		fieldUnit = FieldUnit.NewInstance();
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
		secondCollector.setGivenName("Andreas");
		secondCollector.setFamilyName("Muller");
		collector.addTeamMember(secondCollector);
		Person thirdCollector = Person.NewTitledInstance("Kohlbecker");
		collector.addTeamMember(thirdCollector);
		fieldUnit.setPrimaryCollector(primaryCollector);

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


    @Test
    public void testGetTitleCache() {
        String correctCache = "Germany, Berlin-Dahlem, E side of Englerallee, alt. 40 m, 10\u00B034'1.2\"N, 12\u00B018'E (WGS84), sand dunes, 3 May 2005, Kilian 5678, A. Muller & Kohlbecker; flowers blue.";
        addEcology(fieldUnit, ecology);
        addPlantDescription(fieldUnit, plantDescription);
        Assert.assertEquals(correctCache, fieldUnit.getTitleCache());

        //freetext without unit
        String altitudeText = "approx. 40";
        fieldUnit.getGatheringEvent().setAbsoluteElevationText(altitudeText);
        String expected = correctCache.replace("alt. 40 m", "alt. "+ altitudeText);
        Assert.assertEquals(expected, fieldUnit.getTitleCache());

        //freetext with unit
        String altitudeTextM = "approx. 40 m";
        fieldUnit.getGatheringEvent().setAbsoluteElevationText(altitudeTextM);
        expected = correctCache.replace("alt. 40 m", "alt. "+ altitudeTextM);
        Assert.assertEquals(expected, fieldUnit.getTitleCache());
    }

    private void addEcology(FieldUnit fieldUnit, String ecology) {
        SpecimenDescription description = SpecimenDescription.NewInstance(fieldUnit);
        TextData textData = TextData.NewInstance(Feature.ECOLOGY(), ecology, Language.DEFAULT(), null);
        description.addElement(textData);
    }

    private void addPlantDescription(FieldUnit fieldUnit, String plantDescription) {
        SpecimenDescription description = SpecimenDescription.NewInstance(fieldUnit);
        TextData textData = TextData.NewInstance(Feature.DESCRIPTION(), plantDescription, Language.DEFAULT(), null);
        description.addElement(textData);
    }
}
