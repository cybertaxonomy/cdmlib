/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.persistence.dao.hibernate.media;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.hibernate.Hibernate;
import org.junit.Before;
import org.junit.Test;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.model.description.MediaKey;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.persistence.dao.common.IDefinedTermDao;
import eu.etaxonomy.cdm.persistence.dao.media.IMediaDao;
import eu.etaxonomy.cdm.persistence.dao.taxon.ITaxonDao;
import eu.etaxonomy.cdm.test.integration.CdmIntegrationTest;

@DataSet
public class MediaDaoImplTest extends CdmIntegrationTest {

	@SpringBeanByType
	IMediaDao mediaDao;

	@SpringBeanByType
	IDefinedTermDao definedTermDao;

	@SpringBeanByType
	ITaxonDao taxonDao;

	UUID europeUuid;
	UUID africaUuid;
	UUID sphingidaeUuid;

	Set<Taxon> taxonomicScope;
	Set<NamedArea> geoScopes;

	@Before
	public void setUp() {
		europeUuid = UUID.fromString("e860871c-3a14-4ef2-9367-bbd92586c95b");
		africaUuid = UUID.fromString("9444016a-b334-4772-8795-ed4019552087");
		sphingidaeUuid = UUID.fromString("54e767ee-894e-4540-a758-f906ecb4e2d9");

		taxonomicScope = new HashSet<Taxon>();
		geoScopes = new HashSet<NamedArea>();
	}

	@Test
	public void testCountMediaKeys() {
		long numberOfMediaKeys = mediaDao.countMediaKeys(null,null);

		assertEquals("countMediaKeys should return 3",3,numberOfMediaKeys);
	}

	@Test
	public void testGetMediaKeys() {
		List<String> propertyPaths = new ArrayList<String>();
		propertyPaths.add("title");
		List<MediaKey> keys = mediaDao.getMediaKeys(null, null, null, null,propertyPaths);

		assertNotNull("getMediaKeys should return a List",keys);
		assertFalse("The list should not be empty",keys.isEmpty());
		assertEquals("The list should contain 3 MediaKey instances",3, keys.size());
		assertTrue("Media.title should have been initialized",Hibernate.isInitialized(keys.get(0).getTitle()));
	}

	@Test
	public void testCountMediaKeysWithScope() {
		NamedArea europe = (NamedArea)definedTermDao.findByUuid(europeUuid);
		NamedArea africa = (NamedArea)definedTermDao.findByUuid(africaUuid);
		Taxon sphingidae = (Taxon)taxonDao.findByUuid(sphingidaeUuid);
		assert europe != null : "NamedArea must exist";
		assert africa != null : "NamedArea must exist";
		assert sphingidae != null : "Taxon must exist";

		geoScopes.add(europe);
		geoScopes.add(africa);
		taxonomicScope.add(sphingidae);

		long numberOfMediaKeys = mediaDao.countMediaKeys(taxonomicScope,geoScopes);

		assertEquals("countMediaKeys should return 1",1,numberOfMediaKeys);
	}

	@Test
	public void testGetMediaKeysWithScope() {
		List<String> propertyPaths = new ArrayList<String>();
		propertyPaths.add("title");
		NamedArea europe = (NamedArea)definedTermDao.findByUuid(europeUuid);
		NamedArea africa = (NamedArea)definedTermDao.findByUuid(africaUuid);
		Taxon sphingidae = (Taxon)taxonDao.findByUuid(sphingidaeUuid);
		assert europe != null : "NamedArea must exist";
		assert africa != null : "NamedArea must exist";
		assert sphingidae != null : "Taxon must exist";

		geoScopes.add(europe);
		geoScopes.add(africa);
		taxonomicScope.add(sphingidae);

		List<MediaKey> keys = mediaDao.getMediaKeys(taxonomicScope,geoScopes, null, null,propertyPaths);

		assertNotNull("getMediaKeys should return a List",keys);
		assertFalse("The list should not be empty",keys.isEmpty());
		assertEquals("The list should contain 1 MediaKey instance",1, keys.size());
		assertTrue("Media.title should have been initialized",Hibernate.isInitialized(keys.get(0).getTitle()));
	}

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.test.integration.CdmIntegrationTest#createTestData()
     */
    @Override
    public void createTestDataSet() throws FileNotFoundException {
        // TODO Auto-generated method stub

    }
}
