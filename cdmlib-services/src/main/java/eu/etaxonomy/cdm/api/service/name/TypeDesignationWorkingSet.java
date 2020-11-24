/**
* Copyright (C) 2020 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.name;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import eu.etaxonomy.cdm.model.common.VersionableEntity;
import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignation;
import eu.etaxonomy.cdm.model.name.TypeDesignationStatusBase;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnit;
import eu.etaxonomy.cdm.model.occurrence.FieldUnit;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;
import eu.etaxonomy.cdm.model.term.TermVocabulary;
import eu.etaxonomy.cdm.ref.TypedEntityReference;

/**
 * TypeDesignations which refer to the same FieldUnit (SpecimenTypeDesignation) or TaxonName
 * (NameTypeDesignation) form a working set. The <code>TypeDesignationWorkingSet</code> internally
 * works with EnityReferences to the actual TypeDesignations.
 *
 * The EntityReferences for TypeDesignations are grouped by the according TypeDesignationStatus.
 * The TypeDesignationStatusBase keys can be ordered by the term order defined in the vocabulary.
 *
 * A workingset can be referenced by the <code>baseEntityReference</code>.
 *
 * @author a.kohlbecker
 * @since Mar 10, 2017
 */
public class TypeDesignationWorkingSet implements Serializable {

    private static final long serialVersionUID = -1329007606500890729L;

    private LinkedHashMap<TypeDesignationStatusBase<?>, Collection<TypedEntityReference>> internalMap
            = new LinkedHashMap<>();

    public static final NullTypeDesignationStatus NULL_STATUS = new NullTypeDesignationStatus();

    private String label = null;

    private TypedEntityReference<? extends VersionableEntity> baseEntityReference;

    private VersionableEntity baseEntity;

    public TypeDesignationWorkingSet(VersionableEntity baseEntity,
            TypedEntityReference<? extends VersionableEntity> baseEntityReference) {
        this.baseEntity = baseEntity;
        this.baseEntityReference = baseEntityReference;
    }

    public VersionableEntity getBaseEntity() {
        return baseEntity;
    }

    public Collection<TypedEntityReference> get(TypeDesignationStatusBase<?> typeStatus) {
        return internalMap.get(typeStatus);
    }

    public Set<TypeDesignationStatusBase<?>> keySet() {
        return internalMap.keySet();
    }

    public Collection<TypedEntityReference> put(TypeDesignationStatusBase<?> key, Collection<TypedEntityReference> collection) {
        return internalMap.put(key, collection);
    }

    public List<TypedEntityReference> getTypeDesignations() {
        List<TypedEntityReference> typeDesignations = new ArrayList<>();
        internalMap.values().forEach(typeDesignationReferences -> typeDesignationReferences.forEach(td -> typeDesignations.add(td)));
        return typeDesignations;
    }

    public void insert(TypeDesignationStatusBase<?> status, TypedEntityReference<?> typeDesignationEntityReference) {

        if(status == null){
            status = NULL_STATUS;
        }
        if(!internalMap.containsKey(status)){
            internalMap.put(status, new ArrayList<>());
        }
        internalMap.get(status).add(typeDesignationEntityReference);
    }

    public String getLabel() {
        return label;
    }
    public void setRepresentation(String representation){
        this.label = representation;
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

    public boolean isSpecimenTypeDesigationWorkingSet() {
        return SpecimenOrObservationBase.class.isAssignableFrom(baseEntity.getClass());
    }

    public TypeDesignationWorkingSetType getWorkingsetType() {
        return isSpecimenTypeDesigationWorkingSet() ? TypeDesignationWorkingSetType.SPECIMEN_TYPE_DESIGNATION_WORKINGSET : TypeDesignationWorkingSetType.NAME_TYPE_DESIGNATION_WORKINGSET;
    }

    @SuppressWarnings({ "deprecation", "serial" })
    static class NullTypeDesignationStatus extends TypeDesignationStatusBase<NullTypeDesignationStatus>{

        @Override
        public void resetTerms() {}

        @Override
        protected void setDefaultTerms(TermVocabulary<NullTypeDesignationStatus> termVocabulary) {}

        @Override
        public boolean hasDesignationSource() {
            return false;
        }
    }

    public enum TypeDesignationWorkingSetType {
        SPECIMEN_TYPE_DESIGNATION_WORKINGSET,
        NAME_TYPE_DESIGNATION_WORKINGSET,
    }

    @Override
    public String toString(){
        if(label != null){
            return label;
        } else {
            return super.toString();
        }
    }

}