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
	
	static String baseUrl = "http://gso.gbv.de/sru/DB=2.1/";
	
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
	@Ignore // ignore web accessing tests
	public void testDoSearchRetrieve(){
		
		List<Reference> refList = sruServiceWrapper.doSearchRetrieve("pica.tit=\"Linnaei Species Plantarum Europae\"", "dc");
		// -> http://gso.gbv.de/sru/DB=2.1/?version=1.1&operation=searchRetrieve&query=pica.tit%3D%22Species+Plantarum%22&recordSchema=dc
			
		Assert.assertEquals("There should be exactly 2 result for 'Linnaei Species Plantarum Europae'", 2, refList.size());
		Reference reference = refList.get(0);
		logger.info(reference.toString());
		//title cache
		Assert.assertEquals("Title Cache for Abies albertiana should be 'Linnaei Species Plantarum Europae Pars 2. Supplementum Plantarum Europaearum ...'", "Linnaei Species Plantarum Europae Pars 2. Supplementum Plantarum Europaearum ...", reference.getTitleCache());

	}	


}
