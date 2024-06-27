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

import eu.etaxonomy.cdm.api.service.name.TypeDesignationGroup.TypeDesignationSetType;
import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.model.common.VersionableEntity;
import eu.etaxonomy.cdm.model.name.NameTypeDesignation;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.name.TypeDesignationBase;
import eu.etaxonomy.cdm.model.name.TypeDesignationStatusBase;
import eu.etaxonomy.cdm.ref.TypedEntityReference;
import eu.etaxonomy.cdm.ref.TypedEntityReferenceFactory;
import eu.etaxonomy.cdm.strategy.cache.TagEnum;
import eu.etaxonomy.cdm.strategy.cache.TaggedTextBuilder;
import eu.etaxonomy.cdm.strategy.cache.name.INameCacheStrategy;

/**
 * @author muellera
 * @since 22.04.2024
 */
public class NameTypeDesignationGroupFormatter extends TypeDesignationGroupFormatterBase<NameTypeDesignation> {

    public static final NameTypeDesignationGroupFormatter INSTANCE() {
        return new NameTypeDesignationGroupFormatter();
    }

    public void format(TaggedTextBuilder finalBuilder, TypeDesignationGroupContainer container,
            Map<VersionableEntity,TypeDesignationGroup> orderedBaseEntity2TypesMap,
            int typeSetCount,
            TypeDesignationGroupFormatterConfiguration config,
            NameTypeDesignation baseEntity, TypeDesignationSetType lastWsType) {

        TypeDesignationGroup typeDesignationGroup = orderedBaseEntity2TypesMap.get(baseEntity);

        TaggedTextBuilder localBuilder = new TaggedTextBuilder();
        if(typeSetCount > 0){
            localBuilder.add(TagEnum.separator, TYPE_SEPARATOR);
        }else if (config.isWithStartingTypeLabel() &&
                (!config.isWithPrecedingMainType() || baseEntity.getTypeStatus() == null )){
            //TODO this is not really exact as we may want to handle specimen types and
            //name types separately, but this is such a rare case (if at all) and
            //increases complexity so it is not yet implemented
            boolean isPlural = hasMultipleTypes(orderedBaseEntity2TypesMap);
            String label = (isPlural? "Type": "Type");  // for now we do not distinguish if multiple groups exist, this may change in future:  #9596#note-45
            localBuilder.add(TagEnum.label, label);
            localBuilder.add(TagEnum.postSeparator, ": ");
        }

        //TODO why is typeDesingationSet not a list
        List<TypeDesignationStatusBase<?>> statusList = new ArrayList<>(typeDesignationGroup.keySet());
        statusList.sort(statusComparator);

        boolean hasPrecedingStatusLabel = config.isWithPrecedingMainType() && !statusList.isEmpty();
        if (hasPrecedingStatusLabel){
            addStatusLabel(config, localBuilder, typeDesignationGroup, statusList.get(0), container,
                    lastWsType, typeSetCount, true, true);
        }

        boolean hasExplicitBaseEntity = hasExplicitBaseEntity(baseEntity, typeDesignationGroup);
        if(hasExplicitBaseEntity && !entityLabel(baseEntity, config).isEmpty()){
            localBuilder.add(TagEnum.specimenOrObservation, entityLabel(baseEntity, config), baseEntity);
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
        typeDesignationGroup.setRepresentation(localBuilder.toString());
        finalBuilder.addAll(localBuilder);
        return;

    }

    @Override
    protected void buildTaggedTextForTypeDesignationBase(TypeDesignationBase<?> td,
            TaggedTextBuilder workingsetBuilder, TypeDesignationGroupFormatterConfiguration config) {

        TypedEntityReference<?> typeDesignationEntity = TypedEntityReferenceFactory.fromEntity(td, false);
        if(td instanceof NameTypeDesignation){
            buildTaggedTextForNameTypeDesignation((NameTypeDesignation)td, workingsetBuilder, typeDesignationEntity);
//        } else if (td instanceof TextualTypeDesignation){
//            buildTaggedTextForTextualTypeDesignation((TextualTypeDesignation)td, workingsetBuilder, typeDesignationEntity);
//        } else if (td instanceof SpecimenTypeDesignation){
//            buildTaggedTextForSpecimenTypeDesignation((SpecimenTypeDesignation)td, workingsetBuilder, typeDesignationEntity);
        }else{
            throw new RuntimeException("Unhandled TypeDesignation type");
        }
    }

    private void buildTaggedTextForNameTypeDesignation(NameTypeDesignation td, TaggedTextBuilder workingsetBuilder,
            TypedEntityReference<?> typeDesignationEntity) {

        TaxonName typeName = td.getTypeName();
        if (typeName != null){
            INameCacheStrategy formatter = typeName.cacheStrategy();
            workingsetBuilder.addAll(formatter.getTaggedTitle(typeName));
            workingsetBuilder.addAll(formatter.getNomStatusTags(typeName, true, false));
        }

        String flags = null;

        if(td.isNotDesignated()){
            flags = "not designated";
        }
        if(td.isRejectedType()){
            flags = CdmUtils.concat(", ", flags, "rejected");
        }
        if(td.isConservedType()){
            flags = CdmUtils.concat(", ", flags, "conserved");
        }
        if (flags != null){
            workingsetBuilder.add(TagEnum.typeDesignation, flags, typeDesignationEntity);
        }
    }

    /**
     * Checks if the baseType is the same as the (only?) type in the type designation workingset.
     */
    private boolean hasExplicitBaseEntity(NameTypeDesignation ntd,
            TypeDesignationGroup typeDesignationGroup) {

        return false;
    }

    @Override
    protected String entityLabel(NameTypeDesignation ntd, TypeDesignationGroupFormatterConfiguration config) {
        return "";  //TODO correct, or ntd.toString() ?
    }
}