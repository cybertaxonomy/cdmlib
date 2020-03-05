package eu.etaxonomy.cdm.model.media;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.model.common.CdmBase;

public class MediaUtils {

    private static final Logger logger = Logger.getLogger(MediaUtils.class);


    /**
     *
     * @param media
     * @param representationPartType
     * @param size
     * @param height
     * @param widthOrDuration
     * @param mimeTypes
     * @param missingValStrategy
     * @return
     */
    public static MediaRepresentation findBestMatchingRepresentation(Media media,
            Class<? extends MediaRepresentationPart> representationPartType, Integer size, Integer height,
            Integer widthOrDuration, String[] mimeTypes, MissingValueStrategy missingValStrategy){

        // find best matching representations of each media
        SortedMap<Long, MediaRepresentation> prefRepresentations
                = filterAndOrderMediaRepresentations(media.getRepresentations(), representationPartType, mimeTypes,
                        size, widthOrDuration, height, missingValStrategy);
        if(prefRepresentations.size() > 0){
            MediaRepresentation prefOne = prefRepresentations.get(prefRepresentations.firstKey());
            return prefOne;
        }
        return null;
    }

    /**
     * Return the first {@link MediaRepresentationPart} found for the given {@link Media}
     * or <code>null</code> otherwise.
     * @param media the media which is searched for the first part
     * @return the first part found or <code>null</code>
     */
    public static MediaRepresentationPart getFirstMediaRepresentationPart(Media media){
        if(media==null){
            return null;
        }
        MediaRepresentationPart mediaRepresentationPart = null;
        Set<MediaRepresentation> representations = media.getRepresentations();
        if(representations!=null && representations.size()>0){
            MediaRepresentation mediaRepresentation = representations.iterator().next();
            List<MediaRepresentationPart> parts = mediaRepresentation.getParts();
            if(parts!=null && parts.size()>0){
                mediaRepresentationPart = parts.iterator().next();
            }
        }
        return mediaRepresentationPart;
    }

    /**
     * Filters the given List of Media by the supplied filter parameters <code>representationPartType</code>,
     * <code>mimeTypes</code>, <code>widthOrDuration</code>, <code>height</code>, <code>size</code>.
     * Only best matching MediaRepresentation remains attached to the Media entities.
     * A Media entity may be completely omitted in the resulting list if  {@link #filterAndOrderMediaRepresentations(Set, Class, String[], Integer, Integer, Integer)}
     * is not returning any matching representation. This can be the case if a <code>representationPartType</code> is supplied.
     *
     * @param mediaList
     * @param representationPartType any subclass of {@link MediaRepresentationPart}
     * @param mimeTypes
     * @param widthOrDuration
     * @param height
     * @param size
     * @return
     */
    public static Map<Media, MediaRepresentation> findPreferredMedia(List<Media> mediaList,
            Class<? extends MediaRepresentationPart> representationPartType, String[] mimeTypes, Integer widthOrDuration,
            Integer height, Integer size, MissingValueStrategy missingValStrat) {

        if(mimeTypes != null) {
            for(int i=0; i<mimeTypes.length; i++){
                mimeTypes[i] = mimeTypes[i].replace(':', '/');
            }
        }

        Map<Media, MediaRepresentation> returnMediaList;
        if(mediaList != null){
            returnMediaList = new LinkedHashMap<>(mediaList.size());
            for(Media media : mediaList){

                Set<MediaRepresentation> candidateRepresentations = new LinkedHashSet<>();
                candidateRepresentations.addAll(media.getRepresentations());

                SortedMap<Long, MediaRepresentation> prefRepresentations
                    = filterAndOrderMediaRepresentations(candidateRepresentations, representationPartType,
                            mimeTypes, size, widthOrDuration, height, missingValStrat);

                if(prefRepresentations.size() > 0){
                    // Media.representations is a set
                    // so it cannot retain the sorting which has been found by filterAndOrderMediaRepresentations()
                    // thus we take first one and remove all other representations
                    returnMediaList.put(media, prefRepresentations.get(prefRepresentations.firstKey()));
                }

            }
        }
        else{
            returnMediaList = new HashMap<>();
        }
        return returnMediaList;
    }

    /**
     * @see also cdm-dataportal: cdm-api.module#cdm_preferred_media_representations()
     *
     * @param mediaRepresentations
     * @param representationPartType
     * @param mimeTypeRegexes
     * @param size
     *  Applies to all {@link MediaRepresentationPart}s (value = <code>null</code> means ignore, for maximum size use {@link Integer#MAX_VALUE})
     * @param widthOrDuration
     *   Applied to {@link ImageFile#getWidth()}, or {@link {@link MovieFile#getDuration()},
     *   or {@link {@link AudioFile#getDuration()} (value = <code>null</code> means ignore,
     *   for maximum use {@link Integer#MAX_VALUE})
     * @param height
     *   The height is only applied to {@link ImageFile}s (value = <code>null</code> means ignore,
     *   for maximum height use {@link Integer#MAX_VALUE})
     * @return
     */
    public static SortedMap<Long, MediaRepresentation> filterAndOrderMediaRepresentations(
            Set<MediaRepresentation> mediaRepresentations,
            Class<? extends MediaRepresentationPart> representationPartType, String[] mimeTypeRegexes,
            Integer size, Integer widthOrDuration, Integer height,
            MissingValueStrategy missingValStrat) {

        SortedMap<Long, MediaRepresentation> prefRepr = new TreeMap<>();

        Dimension preferredImageDimensions = dimensionsFilter(widthOrDuration, height, null);
        long preferredExpansion = expanse(preferredImageDimensions);
        logger.debug("preferredExpansion: " + preferredExpansion);

        mimeTypeRegexes = (mimeTypeRegexes == null ? new String[]{".*"} : mimeTypeRegexes);

        for (String mimeTypeRegex : mimeTypeRegexes) {
            // getRepresentationByMimeType
            Pattern mimeTypePattern = Pattern.compile(mimeTypeRegex);
            int representationCnt = 0;
            for (MediaRepresentation representation : mediaRepresentations) {

                List<MediaRepresentationPart> matchingParts = new ArrayList<>();


                // check MIME type
                boolean isMimeTypeMatch = representation.getMimeType() == null
                        || mimeTypePattern.matcher(representation.getMimeType()).matches();
                if(logger.isDebugEnabled()){
                    logger.debug("isMimeTypeMatch: " + Boolean.valueOf(isMimeTypeMatch).toString());
                }

                long dimensionsDeltaAllParts = 0;

                //first the size is used for comparison
                for (MediaRepresentationPart part : representation.getParts()) {

                    // check representationPartType
                    boolean isRepresentationPartTypeMatch = representationPartType == null
                            || part.getClass().isAssignableFrom(representationPartType);
                    if(logger.isDebugEnabled()){
                        logger.debug("isRepresentationPartTypeMatch: " + Boolean.valueOf(isRepresentationPartTypeMatch).toString());
                    }

                    if ( !(isRepresentationPartTypeMatch && isMimeTypeMatch) ) {
                        continue;
                    }

                    if(logger.isDebugEnabled()){
                        logger.debug(part + " matches");
                    }
                    matchingParts.add(part);

                    if (size != null && missingValStrat.applyTo(part.getSize()) != null){
                        int sizeOfPart = missingValStrat.applyTo(part.getSize());
                        int distance = sizeOfPart - size;
                        if (distance < 0) {
                            distance *= -1;
                        }
                        dimensionsDeltaAllParts += distance;

                    }

                    //if height and width/duration is defined, add this information, too
                    if (preferredImageDimensions != null || widthOrDuration != null){
                        long expansionDelta = 0;
                        if (part.isInstanceOf(ImageFile.class)) {
                            if (preferredImageDimensions != null){
                                ImageFile image = CdmBase.deproxy(part, ImageFile.class);
                                Dimension imageDimension = dimensionsFilter(image.getWidth(), image.getHeight(), missingValStrat);
                                if (imageDimension != null){
                                    expansionDelta = Math.abs(expanse(imageDimension) - preferredExpansion);
                                    logger.debug("#### " +  preferredExpansion + " - " +  expanse(imageDimension) + " = " + (preferredExpansion - expanse(imageDimension)));
                                }
                                if(logger.isDebugEnabled()){
                                    if(logger.isDebugEnabled()){
                                        logger.debug("part [" + part.getUri() + "; " + imageDimension + "] : preferredImageDimensions= " + preferredImageDimensions + ", size= "  + size+ " >>" + expansionDelta );
                                    }
                                }
                            }
                        }
                        else if (part.isInstanceOf(MovieFile.class)){
                                MovieFile movie = CdmBase.deproxy(part, MovieFile.class);
                             if(widthOrDuration != null){
                                expansionDelta = missingValStrat.applyTo(movie.getDuration()) - widthOrDuration;
                            }
                             if(logger.isDebugEnabled()){
                                 logger.debug("part MovieFile[" + part.getUri() + "; duration=" + missingValStrat.applyTo(movie.getDuration()) + "] : preferrdDuration= " + widthOrDuration + ", size= "  + size+ " >>" + expansionDelta );
                             }
                        } else if (part.isInstanceOf(AudioFile.class)){
                            AudioFile audio = CdmBase.deproxy(part, AudioFile.class);
                            if(widthOrDuration != null) {
                                expansionDelta = missingValStrat.applyTo(audio.getDuration()) - widthOrDuration;
                            }
                            if(logger.isDebugEnabled()){
                                logger.debug("part AudioFile[" + part.getUri() + "; duration=" + missingValStrat.applyTo(audio.getDuration()) + "] : preferrdDuration= " + widthOrDuration + ", size= "  + size + " >>" + expansionDelta );
                            }
                        }
                        // the expansionDelta is summed up since the parts together for the whole
                        // which is bigger than only a part. By simply summing up images splitted
                        // into parts have too much weight compared to the single image but since
                        // parts are not used at all this is currently not a problem
                        dimensionsDeltaAllParts += expansionDelta;

                    }
                } // loop parts
                logger.debug("matchingParts.size():" + matchingParts.size());
                if(matchingParts.size() > 0 ){
                    representation.getParts().clear();
                    representation.getParts().addAll(matchingParts);
                    prefRepr.put((dimensionsDeltaAllParts + representationCnt++), representation);
                }
            } // loop representations
        } // loop mime types
        logger.debug(prefRepr.size() + " preferred representations found");


        return prefRepr;

    }

    /**
     * @param imageDimension
     * @return
     */
    static long expanse(Dimension imageDimension) {
        if(imageDimension != null){
            if(imageDimension.height >= Integer.MAX_VALUE-1 && imageDimension.width >= Integer.MAX_VALUE-1){
                return Long.MAX_VALUE;
            } else {
                return imageDimension.height * imageDimension.width;
            }
        } else {
            return -1;
        }
    }

    /**
     * @param widthOrDuration
     * @param height
     * @param mvs Will be applied when both, width and height, are <code>null</code>
     */
    static Dimension dimensionsFilter(Integer width, Integer height, MissingValueStrategy mvs) {
        Dimension imageDimensions = null;
        if(height != null || width != null){
            imageDimensions = new Dimension();
            if (height != null && width == null){
                imageDimensions.setSize(1, height);
            } else if(height == null && width != null) {
                imageDimensions.setSize(width, 1); // --> height will be respected and width is ignored
            } else {
                imageDimensions.setSize(width, height);
            }
        } else {
            // both, width and height, are null
            if(mvs != null){
                imageDimensions = new Dimension(mvs.applyTo(null), mvs.applyTo(null));
            }
        }
        return imageDimensions;
    }

    /**
     * Strategies for replacing <code>null</code> values with a numeric value.
     *
     * @author a.kohlbecker
     */
    public enum MissingValueStrategy {
        /**
         * replace <code>null</code> by {@link Integer#MAX_VALUE}
         */
        MAX(Integer.MAX_VALUE),
        /**
         * replace <code>null</code> by <code>0</code>
         */
        ZERO(0);

        private Integer defaultValue;

        MissingValueStrategy(Integer defaultValue){
            this.defaultValue = defaultValue;
        }

        public Integer applyTo(Integer val){
            if(val == null){
                return defaultValue;
            } else {
                return val;
            }
        }
    }
  }
