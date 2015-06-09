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
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.spring.annotation.SpringBeanByName;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.api.service.IOccurrenceService;
import eu.etaxonomy.cdm.io.common.CdmApplicationAwareDefaultImport;
import eu.etaxonomy.cdm.model.common.DefinedTerm;
import eu.etaxonomy.cdm.model.media.MediaUtils;
import eu.etaxonomy.cdm.model.molecular.Amplification;
import eu.etaxonomy.cdm.model.molecular.AmplificationResult;
import eu.etaxonomy.cdm.model.molecular.DnaSample;
import eu.etaxonomy.cdm.model.molecular.Primer;
import eu.etaxonomy.cdm.model.molecular.Sequence;
import eu.etaxonomy.cdm.model.molecular.SequenceDirection;
import eu.etaxonomy.cdm.model.molecular.SequenceString;
import eu.etaxonomy.cdm.model.molecular.SingleRead;
import eu.etaxonomy.cdm.model.occurrence.DerivationEventType;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnit;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationType;
import eu.etaxonomy.cdm.model.reference.Reference;
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

	@Test
    @DataSet( value="../../../BlankDataSet.xml", loadStrategy=CleanSweepInsertLoadStrategy.class)
    public void testImportGgbn() {
        String inputFile = "/eu/etaxonomy/cdm/io/specimen/abcd206/in/db6.xml";
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
        DnaSample dnaSample = occurrenceService.list(DnaSample.class, null, null, null, null).get(0);
        assertEquals("Wrong derivation type!", DerivationEventType.DNA_EXTRACTION(), dnaSample.getDerivedFrom().getType());
        assertEquals("Wrong number of originals", 1, dnaSample.getDerivedFrom().getOriginals().size());

        //dna quality
//        DnaQuality dnaQuality = dnaSample.getDnaQuality();
//        assertNotNull("Dna quality is null", dnaQuality!=null);
//        assertEquals(new Double("0,77"),dnaQuality.getRatioOfAbsorbance260_230());
//        assertEquals(new Double("1,38"),dnaQuality.getRatioOfAbsorbance260_280());

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
        Reference reference = sequence.getCitations().iterator().next();
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
	}

	@Test
	@DataSet( value="AbcdGgbnImportTest.testAttachDnaSampleToDerivedUnit.xml", loadStrategy=CleanSweepInsertLoadStrategy.class)
	public void testAttachDnaSampleToDerivedUnit(){
	    UUID derivedUnit1Uuid = UUID.fromString("eb40cb0f-efb2-4985-819e-a9168f6d61fe");

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


	    String inputFile = "/eu/etaxonomy/cdm/io/specimen/abcd206/in/db6.xml";
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

	    DerivedUnit derivedUnit = (DerivedUnit) occurrenceService.load(derivedUnit1Uuid);
	    assertTrue(derivedUnits.contains(derivedUnit));

	    assertEquals("Number of dna samples is incorrect", 1, occurrenceService.count(DnaSample.class));
	    DnaSample dnaSample = occurrenceService.list(DnaSample.class, null, null, null, null).get(0);
	    assertEquals("Wrong derivation type!", DerivationEventType.DNA_EXTRACTION(), dnaSample.getDerivedFrom().getType());

	    assertEquals("Wrong number of originals", 1, dnaSample.getDerivedFrom().getOriginals().size());

	}

	@Test
	@DataSet( value="AbcdGgbnImportTest.testNoAttachDnaSampleToDerivedUnit.xml", loadStrategy=CleanSweepInsertLoadStrategy.class)
	public void testNoAttachDnaSampleToDerivedUnit(){
	    UUID derivedUnit1Uuid = UUID.fromString("eb40cb0f-efb2-4985-819e-a9168f6d61fe");

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


	    String inputFile = "/eu/etaxonomy/cdm/io/specimen/abcd206/in/db6.xml";
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
	    assertEquals("Number of derived units is incorrect", 3, occurrenceService.count(DerivedUnit.class));
	    List<DerivedUnit> derivedUnits = occurrenceService.list(DerivedUnit.class, null, null, null, null);
	    assertEquals("Number of derived units is incorrect", 3, derivedUnits.size());

	    DerivedUnit derivedUnit = (DerivedUnit) occurrenceService.load(derivedUnit1Uuid);
	    assertTrue(derivedUnits.contains(derivedUnit));

	    assertEquals("Number of dna samples is incorrect", 1, occurrenceService.count(DnaSample.class));
	    DnaSample dnaSample = occurrenceService.list(DnaSample.class, null, null, null, null).get(0);
	    assertEquals("Wrong derivation type!", DerivationEventType.DNA_EXTRACTION(), dnaSample.getDerivedFrom().getType());

	    assertEquals("Wrong number of originals", 1, dnaSample.getDerivedFrom().getOriginals().size());

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
