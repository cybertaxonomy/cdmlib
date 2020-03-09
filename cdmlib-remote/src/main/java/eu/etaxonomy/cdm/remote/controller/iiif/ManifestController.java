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
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import eu.etaxonomy.cdm.api.service.ITermService;
import eu.etaxonomy.cdm.api.service.util.TaxonRelationshipEdge;
import eu.etaxonomy.cdm.common.media.ImageInfo;
import eu.etaxonomy.cdm.model.common.Credit;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.LanguageString;
import eu.etaxonomy.cdm.model.media.ImageFile;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.media.MediaRepresentation;
import eu.etaxonomy.cdm.model.media.MediaRepresentationPart;
import eu.etaxonomy.cdm.model.media.MediaUtils;
import eu.etaxonomy.cdm.model.media.Rights;
import eu.etaxonomy.cdm.model.media.RightsType;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.persistence.dao.initializer.EntityInitStrategy;
import eu.etaxonomy.cdm.remote.controller.AbstractController;
import eu.etaxonomy.cdm.remote.controller.MediaPortalController;
import eu.etaxonomy.cdm.remote.controller.TaxonPortalController;
import eu.etaxonomy.cdm.remote.controller.TaxonPortalController.EntityMediaContext;
import eu.etaxonomy.cdm.remote.controller.util.ControllerUtils;
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

    @Autowired
    ITermService termService;

    private String[] tumbnailMimetypes = new String[] {"image/.*", ".*"};

    /**
     * Universalviewer only shows one attribution value in the popup panel
     * Therefore it makes sense to join all of them.
     */
    private boolean doJoinAttributions = true;

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
                @RequestParam(value = "includeTaxonomicChildren", required = false) Boolean  includeTaxonomicChildren,
                HttpServletRequest request, HttpServletResponse response) throws IOException {

            logger.info("doGetMedia() " + AbstractController.requestPathAndQuery(request));

            EntityInitStrategy taxonInitStrategy = includeTaxonomicChildren? TaxonPortalController.TAXON_WITH_CHILDNODES_INIT_STRATEGY : TaxonPortalController.TAXON_INIT_STRATEGY;
            EntityMediaContext<Taxon> entityMediaContext = taxonPortalController.loadMediaForTaxonAndRelated(uuid,
                    relationshipUuids, relationshipInversUuids,
                    includeTaxonDescriptions, includeOccurrences, includeTaxonNameDescriptions,
                    response, taxonInitStrategy.getPropertyPaths(),
                    MediaPortalController.MEDIA_INIT_STRATEGY.getPropertyPaths());

            if(includeTaxonomicChildren){
                Set<TaxonRelationshipEdge> includeRelationships = ControllerUtils.loadIncludeRelationships(relationshipUuids, relationshipInversUuids, termService);
                entityMediaContext.setMedia(
                        taxonPortalController.addTaxonomicChildrenMedia(includeTaxonDescriptions, includeOccurrences, includeTaxonNameDescriptions, entityMediaContext.getEntity(), includeRelationships, entityMediaContext.getMedia())
                                );
            }

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
            for(ImageContent image  : fullSizeImageContents){
                canvas.addImage(image);
            }
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
            canvas = addAttributionAndLicense(media, canvas, mediaMetadata);
            orderMedatadaItems(canvas);
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
        copyAttributionAndLicenseToManifest(manifest);

        return manifest;
    }

    /**
     * Due to limitations in universal viewer it seems not to be able
     * to show attribution and licenses, therefore we copy this data to
     * also to the metadata
     *
     * <b>NOTE:</b> This method expects that the canvas attributions and
     * licenses are not localized!!!!
     *
     * @param canvas
     */
    private void copyAttributionAndLicenseToManifest(Manifest manifest) {

         PropertyValue attributions = new PropertyValue();
         List<URI> licenses = new ArrayList<>();
         String firstAttributionString = null;
         boolean hasAttributions = false;
         boolean hasLicenses = false;
         boolean hasDiversAttributions = false;
         boolean hasDiversLicenses = false;
         String firstLicensesString = null;


        for (Sequence sequence : manifest.getSequences()) {
            for (Canvas canvas : sequence.getCanvases()) {
                if (canvas.getAttribution() != null) {
                    canvas.getAttribution().getValues().stream().forEachOrdered(val -> attributions.addValue(val));
                    String thisAttributionString = canvas.getAttribution().getValues()
                                .stream()
                                .sorted()
                                .collect(Collectors.joining());
                    if(firstAttributionString == null){
                        firstAttributionString = thisAttributionString;
                        hasAttributions = true;
                    } else {
                        hasDiversAttributions |=  !firstAttributionString.equals(thisAttributionString);
                    }
                }
                if (canvas.getLicenses() != null && canvas.getLicenses().size() > 0) {
                    licenses.addAll(canvas.getLicenses());
                    String thisLicensesString = canvas.getLicenses()
                                .stream()
                                .map(URI::toString)
                                .sorted()
                                .collect(Collectors.joining());
                    if(firstLicensesString == null){
                        firstLicensesString = thisLicensesString;
                        hasLicenses = true;
                    } else {
                        hasDiversLicenses |=  !firstLicensesString.equals(thisLicensesString);
                    }
                }
            }
        }
        String diversityInfo = "";

        if(hasAttributions || hasLicenses){
            String dataTypes ;
            if(hasAttributions && hasLicenses) {
                dataTypes = "attributions and licenses";
            } else if(hasAttributions){
                dataTypes = "attributions";
            } else {
                dataTypes = "licenses";
            }
            if(hasDiversAttributions || hasDiversLicenses){
                diversityInfo = "Individual " + dataTypes + " per Item:";
            } else {
                diversityInfo = "Same " + dataTypes + " for any Item:";
            }
            if(hasAttributions){
                List<String> attrs = new ArrayList<>(attributions.getValues());
                attrs = attrs.stream().sorted().distinct().collect(Collectors.toList());
                if(doJoinAttributions){
                    attrs.add(0, diversityInfo + "<br/>" + attrs.get(0));
                    attrs.remove(1);
                    manifest.addAttribution(attrs.stream()
                            .sorted()
                            .distinct()
                            .collect(Collectors.joining("; ")));
                } else {
                    manifest.addAttribution(diversityInfo, attrs.toArray(
                            new String[attributions.getValues().size()]
                            ));
                }
            }
            licenses.stream()
                .map(URI::toString)
                .sorted()
                .distinct()
                .forEachOrdered(l -> manifest.addLicense(l));
        }
    }

    /**
     * @param canvas
     */
    private void orderMedatadaItems(Canvas canvas) {
        // TODO Auto-generated method stub
        // order by label name, Title, description, author, license, attribution should come first.
    }


    /**
     * @param mediaMetadata
     * @return
     */
    private List<MetadataEntry> deduplicateMetadata(List<MetadataEntry> mediaMetadata) {
        Map<String, MetadataEntry> dedupMap = new HashMap<>();
        mediaMetadata.stream().forEach(mde -> {
                String dedupKey = mde.getLabelString() + ":" + mde.getValueString();
                dedupMap.put(dedupKey, mde);
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


        if(media.getTitle() != null){
            // TODO get localized titleCache
            metadata.add(new MetadataEntry("Title", media.getTitleCache()));
        }
        if(media.getArtist() != null){
            metadata.add(new MetadataEntry("Artist", media.getArtist().getTitleCache()));
        }
        if(media.getAllDescriptions().size() > 0){
            // TODO get localized description
            PropertyValue descriptionValues = new PropertyValue();
            for(LanguageString description : media.getAllDescriptions().values()){
                descriptionValues.addValue(description.getText());
            }
            metadata.add(new MetadataEntry(new PropertyValue("Description"), descriptionValues));
        }
        if(media.getMediaCreated() != null){
            metadata.add(new MetadataEntry("Created on", media.getMediaCreated().toString())); // TODO is this correct to string conversion?
        }
        return metadata;
    }

    private <T extends Resource<T>> T addAttributionAndLicense(IdentifiableEntity<?> entity, T resource, List<MetadataEntry> metadata) {

        List<Language> languages = LocaleContext.getLanguages();

        List<String> rightsTexts = new ArrayList<>();
        List<String> creditTexts = new ArrayList<>();
        List<URI> license = new ArrayList<>();

        if(entity.getRights() != null && entity.getRights().size() > 0){
            for(Rights right : entity.getRights()){
                String rightText = "";
                // TODO get localized texts below
                // --- LICENSE
                if(right.getType().equals(RightsType.LICENSE())){
                    String licenseText = "";
                    String licenseAbbrev = "";
                    if(right.getText() != null){
                        licenseText = right.getText();
                    }
                    if(right.getAbbreviatedText() != null){
                        licenseAbbrev = right.getAbbreviatedText().trim();
                    }
                    if(right.getUri() != null){
                        if(!licenseAbbrev.isEmpty()) {
                            licenseAbbrev =  htmlLink(right.getUri(), licenseAbbrev);
                        } else if(!licenseText.isEmpty()) {
                            licenseText =  htmlLink(right.getUri(), licenseText);
                        } else {
                            licenseText =  htmlLink(right.getUri(), right.getUri().toString());
                        }
                        license.add(right.getUri());
                    }
                    rightText = licenseAbbrev + (licenseText.isEmpty() ? "" : " ") + licenseText;
                }
                // --- COPYRIGHT
                if(right.getType().equals(RightsType.COPYRIGHT())){
                    // titleCache + agent
                    String copyRightText = "";
                    if(right.getText() != null){
                        copyRightText = right.getText();
                        //  sanitize potential '(c)' away
                        copyRightText.replace("(c)", "").trim();
                    }
                    if(right.getAgent() != null){
                        // may only apply to RightsType.accessRights
                        copyRightText += " " + right.getAgent().getTitleCache();
                    }
                    if(!copyRightText.isEmpty()){
                        copyRightText = "© " + copyRightText;
                    }
                    rightText = copyRightText;
                }
                if(right.getType().equals(RightsType.ACCESS_RIGHTS())){
                    // titleCache + agent
                    String accessRights = right.getText();
                    if(right.getAgent() != null){
                        // may only apply to RightsType.accessRights
                        accessRights = " " + right.getAgent().getTitleCache();
                    }
                    rightText = accessRights;
                }
                if(!rightText.isEmpty()){
                    rightsTexts.add(rightText);
                }
            }
        }
        if(entity.getCredits() != null && entity.getCredits().size() > 0){
            for(Credit credit : entity.getCredits()){
                String creditText = "";
                if(credit.getText() != null){
                    creditText += credit.getText();
                }
                if(creditText.isEmpty() && credit.getAbbreviatedText() != null){
                    creditText += credit.getAbbreviatedText();
                }
                if(credit.getAgent() != null){
                    // may only apply to RightsType.accessRights
                    creditText += " " + credit.getAgent().getTitleCache();
                }
                creditTexts.add(creditText);
            }
        }

        if(rightsTexts.size() > 0){
            String joinedRights = rightsTexts.stream().collect(Collectors.joining(", "));
            resource.addAttribution(joinedRights);
            if(metadata != null){
                metadata.add(new MetadataEntry(new PropertyValue("Copyright"), new PropertyValue(joinedRights)));
            }
        }
        if(creditTexts.size() > 0){
            String joinedCredits = creditTexts.stream().collect(Collectors.joining(", "));
            resource.addAttribution(joinedCredits);
            if(metadata != null){
                metadata.add(new MetadataEntry(new PropertyValue("Credit"), new PropertyValue(joinedCredits)));
            }
        }
        resource.setLicenses(license);
        return resource;
    }

    /**
     * @param right
     * @return
     */
    String htmlLink(URI uri, String text) {
        return String.format(" <a href=\"%s\">%s</a>", uri, text);
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
