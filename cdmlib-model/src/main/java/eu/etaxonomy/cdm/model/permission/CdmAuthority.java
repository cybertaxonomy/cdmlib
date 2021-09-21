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
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;
import org.springframework.security.core.GrantedAuthority;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.jaxb.UUIDAdapter;
import eu.etaxonomy.cdm.model.term.DefinedTermBase;
import eu.etaxonomy.cdm.model.term.TermType;
import eu.etaxonomy.cdm.model.term.TermVocabulary;

/**
 * Model class for representing a structured CDM Authority<BR>.
 *
 * To be extended by implementations of {@link GrantedAuthority}.
 * that will replace {@link GrantedAuthorityImpl} in future version.
 *
 * It consists of 4 parts:<BR>
 * <ul>
 * <li>permission class: for defining a filter on a certain CDM class handled by this authority (obligatory)</li>
 * <li>property: a certain property in the above permission class (optional)</li>
 * <li>operations: the set of allowed operations from CRUD (obligatory)</li>
 * <li>target: for defining a filter on a certain instance of the permission class (optional)</li>
 * </ul>
 *
 * @see https://dev.e-taxonomy.eu/redmine/issues/7099
 *
 * @author a.mueller
 * @since 09.08.2019
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CdmAuthority", propOrder = {
        "permissionClass",
        "property",
        "targetUuid",
        "operations"}
)
@XmlRootElement(name = "CdmAuthority")
@Entity
public class CdmAuthority extends AuthorityBase {

    private static final long serialVersionUID = 3777547489226033333L;

    /**
     * The {@link TermType type} of this term. Needs to be the same type in a {@link DefinedTermBase defined term}
     * and in it's {@link TermVocabulary vocabulary}.
     */
    @XmlAttribute(name ="PermissionClass")
//    @NotNull  //AM: why did we remove NotNull here? For now ther permission class should be obligatory IMO
    @Type(type = "eu.etaxonomy.cdm.hibernate.EnumUserType",
        parameters = {@Parameter(name  = "enumClass", value = "eu.etaxonomy.cdm.model.permission.PermissionClass")}
    )
    private PermissionClass permissionClass;

    //Not always needed, therefore not NOTNULL
    private String property;

    @XmlAttribute(name ="Operations")
    @NotNull  //an empty operations set will result in "#"
    @Type(type = "eu.etaxonomy.cdm.hibernate.EnumSetUserType",
        parameters = {@Parameter(name = "enumClass", value = "eu.etaxonomy.cdm.model.permission.CRUD")}
    )
    private EnumSet<CRUD> operations = EnumSet.noneOf(CRUD.class);

    @XmlJavaTypeAdapter(UUIDAdapter.class)
    @Type(type="uuidUserType")
    @Column(length=36)  //TODO needed? Type UUID will always assure that is exactly 36
    private UUID targetUuid;

// *************************** Factory Methods ********************************/

    public static CdmAuthority NewInstance(PermissionClass permissionClass, String property, EnumSet<CRUD> operations,
            UUID targetUuid){
        return new CdmAuthority(permissionClass, property, operations, targetUuid);
    }

// *************************** CONSTRUCTOR ********************************/

    //for hibernate use only
    private CdmAuthority(){}

    private CdmAuthority(PermissionClass permissionClass, String property, EnumSet<CRUD> operations,
            UUID targetUuid) {
        super();
        this.permissionClass = permissionClass;
        this.property = property;
        setOperations(operations);
        this.targetUuid = targetUuid;
    }

// ********************** GETTER / SETTER **************************/

    public PermissionClass getPermissionClass() {
        return permissionClass;
    }
    public void setPermissionClass(PermissionClass permissionClass) {
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
        if (operations == null){  //just in case
            operations = EnumSet.noneOf(CRUD.class);
        }
        return operations;
    }
    public void setOperations(EnumSet<CRUD> operations) {
        if (operations == null){
            operations = EnumSet.noneOf(CRUD.class);
        }
        this.operations = operations;
    }
    public void addOperation(CRUD operation){
        this.operations.add(operation);
    }
    public void removeOperation(CRUD operation){
        this.operations.remove(operation);
    }

// *********************** STRING *****************************

    @Override
    public String toString() {
        return (permissionClass != null ? permissionClass : "-NO CLASS-")
             + (isNotBlank(property) ? "." + property : "")
             + "" + operations + ""
             + (targetUuid != null ? "{" + targetUuid + "}" : "");
    }



// ************************* CLONE *****************************/

    public boolean isEqual(Object obj) {
        if (this == obj) {
            return true;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        CdmAuthority other = (CdmAuthority) obj;
        if (permissionClass != other.permissionClass) {
            return false;
        }
        if (!operations.equals(other.operations)) {
            return false;
        }
        if (!CdmUtils.Nz(property).equals(CdmUtils.Nz(other.property))){
            return false;
        }
        if (targetUuid == null) {
            if (other.targetUuid != null) {
                return false;
            }
        } else if (!targetUuid.equals(other.targetUuid)) {
            return false;
        }
        return true;
    }

    @Override
    public CdmAuthority clone() throws CloneNotSupportedException {

        CdmAuthority result = (CdmAuthority)super.clone();
        result.operations = this.operations.clone();
        return result;
    }

}