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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import eu.etaxonomy.cdm.api.dto.RegistrationDTO.RankedNameReference;
import eu.etaxonomy.cdm.api.service.exception.TypeDesignationSetException;
import eu.etaxonomy.cdm.api.service.name.TypeDesignationGroupComparator.ORDER_BY;
import eu.etaxonomy.cdm.compare.name.TypeDesignationStatusComparator;
import eu.etaxonomy.cdm.hibernate.HibernateProxyHelper;
import eu.etaxonomy.cdm.model.common.AnnotatableEntity;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
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
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;
import eu.etaxonomy.cdm.strategy.cache.HTMLTagRules;
import eu.etaxonomy.cdm.strategy.cache.TaggedTextBuilder;

/**
 * Container for or collection of {@link TypeDesignationBase type designations} for a single typified name.
 *
 * Type designations are ordered by the base type which is a {@link NameTypeDesignation} for
 * {@link NameTypeDesignation name type designations} or a {@link FieldUnit} in case of
 * {@link SpecimenTypeDesignation specimen type designations} (or a {@link DerivedUnit} if the
 * field unit is missing). For {@link TextualTypeDesignation}s it is the textualTypeDesignation itself.<BR>
 * The type designations per base type are furthermore ordered by the
 * {@link TypeDesignationStatusBase type designation status}.
 * <BR>
 * All type designations belonging to one base type are handled in a {@link TypeDesignationGroup}.
 * <BR>
 * The {@link TypeDesignationGroupContainer} can be formatted by using the {@link TypeDesignationGroupContainerFormatter}
 *
 * @author a.kohlbecker
 * @since Mar 10, 2017
 */
public class TypeDesignationGroupContainer {

    private static final Logger logger = LogManager.getLogger();

    //TODO remove FieldUnit workaround
    final static FieldUnit NOT_DESIGNATED = FieldUnit.NewInstance();

     private Map<UUID,TypeDesignationBase<?>> typeDesignations = new HashMap<>();

    private TaxonName typifiedName;

    private Comparator<TypeDesignationGroup> typeDesignationSetComparator = TypeDesignationGroupComparator.INSTANCE();

    /**
     * TODO is this documentation still valid?
     *
     * Groups the EntityReferences for each of the TypeDesignations by the
     * according TypeDesignationStatus.
     * The TypeDesignationStatusBase keys are already ordered by the term
     * order defined in the vocabulary.
     */
    private LinkedHashMap<VersionableEntity,TypeDesignationGroup> orderedBaseEntity2typeDesignationsMap = new LinkedHashMap<>();

    private List<String> problems = new ArrayList<>();

// **************************** FACTORY ***************************************/

    public static TypeDesignationGroupContainer NewDefaultInstance(
            @SuppressWarnings("rawtypes") Collection<? extends TypeDesignationBase> typeDesignations)
            throws TypeDesignationSetException{
        return new TypeDesignationGroupContainer(typeDesignations, null, null);
    }

    public static TypeDesignationGroupContainer NewInstance(
            @SuppressWarnings("rawtypes") Collection<TypeDesignationBase> typeDesignations,
            TypeDesignationGroupComparator.ORDER_BY orderBy)
            throws TypeDesignationSetException{
        TypeDesignationGroupContainer result = new TypeDesignationGroupContainer(typeDesignations, null, orderBy);
        return result;
    }

// **************************** CONSTRUCTOR ***********************************/

    //TODO make constructors protected

    public TypeDesignationGroupContainer(
            @SuppressWarnings("rawtypes") Collection<? extends TypeDesignationBase> typeDesignations,
            TaxonName typifiedName,
            ORDER_BY orderBy) throws TypeDesignationSetException  {

        if (orderBy != null) {
            typeDesignationSetComparator = new TypeDesignationGroupComparator(orderBy);
        }
        for (TypeDesignationBase<?> typeDes: typeDesignations){
            this.typeDesignations.put(typeDes.getUuid(), typeDes);
        }
        try {
        	if (typifiedName == null) {
        	    findTypifiedName();
        	}
        }catch (TypeDesignationSetException e) {
        	if (this.typifiedName == null) {
        		throw e;
        	}
        	this.typifiedName = typifiedName;
        }

        groupAndSort();
    }

    public TypeDesignationGroupContainer(HomotypicalGroup group) {
        for (TypeDesignationBase<?> typeDes: group.getTypeDesignations()){
            this.typeDesignations.put(typeDes.getUuid(), typeDes);
        }
        //findTypifiedName();
        groupAndSort();
    }
    public TypeDesignationGroupContainer(HomotypicalGroup group, boolean containsTypeStatements) {
        for (TypeDesignationBase<?> typeDes: group.getTypeDesignations()){
            if (!(typeDes instanceof TextualTypeDesignation) || (typeDes instanceof TextualTypeDesignation && containsTypeStatements) ) {
                this.typeDesignations.put(typeDes.getUuid(), typeDes);
            }
        }
        //findTypifiedName();
        groupAndSort();
    }

    public TypeDesignationGroupContainer(TaxonName typifiedName) {
        this.typifiedName = typifiedName;
    }

// **************************************************************************/

    /**
     * Add one or more TypeDesignations to the manager. This causes re-grouping and re-ordering
     * of all managed TypeDesignations.
     *
     * @param typeDesignations
     */
    public void addTypeDesigations(TypeDesignationBase<?> ... typeDesignations){
        for (TypeDesignationBase<?> typeDes: typeDesignations){
            this.typeDesignations.put(typeDes.getUuid(), typeDes);
        }
        groupAndSort();
    }

    public TaxonName getTypifiedName() {
        return typifiedName;
    }

// ******************************** METHODS *********************************/

    /**
     * Groups and orders all managed TypeDesignations.
     */
    protected void groupAndSort() {

        Map<VersionableEntity,TypeDesignationGroup> baseEntity2TypeDesignationsMap = new HashMap<>();
        this.typeDesignations.values().forEach(td -> addTypeDesignationToGroup(baseEntity2TypeDesignationsMap, td));

        orderedBaseEntity2typeDesignationsMap = orderBaseEntity2TypeDesignationsMap(baseEntity2TypeDesignationsMap);
    }

    private void addTypeDesignationToGroup(Map<VersionableEntity,TypeDesignationGroup> baseEntity2typeDesignationsMap,
            TypeDesignationBase<?> td){

        td = HibernateProxyHelper.deproxy(td);
        TypeDesignationStatusBase<?> status = td.getTypeStatus();

        try {
            AnnotatableEntity baseEntity = baseEntity(td);

            TaggedTextBuilder workingsetBuilder = new TaggedTextBuilder();
            boolean withCitation = true;
            TypeDesignationGroupContainerFormatter.buildTaggedTextForSingleType(td, withCitation, workingsetBuilder, 0);

            //use DTO to compute tagged text only once and make it comparable for sorting (via computed label)
            @SuppressWarnings({ "unchecked", "rawtypes" })
            TypeDesignationDTO<?> typeDesignationDTO
                = new TypeDesignationDTO(
                    td.getClass(),
                    td.getUuid(),
                    workingsetBuilder.getTaggedText(),
                    getTypeUuid(td));

            if(!baseEntity2typeDesignationsMap.containsKey(baseEntity)){
                baseEntity2typeDesignationsMap.put(baseEntity, new TypeDesignationGroup(baseEntity));
            }
            baseEntity2typeDesignationsMap.get(baseEntity).add(status, typeDesignationDTO);

        } catch (DataIntegrityException e){
            problems.add(e.getMessage());
        }
    }

    /**
     * Returns the uuid of the type designated by this {@link TypeDesignationDTO#}.
     * This is either a TaxonName or a {@link SpecimenOrObservationBase}.
     */
    private UUID getTypeUuid(TypeDesignationBase<?> td) {
        IdentifiableEntity<?> type;
        if (td instanceof SpecimenTypeDesignation){
            type = ((SpecimenTypeDesignation) td).getTypeSpecimen();
        }else if (td instanceof NameTypeDesignation){
            type = ((NameTypeDesignation) td).getTypeName();
        }else{
            type = null;
        }
        return type == null? null : type.getUuid();
    }

    protected AnnotatableEntity baseEntity(TypeDesignationBase<?> td) throws DataIntegrityException {

        AnnotatableEntity baseEntity = null;
        if(td instanceof SpecimenTypeDesignation){
            SpecimenTypeDesignation std = (SpecimenTypeDesignation) td;
            FieldUnit fu = findFieldUnit(std.getTypeSpecimen());
            if(fu != null){
                baseEntity = fu;
            } else if(((SpecimenTypeDesignation) td).getTypeSpecimen() != null){
                baseEntity = ((SpecimenTypeDesignation) td).getTypeSpecimen();
            } else if (td.isNotDesignated()) {
                baseEntity = NOT_DESIGNATED;
            }
        } else if(td instanceof NameTypeDesignation){
            baseEntity = td;
            // only other option is TaxonName
            //baseEntity = ((NameTypeDesignation)td).getTypeName();
        } else if (td instanceof TextualTypeDesignation) {
            baseEntity = td;
        }
        if(baseEntity == null) {
            throw new DataIntegrityException("Incomplete TypeDesignation, no type (specimen or name) found in " + td.toString());
        }
        return baseEntity;
    }

    private LinkedHashMap<VersionableEntity,TypeDesignationGroup> orderBaseEntity2TypeDesignationsMap(
            Map<VersionableEntity,TypeDesignationGroup> baseEntity2TypeDesignationsMap){

       // order the FieldUnit TypeName keys
       Collection<TypeDesignationGroup> typeDesignations
               = baseEntity2TypeDesignationsMap.values();
       LinkedList<TypeDesignationGroup> baseEntityKeyList
               = new LinkedList<>(typeDesignations);
       Collections.sort(baseEntityKeyList, typeDesignationSetComparator);

       // new LinkedHashMap for the ordered FieldUnitOrTypeName keys
       LinkedHashMap<VersionableEntity,TypeDesignationGroup> orderedBaseEntity2TypeDesignationsMap
           = new LinkedHashMap<>(baseEntity2TypeDesignationsMap.size());

       for(TypeDesignationGroup entry : baseEntityKeyList){
           VersionableEntity baseEntity = HibernateProxyHelper.deproxy(entry.getBaseEntity());
           TypeDesignationGroup typeDesignationGroup = baseEntity2TypeDesignationsMap.get(baseEntity);
           // order the TypeDesignationStatusBase keys
            List<TypeDesignationStatusBase<?>> keyList = new LinkedList<>(typeDesignationGroup.keySet());
            Collections.sort(keyList, new TypeDesignationStatusComparator());
            // new LinkedHashMap for the ordered TypeDesignationStatusBase keys
            TypeDesignationGroup orderedStringsByOrderedTypes = new TypeDesignationGroup(
                    typeDesignationGroup.getBaseEntity());
            keyList.forEach(key -> orderedStringsByOrderedTypes.put(key, typeDesignationGroup.get(key)));
            orderedBaseEntity2TypeDesignationsMap.put(baseEntity, orderedStringsByOrderedTypes);
        }

        return orderedBaseEntity2TypeDesignationsMap;
    }

    /**
     * FIXME use the validation framework validators to store the validation problems!!!
     *
     * @throws TypeDesignationSetException
     */
    private void findTypifiedName() throws TypeDesignationSetException {

        List<String> problems = new ArrayList<>();

        TaxonName typifiedName = null;

        for(TypeDesignationBase<?> typeDesignation : typeDesignations.values()){
            Set<TaxonName> typifiedNames = typeDesignation.getTypifiedNames();
            if(typifiedNames.isEmpty()){

                //TODO instead throw RegistrationValidationException()
                problems.add("Missing typified name in " + typeDesignation.toString());
                continue;
            }
            if(typifiedNames.size() > 1){
                //TODO instead throw RegistrationValidationException()
//                problems.add("Multiple typified names in type designation '" + typeDesignation.toString() + "'");

                //TODO it is possible that a type designation set has > 1 typified names. For name type designations
                //this is even relatively often the case (for specimen type designations it is a rare exception
                //or the types where stored not only at the type giving name but also at other names - happened
                //for Cichorieae at least)
                //=> we do not handle this as a problem
                //continue;
            }
            if(typifiedName == null){
                // remember
                typifiedName = typeDesignation.getTypifiedNames().iterator().next();
            } else {
                // compare
                TaxonName otherTypifiedName = typeDesignation.getTypifiedNames().iterator().next();
                if(!typifiedName.getUuid().equals(otherTypifiedName.getUuid())){
                    //TODO instead throw RegistrationValidationException()
                    String message = "Multiple typified names [" + typifiedName.getTitleCache()+ "/" + typifiedName.getUuid() + " and "  + otherTypifiedName.getTitleCache() + "/" + otherTypifiedName.getUuid()  + "] in type designation set '" + typeDesignations.toString() + "'";
                    logger.warn(message);
                    //TODO see comment on "typifiedNames.size() > 1" above
//                    problems.add(message);
                }
            }
        }
        if(!problems.isEmpty()){
            // FIXME use the validation framework
            throw new TypeDesignationSetException("Inconsistent type designations", problems);
        }

        if(typifiedName != null){
            // ON SUCCESS -------------------
            this.typifiedName = typifiedName;
        }
    }

    /**
     * @return the title cache of the typifying name or <code>null</code>
     */
    public String getTypifiedNameCache() {
        if(typifiedName != null){
            return typifiedName.getTitleCache();
        }
        return null;
    }

    /**
     * @return the title cache of the typifying name or <code>null</code>
     */
    public RankedNameReference getTypifiedNameAsEntityRef() {
       return new RankedNameReference(typifiedName.getUuid(), typifiedName.getTitleCache(), typifiedName.isSupraSpecific());
    }

    public Collection<TypeDesignationBase<?>> getTypeDesignations() {
        return typeDesignations.values();
    }

    public TypeDesignationBase<?> findTypeDesignation(UUID uuid) {
        return this.typeDesignations.get(uuid);
    }

    public Map<VersionableEntity,TypeDesignationGroup> getOrderedTypeDesignationSets() {
        return orderedBaseEntity2typeDesignationsMap;
    }

    private FieldUnit findFieldUnit(DerivedUnit du) {

        if(du == null || du.getOriginals() == null || du.getOriginals().isEmpty()){
            return null;
        }
        @SuppressWarnings("rawtypes")
        Set<SpecimenOrObservationBase> originals = du.getOriginals();
        @SuppressWarnings("rawtypes")
        Optional<SpecimenOrObservationBase> fieldUnit = originals.stream()
                .filter(original -> original instanceof FieldUnit).findFirst();
        if (fieldUnit.isPresent()) {
            return (FieldUnit) fieldUnit.get();
        } else {
            for (@SuppressWarnings("rawtypes") SpecimenOrObservationBase sob : originals) {
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

    public String print(boolean withCitation, boolean withStartingTypeLabel, boolean withNameIfAvailable, boolean withPrecedingMainType, boolean withAccessionNoType) {
        return new TypeDesignationGroupContainerFormatter(withCitation, withStartingTypeLabel, withNameIfAvailable, withPrecedingMainType, withAccessionNoType).format(this);
    }

    public String print(boolean withCitation, boolean withStartingTypeLabel, boolean withNameIfAvailable, boolean withPrecedingMainType, boolean withAccessionNoType, HTMLTagRules htmlRules) {
        return new TypeDesignationGroupContainerFormatter(withCitation, withStartingTypeLabel, withNameIfAvailable, withPrecedingMainType, withAccessionNoType).format(this, htmlRules);
    }


    class DataIntegrityException extends Exception {

        private static final long serialVersionUID = 1464726696296824905L;

        public DataIntegrityException(String string) {
            super(string);
        }
    }
}
