/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.test.suite;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.database.CdmDataSourceTest;
import eu.etaxonomy.cdm.database.DatabaseEnumTest;
import eu.etaxonomy.cdm.persistence.dao.hibernate.agent.AgentDaoImplTest;
import eu.etaxonomy.cdm.persistence.dao.hibernate.common.CdmEntityDaoBaseTest;
import eu.etaxonomy.cdm.persistence.dao.hibernate.common.DaoBaseTest;
import eu.etaxonomy.cdm.persistence.dao.hibernate.common.DefinedTermDaoImplTest;
import eu.etaxonomy.cdm.persistence.dao.hibernate.common.IdentifiableDaoBaseTest;
import eu.etaxonomy.cdm.persistence.dao.hibernate.name.TypeDesignationDaoHibernateImplTest;
import eu.etaxonomy.cdm.persistence.dao.hibernate.taxon.TaxonDaoHibernateImplTest;


@RunWith(Suite.class)
@Suite.SuiteClasses(
		{
			AgentDaoImplTest.class,
			TypeDesignationDaoHibernateImplTest.class,
			/* TODO fails in TypeDesignationDaoHibernateImplTest.testSaveTypeDesignations
			 * with:
			 * org.hibernate.HibernateException: No Hibernate Session bound to thread, and configuration does not allow creation of non-transactional one here
			 * at org.springframework.orm.hibernate3.SpringSessionContext.currentSession(SpringSessionContext.java:63)
			 */
		}
	)
public class TestsShouldNotFailInSuite_2 {

	private static final  Logger logger = Logger.getLogger(TestsShouldNotFailInSuite_2.class);

	// the class remains completely empty,
	// being used only as a holder for the above annotations

}