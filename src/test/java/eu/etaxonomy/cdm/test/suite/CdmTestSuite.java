package eu.etaxonomy.cdm.test.suite;


import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.aspectj.PropertyChangeTest;

import eu.etaxonomy.cdm.model.name.*;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    TaxonNameTest.class,
    PropertyChangeTest.class
    //, NameRelationshipTest.class
})

public class CdmTestSuite {
	static Logger logger = Logger.getLogger(CdmTestSuite.class);

	// the class remains completely empty, 
    // being used only as a holder for the above annotations

	
	//console test  //TODO test
	public static void consoleRun(){
        org.junit.runner.JUnitCore.runClasses(
             TaxonNameTest.class, 
             PropertyChangeTest.class
             //, NameRelationshipTest.class
	    ); 
	}
}