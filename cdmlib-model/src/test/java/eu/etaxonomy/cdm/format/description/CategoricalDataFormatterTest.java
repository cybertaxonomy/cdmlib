/**
* Copyright (C) 2020 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.format.description;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import eu.etaxonomy.cdm.format.ICdmFormatter.FormatKey;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.description.CategoricalData;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.NoDescriptiveDataStatus;
import eu.etaxonomy.cdm.model.description.State;
import eu.etaxonomy.cdm.model.description.StateData;
import eu.etaxonomy.cdm.model.term.DefinedTerm;
import eu.etaxonomy.cdm.test.TermTestBase;

/**
 * Test for {@link CategoricalDataFormatter}.
 *
 * @author a.mueller
 * @since 11.03.2020
 */
public class CategoricalDataFormatterTest extends TermTestBase {

    private State state1;
    private State state2;
    private State state3;
    private StateData stateData1;
    private StateData stateData2;
    private StateData stateData3;

    @Before
    public void setUp() throws Exception {
        state1 = State.NewInstance("state1 text", "state1", "st.1");
        state1.getRepresentations().iterator().next().setPlural("state1s");
        state2 = State.NewInstance("state2 text", "state2", "st.2");
        state3 = State.NewInstance("state3 text", "state3", "st.3");
        stateData1 = StateData.NewInstance(state1);
        stateData2 = StateData.NewInstance(state2);
        stateData3 = StateData.NewInstance(state3);
    }

    @Test
    public void testFormat() {
        CategoricalData catData = CategoricalData.NewInstance(Feature.HABITAT());
        catData.addStateData(stateData1);
        FormatKey[] formatKey = null;
        CategoricalDataFormatter formatter = new CategoricalDataFormatter(catData, formatKey);
        String text = formatter.format(catData, formatKey);
        Assert.assertEquals("state1", text);
        catData.addStateData(stateData2);
        text = formatter.format(catData, formatKey);
        Assert.assertEquals("state1, state2", text);
        catData.addStateData(stateData3);
        text = formatter.format(catData, formatKey);
        Assert.assertEquals("state1, state2, state3", text);

        //plural
        stateData1.setUsePlural(true);
        text = formatter.format(catData, formatKey);
        Assert.assertEquals("state1s, state2, state3", text);

        //state modifier
        stateData1.addModifier(DefinedTerm.SEX_FEMALE());  //use female and male here to avoid having to create a new modifier
        stateData3.addModifier(DefinedTerm.SEX_MALE());
        stateData3.addModifier(DefinedTerm.SEX_UNKNOWN());
        stateData2.putModifyingText(Language.DEFAULT(), "textmodi");

        text = formatter.format(catData, formatKey);
        //TODO do we want a separator between 2 statedata modifiers, here male and unknown?
        Assert.assertEquals("female state1s, textmodi state2, male unknown state3", text);

        //TODO test with additional basedata like timeperiod etc.


    }

    @Test
    public void testFormatNoData() {
        //no data status
        CategoricalData catData = CategoricalData.NewInstance(Feature.HABITAT());
        FormatKey[] formatKey = null;
        CategoricalDataFormatter formatter = new CategoricalDataFormatter(catData, formatKey);

        //fully empty
        String text = formatter.format(catData, formatKey);
        Assert.assertEquals("", text);  //behavior may change in future

        //data unavailable
        catData.setNoDataStatus(NoDescriptiveDataStatus.DataUnavailable);
        text = formatter.format(catData, formatKey);
        Assert.assertEquals("data unavailable", text);

        //data unavailable, but still data exists
        catData.addStateData(stateData2);
        text = formatter.format(catData, formatKey);
        Assert.assertEquals("data unavailable (state2)", text);
    }
}