/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.hibernate;

import java.io.FileNotFoundException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.dbunit.annotation.DataSets;
import org.unitils.spring.annotation.SpringBeanByType;

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
import eu.etaxonomy.cdm.test.integration.CdmTransactionalIntegrationTest;
import eu.etaxonomy.cdm.test.unitils.CleanSweepInsertLoadStrategy;

/**
 * @author a.mueller
 */
public class SaveOrUpdateEntityListenerTest extends CdmTransactionalIntegrationTest {

    @SuppressWarnings("unused")
    private static final Logger logger = LogManager.getLogger();

	@SpringBeanByType
	private OccurrenceDaoHibernateImpl occurrenceDao;

	@SpringBeanByType
    private TaxonDaoHibernateImpl taxonDao;

    @SpringBeanByType
    private TaxonNameDaoHibernateImpl nameDao;

	@Before
	public void setUp() throws Exception {
	}

	/**
	 * Test if save or update event correctly handled
	 */
	@Test
    @DataSets({
        @DataSet(loadStrategy=CleanSweepInsertLoadStrategy.class, value="/eu/etaxonomy/cdm/database/ClearDB_with_Terms_DataSet.xml"),
        @DataSet(value="/eu/etaxonomy/cdm/database/TermsDataSet-with_auditing_info.xml"),

    })
	public void testOnSaveOrUpdateDeterminationTaxonName() {

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
				Assert.assertNotNull("Taxon should not be null", persistedDetEvent.getTaxon());
				Assert.assertNull("TaxonName should be null, because taxon has no name", persistedDetEvent.getTaxonName());
			}else if (persistedDetEvent.getUuid().equals(detWithTaxonOnlyAndNameOnTaxon.getUuid())){
				Assert.assertNotNull("Taxon should not be null", persistedDetEvent.getTaxon());
				Assert.assertNotNull("TaxonName should not be null, injected by listener from taxon", persistedDetEvent.getTaxonName());
			}else if (persistedDetEvent.getUuid().equals(detWithNameOnly.getUuid())){
				Assert.assertNull("Taxon should be null as only name was attached", persistedDetEvent.getTaxon());
				Assert.assertNotNull("TaxonName should not be null as set manually", persistedDetEvent.getTaxonName());
			}else{
				Assert.fail("All cases should be handled");
			}
		}
	}

	@Override
	public void createTestDataSet() throws FileNotFoundException {}
}