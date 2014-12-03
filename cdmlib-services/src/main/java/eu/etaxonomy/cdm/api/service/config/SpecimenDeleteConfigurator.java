// $Id$
/**
* Copyright (C) 2014 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.config;


/**
 * @author pplitzner
 * @date Nov 10, 2014
 *
 */
public class SpecimenDeleteConfigurator extends DeleteConfiguratorBase {

    /**
     * Deletes all sub derivates of the specimen
     */
    private boolean deleteChildren;
    /**
     * TODO: is this needed? For which use case?
     * Shifts the sub derivates of the deleted specimen to the parents of the
     * deleted specimen i.e. adds the sub derivates to the derivation event of
     * the parent
     */
//    private boolean shiftHierarchyUp;
    private boolean deleteFromTypeDesignation;
    private boolean deleteFromIndividualsAssociation;

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

}