/**
* Copyright (C) 2011 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.description;

import java.util.List;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import eu.etaxonomy.cdm.model.common.DefaultTermInitializer;
import eu.etaxonomy.cdm.model.common.DefinedTerm;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.TermType;
import eu.etaxonomy.cdm.model.common.TermVocabulary;


/**
 * @author k.luther
 * @since 11.04.2011
 */
public class DescriptionElementTest {
@SuppressWarnings("unused")
private static Logger logger = Logger.getLogger(DescriptionElementTest.class);


	@BeforeClass
	public static void setUpBeforeClass() {
		if (Language.DEFAULT() == null){
		    DefaultTermInitializer vocabularyStore = new DefaultTermInitializer();
		    vocabularyStore.initialize();
		}
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {

	}

/* ************************** TESTS **********************************************************/


	@Test
	public void testGetModifiersVocabulary(){
		TaxonDescription desc = TaxonDescription.NewInstance();
		CategoricalData data = CategoricalData.NewInstance();
		desc.addElement(data);
		StateData stateData = StateData.NewInstance();
		data.addStateData(stateData);

		TermType modifierType = TermType.Modifier;
		TermVocabulary<DefinedTerm> plantPartVoc = TermVocabulary.NewInstance(modifierType,"plant parts", "plant parts", "parts", null);
		DefinedTerm leaf = DefinedTerm.NewModifierInstance("leaf", "leaf", null);
		plantPartVoc.addTerm(leaf);
		data.addModifier(leaf);
		DefinedTerm peduncle = DefinedTerm.NewModifierInstance("peduncle", "peduncle", null);
		plantPartVoc.addTerm(peduncle);
		data.addModifier(peduncle);
		DefinedTerm notExistingPart = DefinedTerm.NewModifierInstance("not existing part", "not existing part", null);
		plantPartVoc.addTerm(notExistingPart);

		TermVocabulary<DefinedTerm> ethnicGroupVoc = TermVocabulary.NewInstance(TermType.Modifier,"An ethnic group", "ethnic group", null, null);
		DefinedTerm scots = DefinedTerm.NewModifierInstance("Scots ", "Scots", null);
		ethnicGroupVoc.addTerm(scots);
		data.addModifier(scots);


		List<DefinedTerm> modifiers = data.getModifiers(plantPartVoc);
		Assert.assertEquals("There should be 2 modifiers of type 'plant part'", 2, modifiers.size());
		Assert.assertEquals("There should be 3 terms in the 'plant part' vocabulary", 3, plantPartVoc.size());
		Assert.assertEquals("There should be 1 modifiers of type 'ethnic group'", 1, data.getModifiers(ethnicGroupVoc).size());
		Assert.assertEquals("There should be 3 modifiers all together", 3, data.getModifiers().size());

	}

}


