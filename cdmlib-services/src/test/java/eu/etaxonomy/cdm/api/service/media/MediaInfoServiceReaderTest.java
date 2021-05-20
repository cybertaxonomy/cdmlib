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


    private URI jpegImageUri, jpegMetadataUri;

    @Before
    public void setUp() throws Exception {

        jpegImageUri = new URI("https://pictures.bgbm.org/digilib/Scaler?fn=Cyprus/Sisymbrium_aegyptiacum_C1.jpg&mo=file");
        jpegMetadataUri = new URI("https://image.bgbm.org/metadata/info?file=Cyprus/Sisymbrium_aegyptiacum_C1.jpg");
    }

    @Test
    public void testJpeg() throws URISyntaxException, IOException, HttpException {
        if(UriUtils.isInternetAvailable(new URI("https://image.bgbm.org/"))){
            CdmImageInfo imageInfo = new MediaInfoServiceReader(jpegImageUri, jpegMetadataUri).read().getCdmImageInfo();
            assertNotNull(imageInfo);
            assertEquals(954, imageInfo.getHeight());
            assertEquals(1400, imageInfo.getWidth());

        } else {
            logger.warn("test testNewInstanceRemotePng() skipped, since server is not available");
        }
    }

}
