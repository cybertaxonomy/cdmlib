package eu.etaxonomy.cdm.model.description;

import java.io.PrintStream;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import eu.etaxonomy.cdm.model.common.DefaultTermInitializer;
import eu.etaxonomy.cdm.model.taxon.Taxon;

public class PolytomousKeyTest {
	private static Logger logger = Logger.getLogger(PolytomousKeyTest.class);

	private PolytomousKey key1;
	private Taxon taxon1;
	
	
	@BeforeClass
	public static void setUpBeforeClass() {
		DefaultTermInitializer vocabularyStore = new DefaultTermInitializer();
		vocabularyStore.initialize();
	}
	
	@Before
	public void setUp() throws Exception {
		key1 = PolytomousKey.NewInstance();
		key1.setTitleCache("My Test Key", true);
		PolytomousKeyNode root = key1.getRoot();
		root.setQuestion(KeyStatement.NewInstance("Is this Aus bus?"));
		//child1
		taxon1 = Taxon.NewInstance(null, null);
		taxon1.setTitleCache("Aus bus L.", true);
		PolytomousKeyNode child1 = PolytomousKeyNode.NewInstance("Yes", null, taxon1, null);
		Feature feature1 = Feature.NewInstance(null, "Leaf", null);
		child1.setFeature(feature1);
		root.addChild(child1);
		
		//child2
		Taxon taxon2 = Taxon.NewInstance(null, null);
		taxon2.setTitleCache("Cus dus Mill.", true);
		PolytomousKeyNode child2 = PolytomousKeyNode.NewInstance("No");
		child2.setTaxon(taxon2);
		root.addChild(child2);
		//child3
		Taxon taxon3 = Taxon.NewInstance(null, null);
		taxon3.setTitleCache("Cus dus subs. rus L.", true);
		PolytomousKeyNode child3 = PolytomousKeyNode.NewInstance("Long and wide");
		child3.setTaxon(taxon3);
		child1.addChild(child3);
		//child4
		Taxon taxon4 = Taxon.NewInstance(null, null);
		taxon4.setTitleCache("Cus dus subs. zus L.", true);
		PolytomousKeyNode child4 = PolytomousKeyNode.NewInstance("Small and narrow");
		child4.setTaxon(taxon4);
		child1.addChild(child4);
		
		key1.setRoot(root);

		PolytomousKey key2 = PolytomousKey.NewTitledInstance("Second Key");
		child3.setSubkey(key2);
		
		child4.setOtherNode(key2.getRoot());
		
	}
	
//********************* Tests *******************************************************/	
	
	@Test
	public void testNewInstance() {
		PolytomousKey newKey = PolytomousKey.NewInstance();
		Assert.assertNotNull(newKey);
	}

	@Test
	public void testNewTitledInstance() {
		logger.warn("testNewTitledInstance Not yet implemented");
	}

	@Test
	public void testPolytomousKey() {
		PolytomousKey newKey = new PolytomousKey();
		Assert.assertNotNull(newKey);
	}

	@Test
	public void testPrint() {
		PrintStream stream = null;
		String strKey = key1.print(stream);
		System.out.println(strKey);
		Assert.assertEquals("", "My Test Key\n  0. Is this Aus bus?\n    a) Yes ... 1, Aus bus L.\n    b) No ... Cus dus Mill.\n  1. Leaf\n    a) Long and wide ... Cus dus subs. rus L., Second Key\n    b) Small and narrow ... Cus dus subs. zus L., Second Key 0\n", strKey);
	}

//	@Test
//	public void testGetChildren() {
//		fail("Not yet implemented");
//	}

}
