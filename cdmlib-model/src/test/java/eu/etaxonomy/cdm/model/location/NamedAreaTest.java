// $Id$
/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.location;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import eu.etaxonomy.cdm.model.common.DefaultTermInitializer;
import eu.etaxonomy.cdm.model.common.Language;

/**
 * @author a.mueller
 * @date 26.05.2011
 *
 */
public class NamedAreaTest {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(NamedAreaTest.class);
	
	private NamedArea namedArea1;
	private NamedAreaLevel level1;
	private NamedAreaType areaType1;
	
	
	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		DefaultTermInitializer initializer = new DefaultTermInitializer();
		initializer.initialize();
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		namedArea1 = NamedArea.NewInstance("Description for Named Area 1", "Named Area 1", "NA1");
		level1 = NamedAreaLevel.NewInstance("Description for level 1", "Level 1", "L1");
		namedArea1.setLevel(level1);
	}
	
	@Test
	public void NewInstanceStringStringStringTest(){
		Assert.assertEquals("Description for Named Area 1", namedArea1.getDescription());
		Assert.assertEquals("Named Area 1", namedArea1.getLabel());
		Assert.assertEquals("NA1", namedArea1.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel());
	}

	
	@Test
	public void labelWithLevelTest(){
		Assert.assertEquals("Named Area 1 - Level 1", NamedArea.labelWithLevel(namedArea1, Language.DEFAULT()));
		Assert.assertEquals("Germany - WaterbodyOrCountry", NamedArea.labelWithLevel(WaterbodyOrCountry.GERMANY(), Language.DEFAULT()));
		Assert.assertEquals("Germany - TDWG Level 3", NamedArea.labelWithLevel(TdwgArea.getAreaByTdwgAbbreviation("GER"), Language.DEFAULT()));
		NamedArea namedArea2 = NamedArea.NewInstance("Description for Named Area 2", "", "NA2");
		Assert.assertEquals("NA2 - NamedArea", NamedArea.labelWithLevel(namedArea2, Language.DEFAULT()));
		NamedArea namedArea3 = NamedArea.NewInstance("Description for Named Area 3", null, " ");
		Assert.assertEquals("Description for Named Area 3 - NamedArea", NamedArea.labelWithLevel(namedArea3, Language.DEFAULT()));
		
		//TODO include Vocabulay information
	}

}
