package eu.etaxonomy.cdm.test.suite;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.database.CdmDataSourceTest;
import eu.etaxonomy.cdm.database.DatabaseEnumTest;
import eu.etaxonomy.cdm.persistence.dao.common.CdmEntityDaoBaseTest;
import eu.etaxonomy.cdm.persistence.dao.common.DaoBaseTest;
import eu.etaxonomy.cdm.persistence.dao.common.DefinedTermDaoImplTest;
import eu.etaxonomy.cdm.persistence.dao.common.IdentifiableDaoBaseTest;
import eu.etaxonomy.cdm.persistence.dao.taxon.TaxonDaoHibernateImplTest;


@RunWith(Suite.class)
@Suite.SuiteClasses( 
		{ 	
			//database
			CdmDataSourceTest.class,
			DatabaseEnumTest.class,			
			//dao.common
			CdmEntityDaoBaseTest.class,
			DaoBaseTest.class,
			DefinedTermDaoImplTest.class,
			IdentifiableDaoBaseTest.class,
			//dao.Taxon
			TaxonDaoHibernateImplTest.class
		}
	)
public class CdmTestSuite {
	static Logger logger = Logger.getLogger(CdmTestSuite.class);

	// the class remains completely empty, 
	// being used only as a holder for the above annotations

	//console test  //TODO test
	public static void consoleRun() {
		org.junit.runner.JUnitCore.runClasses(
				//database
				CdmDataSourceTest.class,
				DatabaseEnumTest.class,			
				//dao.common
				CdmEntityDaoBaseTest.class,
				DaoBaseTest.class,
				DefinedTermDaoImplTest.class,
				IdentifiableDaoBaseTest.class,
				//dao.Taxon
				TaxonDaoHibernateImplTest.class
					);
	}
}