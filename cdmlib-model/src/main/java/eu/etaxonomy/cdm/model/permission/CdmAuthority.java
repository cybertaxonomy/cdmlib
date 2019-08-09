/**
* Copyright (C) 2019 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.permission;

import java.util.EnumSet;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * CDM authority class.<BR>
 *
 * @see https://dev.e-taxonomy.eu/redmine/issues/7099
 *
 * @author a.mueller
 * @since 09.08.2019
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CdmAuthority", propOrder = {
        "property",
        "targetUuid"}
)
@XmlRootElement(name = "CdmAuthority")
@Entity
public class CdmAuthority extends AuthorityBase {

    private static final long serialVersionUID = 3777547489226033333L;

    private CdmPermissionClass permissionClass;

    @Column(unique = true)
    @NotNull
    private String property;

    private EnumSet<CRUD> operations = EnumSet.noneOf(CRUD.class);

    private UUID targetUuid;

// *************************** Factory Methods ********************************/

    public static CdmAuthority NewInstance(CdmPermissionClass permissionClass, String property, EnumSet<CRUD> operations,
            UUID targetUuid){
        return new CdmAuthority(permissionClass, property, operations, targetUuid);
    }

// *************************** CONSTRUCTOR ********************************/

    //for hibernate use only
    private CdmAuthority(){}

    private CdmAuthority(CdmPermissionClass permissionClass, String property, EnumSet<CRUD> operations,
            UUID targetUuid) {
        super();
        this.permissionClass = permissionClass;
        this.property = property;
        this.operations = operations;
        this.targetUuid = targetUuid;
    }


    // ********************** GETTER / SETTER **************************/

    public CdmPermissionClass getPermissionClass() {
        return permissionClass;
    }
    public void setPermissionClass(CdmPermissionClass permissionClass) {
        this.permissionClass = permissionClass;
    }

    public UUID getTargetUuid() {
        return targetUuid;
    }
    public void setTargetUuid(UUID targetUuid) {
        this.targetUuid = targetUuid;
    }

    public String getProperty() {
        return property;
    }
    public void setProperty(String property) {
        this.property = property;
    }

    public EnumSet<CRUD> getOperations() {
        return operations;
    }
    public void setOperations(EnumSet<CRUD> operations) {
        this.operations = operations;
    }
    public void addOperation(CRUD operation){
        this.operations.add(operation);
    }
    public void removeOperation(CRUD operation){
        this.operations.remove(operation);
    }

}
