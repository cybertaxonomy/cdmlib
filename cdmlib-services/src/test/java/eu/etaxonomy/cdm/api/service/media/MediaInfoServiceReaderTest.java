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
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.net.URISyntaxException;

import org.apache.http.HttpException;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import eu.etaxonomy.cdm.common.URI;
import eu.etaxonomy.cdm.common.UriUtils;
import eu.etaxonomy.cdm.common.media.CdmImageInfo;

/**
 * @author a.kohlbecker
 * @since May 17, 2021
 */
public class MediaInfoServiceReaderTest  {

    public static final Logger logger = Logger.getLogger(MediaInfoServiceReaderTest.class);

    @Before
    public void setUp() throws Exception {}

    @Test
    public void testJpegBgbmPictures() throws URISyntaxException, IOException, HttpException {
        URI jpegImageUri = new URI("https://pictures.bgbm.org/digilib/Scaler?fn=Cyprus/Sisymbrium_aegyptiacum_C1.jpg&mo=file");
        URI jpegMetadataUri = new URI("https://image.bgbm.org/metadata/info?file=Cyprus/Sisymbrium_aegyptiacum_C1.jpg");

        if(UriUtils.isInternetAvailable(new URI("https://image.bgbm.org/"))){
            CdmImageInfo imageInfo = new MediaInfoServiceReader(jpegImageUri, jpegMetadataUri).read().getCdmImageInfo();
            assertNotNull(imageInfo);
            assertEquals(954, imageInfo.getHeight());
            assertEquals(1400, imageInfo.getWidth());

        } else {
            logger.warn("test testNewInstanceRemotePng() skipped, since server is not available");
        }
    }

    @Test
    public void testJpegBoMediahub() throws URISyntaxException, IOException, HttpException {
        URI jpegImageUri = new URI("https://mediahub.bo.berlin/api/File/Original/1adb62d8-0b67-4128-927b-b713d164f98e/Sisymbrium_aegyptiacum_E1.JPG");
        URI jpegMetadataUri = new URI("https://image.bgbm.org/metadata/info?file=mediacloud/org1/1adb62d8-0b67-4128-927b-b713d164f98e/original");

        if(UriUtils.isInternetAvailable(new URI("https://image.bgbm.org/"))){
            CdmImageInfo imageInfo = new MediaInfoServiceReader(jpegImageUri, jpegMetadataUri).read().getCdmImageInfo();
            assertNotNull(imageInfo);
            assertEquals(937, imageInfo.getHeight());
            assertEquals(1400, imageInfo.getWidth());

        } else {
            logger.warn("test testNewInstanceRemotePng() skipped, since server is not available");
        }
    }
}