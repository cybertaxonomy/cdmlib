/**
* Copyright (C) 2024 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.name;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import eu.etaxonomy.cdm.api.service.name.TypeDesignationSet.TypeDesignationSetType;
import eu.etaxonomy.cdm.hibernate.HibernateProxyHelper;
import eu.etaxonomy.cdm.model.common.IdentifiableSource;
import eu.etaxonomy.cdm.model.common.VersionableEntity;
import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignation;
import eu.etaxonomy.cdm.model.name.TypeDesignationBase;
import eu.etaxonomy.cdm.model.name.TypeDesignationStatusBase;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnit;
import eu.etaxonomy.cdm.model.occurrence.MediaSpecimen;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;
import eu.etaxonomy.cdm.ref.TypedEntityReference;
import eu.etaxonomy.cdm.ref.TypedEntityReferenceFactory;
import eu.etaxonomy.cdm.ref.TypedEntityReferenceWithLink;
import eu.etaxonomy.cdm.strategy.cache.TagEnum;
import eu.etaxonomy.cdm.strategy.cache.TaggedTextBuilder;
import eu.etaxonomy.cdm.strategy.cache.occurrence.DerivedUnitDefaultCacheStrategy;

/**
 * @author muellera
 * @since 22.04.2024
 */
public class SpecimenTypeDesignationSetFormatter extends TypeDesignationSetFormatterBase<SpecimenOrObservationBase> {

    public static final SpecimenTypeDesignationSetFormatter INSTANCE() {
        return new SpecimenTypeDesignationSetFormatter();
    }

    public void format(TaggedTextBuilder finalBuilder, TypeDesignationSetContainer manager,
            Map<VersionableEntity,TypeDesignationSet> orderedBaseEntity2TypesMap,
            int typeSetCount,
            TypeDesignationSetFormatterConfiguration config,
            SpecimenOrObservationBase<?> sob, TypeDesignationSetType lastWsType) {

        TypeDesignationSet typeDesignationSet = orderedBaseEntity2TypesMap.get(sob);

        TaggedTextBuilder localBuilder = new TaggedTextBuilder();

        //TODO why is typeDesingationSet not a list
        List<TypeDesignationStatusBase<?>> statusList = new ArrayList<>(typeDesignationSet.keySet());
        statusList.sort(statusComparator);


        if(typeSetCount > 0){
            localBuilder.add(TagEnum.separator, TYPE_SEPARATOR);
        }else if (config.isWithStartingTypeLabel()
                && !config.isWithPrecedingMainType() || sob == TypeDesignationSetContainer.NOT_DESIGNATED){
            //TODO this is not really exact as we may want to handle specimen types and
            //name types separately, but this is such a rare case (if at all) and
            //increases complexity so it is not yet implemented
            boolean isPlural = hasMultipleTypes(orderedBaseEntity2TypesMap);
            localBuilder.add(TagEnum.label, (isPlural? "Types": "Type"));
            localBuilder.add(TagEnum.postSeparator, ": ");
        }


        boolean hasPrecedingStatusLabel = config.isWithPrecedingMainType() && !statusList.isEmpty();
        if (hasPrecedingStatusLabel){
            addStatusLabel(localBuilder, typeDesignationSet, statusList.get(0), lastWsType, typeSetCount, true);
        }

        if (sob == TypeDesignationSetContainer.NOT_DESIGNATED) {
            localBuilder.add(TagEnum.typeDesignation, "not designated");
            typeDesignationSet.getTypeDesignations().stream().forEach(tdDTO->{
                TypeDesignationBase<?> typeDesig =  manager.findTypeDesignation(tdDTO.getUuid());
                if (config.isWithCitation()) {
                    handleGeneralSource(typeDesig, localBuilder, config);
                }
            });
        }else {

            boolean hasExplicitBaseEntity = hasExplicitBaseEntity(sob, typeDesignationSet);
            if(hasExplicitBaseEntity && !entityLabel(sob, config).isEmpty()){
                localBuilder.add(TagEnum.specimenOrObservation, entityLabel(sob, config), sob);
            }
            int typeStatusCount = 0;
            if (config.isWithBrackets() && hasExplicitBaseEntity){
                localBuilder.add(TagEnum.separator, TYPE_STATUS_PARENTHESIS_LEFT);
            }
            for(TypeDesignationStatusBase<?> typeStatus : statusList) {
                typeStatusCount = buildTaggedTextForSingleTypeStatus(manager, localBuilder,
                        typeDesignationSet, typeStatusCount, typeStatus,
                        lastWsType, typeSetCount, hasPrecedingStatusLabel, config);
            }
            if (config.isWithBrackets() && hasExplicitBaseEntity){
                localBuilder.add(TagEnum.separator, TYPE_STATUS_PARENTHESIS_RIGHT);
            }
        }

        typeDesignationSet.setRepresentation(localBuilder.toString());
        finalBuilder.addAll(localBuilder);
        return;
    }

    /**
     * Checks if the baseType is the same as the (only?) type in the type designation workingset.
     */
    private boolean hasExplicitBaseEntity(SpecimenOrObservationBase<?> sob,
            TypeDesignationSet typeDesignationSet) {

        UUID baseUuid = sob.getUuid();
        for (TypeDesignationDTO<?> dto: typeDesignationSet.getTypeDesignations()){
            if (!baseUuid.equals(dto.getTypeUuid())){
                return true;
            }
        }

        return false;
    }

    @Override
    protected String entityLabel(SpecimenOrObservationBase sob, TypeDesignationSetFormatterConfiguration config) {
        return sob.getTitleCache();
    }

//    int buildTaggedTextForSingleTypeStatus(TypeDesignationSetContainer manager,
//            TaggedTextBuilder workingsetBuilder, TypeDesignationSet typeDesignationSet,
//            int typeStatusCount, TypeDesignationStatusBase<?> typeStatus,
//            TypeDesignationSetType lastWsType, int typeSetCount, boolean hasPrecedingStatusLabel,
//            boolean withCitation
//            ) {
//
//        //starting separator
//        if(typeStatusCount++ > 0){
//            workingsetBuilder.add(TagEnum.separator, TypeDesignationSetContainerFormatter.TYPE_STATUS_SEPARATOR);
//        }
//        boolean statusLabelPreceding = hasPrecedingStatusLabel && typeStatusCount == 1;
//
//        //status label
//        if (!statusLabelPreceding) {
//            addStatusLabel(workingsetBuilder, typeDesignationSet, typeStatus, lastWsType, typeSetCount, false);
//        }
//
//        //designation + sources
//        int typeDesignationCount = 0;
//        for(TypeDesignationDTO<?> typeDesignationDTO : createSortedList(typeDesignationSet, typeStatus)) {
//            TypeDesignationBase<?> typeDes = manager.findTypeDesignation(typeDesignationDTO.getUuid());
//
//            typeDesignationCount = buildTaggedTextForSingleType(typeDes, withCitation,
//                    workingsetBuilder, typeDesignationCount);
//        }
//        return typeStatusCount;
//    }

    @Override
    protected void buildTaggedTextForTypeDesignationBase(TypeDesignationBase<?> td,
            TaggedTextBuilder workingsetBuilder) {

        TypedEntityReference<?> typeDesignationEntity = TypedEntityReferenceFactory.fromEntity(td, false);
//        if(td instanceof NameTypeDesignation){
//            buildTaggedTextForNameTypeDesignation((NameTypeDesignation)td, workingsetBuilder, typeDesignationEntity);
//        } else if (td instanceof TextualTypeDesignation){
//            buildTaggedTextForTextualTypeDesignation((TextualTypeDesignation)td, workingsetBuilder, typeDesignationEntity);
//        } else
        if (td instanceof SpecimenTypeDesignation){
            buildTaggedTextForSpecimenTypeDesignation((SpecimenTypeDesignation)td, workingsetBuilder, typeDesignationEntity);
        }else{
            throw new RuntimeException("Unhandled TypeDesignation type");
        }
    }

    private void buildTaggedTextForSpecimenTypeDesignation(SpecimenTypeDesignation td,
            TaggedTextBuilder builder, TypedEntityReference<?> typeDesignationEntity) {

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
                String typeSpecimenTitle = (isMediaSpecimen ? "[icon] " : "");

                //media specimen
                if(isMediaSpecimen
                            && HibernateProxyHelper.deproxyOrNull(du.getCollection()) == null  //TODO not sure if only checking the collection is enough, but as we also check existence of sources now the case that only an accession number exists is also covered here
                            && ((MediaSpecimen)du).getMediaSpecimen() != null
                            && !((MediaSpecimen)du).getMediaSpecimen().getSources().isEmpty()
                        ){
                    // special case of a published image which is not covered by the DerivedUnitFacadeCacheStrategy
                    builder.add(TagEnum.typeDesignation, "[icon] in", typeDesignationEntity); //TODO how to better use tagged text here, the type designation itself has no real text; we could include the sources but that makes them unusable as sources :-(
                    MediaSpecimen msp = (MediaSpecimen)du;
                    int count = 0;
                    for(IdentifiableSource source : msp.getMediaSpecimen().getSources()){
                        if (source.getType().isPublicSource()){
                            if (count++ > 0){
                                builder.add(TagEnum.separator, TypeDesignationSetContainerFormatter.SOURCE_SEPARATOR);
                            }
                            addSource(builder, source);
                        }
                    }
                //other specimen
                } else {
                    DerivedUnitDefaultCacheStrategy cacheStrategy = DerivedUnitDefaultCacheStrategy.NewInstance(true, false, true, " ");
                    String titleCache = cacheStrategy.getTitleCache(du, true);
                    // removing parentheses from code + accession number, see https://dev.e-taxonomy.eu/redmine/issues/8365
                    titleCache = titleCache.replaceAll("[\\(\\)]", "");
                    typeSpecimenTitle += titleCache;
                    URI link = getLink(du);  //
                    TypedEntityReferenceWithLink entity = new TypedEntityReferenceWithLink(du.getClass(),
                            du.getUuid(), typeSpecimenTitle, link);
                    builder.add(TagEnum.typeDesignation, typeSpecimenTitle, typeDesignationEntity);
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
            return du.getPreferredStableUri().getJavaUri();
        }else {
            return null;
        }
    }

}
