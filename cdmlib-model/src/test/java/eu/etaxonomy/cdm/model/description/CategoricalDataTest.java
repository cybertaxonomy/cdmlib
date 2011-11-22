// $Id$
/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.description;

import static org.junit.Assert.*;

import java.util.List;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import eu.etaxonomy.cdm.model.common.TermVocabulary;

/**
 * @author a.mueller
 * @date 22.11.2011
 *
 */
public class CategoricalDataTest {

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
	}

//************************ TESTS ********************************************	
	
	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.description.CategoricalData#getStatesOnly()}.
	 */
	@Test
	public void testGetStatesOnly() {
		TaxonDescription desc = TaxonDescription.NewInstance();
		CategoricalData data = CategoricalData.NewInstance();
		desc.addElement(data);
		TermVocabulary<State> useCategoryVocabulary = TermVocabulary.NewInstance("Use category vocabulary", "use categories", null, null); 
		State useCategory1 = State.NewInstance("My first use category", "use category 1", null);
		useCategoryVocabulary.addTerm(useCategory1);
		State useCategory2 = State.NewInstance("My favorite use category", "use category 2", null);
		useCategoryVocabulary.addTerm(useCategory2);
		
		StateData stateData = StateData.NewInstance(useCategory1);
		data.addState(stateData);
		StateData stateData2 = StateData.NewInstance(useCategory2);
		stateData2.addModifier(Modifier.NewInstance(null, "Any modifer", null));
		data.addState(stateData2);
		
		List<State> states = data.getStatesOnly();
		Assert.assertEquals("There should be 2 states", 2, states.size());
		
		
	}

}
