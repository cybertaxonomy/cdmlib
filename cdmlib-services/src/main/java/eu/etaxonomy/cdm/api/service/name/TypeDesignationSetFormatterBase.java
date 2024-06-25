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
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import eu.etaxonomy.cdm.api.service.name.TypeDesignationSet.TypeDesignationSetType;
import eu.etaxonomy.cdm.compare.name.TypeDesignationStatusComparator;
import eu.etaxonomy.cdm.format.reference.OriginalSourceFormatter;
import eu.etaxonomy.cdm.model.common.IdentifiableSource;
import eu.etaxonomy.cdm.model.common.VersionableEntity;
import eu.etaxonomy.cdm.model.name.TypeDesignationBase;
import eu.etaxonomy.cdm.model.name.TypeDesignationStatusBase;
import eu.etaxonomy.cdm.model.reference.OriginalSourceBase;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.strategy.cache.TagEnum;
import eu.etaxonomy.cdm.strategy.cache.TaggedText;
import eu.etaxonomy.cdm.strategy.cache.TaggedTextBuilder;

/**
 * @author muellera
 * @since 22.04.2024
 */
public abstract class TypeDesignationSetFormatterBase<T extends VersionableEntity> {

    static final String TYPE_SEPARATOR = "; ";
    static final String TYPE_STATUS_PARENTHESIS_LEFT = " (";
    static final String TYPE_STATUS_PARENTHESIS_RIGHT = ")";

    protected static TypeDesignationStatusComparator statusComparator = new TypeDesignationStatusComparator<>();

    /**
     * Returns <code>true</code> if the working set has either multiple working sets
     * or if it has a single working set but this workingset has multiple type designations.
     */
    protected boolean hasMultipleTypes(
            Map<VersionableEntity,TypeDesignationSet> typeWorkingSets) {

        if (typeWorkingSets == null || typeWorkingSets.isEmpty()){
            return false;
        }else if (typeWorkingSets.keySet().size() > 1) {
            return true;
        }
        TypeDesignationSet singleSet = typeWorkingSets.values().iterator().next();
        return singleSet.getTypeDesignations().size() > 1;
    }

    protected void addStatusLabel(TaggedTextBuilder builder, TypeDesignationSet typeDesignationSet,
            TypeDesignationStatusBase<?> typeStatus, TypeDesignationSetType lastWsType,
            int typeSetCount, boolean capitalize) {

        boolean isPlural = typeDesignationSet.get(typeStatus).size() > 1;
        String statusLabel = null;
        if(typeStatus != TypeDesignationSet.NULL_STATUS){
            statusLabel = typeStatus.getLabel();
        }else if (typeDesignationSet.getWorkingsetType() != lastWsType
                && (builder.size() > 0 && typeSetCount > 0 )){
            //only for the first name type (coming after a specimen type add the label (extremely rare case, if at all existing)
            if (typeDesignationSet.getWorkingsetType().isNameType()) {
                statusLabel = "nametype";
            }else if (typeDesignationSet.getWorkingsetType().isSpecimenType()) {
                statusLabel = "type";
            }
        }
        if (statusLabel != null){
            statusLabel = (isPlural ? statusLabel + "s" : statusLabel);
            if (builder.size() == 0 || capitalize){
                statusLabel = StringUtils.capitalize(statusLabel);
            }
            builder.add(TagEnum.label, statusLabel);
            builder.add(TagEnum.postSeparator, TypeDesignationSetContainerFormatter.POST_STATUS_SEPARATOR);
        }
    }

    protected int buildTaggedTextForSingleTypeStatus(TypeDesignationSetContainer container,
            TaggedTextBuilder builder, TypeDesignationSet typeDesignationSet,
            int typeStatusCount, TypeDesignationStatusBase<?> typeStatus,
            TypeDesignationSetType lastWsType, int typeSetCount, boolean hasPrecedingStatusLabel,
            TypeDesignationSetFormatterConfiguration config
            ) {

        //starting separator
        if(typeStatusCount++ > 0){
            builder.add(TagEnum.separator, TypeDesignationSetContainerFormatter.TYPE_STATUS_SEPARATOR);
        }
        boolean statusLabelPreceding = hasPrecedingStatusLabel && typeStatusCount == 1 /*check if is first */;

        //status label - only if it has not been added before already
        if (!statusLabelPreceding) {
            addStatusLabel(builder, typeDesignationSet, typeStatus, lastWsType, typeSetCount, false);
        }

        //designation + sources
        int typeDesignationCount = 0;
        for(TypeDesignationDTO<?> typeDesignationDTO : createSortedList(typeDesignationSet, typeStatus)) {
            //"revert" DTO to entity
            TypeDesignationBase<?> typeDes = container.findTypeDesignation(typeDesignationDTO.getUuid());

            typeDesignationCount = buildTaggedTextForSingleType(typeDes, config,
                    builder, typeDesignationCount);
        }
        return typeStatusCount;
    }

    private List<TypeDesignationDTO> createSortedList(
            TypeDesignationSet typeDesignationSet, TypeDesignationStatusBase<?> typeStatus) {

        List<TypeDesignationDTO> typeDesignationDTOs = new ArrayList<>(typeDesignationSet.get(typeStatus));
        Collections.sort(typeDesignationDTOs);
        return typeDesignationDTOs;
    }

    protected int buildTaggedTextForSingleType(TypeDesignationBase<?> typeDes,
            TypeDesignationSetFormatterConfiguration config,
            TaggedTextBuilder builder, int typeDesignationCount) {

        if(typeDesignationCount++ > 0){
            builder.add(TagEnum.separator, TypeDesignationSetContainerFormatter.TYPE_DESIGNATION_SEPARATOR);
        }
        buildTaggedTextForTypeDesignationBase(typeDes, builder, config);
        if (config.isWithCitation()){

            //lectotype source
            handleLectotypeSource(typeDes, builder, config);
            handleGeneralSource(typeDes, builder, config);
        }

        return typeDesignationCount;
    }

    private void handleLectotypeSource(TypeDesignationBase<?> typeDes, TaggedTextBuilder workingsetBuilder,
            TypeDesignationSetFormatterConfiguration config) {

        OriginalSourceBase lectoSource = typeDes.getDesignationSource();
        if (hasLectoSource(typeDes)){
            if (config.getSourceTypeFilter() == null || config.getSourceTypeFilter().contains(typeDes.getDesignationSource().getType())) {
                workingsetBuilder.add(TagEnum.separator, TypeDesignationSetContainerFormatter.REFERENCE_DESIGNATED_BY);
                addSource(workingsetBuilder, lectoSource);
            }
        }
    }

    protected void handleGeneralSource(TypeDesignationBase<?> typeDes,
            TaggedTextBuilder workingsetBuilder, TypeDesignationSetFormatterConfiguration config) {

        //general sources
        if (!typeDes.getSources().isEmpty()) {
            workingsetBuilder.add(TagEnum.separator,
                    TypeDesignationSetContainerFormatter.REFERENCE_PARENTHESIS_LEFT + TypeDesignationSetContainerFormatter.REFERENCE_FIDE);
            int count = 0;
            for (IdentifiableSource source: typeDes.getSources()){
                if (config.getSourceTypeFilter() == null || config.getSourceTypeFilter().contains(source.getType())) {
                    if (count++ > 0){
                        workingsetBuilder.add(TagEnum.separator, TypeDesignationSetContainerFormatter.SOURCE_SEPARATOR);
                    }
                    addSource(workingsetBuilder, source);
                }
            }
            workingsetBuilder.add(TagEnum.separator, TypeDesignationSetContainerFormatter.REFERENCE_PARENTHESIS_RIGHT);
        }
    }

    protected abstract void buildTaggedTextForTypeDesignationBase(TypeDesignationBase<?> typeDes,
            TaggedTextBuilder workingsetBuilder, TypeDesignationSetFormatterConfiguration config);

    /**
     * Adds the tags for the given source.
     */
    protected void addSource(TaggedTextBuilder workingsetBuilder,
            OriginalSourceBase source) {
        Reference ref = source.getCitation();
        if (ref != null){
            String citation = OriginalSourceFormatter.INSTANCE.format(source);
            workingsetBuilder.add(TaggedText.NewReferenceInstance(TagEnum.reference, citation, ref));
        }
    }

    private static boolean hasLectoSource(TypeDesignationBase<?> typeDes) {
        return typeDes.getDesignationSource() != null &&
                    (typeDes.getDesignationSource().getCitation() != null
                      || isNotBlank(typeDes.getDesignationSource().getCitationMicroReference())
                     );
    }

    private static boolean isNotBlank(String str){
        return StringUtils.isNotBlank(str);
    }

    protected abstract String entityLabel(T baseEntity, TypeDesignationSetFormatterConfiguration config);

    protected static TypeDesignationSetFormatterBase getFormatter(TypeDesignationSet tds) {
        if (tds.isSpecimenWorkingSet()) {
            return SpecimenTypeDesignationSetFormatter.INSTANCE();
        }else if (tds.isNameWorkingSet()) {
            return NameTypeDesignationSetFormatter.INSTANCE();
        }else {
            return TextualTypeDesignationSetFormatter.INSTANCE();
        }
    }
}
