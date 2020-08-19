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
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import eu.etaxonomy.cdm.common.UriUtils;

/**
 * @author n.hoffmann
 */
public class CdmImageInfoTest {

    private static final String OFFLINE = "OFFLINE";

    public static final Logger logger = Logger.getLogger(CdmImageInfoTest.class);

    private URI jpegUri;
    private URI tiffUri;
    private CdmImageInfo jpegInstance;
    private CdmImageInfo tifInstance;

    private URI remotePngUri;
    private CdmImageInfo pngInstance;

    @Before
    public void setUp() throws Exception {
        URL jpegUrl = CdmImageInfoTest.class.getResource("/images/OregonScientificDS6639-DSC_0307-small.jpg");
        jpegUri = jpegUrl.toURI();

        URL tiffUrl = CdmImageInfoTest.class.getResource("/images/OregonScientificDS6639-DSC_0307-small.tif");
        tiffUri = tiffUrl.toURI();

        remotePngUri = URI.create("https://dev.e-taxonomy.eu/trac_htdocs/logo_edit.png");
    }

    @Test
    public void testNewInstanceJpeg(){
        try {
            CdmImageInfo.NewInstance(jpegUri, 0);
        } catch (Exception e) {
            fail("NewInstance method should not throw exceptions for existing uncorrupted images.");
        }
    }

    @Test
    public void testNewInstanceTiff() {
        try {
            CdmImageInfo.NewInstance(tiffUri, 0);
        } catch (Exception e) {
            fail("NewInstance method should not throw exceptions for existing uncorrupted images.");
        }
    }

    @Test
    public void testNewInstanceRemotePng() {
        if(UriUtils.isInternetAvailable(remotePngUri)){
            try {
                CdmImageInfo.NewInstance(remotePngUri, 3000);
            } catch (Exception e) {
                fail("NewInstance method should not throw exceptions for existing uncorrupted images.");
            }
        } else {
            logger.warn("test testNewInstanceRemotePng() skipped, since server is not available");
        }
    }

    @Test(expected=IOException.class)
    public void testNewInstanceFileDoesNotExist() throws HttpException, IOException {
        URI nonExistentUri = URI.create("file:///nonExistentImage.jpg");

        CdmImageInfo.NewInstance(nonExistentUri, 0);
    }

    private CdmImageInfo getJpegInstance(){
        if(jpegInstance == null){
            try {
                jpegInstance = CdmImageInfo.NewInstance(jpegUri, 0);
            } catch (Exception e) {
                fail("This case should have been covered by other tests.");
                return null;
            }
        }
        return jpegInstance;
    }

    private CdmImageInfo getTifInstance(){
        if(tifInstance == null){
            try {
                tifInstance = CdmImageInfo.NewInstance(tiffUri, 0);
            } catch (Exception e) {
                fail("This case should have been covered by other tests.");
                return null;
            }
        }
        return tifInstance;
    }

    private CdmImageInfo getRemotePngInstance() throws IOException{
        if (!UriUtils.isInternetAvailable(remotePngUri)){
            throw new IOException(OFFLINE);
        }
        if(pngInstance == null){
            try {
                pngInstance = CdmImageInfo.NewInstance(remotePngUri, 3000);
            } catch (Exception e) {
                fail("This case should have been covered by other tests.");
                return null;
            }
        }
        return pngInstance;
    }

    /**
     * Test method for {@link eu.etaxonomy.cdm.common.media.CdmImageInfo#getWidth()}.
     */
    @Test
    public void testGetWidth() {
        Assert.assertEquals(300, getJpegInstance().getWidth());
        Assert.assertEquals(300, getTifInstance().getWidth());

        try {
            Assert.assertEquals(93, getRemotePngInstance().getWidth());
        } catch (IOException e){
            if(e.getMessage().equals(OFFLINE)){
                logger.warn("test part skipped, since server is not available.");
            }
        }
    }

    /**
     * Test method for {@link eu.etaxonomy.cdm.common.media.CdmImageInfo#getHeight()}.
     */
    @Test
    public void testGetHeight() {
        Assert.assertEquals(225, getJpegInstance().getHeight());
        Assert.assertEquals(225, getTifInstance().getHeight());

        try {
            Assert.assertEquals(93, getRemotePngInstance().getHeight());
        } catch (IOException e){
            if(e.getMessage().equals(OFFLINE)){
                logger.warn("test part skipped, since server is not available.");
            }
        }
    }

    /**
     * Test method for {@link eu.etaxonomy.cdm.common.media.CdmImageInfo#getBitPerPixel()}.
     */
    @Test
    public void testGetBitPerPixel() {
        Assert.assertEquals(24, getJpegInstance().getBitPerPixel());
        Assert.assertEquals(24, getTifInstance().getBitPerPixel());

        try {
            Assert.assertEquals(32, getRemotePngInstance().getBitPerPixel());
        } catch (IOException e){
            if(e.getMessage().equals(OFFLINE)){
                logger.warn("test part skipped, since server is not available.");
            }
        }
    }

    /**
     * Test method for {@link eu.etaxonomy.cdm.common.media.CdmImageInfo#getFormatName()}.
     */
    @Test
    public void testGetFormatName() {
        Assert.assertEquals("JPEG (Joint Photographic Experts Group) Format", getJpegInstance().getFormatName());
        Assert.assertEquals("TIFF Tag-based Image File Format", getTifInstance().getFormatName());

        try {
            Assert.assertEquals("PNG Portable Network Graphics", getRemotePngInstance().getFormatName());
        } catch (IOException e){
            if(e.getMessage().equals(OFFLINE)){
                logger.warn("test part skipped, since server is not available.");
            }
        }
    }

    /**
     * Test method for {@link eu.etaxonomy.cdm.common.media.CdmImageInfo#getMimeType()}.
     */
    @Test
    public void testGetMimeType() {
        Assert.assertEquals(MimeType.JPEG.getMimeType(), getJpegInstance().getMimeType());
        Assert.assertEquals(MimeType.TIFF.getMimeType(), getTifInstance().getMimeType());

        try {
            Assert.assertEquals(MimeType.PNG.getMimeType(), getRemotePngInstance().getMimeType());
        } catch (IOException e){
            if(e.getMessage().equals(OFFLINE)){
                logger.warn("test part skipped, since server is not available.");
            }
        }
    }

    @Test
    public void testGetLength(){
        Assert.assertEquals(55872, getJpegInstance().getLength());
        Assert.assertEquals(202926, getTifInstance().getLength());

        try {
            Assert.assertEquals(9143, getRemotePngInstance().getLength());
        } catch (IOException e){
            if(e.getMessage().equals(OFFLINE)){
                logger.warn("test part skipped, since server is not available.");
            }
        }
    }

    @Test
    public void testGetSuffix(){
        Assert.assertEquals("jpg", getJpegInstance().getSuffix());
        Assert.assertEquals("tif", getTifInstance().getSuffix());

        try {
            Assert.assertEquals("png", getRemotePngInstance().getSuffix());
        } catch (IOException e){
            if(e.getMessage().equals(OFFLINE)){
                logger.warn("test part skipped, since server is not available.");
            }
        }
    }

    @Test
    public void testReadMetaDataJpeg() throws IOException, HttpException{
        CdmImageInfo instance = getJpegInstance();

        instance.readMetaData(0);
        Map<String, String> metaData = instance.getMetaData();
        Assert.assertEquals(52, metaData.size());

        Assert.assertEquals("My taxon", metaData.get("Taxon"));
        Assert.assertEquals("on the road", metaData.get("Locality"));
        Assert.assertEquals("15.02.1955", metaData.get("Date"));
        Assert.assertEquals("Any person", metaData.get("Photographer"));
        Assert.assertEquals("My Keyword; Second Keyword", metaData.get("Keywords"));
    }


    @Test
    public void testReadMetaDataTif() throws IOException, HttpException{
        CdmImageInfo instance = getTifInstance();
        instance.readMetaData(0);
        Map<String, String> metaData = instance.getMetaData();
        Assert.assertEquals(15, metaData.size());
    }

    @Test
    public void testReadMetaDataRemotePng() throws HttpException{

        try {
            CdmImageInfo instance = getRemotePngInstance();
            instance.readMetaData(3000);
            Map<String, String> metaData = instance.getMetaData();
            Assert.assertEquals(1, metaData.size());

        } catch (IOException e){
            if(e.getMessage().equals(OFFLINE)){
                logger.warn("test testReadMetaDataRemotePng() skipped, since server is not available.");
            }
        }
    }
}
