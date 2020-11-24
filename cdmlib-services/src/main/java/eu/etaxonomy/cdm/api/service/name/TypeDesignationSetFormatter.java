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

import org.apache.commons.lang3.StringUtils;

import eu.etaxonomy.cdm.common.UTF8;
import eu.etaxonomy.cdm.model.common.IdentifiableSource;
import eu.etaxonomy.cdm.model.common.VersionableEntity;
import eu.etaxonomy.cdm.model.name.NameTypeDesignation;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.name.TypeDesignationBase;
import eu.etaxonomy.cdm.model.name.TypeDesignationStatusBase;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;
import eu.etaxonomy.cdm.model.reference.OriginalSourceBase;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.ref.TypedEntityReference;
import eu.etaxonomy.cdm.strategy.cache.TagEnum;
import eu.etaxonomy.cdm.strategy.cache.TaggedCacheHelper;
import eu.etaxonomy.cdm.strategy.cache.TaggedText;
import eu.etaxonomy.cdm.strategy.cache.TaggedTextBuilder;
import eu.etaxonomy.cdm.strategy.cache.reference.DefaultReferenceCacheStrategy;

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

    /**
     * @param withCitation
     * @param withStartingTypeLabel
     * @param withNameIfAvailable
     */
    public TypeDesignationSetFormatter(boolean withCitation, boolean withStartingTypeLabel,
            boolean withNameIfAvailable) {
        this.withCitation = withCitation;
        this.withStartingTypeLabel = withStartingTypeLabel;
        this.withNameIfAvailable = withNameIfAvailable;
    }

    public String format(TypeDesignationSetManager manager){
        return TaggedCacheHelper.createString(toTaggedText(manager));
    }

    public List<TaggedText> toTaggedText(TypeDesignationSetManager manager){
        return buildTaggedText(manager);
    }

    private List<TaggedText> buildTaggedText(TypeDesignationSetManager manager){
        boolean withBrackets = true;  //still unclear if this should become a parameter or should be always true

        TaggedTextBuilder finalBuilder = new TaggedTextBuilder();

        if(withNameIfAvailable && manager.getTypifiedNameCache() != null){
            finalBuilder.add(TagEnum.name, manager.getTypifiedNameCache(), new TypedEntityReference<>(TaxonName.class, manager.getTypifiedName().getUuid()));
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

        LinkedHashMap<TypedEntityReference<? extends VersionableEntity>, TypeDesignationWorkingSet> orderedByTypesByBaseEntity = manager.getOrderedTypeDesignationWorkingSets();

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

        if(!baseEntityRef.getLabel().isEmpty()){
            workingsetBuilder.add(TagEnum.specimenOrObservation, baseEntityRef.getLabel(), baseEntityRef);
        }
        TypeDesignationWorkingSet typeDesignationWorkingSet = orderedByTypesByBaseEntity.get(baseEntityRef);
        int typeStatusCount = 0;
        if (withBrackets && isSpecimenTypeDesignation){
            workingsetBuilder.add(TagEnum.separator, TYPE_STATUS_PARENTHESIS_LEFT);
        }
        for(TypeDesignationStatusBase<?> typeStatus : typeDesignationWorkingSet.keySet()) {
            typeStatusCount = buildTaggedTextForSingleTypeStatus(manager, workingsetBuilder,
                    typeDesignationWorkingSet, typeStatusCount, typeStatus, typeSetCount);
        }
        if (withBrackets && isSpecimenTypeDesignation){
            workingsetBuilder.add(TagEnum.separator, TYPE_STATUS_PARENTHESIS_RIGHT);
        }
        typeDesignationWorkingSet.setRepresentation(workingsetBuilder.toString());
        finalBuilder.addAll(workingsetBuilder);
        return;
    }


    private int buildTaggedTextForSingleTypeStatus(TypeDesignationSetManager manager, TaggedTextBuilder workingsetBuilder,
            TypeDesignationWorkingSet typeDesignationWorkingSet, int typeStatusCount,
            TypeDesignationStatusBase<?> typeStatus, int typeSetCount) {
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
        for(TypedEntityReference<?> typeDesignationEntityReference : createSortedList(typeDesignationWorkingSet, typeStatus)) {
            typeDesignationCount = buildTaggedTextForSingleType(manager, workingsetBuilder, typeDesignationCount,
                    typeDesignationEntityReference);
        }
        return typeStatusCount;
    }

    private int buildTaggedTextForSingleType(TypeDesignationSetManager manager, TaggedTextBuilder workingsetBuilder,
            int typeDesignationCount, TypedEntityReference<?> typeDesignationEntityReference) {
        if(typeDesignationCount++ > 0){
            workingsetBuilder.add(TagEnum.separator, TYPE_DESIGNATION_SEPARATOR);
        }

        workingsetBuilder.add(TagEnum.typeDesignation, typeDesignationEntityReference.getLabel(), typeDesignationEntityReference);

        if (withCitation){
            TypeDesignationBase<?> typeDes = manager.findTypeDesignation(typeDesignationEntityReference);

            //lectotype source
            OriginalSourceBase<?> lectoSource = typeDes.getSource();
            if (hasLectoSource(typeDes)){
                workingsetBuilder.add(TagEnum.separator, REFERENCE_DESIGNATED_BY);
                addSource(workingsetBuilder, typeDesignationEntityReference, lectoSource);
            }
            //general sources
            if (!typeDes.getSources().isEmpty()) {
                workingsetBuilder.add(TagEnum.separator, REFERENCE_PARENTHESIS_LEFT + REFERENCE_FIDE);
                int count = 0;
                for (IdentifiableSource source: typeDes.getSources()){
                    if (count++ > 0){
                        workingsetBuilder.add(TagEnum.separator, SOURCE_SEPARATOR);
                    }
                    addSource(workingsetBuilder, typeDesignationEntityReference, source);
                }
                workingsetBuilder.add(TagEnum.separator, REFERENCE_PARENTHESIS_RIGHT);
            }
        }
        return typeDesignationCount;
    }


    /**
     * Adds the tags for the given source.
     */
    private void addSource(TaggedTextBuilder workingsetBuilder, TypedEntityReference<?> typeDesignationEntityReference,
            OriginalSourceBase<?> source) {
        Reference ref = source.getCitation();
        if (ref != null){
            DefaultReferenceCacheStrategy strategy = ((DefaultReferenceCacheStrategy)ref.getCacheStrategy());
            String shortCitation = strategy.createShortCitation(ref, source.getCitationMicroReference(), false);
            workingsetBuilder.add(TagEnum.reference, shortCitation, typeDesignationEntityReference);
        }
    }

    private boolean hasLectoSource(TypeDesignationBase<?> typeDes) {
        return typeDes.getSource() != null &&
                    (typeDes.getSource().getCitation() != null
                      || isNotBlank(typeDes.getSource().getCitationMicroReference())
                     );
    }

    private List<TypedEntityReference<TypeDesignationBase<?>>> createSortedList(
            TypeDesignationWorkingSet typeDesignationWorkingSet, TypeDesignationStatusBase<?> typeStatus) {
        @SuppressWarnings({ "rawtypes", "unchecked" })
        List<TypedEntityReference<TypeDesignationBase<?>>> typeDesignationEntityReferences = new ArrayList(typeDesignationWorkingSet.get(typeStatus));
        Collections.sort(typeDesignationEntityReferences, new TypedEntityComparator());
        return typeDesignationEntityReferences;
    }

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

    private boolean isNotBlank(String str){
        return StringUtils.isNotBlank(str);
    }
}
