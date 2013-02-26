package eu.etaxonomy.cdm.model.molecular;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import eu.etaxonomy.cdm.model.common.DefaultTermInitializer;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.LanguageString;
import eu.etaxonomy.cdm.model.media.Media;


public class MolecularTest {
	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(MolecularTest.class);

	private DnaSample dnaSample;
	private Locus locus;
	private PhylogeneticTree phyloTree;
	private Sequence seq;
	
	
	@BeforeClass
	public static void setUpBeforeClass() {
		DefaultTermInitializer vocabularyStore = new DefaultTermInitializer();
		vocabularyStore.initialize();
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		dnaSample = DnaSample.NewInstance();
		seq = new Sequence();
		
		seq.setBarcode(true);
		seq.setSequence("ATTGCCATCG");
		
		GenBankAccession genBankAccession = new GenBankAccession();
		genBankAccession.setAccessionNumber("12393247");
		seq.addGenBankAccession(genBankAccession );
		Media chromatogram = Media.NewInstance();
		chromatogram.putTitle(LanguageString.NewInstance("chromatogram", Language.ENGLISH()));
		seq.addChromatogram(chromatogram);
		
		Sequence otherSeq = new Sequence();
		
		otherSeq.setBarcode(true);
		otherSeq.setSequence("CATCGAGTTGC");
		dnaSample.addSequences(seq);
		dnaSample.addSequences(otherSeq);
		
		locus= Locus.NewInstance("Test", "test locus");
		
		phyloTree =new PhylogeneticTree();
		phyloTree.addUsedSequences(seq);
		phyloTree.addUsedSequences(otherSeq);
	}

/* ************************** TESTS **********************************************************/
	
	@Test
	public void testClone(){
		
		
		PhylogeneticTree phyloTreeClone = (PhylogeneticTree)phyloTree.clone();
		assertTrue(phyloTreeClone.getUsedSequences().size() == 2);
		
		assertNotSame(phyloTreeClone.getUsedSequences().iterator().next(), phyloTree.getUsedSequences().iterator().next());
		
		
		Sequence sequenceClone = (Sequence)seq.clone();
		assertEquals(sequenceClone.getChromatograms().iterator().next().getAllTitles().get(0),seq.getChromatograms().iterator().next().getAllTitles().get(0));
		
		Iterator<Media> mediaIteratorClone = sequenceClone.getChromatograms().iterator();
		Iterator<Media> mediaIterator = sequenceClone.getChromatograms().iterator();
		Media test = (Media)mediaIterator.next();
		LanguageString title = test.getTitle(Language.ENGLISH());
		test = (Media)mediaIteratorClone.next();
		LanguageString titleClone = test.getTitle(Language.ENGLISH());		
		assertEquals(title, titleClone);
		
		
		
		assertTrue (sequenceClone.getGenBankAccession().size() == seq.getGenBankAccession().size());
		
		Iterator<GenBankAccession> genBankAccessionIteratorClone = sequenceClone.getGenBankAccession().iterator();
		Iterator<GenBankAccession> genBankAccessionIterator = seq.getGenBankAccession().iterator();
		GenBankAccession genBankAccession = (GenBankAccession)genBankAccessionIterator.next();
		String numberStr = genBankAccession.getAccessionNumber();
		GenBankAccession genBankAccessionClone = (GenBankAccession)genBankAccessionIteratorClone.next();
		String numberStrClone = genBankAccessionClone.getAccessionNumber();	
		assertEquals(numberStr, numberStrClone);
		assertNotSame(genBankAccession, genBankAccessionClone);
		
		DnaSample dnaSampleClone = (DnaSample)dnaSample.clone();
		Sequence[] seqArray = new Sequence[dnaSample.getSequences().size()];
		seqArray = dnaSample.getSequences().toArray(seqArray);
		Sequence[] seqArrayClone = dnaSampleClone.getSequences().toArray(seqArray);
		boolean testBool = true;
		for (Sequence seqTest: seqArray){
			if (testBool == false) break;
			testBool = false;
			for (Sequence seq: seqArrayClone){
				if (seq.equals(seqTest)) {
					testBool = true;
					break;
				} else testBool = false;
				
			}
		}
		assertTrue(testBool);
		
		
		
		
	}
	
}
