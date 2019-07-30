package eu.etaxonomy.cdm.strategy.generate;

import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.description.CategoricalData;
import eu.etaxonomy.cdm.model.description.DescriptiveDataSet;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.PolytomousKey;
import eu.etaxonomy.cdm.model.description.PolytomousKeyNode;
import eu.etaxonomy.cdm.model.description.QuantitativeData;
import eu.etaxonomy.cdm.model.description.State;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.name.TaxonNameFactory;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.term.DefaultTermInitializer;
import eu.etaxonomy.cdm.model.term.TermTree;
import eu.etaxonomy.cdm.model.term.TermType;

/**
 * @author m.venin
 * @author a.mueller
 * @since 16.12.2010
 */
public class PolytomousKeyGeneratorTest {
	@SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(PolytomousKeyGeneratorTest.class);

	private Feature featureShape;
	private Feature featurePresence;
	private Feature featureLength;
	private Feature featureColour;

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

	private static UUID uuidFeatureShape = UUID.fromString("a61abb0c-51fb-4af4-aee4-f5894845133f");
	private static UUID uuidFeaturePresence = UUID.fromString("03cb2744-52a0-4127-be5d-265fe59b426f");
	private static UUID uuidFeatureLength = UUID.fromString("5de4f981-83fb-41d2-9900-b52cf5782a85");
	private static UUID uuidFeatureColour = UUID.fromString("7a8deb1a-144f-4be5-ba0d-9e77724697cb");


	private PolytomousKeyGenerator generator;

	@Before
	public void setUp() throws Exception {
	    if(Language.DEFAULT() == null){
            new DefaultTermInitializer().initialize();
        }

	    featureShape = createFeature("Shape of the head", uuidFeatureShape, false);
	    featurePresence = createFeature("Presence of wings", uuidFeaturePresence, false);
	    featureLength = createFeature("Length of wings", uuidFeatureLength, true);
	    featureColour = createFeature("Colour", uuidFeatureColour, false);
	       featureShape.setSupportsCategoricalData(true);
	        featurePresence.setSupportsCategoricalData(true);
	        featureLength.setSupportsQuantitativeData(true);
	        featureColour.setSupportsCategoricalData(true);


		taxon1 = getTaxon(1);
		taxon2 = getTaxon(2);
		taxon3 = getTaxon(3);
		taxon4 = getTaxon(4);
		taxon5 = getTaxon(5);
		taxon6 = getTaxon(6);
		taxon7 = getTaxon(7);
		taxon8 = getTaxon(8);

		taxond1 = TaxonDescription.NewInstance(taxon1);
		taxond2 = TaxonDescription.NewInstance(taxon2);
		taxond3 = TaxonDescription.NewInstance(taxon3);
		taxond4 = TaxonDescription.NewInstance(taxon4);
		taxond5 = TaxonDescription.NewInstance(taxon5);
		taxond6 = TaxonDescription.NewInstance(taxon6);
		taxond7 = TaxonDescription.NewInstance(taxon7);
		taxond8 = TaxonDescription.NewInstance(taxon8);
		taxond1.setTitleCache("td1", true);
		taxond2.setTitleCache("td2", true);
		taxond3.setTitleCache("td3", true);
		taxond4.setTitleCache("td4", true);
		taxond5.setTitleCache("td5", true);
		taxond6.setTitleCache("td6", true);
		taxond7.setTitleCache("td7", true);
		taxond8.setTitleCache("td8", true);

		State triangular = State.NewInstance("", "Triangular", "");
		State circular = State.NewInstance("", "Circular", "");

		State yellow = State.NewInstance("", "Yellow", "");
		State blue = State.NewInstance("","Blue","");

		State yes = State.NewInstance("","Yes","");
		State no = State.NewInstance("","No","");

		triangular.getTitleCache();
		circular.getTitleCache();
		yellow.getTitleCache();
		blue.getTitleCache();
		yes.getTitleCache();
		no.getTitleCache();

		CategoricalData catd11 = CategoricalData.NewInstance(triangular, featureShape);
		CategoricalData catd12 = CategoricalData.NewInstance(triangular, featureShape);
		CategoricalData catd13 = CategoricalData.NewInstance(triangular, featureShape);
		CategoricalData catd14 = CategoricalData.NewInstance(triangular, featureShape);
		CategoricalData catd15 = CategoricalData.NewInstance(circular, featureShape);
		CategoricalData catd16 = CategoricalData.NewInstance(circular, featureShape);
		CategoricalData catd17 = CategoricalData.NewInstance(circular, featureShape);
		CategoricalData catd18 = CategoricalData.NewInstance(circular, featureShape);

		/*************************/

		CategoricalData catd21 = CategoricalData.NewInstance(yes, featurePresence);
		CategoricalData catd22 = CategoricalData.NewInstance(yes, featurePresence);
		CategoricalData catd23 = CategoricalData.NewInstance(yes, featurePresence);
		CategoricalData catd24 = CategoricalData.NewInstance(yes, featurePresence);
		CategoricalData catd25 = CategoricalData.NewInstance(yes, featurePresence);
		CategoricalData catd26 = CategoricalData.NewInstance(yes, featurePresence);
		CategoricalData catd27 = CategoricalData.NewInstance(yes, featurePresence);
		CategoricalData catd28 = CategoricalData.NewInstance(no, featurePresence);

		/*************************/

		QuantitativeData qtd31 = QuantitativeData.NewMinMaxInstance(featureLength, 0, 3);
		QuantitativeData qtd32 = QuantitativeData.NewMinMaxInstance(featureLength, 0, 3);
		QuantitativeData qtd33 = QuantitativeData.NewMinMaxInstance(featureLength, 6, 9);
		QuantitativeData qtd34 = QuantitativeData.NewMinMaxInstance(featureLength, 6, 9);
		QuantitativeData qtd35 = QuantitativeData.NewMinMaxInstance(featureLength, 0, 3);
		QuantitativeData qtd36 = QuantitativeData.NewMinMaxInstance(featureLength, 0, 3);
		QuantitativeData qtd37 = QuantitativeData.NewMinMaxInstance(featureLength, 6, 9);
//		QuantitativeData qtd38 = QuantitativeData.NewMinMaxInstance(feature3, 6, 9);

		/*************************/

		CategoricalData catd41 = CategoricalData.NewInstance(blue, featureColour);
		CategoricalData catd42 = CategoricalData.NewInstance(yellow, featureColour);
		CategoricalData catd43 = CategoricalData.NewInstance(blue, featureColour);
		CategoricalData catd44 = CategoricalData.NewInstance(yellow, featureColour);
		CategoricalData catd45 = CategoricalData.NewInstance(blue, featureColour);
		CategoricalData catd46 = CategoricalData.NewInstance(blue, featureColour);
		CategoricalData catd47 = CategoricalData.NewInstance(blue, featureColour);
		CategoricalData catd48 = CategoricalData.NewInstance(blue, featureColour);

		/*************************/
		taxond1.addElement(catd11); //Shape triangular
		taxond1.addElement(catd21); //present
		taxond1.addElement(qtd31);  //length 0-3
		taxond1.addElement(catd41); //color blue

		taxond2.addElement(catd12);  //Shape triangular
		taxond2.addElement(catd22);  //present
		taxond2.addElement(qtd32);   //length 0-3
		taxond2.addElement(catd42);  //color yellow

		taxond3.addElement(catd13);  //Shape triangular
		taxond3.addElement(catd23);  //present
		taxond3.addElement(qtd33);   //length 6-9
		taxond3.addElement(catd43);  //color blue

		taxond4.addElement(catd14);  //Shape triangular
		taxond4.addElement(catd24);  //present
		taxond4.addElement(qtd34);   //length 6-9
		taxond4.addElement(catd44);  //color yellow

		taxond5.addElement(catd15);  //Shape circular
		taxond5.addElement(catd25);  //present
		taxond5.addElement(qtd35);   //length 0-3
		taxond5.addElement(catd45);  //color blue

		taxond6.addElement(catd16);  //Shape circular
		taxond6.addElement(catd26);  //present
		taxond6.addElement(qtd36);   //length 0-3
		taxond6.addElement(catd46);  //color blue

		taxond7.addElement(catd17);  //Shape circular
		taxond7.addElement(catd27);  //present
		taxond7.addElement(qtd37);   //length 6-9
		taxond7.addElement(catd47);  //color blue

		taxond8.addElement(catd18);  //Shape circular
		taxond8.addElement(catd28);  //absent
//		taxond8.addElement(qtd38); // This taxon has no wings
		taxond8.addElement(catd48);  //color blue

		/*************************/

		features = new ArrayList<>();
		features.add(featureShape);
		features.add(featurePresence);
		features.add(featureLength);
		features.add(featureColour);

		taxa = new HashSet<>();
		taxa.add(taxond1);
		taxa.add(taxond2);
		taxa.add(taxond3);
		taxa.add(taxond4);
		taxa.add(taxond5);
		taxa.add(taxond6);
		taxa.add(taxond7);
		taxa.add(taxond8);

	}

    /**
     * @param title
     * @param uuid
     * @param isQuantitative
     * @return
     */
    private Feature createFeature(String title, UUID uuid, boolean isQuantitative) {
        Feature result = Feature.NewInstance("",title,"");
        result.getTitleCache();
        result.setUuid(uuid);
        if (isQuantitative){
            result.setSupportsQuantitativeData(true);
        }else{
            result.setSupportsCategoricalData(true);
        }
        return result;
    }

    /**
     * @param i
     * @return
     */
    private Taxon getTaxon(int i) {
        TaxonName tn = TaxonNameFactory.NewNonViralInstance(Rank.SPECIES());
        tn.setGenusOrUninomial("Taxon");
        tn.setSpecificEpithet(String.valueOf(i));
        Taxon result = Taxon.NewInstance(tn, ReferenceFactory.newBook());
        result.getTitleCache();
        return result;
    }

//*************************** TESTS *********************** /


	@Test
	public void testInvoke() {
		generator = new PolytomousKeyGenerator();
		PolytomousKeyGeneratorConfigurator configurator = new PolytomousKeyGeneratorConfigurator();
		configurator.setDataSet(createDataSet());
		configurator.setMerge(false);
//		generator.setFeatures(features);
//		generator.setTaxa(taxa);
		PolytomousKey result = generator.invoke(configurator);
		result.setTitleCache("No Merge Key", true);
		assertNotNull("Key should exist.", result);
        PolytomousKeyNode root = result.getRoot();
        Assert.assertEquals(featureShape, root.getFeature());
	    result.print(System.out);
	}

	@Test
	public void testInvokeMergeModeON() {
		generator = new PolytomousKeyGenerator();
		PolytomousKeyGeneratorConfigurator configurator = new PolytomousKeyGeneratorConfigurator();
        configurator.setDataSet(createDataSet());
		configurator.setMerge(true);
		PolytomousKey result = generator.invoke(configurator);
		result.setTitleCache("Merge Key", true);
        assertNotNull("Key should exist (merge mode ON).", result);
        PolytomousKeyNode root = result.getRoot();
	    Assert.assertEquals(featureShape, root.getFeature());
        result.print(System.out);

	}


    @Test
    @Ignore
    public void testInvokeWithDependencies() {
        generator = new PolytomousKeyGenerator();
        PolytomousKeyGeneratorConfigurator configurator = new PolytomousKeyGeneratorConfigurator();
        configurator.setMerge(true);

//        generator.setFeatures(features);
//        generator.setTaxa(taxa);
////        generator.setDependencies(tree);// TODO create a tree with dependencies to test this function
        generator.invoke(configurator);
        assertNotNull("Key should exist (dependencies are present).",generator.invoke(configurator));
    }

    /**
     * @param configurator
     * @return
     */
    private DescriptiveDataSet createDataSet() {
        DescriptiveDataSet dataset = DescriptiveDataSet.NewInstance();
        dataset.setDescriptiveSystem(createFeatureTree());
//		generator.setFeatures(features);
        for (TaxonDescription desc : taxa){
            dataset.addDescription(desc);
        }
//        generator.setTaxa(taxa);
        return dataset;
    }

	/**
     * @return
     */
    private TermTree<Feature> createFeatureTree() {
        TermTree<Feature> result = TermTree.NewInstance(TermType.Feature, Feature.class);
        for (Feature feature: features) {
            result.getRoot().addChild(feature);
        }
        return result;
    }


}
