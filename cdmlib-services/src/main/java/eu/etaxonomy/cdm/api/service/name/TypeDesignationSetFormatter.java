/**
* Copyright (C) 2020 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.name;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;

import eu.etaxonomy.cdm.api.facade.DerivedUnitFacadeCacheStrategy;
import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.common.UTF8;
import eu.etaxonomy.cdm.format.reference.OriginalSourceFormatter;
import eu.etaxonomy.cdm.hibernate.HibernateProxyHelper;
import eu.etaxonomy.cdm.model.common.IdentifiableSource;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.VersionableEntity;
import eu.etaxonomy.cdm.model.name.NameTypeDesignation;
import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignation;
import eu.etaxonomy.cdm.model.name.TextualTypeDesignation;
import eu.etaxonomy.cdm.model.name.TypeDesignationBase;
import eu.etaxonomy.cdm.model.name.TypeDesignationStatusBase;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnit;
import eu.etaxonomy.cdm.model.occurrence.MediaSpecimen;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;
import eu.etaxonomy.cdm.model.reference.OriginalSourceBase;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.ref.TypedEntityReference;
import eu.etaxonomy.cdm.strategy.cache.HTMLTagRules;
import eu.etaxonomy.cdm.strategy.cache.TagEnum;
import eu.etaxonomy.cdm.strategy.cache.TaggedCacheHelper;
import eu.etaxonomy.cdm.strategy.cache.TaggedText;
import eu.etaxonomy.cdm.strategy.cache.TaggedTextBuilder;

/**
 * @author a.mueller
 * @since 24.11.2020
 */
public class TypeDesignationSetFormatter {

    private static final String TYPE_STATUS_SEPARATOR = "; ";
    private static final String TYPE_SEPARATOR = "; ";
    private static final String TYPE_DESIGNATION_SEPARATOR = ", ";
    private static final String TYPE_STATUS_PARENTHESIS_LEFT = " (";
    private static final String TYPE_STATUS_PARENTHESIS_RIGHT = ")";
    private static final String REFERENCE_PARENTHESIS_RIGHT = "]";
    private static final String REFERENCE_PARENTHESIS_LEFT = " [";
    private static final String REFERENCE_DESIGNATED_BY = " designated by ";
    private static final String REFERENCE_FIDE = "fide ";
    private static final String SOURCE_SEPARATOR = ", ";
    private static final String POST_STATUS_SEPARATOR = ": ";
    private static final String POST_NAME_SEPARTOR = UTF8.EN_DASH_SPATIUM.toString();

    boolean withCitation;
    boolean withStartingTypeLabel;
    boolean withNameIfAvailable;

    public TypeDesignationSetFormatter(boolean withCitation, boolean withStartingTypeLabel,
            boolean withNameIfAvailable) {
        this.withCitation = withCitation;
        this.withStartingTypeLabel = withStartingTypeLabel;
        this.withNameIfAvailable = withNameIfAvailable;
    }

    public String format(TypeDesignationSetManager manager){
        return TaggedCacheHelper.createString(toTaggedText(manager));
    }

    public String format(TypeDesignationSetManager manager, HTMLTagRules htmlTagRules){
        return TaggedCacheHelper.createString(toTaggedText(manager), htmlTagRules);
    }

    public List<TaggedText> toTaggedText(TypeDesignationSetManager manager){
        return buildTaggedText(manager);
    }

    private List<TaggedText> buildTaggedText(TypeDesignationSetManager manager){
        boolean withBrackets = true;  //still unclear if this should become a parameter or should be always true

        TaggedTextBuilder finalBuilder = new TaggedTextBuilder();

        if(withNameIfAvailable && manager.getTypifiedNameCache() != null){
            finalBuilder.add(TagEnum.name, manager.getTypifiedNameCache(), TypedEntityReference.fromEntity(manager.getTypifiedName(), false));
            finalBuilder.addPostSeparator(POST_NAME_SEPARTOR);
        }

        int typeSetCount = 0;
        LinkedHashMap<TypedEntityReference<? extends VersionableEntity>, TypeDesignationWorkingSet> orderedByTypesByBaseEntity = manager.getOrderedTypeDesignationWorkingSets();
        if(orderedByTypesByBaseEntity != null){
            for(TypedEntityReference<?> baseEntityRef : orderedByTypesByBaseEntity.keySet()) {
                buildTaggedTextForSingleTypeSet(manager, withBrackets, finalBuilder,
                        typeSetCount, baseEntityRef);
                typeSetCount++;
            }
        }
        return finalBuilder.getTaggedText();
    }

    private void buildTaggedTextForSingleTypeSet(TypeDesignationSetManager manager, boolean withBrackets,
            TaggedTextBuilder finalBuilder, int typeSetCount, TypedEntityReference<?> baseEntityRef) {

        LinkedHashMap<TypedEntityReference<? extends VersionableEntity>, TypeDesignationWorkingSet>
                orderedByTypesByBaseEntity = manager.getOrderedTypeDesignationWorkingSets();

        TaggedTextBuilder workingsetBuilder = new TaggedTextBuilder();
        boolean isSpecimenTypeDesignation = SpecimenOrObservationBase.class.isAssignableFrom(baseEntityRef.getType());
        if(typeSetCount > 0){
            workingsetBuilder.add(TagEnum.separator, TYPE_SEPARATOR);
        }else if (withStartingTypeLabel){
            //TODO this is not really exact as we may want to handle specimen types and
            //name types separately, but this is such a rare case (if at all) and
            //increases complexity so it is not yet implemented
            boolean isPlural = hasMultipleTypes(orderedByTypesByBaseEntity);
            if(isSpecimenTypeDesignation){
                workingsetBuilder.add(TagEnum.label, (isPlural? "Types:": "Type:"));
            } else if (NameTypeDesignation.class.isAssignableFrom(baseEntityRef.getType())){
                workingsetBuilder.add(TagEnum.label, (isPlural? "Nametypes:": "Nametype:"));
            } else {
                //do nothing for now
            }
        }

        boolean hasExplicitBaseEntity = hasExplicitBaseEntity(baseEntityRef, orderedByTypesByBaseEntity.get(baseEntityRef), isSpecimenTypeDesignation);
        if(hasExplicitBaseEntity && !baseEntityRef.getLabel().isEmpty()){
            workingsetBuilder.add(TagEnum.specimenOrObservation, baseEntityRef.getLabel(), baseEntityRef);
        }
        TypeDesignationWorkingSet typeDesignationWorkingSet = orderedByTypesByBaseEntity.get(baseEntityRef);
        int typeStatusCount = 0;
        if (withBrackets && hasExplicitBaseEntity){
            workingsetBuilder.add(TagEnum.separator, TYPE_STATUS_PARENTHESIS_LEFT);
        }
        for(TypeDesignationStatusBase<?> typeStatus : typeDesignationWorkingSet.keySet()) {
            typeStatusCount = buildTaggedTextForSingleTypeStatus(manager, workingsetBuilder,
                    typeDesignationWorkingSet, typeStatusCount, typeStatus, typeSetCount);
        }
        if (withBrackets && hasExplicitBaseEntity){
            workingsetBuilder.add(TagEnum.separator, TYPE_STATUS_PARENTHESIS_RIGHT);
        }
        typeDesignationWorkingSet.setRepresentation(workingsetBuilder.toString());
        finalBuilder.addAll(workingsetBuilder);
        return;
    }


    /**
     * Checks if the baseType is the same as the (only?) type in the type designation workingset.
     */
    private boolean hasExplicitBaseEntity(TypedEntityReference<?> baseEntityRef,
            TypeDesignationWorkingSet typeDesignationWorkingSet, boolean isSpecimenTypeDesignation) {
        if (!isSpecimenTypeDesignation){
            return false;   //name type designations are not handled here
        }else{
            UUID baseUuid = baseEntityRef.getUuid();
            for (TypeDesignationDTO<?> dto: typeDesignationWorkingSet.getTypeDesignations()){
                if (!baseUuid.equals(dto.getTypeUuid())){
                    return true;
                }
            }
        }
        return false;
    }

    private int buildTaggedTextForSingleTypeStatus(TypeDesignationSetManager manager,
            TaggedTextBuilder workingsetBuilder, TypeDesignationWorkingSet typeDesignationWorkingSet,
            int typeStatusCount, TypeDesignationStatusBase<?> typeStatus, int typeSetCount) {

        //starting separator
        if(typeStatusCount++ > 0){
            workingsetBuilder.add(TagEnum.separator, TYPE_STATUS_SEPARATOR);
        }

        boolean isPlural = typeDesignationWorkingSet.get(typeStatus).size() > 1;
        if(typeStatus != TypeDesignationWorkingSet.NULL_STATUS){
            String label = typeStatus.getLabel() + (isPlural ? "s" : "");
            if (workingsetBuilder.size() == 0){
                label = StringUtils.capitalize(label);
            }
            workingsetBuilder.add(TagEnum.label, label);
            workingsetBuilder.add(TagEnum.postSeparator, POST_STATUS_SEPARATOR);
        }else if (workingsetBuilder.size() > 0 && typeSetCount > 0){
            workingsetBuilder.add(TagEnum.label, (isPlural? "Nametypes:": "Nametype:"));
        }

        //designation + sources
        int typeDesignationCount = 0;
        for(TypeDesignationDTO<?> typeDesignationDTO : createSortedList(typeDesignationWorkingSet, typeStatus)) {
            TypeDesignationBase<?> typeDes = manager.findTypeDesignation(typeDesignationDTO.getUuid());

            typeDesignationCount = buildTaggedTextForSingleType(typeDes, withCitation,
                    workingsetBuilder, typeDesignationCount);
        }
        return typeStatusCount;
    }

    protected static int buildTaggedTextForSingleType(TypeDesignationBase<?> typeDes, boolean withCitation,
            TaggedTextBuilder workingsetBuilder, int typeDesignationCount) {

        if(typeDesignationCount++ > 0){
            workingsetBuilder.add(TagEnum.separator, TYPE_DESIGNATION_SEPARATOR);
        }
        buildTaggedTextForTypeDesignationBase(typeDes, workingsetBuilder);
        if (withCitation){

            //lectotype source
            OriginalSourceBase lectoSource = typeDes.getDesignationSource();
            if (hasLectoSource(typeDes)){
                workingsetBuilder.add(TagEnum.separator, REFERENCE_DESIGNATED_BY);
                addSource(workingsetBuilder, lectoSource);
            }
            //general sources
            if (!typeDes.getSources().isEmpty()) {
                workingsetBuilder.add(TagEnum.separator, REFERENCE_PARENTHESIS_LEFT + REFERENCE_FIDE);
                int count = 0;
                for (IdentifiableSource source: typeDes.getSources()){
                    if (count++ > 0){
                        workingsetBuilder.add(TagEnum.separator, SOURCE_SEPARATOR);
                    }
                    addSource(workingsetBuilder, source);
                }
                workingsetBuilder.add(TagEnum.separator, REFERENCE_PARENTHESIS_RIGHT);
            }
        }

        return typeDesignationCount;
    }

    /**
     * Adds the tags for the given source.
     */
    private static void addSource(TaggedTextBuilder workingsetBuilder,
            OriginalSourceBase source) {
        Reference ref = source.getCitation();
        if (ref != null){
            String citation = OriginalSourceFormatter.INSTANCE.format(source);
            workingsetBuilder.add(TagEnum.reference, citation, TypedEntityReference.fromEntity(ref, false));
        }
    }

    private static boolean hasLectoSource(TypeDesignationBase<?> typeDes) {
        return typeDes.getDesignationSource() != null &&
                    (typeDes.getDesignationSource().getCitation() != null
                      || isNotBlank(typeDes.getDesignationSource().getCitationMicroReference())
                     );
    }

    private List<TypeDesignationDTO> createSortedList(
            TypeDesignationWorkingSet typeDesignationWorkingSet, TypeDesignationStatusBase<?> typeStatus) {

        List<TypeDesignationDTO> typeDesignationDTOs = new ArrayList<>(typeDesignationWorkingSet.get(typeStatus));
        Collections.sort(typeDesignationDTOs);
        return typeDesignationDTOs;
    }

    /**
     * Returns <code>true</code> if the working set has either multiple working sets
     * or if it has a single working set but this workingset has multiple type designations.
     */
    private boolean hasMultipleTypes(
            LinkedHashMap<TypedEntityReference<? extends VersionableEntity>, TypeDesignationWorkingSet> typeWorkingSets) {
        if (typeWorkingSets == null || typeWorkingSets.isEmpty()){
            return false;
        }else if (typeWorkingSets.keySet().size() > 1) {
            return true;
        }
        TypeDesignationWorkingSet singleSet = typeWorkingSets.values().iterator().next();
        return singleSet.getTypeDesignations().size() > 1;
    }

    private static void buildTaggedTextForTypeDesignationBase(TypeDesignationBase<?> td,
            TaggedTextBuilder workingsetBuilder) {
        TypedEntityReference<?> typeDesignationEntity = TypedEntityReference.fromEntity(td, false);
        if(td instanceof NameTypeDesignation){
            buildTaggedTextForNameTypeDesignation((NameTypeDesignation)td, workingsetBuilder, typeDesignationEntity);
        } else if (td instanceof TextualTypeDesignation){
            buildTaggedTextForTextualTypeDesignation((TextualTypeDesignation)td, workingsetBuilder, typeDesignationEntity);
        } else if (td instanceof SpecimenTypeDesignation){
            buildTaggedTextForSpecimenTypeDesignation((SpecimenTypeDesignation)td, false, workingsetBuilder, typeDesignationEntity);
        }else{
            throw new RuntimeException("Unknown TypeDesignation type");
        }
    }


    private static void buildTaggedTextForNameTypeDesignation(NameTypeDesignation td, TaggedTextBuilder workingsetBuilder,
            TypedEntityReference<?> typeDesignationEntity) {

        if (td.getTypeName() != null){
            workingsetBuilder.addAll(td.getTypeName().getCacheStrategy().getTaggedTitle(td.getTypeName()));
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

    private static void buildTaggedTextForTextualTypeDesignation(TextualTypeDesignation td,
            TaggedTextBuilder workingsetBuilder, TypedEntityReference<?> typeDesignationEntity) {

        String result = td.getPreferredText(Language.DEFAULT());
        if (td.isVerbatim()){
            result = "\"" + result + "\"";  //TODO which character to use?
        }
        workingsetBuilder.add(TagEnum.typeDesignation, result, typeDesignationEntity);
    }

    private static void buildTaggedTextForSpecimenTypeDesignation(SpecimenTypeDesignation td, boolean useTitleCache,
            TaggedTextBuilder workingsetBuilder, TypedEntityReference<?> typeDesignationEntity) {


        if(useTitleCache){
            String typeDesigTitle = "";
            if(td.getTypeSpecimen() != null){
                String nameTitleCache = td.getTypeSpecimen().getTitleCache();
                //TODO is this needed?
//                if(getTypifiedNameCache() != null){
//                    nameTitleCache = nameTitleCache.replace(getTypifiedNameCache(), "");
//                }
                typeDesigTitle += nameTitleCache;
            }
            workingsetBuilder.add(TagEnum.typeDesignation, typeDesigTitle, typeDesignationEntity);
        } else {
            if (td.getTypeSpecimen() == null){
                workingsetBuilder.add(TagEnum.typeDesignation, "", typeDesignationEntity);
            }else{
                DerivedUnit du = td.getTypeSpecimen();
                if(du.isProtectedTitleCache()){
                    workingsetBuilder.add(TagEnum.typeDesignation, du.getTitleCache(), typeDesignationEntity);
                } else {
                    du = HibernateProxyHelper.deproxy(du);
                    boolean isMediaSpecimen = du instanceof MediaSpecimen;
                    String typeSpecimenTitle = (isMediaSpecimen ? "[icon] " : "");
                    if(isMediaSpecimen
                                && HibernateProxyHelper.deproxyOrNull(du.getCollection()) == null  //TODO not sure if only checking the collection is enough, but as we also check existence of sources now the case that only an accession number exists is also covered here
                                && ((MediaSpecimen)du).getMediaSpecimen() != null
                                && !((MediaSpecimen)du).getMediaSpecimen().getSources().isEmpty()
                            ){
                        // special case of a published image which is not covered by the DerivedUnitFacadeCacheStrategy
                        workingsetBuilder.add(TagEnum.typeDesignation, "[icon] in", typeDesignationEntity); //TODO how to better use tagged text here, the type designation itself has no real text; we could include the sources but that makes them unusable as sources :-(
                        MediaSpecimen msp = (MediaSpecimen)du;
                        int count = 0;
                        for(IdentifiableSource source : msp.getMediaSpecimen().getSources()){
                            if (source.getType().isPrimarySource()){
                                if (count++ > 0){
                                    workingsetBuilder.add(TagEnum.separator, SOURCE_SEPARATOR);
                                }
                                addSource(workingsetBuilder, source);
                            }
                        }
                    } else {
                        DerivedUnitFacadeCacheStrategy cacheStrategy = new DerivedUnitFacadeCacheStrategy();
                        String titleCache = cacheStrategy.getTitleCache(du, true, false);
                        // removing parentheses from code + accession number, see https://dev.e-taxonomy.eu/redmine/issues/8365
                        titleCache = titleCache.replaceAll("[\\(\\)]", "");
                        typeSpecimenTitle += titleCache;
                        workingsetBuilder.add(TagEnum.typeDesignation, typeSpecimenTitle, typeDesignationEntity);
                    }
                } //protected titleCache
            }//fi specimen == null
        }//fi useTitelCache

        if(td.isNotDesignated()){
            //this should not happen together with a defined specimen, therefore we may handle it in a separate tag
            workingsetBuilder.add(TagEnum.typeDesignation, "not designated", typeDesignationEntity);
        }
    }

    private static boolean isNotBlank(String str){
        return StringUtils.isNotBlank(str);
    }
}
