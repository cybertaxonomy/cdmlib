package eu.etaxonomy.cdm.model.media;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import eu.etaxonomy.cdm.common.URI;

public class MediaUtilsTest {

    private Media mediaImage1;
    private Media mediaImage2;
    private Media mediaImage3;
    private Media mediaImage4;
    private Media mediaAudio1;
    private MediaRepresentation smallJPGRepresentation;
    private MediaRepresentation bigJPGRepresentation;
    private MediaRepresentation unknownDimensionJPGRepresentation;
    private MediaRepresentation smallPNGRepresentation;
    private MediaRepresentation bigPNGRepresentation;
    private MediaRepresentation bigMP3Representation;

    @Before
    public void setUp() throws Exception {

        ImageFile smallJPG = ImageFile.NewInstance(new URI("http://foo.bar.net/small.JPG"), 200 * 100, 100, 200);
        smallJPGRepresentation = MediaRepresentation.NewInstance("image/jpg", "jpg");
        smallJPGRepresentation.addRepresentationPart(smallJPG);

        ImageFile bigJPG = ImageFile.NewInstance(new URI("http://foo.bar.net/big.JPG"), 2000 * 1000, 1000, 2000);
        bigJPGRepresentation = MediaRepresentation.NewInstance("image/jpg", "jpg");
        bigJPGRepresentation.addRepresentationPart(bigJPG);

        ImageFile unknownDimensionJPG = ImageFile.NewInstance(new URI("http://foo.bar.net/unknownDimension.JPG"), null, null, null);
        unknownDimensionJPGRepresentation = MediaRepresentation.NewInstance("image/jpg", "jpg");
        unknownDimensionJPGRepresentation.addRepresentationPart(unknownDimensionJPG);

        ImageFile smallPNG = ImageFile.NewInstance(new URI("http://foo.bar.net/small.PNG"), 200 * 100, 100, 200);
        smallPNGRepresentation = MediaRepresentation.NewInstance("image/png", "png");
        smallPNGRepresentation.addRepresentationPart(smallPNG);

        ImageFile bigPNG = ImageFile.NewInstance(new URI("http://foo.bar.net/big.PNG"), 2000 * 1000, 1000, 2000);
        bigPNGRepresentation = MediaRepresentation.NewInstance("image/png", "png");
        bigPNGRepresentation.addRepresentationPart(bigPNG);

        AudioFile bigMP3 = AudioFile.NewInstance(new URI("http://foo.bar.net/big.mp3"), 40000);
        bigMP3Representation = MediaRepresentation.NewInstance("audio/mpeg", "mp3");
        bigMP3Representation.addRepresentationPart(bigMP3);


        mediaImage1 = Media.NewInstance();
        mediaImage1.addRepresentation(smallJPGRepresentation);
        mediaImage1.addRepresentation(bigJPGRepresentation);

        mediaImage4 = Media.NewInstance();
        mediaImage4.addRepresentation(smallJPGRepresentation);
        mediaImage4.addRepresentation(bigJPGRepresentation);
        mediaImage4.addRepresentation(unknownDimensionJPGRepresentation);

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

        List<Media> imageList = new ArrayList<>();
        imageList.add(mediaImage1);
        imageList.add(mediaImage2);
        imageList.add(mediaImage3);

        Map<Media, MediaRepresentation> filteredList = MediaUtils.findPreferredMedia(
                imageList, ImageFile.class, null, null, null, null, MediaUtils.MissingValueStrategy.MAX);

        Assert.assertNotNull(findMediaByUUID(filteredList.keySet(), mediaImage1.getUuid()));
        Assert.assertNotNull(findMediaByUUID(filteredList.keySet(), mediaImage2.getUuid()));
        Assert.assertNotNull(findMediaByUUID(filteredList.keySet(), mediaImage3.getUuid()));

        List<Media> mixedMediaList =  new ArrayList<>();
        mixedMediaList.add(mediaImage1);
        mixedMediaList.add(mediaImage2);
        mixedMediaList.add(mediaImage3);
        mixedMediaList.add(mediaAudio1);
        filteredList = MediaUtils.findPreferredMedia(mixedMediaList, null, null, null, null, null, MediaUtils.MissingValueStrategy.MAX);
        Assert.assertNotNull(findMediaByUUID(filteredList.keySet(), mediaImage1.getUuid()));
        Assert.assertNotNull(findMediaByUUID(filteredList.keySet(), mediaImage2.getUuid()));
        Assert.assertNotNull(findMediaByUUID(filteredList.keySet(), mediaImage3.getUuid()));
        Assert.assertNotNull(findMediaByUUID(filteredList.keySet(), mediaAudio1.getUuid()));

        filteredList = MediaUtils.findPreferredMedia(mixedMediaList, AudioFile.class, null, null, null, null, MediaUtils.MissingValueStrategy.MAX);
        Assert.assertNotNull(findMediaByUUID(filteredList.keySet(), mediaAudio1.getUuid()));

        filteredList = MediaUtils.findPreferredMedia(mixedMediaList, ImageFile.class, null, null, null, null, MediaUtils.MissingValueStrategy.MAX);
        Assert.assertNotNull(findMediaByUUID(filteredList.keySet(), mediaImage1.getUuid()));
        Assert.assertNotNull(findMediaByUUID(filteredList.keySet(), mediaImage2.getUuid()));
        Assert.assertNotNull(findMediaByUUID(filteredList.keySet(), mediaImage3.getUuid()));

    }

    @Test
    public void testfindBestMatchingRepresentation() {

        // Logger.getLogger(MediaUtils.class).setLevel(Level.DEBUG);

        String[] mimetypes = {".*"};

        Assert.assertEquals(unknownDimensionJPGRepresentation.getParts().get(0).getUuid(),
                MediaUtils.findBestMatchingRepresentation(
                        mediaImage4, ImageFile.class, null, Integer.MAX_VALUE, Integer.MAX_VALUE, null, MediaUtils.MissingValueStrategy.MAX).getParts().get(0).getUuid()
                );

        Assert.assertEquals(
                bigJPGRepresentation.getUuid(),
                MediaUtils.findBestMatchingRepresentation(
                mediaImage1, null,  null, Integer.MAX_VALUE, Integer.MAX_VALUE, mimetypes, MediaUtils.MissingValueStrategy.MAX).getUuid()
                );

        Assert.assertEquals(smallJPGRepresentation.getUuid(),
                MediaUtils.findBestMatchingRepresentation(
                mediaImage1, null, null, 200, 300, mimetypes, MediaUtils.MissingValueStrategy.MAX).getUuid());
        Assert.assertEquals(bigJPGRepresentation.getUuid(),
                MediaUtils.findBestMatchingRepresentation(
                mediaImage1, null, null, 1500, 1500, mimetypes, MediaUtils.MissingValueStrategy.MAX).getUuid()
                );

        Assert.assertEquals(smallJPGRepresentation.getUuid(),
                MediaUtils.findBestMatchingRepresentation(
                mediaImage1, null, 300, null, null, mimetypes, MediaUtils.MissingValueStrategy.MAX).getUuid()
                );
        Assert.assertEquals(bigJPGRepresentation.getUuid(),
                MediaUtils.findBestMatchingRepresentation(
                mediaImage1, null, bigJPGRepresentation.getParts().get(0).getSize() - 100, null, null, mimetypes, MediaUtils.MissingValueStrategy.MAX).getUuid()
                );
        Assert.assertEquals(bigJPGRepresentation.getUuid(),
                MediaUtils.findBestMatchingRepresentation(
                mediaImage4, null, bigJPGRepresentation.getParts().get(0).getSize() + 2000, null, null, mimetypes, MediaUtils.MissingValueStrategy.MAX).getUuid()
                );


    }

    /**
     * where some images are loading slow, in these cases the algorithm chooses
     * the high quality representation even if the thumbnail size perfectly fits
     * the preferred size
     *
     * Thumbnails with 150x96 available (=> product is 14400) Preferred size
     * 120x120 defined in setting of taxon gallery (=> product is 14400)
     *
     */
    @Test
    public void testIssue7093() throws URISyntaxException {

        // ---------- PhoenixTheophrasti25.jpg

        ImageFile thumbnail = ImageFile.NewInstance(new URI("http://foo.bar.net/issue7093/thumbnail.JPG"), null, 150, 96);
        MediaRepresentation thumbnailRepresentation = MediaRepresentation.NewInstance("image/jpg", "jpg");
        thumbnailRepresentation.addRepresentationPart(thumbnail);

        ImageFile large = ImageFile.NewInstance(new URI("http://foo.bar.net/issue7093/big.JPG"), null, 670, 1122);
        MediaRepresentation largeRepresentation = MediaRepresentation.NewInstance("image/jpg", "jpg");
        largeRepresentation.addRepresentationPart(large);

        ImageFile middle = ImageFile.NewInstance(new URI("http://foo.bar.net/issue7093/middle.JPG"), null, 350,  586);
        MediaRepresentation middleRepresentation = MediaRepresentation.NewInstance("image/jpg", "jpg");
        middleRepresentation.addRepresentationPart(middle);

        Media media = Media.NewInstance();
        media.addRepresentation(largeRepresentation);
        media.addRepresentation(thumbnailRepresentation);

        String[] mimetypes = {".*"};

        Assert.assertEquals(thumbnailRepresentation, MediaUtils.findBestMatchingRepresentation(
                media, null,  null, 120, 120, mimetypes, MediaUtils.MissingValueStrategy.MAX));

        media.addRepresentation(middleRepresentation);

        Assert.assertEquals(thumbnailRepresentation, MediaUtils.findBestMatchingRepresentation(
                media, null,  null, 120, 120, mimetypes, MediaUtils.MissingValueStrategy.MAX));

        // ---- Phoenix_theophrasti_Turland_2009_0019.jpg, ...

        thumbnail = ImageFile.NewInstance(new URI("http://foo.bar.net/issue7093/thumbnail.JPG"), null, 150, 96);
        thumbnailRepresentation = MediaRepresentation.NewInstance("image/jpg", "jpg");
        thumbnailRepresentation.addRepresentationPart(thumbnail);

        large = ImageFile.NewInstance(new URI("http://foo.bar.net/issue7093/big.JPG"), null,  3787, 2535);
        largeRepresentation = MediaRepresentation.NewInstance("image/jpg", "jpg");
        largeRepresentation.addRepresentationPart(large);

        middle = ImageFile.NewInstance(new URI("http://foo.bar.net/issue7093/middle.JPG"), null, 523, 350);
        middleRepresentation = MediaRepresentation.NewInstance("image/jpg", "jpg");
        middleRepresentation.addRepresentationPart(middle);

        media = Media.NewInstance();
        media.addRepresentation(largeRepresentation);
        media.addRepresentation(thumbnailRepresentation);

        Assert.assertEquals(thumbnailRepresentation, MediaUtils.findBestMatchingRepresentation(
                media, null,  null, 120, 120, mimetypes, MediaUtils.MissingValueStrategy.MAX));

        media.addRepresentation(middleRepresentation);

        Assert.assertEquals(thumbnailRepresentation, MediaUtils.findBestMatchingRepresentation(
                media, null,  null, 120, 120, mimetypes, MediaUtils.MissingValueStrategy.MAX));

    }

}
