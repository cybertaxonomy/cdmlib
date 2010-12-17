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

import eu.etaxonomy.cdm.ext.openurl.MobotOpenUrlQuery;
import eu.etaxonomy.cdm.ext.openurl.MobotOpenUrlResponseSchemaAdapter;
import eu.etaxonomy.cdm.ext.openurl.MobotOpenUrlServiceWrapper;
import eu.etaxonomy.cdm.ext.openurl.OpenUrlReference;
import eu.etaxonomy.cdm.model.reference.Reference;

/**
 * @author a.mueller
 *
 */
public class MobotOpenUrlServiceWrapperTest {
	
	static String baseUrl = "http://www.biodiversitylibrary.org/openurl";
	
	public static final Logger logger = Logger.getLogger(MobotOpenUrlServiceWrapperTest.class);

	private MobotOpenUrlServiceWrapper openUrlServiceWrapper;
	
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		openUrlServiceWrapper = new MobotOpenUrlServiceWrapper();
		openUrlServiceWrapper.setBaseUrl(baseUrl);
		openUrlServiceWrapper.addSchemaAdapter(new MobotOpenUrlResponseSchemaAdapter());
	}

// ******************************* TESTS ******************************************************/

	@Test
	@Ignore // ignore web accessing tests
	public void testDoResolveAndPage() {

		MobotOpenUrlQuery query  = new MobotOpenUrlQuery();
		query.refType = MobotOpenUrlServiceWrapper.ReferenceType.book;
		query.authorFirstName = "Samuel Wendell";
		query.authorFirstName = "Williston";
		query.publicationDate = "1908";
		query.startPage = "Page 16";
		
		List<OpenUrlReference> refList = openUrlServiceWrapper.doResolve(query);

		// Assert.assertEquals("There should be exactly 2 result for 'Linnaei Species Plantarum Europae'",
		// 2, refList.size());
		OpenUrlReference reference = refList.get(0);
		logger.info(reference.toString());
		// title cache
		Assert.assertEquals("Manual of North American Diptera /  by Samuel W. Williston.", reference.getTitleCache());
		Assert.assertEquals("Page 16", reference.getPages());
		
		// -------------------------
		
		refList = openUrlServiceWrapper.doPage(reference, 2);
		OpenUrlReference reference_plus1 = refList.get(0);
		logger.info(reference_plus1.toString());
		Assert.assertEquals("Manual of North American Diptera /  by Samuel W. Williston.", reference_plus1.getTitleCache());
		Assert.assertEquals("Page 18", reference_plus1.getPages());

	}

}
