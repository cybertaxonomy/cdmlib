package eu.etaxonomy.cdm.persistence.dao.hibernate.description;

import org.junit.Test;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.persistence.dao.description.IDescriptionDao;
import eu.etaxonomy.cdm.test.integration.CdmIntegrationTest;

public class DescriptionDaoHibernateImplTest extends CdmIntegrationTest {
	
	@SpringBeanByType
	IDescriptionDao descriptionDao;
	
	@Test
	public void test() {
		// Will finish once I've altered the DescriptionBase -> DescriptionElementBase mapping
	}
}
