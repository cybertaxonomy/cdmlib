/**
 * Copyright (C) 2013 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.ext.openurl;

import java.net.URISyntaxException;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import eu.etaxonomy.cdm.common.URI;
import eu.etaxonomy.cdm.common.UriUtils;

/**
 * @author a.mueller
 */
public class MobotOpenUrlServiceWrapperTest {

    private static final Logger logger = LogManager.getLogger();

    public static final String baseUrl = "http://www.biodiversitylibrary.org/openurl";

	private MobotOpenUrlServiceWrapper openUrlServiceWrapper;
	private static boolean internetIsAvailable = true;

	@BeforeClass
	public static void setUpClass() throws Exception {
		internetIsAvailable = true;
	}

	@Before
	public void setUp() throws Exception {
		openUrlServiceWrapper = new MobotOpenUrlServiceWrapper();
		openUrlServiceWrapper.setBaseUrl(baseUrl);
	}

// ******************************* TESTS ******************************************************/

	@Test
	public void testDoResolveAndPage_1() {

	    try {
            if(!UriUtils.isServiceAvailable(new URI(baseUrl), 1000)) {
                logger.error("Test skipped due to " + baseUrl + " being unavailable");
                return;
            }
        } catch (URISyntaxException e1) {
            logger.error(e1);
        }
		MobotOpenUrlQuery query  = new MobotOpenUrlQuery();
		query.refType = MobotOpenUrlServiceWrapper.ReferenceType.book;
		query.authorFirstName = "Samuel Wendell";
		query.authorFirstName = "Williston";
		query.publicationDate = "1908";
		query.startPage = "Page 16";

		List<OpenUrlReference> refList = openUrlServiceWrapper.doResolve(query);

		if (testInternetConnectivity(refList)){

			// Assert.assertEquals("There should be exactly 2 result for 'Linnaei Species Plantarum Europae'",
			// 2, refList.size());
			OpenUrlReference reference = refList.get(0);
			logger.info(reference.toString());
			// title cache
			Assert.assertEquals("Manual of North American Diptera", reference.getTitleCache());
			// TODO Authorship missing see #6939
			// Assert.assertEquals("Williston, Samuel W. (Samuel Wendell),", reference.getAuthorship().getTitleCache());
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
			Assert.assertEquals("Manual of North American Diptera", reference_plus1.getTitleCache());
			Assert.assertEquals("Page 18", reference_plus1.getPages());
			Assert.assertTrue(reference.getItemUri().equals(reference_plus1.getItemUri()));
			Assert.assertTrue(! reference.getUri().equals(reference_plus1.getUri()));

			logger.info(reference_plus1.getJpegImage(null, null));
			logger.info(reference_plus1.getJpegImage(400, 600));
		}
	}

	@Test
	@Ignore // it seems as if oclc number are no longer supported by the api. Email send to mobot to clarify (a.kohlbecker 2016-07-11)
	public void testDoResolveAndPage_2() {

	    try {
            if(!UriUtils.isServiceAvailable(new URI(baseUrl), 1000)) {
                logger.error("Test skipped due to " + baseUrl + " being unavailable");
                return;
            }
        } catch (URISyntaxException e1) {
            logger.error(e1);
        }
		MobotOpenUrlQuery query  = new MobotOpenUrlQuery();
		query.refType = MobotOpenUrlServiceWrapper.ReferenceType.book;
		query.oclcNumber = "ocm05202749";

		List<OpenUrlReference> refList = openUrlServiceWrapper.doResolve(query);

		if (testInternetConnectivity(refList)){

			// Assert.assertEquals("There should be exactly 2 result for 'Linnaei Species Plantarum Europae'",
			// 2, refList.size());
			OpenUrlReference reference = refList.get(0);
			logger.info(reference.toString());
			Assert.assertEquals("1830", reference.getDatePublished().getEndYear().toString());
			Assert.assertEquals("1797", reference.getDatePublished().getStartYear().toString());
		    logger.info(reference.getJpegImage(null, null));
		}
	}

	@Test
	public void testDoResolveAndPage_3() {

	    try {
            if(!UriUtils.isServiceAvailable(new URI(baseUrl), 1000)) {
                logger.error("Test skipped due to " + baseUrl + " being unavailable");
                return;
            }
        } catch (URISyntaxException e1) {
            logger.error(e1);
        }
		MobotOpenUrlQuery query  = new MobotOpenUrlQuery();
		query.refType = MobotOpenUrlServiceWrapper.ReferenceType.book;
		query.authorName = "Linn\u00E9"; //Linné
		query.abbreviation = "Sp. Pl.";
		query.publicationDate = "1753";
		query.startPage = "813";

		List<OpenUrlReference> refList = openUrlServiceWrapper.doResolve(query);
		if (testInternetConnectivity(refList)){
			Assert.assertTrue("There should be at least one result", refList.size() > 0);
			OpenUrlReference reference = refList.get(0);
		}
	}

	private boolean testInternetConnectivity(List<?> list) {
		if (list == null || list.isEmpty()){
			boolean result = internetIsAvailable && UriUtils.isInternetAvailable(null);
			internetIsAvailable = result;
			return result;
		}
		return true;
	}
}
