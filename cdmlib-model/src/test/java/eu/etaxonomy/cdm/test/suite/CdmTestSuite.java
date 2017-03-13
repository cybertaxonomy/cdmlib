/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.test.suite;

import org.apache.log4j.Logger;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import eu.etaxonomy.cdm.aspectj.PropertyChangeTest;
import eu.etaxonomy.cdm.model.common.CdmBaseTest;
import eu.etaxonomy.cdm.model.common.DefinedTermBaseTest;
import eu.etaxonomy.cdm.model.common.MarkerTypeTest;
import eu.etaxonomy.cdm.model.common.OrderedTermBaseTest;
import eu.etaxonomy.cdm.model.common.OrderedTermVocabularyTest;
import eu.etaxonomy.cdm.model.common.TermVocabularyTest;
import eu.etaxonomy.cdm.model.common.init.TermLoaderTest;
import eu.etaxonomy.cdm.model.description.TextDataTest;
import eu.etaxonomy.cdm.model.name.BotanicalNameTest;
import eu.etaxonomy.cdm.model.taxon.TaxonTest;
import eu.etaxonomy.cdm.strategy.cache.name.BotanicNameCacheStrategyTest;
import eu.etaxonomy.cdm.strategy.parser.NonViralNameParserImplTest;


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
			//description
//			PresenceAbsenceTermBaseTest.class,
			TextDataTest.class,
			//name
			BotanicalNameTest.class,
			//taxon
			TaxonTest.class,
			//strategy
			BotanicNameCacheStrategyTest.class,
			NonViralNameParserImplTest.class,
			MarkerTypeTest.class
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
				//description
//				PresenceAbsenceTermBaseTest.class,
				TextDataTest.class,
				//name
				BotanicalNameTest.class,
				//taxon
				TaxonTest.class,
				//strategy
				BotanicNameCacheStrategyTest.class,
				NonViralNameParserImplTest.class
			);
	}
}
