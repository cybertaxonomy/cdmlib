package eu.etaxonomy.cdm.test.suite;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.aspectj.PropertyChangeTest;

import eu.etaxonomy.cdm.model.common.*;
import eu.etaxonomy.cdm.model.name.*;
import eu.etaxonomy.cdm.model.taxon.*;
import eu.etaxonomy.cdm.strategy.BotanicNameCacheStrategyTest;
import eu.etaxonomy.cdm.strategy.TaxonNameParserBotanicalNameImplTest;


@RunWith(Suite.class)
@Suite.SuiteClasses( 
		{ 	
			//aspectj
			PropertyChangeTest.class,
			//common
			CdmBaseTest.class,
			DefinedTermBaseTest.class,
			OrderedTermBaseTest.class,
			OrderedTermVocabularyTest.class,
			TermLoaderTest.class,
			TermVocabularyTest.class,
			//name
			BotanicalNameTest.class,
			//taxon
			TaxonTest.class,
			//strategy
			BotanicNameCacheStrategyTest.class,
			TaxonNameParserBotanicalNameImplTest.class
		}
	)
public class CdmTestSuite {
	static Logger logger = Logger.getLogger(CdmTestSuite.class);

	// the class remains completely empty, 
	// being used only as a holder for the above annotations

	//console test  //TODO test
	public static void consoleRun() {
		org.junit.runner.JUnitCore.runClasses(
				//aspectj
				PropertyChangeTest.class,
				//common
				CdmBaseTest.class,
				DefinedTermBaseTest.class,
				OrderedTermBaseTest.class,
				OrderedTermVocabularyTest.class,
				TermLoaderTest.class,
				TermVocabularyTest.class,
				//name
				BotanicalNameTest.class,
				//taxon
				TaxonTest.class,
				//strategy
				BotanicNameCacheStrategyTest.class,
				TaxonNameParserBotanicalNameImplTest.class
			);
	}
}