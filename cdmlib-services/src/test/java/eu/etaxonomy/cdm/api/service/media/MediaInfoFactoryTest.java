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
import java.net.URISyntaxException;

import org.apache.http.HttpException;
import org.junit.Before;
import org.junit.Test;
import org.unitils.spring.annotation.SpringBeanByType;

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
    private IMediaInfoFactory mediaInfoFactory;

    private URI transformableURI_ScalerAPI;
    private URI transformableURI_IIIF_API;

    private URI notTransformableURI;

    @Before
    public void initUris() throws URISyntaxException {
        transformableURI_ScalerAPI = new URI("https://pictures.bgbm.org/digilib/Scaler?fn=Cyprus/Sisymbrium_aegyptiacum_C1.jpg&mo=file");
        transformableURI_IIIF_API = new URI("https://pictures.bgbm.org/digilib/Scaler/IIIF/Cichorieae!Lactuca_serriola_Bc_08.jpg/full/full/0/default.jpg");
        notTransformableURI = new URI(MediaInfoFactoryTest.class.getResource("./images/OregonScientificDS6639-DSC_0307-small.jpg"));
    }

    @Test
    public void readMediaInfoViaFileReader() throws IOException, HttpException {
        assertTrue(((MediaInfoFactory)mediaInfoFactory).applyURITransformations(notTransformableURI).isEmpty());
        CdmImageInfo cdmImageInfo = mediaInfoFactory.cdmImageInfo(notTransformableURI, true);
        assertNotNull(cdmImageInfo);
        assertEquals(225, cdmImageInfo.getHeight());
        assertEquals(300, cdmImageInfo.getWidth());
    }

    @Test
    public void readMediaInfoViaServiceReaderScalerAPI() throws IOException, HttpException, URISyntaxException {
        assertEquals(1, ((MediaInfoFactory)mediaInfoFactory).applyURITransformations(transformableURI_ScalerAPI).size());
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
        assertEquals(1, ((MediaInfoFactory)mediaInfoFactory).applyURITransformations(transformableURI_IIIF_API).size());
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

    @Override
    public void createTestDataSet() throws FileNotFoundException {
        // not needed
    }

}
