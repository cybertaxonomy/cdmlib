/**
* Copyright (C) 2020 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.remote.controller.iiif;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpException;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;

import de.digitalcollections.iiif.model.ImageContent;
import de.digitalcollections.iiif.model.MetadataEntry;
import de.digitalcollections.iiif.model.PropertyValue;
import de.digitalcollections.iiif.model.enums.ViewingDirection;
import de.digitalcollections.iiif.model.jackson.IiifObjectMapper;
import de.digitalcollections.iiif.model.sharedcanvas.Canvas;
import de.digitalcollections.iiif.model.sharedcanvas.Manifest;
import de.digitalcollections.iiif.model.sharedcanvas.Resource;
import de.digitalcollections.iiif.model.sharedcanvas.Sequence;
import de.digitalcollections.model.api.identifiable.resource.MimeType;
import eu.etaxonomy.cdm.common.media.ImageInfo;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.LanguageString;
import eu.etaxonomy.cdm.model.media.ImageFile;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.media.MediaRepresentation;
import eu.etaxonomy.cdm.model.media.MediaRepresentationPart;
import eu.etaxonomy.cdm.model.media.MediaUtils;
import eu.etaxonomy.cdm.model.media.Rights;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.remote.controller.AbstractController;
import eu.etaxonomy.cdm.remote.controller.MediaPortalController;
import eu.etaxonomy.cdm.remote.controller.TaxonPortalController;
import eu.etaxonomy.cdm.remote.controller.TaxonPortalController.EntityMediaContext;
import eu.etaxonomy.cdm.remote.editor.CdmTypePropertyEditor;
import eu.etaxonomy.cdm.remote.editor.UUIDPropertyEditor;
import eu.etaxonomy.cdm.remote.editor.UuidList;
import eu.etaxonomy.cdm.remote.l10n.LocaleContext;
import eu.etaxonomy.cdm.strategy.cache.TaggedCacheHelper;
import io.swagger.annotations.Api;
/**
 * Serves media lists as iiif manifest files ( for the IIIF Presentation API see https://iiif.io/api/presentation/2.1/#resource-structure).
 *
 * @see https://dev.e-taxonomy.eu/redmine/issues/8867
 *
 * @author a.kohlbecker
 * @since Feb 28, 2020
 *
 */
@RestController
@CrossOrigin(origins = "*")
@Api("iiif")
@RequestMapping(value = {"/iiif"})
public class ManifestController {

    /**
     *
     */
    private static final int IMAGE_READ_TIMEOUT = 3000;

    /**
     *
     */
    private static final String HTTP_IIIF_CYBERTAXONOMY_ORG = "http://iiif.cybertaxonomy.org/";

    public static final Logger logger = Logger.getLogger(ManifestController.class);

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(UUID.class, new UUIDPropertyEditor());
        // binder.registerCustomEditor(NamedArea.class, new NamedAreaPropertyEditor());
        // binder.registerCustomEditor(MatchMode.class, new MatchModePropertyEditor());
        binder.registerCustomEditor(Class.class, new CdmTypePropertyEditor());
        // binder.registerCustomEditor(UuidList.class, new UUIDListPropertyEditor());
        // binder.registerCustomEditor(DefinedTermBaseList.class, new TermBaseListPropertyEditor<>(termService));
    }


    @Autowired
    TaxonPortalController taxonPortalController;

    private String[] tumbnailMimetypes = new String[] {"image/.*", ".*"};

    @RequestMapping(
            value = {"taxon/{uuid}/manifest"},
            method = RequestMethod.GET)
    public String doTaxonMedia(
                @PathVariable("uuid") UUID uuid,
                @RequestParam(value = "type", required = false) Class<? extends MediaRepresentationPart> type,
                @RequestParam(value = "mimeTypes", required = false) String[] mimeTypes,
                @RequestParam(value = "relationships", required = false) UuidList relationshipUuids,
                @RequestParam(value = "relationshipsInvers", required = false) UuidList relationshipInversUuids,
                @RequestParam(value = "includeTaxonDescriptions", required = false) Boolean  includeTaxonDescriptions,
                @RequestParam(value = "includeOccurrences", required = false) Boolean  includeOccurrences,
                @RequestParam(value = "includeTaxonNameDescriptions", required = false) Boolean  includeTaxonNameDescriptions,
                HttpServletRequest request, HttpServletResponse response) throws IOException {

            logger.info("doGetMedia() " + AbstractController.requestPathAndQuery(request));

            EntityMediaContext<Taxon> entityMediaContext = taxonPortalController.loadMediaForTaxonAndRelated(uuid,
                    relationshipUuids, relationshipInversUuids,
                    includeTaxonDescriptions, includeOccurrences, includeTaxonNameDescriptions,
                    response, TaxonPortalController.TAXON_INIT_STRATEGY.getPropertyPaths(),
                    MediaPortalController.MEDIA_INIT_STRATEGY.getPropertyPaths());

            return serializeManifest(manifestFor(entityMediaContext, "taxon", uuid.toString()));
    }

    private String serializeManifest(Manifest manifest) throws JsonProcessingException{
        IiifObjectMapper iiifMapper = new IiifObjectMapper();
        return iiifMapper.writeValueAsString(manifest);
    }

    /**
     * @param media
     * @return
     */
    private <T extends IdentifiableEntity> Manifest manifestFor(EntityMediaContext<T> entityMediaContext, String onEntitiyType, String onEntityUuid) {


        List<Canvas> canvases = new ArrayList<>(entityMediaContext.getMedia().size());


//        Logger.getLogger(MediaUtils.class).setLevel(Level.DEBUG);
//        logger.setLevel(Level.DEBUG);

        int mediaID = 0;
        for(Media media : entityMediaContext.getMedia()){

            MediaRepresentation fullSizeRepresentation = MediaUtils.findBestMatchingRepresentation(media, null, null, Integer.MAX_VALUE, Integer.MAX_VALUE, null, MediaUtils.MissingValueStrategy.MAX);
            MediaRepresentation thumbnailRepresentation = MediaUtils.findBestMatchingRepresentation(media, null, null, 100, 100, tumbnailMimetypes, MediaUtils.MissingValueStrategy.MAX);
            if(logger.isDebugEnabled()){
                logger.debug("fullSizeRepresentation: " + fullSizeRepresentation.getParts().get(0).getUri());
                logger.debug("thumbnailRepresentation: " + thumbnailRepresentation.getParts().get(0).getUri());
            }

            // FIXME the below only makes sense if the media is an Image!!!!!
            List<ImageContent> fullSizeImageContents = representationPartsToImageContent(fullSizeRepresentation);

            List<ImageContent> thumbnailImageContents;
            if(fullSizeRepresentation.equals(thumbnailRepresentation)){
                thumbnailImageContents = fullSizeImageContents;
            } else {
                thumbnailImageContents = representationPartsToImageContent(thumbnailRepresentation);
            }

            Canvas canvas = new Canvas(iiifID(onEntitiyType, onEntityUuid, Canvas.class, mediaID++));
            for(Language lang : media.getAllTitles().keySet()){
                LanguageString titleLocalized = media.getAllTitles().get(lang);
                canvas.addLabel(titleLocalized.getText());
            }
            canvas.setLabel(new PropertyValue(media.getTitleCache()));
            canvas.setThumbnails(thumbnailImageContents);
            fullSizeImageContents.stream().forEach(ic -> canvas.addImage(ic));
            // TODO  if there is only one image canvas.addImage() internally sets the canvas width and height
            //      to the height of the image, for multiple images it is required to follow the specification:
            //
            // IIIF Presentation API 2.1.1:
            // It is recommended that if there is (at the time of implementation) a single image that depicts the page,
            // then the dimensions of the image are used as the dimensions of the canvas for simplicity. If there are
            // multiple full images, then the dimensions of the largest image should be used. If the largest image’s
            // dimensions are less than 1200 pixels on either edge, then the canvas’s dimensions should be double those
            // of the image.

            List<MetadataEntry> mediaMetadata = mediaMetaData(media);
            List<MetadataEntry> representationMetadata = mediaRepresentationMetaData(fullSizeRepresentation);
            mediaMetadata.addAll(representationMetadata);

            // extractAndAddDesciptions(canvas, mediaMetadata);
            mediaMetadata = deduplicateMetadata(mediaMetadata);
            canvas.addMetadata(mediaMetadata.toArray(new MetadataEntry[mediaMetadata.size()]));
            canvases.add(canvas);
        }

        Sequence sequence = null;
        if(canvases.size() > 0) {
            sequence = new Sequence(iiifID(onEntitiyType, onEntityUuid, Sequence.class, "default"));
            sequence.setViewingDirection(ViewingDirection.LEFT_TO_RIGHT);
            sequence.setCanvases(canvases);
            sequence.setStartCanvas(canvases.get(0).getIdentifier());
        }

        Manifest manifest = new Manifest(iiifID(onEntitiyType, onEntityUuid, Manifest.class, null));
        if(sequence != null){
            // manifest.setLabel(new PropertyValue("Media for " + onEntitiyType + "[" + onEntityUuid + "]")); // TODO better label!!
            manifest.addSequence(sequence);
        } else {
            manifest.setLabel(new PropertyValue("No media found for " + onEntitiyType + "[" + onEntityUuid + "]")); // TODO better label!!
        }
        List<MetadataEntry> entityMetadata = entityMetadata(entityMediaContext.getEntity());
        manifest.addMetadata(entityMetadata.toArray(new MetadataEntry[entityMetadata.size()]));

        return manifest;
    }

    /**
     * @param mediaMetadata
     * @return
     */
    private List<MetadataEntry> deduplicateMetadata(List<MetadataEntry> mediaMetadata) {
        Map<String, MetadataEntry> dedupMap = new HashMap<>();
        mediaMetadata.stream().forEach(mde -> {
                dedupMap.put(mde.getLabelString() + ":" + mde.getValueString(), mde);
            }
        );
        return new ArrayList<MetadataEntry>(dedupMap.values());
    }

    /**
     * @param canvas
     * @param mediaMetadata
     */
    void extractAndAddDesciptions(Resource resource, List<MetadataEntry> mediaMetadata) {
        List<MetadataEntry> descriptions = mediaMetadata.stream()
            .filter(mde -> mde.getLabelString().toLowerCase().matches(".*description.*|.*caption.*"))
            .collect(Collectors.toList());
        mediaMetadata.removeAll(descriptions);
        // FIXME deduplicate mde.getValueString()
        // descriptions.sream ...
        descriptions.stream().forEach(mde -> resource.addDescription(mde.getValueString()));
    }

    /**
     * @param entity
     * @return
     */
    private <T extends IdentifiableEntity> List<MetadataEntry> entityMetadata(T entity) {

        List<MetadataEntry> metadata = new ArrayList<>();
        if(entity instanceof TaxonBase){
            List taggedTitle = ((TaxonBase)entity).getTaggedTitle();
            if(taggedTitle != null){
                //FIXME taggedTitel to HTML!!!!
                metadata.add(new MetadataEntry(entity.getClass().getSimpleName(), TaggedCacheHelper.createString(taggedTitle)));
            }
        } else {
            String titleCache = entity.getTitleCache();
            if(titleCache != null){
                metadata.add(new MetadataEntry(entity.getClass().getSimpleName(), titleCache));
            }
        }

        return metadata;
    }

    private List<MetadataEntry> mediaRepresentationMetaData(MediaRepresentation representation) {

        List<MetadataEntry> metadata = new ArrayList<>();
        boolean needsPrefix = representation.getParts().size() > 1;
        int partIndex = 1;
        for (MediaRepresentationPart part : representation.getParts()) {
            String prefix = "";
            if (needsPrefix) {
                prefix = "Part" + partIndex + " ";
            }
            if (part.getUri() != null) {
                try {
                    ImageInfo imageInfo = ImageInfo.NewInstanceWithMetaData(part.getUri(), IMAGE_READ_TIMEOUT);
                    Map<String, String> result = imageInfo.getMetaData();
                    for (String key : result.keySet()) {
                        metadata.add(new MetadataEntry(key, result.get(key)));
                    }
                } catch (IOException | HttpException e) {
                    logger.error(e);
                    metadata.add(new MetadataEntry(prefix + " Error:", "Problem while loading image metadata"));
                }
            }
        }

        return metadata;
  }

    private List<MetadataEntry> mediaMetaData(Media media) {
        List<MetadataEntry> metadata = new ArrayList<>();
        List<Language> languages = LocaleContext.getLanguages();
        // TODO get localized titleCache
        if(media.getTitle() != null){
            metadata.add(new MetadataEntry("Title", media.getTitleCache()));
        }
        if(media.getArtist() != null){
            metadata.add(new MetadataEntry("Artist", media.getArtist().getTitleCache()));
        }
        PropertyValue rightsTexts = new PropertyValue();
        for(Rights right : media.getRights()){
            // TODO get localized text
            if(right.getText() != null){
                rightsTexts.addValue(right.getText());
            }
        }
        if(rightsTexts.getValues().size() > 0){
            metadata.add(new MetadataEntry(new PropertyValue("Copyright"), rightsTexts));
        }
        if(media.getMediaCreated() != null){
            metadata.add(new MetadataEntry("Created on", media.getMediaCreated().toString())); // TODO is this correct to string conversion?
        }
        return metadata;
    }

    /**
     * @param thumbnailRepresentation
     * @return
     */
    private List<ImageContent> representationPartsToImageContent(MediaRepresentation representation) {
        List<ImageContent> imageContents = new ArrayList<>();
        for(MediaRepresentationPart part : representation.getParts()){
            if(part.getUri() != null){
                ImageContent ic = new ImageContent(part.getUri().toString());
                if(part instanceof ImageFile){
                    ImageFile image = (ImageFile)part;
                    if(image.getWidth() != null && image.getWidth() > 0){
                        ic.setWidth(image.getWidth());
                    }
                    if(image.getHeight() != null && image.getHeight() > 0){
                        ic.setHeight(image.getHeight());
                    }
                    if(representation.getMimeType() != null){
                        ic.setFormat(MimeType.fromTypename(representation.getMimeType()));
                    } else {
                        ic.setFormat(MimeType.MIME_IMAGE);
                    }
                }
                imageContents.add(ic);
            }
        }
        return imageContents;
    }

    /**
     * @param onEntitiyType
     * @param onEntityUuid
     * @return
     */
    private String iiifID(String onEntitiyType, String onEntityUuid, Class<? extends Resource> iiifType, Object index) {
        String indexPart = "";
        if(index != null){
            indexPart = "/" + index.toString();
        }
        return HTTP_IIIF_CYBERTAXONOMY_ORG + onEntitiyType + "/" + onEntityUuid + "/" + iiifType.getSimpleName().toLowerCase() + indexPart;
    }
}
