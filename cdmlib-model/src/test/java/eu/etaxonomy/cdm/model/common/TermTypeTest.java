// $Id$
/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.common;

import static org.junit.Assert.*;

import org.junit.Test;

public class TermTypeTest {

	@Test
	public void test() {
		TermType languageType = TermType.Language;
		assertEquals(TermType.byKey("LA"), languageType);
		
	}

}
