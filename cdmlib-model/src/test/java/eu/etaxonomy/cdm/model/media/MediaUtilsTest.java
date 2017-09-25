package eu.etaxonomy.cdm.model.media;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;

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

    private Media findMediaByUUID(Collection<Media> mediaList, UUID uuid){
        for(Media media : mediaList){
            if(media.getUuid().equals(uuid)){
                return media;
            }
        }
        return null;
    }

    @Test
    public void testFindPreferredMedia(){

        ArrayList<Media> imageList = new ArrayList<>();
        imageList.add(mediaImage1);
        imageList.add(mediaImage2);
        imageList.add(mediaImage3);

        Map<Media, MediaRepresentation> filteredList = MediaUtils.findPreferredMedia(
                imageList, ImageFile.class, null, null, null, null, null);

        Assert.assertNotNull(findMediaByUUID(filteredList.keySet(), mediaImage1.getUuid()));
        Assert.assertNotNull(findMediaByUUID(filteredList.keySet(), mediaImage2.getUuid()));
        Assert.assertNotNull(findMediaByUUID(filteredList.keySet(), mediaImage3.getUuid()));

        ArrayList<Media> mixedMediaList =  new ArrayList<>();
        mixedMediaList.add(mediaImage1);
        mixedMediaList.add(mediaImage2);
        mixedMediaList.add(mediaImage3);
        mixedMediaList.add(mediaAudio1);
        filteredList = MediaUtils.findPreferredMedia(mixedMediaList, null, null, null, null, null, null);
        Assert.assertNotNull(findMediaByUUID(filteredList.keySet(), mediaImage1.getUuid()));
        Assert.assertNotNull(findMediaByUUID(filteredList.keySet(), mediaImage2.getUuid()));
        Assert.assertNotNull(findMediaByUUID(filteredList.keySet(), mediaImage3.getUuid()));
        Assert.assertNotNull(findMediaByUUID(filteredList.keySet(), mediaAudio1.getUuid()));

        filteredList = MediaUtils.findPreferredMedia(mixedMediaList, AudioFile.class, null, null, null, null, null);
        Assert.assertNotNull(findMediaByUUID(filteredList.keySet(), mediaAudio1.getUuid()));

        filteredList = MediaUtils.findPreferredMedia(mixedMediaList, ImageFile.class, null, null, null, null, null);
        Assert.assertNotNull(findMediaByUUID(filteredList.keySet(), mediaImage1.getUuid()));
        Assert.assertNotNull(findMediaByUUID(filteredList.keySet(), mediaImage2.getUuid()));
        Assert.assertNotNull(findMediaByUUID(filteredList.keySet(), mediaImage3.getUuid()));

    }

    @Test
    public void testfindBestMatchingRepresentation() {

        String[] mimetypes = {".*"};

        Assert.assertEquals(smallJPGRepresentation, MediaUtils.findBestMatchingRepresentation(
                mediaImage1, null, null, 200, 300, mimetypes));
        Assert.assertEquals(bigJPGRepresentation, MediaUtils.findBestMatchingRepresentation(
                mediaImage1, null, null, 1500, 1500, mimetypes));

        Assert.assertEquals(smallJPGRepresentation, MediaUtils.findBestMatchingRepresentation(
                mediaImage1, null, 300, null, null, mimetypes));
        Assert.assertEquals(bigJPGRepresentation, MediaUtils.findBestMatchingRepresentation(
                mediaImage1, null, 1500, null, null, mimetypes));

    }

}
