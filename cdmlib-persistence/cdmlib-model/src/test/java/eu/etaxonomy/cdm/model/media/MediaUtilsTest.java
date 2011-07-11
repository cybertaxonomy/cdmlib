package eu.etaxonomy.cdm.model.media;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class MediaUtilsTest {

    private Media mediaImage1;
    private Media mediaImage2;
    private Media mediaImage3;
    private Media mediaAudio1;
    private MediaRepresentation smallJPGRepresentation;
    private MediaRepresentation bigJPGRepresentation;
    private MediaRepresentation smallPNGRepresentation;
    private MediaRepresentation bigPNGRepresentation;
    private MediaRepresentation bigMP3Representation;

    @Before
    public void setUp() throws Exception {

        ImageFile smallJPG = ImageFile.NewInstance(new URI("http://foo.bar.net/small.JPG"), 200, 100, 200);
        smallJPGRepresentation = MediaRepresentation.NewInstance("image/jpg", "jpg");
        smallJPGRepresentation.addRepresentationPart(smallJPG);

        ImageFile bigJPG = ImageFile.NewInstance(new URI("http://foo.bar.net/big.JPG"), 2000, 1000, 2000);
        bigJPGRepresentation = MediaRepresentation.NewInstance("image/jpg", "jpg");
        bigJPGRepresentation.addRepresentationPart(bigJPG);

        ImageFile smallPNG = ImageFile.NewInstance(new URI("http://foo.bar.net/small.PNG"), 200, 100, 200);
        smallPNGRepresentation = MediaRepresentation.NewInstance("image/png", "png");
        smallPNGRepresentation.addRepresentationPart(smallPNG);

        ImageFile bigPNG = ImageFile.NewInstance(new URI("http://foo.bar.net/big.PNG"), 2000, 1000, 2000);
        bigPNGRepresentation = MediaRepresentation.NewInstance("image/png", "png");
        bigPNGRepresentation.addRepresentationPart(bigPNG);

        AudioFile bigMP3 = AudioFile.NewInstance(new URI("http://foo.bar.net/big.mp3"), 40000);
        bigMP3Representation = MediaRepresentation.NewInstance("audio/mpeg", "mp3");
        bigMP3Representation.addRepresentationPart(bigMP3);


        mediaImage1 = Media.NewInstance();
        mediaImage1.addRepresentation(smallJPGRepresentation);
        mediaImage1.addRepresentation(bigJPGRepresentation);

        mediaImage2 = Media.NewInstance();
        mediaImage2.addRepresentation(smallPNGRepresentation);

        mediaImage3 = Media.NewInstance();
        mediaImage3.addRepresentation(bigPNGRepresentation);

        mediaAudio1 = Media.NewInstance();
        mediaAudio1.addRepresentation(bigMP3Representation);


    }

    @Test
    public void testFindPreferredMedia(){

        ArrayList<Media> imageList = new ArrayList<Media>();
        imageList.add(mediaImage1);
        imageList.add(mediaImage2);
        imageList.add(mediaImage3);

        List<Media> filteredList = MediaUtils.findPreferredMedia(imageList, ImageFile.class, null, null, null, null, null);
        Assert.assertTrue(filteredList.contains(mediaImage1));
        Assert.assertTrue(filteredList.contains(mediaImage2));
        Assert.assertTrue(filteredList.contains(mediaImage3));

        ArrayList<Media> mixedMediaList = (ArrayList<Media>) imageList.clone();
        mixedMediaList.add(mediaAudio1);
        filteredList = MediaUtils.findPreferredMedia(mixedMediaList, ImageFile.class, null, null, null, null, null);
        Assert.assertTrue(filteredList.contains(mediaImage1));
        Assert.assertTrue(filteredList.contains(mediaImage2));
        Assert.assertTrue(filteredList.contains(mediaImage3));
        Assert.assertFalse(filteredList.contains(mediaAudio1));

    }

    @Test
    public void testfindBestMatchingRepresentation() {

        String[] mimetypes = {".*"};

        Assert.assertEquals(smallJPGRepresentation, MediaUtils.findBestMatchingRepresentation(mediaImage1, null, null, 200, 300, mimetypes));
        Assert.assertEquals(bigJPGRepresentation, MediaUtils.findBestMatchingRepresentation(mediaImage1, null, null, 1500, 1500, mimetypes));

        Assert.assertEquals(smallJPGRepresentation, MediaUtils.findBestMatchingRepresentation(mediaImage1, null, 300, null, null, mimetypes));
        Assert.assertEquals(bigJPGRepresentation, MediaUtils.findBestMatchingRepresentation(mediaImage1, null, 1500, null, null, mimetypes));

    }



}
