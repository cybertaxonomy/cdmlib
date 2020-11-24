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

import eu.etaxonomy.cdm.api.facade.DerivedUnitFacadeCacheStrategy;
import eu.etaxonomy.cdm.api.service.exception.RegistrationValidationException;
import eu.etaxonomy.cdm.hibernate.HibernateProxyHelper;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
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
import eu.etaxonomy.cdm.ref.EntityReference;
import eu.etaxonomy.cdm.ref.TypedEntityReference;

/**
 * Manages a collection of {@link TypeDesignationBase type designations} for the same typified name.
 *
 * Type designations are ordered by the base type which is a {@link TaxonName} for {@link NameTypeDesignation NameTypeDesignations} or
 * in case of {@link SpecimenTypeDesignation SpecimenTypeDesignations} the  associate {@link FieldUnit} or the {@link DerivedUnit}
 * if the former is missing. The type designations per base type are furthermore ordered by the {@link TypeDesignationStatusBase}.
 *
 * The TypeDesignationSetManager also provides string representations of the whole ordered set of all
 * {@link TypeDesignationBase TypeDesignations} and of the TypeDesignationWorkingSets:
 * <ul>
 *  <li>{@link #print()}
 *  <li>{@link #getOrderedTypeDesignationWorkingSets()} ... {@link TypeDesignationWorkingSet#getLabel()}
 * </ul>
 * Prior using the representations you need to trigger their generation by calling {@link #buildString()}
 *
 * @author a.kohlbecker
 * @since Mar 10, 2017
 */
public class TypeDesignationSetManager {

    //currently not really in use
    enum NameTypeBaseEntityType{
        NAME_TYPE_DESIGNATION,
        TYPE_NAME;
    }

    private NameTypeBaseEntityType nameTypeBaseEntityType = NameTypeBaseEntityType.NAME_TYPE_DESIGNATION;

    private Map<UUID,TypeDesignationBase<?>> typeDesignations = new HashMap<>();

    private TaxonName typifiedName;

    /**
     * Groups the EntityReferences for each of the TypeDesignations by the according TypeDesignationStatus.
     * The TypeDesignationStatusBase keys are already ordered by the term order defined in the vocabulary.
     */
    private LinkedHashMap<TypedEntityReference<? extends VersionableEntity>, TypeDesignationWorkingSet> orderedByTypesByBaseEntity;

    private List<String> problems = new ArrayList<>();

// **************************** CONSTRUCTOR ***********************************/

    public TypeDesignationSetManager(@SuppressWarnings("rawtypes") Collection<TypeDesignationBase> typeDesignations)
            throws RegistrationValidationException{
    	this(typeDesignations, null);
    }

    public TypeDesignationSetManager(@SuppressWarnings("rawtypes") Collection<TypeDesignationBase> typeDesignations,
            TaxonName typifiedName)
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

        Map<TypedEntityReference<? extends VersionableEntity>, TypeDesignationWorkingSet> byBaseEntityByTypeStatus = new HashMap<>();

        this.typeDesignations.values().forEach(td -> mapTypeDesignation(byBaseEntityByTypeStatus, td));
        orderedByTypesByBaseEntity = orderByTypeByBaseEntity(byBaseEntityByTypeStatus);
    }

    private void mapTypeDesignation(Map<TypedEntityReference<? extends VersionableEntity>, TypeDesignationWorkingSet> byBaseEntityByTypeStatus,
            TypeDesignationBase<?> td){

        TypeDesignationStatusBase<?> status = td.getTypeStatus();

        try {
            final VersionableEntity baseEntity = baseEntity(td);
            final TypedEntityReference<? extends VersionableEntity> baseEntityReference = makeEntityReference(baseEntity);

            Class<? extends TypeDesignationBase> clazz = HibernateProxyHelper.deproxy((TypeDesignationBase)td).getClass();
            TypedEntityReference<TypeDesignationBase> typeDesignationEntityReference
            = new TypedEntityReference<>(
                    (Class<TypeDesignationBase>)clazz,
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

    protected static TypedEntityReference<? extends VersionableEntity> makeEntityReference(VersionableEntity baseEntity) {

        baseEntity = CdmBase.deproxy(baseEntity);
        String label = entityLabel(baseEntity);

        TypedEntityReference<? extends VersionableEntity> baseEntityReference =
                new TypedEntityReference<>(baseEntity.getClass(), baseEntity.getUuid(), label);

        return baseEntityReference;
    }

    private static String entityLabel(VersionableEntity baseEntity) {
        String label = "";
        if(baseEntity instanceof IdentifiableEntity<?>){
                label = ((IdentifiableEntity<?>)baseEntity).getTitleCache();
        }
        return label;
    }

    private LinkedHashMap<TypedEntityReference<? extends VersionableEntity>, TypeDesignationWorkingSet> orderByTypeByBaseEntity(
            Map<TypedEntityReference<? extends VersionableEntity>, TypeDesignationWorkingSet> stringsByTypeByBaseEntity){

       // order the FieldUnit TypeName keys
       List<TypedEntityReference<? extends VersionableEntity>> baseEntityKeyList = new LinkedList<>(stringsByTypeByBaseEntity.keySet());
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
            }}
       );

       // new LinkedHashMap for the ordered FieldUnitOrTypeName keys
       LinkedHashMap<TypedEntityReference<? extends VersionableEntity>, TypeDesignationWorkingSet> stringsOrderedbyBaseEntityOrderdByType
           = new LinkedHashMap<>(stringsByTypeByBaseEntity.size());

       for(TypedEntityReference<? extends VersionableEntity> baseEntityRef : baseEntityKeyList){

           TypeDesignationWorkingSet typeDesignationWorkingSet = stringsByTypeByBaseEntity.get(baseEntityRef);
           // order the TypeDesignationStatusBase keys
            List<TypeDesignationStatusBase<?>> keyList = new LinkedList<>(typeDesignationWorkingSet.keySet());
            Collections.sort(keyList, new TypeDesignationStatusComparator());
            // new LinkedHashMap for the ordered TypeDesignationStatusBase keys
            TypeDesignationWorkingSet orderedStringsByOrderedTypes = new TypeDesignationWorkingSet(
                    typeDesignationWorkingSet.getBaseEntity());
            keyList.forEach(key -> orderedStringsByOrderedTypes.put(key, typeDesignationWorkingSet.get(key)));
            stringsOrderedbyBaseEntityOrderdByType.put(baseEntityRef, orderedStringsByOrderedTypes);
       }

        return stringsOrderedbyBaseEntityOrderdByType;
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
    public EntityReference getTypifiedNameAsEntityRef() {
       return new EntityReference(typifiedName.getUuid(), typifiedName.getTitleCache());
    }

    public Collection<TypeDesignationBase<?>> getTypeDesignations() {
        return typeDesignations.values();
    }

    public TypeDesignationBase<?> findTypeDesignation(EntityReference typeDesignationRef) {
        return this.typeDesignations.get(typeDesignationRef.getUuid());
    }

    public LinkedHashMap<TypedEntityReference<? extends VersionableEntity>, TypeDesignationWorkingSet> getOrderedTypeDesignationWorkingSets() {
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

    private String stringify(TextualTypeDesignation td) {
        String result = td.getPreferredText(Language.DEFAULT());
        if (td.isVerbatim()){
            result = "\"" + result + "\"";  //TODO which character to use?
        }
        return result;
    }

    private String stringify(NameTypeDesignation td) {

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
        return new TypeDesignationSetFormatter(withCitation, withStartingTypeLabel, withNameIfAvailable).format(this);
    }

    private boolean isNotBlank(String str){
        return StringUtils.isNotBlank(str);
    }

    class DataIntegrityException extends Exception {

        private static final long serialVersionUID = 1464726696296824905L;

        public DataIntegrityException(String string) {
            super(string);
        }
    }
}
