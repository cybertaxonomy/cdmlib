/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.strategy.cache.occurrence;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
import eu.etaxonomy.cdm.model.occurrence.OccurrenceStatus;
import eu.etaxonomy.cdm.model.occurrence.PreservationMethod;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.model.term.DefinedTerm;
import eu.etaxonomy.cdm.strategy.parser.TimePeriodParser;
import eu.etaxonomy.cdm.test.TermTestBase;

/**
 * Note: this class is mostly a copy from the orignal class DerivedUnitFacadeCacheStrategyTest
 *       in cdmlib-service. (#9678)
 *
 * @author a.mueller
 * @since 22.06.2021
 */
public class DerivedUnitDefaultCacheStrategyTest extends TermTestBase {

    @SuppressWarnings("unused")
    private static final Logger logger = LogManager.getLogger();

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
//      gatheringEvent.setAbsoluteElevationError(absoluteElevationError);
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
        String correctCache = "Germany, Berlin-Dahlem, E side of Englerallee, alt. 40 m, 10\u00B034'1.2\"N, 12\u00B018'E (WGS84), sand dunes, 3 May 2005, Kilian 5678, A. Muller & Kohlbecker; Greuter, Pl. Dahlem. 456 (B: 8909756); flowers blue";
        addEcology(fieldUnit, ecology);
        addPlantDescription(fieldUnit, plantDescription);
        collection.setCode("B");
        Assert.assertEquals(correctCache, specimen.getTitleCache());

        //collection without code but with name
        collection.setCode(null);
        collection.setName("Herbarium Berolinense");
        Assert.assertEquals(correctCache.replace("B: 8909756", "Herbarium Berolinense: 8909756"), specimen.getTitleCache());

        //test status
        collection.setCode("B");
        Reference statusSource = ReferenceFactory.newBook(); //TODO not yet handled in cache strategy as we do not have tagged text here
        statusSource.setTitle("Status test");
        specimen.addStatus(OccurrenceStatus.NewInstance(DefinedTerm.getTermByUuid(DefinedTerm.uuidDestroyed), statusSource, "335"));
        Assert.assertEquals(correctCache.replace("8909756", "8909756, destroyed"), specimen.getTitleCache());

        //test 2 status
        specimen.addStatus(OccurrenceStatus.NewInstance(DefinedTerm.getTermByUuid(DefinedTerm.uuidLost), statusSource, "335"));
        Assert.assertEquals(correctCache.replace("8909756", "8909756, destroyed, lost"), specimen.getTitleCache());

    }

    @Test
    public void testGetTitleCacheWithEtAl() {
        String correctCache = "Germany, Berlin-Dahlem, E side of Englerallee, alt. 40 m, 10\u00B034'1.2\"N, 12\u00B018'E (WGS84), sand dunes, 3 May 2005, Kilian 5678, A. Muller, Kohlbecker & al.; Greuter, Pl. Dahlem. 456 (B: 8909756); flowers blue";
        collector.setHasMoreMembers(true);
        addEcology(fieldUnit, ecology);
        addPlantDescription(fieldUnit, plantDescription);
        collection.setCode("B");
        Assert.assertEquals(correctCache, specimen.getTitleCache());
    }

    //#6381
    @Test
    public void testGetTitleCacheAccessionBarcodeCatalogNumber() {
        addPlantDescription(fieldUnit, plantDescription);
        collection.setCode("B");
        String correctCache = "Germany, Berlin-Dahlem, E side of Englerallee, alt. 40 m, 10\u00B034'1.2\"N, 12\u00B018'E (WGS84), 3 May 2005, Kilian 5678, A. Muller & Kohlbecker; Greuter, Pl. Dahlem. 456 (B: 8909756); flowers blue";
        Assert.assertEquals(correctCache, specimen.getTitleCache());

        specimen.setAccessionNumber(null);
        correctCache = correctCache.replace("B: 8909756", "B: B12345678");
        Assert.assertEquals(correctCache, specimen.getTitleCache());

        specimen.setBarcode(null);
        correctCache = correctCache.replace("B: B12345678", "B: UU879873590");
        Assert.assertEquals(correctCache, specimen.getTitleCache());

        //no deduplication of collection code in accession number according to #6865
        specimen.setAccessionNumber("B 12345");
        correctCache = correctCache.replace("B: UU879873590", "B: B 12345");
        Assert.assertEquals(correctCache, specimen.getTitleCache());
        specimen.setAccessionNumber("B12345");
        correctCache = correctCache.replace("B 12345", "B12345");
        Assert.assertEquals(correctCache, specimen.getTitleCache());

        //but deduplication should take place if explicitly set
        specimen.setAccessionNumber("B 12345");
        specimen.setCacheStrategy(DerivedUnitDefaultCacheStrategy.NewInstance(false, false, true));
        correctCache = correctCache.replace("B: B12345", "B: 12345");
        Assert.assertEquals(correctCache, specimen.getTitleCache());

        //with trailing full stop #9849
        specimen.setCacheStrategy(DerivedUnitDefaultCacheStrategy.NewInstance(false, true, true));
        correctCache = correctCache + ".";
        Assert.assertEquals(correctCache, specimen.getTitleCache());

        //skip field unit
        specimen.setCacheStrategy(DerivedUnitDefaultCacheStrategy.NewInstance(true, false, false));
        correctCache = "Greuter, Pl. Dahlem. 456 (B: B 12345)";
        Assert.assertEquals(correctCache, specimen.getTitleCache());

    }

    @Test
    public void testDerivedUnitWithEmptyFieldUnit() {
        specimen = DerivedUnit.NewPreservedSpecimenInstance();
        derivationEvent = DerivationEvent.NewInstance(DerivationEventType.ACCESSIONING());
        specimen.setDerivedFrom(derivationEvent);
        fieldUnit = FieldUnit.NewInstance();
        fieldUnit.addDerivationEvent(derivationEvent);
        gatheringEvent = GatheringEvent.NewInstance();
        fieldUnit.setGatheringEvent(gatheringEvent);
        Assert.assertEquals("DerivedUnit#0<"+specimen.getUuid()+">", specimen.getTitleCache());

        specimen.setBarcode("B996633");
        Assert.assertEquals("(B996633)", specimen.getTitleCache());  //maybe brackets will be removed in future

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
