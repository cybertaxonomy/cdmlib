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
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;

import eu.etaxonomy.cdm.api.service.IPreferenceService;
import eu.etaxonomy.cdm.api.service.media.MediaUriTransformation;
import eu.etaxonomy.cdm.api.service.media.MediaUriTransformationProcessor;
import eu.etaxonomy.cdm.api.service.media.SearchReplace;
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
 * See {@link MediaUriTransformationProcessor} and {@link MediaUriTransformation}
 *
 * @author a.kohlbecker
 * @since Jul 8, 2020
 */
public class MediaToolbox implements IMediaToolbox {

    private static final String SYS_PROP_MEDIA_REPRESENTATION_TRANSFORMATIONS_RESET = "mediaRepresentationTransformationsReset";

    private static final Logger logger = Logger.getLogger(MediaToolbox.class);

    private List<MediaUriTransformation> transformations = null;

    private Integer mediaRepresentationTransformationsLastHash = null;

    @Autowired
    private IPreferenceService service;

    @Override
    public List<Media> processAndFilterPreferredMediaRepresentations(Class<? extends MediaRepresentationPart> type, String[] mimeTypes,
            Integer widthOrDuration, Integer height, Integer size, List<Media> taxonGalleryMedia) {

        MediaUriTransformationProcessor mediaTransformationProcessor = new MediaUriTransformationProcessor();
        mediaTransformationProcessor.addAll(readTransformations());

        for(Media media : taxonGalleryMedia) {
            List<MediaRepresentation> newRepr = new ArrayList<>();
            for(MediaRepresentation repr : media.getRepresentations()) {
                for(MediaRepresentationPart part : repr.getParts()) {
                    newRepr.addAll(mediaTransformationProcessor.makeNewMediaRepresentationsFor(part));
                }
            }
            media.getRepresentations().addAll(newRepr);
        }
        return filterPreferredMediaRepresentations(type, mimeTypes, widthOrDuration, height, size, taxonGalleryMedia);

    }

    @Override
    public MediaRepresentation processAndFindBestMatchingRepresentation(Media media,
            Class<? extends MediaRepresentationPart> type, Integer size, Integer height, Integer widthOrDuration,
            String[] mimeTypes, MissingValueStrategy missingValStrategy) {

        MediaUriTransformationProcessor mediaTransformationProcessor = new MediaUriTransformationProcessor();
        mediaTransformationProcessor.addAll(readTransformations());

        List<MediaRepresentation> newRepr = new ArrayList<>();
        for (MediaRepresentation repr : media.getRepresentations()) {
            for (MediaRepresentationPart part : repr.getParts()) {
                newRepr.addAll(mediaTransformationProcessor.makeNewMediaRepresentationsFor(part));
            }
        }
        media.getRepresentations().addAll(newRepr);

        return MediaUtils.findBestMatchingRepresentation(media, type, size, height, widthOrDuration, mimeTypes,
                missingValStrategy);

    }

    @Override
    public List<Media> filterPreferredMediaRepresentations(Class<? extends MediaRepresentationPart> type, String[] mimeTypes,
            Integer widthOrDuration, Integer height, Integer size, List<Media> taxonGalleryMedia) {


        Map<Media, MediaRepresentation> mediaRepresentationMap = MediaUtils.findPreferredMedia(taxonGalleryMedia, type, mimeTypes, widthOrDuration, height, size, MediaUtils.MissingValueStrategy.MAX);

        List<Media> filteredMedia = new ArrayList<>(mediaRepresentationMap.size());
        for (Media media : mediaRepresentationMap.keySet()) {
            media.getRepresentations().clear();
            media.addRepresentation(mediaRepresentationMap.get(media));
            filteredMedia.add(media);
        }
        return filteredMedia;
    }

    private List<MediaUriTransformation> readTransformations() {

        //System.setProperty(SYS_PROP_MEDIA_REPRESENTATION_TRANSFORMATIONS_RESET, "1");
        PrefKey key = CdmPreference.NewKey(PreferenceSubject.NewDatabaseInstance(), PreferencePredicate.MediaRepresentationTransformations);
        CdmPreference pref = service.find(key);
        if(pref != null && pref.getValue() != null) {
            if(System.getProperty(SYS_PROP_MEDIA_REPRESENTATION_TRANSFORMATIONS_RESET) == null ||mediaRepresentationTransformationsLastHash == null || mediaRepresentationTransformationsLastHash != pref.getValue().hashCode()) {
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
        if(transformations == null || System.getProperty(SYS_PROP_MEDIA_REPRESENTATION_TRANSFORMATIONS_RESET) != null) {

            transformations = new ArrayList<>();
            MediaUriTransformation tr1 = new MediaUriTransformation();

            tr1.setPathQueryFragment(new SearchReplace("digilib/Scaler/IIIF/([^\\!]+)\\!([^\\/]+)(.*)", "digilib/Scaler/IIIF/$1!$2/full/!200,200/0/default.jpg"));
            tr1.setHost(new SearchReplace("pictures.bgbm.org", "pictures.bgbm.org")); // host part only used for matching, no replace!
            tr1.setMimeType("image/jpeg");
            tr1.setWidth(200);
            tr1.setHeight(200);

            MediaUriTransformation tr2 = new MediaUriTransformation();

            tr2.setPathQueryFragment(new SearchReplace("digilib/Scaler/IIIF/([^\\!]+)\\!([^\\/]+)(.*)", "digilib/Scaler/IIIF/$1!$2/full/!400,400/0/default.jpg"));
            tr2.setHost(new SearchReplace("pictures.bgbm.org", "pictures.bgbm.org")); // host part only used for matching, no replace!
            tr2.setMimeType("image/jpeg");
            tr2.setWidth(400);
            tr2.setHeight(400);


            MediaUriTransformation tr3 = new MediaUriTransformation();
            tr3.setPathQueryFragment(new SearchReplace("digilib/Scaler/\\?fn=([^\\\\/]+)/(\\w+)(.*)", "digilib/Scaler/IIIF/$1!$2/full/!400,400/0/default.jpg"));
            tr3.setHost(new SearchReplace("pictures.bgbm.org", "pictures.bgbm.org")); // host part only used for matching, no replace!
            tr3.setMimeType("image/jpeg");
            tr3.setWidth(400);
            tr3.setHeight(400);

            MediaUriTransformation tr4 = new MediaUriTransformation();
            tr4.setPathQueryFragment(new SearchReplace("digilib/Scaler/\\?fn=([^\\\\/]+)/(\\w+)(.*)", "digilib/Scaler/IIIF/$1!$2/full/!200,200/0/default.jpg"));
            tr4.setHost(new SearchReplace("pictures.bgbm.org", "pictures.bgbm.org")); // host part only used for matching, no replace!
            tr4.setMimeType("image/jpeg");
            tr4.setWidth(200);
            tr4.setHeight(200);

            transformations.add(tr1);
            transformations.add(tr2);
            transformations.add(tr3);
            transformations.add(tr4);

            ObjectMapper mapper = new ObjectMapper();
            CollectionType javaType = mapper.getTypeFactory()
                    .constructCollectionType(List.class, MediaUriTransformation.class);
            try {
                String json = mapper.writerFor(javaType).writeValueAsString(transformations);
                pref = CdmPreference.NewDatabaseInstance(PreferencePredicate.MediaRepresentationTransformations, json);
                service.set(pref);
            } catch (JsonProcessingException e) {
                logger.error(e);
            }
        }

        return transformations;
    }
}
