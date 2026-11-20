/**
* Copyright (C) 2013 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.dao.hibernate.molecular;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import eu.etaxonomy.cdm.model.molecular.DnaSample;
import eu.etaxonomy.cdm.model.molecular.PhylogeneticTree;
import eu.etaxonomy.cdm.model.molecular.Sequence;
import eu.etaxonomy.cdm.model.term.DefinedTerm;
import eu.etaxonomy.cdm.persistence.dao.media.IMediaDao;
import eu.etaxonomy.cdm.persistence.dao.occurrence.IOccurrenceDao;
import eu.etaxonomy.cdm.test.integration.spring.CdmDataSet;
import eu.etaxonomy.cdm.test.integration.spring.CdmTransactionalSpringIntegrationTest;

/**
 * @author muellera
 * @since 07.03.2013
 */
public class MolecularHibernateImplTest  extends CdmTransactionalSpringIntegrationTest {

	private static final String MARKER_LABEL = "ITS1";

	@Autowired
    private IOccurrenceDao occurrenceDao;

	@Autowired
    private IMediaDao mediaDao;

	private UUID phyloTreeUuid;
	private UUID sequenceUuid;
	private UUID uuidSample1;// = UUID.fromString("4b451275-655f-40d6-8d4b-0203574bef15");

	@BeforeAll
	public static void setUpBeforeClass() throws Exception {
	}

	@BeforeEach
	public void setUp() throws Exception {
	}

//**************** TESTS ************************************************

	//Test if DnaSample can be loaded and if Sequence and Marker data can
	//be lazy loaded from database
	//#3340
	@Test
	@CdmDataSet
	public void testLazyLoadSequenceMarker() {
		createTestDataSet();
		DnaSample sample1 = (DnaSample)occurrenceDao.findByUuid(uuidSample1);
		Set<Sequence> sequences = sample1.getSequences();

		Sequence sequence = sequences.iterator().next();
		DefinedTerm marker = sequence.getDnaMarker();
		assertNotNull(marker, "Marker should not be null");
		assertEquals(MARKER_LABEL, marker.getLabel(), "Markers label should be 'Marker'");

		commit();
	}

	@Test
	@CdmDataSet
	public void testLoadUsedSequences() {
		createTestDataSet();
		PhylogeneticTree phyloTree = (PhylogeneticTree)mediaDao.findByUuid(phyloTreeUuid);
		assertNotNull(phyloTree, "Phylogenetic Tree should be found");
		Set<Sequence> sequences = phyloTree.getUsedSequences();

		assertEquals(1, sequences.size());
		Sequence sequence = sequences.iterator().next();
		assertEquals(sequenceUuid, sequence.getUuid());

		commit();
	}

	@Override
    public void createTestDataSet(){
		DnaSample sample = DnaSample.NewInstance();
		Sequence sequence = Sequence.NewInstance("Meine Sequence");
		sequenceUuid = sequence.getUuid();
		sample.addSequence(sequence);
		uuidSample1 = sample.getUuid();

		DefinedTerm marker = DefinedTerm.ITS1_MARKER();
		assertNotNull(marker, "ITS1 marker must not be null");
		sequence.setDnaMarker(marker);

		occurrenceDao.save(sample);

		PhylogeneticTree phyloTree = PhylogeneticTree.NewInstance();
		phyloTree.addUsedSequences(sequence);
		mediaDao.save(phyloTree);
		phyloTreeUuid = phyloTree.getUuid();

		commitAndStartNewTransaction(new String[]{"SpecimenOrObservationBase"});
	}


}
