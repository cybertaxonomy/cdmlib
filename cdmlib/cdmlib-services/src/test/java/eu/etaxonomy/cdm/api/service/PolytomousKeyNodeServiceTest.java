package eu.etaxonomy.cdm.api.service;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.model.description.PolytomousKey;
import eu.etaxonomy.cdm.model.description.PolytomousKeyNode;
import eu.etaxonomy.cdm.test.integration.CdmIntegrationTest;

public class PolytomousKeyNodeServiceTest extends CdmIntegrationTest{

	@SpringBeanByType
	IPolytomousKeyNodeService service;
	
	@SpringBeanByType
	IPolytomousKeyService keyService;
	
	/****************** TESTS *****************************/

	/**
	 * Test method for {@link eu.etaxonomy.cdm.api.service.TaxonServiceImpl#setDao(eu.etaxonomy.cdm.persistence.dao.taxon.ITaxonDao)}.
	 */
	@Test
	public final void testSetDao() {
		Assert.assertNotNull(service);
		Assert.assertNotNull(keyService);
	}
	
	@Test
	//@DataSet(value="CommonServiceImplTest.xml")
	public final void testDelete(){
		
		PolytomousKey key = PolytomousKey.NewTitledInstance("TestPolytomousKey");
		UUID uuidKey =	keyService.save(key);
		PolytomousKeyNode node = PolytomousKeyNode.NewInstance("Test statement");
		key.setRoot(node);
		key.setStartNumber(0);
		
		PolytomousKeyNode child = PolytomousKeyNode.NewInstance("Test statement Nr 2");
		//child.setKey(key);
		
		node.addChild(child,0);
		UUID uuidNode = service.save(node);
		
		node = service.load(uuidNode);	
		UUID uuidChild = node.getChildAt(0).getUuid();
		assertNotNull(node);
		service.delete(node, true);
		node = service.load(uuidNode);
		assertNull(node);
		node = service.load(UUID.fromString("f0dd12ed-ea77-419a-bce6-4282d0067c91"));
		assertNull(node);
		
	}
	
}
