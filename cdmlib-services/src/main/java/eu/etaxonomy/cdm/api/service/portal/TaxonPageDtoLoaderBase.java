/**
* Copyright (C) 2024 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.portal;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import eu.etaxonomy.cdm.api.application.ICdmRepository;
import eu.etaxonomy.cdm.api.dto.portal.AnnotatableDto;
import eu.etaxonomy.cdm.api.dto.portal.AnnotationDto;
import eu.etaxonomy.cdm.api.dto.portal.CdmBaseDto;
import eu.etaxonomy.cdm.api.dto.portal.ContainerDto;
import eu.etaxonomy.cdm.api.dto.portal.MarkerDto;
import eu.etaxonomy.cdm.api.dto.portal.MediaDto2;
import eu.etaxonomy.cdm.api.dto.portal.SingleSourcedDto;
import eu.etaxonomy.cdm.api.dto.portal.SourceDto;
import eu.etaxonomy.cdm.api.dto.portal.SourcedDto;
import eu.etaxonomy.cdm.api.dto.portal.TaxonPageDto;
import eu.etaxonomy.cdm.api.dto.portal.TaxonPageDto.MediaRepresentationDTO;
import eu.etaxonomy.cdm.api.dto.portal.config.ISourceableLoaderConfiguration;
import eu.etaxonomy.cdm.format.common.TypedLabel;
import eu.etaxonomy.cdm.format.reference.OriginalSourceFormatter;
import eu.etaxonomy.cdm.model.common.AnnotatableEntity;
import eu.etaxonomy.cdm.model.common.Annotation;
import eu.etaxonomy.cdm.model.common.AnnotationType;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.ICdmBase;
import eu.etaxonomy.cdm.model.common.IPublishable;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.LanguageString;
import eu.etaxonomy.cdm.model.common.Marker;
import eu.etaxonomy.cdm.model.common.MultilanguageTextHelper;
import eu.etaxonomy.cdm.model.common.SingleSourcedEntityBase;
import eu.etaxonomy.cdm.model.description.DescriptionBase;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.DescriptionElementSource;
import eu.etaxonomy.cdm.model.media.ExternalLink;
import eu.etaxonomy.cdm.model.media.ImageFile;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.media.MediaRepresentation;
import eu.etaxonomy.cdm.model.media.MediaRepresentationPart;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.reference.ISourceable;
import eu.etaxonomy.cdm.model.reference.NamedSource;
import eu.etaxonomy.cdm.model.reference.NamedSourceBase;
import eu.etaxonomy.cdm.model.reference.OriginalSourceBase;
import eu.etaxonomy.cdm.model.reference.OriginalSourceType;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.term.Representation;
import eu.etaxonomy.cdm.model.term.TermBase;
import eu.etaxonomy.cdm.persistence.dao.common.ICdmGenericDao;
import eu.etaxonomy.cdm.strategy.cache.TaggedText;

/**
 * @author muellera
 * @since 27.02.2024
 */
public abstract class TaxonPageDtoLoaderBase {

    protected ICdmRepository repository;
    protected ICdmGenericDao dao;

    public TaxonPageDtoLoaderBase(ICdmRepository repository, ICdmGenericDao dao) {
        this.repository = repository;
        this.dao = dao;
    }

    //currently only used by fact loader
    protected String getTermLabel(TermBase term, Language localeLang) {
        if (term == null) {
            return null;
        }
        Representation rep = term.getPreferredRepresentation(localeLang);
        String label = rep == null ? null : rep.getLabel();
        label = label == null ? term.getLabel() : label;
        return label;
    }

    protected <P extends IPublishable> List<P> filterPublished(List<P> listToPublish) {
        if (listToPublish == null) {
            return null;
        }else {
            return listToPublish.stream().filter(s->s.isPublish()).collect(Collectors.toList());
        }
    }

    protected <P extends IPublishable> Set<P> filterPublished(Set<P> setToPublish) {
        if (setToPublish == null) {
            return null;
        }else {
            return setToPublish.stream().filter(s->s.isPublish()).collect(Collectors.toSet());
        }
    }

    public static void loadBaseData(ISourceableLoaderConfiguration config, CdmBase cdmBase, CdmBaseDto dto) {
        dto.setId(cdmBase.getId());
        dto.setUuid(cdmBase.getUuid());

        loadAnnotatable(config, cdmBase, dto);
        loadSources(config, cdmBase, dto);
        //loadIdentifiable(cdmBase, dto);
    }

    static void loadAnnotatable(ISourceableLoaderConfiguration config, CdmBase cdmBase, CdmBaseDto dto) {
        if (dto instanceof AnnotatableDto && cdmBase.isInstanceOf(AnnotatableEntity.class)) {
            AnnotatableEntity annotatable = CdmBase.deproxy(cdmBase, AnnotatableEntity.class);
            AnnotatableDto annotatableDto = (AnnotatableDto)dto;
            //annotation
            for (Annotation annotation : annotatable.getAnnotations()) {
                UUID annotationTypeUuid = annotation.getAnnotationType() == null ? null : annotation.getAnnotationType().getUuid();
                if (checkAnnotationType(config, annotationTypeUuid, annotation.getText() )) {

                    AnnotationDto annotationDto = new AnnotationDto();
                    annotatableDto.addAnnotation(annotationDto);
                    //TODO id needed? but need to adapt dto and container then
                    loadBaseData(config, annotation, annotationDto);
                    annotationDto.setText(annotation.getText());
                    UUID uuidAnnotationType = annotation.getAnnotationType() == null ? null :annotation.getAnnotationType().getUuid();
                    annotationDto.setTypeUuid(uuidAnnotationType);
                    //language etc. currently not yet used
                }
            }

            //marker
            for (Marker marker : annotatable.getMarkers()) {
                if (marker.getMarkerType() != null
                        //TODO markertype filter
//                        && marker.getMarkerType().getUuid().equals(AnnotationType.uuidEditorial)
                           ){

                    MarkerDto markerDto = new MarkerDto();
                    annotatableDto.addMarker(markerDto);
                    //TODO id needed? but need to adapt dto and container then
                    loadBaseData(config, marker, markerDto);
                    if (marker.getMarkerType() != null) {
                        markerDto.setTypeUuid(marker.getMarkerType().getUuid());
                        //TODO locale
                        markerDto.setType(marker.getMarkerType().getTitleCache());
                    }
                    markerDto.setValue(marker.getValue());
                }
            }
        }
    }

    protected static boolean checkAnnotationType(ISourceableLoaderConfiguration config, UUID typeUuid, String annotationText) {
        return (typeUuid != null
                //config == null currently needs to be allowed as it is also used by DistributionInfoBuilder and
                //also for now empty annotation types needs to be interpreted as "no filter" as we do not distinguish
                //null and empty yet, this may change in future
                && (config == null || config.getAnnotationTypes().isEmpty()
                    || config.getAnnotationTypes().contains(typeUuid)))
             || (typeUuid == null && config != null && config.getAnnotationTypes().contains(AnnotationType.uuidUntyped))
                        //TODO do we really want to exclude empty annotations?
                        && StringUtils.isNotBlank(annotationText)
                        ;
    }

    static void loadSources(ISourceableLoaderConfiguration config, CdmBase cdmBase, CdmBaseDto dto) {
        if (dto instanceof SingleSourcedDto && cdmBase.isInstanceOf(SingleSourcedEntityBase.class)) {
            //TODO other sourced
            SingleSourcedEntityBase sourced = CdmBase.deproxy(cdmBase, SingleSourcedEntityBase.class);
            SingleSourcedDto sourcedDto = (SingleSourcedDto)dto;
            NamedSource source = sourced.getSource();
            if (source != null && isPublicSource(source, config) && !source.checkEmpty(true)) {
                SourceDto sourceDto = makeSource(config, source);
                sourcedDto.setSource(sourceDto);
            }
        } else if (dto instanceof SourcedDto && cdmBase instanceof ISourceable) {
            @SuppressWarnings("unchecked")
            ISourceable<OriginalSourceBase> sourced = (ISourceable<OriginalSourceBase>)cdmBase;
            SourcedDto sourcedDto = (SourcedDto)dto;
            for (OriginalSourceBase source : sourced.getSources()) {
                if (isPublicSource(source, config)) {
                    SourceDto sourceDto = makeSource(config, source);
                    sourcedDto.addSource(sourceDto);
                }
            }
        }
        //load description sources for facts
        if (cdmBase.isInstanceOf(DescriptionElementBase.class)){
            DescriptionBase<?> db = CdmBase.deproxy(cdmBase, DescriptionElementBase.class).getInDescription();
            if (db != null) {  //test sometime do not have a description for facts
                SourcedDto sourcedDto = (SourcedDto)dto;
                for (OriginalSourceBase source : db.getSources()) {
                    if (isPublicSource(source, config)) {
                        SourceDto sourceDto = new SourceDto();
                        loadSource(config, source, sourceDto);
                        sourcedDto.addSource(sourceDto);
                    }
                }
            }
        }
    }

    private static void loadSource(ISourceableLoaderConfiguration config, OriginalSourceBase source, SourceDto sourceDto) {

        source = CdmBase.deproxy(source);
        //base data
        loadBaseData(config, source, sourceDto);

        ICdmBase linkedObject = source.getCitation();
        if (linkedObject == null) {
            //cdmsource
            linkedObject = source.getCdmSource();
        }

        sourceDto.setCitationDetail(source.getCitationMicroReference());
        //citation doi & uri & links
        Reference ref = source.getCitation();
        if (ref != null) {
            sourceDto.setDoi(ref.getDoiUriString());
            sourceDto.setUri(ref.getUri());
            sourceDto.setReferenceUuid(ref.getUuid());
            Set<ExternalLink> links = ref.getLinks();

            for (ExternalLink link : links) {
                if (link.getUri() != null) {
                    sourceDto.addLink(link.getUri());
                }
            }
            sourceDto.setSortableDate(ref.getSortableDateString());

        }

        //accessed
        if(source.getAccessed() != null) {
            sourceDto.setAccessed(source.getAccessed().toString());
        }else if (ref != null && ref.getAccessed() != null) {
            //TODO better formatting
            sourceDto.setAccessed(ref.getAccessed().toString());
        }

        //original info
        sourceDto.setOriginalInfo(source.getOriginalInfo());

        //label
        //TODO this probably does not use specimen or cdmSource if necessary,
        //     also long citation is still preliminary
        String label = OriginalSourceFormatter.INSTANCE_LONG_CITATION.format(source);
        TypedLabel typedLabel = new TypedLabel(source, label);
        sourceDto.addLabel(typedLabel);
        sourceDto.setType(source.getType() != null ? source.getType().toString() : null);

        if (source.isInstanceOf(NamedSourceBase.class)) {
            NamedSourceBase ns = CdmBase.deproxy(source, NamedSourceBase.class);

            //nameUsedInSource
            TaxonName name =  ns.getNameUsedInSource();
            if (name != null) {
                List<TaggedText> taggedName = name.cacheStrategy().getTaggedTitle(name);
                //TODO nom status?
                sourceDto.setNameInSource(taggedName);
                sourceDto.setNameInSourceUuid(name.getUuid());
            }

            //specimen uuid
            if (source.isInstanceOf(DescriptionElementSource.class)) {
                DescriptionElementSource des = CdmBase.deproxy(source, DescriptionElementSource.class);
                if (linkedObject == null) {
                    linkedObject = des.getSpecimen();
                }
            }
        }

        sourceDto.setLinkedUuid(getUuid(linkedObject));
        String linkedObjectStr = linkedObject == null ? null : CdmBase.deproxy(linkedObject).getClass().getSimpleName();
        sourceDto.setLinkedClass(linkedObjectStr);
    }

    protected static SourceDto makeSource(ISourceableLoaderConfiguration config, OriginalSourceBase source) {
        if (source == null) {
            return null;
        }
        SourceDto sourceDto = new SourceDto();
        loadSource(config, source, sourceDto);
        return sourceDto;
    }

    protected static UUID getUuid(ICdmBase cdmBase) {
        return cdmBase == null ? null : cdmBase.getUuid();
    }

    protected static boolean isPublicSource(OriginalSourceBase source, ISourceableLoaderConfiguration config) {
        if (source.getType() == null) {
            return false; //should not happen
        }else {
            OriginalSourceType type = source.getType();
            return config.getSourceTypes().contains(type);
        }
    }

    protected void makeMediaContainer(ISourceableLoaderConfiguration config, TaxonPageDto result, List<Media> medias) {
        ContainerDto<MediaDto2> container = new ContainerDto<>();

        for (Media media : medias) {
            handleSingleMedia(config, container, media);
        }

        if (container.getCount() > 0) {
            result.setMedia(container);
        }
    }

    protected void handleSingleMedia(ISourceableLoaderConfiguration config, ContainerDto<MediaDto2> container, Media media) {

        MediaDto2 dto = new MediaDto2();
        loadBaseData(config, media, dto);

        //media
        dto.setLabel(media.getTitleCache());
        //TODO i18n
        List<Language> languages = new ArrayList<>();
        LanguageString description = MultilanguageTextHelper.getPreferredLanguageString(media.getAllDescriptions(), languages);
        dto.setDescription(description == null ? null : description.getText());

        //representations
        ContainerDto<MediaRepresentationDTO> representations = new ContainerDto<>();
        for (MediaRepresentation rep : media.getRepresentations()) {
            MediaRepresentationDTO repDto = new MediaRepresentationDTO();
            loadBaseData(config, rep, repDto);
            repDto.setMimeType(rep.getMimeType());
            repDto.setSuffix(rep.getSuffix());
            if (!rep.getParts().isEmpty()) {
                //TODO handle message if n(parts) > 1
                MediaRepresentationPart part = rep.getParts().get(0);
                repDto.setUri(part.getUri());
                repDto.setClazz(part.getClass().getSimpleName());
                repDto.setSize(part.getSize());
                if (part.isInstanceOf(ImageFile.class)) {
                    ImageFile image = CdmBase.deproxy(part, ImageFile.class);
                    repDto.setHeight(image.getHeight());
                    repDto.setWidth(image.getWidth());
                }
                //TODO AudioFile etc.
            }
            representations.addItem(repDto);
        }
        if (representations.getCount() > 0) {
            dto.setRepresentations(representations);
        }
        //TODO load representation data
        container.addItem(dto);
    }
}