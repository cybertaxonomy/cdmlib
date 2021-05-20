/**
* Copyright (C) 2019 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.permission;

import javax.persistence.Entity;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Role class.<BR>
 *
 * @see https://dev.e-taxonomy.eu/redmine/issues/7099
 *
 * @author a.mueller
 * @since 09.08.2019
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Role", propOrder = {
        "role"
        }
)
@XmlRootElement(name = "CdmAuthority")
@Entity
public class Role extends AuthorityBase {

    private static final long serialVersionUID = -1897282896233162691L;

//    @Column(unique = true)
//    @NotNull
    private String role;

    // *************************** Factory Methods ********************************/

    public static Role NewInstance(String role){
        return new Role(role);
    }

    // *************************** CONSTRUCTOR ********************************/

    //for hibernate use only
    private Role(){}

    private Role(String role) {
        super();
        this.role = role;
    }

    // ********************** GETTER / SETTER **************************/

    public String getRole() {
        return role;
    }
    public void setRole(String role) {
        this.role = role;
    }

    //************************* STRING ***********************************/

    @Override
    public String toString() {
        return this.role;
    }
}