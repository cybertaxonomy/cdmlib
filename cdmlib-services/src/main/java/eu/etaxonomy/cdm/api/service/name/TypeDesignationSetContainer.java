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

import eu.etaxonomy.cdm.api.service.dto.RegistrationDTO.RankedNameReference;
import eu.etaxonomy.cdm.api.service.exception.TypeDesignationSetException;
import eu.etaxonomy.cdm.api.service.name.TypeDesignationSetComparator.ORDER_BY;
import eu.etaxonomy.cdm.compare.name.TypeDesignationStatusComparator;
import eu.etaxonomy.cdm.hibernate.HibernateProxyHelper;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.common.VersionableEntity;
import eu.etaxonomy.cdm.model.name.HomotypicalGroup;
import eu.etaxonomy.cdm.model.name.NameTypeDesignation;
import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignation;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.name.TypeDesignationBase;
import eu.etaxonomy.cdm.model.name.TypeDesignationStatusBase;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnit;
import eu.etaxonomy.cdm.model.occurrence.FieldUnit;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;
import eu.etaxonomy.cdm.ref.TypedEntityReference;
import eu.etaxonomy.cdm.strategy.cache.HTMLTagRules;
import eu.etaxonomy.cdm.strategy.cache.TaggedTextBuilder;

/**
 * Container for of collection of {@link TypeDesignationBase type designations} for the same typified name.
 *
 * Type designations are ordered by the base type which is a {@link TaxonName} for {@link NameTypeDesignation name type designations} or
 * a {@link FieldUnit} in case of {@link SpecimenTypeDesignation specimen type designations}. The type designations per base type are furthermore ordered by the {@link TypeDesignationStatusBase}
 * (or a {@link DerivedUnit} if the field unit is missing).
 * <BR>
 * All type designations belonging to one base type are handled in a {@link TypeDesignationSet}.
 * <BR>
 * The {@link TypeDesignationSetContainer} can be formatted by using the {@link TypeDesignationSetFormatter}
 *
 * @author a.kohlbecker
 * @since Mar 10, 2017
 */
public class TypeDesignationSetContainer {

    //currently not really in use
    enum NameTypeBaseEntityType{
        NAME_TYPE_DESIGNATION,
        TYPE_NAME;
    }


    private NameTypeBaseEntityType nameTypeBaseEntityType = NameTypeBaseEntityType.NAME_TYPE_DESIGNATION;

    private Map<UUID,TypeDesignationBase<?>> typeDesignations = new HashMap<>();

    private TaxonName typifiedName;

    private Comparator<TypeDesignationSet> typeDesignationSetComparator = TypeDesignationSetComparator.INSTANCE();

    /**
     * Groups the EntityReferences for each of the TypeDesignations by the according TypeDesignationStatus.
     * The TypeDesignationStatusBase keys are already ordered by the term order defined in the vocabulary.
     */
    private LinkedHashMap<VersionableEntity,TypeDesignationSet> orderedByTypesByBaseEntity = new LinkedHashMap<>();

    private List<String> problems = new ArrayList<>();

// **************************** FACTORY ***************************************/

    public static TypeDesignationSetContainer NewDefaultInstance(@SuppressWarnings("rawtypes") Collection<TypeDesignationBase> typeDesignations)
            throws TypeDesignationSetException{
        return new TypeDesignationSetContainer(typeDesignations);
    }

    public static TypeDesignationSetContainer NewInstance(@SuppressWarnings("rawtypes") Collection<TypeDesignationBase> typeDesignations,
            TypeDesignationSetComparator.ORDER_BY orderBy)
            throws TypeDesignationSetException{
        TypeDesignationSetContainer result = new TypeDesignationSetContainer(typeDesignations, null, orderBy);
        return result;
    }

// **************************** CONSTRUCTOR ***********************************/

    private TypeDesignationSetContainer(@SuppressWarnings("rawtypes") Collection<TypeDesignationBase> typeDesignations)
            throws TypeDesignationSetException{
    	this(typeDesignations, null, null);
    }

    public TypeDesignationSetContainer(@SuppressWarnings("rawtypes") Collection<TypeDesignationBase> typeDesignations,
            TaxonName typifiedName, ORDER_BY orderBy)
            throws TypeDesignationSetException  {

        if (orderBy != null) {
            typeDesignationSetComparator = new TypeDesignationSetComparator(orderBy);
        }
        for (TypeDesignationBase<?> typeDes:typeDesignations){
            this.typeDesignations.put(typeDes.getUuid(), typeDes);
        }
        try {
        	findTypifiedName();
        }catch (TypeDesignationSetException e) {
        	if (typifiedName == null) {
        		throw e;
        	}
        	this.typifiedName = typifiedName;
        }

        mapAndSort();
    }

    public TypeDesignationSetContainer(HomotypicalGroup group) {
        for (TypeDesignationBase<?> typeDes: group.getTypeDesignations()){
            this.typeDesignations.put(typeDes.getUuid(), typeDes);
        }
        //findTypifiedName();
        mapAndSort();
    }

    public TypeDesignationSetContainer(TaxonName typifiedName) {
        this.typifiedName = typifiedName;
    }

// **************************************************************************/

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

    public TaxonName getTypifiedName() {
        return typifiedName;
    }

    public void setNameTypeBaseEntityType(NameTypeBaseEntityType nameTypeBaseEntityType){
        this.nameTypeBaseEntityType = nameTypeBaseEntityType;
    }

    public NameTypeBaseEntityType getNameTypeBaseEntityType(){
        return nameTypeBaseEntityType;
    }

// ******************************** METHODS *********************************/

    /**
     * Groups and orders all managed TypeDesignations.
     */
    protected void mapAndSort() {

        Map<VersionableEntity,TypeDesignationSet> byBaseEntityByTypeStatus = new HashMap<>();
        this.typeDesignations.values().forEach(td -> mapTypeDesignation(byBaseEntityByTypeStatus, td));
        orderedByTypesByBaseEntity = orderByTypeByBaseEntity(byBaseEntityByTypeStatus);
    }

    private void mapTypeDesignation(Map<VersionableEntity,TypeDesignationSet> byBaseEntityByTypeStatus,
            TypeDesignationBase<?> td){

        td = HibernateProxyHelper.deproxy(td);
        TypeDesignationStatusBase<?> status = td.getTypeStatus();

        try {
            VersionableEntity baseEntity = baseEntity(td);

            TaggedTextBuilder workingsetBuilder = new TaggedTextBuilder();
            boolean withCitation = true;
            TypeDesignationSetFormatter.buildTaggedTextForSingleType(td, withCitation, workingsetBuilder, 0);

            @SuppressWarnings({ "unchecked", "rawtypes" })
            TypeDesignationDTO<?> typeDesignationDTO
                = new TypeDesignationDTO(
                    td.getClass(),
                    td.getUuid(),
                    workingsetBuilder.getTaggedText(),
                    getTypeUuid(td));

            if(!byBaseEntityByTypeStatus.containsKey(baseEntity)){
                byBaseEntityByTypeStatus.put(baseEntity, new TypeDesignationSet(baseEntity));
            }
            byBaseEntityByTypeStatus.get(baseEntity).insert(status, typeDesignationDTO);

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

    //TODO maybe not needed anymore
    private static TypedEntityReference<? extends VersionableEntity> makeEntityReference(VersionableEntity baseEntity) {

        baseEntity = CdmBase.deproxy(baseEntity);
        String label = TypeDesignationSetFormatter.entityLabel(baseEntity);

        TypedEntityReference<? extends VersionableEntity> baseEntityReference =
                TypedEntityReference.fromEntityWithLabel(baseEntity, label);

        return baseEntityReference;
    }

    private LinkedHashMap<VersionableEntity,TypeDesignationSet> orderByTypeByBaseEntity(
            Map<VersionableEntity,TypeDesignationSet> stringsByTypeByBaseEntity){

       // order the FieldUnit TypeName keys
       Collection<TypeDesignationSet> entrySet
               = stringsByTypeByBaseEntity.values();
       LinkedList<TypeDesignationSet> baseEntityKeyList
               = new LinkedList<>(entrySet);
       Collections.sort(baseEntityKeyList, typeDesignationSetComparator);

       // new LinkedHashMap for the ordered FieldUnitOrTypeName keys
       LinkedHashMap<VersionableEntity,TypeDesignationSet> stringsOrderedbyBaseEntityOrderdByType
           = new LinkedHashMap<>(stringsByTypeByBaseEntity.size());

       for(TypeDesignationSet entry : baseEntityKeyList){
           VersionableEntity baseEntity = entry.getBaseEntity();
           TypeDesignationSet typeDesignationSet = stringsByTypeByBaseEntity.get(baseEntity);
           // order the TypeDesignationStatusBase keys
            List<TypeDesignationStatusBase<?>> keyList = new LinkedList<>(typeDesignationSet.keySet());
            Collections.sort(keyList, new TypeDesignationStatusComparator());
            // new LinkedHashMap for the ordered TypeDesignationStatusBase keys
            TypeDesignationSet orderedStringsByOrderedTypes = new TypeDesignationSet(
                    typeDesignationSet.getBaseEntity());
            keyList.forEach(key -> orderedStringsByOrderedTypes.put(key, typeDesignationSet.get(key)));
            stringsOrderedbyBaseEntityOrderdByType.put(baseEntity, orderedStringsByOrderedTypes);
        }

        return stringsOrderedbyBaseEntityOrderdByType;
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
                problems.add("Multiple typified names in type designation '" + typeDesignation.toString() + "'");
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
                    String message = "Multiple typified names [" + typifiedName.getTitleCache()+ "/" + typifiedName.getUuid() + " and "  + otherTypifiedName.getTitleCache() + "/" + otherTypifiedName.getUuid()  + "] in type designation set '" + typeDesignations.toString() + "'";
                    problems.add(message);
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

    public Map<VersionableEntity,TypeDesignationSet> getOrderedTypeDesignationSets() {
        return orderedByTypesByBaseEntity;
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

    public String print(boolean withCitation, boolean withStartingTypeLabel, boolean withNameIfAvailable) {
        return new TypeDesignationSetFormatter(withCitation, withStartingTypeLabel, withNameIfAvailable).format(this);
    }

    public String print(boolean withCitation, boolean withStartingTypeLabel, boolean withNameIfAvailable, HTMLTagRules htmlRules) {
        return new TypeDesignationSetFormatter(withCitation, withStartingTypeLabel, withNameIfAvailable).format(this, htmlRules);
    }


    class DataIntegrityException extends Exception {

        private static final long serialVersionUID = 1464726696296824905L;

        public DataIntegrityException(String string) {
            super(string);
        }
    }
}
