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
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import eu.etaxonomy.cdm.compare.name.NullTypeDesignationStatus;
import eu.etaxonomy.cdm.model.common.VersionableEntity;
import eu.etaxonomy.cdm.model.name.TypeDesignationStatusBase;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;

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
public class TypeDesignationWorkingSet {

    public static final NullTypeDesignationStatus NULL_STATUS = NullTypeDesignationStatus.SINGLETON();

    private String label = null;

    private VersionableEntity baseEntity;

    private LinkedHashMap<TypeDesignationStatusBase<?>,Collection<TypeDesignationDTO>> designationByStatusMap = new LinkedHashMap<>();


    public enum TypeDesignationWorkingSetType {
        SPECIMEN_TYPE_DESIGNATION_WORKINGSET,
        NAME_TYPE_DESIGNATION_WORKINGSET;
        boolean isSpecimenType(){return this == SPECIMEN_TYPE_DESIGNATION_WORKINGSET;}
        boolean isNameType(){return this == NAME_TYPE_DESIGNATION_WORKINGSET;}
    }

// ********************************* CONSTRUCTOR **************************/

    public TypeDesignationWorkingSet(VersionableEntity baseEntity) {
        this.baseEntity = baseEntity;
    }

// ***********************************************************************/

    public VersionableEntity getBaseEntity() {
        return baseEntity;
    }

    public List<TypeDesignationDTO> getTypeDesignations() {
        List<TypeDesignationDTO> typeDesignations = new ArrayList<>();
        designationByStatusMap.values().forEach(typeDesignationDtos -> typeDesignationDtos.forEach(td -> typeDesignations.add(td)));
        return typeDesignations;
    }

    public Set<TypeDesignationStatusBase<?>> keySet() {
        return designationByStatusMap.keySet();
    }

    public Collection<TypeDesignationDTO> get(TypeDesignationStatusBase<?> typeStatus) {
        return designationByStatusMap.get(typeStatus);
    }

    public void insert(TypeDesignationStatusBase<?> status,
            TypeDesignationDTO<?> typeDesignationDto) {

        if(status == null){
            status = NULL_STATUS;
        }
        if(!designationByStatusMap.containsKey(status)){
            designationByStatusMap.put(status, new ArrayList<>());
        }
        designationByStatusMap.get(status).add(typeDesignationDto);
    }

    public Collection<TypeDesignationDTO> put(TypeDesignationStatusBase<?> status,
            Collection<TypeDesignationDTO> typeDesignationDtos) {
        if(status == null){
            status = NULL_STATUS;
        }
        return designationByStatusMap.put(status, typeDesignationDtos);
    }


    public String getLabel() {
        return label;
    }
    public void setRepresentation(String representation){
        this.label = representation;
    }

//TODO if not needed anymore
//    /**
//     * A reference to the entity which is the common base entity for all TypeDesignations in this workingset.
//     * For a {@link SpecimenTypeDesignation} this is usually the {@link FieldUnit} if it is present. Otherwise it can also be
//     * a {@link DerivedUnit} or something else depending on the specific use case.
//     *
//     * @return the baseEntityReference
//     */
//    public TypedEntityReference<? extends VersionableEntity> getBaseEntityReference() {
//        return baseEntityReference;
//    }

    public boolean isSpecimenWorkingSet() {
        return getWorkingsetType().isSpecimenType();
    }
    public boolean isNameWorkingSet() {
        return getWorkingsetType().isNameType();
    }

    private boolean isSpecimenTypeDesigationWorkingSet() {
        return SpecimenOrObservationBase.class.isAssignableFrom(baseEntity.getClass());
    }

    public TypeDesignationWorkingSetType getWorkingsetType() {
        return isSpecimenTypeDesigationWorkingSet() ? TypeDesignationWorkingSetType.SPECIMEN_TYPE_DESIGNATION_WORKINGSET : TypeDesignationWorkingSetType.NAME_TYPE_DESIGNATION_WORKINGSET;
    }

    /**
     * Uses the <code>comparator</code> to find the highest {@link TypeDesignationStatusBase} term and returns that.
     */
    public TypeDesignationStatusBase<?> highestTypeStatus(Comparator<TypeDesignationStatusBase<?>> comparator) {
        TypeDesignationStatusBase<?> highestTypeStatus = null;
        for(TypeDesignationStatusBase<?> status : designationByStatusMap.keySet()) {
            if(comparator.compare(status, highestTypeStatus) < 0){
                highestTypeStatus = status;
            }
        }
        return highestTypeStatus;
    }

// **************************** toString() ************************************

    @Override
    public String toString(){
        if(label != null){
            return label;
        } else {
            return super.toString();
        }
    }
}