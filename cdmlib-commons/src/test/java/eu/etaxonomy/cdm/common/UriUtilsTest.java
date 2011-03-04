package eu.etaxonomy.cdm.common;

import static org.junit.Assert.*;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class UriUtilsTest {
	private static final Logger logger = Logger.getLogger(UriUtilsTest.class);
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

//********************* TESTS **********************************************/
	
	@Test
	public void testCreateUri() {
		try {
			URL baseUrl = new URL("http://www.abc.de");
			String subPath = "fgh";
			String fragment = "frag";
			URI uri = UriUtils.createUri(baseUrl, subPath, null, fragment);
			Assert.assertEquals("http://www.abc.de/fgh#frag", uri.toString());
			List<NameValuePair> qparams = new ArrayList<NameValuePair>(0);
			NameValuePair pair1 = new BasicNameValuePair("param1","value1");
			qparams.add(pair1);
			uri = UriUtils.createUri(baseUrl, subPath, qparams, fragment);
			Assert.assertEquals("http://www.abc.de/fgh?param1=value1#frag", uri.toString());
			
		} catch (MalformedURLException e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		} catch (URISyntaxException e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}
	}

	@Test
	public void testIsInternetAvailable() {
		URI firstUri = URI.create("http://www.gmx.de/");
		boolean isAvailable = UriUtils.isInternetAvailable(firstUri);
		if (isAvailable == false){
			logger.warn("Internet is not available!");
		}
	}
	
	@Test
	public void testIsRootServerAvailable() {
		boolean isAvailable = UriUtils.isRootServerAvailable("www.gmx.de");
		if (isAvailable == false){
			logger.warn("RootServer is not available!");
		}
	}

}
