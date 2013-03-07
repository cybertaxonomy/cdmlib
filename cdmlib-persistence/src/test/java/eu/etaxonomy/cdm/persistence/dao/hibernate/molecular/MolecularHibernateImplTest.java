package eu.etaxonomy.cdm.persistence.dao.hibernate.molecular;

import java.util.Set;
import java.util.UUID;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.model.molecular.DnaSample;
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
	
	//Test if DnaSample, Sequence and Locus data can be retrieved from database
	//Locus currently does not allow private constructor (javassist can't create class).
	//Still need to find out why.
	//#3340
	@Test
	public void testRetrieveLocus() {
		createTestData();
		DnaSample sample1 = (DnaSample)occurrenceDao.findByUuid(uuidSample1);
		Set<Sequence> sequences = sample1.getSequences();
		
		Sequence sequence = sequences.iterator().next();
		Locus locus = sequence.getLocus();
		Assert.assertEquals("Locus", locus.getName());
		commit();
	}

	private void createTestData(){
		DnaSample sample = DnaSample.NewInstance();
		Sequence sequence = Sequence.NewInstance("Meine Sequence");
		sample.addSequences(sequence);
		sample.setUuid(uuidSample1);
		
		Locus locus = Locus.NewInstance("Locus", null);
		sequence.setLocus(locus);
		
		occurrenceDao.save(sample);
		commitAndStartNewTransaction(new String[]{"DnaSample", "SpecimenOrObservationBase", "Locus"});
	}
	

}
