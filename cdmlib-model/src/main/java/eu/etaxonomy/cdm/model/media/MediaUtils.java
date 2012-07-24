package eu.etaxonomy.cdm.model.media;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

public class MediaUtils {

    private static final Logger logger = Logger.getLogger(MediaUtils.class);

    /**
     * @param representationPartType TODO
     * @param size
     * @param height
     * @param widthOrDuration
     * @param mimeTypeRegexes
     * @return
     *
     *
     */
    public static MediaRepresentation findBestMatchingRepresentation(Media media, Class<? extends MediaRepresentationPart> representationPartType, Integer size, Integer height, Integer widthOrDuration, String[] mimeTypes){
        // find best matching representations of each media
        SortedMap<Integer, MediaRepresentation> prefRepresentations
            = filterAndOrderMediaRepresentations(media.getRepresentations(), null, mimeTypes, size, widthOrDuration, height);
            try {
                // take first one and remove all other representations
                MediaRepresentation prefOne = prefRepresentations.get(prefRepresentations.firstKey());

                return prefOne;

            } catch (NoSuchElementException nse) {
                /* IGNORE */
            }
            return null;
        }

    /**
     *
     * @param mediaList
     * @param representationPartType TODO
     * @param mimeTypes
     * @param sizeTokens
     * @param widthOrDuration
     * @param height
     * @param size
     * @return
     */
    public static List<Media> findPreferredMedia(List<Media> mediaList,
            Class<? extends MediaRepresentationPart> representationPartType, String[] mimeTypes, String[] sizeTokens,
            Integer widthOrDuration, Integer height, Integer size) {

        if(mimeTypes != null) {
            for(int i=0; i<mimeTypes.length; i++){
                mimeTypes[i] = mimeTypes[i].replace(':', '/');
            }
        }

        if(sizeTokens != null) {
            if(sizeTokens.length > 0){
                try {
                    size = Integer.valueOf(sizeTokens[0]);
                } catch (NumberFormatException nfe) {
                    /* IGNORE */
                }
            }
            if(sizeTokens.length > 1){
                try {
                    widthOrDuration = Integer.valueOf(sizeTokens[1]);
                } catch (NumberFormatException nfe) {
                    /* IGNORE */
                }
            }
            if(sizeTokens.length > 2){
                try {
                    height = Integer.valueOf(sizeTokens[2]);
                } catch (NumberFormatException nfe) {
                    /* IGNORE */
                }
            }
        }

        List<Media> returnMediaList = new ArrayList<Media>(mediaList.size());
        if(mediaList != null){
            for(Media media : mediaList){

                // media objects will modifies by this method, so
                // we clone the medias in order to prevent them
                // from being stores accidentally
                // cloning will remove the id
                try {
                    media = (Media) media.clone();
                } catch (CloneNotSupportedException e) {
                    // should never happen
                    logger.error(e);
                }

                Set<MediaRepresentation> candidateRepresentations = new HashSet<MediaRepresentation>();
                candidateRepresentations.addAll(media.getRepresentations());

                SortedMap<Integer, MediaRepresentation> prefRepresentations
                    = filterAndOrderMediaRepresentations(candidateRepresentations, representationPartType, mimeTypes, size, widthOrDuration, height);
                try {
                    if(prefRepresentations.size() > 0){
                        // Media.representations is a set
                        // so it cannot retain the sorting which has been found by filterAndOrderMediaRepresentations()
                        // thus we take first one and remove all other representations

                        media.getRepresentations().clear();
                        media.addRepresentation(prefRepresentations.get(prefRepresentations.firstKey()));
                        returnMediaList.add(media);
                    }
                } catch (NoSuchElementException nse) {
                    logger.debug(nse);
                    /* IGNORE */
                }

            }
        }
        return returnMediaList;
    }

    /**
     * @param media
     * @param mimeTypeRegexes
     * @param size
     * @param widthOrDuration
     * @param height
     * @return
     *
     * TODO move into a media utils class
     * TODO implement the quality filter

    public static SortedMap<String, MediaRepresentation> orderMediaRepresentations(Media media, String[] mimeTypeRegexes,
            Integer size, Integer widthOrDuration, Integer height) {
        SortedMap<String, MediaRepresentation> prefRepr = new TreeMap<String, MediaRepresentation>();
        for (String mimeTypeRegex : mimeTypeRegexes) {
            // getRepresentationByMimeType
            Pattern mimeTypePattern = Pattern.compile(mimeTypeRegex);
            int representationCnt = 0;
            for (MediaRepresentation representation : media.getRepresentations()) {
                int dwa = 0;
                if(representation.getMimeType() == null){
                    prefRepr.put((dwa + representationCnt++) + "_NA", representation);
                } else {
                    Matcher mather = mimeTypePattern.matcher(representation.getMimeType());
                    if (mather.matches()) {

                        /* TODO the quality filter part is being skipped
                         * // look for representation with the best matching parts
                        for (MediaRepresentationPart part : representation.getParts()) {
                            if (part instanceof ImageFile) {
                                ImageFile image = (ImageFile) part;
                                int dw = image.getWidth() * image.getHeight() - height * widthOrDuration;
                                if (dw < 0) {
                                    dw *= -1;
                                }
                                dwa += dw;
                            }
                            dwa = (representation.getParts().size() > 0 ? dwa / representation.getParts().size() : 0);
                        }
                        prefRepr.put((dwa + representationCnt++) + '_' + representation.getMimeType(), representation);

                        // preferred mime type found => end loop
                        break;
                    }
                }
            }
        }
        return prefRepr;
    }

    */
    /**
     * @param mimeTypeRegexes
     * @param size
     * @param widthOrDuration
     * @param height
     * @return
     *
     *
     */
    private static SortedMap<Integer, MediaRepresentation> filterAndOrderMediaRepresentations(Set<MediaRepresentation> mediaRepresentations,
            Class<? extends MediaRepresentationPart> representationPartType, String[] mimeTypeRegexes,
            Integer size, Integer widthOrDuration, Integer height) {

        SortedMap<Integer, MediaRepresentation> prefRepr = new TreeMap<Integer, MediaRepresentation>();


        size = (size == null ? new Integer(0) : size );
        widthOrDuration = (widthOrDuration == null ? new Integer(0) : widthOrDuration);
        height = (height == null ? new Integer(0) : height);
        mimeTypeRegexes = (mimeTypeRegexes == null ? new String[]{".*"} : mimeTypeRegexes);

        for (String mimeTypeRegex : mimeTypeRegexes) {
            // getRepresentationByMimeType
            Pattern mimeTypePattern = Pattern.compile(mimeTypeRegex);
            int representationCnt = 0;
            for (MediaRepresentation representation : mediaRepresentations) {

                List<MediaRepresentationPart> matchingParts = new ArrayList<MediaRepresentationPart>();


                // check MIME type
                boolean mimeTypeOK = representation.getMimeType() == null || mimeTypePattern.matcher(representation.getMimeType()).matches();
                logger.debug("mimeTypeOK: " + Boolean.valueOf(mimeTypeOK).toString());

                int dwa = 0;


                //first the size is used for comparison
                for (MediaRepresentationPart part : representation.getParts()) {

                    // check representationPartType
                    boolean representationPartTypeOK = representationPartType == null || part.getClass().isAssignableFrom(representationPartType);
                    logger.debug("representationPartTypeOK: " + Boolean.valueOf(representationPartTypeOK).toString());

                    if ( !(representationPartTypeOK && mimeTypeOK) ) {
                        continue;
                    }

                    logger.debug(part + " matches");
                    matchingParts.add(part);

                    if (part.getSize()!= null){
                        int sizeOfPart = part.getSize();
                        int distance = sizeOfPart - size;
                        if (distance < 0) {
                            distance*= -1;
                        }
                        dwa += distance;
                    }

                    //if height and width/duration is defined, add this information, too
                    if (height != 0 && widthOrDuration != 0){
                        int durationWidthWeight = 0;

                        if (part instanceof ImageFile) {
                            ImageFile image = (ImageFile) part;
                            durationWidthWeight = image.getWidth() * image.getHeight() - height * widthOrDuration;
                        }
                        else if (part instanceof MovieFile){
                            MovieFile movie = (MovieFile) part;
                            durationWidthWeight = movie.getDuration() - widthOrDuration;

                        }else if (part instanceof AudioFile){
                            AudioFile audio = (AudioFile) part;
                            durationWidthWeight = audio.getDuration() - widthOrDuration;

                        }
                        if (durationWidthWeight < 0) {
                            durationWidthWeight *= -1;
                        }
                        dwa += durationWidthWeight;

                    }
                } // loop parts
                logger.debug("matchingParts.size():" + matchingParts.size());
                if(matchingParts.size() > 0 ){
                    dwa = dwa / matchingParts.size();

                    representation.getParts().clear();
                    representation.getParts().addAll(matchingParts);

                    //keyString =(dwa + representationCnt++) + '_' + representation.getMimeType();

                    prefRepr.put((dwa + representationCnt++), representation);
                }
            } // loop representations
        } // loop mime types
        logger.debug(prefRepr.size() + " preferred representations found");
        return prefRepr;
    }
}
