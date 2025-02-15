/**
* Copyright (C) 2024 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.name;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;

import eu.etaxonomy.cdm.api.service.name.TypeDesignationGroup.TypeDesignationSetType;
import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.common.URI;
import eu.etaxonomy.cdm.compare.name.NullTypeDesignationStatus;
import eu.etaxonomy.cdm.hibernate.HibernateProxyHelper;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.IdentifiableSource;
import eu.etaxonomy.cdm.model.common.VersionableEntity;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignation;
import eu.etaxonomy.cdm.model.name.TypeDesignationBase;
import eu.etaxonomy.cdm.model.name.TypeDesignationStatusBase;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnit;
import eu.etaxonomy.cdm.model.occurrence.FieldUnit;
import eu.etaxonomy.cdm.model.occurrence.MediaSpecimen;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;
import eu.etaxonomy.cdm.ref.TypedEntityReference;
import eu.etaxonomy.cdm.ref.TypedEntityReferenceFactory;
import eu.etaxonomy.cdm.strategy.cache.TagEnum;
import eu.etaxonomy.cdm.strategy.cache.TaggedTextBuilder;
import eu.etaxonomy.cdm.strategy.cache.TaggedTextWithLink;
import eu.etaxonomy.cdm.strategy.cache.occurrence.DerivedUnitDefaultCacheStrategy;
import eu.etaxonomy.cdm.strategy.cache.occurrence.FieldUnitDefaultCacheStrategy;

/**
 * @author muellera
 * @since 22.04.2024
 */
public class SpecimenTypeDesignationGroupFormatter
        extends TypeDesignationGroupFormatterBase<SpecimenOrObservationBase<?>> {

    public static final SpecimenTypeDesignationGroupFormatter INSTANCE() {
        return new SpecimenTypeDesignationGroupFormatter();
    }

    public void format(TaggedTextBuilder finalBuilder, TypeDesignationGroupContainer container,
            Map<VersionableEntity,TypeDesignationGroup> orderedBaseEntity2TypesMap,
            int typeSetCount,
            TypeDesignationGroupFormatterConfiguration config,
            SpecimenOrObservationBase<?> sob, TypeDesignationSetType lastWsType) {

        TypeDesignationGroup typeDesignationGroup = orderedBaseEntity2TypesMap.get(sob);

        TaggedTextBuilder localBuilder = new TaggedTextBuilder();

        //TODO why is typeDesingationSet not a list
        List<TypeDesignationStatusBase<?>> statusList = new ArrayList<>(typeDesignationGroup.keySet());
        statusList.sort(statusComparator);


        if(typeSetCount > 0){
            localBuilder.add(TagEnum.separator, TYPE_SEPARATOR);
        }else if (config.isWithStartingTypeLabel()
                && (!config.isWithPrecedingMainType() || !hasNotNullStatus(statusList))
                || sob == TypeDesignationGroupContainer.NOT_DESIGNATED){
            //TODO this is not really exact as we may want to handle specimen types and
            //name types separately, but this is such a rare case (if at all) and
            //increases complexity so it is not yet implemented
            boolean isPlural = hasMultipleTypes(orderedBaseEntity2TypesMap);
            String label = (isPlural? "Type": "Type");  // for now we do not distinguish if multiple groups exist, this may change in future:  #9596#note-45
            localBuilder.add(TagEnum.label, label);
            localBuilder.add(TagEnum.postSeparator, ": ");
        }


        boolean hasPrecedingStatusLabel = config.isWithPrecedingMainType() && hasNotNullStatus(statusList);
        if (hasPrecedingStatusLabel){
            addStatusLabel(config, localBuilder, typeDesignationGroup, statusList.get(0), container,
                    lastWsType, typeSetCount, true, true);
        }

        if (sob == TypeDesignationGroupContainer.NOT_DESIGNATED) {
            localBuilder.add(TagEnum.typeDesignation, "not designated");
            typeDesignationGroup.getTypeDesignations().stream().forEach(tdDTO->{
                TypeDesignationBase<?> typeDesig =  container.findTypeDesignation(tdDTO.getUuid());
                if (config.isWithCitation()) {
                    handleGeneralSource(typeDesig, localBuilder, config);
                }
            });
        }else {

            boolean hasExplicitBaseEntity = hasExplicitBaseEntity(sob, typeDesignationGroup)
                    && !entityLabel(sob, config).isEmpty();  //current literature media specimen do often have an empty field unit attached as this is the only way to create them #10426, #10425
            if(hasExplicitBaseEntity && !entityLabel(sob, config).isEmpty()){
                localBuilder.add(TagEnum.specimenOrObservation, entityLabel(sob, config), sob);
            }
            int typeStatusCounter = 0;
            if (config.isWithBrackets() && hasExplicitBaseEntity){
                localBuilder.add(TagEnum.separator, TYPE_STATUS_PARENTHESIS_LEFT);
            }
            for(TypeDesignationStatusBase<?> typeStatus : statusList) {
                typeStatusCounter = buildTaggedTextForSingleTypeStatus(container, localBuilder,
                        typeDesignationGroup, typeStatusCounter, statusList.size(), typeStatus,
                        lastWsType, typeSetCount, hasPrecedingStatusLabel, config);
            }
            if (config.isWithBrackets() && hasExplicitBaseEntity){
                localBuilder.add(TagEnum.separator, TYPE_STATUS_PARENTHESIS_RIGHT);
            }
        }

        typeDesignationGroup.setRepresentation(localBuilder.toString());
        finalBuilder.addAll(localBuilder);
        return;
    }

    /**
     * Returns <code>true</code> if the list is not empty and if there is not an
     * only status being the Null-Status placeholder.
     */
    private boolean hasNotNullStatus(List<TypeDesignationStatusBase<?>> statusList) {
        if (CdmUtils.isNullSafeEmpty(statusList)) {
            return false;
        }else if (statusList.size() == 1 && statusList.get(0) == NullTypeDesignationStatus.SINGLETON()) {
            return false;
        }else {
            return true;
        }
    }

    /**
     * Checks if the baseType is the same as the (only?) type in the type designation set.
     */
    private boolean hasExplicitBaseEntity(SpecimenOrObservationBase<?> sob,
            TypeDesignationGroup typeDesignationGroup) {

        UUID baseUuid = sob.getUuid();
        for (TypeDesignationDTO<?> dto: typeDesignationGroup.getTypeDesignations()){
            if (!baseUuid.equals(dto.getTypeUuid())){
                return true;
            }
        }

        return false;
    }

    @Override
    protected String entityLabel(SpecimenOrObservationBase<?> sob,
            TypeDesignationGroupFormatterConfiguration config) {

        if (sob.isInstanceOf(FieldUnit.class)) {
            FieldUnitDefaultCacheStrategy formatter = FieldUnitDefaultCacheStrategy.NewInstance(false, true, false);
            String label = formatter.getTitleCache(CdmBase.deproxy(sob, FieldUnit.class), true);
            if (label.startsWith("FieldUnit#")) {
                return "";
            }else {
                return label;
            }
        } else {
            return sob.getTitleCache();
        }
    }

    @Override
    protected void buildTaggedTextForTypeDesignationBase(TypeDesignationBase<?> td,
            TaggedTextBuilder workingsetBuilder, TypeDesignationGroupFormatterConfiguration config) {

        TypedEntityReference<?> typeDesignationEntity = TypedEntityReferenceFactory.fromEntity(td, false);
        if (td instanceof SpecimenTypeDesignation){
            buildTaggedTextForSpecimenTypeDesignation((SpecimenTypeDesignation)td, workingsetBuilder, typeDesignationEntity, config);
        }else{
            throw new RuntimeException("Unhandled TypeDesignation type");
        }
    }

    private void buildTaggedTextForSpecimenTypeDesignation(SpecimenTypeDesignation td,
            TaggedTextBuilder builder, TypedEntityReference<?> typeDesignationEntity, TypeDesignationGroupFormatterConfiguration config) {

        if (td.getTypeSpecimen() == null){
            builder.add(TagEnum.typeDesignation, "", typeDesignationEntity);
        }else{
            DerivedUnit du = td.getTypeSpecimen();
            if(du.isProtectedTitleCache()){
                //protected title cache
                builder.add(TagEnum.typeDesignation, du.getTitleCache(), typeDesignationEntity);
            } else {
                du = HibernateProxyHelper.deproxy(du);
                boolean isMediaSpecimen = du instanceof MediaSpecimen;
                String icon = (isMediaSpecimen ? "[icon] " : "");

                //media specimen with media and source (= literature media specimen)
                if(isMediaSpecimen
                            && HibernateProxyHelper.deproxyOrNull(du.getCollection()) == null  //TODO not sure if only checking the collection is enough, but as we also check existence of sources now the case that only an accession number exists is also covered here
                            && ((MediaSpecimen)du).getMediaSpecimen() != null
                            && !((MediaSpecimen)du).getMediaSpecimen().getSources().isEmpty()
                        ){
                    // special case of a published image which is not covered by the DerivedUnitFacadeCacheStrategy
                    Media media = ((MediaSpecimen)du).getMediaSpecimen();
                    String mediaTitle = media.getTitle() == null ? "" : media.getTitle().getText();
                    String specimenLabel = CdmUtils.concat(" ", "[icon]", mediaTitle, "in");
                    builder.add(TagEnum.typeDesignation, specimenLabel, typeDesignationEntity); //TODO how to better use tagged text here, the type designation itself has no real text; we could include the sources but that makes them unusable as sources :-(
                    int count = 0;
                    for(IdentifiableSource source : media.getSources()){
                        //TODO add sourceTypes to configuration
                        if (source.getType().isPublicSource()){
                            if (count++ > 0){
                                builder.add(TagEnum.separator, TypeDesignationGroupContainerFormatter.SOURCE_SEPARATOR);
                            }
                            addSource(builder, source);
                        }
                    }
                //other specimen
                } else {
                    //TODO split collection and field number and specimen status into their own tags
                    //     in cache strategy, use TaggedText there for this part
                    DerivedUnitDefaultCacheStrategy.CollectionAccessionSeperator sep
                        = config.isWithAccessionNoType()?
                                DerivedUnitDefaultCacheStrategy.CollectionAccessionSeperator.ACCESION_NO_TYPE
                                : DerivedUnitDefaultCacheStrategy.CollectionAccessionSeperator.SPACE;
                    DerivedUnitDefaultCacheStrategy cacheStrategy = DerivedUnitDefaultCacheStrategy.NewInstance(true, false, false, sep);
                    String titleCache = icon + cacheStrategy.getTitleCache(du, true);
                    // removing parentheses from code + accession number, see https://dev.e-taxonomy.eu/redmine/issues/8365
                    titleCache = titleCache.replaceAll("[\\(\\)]", "");
                    URI link = getLink(du);  //

                    if (link != null) {
                        //if the specimen has a link we split the specimen text and try to
                        //add the link to the accession number
                        String linkedText = cacheStrategy.getUnitNumber(du);
                        if (StringUtils.isBlank(linkedText)) {
                            linkedText = cacheStrategy.getCollectionCode(du);
                        }

                        if (StringUtils.isNotBlank(linkedText) && titleCache.contains(linkedText)) {
                            int pos = titleCache.indexOf(linkedText);
                            String before = titleCache.substring(0, pos);
                            String after = titleCache.substring(pos + linkedText.length());
                            if (StringUtils.isNoneEmpty(before)) {
                                builder.add(TagEnum.typeDesignation, before.trim());
                            }
                            TaggedTextWithLink taggedTextWithLink = TaggedTextWithLink.NewInstance(
                                    TagEnum.typeDesignation, linkedText, typeDesignationEntity, null, link);
                            builder.add(taggedTextWithLink);
                            if (StringUtils.isNotBlank(after)) {
                                if (after.startsWith(", ")) {
                                    builder.addSeparator(", ");
                                    after = after.substring(2);
                                }
                                builder.add(TagEnum.typeDesignation, after);
                            }
                        }else {
                            TaggedTextWithLink taggedTextWithLink = TaggedTextWithLink.NewInstance(
                                    TagEnum.typeDesignation, titleCache, typeDesignationEntity, null, link);
                            builder.add(taggedTextWithLink);
                        }
                    }else {
                        builder.add(TagEnum.typeDesignation, titleCache, typeDesignationEntity);
                    }
                }
            } //protected titleCache
        }//fi specimen == null

        if(td.isNotDesignated()){
            //this should not happen together with a defined specimen, therefore we may handle it in a separate tag
            builder.add(TagEnum.typeDesignation, "not designated", typeDesignationEntity);
        }
    }

    private static URI getLink(DerivedUnit du) {
        if (du.getPreferredStableUri() != null) {
            return du.getPreferredStableUri();
        }else {
            //TODO there may come more link options
            return null;
        }
    }
}