/**
* Copyright (C) 2011 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.description;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import eu.etaxonomy.cdm.model.common.DefaultTermInitializer;
import eu.etaxonomy.cdm.model.common.DefinedTerm;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.LanguageString;
import eu.etaxonomy.cdm.model.common.OriginalSourceType;
import eu.etaxonomy.cdm.model.common.TermType;
import eu.etaxonomy.cdm.model.common.TermVocabulary;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnit;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.model.taxon.Taxon;


/**
 * @author k.luther
 * @created 11.04.2011
 * @version 1.0
 */
public class DescriptionElementTest {
@SuppressWarnings("unused")
private static Logger logger = Logger.getLogger(DescriptionElementTest.class);


	private CategoricalData categorialData;
	private IndividualsAssociation indAssociation;
	private QuantitativeData quantData;
	private Taxon taxon;
	private TaxonInteraction taxonInteraction;

	@BeforeClass
	public static void setUpBeforeClass() {
		DefaultTermInitializer vocabularyStore = new DefaultTermInitializer();
		vocabularyStore.initialize();
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		categorialData = CategoricalData.NewInstance();
		Media media = Media.NewInstance(null, 1000, "jpeg", null);
		categorialData.addMedia(media);

		DescriptionElementSource source = DescriptionElementSource.NewInstance(OriginalSourceType.Unknown);
		Reference citation = ReferenceFactory.newArticle();
		citation.setTitle("Test");
		source.setCitation(citation);
		categorialData.addSource(source );
		StateData state = StateData.NewInstance();
		categorialData.addStateData(state);

		indAssociation = IndividualsAssociation.NewInstance();

		DerivedUnit associatedSpecimen = DerivedUnit.NewPreservedSpecimenInstance();
		associatedSpecimen.setIndividualCount(2);

		indAssociation.setAssociatedSpecimenOrObservation(associatedSpecimen);
		LanguageString langString = LanguageString.NewInstance("Test", Language.ENGLISH());

		indAssociation.putDescription(langString);

		quantData = QuantitativeData.NewInstance();

		StatisticalMeasurementValue statisticalValue = StatisticalMeasurementValue.NewInstance();

		statisticalValue.setType(StatisticalMeasure.AVERAGE() );

		statisticalValue.setValue((float) 23.8);

		quantData.addStatisticalValue(statisticalValue);
		taxon = Taxon.NewInstance(BotanicalName.NewInstance(Rank.SPECIES(), "Abies", null, "alba", null, null, null, null, null), null);
		taxonInteraction = TaxonInteraction.NewInstance();
		taxonInteraction.setTaxon2(taxon);
		langString = LanguageString.NewInstance("TestTaxonInteraction", Language.ENGLISH());

		taxonInteraction.putDescription(langString);

	}

/* ************************** TESTS **********************************************************/
	@Test
	public void testCloneCategorialData(){
		CategoricalData clone = (CategoricalData)categorialData.clone();
		assertEquals(clone.getStateData().size(),categorialData.getStateData().size() );
		assertSame(clone.getStateData().get(0), categorialData.getStateData().get(0));
		assertNotSame(clone, categorialData);

	}

	@Test
	public void testCloneIndividualAssociation(){
		IndividualsAssociation clone = (IndividualsAssociation) indAssociation.clone();
		assertEquals(clone.getFeature(), indAssociation.getFeature());
		assertNotSame(clone.getDescription().get(Language.ENGLISH()), indAssociation.getDescription().get(Language.ENGLISH()));
	}


	@Test
	public void testCloneQuantitativeData(){
		QuantitativeData clone = (QuantitativeData) quantData.clone();
		assertTrue(clone.getStatisticalValues().iterator().next().getValue() == quantData.getStatisticalValues().iterator().next().getValue());
		assertNotSame(clone.getStatisticalValues().iterator().next(), quantData.getStatisticalValues().iterator().next());

	}

	@Test
	public void testCloneTaxonInteraction(){
		TaxonInteraction clone = (TaxonInteraction)taxonInteraction.clone();
		assertNotSame(clone.getDescriptions().get(Language.ENGLISH()), taxonInteraction.getDescriptions().get(Language.ENGLISH()));
		assertTrue(clone.getDescription(Language.ENGLISH()).equals(taxonInteraction.getDescription(Language.ENGLISH())));
	}

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


