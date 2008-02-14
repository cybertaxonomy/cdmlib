/**
 * 
 */
package eu.etaxonomy.cdm.model.common;

import static org.junit.Assert.*;

import java.io.FileNotFoundException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import eu.etaxonomy.cdm.model.common.init.TermLoader;
import eu.etaxonomy.cdm.model.name.Rank;

/**
 * @author a.mueller
 *
 */
public class TermLoaderTest {

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	
/************** TESTS **********************************/
	
	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.common.init.TermLoader#setDefinedTermsMap(java.util.Map)}.
	 */
	@Test
	public void testSetDefinedTermsMap() {
		Map<UUID, DefinedTermBase> dtm = new HashMap<UUID, DefinedTermBase>();
		TermLoader.setDefinedTermsMap(dtm);
		TermLoader termLoader = new TermLoader();
		
		Object result = null;
		try {
			Field dtmField = TermLoader.class.getDeclaredField("definedTermsMap");
			dtmField.setAccessible(true);
			result = (Map)dtmField.get(termLoader);
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
		assertSame(dtm, result);
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.common.init.TermLoader#loadTerms(java.lang.Class, java.lang.String, boolean)}.
	 */
	@Test
	public void testLoadTerms() {
		TermLoader termLoader = new TermLoader();
		try {
			String filename = "Rank.csv";
			boolean isEnumeration = true;
			Class termClass = Rank.class;
			termLoader.loadTerms(termClass, filename, isEnumeration, true);
		} catch (FileNotFoundException e) {
			fail();
		} catch (NoDefinedTermClassException e) {
			fail();;
		}
		assertEquals(Rank.GENUS().getUuid(), UUID.fromString("1b11c34c-48a8-4efa-98d5-84f7f66ef43a"));
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.common.init.TermLoader#loadDefaultTerms(java.lang.Class)}.
	 */
	@Test
	public void testLoadDefaultTerms() {
		TermLoader termLoader = new TermLoader();
		try {
			termLoader.loadDefaultTerms(Rank.class, true);
		} catch (FileNotFoundException e) {
			fail();
		} catch (NoDefinedTermClassException e) {
			fail();;
		}
		assertEquals(Rank.GENUS().getUuid(), UUID.fromString("1b11c34c-48a8-4efa-98d5-84f7f66ef43a"));
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.common.init.TermLoader#loadAllDefaultTerms()}.
	 */
	@Test
	public void testLoadAllDefaultTerms() {
		TermLoader termLoader = new TermLoader();
		try {
			termLoader.loadAllDefaultTerms();
		} catch (FileNotFoundException e) {
			fail();
		} catch (NoDefinedTermClassException e) {
			fail();;
		}
		assertEquals(Rank.GENUS().getUuid(), UUID.fromString("1b11c34c-48a8-4efa-98d5-84f7f66ef43a"));
	}

}
