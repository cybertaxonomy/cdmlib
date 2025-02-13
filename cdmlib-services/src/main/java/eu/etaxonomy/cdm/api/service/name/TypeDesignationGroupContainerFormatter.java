/**
* Copyright (C) 2020 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.name;

import java.net.URI;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import eu.etaxonomy.cdm.api.service.name.TypeDesignationGroup.TypeDesignationSetType;
import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.common.UTF8;
import eu.etaxonomy.cdm.compare.name.TypeDesignationStatusComparator;
import eu.etaxonomy.cdm.format.reference.OriginalSourceFormatter;
import eu.etaxonomy.cdm.hibernate.HibernateProxyHelper;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.common.IdentifiableSource;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.VersionableEntity;
import eu.etaxonomy.cdm.model.name.NameTypeDesignation;
import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignation;
import eu.etaxonomy.cdm.model.name.TextualTypeDesignation;
import eu.etaxonomy.cdm.model.name.TypeDesignationBase;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnit;
import eu.etaxonomy.cdm.model.occurrence.MediaSpecimen;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;
import eu.etaxonomy.cdm.model.reference.OriginalSourceBase;
import eu.etaxonomy.cdm.model.reference.OriginalSourceType;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.ref.TypedEntityReference;
import eu.etaxonomy.cdm.ref.TypedEntityReferenceFactory;
import eu.etaxonomy.cdm.strategy.cache.HTMLTagRules;
import eu.etaxonomy.cdm.strategy.cache.TagEnum;
import eu.etaxonomy.cdm.strategy.cache.TaggedText;
import eu.etaxonomy.cdm.strategy.cache.TaggedTextBuilder;
import eu.etaxonomy.cdm.strategy.cache.TaggedTextFormatter;
import eu.etaxonomy.cdm.strategy.cache.occurrence.DerivedUnitDefaultCacheStrategy;

/**
 * Formatter to format a {@link TypeDesignationGroupContainer}
 *
 * @author a.mueller
 * @since 24.11.2020
 */
public class TypeDesignationGroupContainerFormatter {

    static final String TYPE_STATUS_SEPARATOR = "; ";
    static final String TYPE_DESIGNATION_SEPARATOR = ", ";
    static final String REFERENCE_PARENTHESIS_RIGHT = "]";
    static final String REFERENCE_PARENTHESIS_LEFT = " [";
    static final String REFERENCE_DESIGNATED_BY = "designated by ";
    static final String REFERENCE_FIDE = "fide ";
    static final String SOURCE_SEPARATOR = ", ";
    static final String POST_STATUS_SEPARATOR = ": ";
    private static final String POST_NAME_SEPARTOR = UTF8.EN_DASH_SPATIUM.toString();

    private TypeDesignationGroupFormatterConfiguration configuration = new TypeDesignationGroupFormatterConfiguration();

    private TypeDesignationStatusComparator statusComparator = new TypeDesignationStatusComparator<>();

    public static String entityLabel(VersionableEntity baseEntity,
            TypeDesignationGroupFormatterConfiguration config) {

        String label = "";
        if(baseEntity instanceof IdentifiableEntity<?>){
            label = ((IdentifiableEntity<?>)baseEntity).getTitleCache();
        }else if (baseEntity instanceof TextualTypeDesignation) {
            label = TextualTypeDesignationGroupFormatter.INSTANCE()
                    .entityLabel((TextualTypeDesignation)baseEntity, config);
        }
        //TODO first check if it will not break code
//        else {
//            label = baseEntity.toString();
//        }
        return label;
    }

// ********************** CONSTRUCTOR **********************************/

    public TypeDesignationGroupContainerFormatter() {
    }

    public TypeDesignationGroupContainerFormatter(boolean withCitation, boolean withStartingTypeLabel,
            boolean withNameIfAvailable, boolean withPrecedingMainType, boolean withAccessionNoType) {

        configuration.setWithCitation(withCitation)
            .setWithStartingTypeLabel(withStartingTypeLabel)
            .setWithNameIfAvailable(withNameIfAvailable)
            .setWithPrecedingMainType(withPrecedingMainType)
            .setWithAccessionNoType(withAccessionNoType)
            .setIgnoreSyntypesWithLectotype(false);
    }

// *********************** CONFIGURATION **************************************/

    public TypeDesignationGroupContainerFormatter(TypeDesignationGroupFormatterConfiguration configuration) {
        this.configuration = configuration;
    }

    public TypeDesignationGroupContainerFormatter withCitation(boolean withCitation) {
        configuration.setWithCitation(withCitation);
        return this;
    }

    public TypeDesignationGroupContainerFormatter withStartingTypeLabel(boolean withStartingTypeLabel) {
        configuration.setWithStartingTypeLabel(withStartingTypeLabel);
        return this;
    }

    public TypeDesignationGroupContainerFormatter withNameIfAvailable(boolean withNameIfAvailable) {
        configuration.setWithNameIfAvailable(withNameIfAvailable);
        return this;
    }

    public TypeDesignationGroupContainerFormatter withPrecedingMainType(boolean withPrecedingMainType) {
        configuration.setWithPrecedingMainType(withPrecedingMainType);
        return this;
    }

    public TypeDesignationGroupContainerFormatter withAccessionNoType(boolean withAccessionNoType) {
        configuration.setWithAccessionNoType(withAccessionNoType);
        return this;
    }

    public TypeDesignationGroupContainerFormatter withSourceTypeFilter(EnumSet<OriginalSourceType> sourceTypes) {
        configuration.setSourceTypeFilter(sourceTypes);
        return this;
    }

// ***************** formatting methods ********************************/

    public String format(TypeDesignationGroupContainer manager){
        return TaggedTextFormatter.createString(toTaggedText(manager));
    }

    public String format(TypeDesignationGroupContainer manager, HTMLTagRules htmlTagRules){
        return TaggedTextFormatter.createString(toTaggedText(manager), htmlTagRules);
    }

    public List<TaggedText> toTaggedText(TypeDesignationGroupContainer manager){
        return buildTaggedText(manager);
    }

    private List<TaggedText> buildTaggedText(TypeDesignationGroupContainer manager){

        TaggedTextBuilder finalBuilder = new TaggedTextBuilder();

        //add typified name if available and if configured such
        if(configuration.isWithNameIfAvailable() && manager.getTypifiedNameCache() != null){
            finalBuilder.add(TagEnum.name, manager.getTypifiedNameCache(),
                    TypedEntityReferenceFactory.fromEntity(manager.getTypifiedName(), false));
            finalBuilder.addPostSeparator(POST_NAME_SEPARTOR);
        }

        int typeSetCount = 0;
        Map<VersionableEntity,TypeDesignationGroup> orderedBaseEntity2TypesMap
                    = manager.getOrderedTypeDesignationSets();
        TypeDesignationSetType lastWsType = null;
        if (orderedBaseEntity2TypesMap != null){
            for(VersionableEntity baseEntity : orderedBaseEntity2TypesMap.keySet()) {
                baseEntity = CdmBase.deproxy(baseEntity);
                if (baseEntity.isInstanceOf(SpecimenOrObservationBase.class)) {
                    SpecimenTypeDesignationGroupFormatter.INSTANCE().format(finalBuilder, manager,
                            orderedBaseEntity2TypesMap,
                            typeSetCount, configuration,
                            (SpecimenOrObservationBase<?>)baseEntity,
                            lastWsType);
                }else if (baseEntity.isInstanceOf(NameTypeDesignation.class)) {
                    NameTypeDesignationGroupFormatter.INSTANCE().format(finalBuilder, manager,
                            orderedBaseEntity2TypesMap,
                            typeSetCount, configuration,
                            (NameTypeDesignation)baseEntity, lastWsType);
                }else if (baseEntity.isInstanceOf(TextualTypeDesignation.class)) {
                    TextualTypeDesignationGroupFormatter.INSTANCE().format(finalBuilder, manager,
                            orderedBaseEntity2TypesMap,
                            typeSetCount, configuration,
                            (TextualTypeDesignation)baseEntity, lastWsType);
                }else {
                    throw new RuntimeException("Base type not supported: " + baseEntity.getClass().getSimpleName());
                }
//                }else {
//                    buildTaggedTextForSingleTypeSet(manager, withBrackets, finalBuilder,
//                            typeSetCount, baseEntity, lastWsType);
//                }
                lastWsType = orderedBaseEntity2TypesMap.get(baseEntity).getWorkingsetType();
                typeSetCount++;
            }
        }
        return finalBuilder.getTaggedText();
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
                workingsetBuilder.add(TagEnum.separator, " " + REFERENCE_DESIGNATED_BY);
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
            workingsetBuilder.add(TagEnum.reference, citation, TypedEntityReferenceFactory.fromEntity(ref, false));
        }
    }

    private static boolean hasLectoSource(TypeDesignationBase<?> typeDes) {
        return typeDes.getDesignationSource() != null &&
                    (typeDes.getDesignationSource().getCitation() != null
                      || isNotBlank(typeDes.getDesignationSource().getCitationMicroReference())
                     );
    }

    private static void buildTaggedTextForTypeDesignationBase(TypeDesignationBase<?> td,
            TaggedTextBuilder workingsetBuilder) {

        TypedEntityReference<?> typeDesignationEntity = TypedEntityReferenceFactory.fromEntity(td, false);
        if(td instanceof NameTypeDesignation){
            buildTaggedTextForNameTypeDesignation((NameTypeDesignation)td, workingsetBuilder, typeDesignationEntity);
        } else if (td instanceof TextualTypeDesignation){
            buildTaggedTextForTextualTypeDesignation((TextualTypeDesignation)td, workingsetBuilder, typeDesignationEntity);
        } else if (td instanceof SpecimenTypeDesignation){
            buildTaggedTextForSpecimenTypeDesignation((SpecimenTypeDesignation)td, workingsetBuilder, typeDesignationEntity);
        }else{
            throw new RuntimeException("Unknown TypeDesignation type");
        }
    }

    private static void buildTaggedTextForNameTypeDesignation(NameTypeDesignation td, TaggedTextBuilder workingsetBuilder,
            TypedEntityReference<?> typeDesignationEntity) {

        if (td.getTypeName() != null){
            workingsetBuilder.addAll(td.getTypeName().cacheStrategy().getTaggedTitle(td.getTypeName()));
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

    private static void buildTaggedTextForSpecimenTypeDesignation(SpecimenTypeDesignation td,
            TaggedTextBuilder workingsetBuilder, TypedEntityReference<?> typeDesignationEntity) {

        if (td.getTypeSpecimen() == null){
            workingsetBuilder.add(TagEnum.typeDesignation, "", typeDesignationEntity);
        }else{
            DerivedUnit du = td.getTypeSpecimen();
            if(du.isProtectedTitleCache()){
                //protected title cache
                workingsetBuilder.add(TagEnum.typeDesignation, du.getTitleCache(), typeDesignationEntity);
            } else {
                du = HibernateProxyHelper.deproxy(du);
                boolean isMediaSpecimen = du instanceof MediaSpecimen;
                String typeSpecimenTitle = (isMediaSpecimen ? "[icon] " : "");

                //TODO the following is semantically mostly redundant with the implementation
                //     in MediaSpecimenDefaultCacheStrategy. But there TaggedText is not
                //     yet available. Once it is we should merge the 2 methods.
                //     #10573 and related

                //media specimen
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
                        //TODO add sourceTypes to configuration
                        if (source.getType().isPublicSource()){
                            if (count++ > 0){
                                workingsetBuilder.add(TagEnum.separator, SOURCE_SEPARATOR);
                            }
                            addSource(workingsetBuilder, source);
                        }
                    }
                //other specimen
                } else {
                    //TODO make configurable/or can this code be deleted somehow, it is mostly duplication for Name-/SpecimenTypeDesignationGroupFormatter?
                    DerivedUnitDefaultCacheStrategy.CollectionAccessionSeperator sep
                        = DerivedUnitDefaultCacheStrategy.CollectionAccessionSeperator.SPACE;
                    DerivedUnitDefaultCacheStrategy cacheStrategy = DerivedUnitDefaultCacheStrategy.NewInstance(true, false, false, sep);
                    String titleCache = cacheStrategy.getTitleCache(du, true);
                    // removing parentheses from code + accession number, see https://dev.e-taxonomy.eu/redmine/issues/8365
                    titleCache = titleCache.replaceAll("[\\(\\)]", "");
                    typeSpecimenTitle += titleCache;
                    URI link = getLink(du);  //

                    workingsetBuilder.add(TagEnum.typeDesignation, typeSpecimenTitle, typeDesignationEntity);
                }
            } //protected titleCache
        }//fi specimen == null

        if(td.isNotDesignated()){
            //this should not happen together with a defined specimen, therefore we may handle it in a separate tag
            workingsetBuilder.add(TagEnum.typeDesignation, "not designated", typeDesignationEntity);
        }
    }

    private static URI getLink(DerivedUnit du) {
        if (du.getPreferredStableUri() != null) {
            return du.getPreferredStableUri().getJavaUri();
        }else {
            return null;
        }
    }

    private static boolean isNotBlank(String str){
        return StringUtils.isNotBlank(str);
    }
}