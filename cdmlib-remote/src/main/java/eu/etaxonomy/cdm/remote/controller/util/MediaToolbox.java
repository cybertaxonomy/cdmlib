/**
* Copyright (C) 2020 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.remote.controller.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;

import eu.etaxonomy.cdm.api.service.IPreferenceService;
import eu.etaxonomy.cdm.api.service.media.DefaultMediaTransformations;
import eu.etaxonomy.cdm.api.service.media.MediaUriTransformation;
import eu.etaxonomy.cdm.api.service.media.MediaUriTransformationProcessor;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.media.MediaRepresentation;
import eu.etaxonomy.cdm.model.media.MediaRepresentationPart;
import eu.etaxonomy.cdm.model.media.MediaUtils;
import eu.etaxonomy.cdm.model.media.MediaUtils.MissingValueStrategy;
import eu.etaxonomy.cdm.model.metadata.CdmPreference;
import eu.etaxonomy.cdm.model.metadata.CdmPreference.PrefKey;
import eu.etaxonomy.cdm.model.metadata.PreferencePredicate;
import eu.etaxonomy.cdm.model.metadata.PreferenceSubject;

/**
 * Utility service which creates volatile objects which must not be persisted.
 * <p>
 * By now this class provides methods for filtering {@link Media} and {@link MediaRepresentation}s
 * <p>
 * See also {@link MediaUriTransformationProcessor} and {@link MediaUriTransformation}
 *
 * @author a.kohlbecker
 * @since Jul 8, 2020
 */
@Component // not used for component scan, see eu.etaxonomy.cdm.remote.config.CdmRemoteConfiguration
public class MediaToolbox implements IMediaToolbox {

    private static final Logger logger = Logger.getLogger(MediaToolbox.class);

    private List<MediaUriTransformation> transformations = null;

    private Integer mediaRepresentationTransformationsLastHash = null;

    @Autowired
    private IPreferenceService preferenceService;

    @Override
    public List<Media> processAndFilterPreferredMediaRepresentations(Class<? extends MediaRepresentationPart> type, String[] mimeTypes,
            Integer widthOrDuration, Integer height, Integer size, List<Media> mediaList) {

        MediaUriTransformationProcessor mediaTransformationProcessor = new MediaUriTransformationProcessor();
        mediaTransformationProcessor.addAll(readTransformations());

        for(Media media : mediaList) {
            List<MediaRepresentation> newReprs = new ArrayList<>();
            for(MediaRepresentation repr : media.getRepresentations()) {
                for(MediaRepresentationPart part : repr.getParts()) {
                    newReprs.addAll(mediaTransformationProcessor.makeNewMediaRepresentationsFor(part));
                }
            }
            for(MediaRepresentation r : newReprs) {
                media.addRepresentation(r);
            }
            media.setId(0); // prevent from persisting the modified media entity accidentally
        }
        return filterPreferredMediaRepresentations(mediaList, type, mimeTypes, widthOrDuration, height, size);
    }

    @Override
    public MediaRepresentation processAndFindBestMatchingRepresentation(Media media,
            Class<? extends MediaRepresentationPart> type, Integer size, Integer height, Integer widthOrDuration,
            String[] mimeTypes, MissingValueStrategy missingValStrategy) {

        MediaUriTransformationProcessor mediaTransformationProcessor = new MediaUriTransformationProcessor();
        mediaTransformationProcessor.addAll(readTransformations());

        Set<MediaRepresentation> newReprs = new HashSet<>();
        for (MediaRepresentation repr : media.getRepresentations()) {
            for (MediaRepresentationPart part : repr.getParts()) {
                newReprs.addAll(mediaTransformationProcessor.makeNewMediaRepresentationsFor(part));
            }
        }
        newReprs.addAll(media.getRepresentations());
        return MediaUtils.findBestMatchingRepresentation(newReprs, type, size, height, widthOrDuration, mimeTypes, missingValStrategy);
    }

    /**
     * @deprecated needs to be replaced, see https://dev.e-taxonomy.eu/redmine/issues/9160
     */
    @Override
    @Deprecated
    public List<Media> filterPreferredMediaRepresentations(List<Media> mediaList, Class<? extends MediaRepresentationPart> type,
            String[] mimeTypes, Integer widthOrDuration, Integer height, Integer size) {

        Map<Media, MediaRepresentation> mediaRepresentationMap = MediaUtils.findPreferredMedia(mediaList, type, mimeTypes, widthOrDuration, height, size, MediaUtils.MissingValueStrategy.MAX);

        List<Media> filteredMedia = new ArrayList<>(mediaRepresentationMap.size());
        for (Media media : mediaRepresentationMap.keySet()) {
            media.getRepresentations().clear();
            media.addRepresentation(mediaRepresentationMap.get(media));
            media.setId(0); // prevent from persisting the modified media entity accidentally
            filteredMedia.add(media);
        }
        return filteredMedia;
    }

    /**
     * Read the {@link MediaUriTransformation MediaUriTransformations} from the cdm preferences ({@link PreferencePredicate.MediaRepresentationTransformations}
     * or use the default defined in {@link DefaultMediaTransformations#digilib()}
     *
     */
    private List<MediaUriTransformation> readTransformations() {

        PrefKey key = CdmPreference.NewKey(PreferenceSubject.NewDatabaseInstance(), PreferencePredicate.MediaRepresentationTransformations);
        CdmPreference pref = preferenceService.find(key);
        if(pref != null && pref.getValue() != null) {
            if(mediaRepresentationTransformationsLastHash == null || mediaRepresentationTransformationsLastHash != pref.getValue().hashCode()) {
                // loaded value is different from last value
                ObjectMapper mapper = new ObjectMapper();
                CollectionType javaType = mapper.getTypeFactory()
                        .constructCollectionType(List.class, MediaUriTransformation.class);
                try {
                    transformations = mapper.readValue(pref.getValue(), javaType);
                    mediaRepresentationTransformationsLastHash = pref.getValue().hashCode();
                } catch (JsonMappingException e) {
                    logger.error(e);
                } catch (JsonProcessingException e) {
                    logger.error(e);
                }
            }
        }
        if(transformations == null) {
            transformations = DefaultMediaTransformations.digilib();
        }

        return transformations;
    }

    /**
     * @param trans the list of MediaUriTransformation to be serialized
     * @return the JSON string
     * @throws JsonProcessingException
     */
    static protected String transformationsToJson(List<MediaUriTransformation> trans) throws JsonProcessingException {

      ObjectMapper mapper = new ObjectMapper();
      CollectionType javaType = mapper.getTypeFactory()
              .constructCollectionType(List.class, MediaUriTransformation.class);

      String json = mapper.writerFor(javaType).withDefaultPrettyPrinter().writeValueAsString(trans);
      return json;
    }

    /**
     *
     * @param args
     * @throws JsonProcessingException
     */
    public static void main(String[] args) throws JsonProcessingException {

        System.out.println("Default tansformations for digilib");
        System.out.println("========================================");
        System.out.println(transformationsToJson(DefaultMediaTransformations.digilib()));
    }

}
