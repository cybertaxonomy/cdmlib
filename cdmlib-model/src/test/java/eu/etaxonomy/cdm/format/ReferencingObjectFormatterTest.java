/**
* Copyright (C) 2022 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.format;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.etaxonomy.cdm.common.UTF8;
import eu.etaxonomy.cdm.format.description.QuantitativeDataFormatter;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.ExtendedTimePeriod;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.description.CategoricalData;
import eu.etaxonomy.cdm.model.description.CommonTaxonName;
import eu.etaxonomy.cdm.model.description.Distribution;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.IndividualsAssociation;
import eu.etaxonomy.cdm.model.description.PresenceAbsenceTerm;
import eu.etaxonomy.cdm.model.description.QuantitativeData;
import eu.etaxonomy.cdm.model.description.State;
import eu.etaxonomy.cdm.model.description.StateData;
import eu.etaxonomy.cdm.model.description.StatisticalMeasure;
import eu.etaxonomy.cdm.model.description.StatisticalMeasurementValue;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.description.TaxonInteraction;
import eu.etaxonomy.cdm.model.description.TemporalData;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.location.Country;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.name.TaxonNameFactory;
import eu.etaxonomy.cdm.model.occurrence.Collection;
import eu.etaxonomy.cdm.model.occurrence.DerivationEvent;
import eu.etaxonomy.cdm.model.occurrence.DerivationEventType;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnit;
import eu.etaxonomy.cdm.model.occurrence.FieldUnit;
import eu.etaxonomy.cdm.model.occurrence.GatheringEvent;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.strategy.parser.NonViralNameParserImpl;
import eu.etaxonomy.cdm.strategy.parser.TimePeriodParser;
import eu.etaxonomy.cdm.test.TermTestBase;

/**
 * Testclass for {@link ReferencingObjectFormatter}
 * <BR> #9999
 *
 * @author a.mueller
 * @date 24.03.2022
 */
public class ReferencingObjectFormatterTest extends TermTestBase {

    private Person person1;
    private TaxonName name1;
    private Taxon taxon1;
    private TaxonDescription taxonDescription1;
    private Reference reference1;

    @BeforeEach
    public void setUp() throws Exception {
        person1 = Person.NewTitledInstance("Linne");
        name1 = NonViralNameParserImpl.NewInstance().parseReferencedName("Abies alba Mill., Sp. Pl.: 123. 1753");
        reference1 = ReferenceFactory.newBook();
        reference1.setAuthorship(person1);
        reference1.setTitle("Species plantarum");
        reference1.setDatePublished(TimePeriodParser.parseStringVerbatim("1753"));
        taxon1 = Taxon.NewInstance(name1, reference1);

        taxonDescription1 = TaxonDescription.NewInstance(taxon1);
        taxonDescription1.setTitleCache("Description for taxon1", true);
    }


    @Test
    public void testFormat_IdentifiableEntities() {
        //default is to return titleCache

        //Person
        assertEquals("Linne", defaultFormat(person1));

        //TaxonName
        assertEquals("Abies alba Mill.", defaultFormat(name1));

        //Taxon
        assertEquals("Abies alba Mill. sec. Linne 1753", defaultFormat(taxon1));

        //Reference
        assertEquals("Linne 1753: Species plantarum", defaultFormat(reference1));

        //TaxonDescription
        assertEquals("Description for taxon1", defaultFormat(taxonDescription1));

    }

    @Test
    public void testFormat_Descriptive() {

        //TextData
        TextData textData = TextData.NewInstance(Feature.DESCRIPTION());
        textData.putText(Language.ENGLISH(), "My text data");
        assertEquals("My text data", defaultFormat(textData));

        taxonDescription1.addElement(textData);
        assertEquals("My text data (Abies alba Mill. sec. Linne 1753)", defaultFormat(textData));

        //CategoricalData
        CategoricalData catData = CategoricalData.NewInstance(Feature.DESCRIPTION());
        //TODO
        assertEquals("Description[[]]", defaultFormat(catData));

        State state1 = State.NewInstance("State1", "State1", null);
        StateData stateData1 = catData.addStateData(state1);
        catData.addStateData(State.NewInstance("State2", "State2", null));
        assertEquals("State1", defaultFormat(stateData1));
        assertEquals("State1, State2", defaultFormat(catData));

        taxonDescription1.addElement(catData);
        assertEquals("State1, State2 (Abies alba Mill. sec. Linne 1753)", defaultFormat(catData));
        assertEquals("State1 (Abies alba Mill. sec. Linne 1753)", defaultFormat(stateData1));

        //Quantitative Data
        QuantitativeData quantData = QuantitativeData.NewMinMaxInstance(Feature.DESCRIPTION(), BigDecimal.valueOf(2d), BigDecimal.valueOf(4d));
        taxonDescription1.addElement(quantData);
        String sep = QuantitativeDataFormatter.lowerUpperSep;
        assertEquals("2.0"+sep+"4.0 (Abies alba Mill. sec. Linne 1753)", defaultFormat(quantData));
        StatisticalMeasurementValue minValue = quantData.getStatisticalValues().stream().filter(v->v.getType().equals(StatisticalMeasure.MIN())).findFirst().get();
        //TODO
        assertEquals("Min=2.0 (Abies alba Mill. sec. Linne 1753)", defaultFormat(minValue));


        //Common Name
        CommonTaxonName commonName = CommonTaxonName.NewInstance("Butterblume", Language.GERMAN(), NamedArea.EUROPE());
        taxonDescription1.addElement(commonName);
        assertEquals("Butterblume (Abies alba Mill. sec. Linne 1753)", defaultFormat(commonName));

        //Distribution
        Distribution distribution = Distribution.NewInstance(NamedArea.EUROPE(), PresenceAbsenceTerm.PRESENT());
        taxonDescription1.addElement(distribution);
        assertEquals("Europe, present (Abies alba Mill. sec. Linne 1753)", defaultFormat(distribution)); //TODO do we really need the status here? Should it be rather short than exact?

        //TaxonInteraction
        TaxonInteraction taxonInteraction = TaxonInteraction.NewInstance(Feature.HOSTPLANT());
        TaxonName name2 = TaxonNameFactory.NewBotanicalInstance(Rank.SPECIES(), "Hostus", null, "plantus", null, person1, reference1, null, null);
        Taxon taxon2 = Taxon.NewInstance(name2, reference1);
        taxonInteraction.setTaxon2(taxon2);
        taxonDescription1.addElement(taxonInteraction);
        assertEquals("Hostus plantus Linne (Abies alba Mill. sec. Linne 1753)", defaultFormat(taxonInteraction));

        //IndividualsAssociation
        IndividualsAssociation individualsAssociation = IndividualsAssociation.NewInstance(null);
        individualsAssociation.setAssociatedSpecimenOrObservation(getDerivedUnit());
        taxonDescription1.addElement(individualsAssociation);
        //May change to a shorter form in future
        assertEquals("Germany, 11 Apr 2011, Linne 125 (B: B555) (Abies alba Mill. sec. Linne 1753)", defaultFormat(individualsAssociation));

        //TemporalData
        ExtendedTimePeriod period = ExtendedTimePeriod.NewExtendedMonthInstance(5, 6);
        TemporalData temporalData = TemporalData.NewInstance(Feature.FLOWERING_PERIOD(), period);
        taxonDescription1.addElement(temporalData);
        assertEquals("May"+UTF8.EN_DASH+"Jun (Abies alba Mill. sec. Linne 1753)", defaultFormat(temporalData));
    }

    @Test
    public void testFormat_Occurrence() {

        //GatheringEvent
        GatheringEvent gatheringEvent = getGatheringEvent();
        assertEquals("Linne, 11 Apr 2011, Germany", defaultFormat(gatheringEvent));  //may change in future

        //DerivationEvent #10640
        DerivationEvent derivationEvent = getDerivationEvent();
        assertEquals("accessioning; Linne 125 -> B: B555", defaultFormat(derivationEvent));
    }

    private DerivedUnit getDerivedUnit() {
        DerivedUnit derivedUnit = DerivedUnit.NewPreservedSpecimenInstance();
        getFieldUnit().addDerivedUnit(derivedUnit, null);
        derivedUnit.setAccessionNumber("B555");
        derivedUnit.setCollection(Collection.NewInstance("B", "Berlin Herbar"));
        return derivedUnit;
    }

    private FieldUnit getFieldUnit() {
        FieldUnit fieldUnit = FieldUnit.NewInstance();
        fieldUnit.setGatheringEvent(getGatheringEvent());
        fieldUnit.setFieldNumber("125");
        return fieldUnit;
    }

    private GatheringEvent getGatheringEvent() {
        GatheringEvent gatheringEvent = GatheringEvent.NewInstance();
        gatheringEvent.setCountry(Country.GERMANY());
        gatheringEvent.setCollector(person1);
        gatheringEvent.setTimeperiod(TimePeriodParser.parseString("11 Apr 2011"));
        return gatheringEvent;
    }

    private DerivationEvent getDerivationEvent() {

        DerivationEvent derivationEvent = DerivationEvent.NewSimpleInstance(
                getFieldUnit(), getDerivedUnit(), DerivationEventType.ACCESSIONING());
        derivationEvent.setTimeperiod(TimePeriodParser.parseString("1972"));
        return derivationEvent;
    }

    private String defaultFormat(CdmBase cdmBase) {
        return ReferencingObjectFormatter.format(cdmBase, Language.ENGLISH());
    }

}
