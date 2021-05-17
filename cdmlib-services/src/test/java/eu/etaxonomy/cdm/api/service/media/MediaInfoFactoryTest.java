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

    private URI transformableURI;

    private URI notTransformableURI;

    @Before
    public void initUris() throws URISyntaxException {
        transformableURI = new URI("https://pictures.bgbm.org/digilib/Scaler?fn=Cyprus/Sisymbrium_aegyptiacum_C1.jpg&mo=file");
        notTransformableURI = new URI(MediaInfoFactoryTest.class.getResource("./images/OregonScientificDS6639-DSC_0307-small.jpg"));
    }

    @Test
    public void readMediaInfoViaFileReader() throws IOException, HttpException {
        assertTrue(((MediaInfoFactory)mediaInfoFactory).applyURITransformations(notTransformableURI).isEmpty());
        CdmImageInfo cdmImageInfo = mediaInfoFactory.cdmImageInfoWithMetaData(notTransformableURI);
        assertNotNull(cdmImageInfo);
        assertEquals(225, cdmImageInfo.getHeight());
        assertEquals(300, cdmImageInfo.getWidth());
    }

    @Test
    public void readMediaInfoViaServiceReader() throws IOException, HttpException, URISyntaxException {
        assertEquals(1, ((MediaInfoFactory)mediaInfoFactory).applyURITransformations(transformableURI).size());
        if(UriUtils.isInternetAvailable(new URI("https://image.bgbm.org/"))){
            CdmImageInfo cdmImageInfo = mediaInfoFactory.cdmImageInfoWithMetaData(transformableURI);
            assertNotNull(cdmImageInfo);
            assertEquals(954, cdmImageInfo.getHeight());
            assertEquals(1400, cdmImageInfo.getWidth());
        }

    }

    @Override
    public void createTestDataSet() throws FileNotFoundException {
        // not needed
    }

}
