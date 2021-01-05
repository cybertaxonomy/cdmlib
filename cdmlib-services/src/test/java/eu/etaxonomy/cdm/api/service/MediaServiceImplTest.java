/**
* Copyright (C) 2020 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;

import org.apache.http.HttpException;
import org.junit.BeforeClass;
import org.junit.Test;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.common.URI;
import eu.etaxonomy.cdm.model.media.ImageFile;
import eu.etaxonomy.cdm.model.media.MediaRepresentation;
import eu.etaxonomy.cdm.model.media.MediaRepresentationPart;
import eu.etaxonomy.cdm.test.integration.CdmTransactionalIntegrationTest;

/**
 * @author a.kohlbecker
 * @since Jul 13, 2020
 */
public class MediaServiceImplTest extends CdmTransactionalIntegrationTest {

    @SpringBeanByType
    private IMediaService mediaService;

    static final private String imageFileName1 = "IPTC-PhotometadataRef-Std2019.1.jpg";
    static private MediaRepresentation repr1;
    static private MediaRepresentationPart part1;

    @BeforeClass
    static public void makeMedia() throws URISyntaxException {
        URL mediaURL = MediaServiceImplTest.class.getResource(imageFileName1);
        part1 = ImageFile.NewInstance(URI.fromUrl(mediaURL), null);
        repr1 = MediaRepresentation.NewInstance("image/jpeg", "jpg");
        repr1.addRepresentationPart(part1);
    }


    @Test
    public void testReadResourceMetadataFiltered() throws IOException, HttpException {
         //Logger.getLogger(IdentifiableServiceBase.class).setLevel(Level.DEBUG);
        Map<String, String> mediaData = mediaService.readResourceMetadataFiltered(repr1);
        assertNotNull(mediaData);
        assertTrue("IPCT data in the positive list of the includes", mediaData.containsKey("Copyright"));
        assertTrue("IPCT data in the positive list of the includes", mediaData.containsKey("Artist"));
        assertTrue("IPCT data not in includes", !mediaData.containsKey("Writer"));
        assertEquals("Copyright (Notice) 2019.1 IPTC - www.iptc.org  (ref2019.1)", mediaData.get("Copyright"));
        assertEquals("Creator1 (ref2019.1)", mediaData.get("Artist"));
    }

    @Override
    public void createTestDataSet() throws FileNotFoundException {
        // no test data needed so far
    }

}
