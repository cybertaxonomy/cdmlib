/**
* Copyright (C) 2016 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.dto;

import java.util.UUID;

/**
 * @author a.mueller
 * @date 05.07.2016
 *
 */
public class GroupedTaxonDTO {

    private UUID taxonUuid;

    private UUID groupTaxonUuid;

    private String groupTaxonName;

    /**
     * @return the taxonUuid
     */
    public UUID getTaxonUuid() {
        return taxonUuid;
    }

    /**
     * @param taxonUuid the taxonUuid to set
     */
    public void setTaxonUuid(UUID taxonUuid) {
        this.taxonUuid = taxonUuid;
    }

    /**
     * @return the groupTaxonUuid
     */
    public UUID getGroupTaxonUuid() {
        return groupTaxonUuid;
    }

    /**
     * @param groupTaxonUuid the groupTaxonUuid to set
     */
    public void setGroupTaxonUuid(UUID groupTaxonUuid) {
        this.groupTaxonUuid = groupTaxonUuid;
    }

    /**
     * @return the groupTaxonName
     */
    public String getGroupTaxonName() {
        return groupTaxonName;
    }

    /**
     * @param groupTaxonName the groupTaxonName to set
     */
    public void setGroupTaxonName(String groupTaxonName) {
        this.groupTaxonName = groupTaxonName;
    }

//*********************** toString() ***************************/
    @Override
    public String toString() {
        String result = "taxon:" + (taxonUuid == null? "-":taxonUuid.toString())
                + "; group:" + (groupTaxonUuid == null? "-":groupTaxonUuid.toString())
                + "; group name:" + (groupTaxonName == null? "-":groupTaxonName.toString());
        return result;
    }


}
