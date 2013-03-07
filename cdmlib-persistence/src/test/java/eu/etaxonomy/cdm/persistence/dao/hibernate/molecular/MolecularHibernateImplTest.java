package eu.etaxonomy.cdm.persistence.dao.hibernate.molecular;

import java.util.Set;
import java.util.UUID;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.model.molecular.DnaSample;
import eu.etaxonomy.cdm.model.molecular.GenBankAccession;
import eu.etaxonomy.cdm.model.molecular.Locus;
import eu.etaxonomy.cdm.model.molecular.Sequence;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;
import eu.etaxonomy.cdm.persistence.dao.occurrence.IOccurrenceDao;
import eu.etaxonomy.cdm.test.integration.CdmTransactionalIntegrationTest;

public class MolecularHibernateImplTest  extends CdmTransactionalIntegrationTest {

    final static UUID uuidSample1 = UUID.fromString("4b451275-655f-40d6-8d4b-0203574bef15");
	
	@SpringBeanByType
    private IOccurrenceDao occurrenceDao;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

//**************** TESTS ************************************************	
	
	//Test if DnaSample can be loaded and if Sequence, Locus and GenBankAccession data can 
	//be lazy loaded from database
	//#3340
	@Test
	public void testLazyLoadSequenceLocusGenbankaccession() {
		createTestData();
		DnaSample sample1 = (DnaSample)occurrenceDao.findByUuid(uuidSample1);
		Set<Sequence> sequences = sample1.getSequences();
		
		Sequence sequence = sequences.iterator().next();
		Locus locus = sequence.getLocus();
		Assert.assertEquals("Locus", locus.getName());
		Set<GenBankAccession> accessions = sequence.getGenBankAccession();
		GenBankAccession accession = accessions.iterator().next();
		Assert.assertEquals("123", accession.getAccessionNumber());
		commit();
	}

	private void createTestData(){
		DnaSample sample = DnaSample.NewInstance();
		Sequence sequence = Sequence.NewInstance("Meine Sequence");
		sample.addSequences(sequence);
		sample.setUuid(uuidSample1);
		
		Locus locus = Locus.NewInstance("Locus", null);
		sequence.setLocus(locus);
		
		GenBankAccession accession = GenBankAccession.NewInstance("123");
		sequence.addGenBankAccession(accession);
		
		occurrenceDao.save(sample);
		commitAndStartNewTransaction(new String[]{"DnaSample", "SpecimenOrObservationBase", "Locus"});
	}
	

}
