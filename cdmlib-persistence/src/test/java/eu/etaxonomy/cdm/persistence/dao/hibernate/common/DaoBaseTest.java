/**
 * 
 */
package eu.etaxonomy.cdm.persistence.dao.hibernate.common;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.persistence.dao.hibernate.taxon.TaxonDaoHibernateImpl;
import eu.etaxonomy.cdm.test.integration.CdmIntegrationTest;

/**
 * @author a.mueller
 *
 */
public class DaoBaseTest  extends CdmIntegrationTest {
	
	
	@SpringBeanByType
	private  TaxonDaoHibernateImpl daoBaseTester;
	
/************ TESTS ********************************/

	/**
	 * Test method for {@link eu.etaxonomy.cdm.persistence.dao.hibernate.common.DaoBase#getSession()}.
	 */
	@Test
	public void testGetSession() {
		assertNotNull(daoBaseTester.getSession());
	}

}
