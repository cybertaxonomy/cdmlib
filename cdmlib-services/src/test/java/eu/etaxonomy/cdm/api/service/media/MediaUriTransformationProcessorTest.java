/**
* Copyright (C) 2020 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.media;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.net.URISyntaxException;
import java.util.List;

import org.junit.Test;

import eu.etaxonomy.cdm.common.URI;
import eu.etaxonomy.cdm.model.media.ImageFile;
import eu.etaxonomy.cdm.model.media.MediaRepresentation;

/**
 * @author a.kohlbecker
 * @since Jul 7, 2020
 */
public class MediaUriTransformationProcessorTest {

    private static final String URI_STRING_1 = "https://pictures.bgbm.org/digilib/Scaler/?fn=Cyprus/Salvia_aethiopis_A1.jpg&mo=file";

    private static final String PATTERN_1 = "digilib/Scaler/\\?fn=([^\\/]+)/([^\\&]+)(.*)";

    private MediaRepresentation makeImageMediaRepresentation(int width, int height) throws URISyntaxException {
        MediaRepresentation repr = MediaRepresentation.NewInstance("image/jpeg", null);
        ImageFile part = ImageFile.NewInstance(new URI(URI_STRING_1), null, height, width);
        repr.addRepresentationPart(part);
        return repr;
    }

    @Test
    public void testUriTranformation() throws URISyntaxException {
        URI uri = new URI(URI_STRING_1);

        MediaUriTransformation transformation1 = new MediaUriTransformation();
        transformation1.setPathQueryFragment(new SearchReplace(PATTERN_1, "digilib/Scaler/IIIF/$1!$2/full/full/0/default.jpg"));

        MediaUriTransformation transformation2 = new MediaUriTransformation();
        transformation2.setPathQueryFragment(new SearchReplace(PATTERN_1, "digilib/Scaler/IIIF/$1!$2/!400,400/full/0/default.jpg"));
        transformation2.setHost(new SearchReplace("pictures.bgbm.org", "images.bgbm.org")); // host part wont match => no transformation!

        MediaUriTransformationProcessor processor = new MediaUriTransformationProcessor();
        processor.add(transformation1);
        processor.add(transformation2);

        List<URI> transformed =  processor.applyTo(uri);
        assertEquals("https://pictures.bgbm.org/digilib/Scaler/IIIF/Cyprus!Salvia_aethiopis_A1.jpg/full/full/0/default.jpg", transformed.get(0).toString());
        assertEquals("https://images.bgbm.org/digilib/Scaler/IIIF/Cyprus!Salvia_aethiopis_A1.jpg/!400,400/full/0/default.jpg", transformed.get(1).toString());
    }

    @Test
    public void testUriTranformationWithNotMatchCase() throws URISyntaxException {
        URI uri = new URI(URI_STRING_1);

        MediaUriTransformation transformation2 = new MediaUriTransformation();
        transformation2.setPathQueryFragment(new SearchReplace(PATTERN_1, "digilib/Scaler/IIIF/$1!$2/!400,400/full/0/default.jpg"));
        transformation2.setHost(new SearchReplace("pictures.bgbm.org", "pictures.bgbm.org")); // host part only used for matching, no replace!
        transformation2.setMaxExtend(true);

        MediaUriTransformationProcessor processor = new MediaUriTransformationProcessor();
        processor.add(transformation2);

        List<URI> transformed =  processor.applyTo(uri);
        assertEquals("https://pictures.bgbm.org/digilib/Scaler/IIIF/Cyprus!Salvia_aethiopis_A1.jpg/!400,400/full/0/default.jpg", transformed.get(0).toString());
    }

    @Test
    public void testUriTranformationWithMatchCase() throws URISyntaxException {
        URI uri = new URI(URI_STRING_1);

        MediaUriTransformation transformation2 = new MediaUriTransformation();
        transformation2.setPathQueryFragment(new SearchReplace(PATTERN_1, "digilib/Scaler/IIIF/$1!$2/!400,400/full/0/default.jpg"));
        transformation2.setHost(new SearchReplace("not.matching.host", "images.bgbm.org"));
        transformation2.setMaxExtend(true);

        MediaUriTransformationProcessor processor = new MediaUriTransformationProcessor();
        processor.add(transformation2);

        List<URI> transformed =  processor.applyTo(uri);
        assertTrue(transformed.isEmpty());
    }

    @Test
    public void testMakeMedia() throws URISyntaxException {

        URI uri = new URI(URI_STRING_1);

        MediaUriTransformation transformation1 = new MediaUriTransformation();
        transformation1.setPathQueryFragment(new SearchReplace(PATTERN_1, "digilib/Scaler/IIIF/$1!$2/400,200/full/0/default.jpg"));
        transformation1.setMimeType("image/jpeg");
        transformation1.setWidth(400);
        transformation1.setHeight(200);
        transformation1.setMaxExtend(true);
        MediaUriTransformationProcessor processor = new MediaUriTransformationProcessor();
        processor.add(transformation1);

        List<MediaRepresentation> representations = processor.makeNewMediaRepresentationsFor(uri);

        assertEquals("https://pictures.bgbm.org/digilib/Scaler/IIIF/Cyprus!Salvia_aethiopis_A1.jpg/400,200/full/0/default.jpg", representations.get(0).getParts().get(0).getUri().toString());
        assertEquals(ImageFile.class, representations.get(0).getParts().get(0).getClass());
        ImageFile image = (ImageFile)representations.get(0).getParts().get(0);
        assertEquals(transformation1.getWidth(), image.getWidth());
        assertEquals(transformation1.getHeight(), image.getHeight());
    }

    @Test
    public void testMakeMediaWithNotMatchCase() throws URISyntaxException {

        MediaUriTransformation transformation2 = new MediaUriTransformation();
        transformation2.setPathQueryFragment(new SearchReplace(PATTERN_1, "digilib/Scaler/IIIF/$1!$2/!400,400/full/0/default.jpg"));
        transformation2.setHost(new SearchReplace("not.matching.host", "pictures.bgbm.org")); // host part only used for matching, no replace!
        transformation2.setMimeType("image/jpeg");
        transformation2.setWidth(400);
        transformation2.setHeight(200);
        transformation2.setMaxExtend(true);
        MediaUriTransformationProcessor processor = new MediaUriTransformationProcessor();
        processor.add(transformation2);


        MediaRepresentation repr = makeImageMediaRepresentation(2000, 1500);

        List<MediaRepresentation> representations = processor.makeNewMediaRepresentationsFor(repr.getParts().get(0));
        assertTrue(representations.isEmpty());
    }

    @Test
    public void testMakeMediaCalculateWidth() throws URISyntaxException {

        MediaUriTransformation transformation1 = new MediaUriTransformation();
        transformation1.setPathQueryFragment(new SearchReplace(PATTERN_1, "digilib/Scaler/IIIF/$1!$2/400,/full/0/default.jpg"));
        transformation1.setMimeType("image/jpeg");
        transformation1.setWidth(400);
        transformation1.setHeight(null);
        transformation1.setMaxExtend(false);
        MediaUriTransformationProcessor processor = new MediaUriTransformationProcessor();
        processor.add(transformation1);

        MediaRepresentation repr = makeImageMediaRepresentation(2000, 1500); // aspect ratio = 4/3

        List<MediaRepresentation> representations = processor.makeNewMediaRepresentationsFor(repr.getParts().get(0));

        assertEquals("https://pictures.bgbm.org/digilib/Scaler/IIIF/Cyprus!Salvia_aethiopis_A1.jpg/400,/full/0/default.jpg", representations.get(0).getParts().get(0).getUri().toString());
        assertEquals(ImageFile.class, representations.get(0).getParts().get(0).getClass());
        ImageFile image = (ImageFile)representations.get(0).getParts().get(0);
        assertEquals(Integer.valueOf(400), image.getWidth());
        assertEquals(Integer.valueOf(300), image.getHeight());
    }

    @Test
    public void testMakeMediaCalculateHeight() throws URISyntaxException {


        MediaUriTransformation transformation1 = new MediaUriTransformation();
        transformation1.setPathQueryFragment(new SearchReplace(PATTERN_1, "digilib/Scaler/IIIF/$1!$2/,300/full/0/default.jpg"));
        transformation1.setMimeType("image/jpeg");
        transformation1.setWidth(null);
        transformation1.setHeight(300);
        transformation1.setMaxExtend(false);
        MediaUriTransformationProcessor processor = new MediaUriTransformationProcessor();
        processor.add(transformation1);

        MediaRepresentation repr = makeImageMediaRepresentation(2000, 1500); // aspect ratio = 4/3

        List<MediaRepresentation> representations = processor.makeNewMediaRepresentationsFor(repr.getParts().get(0));

        assertEquals("https://pictures.bgbm.org/digilib/Scaler/IIIF/Cyprus!Salvia_aethiopis_A1.jpg/,300/full/0/default.jpg", representations.get(0).getParts().get(0).getUri().toString());
        assertEquals(ImageFile.class, representations.get(0).getParts().get(0).getClass());
        ImageFile image = (ImageFile)representations.get(0).getParts().get(0);
        assertEquals(Integer.valueOf(400), image.getWidth());
        assertEquals(Integer.valueOf(300), image.getHeight());
    }

    @Test
    public void testMakeMediaCropLandcape() throws URISyntaxException {

        MediaUriTransformation transformation1 = new MediaUriTransformation();
        transformation1.setPathQueryFragment(new SearchReplace(PATTERN_1, "digilib/Scaler/?fn=$1/$2&mo=crop&dw=200&dh=147&uvfix=1"));
        transformation1.setMimeType("image/jpeg");
        transformation1.setWidth(200);
        transformation1.setHeight(147);
        transformation1.setMaxExtend(false);
        MediaUriTransformationProcessor processor = new MediaUriTransformationProcessor();
        processor.add(transformation1);

        MediaRepresentation repr = makeImageMediaRepresentation(2000, 1500); // aspect ratio = 4/3

        List<MediaRepresentation> representations = processor.makeNewMediaRepresentationsFor(repr.getParts().get(0));

        assertEquals("https://pictures.bgbm.org/digilib/Scaler/?fn=Cyprus/Salvia_aethiopis_A1.jpg&mo=crop&dw=200&dh=147&uvfix=1", representations.get(0).getParts().get(0).getUri().toString());
        assertEquals(ImageFile.class, representations.get(0).getParts().get(0).getClass());
        ImageFile image = (ImageFile)representations.get(0).getParts().get(0);
        assertEquals(Integer.valueOf(200), image.getWidth());
        assertEquals(Integer.valueOf(147), image.getHeight());

        // aspect ratio = 3/4
        MediaRepresentation repr2 = makeImageMediaRepresentation(1500, 2000);

        representations = processor.makeNewMediaRepresentationsFor(repr2.getParts().get(0));

        assertEquals("https://pictures.bgbm.org/digilib/Scaler/?fn=Cyprus/Salvia_aethiopis_A1.jpg&mo=crop&dw=200&dh=147&uvfix=1", representations.get(0).getParts().get(0).getUri().toString());
        assertEquals(ImageFile.class, representations.get(0).getParts().get(0).getClass());
        image = (ImageFile)representations.get(0).getParts().get(0);
        assertEquals(Integer.valueOf(200), image.getWidth());
        assertEquals(Integer.valueOf(147), image.getHeight());
    }

    @Test
    public void testMakeMediaCropPortrait() throws URISyntaxException {

        MediaUriTransformation transformation1 = new MediaUriTransformation();
        transformation1.setPathQueryFragment(new SearchReplace(PATTERN_1, "digilib/Scaler/?fn=$1/$2&mo=crop&dw=147&dh=200&uvfix=1"));
        transformation1.setMimeType("image/jpeg");
        transformation1.setWidth(147);
        transformation1.setHeight(200);
        transformation1.setMaxExtend(false);
        MediaUriTransformationProcessor processor = new MediaUriTransformationProcessor();
        processor.add(transformation1);

        MediaRepresentation repr = makeImageMediaRepresentation(2000, 1500); // aspect ratio = 4/3

        List<MediaRepresentation> representations = processor.makeNewMediaRepresentationsFor(repr.getParts().get(0));

        assertEquals("https://pictures.bgbm.org/digilib/Scaler/?fn=Cyprus/Salvia_aethiopis_A1.jpg&mo=crop&dw=147&dh=200&uvfix=1", representations.get(0).getParts().get(0).getUri().toString());
        assertEquals(ImageFile.class, representations.get(0).getParts().get(0).getClass());
        ImageFile image = (ImageFile)representations.get(0).getParts().get(0);
        assertEquals(Integer.valueOf(147), image.getWidth());
        assertEquals(Integer.valueOf(200), image.getHeight());

        // aspect ratio = 3/4
        MediaRepresentation repr2 = makeImageMediaRepresentation(1500, 2000);

        representations = processor.makeNewMediaRepresentationsFor(repr2.getParts().get(0));

        assertEquals("https://pictures.bgbm.org/digilib/Scaler/?fn=Cyprus/Salvia_aethiopis_A1.jpg&mo=crop&dw=147&dh=200&uvfix=1", representations.get(0).getParts().get(0).getUri().toString());
        assertEquals(ImageFile.class, representations.get(0).getParts().get(0).getClass());
        image = (ImageFile)representations.get(0).getParts().get(0);
        assertEquals(Integer.valueOf(147), image.getWidth());
        assertEquals(Integer.valueOf(200), image.getHeight());
    }

    @Test
    public void testMakeMediaCropSquare() throws URISyntaxException {

        MediaUriTransformation transformation1 = new MediaUriTransformation();
        transformation1.setPathQueryFragment(new SearchReplace(PATTERN_1, "digilib/Scaler/IIIF/$1!$2/400,400/full/0/default.jpg"));
        transformation1.setMimeType("image/jpeg");
        transformation1.setWidth(400);
        transformation1.setHeight(400);
        transformation1.setMaxExtend(false);
        MediaUriTransformationProcessor processor = new MediaUriTransformationProcessor();
        processor.add(transformation1);

        // aspect ratio = 4/3
        MediaRepresentation repr1 = makeImageMediaRepresentation(2000, 1500);

        List<MediaRepresentation> representations = processor.makeNewMediaRepresentationsFor(repr1.getParts().get(0));

        assertEquals("https://pictures.bgbm.org/digilib/Scaler/IIIF/Cyprus!Salvia_aethiopis_A1.jpg/400,400/full/0/default.jpg", representations.get(0).getParts().get(0).getUri().toString());
        assertEquals(ImageFile.class, representations.get(0).getParts().get(0).getClass());
        ImageFile image = (ImageFile)representations.get(0).getParts().get(0);
        assertEquals(Integer.valueOf(400), image.getWidth());
        assertEquals(Integer.valueOf(400), image.getHeight());

        // aspect ratio = 3/4
        MediaRepresentation repr2 = makeImageMediaRepresentation(1500, 2000);

        representations = processor.makeNewMediaRepresentationsFor(repr2.getParts().get(0));

        assertEquals("https://pictures.bgbm.org/digilib/Scaler/IIIF/Cyprus!Salvia_aethiopis_A1.jpg/400,400/full/0/default.jpg", representations.get(0).getParts().get(0).getUri().toString());
        assertEquals(ImageFile.class, representations.get(0).getParts().get(0).getClass());
        image = (ImageFile)representations.get(0).getParts().get(0);
        assertEquals(Integer.valueOf(400), image.getWidth());
        assertEquals(Integer.valueOf(400), image.getHeight());
    }

    @Test
    public void testMakeMediaCalculateExtend() throws URISyntaxException {

        MediaUriTransformation transformation1 = new MediaUriTransformation();
        transformation1.setPathQueryFragment(new SearchReplace(PATTERN_1, "digilib/Scaler/IIIF/$1!$2/!400,400/full/0/default.jpg"));
        transformation1.setMimeType("image/jpeg");
        transformation1.setWidth(400);
        transformation1.setHeight(400);
        transformation1.setMaxExtend(true);
        MediaUriTransformationProcessor processor = new MediaUriTransformationProcessor();
        processor.add(transformation1);

        // aspect ratio = 4/3
        MediaRepresentation repr1 = makeImageMediaRepresentation(2000, 1500);

        List<MediaRepresentation> representations = processor.makeNewMediaRepresentationsFor(repr1.getParts().get(0));

        assertEquals("https://pictures.bgbm.org/digilib/Scaler/IIIF/Cyprus!Salvia_aethiopis_A1.jpg/!400,400/full/0/default.jpg", representations.get(0).getParts().get(0).getUri().toString());
        assertEquals(ImageFile.class, representations.get(0).getParts().get(0).getClass());
        ImageFile image = (ImageFile)representations.get(0).getParts().get(0);
        assertEquals(Integer.valueOf(400), image.getWidth());
        assertEquals(Integer.valueOf(300), image.getHeight());

        // aspect ratio = 3/4
        MediaRepresentation repr2 = makeImageMediaRepresentation(1500, 2000);

        representations = processor.makeNewMediaRepresentationsFor(repr2.getParts().get(0));

        assertEquals("https://pictures.bgbm.org/digilib/Scaler/IIIF/Cyprus!Salvia_aethiopis_A1.jpg/!400,400/full/0/default.jpg", representations.get(0).getParts().get(0).getUri().toString());
        assertEquals(ImageFile.class, representations.get(0).getParts().get(0).getClass());
        image = (ImageFile)representations.get(0).getParts().get(0);
        assertEquals(Integer.valueOf(300), image.getWidth());
        assertEquals(Integer.valueOf(400), image.getHeight());
    }

    @Test
    public void testMakeMediaCalculateExtendRect() throws URISyntaxException {

        MediaUriTransformation transformation1 = new MediaUriTransformation();
        transformation1.setPathQueryFragment(new SearchReplace(PATTERN_1, "digilib/Scaler/IIIF/$1!$2/!300,400/full/0/default.jpg"));
        transformation1.setMimeType("image/jpeg");
        transformation1.setWidth(300);
        transformation1.setHeight(400);
        transformation1.setMaxExtend(true);
        MediaUriTransformationProcessor processor = new MediaUriTransformationProcessor();
        processor.add(transformation1);

        // aspect ratio = 4/3
        MediaRepresentation repr1 = makeImageMediaRepresentation(2000, 1500);

        List<MediaRepresentation> representations = processor.makeNewMediaRepresentationsFor(repr1.getParts().get(0));

        assertEquals(ImageFile.class, representations.get(0).getParts().get(0).getClass());
        ImageFile image = (ImageFile)representations.get(0).getParts().get(0);
        assertEquals(Integer.valueOf(300), image.getWidth());
        assertEquals(Integer.valueOf(225), image.getHeight());

        // aspect ratio = 3/4
        MediaRepresentation repr2 = makeImageMediaRepresentation(1500, 2000);

        transformation1.setWidth(400);
        transformation1.setHeight(300);
        transformation1.setMaxExtend(true);

        representations = processor.makeNewMediaRepresentationsFor(repr2.getParts().get(0));

        assertEquals(ImageFile.class, representations.get(0).getParts().get(0).getClass());
        image = (ImageFile)representations.get(0).getParts().get(0);
        assertEquals(Integer.valueOf(225), image.getWidth());
        assertEquals(Integer.valueOf(300), image.getHeight());
    }
}
