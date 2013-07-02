package eu.etaxonomy.cdm.persistence.dao.hibernate.molecular;

import java.util.Set;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.model.molecular.DnaSample;
import eu.etaxonomy.cdm.model.molecular.Locus;
import eu.etaxonomy.cdm.model.molecular.PhylogeneticTree;
import eu.etaxonomy.cdm.model.molecular.Sequence;
import eu.etaxonomy.cdm.persistence.dao.media.IMediaDao;
import eu.etaxonomy.cdm.persistence.dao.occurrence.IOccurrenceDao;
import eu.etaxonomy.cdm.test.integration.CdmTransactionalIntegrationTest;

public class MolecularHibernateImplTest  extends CdmTransactionalIntegrationTest {

   
    
	@SpringBeanByType
    private IOccurrenceDao occurrenceDao;
	
	@SpringBeanByType
    private IMediaDao mediaDao;
	
	private UUID phyloTreeUuid;
	private UUID sequenceUuid;
	private UUID uuidSample1;// = UUID.fromString("4b451275-655f-40d6-8d4b-0203574bef15");
		
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

//**************** TESTS ************************************************	
	
	//Test if DnaSample can be loaded and if Sequence and Locus data can 
	//be lazy loaded from database
	//#3340
	@Test
	public void testLazyLoadSequenceLocus() {
		createTestData();
		DnaSample sample1 = (DnaSample)occurrenceDao.findByUuid(uuidSample1);
		Set<Sequence> sequences = sample1.getSequences();
		
		Sequence sequence = sequences.iterator().next();
		Locus locus = sequence.getLocus();
		Assert.assertEquals("Locus", locus.getName());
		
		commit();
	}
	
	@Test
	public void testLoadUsedSequences() {
		createTestData();
		PhylogeneticTree phyloTree = (PhylogeneticTree)mediaDao.findByUuid(phyloTreeUuid);
		Assert.assertNotNull("Phylogenetic Tree should be found", phyloTree);
		Set<Sequence> sequences = phyloTree.getUsedSequences();
		
		Assert.assertEquals(1, sequences.size());
		Sequence sequence = sequences.iterator().next();
		Assert.assertEquals(sequenceUuid, sequence.getUuid());
		
		commit();
	}

	private void createTestData(){
		DnaSample sample = DnaSample.NewInstance();
		Sequence sequence = Sequence.NewInstance("Meine Sequence");
		sequenceUuid = sequence.getUuid();
		sample.addSequence(sequence);
		uuidSample1 = sample.getUuid();
		
		Locus locus = Locus.NewInstance("Locus", null);
		sequence.setLocus(locus);
		
		occurrenceDao.save(sample);
		
		PhylogeneticTree phyloTree = PhylogeneticTree.NewInstance();
		phyloTree.addUsedSequences(sequence);
		mediaDao.save(phyloTree);
		phyloTreeUuid = phyloTree.getUuid();
		
		commitAndStartNewTransaction(new String[]{"DnaSample", "SpecimenOrObservationBase", "Locus"});
	}
	

}
