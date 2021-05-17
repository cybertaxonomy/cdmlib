/**
 *
 */
package eu.etaxonomy.cdm.api.service.media;

import static org.junit.Assert.fail;

import java.io.IOException;
import java.net.URL;
import java.util.Map;

import org.apache.http.HttpException;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import eu.etaxonomy.cdm.common.URI;
import eu.etaxonomy.cdm.common.UriUtils;
import eu.etaxonomy.cdm.common.media.CdmImageInfo;
import eu.etaxonomy.cdm.common.media.MimeType;

/**
 * @author n.hoffmann
 */
public class MediaInfoFileReaderTest {

    private static final String OFFLINE = "OFFLINE";

    public static final Logger logger = Logger.getLogger(MediaInfoFileReaderTest.class);

    private URI jpegUri;
    private URI tiffUri;
    private CdmImageInfo jpegInstance;
    private CdmImageInfo tifInstance;

    private URI remotePngUri;
    private CdmImageInfo pngInstance;

    @Before
    public void setUp() throws Exception {
        URL jpegUrl = MediaInfoFileReaderTest.class.getResource("./images/OregonScientificDS6639-DSC_0307-small.jpg");
        jpegUri = new URI(jpegUrl);

        URL tiffUrl = MediaInfoFileReaderTest.class.getResource("./images/OregonScientificDS6639-DSC_0307-small.tif");
        tiffUri = new URI(tiffUrl);

        remotePngUri = URI.create("https://dev.e-taxonomy.eu/trac_htdocs/logo_edit.png");
    }

    @Test
    public void testNewInstanceJpeg(){
        try {
            new MediaInfoFileReader(jpegUri).readBaseInfo();
        } catch (Exception e) {
            fail("NewInstance method should not throw exceptions for existing uncorrupted images.");
        }
    }

    @Test
    public void testNewInstanceTiff() {
        try {
            new MediaInfoFileReader(tiffUri).readBaseInfo();
        } catch (Exception e) {
            fail("NewInstance method should not throw exceptions for existing uncorrupted images.");
        }
    }

    @Test
    public void testNewInstanceRemotePng() {
        if(UriUtils.isInternetAvailable(remotePngUri)){
            try {
                new MediaInfoFileReader(remotePngUri).readBaseInfo();
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

        new MediaInfoFileReader(nonExistentUri).readBaseInfo();
    }

    private CdmImageInfo getJpegInstance(){
        if(jpegInstance == null){
            try {
                jpegInstance =  new MediaInfoFileReader(jpegUri).readBaseInfo().getCdmImageInfo();
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
                tifInstance = new MediaInfoFileReader(tiffUri).readBaseInfo().getCdmImageInfo();
            } catch (Exception e) {
                fail("This case should have been covered by other tests.");
                return null;
            }
        }
        return tifInstance;
    }

    private CdmImageInfo getRemotePngBaseInfo() throws IOException{
        if (!UriUtils.isInternetAvailable(remotePngUri)){
            throw new IOException(OFFLINE);
        }
        if(pngInstance == null){
            try {
                pngInstance = new MediaInfoFileReader(remotePngUri).readBaseInfo().getCdmImageInfo();
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
            Assert.assertEquals(93, getRemotePngBaseInfo().getWidth());
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
            Assert.assertEquals(93, getRemotePngBaseInfo().getHeight());
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
            Assert.assertEquals(32, getRemotePngBaseInfo().getBitPerPixel());
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
            Assert.assertEquals("PNG Portable Network Graphics", getRemotePngBaseInfo().getFormatName());
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
            Assert.assertEquals(MimeType.PNG.getMimeType(), getRemotePngBaseInfo().getMimeType());
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
            Assert.assertEquals(9143, getRemotePngBaseInfo().getLength());
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
            Assert.assertEquals("png", getRemotePngBaseInfo().getSuffix());
        } catch (IOException e){
            if(e.getMessage().equals(OFFLINE)){
                logger.warn("test part skipped, since server is not available.");
            }
        }
    }

    @Test
    public void testReadMetaDataJpeg() throws IOException, HttpException{

        CdmImageInfo instance = new MediaInfoFileReader(jpegUri).readMetaData().getCdmImageInfo();
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
        CdmImageInfo instance = new MediaInfoFileReader(tiffUri).readBaseInfo().readMetaData().getCdmImageInfo();
        Map<String, String> metaData = instance.getMetaData();
        Assert.assertEquals(15, metaData.size());
    }

    @Test
    public void testReadMetaDataRemotePng() throws HttpException {

        try {
            CdmImageInfo instance = new MediaInfoFileReader(remotePngUri).readMetaData().getCdmImageInfo();
            Map<String, String> metaData = instance.getMetaData();
            Assert.assertEquals(1, metaData.size());

        } catch (IOException e){
            if(e.getMessage().equals(OFFLINE)){
                logger.warn("test testReadMetaDataRemotePng() skipped, since server is not available.");
            }
        }
    }
}
