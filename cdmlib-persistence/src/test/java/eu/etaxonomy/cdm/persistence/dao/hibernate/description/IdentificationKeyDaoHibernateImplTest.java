/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.persistence.dao.hibernate.description;

import java.io.FileNotFoundException;
import java.util.List;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.model.description.IIdentificationKey;
import eu.etaxonomy.cdm.model.description.MediaKey;
import eu.etaxonomy.cdm.model.description.PolytomousKey;
import eu.etaxonomy.cdm.persistence.dao.description.IIdentificationKeyDao;
import eu.etaxonomy.cdm.test.integration.CdmIntegrationTest;

@DataSet
public class IdentificationKeyDaoHibernateImplTest extends CdmIntegrationTest {

	@SpringBeanByType
	IIdentificationKeyDao identificationKeyDao;

	UUID taxonUuid = UUID.fromString("54e767ee-894e-4540-a758-f906ecb4e2d9");

	@Before
	public void setUp() {

	}

	@Test
	public void testList() {
		identificationKeyDao.list(null, null, null);
	}

	@Test
	public void testFindByTaxonomicScope() {
		Long count1 = identificationKeyDao.countByTaxonomicScope(taxonUuid, IIdentificationKey.class);
		Assert.assertTrue(count1.equals(2l));
		List<IIdentificationKey> list1 = identificationKeyDao.findByTaxonomicScope(taxonUuid, IIdentificationKey.class, null, null, null);
		Assert.assertEquals(list1.size(), 2);

		Long count2 = identificationKeyDao.countByTaxonomicScope(taxonUuid, MediaKey.class);
		Assert.assertTrue(count2.equals(2l));
		List<MediaKey> list2 = identificationKeyDao.findByTaxonomicScope(taxonUuid, MediaKey.class, null, null, null);
		Assert.assertEquals(list2.size(), 2);

		Long count3 = identificationKeyDao.countByTaxonomicScope(taxonUuid, PolytomousKey.class);
		Assert.assertTrue(count3.equals(0l));
		List<PolytomousKey> list3 = identificationKeyDao.findByTaxonomicScope(taxonUuid, PolytomousKey.class, null, null, null);
		Assert.assertEquals(list3.size(), 0);
	}

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.test.integration.CdmIntegrationTest#createTestData()
     */
    @Override
    public void createTestDataSet() throws FileNotFoundException {
        // TODO Auto-generated method stub

    }



}
