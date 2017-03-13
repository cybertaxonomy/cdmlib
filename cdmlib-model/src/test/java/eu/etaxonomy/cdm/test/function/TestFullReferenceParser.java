/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.test.function;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.model.name.IBotanicalName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonNameFactory;
import eu.etaxonomy.cdm.strategy.parser.INonViralNameParser;
import eu.etaxonomy.cdm.strategy.parser.NonViralNameParserImpl;

/**
 * @author a.mueller
 * @created 21.11.2008
 */
public class TestFullReferenceParser {
	private static final Logger logger = Logger.getLogger(TestFullReferenceParser.class);


	private boolean test(){
		INonViralNameParser parser = NonViralNameParserImpl.NewInstance();
		IBotanicalName nameToBeFilled = TaxonNameFactory.NewBotanicalInstance(null);
		String fullReference = "Abies alba Mill.,  Sp.   Pl. 4: 455. 1987.";
		boolean makeEmpty = false;
		Rank rank = null;
		parser.parseReferencedName(nameToBeFilled, fullReference, rank, makeEmpty);
		return true;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		TestFullReferenceParser test = new TestFullReferenceParser();
		test.test();

	}
}
