package eu.etaxonomy.cdm.strategy.generate;

import static org.junit.Assert.assertNotNull;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
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
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.DescriptiveDataSet;
import eu.etaxonomy.cdm.model.description.Distribution;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.PolytomousKey;
import eu.etaxonomy.cdm.model.description.PolytomousKeyNode;
import eu.etaxonomy.cdm.model.description.PresenceAbsenceTerm;
import eu.etaxonomy.cdm.model.description.QuantitativeData;
import eu.etaxonomy.cdm.model.description.State;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.location.Country;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.name.TaxonNameFactory;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.model.term.DefaultTermInitializer;
import eu.etaxonomy.cdm.model.term.TermNode;
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

    private static final boolean debug = true;

    private static final String GT_3 = " > 3.0";
//    private static final String GT_3_5 = " > 3.5";
    private static final String LESS_3 = " < 3.0";
//    private static final String LESS_3_5 = " < 3.5";

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

	private Classification classification;

	private Taxon taxonGenus;
	private Taxon taxon1;
	private Taxon taxon2;
	private Taxon taxon3;
	private Taxon taxon4;
	private Taxon taxon5;
	private Taxon taxon6;
	private Taxon taxon7;
	private Taxon taxon8;

	private TaxonDescription tdGenus;
    private TaxonDescription td1;
	private TaxonDescription td2;
	private TaxonDescription td3;
	private TaxonDescription td4;
	private TaxonDescription td5;
	private TaxonDescription td6;
	private TaxonDescription td7;
	private TaxonDescription td8;

    private CategoricalData catd11;
    private CategoricalData catd12;
    private CategoricalData catd13;
    private CategoricalData catd14;
    private CategoricalData catd15;
    private CategoricalData catd27;
    private CategoricalData catd28;
	private QuantitativeData qtd31;
    private QuantitativeData qtd32;
    private QuantitativeData qtd33;
    private QuantitativeData qtd34;
    private QuantitativeData qtd35;
    private QuantitativeData qtd36;
    private QuantitativeData qtd37;
    private QuantitativeData qtd38;

	private Set<TaxonDescription> taxa;

	private static UUID uuidFeatureShape = UUID.fromString("a61abb0c-51fb-4af4-aee4-f5894845133f");
	private static UUID uuidFeaturePresence = UUID.fromString("03cb2744-52a0-4127-be5d-265fe59b426f");
	private static UUID uuidFeatureLength = UUID.fromString("5de4f981-83fb-41d2-9900-b52cf5782a85");
	private static UUID uuidFeatureColour = UUID.fromString("7a8deb1a-144f-4be5-ba0d-9e77724697cb");

	private static UUID uuidTdGenus = UUID.fromString("cbdfa57e-1773-4503-9e56-469c9894f6fc");
    private static UUID uuidTd1 = UUID.fromString("b392720c-8c64-4cbf-8207-992146f51fd5");
    private static UUID uuidTd2 = UUID.fromString("341d8ef1-fd07-4a91-8d53-dd6e729ad20b");
    private static UUID uuidTd3 = UUID.fromString("f174180f-86fe-475f-88f4-d0231fa96725");
    private static UUID uuidTd4 = UUID.fromString("3c90104f-ff81-43eb-a0f1-17eec1e77f49");
    private static UUID uuidTd5 = UUID.fromString("74b12419-4d2f-424d-9ca7-bba4c338df2e");
    private static UUID uuidTd6 = UUID.fromString("8df21f07-3bc0-4a88-a270-6c6050509975");
    private static UUID uuidTd7 = UUID.fromString("fc064338-adef-4657-bc69-34b0a9cc51a6");
    private static UUID uuidTd8 = UUID.fromString("b0458406-8e76-4f1a-9034-79cc661caf2a");

	private PolytomousKeyGenerator generator = new PolytomousKeyGenerator();

	@Before
	public void setUp() throws Exception {
	    if(Language.DEFAULT() == null){
            new DefaultTermInitializer().initialize();
        }

	    featureShape = createFeature("Shape of the head", uuidFeatureShape, CATEGORICAL);
	    featurePresence = createFeature("Presence of wings", uuidFeaturePresence, CATEGORICAL);
	    featureLength = createFeature("Length of wings", uuidFeatureLength, QUANTITATIVE);
	    featureColour = createFeature("Colour", uuidFeatureColour, CATEGORICAL);

	    taxonGenus = getTaxon(0);
        taxon1 = getTaxon(1);
		taxon2 = getTaxon(2);
		taxon3 = getTaxon(3);
		taxon4 = getTaxon(4);
		taxon5 = getTaxon(5);
		taxon6 = getTaxon(6);
		taxon7 = getTaxon(7);
		taxon8 = getTaxon(8);

		tdGenus = createTaxonDescription(taxonGenus, "tdGenus", uuidTdGenus);
        td1 = createTaxonDescription(taxon1, "td1", uuidTd1);
		td2 = createTaxonDescription(taxon2, "td2", uuidTd2);
		td3 = createTaxonDescription(taxon3, "td3", uuidTd3);
		td4 = createTaxonDescription(taxon4, "td4", uuidTd4);
		td5 = createTaxonDescription(taxon5, "td5", uuidTd5);
		td6 = createTaxonDescription(taxon6, "td6", uuidTd6);
		td7 = createTaxonDescription(taxon7, "td7", uuidTd7);
		td8 = createTaxonDescription(taxon8, "td8", uuidTd8);

		triangular = createState("Triangular");
		circular = createState("Circular");
		oval = createState("Oval");

		yellow = createState("Yellow");
		blue = createState("Blue");

		yes = createState("Yes");
		no = createState("No");

		catd11 = CategoricalData.NewInstance(triangular, featureShape);
		catd11.addStateData(oval);
		catd12 = CategoricalData.NewInstance(triangular, featureShape);
		catd13 = CategoricalData.NewInstance(triangular, featureShape);
		catd14 = CategoricalData.NewInstance(triangular, featureShape);
		catd15 = CategoricalData.NewInstance(circular, featureShape);
		CategoricalData catd16 = CategoricalData.NewInstance(circular, featureShape);
		CategoricalData catd17 = CategoricalData.NewInstance(circular, featureShape);
		CategoricalData catd18 = CategoricalData.NewInstance(circular, featureShape);

		//*************************/

		CategoricalData catd21 = CategoricalData.NewInstance(yes, featurePresence);
		CategoricalData catd22 = CategoricalData.NewInstance(yes, featurePresence);
		CategoricalData catd23 = CategoricalData.NewInstance(yes, featurePresence);
		CategoricalData catd24 = CategoricalData.NewInstance(yes, featurePresence);
		CategoricalData catd25 = CategoricalData.NewInstance(yes, featurePresence);
		CategoricalData catd26 = CategoricalData.NewInstance(yes, featurePresence);
		catd27 = CategoricalData.NewInstance(yes, featurePresence);
		catd28 = CategoricalData.NewInstance(no, featurePresence);

		//*************************/

		qtd31 = QuantitativeData.NewExactValueInstance(featureLength, new BigDecimal("0.0"), new BigDecimal("3.0"));
//        qtd31 = QuantitativeData.NewMinMaxInstance(featureLength, 0, 3);
		qtd32 = QuantitativeData.NewMinMaxInstance(featureLength, new BigDecimal("0.0"), new BigDecimal("3.0"));
		qtd33 = QuantitativeData.NewMinMaxInstance(featureLength, new BigDecimal("6.0"), new BigDecimal("9.0"));
		qtd34 = QuantitativeData.NewMinMaxInstance(featureLength, new BigDecimal("6.0"), new BigDecimal("9.0"));
		qtd35 = QuantitativeData.NewMinMaxInstance(featureLength, new BigDecimal("0.0"), new BigDecimal("3.0"));
		qtd36 = QuantitativeData.NewMinMaxInstance(featureLength, new BigDecimal("0.0"), new BigDecimal("3.0"));
		qtd37 = QuantitativeData.NewMinMaxInstance(featureLength, new BigDecimal("6.0"), new BigDecimal("9.0"));
		qtd38 = QuantitativeData.NewMinMaxInstance(featureLength, new BigDecimal("0.0"), new BigDecimal("3.0"));

		//*************************/

		CategoricalData catd41 = CategoricalData.NewInstance(blue, featureColour);
		CategoricalData catd42 = CategoricalData.NewInstance(yellow, featureColour);
		CategoricalData catd43 = CategoricalData.NewInstance(blue, featureColour);
		CategoricalData catd44 = CategoricalData.NewInstance(yellow, featureColour);
		CategoricalData catd45 = CategoricalData.NewInstance(blue, featureColour);
		CategoricalData catd46 = CategoricalData.NewInstance(blue, featureColour);
		CategoricalData catd47 = CategoricalData.NewInstance(blue, featureColour);
		CategoricalData catd48 = CategoricalData.NewInstance(blue, featureColour);

		//*************************/

		catd11.clone(tdGenus); //Shape triangular

		td1.addElement(catd11); //Shape triangular
		td1.addElement(catd21); //present
		td1.addElement(qtd31);  //length 0-3
		td1.addElement(catd41); //color blue

		td2.addElement(catd12);  //Shape triangular
		td2.addElement(catd22);  //present
		td2.addElement(qtd32);   //length 0-3
		td2.addElement(catd42);  //color yellow

		td3.addElement(catd13);  //Shape triangular
		td3.addElement(catd23);  //present
		td3.addElement(qtd33);   //length 6-9
		td3.addElement(catd43);  //color blue

		td4.addElement(catd14);  //Shape triangular
		td4.addElement(catd24);  //present
		td4.addElement(qtd34);   //length 6-9
		td4.addElement(catd44);  //color yellow

		td5.addElement(catd15);  //Shape circular
		td5.addElement(catd25);  //present
		td5.addElement(qtd35);   //length 0-3
		td5.addElement(catd45);  //color blue

		td6.addElement(catd16);  //Shape circular
		td6.addElement(catd26);  //present
		td6.addElement(qtd36);   //length 0-3
		td6.addElement(catd46);  //color blue

		td7.addElement(catd17);  //Shape circular
		td7.addElement(catd27);  //present
		td7.addElement(qtd37);   //length 6-9
		td7.addElement(catd47);  //color blue

		td8.addElement(catd18);  //Shape circular
		td8.addElement(catd28);  //absent
//		taxond8.addElement(qtd38); // This taxon has no wings
		td8.addElement(catd48);  //color blue

		/******* add non-character data, this should have no influence **/
		TaxonDescription nonCharacterDesc = TaxonDescription.NewInstance(taxon1);
		Distribution distribution = Distribution.NewInstance(Country.GERMANY(), PresenceAbsenceTerm.PRESENT());
		nonCharacterDesc.addElement(distribution);

		td2.addElement(TextData.NewInstance(Feature.ANATOMY(), "Test", Language.DEFAULT(), null));

		//*************************************************/

		taxa = new HashSet<>();
		taxa.add(tdGenus);
        taxa.add(td1);
		taxa.add(td2);
		taxa.add(td3);
		taxa.add(td4);
		taxa.add(td5);
		taxa.add(td6);
		taxa.add(td7);
		taxa.add(td8);

		classification = Classification.NewInstance("Test Classification");
		Taxon rootTaxon = taxonGenus;
		TaxonNode genusTaxonNode = classification.addChildTaxon(rootTaxon, null, null);
		genusTaxonNode.addChildTaxon(taxon1, null, null);
		genusTaxonNode.addChildTaxon(taxon2, null, null);
		genusTaxonNode.addChildTaxon(taxon3, null, null);
		genusTaxonNode.addChildTaxon(taxon4, null, null);
		genusTaxonNode.addChildTaxon(taxon5, null, null);
		genusTaxonNode.addChildTaxon(taxon6, null, null);
		genusTaxonNode.addChildTaxon(taxon7, null, null);
		genusTaxonNode.addChildTaxon(taxon8, null, null);
	}

//*************************** TESTS *********************** /

	@Test
	public void testInvokeMergeModeOff() {
		PolytomousKeyGeneratorConfigurator configurator = createDefaultConfig();
		configurator.setMerge(false);
		PolytomousKey result = generator.invoke(configurator);
		result.setTitleCache("No Merge Key", true);
        if (debug) {result.print(System.out);}

	    //Assertions
		assertNotNull("Key should exist.", result);
        PolytomousKeyNode root = result.getRoot();
        Assert.assertEquals(featureShape, root.getFeature());
        Assert.assertNull(root.getTaxon());

        //circular
        PolytomousKeyNode circularNode = root.getChildAt(0);
        assertInnerNode(circularNode, circular, featurePresence);

            //yes
            PolytomousKeyNode yesNode = circularNode.getChildAt(0);
            assertInnerNode(yesNode, yes, featureLength);

                //<3
                PolytomousKeyNode less3Node = yesNode.getChildAt(0);
                assertIsTaxonList(less3Node, LESS_3, taxon5, taxon6);

                //>3
                assertSingleTaxon(yesNode.getChildAt(1), taxon7, GT_3);

            //no
            assertSingleTaxon(circularNode.getChildAt(1), taxon8, no);

        //triangular
        PolytomousKeyNode triangularNode = root.getChildAt(1);
        assertInnerNode(triangularNode, triangular, featureLength);

            //<3
            less3Node = triangularNode.getChildAt(0);


                //blue
                assertSingleTaxon(less3Node.getChildAt(0), taxon1, blue);
                //yellow
                assertSingleTaxon(less3Node.getChildAt(1), taxon2, yellow);

            //>3
            PolytomousKeyNode gt3Node = triangularNode.getChildAt(1);
            assertInnerNode(gt3Node, GT_3, featureColour);

                //blue
                assertSingleTaxon(gt3Node.getChildAt(0), taxon3, blue);
                //yellow
                assertSingleTaxon(gt3Node.getChildAt(1), taxon4, yellow);

        //oval
        assertSingleTaxon(root.getChildAt(2), taxon1, oval);
	}

    @Test
    public void testInvokeMergeModeON() {
        PolytomousKeyGeneratorConfigurator configurator = createDefaultConfig();
        configurator.setMerge(true);
        PolytomousKey result = generator.invoke(configurator);
        result.setTitleCache("Merge Key", true);
        assertNotNull("Key should exist (merge mode ON).", result);
        if (debug) {result.print(System.out);}

        //Assertions
        assertNotNull("Key should exist.", result);
        PolytomousKeyNode root = result.getRoot();
        Assert.assertEquals(featureShape, root.getFeature());
        Assert.assertNull(root.getTaxon());

        //triangular or oval
        PolytomousKeyNode triangularNode = root.getChildAt(0);
        assertInnerNode(triangularNode, "Oval or Triangular", featureLength);

            //<3
            PolytomousKeyNode lessNode = triangularNode.getChildAt(0);
            assertInnerNode(lessNode, LESS_3 , featureColour);
                //blue
                assertSingleTaxon(lessNode.getChildAt(0), taxon1, blue);
                //yellow
                assertSingleTaxon(lessNode.getChildAt(1), taxon2, yellow);

            //>3
            PolytomousKeyNode gtNode = triangularNode.getChildAt(1);
            assertInnerNode(gtNode, GT_3, featureColour);
                //blue
                assertSingleTaxon(gtNode.getChildAt(0), taxon3, blue);
                //yellow
                assertSingleTaxon(gtNode.getChildAt(1), taxon4, yellow);

        //circular
        PolytomousKeyNode circularNode = root.getChildAt(1);
        assertInnerNode(circularNode, circular, featurePresence);

            //yes
            PolytomousKeyNode yesNode = circularNode.getChildAt(0);
            assertInnerNode(yesNode, yes, featureLength);

                //<3
                assertIsTaxonList(yesNode.getChildAt(0), LESS_3 , taxon5, taxon6);

                //>3
                assertSingleTaxon(yesNode.getChildAt(1), taxon7, GT_3);

            //no
            assertSingleTaxon(circularNode.getChildAt(1), taxon8, no);
    }

    @Test
    public void testInvokeMergeReuseFeature() {
        PolytomousKeyGeneratorConfigurator configurator = createDefaultConfig();
        td1.removeElement(qtd31);
        td2.removeElement(qtd32);
        td3.removeElement(qtd33);
        td4.removeElement(qtd34);
        catd12.addStateData(oval);
        catd12.addStateData(circular);
        configurator.setMerge(true);
        PolytomousKey result = generator.invoke(configurator);
        result.setTitleCache("Merge Key with feature reuse", true);
        assertNotNull("Key should exist (merge mode with feature reuse).", result);
        if (debug) {result.print(System.out);}

        //Assertions
        assertNotNull("Key should exist.", result);
        PolytomousKeyNode root = result.getRoot();
        Assert.assertEquals(featureShape, root.getFeature());

        //triangular or oval
        PolytomousKeyNode ovalOrTriangularNode = root.getChildren().stream()
                .filter(pkn->pkn.getStatement().getLabelText(Language.DEFAULT()).equals("Oval or Triangular"))
                .findFirst().get();
        Assert.assertEquals("Oval or Triangular", label(ovalOrTriangularNode));

            //blue
            PolytomousKeyNode blueNode = ovalOrTriangularNode.getChildAt(0);
            Assert.assertEquals(blue.getLabel(), label(blueNode));

                //triangular
                PolytomousKeyNode triangularNode = blueNode.getChildAt(0);
                Assert.assertEquals("Shape of head should be reused in this branch", "Triangular", label(triangularNode));

            //yellow
            PolytomousKeyNode yellowNode = ovalOrTriangularNode.getChildAt(1);
            Assert.assertEquals(yellow.getLabel(), label(yellowNode));

                //triangular
                PolytomousKeyNode ovalNode = yellowNode.getChildAt(1);
                Assert.assertEquals("Shape of head should be reused in this branch, "
                        + "but only for remaining states triangular and oval. "
                        + "'Circular' must not be available anymore", "Oval", label(ovalNode));

        PolytomousKeyNode circularNode = root.getChildren().stream()
                .filter(pkn->pkn.getStatement().getLabelText(Language.DEFAULT()).equals("Circular"))
                .findFirst().get();

            //presence yes
            PolytomousKeyNode presenceNode = circularNode.getChildAt(0);
            Assert.assertEquals(yes.getLabel(), label(presenceNode));

                //blue
                blueNode = presenceNode.getChildAt(0);
                Assert.assertEquals(blue.getLabel(), label(blueNode));

                    //length
                    PolytomousKeyNode lowerNode = blueNode.getChildAt(0);
                    assertIsTaxonList(lowerNode, LESS_3, taxon5, taxon6);  //test no feature left
    }


    /**
     * With dependencies is difficult to check because it only changes the performance
     * if data is clean. So in this test we first check some dirty data with
     * dependency check and then without.
     * In the first run it correctly removes the length of wings check at the end
     * as length of wings is not applicable if presence of wings = no.
     * In the second run it does the length of wings check as it does not
     * use dependency check.
     */
    @Test
    public void testInvokeWithoutDependencies() {
        generator = new PolytomousKeyGenerator();
        PolytomousKeyGeneratorConfigurator configurator = createDefaultConfig();
        configurator.setMerge(true);
        configurator.setUseDependencies(true);
        catd27.getStateData().get(0).setState(no);
        td8.addElement(qtd38);

        PolytomousKey result = generator.invoke(configurator);
        result.setTitleCache("Merge Key with dependency", true);
        assertNotNull("Key should exist (dependency on)", result);
        if (debug) {result.print(System.out);}

        //Assertions
        assertNotNull("Key should exist.", result);
        PolytomousKeyNode root = result.getRoot();
        Assert.assertEquals(featureShape, root.getFeature());

        //circular
        PolytomousKeyNode circularNode = root.getChildAt(1);
        assertInnerNode(circularNode, circular, featurePresence);

            //no
            assertIsTaxonList(circularNode.getChildAt(0), no, taxon8, taxon7);

            //yes
            assertIsTaxonList(circularNode.getChildAt(1), yes, taxon5, taxon6);

        //and now without dependency check
        configurator.setUseDependencies(false);

        result = generator.invoke(configurator);
        result.setTitleCache("Merge Key without dependency", true);
        assertNotNull("Key should exist (dependency off)", result);
        if (debug) {result.print(System.out);}

        //Assertions
        assertNotNull("Key should exist.", result);
        root = result.getRoot();
        Assert.assertEquals(featureShape, root.getFeature());

        //circular
        circularNode = root.getChildAt(1);
        assertInnerNode(circularNode, circular, featurePresence);

            //no
            PolytomousKeyNode noNode = assertInnerNode(circularNode.getChildAt(0), no, featureLength);

                //as dependency check is switched off we distinguish length, though length should be inapplicable here
                assertSingleTaxon(noNode.getChildAt(0), taxon8, LESS_3);
                assertSingleTaxon(noNode.getChildAt(1), taxon7, GT_3);

            //yes
            assertIsTaxonList(circularNode.getChildAt(1), yes, taxon5, taxon6);
    }

    @Test
    public void testTaxonomicHierarchy() {

        tdGenus.getElements().clear();
        tdGenus.addElements(mergeTaxDescriptions(td1, td2, td3, td4));
        TaxonNode genus1Node = classification.getRootNode().getChildNodes().iterator().next();
        removeTaxon5_8(genus1Node);

        UUID uuidTdGenus2 = UUID.fromString("3eed217a-fd40-4a38-997f-1f4360133d0d");
        Taxon taxonGenus2 = getTaxon(10);
        TaxonDescription tdGenus2 = createTaxonDescription(taxonGenus2, "tdGenus2", uuidTdGenus2);
        taxa.add(tdGenus2);
        TaxonNode genus2Node = classification.getRootNode().addChildTaxon(taxonGenus2, null, null);
        genus2Node.addChildTaxon(taxon5, null, null);
        genus2Node.addChildTaxon(taxon6, null, null);
        genus2Node.addChildTaxon(taxon7, null, null);
        genus2Node.addChildTaxon(taxon8, null, null);
        tdGenus2.addElements(mergeTaxDescriptions(td5, td6, td7, td8));

        PolytomousKeyGeneratorConfigurator configurator = createDefaultConfig();
        PolytomousKey result = generator.invoke(configurator);
        result.setTitleCache("Merge Key", true);
        assertNotNull("Key should exist (merge mode ON).", result);
        if (debug) {result.print(System.out);}

        //Assertions
        assertNotNull("Key should exist.", result);
        PolytomousKeyNode root = result.getRoot();
        Assert.assertEquals(featureShape, root.getFeature());
        Assert.assertNull(root.getTaxon());

        //triangular or oval
        PolytomousKeyNode triangularNode = root.getChildAt(0);
        assertInnerNodeWithTaxon(triangularNode, "Oval or Triangular", featureLength, taxonGenus);

            //<3
            PolytomousKeyNode lessNode = triangularNode.getChildAt(0);
            assertInnerNode(lessNode, LESS_3 , featureColour);
                //blue
                assertSingleTaxon(lessNode.getChildAt(0), taxon1, blue);
                //yellow
                assertSingleTaxon(lessNode.getChildAt(1), taxon2, yellow);

            //>3
            PolytomousKeyNode gtNode = triangularNode.getChildAt(1);
            assertInnerNode(gtNode, GT_3, featureColour);
                //blue
                assertSingleTaxon(gtNode.getChildAt(0), taxon3, blue);
                //yellow
                assertSingleTaxon(gtNode.getChildAt(1), taxon4, yellow);

        //circular
        PolytomousKeyNode circularNode = root.getChildAt(1);
        assertInnerNodeWithTaxon(circularNode, circular, featurePresence, taxonGenus2);

            //yes
            PolytomousKeyNode yesNode = circularNode.getChildAt(0);
            assertInnerNode(yesNode, yes, featureLength);

                //<3
                assertIsTaxonList(yesNode.getChildAt(0), LESS_3 , taxon5, taxon6);

                //>3
                assertSingleTaxon(yesNode.getChildAt(1), taxon7, GT_3);

            //no
            assertSingleTaxon(circularNode.getChildAt(1), taxon8, no);
    }

    private DescriptionElementBase[] mergeTaxDescriptions(TaxonDescription td5, TaxonDescription td6, TaxonDescription td7,
            TaxonDescription td8) {
        List<DescriptionElementBase> list = new ArrayList<>();
        list.addAll(clonedDescElements(td5.getElements()));
        list.addAll(clonedDescElements(td6.getElements()));
        list.addAll(clonedDescElements(td7.getElements()));
        list.addAll(clonedDescElements(td8.getElements()));

        return list.toArray(new DescriptionElementBase[0]);
    }

    private Set<DescriptionElementBase> clonedDescElements(Set<DescriptionElementBase> elements) {
        Set<DescriptionElementBase> result = new HashSet<>();
        for (DescriptionElementBase deb : elements){
            try {
                result.add((DescriptionElementBase)deb.clone());
            } catch (CloneNotSupportedException e) {
                throw new RuntimeException(e);
            }
        }
        return result;
    }

    private void removeTaxon5_8(TaxonNode genus1Node) {
        genus1Node.deleteChildNode(genus1Node.getChildNodes().get(7));
        genus1Node.deleteChildNode(genus1Node.getChildNodes().get(6));
        genus1Node.deleteChildNode(genus1Node.getChildNodes().get(5));
        genus1Node.deleteChildNode(genus1Node.getChildNodes().get(4));
    }

    @Test
    public void testDependencyScore() {
        generator = new PolytomousKeyGenerator();
        PolytomousKeyGeneratorConfigurator configurator = createDefaultConfig(); //new PolytomousKeyGeneratorConfigurator();
        configurator.getDataSet().getDescriptiveSystem().getRoot().removeChild(0); //remove shape feature
        configurator.setMerge(true);
        configurator.setUseDependencies(true);

        PolytomousKey result = generator.invoke(configurator);
        result.setTitleCache("Test Dependency Score Key", true);
        if (debug) {result.print(System.out);}

        //Assertions
        assertNotNull("Key should exist.", result);
        PolytomousKeyNode root = result.getRoot();
        Assert.assertEquals("Root feature should be 'presence' as it inherits score from 'length of wings'", featurePresence, root.getFeature());
        //...otherwise it would be color, both have a score of 12.0 but presence comes first in list
    }

    @Test
    @Ignore
    public void testMultipleDependencies() {

        //TODO test only if multiple dependencies are supported for 1 parent feature; not yet implemented; requires restructuring the feature tree, created by createDefaultConfig()
//        generator = new PolytomousKeyGenerator();
//        PolytomousKeyGeneratorConfigurator configurator = createDefaultConfig(); //new PolytomousKeyGeneratorConfigurator();
//        configurator.getDataSet().getDescriptiveSystem().getRoot().removeChild(0); //remove shape feature
//        configurator.setMerge(true);
//        configurator.setUseDependencies(true);
//
//        PolytomousKey result = generator.invoke(configurator);
//        result.setTitleCache("Test Dependency Score Key", true);
//        if (debug) {result.print(System.out);}
//
//        //Assertions
//        assertNotNull("Key should exist.", result);
//        PolytomousKeyNode root = result.getRoot();
//        Assert.assertEquals("Root feature should be 'presence' as it inherits score from 'length of wings'", featurePresence, root.getFeature());
//        //...otherwise it would be colour, both have a score of 12.0 but presence comes first in list
    }

    /**
     * Asserts that the node is an inner node (has no taxon) and uses the given feature
     * and has the given statement label.
     */
    private void assertInnerNode(PolytomousKeyNode node, String label, Feature feature) {
        Assert.assertEquals(label, label(node));
        Assert.assertEquals(feature, node.getFeature());
        Assert.assertNull(node.getTaxon());
    }

    private void assertInnerNodeWithTaxon(PolytomousKeyNode node, String label, Feature feature, Taxon taxon) {
        Assert.assertEquals(label, label(node));
        Assert.assertEquals(feature, node.getFeature());
        Assert.assertEquals(taxon, node.getTaxon());
    }

    private PolytomousKeyNode assertInnerNode(PolytomousKeyNode node, State state, Feature feature) {
        assertInnerNode(node, state.getLabel(), feature);
        return node;
    }
    private PolytomousKeyNode assertInnerNodeWithTaxon(PolytomousKeyNode node, State state, Feature feature, Taxon taxon) {
        assertInnerNodeWithTaxon(node, state.getLabel(), feature, taxon);
        return node;
    }

    private void assertSingleTaxon(PolytomousKeyNode node, Taxon taxon, String statement) {
        Assert.assertNotNull(node.getStatement());
        Assert.assertEquals(statement, label(node));
        Assert.assertTrue(node.getChildren().isEmpty());
        Assert.assertEquals(taxon, node.getTaxon());
    }

    private void assertSingleTaxon(PolytomousKeyNode node, Taxon taxon, State state) {
        assertSingleTaxon(node, taxon, state.getLabel());
    }

    private void assertIsTaxonList(PolytomousKeyNode node, State state, Taxon... taxa) {
        assertIsTaxonList(node, state.getLabel(), taxa);
    }

    private void assertIsTaxonList(PolytomousKeyNode node, String label, Taxon... taxa) {
        Assert.assertNotNull(node.getStatement());
        Assert.assertEquals(label, label(node));
        Assert.assertNull(node.getFeature());
        Assert.assertNull(node.getTaxon());
        Assert.assertTrue(node.getChildren().size() > 1);
        for (PolytomousKeyNode child : node.getChildren()){
            Assert.assertTrue(Arrays.asList(taxa).contains(child.getTaxon()));
            Assert.assertNull(child.getStatement());
            Assert.assertTrue(child.getChildren().isEmpty());
        }
    }

    private PolytomousKeyGeneratorConfigurator createDefaultConfig() {
        PolytomousKeyGeneratorConfigurator configurator = new PolytomousKeyGeneratorConfigurator();
        configurator.setDataSet(createDataSet());
        configurator.setDebug(debug);
        return configurator;
    }

    private DescriptiveDataSet createDataSet() {
        DescriptiveDataSet dataset = DescriptiveDataSet.NewInstance();
        dataset.setDescriptiveSystem(createFeatureTree());
        for (TaxonDescription desc : taxa){
            dataset.addDescription(desc);
        }
        return dataset;
    }

    private TermTree<Feature> createFeatureTree() {
        TermTree<Feature> result = TermTree.NewInstance(TermType.Feature, Feature.class);
        result.getRoot().addChild(featureShape);
        TermNode<Feature> nodePresence = result.getRoot().addChild(featurePresence);
        TermNode<Feature> nodeLength = nodePresence.addChild(featureLength);
        nodeLength.addInapplicableState(featurePresence, no);
        nodePresence.addChild(featureColour);

        return result;
    }

    private Object label(PolytomousKeyNode node) {
        return node.getStatement()== null?"no statement":node.getStatement().getLabelText(Language.DEFAULT());
    }

    private TaxonDescription createTaxonDescription(Taxon taxon, String title, UUID uuid) {
        TaxonDescription result = TaxonDescription.NewInstance(taxon);
        result.setTitleCache(title, true);
        result.setUuid(uuid);
        return result;
    }

    private State createState(String label) {
        State state = State.NewInstance("", label, "");
        state.getTitleCache();  //for better debugging
        return state;
    }

    private Feature createFeature(String title, UUID uuid, boolean isQuantitative) {
        Feature result = Feature.NewInstance("",title,"");
        result.setUuid(uuid);
        if (isQuantitative){
            result.setSupportsQuantitativeData(true);
        }else{
            result.setSupportsCategoricalData(true);
        }
        result.getTitleCache();
        return result;
    }

    private Taxon getTaxon(int i) {
        TaxonName tn = TaxonNameFactory.NewNonViralInstance(Rank.SPECIES());
        tn.setGenusOrUninomial("Taxon");
        tn.setSpecificEpithet(String.valueOf(i));
        Taxon result = Taxon.NewInstance(tn, ReferenceFactory.newBook());
        result.getTitleCache();
        return result;
    }


}
