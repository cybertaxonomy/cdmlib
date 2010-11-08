package eu.etaxonomy.cdm.model.description;

import static org.junit.Assert.*;

import java.io.PrintStream;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import eu.etaxonomy.cdm.model.common.DefaultTermInitializer;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.LanguageString;
import eu.etaxonomy.cdm.model.taxon.Taxon;

public class PolytomousKeyTest {
	private static Logger logger = Logger.getLogger(PolytomousKeyTest.class);

	private PolytomousKey key1;
	private Taxon taxon1;
	
	
	@BeforeClass
	public static void setUpBeforeClass() {
//		DefaultTermInitializer vocabularyStore = new DefaultTermInitializer();
//		vocabularyStore.initialize();
	}
	
	@Before
	public void setUp() throws Exception {
		key1 = PolytomousKey.NewInstance();
		key1.setTitleCache("My Test Key", true);
		PolytomousKeyNode root = PolytomousKeyNode.NewInstance();
		taxon1 = Taxon.NewInstance(null, null);
		taxon1.setTitleCache("Aus bus L.", true);
		root.setTaxon(taxon1);
		key1.setRoot(root);

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
		Assert.assertEquals("", "My Test Key\n  Root. : Aus bus L.\n", strKey);
	}

//	@Test
//	public void testGetChildren() {
//		fail("Not yet implemented");
//	}

}
