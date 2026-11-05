/**
* Copyright (C) 2021 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.media;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.net.URL;
import java.util.Map;

import org.apache.hc.core5.http.HttpException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.etaxonomy.cdm.common.URI;
import eu.etaxonomy.cdm.common.UriUtils;
import eu.etaxonomy.cdm.common.media.CdmImageInfo;
import eu.etaxonomy.cdm.common.media.MimeType;

/**
 * @author n.hoffmann
 */
public class MediaInfoFileReaderTest {

    private static final Logger logger = LogManager.getLogger();

    private static final String OFFLINE = "OFFLINE";

    private URI jpegUri;
    private URI tiffUri;
    private CdmImageInfo jpegInstance;
    private CdmImageInfo tifInstance;

    private URI remotePngUri;
    private CdmImageInfo pngInstance;

    @BeforeEach
    public void setUp() throws Exception {
        URL jpegUrl = MediaInfoFileReaderTest.class.getResource("./images/OregonScientificDS6639-DSC_0307-small-1.jpg");
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

    @Test
    public void testNewInstanceFileDoesNotExist() throws HttpException, IOException {
        URI nonExistentUri = URI.create("file:///nonExistentImage.jpg");

        assertThrows(IOException.class, () -> new MediaInfoFileReader(nonExistentUri).readBaseInfo());
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
        assertEquals(300, getJpegInstance().getWidth());
        assertEquals(300, getTifInstance().getWidth());

        try {
            assertEquals(93, getRemotePngBaseInfo().getWidth());
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
        assertEquals(225, getJpegInstance().getHeight());
        assertEquals(225, getTifInstance().getHeight());

        try {
            assertEquals(93, getRemotePngBaseInfo().getHeight());
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
        assertEquals(24, getJpegInstance().getBitPerPixel());
        assertEquals(24, getTifInstance().getBitPerPixel());

        try {
            assertEquals(32, getRemotePngBaseInfo().getBitPerPixel());
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
        assertEquals("JPEG (Joint Photographic Experts Group) Format", getJpegInstance().getFormatName());
        assertEquals("TIFF Tag-based Image File Format", getTifInstance().getFormatName());

        try {
            assertEquals("PNG Portable Network Graphics", getRemotePngBaseInfo().getFormatName());
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
        assertEquals(MimeType.JPEG.getMimeType(), getJpegInstance().getMimeType());
        assertEquals(MimeType.TIFF.getMimeType(), getTifInstance().getMimeType());

        try {
            assertEquals(MimeType.PNG.getMimeType(), getRemotePngBaseInfo().getMimeType());
        } catch (IOException e){
            if(e.getMessage().equals(OFFLINE)){
                logger.warn("test part skipped, since server is not available.");
            }
        }
    }

    @Test
    public void testGetLength(){
        assertEquals(35339, getJpegInstance().getLength());
        assertEquals(202926, getTifInstance().getLength());

        try {
            assertEquals(9143, getRemotePngBaseInfo().getLength());
        } catch (IOException e){
            if(e.getMessage().equals(OFFLINE)){
                logger.warn("test part skipped, since server is not available.");
            }
        }
    }

    @Test
    public void testGetSuffix(){
        assertEquals("jpg", getJpegInstance().getSuffix());
        assertEquals("tif", getTifInstance().getSuffix());

        try {
            assertEquals("png", getRemotePngBaseInfo().getSuffix());
        } catch (IOException e){
            if(e.getMessage().equals(OFFLINE)){
                logger.warn("test part skipped, since server is not available.");
            }
        }
    }

    @Test
    public void testReadMetaDataJpeg() throws IOException, HttpException{

        CdmImageInfo instance = new MediaInfoFileReader(jpegUri).read().getCdmImageInfo();
        Map<String, String> metaData = instance.getMetaData();
        //At this point we do not have the filter/deduplication information
        assertEquals(57, metaData.size());

        assertEquals("My taxon", metaData.get("Taxon"));
        assertEquals("on the road", metaData.get("Locality"));
        assertEquals("15.02.1955", metaData.get("Date Created"));
        assertEquals("Any person", metaData.get("Photographer"));
        //only simple keywords needed
        assertEquals("My Keyword; Second Keyword", metaData.get("Keywords"));
    }


    @Test
    public void testReadMetaDataTif() throws IOException, HttpException{
        CdmImageInfo instance = new MediaInfoFileReader(tiffUri).readBaseInfo().read().getCdmImageInfo();
        Map<String, String> metaData = instance.getMetaData();
        assertEquals(15, metaData.size());
    }

    @Test
    public void testReadMetaDataRemotePng() throws HttpException {

        try {
            CdmImageInfo instance = new MediaInfoFileReader(remotePngUri).read().getCdmImageInfo();
            Map<String, String> metaData = instance.getMetaData();
            assertEquals(1, metaData.size());

        } catch (IOException e){
            if(e.getMessage().equals(OFFLINE)){
                logger.warn("test testReadMetaDataRemotePng() skipped, since server is not available.");
            }
        }
    }
}
