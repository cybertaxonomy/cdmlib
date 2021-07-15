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
import eu.etaxonomy.cdm.model.term.DefinedTerm;
import eu.etaxonomy.cdm.strategy.parser.TimePeriodParser;
import eu.etaxonomy.cdm.test.TermTestBase;

/**
 * @author a.mueller
 * @since 03.06.2010
 *
 * @deprecated with #9678 a similar cache strategy (DerivedUnitCacheStrategy)
 *      was implemented in cdmlib-model. This class may be removed in future.
 */
@Deprecated
public class DerivedUnitFacadeCacheStrategyTest extends TermTestBase {

    @SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(DerivedUnitFacadeCacheStrategyTest.class);

	private DerivedUnit specimen;
	private DerivationEvent derivationEvent;
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

	private String exsiccatum = "Greuter, Pl. Dahlem. 456";
	private String accessionNumber = "8909756";
	private String catalogNumber = "UU879873590";
	private String barcode = "B12345678";
	private TaxonName taxonName = TaxonNameFactory.NewBotanicalInstance(Rank.GENUS(), "Abies", null, null, null, null, null, null, null);
	private String collectorsNumber = "234589913A34";
	private Collection collection = Collection.NewInstance();

	private PreservationMethod preservationMethod = PreservationMethod.NewInstance(null, "my prservation");

	private DerivedUnitFacade specimenFacade;

	private DerivedUnit collectionSpecimen;
	private GatheringEvent existingGatheringEvent;
	private DerivationEvent firstDerivationEvent;
	private FieldUnit firstFieldObject;
	private Media media1 = Media.NewInstance();

//****************************** SET UP *****************************************/

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
		secondCollector.setGivenName("Andreas");
		secondCollector.setFamilyName("Muller");
		collector.addTeamMember(secondCollector);
		Person thirdCollector = Person.NewTitledInstance("Kohlbecker");
		collector.addTeamMember(thirdCollector);
		fieldUnit.setPrimaryCollector(primaryCollector);

		specimen.setAccessionNumber(accessionNumber);
		specimen.setCatalogNumber(catalogNumber);
		specimen.setBarcode(barcode);
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

	@Test
	public void testGetTitleCache() {
		String correctCache = "Germany, Berlin-Dahlem, E side of Englerallee, alt. 40 m, 10\u00B034'1.2\"N, 12\u00B018'E (WGS84), sand dunes, 3 May 2005, Kilian 5678, A. Muller & Kohlbecker; Greuter, Pl. Dahlem. 456 (B 8909756); flowers blue.";
		specimenFacade.setEcology(ecology);
		specimenFacade.setPlantDescription(plantDescription);
		collection.setCode("B");
		Assert.assertEquals(correctCache, specimenFacade.getTitleCache());
        collection.setCode(null);
        collection.setName("Herbarium Berolinense");
        Assert.assertEquals(correctCache.replace("B 8909756", "Herbarium Berolinense 8909756"), specimenFacade.getTitleCache());
	}

    @Test
    public void testGetTitleCacheWithEtAl() {
        String correctCache = "Germany, Berlin-Dahlem, E side of Englerallee, alt. 40 m, 10\u00B034'1.2\"N, 12\u00B018'E (WGS84), sand dunes, 3 May 2005, Kilian 5678, A. Muller, Kohlbecker & al.; Greuter, Pl. Dahlem. 456 (B 8909756); flowers blue.";
        collector.setHasMoreMembers(true);
        specimenFacade.setEcology(ecology);
        specimenFacade.setPlantDescription(plantDescription);
        collection.setCode("B");
        Assert.assertEquals(correctCache, specimenFacade.getTitleCache());
    }

    //#6381
    @Test
    public void testGetTitleCacheAccessionBarcodeCatalogNumber() {
        //Note: Collection Code B might be deduplicated in future
        specimenFacade.setPlantDescription(plantDescription);
        collection.setCode("B");
        String correctCache = "Germany, Berlin-Dahlem, E side of Englerallee, alt. 40 m, 10\u00B034'1.2\"N, 12\u00B018'E (WGS84), 3 May 2005, Kilian 5678, A. Muller & Kohlbecker; Greuter, Pl. Dahlem. 456 (B 8909756); flowers blue.";
        Assert.assertEquals(correctCache, specimenFacade.getTitleCache());
        specimenFacade.setAccessionNumber(null);
        correctCache = "Germany, Berlin-Dahlem, E side of Englerallee, alt. 40 m, 10\u00B034'1.2\"N, 12\u00B018'E (WGS84), 3 May 2005, Kilian 5678, A. Muller & Kohlbecker; Greuter, Pl. Dahlem. 456 (B B12345678); flowers blue.";
        Assert.assertEquals(correctCache, specimenFacade.getTitleCache());
        specimenFacade.setBarcode(null);
        correctCache = "Germany, Berlin-Dahlem, E side of Englerallee, alt. 40 m, 10\u00B034'1.2\"N, 12\u00B018'E (WGS84), 3 May 2005, Kilian 5678, A. Muller & Kohlbecker; Greuter, Pl. Dahlem. 456 (B UU879873590); flowers blue.";
        Assert.assertEquals(correctCache, specimenFacade.getTitleCache());
    }
}
