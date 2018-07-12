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
import java.util.Arrays;
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

import org.hibernate.search.hcore.util.impl.HibernateHelper;

import eu.etaxonomy.cdm.api.facade.DerivedUnitFacadeCacheStrategy;
import eu.etaxonomy.cdm.api.service.exception.RegistrationValidationException;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.TermVocabulary;
import eu.etaxonomy.cdm.model.common.VersionableEntity;
import eu.etaxonomy.cdm.model.name.NameTypeDesignation;
import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignation;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.name.TypeDesignationBase;
import eu.etaxonomy.cdm.model.name.TypeDesignationStatusBase;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnit;
import eu.etaxonomy.cdm.model.occurrence.FieldUnit;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;
import eu.etaxonomy.cdm.ref.EntityReference;
import eu.etaxonomy.cdm.ref.TypedEntityReference;
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
 *
 */
public class TypeDesignationSetManager {

    enum NameTypeBaseEntityType{

        NAME_TYPE_DESIGNATION,
        TYPE_NAME;

    }

    private static final String TYPE_STATUS_SEPARATOR = "; ";

    private static final String TYPE_SEPARATOR = "; ";

    private static final String TYPE_DESIGNATION_SEPARATOR = ", ";

    private Collection<TypeDesignationBase> typeDesignations;

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

    private boolean printCitation = false;

    /**
     * @param containgEntity
     * @param taxonName
     * @throws RegistrationValidationException
     *
     */
    public TypeDesignationSetManager(Collection<TypeDesignationBase> typeDesignations) throws RegistrationValidationException {
        this.typeDesignations = typeDesignations;
        findTypifiedName();
        mapAndSort();
    }

    /**
     * @param typifiedName2
     */
    public TypeDesignationSetManager(TaxonName typifiedName) {
        this.typeDesignations = new ArrayList<>();
        this.typifiedNameRef = new EntityReference(typifiedName.getUuid(), typifiedName.getTitleCache());
    }

    /**
     * Add one or more TypeDesignations to the manager. This causes re-grouping and re-ordering
     * of all managed TypeDesignations.
     *
     * @param containgEntity
     * @param typeDesignations
     */
    public void addTypeDesigations(CdmBase containgEntity, TypeDesignationBase ... typeDesignations){
       this.typeDesignations.addAll(Arrays.asList(typeDesignations));
       mapAndSort();
    }

    /**
     * Groups and orders all managed TypeDesignations.
     *
     * @param containgEntity
     */
    protected void mapAndSort() {
        finalString = null;
        Map<TypedEntityReference, TypeDesignationWorkingSet> byBaseEntityByTypeStatus = new HashMap<>();
        this.typeDesignations.forEach(td -> mapTypeDesignation(byBaseEntityByTypeStatus, td));
        orderedByTypesByBaseEntity = orderByTypeByBaseEntity(byBaseEntityByTypeStatus);
    }


    /**
     * @param byBaseEntityByTypeStatus
     * @param td
     */
    private void mapTypeDesignation(Map<TypedEntityReference, TypeDesignationWorkingSet> byBaseEntityByTypeStatus,
            TypeDesignationBase<?> td){

        TypeDesignationStatusBase<?> status = td.getTypeStatus();

        try {
            final VersionableEntity baseEntity = baseEntity(td);
            final TypedEntityReference<VersionableEntity> baseEntityReference = makeEntityReference(baseEntity);

            EntityReference typeDesignationEntityReference = new EntityReference(td.getUuid(), stringify(td));

            TypeDesignationWorkingSet typedesignationWorkingSet;
            if(!byBaseEntityByTypeStatus.containsKey(baseEntityReference)){
                byBaseEntityByTypeStatus.put(baseEntityReference, new TypeDesignationWorkingSet(baseEntity, baseEntityReference));
            }

            typedesignationWorkingSet = byBaseEntityByTypeStatus.get(baseEntityReference);
            typedesignationWorkingSet.insert(status, typeDesignationEntityReference);
        } catch (DataIntegrityException e){
            problems.add(e.getMessage());
        }
    }

    /**
     * @param td
     * @return
     * @throws DataIntegrityException
     */
    protected VersionableEntity baseEntity(TypeDesignationBase<?> td) throws DataIntegrityException {

        VersionableEntity baseEntity = null;
        if(td  instanceof SpecimenTypeDesignation){
            SpecimenTypeDesignation std = (SpecimenTypeDesignation) td;
            FieldUnit fu = findFieldUnit(std);
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

    /**
     * @param td
     * @return
     */
    protected TypedEntityReference<VersionableEntity> makeEntityReference(VersionableEntity baseEntity) {

        baseEntity = (VersionableEntity) HibernateHelper.unproxy(baseEntity);
        String label = "";
        if(baseEntity  instanceof FieldUnit){
                label = ((FieldUnit)baseEntity).getTitleCache();
        }

        TypedEntityReference<VersionableEntity> baseEntityReference = new TypedEntityReference(baseEntity.getClass(), baseEntity.getUuid(), label);

        return baseEntityReference;
    }


    private LinkedHashMap<TypedEntityReference, TypeDesignationWorkingSet> orderByTypeByBaseEntity(
            Map<TypedEntityReference, TypeDesignationWorkingSet> stringsByTypeByBaseEntity){

       // order the FieldUnit TypeName keys
       List<TypedEntityReference> baseEntityKeyList = new LinkedList<>(stringsByTypeByBaseEntity.keySet());
       Collections.sort(baseEntityKeyList, new Comparator<TypedEntityReference>(){
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
        public int compare(TypedEntityReference o1, TypedEntityReference o2) {

            Class type1 = o1.getType();
            Class type2 = o2.getType();

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

    public TypeDesignationSetManager buildString(){

        if(finalString == null){

            finalString = "";
            if(getTypifiedNameCache() != null){
                finalString += getTypifiedNameCache() + " ";
            }

            int typeCount = 0;
            if(orderedByTypesByBaseEntity != null){
                for(TypedEntityReference baseEntityRef : orderedByTypesByBaseEntity.keySet()) {
                    StringBuilder sb = new StringBuilder();
                    if(typeCount++ > 0){
                        sb.append(TYPE_SEPARATOR);
                    }
                    boolean isNameTypeDesignation = false;
                    if(SpecimenOrObservationBase.class.isAssignableFrom(baseEntityRef.getType())){
                        sb.append("Type: ");
                    } else {
                        sb.append("NameType: ");
                        isNameTypeDesignation = true;
                    }
                    if(!baseEntityRef.getLabel().isEmpty()){
                        sb.append(baseEntityRef.getLabel()).append(" ");
                    }
                    TypeDesignationWorkingSet typeDesignationWorkingSet = orderedByTypesByBaseEntity.get(baseEntityRef);
                    if(!isNameTypeDesignation ){
                        sb.append("(");
                    }
                    int typeStatusCount = 0;
                    for(TypeDesignationStatusBase<?> typeStatus : typeDesignationWorkingSet.keySet()) {
                        if(typeStatusCount++  > 0){
                            sb.append(TYPE_STATUS_SEPARATOR);
                        }
                        boolean isPlural = typeDesignationWorkingSet.get(typeStatus).size() > 1;
                        if(!typeStatus.equals(NULL_STATUS)) {
                            sb.append(typeStatus.getLabel());
                            if(isPlural){
                                sb.append("s: ");
                            } else {
                                sb.append(", ");
                            }
                        }
                        int typeDesignationCount = 0;
                        for(EntityReference typeDesignationEntityReference : typeDesignationWorkingSet.get(typeStatus)) {
                            if(typeDesignationCount++  > 0){
                                sb.append(TYPE_DESIGNATION_SEPARATOR);
                            }
                            sb.append(typeDesignationEntityReference.getLabel());
                        }
                    }
                    if(!isNameTypeDesignation ){
                        sb.append(")");
                    }
                    typeDesignationWorkingSet.setRepresentation(sb.toString());
                    finalString += typeDesignationWorkingSet.getRepresentation();
                }
            }
        }
        return this;
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

        for(TypeDesignationBase<?> typeDesignation : typeDesignations){
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

    /**
     * @return
     */
    public Collection<TypeDesignationBase> getTypeDesignations() {
        return typeDesignations;
    }

    /**
     * @param ref
     * @return
     */
    public TypeDesignationBase findTypeDesignation(EntityReference typeDesignationRef) {
        for(TypeDesignationBase td : typeDesignations){
            if(td.getUuid().equals(typeDesignationRef.getUuid())){
                return td;
            }
        }
        // TODO Auto-generated method stub
        return null;
    }


    public LinkedHashMap<TypedEntityReference, TypeDesignationWorkingSet> getOrderdTypeDesignationWorkingSets() {
        return orderedByTypesByBaseEntity;
    }

    /**
     * @param td
     * @return
     */
    private String stringify(TypeDesignationBase td) {

        if(td instanceof NameTypeDesignation){
            return stringify((NameTypeDesignation)td);
        } else {
            return stringify((SpecimenTypeDesignation)td, false);
        }
    }


    /**
     * @param td
     * @return
     */
    protected String stringify(NameTypeDesignation td) {

        StringBuffer sb = new StringBuffer();

        if(td.getTypeName() != null){
            sb.append(td.getTypeName().getTitleCache());
        }
        if(td.getCitation() != null){
            sb.append(" ").append(td.getCitation().getTitleCache());
            if(td.getCitationMicroReference() != null){
                sb.append(":").append(td.getCitationMicroReference());
            }
        }
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

    /**
     * @param td
     * @return
     */
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
                    DerivedUnitFacadeCacheStrategy cacheStrategy = new DerivedUnitFacadeCacheStrategy();
                    result += cacheStrategy.getTitleCache(du, true);
                }
            }
        }

        if(isPrintCitation() && td.getCitation() != null){
            if(td.getCitation().getAbbrevTitle() != null){
                result += " " + td.getCitation().getAbbrevTitle();
            } else {
                result += " " + td.getCitation().getTitleCache();
            }
            if(td.getCitationMicroReference() != null){
                result += " :" + td.getCitationMicroReference();
            }
        }
        if(td.isNotDesignated()){
            result += " not designated";
        }

        return result;
    }

    /**
     * @param td
     * @return
     * @deprecated
     */
    @Deprecated
    private FieldUnit findFieldUnit(SpecimenTypeDesignation td) {

        DerivedUnit du = td.getTypeSpecimen();
        return findFieldUnit(du);
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

    public String print() {
        return finalString.trim();
    }

    /**
     * @return the printCitation
     */
    public boolean isPrintCitation() {
        return printCitation;
    }

    /**
     * @param printCitation the printCitation to set
     */
    public void setPrintCitation(boolean printCitation) {
        this.printCitation = printCitation;
    }

    /**
     * @return the typifiedName
     */
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
    public class TypeDesignationWorkingSet extends LinkedHashMap<TypeDesignationStatusBase<?>, Collection<EntityReference>> {

        private static final long serialVersionUID = -1329007606500890729L;

        String workingSetRepresentation = null;

        TypedEntityReference<VersionableEntity> baseEntityReference;

        VersionableEntity baseEntity;

        List<DerivedUnit> derivedUnits = null;

        /**
         * @param baseEntityReference
         */
        public TypeDesignationWorkingSet(VersionableEntity baseEntity, TypedEntityReference<VersionableEntity> baseEntityReference) {
            this.baseEntity = baseEntity;
            this.baseEntityReference = baseEntityReference;
        }

        /**
         * @return
         */
        public VersionableEntity getBaseEntity() {
            return baseEntity;
        }

        public List<EntityReference> getTypeDesignations() {
            List<EntityReference> typeDesignations = new ArrayList<>();
            this.values().forEach(typeDesignationReferences -> typeDesignationReferences.forEach(td -> typeDesignations.add(td)));
            return typeDesignations;
        }

        /**
         * @param status
         * @param typeDesignationEntityReference
         */
        public void insert(TypeDesignationStatusBase<?> status, EntityReference typeDesignationEntityReference) {

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
        public TypedEntityReference<VersionableEntity> getBaseEntityReference() {
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

        /**
         * @return
         */
        public boolean isSpecimenTypeDesigationWorkingSet() {
            return SpecimenOrObservationBase.class.isAssignableFrom(baseEntityReference.getType());
        }

        public TypeDesignationWorkingSetType getWorkingsetType() {
            return isSpecimenTypeDesigationWorkingSet() ? TypeDesignationWorkingSetType.SPECIMEN_TYPE_DESIGNATION_WORKINGSET : TypeDesignationWorkingSetType.NAME_TYPE_DESIGNATION_WORKINGSET;
        }

    }

    public enum TypeDesignationWorkingSetType {
        SPECIMEN_TYPE_DESIGNATION_WORKINGSET,
        NAME_TYPE_DESIGNATION_WORKINGSET,
    }

    @SuppressWarnings({ "deprecation", "serial" })
    class NullTypeDesignationStatus extends TypeDesignationStatusBase<NullTypeDesignationStatus>{

        /**
         * {@inheritDoc}
         */
        @Override
        public void resetTerms() {
            // empty

        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected void setDefaultTerms(TermVocabulary<NullTypeDesignationStatus> termVocabulary) {
            // empty
        }

    }

    class DataIntegrityException extends Exception {

        private static final long serialVersionUID = 1464726696296824905L;

        /**
         * @param string
         */
        public DataIntegrityException(String string) {
            super(string);
        }
    }
}
