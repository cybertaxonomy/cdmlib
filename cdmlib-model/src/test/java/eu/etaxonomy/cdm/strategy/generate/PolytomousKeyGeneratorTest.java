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

	private static final boolean QUANTITATIVE = true;
	private static final boolean CATEGORICAL = false;


	private Feature featureShape;
	private Feature featurePresence;
	private Feature featureLength;
	private Feature featureColour;

	private State triangular;
	private State circular;
	private State oval;
    private State yellow;
	private State blue;
	private State yes;
	private State no;

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


	private CategoricalData catd12;
	private QuantitativeData qtd31;
    private QuantitativeData qtd32;
    private QuantitativeData qtd33;
    private QuantitativeData qtd34;
    private QuantitativeData qtd35;
    private QuantitativeData qtd36;
    private QuantitativeData qtd37;

	private Set<TaxonDescription> taxa;
	private List<Feature> features;

	private static UUID uuidFeatureShape = UUID.fromString("a61abb0c-51fb-4af4-aee4-f5894845133f");
	private static UUID uuidFeaturePresence = UUID.fromString("03cb2744-52a0-4127-be5d-265fe59b426f");
	private static UUID uuidFeatureLength = UUID.fromString("5de4f981-83fb-41d2-9900-b52cf5782a85");
	private static UUID uuidFeatureColour = UUID.fromString("7a8deb1a-144f-4be5-ba0d-9e77724697cb");

	private static UUID uuidTd1 = UUID.fromString("b392720c-8c64-4cbf-8207-992146f51fd5");
    private static UUID uuidTd2 = UUID.fromString("341d8ef1-fd07-4a91-8d53-dd6e729ad20b");
    private static UUID uuidTd3 = UUID.fromString("f174180f-86fe-475f-88f4-d0231fa96725");
    private static UUID uuidTd4 = UUID.fromString("3c90104f-ff81-43eb-a0f1-17eec1e77f49");
    private static UUID uuidTd5 = UUID.fromString("74b12419-4d2f-424d-9ca7-bba4c338df2e");
    private static UUID uuidTd6 = UUID.fromString("8df21f07-3bc0-4a88-a270-6c6050509975");
    private static UUID uuidTd7 = UUID.fromString("fc064338-adef-4657-bc69-34b0a9cc51a6");
    private static UUID uuidTd8 = UUID.fromString("b0458406-8e76-4f1a-9034-79cc661caf2a");


	private PolytomousKeyGenerator generator;

	@Before
	public void setUp() throws Exception {
	    if(Language.DEFAULT() == null){
            new DefaultTermInitializer().initialize();
        }

	    featureShape = createFeature("Shape of the head", uuidFeatureShape, CATEGORICAL);
	    featurePresence = createFeature("Presence of wings", uuidFeaturePresence, CATEGORICAL);
	    featureLength = createFeature("Length of wings", uuidFeatureLength, QUANTITATIVE);
	    featureColour = createFeature("Colour", uuidFeatureColour, CATEGORICAL);


		taxon1 = getTaxon(1);
		taxon2 = getTaxon(2);
		taxon3 = getTaxon(3);
		taxon4 = getTaxon(4);
		taxon5 = getTaxon(5);
		taxon6 = getTaxon(6);
		taxon7 = getTaxon(7);
		taxon8 = getTaxon(8);


		taxond1 = createTaxonDescription(taxon1, "td1", uuidTd1);
		taxond2 = createTaxonDescription(taxon2, "td2", uuidTd2);
		taxond3 = createTaxonDescription(taxon3, "td3", uuidTd3);
		taxond4 = createTaxonDescription(taxon4, "td4", uuidTd4);
		taxond5 = createTaxonDescription(taxon5, "td5", uuidTd5);
		taxond6 = createTaxonDescription(taxon6, "td6", uuidTd6);
		taxond7 = createTaxonDescription(taxon7, "td7", uuidTd7);
		taxond8 = createTaxonDescription(taxon8, "td8", uuidTd8);

		triangular = createState("Triangular");
		circular = createState("Circular");
		oval = createState("Oval");

		yellow = createState("Yellow");
		blue = createState("Blue");

		yes = createState("Yes");
		no = createState("No");

		CategoricalData catd11 = CategoricalData.NewInstance(triangular, featureShape);
		catd11.addStateData(oval);
		catd12 = CategoricalData.NewInstance(triangular, featureShape);
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

		qtd31 = QuantitativeData.NewMinMaxInstance(featureLength, 0, 3);
		qtd32 = QuantitativeData.NewMinMaxInstance(featureLength, 0, 3);
		qtd33 = QuantitativeData.NewMinMaxInstance(featureLength, 6, 9);
		qtd34 = QuantitativeData.NewMinMaxInstance(featureLength, 6, 9);
		qtd35 = QuantitativeData.NewMinMaxInstance(featureLength, 0, 3);
		qtd36 = QuantitativeData.NewMinMaxInstance(featureLength, 0, 3);
		qtd37 = QuantitativeData.NewMinMaxInstance(featureLength, 6, 9);
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
     * @param taxon12
     * @param string
     * @param uuidTd1
     * @return
     */
    private TaxonDescription createTaxonDescription(Taxon taxon, String title, UUID uuid) {
        TaxonDescription result = TaxonDescription.NewInstance(taxon);
        result.setTitleCache(title, true);
        result.setUuid(uuid);
        return result;
    }

    /**
     * @param string
     * @return
     */
    private State createState(String label) {
        State state = State.NewInstance("", label, "");
        state.getTitleCache();  //for better debugging
        return state;
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
	public void testInvokeMergeModeOff() {
		generator = new PolytomousKeyGenerator();
		PolytomousKeyGeneratorConfigurator configurator = new PolytomousKeyGeneratorConfigurator();
		configurator.setDataSet(createDataSet());
		configurator.setMerge(false);
		PolytomousKey result = generator.invoke(configurator);
		result.setTitleCache("No Merge Key", true);
	    result.print(System.out);

	    //Assertions
		assertNotNull("Key should exist.", result);
        PolytomousKeyNode root = result.getRoot();
        Assert.assertEquals(featureShape, root.getFeature());

        //circular
        PolytomousKeyNode circularNode = root.getChildAt(0);
        Assert.assertEquals(circular.getLabel(), label(circularNode));
        Assert.assertEquals(featurePresence, circularNode.getFeature());
            //yes
            PolytomousKeyNode yesNode = circularNode.getChildAt(0);
            Assert.assertEquals(yes.getLabel(), label(yesNode));
            Assert.assertEquals(featureLength, yesNode.getFeature());
            //no
            PolytomousKeyNode noNode = circularNode.getChildAt(1);
            Assert.assertEquals(no.getLabel(), label(noNode));
            Assert.assertTrue(noNode.getChildren().isEmpty());
            Assert.assertEquals(taxon8, noNode.getTaxon());


        //triangular
        PolytomousKeyNode triangularNode = root.getChildAt(1);
        Assert.assertEquals(triangular.getLabel(), label(triangularNode));


        //oval
        PolytomousKeyNode ovalNode = root.getChildAt(2);
        Assert.assertEquals(oval.getLabel(), label(ovalNode));
        Assert.assertEquals(taxon1, ovalNode.getTaxon());

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
        result.print(System.out);

        //Assertions
        assertNotNull("Key should exist.", result);
        PolytomousKeyNode root = result.getRoot();
        Assert.assertEquals(featureShape, root.getFeature());

        //triangular or oval
        PolytomousKeyNode triangularNode = root.getChildAt(0);
        Assert.assertEquals("Oval or Triangular", label(triangularNode));

        //circular
        PolytomousKeyNode circularNode = root.getChildAt(1);
        Assert.assertEquals(circular.getLabel(), label(circularNode));
        Assert.assertEquals(featurePresence, circularNode.getFeature());

            //yes
            PolytomousKeyNode yesNode = circularNode.getChildAt(0);
            Assert.assertEquals(yes.getLabel(), label(yesNode));
//            Assert.assertEquals(featureLength, circularNode.getFeature());

            //no
            PolytomousKeyNode noNode = circularNode.getChildAt(1);
            Assert.assertEquals(no.getLabel(), label(noNode));
            Assert.assertTrue(noNode.getChildren().isEmpty());
            Assert.assertEquals(taxon8, noNode.getTaxon());

	}

   @Test
    public void testInvokeMergeReuseFeature() {
        generator = new PolytomousKeyGenerator();
        PolytomousKeyGeneratorConfigurator configurator = new PolytomousKeyGeneratorConfigurator();
        configurator.setDataSet(createDataSet());
        taxond1.removeElement(qtd31);
        taxond2.removeElement(qtd32);
        taxond3.removeElement(qtd33);
        taxond4.removeElement(qtd34);
        catd12.addStateData(oval);
        configurator.setMerge(true);
        PolytomousKey result = generator.invoke(configurator);
        result.setTitleCache("Merge Key with feature reuse", true);
        assertNotNull("Key should exist (merge mode with feature reuse).", result);
        result.print(System.out);

        //Assertions
        assertNotNull("Key should exist.", result);
        PolytomousKeyNode root = result.getRoot();
        Assert.assertEquals(featureShape, root.getFeature());

        //triangular or oval
        PolytomousKeyNode ovalOrTriangularNode = root.getChildAt(0);
        Assert.assertEquals("Oval or Triangular", label(ovalOrTriangularNode));

            //blue
            PolytomousKeyNode blueNode = ovalOrTriangularNode.getChildAt(0);
            Assert.assertEquals(blue.getLabel(), label(blueNode));

                //triangular or oval
                PolytomousKeyNode triangularNode = blueNode.getChildAt(0);
                Assert.assertEquals("Shape of head should be reused in this branch", "Triangular", label(triangularNode));
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

    /**
     * @param blueNode
     * @return
     */
    private Object label(PolytomousKeyNode node) {
        return node.getStatement().getLabelText(Language.DEFAULT());
    }


}
