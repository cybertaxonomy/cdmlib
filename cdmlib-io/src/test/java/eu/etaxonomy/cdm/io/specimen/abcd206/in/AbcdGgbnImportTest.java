/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.specimen.abcd206.in;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.ParseException;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.dbunit.annotation.DataSets;
import org.unitils.spring.annotation.SpringBeanByName;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.api.service.IOccurrenceService;
import eu.etaxonomy.cdm.api.service.IReferenceService;
import eu.etaxonomy.cdm.api.service.ITaxonNodeService;
import eu.etaxonomy.cdm.api.service.ITaxonService;
import eu.etaxonomy.cdm.api.service.ITermService;
import eu.etaxonomy.cdm.api.service.config.FindOccurrencesConfigurator;
import eu.etaxonomy.cdm.io.common.CdmApplicationAwareDefaultImport;
import eu.etaxonomy.cdm.model.common.DefinedTerm;
import eu.etaxonomy.cdm.model.media.MediaUtils;
import eu.etaxonomy.cdm.model.molecular.Amplification;
import eu.etaxonomy.cdm.model.molecular.AmplificationResult;
import eu.etaxonomy.cdm.model.molecular.DnaQuality;
import eu.etaxonomy.cdm.model.molecular.DnaSample;
import eu.etaxonomy.cdm.model.molecular.Primer;
import eu.etaxonomy.cdm.model.molecular.Sequence;
import eu.etaxonomy.cdm.model.molecular.SequenceDirection;
import eu.etaxonomy.cdm.model.molecular.SequenceString;
import eu.etaxonomy.cdm.model.molecular.SingleRead;
import eu.etaxonomy.cdm.model.occurrence.DerivationEvent;
import eu.etaxonomy.cdm.model.occurrence.DerivationEventType;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnit;
import eu.etaxonomy.cdm.model.occurrence.FieldUnit;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationType;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.persistence.query.MatchMode;
import eu.etaxonomy.cdm.test.integration.CdmTransactionalIntegrationTest;
import eu.etaxonomy.cdm.test.unitils.CleanSweepInsertLoadStrategy;

/**
 * @author a.mueller
 * @created 29.01.2009
 */
public class AbcdGgbnImportTest extends CdmTransactionalIntegrationTest {

	@SpringBeanByName
	private CdmApplicationAwareDefaultImport<?> defaultImport;

	@SpringBeanByType
	private IOccurrenceService occurrenceService;

	@SpringBeanByType
	private IReferenceService referenceService;

	@SpringBeanByType
	private ITermService termService;

	@SpringBeanByType
	private ITaxonService taxonService;

	@SpringBeanByType
	private ITaxonNodeService taxonNodeService;

	/**
	 * Tests import import of two DNA unit belonging to two different taxa
	 * @throws ParseException
	 */
	@Test
	@DataSet( value="../../../BlankDataSet.xml", loadStrategy=CleanSweepInsertLoadStrategy.class)
	public void testImportTwoDnaUnitsWithTwoTaxa() throws ParseException {
	    String inputFile = "/eu/etaxonomy/cdm/io/specimen/abcd206/in/Campanula_2taxa.xml";
        URL url = this.getClass().getResource(inputFile);
        assertNotNull("URL for the test file '" + inputFile + "' does not exist", url);

        Abcd206ImportConfigurator importConfigurator = null;
        try {
            importConfigurator = Abcd206ImportConfigurator.NewInstance(url.toURI(), null,false);
        } catch (URISyntaxException e) {
            e.printStackTrace();
            Assert.fail();
        }
        assertNotNull("Configurator could not be created", importConfigurator);

        boolean result = defaultImport.invoke(importConfigurator);
        assertTrue("Return value for import.invoke should be true", result);
        assertEquals("Number of derived units is incorrect", 4, occurrenceService.count(DerivedUnit.class));
        assertEquals("Number of dna samples is incorrect", 2, occurrenceService.count(DnaSample.class));
        assertEquals("Number of field units is incorrect", 2, occurrenceService.count(FieldUnit.class));
        /*
         * Default classification
         *  - Campanula
         *   - Campanula glomerata
         *   - Campanula bononiensis
         */
        assertEquals("Number of taxon nodes is incorrect", 4, taxonNodeService.count(TaxonNode.class));
        assertEquals("Number of taxa is incorrect", 3, taxonService.count(TaxonBase.class));
        assertEquals(1, taxonService.findByTitle(Taxon.class, "Campanula bononiensis", MatchMode.ANYWHERE, null, null, null, null, null).getRecords().size());
        assertEquals(1, taxonService.findByTitle(Taxon.class, "Campanula glomerata", MatchMode.ANYWHERE, null, null, null, null, null).getRecords().size());

	}
	/**
	 * Tests import import of 59 DNA unit
	 */
	@Test
	@Ignore
	@DataSet( value="../../../BlankDataSet.xml", loadStrategy=CleanSweepInsertLoadStrategy.class)
	public void testImport59Units() {
	    String inputFile = "/eu/etaxonomy/cdm/io/specimen/abcd206/in/Campanula_59taxa.xml";
        URL url = this.getClass().getResource(inputFile);
        assertNotNull("URL for the test file '" + inputFile + "' does not exist", url);

        Abcd206ImportConfigurator importConfigurator = null;
        try {
            importConfigurator = Abcd206ImportConfigurator.NewInstance(url.toURI(), null,false);
        } catch (URISyntaxException e) {
            e.printStackTrace();
            Assert.fail();
        }
        assertNotNull("Configurator could not be created", importConfigurator);

        importConfigurator.setIgnoreAuthorship(true);
        boolean result = defaultImport.invoke(importConfigurator);
        assertTrue("Return value for import.invoke should be true", result);
        assertEquals("Number of derived units is incorrect", 118, occurrenceService.count(DerivedUnit.class));
        assertEquals("Number of dna samples is incorrect", 59, occurrenceService.count(DnaSample.class));

	}

	/**
	 * Tests import import of DNA unit and all its parameters
	 * and sub derivatives (sequence, amplification, etc.)
	 * @throws ParseException
	 */
	@Test
    @DataSet( value="../../../BlankDataSet.xml", loadStrategy=CleanSweepInsertLoadStrategy.class)
    public void testImportGgbn() throws ParseException {
        String inputFile = "/eu/etaxonomy/cdm/io/specimen/abcd206/in/db6_without_association.xml";
        URL url = this.getClass().getResource(inputFile);
        assertNotNull("URL for the test file '" + inputFile + "' does not exist", url);

        Abcd206ImportConfigurator importConfigurator = null;
        try {
            importConfigurator = Abcd206ImportConfigurator.NewInstance(url.toURI(), null,false);
        } catch (URISyntaxException e) {
            e.printStackTrace();
            Assert.fail();
        }
        assertNotNull("Configurator could not be created", importConfigurator);

        boolean result = defaultImport.invoke(importConfigurator);
        assertTrue("Return value for import.invoke should be true", result);
        assertEquals("Number of derived units is incorrect", 1, occurrenceService.count(DerivedUnit.class));
        assertEquals("Number of dna samples is incorrect", 1, occurrenceService.count(DnaSample.class));
        assertEquals("Number of field units is incorrect", 1, occurrenceService.count(FieldUnit.class));

        //dna sample
        FindOccurrencesConfigurator dnaConfig = new FindOccurrencesConfigurator();
        dnaConfig.setSignificantIdentifier("DB 6");
        List<SpecimenOrObservationBase> dnaRecords = occurrenceService.findByTitle(dnaConfig).getRecords();
        assertEquals(1, dnaRecords.size());
        SpecimenOrObservationBase dnaSpecimen = dnaRecords.iterator().next();
        assertEquals(DnaSample.class, dnaSpecimen.getClass());
        DnaSample dnaSample = (DnaSample) dnaSpecimen;
        DerivationEvent derivedFrom = dnaSample.getDerivedFrom();
        assertNotNull(derivedFrom);
        assertEquals("Wrong derivation type!", DerivationEventType.DNA_EXTRACTION(), derivedFrom.getType());
        assertEquals("Wrong number of originals", 1, derivedFrom.getOriginals().size());
        assertTrue(derivedFrom.getOriginals().iterator().next() instanceof FieldUnit);
        assertEquals("DNA Bank", dnaSample.getCollection().getCode());
        assertEquals(SpecimenOrObservationType.DnaSample, dnaSample.getRecordBasis());
        //preservation/preparation
        assertNotNull(derivedFrom.getActor());
        assertEquals("Bansemer, Jana", derivedFrom.getActor().getTitleCache());
        assertNotNull(derivedFrom.getTimeperiod());
        assertEquals((Integer)2002,derivedFrom.getTimeperiod().getStartYear());
        assertEquals((Integer)8,derivedFrom.getTimeperiod().getStartMonth());
        assertEquals((Integer)13,derivedFrom.getTimeperiod().getStartDay());
        assertNotNull(dnaSample.getPreservation());
        assertEquals("DNeasy Plant Mini Spin Kit Qiagen", derivedFrom.getDescription());
        //sample designation
        Set<String> identifiers = dnaSample.getIdentifiers((DefinedTerm) termService.find(UUID.fromString("fadeba12-1be3-4bc7-9ff5-361b088d86fc")));
        assertNotNull(identifiers);
        assertEquals(1, identifiers.size());
        assertEquals("CAM010", identifiers.iterator().next());


        //dna quality
        DnaQuality dnaQuality = dnaSample.getDnaQuality();
        assertNotNull("Dna quality is null", dnaQuality!=null);
        assertEquals(new Double("0.77"),dnaQuality.getRatioOfAbsorbance260_230());
        assertEquals(new Double("1.38"),dnaQuality.getRatioOfAbsorbance260_280());
        assertEquals(new DateTime(2008, 4, 15, 0, 0),dnaQuality.getQualityCheckDate());
//        assertEquals(MeasurementUnit.NewInstance(, label, labelAbbrev)DateTime(2008, 4, 15, 0, 0),dnaQuality.getQualityCheckDate());

        //amplifications
        Set<AmplificationResult> amplificationResults = dnaSample.getAmplificationResults();
        assertNotNull(amplificationResults);
        assertEquals(1,  amplificationResults.size());
        AmplificationResult amplificationResult = amplificationResults.iterator().next();
        Amplification amplification = amplificationResult.getAmplification();
        assertNotNull("Amplification is null", amplification);
        DefinedTerm dnaMarker = amplification.getDnaMarker();
        assertNotNull(dnaMarker);
        assertEquals("ITS (ITS1, 5.8S rRNA, ITS2)", dnaMarker.getLabel());

        //amplification primers
        Primer forwardPrimer = amplification.getForwardPrimer();
        assertNotNull(forwardPrimer);
        assertEquals("PIpetB1411F", forwardPrimer.getLabel());
        assertEquals("5´-GCCGTMTTTATGTTAATGC-3´", forwardPrimer.getSequence().getString());
        assertNotNull(forwardPrimer.getPublishedIn());
        assertEquals("Löhne & Borsch 2005", forwardPrimer.getPublishedIn().getTitle());

        Primer reversePrimer = amplification.getReversePrimer();
        assertNotNull(reversePrimer);
        assertEquals("PIpetD738R", reversePrimer.getLabel());
        assertEquals("5´-AATTTAGCYCTTAATACAGG-3´", reversePrimer.getSequence().getString());

        //sequencing
        Set<Sequence> sequences = dnaSample.getSequences();
        assertNotNull(sequences);
        assertEquals(1, sequences.size());
        Sequence sequence = sequences.iterator().next();
        SequenceString consensusSequence = sequence.getConsensusSequence();
        assertNotNull(consensusSequence);
        assertEquals(
                "TTTCGGGTCC TTTATAGTGA AGATATAGCA TAGATAGTTG TAATCCATTA" +
        		" TGTATCATTG GGGAAGGAAG GAGAATATTT TTTTGATAGA ATACAAGTAT" +
        		" GGATTATTGA AACTAATACG CCATGTATTT GGATATTTCC CTTGAACTGC" +
        		" ATAATATTCT TTATTTTCCA TGAATAGTGT AAGGGAATTT TTCGAAGAGA" +
        		" AAATGGATTA TGGGAGTGTG TGACTTGAGC TATTGATTGG TCTGTGCAGA" +
        		" TACGGGCTTT TATCTATCTG CCACATTGTA ATTCACAAAC CAATGTGTCT" +
        		" TTGTTCCAAC CATCGCGTAA GCCCCATACA GAAGATAGGC TGGTTCGCTT" +
        		" GAAGAGAATC TTTTCTATGA TCAGATCCGA ATTATGTCGT ACATGAGCAG" +
        		" GCTCCGTAAG ATCTAGTTGA CTTAAGTCAA ACTTCAATAG TATAAAAATG" +
        		" CACTCATTTC CTCTGCATTG ACACGAGCTA TGAGACTATC GGAGTGAAAG" +
        		" AAAGGGTCTA AAGAAGAAGA AAGCTTGGGC TAGATTAGTA ACAAGTAAAT" +
        		" CCTTTGTGTG TGTGTTTGTA ATTAGTAAAT GGGCTCTCAA TATTTTGGGG" +
        		" CTAATTACTG ATCCTAAGGT TTGAGACGAC CCAGAAAGCA CTTGATCATA" +
        		" TCACGATTGA CTTTGTAAGC CTACTTGGGT ATTGAGTATT TACTTGTAAG" +
        		" AACCGAATTC TTTGGGGGAT AGTTGCAAAA AGAATCCAGT CAATTGTTCT" +
        		" TACGTAAAAC CATTCATATC TCGTATATGG ATATGTCTAG ATAGGCTATC" +
        		" GATTTTCGAT GGATTCGTTT GGTTCTTTTG ATTATTGCTC GAGCTGGATG" +
        		" ATGAAAAATT ATCATGTCCG GTTCCTTCG",consensusSequence.getString());
//        assertEquals((Integer)912, consensusSequence.getLength());
        assertNotNull(sequence.getContigFile());
        assertEquals(URI.create("http://ww2.biocase.org/websvn/filedetails.php?repname=campanula&path=%2FCAM385_Campa_drabifolia.pde"), MediaUtils.getFirstMediaRepresentationPart(sequence.getContigFile()).getUri());
        assertEquals(1, sequence.getCitations().size());
        Reference<?> reference = sequence.getCitations().iterator().next();
        assertEquals("Gemeinholzer,B., Bachmann,K. (2005): Examining morphological "
                + "and molecular diagnostic character states in "
                + "Cichorium intybus L. (Asteraceae) and Cichorium spinosum L."
                + " Plant Systematics and Evolution 253 (1-3): 105-123.", reference.getTitle());

        //single reads
        Set<SingleRead> singleReads = sequence.getSingleReads();
        assertNotNull(singleReads);
        assertEquals(2, singleReads.size());
        for (SingleRead singleRead : singleReads) {
            if(singleRead.getDirection().equals(SequenceDirection.Forward)){
                assertNotNull(singleRead.getPherogram());
                assertEquals(URI.create("http://ww2.biocase.org/websvn/filedetails.php?repname=campanula&path=%2FCAM385_GM312-petD_F.ab1"), MediaUtils.getFirstMediaRepresentationPart(singleRead.getPherogram()).getUri());
            }
            else{
                assertNotNull(singleRead.getPherogram());
                assertEquals(URI.create("http://ww2.biocase.org/websvn/filedetails.php?repname=campanula&path=%2FCAM385_GM312-petD_R.ab1"), MediaUtils.getFirstMediaRepresentationPart(singleRead.getPherogram()).getUri());
            }
        }
        assertNotNull(amplificationResult.getSingleReads());
        assertEquals(amplificationResult.getSingleReads(), singleReads);


	}

	/**
	 * Tests import of DNA unit which is associated to a specimen being its parent derivative
	 * @throws ParseException
	 */
	@Test
	@DataSet( value="../../../BlankDataSet.xml", loadStrategy=CleanSweepInsertLoadStrategy.class)
	public void testImportAssociatedSpecimenSameIndividual() throws ParseException {
	    String inputFile = "/eu/etaxonomy/cdm/io/specimen/abcd206/in/db6_parent_child_association.xml";
	    URL url = this.getClass().getResource(inputFile);
	    assertNotNull("URL for the test file '" + inputFile + "' does not exist", url);

	    Abcd206ImportConfigurator importConfigurator = null;
	    try {
	        importConfigurator = Abcd206ImportConfigurator.NewInstance(url.toURI(), null,false);
	    } catch (URISyntaxException e) {
	        e.printStackTrace();
	        Assert.fail();
	    }
	    assertNotNull("Configurator could not be created", importConfigurator);

	    boolean result = defaultImport.invoke(importConfigurator);
	    assertTrue("Return value for import.invoke should be true", result);
	    assertEquals("Number of derived units is incorrect", 2, occurrenceService.count(DerivedUnit.class));
	    assertEquals("Number of dna samples is incorrect", 1, occurrenceService.count(DnaSample.class));
	    assertEquals("Number of field units is incorrect", 1, occurrenceService.count(FieldUnit.class));

	    //associated specimen
	    FindOccurrencesConfigurator config = new FindOccurrencesConfigurator();
	    config.setSignificantIdentifier("B 10 0066577");
	    List<SpecimenOrObservationBase> records = occurrenceService.findByTitle(config).getRecords();
	    assertEquals(1, records.size());
	    SpecimenOrObservationBase derivedUnitSpecimen = records.iterator().next();
	    assertEquals(DerivedUnit.class, derivedUnitSpecimen.getClass());
	    DerivedUnit specimen = (DerivedUnit) derivedUnitSpecimen;
	    assertEquals("Herbarium Berolinense", specimen.getCollection().getCode());
	    assertTrue(SpecimenOrObservationType.DnaSample!=specimen.getRecordBasis());

	    //dna sample
	    FindOccurrencesConfigurator dnaConfig = new FindOccurrencesConfigurator();
	    dnaConfig.setSignificantIdentifier("DB 6");
	    List<SpecimenOrObservationBase> dnaRecords = occurrenceService.findByTitle(dnaConfig).getRecords();
	    assertEquals(1, dnaRecords.size());
	    SpecimenOrObservationBase dnaSpecimen = dnaRecords.iterator().next();
	    assertEquals(DnaSample.class, dnaSpecimen.getClass());
	    DnaSample dnaSample = (DnaSample) dnaSpecimen;
	    DerivationEvent derivedFrom = dnaSample.getDerivedFrom();
	    assertNotNull(derivedFrom);
	    assertEquals("Wrong derivation type!", DerivationEventType.DNA_EXTRACTION(), derivedFrom.getType());
	    assertEquals("Wrong number of originals", 1, derivedFrom.getOriginals().size());
	    assertTrue(derivedFrom.getOriginals().iterator().next() instanceof DerivedUnit);
	    assertEquals("DNA Bank", dnaSample.getCollection().getCode());
	    assertEquals(SpecimenOrObservationType.DnaSample, dnaSample.getRecordBasis());
	}

	/**
     * Tests import of DNA unit which is associated to a specimen being its sibling
     * by having the same field unit
	 * @throws ParseException
	 */
	@Test
	@DataSet( value="../../../BlankDataSet.xml", loadStrategy=CleanSweepInsertLoadStrategy.class)
	public void testImportAssociatedSpecimenSamePopulation() throws ParseException {
	    String inputFile = "/eu/etaxonomy/cdm/io/specimen/abcd206/in/db6_sibling_association.xml";
	    URL url = this.getClass().getResource(inputFile);
	    assertNotNull("URL for the test file '" + inputFile + "' does not exist", url);

	    Abcd206ImportConfigurator importConfigurator = null;
	    try {
	        importConfigurator = Abcd206ImportConfigurator.NewInstance(url.toURI(), null,false);
	    } catch (URISyntaxException e) {
	        e.printStackTrace();
	        Assert.fail();
	    }
	    assertNotNull("Configurator could not be created", importConfigurator);

	    boolean result = defaultImport.invoke(importConfigurator);
	    assertTrue("Return value for import.invoke should be true", result);
	    assertEquals("Number of derived units is incorrect", 2, occurrenceService.count(DerivedUnit.class));
	    assertEquals("Number of dna samples is incorrect", 1, occurrenceService.count(DnaSample.class));
	    assertEquals("Number of field units is incorrect", 1, occurrenceService.count(FieldUnit.class));

	    //associated specimen
	    FindOccurrencesConfigurator config = new FindOccurrencesConfigurator();
	    config.setSignificantIdentifier("B 10 0066577");
	    List<SpecimenOrObservationBase> records = occurrenceService.findByTitle(config).getRecords();
	    assertEquals(1, records.size());
	    SpecimenOrObservationBase derivedUnitSpecimen = records.iterator().next();
	    assertEquals(DerivedUnit.class, derivedUnitSpecimen.getClass());
	    DerivedUnit specimen = (DerivedUnit) derivedUnitSpecimen;
	    assertEquals("Herbarium Berolinense", specimen.getCollection().getCode());
	    assertTrue(SpecimenOrObservationType.DnaSample!=specimen.getRecordBasis());

	    //dna sample
	    FindOccurrencesConfigurator dnaConfig = new FindOccurrencesConfigurator();
	    dnaConfig.setSignificantIdentifier("DB 6");
	    List<SpecimenOrObservationBase> dnaRecords = occurrenceService.findByTitle(dnaConfig).getRecords();
	    assertEquals(1, dnaRecords.size());
	    SpecimenOrObservationBase dnaSpecimen = dnaRecords.iterator().next();
	    assertEquals(DnaSample.class, dnaSpecimen.getClass());
	    DnaSample dnaSample = (DnaSample) dnaSpecimen;
	    DerivationEvent derivedFrom = dnaSample.getDerivedFrom();
	    assertNotNull(derivedFrom);
	    assertEquals("Wrong derivation type!", DerivationEventType.DNA_EXTRACTION(), derivedFrom.getType());
	    assertEquals("Wrong number of originals", 1, derivedFrom.getOriginals().size());
	    assertTrue(derivedFrom.getOriginals().iterator().next() instanceof FieldUnit);
	    assertEquals("DNA Bank", dnaSample.getCollection().getCode());
	    assertEquals(SpecimenOrObservationType.DnaSample, dnaSample.getRecordBasis());

	    //TODO field unit
	}

    /**
     * Tests import of DNA unit and attaching it to an existing specimen to
     * which it has a parent-child UnitAssociation. The derived unit should not
     * be imported because it already exists in the data base. The field unit
     * should not be overwritten by the FieldUnit of the DnaSample.
     */
	@Test
    @DataSets({
        @DataSet(loadStrategy=CleanSweepInsertLoadStrategy.class, value="../../../BlankDataSet.xml"),
        @DataSet( value="AbcdGgbnImportTest.testAttachDnaSampleToDerivedUnit.xml", loadStrategy=CleanSweepInsertLoadStrategy.class)
    })
	public void testAttachDnaSampleToExistingDerivedUnit_parentChild(){
	    UUID fieldUnit1Uuid = UUID.fromString("0f896630-48d6-4352-9c91-278be28ce19c");
	    UUID derivedUnit1Uuid = UUID.fromString("eb40cb0f-efb2-4985-819e-a9168f6d61fe");

	    String inputFile = "/eu/etaxonomy/cdm/io/specimen/abcd206/in/db6_parent_child_association.xml";
	    URL url = this.getClass().getResource(inputFile);
	    assertNotNull("URL for the test file '" + inputFile + "' does not exist", url);

	    Abcd206ImportConfigurator importConfigurator = null;
	    try {
	        importConfigurator = Abcd206ImportConfigurator.NewInstance(url.toURI(), null,false);
	    } catch (URISyntaxException e) {
	        e.printStackTrace();
	        Assert.fail();
	    }
	    assertNotNull("Configurator could not be created", importConfigurator);

	    boolean result = defaultImport.invoke(importConfigurator);
	    assertTrue("Return value for import.invoke should be true", result);occurrenceService.list(DerivedUnit.class, null, null, null, null);
	    assertEquals("Number of derived units is incorrect", 2, occurrenceService.count(DerivedUnit.class));
	    List<DerivedUnit> derivedUnits = occurrenceService.list(DerivedUnit.class, null, null, null, null);
	    assertEquals("Number of derived units is incorrect", 2, derivedUnits.size());
	    assertEquals("Number of dna samples is incorrect", 1, occurrenceService.count(DnaSample.class));
	    assertEquals("Number of field units is incorrect", 1, occurrenceService.count(FieldUnit.class));


	    DerivedUnit derivedUnit = (DerivedUnit) occurrenceService.load(derivedUnit1Uuid);
	    assertTrue(derivedUnits.contains(derivedUnit));

	    assertEquals("Number of dna samples is incorrect", 1, occurrenceService.count(DnaSample.class));
	    DnaSample dnaSample = occurrenceService.list(DnaSample.class, null, null, null, null).get(0);
	    assertEquals("Wrong derivation type!", DerivationEventType.DNA_EXTRACTION(), dnaSample.getDerivedFrom().getType());

	    assertEquals("Wrong number of originals", 1, dnaSample.getDerivedFrom().getOriginals().size());

	    FieldUnit specimenFieldUnit = (FieldUnit) occurrenceService.load(fieldUnit1Uuid);
	    Collection<FieldUnit> fieldUnits = occurrenceService.getFieldUnits(dnaSample.getUuid());
	    assertEquals(1, fieldUnits.size());
	    FieldUnit dnaSampleFieldUnit = fieldUnits.iterator().next();
        assertEquals(specimenFieldUnit, dnaSampleFieldUnit);
        assertEquals("fieldUnit1", dnaSampleFieldUnit.getTitleCache());

	}

	/**
	 * Tests import of DNA unit and attaching it to an existing specimen to which
	 * it has a sibling UnitAssociation. The derived unit should not be imported because it already exists in the data base.
	 * The DnaSample should be attached to the existing FieldUnit of the DerivedUnit
	 */
	@Test
	@DataSets({
	    @DataSet(loadStrategy=CleanSweepInsertLoadStrategy.class, value="../../../BlankDataSet.xml"),
	    @DataSet( value="AbcdGgbnImportTest.testAttachDnaSampleToDerivedUnit.xml", loadStrategy=CleanSweepInsertLoadStrategy.class)
	})
	public void testAttachDnaSampleToExistingDerivedUnit_sibling(){
        UUID fieldUnit1Uuid = UUID.fromString("0f896630-48d6-4352-9c91-278be28ce19c");
	    UUID derivedUnit1Uuid = UUID.fromString("eb40cb0f-efb2-4985-819e-a9168f6d61fe");

	    String inputFile = "/eu/etaxonomy/cdm/io/specimen/abcd206/in/db6_parent_child_association.xml";
	    URL url = this.getClass().getResource(inputFile);
	    assertNotNull("URL for the test file '" + inputFile + "' does not exist", url);

	    Abcd206ImportConfigurator importConfigurator = null;
	    try {
	        importConfigurator = Abcd206ImportConfigurator.NewInstance(url.toURI(), null,false);
	    } catch (URISyntaxException e) {
	        e.printStackTrace();
	        Assert.fail();
	    }
	    assertNotNull("Configurator could not be created", importConfigurator);

	    boolean result = defaultImport.invoke(importConfigurator);
	    assertTrue("Return value for import.invoke should be true", result);
	    assertEquals("Number of derived units is incorrect", 2, occurrenceService.count(DerivedUnit.class));
	    List<DerivedUnit> derivedUnits = occurrenceService.list(DerivedUnit.class, null, null, null, null);
	    assertEquals("Number of derived units is incorrect", 2, derivedUnits.size());
	    assertEquals("Number of field units is incorrect", 1, occurrenceService.count(FieldUnit.class));
	    assertEquals("Number of dna samples is incorrect", 1, occurrenceService.count(DnaSample.class));

	    DerivedUnit derivedUnit = (DerivedUnit) occurrenceService.load(derivedUnit1Uuid);
	    assertTrue(derivedUnits.contains(derivedUnit));

	    assertEquals("Number of dna samples is incorrect", 1, occurrenceService.count(DnaSample.class));
	    DnaSample dnaSample = occurrenceService.list(DnaSample.class, null, null, null, null).get(0);
	    assertEquals("Wrong derivation type!", DerivationEventType.DNA_EXTRACTION(), dnaSample.getDerivedFrom().getType());

	    assertEquals("Wrong number of originals", 1, dnaSample.getDerivedFrom().getOriginals().size());
        FieldUnit specimenFieldUnit = (FieldUnit) occurrenceService.load(fieldUnit1Uuid);
        Collection<FieldUnit> fieldUnits = occurrenceService.getFieldUnits(dnaSample.getUuid());
        assertEquals(1, fieldUnits.size());
        FieldUnit dnaSampleFieldUnit = fieldUnits.iterator().next();
        assertEquals(specimenFieldUnit, dnaSampleFieldUnit);
        assertEquals("fieldUnit1", dnaSampleFieldUnit.getTitleCache());

	}

	@Test
	public void testAvoidDuplicateMolecularData(){

	}

	/**
     * Tests importing of DNA unit with an ABCD with only few fields filled.
     * Should just check that no NPEs occur when some fields are missing.
     */
    @Test
    @DataSets({
        @DataSet(loadStrategy=CleanSweepInsertLoadStrategy.class, value="../../../BlankDataSet.xml"),
    })
    public void testImportGgbnSparseData(){
        String inputFile = "/eu/etaxonomy/cdm/io/specimen/abcd206/in/db6_sparse_data.xml";
        URL url = this.getClass().getResource(inputFile);
	    assertNotNull("URL for the test file '" + inputFile + "' does not exist", url);

	    Abcd206ImportConfigurator importConfigurator = null;
	    try {
	        importConfigurator = Abcd206ImportConfigurator.NewInstance(url.toURI(), null,false);
	    } catch (URISyntaxException e) {
	        e.printStackTrace();
	        Assert.fail();
	    }
	    assertNotNull("Configurator could not be created", importConfigurator);

	    boolean result = defaultImport.invoke(importConfigurator);
	    assertTrue("Return value for import.invoke should be true", result);
	}

	/**
	 * Tests importing of DNA unit without attaching it to an existing specimen.
	 * Creates a FieldUnit with an attached DnaSample.
	 */
	@Test
    @DataSets({
        @DataSet(loadStrategy=CleanSweepInsertLoadStrategy.class, value="../../../BlankDataSet.xml"),
        @DataSet( value="AbcdGgbnImportTest.testNoAttachDnaSampleToDerivedUnit.xml", loadStrategy=CleanSweepInsertLoadStrategy.class)
    })
	public void testNoAttachDnaSampleToDerivedUnit(){
	    UUID derivedUnit1Uuid = UUID.fromString("eb40cb0f-efb2-4985-819e-a9168f6d61fe");
	    UUID fieldUnit1Uuid = UUID.fromString("b5f58da5-4442-4001-9d13-33f41518b72a");

//        DerivedUnit derivedUnit = DerivedUnit.NewInstance(SpecimenOrObservationType.Fossil);
//        derivedUnit.setAccessionNumber("B 10 0066577");
//        derivedUnit.setTitleCache("testUnit1", true);
//
//        derivedUnit.setUuid(derivedUnit1Uuid );
//
//        occurrenceService.save(derivedUnit);
//
//        commitAndStartNewTransaction(null);
//
//        setComplete();
//        endTransaction();
//
//
//        try {
//            writeDbUnitDataSetFile(new String[] {
//                    "SpecimenOrObservationBase",
//            }, "testAttachDnaSampleToDerivedUnit");
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }


	    String inputFile = "/eu/etaxonomy/cdm/io/specimen/abcd206/in/db6_without_association.xml";
	    URL url = this.getClass().getResource(inputFile);
	    assertNotNull("URL for the test file '" + inputFile + "' does not exist", url);

	    Abcd206ImportConfigurator importConfigurator = null;
	    try {
	        importConfigurator = Abcd206ImportConfigurator.NewInstance(url.toURI(), null,false);
	    } catch (URISyntaxException e) {
	        e.printStackTrace();
	        Assert.fail();
	    }
	    assertNotNull("Configurator could not be created", importConfigurator);

	    assertEquals("Number of derived units is incorrect", 1, occurrenceService.count(DerivedUnit.class));
	    boolean result = defaultImport.invoke(importConfigurator);
	    assertTrue("Return value for import.invoke should be true", result);
	    assertEquals("Number of derived units is incorrect", 2, occurrenceService.count(DerivedUnit.class));
	    List<DerivedUnit> derivedUnits = occurrenceService.list(DerivedUnit.class, null, null, null, null);
	    assertEquals("Number of derived units is incorrect", 2, derivedUnits.size());
	    assertEquals("Number of field units is incorrect", 2, occurrenceService.count(FieldUnit.class));
	    assertEquals("Number of dna samples is incorrect", 1, occurrenceService.count(DnaSample.class));

	    DerivedUnit derivedUnit = (DerivedUnit) occurrenceService.load(derivedUnit1Uuid);
	    assertTrue(derivedUnits.contains(derivedUnit));

	    DnaSample dnaSample = occurrenceService.list(DnaSample.class, null, null, null, null).get(0);
	    assertEquals("Wrong derivation type!", DerivationEventType.DNA_EXTRACTION(), dnaSample.getDerivedFrom().getType());

	    assertEquals("Wrong number of originals", 1, dnaSample.getDerivedFrom().getOriginals().size());
	    FieldUnit specimenFieldUnit = (FieldUnit) occurrenceService.load(fieldUnit1Uuid);
	    SpecimenOrObservationBase dnaSampleFieldUnit = dnaSample.getDerivedFrom().getOriginals().iterator().next();
	    assertTrue(!specimenFieldUnit.equals(dnaSampleFieldUnit));

	}

    @Override
    public void createTestDataSet() throws FileNotFoundException {
        UUID derivedUnit1Uuid = UUID.fromString("eb40cb0f-efb2-4985-819e-a9168f6d61fe");

        DerivedUnit derivedUnit = DerivedUnit.NewInstance(SpecimenOrObservationType.Fossil);
        derivedUnit.setAccessionNumber("B 10 0066577");
        derivedUnit.setTitleCache("testUnit1", true);

        derivedUnit.setUuid(derivedUnit1Uuid );

        occurrenceService.save(derivedUnit);

        commitAndStartNewTransaction(null);

        setComplete();
        endTransaction();


        try {
            writeDbUnitDataSetFile(new String[] {
                    "SpecimenOrObservationBase",
            }, "testAttachDnaSampleToDerivedUnit");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

}
