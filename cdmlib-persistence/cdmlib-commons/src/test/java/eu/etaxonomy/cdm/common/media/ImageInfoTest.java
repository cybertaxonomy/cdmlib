/**
 * 
 */
package eu.etaxonomy.cdm.common.media;

import static org.junit.Assert.fail;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.Map;

import org.apache.http.HttpException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import eu.etaxonomy.cdm.common.UriUtils;
import eu.etaxonomy.cdm.common.media.ImageInfo;
import eu.etaxonomy.cdm.common.media.MimeType;

/**
 * @author n.hoffmann
 *
 */
public class ImageInfoTest {

	private URI jpegUri;
	private URI tiffUri;
	private ImageInfo jpegInstance;
	private ImageInfo tifInstance;

	private URI remotePngUri;
	private ImageInfo pngInstance;
	
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		URL jpegUrl = ImageInfoTest.class.getResource("/images/OregonScientificDS6639-DSC_0307-small.jpg");
		jpegUri = jpegUrl.toURI();
		
		URL tiffUrl = ImageInfoTest.class.getResource("/images/OregonScientificDS6639-DSC_0307-small.tif");
		tiffUri = tiffUrl.toURI();
		
		remotePngUri = URI.create("http://dev.e-taxonomy.eu/trac_htdocs/logo_edit.png");
	}
	
	@Test
	public void testNewInstanceJpeg(){
		try {
			ImageInfo.NewInstance(jpegUri, 0);
		} catch (Exception e) {
			fail("NewInstance method should not throw exceptions for existing uncorrupted images.");
		}
	}
		
	@Test
	public void testNewInstanceTiff() {
		try {
			ImageInfo.NewInstance(tiffUri, 0);
		} catch (Exception e) {
			fail("NewInstance method should not throw exceptions for existing uncorrupted images.");
		}
	}
	
	@Test
	public void testNewInstanceRemotePng() {
		try {
			ImageInfo.NewInstance(remotePngUri, 3000);
		} catch (Exception e) {
			fail("NewInstance method should not throw exceptions for existing uncorrupted images.");
		}
	}
	
	@Test(expected=IOException.class)
	public void testNewInstanceFileDoesNotExist() throws HttpException, IOException {
		URI nonExistentUri = URI.create("file:///nonExistentImage.jpg");
		
		ImageInfo.NewInstance(nonExistentUri, 0);	
	}

	private ImageInfo getJpegInstance(){
		if(jpegInstance == null){
			try { 
				jpegInstance = ImageInfo.NewInstance(jpegUri, 0);
			} catch (Exception e) {
				fail("This case should have been covered by other tests.");
				return null;
			}
		}
		return jpegInstance;		
	}
	
	private ImageInfo getTifInstance(){
		if(tifInstance == null){
			try { 
				tifInstance = ImageInfo.NewInstance(tiffUri, 0);
			} catch (Exception e) {
				fail("This case should have been covered by other tests.");
				return null;
			}
		}
		return tifInstance;		
	}
	
	private ImageInfo getRemotePngInstance(){
		if(pngInstance == null){
			try { 
				pngInstance = ImageInfo.NewInstance(remotePngUri, 3000);
			} catch (Exception e) {
				fail("This case should have been covered by other tests.");
				return null;
			}
		}
		return pngInstance;		
	}
	
	/**
	 * Test method for {@link eu.etaxonomy.cdm.common.media.ImageInfo#getWidth()}.
	 */
	@Test
	public void testGetWidth() {
		Assert.assertEquals(300, getJpegInstance().getWidth());
		Assert.assertEquals(300, getTifInstance().getWidth());
		
		if(UriUtils.isInternetAvailable(remotePngUri)){
			Assert.assertEquals(93, getRemotePngInstance().getWidth());
		}
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.common.media.ImageInfo#getHeight()}.
	 */
	@Test
	public void testGetHeight() {
		Assert.assertEquals(225, getJpegInstance().getHeight());
		Assert.assertEquals(225, getTifInstance().getHeight());
		
		if(UriUtils.isInternetAvailable(remotePngUri)){
			Assert.assertEquals(93, getRemotePngInstance().getHeight());
		}
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.common.media.ImageInfo#getBitPerPixel()}.
	 */
	@Test
	public void testGetBitPerPixel() {
		Assert.assertEquals(24, getJpegInstance().getBitPerPixel());
		Assert.assertEquals(24, getTifInstance().getBitPerPixel());
		
		if(UriUtils.isInternetAvailable(remotePngUri)){
			Assert.assertEquals(32, getRemotePngInstance().getBitPerPixel());
		}
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.common.media.ImageInfo#getFormatName()}.
	 */
	@Test
	public void testGetFormatName() {
		Assert.assertEquals("JPEG (Joint Photographic Experts Group) Format", getJpegInstance().getFormatName());
		Assert.assertEquals("TIFF Tag-based Image File Format", getTifInstance().getFormatName());
		
		if(UriUtils.isInternetAvailable(remotePngUri)){
			Assert.assertEquals("PNG Portable Network Graphics", getRemotePngInstance().getFormatName());	
		}
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.common.media.ImageInfo#getMimeType()}.
	 */
	@Test
	public void testGetMimeType() {
		Assert.assertEquals(MimeType.JPEG.getMimeType(), getJpegInstance().getMimeType());
		Assert.assertEquals(MimeType.TIFF.getMimeType(), getTifInstance().getMimeType());
		
		if(UriUtils.isInternetAvailable(remotePngUri)){
			Assert.assertEquals(MimeType.PNG.getMimeType(), getRemotePngInstance().getMimeType());	
		}
	}
	
	@Test
	public void testGetLength(){
		Assert.assertEquals(63500, getJpegInstance().getLength());
		Assert.assertEquals(202926, getTifInstance().getLength());
		
		if(UriUtils.isInternetAvailable(remotePngUri)){
			Assert.assertEquals(9143, getRemotePngInstance().getLength());
		}
	}
	
	@Test
	public void testGetSuffix(){
		Assert.assertEquals("jpg", getJpegInstance().getSuffix());
		Assert.assertEquals("tif", getTifInstance().getSuffix());
		
		if(UriUtils.isInternetAvailable(remotePngUri)){
			Assert.assertEquals("png", getRemotePngInstance().getSuffix());
		}
	}
	
	

	@Test
	public void testReadMetaDataJpeg() throws IOException, HttpException{
		ImageInfo instance = getJpegInstance();
		
		instance.readMetaData(0);
		
		Map<String, String> metaData = instance.getMetaData();
		
		Assert.assertEquals(48, metaData.size());
	}
	

	@Test
	public void testReadMetaDataTif() throws IOException, HttpException{
		ImageInfo instance = getTifInstance();
		
		instance.readMetaData(0);
		
		Map<String, String> metaData = instance.getMetaData();
		
		Assert.assertEquals(15, metaData.size());
	}
	
	@Test
	public void testReadMetaDataRemotePng() throws IOException, HttpException{
		ImageInfo instance = getRemotePngInstance();
		
		instance.readMetaData(3000);
		
		Map<String, String> metaData = instance.getMetaData();
		
		Assert.assertEquals(1, metaData.size());
	}
}
