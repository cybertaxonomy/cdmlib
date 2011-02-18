/**
 * 
 */
package eu.etaxonomy.cdm.ext.openurl;

import java.util.List;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

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
	}

// ******************************* TESTS ******************************************************/

	
	@Test
	//@Ignore // ignore web accessing tests
	public void testDoResolveAndPage_1() {

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
		
		try {
			refList = openUrlServiceWrapper.doPage(reference, 2);
		} catch (Exception e) {
			refList = null;
		}
		Assert.assertNotNull(refList);
		OpenUrlReference reference_plus1 = refList.get(0);
		logger.info(reference_plus1.toString());
		Assert.assertEquals("Manual of North American Diptera /  by Samuel W. Williston.", reference_plus1.getTitleCache());
		Assert.assertEquals("Page 18", reference_plus1.getPages());
		Assert.assertTrue(reference.getItemUri().equals(reference_plus1.getItemUri()));
		Assert.assertTrue(! reference.getUri().equals(reference_plus1.getUri()));
		
		logger.info(reference_plus1.getJpegImage(null, null));
		logger.info(reference_plus1.getJpegImage(400, 600));
	}

	@Test
	public void testDoResolveAndPage_2() {

		MobotOpenUrlQuery query  = new MobotOpenUrlQuery();
		query.refType = MobotOpenUrlServiceWrapper.ReferenceType.book;
		query.oclcNumber = "ocm05202749";
		
		List<OpenUrlReference> refList = openUrlServiceWrapper.doResolve(query);

		// Assert.assertEquals("There should be exactly 2 result for 'Linnaei Species Plantarum Europae'",
		// 2, refList.size());
		OpenUrlReference reference = refList.get(0);
		logger.info(reference.toString());
		Assert.assertEquals("1830", reference.getDatePublished().getEndYear().toString());
		Assert.assertEquals("1797", reference.getDatePublished().getStartYear().toString()); 
	    logger.info(reference.getJpegImage(null, null));
	}
	
	@Test
	public void testDoResolveAndPage_3() {

		MobotOpenUrlQuery query  = new MobotOpenUrlQuery();
		query.refType = MobotOpenUrlServiceWrapper.ReferenceType.book;
		query.authorName = "Linné";
		query.abbreviation = "Sp. Pl.";
		query.publicationDate = "1753";
		query.startPage = "813";
		
		List<OpenUrlReference> refList = openUrlServiceWrapper.doResolve(query);

		Assert.assertTrue("There should be at least one result", refList.size() > 0);
		OpenUrlReference reference = refList.get(0);
	}

}
