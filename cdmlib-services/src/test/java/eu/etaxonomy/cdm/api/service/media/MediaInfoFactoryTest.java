/**
* Copyright (C) 2021 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.media;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.List;

import org.apache.http.HttpException;
import org.cybertaxonomy.media.info.model.MediaInfo;
import org.junit.Before;
import org.junit.Test;
import org.unitils.spring.annotation.SpringBeanByType;

import com.fasterxml.jackson.databind.ObjectMapper;

import eu.etaxonomy.cdm.common.URI;
import eu.etaxonomy.cdm.common.UriUtils;
import eu.etaxonomy.cdm.common.media.CdmImageInfo;
import eu.etaxonomy.cdm.test.integration.CdmIntegrationTest;

/**
 * @author a.kohlbecker
 * @since May 17, 2021
 */
public class MediaInfoFactoryTest extends CdmIntegrationTest {

    @SpringBeanByType
    private MediaInfoFactory mediaInfoFactory;

    private URI transformableURI_ScalerAPI;
    private URI transformableURI_IIIF_API;

    private URI notTransformableURI;

    private URI mediahubUri;

    @Before
    public void initUris() throws URISyntaxException {
        transformableURI_ScalerAPI = new URI("https://pictures.bgbm.org/digilib/Scaler?fn=Cyprus/Sisymbrium_aegyptiacum_C1.jpg&mo=file");
        transformableURI_IIIF_API = new URI("https://pictures.bgbm.org/digilib/Scaler/IIIF/Cichorieae!Lactuca_serriola_Bc_08.jpg/full/full/0/default.jpg");
        notTransformableURI = new URI(MediaInfoFactoryTest.class.getResource("./images/OregonScientificDS6639-DSC_0307-small.jpg"));
        mediahubUri = new URI("https://mediahub.bo.berlin/api/File/Original/1adb62d8-0b67-4128-927b-b713d164f98e/Sisymbrium_aegyptiacum_E1.JPG");
    }

    @Test
    public void readMediaInfoViaFileReader() throws IOException, HttpException {
        assertTrue(mediaInfoFactory.applyURITransformations(notTransformableURI).isEmpty());
        CdmImageInfo cdmImageInfo = mediaInfoFactory.cdmImageInfo(notTransformableURI, true);
        assertNotNull(cdmImageInfo);
        assertEquals(225, cdmImageInfo.getHeight());
        assertEquals(300, cdmImageInfo.getWidth());
    }

    @Test
    public void readMediaInfoViaServiceReaderScalerAPI() throws IOException, HttpException, URISyntaxException {
        assertEquals(1, mediaInfoFactory.applyURITransformations(transformableURI_ScalerAPI).size());
        if(UriUtils.isInternetAvailable(new URI("https://image.bgbm.org/"))){
            CdmImageInfo cdmImageInfo = mediaInfoFactory.cdmImageInfo(transformableURI_ScalerAPI, true);
            assertNotNull(cdmImageInfo);
            assertEquals(954, cdmImageInfo.getHeight());
            assertEquals(1400, cdmImageInfo.getWidth());
            assertEquals("JPEG", cdmImageInfo.getSuffix());
            assertEquals("JPEG", cdmImageInfo.getFormatName());
            assertEquals(24, cdmImageInfo.getBitPerPixel());
            assertEquals("image/jpeg", cdmImageInfo.getMimeType());
            assertEquals(321414, cdmImageInfo.getLength());
            // generic metadata
            assertFalse(cdmImageInfo.getMetaData().isEmpty());
            assertEquals(55, cdmImageInfo.getMetaData().size());
            assertTrue(cdmImageInfo.getMetaData().containsKey("XResolution"));
            // multiple values for XResolution concatenated
            assertEquals("300; 72", cdmImageInfo.getMetaData().get("XResolution"));
        }
    }

    @Test
    public void readMediaInfoViaServiceReaderIIIF_API() throws IOException, HttpException, URISyntaxException {
        assertEquals(1, mediaInfoFactory.applyURITransformations(transformableURI_IIIF_API).size());
        if(UriUtils.isInternetAvailable(new URI("https://image.bgbm.org/"))){
            CdmImageInfo cdmImageInfo = mediaInfoFactory.cdmImageInfo(transformableURI_IIIF_API, true);
            assertNotNull(cdmImageInfo);
            assertEquals(2592, cdmImageInfo.getHeight());
            assertEquals(3456, cdmImageInfo.getWidth());
            assertEquals("JPEG", cdmImageInfo.getSuffix());
            assertEquals("JPEG", cdmImageInfo.getFormatName());
            assertEquals(24, cdmImageInfo.getBitPerPixel());
            assertEquals("image/jpeg", cdmImageInfo.getMimeType());
            assertEquals(3780263, cdmImageInfo.getLength());
            // generic metadata
            assertFalse(cdmImageInfo.getMetaData().isEmpty());
            assertEquals(56, cdmImageInfo.getMetaData().size());
            assertTrue(cdmImageInfo.getMetaData().containsKey("ExifVersion"));
            assertEquals("48, 50, 50, 49", cdmImageInfo.getMetaData().get("ExifVersion"));
            // custom metadata stored in the "Keywords"
            assertEquals("Lactuca serriola", cdmImageInfo.getMetaData().get("Taxon"));
            assertEquals("Germany, Sachsen-Anhalt, am Jersleber See", cdmImageInfo.getMetaData().get("Locality"));
            assertEquals("Lactuca serriola", cdmImageInfo.getMetaData().get("Taxon"));
            assertEquals("23.8.2009", cdmImageInfo.getMetaData().get("Date"));
            assertEquals("N. Kilian", cdmImageInfo.getMetaData().get("Photographer"));
        }
    }

    @Test
    public void testMediaMetadataServiceTransformation() throws URISyntaxException, IOException, HttpException {
        List<URI> list = mediaInfoFactory.applyURITransformations(mediahubUri);
        assertEquals(1, list.size());
        URI transformedUri = list.get(0);
        assertEquals("https://image.bgbm.org/metadata/info?file=mediacloud/org1/1adb62d8-0b67-4128-927b-b713d164f98e/original", transformedUri.toString());
        if(UriUtils.isInternetAvailable(new URI("https://image.bgbm.org/"))){
            InputStream jsonStream = UriUtils.getInputStream(transformedUri);
            ObjectMapper mapper = new ObjectMapper();
            MediaInfo mediaInfo = mapper.readValue(jsonStream, MediaInfo.class);
            assertEquals(937, mediaInfo.getHeight());
            assertEquals(1400, mediaInfo.getWidth());
        }
    }


    @Override
    public void createTestDataSet() throws FileNotFoundException {}
}