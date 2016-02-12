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

import java.io.FileNotFoundException;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.hibernate.proxy.HibernateProxy;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.api.service.IDescriptionService;
import eu.etaxonomy.cdm.api.service.IOccurrenceService;
import eu.etaxonomy.cdm.api.service.ITaxonService;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.DefinedTerm;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.LanguageString;
import eu.etaxonomy.cdm.model.common.TimePeriod;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.IndividualsAssociation;
import eu.etaxonomy.cdm.model.location.Country;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.location.Point;
import eu.etaxonomy.cdm.model.location.ReferenceSystem;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.occurrence.Collection;
import eu.etaxonomy.cdm.model.occurrence.DerivationEvent;
import eu.etaxonomy.cdm.model.occurrence.DerivationEventType;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnit;
import eu.etaxonomy.cdm.model.occurrence.FieldUnit;
import eu.etaxonomy.cdm.model.occurrence.GatheringEvent;
import eu.etaxonomy.cdm.model.occurrence.PreservationMethod;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.strategy.parser.TimePeriodParser;
import eu.etaxonomy.cdm.test.integration.CdmTransactionalIntegrationTest;

/**
 * NOTE: The only reason for having this test is to test if injection of the cache strategy into a
 * standard specimen works. Once we use another default cache strategy then the derived unit facade
 * this test can be deleted or adapted and moved to cdmlib-model.
 *
 * @author a.mueller
 * @date 03.06.2010
 *
 */
public class DerivedUnitFacadeCacheStrategyInjectionTest extends CdmTransactionalIntegrationTest {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(DerivedUnitFacadeCacheStrategyInjectionTest.class);

    @SpringBeanByType
    private IOccurrenceService occService;
    @SpringBeanByType
    private ITaxonService taxonService;
    @SpringBeanByType
    private IDescriptionService descService;

    private static final UUID taxonUuid = UUID.fromString("10cfb372-0b1a-4d82-9707-c5ffd2b93a55");


    private DerivedUnit specimen;
    private DerivationEvent derivationEvent;
    private FieldUnit fieldUnit;
    private GatheringEvent gatheringEvent;
    private final Integer absoluteElevation = 40;
    private final Integer absoluteElevationError = 2;
    private final Team collector = Team.NewInstance();
    private final String collectingMethod = "Collection Method";
    private final Double distanceToGround = 22.0;
    private final Double distanceToSurface = 50.0;
	private final ReferenceSystem referenceSystem = ReferenceSystem.WGS84();
    private final Point exactLocation = Point.NewInstance(12.3, 10.567, referenceSystem, 22);
    private final String gatheringEventDescription = "A nice gathering description";
    private final TimePeriod gatheringPeriod = TimePeriodParser.parseString("03.05.2005");
    private final String ecology = "sand dunes";
    private final String plantDescription = "flowers blue";

    private final String fieldNumber = "5678";
    private final String fieldNotes = "such a beautiful specimen";
    private Person primaryCollector;

    private final Integer individualCount = 1;
	private final DefinedTerm lifeStage = DefinedTerm.NewStageInstance("A wonderful stage", "stage", "st");
	private final DefinedTerm sex = DefinedTerm.NewSexInstance("FemaleMale", "FM", "FM");
	private final NamedArea country = Country.GERMANY();
    private final LanguageString locality = LanguageString.NewInstance("Berlin-Dahlem, E side of Englerallee", Language.DEFAULT());

    private final String exsiccatum = "Greuter, Pl. Dahlem. 456";
    private final String accessionNumber = "8909756";
    private final String catalogNumber = "UU879873590";
    private final TaxonNameBase<?,?> taxonName = BotanicalName.NewInstance(Rank.GENUS(), "Abies", null, null, null, null, null, null, null);
    private final String collectorsNumber = "234589913A34";
    private final Collection collection = Collection.NewInstance();

    private final PreservationMethod preservationMethod = PreservationMethod.NewInstance(null, "my prservation");

    private DerivedUnit collectionSpecimen;
    private GatheringEvent existingGatheringEvent;
    private DerivationEvent firstDerivationEvent;
    private FieldUnit firstFieldObject;
    private final Media media1 = Media.NewInstance();


//****************************** SET UP *****************************************/

//     /**
//      * @throws java.lang.Exception
//      */
//     @BeforeClass
//     public static void setUpBeforeClass() throws Exception {
//         // FIXME maybe this will cause problems in other tests
//         // INDEED !!!! it causes problems thus this is replaced by making this test a  CdmIntegrationTest !!!
//         new DefaultTermInitializer().initialize();
//     }

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
	public void testGetSpecimenTitleCache() {
//		String correctCache = "Germany, Berlin-Dahlem, E side of Englerallee, alt. 40 m, 10\u00B034'1.2\"N, 12\u00B018'E (WGS84), sand dunes, 3.5.2005, Kilian 5678, A. Muller & Kohlbecker; Greuter, Pl. Dahlem. 456 (B 8909756); flowers blue.";
		String correctCache = "Germany, Berlin-Dahlem, E side of Englerallee, alt. 40 m, 10\u00B034'1.2\"N, 12\u00B018'E (WGS84), 3.5.2005, Kilian 5678, A. Muller & Kohlbecker; Greuter, Pl. Dahlem. 456 (B 8909756).";

//		DescriptionElementBase ecologyItem = TextData.NewInstance(Feature.ECOLOGY(), ecology, Language.DEFAULT(), null);
//		SpecimenDescription fieldUnitDescription = SpecimenDescription.NewInstance(fieldUnit);
//		fieldUnitDescription.addElement(ecologyItem);
////		specimenFacade.setEcology(ecology);
//		DescriptionElementBase plantDescItem = TextData.NewInstance(Feature.DESCRIPTION(), plantDescription, Language.DEFAULT(), null);
//		fieldUnitDescription.addElement(plantDescItem);
////		specimenFacade.setPlantDescription(plantDescription);

		collection.setCode("B");
		Assert.assertEquals(correctCache, specimen.getTitleCache());



	}

	   /**
     * Test method for {@link eu.etaxonomy.cdm.api.facade.DerivedUnitFacadeCacheStrategy#getTitleCache(eu.etaxonomy.cdm.model.occurrence.Specimen)}.
     */
    @Test
    public void testGetFieldUnitTitleCache() {
//        String correctCache = "Germany, Berlin-Dahlem, E side of Englerallee, alt. 40 m, 10\u00B034'1.2\"N, 12\u00B018'E (WGS84), sand dunes, 3.5.2005, Kilian 5678, A. Muller & Kohlbecker; flowers blue.";
        String correctCache = "Germany, Berlin-Dahlem, E side of Englerallee, alt. 40 m, 10\u00B034'1.2\"N, 12\u00B018'E (WGS84), 3.5.2005, Kilian 5678, A. Muller & Kohlbecker.";

//        DescriptionElementBase ecologyItem = TextData.NewInstance(Feature.ECOLOGY(), ecology, Language.DEFAULT(), null);
//        SpecimenDescription fieldUnitDescription = SpecimenDescription.NewInstance(fieldUnit);
//        fieldUnitDescription.addElement(ecologyItem);
//        DescriptionElementBase plantDescItem = TextData.NewInstance(Feature.DESCRIPTION(), plantDescription, Language.DEFAULT(), null);
//        fieldUnitDescription.addElement(plantDescItem);

        collection.setCode("B");  //no effect
        Assert.assertEquals(correctCache, fieldUnit.getTitleCache());
    }

    //Test if even a hibernate proxy (javassist) class correctly loads the DerivedUnitCacheStrategy.
    @Test
    @DataSet
    public void testPersistedDerivedUnit(){
        Taxon taxon = (Taxon)this.taxonService.find(taxonUuid);

        IndividualsAssociation indivAssoc = getDescriptionElement(taxon, 5000);
        SpecimenOrObservationBase<?> specimen = indivAssoc.getAssociatedSpecimenOrObservation();
        Assert.assertTrue("Specimen should be proxy otherwise the test does not test what it should", specimen instanceof HibernateProxy);
        String expectedDerivedUnitCache = "Berlin-Dahlem, E side of Englerallee, alt. 40 m, 10°34'1.2\"N, 12°18'E, 3.5.2005, Kilian 5678, A. Muller & Kohlbecker; Greuter, Pl. Dahlem. 456 (8909756).";
        Assert.assertEquals(expectedDerivedUnitCache, specimen.getTitleCache());
    }

    @Test
    @DataSet
    public void testPersistedFieldUnit(){
        Taxon taxon = (Taxon)this.taxonService.find(taxonUuid);
        IndividualsAssociation indivAssoc = getDescriptionElement(taxon, 5001);
        SpecimenOrObservationBase<?> fieldUnit = indivAssoc.getAssociatedSpecimenOrObservation();
        Assert.assertTrue("FieldUnit should be proxy otherwise the test does not test what it should", fieldUnit instanceof HibernateProxy);
        FieldUnit myFieldUnit = CdmBase.deproxy(fieldUnit, FieldUnit.class);
        myFieldUnit.setTitleCache(null);
        String expectedFieldUnitCache = "Berlin-Dahlem, E side of Englerallee, alt. 40 m, 10°34'1.2\"N, 12°18'E, 3.5.2005, Kilian 5678, A. Muller & Kohlbecker.";
        Assert.assertEquals(expectedFieldUnitCache, fieldUnit.getTitleCache());
    }



    /**
     * @param taxon
     * @param i
     * @return
     */
    private IndividualsAssociation getDescriptionElement(Taxon taxon, int id) {
        for (DescriptionElementBase el : taxon.getDescriptions().iterator().next().getElements()){
            if (el.getId() == id) {
                return (IndividualsAssociation)el;
            }
        }
        return null;
    }

    @Override
    @Test
    @Ignore
    public void createTestDataSet() throws FileNotFoundException {
//        specimen.setUuid(derivedUnitUuid);
//        fieldUnit.setUuid(fieldUnitUuid);
//        Taxon taxon = Taxon.NewInstance(null, null);
//        taxon.setUuid(taxonUuid);
//        TaxonDescription desc = TaxonDescription.NewInstance(taxon);
//
//        IndividualsAssociation indAssoc = IndividualsAssociation.NewInstance(specimen);
//        desc.addElement(indAssoc);
//
//        this.taxonService.saveOrUpdate(taxon);
//
//        setComplete();
//        endTransaction();
//
//        writeDbUnitDataSetFile(new String[]{"SpecimenOrObservationBase",
//                "DerivationEvent", "DescriptionBase","DescriptionElementBase",
//                "GatheringEvent","AgentBase","LanguageString","TaxonNameBase",
//                "TaxonBase","Collection",
//                "MaterialOrMethodEvent","SpecimenOrObservationBase_DerivationEvent"});
    }

}
