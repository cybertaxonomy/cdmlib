package eu.etaxonomy.cdm.api.service;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.FileNotFoundException;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
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
        UUID uuidKey =  keyService.save(key).getUuid();
        PolytomousKeyNode node = PolytomousKeyNode.NewInstance("Test statement");
        key.setRoot(node);
        key.setStartNumber(0);

        PolytomousKeyNode child = PolytomousKeyNode.NewInstance("Test statement Nr 2");
        //child.setKey(key);

        node.addChild(child,0);
        UUID uuidNode = service.save(node).getUuid();

        PolytomousKeyNode child1 = PolytomousKeyNode.NewInstance("Test statement Nr 3");
        //child.setKey(key);

        child.addChild(child1,0);
        UUID uuidChild = service.save(child).getUuid();

        PolytomousKeyNode child2 = PolytomousKeyNode.NewInstance("Test statement Nr 4");
        //child.setKey(key);

        child1.addChild(child2,0);
        UUID uuidChild1 = service.save(child1).getUuid();


        node = service.load(uuidChild1);
        UUID uuidChild2 = node.getChildAt(0).getUuid();
        assertNotNull(node);
        service.delete(uuidChild1, false);
        node = service.load(uuidChild1);
        assertNull(node);
        node = service.load(uuidChild2);
        assertNotNull(node);

        node = service.load(uuidChild);

        assertNotNull(node);
        service.delete(uuidChild, true);
        node = service.load(uuidChild);
        assertNull(node);
        node = service.load(uuidChild2);
        assertNull(node);

    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.test.integration.CdmIntegrationTest#createTestData()
     */
    @Override
    public void createTestDataSet() throws FileNotFoundException {
        // TODO Auto-generated method stub

    }

}
