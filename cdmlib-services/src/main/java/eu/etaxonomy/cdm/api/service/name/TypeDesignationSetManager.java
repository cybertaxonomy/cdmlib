/**
* Copyright (C) 2017 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.name;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.search.hcore.util.impl.HibernateHelper;

import eu.etaxonomy.cdm.api.facade.DerivedUnitFacadeCacheStrategy;
import eu.etaxonomy.cdm.api.service.exception.RegistrationValidationException;
import eu.etaxonomy.cdm.common.UTF8;
import eu.etaxonomy.cdm.hibernate.HibernateProxyHelper;
import eu.etaxonomy.cdm.model.common.IdentifiableSource;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.VersionableEntity;
import eu.etaxonomy.cdm.model.name.HomotypicalGroup;
import eu.etaxonomy.cdm.model.name.NameTypeDesignation;
import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignation;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.name.TextualTypeDesignation;
import eu.etaxonomy.cdm.model.name.TypeDesignationBase;
import eu.etaxonomy.cdm.model.name.TypeDesignationStatusBase;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnit;
import eu.etaxonomy.cdm.model.occurrence.FieldUnit;
import eu.etaxonomy.cdm.model.occurrence.MediaSpecimen;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;
import eu.etaxonomy.cdm.model.reference.OriginalSourceBase;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.term.TermVocabulary;
import eu.etaxonomy.cdm.ref.EntityReference;
import eu.etaxonomy.cdm.ref.TypedEntityReference;
import eu.etaxonomy.cdm.strategy.cache.TagEnum;
import eu.etaxonomy.cdm.strategy.cache.TaggedText;
import eu.etaxonomy.cdm.strategy.cache.TaggedTextBuilder;
import eu.etaxonomy.cdm.strategy.cache.reference.DefaultReferenceCacheStrategy;
/**
 * Manages a collection of {@link TypeDesignationBase TypeDesignations} for the same typified name.
 *
 * Type designations are ordered by the base type which is a {@link TaxonName} for {@link NameTypeDesignation NameTypeDesignations} or
 * in case of {@link SpecimenTypeDesignation SpecimenTypeDesignations} the  associate {@link FieldUnit} or the {@link DerivedUnit}
 * if the former is missing. The type designations per base type are furthermore ordered by the {@link TypeDesignationStatusBase}.
 *
 * The TypeDesignationSetManager also provides string representations of the whole ordered set of all
 * {@link TypeDesignationBase TypeDesignations} and of the TypeDesignationWorkingSets:
 * <ul>
 *  <li>{@link #print()}
 *  <li>{@link #getOrderdTypeDesignationWorkingSets()} ... {@link TypeDesignationWorkingSet#getRepresentation()}
 * </ul>
 * Prior using the representations you need to trigger their generation by calling {@link #buildString()}
 *
 * @author a.kohlbecker
 * @since Mar 10, 2017
 */
public class TypeDesignationSetManager {

    enum NameTypeBaseEntityType{
        NAME_TYPE_DESIGNATION,
        TYPE_NAME;
    }

    private static final String TYPE_STATUS_SEPARATOR = "; ";

    private static final String TYPE_SEPARATOR = "; ";

    private static final String TYPE_DESIGNATION_SEPARATOR = ", ";
    private static final String TYPE_STATUS_PARENTHESIS_LEFT = " (";
    private static final String TYPE_STATUS_PARENTHESIS_RIGHT = ") ";
    private static final String REFERENCE_PARENTHESIS_RIGHT = "]";
    private static final String REFERENCE_PARENTHESIS_LEFT = " [";
    private static final String REFERENCE_DESIGNATED_BY = " designated by ";
    private static final String REFERENCE_FIDE = "fide ";
    private static final String SOURCE_SEPARATOR = ", ";
    private static final String POST_STATUS_SEPARATOR = ": ";

    private Map<UUID,TypeDesignationBase<?>> typeDesignations = new HashMap<>();

    private NameTypeBaseEntityType nameTypeBaseEntityType = NameTypeBaseEntityType.NAME_TYPE_DESIGNATION;

    /**
     * Groups the EntityReferences for each of the TypeDesignations by the according TypeDesignationStatus.
     * The TypeDesignationStatusBase keys are already ordered by the term order defined in the vocabulary.
     */
    private LinkedHashMap<TypedEntityReference, TypeDesignationWorkingSet> orderedByTypesByBaseEntity;

    private EntityReference typifiedNameRef;

    private TaxonName typifiedName;

    private String finalString = null;

    final NullTypeDesignationStatus NULL_STATUS = new NullTypeDesignationStatus();

    private List<String> problems = new ArrayList<>();

    private List<TaggedText> taggedText;

    public TypeDesignationSetManager(@SuppressWarnings("rawtypes") Collection<TypeDesignationBase> typeDesignations)
            throws RegistrationValidationException{
    	this(typeDesignations, null);
    }

    public TypeDesignationSetManager(@SuppressWarnings("rawtypes") Collection<TypeDesignationBase> typeDesignations, TaxonName typifiedName)
            throws RegistrationValidationException  {
        for (TypeDesignationBase<?> typeDes:typeDesignations){
            this.typeDesignations.put(typeDes.getUuid(), typeDes);
        }
        try {
        	findTypifiedName();
        }catch (RegistrationValidationException e) {
        	if (typifiedName == null) {
        		throw e;
        	}
        	this.typifiedName = typifiedName;
            this.typifiedNameRef = new EntityReference(typifiedName.getUuid(), typifiedName.getTitleCache());
        }

        mapAndSort();
    }

    public TypeDesignationSetManager(HomotypicalGroup group) {
        for (TypeDesignationBase<?> typeDes: group.getTypeDesignations()){
            this.typeDesignations.put(typeDes.getUuid(), typeDes);
        }
        //findTypifiedName();
        mapAndSort();
    }

    public TypeDesignationSetManager(TaxonName typifiedName) {
        this.typifiedNameRef = new EntityReference(typifiedName.getUuid(), typifiedName.getTitleCache());
    }

    /**
     * Add one or more TypeDesignations to the manager. This causes re-grouping and re-ordering
     * of all managed TypeDesignations.
     *
     * @param containgEntity
     * @param typeDesignations
     */
    public void addTypeDesigations(TypeDesignationBase<?> ... typeDesignations){
        for (TypeDesignationBase<?> typeDes: typeDesignations){
            this.typeDesignations.put(typeDes.getUuid(), typeDes);
        }
        mapAndSort();
    }

    /**
     * Groups and orders all managed TypeDesignations.
     *
     * @param containgEntity
     */
    protected void mapAndSort() {
        finalString = null;
        Map<TypedEntityReference<?>, TypeDesignationWorkingSet> byBaseEntityByTypeStatus = new HashMap<>();

        this.typeDesignations.values().forEach(td -> mapTypeDesignation(byBaseEntityByTypeStatus, td));
        orderedByTypesByBaseEntity = orderByTypeByBaseEntity(byBaseEntityByTypeStatus);
    }

    private void mapTypeDesignation(Map<TypedEntityReference<?>, TypeDesignationWorkingSet> byBaseEntityByTypeStatus,
            TypeDesignationBase<?> td){

        TypeDesignationStatusBase<?> status = td.getTypeStatus();

        try {
            final VersionableEntity baseEntity = baseEntity(td);
            final TypedEntityReference<? extends VersionableEntity> baseEntityReference = makeEntityReference(baseEntity);

            TypedEntityReference<?> typeDesignationEntityReference = new TypedEntityReference<>(
                    HibernateProxyHelper.deproxy(td).getClass(),
                    td.getUuid(),
                    stringify(td));

            if(!byBaseEntityByTypeStatus.containsKey(baseEntityReference)){
                byBaseEntityByTypeStatus.put(baseEntityReference, new TypeDesignationWorkingSet(baseEntity, baseEntityReference));
            }
            byBaseEntityByTypeStatus.get(baseEntityReference).insert(status, typeDesignationEntityReference);

        } catch (DataIntegrityException e){
            problems.add(e.getMessage());
        }
    }

    protected VersionableEntity baseEntity(TypeDesignationBase<?> td) throws DataIntegrityException {

        VersionableEntity baseEntity = null;
        if(td instanceof SpecimenTypeDesignation){
            SpecimenTypeDesignation std = (SpecimenTypeDesignation) td;
            FieldUnit fu = findFieldUnit(std.getTypeSpecimen());
            if(fu != null){
                baseEntity = fu;
            } else if(((SpecimenTypeDesignation) td).getTypeSpecimen() != null){
                baseEntity = ((SpecimenTypeDesignation) td).getTypeSpecimen();
            }
        } else if(td instanceof NameTypeDesignation){
            if(nameTypeBaseEntityType == NameTypeBaseEntityType.NAME_TYPE_DESIGNATION){
                baseEntity = td;
            } else {
                // only other option is TaxonName
                baseEntity = ((NameTypeDesignation)td).getTypeName();
            }
        }
        if(baseEntity == null) {
            throw new DataIntegrityException("Incomplete TypeDesignation, no type missin in " + td.toString());
        }
        return baseEntity;
    }

    protected TypedEntityReference<? extends VersionableEntity> makeEntityReference(VersionableEntity baseEntity) {

        baseEntity = (VersionableEntity) HibernateHelper.unproxy(baseEntity);
        String label = "";
        if(baseEntity  instanceof FieldUnit){
                label = ((FieldUnit)baseEntity).getTitleCache();
        }

        TypedEntityReference<? extends VersionableEntity> baseEntityReference =
                new TypedEntityReference<>((Class<? extends VersionableEntity>)baseEntity.getClass(), baseEntity.getUuid(), label);

        return baseEntityReference;
    }


    private LinkedHashMap<TypedEntityReference, TypeDesignationWorkingSet> orderByTypeByBaseEntity(
            Map<TypedEntityReference<?>, TypeDesignationWorkingSet> stringsByTypeByBaseEntity){

       // order the FieldUnit TypeName keys
       List<TypedEntityReference<?>> baseEntityKeyList = new LinkedList<>(stringsByTypeByBaseEntity.keySet());
       Collections.sort(baseEntityKeyList, new Comparator<TypedEntityReference<?>>(){

       /**
         * Sorts the base entities (TypedEntityReference) in the following order:
         *
         * 1. FieldUnits
         * 2. DerivedUnit (in case of missing FieldUnit we expect the base type to be DerivedUnit)
         * 3. NameType
         *
         * {@inheritDoc}
         */
        @Override
        public int compare(TypedEntityReference<?> o1, TypedEntityReference<?> o2) {

            Class<?> type1 = o1.getType();
            Class<?> type2 = o2.getType();

            if(!type1.equals(type2)) {
                if(type1.equals(FieldUnit.class) || type2.equals(FieldUnit.class)){
                    // FieldUnits first
                    return type1.equals(FieldUnit.class) ? -1 : 1;
                } else {
                    // name types last (in case of missing FieldUnit we expect the base type to be DerivedUnit which comes into the middle)
                    return type2.equals(TaxonName.class) || type2.equals(NameTypeDesignation.class) ? -1 : 1;
                }
            } else {
                return o1.getLabel().compareTo(o2.getLabel());
            }
        }});

       // new LinkedHashMap for the ordered FieldUnitOrTypeName keys
       LinkedHashMap<TypedEntityReference, TypeDesignationWorkingSet> stringsOrderedbyBaseEntityOrderdByType = new LinkedHashMap<>(stringsByTypeByBaseEntity.size());

       for(TypedEntityReference baseEntityRef : baseEntityKeyList){

           TypeDesignationWorkingSet typeDesignationWorkingSet = stringsByTypeByBaseEntity.get(baseEntityRef);
           // order the TypeDesignationStatusBase keys
            List<TypeDesignationStatusBase<?>> keyList = new LinkedList<>(typeDesignationWorkingSet.keySet());
            Collections.sort(keyList, new TypeDesignationStatusComparator());
            // new LinkedHashMap for the ordered TypeDesignationStatusBase keys
            TypeDesignationWorkingSet orderedStringsByOrderedTypes = new TypeDesignationWorkingSet(
                    typeDesignationWorkingSet.getBaseEntity(),
                    baseEntityRef);
            keyList.forEach(key -> orderedStringsByOrderedTypes.put(key, typeDesignationWorkingSet.get(key)));
            stringsOrderedbyBaseEntityOrderdByType.put(baseEntityRef, orderedStringsByOrderedTypes);
       }

        return stringsOrderedbyBaseEntityOrderdByType;
    }

    /*
    private LinkedHashMap<TypedEntityReference, LinkedHashMap<String, Collection<EntityReference>>> buildOrderedRepresentations(){

        orderedStringsByOrderedTypes.keySet().forEach(
                key -> orderedRepresentations.put(
                        getTypeDesignationStytusLabel(key),
                        orderedStringsByOrderedTypes.get(key))
                );
        return orderedRepresentations;
    }
     */

    private void buildString(boolean withCitation, boolean withStartingTypeLabel, boolean withNameIfAvailable){
        boolean withBrackets = true;  //still unclear if this should become a parameter or should be always true

        TaggedTextBuilder finalBuilder = new TaggedTextBuilder();
        finalString = "";

        if(withNameIfAvailable && getTypifiedNameCache() != null){
            finalString += getTypifiedNameCache();
            finalBuilder.add(TagEnum.name, getTypifiedNameCache(), new TypedEntityReference<>(TaxonName.class, getTypifiedNameRef().getUuid()));
        }

        int typeSetCount = 0;
        if(orderedByTypesByBaseEntity != null){
            String separator = UTF8.EN_DASH_SPATIUM.toString();
            finalString += separator;
            finalBuilder.addSeparator(separator);
            for(TypedEntityReference<?> baseEntityRef : orderedByTypesByBaseEntity.keySet()) {
                buildStringForSingleTypeSet(withCitation, withStartingTypeLabel, withBrackets, finalBuilder,
                        typeSetCount, baseEntityRef);
                typeSetCount++;
            }
        }
        finalString = finalString.trim();
        taggedText = finalBuilder.getTaggedText();
    }

    private int buildStringForSingleTypeSet(boolean withCitation, boolean withStartingTypeLabel, boolean withBrackets,
            TaggedTextBuilder finalBuilder, int typeSetCount, TypedEntityReference<?> baseEntityRef) {

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
            typeStatusCount = buildStringForSingleTypeStatus(withCitation, workingsetBuilder,
                    typeDesignationWorkingSet, typeStatusCount, typeStatus, typeSetCount);
        }
        if (withBrackets && isSpecimenTypeDesignation){
            workingsetBuilder.add(TagEnum.separator, TYPE_STATUS_PARENTHESIS_RIGHT);
        }
        typeDesignationWorkingSet.setRepresentation(workingsetBuilder.toString());
        finalString += typeDesignationWorkingSet.getRepresentation();
        finalBuilder.addAll(workingsetBuilder);
        return typeSetCount;
    }

    private boolean hasMultipleTypes(
            LinkedHashMap<TypedEntityReference, TypeDesignationWorkingSet> typeWorkingSets) {
        if (typeWorkingSets == null || typeWorkingSets.isEmpty()){
            return false;
        }else if (typeWorkingSets.keySet().size() > 1) {
            return true;
        }
        TypeDesignationWorkingSet singleSet = typeWorkingSets.values().iterator().next();
        return singleSet.getTypeDesignations().size() > 1;
    }

    private int buildStringForSingleTypeStatus(boolean withCitation, TaggedTextBuilder workingsetBuilder,
            TypeDesignationWorkingSet typeDesignationWorkingSet, int typeStatusCount,
            TypeDesignationStatusBase<?> typeStatus, int typeSetCount) {
        //starting separator
        if(typeStatusCount++ > 0){
            workingsetBuilder.add(TagEnum.separator, TYPE_STATUS_SEPARATOR);
        }

        boolean isPlural = typeDesignationWorkingSet.get(typeStatus).size() > 1;
        if(typeStatus != NULL_STATUS){
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
            typeDesignationCount = buildStringForSingleType(withCitation, workingsetBuilder, typeDesignationCount,
                    typeDesignationEntityReference);
        }
        return typeStatusCount;
    }

    private int buildStringForSingleType(boolean withCitation, TaggedTextBuilder workingsetBuilder, int typeDesignationCount,
            TypedEntityReference<?> typeDesignationEntityReference) {
        if(typeDesignationCount++ > 0){
            workingsetBuilder.add(TagEnum.separator, TYPE_DESIGNATION_SEPARATOR);
        }

        workingsetBuilder.add(TagEnum.typeDesignation, typeDesignationEntityReference.getLabel(), typeDesignationEntityReference);

        if (withCitation){
            TypeDesignationBase<?> typeDes = typeDesignations.get(typeDesignationEntityReference.getUuid());

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
     * Adds the taggs for the given source.
     */
    private void addSource(TaggedTextBuilder workingsetBuilder, TypedEntityReference<?> typeDesignationEntityReference,
            OriginalSourceBase<?> source) {
        Reference ref = source.getCitation();
        String shortCitation = ((DefaultReferenceCacheStrategy)ref.getCacheStrategy()).createShortCitation(ref);
        //TODO still need to add detail
        workingsetBuilder.add(TagEnum.reference, shortCitation, typeDesignationEntityReference);
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

    /**
     * FIXME use the validation framework validators to store the validation problems!!!
     *
     * @return
     * @throws RegistrationValidationException
     */
    private void findTypifiedName() throws RegistrationValidationException {

        List<String> problems = new ArrayList<>();

        TaxonName typifiedName = null;

        for(TypeDesignationBase<?> typeDesignation : typeDesignations.values()){
            typeDesignation.getTypifiedNames();
            if(typeDesignation.getTypifiedNames().isEmpty()){

                //TODO instead throw RegistrationValidationException()
                problems.add("Missing typifiedName in " + typeDesignation.toString());
                continue;
            }
            if(typeDesignation.getTypifiedNames().size() > 1){
              //TODO instead throw RegistrationValidationException()
                problems.add("Multiple typifiedName in " + typeDesignation.toString());
                continue;
            }
            if(typifiedName == null){
                // remember
                typifiedName = typeDesignation.getTypifiedNames().iterator().next();
            } else {
                // compare
                TaxonName otherTypifiedName = typeDesignation.getTypifiedNames().iterator().next();
                if(!typifiedName.getUuid().equals(otherTypifiedName.getUuid())){
                  //TODO instead throw RegistrationValidationException()
                    problems.add("Multiple typifiedName in " + typeDesignation.toString());
                }
            }
        }
        if(!problems.isEmpty()){
            // FIXME use the validation framework
            throw new RegistrationValidationException("Inconsistent type designations", problems);
        }

        if(typifiedName != null){
            // ON SUCCESS -------------------
            this.typifiedName = typifiedName;
            this.typifiedNameRef = new EntityReference(typifiedName.getUuid(), typifiedName.getTitleCache());

        }
    }

    /**
     * @return the title cache of the typifying name or <code>null</code>
     */
    public String getTypifiedNameCache() {
        if(typifiedNameRef != null){
            return typifiedNameRef.getLabel();
        }
        return null;
    }

    /**
     * @return the title cache of the typifying name or <code>null</code>
     */
    public EntityReference getTypifiedNameRef() {
       return typifiedNameRef;
    }

    public Collection<TypeDesignationBase<?>> getTypeDesignations() {
        return typeDesignations.values();
    }

    public TypeDesignationBase<?> findTypeDesignation(EntityReference typeDesignationRef) {
        return this.typeDesignations.get(typeDesignationRef.getUuid());
    }

    public LinkedHashMap<TypedEntityReference, TypeDesignationWorkingSet> getOrderdTypeDesignationWorkingSets() {
        return orderedByTypesByBaseEntity;
    }

    private String stringify(TypeDesignationBase<?> td) {

        if(td instanceof NameTypeDesignation){
            return stringify((NameTypeDesignation)td);
        } else if (td instanceof TextualTypeDesignation){
            return stringify((TextualTypeDesignation)td);
        } else if (td instanceof SpecimenTypeDesignation){
            return stringify((SpecimenTypeDesignation)td, false);
        }else{
            throw new RuntimeException("Unknown TypeDesignation type");
        }
    }

    protected String stringify(TextualTypeDesignation td) {
        String result = td.getPreferredText(Language.DEFAULT());
        if (td.isVerbatim()){
            result = "\"" + result + "\"";  //TODO which character to use?
        }
        return result;
    }

    protected String stringify(NameTypeDesignation td) {

        StringBuffer sb = new StringBuffer();

        if(td.getTypeName() != null){
            sb.append(td.getTypeName().getTitleCache());
        }
//        if(td.getCitation() != null){
//            sb.append(" ").append(td.getCitation().getTitleCache());
//            if(td.getCitationMicroReference() != null){
//                sb.append(":").append(td.getCitationMicroReference());
//            }
//        }
        if(td.isNotDesignated()){
            sb.append(" not designated");
        }
        if(td.isRejectedType()){
            sb.append(" rejected");
        }
        if(td.isConservedType()){
            sb.append(" conserved");
        }
        return sb.toString();
    }

    private String stringify(SpecimenTypeDesignation td, boolean useFullTitleCache) {
        String  result = "";

        if(useFullTitleCache){
            if(td.getTypeSpecimen() != null){
                String nameTitleCache = td.getTypeSpecimen().getTitleCache();
                if(getTypifiedNameCache() != null){
                    nameTitleCache = nameTitleCache.replace(getTypifiedNameCache(), "");
                }
                result += nameTitleCache;
            }
        } else {
            if(td.getTypeSpecimen() != null){
                DerivedUnit du = td.getTypeSpecimen();
                if(du.isProtectedTitleCache()){
                    result += du.getTitleCache();
                } else {
                    du = HibernateProxyHelper.deproxy(du);
                    boolean isMediaSpecimen = du instanceof MediaSpecimen;
                    String typeSpecimenTitle = "";
                    if(isMediaSpecimen && HibernateProxyHelper.deproxyOrNull(du.getCollection()) == null) {
                        // special case of an published image which is not covered by the DerivedUnitFacadeCacheStrategy
                        MediaSpecimen msp = (MediaSpecimen)du;
                        if(msp.getMediaSpecimen() != null){
                            for(IdentifiableSource source : msp.getMediaSpecimen().getSources()){
                                String referenceStr = source.getCitation() == null? "": source.getCitation().getTitleCache();
                                String refDetailStr = source.getCitationMicroReference();
                                if(isNotBlank(refDetailStr)){
                                    typeSpecimenTitle += refDetailStr;
                                }
                                if(!typeSpecimenTitle.isEmpty() && !referenceStr.isEmpty()){
                                    typeSpecimenTitle += " in ";
                                }
                                typeSpecimenTitle += referenceStr + " ";
                            }
                        }
                    } else {
                        DerivedUnitFacadeCacheStrategy cacheStrategy = new DerivedUnitFacadeCacheStrategy();
                        String titleCache = cacheStrategy.getTitleCache(du, true, false);
                        // removing parentheses from code + accession number, see https://dev.e-taxonomy.eu/redmine/issues/8365
                        titleCache = titleCache.replaceAll("[\\(\\)]", "");
                        typeSpecimenTitle += titleCache;
                    }

                    result += (isMediaSpecimen ? "[icon] " : "") + typeSpecimenTitle.trim();
                }
            }
        }

        if(td.isNotDesignated()){
            result += " not designated";
        }

        return result;
    }

    private FieldUnit findFieldUnit(DerivedUnit du) {

        if(du == null || du.getOriginals() == null){
            return null;
        }
        @SuppressWarnings("rawtypes")
        Set<SpecimenOrObservationBase> originals = du.getDerivedFrom().getOriginals();
        @SuppressWarnings("rawtypes")
        Optional<SpecimenOrObservationBase> fieldUnit = originals.stream()
                .filter(original -> original instanceof FieldUnit).findFirst();
        if (fieldUnit.isPresent()) {
            return (FieldUnit) fieldUnit.get();
        } else {
            for (@SuppressWarnings("rawtypes")
            SpecimenOrObservationBase sob : originals) {
                if (sob instanceof DerivedUnit) {
                    FieldUnit fu = findFieldUnit((DerivedUnit) sob);
                    if (fu != null) {
                        return fu;
                    }
                }
            }
        }

        return null;
    }

    public String print(boolean withCitation, boolean withStartingTypeLabel, boolean withNameIfAvailable) {
        buildString(withCitation, withStartingTypeLabel, withNameIfAvailable);
        return finalString;
    }

    public List<TaggedText> toTaggedText(boolean withCitation, boolean withStartingTypeLabel, boolean withNameIfAvailable) {
        buildString(withCitation, withStartingTypeLabel, withNameIfAvailable);
        return taggedText;
    }

    public TaxonName getTypifiedName() {
        return typifiedName;
    }

    public void setNameTypeBaseEntityType(NameTypeBaseEntityType nameTypeBaseEntityType){
        this.nameTypeBaseEntityType = nameTypeBaseEntityType;
    }

    public NameTypeBaseEntityType getNameTypeBaseEntityType(){
        return nameTypeBaseEntityType;
    }

    /**
     * TypeDesignations which refer to the same FieldUnit (SpecimenTypeDesignation) or TaxonName
     * (NameTypeDesignation) form a working set. The <code>TypeDesignationWorkingSet</code> internally
     * works with EnityReferences to the actual TypeDesignations.
     *
     * The EntityReferences for TypeDesignations are grouped by the according TypeDesignationStatus.
     * The TypeDesignationStatusBase keys can be ordered by the term order defined in the vocabulary.
     *
     * A workingset can be referenced by the <code>baseEntityReference</code>.
     */
    public class TypeDesignationWorkingSet extends LinkedHashMap<TypeDesignationStatusBase<?>, Collection<TypedEntityReference>> {

        private static final long serialVersionUID = -1329007606500890729L;

        private String workingSetRepresentation = null;

        private TypedEntityReference<? extends VersionableEntity> baseEntityReference;

        private VersionableEntity baseEntity;

        public TypeDesignationWorkingSet(VersionableEntity baseEntity, TypedEntityReference<? extends VersionableEntity> baseEntityReference) {
            this.baseEntity = baseEntity;
            this.baseEntityReference = baseEntityReference;
        }

        public VersionableEntity getBaseEntity() {
            return baseEntity;
        }

        public List<TypedEntityReference> getTypeDesignations() {
            List<TypedEntityReference> typeDesignations = new ArrayList<>();
            this.values().forEach(typeDesignationReferences -> typeDesignationReferences.forEach(td -> typeDesignations.add(td)));
            return typeDesignations;
        }

        public void insert(TypeDesignationStatusBase<?> status, TypedEntityReference<?> typeDesignationEntityReference) {

            if(status == null){
                status = NULL_STATUS;
            }
            if(!containsKey(status)){
                put(status, new ArrayList<>());
            }
            get(status).add(typeDesignationEntityReference);
        }

        public String getRepresentation() {
            return workingSetRepresentation;
        }
        public void setRepresentation(String representation){
            this.workingSetRepresentation = representation;
        }

        /**
         * A reference to the entity which is the common base entity for all TypeDesignations in this workingset.
         * For a {@link SpecimenTypeDesignation} this is usually the {@link FieldUnit} if it is present. Otherwise it can also be
         * a {@link DerivedUnit} or something else depending on the specific use case.
         *
         * @return the baseEntityReference
         */
        public TypedEntityReference<? extends VersionableEntity> getBaseEntityReference() {
            return baseEntityReference;
        }

        @Override
        public String toString(){
            if(workingSetRepresentation != null){
                return workingSetRepresentation;
            } else {
                return super.toString();
            }
        }

        public boolean isSpecimenTypeDesigationWorkingSet() {
            return SpecimenOrObservationBase.class.isAssignableFrom(baseEntityReference.getType());
        }

        public TypeDesignationWorkingSetType getWorkingsetType() {
            return isSpecimenTypeDesigationWorkingSet() ? TypeDesignationWorkingSetType.SPECIMEN_TYPE_DESIGNATION_WORKINGSET : TypeDesignationWorkingSetType.NAME_TYPE_DESIGNATION_WORKINGSET;
        }
    }

    private boolean isNotBlank(String str){
        return StringUtils.isNotBlank(str);
    }

    public enum TypeDesignationWorkingSetType {
        SPECIMEN_TYPE_DESIGNATION_WORKINGSET,
        NAME_TYPE_DESIGNATION_WORKINGSET,
    }

    @SuppressWarnings({ "deprecation", "serial" })
    class NullTypeDesignationStatus extends TypeDesignationStatusBase<NullTypeDesignationStatus>{

        @Override
        public void resetTerms() {}

        @Override
        protected void setDefaultTerms(TermVocabulary<NullTypeDesignationStatus> termVocabulary) {}

        @Override
        public boolean hasDesignationSource() {
            return false;
        }
    }

    class DataIntegrityException extends Exception {

        private static final long serialVersionUID = 1464726696296824905L;

        public DataIntegrityException(String string) {
            super(string);
        }
    }
}
