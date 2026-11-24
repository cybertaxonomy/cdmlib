/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.hibernate;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.FileNotFoundException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.name.TaxonNameFactory;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnit;
import eu.etaxonomy.cdm.model.occurrence.DeterminationEvent;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationType;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.persistence.dao.hibernate.name.TaxonNameDaoHibernateImpl;
import eu.etaxonomy.cdm.persistence.dao.hibernate.occurrence.OccurrenceDaoHibernateImpl;
import eu.etaxonomy.cdm.persistence.dao.hibernate.taxon.TaxonDaoHibernateImpl;
import eu.etaxonomy.cdm.test.integration.spring.CdmDataSet;
import eu.etaxonomy.cdm.test.integration.spring.CdmTransactionalSpringIntegrationTest;

/**
 * @author a.mueller
 */
public class SaveEntityListenerTest extends CdmTransactionalSpringIntegrationTest {

    @SuppressWarnings("unused")
    private static final Logger logger = LogManager.getLogger();

	@Autowired
	private OccurrenceDaoHibernateImpl occurrenceDao;

	@Autowired
    private TaxonDaoHibernateImpl taxonDao;

    @Autowired
    private TaxonNameDaoHibernateImpl nameDao;

	@BeforeEach
	public void setUp() throws Exception {
	}

	/**
	 * Test if save or update event correctly handled
	 */
	@Test
    @CdmDataSet(cleanSweep = true, value = {
            "/eu/etaxonomy/cdm/database/ClearDB_with_Terms_DataSet.xml",
            "/eu/etaxonomy/cdm/database/TermsDataSet-with_auditing_info.xml"
    })
	public void testOnSaveDeterminationTaxonName() {

		//create test data
	    DerivedUnit unit = DerivedUnit.NewInstance(SpecimenOrObservationType.DerivedUnit);
	    Taxon taxon = Taxon.NewInstance(null, null);
        TaxonName name = TaxonNameFactory.NewBotanicalInstance(Rank.SPECIES());
        Taxon taxon2 = Taxon.NewInstance(name, null);

        occurrenceDao.save(unit);
        taxonDao.save(taxon);
        nameDao.save(name);
        taxonDao.save(taxon2);

        DeterminationEvent detWithTaxonOnlyAndNoName = DeterminationEvent.NewInstance(taxon, unit);
		DeterminationEvent detWithTaxonOnlyAndNameOnTaxon = DeterminationEvent.NewInstance(taxon2, unit);
		DeterminationEvent detWithNameOnly = DeterminationEvent.NewInstance(name, unit);

		commitAndStartNewTransaction(null);

		//test
		unit = (DerivedUnit)occurrenceDao.findByUuid(unit.getUuid());
		for (DeterminationEvent persistedDetEvent : unit.getDeterminations()){
			if (persistedDetEvent.getUuid().equals(detWithTaxonOnlyAndNoName.getUuid())){
				assertNotNull(persistedDetEvent.getTaxon(), "Taxon should not be null");
				assertNull(persistedDetEvent.getTaxonName(), "TaxonName should be null, because taxon has no name");
			}else if (persistedDetEvent.getUuid().equals(detWithTaxonOnlyAndNameOnTaxon.getUuid())){
				assertNotNull(persistedDetEvent.getTaxon(), "Taxon should not be null");
				assertNotNull(persistedDetEvent.getTaxonName(), "TaxonName should not be null, injected by listener from taxon");
			}else if (persistedDetEvent.getUuid().equals(detWithNameOnly.getUuid())){
				assertNull(persistedDetEvent.getTaxon(), "Taxon should be null as only name was attached");
				assertNotNull(persistedDetEvent.getTaxonName(), "TaxonName should not be null as set manually");
			}else{
				fail("All cases should be handled");
			}
		}
	}

	@Override
	public void createTestDataSet() throws FileNotFoundException {}
}