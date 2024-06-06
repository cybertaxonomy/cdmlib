/**
* Copyright (C) 2023 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.format.common;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import eu.etaxonomy.cdm.model.common.ICdmBase;
import eu.etaxonomy.cdm.strategy.cache.TagEnum;

/**
 * @author a.mueller
 * @date 07.01.2023
 */
public class TypedLabel {

//    to make links possible
    private UUID uuid;
    private String clazz;

    //enum semantics   name_author, name_exauthor, separator ? => not needed if we pass all formatting rules from client to server
    private String label;
    private Boolean isNamePart;  //is italics??

    private TagEnum tagType;
    private List<TypedLabel> innerLabels = null;


    public TypedLabel(String label) {
        this(null, null, label, null);
    }

    public TypedLabel(TagEnum tagType, String label) {
        this(null, null, label, tagType);
    }

    public TypedLabel(ICdmBase cdmBase) {
        this(cdmBase.getUuid(), cdmBase.getClass(), null, null);
    }

    public TypedLabel(ICdmBase cdmBase, String label) {
        this(cdmBase.getUuid(), cdmBase.getClass(), label, null);
    }

    public TypedLabel(UUID uuid, Class<? extends ICdmBase> clazz, String label, TagEnum tagType) {
        this.uuid = uuid;
        this.clazz = clazz == null? null : clazz.getSimpleName();
        this.label = label;
        this.tagType = tagType;
    }

    public TypedLabel(UUID uuid, String classSimpleName, String label) {
        this.uuid = uuid;
        this.clazz = classSimpleName;
        this.label = label;
    }


//***************** GETTER / SETTER ********************************/

    public UUID getUuid() {
        return uuid;
    }
    //TODO remove?
    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public String getCdmClass() {
        return clazz;
    }
    //TODO make private? To force setting both, class and uuid
    public void setCdmClass(Class<? extends ICdmBase> cdmClass) {
        this.clazz = cdmClass.getSimpleName();
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

    public void setClassAndId(ICdmBase cdmBase) {
        this.uuid = cdmBase.getUuid();
        this.setCdmClass(cdmBase.getClass());
    }

    public List<TypedLabel> getInnerLabels() {
        return innerLabels;
    }

    public void addInnerLabel(TypedLabel innerLabel) {
        if (innerLabels == null) {
            innerLabels = new ArrayList<>();
        }
        innerLabels.add(innerLabel);
    }
    public void addInnerLabels(List<TypedLabel> innerLabels) {
        if (innerLabels == null) {
            innerLabels = new ArrayList<>();
        }
        innerLabels.addAll(innerLabels);
    }

    @Override
    public String toString() {
        return "TypedLabel [label=" + label + "]";
    }
}