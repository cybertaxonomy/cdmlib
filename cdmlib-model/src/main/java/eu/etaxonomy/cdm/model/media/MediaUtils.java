package eu.etaxonomy.cdm.model.media;

import java.awt.Dimension;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import eu.etaxonomy.cdm.common.URI;
import eu.etaxonomy.cdm.model.common.CdmBase;

public class MediaUtils {

    private static final Logger logger = LogManager.getLogger();


    public static MediaRepresentation findBestMatchingRepresentation(Media media,
            Class<? extends MediaRepresentation> representationType, Integer size, Integer height,
            Integer widthOrDuration, String[] mimeTypes, MissingValueStrategy missingValStrategy){

        // find best matching representations of each media
        Set<MediaRepresentation> representations = media.getRepresentations();
        return findBestMatchingRepresentation(representations, representationType, size, height, widthOrDuration,
                mimeTypes, missingValStrategy);
    }

    public static MediaRepresentation findBestMatchingRepresentation(
            Set<MediaRepresentation> representations, Class<? extends MediaRepresentation> representationType, Integer size,
            Integer height, Integer widthOrDuration, String[] mimeTypes,
            MissingValueStrategy missingValStrategy) {

        SortedMap<Long, MediaRepresentation> prefRepresentations
                = filterAndOrderMediaRepresentations(representations, representationType, mimeTypes,
                        size, widthOrDuration, height, missingValStrategy);
        if(prefRepresentations.size() > 0){
            MediaRepresentation prefOne = prefRepresentations.get(prefRepresentations.firstKey());
            return prefOne;
        }
        return null;
    }

    /**
     * Return the first {@link MediaRepresentation} found for the given {@link Media}
     * or <code>null</code> otherwise.
     * @param media the media which is searched for the first representation
     * @return the first representation found or <code>null</code>
     */
    public static MediaRepresentation getFirstMediaRepresentationPart(Media media){
        if(media==null){
            return null;
        }
        Set<MediaRepresentation> representations = media.getRepresentations();
        if(representations!=null && representations.size()>0){
            return representations.iterator().next();
        }
        return null;
    }

    /**
     * Creates one single {@link MediaRepresentation} for the given {@link Media}
     * if it does not already exists. Otherwise the first representation found is returned.<br>
     * @param media the media for which the representation should be created
     * @return the first or newly created representation
     */
    public static MediaRepresentation initFirstMediaRepresentationPart(Media media, boolean isImage) {
        MediaRepresentation mediaRepresentation = getFirstMediaRepresentationPart(media);
        if(mediaRepresentation==null){
            if(isImage){
                mediaRepresentation = ImageFile.NewInstance((URI)null, (Integer)null);
            }
            else{
                mediaRepresentation = MediaRepresentation.NewInstance();
            }
            media.addRepresentation(mediaRepresentation);
        }
        return mediaRepresentation;
    }

    /**
     * Filters the given List of Media by the supplied filter parameters <code>representationType</code>,
     * <code>mimeTypes</code>, <code>widthOrDuration</code>, <code>height</code>, <code>size</code>.
     * Only best matching MediaRepresentation remains attached to the Media entities.
     * A Media entity may be completely omitted in the resulting list if  {@link #filterAndOrderMediaRepresentations(Set, Class, String[], Integer, Integer, Integer)}
     * is not returning any matching representation. This can be the case if a <code>representationType</code> is supplied.
     *
     * @param mediaList
     * @param representationType any subclass of {@link MediaRepresentation}
     * @param mimeTypes
     * @param widthOrDuration
     * @param height
     * @param size
     * @return
     */
    public static Map<Media, MediaRepresentation> findPreferredMedia(List<Media> mediaList,
            Class<? extends MediaRepresentation> representationType, String[] mimeTypes, Integer widthOrDuration,
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
                    = filterAndOrderMediaRepresentations(candidateRepresentations, representationType,
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
     * @param representationType
     * @param mimeTypeRegexes
     * @param size
     *  Applies to all {@link MediaRepresentation}s (value = <code>null</code> means ignore, for maximum size use {@link Integer#MAX_VALUE})
     * @param widthOrDuration
     *   Applied to {@link ImageFile#getWidth()}, or {@link MovieFile#getDuration()},
     *   or {@link AudioFile#getDuration()} (value = <code>null</code> means ignore,
     *   for maximum use {@link Integer#MAX_VALUE})
     * @param height
     *   The height is only applied to {@link ImageFile}s (value = <code>null</code> means ignore,
     *   for maximum height use {@link Integer#MAX_VALUE})
     * @return
     */
    public static SortedMap<Long, MediaRepresentation> filterAndOrderMediaRepresentations(
            Set<MediaRepresentation> mediaRepresentations,
            Class<? extends MediaRepresentation> representationType, String[] mimeTypeRegexes,
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

                // check MIME type
                boolean isMimeTypeMatch = representation.getMimeType() == null
                        || mimeTypePattern.matcher(representation.getMimeType()).matches();
                if(logger.isDebugEnabled()){
                    logger.debug("isMimeTypeMatch: " + Boolean.valueOf(isMimeTypeMatch).toString());
                }

                // check representationType
                boolean isRepresentationTypeMatch = representationType == null
                        || representation.getClass().isAssignableFrom(representationType);
                if(logger.isDebugEnabled()){
                    logger.debug("isRepresentationTypeMatch: " + Boolean.valueOf(isRepresentationTypeMatch).toString());
                }

                if ( !(isRepresentationTypeMatch && isMimeTypeMatch) ) {
                    continue;
                }

                if(logger.isDebugEnabled()){
                    logger.debug(representation + " matches");
                }

                long dimensionsDelta = 0;

                Integer sizeOfRepr = representation.getSize();
                if(isUndefined(sizeOfRepr)){
                    sizeOfRepr = missingValStrat.applyTo(sizeOfRepr);
                }
                if (size != null && sizeOfRepr != null){
                    int distance = sizeOfRepr - size;
                    if (distance < 0) {
                        distance *= -1;
                    }
                    dimensionsDelta += distance;

                }

                //if height and width/duration is defined, add this information, too
                if (preferredImageDimensions != null || widthOrDuration != null){
                    long expansionDelta = 0;
                    if (representation.isInstanceOf(ImageFile.class)) {
                        if (preferredImageDimensions != null){
                            ImageFile image = CdmBase.deproxy(representation, ImageFile.class);
                            Dimension imageDimension = dimensionsFilter(image.getWidth(), image.getHeight(), missingValStrat);
                            if (imageDimension != null){
                                expansionDelta = Math.abs(expanse(imageDimension) - preferredExpansion);
                            }
                            if(logger.isDebugEnabled()){
                                logger.debug("repr [" + representation.getUri() + "; " + imageDimension + "] : preferredImageDimensions= " + preferredImageDimensions + ", size= "  + size+ " >>" + expansionDelta );
                            }
                        }
                    }
                    else if (representation.isInstanceOf(MovieFile.class)){
                         MovieFile movie = CdmBase.deproxy(representation, MovieFile.class);
                         Integer durationOfMovie = movie.getDuration();
                         if(isUndefined(durationOfMovie)){
                             durationOfMovie = null; // convert potential 0 to null!
                         }
                         durationOfMovie = missingValStrat.applyTo(durationOfMovie);
                         if(widthOrDuration != null){
                            expansionDelta = durationOfMovie - widthOrDuration;
                        }
                         if(logger.isDebugEnabled()){
                             logger.debug("repr MovieFile[" + representation.getUri() + "; duration=" + movie.getDuration() + "-> " + durationOfMovie + "] : preferrdDuration= " + widthOrDuration + ", size= "  + size+ " >>" + expansionDelta );
                         }
                    } else if (representation.isInstanceOf(AudioFile.class)){
                        AudioFile audio = CdmBase.deproxy(representation, AudioFile.class);
                        Integer durationOfAudio = audio.getDuration();
                        if(isUndefined(durationOfAudio)){
                            durationOfAudio = null;  // convert potential 0 to null!
                        }
                        durationOfAudio = missingValStrat.applyTo(durationOfAudio);
                        if(widthOrDuration != null) {
                            expansionDelta = durationOfAudio - widthOrDuration;
                        }
                        if(logger.isDebugEnabled()){
                            logger.debug("repr AudioFile[" + representation.getUri() + "; duration=" +  audio.getDuration() + "-> " + durationOfAudio + "] : preferrdDuration= " + widthOrDuration + ", size= "  + size + " >>" + expansionDelta );
                        }
                    }
                    dimensionsDelta += expansionDelta;

                }
                prefRepr.put((dimensionsDelta + representationCnt++), representation);
            } // loop representations
        } // loop mime types
        if(logger.isDebugEnabled()){
            String text =  prefRepr.keySet().stream()
            .map(key -> key + ": " + (prefRepr.get(key).getUri() == null ? "null" : prefRepr.get(key).getUri().toString()))
            .collect(Collectors.joining(", ", "{", "}"));
            logger.debug("resulting representations: " + text);
        }

        return prefRepr;
    }

    static long expanse(Dimension imageDimension) {
        if(imageDimension != null){
            return (long)imageDimension.height * (long)imageDimension.width;
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
        if(!isUndefined(height) || !isUndefined(width)){
            imageDimensions = new Dimension();
            if (!isUndefined(height) && isUndefined(width)){
                imageDimensions.setSize(1, height);
            } else if(isUndefined(height) && !isUndefined(width)) {
                imageDimensions.setSize(width, 1); // --> height will be respected and width is ignored
            } else {
                imageDimensions.setSize(width, height);
            }
        } else {
            // both, width and height, are undefined

            if(mvs != null){
                // set both values to null so that the MissingValueStrategy can be applied
                // the MissingValueStrategy only get effective when the supplied value  is NULL
                width = null;
                height = null;
                imageDimensions = new Dimension(mvs.applyTo(width), mvs.applyTo(height));
            }
        }
        return imageDimensions;
    }

    static private boolean isUndefined(Integer val) {
        return val == null || val == 0;
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
