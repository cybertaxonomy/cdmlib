package eu.etaxonomy.cdm.test.suite;


import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.aspectj.PropertyChangeTest;
import eu.etaxonomy.cdm.database.DatabaseEnumTest;

import eu.etaxonomy.cdm.model.name.*;
//import eu.etaxonomy.cdm.persistence.dao.common.DefinedTermDaoImplTest;;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    PropertyChangeTest.class,
    DatabaseEnumTest.class,
    //DefinedTermDaoImplTest.class
})

public class CdmTestSuite {
	static Logger logger = Logger.getLogger(CdmTestSuite.class);

	// the class remains completely empty, 
    // being used only as a holder for the above annotations

	
	//console test  //TODO test
	public static void consoleRun(){
        org.junit.runner.JUnitCore.runClasses(
             PropertyChangeTest.class,
             DatabaseEnumTest.class
            // DefinedTermDaoImplTest.class
	    ); 
	}
}