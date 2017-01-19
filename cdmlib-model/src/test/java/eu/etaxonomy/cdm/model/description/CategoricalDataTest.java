/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.description;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;

import org.junit.Before;
import org.junit.Test;

import eu.etaxonomy.cdm.model.common.DefinedTerm;
import eu.etaxonomy.cdm.model.common.TermType;
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
        TermVocabulary<State> useCategoryVocabulary = TermVocabulary.NewInstance(TermType.Feature, "Use category vocabulary", "use categories", null, null);
        State useCategory1 = State.NewInstance("My first use category", "use category 1", null);
        useCategoryVocabulary.addTerm(useCategory1);
        State useCategory2 = State.NewInstance("My favorite use category", "use category 2", null);
        useCategoryVocabulary.addTerm(useCategory2);

        StateData stateData = StateData.NewInstance(useCategory1);
        data.addStateData(stateData);
        StateData stateData2 = StateData.NewInstance(useCategory2);
        stateData2.addModifier(DefinedTerm.NewModifierInstance(null, "Any modifer", null));
        data.addStateData(stateData2);

        List<State> states = data.getStatesOnly();
        Assert.assertEquals("There should be 2 states", 2, states.size());

    }

    /**
     * Test method for {@link eu.etaxonomy.cdm.model.description.CategoricalData#setStateDataOnly(List)}.
     */
    @Test
    public void testSetStatesOnly() {
        TaxonDescription desc = TaxonDescription.NewInstance();
        CategoricalData data = CategoricalData.NewInstance();
        desc.addElement(data);
        TermVocabulary<State> useCategoryVocabulary = TermVocabulary.NewInstance(TermType.Feature,"Use category vocabulary", "use categories", null, null);
        State useCategory1 = State.NewInstance("My first use category", "use category 1", null);
        useCategoryVocabulary.addTerm(useCategory1);
        data.addStateData(useCategory1);
        Assert.assertEquals("There should be 1 state now", 1, data.getStateData().size());

        State useCategory2 = State.NewInstance("My favorite use category", "use category 2", null);
        useCategoryVocabulary.addTerm(useCategory2);
        State useCategory3 = State.NewInstance("My 3rd use category", "use category 3", null);
        useCategoryVocabulary.addTerm(useCategory3);
        List<State> newStates = new ArrayList<State>();
        newStates.addAll(Arrays.asList(useCategory2, useCategory3));

        // setting new states and thus removing useCategory1
        List<StateData> stateDataList = data.setStateDataOnly(newStates);
        Assert.assertEquals("There should be 2 StateData objects", 2, stateDataList.size());
        Assert.assertEquals("There should be 2 StateData objects", 2, data.getStateData().size());
        Assert.assertFalse("Category 1 should not be included anymore", data.getStatesOnly().contains(useCategory1));
        Assert.assertTrue("Category 2 should be included", data.getStatesOnly().contains(useCategory2));

    }

}
