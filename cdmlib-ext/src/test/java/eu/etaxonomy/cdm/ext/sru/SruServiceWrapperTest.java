/**
 * 
 */
package eu.etaxonomy.cdm.ext.sru;

import java.util.List;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import eu.etaxonomy.cdm.ext.dc.DublinCoreSchemaAdapter;
import eu.etaxonomy.cdm.model.reference.Reference;

/**
 * @author a.mueller
 *
 */
public class SruServiceWrapperTest {
	
	static String baseUrl = "http://gso.gbv.de/sru/DB=1.83/";
	
	public static final Logger logger = Logger.getLogger(SruServiceWrapperTest.class);

	private SruServiceWrapper sruServiceWrapper;
	
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		sruServiceWrapper = new SruServiceWrapper();
		sruServiceWrapper.setBaseUrl(baseUrl);
		sruServiceWrapper.addSchemaAdapter(new DublinCoreSchemaAdapter());
	}

// ******************************* TESTS ******************************************************/

	@Test
	//@Ignore // ignore web accessing tests
	public void testDoSearchRetrieve(){
		
		List<Reference> refList_1 = sruServiceWrapper.doSearchRetrieve("pica.tit=\"Linnaei Species Plantarum\"", "dc");
		// -> http://gso.gbv.de/sru/DB=2.1/?version=1.1&operation=searchRetrieve&query=pica.tit%3D%22Species+Plantarum%22&recordSchema=dc
			
		Assert.assertEquals("There should be exactly 5 results for 'Linnaei Species Plantarum'", 5, refList_1.size());
		Reference reference_1 = refList_1.get(0);
		logger.info(reference_1.toString());
		//title cache
		Assert.assertEquals("Title of first entry should be 'Caroli Linnaei species plantarum'", "Caroli Linnaei species plantarum", reference_1.getTitleCache());

		//--------------------------
		
		List<Reference> refList_2 = sruServiceWrapper.doSearchRetrieve("pica.all = \"Species+plantarum\" and pica.dst = \"8305\"", "dc");
		// -> http://gso.gbv.de/sru/DB=2.1/?version=1.1&operation=searchRetrieve&query=pica.tit%3D%22Species+Plantarum%22&recordSchema=dc
			
		Assert.assertTrue("There should be at least 1 result for 'species+plantarum' and digitized", refList_2.size() > 0);
		Reference reference_2 = refList_2.get(0);
		logger.info(reference_2.toString());
	}


}
