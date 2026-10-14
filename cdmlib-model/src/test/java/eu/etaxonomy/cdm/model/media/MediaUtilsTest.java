/**
 * Copyright (C) 2007 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
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

        smallJPGRepresentation = ImageFile.NewInstance(new URI("http://foo.bar.net/small.JPG"),
                "image/jpg", "jpg", 200 * 100, 100, 200);

        bigJPGRepresentation = ImageFile.NewInstance(new URI("http://foo.bar.net/big.JPG"),
                "image/jpg", "jpg", 2000 * 1000, 1000, 2000);

        unknownDimensionJPGRepresentation = ImageFile.NewInstance(new URI("http://foo.bar.net/unknownDimension.JPG"),
                "image/jpg", "jpg", null, null, null);

        smallPNGRepresentation = ImageFile.NewInstance(new URI("http://foo.bar.net/small.PNG"),
                "image/png", "png", 200 * 100, 100, 200);

        bigPNGRepresentation = ImageFile.NewInstance(new URI("http://foo.bar.net/big.PNG"),
                "image/png", "png", 2000 * 1000, 1000, 2000);

        bigMP3Representation = AudioFile.NewInstance(new URI("http://foo.bar.net/big.mp3"),
                "audio/mpeg", "mp3", 40000);

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

        // LogUtils.setLevel(MediaUtils.class, Level.DEBUG);

        String[] mimetypes = {".*"};

        Assert.assertEquals(unknownDimensionJPGRepresentation.getUuid(),
                MediaUtils.findBestMatchingRepresentation(
                        mediaImage4, ImageFile.class, null, Integer.MAX_VALUE, Integer.MAX_VALUE, null, MediaUtils.MissingValueStrategy.MAX).getUuid()
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
                mediaImage1, null, bigJPGRepresentation.getSize() - 100, null, null, mimetypes, MediaUtils.MissingValueStrategy.MAX).getUuid()
                );
        Assert.assertEquals(bigJPGRepresentation.getUuid(),
                MediaUtils.findBestMatchingRepresentation(
                mediaImage4, null, bigJPGRepresentation.getSize() + 2000, null, null, mimetypes, MediaUtils.MissingValueStrategy.MAX).getUuid()
                );


    }

    /**
     * where some images are loading slow, in these cases the algorithm chooses
     * Wthe high quality representation even if the thumbnail size perfectly fits
     * the preferred size
     *
     * Thumbnails with 150x96 available (=> product is 14400) Preferred size
     * 120x120 defined in setting of taxon gallery (=> product is 14400)
     *
     */
    @Test
    public void testIssue7093() throws URISyntaxException {

        // ---------- PhoenixTheophrasti25.jpg

        ImageFile thumbnailRepresentation = ImageFile.NewInstance(new URI("http://foo.bar.net/issue7093/thumbnail.JPG"),
                "image/jpg", "jpg", null, 150, 96);

        ImageFile largeRepresentation = ImageFile.NewInstance(new URI("http://foo.bar.net/issue7093/big.JPG"),
                "image/jpg", "jpg", null, 670, 1122);

        ImageFile middleRepresentation = ImageFile.NewInstance(new URI("http://foo.bar.net/issue7093/middle.JPG"),
                "image/jpg", "jpg", null, 350,  586);

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

        thumbnailRepresentation = ImageFile.NewInstance(new URI("http://foo.bar.net/issue7093/thumbnail.JPG"),
                "image/jpg", "jpg", null, 150, 96);

        largeRepresentation = ImageFile.NewInstance(new URI("http://foo.bar.net/issue7093/big.JPG"),
                "image/jpg", "jpg", null,  3787, 2535);

        middleRepresentation = ImageFile.NewInstance(new URI("http://foo.bar.net/issue7093/middle.JPG"),
                "image/jpg", "jpg", null, 523, 350);

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