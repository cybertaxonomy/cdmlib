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

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.spring.annotation.SpringApplicationContext;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.model.description.IdentificationKey;
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
	public void testCountIdentificationKeys() {
		int numberOfIdentificationKeys = mediaDao.countIdentificationKeys(null,null);
		
		assertEquals("countIdentificationKeys should return 3",3,numberOfIdentificationKeys);
	}
	
	@Test
	public void testGetIdentificationKeys() {
		List<IdentificationKey> keys = mediaDao.getIdentificationKeys(null, null, null, null);
		
		assertNotNull("getIdentificationKeys should return a List",keys);
		assertFalse("The list should not be empty",keys.isEmpty());
		assertEquals("The list should contain 3 IdentificationKey instances",3, keys.size());
	}
	
	@Test
	public void testCountIdentificationKeysWithScope() {
		NamedArea europe = (NamedArea)definedTermDao.findByUuid(europeUuid);
		NamedArea africa = (NamedArea)definedTermDao.findByUuid(africaUuid);
		Taxon sphingidae = (Taxon)taxonDao.findByUuid(sphingidaeUuid);
		assert europe != null : "NamedArea must exist";
		assert africa != null : "NamedArea must exist";
		assert sphingidae != null : "Taxon must exist";
		
		geoScopes.add(europe);
		geoScopes.add(africa);
		taxonomicScope.add(sphingidae);
		
		int numberOfIdentificationKeys = mediaDao.countIdentificationKeys(taxonomicScope,geoScopes);
		
		assertEquals("countIdentificationKeys should return 1",1,numberOfIdentificationKeys);
	}
	
	@Test
	public void testGetIdentificationKeysWithScope() {
		NamedArea europe = (NamedArea)definedTermDao.findByUuid(europeUuid);
		NamedArea africa = (NamedArea)definedTermDao.findByUuid(africaUuid);
		Taxon sphingidae = (Taxon)taxonDao.findByUuid(sphingidaeUuid);
		assert europe != null : "NamedArea must exist";
		assert africa != null : "NamedArea must exist";
		assert sphingidae != null : "Taxon must exist";
		
		geoScopes.add(europe);
		geoScopes.add(africa);
		taxonomicScope.add(sphingidae);
		
		List<IdentificationKey> keys = mediaDao.getIdentificationKeys(taxonomicScope,geoScopes, null, null);
		
		assertNotNull("getIdentificationKeys should return a List",keys);
		assertFalse("The list should not be empty",keys.isEmpty());
		assertEquals("The list should contain 1 IdentificationKey instance",1, keys.size());
	}
	
}
