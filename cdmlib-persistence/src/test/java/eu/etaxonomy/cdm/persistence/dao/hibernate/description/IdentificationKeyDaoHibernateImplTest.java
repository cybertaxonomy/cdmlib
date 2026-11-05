/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.dao.hibernate.description;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.FileNotFoundException;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import eu.etaxonomy.cdm.model.description.IIdentificationKey;
import eu.etaxonomy.cdm.model.description.MediaKey;
import eu.etaxonomy.cdm.model.description.PolytomousKey;
import eu.etaxonomy.cdm.persistence.dao.description.IIdentificationKeyDao;
import eu.etaxonomy.cdm.test.integration.spring.CdmDataSet;
import eu.etaxonomy.cdm.test.integration.spring.CdmSpringIntegrationTest;

@CdmDataSet
public class IdentificationKeyDaoHibernateImplTest extends CdmSpringIntegrationTest {

	@Autowired
	private IIdentificationKeyDao identificationKeyDao;

	private UUID taxonUuid = UUID.fromString("54e767ee-894e-4540-a758-f906ecb4e2d9");

	@BeforeEach
	public void setUp() {
	}

	@Test
	public void testList() {
		identificationKeyDao.list(null, null, null);
	}

	@Test
	public void testFindByTaxonomicScope() {
		Long count1 = identificationKeyDao.countByTaxonomicScope(taxonUuid, IIdentificationKey.class);
		assertTrue(count1.equals(2l));
		List<IIdentificationKey> list1 = identificationKeyDao.findByTaxonomicScope(taxonUuid, IIdentificationKey.class, null, null, null);
		assertEquals(list1.size(), 2);

		Long count2 = identificationKeyDao.countByTaxonomicScope(taxonUuid, MediaKey.class);
		assertTrue(count2.equals(2l));
		List<MediaKey> list2 = identificationKeyDao.findByTaxonomicScope(taxonUuid, MediaKey.class, null, null, null);
		assertEquals(list2.size(), 2);

		Long count3 = identificationKeyDao.countByTaxonomicScope(taxonUuid, PolytomousKey.class);
		assertTrue(count3.equals(0l));
		List<PolytomousKey> list3 = identificationKeyDao.findByTaxonomicScope(taxonUuid, PolytomousKey.class, null, null, null);
		assertEquals(list3.size(), 0);
	}

    @Override
    public void createTestDataSet() throws FileNotFoundException {}
}
