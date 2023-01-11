/**
* Copyright (C) 2023 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.dto.portal;

import java.util.UUID;

import eu.etaxonomy.cdm.model.common.CdmBase;

/**
 * @author a.mueller
 * @date 07.01.2023
 */
public class TypedLabel {

    private UUID uuid;   //or id?
//    to make links possible
    //TODO remove model dependency?
    private Class<? extends CdmBase> cdmClass;
    //enum semantics   name_author, name_exauthor, separator ? => not needed if we pass all formatting rules from client to server
    private Boolean isNamePart;  //is italics??
    private String label;


    public UUID getUuid() {
        return uuid;
    }
    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public Class<? extends CdmBase> getCdmClass() {
        return cdmClass;
    }
    public void setCdmClass(Class<? extends CdmBase> cdmClass) {
        this.cdmClass = cdmClass;
    }

    public Boolean getIsNamePart() {
        return isNamePart;
    }
    public void setIsNamePart(Boolean isNamePart) {
        this.isNamePart = isNamePart;
    }

    public String getLabel() {
        return label;
    }
    public void setLabel(String label) {
        this.label = label;
    }

    public void setClassAndId(CdmBase cdmBase) {
        this.uuid = cdmBase.getUuid();
        this.cdmClass = cdmBase.getClass();
    }



}
