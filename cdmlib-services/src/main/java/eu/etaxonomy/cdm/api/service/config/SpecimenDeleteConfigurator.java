/**
* Copyright (C) 2014 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.config;

import eu.etaxonomy.cdm.model.description.DescriptionBase;
import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignation;


/**
 * @author pplitzner
 \* @since Nov 10, 2014
 *
 */
public class SpecimenDeleteConfigurator extends DeleteConfiguratorBase {

    /**
     * If <code>true</code> all sub derivates of the specimen are deleted.
     */
    private boolean deleteChildren;
    /**
     * TODO: is this needed? For which use case?
     * Shifts the sub derivates of the deleted specimen to the parents of the
     * deleted specimen i.e. adds the sub derivates to the derivation event of
     * the parent
     */
//    private boolean shiftHierarchyUp;
    /**
     * If <code>true</code> the {@link SpecimenTypeDesignation} which
     * uses the specimen.
     */
    private boolean deleteFromTypeDesignation;
    /**
     * If <code>true</code> the
     */
    private boolean deleteFromIndividualsAssociation;
    /**
     * If <code>true</code> the specimen is deleted
     * from a {@link DescriptionBase} if it is set as "described" specimen.<br>
     */
    private boolean deleteFromDescription;

    /**
     * If <code>true</code> then attached molecular data like amplification results
     * and sequences are deleted
     */
    private boolean isDeleteMolecularData;

    public boolean isDeleteChildren() {
        return deleteChildren;
    }

    public void setDeleteChildren(boolean deleteChildren) {
        this.deleteChildren = deleteChildren;
    }

//    public boolean isShiftHierarchyUp() {
//        return shiftHierarchyUp;
//    }
//
//    public void setShiftHierarchyUp(boolean shiftHierarchyUp) {
//        this.shiftHierarchyUp = shiftHierarchyUp;
//    }

    public boolean isDeleteFromTypeDesignation() {
        return deleteFromTypeDesignation;
    }

    public void setDeleteFromTypeDesignation(boolean deleteFromTypeDesignation) {
        this.deleteFromTypeDesignation = deleteFromTypeDesignation;
    }

    public boolean isDeleteFromIndividualsAssociation() {
        return deleteFromIndividualsAssociation;
    }

    public void setDeleteFromIndividualsAssociation(boolean deleteFromIndividualsAssociation) {
        this.deleteFromIndividualsAssociation = deleteFromIndividualsAssociation;
    }

    public boolean isDeleteFromDescription() {
        return deleteFromDescription;
    }

    public void setDeleteFromDescription(boolean deleteFromDescription) {
        this.deleteFromDescription = deleteFromDescription;
    }

    public boolean isDeleteMolecularData() {
        return isDeleteMolecularData;
    }

    public void setDeleteMolecularData(boolean isDeleteMolecularData) {
        this.isDeleteMolecularData = isDeleteMolecularData;
    }

}
