package eu.etaxonomy.cdm.strategy.generate;

import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import eu.etaxonomy.cdm.model.description.CategoricalData;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.QuantitativeData;
import eu.etaxonomy.cdm.model.description.State;
import eu.etaxonomy.cdm.model.description.StateData;
import eu.etaxonomy.cdm.model.description.StatisticalMeasure;
import eu.etaxonomy.cdm.model.description.StatisticalMeasurementValue;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.name.INonViralName;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.taxon.Taxon;

/**
 * @author m.venin
 * @created 16.12.2010
 */

public class IdentificationKeyGeneratorTest {
	@SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(IdentificationKeyGeneratorTest.class);

	private Feature feature1;
	private Feature feature2;
	private Feature feature3;
	private Feature feature4;

	private Taxon taxon1;
	private Taxon taxon2;
	private Taxon taxon3;
	private Taxon taxon4;
	private Taxon taxon5;
	private Taxon taxon6;
	private Taxon taxon7;
	private Taxon taxon8;

	private TaxonDescription taxond1;
	private TaxonDescription taxond2;
	private TaxonDescription taxond3;
	private TaxonDescription taxond4;
	private TaxonDescription taxond5;
	private TaxonDescription taxond6;
	private TaxonDescription taxond7;
	private TaxonDescription taxond8;

	private Set<TaxonDescription> taxa;
	private List<Feature> features;

	private PolytomousKeyGenerator generator;

	@Before
	public void setUp() throws Exception {
		feature1 = Feature.NewInstance("","Shape of the head","");
		feature2 = Feature.NewInstance("","Presence of wings","");
		feature3 = Feature.NewInstance("","Length of wings","");
		feature4 = Feature.NewInstance("","Colour","");

		INonViralName tn1 = TaxonNameBase.NewNonViralInstance(null);
		INonViralName tn2 = TaxonNameBase.NewNonViralInstance(null);
		INonViralName tn3 = TaxonNameBase.NewNonViralInstance(null);
		INonViralName tn4 = TaxonNameBase.NewNonViralInstance(null);
		INonViralName tn5 = TaxonNameBase.NewNonViralInstance(null);
		INonViralName tn6 = TaxonNameBase.NewNonViralInstance(null);
		INonViralName tn7 = TaxonNameBase.NewNonViralInstance(null);
		INonViralName tn8 = TaxonNameBase.NewNonViralInstance(null);

		taxon1 = Taxon.NewInstance(tn1, null);
		taxon2 = Taxon.NewInstance(tn2, null);
		taxon3 = Taxon.NewInstance(tn3, null);
		taxon4 = Taxon.NewInstance(tn4, null);
		taxon5 = Taxon.NewInstance(tn5, null);
		taxon6 = Taxon.NewInstance(tn6, null);
		taxon7 = Taxon.NewInstance(tn7, null);
		taxon8 = Taxon.NewInstance(tn8, null);

		taxond1 = TaxonDescription.NewInstance(taxon1);
		taxond2 = TaxonDescription.NewInstance(taxon2);
		taxond3 = TaxonDescription.NewInstance(taxon3);
		taxond4 = TaxonDescription.NewInstance(taxon4);
		taxond5 = TaxonDescription.NewInstance(taxon5);
		taxond6 = TaxonDescription.NewInstance(taxon6);
		taxond7 = TaxonDescription.NewInstance(taxon7);
		taxond8 = TaxonDescription.NewInstance(taxon8);

		CategoricalData catd11 = CategoricalData.NewInstance();
		catd11.setFeature(feature1);
		StateData sd11 = StateData.NewInstance();
		State s11 = State.NewInstance("","Triangular","");
		State s12 = State.NewInstance("","Circular","");
		sd11.setState(s11);
		catd11.addStateData(sd11);

		CategoricalData catd12 = CategoricalData.NewInstance();
		catd12.setFeature(feature1);
		StateData sd12 = StateData.NewInstance();
		sd12.setState(s11);
		catd12.addStateData(sd12);

		CategoricalData catd13 = CategoricalData.NewInstance();
		catd13.setFeature(feature1);
		StateData sd13 = StateData.NewInstance();
		sd13.setState(s11);
		catd13.addStateData(sd13);

		CategoricalData catd14 = CategoricalData.NewInstance();
		catd14.setFeature(feature1);
		StateData sd14 = StateData.NewInstance();
		sd14.setState(s11);
		catd14.addStateData(sd14);

		CategoricalData catd15 = CategoricalData.NewInstance();
		catd15.setFeature(feature1);
		StateData sd15 = StateData.NewInstance();
		sd15.setState(s12);
		catd15.addStateData(sd15);

		CategoricalData catd16 = CategoricalData.NewInstance();
		catd16.setFeature(feature1);
		StateData sd16 = StateData.NewInstance();
		sd16.setState(s12);
		catd16.addStateData(sd16);

		CategoricalData catd17 = CategoricalData.NewInstance();
		catd17.setFeature(feature1);
		StateData sd17 = StateData.NewInstance();
		sd17.setState(s12);
		catd17.addStateData(sd17);

		CategoricalData catd18 = CategoricalData.NewInstance();
		catd18.setFeature(feature1);
		StateData sd18 = StateData.NewInstance();
		sd18.setState(s12);
		catd18.addStateData(sd18);

		/*************************/

		CategoricalData catd21 = CategoricalData.NewInstance();
		catd21.setFeature(feature2);
		StateData sd21 = StateData.NewInstance();
		State s21 = State.NewInstance("","Yes","");
		State s22 = State.NewInstance("","No","");
		sd21.setState(s21);
		catd21.addStateData(sd21);

		CategoricalData catd22 = CategoricalData.NewInstance();
		catd22.setFeature(feature2);
		StateData sd22 = StateData.NewInstance();
		sd22.setState(s21);
		catd22.addStateData(sd22);

		CategoricalData catd23 = CategoricalData.NewInstance();
		catd23.setFeature(feature2);
		StateData sd23 = StateData.NewInstance();
		sd23.setState(s21);
		catd23.addStateData(sd23);

		CategoricalData catd24 = CategoricalData.NewInstance();
		catd24.setFeature(feature2);
		StateData sd24 = StateData.NewInstance();
		sd24.setState(s21);
		catd24.addStateData(sd24);

		CategoricalData catd25 = CategoricalData.NewInstance();
		catd25.setFeature(feature2);
		StateData sd25 = StateData.NewInstance();
		sd25.setState(s21);
		catd25.addStateData(sd25);

		CategoricalData catd26 = CategoricalData.NewInstance();
		catd26.setFeature(feature2);
		StateData sd26 = StateData.NewInstance();
		sd26.setState(s21);
		catd26.addStateData(sd26);

		CategoricalData catd27 = CategoricalData.NewInstance();
		catd27.setFeature(feature2);
		StateData sd27 = StateData.NewInstance();
		sd27.setState(s21);
		catd27.addStateData(sd27);

		CategoricalData catd28 = CategoricalData.NewInstance();
		catd28.setFeature(feature2);
		StateData sd28 = StateData.NewInstance();
		sd28.setState(s22);
		catd28.addStateData(sd28);

		/*************************/

		QuantitativeData qtd31 = QuantitativeData.NewInstance();
		StatisticalMeasurementValue smv311 = StatisticalMeasurementValue.NewInstance();
		smv311.setValue(0);
		StatisticalMeasure sm311 = StatisticalMeasure.MIN();
		smv311.setType(sm311);
		StatisticalMeasurementValue smv312 = StatisticalMeasurementValue.NewInstance();
		smv312.setValue(3);
		StatisticalMeasure sm312 = StatisticalMeasure.MAX();
		smv312.setType(sm312);
		qtd31.addStatisticalValue(smv311);
		qtd31.addStatisticalValue(smv312);

		QuantitativeData qtd32 = QuantitativeData.NewInstance();
		StatisticalMeasurementValue smv321 = StatisticalMeasurementValue.NewInstance();
		smv321.setValue(0);
		StatisticalMeasure sm321 = StatisticalMeasure.MIN();
		smv321.setType(sm321);
		StatisticalMeasurementValue smv322 = StatisticalMeasurementValue.NewInstance();
		smv322.setValue(3);
		StatisticalMeasure sm322 = StatisticalMeasure.MAX();
		smv322.setType(sm322);
		qtd32.addStatisticalValue(smv321);
		qtd32.addStatisticalValue(smv322);

		QuantitativeData qtd33 = QuantitativeData.NewInstance();
		StatisticalMeasurementValue smv331 = StatisticalMeasurementValue.NewInstance();
		smv331.setValue(6);
		StatisticalMeasure sm331 = StatisticalMeasure.MIN();
		smv331.setType(sm331);
		StatisticalMeasurementValue smv332 = StatisticalMeasurementValue.NewInstance();
		smv332.setValue(9);
		StatisticalMeasure sm332 = StatisticalMeasure.MAX();
		smv332.setType(sm332);
		qtd33.addStatisticalValue(smv331);
		qtd33.addStatisticalValue(smv332);

		QuantitativeData qtd34 = QuantitativeData.NewInstance();
		StatisticalMeasurementValue smv341 = StatisticalMeasurementValue.NewInstance();
		smv341.setValue(6);
		StatisticalMeasure sm341 = StatisticalMeasure.MIN();
		smv341.setType(sm341);
		StatisticalMeasurementValue smv342 = StatisticalMeasurementValue.NewInstance();
		smv342.setValue(9);
		StatisticalMeasure sm342 = StatisticalMeasure.MAX();
		smv342.setType(sm342);
		qtd34.addStatisticalValue(smv341);
		qtd34.addStatisticalValue(smv342);

		QuantitativeData qtd35 = QuantitativeData.NewInstance();
		StatisticalMeasurementValue smv351 = StatisticalMeasurementValue.NewInstance();
		smv351.setValue(0);
		StatisticalMeasure sm351 = StatisticalMeasure.MIN();
		smv351.setType(sm351);
		StatisticalMeasurementValue smv352 = StatisticalMeasurementValue.NewInstance();
		smv352.setValue(3);
		StatisticalMeasure sm352 = StatisticalMeasure.MAX();
		smv352.setType(sm352);
		qtd35.addStatisticalValue(smv351);
		qtd35.addStatisticalValue(smv352);

		QuantitativeData qtd36 = QuantitativeData.NewInstance();
		StatisticalMeasurementValue smv361 = StatisticalMeasurementValue.NewInstance();
		smv361.setValue(0);
		StatisticalMeasure sm361 = StatisticalMeasure.MIN();
		smv361.setType(sm361);
		StatisticalMeasurementValue smv362 = StatisticalMeasurementValue.NewInstance();
		smv362.setValue(3);
		StatisticalMeasure sm362 = StatisticalMeasure.MAX();
		smv362.setType(sm362);
		qtd36.addStatisticalValue(smv361);
		qtd36.addStatisticalValue(smv362);

		QuantitativeData qtd37 = QuantitativeData.NewInstance();
		StatisticalMeasurementValue smv371 = StatisticalMeasurementValue.NewInstance();
		smv371.setValue(6);
		StatisticalMeasure sm371 = StatisticalMeasure.MIN();
		smv371.setType(sm371);
		StatisticalMeasurementValue smv372 = StatisticalMeasurementValue.NewInstance();
		smv372.setValue(9);
		StatisticalMeasure sm372 = StatisticalMeasure.MAX();
		smv372.setType(sm372);
		qtd37.addStatisticalValue(smv371);
		qtd37.addStatisticalValue(smv372);

//		QuantitativeData qtd38 = QuantitativeData.NewInstance();
//		StatisticalMeasurementValue smv381 = StatisticalMeasurementValue.NewInstance();
//		smv381.setValue(6);
//		StatisticalMeasure sm381 = StatisticalMeasure.MIN();
//		smv381.setType(sm381);
//		StatisticalMeasurementValue smv382 = StatisticalMeasurementValue.NewInstance();
//		smv382.setValue(9);
//		StatisticalMeasure sm382 = StatisticalMeasure.MAX();
//		smv382.setType(sm382);
//		qtd38.addStatisticalValue(smv381);
//		qtd38.addStatisticalValue(smv382);

		/*************************/

		CategoricalData catd41 = CategoricalData.NewInstance();
		catd41.setFeature(feature4);
		StateData sd41 = StateData.NewInstance();
		State s41 = State.NewInstance("","Blue","");
		State s42 = State.NewInstance("","Yellow","");
		sd41.setState(s41);
		catd41.addStateData(sd41);

		CategoricalData catd42 = CategoricalData.NewInstance();
		catd42.setFeature(feature4);
		StateData sd42 = StateData.NewInstance();
		sd42.setState(s42);
		catd42.addStateData(sd42);

		CategoricalData catd43 = CategoricalData.NewInstance();
		catd43.setFeature(feature4);
		StateData sd43 = StateData.NewInstance();
		sd43.setState(s41);
		catd43.addStateData(sd43);

		CategoricalData catd44 = CategoricalData.NewInstance();
		catd44.setFeature(feature4);
		StateData sd44 = StateData.NewInstance();
		sd44.setState(s42);
		catd44.addStateData(sd44);

		CategoricalData catd45 = CategoricalData.NewInstance();
		catd45.setFeature(feature4);
		StateData sd45 = StateData.NewInstance();
		sd45.setState(s41);
		catd45.addStateData(sd45);

		CategoricalData catd46 = CategoricalData.NewInstance();
		catd46.setFeature(feature4);
		StateData sd46 = StateData.NewInstance();
		sd46.setState(s41);
		catd46.addStateData(sd46);

		CategoricalData catd47 = CategoricalData.NewInstance();
		catd47.setFeature(feature4);
		StateData sd47 = StateData.NewInstance();
		sd47.setState(s41);
		catd47.addStateData(sd47);

		CategoricalData catd48 = CategoricalData.NewInstance();
		catd48.setFeature(feature4);
		StateData sd48 = StateData.NewInstance();
		sd48.setState(s41);
		catd48.addStateData(sd48);

		/*************************/

		taxond1.addElement(catd11);
		taxond1.addElement(catd21);
		taxond1.addElement(qtd31);
		taxond1.addElement(catd41);

		taxond2.addElement(catd12);
		taxond2.addElement(catd22);
		taxond2.addElement(qtd32);
		taxond2.addElement(catd42);

		taxond3.addElement(catd13);
		taxond3.addElement(catd23);
		taxond3.addElement(qtd33);
		taxond3.addElement(catd43);

		taxond4.addElement(catd14);
		taxond4.addElement(catd24);
		taxond4.addElement(qtd34);
		taxond4.addElement(catd44);

		taxond5.addElement(catd15);
		taxond5.addElement(catd25);
		taxond5.addElement(qtd35);
		taxond5.addElement(catd45);

		taxond6.addElement(catd16);
		taxond6.addElement(catd26);
		taxond6.addElement(qtd36);
		taxond6.addElement(catd46);

		taxond7.addElement(catd17);
		taxond7.addElement(catd27);
		taxond7.addElement(qtd37);
		taxond7.addElement(catd47);

		taxond8.addElement(catd18);
		taxond8.addElement(catd28);
//		taxond8.addElement(qtd38); // This taxon has no wings
		taxond8.addElement(catd48);

		/*************************/

		features = new ArrayList<Feature>();
		features.add(feature1);
		features.add(feature2);
		features.add(feature3);
		features.add(feature4);

		taxa = new HashSet<TaxonDescription>();
		taxa.add(taxond1);
		taxa.add(taxond2);
		taxa.add(taxond3);
		taxa.add(taxond4);
		taxa.add(taxond5);
		taxa.add(taxond6);
		taxa.add(taxond7);
		taxa.add(taxond8);

	}

//*************************** TESTS *********************** /


	@Test
	public void testInvoke() {
		generator = new PolytomousKeyGenerator();
		generator.setFeatures(features);
		generator.setTaxa(taxa);
		assertNotNull("Key should exist.",generator.invoke());
	}

	@Test
	public void testInvokeMergeModeON() {
//		generator = new IdentificationKeyGenerator();
//		generator.setFeatures(features);
//		generator.setTaxa(taxa);
//		generator.mergeModeON();
//		generator.invoke();
//		assertNotNull("Key should exist (merge mode ON).",generator.invoke());;
	}

	@Test
	public void testInvokeWithDependencies() {
//		generator = new IdentificationKeyGenerator();
//		generator.setFeatures(features);
//		generator.setTaxa(taxa);
////		generator.setDependencies(tree);// TODO create a tree with dependencies to test this function
//		generator.invoke();
//		assertNotNull("Key should exist (dependencies are present).",generator.invoke());;
	}

}
